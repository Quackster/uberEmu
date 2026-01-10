package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetUserFlatCatsMessageComposer (ID 151).
 * Processes room category requests from the client.
 */
public class GetUserFlatCatsMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetUserFlatCatsMessageComposerHandler.class);
    private final Game game;
    
    public GetUserFlatCatsMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.GetUserFlatCatsEvent event = new com.uber.server.event.packet.navigator.GetUserFlatCatsEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        var navigator = game.getNavigator();
        if (navigator == null) {
            return;
        }
        
        // TODO: Replace with UserFlatCatsEventComposer (ID 221)
        client.sendMessage(navigator.serializeRoomCategories());
    }
}
