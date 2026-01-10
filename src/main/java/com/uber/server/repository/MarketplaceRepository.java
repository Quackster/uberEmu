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
 * Repository for marketplace database operations.
 */
public class MarketplaceRepository {
    private static final Logger logger = LoggerFactory.getLogger(MarketplaceRepository.class);
    private final DatabasePool databasePool;
    
    public MarketplaceRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Creates a new marketplace offer.
     * @param itemId Item ID
     * @param userId User ID selling the item
     * @param askingPrice Asking price
     * @param totalPrice Total price (including commission)
     * @param publicName Public name of the item
     * @param spriteId Sprite ID
     * @param itemType Item type (1 or 2)
     * @param timestamp Timestamp
     * @param extraData Extra data
     * @return True if offer was created successfully
     */
    public boolean createOffer(long itemId, long userId, int askingPrice, int totalPrice, 
                              String publicName, int spriteId, int itemType, long timestamp, 
                              String extraData) {
        String sql = """
            INSERT INTO catalog_marketplace_offers (item_id, user_id, asking_price, total_price, public_name, sprite_id, item_type, timestamp, extra_data)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            stmt.setLong(2, userId);
            stmt.setInt(3, askingPrice);
            stmt.setInt(4, totalPrice);
            stmt.setString(5, publicName);
            stmt.setInt(6, spriteId);
            stmt.setInt(7, itemType);
            stmt.setLong(8, timestamp);
            stmt.setString(9, extraData);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to create marketplace offer: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets marketplace offers with filters.
     * @param minCost Minimum cost filter (negative to ignore)
     * @param maxCost Maximum cost filter (negative to ignore)
     * @param searchQuery Search query (empty/null to ignore)
     * @param orderByPriceDesc True to order by price descending, false for ascending
     * @param minTimestamp Minimum timestamp (for filtering old offers)
     * @return List of offer data
     */
    public List<Map<String, Object>> getOffers(int minCost, int maxCost, String searchQuery, 
                                                boolean orderByPriceDesc, long minTimestamp) {
        StringBuilder sql = new StringBuilder("SELECT * FROM catalog_marketplace_offers WHERE state = '1' AND timestamp >= ?");
        List<Map<String, Object>> offers = new ArrayList<>();
        
        if (minCost >= 0) {
            sql.append(" AND total_price >= ?");
        }
        if (maxCost >= 0) {
            sql.append(" AND total_price <= ?");
        }
        if (searchQuery != null && !searchQuery.isEmpty()) {
            sql.append(" AND public_name LIKE ?");
        }
        
        sql.append(orderByPriceDesc ? " ORDER BY asking_price DESC" : " ORDER BY asking_price ASC");
        sql.append(" LIMIT 100");
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            stmt.setLong(paramIndex++, minTimestamp);
            
            if (minCost >= 0) {
                stmt.setInt(paramIndex++, minCost);
            }
            if (maxCost >= 0) {
                stmt.setInt(paramIndex++, maxCost);
            }
            if (searchQuery != null && !searchQuery.isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchQuery + "%");
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> offer = new HashMap<>();
                    offer.put("offer_id", rs.getLong("offer_id"));
                    offer.put("item_id", rs.getLong("item_id"));
                    offer.put("user_id", rs.getLong("user_id"));
                    offer.put("asking_price", rs.getInt("asking_price"));
                    offer.put("total_price", rs.getInt("total_price"));
                    offer.put("public_name", rs.getString("public_name"));
                    offer.put("sprite_id", rs.getInt("sprite_id"));
                    offer.put("item_type", rs.getInt("item_type"));
                    offer.put("timestamp", rs.getLong("timestamp"));
                    offer.put("state", rs.getInt("state"));
                    offer.put("extra_data", rs.getString("extra_data"));
                    offers.add(offer);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get marketplace offers: {}", e.getMessage(), e);
        }
        
        return offers;
    }
    
    /**
     * Gets all offers for a specific user.
     * @param userId User ID
     * @return List of user's offers
     */
    public List<Map<String, Object>> getUserOffers(long userId) {
        String sql = "SELECT * FROM catalog_marketplace_offers WHERE user_id = ?";
        List<Map<String, Object>> offers = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> offer = new HashMap<>();
                    offer.put("offer_id", rs.getLong("offer_id"));
                    offer.put("item_id", rs.getLong("item_id"));
                    offer.put("user_id", rs.getLong("user_id"));
                    offer.put("asking_price", rs.getInt("asking_price"));
                    offer.put("total_price", rs.getInt("total_price"));
                    offer.put("public_name", rs.getString("public_name"));
                    offer.put("sprite_id", rs.getInt("sprite_id"));
                    offer.put("item_type", rs.getInt("item_type"));
                    offer.put("timestamp", rs.getLong("timestamp"));
                    offer.put("state", rs.getInt("state"));
                    offer.put("extra_data", rs.getString("extra_data"));
                    offers.add(offer);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get user marketplace offers: {}", e.getMessage(), e);
        }
        
        return offers;
    }
    
    /**
     * Gets total profits from sold offers for a user.
     * @param userId User ID
     * @return Total profits from sold offers (state = 2)
     */
    public int getUserProfits(long userId) {
        String sql = """
            SELECT SUM(asking_price) as total_profit FROM catalog_marketplace_offers
            WHERE state = '2' AND user_id = ?
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Object totalProfit = rs.getObject("total_profit");
                    if (totalProfit != null) {
                        return rs.getInt("total_profit");
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get user marketplace profits: {}", e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Gets a specific marketplace offer by offer ID.
     * @param offerId Offer ID
     * @return Offer data map, or null if not found
     */
    public Map<String, Object> getOffer(long offerId) {
        String sql = "SELECT * FROM catalog_marketplace_offers WHERE offer_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, offerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> offer = new HashMap<>();
                    offer.put("offer_id", rs.getLong("offer_id"));
                    offer.put("item_id", rs.getLong("item_id"));
                    offer.put("user_id", rs.getLong("user_id"));
                    offer.put("asking_price", rs.getInt("asking_price"));
                    offer.put("total_price", rs.getInt("total_price"));
                    offer.put("public_name", rs.getString("public_name"));
                    offer.put("sprite_id", rs.getInt("sprite_id"));
                    offer.put("item_type", rs.getInt("item_type"));
                    offer.put("timestamp", rs.getLong("timestamp"));
                    offer.put("state", rs.getInt("state"));
                    offer.put("extra_data", rs.getString("extra_data"));
                    return offer;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get marketplace offer {}: {}", offerId, e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Deletes a marketplace offer.
     * @param offerId Offer ID
     * @return True if deletion was successful
     */
    public boolean deleteOffer(long offerId) {
        String sql = "DELETE FROM catalog_marketplace_offers WHERE offer_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, offerId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete marketplace offer {}: {}", offerId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates the state of a marketplace offer.
     * @param offerId Offer ID
     * @param newState New state (1 = active, 2 = sold, 3 = expired)
     * @return True if update was successful
     */
    public boolean updateOfferState(long offerId, int newState) {
        String sql = "UPDATE catalog_marketplace_offers SET state = ? WHERE offer_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, newState);
            stmt.setLong(2, offerId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update marketplace offer state for offer {}: {}", offerId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deletes all sold offers (state = 2) for a user.
     * @param userId User ID
     * @return Number of offers deleted
     */
    public int deleteSoldOffers(long userId) {
        String sql = "DELETE FROM catalog_marketplace_offers WHERE user_id = ? AND state = '2'";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to delete sold marketplace offers for user {}: {}", userId, e.getMessage(), e);
            return 0;
        }
    }
}
