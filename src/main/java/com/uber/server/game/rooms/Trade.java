package com.uber.server.game.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.items.Item;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.users.inventory.UserItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a trade between two users.
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
                if (roomUser != null && !roomUser.hasStatus("trd")) {
                    roomUser.addStatus("trd", "");
                    roomUser.setUpdateNeeded(true);
                }
            }
        }
        
        // Send trade start message
        var composer = new com.uber.server.messages.outgoing.rooms.TradingOpenComposer(
            userOneId, true, userTwoId, true);
        sendMessageToUsers(composer.compose());
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
 OfferItem()
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
 TakeBackItem()
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
 Accept()
     * @param userId User ID
     */
    public void accept(long userId) {
        TradeUser tradeUser = getTradeUser(userId);
        if (tradeUser == null || tradeStage != 1) {
            return;
        }
        
        tradeUser.setAccepted(true);
        
        var acceptComposer = new com.uber.server.messages.outgoing.rooms.TradingAcceptComposer(userId, true);
        sendMessageToUsers(acceptComposer.compose());
        
        if (isAllUsersAccepted()) {
            var confirmComposer = new com.uber.server.messages.outgoing.rooms.TradingConfirmationComposer();
            sendMessageToUsers(confirmComposer.compose());
            tradeStage = 2;
            clearAccepted();
        }
    }
    
    /**
     * Unaccepts the trade (stage 1).
 Unaccept()
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
 CompleteTrade()
     * @param userId User ID
     */
    public void completeTrade(long userId) {
        TradeUser tradeUser = getTradeUser(userId);
        if (tradeUser == null || tradeStage != 2) {
            return;
        }
        
        tradeUser.setAccepted(true);
        
        var acceptComposer = new com.uber.server.messages.outgoing.rooms.TradingAcceptComposer(userId, true);
        sendMessageToUsers(acceptComposer.compose());
        
        if (isAllUsersAccepted()) {
            tradeStage = 999;
            deliverItems();
            closeTradeClean();
        }
    }
    
    /**
     * Clears accepted status for all users.
 ClearAccepted()
     */
    public void clearAccepted() {
        for (TradeUser user : users.values()) {
            user.setAccepted(false);
        }
    }
    
    /**
     * Updates the trade window.
 UpdateTradeWindow()
     */
    public void updateTradeWindow() {
        ServerMessage message = new ServerMessage(108); // _events[108] = TradeUpdateEvent
        
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
 DeliverItems()
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
 CloseTradeClean()
     */
    public void closeTradeClean() {
        for (TradeUser tradeUser : users.values()) {
            RoomUser roomUser = tradeUser.getRoomUser();
            if (roomUser != null) {
                roomUser.removeStatus("trd");
                roomUser.setUpdateNeeded(true);
            }
        }
        
        var completeComposer = new com.uber.server.messages.outgoing.rooms.TradeCompleteEventComposer();
        sendMessageToUsers(completeComposer.compose());
        
        // Remove from room's active trades
        Room room = getRoom();
        if (room != null) {
            room.removeActiveTrade(this);
        }
    }
    
    /**
     * Closes trade (cancelled).
 CloseTrade()
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
        
        var closedComposer = new com.uber.server.messages.outgoing.rooms.TradingCloseComposer(userId);
        sendMessageToUsers(closedComposer.compose());
    }
    
    /**
     * Sends a message to both trade users.
 SendMessageToUsers()
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
