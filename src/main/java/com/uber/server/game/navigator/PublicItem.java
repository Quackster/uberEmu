package com.uber.server.game.navigator;

/**
 * Represents a public room item in the navigator.
 */
public class PublicItem {
    public enum PublicImageType {
        INTERNAL,
        EXTERNAL
    }
    
    private final int id;
    private final int type;
    private final String caption;
    private final String image;
    private final PublicImageType imageType;
    private final long roomId;
    private final int categoryId;
    private final int parentId;
    private final int orderId;
    
    public PublicItem(int id, int type, String caption, String image, PublicImageType imageType,
                     long roomId, int categoryId, int parentId, int orderId) {
        this.id = id;
        this.type = type;
        this.caption = caption;
        this.image = image;
        this.imageType = imageType;
        this.roomId = roomId;
        this.categoryId = categoryId;
        this.parentId = parentId;
        this.orderId = orderId;
    }
    
    public boolean isCategory() {
        return categoryId > 0;
    }
    
    // Getters
    public int getId() { return id; }
    public int getType() { return type; }
    public String getCaption() { return caption; }
    public String getImage() { return image; }
    public PublicImageType getImageType() { return imageType; }
    public long getRoomId() { return roomId; }
    public int getCategoryId() { return categoryId; }
    public int getParentId() { return parentId; }
    public int getOrderId() { return orderId; }
    
    /**
     * Serializes this public item to a ServerMessage.
     * @param message ServerMessage to append to
     */
    public void serialize(com.uber.server.messages.ServerMessage message) {
        message.appendInt32(id);
        
        com.uber.server.game.rooms.RoomData roomData = null;
        if (!isCategory()) {
            // Get room data for non-category items
            com.uber.server.game.Game game = com.uber.server.game.Game.getInstance();
            if (game != null && game.getRoomManager() != null) {
                roomData = game.getRoomManager().generateRoomData(roomId);
            }
        }
        
        if (isCategory()) {
            message.appendStringWithBreak(caption);
        } else {
            if (roomData != null) {
                message.appendStringWithBreak(roomData.getName());
            } else {
                message.appendStringWithBreak(caption);
            }
        }
        
        // Description
        if (roomData != null) {
            message.appendStringWithBreak(roomData.getDescription());
        } else {
            message.appendStringWithBreak("");
        }
        
        message.appendInt32(type);
        message.appendStringWithBreak(caption);
        message.appendStringWithBreak((imageType == PublicImageType.EXTERNAL) ? image : "");
        
        if (!isCategory()) {
            message.appendUInt(0);
            if (roomData != null) {
                message.appendInt32(roomData.getUsersNow());
                message.appendInt32(3);
                message.appendStringWithBreak((imageType == PublicImageType.INTERNAL) ? image : "");
                message.appendUInt(1337);
                message.appendInt32(0);
                message.appendStringWithBreak(roomData.getCCTs() != null ? roomData.getCCTs() : "");
                message.appendInt32(roomData.getUsersMax());
                message.appendUInt(roomId);
            } else {
                message.appendInt32(0);
                message.appendInt32(3);
                message.appendStringWithBreak((imageType == PublicImageType.INTERNAL) ? image : "");
                message.appendUInt(1337);
                message.appendInt32(0);
                message.appendStringWithBreak("");
                message.appendInt32(0);
                message.appendUInt(roomId);
            }
        } else {
            message.appendInt32(0);
            message.appendInt32(4);
            message.appendInt32(categoryId);
        }
    }
}
