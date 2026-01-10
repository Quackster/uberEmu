package com.uber.server.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods as being event handler methods.
 * Similar to Bukkit/Spigot/Paper EventHandler annotation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
    /**
     * Define the priority of the event handler.
     * @return Priority of the event handler
     */
    EventPriority priority() default EventPriority.NORMAL;
    
    /**
     * Define if the handler ignores a cancelled event.
     * @return true if the handler ignores a cancelled event
     */
    boolean ignoreCancelled() default false;
}
