package com.uber.server.game.users.inventory;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.EffectRepository;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages avatar effects for a user.
 */
public class AvatarEffectsInventoryComponent {
    private static final Logger logger = LoggerFactory.getLogger(AvatarEffectsInventoryComponent.class);
    
    private final long userId;
    private final Game game;
    private final EffectRepository effectRepository;
    private final CopyOnWriteArrayList<AvatarEffect> effects;
    private int currentEffect;
    
    public AvatarEffectsInventoryComponent(long userId, Game game, EffectRepository effectRepository) {
        this.userId = userId;
        this.game = game;
        this.effectRepository = effectRepository;
        this.effects = new CopyOnWriteArrayList<>();
        this.currentEffect = -1;
    }
    
    /**
     * Gets the number of effects.
     * @return Effect count
     */
    public int getCount() {
        return effects.size();
    }
    
    /**
     * Gets the current active effect ID.
     * @return Current effect ID, or -1 if none
     */
    public int getCurrentEffect() {
        return currentEffect;
    }
    
    /**
     * Sets the current active effect ID.
     * @param effectId Effect ID, or -1 for none
     */
    public void setCurrentEffect(int effectId) {
        this.currentEffect = effectId;
    }
    
    /**
     * Loads effects from the database.
     */
    public void loadEffects() {
        effects.clear();
        
        List<Map<String, Object>> effectData = effectRepository.loadEffects(userId);
        for (Map<String, Object> row : effectData) {
            try {
                int effectId = ((Number) row.get("effect_id")).intValue();
                int totalDuration = ((Number) row.get("total_duration")).intValue();
                boolean activated = "1".equals(row.get("is_activated"));
                long activatedStamp = ((Number) row.get("activated_stamp")).longValue();
                
                AvatarEffect effect = new AvatarEffect(effectId, totalDuration, activated, activatedStamp);
                
                // Remove expired effects
                if (effect.hasExpired()) {
                    effectRepository.deleteEffect(userId, effectId);
                    continue;
                }
                
                effects.add(effect);
            } catch (Exception e) {
                logger.error("Failed to load effect: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Adds a new effect to the user.
     * @param effectId Effect ID
     * @param duration Duration in seconds
     */
    public void addEffect(int effectId, int duration) {
        if (!effectRepository.addEffect(userId, effectId, duration)) {
            logger.error("Failed to add effect {} to user {} in database", effectId, userId);
            return;
        }
        
        AvatarEffect effect = new AvatarEffect(effectId, duration, false, 0);
        effects.add(effect);
        
        // Notify client
        GameClient client = getClient();
        if (client != null) {
            ServerMessage response = new ServerMessage(461);
            response.appendInt32(effectId);
            response.appendInt32(duration);
            client.sendMessage(response);
        }
    }
    
    /**
     * Stops an activated effect.
     * @param effectId Effect ID
     */
    public void stopEffect(int effectId) {
        AvatarEffect effect = getEffect(effectId, true);
        
        if (effect == null || !effect.hasExpired()) {
            return;
        }
        
        if (!effectRepository.deleteActivatedEffect(userId, effectId)) {
            logger.error("Failed to delete activated effect {} from user {} in database", effectId, userId);
        }
        
        effects.remove(effect);
        
        // Notify client
        GameClient client = getClient();
        if (client != null) {
            ServerMessage response = new ServerMessage(463);
            response.appendInt32(effectId);
            client.sendMessage(response);
        }
        
        // If this was the current effect, clear it
        if (currentEffect == effectId) {
            applyEffect(-1);
        }
    }
    
    /**
     * Applies an effect in a room.
     * @param effectId Effect ID to apply, or -1 to clear
     */
    public void applyEffect(int effectId) {
        if (!hasEffect(effectId, true)) {
            return;
        }
        
        // Update current effect
        currentEffect = effectId;
        
        // Apply effect to room
        if (game != null && game.getRoomManager() != null) {
            GameClient client = game.getClientManager().getClientByHabbo(userId);
            if (client != null && client.getHabbo() != null && client.getHabbo().isInRoom()) {
                com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(client.getHabbo().getCurrentRoomId());
                if (room != null) {
                    com.uber.server.game.rooms.RoomUser user = room.getRoomUserByHabbo(userId);
                    if (user != null) {
                        com.uber.server.messages.ServerMessage message = new com.uber.server.messages.ServerMessage(485);
                        message.appendInt32(user.getVirtualId());
                        message.appendInt32(effectId);
                        room.sendMessage(message);
                    }
                }
            }
        }
    }
    
    /**
     * Enables (activates) an effect.
     * @param effectId Effect ID
     */
    public void enableEffect(int effectId) {
        AvatarEffect effect = getEffect(effectId, false);
        
        if (effect == null || effect.hasExpired() || effect.isActivated()) {
            return;
        }
        
        long timestamp = TimeUtil.getUnixTimestamp();
        if (!effectRepository.enableEffect(userId, effectId, timestamp)) {
            logger.error("Failed to enable effect {} for user {} in database", effectId, userId);
            return;
        }
        
        effect.activate();
        
        // Notify client
        GameClient client = getClient();
        if (client != null) {
            ServerMessage response = new ServerMessage(462);
            response.appendInt32(effect.getEffectId());
            response.appendInt32(effect.getTotalDuration());
            client.sendMessage(response);
        }
    }
    
    /**
     * Checks if the user has an effect.
     * @param effectId Effect ID, or -1 to check if any effect exists
     * @param ifEnabledOnly If true, only checks activated effects
     * @return True if user has the effect, false otherwise
     */
    public boolean hasEffect(int effectId, boolean ifEnabledOnly) {
        if (effectId == -1) {
            return true;
        }
        
        for (AvatarEffect effect : effects) {
            if (ifEnabledOnly && !effect.isActivated()) {
                continue;
            }
            
            if (effect.hasExpired()) {
                continue;
            }
            
            if (effect.getEffectId() == effectId) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets an effect by ID.
     * @param effectId Effect ID
     * @param ifEnabledOnly If true, only returns activated effects
     * @return AvatarEffect object, or null if not found
     */
    public AvatarEffect getEffect(int effectId, boolean ifEnabledOnly) {
        for (AvatarEffect effect : effects) {
            if (ifEnabledOnly && !effect.isActivated()) {
                continue;
            }
            
            if (effect.getEffectId() == effectId) {
                return effect;
            }
        }
        
        return null;
    }
    
    /**
     * Checks for and removes expired effects.
     */
    public void checkExpired() {
        List<Integer> toRemove = new ArrayList<>();
        
        for (AvatarEffect effect : effects) {
            if (effect.hasExpired()) {
                toRemove.add(effect.getEffectId());
            }
        }
        
        for (Integer effectId : toRemove) {
            stopEffect(effectId);
        }
    }
    
    /**
     * Serializes effects to a ServerMessage.
     * @return ServerMessage with effect data (ID 460)
     */
    public ServerMessage serialize() {
        ServerMessage message = new ServerMessage(460);
        message.appendInt32(getCount());
        
        for (AvatarEffect effect : effects) {
            message.appendInt32(effect.getEffectId());
            message.appendInt32(effect.getTotalDuration());
            message.appendBoolean(!effect.isActivated());
            message.appendInt32(effect.getTimeLeft());
        }
        
        return message;
    }
    
    /**
     * Gets the GameClient for this user.
     * @return GameClient object, or null if user is not online
     */
    private GameClient getClient() {
        if (game == null || game.getClientManager() == null) {
            return null;
        }
        return game.getClientManager().getClientByHabbo(userId);
    }
}
