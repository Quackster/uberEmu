using System;
using System.Collections.Generic;
using System.Text;
using System.Data;

using Uber.HabboHotel.Items;
using Uber.HabboHotel.Catalogs;
using Uber.Storage;

namespace Uber.Messages
{
    partial class GameClientMessageHandler
    {
        private void GetCatalogIndex()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetCatalog().SerializeIndex(Session));
        }

        private void GetCatalogPage()
        {
            CatalogPage Page = UberEnvironment.GetGame().GetCatalog().GetPage(Request.PopWiredInt32());

            if (Page == null || !Page.Enabled || !Page.Visible || Page.ComingSoon || Page.MinRank > Session.GetHabbo().Rank)
            {
                return;
            }

            if (Page.ClubOnly && !Session.GetHabbo().GetSubscriptionManager().HasSubscription("habbo_club"))
            {
                Session.SendNotif("This page is for Uber Club members only!");
                return;
            }

            Session.SendMessage(UberEnvironment.GetGame().GetCatalog().SerializePage(Page));

            if (Page.Layout == "recycler")
            {
                GetResponse().Init(507);
                GetResponse().AppendBoolean(true);
                GetResponse().AppendBoolean(false);
                SendResponse();
            }
        }

        private void RedeemVoucher()
        {
            UberEnvironment.GetGame().GetCatalog().GetVoucherHandler().TryRedeemVoucher(Session, Request.PopFixedString());
        }

        private void HandlePurchase()
        {
            int PageId = Request.PopWiredInt32();
            uint ItemId = Request.PopWiredUInt();
            string ExtraData = Request.PopFixedString();

            UberEnvironment.GetGame().GetCatalog().HandlePurchase(Session, PageId, ItemId, ExtraData, false, "", "");
        }

        private void PurchaseGift()
        {
            int PageId = Request.PopWiredInt32();
            uint ItemId = Request.PopWiredUInt();
            string ExtraData = Request.PopFixedString();
            string GiftUser = UberEnvironment.FilterInjectionChars(Request.PopFixedString());
            string GiftMessage = UberEnvironment.FilterInjectionChars(Request.PopFixedString());

            UberEnvironment.GetGame().GetCatalog().HandlePurchase(Session, PageId, ItemId, ExtraData, true, GiftUser, GiftMessage);
        }

        private void GetRecyclerRewards()
        {
            // GzQAQAXtGIsZJKPAPrIsXLKKPJKsY}JsXBKsX~JJPASCsX|JiXBPsZAKs[|JiYBPsZ}JsXAKsYAKsX}JsY|JsY~Js[{JiZAPs[JsZBKIIRAsX@KsYBKsZJs[@Ks[~JsZ|J

            GetResponse().Init(506);
            GetResponse().AppendInt32(5);

            for (uint i = 5; i >= 1; i--)
            {
                GetResponse().AppendUInt(i);

                if (i <= 1)
                {
                    GetResponse().AppendInt32(0);
                }
                else if (i == 2)
                {
                    GetResponse().AppendInt32(4);
                }
                else if (i == 3)
                {
                    GetResponse().AppendInt32(40);
                }
                else if (i == 4)
                {
                    GetResponse().AppendInt32(200);
                }
                else if (i >= 5)
                {
                    GetResponse().AppendInt32(2000);
                }

                List<EcotronReward> Rewards = UberEnvironment.GetGame().GetCatalog().GetEcotronRewardsForLevel(i);

                GetResponse().AppendInt32(Rewards.Count);

                foreach (EcotronReward Reward in Rewards)
                {
                    GetResponse().AppendStringWithBreak(Reward.GetBaseItem().Type.ToLower());
                    GetResponse().AppendUInt(Reward.DisplayId);
                }
            }

            SendResponse();
        }

        private void CanGift()
        {
            uint Id = Request.PopWiredUInt();

            CatalogItem Item = UberEnvironment.GetGame().GetCatalog().FindItem(Id);

            if (Item == null)
            {
                return;
            }

            GetResponse().Init(622);
            GetResponse().AppendUInt(Item.Id);
            GetResponse().AppendBoolean(Item.GetBaseItem().AllowGift);
            SendResponse();
        }

        private void GetCataData1()
        {
            GetResponse().Init(612);
            //  1 1 1 5 1 10000 48 7
            GetResponse().AppendInt32(1);
            GetResponse().AppendInt32(1);
            GetResponse().AppendInt32(1);
            GetResponse().AppendInt32(5);
            GetResponse().AppendInt32(1);
            GetResponse().AppendInt32(10000);
            GetResponse().AppendInt32(48);
            GetResponse().AppendInt32(7);
            SendResponse();
        }

        private void GetCataData2()
        {
            // Il
            GetResponse().Init(620);
            GetResponse().AppendInt32(1);
            GetResponse().AppendInt32(1);
            GetResponse().AppendInt32(10);
            GetResponse().AppendInt32(3064);
            GetResponse().AppendInt32(3065);
            GetResponse().AppendInt32(3066);
            GetResponse().AppendInt32(3067);
            GetResponse().AppendInt32(3068);
            GetResponse().AppendInt32(3069);
            GetResponse().AppendInt32(3070);
            GetResponse().AppendInt32(3071);
            GetResponse().AppendInt32(3072);
            GetResponse().AppendInt32(3073);
            GetResponse().AppendInt32(7);
            GetResponse().AppendInt32(0);
            GetResponse().AppendInt32(1);
            GetResponse().AppendInt32(2);
            GetResponse().AppendInt32(3);
            GetResponse().AppendInt32(4);
            GetResponse().AppendInt32(5);
            GetResponse().AppendInt32(6);
            GetResponse().AppendInt32(11);
            GetResponse().AppendInt32(0);
            GetResponse().AppendInt32(1);
            GetResponse().AppendInt32(2);
            GetResponse().AppendInt32(3);
            GetResponse().AppendInt32(4);
            GetResponse().AppendInt32(5);
            GetResponse().AppendInt32(6);
            GetResponse().AppendInt32(7);
            GetResponse().AppendInt32(8);
            GetResponse().AppendInt32(9);
            GetResponse().AppendInt32(10);
            GetResponse().AppendInt32(1);
            SendResponse();
        }

        private void MarketplaceCanSell()
        {
            GetResponse().Init(611);
            GetResponse().AppendBoolean(true);
            GetResponse().AppendInt32(99999);
            SendResponse();
        }

        private void MarketplacePostItem()
        {
            if (Session.GetHabbo().GetInventoryComponent() == null)
            {
                return;
            }

            int sellingPrice = Request.PopWiredInt32();
            int junk = Request.PopWiredInt32();
            uint itemId = Request.PopWiredUInt();

            UserItem Item = Session.GetHabbo().GetInventoryComponent().GetItem(itemId);

            if (Item == null || !Item.GetBaseItem().AllowTrade)
            {
                return;
            }

            UberEnvironment.GetGame().GetCatalog().GetMarketplace().SellItem(Session, Item.Id, sellingPrice);
        }

        private void MarketplaceGetOwnOffers()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetCatalog().GetMarketplace().SerializeOwnOffers(Session.GetHabbo().Id));
        }

        private void MarketplaceTakeBack()
        {
            uint ItemId = Request.PopWiredUInt();
            DataRow Row = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Row = dbClient.ReadDataRow("SELECT * FROM catalog_marketplace_offers WHERE offer_id = '" + ItemId + "' LIMIT 1");
            }

            if (Row == null || (uint)Row["user_id"] != Session.GetHabbo().Id || (string)Row["state"] != "1")
            {
                return;
            }

            Item Item = UberEnvironment.GetGame().GetItemManager().GetItem((uint)Row["item_id"]);

            if (Item == null)
            {
                return;
            }

            UberEnvironment.GetGame().GetCatalog().DeliverItems(Session, Item, 1, (String)Row["extra_data"]);

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("DELETE FROM catalog_marketplace_offers WHERE offer_id = '" + ItemId + "' LIMIT 1");
            }

            GetResponse().Init(614);
            GetResponse().AppendUInt((uint)Row["offer_id"]);
            GetResponse().AppendBoolean(true);
            SendResponse();
        }

        private void MarketplaceClaimCredits()
        {
            DataTable Results = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Results = dbClient.ReadDataTable("SELECT asking_price FROM catalog_marketplace_offers WHERE user_id = '" + Session.GetHabbo().Id + "' AND state = '2'");
            }

            if (Results == null)
            {
                return;
            }

            int Profit = 0;

            foreach (DataRow Row in Results.Rows)
            {
                Profit += (int)Row["asking_price"];
            }

            if (Profit >= 1)
            {
                Session.GetHabbo().Credits += Profit;
                Session.GetHabbo().UpdateCreditsBalance(true);
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("DELETE FROM catalog_marketplace_offers WHERE user_id = '" + Session.GetHabbo().Id + "' AND state = '2'");
            }
        }

        private void MarketplaceGetOffers()
        {
            int MinPrice = Request.PopWiredInt32();
            int MaxPrice = Request.PopWiredInt32();
            string SearchQuery = Request.PopFixedString();
            int FilterMode = Request.PopWiredInt32();

            Session.SendMessage(UberEnvironment.GetGame().GetCatalog().GetMarketplace().SerializeOffers(MinPrice, MaxPrice, SearchQuery, FilterMode));
        }

        private void MarketplacePurchase()
        {
            uint ItemId = Request.PopWiredUInt();
            DataRow Row = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Row = dbClient.ReadDataRow("SELECT * FROM catalog_marketplace_offers WHERE offer_id = '" + ItemId + "' LIMIT 1");
            }

            if (Row == null || (string)Row["state"] != "1" || (double)Row["timestamp"] <= UberEnvironment.GetGame().GetCatalog().GetMarketplace().FormatTimestamp())
            {
                Session.SendNotif("Sorry, this offer has expired.");
                return;
            }

            Item Item = UberEnvironment.GetGame().GetItemManager().GetItem((uint)Row["item_id"]);

            if (Item == null)
            {
                return;
            }

            if ((int)Row["total_price"] >= 1)
            {
                Session.GetHabbo().Credits -= (int)Row["total_price"];
                Session.GetHabbo().UpdateCreditsBalance(true);
            }

            UberEnvironment.GetGame().GetCatalog().DeliverItems(Session, Item, 1, (String)Row["extra_data"]);

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("UPDATE catalog_marketplace_offers SET state = '2' WHERE offer_id = '" + ItemId + "' LIMIT 1");
            }

            Session.GetMessageHandler().GetResponse().Init(67);
            Session.GetMessageHandler().GetResponse().AppendUInt(Item.ItemId);
            Session.GetMessageHandler().GetResponse().AppendStringWithBreak(Item.Name);
            Session.GetMessageHandler().GetResponse().AppendInt32(0);
            Session.GetMessageHandler().GetResponse().AppendInt32(0);
            Session.GetMessageHandler().GetResponse().AppendInt32(1);
            Session.GetMessageHandler().GetResponse().AppendStringWithBreak(Item.Type.ToLower());
            Session.GetMessageHandler().GetResponse().AppendInt32(Item.SpriteId);
            Session.GetMessageHandler().GetResponse().AppendStringWithBreak("");
            Session.GetMessageHandler().GetResponse().AppendInt32(1);
            Session.GetMessageHandler().GetResponse().AppendInt32(-1);
            Session.GetMessageHandler().GetResponse().AppendStringWithBreak("");
            Session.GetMessageHandler().SendResponse();

            Session.SendMessage(UberEnvironment.GetGame().GetCatalog().GetMarketplace().SerializeOffers(-1, -1, "", 1));
        }

        private void CheckPetName()
        {
            Session.GetMessageHandler().GetResponse().Init(36);
            Session.GetMessageHandler().GetResponse().AppendInt32(UberEnvironment.GetGame().GetCatalog().CheckPetName(Request.PopFixedString()) ? 0 : 2);
            Session.GetMessageHandler().SendResponse();
        }

        public void RegisterCatalog()
        {
            RequestHandlers[101] = new RequestHandler(GetCatalogIndex);
            RequestHandlers[102] = new RequestHandler(GetCatalogPage);
            RequestHandlers[129] = new RequestHandler(RedeemVoucher);
            RequestHandlers[100] = new RequestHandler(HandlePurchase);
            RequestHandlers[472] = new RequestHandler(PurchaseGift);
            RequestHandlers[412] = new RequestHandler(GetRecyclerRewards);
            RequestHandlers[3030] = new RequestHandler(CanGift);
            RequestHandlers[3011] = new RequestHandler(GetCataData1);
            RequestHandlers[473] = new RequestHandler(GetCataData2);
            RequestHandlers[3012] = new RequestHandler(MarketplaceCanSell);
            RequestHandlers[3010] = new RequestHandler(MarketplacePostItem);
            RequestHandlers[3019] = new RequestHandler(MarketplaceGetOwnOffers);
            RequestHandlers[3015] = new RequestHandler(MarketplaceTakeBack);
            RequestHandlers[3016] = new RequestHandler(MarketplaceClaimCredits);
            RequestHandlers[3018] = new RequestHandler(MarketplaceGetOffers);
            RequestHandlers[3014] = new RequestHandler(MarketplacePurchase);
            RequestHandlers[42] = new RequestHandler(CheckPetName);
        }
    }
}
