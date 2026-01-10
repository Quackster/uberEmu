package com.uber.server.game.support;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.repository.ModerationBanRepository;
import com.uber.server.repository.UserInfoRepository;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages moderation bans.
 */
public class ModerationBanManager {
    private static final Logger logger = LoggerFactory.getLogger(ModerationBanManager.class);
    private final List<ModerationBan> bans;
    private final ModerationBanRepository banRepository;
    private final UserInfoRepository userInfoRepository;
    private final Game game;
    
    public ModerationBanManager(ModerationBanRepository banRepository, 
                               UserInfoRepository userInfoRepository,
                               Game game) {
        this.bans = new CopyOnWriteArrayList<>();
        this.banRepository = banRepository;
        this.userInfoRepository = userInfoRepository;
        this.game = game;
    }
    
    /**
     * Loads all active bans from the database.
     */
    public void loadBans() {
        bans.clear();
        
        List<Map<String, Object>> banData = banRepository.loadBans();
        for (Map<String, Object> row : banData) {
            try {
                String banTypeStr = (String) row.get("bantype");
                ModerationBanType type = ModerationBanType.IP;
                
                if ("user".equals(banTypeStr)) {
                    type = ModerationBanType.USERNAME;
                }
                
                String value = (String) row.get("value");
                String reason = (String) row.get("reason");
                long expire = ((Number) row.get("expire")).longValue();
                
                ModerationBan ban = new ModerationBan(type, value, reason, expire);
                bans.add(ban);
            } catch (Exception e) {
                logger.error("Failed to load ban: {}", e.getMessage(), e);
            }
        }
        
        logger.info("Loaded {} active bans", bans.size());
    }
    
    /**
     * Checks for ban conflicts for a client and throws ModerationBanException if banned.
     * @param client GameClient to check
     * @throws ModerationBanException if client is banned
     */
    public void checkForBanConflicts(GameClient client) throws ModerationBanException {
        if (client == null || client.getConnection() == null) {
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
     * @return True if banned, false otherwise
     */
    public boolean isBanned(String ipAddress, String username) {
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
    
    /**
     * Bans a user.
     * @param client GameClient to ban
     * @param moderator Moderator username
     * @param lengthSeconds Ban length in seconds
     * @param reason Ban reason
     * @param ipBan If true, bans by IP; otherwise bans by username
     */
    public void banUser(GameClient client, String moderator, long lengthSeconds, 
                       String reason, boolean ipBan) {
        if (client == null || client.getHabbo() == null) {
            return;
        }
        
        ModerationBanType type = ModerationBanType.USERNAME;
        String var = client.getHabbo().getUsername();
        String rawVar = "user";
        long expire = TimeUtil.getUnixTimestamp() + lengthSeconds;
        
        if (ipBan) {
            type = ModerationBanType.IP;
            var = client.getConnection() != null ? client.getConnection().getIPAddress() : "";
            rawVar = "ip";
        }
        
        // Create ban object
        ModerationBan ban = new ModerationBan(type, var, reason, expire);
        bans.add(ban);
        
        // Get moderator user ID
        long moderatorId = 0;
        if (moderator != null && !moderator.isEmpty() && game != null && game.getUserRepository() != null) {
            moderatorId = game.getUserRepository().getUserIdByUsername(moderator);
        }
        
        // Create ban in database
        LocalDateTime now = LocalDateTime.now();
        String addedDate = now.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        
        if (!banRepository.createBan(rawVar, var, reason, expire, moderatorId, addedDate)) {
            logger.error("Failed to create ban in database");
            bans.remove(ban);
            return;
        }
        
        // Update ban count for affected users
        if (ipBan) {
            // Update ban count for all users with this IP
            List<Long> affectedUserIds = banRepository.getUsersByIp(var);
            for (Long userId : affectedUserIds) {
                userInfoRepository.incrementBanCount(userId);
            }
        } else {
            // Update ban count for the specific user
            userInfoRepository.incrementBanCount(client.getHabbo().getId());
        }
        
        // Send ban message and disconnect
        client.sendNotif("You have been banned: " + reason);
        client.disconnect();
    }
}
