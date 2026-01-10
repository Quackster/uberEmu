package com.uber.server.game.users.badges;

import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.BadgeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages user badges.
 */
public class BadgeComponent {
    private static final Logger logger = LoggerFactory.getLogger(BadgeComponent.class);
    
    private final long userId;
    private final BadgeRepository badgeRepository;
    private final CopyOnWriteArrayList<Badge> badges;
    
    public BadgeComponent(long userId, BadgeRepository badgeRepository) {
        this.userId = userId;
        this.badgeRepository = badgeRepository;
        this.badges = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Gets the total number of badges.
     * @return Badge count
     */
    public int getCount() {
        return badges.size();
    }
    
    /**
     * Gets the number of equipped badges (badges with slot > 0).
     * @return Equipped badge count
     */
    public int getEquippedCount() {
        int count = 0;
        for (Badge badge : badges) {
            if (badge.getSlot() > 0) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Gets the list of all badges.
     * @return Badge list
     */
    public List<Badge> getBadgeList() {
        return new ArrayList<>(badges);
    }
    
    /**
     * Gets a badge by code.
     * @param code Badge code
     * @return Badge object, or null if not found
     */
    public Badge getBadge(String code) {
        if (code == null) {
            return null;
        }
        
        String lowerCode = code.toLowerCase();
        for (Badge badge : badges) {
            if (badge.getCode().toLowerCase().equals(lowerCode)) {
                return badge;
            }
        }
        return null;
    }
    
    /**
     * Checks if the user has a badge.
     * @param code Badge code
     * @return True if user has the badge, false otherwise
     */
    public boolean hasBadge(String code) {
        return getBadge(code) != null;
    }
    
    /**
     * Gives a badge to the user.
     * @param code Badge code
     * @param inDatabase If true, saves to database
     */
    public void giveBadge(String code, boolean inDatabase) {
        giveBadge(code, 0, inDatabase);
    }
    
    /**
     * Gives a badge to the user with a specific slot.
     * @param code Badge code
     * @param slot Badge slot (0 = unequipped)
     * @param inDatabase If true, saves to database
     */
    public void giveBadge(String code, int slot, boolean inDatabase) {
        if (hasBadge(code)) {
            return;
        }
        
        if (inDatabase) {
            if (!badgeRepository.addBadge(userId, code, slot)) {
                logger.error("Failed to add badge {} to user {} in database", code, userId);
                return;
            }
        }
        
        badges.add(new Badge(code, slot));
    }
    
    /**
     * Sets the slot for a badge.
     * @param code Badge code
     * @param slot New slot number
     */
    public void setBadgeSlot(String code, int slot) {
        Badge badge = getBadge(code);
        if (badge == null) {
            return;
        }
        
        badge.setSlot(slot);
    }
    
    /**
     * Resets all badge slots to 0 (unequips all badges).
     */
    public void resetSlots() {
        for (Badge badge : badges) {
            badge.setSlot(0);
        }
        
        badgeRepository.resetBadgeSlots(userId);
    }
    
    /**
     * Removes a badge from the user.
     * @param code Badge code
     */
    public void removeBadge(String code) {
        if (!hasBadge(code)) {
            return;
        }
        
        Badge badge = getBadge(code);
        if (badge == null) {
            return;
        }
        
        if (!badgeRepository.removeBadge(userId, code)) {
            logger.error("Failed to remove badge {} from user {} in database", code, userId);
            return;
        }
        
        badges.remove(badge);
    }
    
    /**
     * Loads badges from the database.
     */
    public void loadBadges() {
        badges.clear();
        
        List<Map<String, Object>> badgeData = badgeRepository.loadBadges(userId);
        for (Map<String, Object> row : badgeData) {
            try {
                String badgeId = (String) row.get("badge_id");
                int slot = ((Number) row.get("badge_slot")).intValue();
                giveBadge(badgeId, slot, false); // Don't save to database, already loaded
            } catch (Exception e) {
                logger.error("Failed to load badge: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Serializes badges to a ServerMessage.
     * @return ServerMessage with badge data (ID 229)
     */
    public ServerMessage serialize() {
        List<Badge> equippedBadges = new ArrayList<>();
        
        ServerMessage message = new ServerMessage(229);
        message.appendInt32(getCount());
        
        for (Badge badge : badges) {
            message.appendStringWithBreak(badge.getCode());
            
            if (badge.getSlot() > 0) {
                equippedBadges.add(badge);
            }
        }
        
        message.appendInt32(equippedBadges.size());
        
        for (Badge badge : equippedBadges) {
            message.appendInt32(badge.getSlot());
            message.appendStringWithBreak(badge.getCode());
        }
        
        return message;
    }
}
