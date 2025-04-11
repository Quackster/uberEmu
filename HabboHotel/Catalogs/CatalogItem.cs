using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.Messages;
using Uber.HabboHotel.Items;

namespace Uber.HabboHotel.Catalogs
{
    class CatalogItem
    {
        public uint Id;
        public List<uint> ItemIds;
        public string Name;
        public int CreditsCost;
        public int PixelsCost;
        public int Amount;

        public Boolean IsDeal
        {
            get
            {
                if (ItemIds.Count > 1)
                {
                    return true;
                }

                return false;
            }
        }

        public CatalogItem(uint Id, string Name, string ItemIds, int CreditsCost, int PixelsCost, int Amount)
        {
            this.Id = Id;
            this.Name = Name;
            this.ItemIds = new List<uint>();

            foreach (string ItemId in ItemIds.Split(','))
            {
                this.ItemIds.Add(uint.Parse(ItemId));
            }

            this.CreditsCost = CreditsCost;
            this.PixelsCost = PixelsCost;
            this.Amount = Amount;
        }

        public Item GetBaseItem()
        {
            if (IsDeal)
            {
                return null;
            }

            return UberEnvironment.GetGame().GetItemManager().GetItem(ItemIds[0]);
        }

        public void Serialize(ServerMessage Message)
        {
            // PMYoNktchn_stovesKHI[bKIM

            if (IsDeal)
            {
                // 8 328 deal01 [] 5 0 2 s [] 26 [] 3 -1
                // PBXRAdeal01QAHJsRFKMsQEIM

                throw new NotImplementedException("Multipile item ids set for catalog item #" + Id + ", but this is usupported at this point");
            }
            else
            {
                Message.AppendUInt(Id);
                Message.AppendStringWithBreak(Name);
                Message.AppendInt32(CreditsCost);
                Message.AppendInt32(PixelsCost);
                Message.AppendInt32(1);
                Message.AppendStringWithBreak(GetBaseItem().Type);
                Message.AppendInt32(GetBaseItem().SpriteId);
                Message.AppendStringWithBreak("");
                Message.AppendInt32(Amount);
                Message.AppendInt32(-1);
            }
        }
    }
}
