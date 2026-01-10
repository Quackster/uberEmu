package com.uber.server.handlers.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for sending an instant invite (message ID 34).
 */
public class SendInstantInviteHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SendInstantInviteHandler.class);
    private final Game game;
    
    public SendInstantInviteHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int count = message.popWiredInt32();
        List<Long> userIds = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            userIds.add(message.popWiredUInt());
        }
        
        String inviteMessage = message.popFixedString();
        
        com.uber.server.event.packet.messenger.SendRoomInviteEvent event = new com.uber.server.event.packet.messenger.SendRoomInviteEvent(client, message, userIds, inviteMessage);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        userIds = event.getUserIds();
        inviteMessage = event.getMessage();
        
        // Filter injection characters
        inviteMessage = StringUtil.filterInjectionChars(inviteMessage, true);
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getMessenger() == null) {
            return;
        }
        
        ServerMessage invite = new ServerMessage(135);
        invite.appendUInt(habbo.getId());
        invite.appendStringWithBreak(inviteMessage);
        
        // Send invite to all specified users who are friends
        for (Long userId : userIds) {
            if (!habbo.getMessenger().friendshipExists(habbo.getId(), userId)) {
                continue; // Not a friend
            }
            
            GameClient targetClient = game.getClientManager().getClientByHabbo(userId);
            if (targetClient != null) {
                targetClient.sendMessage(invite);
            }
        }
    }
}
