package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for picking a ticket (message ID 450).
 */
public class ModPickTicketHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModPickTicketHandler.class);
    private final Game game;
    
    public ModPickTicketHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: Handler reads junk + ticketId, but event expects just ticketId (as per PacketEventFactory)
        // Reading according to event structure (junk will be skipped by reading ticketId directly)
        int ticketId = message.popWiredInt32(); // This matches PacketEventFactory pattern
        
        com.uber.server.event.packet.help.ModPickTicketEvent event = new com.uber.server.event.packet.help.ModPickTicketEvent(
            client, message, ticketId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        ticketId = event.getTicketId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_mod")) {
            return;
        }
        
        game.getModerationTool().pickTicket(client, ticketId);
    }
}
