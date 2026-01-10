package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for releasing a ticket (message ID 451).
 */
public class ModReleaseTicketHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModReleaseTicketHandler.class);
    private final Game game;
    
    public ModReleaseTicketHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: Handler reads amount and loops through multiple tickets
        // ModReleaseTicketEvent expects just single ticketId (int)
        // Handler needs to process multiple tickets, so using GenericPacketEvent due to structure mismatch
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 451);
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
        int amount = message.popWiredInt32();
        
        // Release multiple tickets
        for (int i = 0; i < amount; i++) {
            long ticketId = message.popWiredUInt();
            game.getModerationTool().releaseTicket(client, ticketId);
        }
    }
}
