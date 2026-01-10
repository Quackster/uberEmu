package com.uber.server.messages.incoming.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for SendRoomInviteMessageComposer (ID 34).
 * Processes room invite requests from the client.
 */
public class SendRoomInviteMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(SendRoomInviteMessageComposerHandler.class);
    private final Game game;
    
    public SendRoomInviteMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getMessenger() == null) {
            return;
        }
        
        int count = message.popWiredInt32();
        List<Long> userIds = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            userIds.add(message.popWiredUInt());
        }
        
        String inviteMessage = StringUtil.filterInjectionChars(message.popFixedString(), true);
        
        // TODO: Replace with RoomInviteEventComposer (ID 135)
        ServerMessage invite = new ServerMessage(135);
        invite.appendUInt(habbo.getId());
        invite.appendStringWithBreak(inviteMessage);
        
        // Send invite to all specified users who are friends
        for (Long userId : userIds) {
            if (!habbo.getMessenger().friendshipExists(habbo.getId(), userId)) {
                continue; // Not a friend
            }
            
            var targetClient = game.getClientManager().getClientByHabbo(userId);
            if (targetClient != null) {
                targetClient.sendMessage(invite);
            }
        }
    }
}
