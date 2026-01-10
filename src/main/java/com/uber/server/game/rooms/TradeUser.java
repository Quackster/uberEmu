package com.uber.server.game.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.users.inventory.UserItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a user in a trade.
 */
public class TradeUser {
    private static final Logger logger = LoggerFactory.getLogger(TradeUser.class);
    
    private final long userId;
    private final long roomId;
    private final Game game;
    private final ConcurrentHashMap<Long, UserItem> offeredItems;
    private boolean accepted;
    
    public TradeUser(long userId, long roomId, Game game) {
        this.userId = userId;
        this.roomId = roomId;
        this.game = game;
        this.offeredItems = new ConcurrentHashMap<>();
        this.accepted = false;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public boolean hasAccepted() {
        return accepted;
    }
    
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
    
    /**
     * Gets list of offered items.
     * @return List of UserItems
     */
    public List<UserItem> getOfferedItems() {
        return new ArrayList<>(offeredItems.values());
    }
    
    /**
     * Adds an item to offered items.
     * @param item UserItem to add
     */
    public void addOfferedItem(UserItem item) {
        if (item != null) {
            offeredItems.put(item.getId(), item);
        }
    }
    
    /**
     * Removes an item from offered items.
     * @param itemId Item ID to remove
     */
    public void removeOfferedItem(long itemId) {
        offeredItems.remove(itemId);
    }
    
    /**
     * Gets the RoomUser for this trade user.
     * @return RoomUser object, or null if not found
     */
    public RoomUser getRoomUser() {
        if (game == null || game.getRoomManager() == null) {
            return null;
        }
        
        Room room = game.getRoomManager().getRoom(roomId);
        if (room == null) {
            return null;
        }
        
        return room.getRoomUserByHabbo(userId);
    }
    
    /**
     * Gets the GameClient for this trade user.
     * @return GameClient object, or null if user is not online
     */
    public GameClient getClient() {
        if (game == null || game.getClientManager() == null) {
            return null;
        }
        return game.getClientManager().getClientByHabbo(userId);
    }
}
