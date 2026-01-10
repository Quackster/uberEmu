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
 * Handler for waving (message ID 94).
 * Ported from Messages/Requests/Rooms.cs Wave()
 */
public class WaveHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(WaveHandler.class);
    private final Game game;
    
    public WaveHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        com.uber.server.rooms.RoomUser roomUser = room.getRoomUserByHabbo(habbo.getId());
        if (roomUser == null) {
            return;
        }
        
        roomUser.unidle();
        roomUser.setDanceId(0); // Stop dancing when waving
        
        ServerMessage waveMessage = new ServerMessage(481);
        waveMessage.appendInt32(roomUser.getVirtualId());
        room.sendMessage(waveMessage);
    }
}
