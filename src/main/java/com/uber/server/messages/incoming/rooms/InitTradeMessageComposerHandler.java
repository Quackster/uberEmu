package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for InitTradeMessageComposer (ID 71).
 * Processes trade initiation requests from the client.
 */
public class InitTradeMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(InitTradeMessageComposerHandler.class);
    private final Game game;
    
    public InitTradeMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.canTradeInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.RoomUser user = room.getRoomUserByHabbo(habbo.getId());
        if (user == null) {
            return;
        }
        
        int virtualId = message.popWiredInt32();
        com.uber.server.game.rooms.RoomUser user2 = room.getRoomUserByVirtualId(virtualId);
        
        if (user2 == null) {
            return;
        }
        
        room.tryStartTrade(user, user2);
    }
}
