package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;

/**
 * Handler for GetRoomData1MessageComposer (ID 215).
 * Processes first room data request in room entry sequence.
 */
public class GetRoomData1MessageComposerHandler implements IncomingMessageHandler {
    private final Game game;
    
    public GetRoomData1MessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int roomId = message.popWiredInt32();
        
        com.uber.server.event.packet.room.GetRoomData1Event event = new com.uber.server.event.packet.room.GetRoomData1Event(client, message, roomId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        roomId = event.getRoomId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getLoadingRoom() <= 0) {
            return;
        }
        
        // Send empty response
        var composer = new com.uber.server.messages.outgoing.rooms.FurnitureAliasesComposer(0);
        client.sendMessage(composer.compose());
    }
}
