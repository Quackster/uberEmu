package com.uber.server.game.items;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.pathfinding.Coord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an item placed in a room.
 */
public class RoomItem {
    private static final Logger logger = LoggerFactory.getLogger(RoomItem.class);
    
    private final long id;
    private final long roomId;
    private final long baseItem;
    private String extraData;
    private int x;
    private int y;
    private double z;
    private int rot;
    private String wallPos;
    
    private boolean updateNeeded;
    private int updateCounter;
    private long interactingUser;
    private long interactingUser2;
    
    private final Game game;
    private final ItemManager itemManager;
    
    public RoomItem(long id, long roomId, long baseItem, String extraData, int x, int y, double z, int rot, String wallPos, Game game) {
        this.id = id;
        this.roomId = roomId;
        this.baseItem = baseItem;
        this.extraData = extraData != null ? extraData : "";
        this.x = x;
        this.y = y;
        this.z = z;
        this.rot = rot;
        this.wallPos = wallPos != null ? wallPos : "";
        this.updateNeeded = false;
        this.updateCounter = 0;
        this.interactingUser = 0;
        this.interactingUser2 = 0;
        this.game = game;
        this.itemManager = game != null ? game.getItemManager() : null;
    }
    
    public long getId() { return id; }
    public long getRoomId() { return roomId; }
    public long getBaseItemId() { return baseItem; }
    public String getExtraData() { return extraData; }
    public void setExtraData(String extraData) { this.extraData = extraData != null ? extraData : ""; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    public int getRot() { return rot; }
    public void setRot(int rot) { this.rot = rot; }
    public String getWallPos() { return wallPos; }
    public void setWallPos(String wallPos) { this.wallPos = wallPos != null ? wallPos : ""; }
    public boolean isUpdateNeeded() { return updateNeeded; }
    public void setUpdateNeeded(boolean updateNeeded) { this.updateNeeded = updateNeeded; }
    public long getInteractingUser() { return interactingUser; }
    public void setInteractingUser(long interactingUser) { this.interactingUser = interactingUser; }
    public long getInteractingUser2() { return interactingUser2; }
    public void setInteractingUser2(long interactingUser2) { this.interactingUser2 = interactingUser2; }
    
    /**
     * Gets the coordinate of this item.
     * @return Coord object
     */
    public Coord getCoordinate() {
        return new Coord(x, y);
    }
    
    /**
     * Gets the total height (Z + item height).
     * @return Total height
     */
    public double getTotalHeight() {
        Item base = getBaseItem();
        if (base == null) {
            return z;
        }
        return z + base.getHeight();
    }
    
    /**
     * Checks if this is a wall item.
     * @return True if wall item
     */
    public boolean isWallItem() {
        Item base = getBaseItem();
        if (base == null) {
            return false;
        }
        return "i".equalsIgnoreCase(base.getType());
    }
    
    /**
     * Checks if this is a floor item.
     * @return True if floor item
     */
    public boolean isFloorItem() {
        Item base = getBaseItem();
        if (base == null) {
            return false;
        }
        return "s".equalsIgnoreCase(base.getType());
    }
    
    /**
     * Gets the base item definition.
     * @return Item object, or null if not found
     */
    public Item getBaseItem() {
        if (itemManager == null) {
            return null;
        }
        return itemManager.getItem(baseItem);
    }
    
    /**
     * Gets the coordinate square in front of the item (based on rotation).
     * @return Coord in front
     */
    public Coord getSquareInFront() {
        Coord sq = new Coord(x, y);
        
        switch (rot) {
            case 0: // North
                sq.setY(sq.getY() - 1);
                break;
            case 2: // East
                sq.setX(sq.getX() + 1);
                break;
            case 4: // South
                sq.setY(sq.getY() + 1);
                break;
            case 6: // West
                sq.setX(sq.getX() - 1);
                break;
        }
        
        return sq;
    }
    
    /**
     * Gets the coordinate square behind the item (based on rotation).
     * @return Coord behind
     */
    public Coord getSquareBehind() {
        Coord sq = new Coord(x, y);
        
        switch (rot) {
            case 0: // North
                sq.setY(sq.getY() + 1);
                break;
            case 2: // East
                sq.setX(sq.getX() - 1);
                break;
            case 4: // South
                sq.setY(sq.getY() - 1);
                break;
            case 6: // West
                sq.setX(sq.getX() + 1);
                break;
        }
        
        return sq;
    }
    
    /**
     * Gets the room this item is in.
     * @return Room instance
     */
    public com.uber.server.game.rooms.Room getRoom() {
        if (game == null || game.getRoomManager() == null) {
            return null;
        }
        return game.getRoomManager().getRoom(roomId);
    }
    
    /**
     * Gets the game instance.
     * @return Game instance
     */
    public Game getGame() {
        return game;
    }
    
    /**
     * Gets the appropriate interactor for this item.
     * @return FurniInteractor instance
     */
    public com.uber.server.game.items.interactors.FurniInteractor getInteractor() {
        Item base = getBaseItem();
        if (base == null) {
            return new com.uber.server.game.items.interactors.InteractorStatic();
        }
        
        String interactionType = base.getInteractionType();
        if (interactionType == null) {
            return new com.uber.server.game.items.interactors.InteractorStatic();
        }
        
        String lowerType = interactionType.toLowerCase();
        
        return switch (lowerType) {
            case "teleport" -> new com.uber.server.game.items.interactors.InteractorTeleport();
            case "bottle" -> new com.uber.server.game.items.interactors.InteractorSpinningBottle();
            case "dice" -> new com.uber.server.game.items.interactors.InteractorDice();
            case "habbowheel" -> new com.uber.server.game.items.interactors.InteractorHabboWheel();
            case "loveshuffler" -> new com.uber.server.game.items.interactors.InteractorLoveShuffler();
            case "onewaygate" -> new com.uber.server.game.items.interactors.InteractorOneWayGate();
            case "alert" -> new com.uber.server.game.items.interactors.InteractorAlert();
            case "vendingmachine" -> new com.uber.server.game.items.interactors.InteractorVendor();
            case "gate" -> new com.uber.server.game.items.interactors.InteractorGate(base.getInteractionModesCount());
            case "scoreboard" -> new com.uber.server.game.items.interactors.InteractorScoreboard();
            default -> new com.uber.server.game.items.interactors.InteractorGenericSwitch(base.getInteractionModesCount());
        };
    }
    
    /**
     * Serializes the item to a ServerMessage.
     * @param message ServerMessage to append to
     */
    public void serialize(ServerMessage message) {
        Item base = getBaseItem();
        if (base == null) {
            logger.warn("Cannot serialize RoomItem {}: base item {} not found", id, baseItem);
            return;
        }
        
        if (isFloorItem()) {
            // Floor item serialization
            message.appendUInt(id);
            message.appendInt32(base.getSpriteId());
            message.appendInt32(x);
            message.appendInt32(y);
            message.appendInt32(rot);
            message.appendStringWithBreak(String.format("%.2f", z).replace(',', '.'));
            message.appendInt32(0);
            message.appendStringWithBreak(extraData != null ? extraData : "");
            message.appendInt32(-1);
        } else if (isWallItem()) {
            // Wall item serialization
            message.appendStringWithBreak(String.valueOf(id));
            message.appendInt32(base.getSpriteId());
            message.appendStringWithBreak(wallPos != null ? wallPos : "");
            
            // Handle postit items specially
            String interactionType = base.getInteractionType() != null ? base.getInteractionType().toLowerCase() : "";
            if ("postit".equals(interactionType) && extraData != null && !extraData.isEmpty()) {
                // Postit extra data format: "color text" - we only send the color part
                String[] parts = extraData.split(" ", 2);
                message.appendStringWithBreak(parts.length > 0 ? parts[0] : "");
            } else {
                message.appendStringWithBreak(extraData != null ? extraData : "");
            }
        }
    }
    
    /**
     * Updates the item state.
     * @param allUsers If true, updates all users; if false, only users nearby
     * @param inDatabase If true, saves to database
     */
    public void updateState(boolean allUsers, boolean inDatabase) {
        // Mark as needing update
        this.updateNeeded = true;
        this.updateCounter++;
        
        // Update item in database if requested
        if (inDatabase && game != null && game.getRoomItemRepository() != null) {
            game.getRoomItemRepository().updatePosition(id, x, y, z, rot);
            game.getRoomItemRepository().updateExtraData(id, extraData);
        }
        
        // Send update message to room users
        if (game != null && game.getRoomManager() != null) {
            com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(roomId);
            if (room != null) {
                com.uber.server.messages.ServerMessage message = new com.uber.server.messages.ServerMessage(85);
                serialize(message);
                if (allUsers) {
                    room.sendMessage(message);
                } else {
                    // TODO: Send only to nearby users when room user system is complete
                    room.sendMessage(message);
                }
            }
        }
    }
    
    /**
     * Processes item updates (called every 500ms by room processor).
     */
    public void processUpdates() {
        this.updateCounter--;
        
        if (this.updateCounter <= 0) {
            this.updateNeeded = false;
            this.updateCounter = 0;
            
            // Process item-specific updates based on interaction type
            Item base = getBaseItem();
            if (base == null) {
                return;
            }
            
            String interactionType = base.getInteractionType() != null ? base.getInteractionType().toLowerCase() : "";
            
            switch (interactionType) {
                case "bottle":
                    // Spinning bottle - random result
                    int bottleResult = (int) (Math.random() * 8); // 0-7
                    setExtraData(String.valueOf(bottleResult));
                    updateState(true, true);
                    break;
                    
                case "dice":
                    // Dice - random result 1-6
                    int diceResult = (int) (Math.random() * 6) + 1; // 1-6
                    setExtraData(String.valueOf(diceResult));
                    updateState(true, true);
                    break;
                    
                case "habbowheel":
                    // Habbo wheel - random result 1-10
                    int wheelResult = (int) (Math.random() * 10) + 1; // 1-10
                    setExtraData(String.valueOf(wheelResult));
                    updateState(true, true);
                    break;
                    
                case "loveshuffler":
                    if ("0".equals(extraData)) {
                        int shufflerResult = (int) (Math.random() * 4) + 1; // 1-4
                        setExtraData(String.valueOf(shufflerResult));
                        reqUpdate(20);
                        updateState(false, true);
                    } else if (!"-1".equals(extraData)) {
                        setExtraData("-1");
                        updateState(false, true);
                    }
                    break;
                    
                case "alert":
                    if ("1".equals(extraData)) {
                        setExtraData("0");
                        updateState(false, true);
                    }
                    break;
                    
                case "vendingmachine":
                    if ("1".equals(extraData)) {
                        // Vending machine logic would go here
                        // For now, just reset
                        setExtraData("0");
                        setInteractingUser(0);
                        updateState(false, true);
                    }
                    break;
                    
                // Teleport and onewaygate require more complex logic with room users
                // Will be handled when FurniInteractor is fully implemented
            }
        }
    }
    
    /**
     * Requests an update after a certain number of cycles.
     * @param cycles Number of cycles (500ms each) to wait before processing
     */
    public void reqUpdate(int cycles) {
        this.updateCounter = cycles;
        this.updateNeeded = true;
    }
}
