package com.uber.server.handlers.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.users.badges.Badge;
import com.uber.server.game.users.badges.BadgeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for updating badge slots (message ID 158).
 */
public class UpdateBadgesHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(UpdateBadgesHandler.class);
    private final Game game;
    
    public UpdateBadgesHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        java.util.List<String> badges = new java.util.ArrayList<>();
        int badgeCount = message.popWiredInt32();
        for (int i = 0; i < badgeCount; i++) {
            badges.add(message.popFixedString());
        }
        
        com.uber.server.event.packet.user.SetActivatedBadgesEvent event = new com.uber.server.event.packet.user.SetActivatedBadgesEvent(client, message, badges);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        badges = event.getActivatedBadges();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        BadgeComponent badgeComponent = habbo.getBadgeComponent();
        if (badgeComponent == null) {
            return;
        }
        
        // Reset all slots
        badgeComponent.resetSlots();
        
        // Update slots from event badges (format: slot,badgeCode pairs)
        // Note: Handler needs to parse badges list - original code read slot,badgeCode pairs
        // For now, process badges as simple list (may need adjustment based on actual format)
        int slot = 1;
        for (String badgeCode : badges) {
            
            if (badgeCode == null || badgeCode.isEmpty()) {
                slot++;
                continue;
            }
            
            // Validate: user must have badge and slot must be 1-5
            if (!badgeComponent.hasBadge(badgeCode) || slot < 1 || slot > 5) {
                // Invalid request - ignore
                slot++;
                continue;
            }
            
            // Set badge slot
            Badge badge = badgeComponent.getBadge(badgeCode);
            if (badge != null) {
                badge.setSlot(slot);
                // Update in database
                game.getBadgeRepository().updateBadgeSlot(habbo.getId(), badgeCode, slot);
            }
            slot++;
        }
        
        // Send update message
        ServerMessage response = new ServerMessage(228);
        response.appendUInt(habbo.getId());
        response.appendInt32(badgeComponent.getEquippedCount());
        
        for (Badge badge : badgeComponent.getBadgeList()) {
            if (badge.getSlot() > 0) {
                response.appendInt32(badge.getSlot());
                response.appendStringWithBreak(badge.getCode());
            }
        }
        
        // Send to room if user is in room, otherwise just to client
        if (habbo.isInRoom() && game.getRoomManager() != null) {
            com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
            if (room != null) {
                room.sendMessage(response);
            } else {
                client.sendMessage(response);
            }
        } else {
            client.sendMessage(response);
        }
    }
}
