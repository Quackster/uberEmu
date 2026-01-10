package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for UserFigureUpdateMessageEvent (ID 266).
 * Sends user figure/look update to the client.
 */
public class UserChangeComposer extends OutgoingMessageComposer {
    private final int virtualId; // -1 for self, otherwise virtual ID for room user
    private final String look;
    private final String gender;
    private final String motto;
    
    public UserChangeComposer(int virtualId, String look, String gender, String motto) {
        this.virtualId = virtualId;
        this.look = look;
        this.gender = gender;
        this.motto = motto;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(266); // _events[266] = UserFigureUpdateMessageEvent
        msg.appendInt32(virtualId);
        msg.appendStringWithBreak(look);
        msg.appendStringWithBreak(gender);
        msg.appendStringWithBreak(motto);
        return msg;
    }
}
