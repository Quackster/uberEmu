package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.rooms.RoomData;
import com.uber.server.game.rooms.RoomModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetRoomData2 (message ID 390).
 * Ported from Messages/Requests/Rooms.cs GetRoomData2()
 */
public class GetRoomData2Handler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetRoomData2Handler.class);
    private final Game game;
    
    public GetRoomData2Handler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
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
            client.sendMessage(new ServerMessage(18));
            habbo.setLoadingRoom(0);
            habbo.setLoadingChecksPassed(false);
            return;
        }
        
        // Send heightmap (ID 31) and relative heightmap (ID 470)
        client.sendMessage(model.serializeHeightmap());
        client.sendMessage(model.serializeRelativeHeightmap());
    }
}
