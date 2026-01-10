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
 * Handler for answering doorbell (message ID 98).
 */
public class AnswerDoorbellHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(AnswerDoorbellHandler.class);
    private final Game game;
    
    public AnswerDoorbellHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String username = message.popFixedString();
        byte[] resultBytes = message.readBytes(1);
        byte answer = (resultBytes != null && resultBytes.length > 0) ? resultBytes[0] : 0;
        
        com.uber.server.event.packet.room.AnswerDoorbellEvent event = new com.uber.server.event.packet.room.AnswerDoorbellEvent(
            client, message, username, answer);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        username = event.getUsername();
        answer = event.getAnswer();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client)) {
            return;
        }
        
        // Get client by username (name parameter is username, not ID)
        GameClient targetClient = game.getClientManager().getClientByHabbo(username);
        if (targetClient == null || targetClient.getHabbo() == null) {
            return;
        }
        
        // 65 = 'A' (accept), anything else = decline
        if (event.isAccept()) {
            targetClient.getHabbo().setLoadingChecksPassed(true);
            
            ServerMessage acceptMessage = new ServerMessage(41);
            targetClient.sendMessage(acceptMessage);
        } else {
            ServerMessage declineMessage = new ServerMessage(131);
            targetClient.sendMessage(declineMessage);
        }
    }
}
