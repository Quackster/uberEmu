using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.HabboHotel.GameClients;
using Uber.Storage;

namespace Uber.HabboHotel.Support
{
    class ModerationBanManager
    {
        public List<ModerationBan> Bans;

        public ModerationBanManager()
        {
            Bans = new List<ModerationBan>();
        }

        public void LoadBans()
        {
            Bans.Clear();

            DataTable BanData = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                BanData = dbClient.ReadDataTable("SELECT bantype,value,reason,expire FROM bans WHERE expire > '" + UberEnvironment.GetUnixTimestamp() + "'");
            }

            if (BanData == null)
            {
                return;
            }

            foreach (DataRow Row in BanData.Rows)
            {
                ModerationBanType Type = ModerationBanType.IP;

                if ((string)Row["bantype"] == "user")
                {
                    Type = ModerationBanType.USERNAME;
                }

                Bans.Add(new ModerationBan(Type, (string)Row["value"], (string)Row["reason"], (Double)Row["expire"]));
            }
        }

        public void CheckForBanConflicts(GameClient Client)
        {
            lock (Bans)
            {
                foreach (ModerationBan Ban in Bans)
                {
                    if (Ban.Expired)
                    {
                        continue;
                    }

                    if (Ban.Type == ModerationBanType.IP && Client.GetConnection().IPAddress == Ban.Variable)
                    {
                        throw new ModerationBanException(Ban.ReasonMessage);
                    }

                    if (Client.GetHabbo() != null)
                    {
                        if (Ban.Type == ModerationBanType.USERNAME && Client.GetHabbo().Username.ToLower() == Ban.Variable.ToLower())
                        {
                            throw new ModerationBanException(Ban.ReasonMessage);
                        }
                    }
                }
            }
        }

        // PENDING REWRITE
        public void BanUser(GameClient Client, string Moderator, Double LengthSeconds, string Reason, Boolean IpBan)
        {
            ModerationBanType Type = ModerationBanType.USERNAME;
            string Var = Client.GetHabbo().Username;
            string RawVar = "user";
            Double Expire = UberEnvironment.GetUnixTimestamp() + LengthSeconds;

            if (IpBan)
            {
                Type = ModerationBanType.IP;
                Var = Client.GetConnection().IPAddress;
                RawVar = "ip";
            }

            Bans.Add(new ModerationBan(Type, Var, Reason, Expire));

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("INSERT INTO bans (bantype,value,reason,expire,added_by,added_date) VALUES ('" + RawVar + "','" + Var + "','" + Reason + "','" + Expire + "','" + Moderator + "','" + DateTime.Now.ToLongDateString() + "')");
            }

            if (IpBan)
            {
                DataTable UsersAffected = null;

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    UsersAffected = dbClient.ReadDataTable("SELECT id FROM users WHERE ip_last = '" + Var + "'");
                }

                if (UsersAffected != null)
                {
                    foreach (DataRow Row in UsersAffected.Rows)
                    {
                        using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                        {
                            dbClient.ExecuteQuery("UPDATE user_info SET bans = bans + 1 WHERE user_id = '" + (uint)Row["id"] + "' LIMIT 1");
                        }
                    }
                }
            }
            else
            {
                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE user_info SET bans = bans + 1 WHERE user_id = '" + Client.GetHabbo().Id + "' LIMIT 1");
                }
            }

            Client.SendBanMessage("You have been banned: " + Reason);
            Client.Disconnect();
        }
    }
}
