using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.HabboHotel.Users.Badges;
using Uber.HabboHotel.Rooms;
using Uber.HabboHotel.Misc;
using Uber.Storage;

namespace Uber.Messages
{
    partial class GameClientMessageHandler
    {
        private void GetUserInfo()
        {
            // @E276875100dmethlg-270-64.hr-165-32.ch-260-64.sh-300-64.hd-205-2MRoy de JongHHHHHH
            // @E276875100dmethsh-905-62.fa-1201-.ha-1018-.hr-893-42.lg-280-64.hd-190-1.ch-260-74MRoy de JongHHHPBJK

            GetResponse().Init(5);
            GetResponse().AppendStringWithBreak(Session.GetHabbo().Id.ToString());
            GetResponse().AppendStringWithBreak(Session.GetHabbo().Username);
            GetResponse().AppendStringWithBreak(Session.GetHabbo().Look);
            GetResponse().AppendStringWithBreak(Session.GetHabbo().Gender.ToUpper());
            GetResponse().AppendStringWithBreak(Session.GetHabbo().Motto);
            GetResponse().AppendStringWithBreak(Session.GetHabbo().RealName);
            GetResponse().AppendInt32(0);
            GetResponse().AppendStringWithBreak("");
            GetResponse().AppendInt32(0);
            GetResponse().AppendInt32(0);
            GetResponse().AppendInt32(Session.GetHabbo().Respect);
            GetResponse().AppendInt32(Session.GetHabbo().DailyRespectPoints); // respect to give away
            GetResponse().AppendInt32(Session.GetHabbo().DailyPetRespectPoints);
            SendResponse();
        }

        private void GetBalance()
        {
            Session.GetHabbo().UpdateCreditsBalance(false);
            Session.GetHabbo().UpdateActivityPointsBalance(false);
        }

        private void GetSubscriptionData()
        {
            string SubscriptionId = Request.PopFixedString();

            GetResponse().Init(7);
            GetResponse().AppendStringWithBreak(SubscriptionId.ToLower());

            if (Session.GetHabbo().GetSubscriptionManager().HasSubscription(SubscriptionId))
            {
                Double Expire = Session.GetHabbo().GetSubscriptionManager().GetSubscription(SubscriptionId).ExpireTime;
                Double TimeLeft = Expire - UberEnvironment.GetUnixTimestamp();
                int TotalDaysLeft = (int)Math.Ceiling(TimeLeft / 86400);
                int MonthsLeft = TotalDaysLeft / 31;

                if (MonthsLeft >= 1) MonthsLeft--;

                GetResponse().AppendInt32(TotalDaysLeft - (MonthsLeft * 31));
                GetResponse().AppendBoolean(true);
                GetResponse().AppendInt32(MonthsLeft);
            }
            else
            {
                for (int i = 0; i < 3; i++)
                {
                    GetResponse().AppendInt32(0);
                }
            }

            SendResponse();
        }

        private void GetBadges()
        {
            Session.SendMessage(Session.GetHabbo().GetBadgeComponent().Serialize());
        }

        private void UpdateBadges()
        {
            Session.GetHabbo().GetBadgeComponent().ResetSlots();

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("UPDATE user_badges SET badge_slot = '0' WHERE user_id = '" + Session.GetHabbo().Id + "'");
            }

            while (Request.RemainingLength > 0)
            {
                int Slot = Request.PopWiredInt32();
                string Badge = Request.PopFixedString();

                if (Badge.Length == 0)
                {
                    continue;
                }

                if (!Session.GetHabbo().GetBadgeComponent().HasBadge(Badge) || Slot < 1 || Slot > 5)
                {
                    // zomg haxx0r
                    return;
                }

                Session.GetHabbo().GetBadgeComponent().GetBadge(Badge).Slot = Slot;

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.AddParamWithValue("slotid", Slot);
                    dbClient.AddParamWithValue("badge", Badge);
                    dbClient.AddParamWithValue("userid", Session.GetHabbo().Id);
                    dbClient.ExecuteQuery("UPDATE user_badges SET badge_slot = @slotid WHERE badge_id = @badge AND user_id = @userid LIMIT 1");
                }
            }

            ServerMessage Message = new ServerMessage(228);
            Message.AppendUInt(Session.GetHabbo().Id);
            Message.AppendInt32(Session.GetHabbo().GetBadgeComponent().EquippedCount);

            foreach (Badge Badge in Session.GetHabbo().GetBadgeComponent().BadgeList)
            {
                if (Badge.Slot <= 0)
                {
                    continue;
                }

                Message.AppendInt32(Badge.Slot);
                Message.AppendStringWithBreak(Badge.Code);
            }

            if (Session.GetHabbo().InRoom && UberEnvironment.GetGame().GetRoomManager().GetRoom(Session.GetHabbo().CurrentRoomId) != null)
            {
                UberEnvironment.GetGame().GetRoomManager().GetRoom(Session.GetHabbo().CurrentRoomId).SendMessage(Message);
            }
            else
            {
                Session.SendMessage(Message);
            }
        }

        private void GetAchievements()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetAchievementManager().SerializeAchievementList(Session));
        }

        private void ChangeLook()
        {
            if (Session.GetHabbo().MutantPenalty)
            {
                Session.SendNotif("Because of a penalty or restriction on your account, you are not allowed to change your look.");
                return;
            }

            string Gender = Request.PopFixedString().ToUpper();
            string Look = UberEnvironment.FilterInjectionChars(Request.PopFixedString());

            if (!AntiMutant.ValidateLook(Look, Gender))
            {
                return;
            }

            Session.GetHabbo().Look = Look;
            Session.GetHabbo().Gender = Gender.ToLower();

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("look", Look);
                dbClient.AddParamWithValue("gender", Gender);
                dbClient.ExecuteQuery("UPDATE users SET look = @look, gender = @gender WHERE id = '" + Session.GetHabbo().Id + "' LIMIT 1");
            }

            Session.GetMessageHandler().GetResponse().Init(266);
            Session.GetMessageHandler().GetResponse().AppendInt32(-1);
            Session.GetMessageHandler().GetResponse().AppendStringWithBreak(Session.GetHabbo().Look);
            Session.GetMessageHandler().GetResponse().AppendStringWithBreak(Session.GetHabbo().Gender.ToLower());
            Session.GetMessageHandler().GetResponse().AppendStringWithBreak(Session.GetHabbo().Motto);
            Session.GetMessageHandler().SendResponse();

            if (Session.GetHabbo().InRoom)
            {
                Room Room = Session.GetHabbo().CurrentRoom;

                if (Room == null)
                {
                    return;
                }

                RoomUser User = Room.GetRoomUserByHabbo(Session.GetHabbo().Id);

                if (User == null)
                {
                    return;
                }

                ServerMessage RoomUpdate = new ServerMessage(266);
                RoomUpdate.AppendInt32(User.VirtualId);
                RoomUpdate.AppendStringWithBreak(Session.GetHabbo().Look);
                RoomUpdate.AppendStringWithBreak(Session.GetHabbo().Gender.ToLower());
                RoomUpdate.AppendStringWithBreak(Session.GetHabbo().Motto);
                Room.SendMessage(RoomUpdate);
            }
        }

        private void GetWardrobe()
        {
            GetResponse().Init(267);
            GetResponse().AppendBoolean(Session.GetHabbo().HasFuse("fuse_use_wardrobe"));

            if (Session.GetHabbo().HasFuse("fuse_use_wardrobe"))
            {
                DataTable WardrobeData = null;

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.AddParamWithValue("userid", Session.GetHabbo().Id);
                    WardrobeData = dbClient.ReadDataTable("SELECT * FROM user_wardrobe WHERE user_id = @userid");
                }

                if (WardrobeData == null)
                {
                    GetResponse().AppendInt32(0);
                }
                else
                {
                    GetResponse().AppendInt32(WardrobeData.Rows.Count);

                    foreach (DataRow Row in WardrobeData.Rows)
                    {
                        GetResponse().AppendUInt((uint)Row["slot_id"]);
                        GetResponse().AppendStringWithBreak((string)Row["look"]);
                        GetResponse().AppendStringWithBreak((string)Row["gender"]);
                    }
                }
            }

            SendResponse();
        }

        private void SaveWardrobe()
        {
            uint SlotId = Request.PopWiredUInt();

            string Look = Request.PopFixedString();
            string Gender = Request.PopFixedString();

            if (!AntiMutant.ValidateLook(Look, Gender))
            {
                return;
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("userid", Session.GetHabbo().Id);
                dbClient.AddParamWithValue("slotid", SlotId);
                dbClient.AddParamWithValue("look", Look);
                dbClient.AddParamWithValue("gender", Gender.ToUpper());

                if (dbClient.ReadDataRow("SELECT null FROM user_wardrobe WHERE user_id = @userid AND slot_id = @slotid LIMIT 1") != null)
                {
                    dbClient.ExecuteQuery("UPDATE user_wardrobe SET look = @look, gender = @gender WHERE user_id = @userid AND slot_id = @slotid LIMIT 1");
                }
                else
                {
                    dbClient.ExecuteQuery("INSERT INTO user_wardrobe (user_id,slot_id,look,gender) VALUES (@userid,@slotid,@look,@gender)");
                }
            }
        }

        private void GetPetsInventory()
        {
            if (Session.GetHabbo().GetInventoryComponent() == null)
            {
                return;
            }

            Session.SendMessage(Session.GetHabbo().GetInventoryComponent().SerializePetInventory());
        }

        public void RegisterUsers()
        {
            RequestHandlers[7] = new RequestHandler(GetUserInfo);
            RequestHandlers[8] = new RequestHandler(GetBalance);
            RequestHandlers[26] = new RequestHandler(GetSubscriptionData);

            RequestHandlers[157] = new RequestHandler(GetBadges);
            RequestHandlers[158] = new RequestHandler(UpdateBadges);
            RequestHandlers[370] = new RequestHandler(GetAchievements);

            RequestHandlers[44] = new RequestHandler(ChangeLook);
            RequestHandlers[375] = new RequestHandler(GetWardrobe);
            RequestHandlers[376] = new RequestHandler(SaveWardrobe);

            RequestHandlers[404] = new RequestHandler(GetInventory);
            RequestHandlers[3000] = new RequestHandler(GetPetsInventory);
        }
    }
}
