package com.uber.server.handlers.rooms;

import com.uber.server.event.packet.room.GetPetInfoEvent;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.repository.PetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Handler for getting pet info (message ID 3001).
 */
public class GetPetInfoHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetPetInfoHandler.class);
    private final Game game;
    
    public GetPetInfoHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long petId = message.popWiredUInt();
        
        GetPetInfoEvent event = new GetPetInfoEvent(client, message, petId);
        Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        petId = event.getPetId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        // Get pet data from database
        PetRepository petRepository = game.getPetRepository();
        if (petRepository == null) {
            return;
        }
        
        Map<String, Object> petRow = petRepository.getPet(petId);
        if (petRow == null) {
            return;
        }
        
        // Generate Pet object and serialize info
        com.uber.server.game.pets.Pet pet = game.getCatalog().generatePetFromRow(petRow);
        if (pet != null) {
            client.sendMessage(pet.serializeInfo());
        }
    }
}
