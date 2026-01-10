package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.game.rooms.RoomData;
import com.uber.server.game.rooms.RoomModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetRoomData2MessageComposer (ID 390).
 * Processes second room data request in room entry sequence.
 */
public class GetRoomData2MessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetRoomData2MessageComposerHandler.class);
    private final Game game;
    
    public GetRoomData2MessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int roomId = message.popWiredInt32();
        
        com.uber.server.event.packet.room.GetRoomData2Event event = new com.uber.server.event.packet.room.GetRoomData2Event(client, message, roomId);
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
        
        RoomData data = game.getRoomManager().generateRoomData(habbo.getLoadingRoom());
        if (data == null) {
            habbo.setLoadingRoom(0);
            habbo.setLoadingChecksPassed(false);
            return;
        }
        
        RoomModel model = null;
        if (data != null && data.getModelName() != null) {
            model = game.getRoomManager().getModel(data.getModelName());
        }
        if (model == null) {
            client.sendNotif("Sorry, model data is missing from this room and therefore cannot be loaded.");
            var composer = new com.uber.server.messages.outgoing.rooms.RoomEntryErrorMessageEventComposer();
            client.sendMessage(composer.compose());
            habbo.setLoadingRoom(0);
            habbo.setLoadingChecksPassed(false);
            return;
        }
        
        // Send heightmap and relative heightmap
        var heightmapComposer = new com.uber.server.messages.outgoing.rooms.HeightMapComposer(model.serializeHeightmap());
        client.sendMessage(heightmapComposer.compose());
        var relativeHeightmapComposer = new com.uber.server.messages.outgoing.rooms.FloorHeightMapComposer(model.serializeRelativeHeightmap());
        client.sendMessage(relativeHeightmapComposer.compose());
    }
}
