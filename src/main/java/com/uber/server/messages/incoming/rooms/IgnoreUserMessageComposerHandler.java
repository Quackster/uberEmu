package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for IgnoreUserMessageComposer (ID 319).
 * Processes user ignore requests from the client.
 */
public class IgnoreUserMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(IgnoreUserMessageComposerHandler.class);
    private final Game game;
    
    public IgnoreUserMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String username = message.popFixedString();
        
        com.uber.server.event.packet.room.IgnoreUserEvent event = new com.uber.server.event.packet.room.IgnoreUserEvent(client, message, username);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        username = event.getUsername();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        // TODO: Implement user ignoring functionality
        // long userId = message.popWiredUInt();
        // if (!habbo.getMutedUsers().contains(userId)) {
        //     habbo.getMutedUsers().add(userId);
        //     var composer = new com.uber.server.messages.outgoing.users.IgnoreResultMessageEventComposer(1);
        //     client.sendMessage(composer.compose());
        // }
        
        logger.debug("IgnoreUser called by user {}", habbo.getId());
    }
}
