package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client purchases a gift (packet ID 472).
 */
public class PurchaseGiftEvent extends PacketReceiveEvent {
    private int pageId;
    private int itemId;
    private String extraData;
    private String recipientName;
    private String giftMessage;
    private int giftSpriteId;
    private int giftRibbon;
    private int giftBox;
    
    public PurchaseGiftEvent(GameClient client, ClientMessage message, int pageId, int itemId,
                             String extraData, String recipientName, String giftMessage,
                             int giftSpriteId, int giftRibbon, int giftBox) {
        super(client, message, 472);
        this.pageId = pageId;
        this.itemId = itemId;
        this.extraData = extraData;
        this.recipientName = recipientName;
        this.giftMessage = giftMessage;
        this.giftSpriteId = giftSpriteId;
        this.giftRibbon = giftRibbon;
        this.giftBox = giftBox;
    }
    
    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    
    public String getExtraData() { return extraData; }
    public void setExtraData(String extraData) { this.extraData = extraData; }
    
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    
    public String getGiftMessage() { return giftMessage; }
    public void setGiftMessage(String giftMessage) { this.giftMessage = giftMessage; }
    
    public int getGiftSpriteId() { return giftSpriteId; }
    public void setGiftSpriteId(int giftSpriteId) { this.giftSpriteId = giftSpriteId; }
    
    public int getGiftRibbon() { return giftRibbon; }
    public void setGiftRibbon(int giftRibbon) { this.giftRibbon = giftRibbon; }
    
    public int getGiftBox() { return giftBox; }
    public void setGiftBox(int giftBox) { this.giftBox = giftBox; }
}
