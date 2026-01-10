package com.uber.server.game.handlers.registration;

import com.uber.server.game.Game;
import com.uber.server.messages.PacketHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers user-related packet handlers.
 */
public class UserHandlerRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(UserHandlerRegistrar.class);
    
    private final PacketHandlerRegistry registry;
    private final Game game;
    
    public UserHandlerRegistrar(PacketHandlerRegistry registry, Game game) {
        this.registry = registry;
        this.game = game;
    }
    
    /**
     * Registers all user handlers.
     */
    public void register() {
        // GetUserInfo handler (ID 7)
        registry.register(7, new com.uber.server.messages.incoming.handshake.InfoRetrieveMessageComposerHandler()); // InfoRetrieveMessageComposer (ID 7)
        
        // GetBalance handler (ID 8)
        registry.register(8, new com.uber.server.messages.incoming.users.GetCreditsInfoComposerHandler(game)); // GetCreditsInfoComposer (ID 8)
        
        // GetSubscriptionData handler (ID 26)
        registry.register(26, new com.uber.server.messages.incoming.users.GetSubscriptionDataMessageComposerHandler(game)); // GetSubscriptionDataMessageComposer (ID 26)
        
        // UpdateLook handler (ID 44)
        registry.register(44, new com.uber.server.messages.incoming.users.ChangeLooksMessageComposerHandler(game)); // ChangeLooksMessageComposer (ID 44)
        
        // GetBadges handler (ID 157)
        registry.register(157, new com.uber.server.messages.incoming.users.GetBadgesComposerHandler(game)); // GetBadgesComposer (ID 157)
        
        // UpdateBadges handler (ID 158)
        registry.register(158, new com.uber.server.messages.incoming.users.SetActivatedBadgesComposerHandler(game)); // SetActivatedBadgesComposer (ID 158)
        
        // GetInventory handler (ID 404)
        registry.register(404, new com.uber.server.messages.incoming.users.RequestFurniInventoryComposerHandler(game)); // RequestFurniInventoryComposer (ID 404)
        
        // GetPetsInventory handler (ID 3000)
        registry.register(3000, new com.uber.server.messages.incoming.users.GetPetInventoryComposerHandler(game)); // GetPetInventoryComposer (ID 3000)
        
        // GetAchievements handler (ID 370)
        registry.register(370, new com.uber.server.messages.incoming.users.GetAchievementsComposerHandler(game)); // GetAchievementsComposer (ID 370)
        
        // GetWardrobe handler (ID 375)
        registry.register(375, new com.uber.server.handlers.users.GetWardrobeHandler(game)); // GetWardrobe (ID 375)
        
        // SaveWardrobe handler (ID 376)
        registry.register(376, new com.uber.server.handlers.users.SaveWardrobeHandler(game)); // SaveWardrobe (ID 376)
        
        logger.debug("Registered user handlers");
    }
}
