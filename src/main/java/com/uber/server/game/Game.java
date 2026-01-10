package com.uber.server.game;

import com.uber.server.repository.*;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main game manager that initializes and manages all game components.
 * Ported from HabboHotel/Game.cs
 */
public class Game {
    private static final Logger logger = LoggerFactory.getLogger(Game.class);
    private static final String VERSION = "RELEASE48-25528-25547-201003230310";
    
    private static Game instance;
    
    private GameClientManager clientManager;
    // Managers will be added as they are ported
    // private ModerationBanManager banManager;
    private com.uber.server.roles.RoleManager roleManager;
    private com.uber.server.support.HelpTool helpTool;
    private com.uber.server.catalog.Catalog catalog;
    private com.uber.server.navigator.Navigator navigator;
    private com.uber.server.items.ItemManager itemManager;
    private com.uber.server.rooms.RoomManager roomManager;
    // private AdvertisementManager advertisementManager;
    private com.uber.server.misc.PixelManager pixelManager;
    private com.uber.server.achievements.AchievementManager achievementManager;
    private com.uber.server.support.ModerationBanManager banManager;
    private com.uber.server.support.ModerationTool moderationTool;
    // private BotManager botManager;
    
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
        
        // Initialize managers (will be added as they are ported)
        banManager = new com.uber.server.support.ModerationBanManager(
            moderationBanRepository, 
            userInfoRepository,
            this);
        banManager.loadBans();
        
        roleManager = new com.uber.server.roles.RoleManager(roleRepository);
        roleManager.loadRoles();
        roleManager.loadRights();
        
        // ItemManager must be initialized first as other managers depend on it
        itemManager = new com.uber.server.items.ItemManager(itemRepository);
        itemManager.loadItems();
        
        helpTool = new com.uber.server.support.HelpTool(helpRepository);
        helpTool.loadCategories();
        helpTool.loadTopics();
        
        catalog = new com.uber.server.catalog.Catalog(catalogRepository, ecotronRepository, itemManager,
                inventoryRepository, petRepository, userRepository, marketplaceRepository, this);
        catalog.initialize();
        
        navigator = new com.uber.server.navigator.Navigator(navigatorRepository, this);
        navigator.initialize();
        
        roomManager = new com.uber.server.rooms.RoomManager(roomRepository, roomItemRepository, this);
        roomManager.loadModels();
        
        // advertisementManager = new AdvertisementManager(advertisementRepository);
        // advertisementManager.loadRoomAdvertisements();
        
        pixelManager = new com.uber.server.misc.PixelManager();
        pixelManager.start();
        
        achievementManager = new com.uber.server.achievements.AchievementManager(achievementRepository, this);
        achievementManager.loadAchievements();
        
        moderationTool = new com.uber.server.support.ModerationTool(
            moderationRepository,
            userInfoRepository,
            chatLogRepository,
            this);
        moderationTool.loadMessagePresets();
        moderationTool.loadPendingTickets();
        
        // botManager = new BotManager();
        // botManager.loadBots();
        
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
     * Ported from HabboHotel/Game.cs DatabaseCleanup()
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
    
    // Manager getters will be added as managers are ported
    public com.uber.server.support.ModerationBanManager getBanManager() { 
        return banManager; 
    }
    public com.uber.server.roles.RoleManager getRoleManager() { return roleManager; }
    public com.uber.server.support.HelpTool getHelpTool() { return helpTool; }
    public com.uber.server.catalog.Catalog getCatalog() { return catalog; }
    public com.uber.server.navigator.Navigator getNavigator() { return navigator; }
    public com.uber.server.items.ItemManager getItemManager() { return itemManager; }
    public com.uber.server.rooms.RoomManager getRoomManager() { return roomManager; }
    // public AdvertisementManager getAdvertisementManager() { return advertisementManager; }
    public com.uber.server.misc.PixelManager getPixelManager() { return pixelManager; }
    public com.uber.server.achievements.AchievementManager getAchievementManager() { 
        return achievementManager; 
    }
    public com.uber.server.support.ModerationTool getModerationTool() { 
        return moderationTool; 
    }
    // public BotManager getBotManager() { return botManager; }
    
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
