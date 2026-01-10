package com.uber.server.messages.incoming.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetFaqTextMessageComposer (ID 418).
 * Processes FAQ text requests from the client.
 */
public class GetFaqTextMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetFaqTextMessageComposerHandler.class);
    private final Game game;
    
    public GetFaqTextMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        long topicId = message.popWiredUInt();
        
        var topic = game.getHelpTool().getTopic(topicId);
        if (topic != null) {
            // TODO: Replace with FaqTextMessageEventComposer (ID 520)
            client.sendMessage(game.getHelpTool().serializeTopic(topic));
        }
    }
}
