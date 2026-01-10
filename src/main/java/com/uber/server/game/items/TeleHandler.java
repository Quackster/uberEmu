package com.uber.server.game.items;

import com.uber.server.game.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for handling teleport links.
 */
public class TeleHandler {
    private static final Logger logger = LoggerFactory.getLogger(TeleHandler.class);
    
    /**
     * Gets the linked teleport ID for a teleport.
     * @param teleId Teleport item ID
     * @param game Game instance for accessing repository
     * @return Linked teleport ID, or 0 if not found
     */
    public static long getLinkedTele(long teleId, Game game) {
        if (game == null || game.getRoomItemRepository() == null) {
            return 0;
        }
        
        try {
            Long linkedId = game.getRoomItemRepository().getLinkedTele(teleId);
            return linkedId != null ? linkedId : 0;
        } catch (Exception e) {
            logger.error("Failed to get linked tele for {}: {}", teleId, e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Gets the room ID for a teleport item.
     * @param teleId Teleport item ID
     * @param game Game instance for accessing repository
     * @return Room ID, or 0 if not found
     */
    public static long getTeleRoomId(long teleId, Game game) {
        if (game == null || game.getRoomItemRepository() == null) {
            return 0;
        }
        
        try {
            Long roomId = game.getRoomItemRepository().getTeleRoomId(teleId);
            return roomId != null ? roomId : 0;
        } catch (Exception e) {
            logger.error("Failed to get tele room ID for {}: {}", teleId, e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Checks if a teleport is linked.
     * @param teleId Teleport item ID
     * @param game Game instance for accessing repository
     * @return True if teleport is linked
     */
    public static boolean isTeleLinked(long teleId, Game game) {
        long linkId = getLinkedTele(teleId, game);
        if (linkId == 0) {
            return false;
        }
        
        long roomId = getTeleRoomId(linkId, game);
        return roomId != 0;
    }
}
