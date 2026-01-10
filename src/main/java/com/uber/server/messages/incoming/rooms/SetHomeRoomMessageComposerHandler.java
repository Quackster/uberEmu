package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for SetHomeRoomMessageComposer (ID 384).
 * Processes home room setting requests from the client.
 */
public class SetHomeRoomMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(SetHomeRoomMessageComposerHandler.class);
    private final Game game;
    
    public SetHomeRoomMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        long roomId = message.popWiredUInt();
        com.uber.server.game.rooms.RoomData data = game.getRoomManager().generateRoomData(roomId);
        
        if (roomId != 0) {
            if (data == null || !data.getOwner().toLowerCase().equals(habbo.getUsername().toLowerCase())) {
                return;
            }
        }
        
        // Update home room
        habbo.setHomeRoom(roomId);
        game.getUserRepository().updateHomeRoom(habbo.getId(), roomId);
        
        // Send confirmation
        var homeRoomComposer = new com.uber.server.messages.outgoing.rooms.NavigatorSettingsComposer(roomId);
        client.sendMessage(homeRoomComposer.compose());
    }
}
