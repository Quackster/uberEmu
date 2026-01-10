package com.uber.server.game.clients.services;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.GameEnvironment;
import com.uber.server.game.clients.PixelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for checking and updating pixel (activity points) for clients.
 * Extracted from GameClientManager.
 */
public class PixelUpdateService {
    private static final Logger logger = LoggerFactory.getLogger(PixelUpdateService.class);
    
    /**
     * Checks and updates pixel (activity points) for all clients.
     * @param clients Map of client IDs to GameClient instances
     */
    public void checkPixelUpdates(ConcurrentMap<Long, GameClient> clients) {
        if (clients == null) {
            return;
        }
        
        Game game = null;
        try {
            game = GameEnvironment.getInstance().getGame();
        } catch (Exception e) {
            logger.warn("Could not get Game instance for pixel updates: {}", e.getMessage());
            return;
        }
        
        if (game == null || game.getPixelManager() == null) {
            return;
        }
        
        PixelManager pixelManager = game.getPixelManager();
        
        // Create a copy of keys to iterate safely
        List<Long> clientIds = new ArrayList<>(clients.keySet());
        
        for (Long clientId : clientIds) {
            GameClient client = clients.get(clientId);
            if (client == null || client.getHabbo() == null) {
                continue;
            }
            
            try {
                if (pixelManager.needsUpdate(client)) {
                    pixelManager.givePixels(client);
                }
            } catch (Exception e) {
                logger.debug("Error updating pixels for client {}: {}", clientId, e.getMessage());
            }
        }
    }
}
