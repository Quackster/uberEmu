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
 */
public class RateRoomHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(RateRoomHandler.class);
    private final Game game;
    
    public RateRoomHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int roomId = message.popWiredInt32();
        int rating = message.popWiredInt32();
        
        com.uber.server.event.packet.room.RateFlatEvent event = new com.uber.server.event.packet.room.RateFlatEvent(
            client, message, roomId, rating);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        roomId = event.getRoomId();
        rating = event.getRating();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        // Check if user already rated or is owner
        if (habbo.getRatedRooms().contains(room.getRoomId()) || room.checkRights(client, true)) {
            return;
        }
        
        // Update room score
        int scoreChange = switch (rating) {
            case -1 -> -1;
            case 1 -> 1;
            default -> 0; // Invalid rating
        };
        
        int newScore = room.getData().getScore() + scoreChange;
        room.getData().setScore(newScore);
        
        // Update database
        game.getRoomRepository().updateRoomScore(room.getRoomId(), newScore);
        
        // Mark room as rated
        habbo.addRatedRoom(room.getRoomId());
        
        // Send updated score
        var composer = new com.uber.server.messages.outgoing.rooms.RoomRatingComposer(newScore);
        client.sendMessage(composer.compose());
    }
}
