using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.HabboHotel.GameClients;
using Uber.HabboHotel.Items;
using Uber.Storage;
using Uber.Messages;

namespace Uber.HabboHotel.Catalogs
{
    class Marketplace
    {
        public Boolean CanSellItem(UserItem Item)
        {
            if (!Item.GetBaseItem().AllowTrade || !Item.GetBaseItem().AllowMarketplaceSell)
            {
                return false;
            }

            return true;
        }

        public void SellItem(GameClient Session, uint ItemId, int SellingPrice)
        {
            UserItem Item = Session.GetHabbo().GetInventoryComponent().GetItem(ItemId);

            if (Item == null || SellingPrice > 10000 || !CanSellItem(Item))
            {
                Session.GetMessageHandler().GetResponse().Init(610);
                Session.GetMessageHandler().GetResponse().AppendBoolean(false);
                Session.GetMessageHandler().SendResponse();

                return;
            }

            int Comission = CalculateComissionPrice(SellingPrice);
            int TotalPrice = SellingPrice + Comission;
            int ItemType = 1;

            if (Item.GetBaseItem().Type == "i")
            {
                ItemType++;
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("public_name", Item.GetBaseItem().PublicName);
                dbClient.AddParamWithValue("extra_data", Item.ExtraData);
                dbClient.ExecuteQuery("INSERT INTO catalog_marketplace_offers (item_id,user_id,asking_price,total_price,public_name,sprite_id,item_type,timestamp,extra_data) VALUES ('" + Item.BaseItem + "','" + Session.GetHabbo().Id + "','" + SellingPrice + "','" + TotalPrice + "',@public_name,'" + Item.GetBaseItem().SpriteId + "','" + ItemType + "','" + UberEnvironment.GetUnixTimestamp() + "',@extra_data)");
            }

            Session.GetHabbo().GetInventoryComponent().RemoveItem(ItemId);

            Session.GetMessageHandler().GetResponse().Init(610);
            Session.GetMessageHandler().GetResponse().AppendBoolean(true);
            Session.GetMessageHandler().SendResponse();
        }

        public int CalculateComissionPrice(float SellingPrice)
        {
            return (int)Math.Ceiling((float)(SellingPrice / 100));
        }

        public Double FormatTimestamp()
        {
            return UberEnvironment.GetUnixTimestamp() - 172800;
        }

        public ServerMessage SerializeOffers(int MinCost, int MaxCost, String SearchQuery, int FilterMode)
        {
            // IgI`UJUIIY~JX]gXoAJISA

            DataTable Data = null;
            StringBuilder WhereClause = new StringBuilder();
            string OrderMode = "";

            WhereClause.Append("WHERE state = '1' AND timestamp >= " + FormatTimestamp());

            if (MinCost >= 0)
            {
                WhereClause.Append(" AND total_price >= " + MinCost);
            }

            if (MaxCost >= 0)
            {
                WhereClause.Append(" AND total_price <= " + MaxCost);
            }

            switch (FilterMode)
            {
                case 1:
                default:

                    OrderMode = "ORDER BY asking_price DESC";
                    break;

                case 2:

                    OrderMode = "ORDER BY asking_price ASC";
                    break;
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("search_query", "%" + SearchQuery + "%");

                if (SearchQuery.Length >= 1)
                {
                    WhereClause.Append(" AND public_name LIKE @search_query");
                }

                Data = dbClient.ReadDataTable("SELECT * FROM catalog_marketplace_offers " + WhereClause.ToString() + " " + OrderMode + " LIMIT 100");
            }

            ServerMessage Message = new ServerMessage(615);

            if (Data != null)
            {
                Message.AppendInt32(Data.Rows.Count);

                foreach (DataRow Row in Data.Rows)
                {
                    Message.AppendUInt((uint)Row["offer_id"]);
                    Message.AppendInt32(2);
                    Message.AppendInt32(int.Parse(Row["item_type"].ToString()));
                    Message.AppendInt32((int)Row["sprite_id"]); // ??
                    Message.AppendInt32((int)Row["total_price"]);
                    Message.AppendInt32((int)Row["sprite_id"]); // ??
                    Message.AppendInt32((int)Row["total_price"]); // average
                    Message.AppendInt32(1); // offers
                }
            }
            else
            {
                Message.AppendInt32(0);
            }

            return Message;
        }

        public ServerMessage SerializeOwnOffers(uint HabboId)
        {
            DataTable Data = null;
            int Profits = 0;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT * FROM catalog_marketplace_offers WHERE user_id = '" + HabboId + "'");
                String RawProfit = dbClient.ReadDataRow("SELECT SUM(asking_price) FROM catalog_marketplace_offers WHERE state = '2' AND user_id = '" + HabboId + "'")[0].ToString();

                if (RawProfit.Length > 0)
                {
                    Profits = int.Parse(RawProfit);
                }
            }

            ServerMessage Message = new ServerMessage(616);
            Message.AppendInt32(Profits);

            if (Data != null)
            {
                Message.AppendInt32(Data.Rows.Count);

                foreach (DataRow Row in Data.Rows)
                {
                    // IhHI`n~^II[EFPN[OKPA

                    int MinutesLeft = (int)Math.Floor((((Double)Row["timestamp"] + 172800) - UberEnvironment.GetUnixTimestamp()) / 60);
                    int state = int.Parse(Row["state"].ToString());

                    if (MinutesLeft <= 0)
                    {
                        state = 3;
                        MinutesLeft = 0;
                    }

                    Message.AppendUInt((uint)Row["offer_id"]);
                    Message.AppendInt32(state); // 1 = active, 2 = sold, 3 = expired
                    Message.AppendInt32(int.Parse(Row["item_type"].ToString())); // always 1 (??)
                    Message.AppendInt32((int)Row["sprite_id"]);
                    Message.AppendInt32((int)Row["total_price"]); // ??
                    Message.AppendInt32(MinutesLeft);
                    Message.AppendInt32((int)Row["sprite_id"]);
                }
            }
            else
            {
                Message.AppendInt32(0);
            }

            return Message;
        }
    }
}
