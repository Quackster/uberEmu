package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetPopularRoomTagsMessageComposer (ID 382).
 * Processes popular room tags requests from the client.
 */
public class GetPopularRoomTagsMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetPopularRoomTagsMessageComposerHandler.class);
    private final Game game;
    
    public GetPopularRoomTagsMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.GetPopularRoomTagsEvent event = new com.uber.server.event.packet.navigator.GetPopularRoomTagsEvent(
            client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        if (game.getNavigator() == null) {
            return;
        }
        
        // TODO: Replace with PopularRoomTagsResultEventComposer (ID 452)
        client.sendMessage(game.getNavigator().serializePopularRoomTags());
    }
}
