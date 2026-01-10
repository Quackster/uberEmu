package com.uber.server.game.clients;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.GameEnvironment;
import com.uber.server.game.Habbo;
import com.uber.server.game.threading.GameThreadPool;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages activity points (pixels) for users.
 * Uses shared thread pool from GameThreadPool.
 */
public class PixelManager {
    private static final Logger logger = LoggerFactory.getLogger(PixelManager.class);
    
    private static final int RCV_EVERY_MINS = 15;
    private static final int RCV_AMOUNT = 50;
    
    private ScheduledFuture<?> processTask;
    
    public PixelManager() {
        // No initialization needed - will use shared thread pool
    }
    
    /**
     * Starts the pixel manager worker.
     * Uses shared thread pool from GameThreadPool.
     */
    public void start() {
        if (processTask != null && !processTask.isCancelled()) {
            return; // Already running
        }
        
        ScheduledExecutorService executor = GameThreadPool.getInstance().getGameExecutor();
        
        // Schedule pixel updates every 15 seconds
        processTask = executor.scheduleWithFixedDelay(() -> {
            try {
                Game game = null;
                try {
                    game = GameEnvironment.getInstance().getGame();
                } catch (Exception e) {
                    logger.error("Could not get Game instance: {}", e.getMessage(), e);
                    return;
                }
                
                if (game != null && game.getClientManager() != null) {
                    game.getClientManager().checkPixelUpdates();
                }
            } catch (Exception e) {
                logger.error("Error in PixelManager: {}", e.getMessage(), e);
            }
        }, 0, 15, TimeUnit.SECONDS);
        
        logger.info("PixelManager started");
    }
    
    /**
     * Stops the pixel manager worker.
     */
    public void stop() {
        if (processTask != null) {
            processTask.cancel(false);
            processTask = null;
        }
        logger.info("PixelManager stopped");
    }
    
    /**
     * Checks if a client needs a pixel update.
     * @param client GameClient to check
     * @return True if client needs update
     */
    public boolean needsUpdate(GameClient client) {
        if (client == null || client.getHabbo() == null) {
            return false;
        }
        
        Habbo habbo = client.getHabbo();
        long currentTimestamp = TimeUtil.getUnixTimestamp();
        long lastUpdate = habbo.getLastActivityPointsUpdate();
        
        double passedMins = (currentTimestamp - lastUpdate) / 60.0;
        
        return passedMins >= RCV_EVERY_MINS;
    }
    
    /**
     * Gives pixels (activity points) to a client.
     * @param client GameClient to give pixels to
     */
    public void givePixels(GameClient client) {
        if (client == null || client.getHabbo() == null) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        long timestamp = TimeUtil.getUnixTimestamp();
        
        habbo.setLastActivityPointsUpdate(timestamp);
        habbo.setActivityPoints(habbo.getActivityPoints() + RCV_AMOUNT);
        habbo.updateActivityPointsBalance(true, RCV_AMOUNT);
    }
}
