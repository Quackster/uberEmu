package com.uber.server.messages.incoming.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for CallForHelpMessageComposer (ID 453).
 * Processes help ticket submission requests from the client.
 */
public class CallForHelpMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(CallForHelpMessageComposerHandler.class);
    private final Game game;
    
    public CallForHelpMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String messageText = message.popFixedString();
        int categoryId = message.popWiredInt32();
        int roomId = message.popWiredInt32();
        
        com.uber.server.event.packet.help.CallForHelpEvent event = new com.uber.server.event.packet.help.CallForHelpEvent(client, message, messageText, categoryId, roomId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        messageText = event.getMessage();
        categoryId = event.getCategoryId();
        roomId = event.getRoomId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        boolean errorOccurred = false;
        
        // Check if user already has a pending ticket
        if (game.getModerationTool().userHasPendingTicket(habbo.getId())) {
            errorOccurred = true;
        }
        
        if (!errorOccurred) {
            String ticketMessage = StringUtil.filterInjectionChars(messageText);
            // Use categoryId as type, roomId for context
            game.getModerationTool().sendNewTicket(client, categoryId, 0, ticketMessage);
        }
        
        // TODO: Replace with CallForHelpResultMessageEventComposer (ID 321)
        ServerMessage response = new ServerMessage(321);
        response.appendBoolean(errorOccurred);
        client.sendMessage(response);
    }
}
