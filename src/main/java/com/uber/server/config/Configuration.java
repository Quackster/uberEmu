package com.uber.server.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration class that holds key-value pairs from configuration file.
 * Thread-safe for read operations after initialization.
 */
public class Configuration {
    private final Map<String, String> data;
    
    public Configuration() {
        this.data = new HashMap<>();
    }
    
    /**
     * Gets a configuration value by key.
     * @param key The configuration key
     * @return The configuration value, or null if not found
     */
    public String get(String key) {
        return data.get(key);
    }
    
    /**
     * Gets a configuration value by key with a default value.
     * @param key The configuration key
     * @param defaultValue Default value if key not found
     * @return The configuration value or defaultValue
     */
    public String get(String key, String defaultValue) {
        return data.getOrDefault(key, defaultValue);
    }
    
    /**
     * Gets a configuration value as an integer.
     * @param key The configuration key
     * @return The integer value, or 0 if not found or invalid
     */
    public int getInt(String key) {
        String value = data.get(key);
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Gets a configuration value as a boolean.
     * @param key The configuration key
     * @return True if value is "1", false otherwise
     */
    public boolean getBoolean(String key) {
        String value = data.get(key);
        return "1".equals(value);
    }
    
    /**
     * Sets a configuration value.
     * @param key The configuration key
     * @param value The configuration value
     */
    public void set(String key, String value) {
        if (key != null && value != null) {
            data.put(key, value);
        }
    }
    
    /**
     * Checks if a key exists in the configuration.
     * @param key The configuration key
     * @return True if key exists
     */
    public boolean containsKey(String key) {
        return data.containsKey(key);
    }
    
    /**
     * Returns all configuration data (read-only view).
     * @return Map of configuration entries
     */
    public Map<String, String> getAll() {
        return Map.copyOf(data);
    }
}
