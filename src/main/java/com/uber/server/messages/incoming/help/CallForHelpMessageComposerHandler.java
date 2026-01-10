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
            String ticketMessage = StringUtil.filterInjectionChars(message.popFixedString());
            int junk = message.popWiredInt32(); // Unused
            int type = message.popWiredInt32();
            long reportedUserId = message.popWiredUInt();
            
            game.getModerationTool().sendNewTicket(client, type, reportedUserId, ticketMessage);
        }
        
        // TODO: Replace with CallForHelpResultMessageEventComposer (ID 321)
        ServerMessage response = new ServerMessage(321);
        response.appendBoolean(errorOccurred);
        client.sendMessage(response);
    }
}
