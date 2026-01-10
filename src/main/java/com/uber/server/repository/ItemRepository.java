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
 * Repository for furniture/item database operations.
 * Used by ItemManager to load item definitions.
 */
public class ItemRepository {
    private static final Logger logger = LoggerFactory.getLogger(ItemRepository.class);
    private final DatabasePool databasePool;
    
    public ItemRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads all furniture items from the database.
     * @return List of item data maps
     */
    public List<Map<String, Object>> loadAllItems() {
        String sql = "SELECT * FROM furniture";
        List<Map<String, Object>> items = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", rs.getLong("id"));
                item.put("sprite_id", rs.getInt("sprite_id"));
                item.put("public_name", rs.getString("public_name"));
                item.put("item_name", rs.getString("item_name"));
                item.put("type", rs.getString("type"));
                item.put("width", rs.getInt("width"));
                item.put("length", rs.getInt("length"));
                item.put("stack_height", rs.getDouble("stack_height"));
                item.put("can_stack", rs.getString("can_stack"));
                item.put("is_walkable", rs.getString("is_walkable"));
                item.put("can_sit", rs.getString("can_sit"));
                item.put("allow_recycle", rs.getString("allow_recycle"));
                item.put("allow_trade", rs.getString("allow_trade"));
                item.put("allow_marketplace_sell", rs.getString("allow_marketplace_sell"));
                item.put("allow_gift", rs.getString("allow_gift"));
                item.put("allow_inventory_stack", rs.getString("allow_inventory_stack"));
                item.put("interaction_type", rs.getString("interaction_type"));
                item.put("interaction_modes_count", rs.getInt("interaction_modes_count"));
                item.put("vending_ids", rs.getString("vending_ids"));
                items.add(item);
            }
        } catch (SQLException e) {
            logger.error("Failed to load furniture items: {}", e.getMessage(), e);
        }
        
        return items;
    }
}
