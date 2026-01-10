package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for UserObjectEvent (ID 5).
 * Sends user profile information to the client.
 */
public class UserObjectComposer extends OutgoingMessageComposer {
    private final long userId;
    private final String username;
    private final String look;
    private final String gender;
    private final String motto;
    private final String realName;
    private final int respect;
    private final int dailyRespectPoints;
    private final int dailyPetRespectPoints;
    
    public UserObjectComposer(long userId, String username, String look, String gender,
                                   String motto, String realName, int respect,
                                   int dailyRespectPoints, int dailyPetRespectPoints) {
        this.userId = userId;
        this.username = username != null ? username : "";
        this.look = look != null ? look : "";
        this.gender = gender != null ? gender : "M";
        this.motto = motto != null ? motto : "";
        this.realName = realName != null ? realName : "";
        this.respect = respect;
        this.dailyRespectPoints = dailyRespectPoints;
        this.dailyPetRespectPoints = dailyPetRespectPoints;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(5); // _events[5] = UserObjectEvent
        msg.appendStringWithBreak(String.valueOf(userId));
        msg.appendStringWithBreak(username);
        msg.appendStringWithBreak(look);
        msg.appendStringWithBreak(gender.toUpperCase());
        msg.appendStringWithBreak(motto);
        msg.appendStringWithBreak(realName);
        msg.appendInt32(0);
        msg.appendStringWithBreak("");
        msg.appendInt32(0);
        msg.appendInt32(0);
        msg.appendInt32(respect);
        msg.appendInt32(dailyRespectPoints);
        msg.appendInt32(dailyPetRespectPoints);
        return msg;
    }
}
