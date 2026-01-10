package com.uber.server.handlers.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for sending an instant message (message ID 33).
 * Ported from Messages/Requests/Messenger.cs SendInstantMessenger()
 */
public class SendInstantMessengerHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SendInstantMessengerHandler.class);
    private final Game game;
    
    public SendInstantMessengerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getMessenger() == null) {
            return;
        }
        
        long userId = message.popWiredUInt();
        String messageText = StringUtil.filterInjectionChars(message.popFixedString(), true);
        
        habbo.getMessenger().sendInstantMessage(userId, messageText);
    }
}
