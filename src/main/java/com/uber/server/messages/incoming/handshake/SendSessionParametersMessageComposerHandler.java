package com.uber.server.messages.incoming.handshake;

import com.uber.server.event.packet.handshake.SendSessionParametersEvent;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for SendSessionParametersMessageComposer (ID 206).
 * Processes session parameter requests from the client.
 * Note: Class name inferred from pattern - should be verified against XML.
 */
public class SendSessionParametersMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(SendSessionParametersMessageComposerHandler.class);
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        SendSessionParametersEvent event = new SendSessionParametersEvent(client, message);
        Game.getInstance().getEventManager().callEvent(event);
        
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
        
        var composer = new com.uber.server.messages.outgoing.handshake.SessionParamsComposer(response);
        client.sendMessage(composer.compose());
    }
}
