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
 */
public class GetUserInfoHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetUserInfoHandler.class);
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        var composer = new com.uber.server.messages.outgoing.users.UserObjectEventComposer(
            habbo.getId(), habbo.getUsername(), habbo.getLook(), habbo.getGender(),
            habbo.getMotto(), habbo.getRealName(), habbo.getRespect(),
            habbo.getDailyRespectPoints(), habbo.getDailyPetRespectPoints());
        client.sendMessage(composer.compose());
    }
}
