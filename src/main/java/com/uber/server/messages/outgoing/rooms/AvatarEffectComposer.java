package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for UserAvatarEffectMessageEvent (ID 485).
 * Sends user avatar effect status to the client.
 */
public class AvatarEffectComposer extends OutgoingMessageComposer {
    private final int virtualId;
    private final int effectId;
    
    public AvatarEffectComposer(int virtualId, int effectId) {
        this.virtualId = virtualId;
        this.effectId = effectId;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(485); // _events[485] = UserAvatarEffectMessageEvent
        msg.appendInt32(virtualId);
        msg.appendInt32(effectId);
        return msg;
    }
}
