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
        
        // Process users and bots
        List<Long> toRemove = new ArrayList<>();
        int userCount = 0;
        
        for (RoomUser user : users.values()) {
            // Increment idle time
            user.incrementIdleTime();
            
            // Check if user should fall asleep (idle for 600 ticks = 300 seconds)
            if (!user.isAsleep() && user.getIdleTime() >= 600) {
                user.setAsleep(true);
                var sleepComposer = new com.uber.server.messages.outgoing.rooms.UserSleepingMessageEventComposer(
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
}
