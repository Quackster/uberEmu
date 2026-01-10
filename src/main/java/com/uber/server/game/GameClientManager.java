package com.uber.server.game;

import com.uber.server.game.threading.GameThreadPool;
import com.uber.server.messages.PacketHandlerRegistry;
import com.uber.server.messages.ServerMessage;
import com.uber.server.net.TcpConnection;
import com.uber.server.net.TcpConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages all game clients.
 * Thread-safe with ConcurrentHashMap and proper iteration patterns.
 */
public class GameClientManager {
    private static final Logger logger = LoggerFactory.getLogger(GameClientManager.class);
    
    private final ConcurrentMap<Long, GameClient> clients;
    private final PacketHandlerRegistry handlerRegistry;
    private final TcpConnectionManager connectionManager;
    private final AtomicBoolean isRunning;
    
    public GameClientManager(PacketHandlerRegistry handlerRegistry, TcpConnectionManager connectionManager) {
        this.clients = new ConcurrentHashMap<>();
        this.handlerRegistry = handlerRegistry;
        this.connectionManager = connectionManager;
        this.isRunning = new AtomicBoolean(false);
    }
    
    /**
     * Gets the number of clients.
     * @return Client count
     */
    public int getClientCount() {
        return clients.size();
    }
    
    /**
     * Gets a client by ID.
     * Thread-safe: uses ConcurrentHashMap.get().
     * @param clientId Client ID
     * @return The client, or null if not found
     */
    public GameClient getClient(long clientId) {
        return clients.get(clientId);
    }
    
    /**
     * Gets a client by Habbo user ID.
     * Thread-safe: iterates through clients and checks Habbo ID.
     * @param habboId Habbo user ID
     * @return The client, or null if not found
     */
    public GameClient getClientByHabbo(long habboId) {
        for (GameClient client : clients.values()) {
            if (client == null) {
                continue;
            }
            
            Habbo habbo = client.getHabbo();
            if (habbo == null) {
                continue;
            }
            
            if (habbo.getId() == habboId) {
                return client;
            }
        }
        
        return null;
    }
    
    /**
     * Gets a client by Habbo username.
     * Thread-safe: iterates through clients and checks username.
     * @param username Habbo username
     * @return The client, or null if not found
     */
    public GameClient getClientByHabbo(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        
        String lowerUsername = username.toLowerCase();
        
        for (GameClient client : clients.values()) {
            if (client == null) {
                continue;
            }
            
            Habbo habbo = client.getHabbo();
            if (habbo == null) {
                continue;
            }
            
            if (habbo.getUsername().toLowerCase().equals(lowerUsername)) {
                return client;
            }
        }
        
        return null;
    }
    
    /**
     * Starts a new client.
     * Thread-safe: atomic put-if-absent.
     * @param clientId Connection/client ID
     */
    public void startClient(long clientId) {
        TcpConnection connection = connectionManager.getConnection(clientId);
        if (connection == null) {
            logger.warn("Cannot start client {}: connection not found", clientId);
            return;
        }
        
        // Create client atomically
        GameClient client = clients.computeIfAbsent(clientId, id -> {
            logger.debug("Creating new game client for connection {}", id);
            return new GameClient(id, connection, handlerRegistry);
        });
        
        client.startConnection();
        logger.debug("Started client {}", clientId);
    }
    
    /**
     * Stops a client.
     * Thread-safe: atomic removal.
     * @param clientId Client ID
     */
    public void stopClient(long clientId) {
        GameClient client = clients.remove(clientId);
        
        if (client != null) {
            logger.debug("Stopping client {}", clientId);
            connectionManager.dropConnection(clientId);
            client.stop();
        }
    }
    
    /**
     * Removes a client from the map.
     * @param clientId Client ID
     * @return True if client was removed
     */
    public boolean removeClient(long clientId) {
        return clients.remove(clientId) != null;
    }
    
    /**
     * Clears all clients.
     */
    public void clear() {
        // Stop all clients before clearing
        for (GameClient client : clients.values()) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.warn("Error stopping client: {}", e.getMessage());
            }
        }
        clients.clear();
    }
    
    /**
     * Starts the connection checker thread.
     * Checks for timed-out connections and sends ping messages.
     * Uses shared thread pool from GameThreadPool.
     */
    public void startConnectionChecker(int pingInterval) {
        if (isRunning.getAndSet(true)) {
            logger.warn("Connection checker is already running");
            return;
        }
        
        if (pingInterval <= 100) {
            throw new IllegalArgumentException("Invalid configuration value for ping interval! Must be above 100 milliseconds.");
        }
        
        // Use shared thread pool instead of dedicated executor
        ScheduledExecutorService executor = GameThreadPool.getInstance().getGameExecutor();
        
        var pingComposer = new com.uber.server.messages.outgoing.global.PingComposer();
        ServerMessage pingMessage = pingComposer.compose();
        
        executor.scheduleWithFixedDelay(() -> {
            try {
                List<Long> timedOutClients = new ArrayList<>();
                List<GameClient> toPing = new ArrayList<>();
                
                // Create a copy of keys to iterate safely
                // This avoids ConcurrentModificationException when modifying during iteration
                List<Long> clientIds = new ArrayList<>(clients.keySet());
                
                for (Long clientId : clientIds) {
                    GameClient client = clients.get(clientId);
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
                    stopClient(clientId);
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
     * Note: We don't shut down the shared executor here, only mark as not running.
     */
    public void stopConnectionChecker() {
        if (!isRunning.getAndSet(false)) {
            return;
        }
        
        logger.info("Connection checker stopped");
    }
    
    /**
     * Broadcasts a message to all clients.
     * Thread-safe iteration with copy of keys.
     * @param message Message to broadcast
     */
    public void broadcastMessage(ServerMessage message) {
        broadcastMessage(message, null);
    }
    
    /**
     * Broadcasts a message to clients with a fuse requirement.
     * @param message Message to broadcast
     * @param fuseRequirement Fuse requirement (null = no requirement)
     */
    public void broadcastMessage(ServerMessage message, String fuseRequirement) {
        if (message == null) {
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
     * Gets a username by user ID.
     * @param userId User ID
     * @return Username, or empty string if not found
     */
    public String getNameById(long userId) {
        GameClient client = getClientByHabbo(userId);
        if (client != null && client.getHabbo() != null) {
            return client.getHabbo().getUsername();
        }
        return "";
    }
    
    /**
     * Checks and updates avatar effects for all clients.
     */
    public void checkEffects() {
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
    
    /**
     * Checks and updates pixel (activity points) for all clients.
     */
    public void checkPixelUpdates() {
        Game game = null;
        try {
            game = com.uber.server.game.GameEnvironment.getInstance().getGame();
        } catch (Exception e) {
            logger.warn("Could not get Game instance for pixel updates: {}", e.getMessage());
            return;
        }
        
        if (game == null || game.getPixelManager() == null) {
            return;
        }
        
        com.uber.server.game.clients.PixelManager pixelManager = game.getPixelManager();
        
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

    public ConcurrentMap<Long, GameClient> getClients() {
        return clients;
    }
}
