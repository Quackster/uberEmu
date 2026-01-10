package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting room tool data (message ID 459).
 */
public class ModGetRoomToolHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModGetRoomToolHandler.class);
    private final Game game;
    
    public ModGetRoomToolHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: PacketEventFactory shows roomId as int
        int roomId = message.popWiredInt32();
        
        com.uber.server.event.packet.help.ModGetRoomToolEvent event = new com.uber.server.event.packet.help.ModGetRoomToolEvent(
            client, message, roomId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        roomId = event.getRoomId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_mod")) {
            return;
        }
        
        long roomIdLong = roomId;
        com.uber.server.game.rooms.RoomData roomData = game != null && game.getRoomManager() != null ?
                                                 game.getRoomManager().generateNullableRoomData(roomIdLong) : null;
        
        if (roomData == null) {
            return;
        }
        
        com.uber.server.messages.ServerMessage toolMessage = game.getModerationTool().serializeRoomTool(roomData);
        if (toolMessage != null) {
            client.sendMessage(toolMessage);
        }
    }
}
