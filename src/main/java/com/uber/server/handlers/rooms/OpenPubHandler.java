package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.rooms.RoomData;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for opening a public room (message ID 2).
 * Opens a public room by preparing it for user entry.
 */
public class OpenPubHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(OpenPubHandler.class);
    private final Game game;
    
    public OpenPubHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.room.OpenPubEvent event = new com.uber.server.event.packet.room.OpenPubEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        message.popWiredInt32(); // Junk
        long roomId = message.popWiredUInt();
        message.popWiredInt32(); // Junk2
        
        if (game.getRoomManager() == null) {
            return;
        }
        
        RoomData data = game.getRoomManager().generateRoomData(roomId);
        
        if (data == null || !data.isPublicRoom()) {
            return;
        }
        
        // Use OpenConnectionMessageComposerHandler's prepareRoomForUser method
        // since it's already registered for similar functionality (ID 391)
        com.uber.server.messages.incoming.rooms.OpenConnectionMessageComposerHandler handler = 
            new com.uber.server.messages.incoming.rooms.OpenConnectionMessageComposerHandler(game);
        handler.prepareRoomForUser(client, habbo, data.getId(), "");
    }
}
