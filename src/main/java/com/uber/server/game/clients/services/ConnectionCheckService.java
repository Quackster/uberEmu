package com.uber.server.game.clients.services;

import com.uber.server.game.GameClient;
import com.uber.server.game.GameClientManager;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for checking client connections and managing ping/pong.
 * Extracted from GameClientManager.
 */
public class ConnectionCheckService {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionCheckService.class);
    
    private final GameClientManager clientManager;
    private ScheduledExecutorService connectionChecker;
    private final AtomicBoolean isRunning;
    
    public ConnectionCheckService(GameClientManager clientManager) {
        this.clientManager = clientManager;
        this.isRunning = new AtomicBoolean(false);
    }
    
    /**
     * Starts the connection checker thread.
     * Checks for timed-out connections and sends ping messages.
     */
    public void startConnectionChecker(int pingInterval) {
        if (isRunning.getAndSet(true)) {
            logger.warn("Connection checker is already running");
            return;
        }
        
        if (pingInterval <= 100) {
            throw new IllegalArgumentException("Invalid configuration value for ping interval! Must be above 100 milliseconds.");
        }
        
        connectionChecker = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Connection Checker");
            t.setDaemon(true);
            return t;
        });
        
        var pingComposer = new com.uber.server.messages.outgoing.global.PingComposer();
        ServerMessage pingMessage = pingComposer.compose();
        
        connectionChecker.scheduleWithFixedDelay(() -> {
            try {
                List<Long> timedOutClients = new ArrayList<>();
                List<GameClient> toPing = new ArrayList<>();
                
                // Create a copy of keys to iterate safely
                List<Long> clientIds = new ArrayList<>(clientManager.getClients().keySet());
                
                for (Long clientId : clientIds) {
                    GameClient client = clientManager.getClient(clientId);
                    if (client == null) {
                        continue;
                    }
                    
                    if (client.isPongOK()) {
                        client.setPongOK(false);
                        toPing.add(client);
                    } else {
                        timedOutClients.add(clientId);
                    }
                }
                
                // Remove timed-out clients
                for (Long clientId : timedOutClients) {
                    logger.debug("Client {} timed out, disconnecting", clientId);
                    clientManager.stopClient(clientId);
                }
                
                // Send ping to active clients
                for (GameClient client : toPing) {
                    try {
                        if (client.getConnection() != null && client.getConnection().isAlive()) {
                            client.getConnection().sendMessage(pingMessage);
                        }
                    } catch (Exception e) {
                        logger.debug("Failed to send ping to client {}: {}", client.getClientId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.error("Error in connection checker: {}", e.getMessage(), e);
            }
        }, pingInterval, pingInterval, TimeUnit.MILLISECONDS);
        
        logger.info("Connection checker started with interval {}ms", pingInterval);
    }
    
    /**
     * Stops the connection checker.
     */
    public void stopConnectionChecker() {
        if (!isRunning.getAndSet(false)) {
            return;
        }
        
        if (connectionChecker != null && !connectionChecker.isShutdown()) {
            connectionChecker.shutdown();
            try {
                if (!connectionChecker.awaitTermination(5, TimeUnit.SECONDS)) {
                    connectionChecker.shutdownNow();
                }
            } catch (InterruptedException e) {
                connectionChecker.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("Connection checker stopped");
    }
    
    public boolean isRunning() {
        return isRunning.get();
    }
}
