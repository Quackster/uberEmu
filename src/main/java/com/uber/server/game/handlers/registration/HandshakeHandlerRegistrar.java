package com.uber.server.game.handlers.registration;

import com.uber.server.game.Game;
import com.uber.server.handlers.handshake.SendSessionParamsHandler;
import com.uber.server.messages.PacketHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers handshake packet handlers.
 */
public class HandshakeHandlerRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(HandshakeHandlerRegistrar.class);
    
    private final PacketHandlerRegistry registry;
    private final Game game;
    
    public HandshakeHandlerRegistrar(PacketHandlerRegistry registry, Game game) {
        this.registry = registry;
        this.game = game;
    }
    
    /**
     * Registers all handshake handlers.
     */
    public void register() {
        // TODO: move this handler over to match the xml
        registry.register(206, new com.uber.server.messages.incoming.handshake.SendSessionParametersMessageComposerHandler()); // SendSessionParamsHandler (ID 206)

        // GetSessionParameters handler (ID 1817)
        registry.register(1817, new com.uber.server.messages.incoming.handshake.GetSessionParametersMessageComposerHandler()); // GetSessionParametersMessageComposer (ID 1817)
        
        // SSO Login handler (ID 415)
        registry.register(415, new com.uber.server.messages.incoming.handshake.SSOTicketMessageComposerHandler(game)); // SSOTicketMessageComposer (ID 415)
        
        logger.debug("Registered handshake handlers");
    }
}
