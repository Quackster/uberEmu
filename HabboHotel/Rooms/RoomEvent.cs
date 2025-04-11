using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.Messages;
using Uber.HabboHotel.GameClients;

namespace Uber.HabboHotel.Rooms
{
    class RoomEvent
    {
        public string Name;
        public string Description;
        public int Category;
        public SynchronizedCollection<string> Tags;
        public string StartTime;

        public uint RoomId;

        public RoomEvent(uint RoomId, string Name, string Description, int Category, SynchronizedCollection<string> Tags)
        {
            this.RoomId = RoomId;
            this.Name = Name;
            this.Description = Description;
            this.Category = Category;
            this.Tags = Tags;
            this.StartTime = DateTime.Now.ToShortTimeString();
        }

        public ServerMessage Serialize(GameClient Session)
        {
            ServerMessage Message = new ServerMessage(370);
            Message.AppendStringWithBreak(Session.GetHabbo().Id + "");
            Message.AppendStringWithBreak(Session.GetHabbo().Username);
            Message.AppendStringWithBreak(RoomId + "");
            Message.AppendInt32(Category);
            Message.AppendStringWithBreak(Name);
            Message.AppendStringWithBreak(Description);
            Message.AppendStringWithBreak(StartTime);
            Message.AppendInt32(Tags.Count);

            foreach (string Tag in Tags)
            {
                Message.AppendStringWithBreak(Tag);
            }

            return Message;
        }
    }
}
