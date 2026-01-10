package com.uber.server.game.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.Item;
import com.uber.server.game.items.ItemManager;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.*;
import com.uber.server.util.StringUtil;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the catalog system.
 */
public class Catalog {
    private static final Logger logger = LoggerFactory.getLogger(Catalog.class);

    private final ConcurrentHashMap<Integer, CatalogPage> pages;
    private final List<EcotronReward> ecotronRewards;
    private final CatalogRepository catalogRepository;
    private final EcotronRepository ecotronRepository;
    private final ItemManager itemManager;
    private final InventoryRepository inventoryRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final MarketplaceRepository marketplaceRepository;
    private final Game game;
    private final Marketplace marketplace;
    private final Random random;

    public Catalog(CatalogRepository catalogRepository, EcotronRepository ecotronRepository, ItemManager itemManager,
                   InventoryRepository inventoryRepository, PetRepository petRepository, UserRepository userRepository,
                   MarketplaceRepository marketplaceRepository, Game game) {
        this.pages = new ConcurrentHashMap<>();
        this.ecotronRewards = new ArrayList<>();
        this.catalogRepository = catalogRepository;
        this.ecotronRepository = ecotronRepository;
        this.itemManager = itemManager;
        this.inventoryRepository = inventoryRepository;
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        this.marketplaceRepository = marketplaceRepository;
        this.game = game;
        this.marketplace = new Marketplace(marketplaceRepository, game, this);
        this.random = new Random();
    }

    // Legacy constructor for backward compatibility
    public Catalog(CatalogRepository catalogRepository, EcotronRepository ecotronRepository, ItemManager itemManager) {
        this(catalogRepository, ecotronRepository, itemManager, null, null, null, null, null);
    }

    /**
     * Initializes the catalog by loading pages and ecotron rewards.
     */
    public void initialize() {
        pages.clear();
        ecotronRewards.clear();

        // Load catalog pages
        List<Map<String, Object>> pageData = catalogRepository.loadCatalogPages();
        for (Map<String, Object> row : pageData) {
            try {
                int id = ((Number) row.get("id")).intValue();
                int parentId = ((Number) row.get("parent_id")).intValue();
                String caption = (String) row.get("caption");
                boolean visible = parseBoolean(row.get("visible"));
                boolean enabled = parseBoolean(row.get("enabled"));
                boolean comingSoon = parseBoolean(row.get("coming_soon"));
                long minRank = ((Number) row.get("min_rank")).longValue();
                boolean clubOnly = parseBoolean(row.get("club_only"));
                int iconColor = ((Number) row.get("icon_color")).intValue();
                int iconImage = ((Number) row.get("icon_image")).intValue();
                String layout = (String) row.get("page_layout");
                String layoutHeadline = (String) row.get("page_headline");
                String layoutTeaser = (String) row.get("page_teaser");
                String layoutSpecial = (String) row.get("page_special");
                String text1 = (String) row.get("page_text1");
                String text2 = (String) row.get("page_text2");
                String textDetails = (String) row.get("page_text_details");
                String textTeaser = (String) row.get("page_text_teaser");

                CatalogPage page = new CatalogPage(id, parentId, caption, visible, enabled, comingSoon,
                        minRank, clubOnly, iconColor, iconImage, layout, layoutHeadline, layoutTeaser,
                        layoutSpecial, text1, text2, textDetails, textTeaser, catalogRepository, itemManager);

                pages.put(id, page);
            } catch (Exception e) {
                logger.error("Failed to create catalog page: {}", e.getMessage(), e);
            }
        }

        // Load ecotron rewards
        List<Map<String, Object>> ecoData = ecotronRepository.loadEcotronRewards();
        for (Map<String, Object> row : ecoData) {
            try {
                EcotronReward reward = new EcotronReward(
                        ((Number) row.get("id")).longValue(),
                        ((Number) row.get("display_id")).longValue(),
                        ((Number) row.get("item_id")).longValue(),
                        ((Number) row.get("reward_level")).longValue()
                );
                ecotronRewards.add(reward);
            } catch (Exception e) {
                logger.error("Failed to create ecotron reward: {}", e.getMessage(), e);
            }
        }

        logger.info("Initialized catalog with {} pages and {} ecotron rewards", pages.size(), ecotronRewards.size());
    }

    /**
     * Parses a boolean value from database.
     */
    private boolean parseBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        String str = value.toString().trim();
        return "1".equals(str) || "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str);
    }

    /**
     * Finds a catalog item by ID.
     * @param itemId Catalog item ID
     * @return CatalogItem, or null if not found
     */
    public CatalogItem findItem(long itemId) {
        for (CatalogPage page : pages.values()) {
            CatalogItem item = page.getItem(itemId);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /**
     * Checks if a base item is in the catalog.
     * @param baseId Base item ID
     * @return True if item is in catalog
     */
    public boolean isItemInCatalog(long baseId) {
        // TODO: Add method to CatalogRepository to check if item is in catalog
        // For now, search through pages
        for (CatalogPage page : pages.values()) {
            for (CatalogItem item : page.getItems()) {
                if (item.getItemIds().contains(baseId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the tree size (number of child pages) for a page.
     * @param client GameClient for rank filtering
     * @param treeId Parent page ID
     * @return Number of child pages
     */
    public int getTreeSize(GameClient client, int treeId) {
        Habbo habbo = client != null ? client.getHabbo() : null;
        if (habbo == null) {
            return 0;
        }
        
        int count = 0;
        for (CatalogPage page : pages.values()) {
            if (page.getParentId() == treeId && page.getMinRank() <= habbo.getRank()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Gets the tree size (number of child pages) for a page (legacy method for backward compatibility).
     * @param treeId Parent page ID
     * @param userRank User rank for filtering
     * @return Number of child pages
     */
    public int getTreeSize(int treeId, long userRank) {
        int count = 0;
        for (CatalogPage page : pages.values()) {
            if (page.getParentId() == treeId && page.getMinRank() <= userRank) {
                count++;
            }
        }
        return count;
    }

    // Getters
    public CatalogPage getPage(int pageId) {
        return pages.get(pageId);
    }

    public Map<Integer, CatalogPage> getPages() {
        return new HashMap<>(pages);
    }

    public List<EcotronReward> getEcotronRewards() {
        return new ArrayList<>(ecotronRewards);
    }

    /**
     * Gets ecotron rewards for a specific level.
     * @param level Reward level
     * @return List of rewards for that level
     */
    public List<EcotronReward> getEcotronRewardsForLevel(long level) {
        List<EcotronReward> rewards = new ArrayList<>();
        for (EcotronReward reward : ecotronRewards) {
            if (reward.getRewardLevel() == level) {
                rewards.add(reward);
            }
        }
        return rewards;
    }

    /**
     * Gets a random ecotron reward based on probability.
     * @return Random EcotronReward
     */
    public EcotronReward getRandomEcotronReward() {
        long level = 1;

        // Probability-based level selection
        int rand = random.nextInt(2000) + 1;
        if (rand == 2000) {
            level = 5;
        } else {
            rand = random.nextInt(200) + 1;
            if (rand == 200) {
                level = 4;
            } else {
                rand = random.nextInt(40) + 1;
                if (rand == 40) {
                    level = 3;
                } else {
                    rand = random.nextInt(4) + 1;
                    if (rand == 4) {
                        level = 2;
                    }
                }
            }
        }

        List<EcotronReward> levelRewards = getEcotronRewardsForLevel(level);
        if (levelRewards.isEmpty()) {
            // Fallback to level 1 if no rewards for selected level
            levelRewards = getEcotronRewardsForLevel(1);
        }

        if (levelRewards.isEmpty()) {
            return null;
        }

        return levelRewards.get(random.nextInt(levelRewards.size()));
    }

    /**
     * Generates a Pet from a database row.
     * @param row Database row map
     * @return Pet instance
     */
    public com.uber.server.game.pets.Pet generatePetFromRow(Map<String, Object> row) {
        if (row == null || game == null || game.getPetRepository() == null) {
            return null;
        }

        return com.uber.server.game.pets.Pet.fromRow(row, game.getPetRepository(), game);
    }

    /**
     * Handles a catalog purchase.
     * @param client GameClient making the purchase
     * @param pageId Catalog page ID
     * @param itemId Catalog item ID
     * @param extraData Extra data for the item
     * @param isGift Whether this is a gift purchase
     * @param giftUser Username of gift recipient (if isGift)
     * @param giftMessage Gift message (if isGift)
     */
    public void handlePurchase(GameClient client, int pageId, long itemId, String extraData,
                               boolean isGift, String giftUser, String giftMessage) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }

        CatalogPage page = getPage(pageId);
        if (page == null || page.isComingSoon() || !page.isEnabled() || !page.isVisible()) {
            return;
        }

        if (page.isClubOnly() && !habbo.getSubscriptionManager().hasSubscription("habbo_club")) {
            return;
        }

        CatalogItem item = page.getItem(itemId);
        if (item == null) {
            return;
        }

        long giftUserId = 0;
        if (isGift) {
            Item baseItem = item.getBaseItem(itemManager);
            if (baseItem == null || !baseItem.allowGift()) {
                return;
            }

            // Find gift recipient
            giftUserId = userRepository.getUserIdByUsername(giftUser);

            if (giftUserId == 0) {
                var composer = new com.uber.server.messages.outgoing.catalog.GiftWrappingErrorMessageEventComposer(true, giftUser);
                client.sendMessage(composer.compose());
                return;
            }
        }

        // Check if user has enough credits/pixels
        boolean creditsError = false;
        boolean pixelError = false;

        if (habbo.getCredits() < item.getCreditsCost()) {
            creditsError = true;
        }

        if (habbo.getActivityPoints() < item.getPixelsCost()) {
            pixelError = true;
        }

        if (creditsError || pixelError) {
            var composer = new com.uber.server.messages.outgoing.catalog.PurchaseErrorMessageEventComposer(creditsError, pixelError);
            ServerMessage response = composer.compose();
            client.sendMessage(response);
            return;
        }

        Item baseItem = item.getBaseItem(itemManager);
        if (baseItem == null) {
            return;
        }

        if (isGift && "e".equalsIgnoreCase(baseItem.getType())) {
            client.sendNotif("You can not send this item as a gift.");
            return;
        }

        // Validate and process extra data based on interaction type
        String processedExtraData = processExtraData(extraData, baseItem, habbo);
        if (processedExtraData == null) {
            return; // Validation failed
        }

        // Deduct credits/pixels
        if (item.getCreditsCost() > 0) {
            habbo.setCredits(habbo.getCredits() - item.getCreditsCost());
            habbo.updateCreditsBalance(userRepository, true);
        }

        if (item.getPixelsCost() > 0) {
            habbo.setActivityPoints(habbo.getActivityPoints() - item.getPixelsCost());
            habbo.updateActivityPointsBalance(true);
        }

        // Send purchase confirmation
        ServerMessage response = new ServerMessage(67);
        response.appendUInt(baseItem.getId());
        response.appendStringWithBreak(baseItem.getItemName());
        response.appendInt32(item.getCreditsCost());
        response.appendInt32(item.getPixelsCost());
        response.appendInt32(1);
        response.appendStringWithBreak(baseItem.getType().toLowerCase());
        response.appendInt32(baseItem.getSpriteId());
        response.appendStringWithBreak("");
        response.appendInt32(1);
        response.appendInt32(-1);
        response.appendStringWithBreak("");
        var composer = new com.uber.server.messages.outgoing.catalog.PurchaseOKComposer(response);
        client.sendMessage(composer.compose());

        if (isGift) {
            // Create gift
            long genId = catalogRepository.generateItemId();
            Item presentItem = generatePresent();

            String giftMsg = "!" + StringUtil.filterInjectionChars(giftMessage, true);
            if (!inventoryRepository.createUserItem(genId, giftUserId, presentItem.getId(), giftMsg)) {
                logger.error("Failed to create gift item for user {}", giftUserId);
                return;
            }

            if (!inventoryRepository.createUserPresent(genId, baseItem.getId(), item.getAmount(), processedExtraData)) {
                logger.error("Failed to create gift present for user {}", giftUserId);
                return;
            }

            // Notify recipient if online
            Game game = Game.getInstance();
            GameClient receiver = game.getClientManager().getClientByHabbo(giftUserId);
            if (receiver != null && receiver.getHabbo() != null) {
                receiver.sendNotif("You have received a gift! Check your inventory.");
                receiver.getHabbo().getInventoryComponent().updateItems(true);
            }

            client.sendNotif("Gift sent successfully!");
        } else {
            // Deliver items
            deliverItems(client, baseItem, item.getAmount(), processedExtraData);
        }
    }

    /**
     * Processes and validates extra data based on item interaction type.
     * @param extraData Original extra data
     * @param baseItem Base item
     * @param habbo User making purchase
     * @return Processed extra data, or null if validation failed
     */
    private String processExtraData(String extraData, Item baseItem, Habbo habbo) {
        String interactionType = baseItem.getInteractionType().toLowerCase();

        switch (interactionType) {
            case "pet":
                try {
                    String[] bits = extraData.split("\n");
                    if (bits.length < 3) {
                        return null;
                    }

                    String petName = bits[0];
                    String race = bits[1];
                    String color = bits[2];

                    // Validate pet name
                    if (!checkPetName(petName)) {
                        return null;
                    }

                    // Validate race (must be 3 characters and parseable as int)
                    try {
                        Integer.parseInt(race);
                        if (race.length() != 3) {
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        return null;
                    }

                    // Validate color (must be 6 characters)
                    if (color.length() != 6) {
                        return null;
                    }
                } catch (Exception e) {
                    return null;
                }
                break;

            case "roomeffect":
                try {
                    double number = Double.parseDouble(extraData);
                    extraData = String.valueOf(number).replace(',', '.');
                } catch (Exception e) {
                    // Keep original extraData
                }
                break;

            case "postit":
                extraData = "FFFF33";
                break;

            case "dimmer":
                extraData = "1,1,1,#000000,255";
                break;

            case "trophy":
                LocalDateTime now = LocalDateTime.now();
                String dateStr = now.getDayOfMonth() + "-" + now.getMonthValue() + "-" + now.getYear();
                extraData = habbo.getUsername() + "\t" + dateStr + "\t" + StringUtil.filterInjectionChars(extraData, true);
                break;

            default:
                extraData = "";
                break;
        }

        return extraData;
    }

    /**
     * Checks if a pet name is valid.
     * @param petName Pet name to validate
     * @return True if valid
     */
    public boolean checkPetName(String petName) {
        if (petName == null || petName.length() < 1 || petName.length() > 16) {
            return false;
        }

        return StringUtil.isValidAlphaNumeric(petName);
    }

    /**
     * Gets the marketplace manager.
     * @return Marketplace instance
     */
    public Marketplace getMarketplace() {
        return marketplace;
    }

    /**
     * Gets the catalog repository.
     * @return CatalogRepository instance
     */
    public CatalogRepository getCatalogRepository() {
        return catalogRepository;
    }

    /**
     * Delivers items to the user's inventory.
     * @param client GameClient receiving items
     * @param item Base item
     * @param amount Amount to deliver
     * @param extraData Extra data
     */
    public void deliverItems(GameClient client, Item item, int amount, String extraData) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }

        String itemType = item.getType().toLowerCase();

        switch (itemType) {
            case "i":
            case "s":
                // Inventory items
                for (int i = 0; i < amount; i++) {
                    long generatedId = catalogRepository.generateItemId();
                    if (generatedId == 0) {
                        logger.error("Failed to generate item ID");
                        continue;
                    }

                    String interactionType = item.getInteractionType().toLowerCase();

                    switch (interactionType) {
                        case "pet":
                            String[] petData = extraData.split("\n");
                            if (petData.length >= 3) {
                                long petId = createPet(habbo.getId(), petData[0], 5, petData[1], petData[2]);
                                if (petId > 0) {
                                    Map<String, Object> pet = petRepository.getPet(petId);
                                    if (pet != null) {
                                        habbo.getInventoryComponent().addPet(pet);
                                    }
                                }
                                // Add pet item (pet box)
                                habbo.getInventoryComponent().addItem(generatedId, 320, "0");
                            }
                            break;

                        case "teleport":
                            long teleTwo = catalogRepository.generateItemId();
                            if (teleTwo > 0 && generatedId > 0) {
                                // Create teleport link in database
                                catalogRepository.createTeleLink(generatedId, teleTwo);
                                // Add both teleporters to inventory
                                habbo.getInventoryComponent().addItem(teleTwo, item.getId(), "0");
                                habbo.getInventoryComponent().addItem(generatedId, item.getId(), "0");
                            }
                            break;

                        case "dimmer":
                            // Moodlight items need special handling - simplified for now
                            habbo.getInventoryComponent().addItem(generatedId, item.getId(), extraData);
                            break;

                        default:
                            habbo.getInventoryComponent().addItem(generatedId, item.getId(), extraData);
                            break;
                    }
                }
                habbo.getInventoryComponent().updateItems(true);
                break;

            case "e":
                // Avatar effects
                for (int i = 0; i < amount; i++) {
                    habbo.getAvatarEffectsInventoryComponent().addEffect(item.getSpriteId(), 3600);
                }
                break;

            case "h":
                // Habbo Club subscription
                for (int i = 0; i < amount; i++) {
                    habbo.getSubscriptionManager().addOrExtendSubscription("habbo_club", 2678400);
                }

                // Give HC1 badge if not already have it
                if (!habbo.getBadgeComponent().hasBadge("HC1")) {
                    habbo.getBadgeComponent().giveBadge("HC1", true);
                }

                // Send subscription update
                ServerMessage subResponse = new ServerMessage(7);
                subResponse.appendStringWithBreak("habbo_club");

                if (habbo.getSubscriptionManager().hasSubscription("habbo_club")) {
                    com.uber.server.game.users.subscriptions.Subscription sub =
                            habbo.getSubscriptionManager().getSubscription("habbo_club");
                    if (sub != null) {
                        long expireTime = sub.getExpireTime();
                        long timeLeft = expireTime - TimeUtil.getUnixTimestamp();
                        int totalDaysLeft = (int) Math.ceil(timeLeft / 86400.0);
                        int monthsLeft = totalDaysLeft / 31;
                        if (monthsLeft >= 1) monthsLeft--;

                        subResponse.appendInt32(totalDaysLeft - (monthsLeft * 31));
                        subResponse.appendBoolean(true);
                        subResponse.appendInt32(monthsLeft);
                    } else {
                        subResponse.appendInt32(0);
                        subResponse.appendBoolean(false);
                        subResponse.appendInt32(0);
                    }
                } else {
                    subResponse.appendInt32(0);
                    subResponse.appendBoolean(false);
                    subResponse.appendInt32(0);
                }
                var subscriptionComposer = new com.uber.server.messages.outgoing.users.ScrSendUserInfoComposer(subResponse);
                client.sendMessage(subscriptionComposer.compose());

                // Send rights update
                Game game = Game.getInstance();
                if (game.getRoleManager() != null) {
                    List<String> rights = game.getRoleManager().getRightsForHabbo(habbo);
                    var rightsComposer = new com.uber.server.messages.outgoing.handshake.UserRightsComposer(rights);
                    client.sendMessage(rightsComposer.compose());
                }
                break;

            default:
                client.sendNotif("Something went wrong! The item type could not be processed. Please do not try to buy this item anymore, instead inform support as soon as possible.");
                break;
        }
    }

    /**
     * Generates a random present item.
     * @return Present item
     */
    private Item generatePresent() {
        int randomNum = random.nextInt(7); // 0-6

        long[] presentIds = {164, 165, 166, 167, 168, 169, 170};
        long presentId = presentIds[randomNum];

        return itemManager.getItem(presentId);
    }

    /**
     * Creates a pet in the database.
     * @param userId User ID
     * @param name Pet name
     * @param type Pet type
     * @param race Pet race
     * @param color Pet color
     * @return Pet ID, or 0 if failed
     */
    private long createPet(long userId, String name, int type, String race, String color) {
        long createTimestamp = TimeUtil.getUnixTimestamp();
        long petId = petRepository.createPet(userId, name, type, race, color, createTimestamp);
        return petId;
    }
    
    /**
     * Serializes the catalog index for a client.
     * @param client GameClient requesting the index
     * @return ServerMessage with catalog index (ID 126)
     */
    public ServerMessage serializeIndex(GameClient client) {
        ServerMessage index = new ServerMessage(126); // Will be wrapped by composer when sent
        index.appendBoolean(false);
        index.appendInt32(0);
        index.appendInt32(0);
        index.appendInt32(-1);
        index.appendStringWithBreak("");
        index.appendBoolean(false);
        index.appendInt32(getTreeSize(client, -1));
        
        // Serialize root pages and their children
        for (CatalogPage page : pages.values()) {
            if (page.getParentId() != -1) {
                continue;
            }
            
            page.serialize(client, index);
            
            // Serialize child pages
            for (CatalogPage childPage : pages.values()) {
                if (childPage.getParentId() == page.getId()) {
                    childPage.serialize(client, index);
                }
            }
        }
        
        return index;
    }
    
    /**
     * Serializes a catalog page for a client.
     * @param page CatalogPage to serialize
     * @return ServerMessage with page data (ID 127)
     */
    public ServerMessage serializePage(CatalogPage page) {
        ServerMessage pageData = new ServerMessage(127); // Will be wrapped by composer when sent
        pageData.appendInt32(page.getId());
        
        String layout = page.getLayout();
        
        switch (layout) {
            case "frontpage":
                pageData.appendStringWithBreak("frontpage3");
                pageData.appendInt32(3);
                pageData.appendStringWithBreak(page.getLayoutHeadline());
                pageData.appendStringWithBreak(page.getLayoutTeaser());
                pageData.appendStringWithBreak("");
                pageData.appendInt32(11);
                pageData.appendStringWithBreak(page.getText1());
                pageData.appendStringWithBreak("");
                pageData.appendStringWithBreak(page.getText2());
                pageData.appendStringWithBreak(page.getTextDetails());
                pageData.appendStringWithBreak("");
                pageData.appendStringWithBreak("#FAF8CC");
                pageData.appendStringWithBreak("#FAF8CC");
                pageData.appendStringWithBreak("Other ways to get more credits >");
                pageData.appendStringWithBreak("magic.credits");
                break;
                
            case "recycler_info":
                pageData.appendStringWithBreak(page.getLayout());
                pageData.appendInt32(2);
                pageData.appendStringWithBreak(page.getLayoutHeadline());
                pageData.appendStringWithBreak(page.getLayoutTeaser());
                pageData.appendInt32(3);
                pageData.appendStringWithBreak(page.getText1());
                pageData.appendStringWithBreak(page.getText2());
                pageData.appendStringWithBreak(page.getTextDetails());
                break;
                
            case "recycler_prizes":
                pageData.appendStringWithBreak("recycler_prizes");
                pageData.appendInt32(1);
                pageData.appendStringWithBreak("catalog_recycler_headline3");
                pageData.appendInt32(1);
                pageData.appendStringWithBreak(page.getText1());
                break;
                
            case "spaces":
                pageData.appendStringWithBreak(page.getLayout());
                pageData.appendInt32(1);
                pageData.appendStringWithBreak(page.getLayoutHeadline());
                pageData.appendInt32(1);
                pageData.appendStringWithBreak(page.getText1());
                break;
                
            case "recycler":
                pageData.appendStringWithBreak(page.getLayout());
                pageData.appendInt32(2);
                pageData.appendStringWithBreak(page.getLayoutHeadline());
                pageData.appendStringWithBreak(page.getLayoutTeaser());
                pageData.appendInt32(1);
                // Text1 should be limited to 10 characters for recycler layout
                String recyclerText1 = page.getText1();
                if (recyclerText1 != null && recyclerText1.length() > 10) {
                    recyclerText1 = recyclerText1.substring(0, 10);
                }
                pageData.appendStringWithBreak(recyclerText1 != null ? recyclerText1 : "");
                pageData.appendStringWithBreak(page.getText2());
                pageData.appendStringWithBreak(page.getTextDetails());
                break;
                
            case "trophies":
                pageData.appendStringWithBreak("trophies");
                pageData.appendInt32(1);
                pageData.appendStringWithBreak(page.getLayoutHeadline());
                pageData.appendInt32(2);
                pageData.appendStringWithBreak(page.getText1());
                pageData.appendStringWithBreak(page.getTextDetails());
                break;
                
            case "pets":
                pageData.appendStringWithBreak("pets");
                pageData.appendInt32(2);
                pageData.appendStringWithBreak(page.getLayoutHeadline());
                pageData.appendStringWithBreak(page.getLayoutTeaser());
                pageData.appendInt32(4);
                pageData.appendStringWithBreak(page.getText1());
                pageData.appendStringWithBreak("Give a name:");
                pageData.appendStringWithBreak("Pick a color:");
                pageData.appendStringWithBreak("Pick a race:");
                break;
                
            default:
                pageData.appendStringWithBreak(page.getLayout());
                pageData.appendInt32(3);
                pageData.appendStringWithBreak(page.getLayoutHeadline());
                pageData.appendStringWithBreak(page.getLayoutTeaser());
                pageData.appendStringWithBreak(page.getLayoutSpecial());
                pageData.appendInt32(3);
                pageData.appendStringWithBreak(page.getText1());
                pageData.appendStringWithBreak(page.getTextDetails());
                pageData.appendStringWithBreak(page.getTextTeaser());
                break;
        }
        
        pageData.appendInt32(page.getItems().size());
        
        for (CatalogItem item : page.getItems()) {
            item.serialize(pageData, itemManager);
        }
        
        return pageData;
    }
}