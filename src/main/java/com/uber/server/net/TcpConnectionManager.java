package com.uber.server.net;

import com.uber.server.game.GameClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages all TCP connections.
 * Thread-safe with ConcurrentHashMap.
 */
public class TcpConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(TcpConnectionManager.class);
    
    private final int maxSimultaneousConnections;
    private final ConcurrentMap<Long, TcpConnection> connections;
    private final TcpConnectionListener listener;
    private final TcpConnectionFactory factory;
    
    public TcpConnectionManager(String localIP, int port, int maxSimultaneousConnections) {
        this.maxSimultaneousConnections = maxSimultaneousConnections;
        this.connections = new ConcurrentHashMap<>();
        this.factory = new TcpConnectionFactory();
        this.listener = new TcpConnectionListener(localIP, port, this);
    }
    
    /**
     * Gets the number of active connections.
     * @return Number of active connections
     */
    public int getAmountOfActiveConnections() {
        return connections.size();
    }
    
    /**
     * Checks if a connection exists.
     * @param id Connection ID
     * @return True if connection exists
     */
    public boolean containsConnection(long id) {
        return connections.containsKey(id);
    }
    
    /**
     * Gets a connection by ID.
     * @param id Connection ID
     * @return The connection, or null if not found
     */
    public TcpConnection getConnection(long id) {
        return connections.get(id);
    }
    
    /**
     * Gets the TCP connection listener.
     * @return The listener
     */
    public TcpConnectionListener getListener() {
        return listener;
    }
    
    /**
     * Gets the connection factory.
     * @return The factory
     */
    public TcpConnectionFactory getFactory() {
        return factory;
    }
    
    private GameClientManager gameClientManager;
    
    /**
     * Sets the game client manager for connection handling.
     * @param gameClientManager The game client manager
     */
    public void setGameClientManager(GameClientManager gameClientManager) {
        this.gameClientManager = gameClientManager;
    }
    
    /**
     * Gets the game client manager.
     * @return The game client manager
     */
    public GameClientManager getGameClientManager() {
        return gameClientManager;
    }
    
    /**
     * Handles a new connection.
     * Thread-safe: checks connection limit and adds atomically.
     * @param connection The new connection
     */
    public void handleNewConnection(TcpConnection connection) {
        if (connection == null) {
            return;
        }
        
        // Check connection limit (atomic check)
        if (getAmountOfActiveConnections() >= maxSimultaneousConnections) {
            logger.warn("Connection limit reached ({}), rejecting connection [{}]", 
                    maxSimultaneousConnections, connection.getId());
            connection.stop();
            return;
        }
        
        // Add connection atomically
        connections.put(connection.getId(), connection);
        
        // Notify game client manager to start client
        if (gameClientManager != null) {
            gameClientManager.startClient(connection.getId());
        }
    }
    
    /**
     * Drops a connection by ID.
     * Thread-safe removal.
     * @param id Connection ID
     */
    public void dropConnection(long id) {
        TcpConnection connection = connections.remove(id);
        
        if (connection != null) {
            logger.debug("Dropped connection [{} / {}]", id, connection.getIPAddress());
            connection.stop();
        }
    }
    
    /**
     * Verifies a connection is still alive.
     * @param id Connection ID
     * @return True if connection exists and is alive
     */
    public boolean verifyConnection(long id) {
        TcpConnection connection = connections.get(id);
        return connection != null && connection.testConnection();
    }
    
    /**
     * Destroys the connection manager and stops the listener.
     */
    public void destroyManager() {
        listener.stop();
        
        // Stop all connections
        for (TcpConnection connection : connections.values()) {
            try {
                connection.stop();
            } catch (Exception e) {
                logger.warn("Error stopping connection: {}", e.getMessage());
            }
        }
        
        connections.clear();
        logger.info("Connection manager destroyed");
    }
}
