using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.Messages;
using Uber.HabboHotel.GameClients;

namespace Uber.HabboHotel.Items
{
    class UserItem
    {
        public uint Id;
        public uint BaseItem;
        public string ExtraData;

        public UserItem(uint Id, uint BaseItem, string ExtraData)
        {
            this.Id = Id;
            this.BaseItem = BaseItem;
            this.ExtraData = ExtraData;
        }

        public void Serialize(ServerMessage Message, Boolean Inventory)
        {
            Message.AppendUInt(Id);
            Message.AppendInt32(0);
            Message.AppendStringWithBreak(GetBaseItem().Type.ToUpper());
            Message.AppendUInt(Id);
            Message.AppendInt32(GetBaseItem().SpriteId);

            if (GetBaseItem().Name.Contains("a2"))
            {
                Message.AppendInt32(3);
            }
            else if (GetBaseItem().Name.Contains("wallpaper"))
            {
                Message.AppendInt32(2);
            }
            else if (GetBaseItem().Name.Contains("landscape"))
            {
                Message.AppendInt32(4);
            }
            else
            {
                Message.AppendInt32(0);
            }

            Message.AppendStringWithBreak(ExtraData);
            Message.AppendBoolean(GetBaseItem().AllowRecycle);
            Message.AppendBoolean(GetBaseItem().AllowTrade);
            Message.AppendBoolean(GetBaseItem().AllowInventoryStack);
            Message.AppendBoolean(UberEnvironment.GetGame().GetCatalog().GetMarketplace().CanSellItem(this));
            Message.AppendInt32(-1);

            if (GetBaseItem().Type.ToLower() == "s")
            {
                Message.AppendStringWithBreak("");
                Message.AppendInt32(-1);
            }
        }

        public Item GetBaseItem()
        {
            return UberEnvironment.GetGame().GetItemManager().GetItem(BaseItem);
        }
    }
}
