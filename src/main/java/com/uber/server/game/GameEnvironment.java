package com.uber.server.game;

import com.uber.server.config.Configuration;
import com.uber.server.config.ConfigLoader;
import com.uber.server.messages.PacketHandlerRegistry;
import com.uber.server.net.TcpConnectionManager;
import com.uber.server.repository.*;
import com.uber.server.storage.DatabasePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Game environment that initializes and manages all server components.
 */
public class GameEnvironment {
    private static final Logger logger = LoggerFactory.getLogger(GameEnvironment.class);
    private static final String PRETTY_VERSION = "uberEmu v1.0.0.0-dev (Build 1000)";
    
    private static GameEnvironment instance;
    
    private Configuration configuration;
    private DatabasePool databasePool;
    private PacketHandlerRegistry handlerRegistry;
    private TcpConnectionManager connectionManager;
    private GameClientManager clientManager;
    private Game game;
    
    // Repositories
    private UserRepository userRepository;
    private RoomRepository roomRepository;
    private RoomItemRepository roomItemRepository;
    private InventoryRepository inventoryRepository;
    private BadgeRepository badgeRepository;
    private EffectRepository effectRepository;
    private CatalogRepository catalogRepository;
    private MarketplaceRepository marketplaceRepository;
    private NavigatorRepository navigatorRepository;
    private HelpRepository helpRepository;
    private ModerationRepository moderationRepository;
    private ModerationBanRepository moderationBanRepository;
    private UserInfoRepository userInfoRepository;
    private ChatLogRepository chatLogRepository;
    private PetRepository petRepository;
    private AchievementRepository achievementRepository;
    private VoucherRepository voucherRepository;
    private MessengerRepository messengerRepository;
    private SubscriptionRepository subscriptionRepository;
    private EcotronRepository ecotronRepository;
    private AdvertisementRepository advertisementRepository;
    private MoodlightRepository moodlightRepository;
    private WardrobeRepository wardrobeRepository;
    private ItemRepository itemRepository;
    private RoleRepository roleRepository;
    private BotRepository botRepository;
    
    private GameEnvironment() {
        // Singleton pattern
    }
    
    /**
     * Gets the singleton instance.
     * @return GameEnvironment instance
     */
    public static synchronized GameEnvironment getInstance() {
        if (instance == null) {
            instance = new GameEnvironment();
        }
        return instance;
    }
    
    /**
     * Initializes the game environment.
     * Loads configuration, database pool, networking, and game components.
     */
    public void initialize() throws Exception {
        logger.info("");
        logger.info("              |              ,---.          |         |");
        logger.info("         .   .|---.,---.,---.|--- ,-.-..   .|    ,---.|--- ,---.,---.");
        logger.info("         |   ||   ||---'|    |    | | ||   ||    ,---||    |   ||    ");
        logger.info("         `---'`---'`---'`    `---'` ' '`---'`---'`---^`---'`---'`    ");
        logger.info("");
        logger.info("                                   {}", PRETTY_VERSION);
        logger.info("");
        logger.info("       ------------------------------------------------------------------");
        logger.info("");
        
        logger.info("Initializing uberEmulator...");
        
        try {
            // Load configuration
            configuration = ConfigLoader.load();
            logger.info("Configuration loaded");
            
            // Initialize database pool
            try {
                databasePool = new DatabasePool(configuration);
            } catch (SQLException e) {
                throw new Exception("Failed to initialize database pool: " + e.getMessage(), e);
            }
            
            // Initialize repositories
            initializeRepositories();
            logger.info("Repositories initialized");
            
            // Initialize packet handler registry
            handlerRegistry = new PacketHandlerRegistry();
            logger.info("Packet handler registry initialized");
            
            // Initialize TCP connection manager
            String bindIP = configuration.get("game.tcp.bindip");
            int port = configuration.getInt("game.tcp.port");
            int conLimit = configuration.getInt("game.tcp.conlimit");
            
            connectionManager = new TcpConnectionManager(bindIP, port, conLimit);
            logger.info("TCP connection manager initialized");
            
            // Initialize game client manager
            clientManager = new GameClientManager(handlerRegistry, connectionManager);
            connectionManager.setGameClientManager(clientManager); // Set reference for connection handling
            logger.info("Game client manager initialized");
            
            // Start TCP listener
            connectionManager.getListener().start();
            
            // Start connection checker if enabled
            if (configuration.getBoolean("client.ping.enabled")) {
                int pingInterval = configuration.getInt("client.ping.interval");
                clientManager.startConnectionChecker(pingInterval);
            }
            
            // Initialize game instance
            game = Game.getInstance(this);
            game.initialize();
            logger.info("Game instance initialized");
            
            // Register packet handlers
            HandlerInitializer handlerInitializer = new HandlerInitializer(this, game, handlerRegistry);
            handlerInitializer.registerAllHandlers();
            logger.info("Packet handlers registered");
            
            logger.info("The environment has initialized successfully. Ready for connections.");
            
        } catch (ConfigLoader.ConfigLoadException e) {
            logger.error("Configuration error: {}", e.getMessage());
            throw new Exception("Failed to load configuration: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to initialize uberEmulator: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Destroys the game environment and releases all resources.
     */
    public void destroy() {
        logger.info("Destroying uberEmu environment...");
        
        if (game != null) {
            logger.info("Destroying game...");
            game.destroy();
        }
        
        if (clientManager != null) {
            logger.info("Stopping client manager...");
            clientManager.stopConnectionChecker();
            clientManager.clear();
        }
        
        if (connectionManager != null) {
            logger.info("Destroying connection manager...");
            connectionManager.getListener().stop();
            connectionManager.destroyManager();
        }
        
        // Shutdown shared thread pool
        try {
            com.uber.server.game.threading.GameThreadPool.getInstance().shutdown();
        } catch (Exception e) {
            logger.warn("Error shutting down GameThreadPool: {}", e.getMessage());
        }
        
        if (databasePool != null) {
            logger.info("Closing database pool...");
            databasePool.close();
        }
        
        logger.info("Uninitialized successfully. Closing.");
    }
    
    // Getters
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public DatabasePool getDatabasePool() {
        return databasePool;
    }
    
    public PacketHandlerRegistry getHandlerRegistry() {
        return handlerRegistry;
    }
    
    public TcpConnectionManager getConnectionManager() {
        return connectionManager;
    }
    
    public GameClientManager getClientManager() {
        return clientManager;
    }
    
    public Game getGame() {
        return game;
    }
    
    public static String getPrettyVersion() {
        return PRETTY_VERSION;
    }
    
    /**
     * Initializes all repository instances.
     */
    private void initializeRepositories() {
        userRepository = new UserRepository(databasePool);
        roomRepository = new RoomRepository(databasePool);
        roomItemRepository = new RoomItemRepository(databasePool);
        inventoryRepository = new InventoryRepository(databasePool);
        badgeRepository = new BadgeRepository(databasePool);
        effectRepository = new EffectRepository(databasePool);
        catalogRepository = new CatalogRepository(databasePool);
        marketplaceRepository = new MarketplaceRepository(databasePool);
        navigatorRepository = new NavigatorRepository(databasePool);
        helpRepository = new HelpRepository(databasePool);
        moderationRepository = new ModerationRepository(databasePool);
        moderationBanRepository = new ModerationBanRepository(databasePool);
        userInfoRepository = new UserInfoRepository(databasePool);
        chatLogRepository = new ChatLogRepository(databasePool);
        petRepository = new PetRepository(databasePool);
        achievementRepository = new AchievementRepository(databasePool);
        voucherRepository = new VoucherRepository(databasePool);
        messengerRepository = new MessengerRepository(databasePool);
        subscriptionRepository = new SubscriptionRepository(databasePool);
        ecotronRepository = new EcotronRepository(databasePool);
        advertisementRepository = new AdvertisementRepository(databasePool);
        moodlightRepository = new MoodlightRepository(databasePool);
        wardrobeRepository = new WardrobeRepository(databasePool);
        itemRepository = new com.uber.server.repository.ItemRepository(databasePool);
        roleRepository = new RoleRepository(databasePool);
        botRepository = new BotRepository(databasePool);
    }
    
    // Repository getters
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
    public BotRepository getBotRepository() { return botRepository; }
}
