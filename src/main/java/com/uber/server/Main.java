package com.uber.server;

import com.uber.server.core.CommandParser;
import com.uber.server.game.GameEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Main entry point for the UberEmu server.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static GameEnvironment environment;
    private static boolean isShuttingDown = false;
    
    public static void main(String[] args) {
        // Set up uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            logger.error("Uncaught exception in thread {}: {}", thread.getName(), exception.getMessage(), exception);
            shutdown();
        });
        
        // Set up shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered");
            shutdown();
        }, "ShutdownHook"));
        
        try {
            // Initialize environment
            environment = GameEnvironment.getInstance();
            environment.initialize();
            
            // Main loop: read commands from console
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8));
            
            String line;
            while (!isShuttingDown && (line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    processCommand(line.trim());
                }
            }
            
        } catch (Exception e) {
            logger.error("Fatal error: {}", e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Processes a console command.
     */
    private static void processCommand(String command) {
        CommandParser.parse(command, environment);
    }
    
    /**
     * Shuts down the server gracefully.
     */
    private static synchronized void shutdown() {
        if (isShuttingDown) {
            return;
        }
        
        isShuttingDown = true;
        logger.info("Shutting down server...");
        
        if (environment != null) {
            try {
                environment.destroy();
            } catch (Exception e) {
                logger.error("Error during shutdown: {}", e.getMessage(), e);
            }
        }
        
        logger.info("Server shutdown complete");
        System.exit(0);
    }
}
