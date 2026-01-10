package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.rooms.RoomData;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting a single public room info (message ID 388).
 * Returns room data for a public room.
 */
public class GetPubHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetPubHandler.class);
    private final Game game;
    
    public GetPubHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.room.GetPubEvent event = new com.uber.server.event.packet.room.GetPubEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        long roomId = message.popWiredUInt();
        
        if (game.getRoomManager() == null) {
            return;
        }
        
        RoomData data = game.getRoomManager().generateRoomData(roomId);
        
        if (data == null || !data.isPublicRoom()) {
            return;
        }
        
        // Send room data (ID 453)
        ServerMessage response = new ServerMessage(453);
        response.appendUInt(data.getId());
        response.appendStringWithBreak(data.getCCTs() != null ? data.getCCTs() : "");
        response.appendUInt(data.getId());
        client.sendMessage(response);
    }
}
