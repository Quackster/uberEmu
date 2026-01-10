package com.uber.server.game;

import com.uber.server.event.EventManager;
import com.uber.server.plugin.PluginManager;
import com.uber.server.repository.*;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main game manager that initializes and manages all game components.
 */
public class Game {
    private static final Logger logger = LoggerFactory.getLogger(Game.class);
    private static final String VERSION = "RELEASE48-25528-25547-201003230310";
    
    private static Game instance;
    
    private GameClientManager clientManager;
    private com.uber.server.game.roles.RoleManager roleManager;
    private com.uber.server.game.support.HelpTool helpTool;
    private com.uber.server.game.catalog.Catalog catalog;
    private com.uber.server.game.navigator.Navigator navigator;
    private com.uber.server.game.items.ItemManager itemManager;
    private com.uber.server.game.rooms.RoomManager roomManager;
    private com.uber.server.game.advertisements.AdvertisementManager advertisementManager;
    private com.uber.server.game.clients.PixelManager pixelManager;
    private com.uber.server.game.achievements.AchievementManager achievementManager;
    private com.uber.server.game.support.ModerationBanManager banManager;
    private com.uber.server.game.support.ModerationTool moderationTool;
    private com.uber.server.game.bots.BotManager botManager;
    private com.uber.server.plugins.PluginHandler pluginHandler;
    private EventManager eventManager;
    private PluginManager pluginManager;
    
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomItemRepository roomItemRepository;
    private final InventoryRepository inventoryRepository;
    private final BadgeRepository badgeRepository;
    private final EffectRepository effectRepository;
    private final CatalogRepository catalogRepository;
    private final MarketplaceRepository marketplaceRepository;
    private final NavigatorRepository navigatorRepository;
    private final HelpRepository helpRepository;
    private final ModerationRepository moderationRepository;
    private final ModerationBanRepository moderationBanRepository;
    private final UserInfoRepository userInfoRepository;
    private final ChatLogRepository chatLogRepository;
    private final PetRepository petRepository;
    private final AchievementRepository achievementRepository;
    private final VoucherRepository voucherRepository;
    private final MessengerRepository messengerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EcotronRepository ecotronRepository;
    private final AdvertisementRepository advertisementRepository;
    private final MoodlightRepository moodlightRepository;
    private final WardrobeRepository wardrobeRepository;
    private final com.uber.server.repository.ItemRepository itemRepository;
    private final RoleRepository roleRepository;
    private final BotRepository botRepository;
    
    private Thread statisticsThread;
    
    /**
     * Private constructor - use getInstance().
     */
    private Game(GameEnvironment environment) {
        this.clientManager = environment.getClientManager();
        
        // Get repositories from environment
        this.userRepository = environment.getUserRepository();
        this.roomRepository = environment.getRoomRepository();
        this.roomItemRepository = environment.getRoomItemRepository();
        this.inventoryRepository = environment.getInventoryRepository();
        this.badgeRepository = environment.getBadgeRepository();
        this.effectRepository = environment.getEffectRepository();
        this.catalogRepository = environment.getCatalogRepository();
        this.marketplaceRepository = environment.getMarketplaceRepository();
        this.navigatorRepository = environment.getNavigatorRepository();
        this.helpRepository = environment.getHelpRepository();
        this.moderationRepository = environment.getModerationRepository();
        this.moderationBanRepository = environment.getModerationBanRepository();
        this.userInfoRepository = environment.getUserInfoRepository();
        this.chatLogRepository = environment.getChatLogRepository();
        this.petRepository = environment.getPetRepository();
        this.achievementRepository = environment.getAchievementRepository();
        this.voucherRepository = environment.getVoucherRepository();
        this.messengerRepository = environment.getMessengerRepository();
        this.subscriptionRepository = environment.getSubscriptionRepository();
        this.ecotronRepository = environment.getEcotronRepository();
        this.advertisementRepository = environment.getAdvertisementRepository();
        this.moodlightRepository = environment.getMoodlightRepository();
        this.wardrobeRepository = environment.getWardrobeRepository();
        this.itemRepository = environment.getItemRepository();
        this.roleRepository = environment.getRoleRepository();
        this.botRepository = environment.getBotRepository();
        
        // Get additional repositories needed by managers
        // (repositories are already set above)
    }
    
    /**
     * Gets the singleton instance.
     * @param environment GameEnvironment instance
     * @return Game instance
     */
    public static synchronized Game getInstance(GameEnvironment environment) {
        if (instance == null) {
            instance = new Game(environment);
        }
        return instance;
    }
    
    /**
     * Gets the singleton instance (must be initialized first).
     * @return Game instance
     */
    public static Game getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Game instance not initialized. Call getInstance(GameEnvironment) first.");
        }
        return instance;
    }
    
    /**
     * Initializes the game and all managers.
     */
    public void initialize() {
        logger.info("Initializing game components...");
        
        // Perform database cleanup
        performDatabaseCleanup(1);
        
        // Initialize managers
        banManager = new com.uber.server.game.support.ModerationBanManager(
            moderationBanRepository, 
            userInfoRepository,
            this);
        banManager.loadBans();
        
        roleManager = new com.uber.server.game.roles.RoleManager(roleRepository);
        roleManager.loadRoles();
        roleManager.loadRights();
        
        // ItemManager must be initialized first as other managers depend on it
        itemManager = new com.uber.server.game.items.ItemManager(itemRepository);
        itemManager.loadItems();
        
        helpTool = new com.uber.server.game.support.HelpTool(helpRepository);
        helpTool.loadCategories();
        helpTool.loadTopics();
        
        catalog = new com.uber.server.game.catalog.Catalog(catalogRepository, ecotronRepository, itemManager,
                inventoryRepository, petRepository, userRepository, marketplaceRepository, this);
        catalog.initialize();
        
        navigator = new com.uber.server.game.navigator.Navigator(navigatorRepository, this);
        navigator.initialize();
        
        roomManager = new com.uber.server.game.rooms.RoomManager(roomRepository, roomItemRepository, this);
        roomManager.loadModels();
        
        advertisementManager = new com.uber.server.game.advertisements.AdvertisementManager(advertisementRepository);
        advertisementManager.loadRoomAdvertisements();
        
        pixelManager = new com.uber.server.game.clients.PixelManager();
        pixelManager.start();
        
        achievementManager = new com.uber.server.game.achievements.AchievementManager(achievementRepository, this);
        achievementManager.loadAchievements();
        
        moderationTool = new com.uber.server.game.support.ModerationTool(
            moderationRepository,
            userInfoRepository,
            chatLogRepository,
            this);
        moderationTool.loadMessagePresets();
        moderationTool.loadPendingTickets();
        
        botManager = new com.uber.server.game.bots.BotManager(botRepository);
        botManager.loadBots();
        
        // Initialize event system
        eventManager = new EventManager();
        logger.info("Event manager initialized");
        
        // Initialize plugin system (uses EventManager)
        pluginManager = new PluginManager(eventManager, this);
        pluginManager.loadPlugins();
        logger.info("Plugin manager initialized");
        
        // Keep old plugin handler for backward compatibility (deprecated)
        pluginHandler = new com.uber.server.plugins.PluginHandler();
        pluginHandler.loadPlugins();
        
        // Start statistics thread (low priority worker)
        // statisticsThread = new Thread(() -> {
        //     // LowPriorityWorker.process();
        // });
        // statisticsThread.setName("Low Priority Worker");
        // statisticsThread.setPriority(Thread.MIN_PRIORITY);
        // statisticsThread.start();
        
        logger.info("Initialized Habbo Hotel, {}.", VERSION);
    }
    
    /**
     * Performs database cleanup on startup/shutdown.
     * @param serverStatus Server status (1 = online, 0 = offline)
     */
    private void performDatabaseCleanup(int serverStatus) {
        logger.debug("Performing database cleanup (status: {})", serverStatus);
        
        // Reset all users' online status and clear auth tickets
        int usersUpdated = userRepository.updateServerStatus(serverStatus == 1 ? 1 : 0);
        logger.debug("Updated {} users' online status", usersUpdated);
        
        // Reset all rooms' user count to 0
        int roomsUpdated = roomRepository.resetAllRoomUserCounts();
        logger.debug("Reset user count for {} rooms", roomsUpdated);
        
        // Update room visit exit timestamps for users still in rooms
        long exitTimestamp = TimeUtil.getUnixTimestamp();
        int visitsUpdated = userRepository.updateRoomVisitExits(exitTimestamp);
        logger.debug("Updated {} room visit exit timestamps", visitsUpdated);
        
        // Note: server_status table update would go here if that table exists
        // For now, we'll skip it as it's not critical for basic functionality
        
        logger.debug("Database cleanup completed (status: {})", serverStatus);
    }
    
    /**
     * Destroys the game and releases all resources.
     */
    public void destroy() {
        logger.info("Destroying game...");
        
        // Stop pixel manager
        if (pixelManager != null) {
            pixelManager.stop();
            pixelManager = null;
        }
        
        // Stop statistics thread
        if (statisticsThread != null && statisticsThread.isAlive()) {
            statisticsThread.interrupt();
            try {
                statisticsThread.join(5000);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for statistics thread to stop");
            }
            statisticsThread = null;
        }
        
        // Disable plugins
        if (pluginManager != null) {
            pluginManager.disablePlugins();
            pluginManager = null;
        }
        
        if (pluginHandler != null) {
            pluginHandler.unloadPlugins();
            pluginHandler = null;
        }
        
        // Perform cleanup
        performDatabaseCleanup(0);
        
        // Clear managers
        if (clientManager != null) {
            clientManager.clear();
            clientManager.stopConnectionChecker();
        }
        
        // Clear other managers as they are added
        
        logger.info("Destroyed Habbo Hotel.");
    }
    
    // Getters
    public GameClientManager getClientManager() {
        return clientManager;
    }
    
    // Manager getters
    public com.uber.server.game.support.ModerationBanManager getBanManager() { 
        return banManager; 
    }
    public com.uber.server.game.roles.RoleManager getRoleManager() { return roleManager; }
    public com.uber.server.game.support.HelpTool getHelpTool() { return helpTool; }
    public com.uber.server.game.catalog.Catalog getCatalog() { return catalog; }
    public com.uber.server.game.navigator.Navigator getNavigator() { return navigator; }
    public com.uber.server.game.items.ItemManager getItemManager() { return itemManager; }
    public com.uber.server.game.rooms.RoomManager getRoomManager() { return roomManager; }
    public com.uber.server.game.advertisements.AdvertisementManager getAdvertisementManager() { return advertisementManager; }
    public com.uber.server.game.clients.PixelManager getPixelManager() { return pixelManager; }
    public com.uber.server.game.achievements.AchievementManager getAchievementManager() { 
        return achievementManager; 
    }
    public com.uber.server.game.support.ModerationTool getModerationTool() { 
        return moderationTool; 
    }
    public com.uber.server.game.bots.BotManager getBotManager() { return botManager; }
    public com.uber.server.plugins.PluginHandler getPluginHandler() { return pluginHandler; }
    public EventManager getEventManager() { return eventManager; }
    public PluginManager getPluginManager() { return pluginManager; }
    
    // Repository getters (for handlers that need direct repository access)
    public UserRepository getUserRepository() { return userRepository; }
    public RoomRepository getRoomRepository() { return roomRepository; }
    public RoomItemRepository getRoomItemRepository() { return roomItemRepository; }
    public InventoryRepository getInventoryRepository() { return inventoryRepository; }
    public BadgeRepository getBadgeRepository() { return badgeRepository; }
    public EffectRepository getEffectRepository() { return effectRepository; }
    public CatalogRepository getCatalogRepository() { return catalogRepository; }
    public MarketplaceRepository getMarketplaceRepository() { return marketplaceRepository; }
    public NavigatorRepository getNavigatorRepository() { return navigatorRepository; }
    public HelpRepository getHelpRepository() { return helpRepository; }
    public ModerationRepository getModerationRepository() { return moderationRepository; }
    public ModerationBanRepository getModerationBanRepository() { return moderationBanRepository; }
    public UserInfoRepository getUserInfoRepository() { return userInfoRepository; }
    public ChatLogRepository getChatLogRepository() { return chatLogRepository; }
    public PetRepository getPetRepository() { return petRepository; }
    public AchievementRepository getAchievementRepository() { return achievementRepository; }
    public VoucherRepository getVoucherRepository() { return voucherRepository; }
    public MessengerRepository getMessengerRepository() { return messengerRepository; }
    public SubscriptionRepository getSubscriptionRepository() { return subscriptionRepository; }
    public EcotronRepository getEcotronRepository() { return ecotronRepository; }
    public AdvertisementRepository getAdvertisementRepository() { return advertisementRepository; }
    public MoodlightRepository getMoodlightRepository() { return moodlightRepository; }
    public WardrobeRepository getWardrobeRepository() { return wardrobeRepository; }
    public com.uber.server.repository.ItemRepository getItemRepository() { return itemRepository; }
    public RoleRepository getRoleRepository() { return roleRepository; }
    
    public static String getVersion() {
        return VERSION;
    }
}
