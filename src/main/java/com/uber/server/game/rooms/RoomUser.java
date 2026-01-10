package com.uber.server.game.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.pathfinding.Coord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a user in a room.
 * Ported from HabboHotel/Rooms/RoomUser.cs
 */
public class RoomUser {
    private static final Logger logger = LoggerFactory.getLogger(RoomUser.class);
    
    private final long habboId;
    private final long roomId;
    private final int virtualId;
    private final Game game;
    
    private int x;
    private int y;
    private double z;
    private int rotHead;
    private int rotBody;
    
    private int idleTime;
    private int carryItemId;
    private int carryTimer;
    private boolean canWalk;
    private boolean allowOverride;
    private int goalX;
    private int goalY;
    private boolean setStep;
    private int setX;
    private int setY;
    private double setZ;
    
    private boolean isWalking;
    private boolean updateNeeded;
    private boolean isAsleep;
    private final ConcurrentHashMap<String, String> statuses;
    private int danceId;
    private final List<Coord> path;
    private int pathStep;
    private boolean pathRecalcNeeded;
    private int pathRecalcX;
    private int pathRecalcY;
    private int teleDelay;
    
    private boolean isSpectator;
    private boolean isBot;
    private boolean isPet; // True if this RoomUser represents a pet
    private com.uber.server.game.pets.Pet petData; // Pet data if isPet is true
    private com.uber.server.game.bots.RoomBot botData; // Bot data if isBot is true
    private com.uber.server.game.bots.BotAI botAI; // Bot AI instance
    
    public RoomUser(long habboId, long roomId, int virtualId, Game game) {
        this.habboId = habboId;
        this.roomId = roomId;
        this.virtualId = virtualId;
        this.game = game;
        this.x = 0;
        this.y = 0;
        this.z = 0.0;
        this.rotHead = 0;
        this.rotBody = 0;
        this.idleTime = 0;
        this.carryItemId = 0;
        this.carryTimer = 0;
        this.canWalk = true;
        this.allowOverride = false;
        this.goalX = 0;
        this.goalY = 0;
        this.setStep = false;
        this.setX = 0;
        this.setY = 0;
        this.setZ = 0.0;
        this.isWalking = false;
        this.updateNeeded = true;
        this.isAsleep = false;
        this.statuses = new ConcurrentHashMap<>();
        this.danceId = 0;
        this.path = new ArrayList<>();
        this.pathStep = 0;
        this.pathRecalcNeeded = false;
        this.pathRecalcX = 0;
        this.pathRecalcY = 0;
        this.teleDelay = 0;
        this.isSpectator = false;
        this.isBot = false;
        this.isPet = false;
        this.petData = null;
        this.botData = null;
        this.botAI = null;
    }
    
    // Getters and setters
    public long getHabboId() { return habboId; }
    public long getRoomId() { return roomId; }
    public int getVirtualId() { return virtualId; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    public boolean canWalk() { return canWalk; }
    public void setCanWalk(boolean canWalk) { this.canWalk = canWalk; }
    public int getRotHead() { return rotHead; }
    public void setRotHead(int rotHead) { this.rotHead = rotHead; }
    public int getRotBody() { return rotBody; }
    public void setRotBody(int rotBody) { this.rotBody = rotBody; }
    public boolean isWalking() { return isWalking; }
    public void setWalking(boolean walking) { isWalking = walking; }
    public boolean isSpectator() { return isSpectator; }
    public void setSpectator(boolean spectator) { isSpectator = spectator; }
    public boolean isBot() { return isBot; }
    public void setBot(boolean bot) { isBot = bot; }
    public boolean isPet() { return isPet; }
    public void setPet(boolean pet) { this.isPet = pet; }
    public com.uber.server.game.pets.Pet getPetData() { return petData; }
    public void setPetData(com.uber.server.game.pets.Pet petData) {
        this.petData = petData;
        if (petData != null) {
            this.isPet = true;
            this.isBot = true; // Pets are bots
            if (petData.getVirtualId() == 0) {
                petData.setVirtualId(this.virtualId);
            }
        }
    }
    public com.uber.server.game.bots.RoomBot getBotData() { return botData; }
    public void setBotData(com.uber.server.game.bots.RoomBot botData) {
        this.botData = botData;
        if (botData != null) {
            this.isBot = true;
        }
    }
    public com.uber.server.game.bots.BotAI getBotAI() { return botAI; }
    public void setBotAI(com.uber.server.game.bots.BotAI botAI) {
        this.botAI = botAI;
    }
    public boolean isUpdateNeeded() { return updateNeeded; }
    public void setUpdateNeeded(boolean updateNeeded) { this.updateNeeded = updateNeeded; }
    public boolean isAsleep() { return isAsleep; }
    public void setAsleep(boolean asleep) { this.isAsleep = asleep; }
    public int getDanceId() { return danceId; }
    public void setDanceId(int danceId) { this.danceId = danceId; }
    public boolean isDancing() { return danceId >= 1; }
    public int getCarryItemId() { return carryItemId; }
    public void setCarryItemId(int carryItemId) { this.carryItemId = carryItemId; }
    public Map<String, String> getStatuses() { return new ConcurrentHashMap<>(statuses); }
    
    /**
     * Gets the coordinate of this user.
     * @return Coord object
     */
    public Coord getCoordinate() {
        return new Coord(x, y);
    }
    
    /**
     * Sets position (x, y, z).
     * Ported from RoomUser.cs SetPos()
     */
    public void setPos(int x, int y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Sets rotation.
     * Ported from RoomUser.cs SetRot()
     */
    public void setRot(int rotation) {
        setRot(rotation, false);
    }
    
    /**
     * Sets rotation (head and body).
     * Ported from RoomUser.cs SetRot(int, bool)
     */
    public void setRot(int rotation, boolean headOnly) {
        if (statuses.containsKey("lay") || isWalking) {
            return;
        }
        
        int diff = this.rotBody - rotation;
        this.rotHead = this.rotBody;
        
        if (statuses.containsKey("sit") || headOnly) {
            // Only rotate head when sitting or headOnly is true
            if (rotBody == 2 || rotBody == 4) {
                if (diff > 0) {
                    rotHead = rotBody - 1;
                } else if (diff < 0) {
                    rotHead = rotBody + 1;
                }
            } else if (rotBody == 0 || rotBody == 6) {
                if (diff > 0) {
                    rotHead = rotBody - 1;
                } else if (diff < 0) {
                    rotHead = rotBody + 1;
                }
            }
        } else if (diff <= -2 || diff >= 2) {
            // Large rotation difference - rotate both head and body
            this.rotHead = rotation;
            this.rotBody = rotation;
        } else {
            // Small rotation difference - only rotate head
            this.rotHead = rotation;
        }
        
        this.updateNeeded = true;
    }
    
    /**
     * Adds a status (like "sit", "lay", "mv", etc.).
     * Ported from RoomUser.cs AddStatus()
     */
    public void addStatus(String key, String value) {
        if (key == null || key.isEmpty()) {
            return;
        }
        statuses.put(key, value != null ? value : "");
        updateNeeded = true;
    }
    
    /**
     * Removes a status.
     * Ported from RoomUser.cs RemoveStatus()
     */
    public void removeStatus(String key) {
        if (key != null && statuses.remove(key) != null) {
            updateNeeded = true;
        }
    }
    
    /**
     * Resets all statuses.
     * Ported from RoomUser.cs ResetStatus()
     */
    public void resetStatus() {
        statuses.clear();
        updateNeeded = true;
    }
    
    /**
     * Moves to a coordinate.
     * Ported from RoomUser.cs MoveTo(Coord)
     */
    public void moveTo(Coord coord) {
        moveTo(coord.getX(), coord.getY());
    }
    
    /**
     * Moves to a position.
     * Ported from RoomUser.cs MoveTo(int, int)
     */
    public void moveTo(int x, int y) {
        unidle();
        pathRecalcNeeded = true;
        pathRecalcX = x;
        pathRecalcY = y;
    }
    
    /**
     * Clears idle status.
     * Ported from RoomUser.cs Unidle()
     */
    public void unidle() {
        this.idleTime = 0;
        
        if (this.isAsleep) {
            this.isAsleep = false;
            
            // Send sleep update message
            Room room = getRoom();
            if (room != null) {
                ServerMessage message = new ServerMessage(486);
                message.appendInt32(virtualId);
                message.appendBoolean(false);
                room.sendMessage(message);
            }
        }
    }
    
    /**
     * Clears movement.
     * Ported from RoomUser.cs ClearMovement()
     */
    public void clearMovement(boolean update) {
        isWalking = false;
        pathRecalcNeeded = false;
        path.clear();
        statuses.remove("mv");
        goalX = 0;
        goalY = 0;
        setStep = false;
        setX = 0;
        setY = 0;
        setZ = 0.0;
        
        if (update) {
            updateNeeded = true;
        }
    }
    
    /**
     * Unlocks walking for the user.
     * Ported from RoomUser.cs UnlockWalking()
     */
    public void unlockWalking() {
        this.canWalk = true;
        this.allowOverride = false;
    }
    
    /**
     * Carries an item.
     * Ported from RoomUser.cs CarryItem()
     */
    public void carryItem(int itemId) {
        this.carryItemId = itemId;
        
        if (itemId > 0) {
            this.carryTimer = 240;
        } else {
            this.carryTimer = 0;
        }
        
        // Send carry item update message
        Room room = getRoom();
        if (room != null) {
            ServerMessage message = new ServerMessage(482);
            message.appendInt32(virtualId);
            message.appendInt32(itemId);
            room.sendMessage(message);
        }
    }
    
    /**
     * Gets speech emotion from message.
     * Ported from RoomUser.cs GetSpeechEmotion()
     * @param message Chat message
     * @return Emotion ID (0 = none, 1 = happy, 2 = angry, 3 = surprised, 4 = sad)
     */
    public int getSpeechEmotion(String message) {
        if (message == null) {
            return 0;
        }
        
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains(":)") || lowerMessage.contains(":d") || lowerMessage.contains("=]") ||
            lowerMessage.contains("=d") || lowerMessage.contains(":>")) {
            return 1; // Happy
        }
        
        if (lowerMessage.contains(">:(") || lowerMessage.contains(":@")) {
            return 2; // Angry
        }
        
        if (lowerMessage.contains(":o")) {
            return 3; // Surprised
        }
        
        if (lowerMessage.contains(":(") || lowerMessage.contains("=[") || 
            lowerMessage.contains(":'(") || lowerMessage.contains("='[")) {
            return 4; // Sad
        }
        
        return 0; // None
    }
    
    /**
     * Handles chat message.
     * Ported from RoomUser.cs Chat()
     */
    public void chat(GameClient session, String message, boolean shout) {
        unidle();
        
        Habbo habbo = session != null ? session.getHabbo() : null;
        if (habbo != null && habbo.isMuted()) {
            if (session != null) {
                session.sendNotif("You are muted.");
            }
            return;
        }
        
        // Handle chat commands (":command")
        if (message != null && message.startsWith(":") && session != null) {
            String command = message.substring(1);
            if (com.uber.server.misc.ChatCommandHandler.parse(session, command)) {
                // Command was handled, don't send as chat
                return;
            }
        }
        
        // Check newbie status
        if (habbo != null && habbo.getNewbieStatus() == 1) {
            // Newbie has spoken, update status to 2
            habbo.setNewbieStatus(2);
            
            // Update in database
            if (game != null && game.getUserRepository() != null) {
                game.getUserRepository().updateNewbieStatus(habbo.getId(), 2);
            }
        }
        
        int chatHeader = shout ? 26 : 24; // 24 = talk, 26 = shout
        
        ServerMessage chatMessage = new ServerMessage(chatHeader);
        chatMessage.appendInt32(virtualId);
        chatMessage.appendStringWithBreak(message);
        chatMessage.appendInt32(getSpeechEmotion(message));
        
        // Send message to room
        Room room = getRoom();
        if (room != null) {
            // Turn heads to look at the speaker
            room.turnHeads(x, y, habboId);
            
            room.sendMessage(chatMessage);
            
            // Notify bots in room
            room.onUserSay(this, message, shout);
        }
        
        logger.debug("Chat from user {} in room {}: {}", habboId, roomId, message);
    }
    
    /**
     * Serializes the user to a ServerMessage (for room entry).
     * Ported from RoomUser.cs Serialize()
     */
    public void serialize(ServerMessage message) {
        if (isSpectator) {
            return;
        }
        
        if (!isBot) {
            // Regular user serialization
            GameClient client = getClient();
            if (client == null || client.getHabbo() == null) {
                return;
            }
            
            Habbo habbo = client.getHabbo();
            
            message.appendUInt(habbo.getId());
            message.appendStringWithBreak(habbo.getUsername());
            message.appendStringWithBreak(habbo.getMotto());
            message.appendStringWithBreak(habbo.getLook());
            message.appendInt32(virtualId);
            message.appendInt32(x);
            message.appendInt32(y);
            message.appendStringWithBreak(String.format("%.1f", z).replace(',', '.'));
            message.appendInt32(2); // Direction
            message.appendInt32(1); // Head direction
            message.appendStringWithBreak(habbo.getGender().toLowerCase());
            message.appendInt32(-1); // Achievement score
            message.appendInt32(-1); // Achievement score (again)
            message.appendInt32(-1); // Achievement score (again)
            message.appendStringWithBreak(""); // Badge code
        } else if (isPet && petData != null) {
            // Pet serialization (from C# RoomUser.cs Serialize() when BotData.AiType == "pet")
            // BaseId would be pet ID (as int), but we use 0 as placeholder until BotAI is ported
            message.appendInt32(0); // BaseId (0 for now, would be petId when BotAI is ported)
            message.appendStringWithBreak(petData.getName() != null ? petData.getName() : "");
            message.appendStringWithBreak(""); // Motto (pets don't have motto)
            message.appendStringWithBreak(petData.getLook());
            message.appendInt32(virtualId);
            message.appendInt32(x);
            message.appendInt32(y);
            message.appendStringWithBreak(String.format("%.1f", z).replace(',', '.'));
            message.appendInt32(4); // Direction (4 for bots/pets)
            message.appendInt32(2); // Pet flag (2 = pet, 3 = bot)
            message.appendInt32(0); // Additional pet field (always 0 for pets)
        } else {
            // Bot serialization (placeholder - full bot support when BotManager is ported)
            message.appendInt32(0); // BaseId (would be -1 or bot ID when BotAI is ported)
            message.appendStringWithBreak("Bot");
            message.appendStringWithBreak("");
            message.appendStringWithBreak("");
            message.appendInt32(virtualId);
            message.appendInt32(x);
            message.appendInt32(y);
            message.appendStringWithBreak(String.format("%.1f", z).replace(',', '.'));
            message.appendInt32(4); // Direction
            message.appendInt32(3); // Bot flag (3 = bot, 2 = pet)
        }
    }
    
    /**
     * Serializes user status update.
     * Ported from RoomUser.cs SerializeStatus()
     */
    public void serializeStatus(ServerMessage message) {
        if (isSpectator) {
            return;
        }
        
        message.appendInt32(virtualId);
        message.appendInt32(x);
        message.appendInt32(y);
        message.appendStringWithBreak(String.format("%.1f", z).replace(',', '.'));
        message.appendInt32(rotHead);
        message.appendInt32(rotBody);
        message.appendString("/");
        
        for (Map.Entry<String, String> status : statuses.entrySet()) {
            message.appendString(status.getKey());
            message.appendString(" ");
            message.appendString(status.getValue());
            message.appendString("/");
        }
        
        message.appendStringWithBreak("/");
    }
    
    /**
     * Gets the GameClient for this user.
     * @return GameClient object, or null if user is not online
     */
    public GameClient getClient() {
        if (game == null || game.getClientManager() == null || isBot) {
            return null;
        }
        return game.getClientManager().getClientByHabbo(habboId);
    }
    
    /**
     * Gets the Room this user is in.
     * @return Room object, or null if room not loaded
     */
    public Room getRoom() {
        if (game == null || game.getRoomManager() == null) {
            return null;
        }
        return game.getRoomManager().getRoom(roomId);
    }
    
    // Getters and setters for chat commands
    public int getIdleTime() { return idleTime; }
    public void incrementIdleTime() { this.idleTime++; }
    public int getCarryTimer() { return carryTimer; }
    public void setCarryTimer(int carryTimer) { this.carryTimer = carryTimer; }
    public void decrementCarryTimer() { if (carryTimer > 0) carryTimer--; }
    public int getTeleDelay() { return teleDelay; }
    public void setTeleDelay(int teleDelay) { this.teleDelay = teleDelay; }
    public void setIdleTime(int idleTime) { this.idleTime = idleTime; }
    public boolean isAllowOverride() { return allowOverride; }
    public void setAllowOverride(boolean allowOverride) { this.allowOverride = allowOverride; }
}
