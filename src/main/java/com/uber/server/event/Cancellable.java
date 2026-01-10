package com.uber.server.event;

/**
 * Represents an event that can be cancelled.
 * Similar to Bukkit/Spigot/Paper Cancellable interface.
 */
public interface Cancellable {
    /**
     * Gets the cancellation state of this event.
     * @return true if this event is cancelled
     */
    boolean isCancelled();
    
    /**
     * Sets the cancellation state of this event.
     * @param cancel true if you wish to cancel this event
     */
    void setCancelled(boolean cancel);
}
