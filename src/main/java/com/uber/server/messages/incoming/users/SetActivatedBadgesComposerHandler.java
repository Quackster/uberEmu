package com.uber.server.messages.incoming.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.game.users.badges.Badge;
import com.uber.server.game.users.badges.BadgeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for SetActivatedBadgesComposer (ID 158).
 * Processes badge slot updates from the client.
 */
public class SetActivatedBadgesComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(SetActivatedBadgesComposerHandler.class);
    private final Game game;
    
    public SetActivatedBadgesComposerHandler(Game game) {
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
        com.uber.server.messages.ServerMessage response = new com.uber.server.messages.ServerMessage(228);
        response.appendUInt(habbo.getId());
        response.appendInt32(badgeComponent.getEquippedCount());
        
        for (Badge badge : badgeComponent.getBadgeList()) {
            if (badge.getSlot() > 0) {
                response.appendInt32(badge.getSlot());
                response.appendStringWithBreak(badge.getCode());
            }
        }
        
        var badgesComposer = new com.uber.server.messages.outgoing.users.HabboUserBadgesComposer(response);
        
        // Send to room if user is in room, otherwise just to client
        if (habbo.isInRoom() && game.getRoomManager() != null) {
            com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
            if (room != null) {
                room.sendMessage(badgesComposer.compose());
            } else {
                client.sendMessage(badgesComposer.compose());
            }
        } else {
            client.sendMessage(badgesComposer.compose());
        }
    }
}
