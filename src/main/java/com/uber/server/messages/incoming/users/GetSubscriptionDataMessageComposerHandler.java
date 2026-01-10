package com.uber.server.messages.incoming.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.game.users.subscriptions.Subscription;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetSubscriptionDataMessageComposer (ID 26).
 * Processes subscription data requests from the client.
 */
public class GetSubscriptionDataMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetSubscriptionDataMessageComposerHandler.class);
    private final Game game;
    
    public GetSubscriptionDataMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.user.GetSubscriptionDataEvent event = new com.uber.server.event.packet.user.GetSubscriptionDataEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        String subscriptionId = message.popFixedString();
        if (subscriptionId == null || subscriptionId.isEmpty()) {
            return;
        }
        
        subscriptionId = subscriptionId.toLowerCase();
        
        com.uber.server.messages.ServerMessage response = new com.uber.server.messages.ServerMessage(7);
        response.appendStringWithBreak(subscriptionId);
        
        if (habbo.getSubscriptionManager() != null && 
            habbo.getSubscriptionManager().hasSubscription(subscriptionId)) {
            
            Subscription subscription = habbo.getSubscriptionManager().getSubscription(subscriptionId);
            if (subscription != null) {
                long expireTime = subscription.getExpireTime();
                long timeLeft = expireTime - TimeUtil.getUnixTimestamp();
                
                int totalDaysLeft = (int) Math.ceil(timeLeft / 86400.0);
                int monthsLeft = totalDaysLeft / 31;
                
                if (monthsLeft >= 1) {
                    monthsLeft--;
                }
                
                response.appendInt32(totalDaysLeft - (monthsLeft * 31));
                response.appendBoolean(true);
                response.appendInt32(monthsLeft);
            } else {
                response.appendInt32(0);
                response.appendBoolean(false);
                response.appendInt32(0);
            }
        } else {
            // No subscription - send three zeros
            response.appendInt32(0);
            response.appendBoolean(false);
            response.appendInt32(0);
        }
        
        var composer = new com.uber.server.messages.outgoing.users.ScrSendUserInfoComposer(response);
        client.sendMessage(composer.compose());
    }
}
