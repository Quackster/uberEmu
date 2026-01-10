package com.uber.server.messages.incoming.handshake;

import com.uber.server.event.packet.handshake.GetSessionParametersEvent;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetSessionParametersMessageComposer (ID 1817).
 * Processes session parameters requests from the client.
 */
public class GetSessionParametersMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetSessionParametersMessageComposerHandler.class);
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        GetSessionParametersEvent event = new GetSessionParametersEvent(client, message);
        Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        // TODO: Replace with SessionParamsMessageEventComposer (ID 0x0101)
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
