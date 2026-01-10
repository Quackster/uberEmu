package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.game.pets.Pet;
import com.uber.server.game.rooms.RoomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for picking up a pet from room (message ID 3003).
 */
public class PickUpPetHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(PickUpPetHandler.class);
    private final Game game;
    
    public PickUpPetHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long petId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.PickUpPetEvent event = new com.uber.server.event.packet.room.PickUpPetEvent(client, message, petId);
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
        
        if (petUser == null || petUser.getPetData() == null || petUser.getPetData().getOwnerId() != habbo.getId()) {
            return;
        }
        
        com.uber.server.game.pets.Pet pet = petUser.getPetData();
        if (pet == null) {
            return;
        }
        
        // Add pet back to inventory
        habbo.getInventoryComponent().addPet(pet);
        
        // Remove pet from room
        room.removeBot(petUser.getVirtualId(), false);
    }
}
