package com.uber.server.game.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.RoomItem;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.RoomItemRepository;
import com.uber.server.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    private com.uber.server.game.items.MoodlightData moodlightData; // Moodlight data (if room has dimmer item)
    
    private int userCounter;
    private boolean keepAlive;
    private int idleTime;
    private ScheduledExecutorService processExecutor;
    private ScheduledFuture<?> processTask;
    
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
        this.idleTime = 0;
        this.processExecutor = null;
        this.processTask = null;
        
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
                com.uber.server.game.items.Item baseItemObj = game.getItemManager().getItem(baseItem);
                if (baseItemObj != null && "dimmer".equalsIgnoreCase(baseItemObj.getInteractionType())) {
                    try {
                        moodlightData = new com.uber.server.game.items.MoodlightData(id, roomItemRepository);
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
                user.addStatus("roomControl", "useradmin");
            } else if (checkRights(session, false)) {
                user.addStatus("roomControl", "");
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
        
        // Call habbo.onEnterRoom()
        habbo.onEnterRoom(roomId);
        
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
            
            // Call habbo.onLeaveRoom()
            habbo.onLeaveRoom();
            
            updateUserCount();
            
            // Notify bots
            for (RoomUser bot : users.values()) {
                if (bot.isBot() && bot.getBotAI() != null) {
                    bot.getBotAI().onUserLeaveRoom(session);
                }
            }
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
    public com.uber.server.game.items.MoodlightData getMoodlightData() {
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
    public RoomUser deployPet(com.uber.server.game.pets.Pet pet, int x, int y) {
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
     * Initializes bots for this room.
     * Ported from HabboHotel/Rooms/Room.cs InitBots()
     */
    public void initBots() {
        if (game == null || game.getBotManager() == null) {
            return;
        }
        
        List<com.uber.server.game.bots.RoomBot> bots = game.getBotManager().getBotsForRoom(roomId);
        for (com.uber.server.game.bots.RoomBot bot : bots) {
            deployBot(bot);
        }
    }
    
    /**
     * Initializes pets for this room.
     * Ported from HabboHotel/Rooms/Room.cs InitPets()
     */
    public void initPets() {
        if (game == null || game.getPetRepository() == null || game.getCatalog() == null) {
            return;
        }
        
        // Load pets from database for this room
        List<Map<String, Object>> petData = game.getPetRepository().loadPetsInRoom(roomId);
        for (Map<String, Object> row : petData) {
            try {
                // Use Catalog to generate pet (similar to C#)
                com.uber.server.game.pets.Pet pet = game.getCatalog().generatePetFromRow(row);
                if (pet == null) {
                    continue;
                }
                
                // Create a RoomBot for the pet (similar to C# Room.cs line 321)
                Map<String, Object> botData = new HashMap<>();
                botData.put("id", pet.getPetId());
                botData.put("room_id", roomId);
                botData.put("ai_type", "pet");
                botData.put("walk_mode", "freeroam");
                botData.put("name", pet.getName() != null ? pet.getName() : "");
                botData.put("motto", "");
                botData.put("look", pet.getLook() != null ? pet.getLook() : "");
                botData.put("x", pet.getX());
                botData.put("y", pet.getY());
                botData.put("z", (int) pet.getZ());
                botData.put("rotation", 0);
                botData.put("min_x", 0);
                botData.put("min_y", 0);
                botData.put("max_x", 0);
                botData.put("max_y", 0);
                
                com.uber.server.game.bots.RoomBot petBot = new com.uber.server.game.bots.RoomBot(
                    botData,
                    null // BotRepository not needed for pets (they don't have speech/responses)
                );
                
                deployBot(petBot, pet);
            } catch (Exception e) {
                logger.error("Failed to load pet for room {}: {}", roomId, e.getMessage(), e);
            }
        }
    }
    
    /**
     * Deploys a bot to the room.
     * Ported from HabboHotel/Rooms/Room.cs DeployBot(RoomBot)
     * @param bot RoomBot to deploy
     * @return RoomUser instance for the bot
     */
    public RoomUser deployBot(com.uber.server.game.bots.RoomBot bot) {
        return deployBot(bot, null);
    }
    
    /**
     * Deploys a bot to the room (with optional pet data).
     * Ported from HabboHotel/Rooms/Room.cs DeployBot(RoomBot, Pet)
     * @param bot RoomBot to deploy
     * @param petData Optional pet data (if this is a pet bot)
     * @return RoomUser instance for the bot
     */
    public RoomUser deployBot(com.uber.server.game.bots.RoomBot bot, com.uber.server.game.pets.Pet petData) {
        if (bot == null) {
            return null;
        }
        
        RoomModel model = getModel();
        if (model == null) {
            return null;
        }
        
        RoomUser botUser = new RoomUser(0, roomId, userCounter++, game);
        
        // Set position
        int botX = bot.getX();
        int botY = bot.getY();
        int botZ = bot.getZ();
        
        if ((botX > 0 && botY > 0) && botX < model.getMapSizeX() && botY < model.getMapSizeY()) {
            botUser.setPos(botX, botY, botZ);
            botUser.setRot(bot.getRot());
        } else {
            // Use door position
            botX = model.getDoorX();
            botY = model.getDoorY();
            botZ = (int) model.getDoorZ();
            botUser.setPos(botX, botY, botZ);
            botUser.setRot(model.getDoorDir());
        }
        
        // Set bot properties
        botUser.setBot(true);
        botUser.setBotData(bot);
        
        if (petData != null) {
            botUser.setPet(true);
            botUser.setPetData(petData);
            petData.setVirtualId(botUser.getVirtualId());
        }
        
        // Generate BotAI
        com.uber.server.game.bots.BotAI botAI = bot.generateBotAI(botUser.getVirtualId());
        if (bot.isPet() && petData != null) {
            botAI.init((int) bot.getBotId(), botUser.getVirtualId(), roomId);
        } else {
            botAI.init(-1, botUser.getVirtualId(), roomId);
        }
        botUser.setBotAI(botAI);
        
        // Add to users map (use botId as key with offset for bots)
        long botKey = bot.getBotId() + 2000000000L; // Different offset than pets
        users.put(botKey, botUser);
        
        // Update status
        botUser.setUpdateNeeded(true);
        
        // Send entry message
        ServerMessage enterMessage = new ServerMessage(28);
        enterMessage.appendInt32(1);
        botUser.serialize(enterMessage);
        sendMessage(enterMessage);
        
        // Call bot AI on enter
        botAI.onSelfEnterRoom();
        
        return botUser;
    }
    
    /**
     * Removes a bot/pet from room.
     * Ported from HabboHotel/Rooms/Room.cs RemoveBot()
     * @param virtualId Virtual ID of bot/pet
     * @param kicked Whether the bot was kicked (vs removed normally)
     */
    public void removeBot(int virtualId, boolean kicked) {
        RoomUser botUser = getRoomUserByVirtualId(virtualId);
        if (botUser == null || !botUser.isBot()) {
            return;
        }
        
        // Call bot AI on leave
        if (botUser.getBotAI() != null) {
            botUser.getBotAI().onSelfLeaveRoom(kicked);
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
            com.uber.server.game.pets.Pet pet = botUser.getPetData();
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
    
    /**
     * Called when a user says something in the room.
     * Notifies all bots in the room.
     * Ported from HabboHotel/Rooms/Room.cs OnUserSay()
     * @param user RoomUser that spoke
     * @param message Message text
     * @param shout True if user shouted
     */
    public void onUserSay(RoomUser user, String message, boolean shout) {
        if (user == null || message == null) {
            return;
        }
        
        for (RoomUser roomUser : users.values()) {
            if (!roomUser.isBot() || roomUser.getBotAI() == null) {
                continue;
            }
            
            if (shout) {
                roomUser.getBotAI().onUserShout(user, message);
            } else {
                roomUser.getBotAI().onUserSay(user, message);
            }
        }
    }
    
    /**
     * Calculates tile distance between two coordinates.
     * Ported from HabboHotel/Rooms/Room.cs TileDistance()
     * @param x1 First X coordinate
     * @param y1 First Y coordinate
     * @param x2 Second X coordinate
     * @param y2 Second Y coordinate
     * @return Tile distance
     */
    public int tileDistance(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return Math.max(dx, dy);
    }
    
    /**
     * Regenerates the user matrix based on current user positions.
     * Ported from HabboHotel/Rooms/Room.cs RegenerateUserMatrix()
     */
    public void regenerateUserMatrix() {
        RoomModel model = getModel();
        if (model == null) {
            return;
        }
        
        // Initialize user matrix (simplified - full implementation would use BedMatrix)
        // For now, just track user positions
        // Note: Full pathfinding implementation would use a 2D boolean array
        // This is a placeholder that will be enhanced when full pathfinding is ported
    }
    
    /**
     * Makes all users in the room turn their heads to look at a coordinate.
     * Ported from HabboHotel/Rooms/Room.cs TurnHeads()
     * @param x X coordinate to look at
     * @param y Y coordinate to look at
     * @param senderId Habbo ID of the user who triggered this (excluded from turning)
     */
    public void turnHeads(int x, int y, long senderId) {
        for (RoomUser user : users.values()) {
            if (user.getHabboId() == senderId || user.isBot() || user.isSpectator()) {
                continue;
            }
            
            // Calculate rotation to look at the coordinate
            int rot = com.uber.server.game.pathfinding.Rotation.calculate(user.getX(), user.getY(), x, y);
            user.setRot(rot, true); // true = head only
        }
    }
    
    /**
     * Checks if a square has users (excluding last step positions).
     * Ported from HabboHotel/Rooms/Room.cs SquareHasUsers(int, int, bool)
     * @param x X coordinate
     * @param y Y coordinate
     * @param lastStep If true, allows users on last step positions
     * @return True if square has users
     */
    public boolean squareHasUsers(int x, int y, boolean lastStep) {
        return squareHasUsers(x, y);
    }
    
    /**
     * Checks if a square has users.
     * Ported from HabboHotel/Rooms/Room.cs SquareHasUsers(int, int)
     * @param x X coordinate
     * @param y Y coordinate
     * @return True if square has users
     */
    public boolean squareHasUsers(int x, int y) {
        for (RoomUser user : users.values()) {
            if (user.isSpectator()) {
                continue;
            }
            if (user.getX() == x && user.getY() == y) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if two tiles are touching (adjacent or same).
     * Ported from HabboHotel/Rooms/Room.cs TilesTouching()
     * @param x1 First X coordinate
     * @param y1 First Y coordinate
     * @param x2 Second X coordinate
     * @param y2 Second Y coordinate
     * @return True if tiles are touching
     */
    public boolean tilesTouching(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return dx <= 1 && dy <= 1;
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
    
    public boolean getKeepAlive() {
        return keepAlive;
    }
    
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
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
        stopProcessRoutine();
        
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
            com.uber.server.game.items.Item baseItem = item.getBaseItem();
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
        
        com.uber.server.game.items.Item baseItem = item.getBaseItem();
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
        
        // Call item interactor OnPlace
        item.getInteractor().onPlace(session, item);
        
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
        
        com.uber.server.game.items.Item baseItem = item.getBaseItem();
        if (baseItem == null || !baseItem.isWallItem()) {
            return false;
        }
        
        // Call item interactor OnPlace
        item.getInteractor().onPlace(session, item);
        
        // Handle special item types
        String interactionType = baseItem.getInteractionType();
        if ("dimmer".equalsIgnoreCase(interactionType)) {
            // Initialize MoodlightData for dimmer items
            try {
                moodlightData = new com.uber.server.game.items.MoodlightData(item.getId(), roomItemRepository);
            } catch (Exception e) {
                logger.warn("Failed to initialize moodlight data for item {}: {}", item.getId(), e.getMessage());
            }
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
        
        // Call item interactor OnRemove
        item.getInteractor().onRemove(session, item);
        
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
    
    /**
     * Starts the room processing routine.
     * Ported from HabboHotel/Rooms/Room.cs StartProcessRoutine()
     */
    public void startProcessRoutine() {
        if (processTask != null && !processTask.isCancelled()) {
            return; // Already running
        }
        
        if (processExecutor == null) {
            processExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "RoomProcessor-" + roomId);
                t.setDaemon(true);
                return t;
            });
        }
        
        // Process every 500ms
        processTask = processExecutor.scheduleAtFixedRate(this::processRoom, 500, 500, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Stops the room processing routine.
     * Ported from HabboHotel/Rooms/Room.cs StopProcessRoutine()
     */
    public void stopProcessRoutine() {
        if (processTask != null) {
            processTask.cancel(false);
            processTask = null;
        }
        
        if (processExecutor != null) {
            processExecutor.shutdown();
            try {
                if (!processExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    processExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                processExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            processExecutor = null;
        }
    }
    
    /**
     * Processes the room (called every 500ms).
     * Ported from HabboHotel/Rooms/Room.cs ProcessRoom()
     */
    private void processRoom() {
        if (!keepAlive) {
            return; // Don't process if room should be dead
        }
        
        // Process item updates
        for (RoomItem item : items.values()) {
            if (item.isUpdateNeeded()) {
                item.processUpdates();
            }
        }
        
        // Process users and bots
        List<Long> toRemove = new ArrayList<>();
        int userCount = 0;
        
        for (RoomUser user : users.values()) {
            // Increment idle time
            user.incrementIdleTime();
            
            // Check if user should fall asleep (idle for 600 ticks = 300 seconds)
            if (!user.isAsleep() && user.getIdleTime() >= 600) {
                user.setAsleep(true);
                ServerMessage sleepMessage = new ServerMessage(486);
                sleepMessage.appendInt32(user.getVirtualId());
                sleepMessage.appendBoolean(true);
                sendMessage(sleepMessage);
            }
            
            // Handle carry item timer
            if (user.getCarryItemId() > 0) {
                user.decrementCarryTimer();
                if (user.getCarryTimer() <= 0) {
                    user.carryItem(0);
                }
            }
            
            // Process bot AI ticks
            if (user.isBot() && user.getBotAI() != null) {
                user.getBotAI().onTimerTick();
            } else {
                userCount++; // Count non-bot users
            }
        }
        
        // Remove users that need to be removed
        for (Long habboId : toRemove) {
            GameClient client = game.getClientManager().getClientByHabbo(habboId);
            if (client != null) {
                removeUserFromRoom(client, true, false);
            }
        }
        
        // Update room idle time
        if (userCount >= 1) {
            this.idleTime = 0;
        } else {
            this.idleTime++;
        }
        
        // Request unload if room has been idle for 60 ticks (30 seconds)
        if (this.idleTime >= 60) {
            logger.debug("Requesting unload of idle room - ID: {}", roomId);
            if (game != null && game.getRoomManager() != null) {
                game.getRoomManager().requestRoomUnload(roomId);
            }
        }
    }
    
}
