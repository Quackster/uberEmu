package com.uber.server.handlers.users;

import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting user info (message ID 7).
 * Ported from Messages/Requests/Users.cs GetUserInfo()
 */
public class GetUserInfoHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetUserInfoHandler.class);
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        ServerMessage response = new ServerMessage(5);
        response.appendStringWithBreak(String.valueOf(habbo.getId()));
        response.appendStringWithBreak(habbo.getUsername());
        response.appendStringWithBreak(habbo.getLook());
        response.appendStringWithBreak(habbo.getGender().toUpperCase());
        response.appendStringWithBreak(habbo.getMotto());
        response.appendStringWithBreak(habbo.getRealName());
        response.appendInt32(0);
        response.appendStringWithBreak("");
        response.appendInt32(0);
        response.appendInt32(0);
        response.appendInt32(habbo.getRespect());
        response.appendInt32(habbo.getDailyRespectPoints());
        response.appendInt32(habbo.getDailyPetRespectPoints());
        
        client.sendMessage(response);
    }
}
