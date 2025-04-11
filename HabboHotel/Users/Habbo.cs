using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.HabboHotel.Items;
using Uber.HabboHotel.Users.Badges;
using Uber.HabboHotel.Users.Subscriptions;
using Uber.HabboHotel.Users.Messenger;
using Uber.HabboHotel.Users.Inventory;
using Uber.HabboHotel.Rooms;
using Uber.HabboHotel.GameClients;
using Uber.Storage;

namespace Uber.HabboHotel.Users
{
    class Habbo
    {
        public uint Id;

        public string Username;
        public string RealName;

        public string AuthTicket;

        public uint Rank;

        public string Motto;
        public string Look;
        public string Gender;

        public int Credits;
        public int ActivityPoints;
        public Double LastActivityPointsUpdate;

        public bool Muted;

        public int Respect;
        public int DailyRespectPoints;
        public int DailyPetRespectPoints;

        public uint LoadingRoom;
        public Boolean LoadingChecksPassed;
        public uint CurrentRoomId;
        public uint HomeRoom;

        public bool IsTeleporting;
        public uint TeleporterId;

        public List<uint> FavoriteRooms;
        public List<uint> MutedUsers;
        public List<string> Tags;
        public Dictionary<uint, int> Achievements;
        public List<uint> RatedRooms;

        private SubscriptionManager SubscriptionManager;
        private HabboMessenger Messenger;
        private BadgeComponent BadgeComponent;
        private InventoryComponent InventoryComponent;
        private AvatarEffectsInventoryComponent AvatarEffectsInventoryComponent;

        public int NewbieStatus;
        public bool SpectatorMode;
        public bool Disconnected;

        public bool CalledGuideBot;
        public bool MutantPenalty;

        public bool BlockNewFriends;

        public Boolean InRoom
        {
            get
            {
                if (CurrentRoomId >= 1)
                {
                    return true;
                }

                return false;
            }
        }

        public Room CurrentRoom
        {
            get
            {
                if (CurrentRoomId <= 0)
                {
                    return null;
                }

                return UberEnvironment.GetGame().GetRoomManager().GetRoom(CurrentRoomId);
            }
        }

        public Habbo(uint Id, string Username, string RealName, string AuthTicket, uint Rank, string Motto, string Look, string Gender, int Credits, int ActivityPoints, Double LastActivityPointsUpdate, bool Muted, uint HomeRoom, int Respect, int DailyRespectPoints, int DailyPetRespectPoints, int NewbieStatus, bool MutantPenalty, bool BlockNewFriends)
        {
            this.Id = Id;
            this.Username = Username;
            this.RealName = RealName;
            this.AuthTicket = AuthTicket;
            this.Rank = Rank;
            this.Motto = Motto;
            this.Look = Look.ToLower();
            this.Gender = Gender.ToLower();
            this.Credits = Credits;
            this.ActivityPoints = ActivityPoints;
            this.LastActivityPointsUpdate = LastActivityPointsUpdate;
            this.Muted = Muted;
            this.LoadingRoom = 0;
            this.LoadingChecksPassed = false;
            this.CurrentRoomId = 0;
            this.HomeRoom = HomeRoom;
            this.FavoriteRooms = new List<uint>();
            this.MutedUsers = new List<uint>();
            this.Tags = new List<string>();
            this.Achievements = new Dictionary<uint, int>();
            this.RatedRooms = new List<uint>();
            this.Respect = Respect;
            this.DailyRespectPoints = DailyRespectPoints;
            this.DailyPetRespectPoints = DailyPetRespectPoints;
            this.NewbieStatus = NewbieStatus;
            this.CalledGuideBot = false;
            this.MutantPenalty = MutantPenalty;
            this.BlockNewFriends = BlockNewFriends;

            this.IsTeleporting = false;
            this.TeleporterId = 0;

            SubscriptionManager = new SubscriptionManager(Id);
            BadgeComponent = new BadgeComponent(Id);
            InventoryComponent = new InventoryComponent(Id);
            AvatarEffectsInventoryComponent = new AvatarEffectsInventoryComponent(Id);

            this.SpectatorMode = false;
            this.Disconnected = false;

            UberEnvironment.GetLogging().WriteLine(Username + " has logged in.", Core.LogLevel.Debug);
        }

        public void LoadData()
        {
            SubscriptionManager.LoadSubscriptions();
            BadgeComponent.LoadBadges();
            InventoryComponent.LoadInventory();
            AvatarEffectsInventoryComponent.LoadEffects();

            LoadAchievements();
            LoadFavorites();
            LoadMutedUsers();
            LoadTags();
        }

        public bool HasFuse(string Fuse)
        {
            if (UberEnvironment.GetGame().GetRoleManager().RankHasRight(Rank, Fuse))
            {
                return true;
            }

            foreach (string SubscriptionId in GetSubscriptionManager().SubList)
            {
                if (UberEnvironment.GetGame().GetRoleManager().SubHasRight(SubscriptionId, Fuse))
                {
                    return true;
                }
            }

            return false;
        }

        public void LoadFavorites()
        {
            this.FavoriteRooms.Clear();
            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT room_id FROM user_favorites WHERE user_id = '" + Id + "'");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                FavoriteRooms.Add((uint)Row["room_id"]);
            }
        }

        public void LoadMutedUsers()
        {
            this.MutedUsers.Clear();
            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT ignore_id FROM user_ignores WHERE user_id = '" + Id + "'");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                MutedUsers.Add((uint)Row["ignore_id"]);
            }
        }

        public void LoadTags()
        {
            this.Tags.Clear();
            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT tag FROM user_tags WHERE user_id = '" + Id + "'");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                Tags.Add((string)Row["tag"]);
            }

            if (Tags.Count >= 5)
            {
                UberEnvironment.GetGame().GetAchievementManager().UnlockAchievement(GetClient(), 7, 1);
            }
        }

        public void LoadAchievements()
        {
            this.Achievements.Clear();
            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT achievement_id,achievement_level FROM user_achievements WHERE user_id = '" + Id + "'");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                Achievements.Add((uint)Row["achievement_id"], (int)Row["achievement_level"]);
            }
        }

        public void OnDisconnect()
        {
            if (this.Disconnected)
            {
                return;
            }

            UberEnvironment.GetLogging().WriteLine(Username + " has logged out.", Core.LogLevel.Debug);

            this.Disconnected = true;
            DateTime Now = DateTime.Now;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("UPDATE users SET last_online = '" + Now.ToString() + "', online = '0' WHERE id = '" + Id + "' LIMIT 1");
            }

            if (InRoom)
            {
                UberEnvironment.GetGame().GetRoomManager().GetRoom(CurrentRoomId).RemoveUserFromRoom(GetClient(), false, false);
            }

            if (Messenger != null)
            {
                Messenger.AppearOffline = true;
                Messenger.OnStatusChanged(true);
                Messenger = null;
            }

            if (SubscriptionManager != null)
            {
                SubscriptionManager.Clear();
                SubscriptionManager = null;
            }

            // todo: drop events, kick bots, etc
        }

        public void OnEnterRoom(uint RoomId)
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("INSERT INTO user_roomvisits (user_id,room_id,entry_timestamp,exit_timestamp,hour,minute) VALUES ('" + Id + "','" + RoomId + "','" + UberEnvironment.GetUnixTimestamp() + "','0','" + DateTime.Now.Hour + "','" + DateTime.Now.Minute + "')");
            }

            this.CurrentRoomId = RoomId;

            Messenger.OnStatusChanged(false);
        }

        public void OnLeaveRoom()
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("UPDATE user_roomvisits SET exit_timestamp = '" + UberEnvironment.GetUnixTimestamp() + "' WHERE room_id = '" + this.CurrentRoomId + "' AND user_id = '" + Id + "' ORDER BY entry_timestamp DESC LIMIT 1");
            }

            this.CurrentRoomId = 0;

            if (Messenger != null)
            {
                Messenger.OnStatusChanged(false);
            }
        }

        public void InitMessenger()
        {
            if (GetMessenger() != null)
            {
                return;
            }

            Messenger = new HabboMessenger(Id);

            Messenger.LoadBuddies();
            Messenger.LoadRequests();

            GetClient().SendMessage(Messenger.SerializeFriends());
            GetClient().SendMessage(Messenger.SerializeRequests());

            Messenger.OnStatusChanged(true);
        }

        public void UpdateCreditsBalance(Boolean InDatabase)
        {
            GetClient().GetMessageHandler().GetResponse().Init(6);
            GetClient().GetMessageHandler().GetResponse().AppendStringWithBreak(Credits + ".0");
            GetClient().GetMessageHandler().SendResponse();

            if (InDatabase)
            {
                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE users SET credits = '" + Credits + "' WHERE id = '" + Id + "' LIMIT 1");
                }
            }
        }

        public void UpdateActivityPointsBalance(Boolean InDatabase)
        {
            UpdateActivityPointsBalance(InDatabase, 0);
        }

        public void UpdateActivityPointsBalance(Boolean InDatabase, int NotifAmount)
        {
            GetClient().GetMessageHandler().GetResponse().Init(438);
            GetClient().GetMessageHandler().GetResponse().AppendInt32(ActivityPoints);
            GetClient().GetMessageHandler().GetResponse().AppendInt32(NotifAmount);
            GetClient().GetMessageHandler().SendResponse();

            if (InDatabase)
            {
                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE users SET activity_points = '" + ActivityPoints + "', activity_points_lastupdate = '" + LastActivityPointsUpdate + "' WHERE id = '" + Id + "' LIMIT 1");
                }
            }
        }

        public void Mute()
        {
            if (!this.Muted)
            {
                GetClient().SendNotif("You have been muted by an moderator.");
                this.Muted = true;
            }
        }

        public void Unmute()
        {
            if (this.Muted)
            {
                GetClient().SendNotif("You have been unmuted by an moderator.");
                this.Muted = false;
            }
        }

        private GameClient GetClient()
        {
            return UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(Id);
        }

        public SubscriptionManager GetSubscriptionManager()
        {
            return SubscriptionManager;
        }

        public HabboMessenger GetMessenger()
        {
            return Messenger;
        }

        public BadgeComponent GetBadgeComponent()
        {
            return BadgeComponent;
        }

        public InventoryComponent GetInventoryComponent()
        {
            return InventoryComponent;
        }

        public AvatarEffectsInventoryComponent GetAvatarEffectsInventoryComponent()
        {
            return AvatarEffectsInventoryComponent;
        }
    }
}
