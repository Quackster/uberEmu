package com.uber.server.game.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized thread pool manager for game and room processing.
 * Provides shared thread pools to avoid creating too many threads.
 * Networking remains on Netty's event loop threads (separate).
 */
public class GameThreadPool {
    private static final Logger logger = LoggerFactory.getLogger(GameThreadPool.class);
    
    private static GameThreadPool instance;
    
    private ScheduledExecutorService gameExecutor;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private volatile boolean shutdown = false;
    
    private GameThreadPool() {
        // Calculate optimal thread pool size
        // Use CPU count + 1 for I/O bound tasks, but cap at reasonable limit
        int corePoolSize = Math.max(2, Math.min(Runtime.getRuntime().availableProcessors() + 1, 8));
        
        gameExecutor = Executors.newScheduledThreadPool(corePoolSize, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "GameThreadPool-" + threadNumber.getAndIncrement());
                t.setDaemon(false); // Keep threads alive for game processing
                return t;
            }
        });
        
        logger.info("GameThreadPool initialized with {} threads", corePoolSize);
    }
    
    /**
     * Gets the singleton instance.
     * @return GameThreadPool instance
     */
    public static synchronized GameThreadPool getInstance() {
        if (instance == null) {
            instance = new GameThreadPool();
        }
        return instance;
    }
    
    /**
     * Gets the shared scheduled executor service for game and room processing.
     * @return ScheduledExecutorService instance
     */
    public ScheduledExecutorService getGameExecutor() {
        if (shutdown) {
            throw new IllegalStateException("GameThreadPool has been shut down");
        }
        return gameExecutor;
    }
    
    /**
     * Shuts down the thread pool gracefully.
     * Waits for running tasks to complete.
     */
    public void shutdown() {
        if (shutdown) {
            return;
        }
        
        shutdown = true;
        logger.info("Shutting down GameThreadPool...");
        
        if (gameExecutor != null && !gameExecutor.isShutdown()) {
            gameExecutor.shutdown();
            try {
                if (!gameExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    logger.warn("GameThreadPool did not terminate within timeout, forcing shutdown");
                    gameExecutor.shutdownNow();
                    if (!gameExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        logger.error("GameThreadPool did not terminate after forced shutdown");
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while shutting down GameThreadPool");
                gameExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("GameThreadPool shut down");
    }
    
    /**
     * Checks if the thread pool is shut down.
     * @return True if shut down
     */
    public boolean isShutdown() {
        return shutdown;
    }
}
