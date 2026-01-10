package com.uber.server.misc;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.items.RoomItem;
import com.uber.server.game.rooms.RoomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles chat commands (commands starting with ":").
 */
public class ChatCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatCommandHandler.class);
    
    /**
     * Parses and executes a chat command.
     * @param session GameClient session
     * @param input Command input (without the leading ":")
     * @return True if command was handled, false otherwise
     */
    public static boolean parse(GameClient session, String input) {
        if (session == null || input == null || input.isEmpty()) {
            return false;
        }
        
        String[] params = input.split(" ");
        if (params.length == 0) {
            return false;
        }
        
        String targetUser = null;
        GameClient targetClient = null;
        Room targetRoom = null;
        RoomUser targetRoomUser = null;
        
        Habbo habbo = session.getHabbo();
        if (habbo == null) {
            return false;
        }
        
        // Get Game instance from GameEnvironment
        Game game;
        try {
            game = com.uber.server.game.GameEnvironment.getInstance().getGame();
        } catch (Exception e) {
            logger.warn("Could not get Game instance: {}", e.getMessage());
            return false;
        }
        
        if (game == null) {
            return false;
        }
        
        try {
            String command = params[0].toLowerCase();
            
            // Debugging/Development commands
            switch (command) {
                case "update_inventory":
                    if (habbo.hasFuse("fuse_admin")) {
                        habbo.getInventoryComponent().updateItems(true);
                        return true;
                    }
                    return false;
                
                case "update_bots":
                    if (habbo.hasFuse("fuse_admin")) {
                        // BotManager reload functionality not yet implemented
                        session.sendNotif("BotManager reload not yet implemented.");
                        return true;
                    }
                    return false;
                
                case "update_catalog":
                    if (habbo.hasFuse("fuse_admin")) {
                        game.getCatalog().initialize();
                        game.getClientManager().broadcastMessage(new ServerMessage(441));
                        return true;
                    }
                    return false;
                
                case "idletime":
                    if (habbo.hasFuse("fuse_admin")) {
                        targetRoom = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
                        if (targetRoom != null) {
                            targetRoomUser = targetRoom.getRoomUserByHabbo(habbo.getId());
                            if (targetRoomUser != null) {
                                targetRoomUser.setIdleTime(600);
                                return true;
                            }
                        }
                    }
                    return false;
                
                case "t":
                    if (habbo.hasFuse("fuse_admin")) {
                        targetRoom = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
                        if (targetRoom == null) {
                            return false;
                        }
                        
                        targetRoomUser = targetRoom.getRoomUserByHabbo(habbo.getId());
                        if (targetRoomUser == null) {
                            return false;
                        }
                        
                        session.sendNotif(String.format("X: %d - Y: %d - Z: %.1f - Rot: %d",
                                targetRoomUser.getX(), targetRoomUser.getY(), targetRoomUser.getZ(),
                                targetRoomUser.getRotBody()));
                        return true;
                    }
                    return false;
                
                case "override":
                    if (habbo.hasFuse("fuse_admin")) {
                        targetRoom = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
                        if (targetRoom == null) {
                            return false;
                        }
                        
                        targetRoomUser = targetRoom.getRoomUserByHabbo(habbo.getId());
                        if (targetRoomUser == null) {
                            return false;
                        }
                        
                        if (targetRoomUser.isAllowOverride()) {
                            targetRoomUser.setAllowOverride(false);
                            session.sendNotif("Walking override disabled.");
                        } else {
                            targetRoomUser.setAllowOverride(true);
                            session.sendNotif("Walking override enabled.");
                        }
                        return true;
                    }
                    return false;
                
                case "drink":
                    if (habbo.hasFuse("fuse_admin")) {
                        targetRoom = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
                        if (targetRoom == null) {
                            return false;
                        }
                        
                        targetRoomUser = targetRoom.getRoomUserByHabbo(habbo.getId());
                        if (targetRoomUser == null) {
                            return false;
                        }
                        
                        try {
                            if (params.length > 1) {
                                int itemId = Integer.parseInt(params[1]);
                                targetRoomUser.carryItem(itemId);
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid item ID
                        }
                        return true;
                    }
                    return false;
                
                case "update_defs":
                    if (habbo.hasFuse("fuse_admin")) {
                        game.getItemManager().loadItems();
                        session.sendNotif("Item definitions reloaded successfully.");
                        return true;
                    }
                    return false;
                
                // General commands
                case "pickall":
                    targetRoom = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
                    if (targetRoom != null && targetRoom.checkRights(session, true)) {
                        List<RoomItem> toRemove = new ArrayList<>(targetRoom.getItems().values());
                        
                        for (RoomItem item : toRemove) {
                            targetRoom.removeFurniture(session, item.getId());
                            habbo.getInventoryComponent().addItem(item.getId(), item.getBaseItem().getId(), item.getExtraData());
                        }
                        
                        habbo.getInventoryComponent().updateItems(true);
                        return true;
                    }
                    return false;
                
                case "commands":
                case "help":
                case "info":
                case "details":
                case "about":
                    session.sendNotif("This server is proudly powered by uberEmulator.\nCopyright (c) 2009, Roy 'Meth0d'\n\nhttp://www.uberemu.info", "http://www.uberemu.info");
                    return true;
                
                case "empty":
                    habbo.getInventoryComponent().clearItems();
                    return true;
                
                // Moderation commands
                case "bustest":
                    if (habbo.hasFuse("fuse_admin")) {
                        ServerMessage message = new ServerMessage(79);
                        message.appendStringWithBreak("This is a test poll!");
                        message.appendInt32(5);
                        message.appendInt32(133333);
                        message.appendStringWithBreak("Some option");
                        message.appendInt32(2);
                        message.appendStringWithBreak("Don't select me");
                        message.appendInt32(3);
                        message.appendStringWithBreak("Meh!");
                        message.appendInt32(4);
                        message.appendStringWithBreak("............");
                        message.appendInt32(5);
                        message.appendStringWithBreak("FUKKEN RAGE");
                        
                        targetRoom = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
                        if (targetRoom != null) {
                            targetRoom.sendMessage(message);
                        }
                        return true;
                    }
                    break;
                
                case "invisible":
                    if (habbo.hasFuse("fuse_admin")) {
                        if (habbo.isSpectatorMode()) {
                            habbo.setSpectatorMode(false);
                            session.sendNotif("Spectator mode disabled. Reload the room to apply changes.");
                        } else {
                            habbo.setSpectatorMode(true);
                            session.sendNotif("Spectator mode enabled. Reload the room to apply changes.");
                        }
                        return true;
                    }
                    return false;
                
                case "ban":
                    if (habbo.hasFuse("fuse_ban")) {
                        if (params.length < 2) {
                            session.sendNotif("Usage: :ban <username> [time] [reason]");
                            return true;
                        }
                        
                        targetUser = params[1];
                        targetClient = game.getClientManager().getClientByHabbo(targetUser);
                        
                        if (targetClient == null) {
                            session.sendNotif("User not found.");
                            return true;
                        }
                        
                        if (targetClient.getHabbo().getRank() >= habbo.getRank()) {
                            session.sendNotif("You are not allowed to ban that user.");
                            return true;
                        }
                        
                        int banTime = 0;
                        if (params.length > 2) {
                            try {
                                banTime = Integer.parseInt(params[2]);
                            } catch (NumberFormatException e) {
                                // Ignore invalid ban time
                            }
                        }
                        
                        if (banTime <= 600) {
                            session.sendNotif("Ban time is in seconds and must be at least 600 seconds (ten minutes). For more specific preset ban times, use the mod tool.");
                        }
                        
                        String banReason = mergeParams(params, 3);
                        game.getBanManager().banUser(targetClient, habbo.getUsername(), banTime, banReason, false);
                        return true;
                    }
                    return false;
                
                case "superban":
                    if (habbo.hasFuse("fuse_superban")) {
                        if (params.length < 2) {
                            session.sendNotif("Usage: :superban <username> [reason]");
                            return true;
                        }
                        
                        targetUser = params[1];
                        targetClient = game.getClientManager().getClientByHabbo(targetUser);
                        
                        if (targetClient == null) {
                            session.sendNotif("User not found.");
                            return true;
                        }
                        
                        if (targetClient.getHabbo().getRank() >= habbo.getRank()) {
                            session.sendNotif("You are not allowed to ban that user.");
                            return true;
                        }
                        
                        String superBanReason = mergeParams(params, 2);
                        game.getBanManager().banUser(targetClient, habbo.getUsername(), 360000000, superBanReason, false);
                        return true;
                    }
                    return false;
                
                case "roomkick":
                    if (habbo.hasFuse("fuse_roomkick")) {
                        targetRoom = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
                        if (targetRoom == null) {
                            return false;
                        }
                        
                        boolean genericMsg = true;
                        String modMsg = mergeParams(params, 1);
                        
                        if (modMsg != null && !modMsg.isEmpty()) {
                            genericMsg = false;
                        }
                        
                        for (RoomUser roomUser : targetRoom.getUsers().values()) {
                            if (roomUser.isBot()) {
                                continue;
                            }
                            
                            GameClient userClient = roomUser.getClient();
                            if (userClient != null && userClient.getHabbo() != null) {
                                if (userClient.getHabbo().getRank() >= habbo.getRank()) {
                                    continue;
                                }
                                
                                if (!genericMsg) {
                                    userClient.sendNotif("You have been kicked by a moderator: " + modMsg);
                                }
                                
                                targetRoom.removeUserFromRoom(userClient, true, genericMsg);
                            }
                        }
                        return true;
                    }
                    return false;
                
                case "roomalert":
                    if (habbo.hasFuse("fuse_roomalert")) {
                        targetRoom = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
                        if (targetRoom == null) {
                            return false;
                        }
                        
                        String msg = mergeParams(params, 1);
                        
                        for (RoomUser roomUser : targetRoom.getUsers().values()) {
                            if (roomUser.isBot()) {
                                continue;
                            }
                            
                            GameClient userClient = roomUser.getClient();
                            if (userClient != null) {
                                userClient.sendNotif(msg);
                            }
                        }
                        return true;
                    }
                    return false;
                
                case "mute":
                    if (habbo.hasFuse("fuse_mute")) {
                        if (params.length < 2) {
                            session.sendNotif("Usage: :mute <username>");
                            return true;
                        }
                        
                        targetUser = params[1];
                        targetClient = game.getClientManager().getClientByHabbo(targetUser);
                        
                        if (targetClient == null || targetClient.getHabbo() == null) {
                            session.sendNotif("Could not find user: " + targetUser);
                            return true;
                        }
                        
                        if (targetClient.getHabbo().getRank() >= habbo.getRank()) {
                            session.sendNotif("You are not allowed to (un)mute that user.");
                            return true;
                        }
                        
                        targetClient.getHabbo().mute();
                        return true;
                    }
                    return false;
                
                case "unmute":
                    if (habbo.hasFuse("fuse_mute")) {
                        if (params.length < 2) {
                            session.sendNotif("Usage: :unmute <username>");
                            return true;
                        }
                        
                        targetUser = params[1];
                        targetClient = game.getClientManager().getClientByHabbo(targetUser);
                        
                        if (targetClient == null || targetClient.getHabbo() == null) {
                            session.sendNotif("Could not find user: " + targetUser);
                            return true;
                        }
                        
                        if (targetClient.getHabbo().getRank() >= habbo.getRank()) {
                            session.sendNotif("You are not allowed to (un)mute that user.");
                            return true;
                        }
                        
                        targetClient.getHabbo().unmute();
                        return true;
                    }
                    return false;
                
                case "alert":
                    if (habbo.hasFuse("fuse_alert")) {
                        if (params.length < 2) {
                            session.sendNotif("Usage: :alert <username> [message]");
                            return true;
                        }
                        
                        targetUser = params[1];
                        targetClient = game.getClientManager().getClientByHabbo(targetUser);
                        
                        if (targetClient == null) {
                            session.sendNotif("Could not find user: " + targetUser);
                            return true;
                        }
                        
                        String alertMsg = mergeParams(params, 2);
                        targetClient.sendNotif(alertMsg, habbo.hasFuse("fuse_admin"));
                        return true;
                    }
                    return false;
                
                case "softkick":
                case "kick":
                    if (habbo.hasFuse("fuse_kick")) {
                        if (params.length < 2) {
                            session.sendNotif("Usage: :kick <username> [reason]");
                            return true;
                        }
                        
                        targetUser = params[1];
                        targetClient = game.getClientManager().getClientByHabbo(targetUser);
                        
                        if (targetClient == null) {
                            session.sendNotif("Could not find user: " + targetUser);
                            return true;
                        }
                        
                        if (habbo.getRank() <= targetClient.getHabbo().getRank()) {
                            session.sendNotif("You are not allowed to kick that user.");
                            return true;
                        }
                        
                        if (targetClient.getHabbo().getCurrentRoomId() < 1) {
                            session.sendNotif("That user is not in a room and can not be kicked.");
                            return true;
                        }
                        
                        targetRoom = game.getRoomManager().getRoom(targetClient.getHabbo().getCurrentRoomId());
                        if (targetRoom == null) {
                            return true;
                        }
                        
                        targetRoom.removeUserFromRoom(targetClient, true, false);
                        
                        String kickReason = mergeParams(params, 2);
                        if (kickReason != null && !kickReason.isEmpty()) {
                            targetClient.sendNotif("A moderator has kicked you from the room for the following reason: " + kickReason);
                        } else {
                            targetClient.sendNotif("A moderator has kicked you from the room.");
                        }
                        return true;
                    }
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error processing chat command: {}", e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * Merges parameters from a specific index onwards into a single string.
     * @param params Parameter array
     * @param start Starting index
     * @return Merged string, or empty string if start >= params.length
     */
    public static String mergeParams(String[] params, int start) {
        if (params == null || start >= params.length) {
            return "";
        }
        
        StringBuilder merged = new StringBuilder();
        for (int i = start; i < params.length; i++) {
            if (i > start) {
                merged.append(" ");
            }
            merged.append(params[i]);
        }
        
        return merged.toString();
    }
}
