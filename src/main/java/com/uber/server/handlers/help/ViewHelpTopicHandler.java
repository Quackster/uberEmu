package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for viewing a help topic (message ID 418).
 */
public class ViewHelpTopicHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ViewHelpTopicHandler.class);
    private final Game game;
    
    public ViewHelpTopicHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long topicId = message.popWiredUInt();
        
        // Note: This handler may be unused - GetFaqTextMessageComposerHandler is registered for ID 418
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 418);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Re-read topicId after event
        message.resetPointer();
        topicId = message.popWiredUInt();
        
        com.uber.server.game.support.HelpTopic topic = game.getHelpTool().getTopic(topicId);
        if (topic != null) {
            client.sendMessage(game.getHelpTool().serializeTopic(topic));
        }
    }
}
