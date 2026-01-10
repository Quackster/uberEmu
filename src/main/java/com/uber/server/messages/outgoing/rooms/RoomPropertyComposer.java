package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomDecorationMessageEvent (ID 46).
 * Sends room decoration (wallpaper/floor/landscape) message to the client.
 */
public class RoomPropertyComposer extends OutgoingMessageComposer {
    private final String decorationType; // "wallpaper", "floor", or "landscape"
    private final String decorationValue;
    
    public RoomPropertyComposer(String decorationType, String decorationValue) {
        this.decorationType = decorationType;
        this.decorationValue = decorationValue;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(46); // _events[46] = RoomDecorationMessageEvent
        msg.appendStringWithBreak(decorationType);
        msg.appendStringWithBreak(decorationValue);
        return msg;
    }
}
