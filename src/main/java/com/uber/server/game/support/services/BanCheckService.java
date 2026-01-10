package com.uber.server.game.support.services;

import com.uber.server.game.GameClient;
import com.uber.server.game.support.ModerationBan;
import com.uber.server.game.support.ModerationBanException;
import com.uber.server.game.support.ModerationBanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service for checking if users are banned.
 * Extracted from ModerationBanManager.
 */
public class BanCheckService {
    private static final Logger logger = LoggerFactory.getLogger(BanCheckService.class);
    
    /**
     * Checks for ban conflicts for a client and throws ModerationBanException if banned.
     * @param client GameClient to check
     * @param bans List of active bans
     * @throws ModerationBanException if client is banned
     */
    public void checkForBanConflicts(GameClient client, List<ModerationBan> bans) throws ModerationBanException {
        if (client == null || client.getConnection() == null || bans == null) {
            return;
        }
        
        String ipAddress = client.getConnection().getIPAddress();
        
        for (ModerationBan ban : bans) {
            if (ban.isExpired()) {
                continue;
            }
            
            // Check IP ban
            if (ban.getType() == ModerationBanType.IP && ipAddress.equals(ban.getVariable())) {
                throw new ModerationBanException(ban.getReasonMessage());
            }
            
            // Check username ban
            if (client.getHabbo() != null) {
                if (ban.getType() == ModerationBanType.USERNAME && 
                    client.getHabbo().getUsername().toLowerCase().equals(ban.getVariable().toLowerCase())) {
                    throw new ModerationBanException(ban.getReasonMessage());
                }
            }
        }
    }
    
    /**
     * Checks if a user is banned.
     * @param ipAddress IP address to check
     * @param username Username to check (can be null)
     * @param bans List of active bans
     * @return True if banned, false otherwise
     */
    public boolean isBanned(String ipAddress, String username, List<ModerationBan> bans) {
        if (bans == null) {
            return false;
        }
        
        if (ipAddress == null) {
            ipAddress = "";
        }
        if (username == null) {
            username = "";
        }
        
        String lowerUsername = username.toLowerCase();
        
        for (ModerationBan ban : bans) {
            if (ban.isExpired()) {
                continue;
            }
            
            if (ban.getType() == ModerationBanType.IP && ipAddress.equals(ban.getVariable())) {
                return true;
            }
            
            if (ban.getType() == ModerationBanType.USERNAME && 
                lowerUsername.equals(ban.getVariable().toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
}
