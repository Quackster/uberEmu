package com.uber.server.messages.incoming.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for DeletePendingCallsForHelpMessageComposer (ID 238).
 * Processes help ticket deletion requests from the client.
 */
public class DeletePendingCallsForHelpMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeletePendingCallsForHelpMessageComposerHandler.class);
    private final Game game;
    
    public DeletePendingCallsForHelpMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.help.DeletePendingCallsForHelpEvent event = new com.uber.server.event.packet.help.DeletePendingCallsForHelpEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        // Check if user has a pending ticket
        if (!game.getModerationTool().userHasPendingTicket(habbo.getId())) {
            return;
        }
        
        // Delete pending ticket
        game.getModerationTool().deletePendingTicketForUser(habbo.getId());
        
        // TODO: Replace with CallForHelpPendingCallsDeletedMessageEventComposer (ID 320)
        ServerMessage response = new ServerMessage(320);
        client.sendMessage(response);
    }
}
