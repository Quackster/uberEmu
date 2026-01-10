package com.uber.server.handlers.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.util.AntiMutant;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for saving a wardrobe item (message ID 376).
 */
public class SaveWardrobeHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SaveWardrobeHandler.class);
    private final Game game;
    
    public SaveWardrobeHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || game.getWardrobeRepository() == null) {
            return;
        }
        
        long slotId = message.popWiredUInt();
        String look = message.popFixedString();
        String gender = message.popFixedString();
        
        // Filter and validate look
        look = StringUtil.filterInjectionChars(look);
        if (!AntiMutant.validateLook(look, gender)) {
            return;
        }
        
        // Save wardrobe item
        String genderUpper = gender != null ? gender.toUpperCase() : "M";
        if (!game.getWardrobeRepository().saveWardrobeItem(habbo.getId(), slotId, look, genderUpper)) {
            logger.warn("Failed to save wardrobe item for user {}", habbo.getId());
        }
    }
}
