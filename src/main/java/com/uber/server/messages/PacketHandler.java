package com.uber.server.messages;

import com.uber.server.game.GameClient;

/**
 * Interface for packet handlers.
 * Each packet handler processes a specific client message.
 */
public interface PacketHandler {
    /**
     * Handles a client message.
     * @param client The game client that sent the message
     * @param message The client message to process
     */
    void handle(GameClient client, ClientMessage message);
}
