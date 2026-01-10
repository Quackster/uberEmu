package com.uber.server.event;

/**
 * Represents the priority of an event handler.
 * Similar to Bukkit/Spigot/Paper EventPriority enum.
 */
public enum EventPriority {
    /**
     * Event call is of very low importance and should be run first,
     * to allow other plugins to further customize the outcome
     */
    LOWEST(0),
    
    /**
     * Event call is of low importance
     */
    LOW(1),
    
    /**
     * Event call is neither important nor unimportant, and may be run normally
     */
    NORMAL(2),
    
    /**
     * Event call is of high importance
     */
    HIGH(3),
    
    /**
     * Event call is critical and must have the final say in what happens to the event
     */
    HIGHEST(4),
    
    /**
     * Event is listened to purely for monitoring the outcome of an event.
     * No modifications to the event should be made under this priority
     */
    MONITOR(5);
    
    private final int slot;
    
    EventPriority(int slot) {
        this.slot = slot;
    }
    
    /**
     * Gets the slot value for this priority.
     * @return Slot value
     */
    public int getSlot() {
        return slot;
    }
}
