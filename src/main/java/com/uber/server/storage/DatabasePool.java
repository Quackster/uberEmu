package com.uber.server.storage;

import com.uber.server.config.Configuration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database connection pool wrapper around HikariCP.
 */
public class DatabasePool {
    private static final Logger logger = LoggerFactory.getLogger(DatabasePool.class);
    private final HikariDataSource dataSource;
    
    /**
     * Creates a new database pool from configuration.
     * @param config Configuration object containing database settings
     * @throws SQLException if pool cannot be created
     */
    public DatabasePool(Configuration config) throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        
        String hostname = config.get("db.hostname");
        int port = config.getInt("db.port");
        String username = config.get("db.username");
        String password = config.get("db.password");
        String databaseName = config.get("db.name");
        
        int minSize = config.getInt("db.pool.minsize");
        int maxSize = config.getInt("db.pool.maxsize");
        
        // Build JDBC URL
        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s", hostname, port, databaseName);
        
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        
        // Connection pool settings
        hikariConfig.setMinimumIdle(minSize);
        hikariConfig.setMaximumPoolSize(maxSize);
        
        // Connection timeout (30 seconds)
        hikariConfig.setConnectionTimeout(30000);
        // Idle timeout (10 minutes)
        hikariConfig.setIdleTimeout(600000);
        // Max lifetime (30 minutes)
        hikariConfig.setMaxLifetime(1800000);
        
        // MySQL-specific settings
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
        
        hikariConfig.setPoolName("UberEmuPool");
        hikariConfig.setAutoCommit(true);
        
        try {
            this.dataSource = new HikariDataSource(hikariConfig);
            
            // Test connection
            try (Connection conn = dataSource.getConnection()) {
                if (conn.isValid(5)) {
                    logger.info("Database pool initialized successfully (min={}, max={})", 
                            minSize, maxSize);
                } else {
                    throw new SQLException("Database connection test failed");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to initialize database pool: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Gets a connection from the pool.
     * @return A database connection
     * @throws SQLException if connection cannot be obtained
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * Gets the underlying HikariDataSource.
     * @return The HikariDataSource
     */
    public HikariDataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * Closes the connection pool and releases all resources.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing database pool...");
            dataSource.close();
            logger.info("Database pool closed");
        }
    }
    
    /**
     * Gets the number of active connections in the pool.
     * @return Number of active connections
     */
    public int getActiveConnections() {
        return dataSource.getHikariPoolMXBean() != null 
                ? dataSource.getHikariPoolMXBean().getActiveConnections() 
                : 0;
    }
    
    /**
     * Gets the number of idle connections in the pool.
     * @return Number of idle connections
     */
    public int getIdleConnections() {
        return dataSource.getHikariPoolMXBean() != null 
                ? dataSource.getHikariPoolMXBean().getIdleConnections() 
                : 0;
    }
    
    /**
     * Gets the total number of connections in the pool.
     * @return Total number of connections
     */
    public int getTotalConnections() {
        return dataSource.getHikariPoolMXBean() != null 
                ? dataSource.getHikariPoolMXBean().getTotalConnections() 
                : 0;
    }
}
