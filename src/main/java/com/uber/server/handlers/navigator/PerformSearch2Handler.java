package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for performing a search (variant 2) (message ID 438).
 */
public class PerformSearch2Handler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(PerformSearch2Handler.class);
    private final Game game;
    
    public PerformSearch2Handler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int junk = message.popWiredInt32(); // Unused
        String tag = message.popFixedString();
        
        com.uber.server.event.packet.navigator.RoomTagSearchEvent event = new com.uber.server.event.packet.navigator.RoomTagSearchEvent(client, message, tag);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        tag = event.getTag();
        
        if (game.getNavigator() == null) {
            return;
        }
        
        String searchQuery = tag;
        client.sendMessage(game.getNavigator().serializeSearchResults(searchQuery));
    }
}
