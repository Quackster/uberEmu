package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;

/**
 * Handler for GetRoomData1 (message ID 215).
 */
public class GetRoomData1Handler implements PacketHandler {
    private final Game game;
    
    public GetRoomData1Handler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getLoadingRoom() <= 0) {
            return;
        }
        
        // Send empty response
        var composer = new com.uber.server.messages.outgoing.rooms.GetRoomData1ResponseMessageEventComposer(0);
        client.sendMessage(composer.compose());
    }
}
