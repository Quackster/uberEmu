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
 * Guide bot AI implementation.
 */
public class GuideBot extends BotAI {
    private static final Logger logger = LoggerFactory.getLogger(GuideBot.class);
    
    private int speechTimer;
    private int actionTimer;
    private final Random random;
    
    public GuideBot() {
        this.random = new Random();
        this.speechTimer = 0;
        this.actionTimer = 0;
    }
    
    @Override
    public void onSelfEnterRoom() {
        RoomUser botUser = getRoomUser();
        if (botUser == null) {
            return;
        }
        
        botUser.chat(null, "Hi and welcome to Uber! I am a bot Guide and I'm here to help you.", 0);
        botUser.chat(null, "This is your own room, you can always come back to room by clicking the nest icon on the left.", 0);
        botUser.chat(null, "If you want to explore the Habbo by yourself, click on the orange hotel icon on the left (we call it navigator).", 0);
        botUser.chat(null, "You will find cool rooms and fun events with other people in them, feel free to visit them.", 0);
        botUser.chat(null, "I can give you tips and hints on what to do here, just ask me a question :)", 0);
    }
    
    @Override
    public void onSelfLeaveRoom(boolean kicked) {
        // Guide bot doesn't do anything special on leave
    }
    
    @Override
    public void onUserEnterRoom(RoomUser user) {
        // Guide bot doesn't react to users entering
    }
    
    @Override
    public void onUserLeaveRoom(GameClient client) {
        Room room = getRoom();
        RoomUser botUser = getRoomUser();
        
        if (room == null || botUser == null || client == null || client.getHabbo() == null) {
            return;
        }
        
        // If room owner leaves, remove the guide bot
        if (room.getData().getOwner().toLowerCase().equals(client.getHabbo().getUsername().toLowerCase())) {
            room.removeBot(botUser.getVirtualId(), false);
        }
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
            case "say" -> botUser.chat(null, response.getResponseText(), 0);
            case "shout" -> botUser.chat(null, response.getResponseText(), 1);
            case "whisper" -> {
                // Use WhisperMessageComposer (outgoing ID 25)
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
        // Guide bot doesn't react to shouts
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
            
            speechTimer = random.nextInt(150); // 0-150
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
            
            actionTimer = random.nextInt(30); // 0-30
        } else {
            actionTimer--;
        }
    }
}
