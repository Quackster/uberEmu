package com.uber.server.game.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.items.RoomItem;
import com.uber.server.game.rooms.mapping.RoomMapping;
import com.uber.server.game.rooms.mapping.SquareState;
import com.uber.server.game.rooms.services.*;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.RoomItemRepository;
import com.uber.server.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a loaded room instance.
 * Refactored to use service classes for better decoupling.
 */
public class Room {
    private static final Logger logger = LoggerFactory.getLogger(Room.class);
    
    private final long roomId;
    private final RoomData data;
    private final Game game;
    private final RoomRepository roomRepository;
    private final RoomItemRepository roomItemRepository;
    
    // Core data structures
    private final ConcurrentHashMap<Long, RoomUser> users;
    private final ConcurrentHashMap<Long, RoomItem> items;
    private final CopyOnWriteArrayList<Long> usersWithRights;
    private final ConcurrentHashMap<Long, Long> bans; // User ID -> Ban timestamp
    private final CopyOnWriteArrayList<Trade> activeTrades;
    
    // Room state
    private RoomEvent event; // Current room event
    private com.uber.server.game.items.MoodlightData moodlightData; // Moodlight data (if room has dimmer item)
    
    private int userCounter;
    private boolean keepAlive;
    private int idleTime;
    
    // Service classes
    private final RoomRightsService rightsService;
    private final RoomBanService banService;
    private final RoomTradeService tradeService;
    private final RoomValidation validation;
    private final RoomSerialization serialization;
    private final RoomItemService itemService;
    private final RoomBotService botService;
    private final RoomProcessService processService;
    private final RoomUserService userService;
    
    // Room mapping/collision system
    private RoomMapping roomMapping;
    
    public Room(long roomId, RoomData data, Game game, RoomRepository roomRepository, RoomItemRepository roomItemRepository) {
        this.roomId = roomId;
        this.data = data;
        this.game = game;
        this.roomRepository = roomRepository;
        this.roomItemRepository = roomItemRepository;
        
        // Initialize data structures
        this.users = new ConcurrentHashMap<>();
        this.items = new ConcurrentHashMap<>();
        this.usersWithRights = new CopyOnWriteArrayList<>();
        this.bans = new ConcurrentHashMap<>();
        this.activeTrades = new CopyOnWriteArrayList<>();
        
        this.userCounter = 0;
        this.keepAlive = true;
        this.idleTime = 0;
        
        // Initialize services
        this.rightsService = new RoomRightsService(this, roomRepository, usersWithRights);
        this.banService = new RoomBanService(this, bans);
        this.tradeService = new RoomTradeService(this, activeTrades, game);
        this.validation = new RoomValidation(this, users);
        this.serialization = new RoomSerialization(this, users);
        
        // Initialize services - they will access Room fields directly (same package)
        com.uber.server.game.items.MoodlightData[] moodlightDataRef = new com.uber.server.game.items.MoodlightData[1];
        moodlightDataRef[0] = null;
        this.itemService = new RoomItemService(this, items, roomItemRepository, moodlightDataRef);
        
        this.botService = new RoomBotService(this, users, validation, userCounter);
        
        boolean[] keepAliveRef = new boolean[1];
        keepAliveRef[0] = keepAlive;
        int[] idleTimeRef = new int[1];
        idleTimeRef[0] = idleTime;
        this.processService = new RoomProcessService(this, items, users, keepAliveRef, idleTimeRef);
        
        this.userService = new RoomUserService(this, users, rightsService, userCounter);
        
        // Initialize room mapping
        RoomModel model = getModel();
        if (model != null) {
            boolean allowWalkthrough = data != null && data.isAllowWalkthrough();
            this.roomMapping = new RoomMapping(this, model, allowWalkthrough);
        }
        
        // Load rights and items
        rightsService.loadRights();
        itemService.loadItems();
        // Note: Room bans are in-memory only (15 minute expiry), no database loading needed
        
        // Regenerate matrix after items are loaded
        if (roomMapping != null) {
            roomMapping.regenerateMatrix();
            roomMapping.regenerateUserMatrix();
        }
    }
    
    /**
     * Loads room items from database.
     * Delegates to RoomItemService.
     */
    public void loadItems() {
        itemService.loadItems();
        // Update moodlight data reference if needed
        // (moodlightData is set during loadItems in the service)
        
        // Regenerate collision matrix after items are loaded
        if (roomMapping != null) {
            roomMapping.regenerateMatrix();
        }
    }
    
    /**
     * Loads room rights (room managers) from database.
     * Delegates to RoomRightsService.
     */
    public void loadRights() {
        rightsService.loadRights();
    }
    
    /**
     * Adds a user to the room.
     * Delegates to RoomUserService.
     */
    public void addUserToRoom(GameClient session, boolean spectator) {
        userService.addUserToRoom(session, spectator);
        userCounter = userService.getUserCounter();
    }
    
    /**
     * Removes a user from the room.
     * Delegates to RoomUserService.
     */
    public void removeUserFromRoom(GameClient session, boolean notifyClient, boolean notifyKick) {
        userService.removeUserFromRoom(session, notifyClient, notifyKick);
    }
    
    /**
     * Sends a message to all users in the room.
     * @param message ServerMessage to send
     */
    public void sendMessage(ServerMessage message) {
        if (message == null) {
            return;
        }
        
        try {
            for (RoomUser user : users.values()) {
                if (user.isBot() || user.isSpectator()) {
                    continue;
                }
                
                GameClient client = user.getClient();
                if (client != null) {
                    client.sendMessage(message);
                }
            }
        } catch (Exception e) {
            logger.error("Error sending message to room {}: {}", roomId, e.getMessage(), e);
        }
    }
    
    /**
     * Sends a message to users with room rights.
     * @param message ServerMessage to send
     */
    public void sendMessageToUsersWithRights(ServerMessage message) {
        if (message == null) {
            return;
        }
        
        for (RoomUser user : users.values()) {
            if (user.isBot() || user.isSpectator()) {
                continue;
            }
            
            GameClient client = user.getClient();
            if (client != null && checkRights(client, false)) {
                client.sendMessage(message);
            }
        }
    }
    
    /**
     * Checks if a user has room rights.
     * Delegates to RoomRightsService.
     */
    public boolean checkRights(GameClient session) {
        return rightsService.checkRights(session);
    }
    
    /**
     * Checks if a user has room rights.
     * Delegates to RoomRightsService.
     */
    public boolean checkRights(GameClient session, boolean requireOwnership) {
        return rightsService.checkRights(session, requireOwnership);
    }
    
    /**
     * Gets a RoomUser by Habbo ID.
     * Delegates to RoomUserService.
     */
    public RoomUser getRoomUserByHabbo(long habboId) {
        return userService.getRoomUserByHabbo(habboId);
    }
    
    /**
     * Gets a RoomUser by Habbo username.
     * Delegates to RoomUserService.
     */
    public RoomUser getRoomUserByHabbo(String username) {
        return userService.getRoomUserByHabbo(username);
    }
    
    /**
     * Gets a RoomUser by virtual ID.
     * Delegates to RoomUserService.
     */
    public RoomUser getRoomUserByVirtualId(int virtualId) {
        return userService.getRoomUserByVirtualId(virtualId);
    }
    
    /**
     * Gets a room item by ID.
     * Delegates to RoomItemService.
     */
    public RoomItem getItem(long itemId) {
        return itemService.getItem(itemId);
    }
    
    /**
     * Gets list of floor items.
     * Delegates to RoomItemService.
     */
    public List<RoomItem> getFloorItems() {
        return itemService.getFloorItems();
    }
    
    /**
     * Gets list of wall items.
     * Delegates to RoomItemService.
     */
    public List<RoomItem> getWallItems() {
        return itemService.getWallItems();
    }
    
    /**
     * Serializes status updates for users in the room.
     * Delegates to RoomSerialization.
     */
    public ServerMessage serializeStatusUpdates(boolean all) {
        return serialization.serializeStatusUpdates(all);
    }
    
    /**
     * Updates the room user count in database and RoomData.
     * Delegates to RoomUserService.
     */
    public void updateUserCount() {
        int count = userService.getUserCount();
        roomRepository.updateUserCount(roomId, count);
    }
    
    /**
     * Gets the RoomModel for this room.
     * @return RoomModel object, or null if not found
     */
    public RoomModel getModel() {
        if (game == null || game.getRoomManager() == null || data == null) {
            return null;
        }
        return game.getRoomManager().getModel(data.getModelName());
    }
    
    /**
     * Gets the RoomMapping instance for collision detection.
     * @return RoomMapping instance, or null if not initialized
     */
    public RoomMapping getRoomMapping() {
        return roomMapping;
    }
    
    /**
     * Regenerates the collision matrix (call when items change).
     */
    public void regenerateMatrix() {
        if (roomMapping != null) {
            roomMapping.regenerateMatrix();
        }
    }
    
    /**
     * Regenerates the user matrix (call when users move).
     */
    public void regenerateUserMatrix() {
        if (roomMapping != null) {
            roomMapping.regenerateUserMatrix();
        }
    }
    
    /**
     * Gets the Game instance.
     * @return Game instance
     */
    public Game getGame() {
        return game;
    }
    
    /**
     * Sets the moodlight data (public for service access).
     */
    public void setMoodlightData(com.uber.server.game.items.MoodlightData data) {
        this.moodlightData = data;
    }
    
    // Getters
    public long getRoomId() {
        return roomId;
    }
    
    public RoomData getData() {
        return data;
    }
    
    /**
     * Checks if room has an ongoing event.
     * @return True if event exists
     */
    public boolean hasOngoingEvent() {
        return event != null;
    }
    
    /**
     * Gets the current room event.
     * @return RoomEvent, or null if none
     */
    public RoomEvent getEvent() {
        return event;
    }
    
    /**
     * Sets the room event.
     * @param event RoomEvent to set, or null to clear
     */
    public void setEvent(RoomEvent event) {
        this.event = event;
    }
    
    /**
     * Gets the moodlight data for this room.
     * @return MoodlightData, or null if room has no moodlight
     */
    public com.uber.server.game.items.MoodlightData getMoodlightData() {
        return moodlightData;
    }
    
    /**
     * Gets pet count in room.
     * Delegates to RoomUserService.
     */
    public int getPetCount() {
        return userService.getPetCount();
    }
    
    /**
     * Gets a pet by pet ID.
     * Delegates to RoomUserService.
     */
    public RoomUser getPet(long petId) {
        return userService.getPet(petId);
    }
    
    /**
     * Checks if a position is walkable.
     * Uses RoomMapping if available, otherwise delegates to RoomValidation.
     */
    public boolean canWalk(int x, int y, double z, boolean lastStep) {
        if (roomMapping != null) {
            return roomMapping.canWalk(x, y, z, lastStep);
        }
        return validation.canWalk(x, y, z, lastStep);
    }
    
    /**
     * Checks if a tile coordinate is valid (within room bounds).
     * @param x X coordinate
     * @param y Y coordinate
     * @return True if tile is within room bounds
     */
    public boolean validTile(int x, int y) {
        RoomModel model = getModel();
        if (model == null) {
            return false;
        }
        
        if (x < 0 || y < 0 || x >= model.getMapSizeX() || y >= model.getMapSizeY()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Deploys a pet as a bot in the room.
     * Delegates to RoomBotService.
     */
    public RoomUser deployPet(com.uber.server.game.pets.Pet pet, int x, int y) {
        RoomUser result = botService.deployPet(pet, x, y);
        userCounter = botService.getUserCounter();
        return result;
    }
    
    /**
     * Initializes bots for this room.
     * Delegates to RoomBotService.
     */
    public void initBots() {
        botService.initBots();
        userCounter = botService.getUserCounter();
    }
    
    /**
     * Initializes pets for this room.
     * Delegates to RoomBotService.
     */
    public void initPets() {
        botService.initPets();
        userCounter = botService.getUserCounter();
    }
    
    /**
     * Deploys a bot to the room.
     * Delegates to RoomBotService.
     */
    public RoomUser deployBot(com.uber.server.game.bots.RoomBot bot) {
        RoomUser result = botService.deployBot(bot);
        userCounter = botService.getUserCounter();
        return result;
    }
    
    /**
     * Deploys a bot to the room (with optional pet data).
     * Delegates to RoomBotService.
     */
    public RoomUser deployBot(com.uber.server.game.bots.RoomBot bot, com.uber.server.game.pets.Pet petData) {
        RoomUser result = botService.deployBot(bot, petData);
        userCounter = botService.getUserCounter();
        return result;
    }
    
    /**
     * Removes a bot/pet from room.
     * Delegates to RoomBotService.
     */
    public void removeBot(int virtualId, boolean kicked) {
        botService.removeBot(virtualId, kicked);
    }
    
    /**
     * Called when a user says something in the room.
     * Delegates to RoomUserService.
     */
    public void onUserSay(RoomUser user, String message, boolean shout) {
        userService.onUserSay(user, message, shout);
    }
    
    /**
     * Calculates tile distance between two coordinates.
     * Delegates to RoomValidation.
     */
    public int tileDistance(int x1, int y1, int x2, int y2) {
        return validation.tileDistance(x1, y1, x2, y2);
    }
    
    /**
     * Makes all users in the room turn their heads to look at a coordinate.
     * Delegates to RoomUserService.
     */
    public void turnHeads(int x, int y, long senderId) {
        userService.turnHeads(x, y, senderId);
    }
    
    /**
     * Checks if a square has users (excluding last step positions).
     * Delegates to RoomValidation.
     */
    public boolean squareHasUsers(int x, int y, boolean lastStep) {
        return validation.squareHasUsers(x, y, lastStep);
    }
    
    /**
     * Checks if a square has users.
     * Delegates to RoomValidation.
     */
    public boolean squareHasUsers(int x, int y) {
        return validation.squareHasUsers(x, y);
    }
    
    /**
     * Checks if two tiles are touching (adjacent or same).
     * Delegates to RoomValidation.
     */
    public boolean tilesTouching(int x1, int y1, int x2, int y2) {
        return validation.tilesTouching(x1, y1, x2, y2);
    }
    
    public Map<Long, RoomUser> getUsers() {
        return new ConcurrentHashMap<>(users);
    }
    
    public int getUserCount() {
        return userService.getUserCount();
    }
    
    public Map<Long, RoomItem> getItems() {
        return new ConcurrentHashMap<>(items);
    }
    
    public boolean isPublicRoom() {
        return data != null && "public".equalsIgnoreCase(data.getType());
    }
    
    public boolean canTradeInRoom() {
        return !isPublicRoom();
    }
    
    public String getOwner() {
        return data != null ? data.getOwner() : "";
    }
    
    public boolean getKeepAlive() {
        return keepAlive;
    }
    
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }
    
    /**
     * Sets the idle time (public for service access).
     */
    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }
    
    public List<Long> getUsersWithRights() {
        return rightsService.getUsersWithRights();
    }
    
    public boolean hasRights(long userId) {
        return rightsService.hasRights(userId);
    }
    
    /**
     * Checks if a user has an active trade.
     * Delegates to RoomTradeService.
     */
    public boolean hasActiveTrade(long userId) {
        return tradeService.hasActiveTrade(userId);
    }
    
    /**
     * Gets a user's active trade.
     * Delegates to RoomTradeService.
     */
    public Trade getUserTrade(long userId) {
        return tradeService.getUserTrade(userId);
    }
    
    /**
     * Tries to start a trade between two users.
     * Delegates to RoomTradeService.
     */
    public void tryStartTrade(RoomUser userOne, RoomUser userTwo) {
        tradeService.tryStartTrade(userOne, userTwo);
    }
    
    /**
     * Tries to stop a trade for a user.
     * Delegates to RoomTradeService.
     */
    public void tryStopTrade(long userId) {
        tradeService.tryStopTrade(userId);
    }
    
    /**
     * Removes an active trade.
     * Delegates to RoomTradeService.
     */
    public void removeActiveTrade(Trade trade) {
        tradeService.removeActiveTrade(trade);
    }
    
    /**
     * Checks if a user is banned from the room.
     * Delegates to RoomBanService.
     */
    public boolean userIsBanned(long userId) {
        return banService.userIsBanned(userId);
    }
    
    /**
     * Checks if a user's ban has expired.
     * Delegates to RoomBanService.
     */
    public boolean hasBanExpired(long userId) {
        return banService.hasBanExpired(userId);
    }
    
    /**
     * Removes a ban from a user.
     * Delegates to RoomBanService.
     */
    public void removeBan(long userId) {
        banService.removeBan(userId);
    }
    
    /**
     * Adds a ban to a user.
     * Delegates to RoomBanService.
     */
    public void addBan(long userId) {
        banService.addBan(userId);
    }
    
    /**
     * Destroys the room (cleanup).
     */
    public void destroy() {
        stopProcessRoutine();
        tradeService.closeAllTrades();
        var composer = new com.uber.server.messages.outgoing.rooms.RoomEntryErrorMessageEventComposer();
        sendMessage(composer.compose());
        keepAlive = false;
        users.clear();
        // Items can remain for room reloading
    }
    
    /**
     * Counts items by interaction type.
     * Delegates to RoomItemService.
     */
    public int itemCountByType(String interactionType) {
        return itemService.itemCountByType(interactionType);
    }
    
    /**
     * Validates and normalizes wall position string.
     * Delegates to RoomValidation.
     */
    public String wallPositionCheck(String wallPosition) {
        return validation.wallPositionCheck(wallPosition);
    }
    
    /**
     * Places a floor item in the room.
     * Delegates to RoomItemService.
     */
    public boolean setFloorItem(GameClient session, RoomItem item, int newX, int newY, int newRot, boolean newItem) {
        return itemService.setFloorItem(session, item, newX, newY, newRot, newItem);
    }
    
    /**
     * Places a wall item in the room.
     * Delegates to RoomItemService.
     */
    public boolean setWallItem(GameClient session, RoomItem item) {
        boolean result = itemService.setWallItem(session, item);
        // Update moodlight data reference if needed (handled in service)
        return result;
    }
    
    /**
     * Removes furniture from the room.
     * Delegates to RoomItemService.
     */
    public void removeFurniture(GameClient session, long itemId) {
        itemService.removeFurniture(session, itemId);
    }
    
    /**
     * Updates room settings.
     * @param name Room name
     * @param description Room description
     * @param state Room state (0=open, 1=locked, 2=password)
     * @param password Room password
     * @param maxUsers Maximum users
     * @param categoryId Category ID
     * @param tags Room tags
     * @param allowPets Allow pets flag
     * @param allowPetsEating Allow pets to eat flag
     * @param allowWalkthrough Allow walkthrough flag
     * @return True if update was successful
     */
    public boolean updateRoomSettings(String name, String description, int state, String password,
                                     int maxUsers, int categoryId, List<String> tags,
                                     boolean allowPets, boolean allowPetsEating, boolean allowWalkthrough) {
        if (data == null) {
            return false;
        }
        
        // Update data object
        data.setName(name);
        data.setDescription(description);
        data.setState(state);
        data.setPassword(password);
        data.setUsersMax(maxUsers);
        data.setCategory(categoryId);
        data.setTags(tags);
        data.setAllowPets(allowPets);
        data.setAllowPetsEating(allowPetsEating);
        data.setAllowWalkthrough(allowWalkthrough);
        
        // Format state string
        String stateStr = "open";
        if (state == 1) {
            stateStr = "locked";
        } else if (state == 2) {
            stateStr = "password";
        }
        
        // Format tags string
        StringBuilder tagsStr = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) tagsStr.append(",");
            tagsStr.append(tags.get(i));
        }
        
        // Update in database
        return roomRepository.updateRoomSettings(roomId, name, description, password, categoryId,
                                                 stateStr, tagsStr.toString(), maxUsers,
                                                 allowPets ? 1 : 0, allowPetsEating ? 1 : 0,
                                                 allowWalkthrough ? 1 : 0);
    }
    
    /**
     * Adds a room right (room manager).
     * Delegates to RoomRightsService.
     */
    public boolean addRight(long userId) {
        return rightsService.addRight(userId);
    }
    
    /**
     * Removes a room right.
     * Delegates to RoomRightsService.
     */
    public boolean removeRight(long userId) {
        return rightsService.removeRight(userId);
    }
    
    /**
     * Removes all room rights.
     * Delegates to RoomRightsService.
     */
    public boolean removeAllRights() {
        return rightsService.removeAllRights();
    }
    
    /**
     * Starts the room processing routine.
     * Delegates to RoomProcessService.
     */
    public void startProcessRoutine() {
        processService.startProcessRoutine();
    }
    
    /**
     * Stops the room processing routine.
     * Delegates to RoomProcessService.
     */
    public void stopProcessRoutine() {
        processService.stopProcessRoutine();
    }
    
    /**
     * Gets furniture items on a specific square.
     * @param x X coordinate
     * @param y Y coordinate
     * @return List of RoomItems on that square, or empty list if none
     */
    public List<com.uber.server.game.items.RoomItem> getFurniObjects(int x, int y) {
        List<com.uber.server.game.items.RoomItem> results = new ArrayList<>();
        
        for (com.uber.server.game.items.RoomItem item : items.values()) {
            if (!item.isFloorItem()) {
                continue;
            }
            
            // Check if item is at this position
            if (item.getX() == x && item.getY() == y) {
                results.add(item);
                continue;
            }
            
            // Check if item affects this square (for multi-tile items)
            // This is a simplified version - full implementation would use GetAffectedTiles
            com.uber.server.game.items.Item baseItem = item.getBaseItem();
            if (baseItem != null) {
                int length = baseItem.getLength();
                int width = baseItem.getWidth();
                int rot = item.getRot();
                int itemX = item.getX();
                int itemY = item.getY();
                
                // Check if (x, y) is within the item's affected area
                boolean isAffected = false;
                if (length > 1 || width > 1) {
                    // Simple check for multi-tile items
                    if (rot == 0 || rot == 4) {
                        // North/South rotation
                        if (x >= itemX && x < itemX + width && y >= itemY && y < itemY + length) {
                            isAffected = true;
                        }
                    } else if (rot == 2 || rot == 6) {
                        // East/West rotation
                        if (x >= itemX && x < itemX + length && y >= itemY && y < itemY + width) {
                            isAffected = true;
                        }
                    }
                }
                
                if (isAffected) {
                    results.add(item);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Updates user status (sitting, laying, etc.) based on position.
     * @param user RoomUser to update
     */
    public void updateUserStatus(RoomUser user) {
        // Remove lay/sit statuses first
        if (user.hasStatus("lay") || user.hasStatus("sit")) {
            user.removeStatus("lay");
            user.removeStatus("sit");
            user.setUpdateNeeded(true);
        }
        
        // Update Z coordinate based on floor height
        double newZ = 0.0;
        if (roomMapping != null) {
            newZ = roomMapping.sqAbsoluteHeight(user.getX(), user.getY());
        } else {
            RoomModel model = getModel();
            if (model != null && user.getX() >= 0 && user.getX() < model.getMapSizeX() &&
                user.getY() >= 0 && user.getY() < model.getMapSizeY()) {
                newZ = model.getSqFloorHeight()[user.getX()][user.getY()];
            }
        }
        
        if (newZ != user.getZ()) {
            user.setZ(newZ);
            user.setUpdateNeeded(true);
        }
        
        // Check if square is a seat (from room model)
        RoomModel model = getModel();
        if (model != null && user.getX() >= 0 && user.getX() < model.getMapSizeX() &&
            user.getY() >= 0 && user.getY() < model.getMapSizeY()) {
            
            SquareState squareState = model.getSqState()[user.getX()][user.getY()];
            if (squareState == SquareState.SEAT) {
                if (!user.hasStatus("sit")) {
                    user.addStatus("sit", "1.0");
                }
                
                user.setZ(model.getSqFloorHeight()[user.getX()][user.getY()]);
                user.setRotHead(model.getSqSeatRot()[user.getX()][user.getY()]);
                user.setRotBody(model.getSqSeatRot()[user.getX()][user.getY()]);
                user.setUpdateNeeded(true);
            }
        }
        
        // Check for furniture items on the square
        List<com.uber.server.game.items.RoomItem> itemsOnSquare = getFurniObjects(user.getX(), user.getY());
        
        for (com.uber.server.game.items.RoomItem item : itemsOnSquare) {
            com.uber.server.game.items.Item baseItem = item.getBaseItem();
            if (baseItem == null) {
                continue;
            }
            
            // Check if item is a seat
            if (baseItem.canSit()) {
                if (!user.hasStatus("sit")) {
                    user.addStatus("sit", String.format("%.1f", baseItem.getHeight()).replace(',', '.'));
                }
                
                user.setZ(item.getZ());
                user.setRotHead(item.getRot());
                user.setRotBody(item.getRot());
                user.setUpdateNeeded(true);
            }
            
            // Check if item is a bed
            if ("bed".equalsIgnoreCase(baseItem.getInteractionType())) {
                if (!user.hasStatus("lay")) {
                    user.addStatus("lay", String.format("%.1f", baseItem.getHeight()).replace(',', '.') + " null");
                }
                
                user.setZ(item.getZ());
                user.setRotHead(item.getRot());
                user.setRotBody(item.getRot());
                user.setUpdateNeeded(true);
            }
        }
    }
    
}
