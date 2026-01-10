package com.uber.server.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.items.Item;
import com.uber.server.messages.ServerMessage;
import com.uber.server.users.inventory.UserItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a trade between two users.
 * Ported from HabboHotel/Rooms/Trade.cs
 */
public class Trade {
    private static final Logger logger = LoggerFactory.getLogger(Trade.class);
    
    private final long oneId;
    private final long twoId;
    private final long roomId;
    private final Game game;
    private final ConcurrentHashMap<Long, TradeUser> users;
    private int tradeStage;
    
    public Trade(long userOneId, long userTwoId, long roomId, Game game) {
        this.oneId = userOneId;
        this.twoId = userTwoId;
        this.roomId = roomId;
        this.game = game;
        this.users = new ConcurrentHashMap<>();
        this.tradeStage = 1;
        
        users.put(userOneId, new TradeUser(userOneId, roomId, game));
        users.put(userTwoId, new TradeUser(userTwoId, roomId, game));
        
        // Add trade status to room users
        Room room = getRoom();
        if (room != null) {
            for (TradeUser tradeUser : users.values()) {
                RoomUser roomUser = tradeUser.getRoomUser();
                if (roomUser != null && !roomUser.getStatuses().containsKey("trd")) {
                    roomUser.addStatus("trd", "");
                    roomUser.setUpdateNeeded(true);
                }
            }
        }
        
        // Send trade start message
        ServerMessage message = new ServerMessage(104);
        message.appendUInt(userOneId);
        message.appendBoolean(true);
        message.appendUInt(userTwoId);
        message.appendBoolean(true);
        sendMessageToUsers(message);
    }
    
    /**
     * Checks if all users have accepted the trade.
     * @return True if all users accepted
     */
    public boolean isAllUsersAccepted() {
        for (TradeUser user : users.values()) {
            if (!user.hasAccepted()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks if trade contains a user.
     * @param userId User ID
     * @return True if user is in trade
     */
    public boolean containsUser(long userId) {
        return users.containsKey(userId);
    }
    
    /**
     * Gets a TradeUser by ID.
     * @param userId User ID
     * @return TradeUser object, or null if not found
     */
    public TradeUser getTradeUser(long userId) {
        return users.get(userId);
    }
    
    /**
     * Offers an item to the trade.
     * Ported from Trade.cs OfferItem()
     * @param userId User ID
     * @param item UserItem to offer
     */
    public void offerItem(long userId, UserItem item) {
        TradeUser tradeUser = getTradeUser(userId);
        if (tradeUser == null || item == null || tradeUser.hasAccepted() || tradeStage != 1) {
            return;
        }
        
        // Check if item allows trade
        Item baseItem = item.getBaseItem();
        if (baseItem == null || !baseItem.allowTrade()) {
            return;
        }
        
        clearAccepted();
        tradeUser.addOfferedItem(item);
        updateTradeWindow();
    }
    
    /**
     * Takes back an item from the trade.
     * Ported from Trade.cs TakeBackItem()
     * @param userId User ID
     * @param item UserItem to take back
     */
    public void takeBackItem(long userId, UserItem item) {
        TradeUser tradeUser = getTradeUser(userId);
        if (tradeUser == null || item == null || tradeUser.hasAccepted() || tradeStage != 1) {
            return;
        }
        
        clearAccepted();
        tradeUser.removeOfferedItem(item.getId());
        updateTradeWindow();
    }
    
    /**
     * Accepts the trade (stage 1).
     * Ported from Trade.cs Accept()
     * @param userId User ID
     */
    public void accept(long userId) {
        TradeUser tradeUser = getTradeUser(userId);
        if (tradeUser == null || tradeStage != 1) {
            return;
        }
        
        tradeUser.setAccepted(true);
        
        ServerMessage message = new ServerMessage(109);
        message.appendUInt(userId);
        message.appendBoolean(true);
        sendMessageToUsers(message);
        
        if (isAllUsersAccepted()) {
            sendMessageToUsers(new ServerMessage(111));
            tradeStage = 2;
            clearAccepted();
        }
    }
    
    /**
     * Unaccepts the trade (stage 1).
     * Ported from Trade.cs Unaccept()
     * @param userId User ID
     */
    public void unaccept(long userId) {
        TradeUser tradeUser = getTradeUser(userId);
        if (tradeUser == null || tradeStage != 1 || isAllUsersAccepted()) {
            return;
        }
        
        tradeUser.setAccepted(false);
        
        ServerMessage message = new ServerMessage(109);
        message.appendUInt(userId);
        message.appendBoolean(false);
        sendMessageToUsers(message);
    }
    
    /**
     * Completes the trade (stage 2).
     * Ported from Trade.cs CompleteTrade()
     * @param userId User ID
     */
    public void completeTrade(long userId) {
        TradeUser tradeUser = getTradeUser(userId);
        if (tradeUser == null || tradeStage != 2) {
            return;
        }
        
        tradeUser.setAccepted(true);
        
        ServerMessage message = new ServerMessage(109);
        message.appendUInt(userId);
        message.appendBoolean(true);
        sendMessageToUsers(message);
        
        if (isAllUsersAccepted()) {
            tradeStage = 999;
            deliverItems();
            closeTradeClean();
        }
    }
    
    /**
     * Clears accepted status for all users.
     * Ported from Trade.cs ClearAccepted()
     */
    public void clearAccepted() {
        for (TradeUser user : users.values()) {
            user.setAccepted(false);
        }
    }
    
    /**
     * Updates the trade window.
     * Ported from Trade.cs UpdateTradeWindow()
     */
    public void updateTradeWindow() {
        ServerMessage message = new ServerMessage(108);
        
        for (TradeUser tradeUser : users.values()) {
            message.appendUInt(tradeUser.getUserId());
            List<UserItem> offeredItems = tradeUser.getOfferedItems();
            message.appendInt32(offeredItems.size());
            
            for (UserItem item : offeredItems) {
                Item baseItem = item.getBaseItem();
                if (baseItem == null) {
                    continue;
                }
                
                message.appendUInt(item.getId());
                message.appendStringWithBreak(baseItem.getType().toLowerCase());
                message.appendUInt(item.getId());
                message.appendInt32(baseItem.getSpriteId());
                message.appendBoolean(true);
                message.appendBoolean(true);
                message.appendStringWithBreak(item.getExtraData() != null ? item.getExtraData() : "");
                message.appendBoolean(false); // xmas 09 special tag
                message.appendBoolean(false); // xmas 09 special tag
                message.appendBoolean(false); // xmas 09 special tag
                
                if ("s".equalsIgnoreCase(baseItem.getType())) {
                    message.appendInt32(-1);
                }
            }
        }
        
        sendMessageToUsers(message);
    }
    
    /**
     * Delivers items to users.
     * Ported from Trade.cs DeliverItems()
     */
    public void deliverItems() {
        TradeUser userOne = getTradeUser(oneId);
        TradeUser userTwo = getTradeUser(twoId);
        
        if (userOne == null || userTwo == null) {
            return;
        }
        
        GameClient clientOne = userOne.getClient();
        GameClient clientTwo = userTwo.getClient();
        
        if (clientOne == null || clientTwo == null || 
            clientOne.getHabbo() == null || clientTwo.getHabbo() == null) {
            return;
        }
        
        // Verify items are still in inventory
        List<UserItem> itemsOne = userOne.getOfferedItems();
        List<UserItem> itemsTwo = userTwo.getOfferedItems();
        
        for (UserItem item : itemsOne) {
            if (clientOne.getHabbo().getInventoryComponent().getItem(item.getId()) == null) {
                clientOne.sendNotif("Trade failed.");
                clientTwo.sendNotif("Trade failed.");
                return;
            }
        }
        
        for (UserItem item : itemsTwo) {
            if (clientTwo.getHabbo().getInventoryComponent().getItem(item.getId()) == null) {
                clientOne.sendNotif("Trade failed.");
                clientTwo.sendNotif("Trade failed.");
                return;
            }
        }
        
        // Deliver items
        for (UserItem item : itemsOne) {
            clientOne.getHabbo().getInventoryComponent().removeItem(item.getId());
            clientTwo.getHabbo().getInventoryComponent().addItem(item.getId(), item.getBaseItemId(), item.getExtraData());
        }
        
        for (UserItem item : itemsTwo) {
            clientTwo.getHabbo().getInventoryComponent().removeItem(item.getId());
            clientOne.getHabbo().getInventoryComponent().addItem(item.getId(), item.getBaseItemId(), item.getExtraData());
        }
        
        // Update inventories
        clientOne.getHabbo().getInventoryComponent().updateItems(false);
        clientTwo.getHabbo().getInventoryComponent().updateItems(false);
    }
    
    /**
     * Closes trade cleanly (after completion).
     * Ported from Trade.cs CloseTradeClean()
     */
    public void closeTradeClean() {
        for (TradeUser tradeUser : users.values()) {
            RoomUser roomUser = tradeUser.getRoomUser();
            if (roomUser != null) {
                roomUser.removeStatus("trd");
                roomUser.setUpdateNeeded(true);
            }
        }
        
        sendMessageToUsers(new ServerMessage(112));
        
        // Remove from room's active trades
        Room room = getRoom();
        if (room != null) {
            room.removeActiveTrade(this);
        }
    }
    
    /**
     * Closes trade (cancelled).
     * Ported from Trade.cs CloseTrade()
     * @param userId User ID who closed the trade
     */
    public void closeTrade(long userId) {
        for (TradeUser tradeUser : users.values()) {
            RoomUser roomUser = tradeUser.getRoomUser();
            if (roomUser != null) {
                roomUser.removeStatus("trd");
                roomUser.setUpdateNeeded(true);
            }
        }
        
        ServerMessage message = new ServerMessage(110);
        message.appendUInt(userId);
        sendMessageToUsers(message);
    }
    
    /**
     * Sends a message to both trade users.
     * Ported from Trade.cs SendMessageToUsers()
     * @param message ServerMessage to send
     */
    public void sendMessageToUsers(ServerMessage message) {
        if (message == null) {
            return;
        }
        
        for (TradeUser tradeUser : users.values()) {
            GameClient client = tradeUser.getClient();
            if (client != null) {
                client.sendMessage(message);
            }
        }
    }
    
    /**
     * Gets the Room this trade is in.
     * @return Room object, or null if not found
     */
    private Room getRoom() {
        if (game == null || game.getRoomManager() == null) {
            return null;
        }
        return game.getRoomManager().getRoom(roomId);
    }
    
    public long getOneId() { return oneId; }
    public long getTwoId() { return twoId; }
    public long getRoomId() { return roomId; }
    public int getTradeStage() { return tradeStage; }
}
