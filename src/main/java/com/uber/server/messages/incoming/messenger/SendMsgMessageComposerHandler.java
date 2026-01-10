package com.uber.server.messages.incoming.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for SendMsgMessageComposer (ID 33).
 * Processes instant message requests from the client.
 */
public class SendMsgMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(SendMsgMessageComposerHandler.class);
    private final Game game;
    
    public SendMsgMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long userId = message.popWiredUInt();
        String messageText = message.popFixedString();
        
        com.uber.server.event.packet.messenger.SendMsgEvent event = new com.uber.server.event.packet.messenger.SendMsgEvent(client, message, userId, messageText);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        userId = event.getUserId();
        messageText = event.getMessage();
        
        // Filter injection characters
        messageText = StringUtil.filterInjectionChars(messageText, true);
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getMessenger() == null) {
            return;
        }
        
        habbo.getMessenger().sendInstantMessage(userId, messageText);
    }
}
