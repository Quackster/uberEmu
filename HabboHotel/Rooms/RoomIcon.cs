using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.Messages;

namespace Uber.HabboHotel.Rooms
{
    class RoomIcon
    {
        public int BackgroundImage;
        public int ForegroundImage;
        public ConcurrentDictionary<int, int> Items;

        public RoomIcon(int BackgroundImage, int ForegroundImage, ConcurrentDictionary<int, int> Items)
        {
            this.BackgroundImage = BackgroundImage;
            this.ForegroundImage = ForegroundImage;
            this.Items = Items;
        }

        public void Serialize(ServerMessage Message)
        {
            Message.AppendInt32(BackgroundImage);
            Message.AppendInt32(ForegroundImage);
            Message.AppendInt32(Items.Count);

            foreach (KeyValuePair<int, int> Item in Items)
            {
                Message.AppendInt32(Item.Key);
                Message.AppendInt32(Item.Value);
            }
        }
    }
}
