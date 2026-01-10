package com.uber.server.handlers.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.util.AntiMutant;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for updating user look (message ID 44).
 */
public class UpdateLookHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(UpdateLookHandler.class);
    private final Game game;
    
    public UpdateLookHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String gender = message.popFixedString();
        String look = message.popFixedString();
        
        com.uber.server.event.packet.user.ChangeLooksEvent event = new com.uber.server.event.packet.user.ChangeLooksEvent(client, message, gender, look);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        gender = event.getGender();
        look = event.getFigure();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        // Check mutant penalty
        if (habbo.isMutantPenalty()) {
            client.sendNotif("Because of a penalty or restriction on your account, you are not allowed to change your look.");
            return;
        }
        
        if (gender == null) {
            return;
        }
        gender = gender.toUpperCase();
        
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
        var figureComposer = new com.uber.server.messages.outgoing.users.UserChangeComposer(
            -1, habbo.getLook(), habbo.getGender(), habbo.getMotto());
        client.sendMessage(figureComposer.compose());
        
        // Update room if user is in a room
        if (habbo.isInRoom() && game.getRoomManager() != null) {
            com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
            if (room != null) {
                com.uber.server.game.rooms.RoomUser roomUser = room.getRoomUserByHabbo(habbo.getId());
                if (roomUser != null) {
                    var roomFigureComposer = new com.uber.server.messages.outgoing.users.UserChangeComposer(
                        roomUser.getVirtualId(), habbo.getLook(), habbo.getGender(), habbo.getMotto());
                    room.sendMessage(roomFigureComposer.compose());
                }
            }
        }
    }
}
