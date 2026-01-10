package com.uber.server.game.catalog;

import com.uber.server.game.GameClient;
import com.uber.server.game.items.ItemManager;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.CatalogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a catalog page.
 */
public class CatalogPage {
    private static final Logger logger = LoggerFactory.getLogger(CatalogPage.class);
    
    private final int id;
    private final int parentId;
    private final String caption;
    private final boolean visible;
    private final boolean enabled;
    private final boolean comingSoon;
    private final long minRank;
    private final boolean clubOnly;
    private final int iconColor;
    private final int iconImage;
    private final String layout;
    private final String layoutHeadline;
    private final String layoutTeaser;
    private final String layoutSpecial;
    private final String text1;
    private final String text2;
    private final String textDetails;
    private final String textTeaser;
    
    private final List<CatalogItem> items;
    
    public CatalogPage(int id, int parentId, String caption, boolean visible, boolean enabled,
                      boolean comingSoon, long minRank, boolean clubOnly, int iconColor, int iconImage,
                      String layout, String layoutHeadline, String layoutTeaser, String layoutSpecial,
                      String text1, String text2, String textDetails, String textTeaser,
                      CatalogRepository catalogRepository, ItemManager itemManager) {
        this.id = id;
        this.parentId = parentId;
        this.caption = caption;
        this.visible = visible;
        this.enabled = enabled;
        this.comingSoon = comingSoon;
        this.minRank = minRank;
        this.clubOnly = clubOnly;
        this.iconColor = iconColor;
        this.iconImage = iconImage;
        this.layout = layout;
        this.layoutHeadline = layoutHeadline;
        this.layoutTeaser = layoutTeaser;
        this.layoutSpecial = layoutSpecial;
        this.text1 = text1;
        this.text2 = text2;
        this.textDetails = textDetails;
        this.textTeaser = textTeaser;
        
        this.items = new ArrayList<>();
        
        // Load items for this page
        List<Map<String, Object>> itemData = catalogRepository.loadCatalogItems(id);
        for (Map<String, Object> row : itemData) {
            String itemIdsStr = (String) row.get("item_ids");
            int amount = ((Number) row.get("amount")).intValue();
            
            if (itemIdsStr == null || itemIdsStr.isEmpty() || amount <= 0) {
                continue;
            }
            
            try {
                CatalogItem item = new CatalogItem(
                    ((Number) row.get("id")).longValue(),
                    (String) row.get("catalog_name"),
                    itemIdsStr,
                    ((Number) row.get("cost_credits")).intValue(),
                    ((Number) row.get("cost_pixels")).intValue(),
                    amount
                );
                items.add(item);
            } catch (Exception e) {
                logger.error("Failed to create catalog item: {}", e.getMessage(), e);
            }
        }
    }
    
    public CatalogItem getItem(long itemId) {
        for (CatalogItem item : items) {
            if (item.getId() == itemId) {
                return item;
            }
        }
        return null;
    }
    
    // Getters
    public int getId() { return id; }
    public int getParentId() { return parentId; }
    public String getCaption() { return caption; }
    public boolean isVisible() { return visible; }
    public boolean isEnabled() { return enabled; }
    public boolean isComingSoon() { return comingSoon; }
    public long getMinRank() { return minRank; }
    public boolean isClubOnly() { return clubOnly; }
    public int getIconColor() { return iconColor; }
    public int getIconImage() { return iconImage; }
    public String getLayout() { return layout; }
    public String getLayoutHeadline() { return layoutHeadline; }
    public String getLayoutTeaser() { return layoutTeaser; }
    public String getLayoutSpecial() { return layoutSpecial; }
    public String getText1() { return text1; }
    public String getText2() { return text2; }
    public String getTextDetails() { return textDetails; }
    public String getTextTeaser() { return textTeaser; }
    public List<CatalogItem> getItems() { return items; }
    
    /**
     * Serializes this catalog page for the index.
     * @param client GameClient requesting the page
     * @param message ServerMessage to append to
     */
    public void serialize(GameClient client, ServerMessage message) {
        message.appendBoolean(visible);
        message.appendInt32(iconColor);
        message.appendInt32(iconImage);
        message.appendInt32(id);
        message.appendStringWithBreak(caption);
        message.appendBoolean(comingSoon);
        
        // Get catalog from game to calculate tree size
        com.uber.server.game.Game game = com.uber.server.game.Game.getInstance();
        if (game != null && game.getCatalog() != null) {
            message.appendInt32(game.getCatalog().getTreeSize(client, id));
        } else {
            message.appendInt32(0);
        }
    }
}
