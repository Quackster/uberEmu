package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting popular tags (message ID 382).
 */
public class GetPopularTagsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetPopularTagsHandler.class);
    private final Game game;
    
    public GetPopularTagsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.GetPopularRoomTagsEvent event = new com.uber.server.event.packet.navigator.GetPopularRoomTagsEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        if (game.getNavigator() == null) {
            return;
        }
        
        client.sendMessage(game.getNavigator().serializePopularRoomTags());
    }
}
