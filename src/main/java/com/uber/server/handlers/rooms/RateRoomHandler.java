package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for rating a room (message ID 261).
 * Ported from Messages/Requests/Rooms.cs RateRoom()
 */
public class RateRoomHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(RateRoomHandler.class);
    private final Game game;
    
    public RateRoomHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        // Check if user already rated or is owner
        if (habbo.getRatedRooms().contains(room.getRoomId()) || room.checkRights(client, true)) {
            return;
        }
        
        int rating = message.popWiredInt32();
        
        // Update room score
        int scoreChange = 0;
        switch (rating) {
            case -1:
                scoreChange = -1;
                break;
            case 1:
                scoreChange = 1;
                break;
            default:
                return; // Invalid rating
        }
        
        int newScore = room.getData().getScore() + scoreChange;
        room.getData().setScore(newScore);
        
        // Update database
        game.getRoomRepository().updateRoomScore(room.getRoomId(), newScore);
        
        // Mark room as rated
        habbo.addRatedRoom(room.getRoomId());
        
        // Send updated score
        ServerMessage response = new ServerMessage(345);
        response.appendInt32(newScore);
        client.sendMessage(response);
    }
}
