package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomAdvertisementEvent (ID 258).
 * Sends room advertisement data to the client.
 */
public class InterstitialComposer extends OutgoingMessageComposer {
    private final String adImage;
    private final String adLink;
    
    public InterstitialComposer(String adImage, String adLink) {
        this.adImage = adImage != null ? adImage : "";
        this.adLink = adLink != null ? adLink : "";
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(258); // _events[258] = RoomAdvertisementEvent
        msg.appendStringWithBreak(adImage);
        msg.appendStringWithBreak(adLink);
        return msg;
    }
}
