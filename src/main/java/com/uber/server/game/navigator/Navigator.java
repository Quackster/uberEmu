package com.uber.server.game.navigator;

import com.uber.server.repository.NavigatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the navigator system.
 */
public class Navigator {
    private static final Logger logger = LoggerFactory.getLogger(Navigator.class);
    
    private final ConcurrentHashMap<Integer, String> publicCategories;
    private final ConcurrentHashMap<Integer, RoomCategory> privateCategories;
    private final ConcurrentHashMap<Integer, PublicItem> publicItems;
    private final NavigatorRepository navigatorRepository;
    private final com.uber.server.game.Game game;
    
    public Navigator(NavigatorRepository navigatorRepository, com.uber.server.game.Game game) {
        this.publicCategories = new ConcurrentHashMap<>();
        this.privateCategories = new ConcurrentHashMap<>();
        this.publicItems = new ConcurrentHashMap<>();
        this.navigatorRepository = navigatorRepository;
        this.game = game;
    }
    
    /**
     * Initializes the navigator by loading categories and public items.
     */
    public void initialize() {
        publicCategories.clear();
        privateCategories.clear();
        publicItems.clear();
        
        // Load public categories
        List<Map<String, Object>> pubCats = navigatorRepository.loadPublicCategories();
        for (Map<String, Object> row : pubCats) {
            int id = ((Number) row.get("id")).intValue();
            String caption = (String) row.get("caption");
            publicCategories.put(id, caption);
        }
        
        // Load private categories
        List<Map<String, Object>> privCats = navigatorRepository.loadPrivateCategories();
        for (Map<String, Object> row : privCats) {
            int id = ((Number) row.get("id")).intValue();
            String caption = (String) row.get("caption");
            int minRank = ((Number) row.get("min_rank")).intValue();
            privateCategories.put(id, new RoomCategory(id, caption, minRank));
        }
        
        // Load public items
        List<Map<String, Object>> pubItems = navigatorRepository.loadPublicItems();
        for (Map<String, Object> row : pubItems) {
            int id = ((Number) row.get("id")).intValue();
            int bannertype = ((Number) row.get("bannertype")).intValue();
            String caption = (String) row.get("caption");
            String image = (String) row.get("image");
            String imageTypeStr = (String) row.get("image_type");
            PublicItem.PublicImageType imageType = "internal".equalsIgnoreCase(imageTypeStr) 
                    ? PublicItem.PublicImageType.INTERNAL 
                    : PublicItem.PublicImageType.EXTERNAL;
            long roomId = ((Number) row.get("room_id")).longValue();
            int categoryId = ((Number) row.get("category_id")).intValue();
            int categoryParentId = ((Number) row.get("category_parent_id")).intValue();
            int ordernum = ((Number) row.get("ordernum")).intValue();
            
            publicItems.put(id, new PublicItem(id, bannertype, caption, image, imageType, roomId, categoryId, categoryParentId, ordernum));
        }
        
        logger.info("Initialized navigator with {} public categories, {} private categories, {} public items",
                publicCategories.size(), privateCategories.size(), publicItems.size());
    }
    
    public int getCountForParent(int parentId) {
        int count = 0;
        for (PublicItem item : publicItems.values()) {
            if (item.getParentId() == parentId || parentId == -1) {
                count++;
            }
        }
        return count;
    }
    
    public RoomCategory getRoomCategory(int id) {
        return privateCategories.get(id);
    }
    
    // Getters
    public Map<Integer, String> getPublicCategories() {
        return new ConcurrentHashMap<>(publicCategories);
    }
    
    public Map<Integer, RoomCategory> getPrivateCategories() {
        return new ConcurrentHashMap<>(privateCategories);
    }
    
    public Map<Integer, PublicItem> getPublicItems() {
        return new ConcurrentHashMap<>(publicItems);
    }
    
    public List<PublicItem> getPublicItemsSorted() {
        List<PublicItem> items = new ArrayList<>(publicItems.values());
        items.sort((a, b) -> Integer.compare(a.getOrderId(), b.getOrderId()));
        return items;
    }
    
    /**
     * Serializes room categories to a ServerMessage.
     * @return ServerMessage with room categories (ID 221)
     */
    public com.uber.server.messages.ServerMessage serializeRoomCategories() {
        com.uber.server.messages.ServerMessage categories = new com.uber.server.messages.ServerMessage(221);
        categories.appendInt32(privateCategories.size());
        
        for (RoomCategory roomCategory : privateCategories.values()) {
            if (roomCategory.getId() > 0) {
                categories.appendBoolean(true);
            }
            
            categories.appendInt32(roomCategory.getId());
            categories.appendStringWithBreak(roomCategory.getCaption());
        }
        
        categories.appendStringWithBreak("");
        
        return categories;
    }
    
    /**
     * Serializes public rooms to a ServerMessage.
     * @return ServerMessage with public rooms (ID 450)
     */
    public com.uber.server.messages.ServerMessage serializePublicRooms() {
        List<PublicItem> sortedItems = getPublicItemsSorted();
        
        com.uber.server.messages.ServerMessage frontpage = new com.uber.server.messages.ServerMessage(450);
        frontpage.appendInt32(getCountForParent(-1));
        
        for (PublicItem publicItem : sortedItems) {
            publicItem.serialize(frontpage);
        }
        
        return frontpage;
    }
    
    /**
     * Serializes favorite rooms for a session.
     * @param session GameClient session
     * @return ServerMessage with favorite rooms (ID 451)
     */
    public com.uber.server.messages.ServerMessage serializeFavoriteRooms(com.uber.server.game.GameClient session) {
        if (session == null || session.getHabbo() == null || game == null) {
            return new com.uber.server.messages.ServerMessage(451);
        }
        
        com.uber.server.messages.ServerMessage rooms = new com.uber.server.messages.ServerMessage(451);
        rooms.appendInt32(0); // Category
        rooms.appendInt32(6); // Mode
        rooms.appendStringWithBreak("");
        
        java.util.List<Long> favoriteRooms = session.getHabbo().getFavoriteRooms();
        rooms.appendInt32(favoriteRooms.size());
        
        for (Long roomId : favoriteRooms) {
            com.uber.server.game.rooms.RoomData data = game.getRoomManager().generateRoomData(roomId);
            if (data != null) {
                data.serialize(rooms, false);
            }
        }
        
        return rooms;
    }
    
    /**
     * Serializes recent rooms for a session.
     * @param session GameClient session
     * @return ServerMessage with recent rooms (ID 451)
     */
    public com.uber.server.messages.ServerMessage serializeRecentRooms(com.uber.server.game.GameClient session) {
        if (session == null || game == null) {
            return new com.uber.server.messages.ServerMessage(451);
        }
        
        java.util.List<Map<String, Object>> visits = navigatorRepository.loadRecentRoomVisits();
        java.util.List<com.uber.server.game.rooms.RoomData> validRecentRooms = new java.util.ArrayList<>();
        java.util.Set<Long> roomsListed = new java.util.HashSet<>();
        
        for (Map<String, Object> visit : visits) {
            long roomId = ((Number) visit.get("room_id")).longValue();
            if (roomsListed.contains(roomId)) {
                continue;
            }
            
            com.uber.server.game.rooms.RoomData data = game.getRoomManager().generateRoomData(roomId);
            if (data != null && !data.isPublicRoom()) {
                validRecentRooms.add(data);
                roomsListed.add(roomId);
            }
        }
        
        com.uber.server.messages.ServerMessage rooms = new com.uber.server.messages.ServerMessage(451);
        rooms.appendInt32(0); // Category
        rooms.appendInt32(7); // Mode
        rooms.appendStringWithBreak("");
        rooms.appendInt32(validRecentRooms.size());
        
        for (com.uber.server.game.rooms.RoomData data : validRecentRooms) {
            data.serialize(rooms, false);
        }
        
        return rooms;
    }
    
    /**
     * Serializes popular room tags.
     * @return ServerMessage with popular tags (ID 452)
     */
    public com.uber.server.messages.ServerMessage serializePopularRoomTags() {
        java.util.List<Map<String, Object>> rooms = navigatorRepository.loadPopularRoomTags();
        java.util.Map<String, Integer> tags = new ConcurrentHashMap<>();
        
        for (Map<String, Object> room : rooms) {
            String tagsStr = (String) room.get("tags");
            int usersNow = ((Number) room.get("users_now")).intValue();
            
            if (tagsStr != null && !tagsStr.isEmpty()) {
                String[] tagArray = tagsStr.split(",");
                for (String tag : tagArray) {
                    tag = tag.trim().toLowerCase();
                    if (!tag.isEmpty()) {
                        tags.put(tag, tags.getOrDefault(tag, 0) + usersNow);
                    }
                }
            }
        }
        
        // Sort tags by count (descending)
        java.util.List<java.util.Map.Entry<String, Integer>> sortedTags = new java.util.ArrayList<>(tags.entrySet());
        sortedTags.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        
        com.uber.server.messages.ServerMessage message = new com.uber.server.messages.ServerMessage(452);
        message.appendInt32(sortedTags.size());
        
        for (java.util.Map.Entry<String, Integer> entry : sortedTags) {
            message.appendStringWithBreak(entry.getKey());
            message.appendInt32(entry.getValue());
        }
        
        return message;
    }
    
    /**
     * Serializes search results.
     * @param searchQuery Search query string
     * @return ServerMessage with search results (ID 451)
     */
    public com.uber.server.messages.ServerMessage serializeSearchResults(String searchQuery) {
        if (game == null) {
            return new com.uber.server.messages.ServerMessage(451);
        }
        
        String filteredQuery = com.uber.server.util.StringUtil.filterInjectionChars(
                (searchQuery != null ? searchQuery : "").toLowerCase().trim(), true);
        
        java.util.List<com.uber.server.game.rooms.RoomData> results = new java.util.ArrayList<>();
        
        if (!filteredQuery.isEmpty()) {
            java.util.List<Map<String, Object>> roomDataList = navigatorRepository.searchRooms(filteredQuery);
            for (Map<String, Object> roomData : roomDataList) {
                com.uber.server.game.rooms.RoomData data = new com.uber.server.game.rooms.RoomData();
                data.fill(roomData);
                results.add(data);
            }
        }
        
        com.uber.server.messages.ServerMessage message = new com.uber.server.messages.ServerMessage(451);
        message.appendInt32(1); // Category
        message.appendInt32(9); // Mode
        message.appendStringWithBreak(filteredQuery);
        message.appendInt32(results.size());
        
        for (com.uber.server.game.rooms.RoomData room : results) {
            room.serialize(message, false);
        }
        
        return message;
    }
    
    /**
     * Serializes room listing for a specific mode.
     * @param session GameClient session
     * @param mode Mode (-5=rooms with friends, -4=friends' rooms, -3=own rooms, -2=high rated, -1=popular, >=0=category)
     * @return ServerMessage with room listing (ID 451)
     */
    public com.uber.server.messages.ServerMessage serializeRoomListing(com.uber.server.game.GameClient session, int mode) {
        if (game == null) {
            return new com.uber.server.messages.ServerMessage(451);
        }
        
        com.uber.server.messages.ServerMessage rooms = new com.uber.server.messages.ServerMessage(451);
        
        // Set header based on mode
        if (mode >= -1) {
            rooms.appendInt32(mode);
            rooms.appendInt32(1);
        } else if (mode == -2) {
            rooms.appendInt32(0);
            rooms.appendInt32(2); // High rated
        } else if (mode == -3) {
            rooms.appendInt32(0);
            rooms.appendInt32(5); // Own rooms
        } else if (mode == -4) {
            rooms.appendInt32(0);
            rooms.appendInt32(3); // Friends' rooms
        } else if (mode == -5) {
            rooms.appendInt32(0);
            rooms.appendInt32(4); // Rooms with friends
        }
        
        rooms.appendStringWithBreak("");
        
        java.util.List<Map<String, Object>> roomDataList = new java.util.ArrayList<>();
        
        // Load rooms based on mode
        if (mode == -5) {
            // Rooms with friends
            if (session != null && session.getHabbo() != null && session.getHabbo().getMessenger() != null) {
                java.util.List<Long> friendRoomIds = new java.util.ArrayList<>();
                for (com.uber.server.game.users.messenger.MessengerBuddy buddy : session.getHabbo().getMessenger().getBuddies()) {
                    com.uber.server.game.GameClient client = game.getClientManager().getClientByHabbo(buddy.getId());
                    if (client != null && client.getHabbo() != null && client.getHabbo().getCurrentRoomId() > 0) {
                        friendRoomIds.add(client.getHabbo().getCurrentRoomId());
                    }
                }
                if (!friendRoomIds.isEmpty()) {
                    roomDataList = navigatorRepository.loadRoomsByIds(friendRoomIds);
                }
            }
        } else if (mode == -4) {
            // Friends' rooms
            if (session != null && session.getHabbo() != null && session.getHabbo().getMessenger() != null) {
                java.util.List<String> friendNames = new java.util.ArrayList<>();
                for (com.uber.server.game.users.messenger.MessengerBuddy buddy : session.getHabbo().getMessenger().getBuddies()) {
                    friendNames.add(buddy.getUsername());
                }
                if (!friendNames.isEmpty()) {
                    for (String friendName : friendNames) {
                        roomDataList.addAll(navigatorRepository.loadRoomsByOwner(friendName));
                    }
                    // Limit to 40
                    if (roomDataList.size() > 40) {
                        roomDataList = roomDataList.subList(0, 40);
                    }
                }
            }
        } else if (mode == -3) {
            // Own rooms
            if (session != null && session.getHabbo() != null) {
                roomDataList = navigatorRepository.loadRoomsByOwner(session.getHabbo().getUsername());
            }
        } else if (mode == -2) {
            // High rated
            roomDataList = navigatorRepository.loadTopScoredRooms();
        } else if (mode == -1) {
            // Popular
            roomDataList = navigatorRepository.loadPopularRooms();
        } else {
            // Category
            roomDataList = navigatorRepository.loadRoomsByCategory(mode);
        }
        
        rooms.appendInt32(roomDataList.size());
        
        for (Map<String, Object> roomData : roomDataList) {
            com.uber.server.game.rooms.RoomData data = new com.uber.server.game.rooms.RoomData();
            data.fill(roomData);
            data.serialize(rooms, false);
        }
        
        return rooms;
    }
    
    /**
     * Serializes event listing for a category.
     * @param session GameClient session
     * @param categoryId Category ID (0 for all)
     * @return ServerMessage with event listing (ID 451)
     */
    public com.uber.server.messages.ServerMessage serializeEventListing(com.uber.server.game.GameClient session, int categoryId) {
        if (game == null) {
            return new com.uber.server.messages.ServerMessage(451);
        }
        
        com.uber.server.messages.ServerMessage message = new com.uber.server.messages.ServerMessage(451);
        message.appendInt32(categoryId);
        message.appendInt32(12); // Mode
        message.appendStringWithBreak("");
        
        // Get event rooms
        java.util.List<com.uber.server.game.rooms.Room> eventRooms = game.getRoomManager().getEventRoomsForCategory(categoryId);
        message.appendInt32(eventRooms.size());
        
        for (com.uber.server.game.rooms.Room room : eventRooms) {
            com.uber.server.game.rooms.RoomData data = room.getData();
            if (data != null) {
                com.uber.server.game.rooms.RoomEvent event = room.hasOngoingEvent() ? room.getEvent() : null;
                data.serialize(message, true, event); // Show events
            }
        }
        
        return message;
    }
}
