package com.uber.server.messages.incoming.rooms;

import com.uber.server.event.packet.room.StopTypingEvent;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for StopTypingMessageComposer (ID 318).
 * Processes typing stop notifications from the client.
 */
public class StopTypingMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(StopTypingMessageComposerHandler.class);
    private final Game game;
    
    public StopTypingMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        StopTypingEvent event = new StopTypingEvent(client, message);
        Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        com.uber.server.game.rooms.RoomUser roomUser = room.getRoomUserByHabbo(habbo.getId());
        if (roomUser == null) {
            return;
        }
        
        var composer = new com.uber.server.messages.outgoing.rooms.UserTypingMessageComposer(
            roomUser.getVirtualId(), false);
        room.sendMessage(composer.compose());
    }
}
