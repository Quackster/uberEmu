package com.uber.server.misc;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.GameEnvironment;
import com.uber.server.game.Habbo;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages activity points (pixels) for users.
 * Ported from HabboHotel/Misc/PixelManager.cs
 */
public class PixelManager {
    private static final Logger logger = LoggerFactory.getLogger(PixelManager.class);
    
    private static final int RCV_EVERY_MINS = 15;
    private static final int RCV_AMOUNT = 50;
    
    private volatile boolean keepAlive;
    private Thread workerThread;
    
    public PixelManager() {
        this.keepAlive = true;
        this.workerThread = new Thread(this::process);
        this.workerThread.setName("Pixel Manager");
        this.workerThread.setPriority(Thread.MIN_PRIORITY);
    }
    
    /**
     * Starts the pixel manager worker thread.
     */
    public void start() {
        if (workerThread != null && !workerThread.isAlive()) {
            workerThread.start();
            logger.info("PixelManager started");
        }
    }
    
    /**
     * Stops the pixel manager worker thread.
     */
    public void stop() {
        keepAlive = false;
        if (workerThread != null && workerThread.isAlive()) {
            workerThread.interrupt();
            try {
                workerThread.join(5000);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for PixelManager thread to stop");
                Thread.currentThread().interrupt();
            }
        }
        logger.info("PixelManager stopped");
    }
    
    /**
     * Main processing loop for pixel updates.
     */
    private void process() {
        try {
            while (keepAlive) {
                Game game = null;
                try {
                    game = GameEnvironment.getInstance().getGame();
                } catch (Exception e) {
                    logger.error("Could not get Game instance: {}", e.getMessage(), e);
                    break;
                }
                
                if (game != null && game.getClientManager() != null) {
                    game.getClientManager().checkPixelUpdates();
                }
                
                Thread.sleep(15000); // 15 seconds
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("PixelManager thread interrupted");
        } catch (Exception e) {
            logger.error("Error in PixelManager: {}", e.getMessage(), e);
        }
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
