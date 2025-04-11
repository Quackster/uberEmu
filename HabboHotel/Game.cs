using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

using Uber.HabboHotel.Misc;
using Uber.HabboHotel.Catalogs;
using Uber.HabboHotel.GameClients;
using Uber.HabboHotel.Roles;
using Uber.HabboHotel.Support;
using Uber.HabboHotel.Navigators;
using Uber.HabboHotel.Items;
using Uber.HabboHotel.Rooms;
using Uber.HabboHotel.Advertisements;
using Uber.HabboHotel.Achievements;
using Uber.HabboHotel.RoomBots;
using Uber.Storage;

namespace Uber.HabboHotel
{
    class Game
    {
        private const string Version = "RELEASE48-25528-25547-201003230310";

        private GameClientManager ClientManager;
        private ModerationBanManager BanManager;
        private RoleManager RoleManager;
        private HelpTool HelpTool;
        private Catalog Catalog;
        private Navigator Navigator;
        private ItemManager ItemManager;
        private RoomManager RoomManager;
        private AdvertisementManager AdvertisementManager;
        private PixelManager PixelManager;
        private AchievementManager AchievementManager;
        private ModerationTool ModerationTool;
        private BotManager BotManager;
        private Thread StatisticsThread;

        public Game()
        {
            ClientManager = new GameClientManager();

            if (UberEnvironment.GetConfig().data["client.ping.enabled"] == "1")
            {
                ClientManager.StartConnectionChecker();
            }

            BanManager = new ModerationBanManager();
            RoleManager = new RoleManager();
            HelpTool = new HelpTool();
            Catalog = new Catalog();
            Navigator = new Navigator();
            ItemManager = new ItemManager();
            RoomManager = new RoomManager();
            AdvertisementManager = new AdvertisementManager();
            PixelManager = new PixelManager();
            AchievementManager = new AchievementManager();
            ModerationTool = new ModerationTool();
            BotManager = new BotManager();

            BanManager.LoadBans();
            RoleManager.LoadRoles();
            RoleManager.LoadRights();
            HelpTool.LoadCategories();
            HelpTool.LoadTopics();
            Catalog.Initialize();
            Navigator.Initialize();
            ItemManager.LoadItems();
            RoomManager.LoadModels();
            AdvertisementManager.LoadRoomAdvertisements();
            PixelManager.Start();
            AchievementManager.LoadAchievements();
            ModerationTool.LoadMessagePresets();
            ModerationTool.LoadPendingTickets();
            BotManager.LoadBots();

            DatabaseCleanup(1);

            StatisticsThread = new Thread(LowPriorityWorker.Process);
            StatisticsThread.Name = "Low Priority Worker";
            StatisticsThread.Priority = ThreadPriority.Lowest;
            StatisticsThread.Start();

            UberEnvironment.GetLogging().WriteLine("Initialized Habbo Hotel, " + Version + ".");
        }

        public void DatabaseCleanup(int serverStatus)
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("UPDATE users SET auth_ticket = '', online = '0'");
                dbClient.ExecuteQuery("UPDATE rooms SET users_now = '0'");
                dbClient.ExecuteQuery("UPDATE user_roomvisits SET exit_timestamp = '" + UberEnvironment.GetUnixTimestamp() + "' WHERE exit_timestamp <= 0");
                dbClient.ExecuteQuery("UPDATE server_status SET status = '" + serverStatus + "', users_online = '0', rooms_loaded = '0', server_ver = '" + UberEnvironment.PrettyVersion + "', stamp = '" + UberEnvironment.GetUnixTimestamp() + "' LIMIT 1");
                // todo: more db cleanup stuff?
            }

            UberEnvironment.GetLogging().WriteLine("Performed database cleanup", Uber.Core.LogLevel.Debug);
        }

        public void Destroy()
        {
            if (StatisticsThread != null)
            {
                try
                {
                    StatisticsThread.Abort();
                }

                catch (ThreadAbortException) { }

                StatisticsThread = null;
            }

            DatabaseCleanup(0);

            if (GetClientManager() != null)
            {
                GetClientManager().Clear();
                GetClientManager().StopConnectionChecker();
            }

            if (GetPixelManager() != null)
            {
                PixelManager.KeepAlive = false;
            }

            ClientManager = null;
            BanManager = null;
            RoleManager = null;
            HelpTool = null;
            Catalog = null;
            Navigator = null;
            ItemManager = null;
            RoomManager = null;
            AdvertisementManager = null;
            PixelManager = null;

            UberEnvironment.GetLogging().WriteLine("Destroyed Habbo Hotel.");
        }

        public GameClientManager GetClientManager()
        {
            return ClientManager;
        }

        public ModerationBanManager GetBanManager()
        {
            return BanManager;
        }

        public RoleManager GetRoleManager()
        {
            return RoleManager;
        }

        public HelpTool GetHelpTool()
        {
            return HelpTool;
        }

        public Catalog GetCatalog()
        {
            return Catalog;
        }

        public Navigator GetNavigator()
        {
            return Navigator;
        }

        public ItemManager GetItemManager()
        {
            return ItemManager;
        }

        public RoomManager GetRoomManager()
        {
            return RoomManager;
        }

        public AdvertisementManager GetAdvertisementManager()
        {
            return AdvertisementManager;
        }

        public PixelManager GetPixelManager()
        {
            return PixelManager;
        }

        public AchievementManager GetAchievementManager()
        {
            return AchievementManager;
        }

        public ModerationTool GetModerationTool()
        {
            return ModerationTool;
        }

        public BotManager GetBotManager()
        {
            return BotManager;
        }
    }
}
