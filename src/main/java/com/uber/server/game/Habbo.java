package com.uber.server.game;

import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.*;
import com.uber.server.game.users.badges.BadgeComponent;
import com.uber.server.game.users.inventory.AvatarEffectsInventoryComponent;
import com.uber.server.game.users.inventory.InventoryComponent;
import com.uber.server.game.users.messenger.HabboMessenger;
import com.uber.server.game.users.subscriptions.SubscriptionManager;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a logged-in user (Habbo).
 */
public class Habbo {
    private static final Logger logger = LoggerFactory.getLogger(Habbo.class);
    
    private final long id;
    private final String username;
    private final String realName;
    private String authTicket;
    private final long rank;
    private String motto;
    private String look;
    private String gender;
    private int credits;
    private int activityPoints;
    private long lastActivityPointsUpdate;
    private boolean muted;
    private int respect;
    private int dailyRespectPoints;
    private int dailyPetRespectPoints;
    private long homeRoom;
    private int newbieStatus;
    private boolean mutantPenalty;
    private boolean blockNewFriends;
    
    private long currentRoomId;
    private long loadingRoom;
    private boolean loadingChecksPassed;
    
    private final List<Long> favoriteRooms;
    private final List<Long> mutedUsers;
    private final List<String> tags;
    private final ConcurrentHashMap<Long, Integer> achievements;
    private final List<Long> ratedRooms;
    
    // Components
    private SubscriptionManager subscriptionManager;
    private HabboMessenger messenger;
    private BadgeComponent badgeComponent;
    private InventoryComponent inventoryComponent;
    private AvatarEffectsInventoryComponent avatarEffectsInventoryComponent;
    
    private Game game;
    private boolean disconnected;
    private boolean spectatorMode;
    private boolean isTeleporting;
    private long teleporterId;
    private boolean calledGuideBot;
    
    public boolean isCalledGuideBot() { return calledGuideBot; }
    public void setCalledGuideBot(boolean calledGuideBot) { this.calledGuideBot = calledGuideBot; }
    
    /**
     * Called when the user enters a room.
     * @param roomId Room ID
     */
    public void onEnterRoom(long roomId) {
        if (game == null || game.getUserRepository() == null) {
            return;
        }
        
        long timestamp = com.uber.server.util.TimeUtil.getUnixTimestamp();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        
        game.getUserRepository().logRoomVisit(id, roomId, timestamp, hour, minute);
        
        this.currentRoomId = roomId;
        
        if (messenger != null) {
            messenger.onStatusChanged(false);
        }
    }
    
    /**
     * Called when the user leaves a room.
     */
    public void onLeaveRoom() {
        if (currentRoomId <= 0) {
            return;
        }
        
        if (game != null && game.getUserRepository() != null) {
            long timestamp = com.uber.server.util.TimeUtil.getUnixTimestamp();
            game.getUserRepository().updateRoomVisitExit(id, currentRoomId, timestamp);
        }
        
        this.currentRoomId = 0;
        
        if (messenger != null) {
            messenger.onStatusChanged(false);
        }
    }
    
    /**
     * Creates a Habbo instance from user data.
     * @param userData User data map from UserRepository
     * @param game Game instance for accessing repositories and managers
     */
    public Habbo(Map<String, Object> userData, Game game) {
        this.id = ((Number) userData.get("id")).longValue();
        this.username = (String) userData.get("username");
        this.realName = (String) userData.get("real_name");
        this.authTicket = (String) userData.get("auth_ticket");
        this.rank = ((Number) userData.get("rank")).longValue();
        this.motto = (String) userData.get("motto");
        this.look = ((String) userData.get("look")).toLowerCase();
        this.gender = ((String) userData.get("gender")).toLowerCase();
        this.credits = ((Number) userData.get("credits")).intValue();
        this.activityPoints = ((Number) userData.get("activity_points")).intValue();
        this.lastActivityPointsUpdate = ((Number) userData.get("activity_points_lastupdate")).longValue();
        this.muted = ((Number) userData.get("is_muted")).intValue() != 0;
        this.homeRoom = ((Number) userData.get("home_room")).longValue();
        this.respect = ((Number) userData.get("respect")).intValue();
        this.dailyRespectPoints = ((Number) userData.get("daily_respect_points")).intValue();
        this.dailyPetRespectPoints = ((Number) userData.get("daily_pet_respect_points")).intValue();
        this.newbieStatus = ((Number) userData.get("newbie_status")).intValue();
        this.mutantPenalty = ((Number) userData.get("mutant_penalty")).intValue() != 0;
        this.blockNewFriends = "1".equals(userData.get("block_newfriends"));
        
        this.currentRoomId = 0;
        this.loadingRoom = 0;
        this.loadingChecksPassed = false;
        
        this.favoriteRooms = new ArrayList<>();
        this.mutedUsers = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.achievements = new ConcurrentHashMap<>();
        this.ratedRooms = new ArrayList<>();
        
        this.game = game;
        this.disconnected = false;
        this.spectatorMode = false;
        this.isTeleporting = false;
        this.teleporterId = 0;
        this.calledGuideBot = false;
        
        // Initialize components
        if (game != null) {
            this.subscriptionManager = new SubscriptionManager(id, game.getSubscriptionRepository());
            this.badgeComponent = new BadgeComponent(id, game.getBadgeRepository());
            this.inventoryComponent = new InventoryComponent(id, game, game.getInventoryRepository(), game.getPetRepository());
            this.avatarEffectsInventoryComponent = new AvatarEffectsInventoryComponent(id, game, game.getEffectRepository());
            this.messenger = new HabboMessenger(id, game, game.getMessengerRepository(), game.getUserRepository());
        }
        
        logger.debug("{} has logged in.", username);
    }
    
    /**
     * Loads user data from repositories.
     */
    public void loadData() {
        if (game == null) {
            logger.warn("Cannot load data for {}: Game instance is null", username);
            return;
        }
        
        // Load subscriptions
        if (subscriptionManager != null) {
            subscriptionManager.loadSubscriptions();
        }
        
        // Load badges
        if (badgeComponent != null) {
            badgeComponent.loadBadges();
        }
        
        // Load inventory
        if (inventoryComponent != null) {
            inventoryComponent.loadInventory();
        }
        
        // Load effects
        if (avatarEffectsInventoryComponent != null) {
            avatarEffectsInventoryComponent.loadEffects();
        }
        
        // Load messenger
        if (messenger != null) {
            messenger.loadBuddies();
            messenger.loadRequests();
        }
        
        // Load achievements
        Map<Long, Integer> userAchievements = game.getAchievementRepository().loadUserAchievements(id);
        achievements.putAll(userAchievements);
        
        // Load favorites, muted users, tags
        loadFavorites();
        loadMutedUsers();
        loadTags();
    }
    
    // Getters and setters
    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getRealName() { return realName; }
    public String getAuthTicket() { return authTicket; }
    public void setAuthTicket(String authTicket) { this.authTicket = authTicket; }
    public long getRank() { return rank; }
    public String getMotto() { return motto; }
    public void setMotto(String motto) { this.motto = motto; }
    public String getLook() { return look; }
    public void setLook(String look) { this.look = look != null ? look.toLowerCase() : ""; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender != null ? gender.toLowerCase() : ""; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public int getActivityPoints() { return activityPoints; }
    public void setActivityPoints(int activityPoints) { this.activityPoints = activityPoints; }
    public long getLastActivityPointsUpdate() { return lastActivityPointsUpdate; }
    public void setLastActivityPointsUpdate(long lastActivityPointsUpdate) { this.lastActivityPointsUpdate = lastActivityPointsUpdate; }
    public long getHomeRoom() { return homeRoom; }
    public void setHomeRoom(long homeRoom) { this.homeRoom = homeRoom; }
    public long getCurrentRoomId() { return currentRoomId; }
    public void setCurrentRoomId(long currentRoomId) { this.currentRoomId = currentRoomId; }
    public long getLoadingRoom() { return loadingRoom; }
    public void setLoadingRoom(long loadingRoom) { this.loadingRoom = loadingRoom; }
    public boolean isLoadingChecksPassed() { return loadingChecksPassed; }
    public void setLoadingChecksPassed(boolean loadingChecksPassed) { this.loadingChecksPassed = loadingChecksPassed; }
    public boolean isMuted() { return muted; }
    public void setMuted(boolean muted) { this.muted = muted; }
    /**
     * Mutes the user.
     */
    public void mute() {
        this.muted = true;
        if (game != null && game.getUserRepository() != null) {
            game.getUserRepository().updateMutedStatus(id, true);
        }
    }
    
    /**
     * Unmutes the user.
     */
    public void unmute() {
        this.muted = false;
        if (game != null && game.getUserRepository() != null) {
            game.getUserRepository().updateMutedStatus(id, false);
        }
    }
    public int getRespect() { return respect; }
    public void setRespect(int respect) { this.respect = respect; }
    public int getDailyRespectPoints() { return dailyRespectPoints; }
    public void setDailyRespectPoints(int dailyRespectPoints) { this.dailyRespectPoints = dailyRespectPoints; }
    public int getDailyPetRespectPoints() { return dailyPetRespectPoints; }
    public void setDailyPetRespectPoints(int dailyPetRespectPoints) { this.dailyPetRespectPoints = dailyPetRespectPoints; }
    public int getNewbieStatus() { return newbieStatus; }
    public void setNewbieStatus(int newbieStatus) { this.newbieStatus = newbieStatus; }
    public boolean isMutantPenalty() { return mutantPenalty; }
    public boolean isBlockNewFriends() { return blockNewFriends; }
    public boolean isInRoom() { return currentRoomId > 0; }
    public boolean isDisconnected() { return disconnected; }
    public boolean isSpectatorMode() { return spectatorMode; }
    public void setSpectatorMode(boolean spectatorMode) { this.spectatorMode = spectatorMode; }
    public boolean isTeleporting() { return isTeleporting; }
    public void setTeleporting(boolean teleporting) { this.isTeleporting = teleporting; }
    public long getTeleporterId() { return teleporterId; }
    public void setTeleporterId(long teleporterId) { this.teleporterId = teleporterId; }
    public List<Long> getFavoriteRooms() { return new ArrayList<>(favoriteRooms); }
    public List<Long> getMutedUsers() { return new ArrayList<>(mutedUsers); }
    public List<String> getTags() { return new ArrayList<>(tags); }
    public Map<Long, Integer> getAchievements() { return new ConcurrentHashMap<>(achievements); }
    public List<Long> getRatedRooms() { return new ArrayList<>(ratedRooms); }
    public void addRatedRoom(long roomId) {
        if (!ratedRooms.contains(roomId)) {
            ratedRooms.add(roomId);
        }
    }
    
    // Component getters
    public SubscriptionManager getSubscriptionManager() { return subscriptionManager; }
    public HabboMessenger getMessenger() { return messenger; }
    public BadgeComponent getBadgeComponent() { return badgeComponent; }
    public InventoryComponent getInventoryComponent() { return inventoryComponent; }
    public AvatarEffectsInventoryComponent getAvatarEffectsInventoryComponent() { return avatarEffectsInventoryComponent; }
    
    /**
     * Loads favorite rooms from the database.
     */
    public void loadFavorites() {
        favoriteRooms.clear();
        
        if (game == null || game.getUserRepository() == null) {
            return;
        }
        
        List<Long> favorites = game.getUserRepository().loadFavorites(id);
        favoriteRooms.addAll(favorites);
    }
    
    /**
     * Adds a favorite room.
     * @param roomId Room ID to add
     */
    public void addFavoriteRoom(long roomId) {
        if (!favoriteRooms.contains(roomId)) {
            favoriteRooms.add(roomId);
        }
    }
    
    /**
     * Removes a favorite room.
     * @param roomId Room ID to remove
     */
    public void removeFavoriteRoom(long roomId) {
        favoriteRooms.remove(roomId);
    }
    
    /**
     * Loads muted users from the database.
     */
    public void loadMutedUsers() {
        mutedUsers.clear();
        
        if (game == null || game.getUserRepository() == null) {
            return;
        }
        
        List<Long> muted = game.getUserRepository().loadMutedUsers(id);
        mutedUsers.addAll(muted);
    }
    
    /**
     * Loads user tags from the database.
     */
    public void loadTags() {
        tags.clear();
        
        if (game == null || game.getUserRepository() == null) {
            return;
        }
        
        List<String> userTags = game.getUserRepository().loadTags(id);
        tags.addAll(userTags);
        
        // Check if user has 5 or more tags - unlock achievement
        if (tags.size() >= 5 && game.getAchievementManager() != null) {
            GameClient client = getClient();
            if (client != null) {
                game.getAchievementManager().unlockAchievement(client, 7, 1);
            }
        }
    }
    
    /**
     * Checks if the user has a specific permission (fuse).
     * @param fuse Fuse/permission name to check
     * @return True if user has the permission, false otherwise
     */
    public boolean hasFuse(String fuse) {
        if (game == null || fuse == null) {
            return false;
        }
        
        if (game.getRoleManager() != null) {
            if (game.getRoleManager().rankHasRight(rank, fuse)) {
                return true;
            }
            
            if (subscriptionManager != null) {
                for (String subscriptionId : subscriptionManager.getSubList()) {
                    if (game.getRoleManager().subHasRight(subscriptionId, fuse)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Handles user disconnection.
     */
    public void onDisconnect() {
        if (disconnected) {
            return;
        }
        
        logger.debug("{} has logged out.", username);
        this.disconnected = true;
        
        // Update last online timestamp and set offline
        if (game != null && game.getUserRepository() != null) {
            game.getUserRepository().updateLastOnline(id, 0);
        }
        
        // Remove user from room if in room
        if (isInRoom() && game != null && game.getRoomManager() != null) {
            com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(currentRoomId);
            if (room != null) {
                GameClient client = getClient();
                if (client != null) {
                    room.removeUserFromRoom(client, false, false);
                }
            }
        }
        
        // Update messenger status
        if (messenger != null) {
            messenger.setAppearOffline(true);
            messenger.onStatusChanged(true);
            messenger = null;
        }
        
        // Clear subscriptions
        if (subscriptionManager != null) {
            subscriptionManager.clear();
            subscriptionManager = null;
        }
        
        // TODO: Drop events, kick bots, etc.
    }
    
    /**
     * Updates credits balance from database and sends update to client.
     * @param userRepository UserRepository instance
     * @param inDatabase If true, updates database with current credits
     */
    public void updateCreditsBalance(UserRepository userRepository, boolean inDatabase) {
        // Send credits update message to client
        GameClient client = getClient();
        if (client != null) {
            var composer = new com.uber.server.messages.outgoing.users.CreditBalanceComposer(credits);
            client.sendMessage(composer.compose());
        }
        
        if (inDatabase && userRepository != null) {
            userRepository.updateCredits(id, credits);
        } else if (!inDatabase && userRepository != null) {
            // Reload credits from database
            int dbCredits = userRepository.getCredits(id);
            if (dbCredits != credits) {
                credits = dbCredits;
                // Send updated credits to client
                if (client != null) {
                    var composer = new com.uber.server.messages.outgoing.users.CreditBalanceComposer(credits);
                    client.sendMessage(composer.compose());
                }
            }
        }
    }
    
    /**
     * Updates credits balance (calls updateCreditsBalance with inDatabase=false).
     * @param userRepository UserRepository instance
     */
    public void updateCreditsBalance(UserRepository userRepository) {
        updateCreditsBalance(userRepository, false);
    }
    
    /**
     * Updates credits and saves to database.
     * @param userRepository UserRepository instance
     * @param newCredits New credits amount
     */
    public void setCreditsAndUpdate(UserRepository userRepository, int newCredits) {
        this.credits = newCredits;
        if (userRepository != null) {
            userRepository.updateCredits(id, newCredits);
        }
    }
    
    /**
     * Updates activity points balance.
     * @param sendUpdate If true, sends update to client
     */
    public void updateActivityPointsBalance(boolean sendUpdate) {
        updateActivityPointsBalance(sendUpdate, 0);
    }
    
    /**
     * Updates activity points balance.
     * @param sendUpdate If true, sends update to client
     * @param notifAmount Notification amount
     */
    public void updateActivityPointsBalance(boolean sendUpdate, int notifAmount) {
        if (sendUpdate) {
            GameClient client = getClient();
            if (client != null) {
                var composer = new com.uber.server.messages.outgoing.users.HabboActivityPointNotificationComposer(activityPoints, notifAmount);
                client.sendMessage(composer.compose());
            }
        }
        
        // Update in database
        if (game != null && game.getUserRepository() != null) {
            long now = TimeUtil.getUnixTimestamp();
            game.getUserRepository().updateActivityPoints(id, activityPoints, now);
            this.lastActivityPointsUpdate = now;
        }
    }
    
    /**
     * Initializes messenger (creates if needed, loads data, and sends to client).
     */
    public void initMessenger() {
        // Create messenger if it doesn't exist
        if (messenger == null) {
            messenger = new HabboMessenger(id, game, game.getMessengerRepository(), game.getUserRepository());
        }
        
        // Load buddies and requests (safe to call multiple times as they clear first)
        messenger.loadBuddies();
        messenger.loadRequests();
        
        // Always send messenger data to client, even if messenger already existed
        GameClient client = getClient();
        if (client != null) {
            client.sendMessage(messenger.serializeFriends());
            client.sendMessage(messenger.serializeRequests());
            
            messenger.onStatusChanged(true);
        }
    }
    
    /**
     * Gets the GameClient for this user.
     * @return GameClient object, or null if user is not online
     */
    public GameClient getClient() {
        if (game == null || game.getClientManager() == null) {
            return null;
        }
        return game.getClientManager().getClientByHabbo(id);
    }
}
