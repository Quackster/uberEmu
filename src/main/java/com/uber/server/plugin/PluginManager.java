package com.uber.server.plugin;

import com.uber.server.event.EventManager;
import com.uber.server.event.Listener;
import com.uber.server.game.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Manages plugins using a Bukkit/Spigot/Paper-style system.
 * Handles plugin loading, lifecycle, and event registration.
 */
public class PluginManager {
    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);
    
    private final EventManager eventManager;
    private final Game game;
    private final Map<String, Plugin> plugins;
    private final Map<String, Class<?>> pluginClasses;
    private final List<Plugin> loadedPlugins;
    
    public PluginManager(EventManager eventManager, Game game) {
        this.eventManager = eventManager;
        this.game = game;
        this.plugins = new ConcurrentHashMap<>();
        this.pluginClasses = new ConcurrentHashMap<>();
        this.loadedPlugins = new ArrayList<>();
    }
    
    /**
     * Loads all plugins from the plugins directory.
     */
    public void loadPlugins() {
        loadPlugins("plugins");
    }
    
    /**
     * Loads all plugins from a specific directory.
     * @param directory Directory path to load plugins from
     */
    public void loadPlugins(String directory) {
        File pluginDir = new File(directory);
        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
            logger.info("Created plugin directory: {}", directory);
            return;
        }
        
        if (!pluginDir.isDirectory()) {
            logger.warn("Plugin directory {} is not a directory", directory);
            return;
        }
        
        File[] files = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null) {
            logger.info("No plugins found in {}", directory);
            return;
        }
        
        logger.info("Loading plugins from {}...", directory);
        
        // First pass: load plugin classes
        for (File file : files) {
            try {
                loadPlugin(file);
            } catch (Exception e) {
                logger.error("Failed to load plugin {}: {}", file.getName(), e.getMessage(), e);
            }
        }
        
        // Second pass: enable plugins
        for (Plugin plugin : loadedPlugins) {
            try {
                enablePlugin(plugin);
            } catch (Exception e) {
                logger.error("Failed to enable plugin {}: {}", plugin.getName(), e.getMessage(), e);
            }
        }
        
        logger.info("Loaded {} plugin(s)", loadedPlugins.size());
    }
    
    /**
     * Loads a plugin from a JAR file.
     * @param file JAR file
     */
    private void loadPlugin(File file) throws Exception {
        logger.debug("Loading plugin: {}", file.getName());
        
        URL[] urls = {file.toURI().toURL()};
        URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());
        
        // Find plugin class by scanning JAR manifest or by convention
        String pluginClassName = findPluginClass(file);
        if (pluginClassName == null) {
            logger.warn("Could not find plugin class in {}", file.getName());
            return;
        }
        
        Class<?> pluginClass = classLoader.loadClass(pluginClassName);
        if (!Plugin.class.isAssignableFrom(pluginClass)) {
            logger.warn("Class {} does not implement Plugin interface", pluginClassName);
            return;
        }
        
        Plugin plugin = (Plugin) pluginClass.getDeclaredConstructor().newInstance();
        plugin.setPluginManager(this);
        
        pluginClasses.put(plugin.getName(), pluginClass);
        loadedPlugins.add(plugin);
        
        logger.info("Loaded plugin: {} v{}", plugin.getName(), plugin.getVersion());
    }
    
    /**
     * Finds the plugin class name in a JAR file.
     * Checks manifest first, then scans for classes implementing Plugin.
     * @param file JAR file
     * @return Plugin class name, or null if not found
     */
    private String findPluginClass(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            // Check manifest for Plugin-Class attribute
            java.util.jar.Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                String pluginClass = manifest.getMainAttributes().getValue("Plugin-Class");
                if (pluginClass != null && !pluginClass.isEmpty()) {
                    return pluginClass;
                }
            }
            
            // Scan for classes implementing Plugin
            Enumeration<JarEntry> entries = jarFile.entries();
            URL[] urls = {file.toURI().toURL()};
            URLClassLoader tempLoader = new URLClassLoader(urls, getClass().getClassLoader());
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace("/", ".").replace(".class", "");
                    try {
                        Class<?> clazz = tempLoader.loadClass(className);
                        if (Plugin.class.isAssignableFrom(clazz) && !clazz.isInterface() && !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                            tempLoader.close();
                            return className;
                        }
                    } catch (ClassNotFoundException | NoClassDefFoundError e) {
                        // Skip this class
                    }
                }
            }
            tempLoader.close();
        } catch (IOException e) {
            logger.error("Error reading JAR file {}: {}", file.getName(), e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Enables a plugin.
     * @param plugin Plugin to enable
     */
    public void enablePlugin(Plugin plugin) {
        if (plugin.isEnabled()) {
            return;
        }
        
        try {
            logger.info("Enabling plugin: {} v{}", plugin.getName(), plugin.getVersion());
            
            if (plugin instanceof PluginBase) {
                ((PluginBase) plugin).setEnabled(true);
            }
            
            plugin.onEnable();
            
            plugins.put(plugin.getName().toLowerCase(), plugin);
            
            logger.info("Plugin {} v{} enabled", plugin.getName(), plugin.getVersion());
        } catch (Exception e) {
            logger.error("Error enabling plugin {}: {}", plugin.getName(), e.getMessage(), e);
            disablePlugin(plugin);
        }
    }
    
    /**
     * Disables a plugin.
     * @param plugin Plugin to disable
     */
    public void disablePlugin(Plugin plugin) {
        if (!plugin.isEnabled()) {
            return;
        }
        
        try {
            logger.info("Disabling plugin: {}", plugin.getName());
            
            // Unregister all event listeners for this plugin
            eventManager.unregisterEvents(plugin);
            
            plugin.onDisable();
            
            if (plugin instanceof PluginBase) {
                ((PluginBase) plugin).setEnabled(false);
            }
            
            plugins.remove(plugin.getName().toLowerCase());
            
            logger.info("Plugin {} disabled", plugin.getName());
        } catch (Exception e) {
            logger.error("Error disabling plugin {}: {}", plugin.getName(), e.getMessage(), e);
        }
    }
    
    /**
     * Registers event listeners for a plugin.
     * @param listener Listener instance
     * @param plugin Plugin instance
     */
    public void registerEvents(Listener listener, Plugin plugin) {
        eventManager.registerEvents(listener, plugin);
    }
    
    /**
     * Gets a plugin by name.
     * @param name Plugin name
     * @return Plugin instance, or null if not found
     */
    public Plugin getPlugin(String name) {
        return plugins.get(name.toLowerCase());
    }
    
    /**
     * Gets all loaded plugins.
     * @return List of plugins
     */
    public List<Plugin> getPlugins() {
        return new ArrayList<>(loadedPlugins);
    }
    
    /**
     * Disables all plugins.
     */
    public void disablePlugins() {
        for (Plugin plugin : new ArrayList<>(loadedPlugins)) {
            disablePlugin(plugin);
        }
    }
    
    /**
     * Gets the EventManager instance.
     * @return EventManager
     */
    public EventManager getEventManager() {
        return eventManager;
    }
    
    /**
     * Gets the Game instance.
     * @return Game instance
     */
    public Game getGame() {
        return game;
    }
}
