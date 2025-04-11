using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.HabboHotel.GameClients;
using Uber.HabboHotel.Rooms;

namespace Uber.HabboHotel.RoomBots
{
    abstract class BotAI
    {
        public int BaseId;
        private int RoomUserId;
        private uint RoomId;

        public BotAI() { }

        public void Init(int BaseId, int RoomUserId, uint RoomId)
        {
            this.BaseId = BaseId;
            this.RoomUserId = RoomUserId;
            this.RoomId = RoomId;
        }

        public Room GetRoom()
        {
            return UberEnvironment.GetGame().GetRoomManager().GetRoom(RoomId);
        }

        public RoomUser GetRoomUser()
        {
            return GetRoom().GetRoomUserByVirtualId(RoomUserId);
        }

        public RoomBot GetBotData()
        {
            return GetRoomUser().BotData;
        }

        public abstract void OnSelfEnterRoom();
        public abstract void OnSelfLeaveRoom(bool Kicked);
        public abstract void OnUserEnterRoom(RoomUser User);
        public abstract void OnUserLeaveRoom(GameClient Client);
        public abstract void OnUserSay(RoomUser User, string Message);
        public abstract void OnUserShout(RoomUser User, string Message);
        public abstract void OnTimerTick();
    }
}
