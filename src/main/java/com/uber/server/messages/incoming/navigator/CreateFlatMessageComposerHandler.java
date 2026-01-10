package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for CreateFlatMessageComposer (ID 29).
 * Processes room creation requests from the client.
 */
public class CreateFlatMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(CreateFlatMessageComposerHandler.class);
    private final Game game;
    
    public CreateFlatMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String name = message.popFixedString();
        String description = message.popFixedString();
        String model = message.popFixedString();
        int categoryId = message.popWiredInt32();
        int maxUsers = message.popWiredInt32();
        int tradeMode = message.popWiredInt32();
        
        com.uber.server.event.packet.navigator.CreateFlatEvent event = new com.uber.server.event.packet.navigator.CreateFlatEvent(client, message, name, description, model, categoryId, maxUsers, tradeMode);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        name = event.getName();
        description = event.getDescription();
        model = event.getModel();
        categoryId = event.getCategoryId();
        maxUsers = event.getMaxUsers();
        tradeMode = event.getTradeMode();
        
        // Filter injection characters
        String roomName = StringUtil.filterInjectionChars(name, true);
        
        if (roomName == null || model == null) {
            return;
        }
        
        var newRoom = game.getRoomManager().createRoom(client, roomName, model);
        
        if (newRoom != null) {
            // Send FlatCreatedEvent (outgoing ID 59 from _events[59])
            var composer = new com.uber.server.messages.outgoing.navigator.FlatCreatedComposer(
                newRoom.getId(), newRoom.getName());
            client.sendMessage(composer.compose());
        }
    }
}
