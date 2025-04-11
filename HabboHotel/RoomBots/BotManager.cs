using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.HabboHotel.Rooms;
using Uber.Storage;

namespace Uber.HabboHotel.RoomBots
{
    class BotManager
    {
        private List<RoomBot> Bots;

        public BotManager()
        {
            Bots = new List<RoomBot>();
        }

        public void LoadBots()
        {
            Bots = new List<RoomBot>();

            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT * FROM bots");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                Bots.Add(new RoomBot((uint)Row["id"], (uint)Row["room_id"], (string)Row["ai_type"], (string)Row["walk_mode"],
                    (String)Row["name"], (string)Row["motto"], (String)Row["look"], (int)Row["x"], (int)Row["y"], (int)Row["z"],
                    (int)Row["rotation"], (int)Row["min_x"], (int)Row["min_y"], (int)Row["max_x"], (int)Row["max_y"]));
            }
        }

        public bool RoomHasBots(uint RoomId)
        {
            return (GetBotsForRoom(RoomId).Count >= 1);
        }

        public List<RoomBot> GetBotsForRoom(uint RoomId)
        {
            List<RoomBot> List = new List<RoomBot>();

            lock (Bots)
            {
                foreach (RoomBot Bot in Bots)
                {
                    if (Bot.RoomId == RoomId)
                    {
                        List.Add(Bot);
                    }
                }
            }

            return List;
        }

        public RoomBot GetBot(uint BotId)
        {
            lock (Bots)
            {
                foreach (RoomBot Bot in Bots)
                {
                    if (Bot.BotId == BotId)
                    {
                        return Bot;
                    }
                }
            }

            return null;
        }
    }
}
