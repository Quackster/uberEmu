package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.pets.Pet;
import com.uber.server.game.rooms.RoomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for respecting a pet (message ID 3005).
 */
public class RespectPetHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(RespectPetHandler.class);
    private final Game game;
    
    public RespectPetHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long petId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.RespectPetEvent event = new com.uber.server.event.packet.room.RespectPetEvent(client, message, petId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        petId = event.getPetId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || room.isPublicRoom()) {
            return;
        }
        
        // Check if pets are allowed or user has rights
        if (!room.getData().isAllowPets() && !room.checkRights(client, true)) {
            return;
        }
        RoomUser petUser = room.getPet(petId);
        
        if (petUser == null || petUser.getPetData() == null || petUser.getPetData().getOwnerId() == habbo.getId()) {
            return; // Can't respect own pet
        }
        
        Pet pet = petUser.getPetData();
        
        // Check daily pet respect points
        if (habbo.getDailyPetRespectPoints() <= 0) {
            return;
        }
        
        // Decrement daily pet respect points
        habbo.setDailyPetRespectPoints(habbo.getDailyPetRespectPoints() - 1);
        game.getUserRepository().updateDailyPetRespectPoints(habbo.getId(), habbo.getDailyPetRespectPoints());
        
        // Give respect to pet
        pet.onRespect();
        
        // Send respect message to room
        ServerMessage respectMsg = new ServerMessage(610);
        respectMsg.appendUInt(pet.getPetId());
        respectMsg.appendInt32(petUser.getVirtualId());
        room.sendMessage(respectMsg);
    }
}
