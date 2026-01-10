package com.uber.server.event;

import com.uber.server.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages event registration and dispatching.
 * Similar to Bukkit/Spigot/Paper event system.
 */
public class EventManager {
    private static final Logger logger = LoggerFactory.getLogger(EventManager.class);
    
    private final Map<Class<? extends Event>, Map<EventPriority, List<RegisteredListener>>> handlers;
    private final Map<Listener, List<RegisteredListener>> listenerMap;
    
    public EventManager() {
        this.handlers = new ConcurrentHashMap<>();
        this.listenerMap = new ConcurrentHashMap<>();
    }
    
    /**
     * Registers all event handlers in a listener class.
     * @param listener Listener instance
     * @param plugin Plugin instance that owns the listener
     */
    public void registerEvents(Listener listener, Plugin plugin) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        
        List<RegisteredListener> registered = new ArrayList<>();
        
        for (Method method : listener.getClass().getDeclaredMethods()) {
            EventHandler handler = method.getAnnotation(EventHandler.class);
            if (handler == null) {
                continue;
            }
            
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1) {
                logger.warn("Method {} in {} has invalid event handler signature (expected 1 parameter)", 
                    method.getName(), listener.getClass().getName());
                continue;
            }
            
            Class<?> eventClass = params[0];
            if (!Event.class.isAssignableFrom(eventClass)) {
                logger.warn("Method {} in {} has invalid event handler signature (parameter must extend Event)", 
                    method.getName(), listener.getClass().getName());
                continue;
            }
            
            @SuppressWarnings("unchecked")
            Class<? extends Event> eventType = (Class<? extends Event>) eventClass;
            
            method.setAccessible(true);
            RegisteredListener registeredListener = new RegisteredListener(
                listener, method, handler.priority(), plugin, handler.ignoreCancelled()
            );
            
            handlers.computeIfAbsent(eventType, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(handler.priority(), k -> new CopyOnWriteArrayList<>())
                .add(registeredListener);
            
            registered.add(registeredListener);
            
            logger.debug("Registered event handler: {} for event {} with priority {}", 
                method.getName(), eventType.getSimpleName(), handler.priority());
        }
        
        listenerMap.put(listener, registered);
    }
    
    /**
     * Unregisters all event handlers for a listener.
     * @param listener Listener instance
     */
    public void unregisterEvents(Listener listener) {
        List<RegisteredListener> registered = listenerMap.remove(listener);
        if (registered == null) {
            return;
        }
        
        for (RegisteredListener registeredListener : registered) {
            Class<? extends Event> eventType = registeredListener.getEventType();
            Map<EventPriority, List<RegisteredListener>> priorityMap = handlers.get(eventType);
            if (priorityMap != null) {
                for (List<RegisteredListener> list : priorityMap.values()) {
                    list.remove(registeredListener);
                }
                if (priorityMap.values().stream().allMatch(List::isEmpty)) {
                    handlers.remove(eventType);
                }
            }
        }
    }
    
    /**
     * Unregisters all event handlers for a plugin.
     * @param plugin Plugin instance
     */
    public void unregisterEvents(Plugin plugin) {
        List<Listener> toRemove = new ArrayList<>();
        for (Map.Entry<Listener, List<RegisteredListener>> entry : listenerMap.entrySet()) {
            if (entry.getValue().stream().anyMatch(rl -> rl.getPlugin() == plugin)) {
                toRemove.add(entry.getKey());
            }
        }
        
        for (Listener listener : toRemove) {
            unregisterEvents(listener);
        }
    }
    
    /**
     * Calls an event to all registered listeners.
     * @param event Event to call
     */
    public void callEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        Class<? extends Event> eventClass = event.getClass();
        Map<EventPriority, List<RegisteredListener>> priorityMap = handlers.get(eventClass);
        
        if (priorityMap == null || priorityMap.isEmpty()) {
            return;
        }
        
        // Call handlers in priority order
        for (EventPriority priority : EventPriority.values()) {
            List<RegisteredListener> listeners = priorityMap.get(priority);
            if (listeners == null || listeners.isEmpty()) {
                continue;
            }
            
            for (RegisteredListener listener : listeners) {
                try {
                    // Check if event is cancelled and listener ignores cancelled events
                    if (event instanceof Cancellable) {
                        Cancellable cancellable = (Cancellable) event;
                        if (cancellable.isCancelled() && listener.isIgnoringCancelled()) {
                            continue;
                        }
                    }
                    
                    listener.callEvent(event);
                } catch (Exception e) {
                    logger.error("Error dispatching event {} to listener {} in plugin {}", 
                        eventClass.getSimpleName(), 
                        listener.getListener().getClass().getName(),
                        listener.getPlugin().getName(), e);
                }
            }
        }
    }
    
    /**
     * Represents a registered event listener.
     */
    private static class RegisteredListener {
        private final Listener listener;
        private final Method method;
        private final EventPriority priority;
        private final Plugin plugin;
        private final boolean ignoreCancelled;
        
        public RegisteredListener(Listener listener, Method method, EventPriority priority, 
                                 Plugin plugin, boolean ignoreCancelled) {
            this.listener = listener;
            this.method = method;
            this.priority = priority;
            this.plugin = plugin;
            this.ignoreCancelled = ignoreCancelled;
        }
        
        public void callEvent(Event event) throws Exception {
            method.invoke(listener, event);
        }
        
        public Listener getListener() {
            return listener;
        }
        
        public EventPriority getPriority() {
            return priority;
        }
        
        public Plugin getPlugin() {
            return plugin;
        }
        
        public boolean isIgnoringCancelled() {
            return ignoreCancelled;
        }
        
        @SuppressWarnings("unchecked")
        public Class<? extends Event> getEventType() {
            return (Class<? extends Event>) method.getParameterTypes()[0];
        }
    }
}
