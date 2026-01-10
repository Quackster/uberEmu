package com.uber.server.game.handlers.registration;

import com.uber.server.game.Game;
import com.uber.server.messages.PacketHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers catalog-related packet handlers.
 */
public class CatalogHandlerRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(CatalogHandlerRegistrar.class);
    
    private final PacketHandlerRegistry registry;
    private final Game game;
    
    public CatalogHandlerRegistrar(PacketHandlerRegistry registry, Game game) {
        this.registry = registry;
        this.game = game;
    }
    
    /**
     * Registers all catalog handlers.
     */
    public void register() {
        // GetCatalogIndex handler (ID 101)
        registry.register(101, new com.uber.server.messages.incoming.catalog.GetCatalogIndexComposerHandler(game)); // GetCatalogIndexComposer (ID 101)
        
        // GetCatalogPage handler (ID 102)
        registry.register(102, new com.uber.server.messages.incoming.catalog.GetCatalogPageComposerHandler(game)); // GetCatalogPageComposer (ID 102)
        
        // HandlePurchase handler (ID 100) - CRITICAL
        registry.register(100, new com.uber.server.messages.incoming.catalog.HandlePurchaseMessageComposerHandler(game)); // HandlePurchaseMessageComposer (ID 100)
        
        // RedeemVoucher handler (ID 129)
        registry.register(129, new com.uber.server.messages.incoming.catalog.RedeemVoucherMessageComposerHandler(game)); // RedeemVoucherMessageComposer (ID 129)
        
        // Advanced catalog handlers
        registry.register(472, new com.uber.server.handlers.catalog.PurchaseGiftHandler(game)); // PurchaseGift
        registry.register(412, new com.uber.server.handlers.catalog.GetRecyclerRewardsHandler(game)); // GetRecyclerRewards
        registry.register(3030, new com.uber.server.handlers.catalog.CanGiftHandler(game)); // CanGift
        registry.register(3011, new com.uber.server.handlers.catalog.GetCatalogData1Handler(game)); // GetCatalogData1
        registry.register(473, new com.uber.server.handlers.catalog.GetCatalogData2Handler(game)); // GetCatalogData2
        registry.register(42, new com.uber.server.messages.incoming.catalog.CheckPetNameMessageComposerHandler(game)); // CheckPetNameMessageComposer (ID 42)
        
        // Marketplace handlers
        registry.register(3012, new com.uber.server.handlers.catalog.MarketplaceCanSellHandler(game)); // MarketplaceCanSell
        registry.register(3010, new com.uber.server.handlers.catalog.MarketplacePostItemHandler(game)); // MarketplacePostItem
        registry.register(3019, new com.uber.server.handlers.catalog.MarketplaceGetOwnOffersHandler(game)); // MarketplaceGetOwnOffers
        registry.register(3015, new com.uber.server.handlers.catalog.MarketplaceTakeBackHandler(game)); // MarketplaceTakeBack
        registry.register(3016, new com.uber.server.handlers.catalog.MarketplaceClaimCreditsHandler(game)); // MarketplaceClaimCredits
        registry.register(3018, new com.uber.server.handlers.catalog.MarketplaceGetOffersHandler(game)); // MarketplaceGetOffers
        registry.register(3014, new com.uber.server.handlers.catalog.MarketplacePurchaseHandler(game)); // MarketplacePurchase
        
        logger.debug("Registered catalog handlers");
    }
}
