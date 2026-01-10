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
 * Ported from Messages/Requests/Rooms.cs ApplyEffect()
 */
public class ApplyEffectHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApplyEffectHandler.class);
    private final Game game;
    
    public ApplyEffectHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        int effectId = message.popWiredInt32();
        habbo.getAvatarEffectsInventoryComponent().applyEffect(effectId);
    }
}
