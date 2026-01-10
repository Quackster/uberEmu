package com.uber.server.game.rooms.services;

import com.uber.server.game.items.RoomItem;
import com.uber.server.game.pathfinding.Coord;
import com.uber.server.game.pathfinding.Pathfinder;
import com.uber.server.game.pathfinding.Rotation;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomModel;
import com.uber.server.game.rooms.RoomUser;
import com.uber.server.game.threading.GameThreadPool;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing room processing routine.
 * Handles periodic room updates (every 500ms).
 */
public class RoomProcessService {
    private static final Logger logger = LoggerFactory.getLogger(RoomProcessService.class);
    
    private final Room room;
    private final ConcurrentHashMap<Long, RoomItem> items;
    private final ConcurrentHashMap<Long, RoomUser> users;
    private final boolean[] keepAliveRef; // Use array to allow modification
    private final int[] idleTimeRef; // Use array to allow modification
    
    private ScheduledFuture<?> processTask;
    
    public RoomProcessService(Room room, ConcurrentHashMap<Long, RoomItem> items,
                             ConcurrentHashMap<Long, RoomUser> users,
                             boolean[] keepAliveRef, int[] idleTimeRef) {
        this.room = room;
        this.items = items;
        this.users = users;
        this.keepAliveRef = keepAliveRef;
        this.idleTimeRef = idleTimeRef;
    }
    
    /**
     * Starts the room processing routine.
     * Uses shared thread pool from GameThreadPool.
     */
    public void startProcessRoutine() {
        if (processTask != null && !processTask.isCancelled()) {
            return; // Already running
        }
        
        // Use shared thread pool instead of per-room executor
        ScheduledExecutorService executor = GameThreadPool.getInstance().getGameExecutor();
        
        // Process every 500ms
        processTask = executor.scheduleAtFixedRate(this::processRoom, 500, 500, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Stops the room processing routine.
     * Note: We don't shut down the shared executor here, only cancel our task.
     */
    public void stopProcessRoutine() {
        if (processTask != null) {
            processTask.cancel(false);
            processTask = null;
        }
    }
    
    /**
     * Processes the room (called every 500ms).
     */
    private void processRoom() {
        if (!keepAliveRef[0]) {
            return; // Don't process if room should be dead
        }
        
        // Process item updates
        for (RoomItem item : items.values()) {
            if (item.isUpdateNeeded()) {
                item.processUpdates();
            }
        }
        
        // Process rollers - move items and users on top of them
        processRollers();
        
        // Process users and bots
        List<Long> toRemove = new ArrayList<>();
        int userCount = 0;
        
        for (RoomUser user : users.values()) {
            // Increment idle time
            user.incrementIdleTime();
            
            // Check if user should fall asleep (idle for 600 ticks = 300 seconds)
            if (!user.isAsleep() && user.getIdleTime() >= 600) {
                user.setAsleep(true);
                var sleepComposer = new com.uber.server.messages.outgoing.rooms.SleepComposer(
                    user.getVirtualId(), true);
                room.sendMessage(sleepComposer.compose());
            }
            
            // Handle carry item timer
            if (user.getCarryItemId() > 0) {
                user.decrementCarryTimer();
                if (user.getCarryTimer() <= 0) {
                    user.carryItem(0);
                }
            }
            
            boolean invalidSetStep = false;
            
            // Handle SetStep (immediate position change)
            if (user.isSetStep()) {
                if (room.canWalk(user.getSetX(), user.getSetY(), 0, true) || user.isAllowOverride()) {
                    // Update UserMatrix
                    if (room.getRoomMapping() != null) {
                        room.getRoomMapping().setUserPosition(user.getX(), user.getY(), false);
                    }
                    
                    user.setX(user.getSetX());
                    user.setY(user.getSetY());
                    user.setZ(user.getSetZ());
                    
                    // Update UserMatrix
                    if (room.getRoomMapping() != null) {
                        room.getRoomMapping().setUserPosition(user.getX(), user.getY(), true);
                    }
                    
                    // Update user status
                    user.setUpdateNeeded(true);
                } else {
                    invalidSetStep = true;
                }
                
                user.setSetStep(false);
            }
            
            // Handle path recalculation
            if (user.isPathRecalcNeeded()) {
                Pathfinder pathfinder = new Pathfinder(room, user);
                
                user.setGoalX(user.getPathRecalcX());
                user.setGoalY(user.getPathRecalcY());
                
                List<Coord> calculatedPath = user.getPath();
                if (calculatedPath != null) {
                    calculatedPath.clear();
                }
                List<Coord> path = pathfinder.findPath();
                if (path != null && calculatedPath != null) {
                    calculatedPath.addAll(path);
                }
                
                if (calculatedPath != null && calculatedPath.size() > 1) {
                    user.setPathStep(1);
                    user.setWalking(true);
                    user.setPathRecalcNeeded(false);
                } else {
                    user.setPathRecalcNeeded(false);
                    if (calculatedPath != null) {
                        calculatedPath.clear();
                    }
                }
            }
            
            // Handle walking along path
            if (user.isWalking()) {
                List<Coord> path = user.getPath();
                if (path == null || invalidSetStep || user.getPathStep() >= path.size() || 
                    (user.getGoalX() == user.getX() && user.getGoalY() == user.getY())) {
                    // Path complete or invalid
                    if (path != null) {
                        path.clear();
                    }
                    user.setWalking(false);
                    user.removeStatus("mv");
                    user.setPathRecalcNeeded(false);
                    
                    // Check if user is at door and should be removed
                    RoomModel model = room.getModel();
                    if (model != null && user.getX() == model.getDoorX() && 
                        user.getY() == model.getDoorY() && !toRemove.contains(user.getHabboId()) && 
                        !user.isBot()) {
                        toRemove.add(user.getHabboId());
                    }
                    
                    // Update user status (sitting, laying, etc.) when they reach destination
                    room.updateUserStatus(user);
                    
                    user.setUpdateNeeded(true);
                } else {
                    // Move to next step in path
                    // Path is ordered from goal to start, so we need to index backwards
                    int k = (path.size() - user.getPathStep()) - 1;
                    if (k < 0 || k >= path.size()) {
                        // Invalid path index, stop walking
                        path.clear();
                        user.setWalking(false);
                        user.setUpdateNeeded(true);
                        continue;
                    }
                    Coord nextStep = path.get(k);
                    user.setPathStep(user.getPathStep() + 1);
                    
                    int nextX = nextStep.getX();
                    int nextY = nextStep.getY();
                    
                    user.removeStatus("mv");
                    
                    boolean lastStep = false;
                    if (nextX == user.getGoalX() && nextY == user.getGoalY()) {
                        lastStep = true;
                    }
                    
                    if (room.canWalk(nextX, nextY, 0, lastStep) || user.isAllowOverride()) {
                        // Calculate absolute height at next position
                        double nextZ = 0.0;
                        if (room.getRoomMapping() != null) {
                            nextZ = room.getRoomMapping().sqAbsoluteHeight(nextX, nextY);
                        } else {
                            RoomModel model = room.getModel();
                            if (model != null && nextX >= 0 && nextX < model.getMapSizeX() && 
                                nextY >= 0 && nextY < model.getMapSizeY()) {
                                nextZ = model.getSqFloorHeight()[nextX][nextY];
                            }
                        }
                        
                        // Remove sit/lay statuses
                        user.removeStatus("lay");
                        user.removeStatus("sit");
                        
                        // Add movement status
                        String mvStatus = nextX + "," + nextY + "," + String.format("%.1f", nextZ).replace(',', '.');
                        user.addStatus("mv", mvStatus);
                        
                        // Calculate rotation
                        int newRot = Rotation.calculate(user.getX(), user.getY(), nextX, nextY);
                        user.setRotBody(newRot);
                        user.setRotHead(newRot);
                        
                        // Set next step position (will be applied in next cycle)
                        user.setSetStep(true);
                        if (room.getRoomMapping() != null) {
                            // Use BedMatrix to get actual position (for beds)
                            RoomModel model = room.getModel();
                            if (model != null && nextX >= 0 && nextX < model.getMapSizeX() && 
                                nextY >= 0 && nextY < model.getMapSizeY()) {
                                Coord bedCoord = room.getRoomMapping().getBedMatrix()[nextX][nextY];
                                user.setSetX(bedCoord.getX());
                                user.setSetY(bedCoord.getY());
                            } else {
                                user.setSetX(nextX);
                                user.setSetY(nextY);
                            }
                        } else {
                            user.setSetX(nextX);
                            user.setSetY(nextY);
                        }
                        user.setSetZ(nextZ);
                    } else {
                        // Can't walk to next step, stop walking
                        user.setWalking(false);
                    }
                    
                    user.setUpdateNeeded(true);
                }
            } else {
                // Not walking - remove mv status if present
                if (user.hasStatus("mv")) {
                    user.removeStatus("mv");
                    user.setUpdateNeeded(true);
                }
            }
            
            // Process bot AI ticks
            if (user.isBot() && user.getBotAI() != null) {
                user.getBotAI().onTimerTick();
            } else {
                userCount++; // Count non-bot users
            }
        }
        
        // Remove users that need to be removed
        for (Long habboId : toRemove) {
            com.uber.server.game.GameClient client = room.getGame().getClientManager().getClientByHabbo(habboId);
            if (client != null) {
                room.removeUserFromRoom(client, true, false);
            }
        }
        
        // Serialize and send status updates for users that need updating
        ServerMessage statusUpdates = room.serializeStatusUpdates(false);
        if (statusUpdates != null) {
            room.sendMessage(statusUpdates);
        }
        
        // Update room idle time
        if (userCount >= 1) {
            idleTimeRef[0] = 0;
        } else {
            idleTimeRef[0]++;
        }
        
        // Update Room's idleTime field
        room.setIdleTime(idleTimeRef[0]);
        
        // Request unload if room has been idle for 60 ticks (30 seconds)
        if (idleTimeRef[0] >= 60) {
            logger.debug("Requesting unload of idle room - ID: {}", room.getRoomId());
            if (room.getGame() != null && room.getGame().getRoomManager() != null) {
                room.getGame().getRoomManager().requestRoomUnload(room.getRoomId());
            }
        }
    }
    
    /**
     * Processes rollers in the room.
     * Rollers move items and users on top of them in the direction they face.
     * This matches Habbo Release 49 behavior.
     */
    private void processRollers() {
        List<RoomItem> rollers = new ArrayList<>();
        
        // Find all rollers in the room
        for (RoomItem item : items.values()) {
            if (isRoller(item)) {
                rollers.add(item);
            }
        }
        
        // Process each roller
        for (RoomItem roller : rollers) {
            int rollerX = roller.getX();
            int rollerY = roller.getY();
            int rollerRot = roller.getRot();
            
            // Calculate the direction the roller faces
            // Rotation: 0=North, 2=East, 4=South, 6=West
            int nextX = rollerX;
            int nextY = rollerY;
            
            switch (rollerRot) {
                case 0: // North
                    nextY = rollerY - 1;
                    break;
                case 2: // East
                    nextX = rollerX + 1;
                    break;
                case 4: // South
                    nextY = rollerY + 1;
                    break;
                case 6: // West
                    nextX = rollerX - 1;
                    break;
                default:
                    continue; // Invalid rotation, skip this roller
            }
            
            // Check if target tile is valid
            if (!room.validTile(nextX, nextY)) {
                continue;
            }
            
            // Move items on top of the roller
            List<RoomItem> itemsOnRoller = getItemsAtPosition(rollerX, rollerY);
            for (RoomItem itemOnRoller : itemsOnRoller) {
                // Don't move the roller itself
                if (itemOnRoller.getId() == roller.getId()) {
                    continue;
                }
                
                // Don't move items that are already being moved
                if (itemOnRoller.isUpdateNeeded()) {
                    continue;
                }
                
                // Check if target position is walkable/valid for the item
                if (canPlaceItemAt(itemOnRoller, nextX, nextY)) {
                    // Calculate new Z coordinate
                    double newZ = 0.0;
                    if (room.getRoomMapping() != null) {
                        newZ = room.getRoomMapping().sqAbsoluteHeight(nextX, nextY);
                    } else {
                        RoomModel model = room.getModel();
                        if (model != null && nextX >= 0 && nextX < model.getMapSizeX() && 
                            nextY >= 0 && nextY < model.getMapSizeY()) {
                            newZ = model.getSqFloorHeight()[nextX][nextY];
                        }
                    }
                    
                    // Move the item
                    itemOnRoller.setX(nextX);
                    itemOnRoller.setY(nextY);
                    itemOnRoller.setZ(newZ);
                    
                    // Update item position in database and send update to room
                    if (room.getGame() != null && room.getGame().getRoomItemRepository() != null) {
                        room.getGame().getRoomItemRepository().updatePosition(
                            itemOnRoller.getId(), nextX, nextY, newZ, itemOnRoller.getRot());
                    }
                    
                    // Send update message to room
                    ServerMessage message = new ServerMessage(95);
                    itemOnRoller.serialize(message);
                    room.sendMessage(message);
                    
                    // Regenerate collision matrix after moving item
                    if (room.getRoomMapping() != null) {
                        room.getRoomMapping().regenerateMatrix();
                    }
                }
            }
            
            // Move users on top of the roller
            List<RoomUser> usersOnRoller = getUsersAtPosition(rollerX, rollerY);
            for (RoomUser userOnRoller : usersOnRoller) {
                // Don't move users who are already walking or have a pending step
                if (userOnRoller.isWalking() || userOnRoller.isSetStep()) {
                    continue;
                }
                
                // Check if target position is walkable
                if (room.canWalk(nextX, nextY, 0, false)) {
                    // Calculate new Z coordinate
                    double newZ = 0.0;
                    if (room.getRoomMapping() != null) {
                        newZ = room.getRoomMapping().sqAbsoluteHeight(nextX, nextY);
                    } else {
                        RoomModel model = room.getModel();
                        if (model != null && nextX >= 0 && nextX < model.getMapSizeX() && 
                            nextY >= 0 && nextY < model.getMapSizeY()) {
                            newZ = model.getSqFloorHeight()[nextX][nextY];
                        }
                    }
                    
                    // Update UserMatrix - remove from old position
                    if (room.getRoomMapping() != null) {
                        room.getRoomMapping().setUserPosition(userOnRoller.getX(), userOnRoller.getY(), false);
                    }
                    
                    // Move the user
                    userOnRoller.setX(nextX);
                    userOnRoller.setY(nextY);
                    userOnRoller.setZ(newZ);
                    
                    // Calculate rotation based on movement direction
                    int newRot = Rotation.calculate(rollerX, rollerY, nextX, nextY);
                    userOnRoller.setRotBody(newRot);
                    userOnRoller.setRotHead(newRot);
                    
                    // Update UserMatrix - add to new position
                    if (room.getRoomMapping() != null) {
                        room.getRoomMapping().setUserPosition(nextX, nextY, true);
                    }
                    
                    // Mark user as needing update
                    userOnRoller.setUpdateNeeded(true);
                }
            }
        }
    }
    
    /**
     * Checks if an item is a roller.
     * Rollers are identified by their item name containing "roller".
     */
    private boolean isRoller(RoomItem item) {
        if (item == null || !item.isFloorItem()) {
            return false;
        }
        
        com.uber.server.game.items.Item baseItem = item.getBaseItem();
        if (baseItem == null) {
            return false;
        }
        
        String itemName = baseItem.getItemName();
        if (itemName == null) {
            return false;
        }
        
        // Check if item name contains "roller" (case-insensitive)
        return itemName.toLowerCase().contains("roller");
    }
    
    /**
     * Gets all items at a specific position.
     */
    private List<RoomItem> getItemsAtPosition(int x, int y) {
        List<RoomItem> itemsAtPos = new ArrayList<>();
        
        for (RoomItem item : items.values()) {
            if (!item.isFloorItem()) {
                continue;
            }
            
            // Check if item is at this position
            if (item.getX() == x && item.getY() == y) {
                itemsAtPos.add(item);
            } else {
                // Check if item occupies this position (multi-tile items)
                com.uber.server.game.items.Item baseItem = item.getBaseItem();
                if (baseItem != null) {
                    com.uber.server.game.rooms.mapping.RoomMapping mapping = room.getRoomMapping();
                    if (mapping != null) {
                        java.util.Map<Integer, com.uber.server.game.rooms.mapping.AffectedTile> affectedTiles = 
                            mapping.getAffectedTiles(baseItem.getLength(), baseItem.getWidth(), 
                                                    item.getX(), item.getY(), item.getRot());
                        for (com.uber.server.game.rooms.mapping.AffectedTile tile : affectedTiles.values()) {
                            if (tile.getX() == x && tile.getY() == y) {
                                itemsAtPos.add(item);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return itemsAtPos;
    }
    
    /**
     * Gets all users at a specific position.
     */
    private List<RoomUser> getUsersAtPosition(int x, int y) {
        List<RoomUser> usersAtPos = new ArrayList<>();
        
        for (RoomUser user : users.values()) {
            if (user.getX() == x && user.getY() == y) {
                usersAtPos.add(user);
            }
        }
        
        return usersAtPos;
    }
    
    /**
     * Checks if an item can be placed at a specific position.
     */
    private boolean canPlaceItemAt(RoomItem item, int x, int y) {
        if (!room.validTile(x, y)) {
            return false;
        }
        
        // Check if there's already an item at this position that blocks placement
        List<RoomItem> itemsAtPos = getItemsAtPosition(x, y);
        for (RoomItem existingItem : itemsAtPos) {
            // Allow stacking if both items can stack
            com.uber.server.game.items.Item baseItem = item.getBaseItem();
            com.uber.server.game.items.Item existingBaseItem = existingItem.getBaseItem();
            
            if (baseItem != null && existingBaseItem != null) {
                // If neither can stack, block placement
                if (!baseItem.canStack() && !existingBaseItem.canStack()) {
                    return false;
                }
            }
        }
        
        return true;
    }
}
