package com.uber.server.plugins;

/**
 * Interface for plugin host (provides plugin management).
 */
public interface IUberPluginHost {
    /**
     * Registers a packet handler for a plugin.
     * @param pluginName Plugin name
     * @param headerId Packet header ID
     * @return True if registration was successful
     */
    boolean registerPacketHandler(String pluginName, int headerId);
    
    /**
     * Unregisters a packet handler.
     * @param pluginName Plugin name
     * @param headerId Packet header ID
     */
    void unregisterPacketHandler(String pluginName, int headerId);
    
    /**
     * Checks if a packet handler is registered.
     * @param headerId Packet header ID
     * @return True if handler is registered
     */
    boolean hasPacketHandler(int headerId);
}
