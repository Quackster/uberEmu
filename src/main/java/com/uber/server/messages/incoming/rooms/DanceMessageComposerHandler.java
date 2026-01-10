package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for DanceMessageComposer (ID 93).
 * Processes dance actions from the client.
 */
public class DanceMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(DanceMessageComposerHandler.class);
    private final Game game;
    
    public DanceMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int danceId = message.popWiredInt32();
        
        com.uber.server.event.packet.room.DanceEvent event = new com.uber.server.event.packet.room.DanceEvent(client, message, danceId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        danceId = event.getDanceId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        var room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        var roomUser = room.getRoomUserByHabbo(habbo.getId());
        if (roomUser == null) {
            return;
        }
        
        roomUser.unidle();
        
        // Validate dance ID (0-4, and 2-4 require club membership)
        if (danceId < 0 || danceId > 4 || 
            (!habbo.hasFuse("fuse_use_club_dance") && danceId > 1)) {
            danceId = 0;
        }
        
        // Stop carrying item if dancing
        if (danceId > 0 && roomUser.getCarryItemId() > 0) {
            roomUser.carryItem(0);
        }
        
        roomUser.setDanceId(danceId);
        
        // Send DanceMessageEvent (outgoing ID 480 from _events[480])
        var danceComposer = new com.uber.server.messages.outgoing.rooms.DanceMessageComposer(
            roomUser.getVirtualId(), danceId);
        room.sendMessage(danceComposer.compose());
    }
}
