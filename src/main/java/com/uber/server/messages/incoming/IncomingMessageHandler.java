package com.uber.server.messages.incoming;

import com.uber.server.messages.PacketHandler;

/**
 * Base interface for incoming message handlers.
 * All handlers that process messages from the client should implement this interface.
 */
public interface IncomingMessageHandler extends PacketHandler {
    // Marker interface - PacketHandler already provides the handle method
}
