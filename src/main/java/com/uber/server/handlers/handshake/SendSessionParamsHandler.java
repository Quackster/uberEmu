package com.uber.server.handlers.handshake;

import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for sending session parameters (message ID 206).
 */
public class SendSessionParamsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SendSessionParamsHandler.class);
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 206);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        ServerMessage response = new ServerMessage(257);
        response.appendInt32(9);
        response.appendInt32(0);
        response.appendInt32(0);
        response.appendInt32(1);
        response.appendInt32(1);
        response.appendInt32(3);
        response.appendInt32(0);
        response.appendInt32(2);
        response.appendInt32(1);
        response.appendInt32(4);
        response.appendInt32(0);
        response.appendInt32(5);
        response.appendStringWithBreak("dd-MM-yyyy");
        response.appendInt32(7);
        response.appendBoolean(false);
        response.appendInt32(8);
        response.appendStringWithBreak("hotel-co.uk");
        response.appendInt32(9);
        response.appendBoolean(false);
        
        client.sendMessage(response);
    }
}
