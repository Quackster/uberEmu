package com.uber.server.game.bots;

import com.uber.server.game.GameClient;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomModel;
import com.uber.server.game.rooms.RoomUser;
import com.uber.server.game.pathfinding.Rotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Pet bot AI implementation.
 */
public class PetBot extends BotAI {
    private static final Logger logger = LoggerFactory.getLogger(PetBot.class);
    
    private int speechTimer;
    private int actionTimer;
    private final Random random;
    
    public PetBot(int virtualId) {
        this.random = new Random((virtualId ^ 2) + (int) System.currentTimeMillis());
        this.speechTimer = random.nextInt(240) + 10; // 10-250
        this.actionTimer = random.nextInt(20) + 10; // 10-30
    }
    
    @Override
    public void onSelfEnterRoom() {
        RoomUser botUser = getRoomUser();
        if (botUser != null) {
            botUser.chat(null, "*drool over master*", 0);
        }
    }
    
    @Override
    public void onSelfLeaveRoom(boolean kicked) {
        // Pet doesn't do anything special on leave
    }
    
    @Override
    public void onUserEnterRoom(RoomUser user) {
        RoomUser botUser = getRoomUser();
        if (botUser == null || botUser.getPetData() == null || user == null || user.getClient() == null) {
            return;
        }
        
        com.uber.server.game.pets.Pet petData = botUser.getPetData();
        GameClient userClient = user.getClient();
        
        if (userClient.getHabbo() != null && 
            userClient.getHabbo().getUsername().toLowerCase().equals(petData.getOwnerName().toLowerCase())) {
            botUser.chat(null, "*drool over master*", 0);
        }
    }
    
    @Override
    public void onUserLeaveRoom(GameClient client) {
        // Pet doesn't react to users leaving
    }
    
    @Override
    public void onUserSay(RoomUser user, String message) {
        RoomUser botUser = getRoomUser();
        if (botUser == null || botUser.getPetData() == null || user == null || message == null) {
            return;
        }
        
        com.uber.server.game.pets.Pet petData = botUser.getPetData();
        String petName = petData.getName();
        
        if (petName == null) {
            return;
        }
        
        String lowerMessage = message.toLowerCase();
        String lowerPetName = petName.toLowerCase();
        
        if (lowerMessage.equals(lowerPetName)) {
            // Pet looks at user
            botUser.setRot(Rotation.calculate(botUser.getX(), botUser.getY(), user.getX(), user.getY()));
            return;
        }
        
        if (lowerMessage.startsWith(lowerPetName + " ")) {
            // Pet is confused by command
            botUser.chat(null, "*confused*", 0);
        }
    }
    
    @Override
    public void onUserShout(RoomUser user, String message) {
        // Pet doesn't react to shouts
    }
    
    @Override
    public void onTimerTick() {
        RoomBot botData = getBotData();
        RoomUser botUser = getRoomUser();
        Room room = getRoom();
        
        if (botData == null || botUser == null || room == null) {
            return;
        }
        
        // Handle speech timer
        if (speechTimer <= 0) {
            RandomSpeech speech = botData.getRandomSpeech();
            if (speech != null) {
                botUser.chat(null, speech.getMessage(), speech.isShout() ? 1 : 0);
            }
            
            speechTimer = random.nextInt(290) + 10; // 10-300
        } else {
            speechTimer--;
        }
        
        // Handle action timer (movement)
        if (actionTimer <= 0) {
            RoomModel model = room.getModel();
            if (model != null) {
                int randomX = random.nextInt(model.getMapSizeX());
                int randomY = random.nextInt(model.getMapSizeY());
                botUser.moveTo(randomX, randomY);
            }
            
            actionTimer = random.nextInt(29) + 1; // 1-30
        } else {
            actionTimer--;
        }
    }
}
