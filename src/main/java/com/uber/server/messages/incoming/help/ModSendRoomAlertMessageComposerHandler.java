package com.uber.server.messages.incoming.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for ModSendRoomAlertMessageComposer (ID 200).
 * Processes moderation room alert requests from the client.
 */
public class ModSendRoomAlertMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModSendRoomAlertMessageComposerHandler.class);
    private final Game game;
    
    public ModSendRoomAlertMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_alert")) {
            return;
        }
        
        int one = message.popWiredInt32(); // Unused
        int two = message.popWiredInt32();
        String alertMessage = message.popFixedString();
        
        long roomId = habbo.getCurrentRoomId();
        boolean caution = (two != 3); // If two == 3, it's a message, not a caution
        
        game.getModerationTool().roomAlert(roomId, caution, alertMessage);
    }
}
