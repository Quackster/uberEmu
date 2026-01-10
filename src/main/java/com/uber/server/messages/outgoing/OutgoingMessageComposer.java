package com.uber.server.messages.outgoing;

import com.uber.server.messages.ServerMessage;

/**
 * Base class for outgoing message composers.
 * All classes that create messages to send to the client should extend this class.
 */
public abstract class OutgoingMessageComposer {
    /**
     * Composes and returns a ServerMessage ready to send to the client.
     * @return ServerMessage with the appropriate message ID and body
     */
    public abstract ServerMessage compose();
}
