package com.uber.server.game.bots;

import com.uber.server.game.GameClient;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomModel;
import com.uber.server.game.rooms.RoomUser;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Generic bot AI implementation.
 */
public class GenericBot extends BotAI {
    private static final Logger logger = LoggerFactory.getLogger(GenericBot.class);
    
    private int speechTimer;
    private int actionTimer;
    private final Random random;
    
    public GenericBot(int virtualId) {
        this.random = new Random((virtualId ^ 2) + (int) System.currentTimeMillis());
        this.speechTimer = random.nextInt(240) + 10; // 10-250
        this.actionTimer = random.nextInt(20) + 10; // 10-30
    }
    
    @Override
    public void onSelfEnterRoom() {
        // Generic bots don't do anything special on entry
    }
    
    @Override
    public void onSelfLeaveRoom(boolean kicked) {
        // Generic bots don't do anything special on leave
    }
    
    @Override
    public void onUserEnterRoom(RoomUser user) {
        // Generic bots don't react to users entering
    }
    
    @Override
    public void onUserLeaveRoom(GameClient client) {
        // Generic bots don't react to users leaving
    }
    
    @Override
    public void onUserSay(RoomUser user, String message) {
        Room room = getRoom();
        RoomUser botUser = getRoomUser();
        RoomBot botData = getBotData();
        
        if (room == null || botUser == null || botData == null || user == null || message == null) {
            return;
        }
        
        // Check distance (tile distance > 8, skip)
        if (room.tileDistance(botUser.getX(), botUser.getY(), user.getX(), user.getY()) > 8) {
            return;
        }
        
        BotResponse response = botData.getResponse(message);
        if (response == null) {
            return;
        }
        
        String responseType = response.getResponseType().toLowerCase();
        switch (responseType) {
            case "say" -> {
                botUser.chat(null, response.getResponseText(), 0); // 0 = talk
            }
            case "shout" -> {
                botUser.chat(null, response.getResponseText(), 1); // 1 = shout
            }
            case "whisper" -> {
                var whisperComposer = new com.uber.server.messages.outgoing.rooms.WhisperMessageComposer(
                    botUser.getVirtualId(), response.getResponseText(), 0);
                GameClient userClient = user.getClient();
                if (userClient != null) {
                    userClient.sendMessage(whisperComposer.compose());
                }
            }
        }
        
        if (response.getServeId() >= 1) {
            user.carryItem(response.getServeId());
        }
    }
    
    @Override
    public void onUserShout(RoomUser user, String message) {
        if (random.nextInt(11) >= 5) { // 50% chance
            RoomUser botUser = getRoomUser();
            if (botUser != null) {
                botUser.chat(null, "There's no need to shout!", 1); // 1 = shout
            }
        }
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
                String walkMode = botData.getWalkingMode().toLowerCase();
                int randomX = 0;
                int randomY = 0;
                
                        switch (walkMode) {
                    case "freeroam" -> {
                        randomX = random.nextInt(model.getMapSizeX());
                        randomY = random.nextInt(model.getMapSizeY());
                        botUser.moveTo(randomX, randomY);
                    }
                    case "specified_range" -> {
                        randomX = random.nextInt(botData.getMaxX() - botData.getMinX() + 1) + botData.getMinX();
                        randomY = random.nextInt(botData.getMaxY() - botData.getMinY() + 1) + botData.getMinY();
                        botUser.moveTo(randomX, randomY);
                    }
                    default -> {
                        // Do nothing - bot stands still
                    }
                }
            }
            
            actionTimer = random.nextInt(29) + 1; // 1-30
        } else {
            actionTimer--;
        }
    }
}
