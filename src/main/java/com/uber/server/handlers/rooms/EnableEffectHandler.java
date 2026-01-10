package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for enabling an effect (message ID 373).
 */
public class EnableEffectHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(EnableEffectHandler.class);
    private final Game game;
    
    public EnableEffectHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int effectId = message.popWiredInt32();
        
        com.uber.server.event.packet.room.EnableEffectEvent event = new com.uber.server.event.packet.room.EnableEffectEvent(client, message, effectId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        effectId = event.getEffectId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        habbo.getAvatarEffectsInventoryComponent().enableEffect(effectId);
    }
}
