using System;
using System.Collections.Generic;
using System.Text;
using System.Data;

using Uber.HabboHotel.GameClients;
using Uber.Messages;
using Uber.Storage;
using Uber.HabboHotel.Items;
using Uber.HabboHotel.Pets;

namespace Uber.HabboHotel.Catalogs
{
    class Catalog
    {
        public Dictionary<int, CatalogPage> Pages;
        public List<EcotronReward> EcotronRewards;

        private VoucherHandler VoucherHandler;
        private Marketplace Marketplace;

        private readonly object ItemGeneratorLock = new object();

        public Catalog()
        {
            VoucherHandler = new VoucherHandler();
            Marketplace = new Marketplace();
        }

        public void Initialize()
        {
            Pages = new Dictionary<int, CatalogPage>();
            EcotronRewards = new List<EcotronReward>();

            DataTable Data = null;
            DataTable EcoData = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT * FROM catalog_pages ORDER BY order_num ASC");
                EcoData = dbClient.ReadDataTable("SELECT * FROM ecotron_rewards ORDER BY item_id");
            }

            if (Data != null)
            {
                foreach (DataRow Row in Data.Rows)
                {
                    Boolean Visible = false;
                    Boolean Enabled = false;
                    Boolean ComingSoon = false;

                    if (Row["visible"].ToString() == "1")
                    {
                        Visible = true;
                    }

                    if (Row["enabled"].ToString() == "1")
                    {
                        Enabled = true;
                    }

                    if (Row["coming_soon"].ToString() == "1")
                    {
                        ComingSoon = true;
                    }

                    Pages.Add((int)Row["id"], new CatalogPage((int)Row["id"], (int)Row["parent_id"],
                        (string)Row["caption"], Visible, Enabled, ComingSoon, (uint)Row["min_rank"],
                        UberEnvironment.EnumToBool(Row["club_only"].ToString()), (int)Row["icon_color"],
                        (int)Row["icon_image"], (string)Row["page_layout"], (string)Row["page_headline"],
                        (string)Row["page_teaser"], (string)Row["page_special"], (string)Row["page_text1"],
                        (string)Row["page_text2"], (string)Row["page_text_details"], (string)Row["page_text_teaser"]));
                }
            }

            if (EcoData != null)
            {
                foreach (DataRow Row in EcoData.Rows)
                {
                    EcotronRewards.Add(new EcotronReward((uint)Row["id"], (uint)Row["display_id"], (uint)Row["item_id"], (uint)Row["reward_level"]));
                }
            }
        }

        public CatalogItem FindItem(uint ItemId)
        {
            lock (Pages.Values)
            {
                foreach (CatalogPage Page in Pages.Values)
                {
                    lock (Page.Items)
                    {
                        foreach (CatalogItem Item in Page.Items)
                        {
                            if (Item.Id == ItemId)
                            {
                                return Item;
                            }
                        }
                    }
                }
            }

            return null;
        }

        public Boolean IsItemInCatalog(uint BaseId)
        {
            DataRow Row = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Row = dbClient.ReadDataRow("SELECT id FROM catalog_items WHERE item_ids = '" + BaseId + "' LIMIT 1");
            }

            if (Row != null)
            {
                return true;
            }

            return false;
        }

        public int GetTreeSize(GameClient Session, int TreeId)
        {
            int i = 0;

            lock (Pages)
            {
                foreach (CatalogPage Page in Pages.Values)
                {
                    if (Page.MinRank > Session.GetHabbo().Rank)
                    {
                        continue;
                    }

                    if (Page.ParentId == TreeId)
                    {
                        i++;
                    }
                }
            }

            return i;
        }

        public CatalogPage GetPage(int Page)
        {
            if (!Pages.ContainsKey(Page))
            {
                return null;
            }

            return Pages[Page];
        }

        public void HandlePurchase(GameClient Session, int PageId, uint ItemId, string ExtraData, Boolean IsGift, string GiftUser, string GiftMessage)
        {
            CatalogPage Page = GetPage(PageId);

            if (Page == null || Page.ComingSoon || !Page.Enabled || !Page.Visible)
            {
                return;
            }

            if (Page.ClubOnly && !Session.GetHabbo().GetSubscriptionManager().HasSubscription("habbo_club"))
            {
                return;
            }

            CatalogItem Item = Page.GetItem(ItemId);

            if (Item == null)
            {
                return;
            }

            uint GiftUserId = 0;

            if (IsGift)
            {
                if (!Item.GetBaseItem().AllowGift)
                {
                    return;
                }

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.AddParamWithValue("gift_user", GiftUser);

                    try
                    {
                        GiftUserId = (uint)dbClient.ReadDataRow("SELECT id FROM users WHERE username = @gift_user LIMIT 1")[0];
                    }
                    catch (Exception) { }
                }

                if (GiftUserId == 0)
                {
                    Session.GetMessageHandler().GetResponse().Init(76);
                    Session.GetMessageHandler().GetResponse().AppendBoolean(true);
                    Session.GetMessageHandler().GetResponse().AppendStringWithBreak(GiftUser);
                    Session.GetMessageHandler().SendResponse();

                    return;
                }
            }

            Boolean CreditsError = false;
            Boolean PixelError = false;

            if (Session.GetHabbo().Credits < Item.CreditsCost)
            {
                CreditsError = true;
            }

            if (Session.GetHabbo().ActivityPoints < Item.PixelsCost)
            {
                PixelError = true;
            }

            if (CreditsError || PixelError)
            {
                Session.GetMessageHandler().GetResponse().Init(68);
                Session.GetMessageHandler().GetResponse().AppendBoolean(CreditsError);
                Session.GetMessageHandler().GetResponse().AppendBoolean(PixelError);
                Session.GetMessageHandler().SendResponse();

                return;
            }

            if (IsGift && Item.GetBaseItem().Type.ToLower() == "e")
            {
                Session.SendNotif("You can not send this item as a gift.");
                return;
            }

            // Extra Data is _NOT_ filtered at this point and MUST BE VERIFIED BELOW:
            switch (Item.GetBaseItem().InteractionType.ToLower())
            {
                case "pet":

                    try
                    {
                        string[] Bits = ExtraData.Split('\n');
                        string PetName = Bits[0];
                        string Race = Bits[1];
                        string Color = Bits[2];

                        int.Parse(Race); // to trigger any possible errors

                        if (!CheckPetName(PetName))
                        {
                            return;
                        }

                        if (Race.Length != 3)
                        {
                            return;
                        }

                        if (Color.Length != 6)
                        {
                            return;
                        }
                    }
                    catch (Exception) { return; }

                    break;

                case "roomeffect":

                    Double Number = 0;

                    try
                    {
                        Number = Double.Parse(ExtraData);
                    }
                    catch (Exception) { }

                    ExtraData = Number.ToString().Replace(',', '.');
                    break; // maintain extra data // todo: validate

                case "postit":

                    ExtraData = "FFFF33";
                    break;

                case "dimmer":

                    ExtraData = "1,1,1,#000000,255";
                    break;

                case "trophy":

                    ExtraData = Session.GetHabbo().Username + Convert.ToChar(9) + DateTime.Now.Day + "-" + DateTime.Now.Month + "-" + DateTime.Now.Year + Convert.ToChar(9) + UberEnvironment.FilterInjectionChars(ExtraData, true);
                    break;

                default:

                    ExtraData = "";
                    break;
            }

            if (Item.CreditsCost > 0)
            {
                Session.GetHabbo().Credits -= Item.CreditsCost;
                Session.GetHabbo().UpdateCreditsBalance(true);
            }

            if (Item.PixelsCost > 0)
            {
                Session.GetHabbo().ActivityPoints -= Item.PixelsCost;
                Session.GetHabbo().UpdateActivityPointsBalance(true);
            }

            Session.GetMessageHandler().GetResponse().Init(67);
            Session.GetMessageHandler().GetResponse().AppendUInt(Item.GetBaseItem().ItemId);
            Session.GetMessageHandler().GetResponse().AppendStringWithBreak(Item.GetBaseItem().Name);
            Session.GetMessageHandler().GetResponse().AppendInt32(Item.CreditsCost);
            Session.GetMessageHandler().GetResponse().AppendInt32(Item.PixelsCost);
            Session.GetMessageHandler().GetResponse().AppendInt32(1);
            Session.GetMessageHandler().GetResponse().AppendStringWithBreak(Item.GetBaseItem().Type.ToLower());
            Session.GetMessageHandler().GetResponse().AppendInt32(Item.GetBaseItem().SpriteId);
            Session.GetMessageHandler().GetResponse().AppendStringWithBreak("");
            Session.GetMessageHandler().GetResponse().AppendInt32(1);
            Session.GetMessageHandler().GetResponse().AppendInt32(-1);
            Session.GetMessageHandler().GetResponse().AppendStringWithBreak("");
            Session.GetMessageHandler().SendResponse();

            if (IsGift)
            {
                uint GenId = GenerateItemId();
                Item Present = GeneratePresent();

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.AddParamWithValue("gift_message", "!" + GiftMessage);
                    dbClient.AddParamWithValue("extra_data", ExtraData);

                    dbClient.ExecuteQuery("INSERT INTO user_items (id,user_id,base_item,extra_data) VALUES ('" + GenId + "','" + GiftUserId + "','" + Present.ItemId + "',@gift_message)");
                    dbClient.ExecuteQuery("INSERT INTO user_presents (item_id,base_id,amount,extra_data) VALUES ('" + GenId + "','" + Item.GetBaseItem().ItemId + "','" + Item.Amount + "',@extra_data)");
                }

                GameClient Receiver = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(GiftUserId);

                if (Receiver != null)
                {
                    Receiver.SendNotif("You have received a gift! Check your inventory.");
                    Receiver.GetHabbo().GetInventoryComponent().UpdateItems(true);
                }

                Session.SendNotif("Gift sent successfully!");
            }
            else
            {
                DeliverItems(Session, Item.GetBaseItem(), Item.Amount, ExtraData);
            }
        }

        public bool CheckPetName(string PetName)
        {
            if (PetName.Length < 1 || PetName.Length > 16)
            {
                return false;
            }

            if (!UberEnvironment.IsValidAlphaNumeric(PetName))
            {
                return false;
            }

            return true;
        }

        public void DeliverItems(GameClient Session, Item Item, int Amount, String ExtraData)
        {
            switch (Item.Type.ToLower())
            {
                case "i":
                case "s":

                    for (int i = 0; i < Amount; i++)
                    {
                        uint GeneratedId = GenerateItemId();

                        switch (Item.InteractionType.ToLower())
                        {
                            case "pet":

                                string[] PetData = ExtraData.Split('\n');

                                Pet GeneratedPet = CreatePet(Session.GetHabbo().Id, PetData[0], 5, PetData[1], PetData[2]);

                                Session.GetHabbo().GetInventoryComponent().AddPet(GeneratedPet);
                                Session.GetHabbo().GetInventoryComponent().AddItem(GeneratedId, 320, "0");

                                break;

                            case "teleport":

                                uint TeleTwo = GenerateItemId();

                                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                                {
                                    dbClient.ExecuteQuery("INSERT INTO tele_links (tele_one_id,tele_two_id) VALUES ('" + GeneratedId + "','" + TeleTwo + "')");
                                    dbClient.ExecuteQuery("INSERT INTO tele_links (tele_one_id,tele_two_id) VALUES ('" + TeleTwo + "','" + GeneratedId + "')");
                                }

                                Session.GetHabbo().GetInventoryComponent().AddItem(TeleTwo, Item.ItemId, "0");
                                Session.GetHabbo().GetInventoryComponent().AddItem(GeneratedId, Item.ItemId, "0");
                                break;

                            case "dimmer":

                                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                                {
                                    dbClient.ExecuteQuery("INSERT INTO room_items_moodlight (item_id,enabled,current_preset,preset_one,preset_two,preset_three) VALUES ('" + GeneratedId + "','0','1','#000000,255,0','#000000,255,0','#000000,255,0')");
                                }

                                Session.GetHabbo().GetInventoryComponent().AddItem(GeneratedId, Item.ItemId, ExtraData);
                                break;

                            default:

                                Session.GetHabbo().GetInventoryComponent().AddItem(GeneratedId, Item.ItemId, ExtraData);
                                break;
                        }
                    }

                    Session.GetHabbo().GetInventoryComponent().UpdateItems(true);
                    break;

                case "e":

                    for (int i = 0; i < Amount; i++)
                    {
                        Session.GetHabbo().GetAvatarEffectsInventoryComponent().AddEffect(Item.SpriteId, 3600);
                    }

                    break;

                case "h":

                    for (int i = 0; i < Amount; i++)
                    {
                        Session.GetHabbo().GetSubscriptionManager().AddOrExtendSubscription("habbo_club", 2678400);
                    }

                    if (!Session.GetHabbo().GetBadgeComponent().HasBadge("HC1"))
                    {
                        Session.GetHabbo().GetBadgeComponent().GiveBadge("HC1", true);
                    }

                    Session.GetMessageHandler().GetResponse().Init(7);
                    Session.GetMessageHandler().GetResponse().AppendStringWithBreak("habbo_club");

                    if (Session.GetHabbo().GetSubscriptionManager().HasSubscription("habbo_club"))
                    {
                        Double Expire = Session.GetHabbo().GetSubscriptionManager().GetSubscription("habbo_club").ExpireTime;
                        Double TimeLeft = Expire - UberEnvironment.GetUnixTimestamp();
                        int TotalDaysLeft = (int)Math.Ceiling(TimeLeft / 86400);
                        int MonthsLeft = TotalDaysLeft / 31;

                        if (MonthsLeft >= 1) MonthsLeft--;

                        Session.GetMessageHandler().GetResponse().AppendInt32(TotalDaysLeft - (MonthsLeft * 31));
                        Session.GetMessageHandler().GetResponse().AppendBoolean(true);
                        Session.GetMessageHandler().GetResponse().AppendInt32(MonthsLeft);
                    }
                    else
                    {
                        for (int i = 0; i < 3; i++)
                        {
                            Session.GetMessageHandler().GetResponse().AppendInt32(0);
                        }
                    }

                    Session.GetMessageHandler().SendResponse();     
               
                    List<string> Rights = UberEnvironment.GetGame().GetRoleManager().GetRightsForHabbo(Session.GetHabbo());

                    Session.GetMessageHandler().GetResponse().Init(2);
                    Session.GetMessageHandler().GetResponse().AppendInt32(Rights.Count);

                    foreach (string Right in Rights)
                    {
                        Session.GetMessageHandler().GetResponse().AppendStringWithBreak(Right);
                    }

                    Session.GetMessageHandler().SendResponse();

                    break;

                default:

                    Session.SendNotif("Something went wrong! The item type could not be processed. Please do not try to buy this item anymore, instead inform support as soon as possible.");
                    break;
            }
        }

        public Item GeneratePresent()
        {
            int Random = UberEnvironment.GetRandomNumber(0, 6);

            switch (Random)
            {
                case 1:

                    return UberEnvironment.GetGame().GetItemManager().GetItem(165); // present_gen1

                case 2:

                    return UberEnvironment.GetGame().GetItemManager().GetItem(166); // present_gen2

                case 3:

                    return UberEnvironment.GetGame().GetItemManager().GetItem(167); // present_gen3

                case 4:

                    return UberEnvironment.GetGame().GetItemManager().GetItem(168); // present_gen4

                case 5:

                    return UberEnvironment.GetGame().GetItemManager().GetItem(169); // present_gen5

                case 6:

                    return UberEnvironment.GetGame().GetItemManager().GetItem(170); // present_gen6

                case 0:
                default:

                    return UberEnvironment.GetGame().GetItemManager().GetItem(164); // present_gen
            }
        }

        public Pet CreatePet(uint UserId, string Name, int Type, string Race, string Color)
        {
            DataRow Row = null;

            lock (UberEnvironment.GetGame().GetCatalog().ItemGeneratorLock)
            {
                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.AddParamWithValue("userid", UserId);
                    dbClient.AddParamWithValue("name", Name);
                    dbClient.AddParamWithValue("type", Type);
                    dbClient.AddParamWithValue("race", Race);
                    dbClient.AddParamWithValue("color", Color);
                    dbClient.AddParamWithValue("createstamp", UberEnvironment.GetUnixTimestamp());
                    dbClient.ReadDataRow("INSERT INTO user_pets (user_id,name,type,race,color,expirience,energy,createstamp) VALUES (@userid,@name,@type,@race,@color,0,100,@createstamp)");
                }

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.AddParamWithValue("userid", UserId);
                    dbClient.AddParamWithValue("name", Name);
                    Row = dbClient.ReadDataRow("SELECT * FROM user_pets WHERE user_id = @userid AND name = @name LIMIT 1");
                }
            }

            return this.GeneratePetFromRow(Row);
        }

        public Pet GeneratePetFromRow(DataRow Row)
        {
            if (Row == null)
            {
                return null;
            }

            return new Pet((uint)Row["id"], (uint)Row["user_id"], (uint)Row["room_id"], (string)Row["name"], (uint)Row["type"], (string)Row["race"], (string)Row["color"], (int)Row["expirience"], (int)Row["energy"], (int)Row["nutrition"], (int)Row["respect"], (double)Row["createstamp"], (int)Row["x"], (int)Row["y"], (double)Row["z"]);
        }

        public uint GenerateItemId()
        {
            lock (this.ItemGeneratorLock)
            {
                uint i = 0;

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    i = (uint)dbClient.ReadDataRow("SELECT id_generator FROM item_id_generator LIMIT 1")[0];
                    dbClient.ExecuteQuery("UPDATE item_id_generator SET id_generator = id_generator + 1 LIMIT 1");
                }

                return i;
            }
        }

        public EcotronReward GetRandomEcotronReward()
        {
            uint Level = 1;

            if (UberEnvironment.GetRandomNumber(1, 2000) == 2000)
            {
                Level = 5;
            }
            else if (UberEnvironment.GetRandomNumber(1, 200) == 200)
            {
                Level = 4;
            }
            else if (UberEnvironment.GetRandomNumber(1, 40) == 40)
            {
                Level = 3;
            }
            else if (UberEnvironment.GetRandomNumber(1, 4) == 4)
            {
                Level = 2;
            }

            List<EcotronReward> PossibleRewards = GetEcotronRewardsForLevel(Level);

            if (PossibleRewards != null && PossibleRewards.Count >= 1)
            {
                return PossibleRewards[UberEnvironment.GetRandomNumber(0, (PossibleRewards.Count - 1))];
            }
            else
            {
                return new EcotronReward(0, 0, 1479, 0); // eco lamp two :D
            }
        }

        public List<EcotronReward> GetEcotronRewardsForLevel(uint Level)
        {
            List<EcotronReward> Rewards = new List <EcotronReward>();

            lock (EcotronRewards)
            {
                foreach (EcotronReward R in EcotronRewards)
                {
                    if (R.RewardLevel == Level)
                    {
                        Rewards.Add(R);
                    }
                }
            }

            return Rewards;
        }

        public ServerMessage SerializeIndex(GameClient Client)
        {
            ServerMessage Index = new ServerMessage(126);
            Index.AppendBoolean(false);
            Index.AppendInt32(0);
            Index.AppendInt32(0);
            Index.AppendInt32(-1);
            Index.AppendStringWithBreak("");
            Index.AppendBoolean(false);
            Index.AppendInt32(GetTreeSize(Client, -1));

            lock (Pages)
            {
                foreach (CatalogPage Page in Pages.Values)
                {
                    if (Page.ParentId != -1)
                    {
                        continue;
                    }

                    Page.Serialize(Client, Index);

                    foreach (CatalogPage _Page in Pages.Values)
                    {
                        if (_Page.ParentId != Page.PageId)
                        {
                            continue;
                        }

                        _Page.Serialize(Client, Index);
                    }
                }
            }

            return Index;
        }

        public ServerMessage SerializePage(CatalogPage Page)
        {
            /* A`ieAfrontpage3Kcatalog_frontpage_headline2_ents_twilight_balconySBTWILIGHT FURNI OUT NOWSink your teeth into Twilight Furni and Black Pura!How do I get Credits easily?1. Always ask permission from the bill payer first.
2. Send HABBO in a UK SMS to 78881. You'll get an SMS back with a voucher code and will be charged Â£3 plus your standard UK SMS rate, normally 10p.
3. Enter the code below to redeem 35 Credits.Redeem a Habbo Voucher code here:#FAF8CC#FAF8CCOther ways to get more credits >magic.creditsH*/

            ServerMessage PageData = new ServerMessage(127);
            PageData.AppendInt32(Page.PageId);

            switch (Page.Layout)
            {
                case "frontpage":

                    PageData.AppendStringWithBreak("frontpage3");
                    PageData.AppendInt32(3);
                    PageData.AppendStringWithBreak(Page.LayoutHeadline);
                    PageData.AppendStringWithBreak(Page.LayoutTeaser);
                    PageData.AppendStringWithBreak("");
                    PageData.AppendInt32(11);
                    PageData.AppendStringWithBreak(Page.Text1);
                    PageData.AppendStringWithBreak("");
                    PageData.AppendStringWithBreak(Page.Text2);
                    PageData.AppendStringWithBreak(Page.TextDetails);
                    PageData.AppendStringWithBreak("");
                    PageData.AppendStringWithBreak("#FAF8CC");
                    PageData.AppendStringWithBreak("#FAF8CC");
                    PageData.AppendStringWithBreak("Other ways to get more credits >");
                    PageData.AppendStringWithBreak("magic.credits");

                    break;

                case "recycler_info":

                    PageData.AppendStringWithBreak(Page.Layout);
                    PageData.AppendInt32(2);
                    PageData.AppendStringWithBreak(Page.LayoutHeadline);
                    PageData.AppendStringWithBreak(Page.LayoutTeaser);
                    PageData.AppendInt32(3);
                    PageData.AppendStringWithBreak(Page.Text1);
                    PageData.AppendStringWithBreak(Page.Text2);
                    PageData.AppendStringWithBreak(Page.TextDetails);

                    break;

                case "recycler_prizes":

                    // Ac@aArecycler_prizesIcatalog_recycler_headline3IDe Ecotron geeft altijd een van deze beloningen:H
                    PageData.AppendStringWithBreak("recycler_prizes");
                    PageData.AppendInt32(1);
                    PageData.AppendStringWithBreak("catalog_recycler_headline3");
                    PageData.AppendInt32(1);
                    PageData.AppendStringWithBreak(Page.Text1);

                    break;

                case "spaces":

                    PageData.AppendStringWithBreak(Page.Layout);
                    PageData.AppendInt32(1);
                    PageData.AppendStringWithBreak(Page.LayoutHeadline);
                    PageData.AppendInt32(1);
                    PageData.AppendStringWithBreak(Page.Text1);

                    break;

                case "recycler":

                    PageData.AppendStringWithBreak(Page.Layout);
                    PageData.AppendInt32(2);
                    PageData.AppendStringWithBreak(Page.LayoutHeadline);
                    PageData.AppendStringWithBreak(Page.LayoutTeaser);
                    PageData.AppendInt32(1);
                    PageData.AppendStringWithBreak(Page.Text1, 10);
                    PageData.AppendStringWithBreak(Page.Text2);
                    PageData.AppendStringWithBreak(Page.TextDetails);

                    break;

                case "trophies":

                    PageData.AppendStringWithBreak("trophies");
                    PageData.AppendInt32(1);
                    PageData.AppendStringWithBreak(Page.LayoutHeadline);
                    PageData.AppendInt32(2);
                    PageData.AppendStringWithBreak(Page.Text1);
                    PageData.AppendStringWithBreak(Page.TextDetails);

                    break;

                case "pets":

                    PageData.AppendStringWithBreak("pets");
                    PageData.AppendInt32(2);
                    PageData.AppendStringWithBreak(Page.LayoutHeadline);
                    PageData.AppendStringWithBreak(Page.LayoutTeaser);
                    PageData.AppendInt32(4);
                    PageData.AppendStringWithBreak(Page.Text1);
                    PageData.AppendStringWithBreak("Give a name:");
                    PageData.AppendStringWithBreak("Pick a color:");
                    PageData.AppendStringWithBreak("Pick a race:");

                    break;

                default:

                    PageData.AppendStringWithBreak(Page.Layout);
                    PageData.AppendInt32(3);
                    PageData.AppendStringWithBreak(Page.LayoutHeadline);
                    PageData.AppendStringWithBreak(Page.LayoutTeaser);
                    PageData.AppendStringWithBreak(Page.LayoutSpecial);
                    PageData.AppendInt32(3);
                    PageData.AppendStringWithBreak(Page.Text1);
                    PageData.AppendStringWithBreak(Page.TextDetails);
                    PageData.AppendStringWithBreak(Page.TextTeaser);


                    // A`jeAdefault_3x3Kctlg_windows_headline1_enctlg_windows_teaser1_encatalog_special_txtbg2KLet some sunshine in! Our windows come in many styles to give an unique look to Your room. Who said Your room does not have a view?Click on an item for more information.Ooh, new view!SC[}HDEV window_tripleQAHIiZpOIMY@IDEV window_basicPAHIiZrOIMX~HDEV window_squareJHIi[pOIMZuIDEV window_skyscraperQAHIi[uOIM[uIdeal_skyscraper1PCHIi[uOQAMXvIdeal_skyscraper2PEHIi[uORBMX@IDEV window_chinese_narrowKHIiYrOIM[HDEV window_chinese_wideQAHIiXrOIMYHDEV window_goldenKHIi[qOIMXHDEV window_grungeQAHIiZqOIMXpIDEV window_holeQAHIiZtOIM[~HDEV window_romantic_narrowKHIiYqOIMZ~HDEV window_romantic_widePAHIiXqOIM[@IDEV window_70s_narrowKHIiXsOIMZ@IDEV window_70s_widePAHIi[rOIM

                    break;
            }

            PageData.AppendInt32(Page.Items.Count);

            lock (Page.Items)
            {
                foreach (CatalogItem Item in Page.Items)
                {
                    Item.Serialize(PageData);
                }
            }

            return PageData;
        }

        public ServerMessage SerializeTestIndex()
        {
            ServerMessage Message = new ServerMessage(126);

            /*sm.AppendBoolean(Visible);
            sm.AppendInt32(IconColor);
            sm.AppendInt32(IconImage);
            sm.AppendInt32(Id);
            sm.AppendStringWithBreak(Caption);
            sm.AppendBoolean(ComingSoon);
            sm.AppendInt32(TreeData);*/

            Message.AppendInt32(0);
            Message.AppendInt32(0);
            Message.AppendInt32(0);
            Message.AppendInt32(-1);
            Message.AppendStringWithBreak("");
            Message.AppendInt32(0);
            Message.AppendInt32(100);

            for (int i = 1; i <= 150; i++)
            {
                Message.AppendInt32(1);
                Message.AppendInt32(i);
                Message.AppendInt32(i);
                Message.AppendInt32(i);
                Message.AppendStringWithBreak("#" + i);
                Message.AppendInt32(0);
                Message.AppendInt32(0);
            }

            return Message;

            // A~HHHMHSBIRAI`ieAFront PageHHHJKaieACollectablesHHIKJMFurni ShopHPJIHSMcieASpacesHHIHSO`jeAWindowsHHIHSEajeAMood LightsHHIHRJbjeATwilightHHIHRNcjeATeleportsHHIHSI`keAModeHHIHSDakeACandyHHIHPLbkeAPuraHHIHRCckeAAreaHHIHQE`leACountryHHIHQIaleALodgeHHIHRKbleAPlasticHHIHPPcleAKitchenHHIHQD`meABathroomHHIHQKameAPlantsHHIHPMbmeARugsHHIHRJcmeARollersHHIHSK`neAPosters and FlagsHHIHPOaneATrophiesHHIHSBbneAExtrasHHHHHcneACameraHHHHRB`oeALimited EditionHHHHPHaoeAUrban Crime SceneHHHHSCboeAOrientalHHHHPNcoeASportHHHQBQL`peARelaxHHHHRLapeARomantique WeddingHHHSBQMbpeASCI FIHHHQBHcpeAHabbo County USHHHHRF`qeAWacky WeekendsHHHHSFaqeAExecutiveHHHHRDbqeABensalemHHHHQCcqeAArcticHHHHPC`reAAlhambraHHHHSNareATiki !HHHHPPbreAChristmasHHHHRMcreAShalimarHHHHRO`seAValentine's LoveHHHHRHaseAHabboweenHHHHQHbseAMoviesHHISAPBMPets ShopHRAHHH`teAPetsHHIHPEateACatsHHIHPFbteADogsHHIHREcteACrocodilesHHIHSJ`ueAPets' AccessoriesHHIHRJaueAPets InfoHHIQAQAMPixel ShopHRAIHPGbweAPixel CollectableHHIHSLcweARentalsHHIHQO`xeASpecial EffectsHHHHPKaxeAPixel DiscountsHHIHSHbxeAHello FurniHHIHPDcxeAAutomobileHHISBRA`yeAHabbo ExchangeHHISAQBayeAHabbo ClubHIIHQBcyeABuy Habbo ClubHHIKSAMEcotronHKIHSAazeAEcotronHHIHRFbzeARewardsHHIHRJczeAInstructionsHHISARB`{eASpecial OffersHHHHHa{eAmagic.creditsHHHHHb{eAmagic.pixelsHH
        }

        public VoucherHandler GetVoucherHandler()
        {
            return VoucherHandler;
        }

        public Marketplace GetMarketplace()
        {
            return Marketplace;
        }
    }
}
