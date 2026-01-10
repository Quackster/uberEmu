package com.uber.server.game.rooms.services;

import com.uber.server.game.Game;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service for managing trades in a room.
 * Handles starting, stopping, and querying active trades.
 */
public class RoomTradeService {
    private static final Logger logger = LoggerFactory.getLogger(RoomTradeService.class);
    
    private final Room room;
    private final CopyOnWriteArrayList<Trade> activeTrades;
    private final Game game;
    
    public RoomTradeService(Room room, CopyOnWriteArrayList<Trade> activeTrades, Game game) {
        this.room = room;
        this.activeTrades = activeTrades;
        this.game = game;
    }
    
    /**
     * Checks if a user has an active trade.
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
     */
    public void tryStartTrade(com.uber.server.game.rooms.RoomUser userOne, 
                              com.uber.server.game.rooms.RoomUser userTwo) {
        if (userOne == null || userTwo == null || userOne.isBot() || userTwo.isBot()) {
            return;
        }
        
        // Check if users are already trading
        if (hasActiveTrade(userOne.getHabboId()) || hasActiveTrade(userTwo.getHabboId())) {
            return;
        }
        
        // Check if room allows trading
        if (!room.canTradeInRoom()) {
            return;
        }
        
        // Create and start trade
        Trade trade = new Trade(userOne.getHabboId(), userTwo.getHabboId(), room.getRoomId(), game);
        activeTrades.add(trade);
    }
    
    /**
     * Tries to stop a trade for a user.
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
     */
    public void removeActiveTrade(Trade trade) {
        if (trade != null) {
            activeTrades.remove(trade);
        }
    }
    
    /**
     * Closes all active trades.
     */
    public void closeAllTrades() {
        for (Trade trade : activeTrades) {
            trade.closeTrade(0); // Close without specific user
        }
        activeTrades.clear();
    }
}
