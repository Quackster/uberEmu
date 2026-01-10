package com.uber.server.game.clients.services;

import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for broadcasting messages to clients.
 * Extracted from GameClientManager.
 */
public class BroadcastService {
    private static final Logger logger = LoggerFactory.getLogger(BroadcastService.class);
    
    /**
     * Broadcasts a message to clients with optional fuse requirement.
     * @param clients Map of client IDs to GameClient instances
     * @param message Message to broadcast
     * @param fuseRequirement Fuse requirement (null = no requirement)
     */
    public void broadcastMessage(ConcurrentMap<Long, GameClient> clients, 
                                ServerMessage message, 
                                String fuseRequirement) {
        if (message == null || clients == null) {
            return;
        }
        
        // Create a copy of keys to iterate safely
        List<Long> clientIds = new ArrayList<>(clients.keySet());
        
        for (Long clientId : clientIds) {
            GameClient client = clients.get(clientId);
            if (client == null) {
                continue;
            }
            
            try {
                // Check fuse requirement if specified
                if (fuseRequirement != null && !fuseRequirement.isEmpty()) {
                    Habbo habbo = client.getHabbo();
                    if (habbo == null || !habbo.hasFuse(fuseRequirement)) {
                        continue;
                    }
                }
                
                client.sendMessage(message);
            } catch (Exception e) {
                logger.debug("Error broadcasting message to client {}: {}", clientId, e.getMessage());
            }
        }
    }
    
    /**
     * Broadcasts a message to all clients.
     * @param clients Map of client IDs to GameClient instances
     * @param message Message to broadcast
     */
    public void broadcastMessage(ConcurrentMap<Long, GameClient> clients, ServerMessage message) {
        broadcastMessage(clients, message, null);
    }
}
