package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting room categories (message ID 151).
 */
public class GetRoomCategoriesHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetRoomCategoriesHandler.class);
    private final Game game;
    
    public GetRoomCategoriesHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 151);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        com.uber.server.game.navigator.Navigator navigator = game.getNavigator();
        if (navigator == null) {
            return;
        }
        
        client.sendMessage(navigator.serializeRoomCategories());
    }
}
