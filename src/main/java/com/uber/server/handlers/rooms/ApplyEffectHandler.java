package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for applying an effect (message ID 372).
 */
public class ApplyEffectHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApplyEffectHandler.class);
    private final Game game;
    
    public ApplyEffectHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int effectId = message.popWiredInt32();
        
        com.uber.server.event.packet.room.ApplyEffectEvent event = new com.uber.server.event.packet.room.ApplyEffectEvent(client, message, effectId);
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
        habbo.getAvatarEffectsInventoryComponent().applyEffect(effectId);
    }
}
