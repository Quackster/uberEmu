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
 * Repository for catalog database operations.
 */
public class CatalogRepository {
    private static final Logger logger = LoggerFactory.getLogger(CatalogRepository.class);
    private final DatabasePool databasePool;
    
    public CatalogRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads catalog items for a page.
     * @param pageId Page ID
     * @return List of catalog item data (id, item_ids, catalog_name, cost_credits, cost_pixels, amount)
     */
    public List<Map<String, Object>> loadCatalogItems(int pageId) {
        String sql = """
            SELECT id, item_ids, catalog_name, cost_credits, cost_pixels, amount
            FROM catalog_items
            WHERE page_id = ?
            ORDER BY item_ids ASC
            """;
        List<Map<String, Object>> items = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, pageId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getLong("id"));
                    item.put("item_ids", rs.getString("item_ids"));
                    item.put("catalog_name", rs.getString("catalog_name"));
                    item.put("cost_credits", rs.getInt("cost_credits"));
                    item.put("cost_pixels", rs.getInt("cost_pixels"));
                    item.put("amount", rs.getInt("amount"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load catalog items for page {}: {}", pageId, e.getMessage(), e);
        }
        
        return items;
    }
    
    /**
     * Loads all catalog pages.
     * @return List of catalog page data
     */
    public List<Map<String, Object>> loadCatalogPages() {
        String sql = "SELECT * FROM catalog_pages ORDER BY order_num ASC";
        List<Map<String, Object>> pages = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> page = new HashMap<>();
                page.put("id", rs.getInt("id"));
                page.put("parent_id", rs.getInt("parent_id"));
                page.put("caption", rs.getString("caption"));
                page.put("visible", rs.getInt("visible"));
                page.put("enabled", rs.getInt("enabled"));
                page.put("coming_soon", rs.getInt("coming_soon"));
                page.put("min_rank", rs.getLong("min_rank"));
                page.put("club_only", rs.getString("club_only"));
                page.put("icon_color", rs.getInt("icon_color"));
                page.put("icon_image", rs.getInt("icon_image"));
                page.put("page_layout", rs.getString("page_layout"));
                page.put("page_headline", rs.getString("page_headline"));
                page.put("page_teaser", rs.getString("page_teaser"));
                page.put("page_special", rs.getString("page_special"));
                page.put("page_text1", rs.getString("page_text1"));
                page.put("page_text2", rs.getString("page_text2"));
                page.put("page_text_details", rs.getString("page_text_details"));
                page.put("page_text_teaser", rs.getString("page_text_teaser"));
                pages.add(page);
            }
        } catch (SQLException e) {
            logger.error("Failed to load catalog pages: {}", e.getMessage(), e);
        }
        
        return pages;
    }
    
    /**
     * Creates a teleport link between two teleporters.
     * @param teleOneId First teleporter ID
     * @param teleTwoId Second teleporter ID
     * @return True if both links were created successfully
     */
    public boolean createTeleLink(long teleOneId, long teleTwoId) {
        String sql = "INSERT INTO tele_links (tele_one_id, tele_two_id) VALUES (?, ?)";
        
        try (Connection conn = databasePool.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt1 = conn.prepareStatement(sql);
                 PreparedStatement stmt2 = conn.prepareStatement(sql)) {
                
                // Create bidirectional link
                stmt1.setLong(1, teleOneId);
                stmt1.setLong(2, teleTwoId);
                stmt1.executeUpdate();
                
                stmt2.setLong(1, teleTwoId);
                stmt2.setLong(2, teleOneId);
                stmt2.executeUpdate();
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Failed to create tele link: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Generates a new unique item ID using the item_id_generator table.
     * @return New item ID
     */
    public long generateItemId() {
        String selectSql = """
            SELECT id_generator
            FROM item_id_generator
            LIMIT 1
            """;
        String updateSql = """
            UPDATE item_id_generator
            SET id_generator = id_generator + 1
            LIMIT 1
            """;
        
        try (Connection conn = databasePool.getConnection()) {
            long itemId;
            
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                 ResultSet rs = selectStmt.executeQuery()) {
                
                if (rs.next()) {
                    itemId = rs.getLong("id_generator");
                } else {
                    logger.error("item_id_generator table is empty!");
                    return 0;
                }
            }
            
            // Update the generator
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.executeUpdate();
            }
            
            return itemId;
        } catch (SQLException e) {
            logger.error("Failed to generate item ID: {}", e.getMessage(), e);
            return 0;
        }
    }
}
