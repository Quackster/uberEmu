package com.uber.server.game;

import com.uber.server.game.handlers.registration.*;
import com.uber.server.messages.PacketHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes and registers all packet handlers.
 * Refactored to use handler registrar classes for better organization.
 */
public class HandlerInitializer {
    private static final Logger logger = LoggerFactory.getLogger(HandlerInitializer.class);
    
    private final GameEnvironment environment;
    private final Game game;
    private final PacketHandlerRegistry registry;
    
    public HandlerInitializer(GameEnvironment environment, Game game, PacketHandlerRegistry registry) {
        this.environment = environment;
        this.game = game;
        this.registry = registry;
    }
    
    /**
     * Registers all packet handlers.
     * Delegates to handler registrar classes.
     */
    public void registerAllHandlers() {
        logger.info("Registering packet handlers...");
        
        // Create and use registrar classes
        new GlobalHandlerRegistrar(registry).register();
        new HandshakeHandlerRegistrar(registry, game).register();
        new UserHandlerRegistrar(registry, game).register();
        new MessengerHandlerRegistrar(registry, game).register();
        new NavigatorHandlerRegistrar(registry, game).register();
        new RoomHandlerRegistrar(registry, game).register();
        new CatalogHandlerRegistrar(registry, game).register();
        new HelpHandlerRegistrar(registry, game).register();
        
        logger.info("Registered {} packet handlers", registry.size());
    }
}
