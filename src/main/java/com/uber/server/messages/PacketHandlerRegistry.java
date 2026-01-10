package com.uber.server.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Registry for packet handlers using ConcurrentHashMap for thread safety.
 */
public class PacketHandlerRegistry {
    private static final Logger logger = LoggerFactory.getLogger(PacketHandlerRegistry.class);
    public static final int HIGHEST_MESSAGE_ID = 4004;
    
    private final Map<Integer, PacketHandler> handlers;
    
    public PacketHandlerRegistry() {
        this.handlers = new ConcurrentHashMap<>();
    }
    
    /**
     * Registers a packet handler for a specific message ID.
     * @param messageId The message ID to handle
     * @param handler The packet handler
     */
    public void register(int messageId, PacketHandler handler) {
        if (messageId < 0 || messageId > HIGHEST_MESSAGE_ID) {
            logger.warn("Attempted to register handler for invalid message ID: {}", messageId);
            return;
        }
        
        if (handler == null) {
            logger.warn("Attempted to register null handler for message ID: {}", messageId);
            return;
        }
        
        handlers.put(messageId, handler);
        logger.debug("Registered handler for message ID: {}", messageId);
    }
    
    /**
     * Gets the handler for a specific message ID.
     * @param messageId The message ID
     * @return The packet handler, or null if not found
     */
    public PacketHandler getHandler(int messageId) {
        return handlers.get(messageId);
    }
    
    /**
     * Checks if a handler exists for a message ID.
     * @param messageId The message ID
     * @return True if a handler is registered
     */
    public boolean hasHandler(int messageId) {
        return handlers.containsKey(messageId);
    }
    
    /**
     * Removes a handler for a specific message ID.
     * @param messageId The message ID
     */
    public void unregister(int messageId) {
        handlers.remove(messageId);
        logger.debug("Unregistered handler for message ID: {}", messageId);
    }
    
    /**
     * Clears all registered handlers.
     */
    public void clear() {
        handlers.clear();
        logger.debug("Cleared all packet handlers");
    }
    
    /**
     * Gets the number of registered handlers.
     * @return Number of registered handlers
     */
    public int size() {
        return handlers.size();
    }
}
