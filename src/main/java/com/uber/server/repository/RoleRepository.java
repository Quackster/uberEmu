package com.uber.server.repository;

import com.uber.server.storage.DatabasePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for role and rights-related database operations.
 */
public class RoleRepository {
    private static final Logger logger = LoggerFactory.getLogger(RoleRepository.class);
    private final DatabasePool databasePool;
    
    public RoleRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads all roles from the database.
     * @return List of role data (id, name)
     */
    public List<Map<String, Object>> loadRoles() {
        String sql = "SELECT * FROM ranks ORDER BY id ASC";
        List<Map<String, Object>> roles = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> role = new HashMap<>();
                role.put("id", rs.getLong("id"));
                role.put("name", rs.getString("name"));
                roles.add(role);
            }
        } catch (SQLException e) {
            logger.error("Failed to load roles: {}", e.getMessage(), e);
        }
        
        return roles;
    }
    
    /**
     * Loads all fuse rights from the database.
     * @return Map of fuse name to minimum rank ID
     */
    public Map<String, Long> loadRights() {
        String sql = "SELECT * FROM fuserights";
        Map<String, Long> rights = new HashMap<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String fuse = rs.getString("fuse").toLowerCase();
                long rank = rs.getLong("rank");
                rights.put(fuse, rank);
            }
        } catch (SQLException e) {
            logger.error("Failed to load rights: {}", e.getMessage(), e);
        }
        
        return rights;
    }
    
    /**
     * Loads all subscription rights from the database.
     * @return Map of fuse name to subscription ID
     */
    public Map<String, String> loadSubRights() {
        String sql = "SELECT * FROM fuserights_subs";
        Map<String, String> subRights = new HashMap<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String fuse = rs.getString("fuse");
                String sub = rs.getString("sub");
                subRights.put(fuse, sub);
            }
        } catch (SQLException e) {
            logger.error("Failed to load subscription rights: {}", e.getMessage(), e);
        }
        
        return subRights;
    }
}
