using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Storage;

namespace Uber.HabboHotel.Items
{
    class ItemManager
    {
        private Dictionary<uint, Item> Items;

        public ItemManager()
        {
        }

        public void LoadItems()
        {
            Items = new Dictionary<uint, Item>();

            DataTable ItemData = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                ItemData = dbClient.ReadDataTable("SELECT * FROM furniture");
            }

            int i = 0;
            int j = 0;

            if (ItemData != null)
            {
                foreach (DataRow Row in ItemData.Rows)
                {
                    try
                    {
                        Items.Add((uint)Row["id"], new Item((uint)Row["id"], (int)Row["sprite_id"], (string)Row["public_name"], 
                            (string)Row["item_name"], (string)Row["type"], (int)Row["width"], (int)Row["length"],
                            (Double)Row["stack_height"], UberEnvironment.EnumToBool(Row["can_stack"].ToString()),
                            UberEnvironment.EnumToBool(Row["is_walkable"].ToString()),
                            UberEnvironment.EnumToBool(Row["can_sit"].ToString()),
                            UberEnvironment.EnumToBool(Row["allow_recycle"].ToString()),
                            UberEnvironment.EnumToBool(Row["allow_trade"].ToString()),
                            UberEnvironment.EnumToBool(Row["allow_marketplace_sell"].ToString()),
                            UberEnvironment.EnumToBool(Row["allow_gift"].ToString()),
                            UberEnvironment.EnumToBool(Row["allow_inventory_stack"].ToString()),
                            (string)Row["interaction_type"], (int)Row["interaction_modes_count"], (string)Row["vending_ids"]));

                        i++;
                    }

                    catch (Exception)
                    {
                        UberEnvironment.GetLogging().WriteLine("Could not load item #" + (uint)Row["id"] + ", please verify the data is okay.", Core.LogLevel.Error);

                        j++;
                    }
                }
            }

            UberEnvironment.GetLogging().WriteLine("Loaded " + i + " item defenition(s).");

            if (j > 0)
            {
                UberEnvironment.GetLogging().WriteLine(j + " item defenition(s) could not be loaded.");
            }
        }

        public Boolean ContainsItem(uint Id)
        {
            return Items.ContainsKey(Id);
        }

        public Item GetItem(uint Id)
        {
            if (ContainsItem(Id))
            {
                return Items[Id];
            }

            return null;
        }
    }
}
