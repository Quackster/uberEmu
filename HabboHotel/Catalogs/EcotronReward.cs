using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.HabboHotel.Items;

namespace Uber.HabboHotel.Catalogs
{
    class EcotronReward
    {
        public uint Id;
        public uint DisplayId;
        public uint BaseId;
        public uint RewardLevel;

        public EcotronReward(uint Id, uint DisplayId, uint BaseId, uint RewardLevel)
        {
            this.Id = Id;
            this.DisplayId = DisplayId;
            this.BaseId = BaseId;
            this.RewardLevel = RewardLevel;
        }

        public Item GetBaseItem()
        {
            return UberEnvironment.GetGame().GetItemManager().GetItem(this.BaseId);
        }
    }
}
