package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for closing a ticket (message ID 452).
 */
public class ModCloseTicketHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModCloseTicketHandler.class);
    private final Game game;
    
    public ModCloseTicketHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: Handler reads result, junk, ticketId, but ModCloseTicketEvent expects just ticketId (int)
        // Handler needs result field, so using GenericPacketEvent due to structure mismatch
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 452);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_mod")) {
            return;
        }
        
        // Re-read after event (GenericPacketEvent doesn't store fields)
        message.resetPointer();
        int result = message.popWiredInt32(); // 1 = invalid, 2 = abusive, 3 = resolved
        int junk = message.popWiredInt32(); // Unused
        long ticketId = message.popWiredUInt();
        
        game.getModerationTool().closeTicket(client, ticketId, result);
    }
}
