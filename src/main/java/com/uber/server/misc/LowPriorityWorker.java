package com.uber.server.misc;

import com.uber.server.game.Game;
import com.uber.server.game.GameEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Low priority background worker thread for maintenance tasks.
 */
public class LowPriorityWorker {
    private static final Logger logger = LoggerFactory.getLogger(LowPriorityWorker.class);
    
    /**
     * Processes low priority background tasks in a loop.
     * Should be run in a separate thread.
     */
    public static void process() {
        // Allow 10 seconds for server to finish initialization
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        
        Game game = null;
        try {
            game = GameEnvironment.getInstance().getGame();
        } catch (Exception e) {
            logger.error("Could not get Game instance: {}", e.getMessage(), e);
            return;
        }
        
        if (game == null) {
            logger.error("Game instance is null, cannot start LowPriorityWorker");
            return;
        }
        
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Garbage Collection
                System.gc();
                
                // Statistics
                int status = 1;
                // Note: usersOnline and roomsLoaded are calculated but not stored in server_status table
                // The updateServerStatus method only updates users table online status.
                
                // Update server status in database
                if (game.getUserRepository() != null) {
                    game.getUserRepository().updateServerStatus(status);
                }
                
                // Effects
                if (game.getClientManager() != null) {
                    game.getClientManager().checkEffects();
                }
                
                // Sleep for 30 seconds
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error in LowPriorityWorker: {}", e.getMessage(), e);
                // Continue processing even if there's an error
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        logger.info("LowPriorityWorker thread stopped");
    }
}
