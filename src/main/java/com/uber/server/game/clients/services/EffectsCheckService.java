package com.uber.server.game.clients.services;

import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for checking and updating avatar effects for clients.
 * Extracted from GameClientManager.
 */
public class EffectsCheckService {
    private static final Logger logger = LoggerFactory.getLogger(EffectsCheckService.class);
    
    /**
     * Checks and updates avatar effects for all clients.
     * @param clients Map of client IDs to GameClient instances
     */
    public void checkEffects(ConcurrentMap<Long, GameClient> clients) {
        if (clients == null) {
            return;
        }
        
        // Create a copy of keys to iterate safely
        List<Long> clientIds = new ArrayList<>(clients.keySet());
        
        for (Long clientId : clientIds) {
            GameClient client = clients.get(clientId);
            if (client == null || client.getHabbo() == null) {
                continue;
            }
            
            try {
                Habbo habbo = client.getHabbo();
                if (habbo.getAvatarEffectsInventoryComponent() != null) {
                    habbo.getAvatarEffectsInventoryComponent().checkExpired();
                }
            } catch (Exception e) {
                logger.debug("Error checking effects for client {}: {}", clientId, e.getMessage());
            }
        }
    }
}
