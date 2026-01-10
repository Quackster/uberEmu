package com.uber.server.handlers.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.util.AntiMutant;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for updating user look (message ID 44).
 * Ported from Messages/Requests/Users.cs ChangeLook()
 */
public class UpdateLookHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(UpdateLookHandler.class);
    private final Game game;
    
    public UpdateLookHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        // Check mutant penalty
        if (habbo.isMutantPenalty()) {
            client.sendNotif("Because of a penalty or restriction on your account, you are not allowed to change your look.");
            return;
        }
        
        // Read gender and look
        String gender = message.popFixedString();
        if (gender == null) {
            return;
        }
        gender = gender.toUpperCase();
        
        String look = message.popFixedString();
        if (look == null) {
            return;
        }
        
        // Filter injection characters and validate look
        look = StringUtil.filterInjectionChars(look);
        if (!AntiMutant.validateLook(look, gender)) {
            return;
        }
        
        // Update look and gender
        habbo.setLook(look);
        habbo.setGender(gender.toLowerCase());
        
        // Update in database
        if (!game.getUserRepository().updateLook(habbo.getId(), look, gender)) {
            logger.warn("Failed to update look for user {}", habbo.getId());
            return;
        }
        
        // Send response
        ServerMessage response = new ServerMessage(266);
        response.appendInt32(-1);
        response.appendStringWithBreak(habbo.getLook());
        response.appendStringWithBreak(habbo.getGender());
        response.appendStringWithBreak(habbo.getMotto());
        client.sendMessage(response);
        
        // Update room if user is in a room
        if (habbo.isInRoom() && game.getRoomManager() != null) {
            com.uber.server.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
            if (room != null) {
                com.uber.server.rooms.RoomUser roomUser = room.getRoomUserByHabbo(habbo.getId());
                if (roomUser != null) {
                    ServerMessage roomUpdate = new ServerMessage(266);
                    roomUpdate.appendInt32(roomUser.getVirtualId());
                    roomUpdate.appendStringWithBreak(habbo.getLook());
                    roomUpdate.appendStringWithBreak(habbo.getGender());
                    roomUpdate.appendStringWithBreak(habbo.getMotto());
                    room.sendMessage(roomUpdate);
                }
            }
        }
    }
}
