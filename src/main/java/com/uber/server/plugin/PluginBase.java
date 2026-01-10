package com.uber.server.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for plugins.
 * Provides default implementation of Plugin interface.
 */
public abstract class PluginBase implements Plugin {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private PluginManager pluginManager;
    private boolean enabled = false;
    
    @Override
    public abstract String getName();
    
    @Override
    public abstract String getVersion();
    
    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }
    
    @Override
    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }
    
    @Override
    public void onEnable() {
        // Override in subclasses
    }
    
    @Override
    public void onDisable() {
        // Override in subclasses
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets the enabled state of this plugin.
     * Called by PluginManager.
     * @param enabled Enabled state
     */
    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Gets the logger for this plugin.
     * @return Logger instance
     */
    public Logger getLogger() {
        return logger;
    }
}
