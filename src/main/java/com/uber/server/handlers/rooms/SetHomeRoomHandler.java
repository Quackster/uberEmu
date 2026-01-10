package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for setting home room (message ID 384).
 */
public class SetHomeRoomHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SetHomeRoomHandler.class);
    private final Game game;
    
    public SetHomeRoomHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int roomId = message.popWiredInt32();
        
        com.uber.server.event.packet.room.SetHomeRoomEvent event = new com.uber.server.event.packet.room.SetHomeRoomEvent(client, message, roomId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        roomId = event.getRoomId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        long roomIdLong = roomId;
        com.uber.server.game.rooms.RoomData data = game.getRoomManager().generateRoomData(roomIdLong);
        
        if (roomIdLong != 0) {
            if (data == null || !data.getOwner().toLowerCase().equals(habbo.getUsername().toLowerCase())) {
                return;
            }
        }
        
        // Update home room
        habbo.setHomeRoom(roomIdLong);
        game.getUserRepository().updateHomeRoom(habbo.getId(), roomIdLong);
        
        // Send confirmation
        ServerMessage response = new ServerMessage(455);
        response.appendUInt(roomIdLong);
        client.sendMessage(response);
    }
}
