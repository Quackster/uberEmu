package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetGuestRoomMessageComposer (ID 385).
 * Processes room info requests from the client.
 */
public class GetGuestRoomMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetGuestRoomMessageComposerHandler.class);
    private final Game game;
    
    public GetGuestRoomMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        long roomId = message.popWiredUInt();
        boolean unk = message.popWiredBoolean(); // Unused
        boolean unk2 = message.popWiredBoolean(); // Unused
        
        var data = game.getRoomManager().generateRoomData(roomId);
        if (data == null) {
            return;
        }
        
        // TODO: Replace with GetGuestRoomResultEventComposer (ID 454)
        ServerMessage response = new ServerMessage(454);
        response.appendInt32(0); // Show events flag
        data.serialize(response, false);
        client.sendMessage(response);
    }
}
