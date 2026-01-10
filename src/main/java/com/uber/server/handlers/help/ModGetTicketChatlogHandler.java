package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.game.support.SupportTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting ticket chat log (message ID 457).
 */
public class ModGetTicketChatlogHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModGetTicketChatlogHandler.class);
    private final Game game;
    
    public ModGetTicketChatlogHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: PacketEventFactory shows ticketId as int, but handler reads as long
        // Matching event structure: reading as int per PacketEventFactory
        int ticketId = message.popWiredInt32();
        
        com.uber.server.event.packet.help.ModGetTicketChatlogEvent event = new com.uber.server.event.packet.help.ModGetTicketChatlogEvent(
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
        
        long ticketIdLong = ticketId;
        SupportTicket ticket = game.getModerationTool().getTicket(ticketIdLong);
        
        if (ticket == null) {
            return;
        }
        
        com.uber.server.game.rooms.RoomData roomData = game != null && game.getRoomManager() != null ?
                                                 game.getRoomManager().generateNullableRoomData(ticket.getRoomId()) : null;
        
        if (roomData == null) {
            return;
        }
        
        com.uber.server.messages.ServerMessage chatlogMessage = 
            game.getModerationTool().serializeTicketChatlog(ticket, roomData, ticket.getTimestamp());
        if (chatlogMessage != null) {
            client.sendMessage(chatlogMessage);
        }
    }
}
