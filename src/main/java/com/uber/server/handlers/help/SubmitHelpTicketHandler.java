package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for submitting a help ticket (message ID 453).
 */
public class SubmitHelpTicketHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SubmitHelpTicketHandler.class);
    private final Game game;
    
    public SubmitHelpTicketHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: This handler uses different parsing than CallForHelpEvent (legacy format?)
        // CallForHelpMessageComposerHandler is registered for ID 453 with different structure
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 453);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
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
        
        // Send response (ID 321)
        ServerMessage response = new ServerMessage(321);
        response.appendBoolean(errorOccurred);
        client.sendMessage(response);
    }
}
