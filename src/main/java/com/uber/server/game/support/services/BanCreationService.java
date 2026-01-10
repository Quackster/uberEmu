package com.uber.server.game.support.services;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.support.ModerationBan;
import com.uber.server.game.support.ModerationBanType;
import com.uber.server.repository.ModerationBanRepository;
import com.uber.server.repository.UserInfoRepository;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for creating bans.
 * Extracted from ModerationBanManager.
 */
public class BanCreationService {
    private static final Logger logger = LoggerFactory.getLogger(BanCreationService.class);
    
    private final ModerationBanRepository banRepository;
    private final UserInfoRepository userInfoRepository;
    private final Game game;
    
    public BanCreationService(ModerationBanRepository banRepository,
                             UserInfoRepository userInfoRepository,
                             Game game) {
        this.banRepository = banRepository;
        this.userInfoRepository = userInfoRepository;
        this.game = game;
    }
    
    /**
     * Bans a user.
     * @param client GameClient to ban
     * @param moderator Moderator username
     * @param lengthSeconds Ban length in seconds
     * @param reason Ban reason
     * @param ipBan If true, bans by IP; otherwise bans by username
     * @param bans List to add the ban to
     */
    public void banUser(GameClient client, String moderator, long lengthSeconds, 
                       String reason, boolean ipBan, List<ModerationBan> bans) {
        if (client == null || client.getHabbo() == null || bans == null) {
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
