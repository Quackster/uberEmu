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
 * Handler for entering infobus (message ID 113).
 */
public class EnterInfobusHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(EnterInfobusHandler.class);
    private final Game game;
    
    public EnterInfobusHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.room.EnterInfobusEvent event = new com.uber.server.event.packet.room.EnterInfobusEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        // Infobus is currently closed
        var composer = new com.uber.server.messages.outgoing.rooms.ParkBusCannotEnterComposer(
            "The Uber Infobus is not yet in use.");
        client.sendMessage(composer.compose());
    }
}
