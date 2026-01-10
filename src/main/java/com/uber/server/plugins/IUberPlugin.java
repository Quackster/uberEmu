package com.uber.server.plugins;

/**
 * Interface for Uber plugins.
 */
public interface IUberPlugin {
    /**
     * Gets the plugin name.
     * @return Plugin name
     */
    String getName();
    
    /**
     * Sets the plugin host.
     * @param host Plugin host instance
     */
    void setHost(IUberPluginHost host);
    
    /**
     * Initializes the plugin.
     */
    void initialize();
    
    /**
     * Handles a packet.
     * @param habboId Habbo user ID
     * @param headerId Packet header ID
     * @param data Packet data
     * @return True if packet was handled
     */
    boolean handlePacket(long habboId, int headerId, byte[] data);
    
    /**
     * Disposes the plugin.
     */
    void dispose();
}
