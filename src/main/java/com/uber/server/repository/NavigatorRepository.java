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
 * Repository for navigator database operations.
 */
public class NavigatorRepository {
    private static final Logger logger = LoggerFactory.getLogger(NavigatorRepository.class);
    private final DatabasePool databasePool;
    
    public NavigatorRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads all public categories.
     * @return List of public category data (id, caption)
     */
    public List<Map<String, Object>> loadPublicCategories() {
        String sql = "SELECT id, caption FROM navigator_pubcats WHERE enabled = '1'";
        List<Map<String, Object>> categories = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> category = new HashMap<>();
                category.put("id", rs.getInt("id"));
                category.put("caption", rs.getString("caption"));
                categories.add(category);
            }
        } catch (SQLException e) {
            logger.error("Failed to load public categories: {}", e.getMessage(), e);
        }
        
        return categories;
    }
    
    /**
     * Loads all private room categories.
     * Note: Database table name is navigator_flatcats (legacy name for room categories).
     * @return List of private category data (id, caption, min_rank)
     */
    public List<Map<String, Object>> loadPrivateCategories() {
        String sql = "SELECT id, caption, min_rank FROM navigator_flatcats WHERE enabled = '1'";
        List<Map<String, Object>> categories = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> category = new HashMap<>();
                category.put("id", rs.getInt("id"));
                category.put("caption", rs.getString("caption"));
                category.put("min_rank", rs.getInt("min_rank"));
                categories.add(category);
            }
        } catch (SQLException e) {
            logger.error("Failed to load private categories: {}", e.getMessage(), e);
        }
        
        return categories;
    }
    
    /**
     * Loads all public items.
     * @return List of public item data
     */
    public List<Map<String, Object>> loadPublicItems() {
        String sql = """
            SELECT id, bannertype, caption, image, image_type, room_id, category_id, category_parent_id, ordernum
            FROM navigator_publics
            ORDER BY ordernum ASC
            """;
        List<Map<String, Object>> items = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", rs.getInt("id"));
                item.put("bannertype", rs.getInt("bannertype"));
                item.put("caption", rs.getString("caption"));
                item.put("image", rs.getString("image"));
                item.put("image_type", rs.getString("image_type"));
                item.put("room_id", rs.getLong("room_id"));
                item.put("category_id", rs.getInt("category_id"));
                item.put("category_parent_id", rs.getInt("category_parent_id"));
                item.put("ordernum", rs.getInt("ordernum"));
                items.add(item);
            }
        } catch (SQLException e) {
            logger.error("Failed to load public items: {}", e.getMessage(), e);
        }
        
        return items;
    }
    
    /**
     * Loads recent room visits.
     * @return List of recent room visit data
     */
    public List<Map<String, Object>> loadRecentRoomVisits() {
        String sql = "SELECT * FROM user_roomvisits ORDER BY entry_timestamp DESC LIMIT 50";
        List<Map<String, Object>> visits = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> visit = new HashMap<>();
                visit.put("room_id", rs.getLong("room_id"));
                visit.put("entry_timestamp", rs.getLong("entry_timestamp"));
                visits.add(visit);
            }
        } catch (SQLException e) {
            logger.error("Failed to load recent room visits: {}", e.getMessage(), e);
        }
        
        return visits;
    }
    
    /**
     * Loads popular room tags.
     * @return List of room data with tags (tags, users_now)
     */
    public List<Map<String, Object>> loadPopularRoomTags() {
        String sql = """
            SELECT tags, users_now FROM rooms WHERE roomtype = 'private' AND users_now > 0
            ORDER BY users_now DESC LIMIT 50
            """;
        List<Map<String, Object>> rooms = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> room = new HashMap<>();
                room.put("tags", rs.getString("tags"));
                room.put("users_now", rs.getInt("users_now"));
                rooms.add(room);
            }
        } catch (SQLException e) {
            logger.error("Failed to load popular room tags: {}", e.getMessage(), e);
        }
        
        return rooms;
    }
    
    /**
     * Searches for rooms matching the query.
     * @param searchQuery Search query
     * @return List of matching room data
     */
    public List<Map<String, Object>> searchRooms(String searchQuery) {
        String sql = """
            SELECT * FROM rooms WHERE (caption LIKE ? OR tags LIKE ? OR owner LIKE ?) AND roomtype = 'private'
            ORDER BY users_now DESC LIMIT 30
            """;
        List<Map<String, Object>> rooms = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String queryPattern = searchQuery + "%";
            String tagsPattern = "%" + searchQuery + "%";
            
            stmt.setString(1, queryPattern);
            stmt.setString(2, tagsPattern);
            stmt.setString(3, queryPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(extractRoomData(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to search rooms: {}", e.getMessage(), e);
        }
        
        return rooms;
    }
    
    /**
     * Loads rooms by owner.
     * @param ownerUsername Owner username
     * @return List of room data
     */
    public List<Map<String, Object>> loadRoomsByOwner(String ownerUsername) {
        String sql = "SELECT * FROM rooms WHERE owner = ? ORDER BY id ASC";
        return loadRoomsWithQuery(sql, ownerUsername);
    }
    
    /**
     * Loads top scored rooms.
     * @return List of top scored room data
     */
    public List<Map<String, Object>> loadTopScoredRooms() {
        String sql = """
            SELECT * FROM rooms WHERE score > 0 AND roomtype = 'private'
            ORDER BY score DESC LIMIT 40
            """;
        return loadRoomsWithQuery(sql, null);
    }
    
    /**
     * Loads popular rooms (by users now).
     * @return List of popular room data
     */
    public List<Map<String, Object>> loadPopularRooms() {
        String sql = "SELECT * FROM rooms WHERE users_now > 0 AND roomtype = 'private' " +
                    "ORDER BY users_now DESC LIMIT 40";
        return loadRoomsWithQuery(sql, null);
    }
    
    /**
     * Loads rooms by category.
     * @param categoryId Category ID
     * @return List of room data
     */
    public List<Map<String, Object>> loadRoomsByCategory(int categoryId) {
        String sql = "SELECT * FROM rooms WHERE category = ? AND roomtype = 'private' " +
                    "ORDER BY users_now DESC LIMIT 40";
        return loadRoomsWithQuery(sql, categoryId);
    }
    
    /**
     * Loads rooms by IDs.
     * @param roomIds List of room IDs
     * @return List of room data
     */
    public List<Map<String, Object>> loadRoomsByIds(List<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        StringBuilder sql = new StringBuilder("SELECT * FROM rooms WHERE");
        List<Map<String, Object>> rooms = new ArrayList<>();
        
        for (int i = 0; i < roomIds.size(); i++) {
            if (i > 0) {
                sql.append(" OR");
            }
            sql.append(" id = ?");
        }
        
        sql.append(" ORDER BY users_now DESC LIMIT 40");
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < roomIds.size(); i++) {
                stmt.setLong(i + 1, roomIds.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(extractRoomData(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load rooms by IDs: {}", e.getMessage(), e);
        }
        
        return rooms;
    }
    
    /**
     * Helper method to load rooms with a query and a single parameter.
     */
    private List<Map<String, Object>> loadRoomsWithQuery(String sql, Object param) {
        List<Map<String, Object>> rooms = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (param != null) {
                if (param instanceof String s) {
                    stmt.setString(1, s);
                } else if (param instanceof Integer i) {
                    stmt.setInt(1, i);
                }
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(extractRoomData(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load rooms: {}", e.getMessage(), e);
        }
        
        return rooms;
    }
    
    /**
     * Helper method to extract room data from ResultSet (all fields needed by RoomData.fill()).
     */
    private Map<String, Object> extractRoomData(ResultSet rs) throws SQLException {
        Map<String, Object> room = new HashMap<>();
        room.put("id", rs.getLong("id"));
        room.put("roomtype", rs.getString("roomtype"));
        room.put("caption", rs.getString("caption"));
        room.put("owner", rs.getString("owner"));
        room.put("model_name", rs.getString("model_name"));
        room.put("description", rs.getString("description"));
        room.put("password", rs.getString("password"));
        room.put("tags", rs.getString("tags"));
        room.put("users_now", rs.getInt("users_now"));
        room.put("users_max", rs.getInt("users_max"));
        room.put("state", rs.getString("state"));
        room.put("category", rs.getInt("category"));
        room.put("allow_pets", rs.getInt("allow_pets"));
        room.put("allow_pets_eat", rs.getInt("allow_pets_eat"));
        room.put("allow_walkthrough", rs.getInt("allow_walkthrough"));
        room.put("icon_bg", rs.getInt("icon_bg"));
        room.put("icon_fg", rs.getInt("icon_fg"));
        room.put("icon_items", rs.getString("icon_items"));
        room.put("score", rs.getInt("score"));
        room.put("public_ccts", rs.getString("public_ccts"));
        room.put("wallpaper", rs.getString("wallpaper"));
        room.put("floor", rs.getString("floor"));
        room.put("landscape", rs.getString("landscape"));
        return room;
    }
}
