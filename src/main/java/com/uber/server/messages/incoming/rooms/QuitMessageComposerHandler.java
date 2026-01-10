package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for QuitMessageComposer (ID 53).
 * Processes room exit requests from the client.
 */
public class QuitMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(QuitMessageComposerHandler.class);
    private final Game game;
    
    public QuitMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.room.QuitRoomEvent event = new com.uber.server.event.packet.room.QuitRoomEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        var room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room != null) {
            room.removeUserFromRoom(client, true, false);
        }
    }
}
