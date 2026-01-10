package com.uber.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Loads configuration from uber-config.conf file and environment variables.
 * Environment variables override file values with format: UBER_KEY_NAME (uppercase, dots replaced with underscores).
 */
public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String CONFIG_FILE = "uber-config.conf";
    
    /**
     * Loads configuration from file and environment variables.
     * @return Configuration object with all settings
     * @throws ConfigLoadException if configuration file cannot be loaded or required keys are missing
     */
    public static Configuration load() throws ConfigLoadException {
        Configuration config = new Configuration();
        
        // Load from file
        loadFromFile(config);
        
        // Override with environment variables
        loadFromEnvironment(config);
        
        // Validate required keys
        validateRequiredKeys(config);
        
        return config;
    }
    
    /**
     * Loads configuration from uber-config.conf file in resources.
     */
    private static void loadFromFile(Configuration config) throws ConfigLoadException {
        try (InputStream is = new FileInputStream(CONFIG_FILE)) {
            if (is == null) {
                throw new ConfigLoadException("Configuration file '" + CONFIG_FILE + "' not found in resources");
            }
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                
                String line;
                int lineNumber = 0;
                
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();
                    
                    // Skip empty lines and comments
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    
                    // Parse key=value pairs
                    int delimiterIndex = line.indexOf('=');
                    if (delimiterIndex == -1) {
                        logger.warn("Invalid configuration line {}: {}", lineNumber, line);
                        continue;
                    }
                    
                    String key = line.substring(0, delimiterIndex).trim();
                    String value = line.substring(delimiterIndex + 1).trim();
                    
                    if (!key.isEmpty()) {
                        config.set(key, value);
                    }
                }
                
                logger.info("Loaded configuration from {}", CONFIG_FILE);
            }
        } catch (Exception e) {
            throw new ConfigLoadException("Failed to load configuration file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Overrides configuration values with environment variables.
     * Environment variable names: UBER_KEY_NAME (key becomes UBER_KEY_NAME)
     * Example: db.hostname -> UBER_DB_HOSTNAME
     */
    private static void loadFromEnvironment(Configuration config) {
        int overrideCount = 0;
        
        for (Map.Entry<String, String> entry : config.getAll().entrySet()) {
            String key = entry.getKey();
            String envKey = "UBER_" + key.toUpperCase().replace('.', '_');
            String envValue = System.getenv(envKey);
            
            if (envValue != null && !envValue.isEmpty()) {
                config.set(key, envValue);
                overrideCount++;
                logger.debug("Overrode {} with environment variable {}", key, envKey);
            }
        }
        
        if (overrideCount > 0) {
            logger.info("Overrode {} configuration values from environment variables", overrideCount);
        }
    }
    
    /**
     * Validates that all required configuration keys are present.
     */
    private static void validateRequiredKeys(Configuration config) throws ConfigLoadException {
        String[] requiredKeys = {
            "db.hostname",
            "db.port",
            "db.username",
            "db.password",
            "db.name",
            "db.pool.minsize",
            "db.pool.maxsize",
            "game.tcp.bindip",
            "game.tcp.port",
            "game.tcp.conlimit"
        };
        
        StringBuilder missingKeys = new StringBuilder();
        for (String key : requiredKeys) {
            if (!config.containsKey(key)) {
                if (missingKeys.length() > 0) {
                    missingKeys.append(", ");
                }
                missingKeys.append(key);
            }
        }
        
        if (missingKeys.length() > 0) {
            throw new ConfigLoadException("Missing required configuration keys: " + missingKeys);
        }
        
        // Validate password security
        String password = config.get("db.password");
        if (password == null || password.isEmpty()) {
            throw new ConfigLoadException("For security reasons, your MySQL password cannot be left blank. Please change your password to start the server.");
        }
        if ("changeme".equals(password)) {
            throw new ConfigLoadException("Your MySQL password may not be 'changeme'.\nPlease change your password to start the server.");
        }
    }
    
    /**
     * Exception thrown when configuration loading fails.
     */
    public static class ConfigLoadException extends Exception {
        public ConfigLoadException(String message) {
            super(message);
        }
        
        public ConfigLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
