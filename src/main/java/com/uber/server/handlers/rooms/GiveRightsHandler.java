package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for giving room rights (message ID 96).
 */
public class GiveRightsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GiveRightsHandler.class);
    private final Game game;
    
    public GiveRightsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long userId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.GiveRightsEvent event = new com.uber.server.event.packet.room.GiveRightsEvent(client, message, userId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        userId = event.getUserId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        
        com.uber.server.game.rooms.RoomUser roomUser = room.getRoomUserByHabbo(userId);
        if (roomUser == null || roomUser.isBot()) {
            return;
        }
        
        if (room.hasRights(userId)) {
            client.sendNotif("User already has rights! (There appears to be a bug with the rights button, we are looking into it - for now rely on 'Advanced settings')");
            return;
        }
        
        if (room.addRight(userId)) {
            ServerMessage response = new ServerMessage(510);
            response.appendUInt(room.getRoomId());
            response.appendUInt(userId);
            
            // Get username
            String username = "";
            GameClient userClient = game.getClientManager().getClientByHabbo(userId);
            if (userClient != null && userClient.getHabbo() != null) {
                username = userClient.getHabbo().getUsername();
            } else {
                username = game.getUserRepository().getRealName(userId);
                if (username == null) {
                    username = "";
                }
            }
            response.appendStringWithBreak(username);
            client.sendMessage(response);
            
            // Add status to room user
            roomUser.addStatus("roomControl", "");
            roomUser.setUpdateNeeded(true);
            
            // Notify user
            if (userClient != null) {
                userClient.sendMessage(new ServerMessage(42));
            }
        }
    }
}
