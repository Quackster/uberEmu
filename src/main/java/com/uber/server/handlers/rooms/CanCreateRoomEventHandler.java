package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for checking if user can create a room event (message ID 345).
 */
public class CanCreateRoomEventHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(CanCreateRoomEventHandler.class);
    private final Game game;
    
    public CanCreateRoomEventHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.room.CanCreateRoomEventEvent event = new com.uber.server.event.packet.room.CanCreateRoomEventEvent(
            client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        
        boolean allow = true;
        int errorCode = 0;
        
        // Check if room state is open (0 = open, 1 = locked, 2 = password)
        if (room.getData().getState() != 0) {
            allow = false;
            errorCode = 3;
        }
        
        var composer = new com.uber.server.messages.outgoing.rooms.CanCreateRoomEventComposer(allow, errorCode);
        client.sendMessage(composer.compose());
    }
}
