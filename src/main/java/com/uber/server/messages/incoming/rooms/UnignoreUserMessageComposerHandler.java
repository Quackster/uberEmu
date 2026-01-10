package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for UnignoreUserMessageComposer (ID 322).
 * Processes user unignore requests from the client.
 */
public class UnignoreUserMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(UnignoreUserMessageComposerHandler.class);
    private final Game game;
    
    public UnignoreUserMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String username = message.popFixedString();
        
        com.uber.server.event.packet.room.UnignoreUserEvent event = new com.uber.server.event.packet.room.UnignoreUserEvent(client, message, username);
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
        
        // TODO: Implement user unignoring functionality
        // long userId = message.popWiredUInt();
        // if (habbo.getMutedUsers().contains(userId)) {
        //     habbo.getMutedUsers().remove(userId);
        //     var composer = new com.uber.server.messages.outgoing.users.IgnoreResultMessageEventComposer(3);
        //     client.sendMessage(composer.compose());
        // }
        
        logger.debug("UnignoreUser called by user {}", habbo.getId());
    }
}
