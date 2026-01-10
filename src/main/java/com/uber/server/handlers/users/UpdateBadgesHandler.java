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
 * Ported from Messages/Requests/Users.cs UpdateBadges()
 */
public class UpdateBadgesHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(UpdateBadgesHandler.class);
    private final Game game;
    
    public UpdateBadgesHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
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
        
        // Update slots from message
        while (message.getRemainingLength() > 0) {
            int slot = message.popWiredInt32();
            String badgeCode = message.popFixedString();
            
            if (badgeCode == null || badgeCode.isEmpty()) {
                continue;
            }
            
            // Validate: user must have badge and slot must be 1-5
            if (!badgeComponent.hasBadge(badgeCode) || slot < 1 || slot > 5) {
                // Invalid request - ignore
                continue;
            }
            
            // Set badge slot
            Badge badge = badgeComponent.getBadge(badgeCode);
            if (badge != null) {
                badge.setSlot(slot);
                // Update in database
                game.getBadgeRepository().updateBadgeSlot(habbo.getId(), badgeCode, slot);
            }
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
