package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import java.util.List;

/**
 * Event fired when a client makes a purchase (packet ID 100).
 */
public class HandlePurchaseEvent extends PacketReceiveEvent {
    private int pageId;
    private List<Integer> itemIds;
    private String extraData;
    private String recipientName;
    private String giftMessage;
    
    public HandlePurchaseEvent(GameClient client, ClientMessage message, int pageId, 
                              List<Integer> itemIds, String extraData, String recipientName, String giftMessage) {
        super(client, message, 100);
        this.pageId = pageId;
        this.itemIds = itemIds;
        this.extraData = extraData;
        this.recipientName = recipientName;
        this.giftMessage = giftMessage;
    }
    
    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    
    public List<Integer> getItemIds() { return itemIds; }
    public void setItemIds(List<Integer> itemIds) { this.itemIds = itemIds; }
    
    public String getExtraData() { return extraData; }
    public void setExtraData(String extraData) { this.extraData = extraData; }
    
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    
    public String getGiftMessage() { return giftMessage; }
    public void setGiftMessage(String giftMessage) { this.giftMessage = giftMessage; }
}
