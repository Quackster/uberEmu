package com.uber.server.game.handlers.registration;

import com.uber.server.messages.PacketHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers global packet handlers (Pong, Ping, etc.).
 */
public class GlobalHandlerRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(GlobalHandlerRegistrar.class);
    
    private final PacketHandlerRegistry registry;
    
    public GlobalHandlerRegistrar(PacketHandlerRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * Registers all global handlers.
     */
    public void register() {
        // Pong handler (ID 196)
        registry.register(196, new com.uber.server.messages.incoming.global.PongMessageComposerHandler()); // PongMessageComposer (ID 196)
        
        // Ping handler (ID 50) - to be implemented
        // registry.register(50, new PingHandler());
        
        logger.debug("Registered global handlers");
    }
}
