package com.uber.server.handlers.rooms;

import com.uber.server.event.packet.room.PlacePetEvent;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.game.pets.Pet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for placing a pet in a room (message ID 3002).
 */
public class PlacePetHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(PlacePetHandler.class);
    private final Game game;
    
    public PlacePetHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long petId = message.popWiredUInt();
        int x = message.popWiredInt32();
        int y = message.popWiredInt32();
        
        PlacePetEvent event = new PlacePetEvent(client, message, petId, x, y);
        Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        petId = event.getPetId();
        x = event.getX();
        y = event.getY();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        // Check if pets are allowed or user has rights
        if (!room.getData().isAllowPets() && !room.checkRights(client, true)) {
            return;
        }
        
        Pet pet = habbo.getInventoryComponent().getPetObject(petId);
        
        if (pet == null || pet.isPlacedInRoom()) {
            return;
        }
        
        // Validate position
        if (!room.canWalk(x, y, 0.0, true)) {
            return;
        }
        
        // Check pet count limit
        if (room.getPetCount() >= com.uber.server.game.rooms.RoomManager.MAX_PETS_PER_ROOM) {
            client.sendNotif("There are too many pets in this room. A room may only contain up to " + 
                           com.uber.server.game.rooms.RoomManager.MAX_PETS_PER_ROOM + " pets.");
            return;
        }
        
        // Deploy pet in room
        com.uber.server.game.rooms.RoomUser petUser = room.deployPet(pet, x, y);
        if (petUser == null) {
            return;
        }
        
        // If room owner, update pet in database
        if (room.checkRights(client, true)) {
            habbo.getInventoryComponent().movePetToRoom(pet.getPetId(), room.getRoomId());
        }
    }
}
