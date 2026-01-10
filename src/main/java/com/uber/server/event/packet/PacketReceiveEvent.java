package com.uber.server.event.packet;

import com.uber.server.event.Cancellable;
import com.uber.server.event.Event;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Base class for all packet receive events.
 * All packet events extend this and are cancellable.
 */
public abstract class PacketReceiveEvent extends Event implements Cancellable {
    private final GameClient client;
    private final ClientMessage message;
    private final int packetId;
    private boolean cancelled = false;
    
    public PacketReceiveEvent(GameClient client, ClientMessage message, int packetId) {
        super(false); // All packet events are synchronous by default
        this.client = client;
        this.message = message;
        this.packetId = packetId;
    }
    
    /**
     * Gets the client that sent the packet.
     * @return GameClient instance
     */
    public GameClient getClient() {
        return client;
    }
    
    /**
     * Gets the raw message.
     * @return ClientMessage instance
     */
    public ClientMessage getMessage() {
        return message;
    }
    
    /**
     * Gets the packet ID.
     * @return Packet ID
     */
    public int getPacketId() {
        return packetId;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
