package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for deleting a pending Call for Help ticket (message ID 238).
 */
public class DeletePendingCallForHelpHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeletePendingCallForHelpHandler.class);
    private final Game game;
    
    public DeletePendingCallForHelpHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: This handler may be unused - DeletePendingCallsForHelpMessageComposerHandler is registered for ID 238
        com.uber.server.event.packet.help.DeletePendingCallsForHelpEvent event = new com.uber.server.event.packet.help.DeletePendingCallsForHelpEvent(
            client, message);
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
        
        // Send response (ID 320)
        ServerMessage response = new ServerMessage(320);
        client.sendMessage(response);
    }
}
