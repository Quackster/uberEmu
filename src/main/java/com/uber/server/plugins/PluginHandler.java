package com.uber.server.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages plugins using URLClassLoader for dynamic loading.
 */
public class PluginHandler implements IUberPluginHost {
    private static final Logger logger = LoggerFactory.getLogger(PluginHandler.class);
    
    private final List<AvailablePlugin> availablePlugins;
    private final ConcurrentHashMap<Integer, String> pluginHandlers; // Header ID -> Plugin name
    
    public PluginHandler() {
        this.availablePlugins = new ArrayList<>();
        this.pluginHandlers = new ConcurrentHashMap<>();
    }
    
    /**
     * Loads plugins from the plugins directory.
     */
    public void loadPlugins() {
        loadPlugins("plugins");
    }
    
    /**
     * Loads plugins from a specific path.
     * @param path Path to load plugins from
     */
    public void loadPlugins(String path) {
        availablePlugins.clear();
        pluginHandlers.clear();
        
        File pluginDir = new File(path);
        if (!pluginDir.exists() || !pluginDir.isDirectory()) {
            logger.warn("Plugin directory {} does not exist", path);
            return;
        }
        
        File[] files = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            initPlugin(file.getAbsolutePath());
        }
        
        logger.info("Loaded {} plugin(s)", availablePlugins.size());
    }
    
    /**
     * Unloads all plugins.
     */
    public void unloadPlugins() {
        for (AvailablePlugin plugin : availablePlugins) {
            try {
                plugin.getInstance().dispose();
            } catch (Exception e) {
                logger.error("Error disposing plugin {}: {}", plugin.getInstance().getName(), e.getMessage(), e);
            }
        }
        
        availablePlugins.clear();
        pluginHandlers.clear();
    }
    
    /**
     * Unloads a specific plugin.
     * @param name Plugin name
     * @return True if plugin was found and unloaded
     */
    public boolean unloadPlugin(String name) {
        for (AvailablePlugin plugin : new ArrayList<>(availablePlugins)) {
            if (plugin.getInstance().getName().equalsIgnoreCase(name)) {
                try {
                    plugin.getInstance().dispose();
                } catch (Exception e) {
                    logger.error("Error disposing plugin {}: {}", name, e.getMessage(), e);
                }
                
                availablePlugins.remove(plugin);
                
                // Remove packet handlers for this plugin
                pluginHandlers.entrySet().removeIf(entry -> entry.getValue().equalsIgnoreCase(name));
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Initializes a plugin from a JAR file.
     * @param fileName Path to JAR file
     */
    private void initPlugin(String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                logger.warn("Plugin file not found: {}", fileName);
                return;
            }
            
            URL[] urls = {file.toURI().toURL()};
            URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());
            
            // Look for classes implementing IUberPlugin
            // Note: This is simplified - full implementation would scan JAR manifest or use ServiceLoader
            // For now, plugins must be in a known package structure
            String className = extractPluginClassName(fileName);
            if (className == null) {
                logger.warn("Could not determine plugin class name for {}", fileName);
                return;
            }
            
            Class<?> pluginClass = classLoader.loadClass(className);
            if (!IUberPlugin.class.isAssignableFrom(pluginClass)) {
                logger.warn("Class {} does not implement IUberPlugin", className);
                return;
            }
            
            IUberPlugin instance = (IUberPlugin) pluginClass.getDeclaredConstructor().newInstance();
            instance.setHost(this);
            instance.initialize();
            
            AvailablePlugin plugin = new AvailablePlugin();
            plugin.setAssemblyPath(fileName);
            plugin.setInstance(instance);
            
            availablePlugins.add(plugin);
            
            logger.info("Initialized plugin: {}", instance.getName());
            
        } catch (Exception e) {
            logger.error("Could not load plugin {}: {}", fileName, e.getMessage(), e);
        }
    }
    
    /**
     * Extracts plugin class name from JAR file name.
     * Simplified - assumes plugin class name matches JAR name.
     * @param fileName JAR file path
     * @return Class name, or null if cannot determine
     */
    private String extractPluginClassName(String fileName) {
        // Simplified: assume plugin class is in com.uber.plugins package and matches filename
        String name = new File(fileName).getName();
        name = name.substring(0, name.lastIndexOf('.'));
        return "com.uber.plugins." + name;
    }
    
    /**
     * Gets a plugin by name.
     * @param pluginName Plugin name
     * @return AvailablePlugin, or null if not found
     */
    public AvailablePlugin getPlugin(String pluginName) {
        for (AvailablePlugin plugin : availablePlugins) {
            if (plugin.getInstance().getName().equalsIgnoreCase(pluginName)) {
                return plugin;
            }
        }
        return null;
    }
    
    @Override
    public boolean registerPacketHandler(String pluginName, int headerId) {
        if (hasPacketHandler(headerId)) {
            return false;
        }
        
        pluginHandlers.put(headerId, pluginName);
        return true;
    }
    
    @Override
    public void unregisterPacketHandler(String pluginName, int headerId) {
        pluginHandlers.remove(headerId);
    }
    
    @Override
    public boolean hasPacketHandler(int headerId) {
        return pluginHandlers.containsKey(headerId);
    }
    
    /**
     * Executes a packet handler if registered by a plugin.
     * @param habboId Habbo user ID
     * @param headerId Packet header ID
     * @param data Packet data
     * @return True if packet was handled by plugin, false if no plugin handler
     */
    public boolean executePacketHandler(long habboId, int headerId, byte[] data) {
        if (!hasPacketHandler(headerId)) {
            return false; // No plugin handler, let normal handler process
        }
        
        AvailablePlugin plugin = getPlugin(pluginHandlers.get(headerId));
        if (plugin == null) {
            return false;
        }
        
        return plugin.getInstance().handlePacket(habboId, headerId, data);
    }
    
    /**
     * Gets the list of available plugins.
     * @return List of available plugins
     */
    public List<AvailablePlugin> getAvailablePlugins() {
        return new ArrayList<>(availablePlugins);
    }
    
    /**
     * Destroys the plugin handler.
     */
    public void destroy() {
        unloadPlugins();
    }
    
    /**
     * Represents an available plugin.
     */
    public static class AvailablePlugin {
        private String assemblyPath;
        private IUberPlugin instance;
        
        public String getAssemblyPath() { return assemblyPath; }
        public void setAssemblyPath(String assemblyPath) { this.assemblyPath = assemblyPath; }
        public IUberPlugin getInstance() { return instance; }
        public void setInstance(IUberPlugin instance) { this.instance = instance; }
    }
}
