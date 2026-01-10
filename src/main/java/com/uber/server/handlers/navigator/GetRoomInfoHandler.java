package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting room info (message ID 385).
 */
public class GetRoomInfoHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetRoomInfoHandler.class);
    private final Game game;
    
    public GetRoomInfoHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int roomId = message.popWiredInt32();
        boolean unk = message.popWiredBoolean(); // Unused
        boolean unk2 = message.popWiredBoolean(); // Unused
        
        com.uber.server.event.packet.navigator.GetGuestRoomEvent event = new com.uber.server.event.packet.navigator.GetGuestRoomEvent(client, message, roomId);
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
        if (data == null) {
            return;
        }
        
        ServerMessage response = new ServerMessage(454);
        response.appendInt32(0); // Show events flag
        data.serialize(response, false);
        client.sendMessage(response);
    }
}
