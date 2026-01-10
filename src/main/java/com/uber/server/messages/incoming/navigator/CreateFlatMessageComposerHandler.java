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
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        String roomName = StringUtil.filterInjectionChars(message.popFixedString(), true);
        String modelName = message.popFixedString();
        String roomState = message.popFixedString(); // Unused - room open by default on creation
        
        if (roomName == null || modelName == null) {
            return;
        }
        
        var newRoom = game.getRoomManager().createRoom(client, roomName, modelName);
        
        if (newRoom != null) {
            // Send FlatCreatedEvent (outgoing ID 59 from _events[59])
            var composer = new com.uber.server.messages.outgoing.navigator.FlatCreatedComposer(
                newRoom.getId(), newRoom.getName());
            client.sendMessage(composer.compose());
        }
    }
}
