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
 * Handler for getting room advertisement (message ID 182).
 */
public class GetAdvertisementHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetAdvertisementHandler.class);
    private final Game game;
    
    public GetAdvertisementHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.room.GetAdvertisementEvent event = new com.uber.server.event.packet.room.GetAdvertisementEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        com.uber.server.game.advertisements.RoomAdvertisement ad = 
            game.getAdvertisementManager().getRandomRoomAdvertisement();
        
        String adImage = "";
        String adLink = "";
        
        if (ad != null) {
            adImage = ad.getAdImage();
            adLink = ad.getAdLink();
            // Increment view count
            ad.onView();
        }
        
        var composer = new com.uber.server.messages.outgoing.rooms.InterstitialComposer(adImage, adLink);
        client.sendMessage(composer.compose());
    }
}
