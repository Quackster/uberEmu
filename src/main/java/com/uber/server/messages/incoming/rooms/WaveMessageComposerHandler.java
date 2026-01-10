package com.uber.server.messages.incoming.rooms;

import com.uber.server.event.packet.room.WaveEvent;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for WaveMessageComposer (ID 94).
 * Processes wave actions from the client.
 */
public class WaveMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(WaveMessageComposerHandler.class);
    private final Game game;
    
    public WaveMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        WaveEvent event = new WaveEvent(client, message);
        Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
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
        roomUser.setDanceId(0); // Stop dancing when waving
        
        // Send WaveMessageEvent (outgoing ID 481 from _events[481])
        var waveComposer = new com.uber.server.messages.outgoing.rooms.WaveMessageComposer(
            roomUser.getVirtualId());
        room.sendMessage(waveComposer.compose());
    }
}
