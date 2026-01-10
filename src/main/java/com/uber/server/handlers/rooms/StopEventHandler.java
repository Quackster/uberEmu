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
 * Handler for stopping a room event (message ID 347).
 */
public class StopEventHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(StopEventHandler.class);
    private final Game game;
    
    public StopEventHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int eventId = message.popWiredInt32();
        
        com.uber.server.event.packet.room.StopEventEvent event = new com.uber.server.event.packet.room.StopEventEvent(client, message, eventId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        eventId = event.getEventId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true) || !room.hasOngoingEvent()) {
            return;
        }
        
        // Clear event
        room.setEvent(null);
        
        // Broadcast event stop message
        var stopComposer = new com.uber.server.messages.outgoing.rooms.RoomEventComposer();
        room.sendMessage(stopComposer.compose());
    }
}
