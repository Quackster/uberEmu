package com.uber.server.plugin;

import com.uber.server.plugin.PluginManager;

/**
 * Represents a plugin.
 * Similar to Bukkit/Spigot/Paper Plugin interface.
 */
public interface Plugin {
    /**
     * Returns the name of this plugin.
     * @return Name of this plugin
     */
    String getName();
    
    /**
     * Returns the version of this plugin.
     * @return Version of this plugin
     */
    String getVersion();
    
    /**
     * Returns the plugin manager that is managing this plugin.
     * @return PluginManager instance
     */
    PluginManager getPluginManager();
    
    /**
     * Sets the plugin manager for this plugin.
     * @param pluginManager PluginManager instance
     */
    void setPluginManager(PluginManager pluginManager);
    
    /**
     * Called when this plugin is enabled.
     */
    void onEnable();
    
    /**
     * Called when this plugin is disabled.
     */
    void onDisable();
    
    /**
     * Returns whether this plugin is currently enabled.
     * @return true if this plugin is enabled, false otherwise
     */
    boolean isEnabled();
}
