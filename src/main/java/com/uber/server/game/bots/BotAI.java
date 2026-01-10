package com.uber.server.game.bots;

import com.uber.server.game.GameClient;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomUser;

/**
 * Abstract base class for bot AI implementations.
 */
public abstract class BotAI {
    protected int baseId;
    protected int roomUserId;
    protected long roomId;
    
    /**
     * Initializes the bot AI.
     * @param baseId Base ID (-1 for regular bots, pet ID for pets)
     * @param roomUserId Virtual ID of the RoomUser
     * @param roomId Room ID
     */
    public void init(int baseId, int roomUserId, long roomId) {
        this.baseId = baseId;
        this.roomUserId = roomUserId;
        this.roomId = roomId;
    }
    
    /**
     * Gets the room this bot is in.
     * @return Room instance
     */
    protected Room getRoom() {
        return com.uber.server.game.Game.getInstance().getRoomManager().getRoom(roomId);
    }
    
    /**
     * Gets the RoomUser instance for this bot.
     * @return RoomUser instance
     */
    protected RoomUser getRoomUser() {
        Room room = getRoom();
        if (room == null) {
            return null;
        }
        return room.getRoomUserByVirtualId(roomUserId);
    }
    
    /**
     * Gets the bot data for this bot.
     * @return RoomBot instance
     */
    protected RoomBot getBotData() {
        RoomUser user = getRoomUser();
        if (user == null) {
            return null;
        }
        return user.getBotData();
    }
    
    /**
     * Called when the bot enters the room.
     */
    public abstract void onSelfEnterRoom();
    
    /**
     * Called when the bot leaves the room.
     * @param kicked True if the bot was kicked
     */
    public abstract void onSelfLeaveRoom(boolean kicked);
    
    /**
     * Called when a user enters the room.
     * @param user RoomUser that entered
     */
    public abstract void onUserEnterRoom(RoomUser user);
    
    /**
     * Called when a user leaves the room.
     * @param client GameClient that left
     */
    public abstract void onUserLeaveRoom(GameClient client);
    
    /**
     * Called when a user says something in the room.
     * @param user RoomUser that spoke
     * @param message Message text
     */
    public abstract void onUserSay(RoomUser user, String message);
    
    /**
     * Called when a user shouts in the room.
     * @param user RoomUser that shouted
     * @param message Message text
     */
    public abstract void onUserShout(RoomUser user, String message);
    
    /**
     * Called on each timer tick (every 500ms).
     */
    public abstract void onTimerTick();
}
