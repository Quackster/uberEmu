package com.uber.server.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.items.RoomItem;
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
 * Ported from HabboHotel/Rooms/Room.cs
 */
public class Room {
    private static final Logger logger = LoggerFactory.getLogger(Room.class);
    
    private final long roomId;
    private final RoomData data;
    private final Game game;
    private final RoomRepository roomRepository;
    private final RoomItemRepository roomItemRepository;
    
    private final ConcurrentHashMap<Long, RoomUser> users;
    private final ConcurrentHashMap<Long, RoomItem> items;
    private final CopyOnWriteArrayList<Long> usersWithRights;
    private final ConcurrentHashMap<Long, Long> bans; // User ID -> Ban timestamp
    private final CopyOnWriteArrayList<Trade> activeTrades;
    
    private RoomEvent event; // Current room event
    private com.uber.server.items.MoodlightData moodlightData; // Moodlight data (if room has dimmer item)
    
    private int userCounter;
    private boolean keepAlive;
    
    public Room(long roomId, RoomData data, Game game, RoomRepository roomRepository, RoomItemRepository roomItemRepository) {
        this.roomId = roomId;
        this.data = data;
        this.game = game;
        this.roomRepository = roomRepository;
        this.roomItemRepository = roomItemRepository;
        
        this.users = new ConcurrentHashMap<>();
        this.items = new ConcurrentHashMap<>();
        this.usersWithRights = new CopyOnWriteArrayList<>();
        this.bans = new ConcurrentHashMap<>();
        this.activeTrades = new CopyOnWriteArrayList<>();
        
        this.userCounter = 0;
        this.keepAlive = true;
        
        // Load rights and items
        loadRights();
        loadItems();
        // Note: Room bans are in-memory only (15 minute expiry), no database loading needed
    }
    
    /**
     * Loads room items from database.
     * Ported from HabboHotel/Rooms/Room.cs LoadFurniture()
     */
    public void loadItems() {
        items.clear();
        
        List<Map<String, Object>> itemData = roomItemRepository.loadRoomItems(roomId);
        for (Map<String, Object> row : itemData) {
            try {
                long id = ((Number) row.get("id")).longValue();
                long baseItem = ((Number) row.get("base_item")).longValue();
                String extraData = (String) row.get("extra_data");
                int x = ((Number) row.get("x")).intValue();
                int y = ((Number) row.get("y")).intValue();
                double z = ((Number) row.get("z")).doubleValue();
                int rot = ((Number) row.get("rot")).intValue();
                String wallPos = (String) row.get("wall_pos");
                
                RoomItem item = new RoomItem(id, roomId, baseItem, extraData, x, y, z, rot, wallPos, game);
                items.put(id, item);
                
                // Initialize moodlight data if this is a dimmer item
                com.uber.server.items.Item baseItemObj = game.getItemManager().getItem(baseItem);
                if (baseItemObj != null && "dimmer".equalsIgnoreCase(baseItemObj.getInteractionType())) {
                    try {
                        moodlightData = new com.uber.server.items.MoodlightData(id, roomItemRepository);
                    } catch (Exception e) {
                        logger.warn("Failed to load moodlight data for item {}: {}", id, e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to load room item: {}", e.getMessage(), e);
            }
        }
        
        logger.debug("Loaded {} items for room {}", items.size(), roomId);
    }
    
    /**
     * Loads room rights (room managers) from database.
     * Ported from HabboHotel/Rooms/Room.cs LoadRights()
     */
    public void loadRights() {
        usersWithRights.clear();
        
        List<Long> rights = roomRepository.loadRoomRights(roomId);
        usersWithRights.addAll(rights);
    }
    
    /**
     * Adds a user to the room.
     * Ported from HabboHotel/Rooms/Room.cs AddUserToRoom()
     * @param session GameClient session
     * @param spectator If true, user enters as spectator
     */
    public void addUserToRoom(GameClient session, boolean spectator) {
        if (session == null || session.getHabbo() == null) {
            return;
        }
        
        Habbo habbo = session.getHabbo();
        RoomUser user = new RoomUser(habbo.getId(), roomId, userCounter++, game);
        
        if (spectator) {
            user.setSpectator(true);
        } else {
            // Get room model for door position
            RoomModel model = getModel();
            if (model != null) {
                user.setPos(model.getDoorX(), model.getDoorY(), model.getDoorZ());
                user.setRot(model.getDoorDir());
            }
            
            // Add status based on rights
            if (checkRights(session, true)) {
                user.addStatus("flatcrtl", "useradmin");
            } else if (checkRights(session, false)) {
                user.addStatus("flatcrtl", "");
            }
            
            // Handle teleporting
            if (habbo.isTeleporting()) {
                RoomItem teleporter = getItem(habbo.getTeleporterId());
                if (teleporter != null) {
                    user.setPos(teleporter.getX(), teleporter.getY(), teleporter.getZ());
                    user.setRot(teleporter.getRot());
                    teleporter.setInteractingUser2(habbo.getId());
                    teleporter.setExtraData("2");
                    teleporter.updateState(false, true);
                }
            }
            
            habbo.setTeleporting(false);
            habbo.setTeleporterId(0);
            
            // Send enter message to room
            ServerMessage enterMessage = new ServerMessage(28);
            enterMessage.appendInt32(1);
            user.serialize(enterMessage);
            sendMessage(enterMessage);
        }
        
        users.put(habbo.getId(), user);
        
        // Update Habbo's current room
        habbo.setCurrentRoomId(roomId);
        
        // TODO: Call habbo.onEnterRoom() when implemented
        // habbo.onEnterRoom(roomId);
        
        if (!spectator) {
            updateUserCount();
            
            // TODO: Notify bots when bot system is ported
            // for (RoomUser bot : users.values()) {
            //     if (bot.isBot() && bot.getBotAI() != null) {
            //         bot.getBotAI().onUserEnterRoom(user);
            //     }
            // }
        }
    }
    
    /**
     * Removes a user from the room.
     * Ported from HabboHotel/Rooms/Room.cs RemoveUserFromRoom()
     * @param session GameClient session
     * @param notifyClient If true, sends leave message to client
     * @param notifyKick If true, sends kick notification
     */
    public void removeUserFromRoom(GameClient session, boolean notifyClient, boolean notifyKick) {
        if (session == null || session.getHabbo() == null) {
            return;
        }
        
        Habbo habbo = session.getHabbo();
        RoomUser user = users.remove(habbo.getId());
        
        if (user == null) {
            return;
        }
        
        if (notifyClient) {
            if (notifyKick) {
                ServerMessage kickMessage = new ServerMessage(33);
                kickMessage.appendInt32(4008);
                session.sendMessage(kickMessage);
            }
            
            ServerMessage leaveMessage = new ServerMessage(18);
            session.sendMessage(leaveMessage);
        }
        
        if (!user.isSpectator()) {
            // TODO: Update UserMatrix when pathfinding is implemented
            
            // Send leave message to room
            ServerMessage leaveUpdate = new ServerMessage(29);
            leaveUpdate.appendUInt(user.getVirtualId());
            sendMessage(leaveUpdate);
            
            // Stop active trades
            if (hasActiveTrade(habbo.getId())) {
                tryStopTrade(habbo.getId());
            }
            
            // Clear room event if owner left
            if (habbo.getUsername().toLowerCase().equals(data.getOwner().toLowerCase())) {
                if (hasOngoingEvent()) {
                    event = null;
                    ServerMessage message = new ServerMessage(370);
                    message.appendStringWithBreak("-1");
                    sendMessage(message);
                }
            }
            
            // Update Habbo's current room
            habbo.setCurrentRoomId(0);
            
            // TODO: Call habbo.onLeaveRoom() when implemented
            // habbo.onLeaveRoom();
            
            updateUserCount();
            
            // TODO: Notify bots when bot system is ported
        }
    }
    
    /**
     * Sends a message to all users in the room.
     * Ported from HabboHotel/Rooms/Room.cs SendMessage()
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
     * Ported from HabboHotel/Rooms/Room.cs SendMessageToUsersWithRights()
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
     * Ported from HabboHotel/Rooms/Room.cs CheckRights()
     * @param session GameClient session
     * @return True if user has rights
     */
    public boolean checkRights(GameClient session) {
        return checkRights(session, false);
    }
    
    /**
     * Checks if a user has room rights.
     * Ported from HabboHotel/Rooms/Room.cs CheckRights(GameClient, bool)
     * @param session GameClient session
     * @param requireOwnership If true, only owner and admins have rights
     * @return True if user has rights
     */
    public boolean checkRights(GameClient session, boolean requireOwnership) {
        if (session == null || session.getHabbo() == null) {
            return false;
        }
        
        Habbo habbo = session.getHabbo();
        
        // Owner always has rights
        if (habbo.getUsername().toLowerCase().equals(data.getOwner().toLowerCase())) {
            return true;
        }
        
        // Check admin fuses
        if (habbo.hasFuse("fuse_admin") || habbo.hasFuse("fuse_any_room_controller")) {
            return true;
        }
        
        if (!requireOwnership) {
            // Check room rights fuse
            if (habbo.hasFuse("fuse_any_room_rights")) {
                return true;
            }
            
            // Check if user has room rights
            if (usersWithRights.contains(habbo.getId())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets a RoomUser by Habbo ID.
     * Ported from HabboHotel/Rooms/Room.cs GetRoomUserByHabbo(uint)
     * @param habboId Habbo user ID
     * @return RoomUser object, or null if not found
     */
    public RoomUser getRoomUserByHabbo(long habboId) {
        for (RoomUser user : users.values()) {
            if (user.isBot() || user.isSpectator()) {
                continue;
            }
            
            if (user.getHabboId() == habboId) {
                return user;
            }
        }
        
        return null;
    }
    
    /**
     * Gets a RoomUser by Habbo username.
     * Ported from HabboHotel/Rooms/Room.cs GetRoomUserByHabbo(string)
     * @param username Username
     * @return RoomUser object, or null if not found
     */
    public RoomUser getRoomUserByHabbo(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        
        String lowerUsername = username.toLowerCase();
        for (RoomUser user : users.values()) {
            if (user.isBot() || user.isSpectator()) {
                continue;
            }
            
            GameClient client = user.getClient();
            if (client != null && client.getHabbo() != null) {
                if (client.getHabbo().getUsername().toLowerCase().equals(lowerUsername)) {
                    return user;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Gets a RoomUser by virtual ID.
     * Ported from HabboHotel/Rooms/Room.cs GetRoomUserByVirtualId()
     * @param virtualId Virtual ID
     * @return RoomUser object, or null if not found
     */
    public RoomUser getRoomUserByVirtualId(int virtualId) {
        for (RoomUser user : users.values()) {
            if (user.getVirtualId() == virtualId) {
                return user;
            }
        }
        
        return null;
    }
    
    /**
     * Gets a room item by ID.
     * Ported from HabboHotel/Rooms/Room.cs GetItem()
     * @param itemId Item ID
     * @return RoomItem object, or null if not found
     */
    public RoomItem getItem(long itemId) {
        return items.get(itemId);
    }
    
    /**
     * Gets list of floor items.
     * @return List of floor RoomItems
     */
    public List<RoomItem> getFloorItems() {
        List<RoomItem> floorItems = new ArrayList<>();
        for (RoomItem item : items.values()) {
            if (item.isFloorItem()) {
                floorItems.add(item);
            }
        }
        return floorItems;
    }
    
    /**
     * Gets list of wall items.
     * @return List of wall RoomItems
     */
    public List<RoomItem> getWallItems() {
        List<RoomItem> wallItems = new ArrayList<>();
        for (RoomItem item : items.values()) {
            if (item.isWallItem()) {
                wallItems.add(item);
            }
        }
        return wallItems;
    }
    
    /**
     * Serializes status updates for users in the room.
     * Ported from HabboHotel/Rooms/Room.cs SerializeStatusUpdates()
     * @param all If true, serializes all users; if false, only users needing updates
     * @return ServerMessage with status updates (ID 34), or null if no updates
     */
    public ServerMessage serializeStatusUpdates(boolean all) {
        List<RoomUser> usersToUpdate = new ArrayList<>();
        
        for (RoomUser user : users.values()) {
            if (!all) {
                if (!user.isUpdateNeeded()) {
                    continue;
                }
                user.setUpdateNeeded(false);
            }
            
            if (!user.isSpectator()) {
                usersToUpdate.add(user);
            }
        }
        
        if (usersToUpdate.isEmpty()) {
            return null;
        }
        
        ServerMessage message = new ServerMessage(34);
        message.appendInt32(usersToUpdate.size());
        
        for (RoomUser user : usersToUpdate) {
            user.serializeStatus(message);
        }
        
        return message;
    }
    
    /**
     * Updates the room user count in database and RoomData.
     * Ported from HabboHotel/Rooms/Room.cs UpdateUserCount()
     */
    public void updateUserCount() {
        int count = 0;
        for (RoomUser user : users.values()) {
            if (!user.isBot() && !user.isSpectator()) {
                count++;
            }
        }
        
        // Update in database
        roomRepository.updateUserCount(roomId, count);
        
        // Update RoomData (if needed - RoomData should be updated through RoomData methods)
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
    public com.uber.server.items.MoodlightData getMoodlightData() {
        return moodlightData;
    }
    
    /**
     * Gets pet count in room.
     * @return Number of pets in room
     */
    public int getPetCount() {
        int count = 0;
        for (RoomUser user : users.values()) {
            if (user.isPet()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Gets a pet by pet ID.
     * @param petId Pet ID
     * @return RoomUser representing the pet, or null if not found
     */
    public RoomUser getPet(long petId) {
        for (RoomUser user : users.values()) {
            if (user.isPet() && user.getPetData() != null && user.getPetData().getPetId() == petId) {
                return user;
            }
        }
        return null;
    }
    
    /**
     * Checks if a position is walkable (simplified version).
     * TODO: Full pathfinding implementation when pathfinding is ported (Phase 11)
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate (unused for now)
     * @param lastStep Whether this is the last step in a path
     * @return True if walkable
     */
    public boolean canWalk(int x, int y, double z, boolean lastStep) {
        RoomModel model = getModel();
        if (model == null) {
            return false;
        }
        
        // Basic bounds check
        if (x < 0 || y < 0) {
            return false;
        }
        
        // Check if square has users (simplified - would use UserMatrix in full implementation)
        for (RoomUser user : users.values()) {
            if (user.getX() == x && user.getY() == y && !user.isSpectator()) {
                return false;
            }
        }
        
        // For now, allow walking (full implementation would check Matrix state)
        return true;
    }
    
    /**
     * Deploys a pet as a bot in the room (simplified version).
     * TODO: Full bot implementation when BotManager is ported (Phase 10.5)
     * @param pet Pet to deploy
     * @param x X coordinate
     * @param y Y coordinate
     * @return RoomUser representing the pet, or null if failed
     */
    public RoomUser deployPet(com.uber.server.pets.Pet pet, int x, int y) {
        if (pet == null) {
            return null;
        }
        
        RoomModel model = getModel();
        if (model == null) {
            return null;
        }
        
        // Validate position
        if (x < 0 || y < 0 || x >= model.getMapSizeX() || y >= model.getMapSizeY()) {
            x = model.getDoorX();
            y = model.getDoorY();
        }
        
        if (!canWalk(x, y, 0.0, true)) {
            return null;
        }
        
        // Calculate Z from model (simplified - full pathfinding would use SqAbsoluteHeight)
        double z = model.getDoorZ();
        
        // Create RoomUser for pet (habboId = 0 for bots/pets)
        RoomUser petUser = new RoomUser(0, roomId, userCounter++, game);
        petUser.setPos(x, y, z);
        petUser.setRot(model.getDoorDir());
        petUser.setBot(true);
        petUser.setPet(true);
        petUser.setPetData(pet);
        
        pet.setRoomId(roomId);
        pet.setPlacedInRoom(true);
        pet.setX(x);
        pet.setY(y);
        pet.setZ(z);
        pet.setVirtualId(petUser.getVirtualId());
        
        // Use petId as key for pets (add large offset to avoid collisions with user IDs)
        // Pet IDs are typically much smaller than user IDs, so using petId + offset is safe
        long petKey = pet.getPetId() + 1000000000L; // Large offset to avoid collisions
        users.put(petKey, petUser);
        
        // Send pet entry message to room
        ServerMessage enterMessage = new ServerMessage(28);
        enterMessage.appendInt32(1);
        petUser.serialize(enterMessage);
        sendMessage(enterMessage);
        
        return petUser;
    }
    
    /**
     * Removes a bot/pet from room.
     * @param virtualId Virtual ID of bot/pet
     * @param kicked Whether the bot was kicked (vs removed normally)
     */
    public void removeBot(int virtualId, boolean kicked) {
        RoomUser botUser = getRoomUserByVirtualId(virtualId);
        if (botUser == null || !botUser.isBot()) {
            return;
        }
        
        // Send leave message
        ServerMessage leaveMessage = new ServerMessage(29);
        leaveMessage.appendInt32(virtualId);
        sendMessage(leaveMessage);
        
        // Remove from users - find by virtualId since we need to find the right key
        long keyToRemove = -1;
        for (Map.Entry<Long, RoomUser> entry : users.entrySet()) {
            if (entry.getValue().getVirtualId() == virtualId) {
                keyToRemove = entry.getKey();
                break;
            }
        }
        if (keyToRemove >= 0) {
            users.remove(keyToRemove);
        }
        
        // If it's a pet, update pet data and database
        if (botUser.isPet() && botUser.getPetData() != null) {
            com.uber.server.pets.Pet pet = botUser.getPetData();
            pet.setPlacedInRoom(false);
            pet.setRoomId(0);
            pet.setX(0);
            pet.setY(0);
            pet.setZ(0.0);
            
            // Update database
            if (game != null && game.getPetRepository() != null) {
                game.getPetRepository().updatePetRoom(pet.getPetId(), 0, 0, 0, 0.0);
            }
        }
    }
    
    public Map<Long, RoomUser> getUsers() {
        return new ConcurrentHashMap<>(users);
    }
    
    public int getUserCount() {
        int count = 0;
        for (RoomUser user : users.values()) {
            if (!user.isBot() && !user.isSpectator()) {
                count++;
            }
        }
        return count;
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
    
    public List<Long> getUsersWithRights() {
        return new ArrayList<>(usersWithRights);
    }
    
    public boolean hasRights(long userId) {
        return usersWithRights.contains(userId);
    }
    
    /**
     * Checks if a user has an active trade.
     * Ported from HabboHotel/Rooms/Room.cs HasActiveTrade()
     * @param userId User ID
     * @return True if user has active trade
     */
    public boolean hasActiveTrade(long userId) {
        for (Trade trade : activeTrades) {
            if (trade.containsUser(userId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets a user's active trade.
     * Ported from HabboHotel/Rooms/Room.cs GetUserTrade()
     * @param userId User ID
     * @return Trade object, or null if not found
     */
    public Trade getUserTrade(long userId) {
        for (Trade trade : activeTrades) {
            if (trade.containsUser(userId)) {
                return trade;
            }
        }
        return null;
    }
    
    /**
     * Tries to start a trade between two users.
     * Ported from HabboHotel/Rooms/Room.cs TryStartTrade()
     * @param userOne First RoomUser
     * @param userTwo Second RoomUser
     */
    public void tryStartTrade(RoomUser userOne, RoomUser userTwo) {
        if (userOne == null || userTwo == null || userOne.isBot() || userTwo.isBot()) {
            return;
        }
        
        // Check if users are already trading
        if (hasActiveTrade(userOne.getHabboId()) || hasActiveTrade(userTwo.getHabboId())) {
            return;
        }
        
        // Check if room allows trading
        if (!canTradeInRoom()) {
            return;
        }
        
        // Create and start trade
        Trade trade = new Trade(userOne.getHabboId(), userTwo.getHabboId(), roomId, game);
        activeTrades.add(trade);
    }
    
    /**
     * Tries to stop a trade for a user.
     * Ported from HabboHotel/Rooms/Room.cs TryStopTrade()
     * @param userId User ID
     */
    public void tryStopTrade(long userId) {
        Trade trade = getUserTrade(userId);
        if (trade == null) {
            return;
        }
        
        trade.closeTrade(userId);
        activeTrades.remove(trade);
    }
    
    /**
     * Removes an active trade (called by Trade.closeTradeClean()).
     * @param trade Trade to remove
     */
    public void removeActiveTrade(Trade trade) {
        if (trade != null) {
            activeTrades.remove(trade);
        }
    }
    
    /**
     * Checks if a user is banned from the room.
     * Ported from HabboHotel/Rooms/Room.cs UserIsBanned()
     * @param userId User ID
     * @return True if user is banned
     */
    public boolean userIsBanned(long userId) {
        return bans.containsKey(userId);
    }
    
    /**
     * Checks if a user's ban has expired.
     * Ported from HabboHotel/Rooms/Room.cs HasBanExpired()
     * Room bans expire after 15 minutes (900 seconds).
     * @param userId User ID
     * @return True if ban has expired or user is not banned
     */
    public boolean hasBanExpired(long userId) {
        if (!userIsBanned(userId)) {
            return true;
        }
        
        long banTimestamp = bans.get(userId);
        long diff = com.uber.server.util.TimeUtil.getUnixTimestamp() - banTimestamp;
        
        // Bans expire after 900 seconds (15 minutes)
        return diff > 900;
    }
    
    /**
     * Removes a ban from a user.
     * Ported from HabboHotel/Rooms/Room.cs RemoveBan()
     * @param userId User ID
     */
    public void removeBan(long userId) {
        bans.remove(userId);
    }
    
    /**
     * Adds a ban to a user.
     * Ported from HabboHotel/Rooms/Room.cs AddBan()
     * Room bans are temporary (15 minute expiry) and stored in memory only.
     * @param userId User ID
     */
    public void addBan(long userId) {
        bans.put(userId, com.uber.server.util.TimeUtil.getUnixTimestamp());
    }
    
    /**
     * Destroys the room (cleanup).
     * Ported from HabboHotel/Rooms/Room.cs Destroy()
     */
    public void destroy() {
        // Close all active trades
        for (Trade trade : activeTrades) {
            trade.closeTrade(0); // Close without specific user
        }
        activeTrades.clear();
        
        // Send kick message to all users
        sendMessage(new ServerMessage(18));
        
        keepAlive = false;
        users.clear();
        // Items can remain for room reloading
    }
    
    /**
     * Counts items by interaction type.
     * Ported from HabboHotel/Rooms/Room.cs ItemCountByType()
     * @param interactionType Interaction type to count
     * @return Count of items with this interaction type
     */
    public int itemCountByType(String interactionType) {
        if (interactionType == null) {
            return 0;
        }
        
        String lowerType = interactionType.toLowerCase();
        int count = 0;
        
        for (RoomItem item : items.values()) {
            com.uber.server.items.Item baseItem = item.getBaseItem();
            if (baseItem != null && lowerType.equals(baseItem.getInteractionType())) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Validates and normalizes wall position string.
     * Ported from HabboHotel/Rooms/Room.cs WallPositionCheck()
     * @param wallPosition Wall position string (e.g., ":w=3,2 l=9,63 l")
     * @return Normalized wall position, or null if invalid
     */
    public String wallPositionCheck(String wallPosition) {
        if (wallPosition == null || wallPosition.isEmpty()) {
            return null;
        }
        
        try {
            // Check for invalid characters
            if (wallPosition.contains("\r") || wallPosition.contains("\t")) {
                return null;
            }
            
            String[] posD = wallPosition.split(" ");
            if (posD.length < 3) {
                return null;
            }
            
            if (!"l".equals(posD[2]) && !"r".equals(posD[2])) {
                return null;
            }
            
            // Parse width
            if (!posD[0].startsWith(":w=")) {
                return null;
            }
            String[] widD = posD[0].substring(3).split(",");
            if (widD.length != 2) {
                return null;
            }
            int widthX = Integer.parseInt(widD[0]);
            int widthY = Integer.parseInt(widD[1]);
            if (widthX < 0 || widthY < 0 || widthX > 200 || widthY > 200) {
                return null;
            }
            
            // Parse length
            if (!posD[1].startsWith("l=")) {
                return null;
            }
            String[] lenD = posD[1].substring(2).split(",");
            if (lenD.length != 2) {
                return null;
            }
            int lengthX = Integer.parseInt(lenD[0]);
            int lengthY = Integer.parseInt(lenD[1]);
            if (lengthX < 0 || lengthY < 0 || lengthX > 200 || lengthY > 200) {
                return null;
            }
            
            return ":w=" + widthX + "," + widthY + " l=" + lengthX + "," + lengthY + " " + posD[2];
        } catch (Exception e) {
            logger.warn("Invalid wall position format: {}", wallPosition);
            return null;
        }
    }
    
    /**
     * Places a floor item in the room (simplified version).
     * Ported from HabboHotel/Rooms/Room.cs SetFloorItem()
     * TODO: Enhance with full pathfinding validation when Pathfinder is ported (Phase 11)
     * @param session GameClient session
     * @param item RoomItem to place
     * @param newX X coordinate
     * @param newY Y coordinate
     * @param newRot Rotation
     * @param newItem If true, this is a new item being placed; if false, moving existing item
     * @return True if placement was successful
     */
    public boolean setFloorItem(GameClient session, RoomItem item, int newX, int newY, int newRot, boolean newItem) {
        if (item == null || session == null) {
            return false;
        }
        
        com.uber.server.items.Item baseItem = item.getBaseItem();
        if (baseItem == null || !baseItem.isFloorItem()) {
            return false;
        }
        
        // Validate rotation
        if (newRot != 0 && newRot != 2 && newRot != 4 && newRot != 6 && newRot != 8) {
            newRot = 0;
        }
        
        // Get room model for basic validation
        RoomModel model = getModel();
        if (model == null) {
            return false;
        }
        
        // Basic coordinate validation (simplified - full validation requires pathfinding)
        // TODO: Add full tile validation when pathfinding is ported
        
        // Calculate Z coordinate (simplified - use floor height from model if available)
        double newZ = 0.0; // Default to 0, will be enhanced with model heightmap
        
        // Update item position
        item.setX(newX);
        item.setY(newY);
        item.setZ(newZ);
        item.setRot(newRot);
        
        // TODO: Call item interactor OnPlace when FurniInteractor is ported
        // item.getInteractor().onPlace(session, item);
        
        if (newItem) {
            // Insert into database
            if (roomItemRepository.createRoomItem(item.getId(), roomId, item.getBaseItemId(), 
                                                  item.getExtraData(), newX, newY, newZ, newRot, "")) {
                items.put(item.getId(), item);
                
                // Send item to room
                ServerMessage message = new ServerMessage(93);
                item.serialize(message);
                sendMessage(message);
                return true;
            }
        } else {
            // Update in database
            if (roomItemRepository.updatePosition(item.getId(), newX, newY, newZ, newRot)) {
                // Send update to room
                ServerMessage message = new ServerMessage(95);
                item.serialize(message);
                sendMessage(message);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Places a wall item in the room (simplified version).
     * Ported from HabboHotel/Rooms/Room.cs SetWallItem()
     * @param session GameClient session
     * @param item RoomItem to place
     * @return True if placement was successful
     */
    public boolean setWallItem(GameClient session, RoomItem item) {
        if (item == null || session == null) {
            return false;
        }
        
        com.uber.server.items.Item baseItem = item.getBaseItem();
        if (baseItem == null || !baseItem.isWallItem()) {
            return false;
        }
        
        // TODO: Call item interactor OnPlace when FurniInteractor is ported
        // item.getInteractor().onPlace(session, item);
        
        // Handle special item types
        String interactionType = baseItem.getInteractionType();
        if ("dimmer".equalsIgnoreCase(interactionType)) {
            // TODO: Initialize MoodlightData when MoodlightData is ported (Phase 10)
            // For now, just place the item
        }
        
        // Insert into database
        if (roomItemRepository.createRoomItem(item.getId(), roomId, item.getBaseItemId(),
                                              item.getExtraData(), 0, 0, 0.0, 0, item.getWallPos())) {
            items.put(item.getId(), item);
            
            // Send item to room
            ServerMessage message = new ServerMessage(83);
            item.serialize(message);
            sendMessage(message);
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes furniture from the room.
     * Ported from HabboHotel/Rooms/Room.cs RemoveFurniture()
     * @param session GameClient session
     * @param itemId Item ID to remove
     */
    public void removeFurniture(GameClient session, long itemId) {
        RoomItem item = getItem(itemId);
        if (item == null) {
            return;
        }
        
        // TODO: Call item interactor OnRemove when FurniInteractor is ported
        // item.getInteractor().onRemove(session, item);
        
        // Send removal message to room
        if (item.isWallItem()) {
            ServerMessage message = new ServerMessage(84);
            message.appendUInt(itemId);
            message.appendStringWithBreak("");
            message.appendBoolean(false);
            sendMessage(message);
        } else if (item.isFloorItem()) {
            ServerMessage message = new ServerMessage(94);
            message.appendUInt(itemId);
            message.appendStringWithBreak("");
            message.appendBoolean(false);
            sendMessage(message);
        }
        
        // Remove from room and database
        items.remove(itemId);
        roomItemRepository.deleteRoomItem(itemId);
    }
    
    /**
     * Updates room settings.
     * Ported from HabboHotel/Rooms/Room.cs (SaveRoomData logic)
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
     * @param userId User ID to give rights to
     * @return True if successful
     */
    public boolean addRight(long userId) {
        if (usersWithRights.contains(userId)) {
            return false; // Already has rights
        }
        
        if (roomRepository.addRoomRight(roomId, userId)) {
            usersWithRights.add(userId);
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes a room right.
     * @param userId User ID to remove rights from
     * @return True if successful
     */
    public boolean removeRight(long userId) {
        if (!usersWithRights.contains(userId)) {
            return false;
        }
        
        if (roomRepository.deleteRoomRights(roomId, new long[]{userId})) {
            usersWithRights.remove(userId);
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes all room rights.
     * @return True if successful
     */
    public boolean removeAllRights() {
        if (usersWithRights.isEmpty()) {
            return true;
        }
        
        if (roomRepository.deleteRoomRights(roomId, null)) {
            usersWithRights.clear();
            return true;
        }
        
        return false;
    }
}
