using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Storage;
using Uber.HabboHotel.Users;

namespace Uber.HabboHotel.Roles
{
    class RoleManager
    {
        private Dictionary<uint, Role> Roles;
        private Dictionary<string, uint> Rights;
        private Dictionary<string, string> SubRights;

        public RoleManager()
        {
            Roles = new Dictionary<uint,Role>();
            Rights = new Dictionary<string, uint>();
            SubRights = new Dictionary<string, string>();
        }

        public void LoadRoles()
        {
            ClearRoles();

            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT * FROM ranks ORDER BY id ASC");
            }

            if (Data != null)
            {
                foreach (DataRow Row in Data.Rows)
                {
                    Roles.Add((uint)Row["id"], new Role((uint)Row["id"], (string)Row["name"]));
                }
            }
        }

        public void LoadRights()
        {
            ClearRights();

            DataTable Data = null;
            DataTable SubData = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT * FROM fuserights");
                SubData = dbClient.ReadDataTable("SELECT * FROM fuserights_subs");
            }

            if (Data != null)
            {
                foreach (DataRow Row in Data.Rows)
                {
                    Rights.Add((string)Row["fuse"].ToString().ToLower(), (uint)Row["rank"]);
                }
            }

            if (SubData != null)
            {
                foreach (DataRow Row in SubData.Rows)
                {
                    SubRights.Add((string)Row["fuse"], (string)Row["sub"]);
                }
            }
        }

        public Boolean RankHasRight(uint RankId, string Fuse)
        {
            if (!ContainsRight(Fuse))
            {
                return false;
            }

            uint MinRank = Rights[Fuse];

            if (RankId >= MinRank)
            {
                return true;
            }

            return false;
        }

        public bool SubHasRight(string Sub, string Fuse)
        {
            if (this.SubRights.ContainsKey(Fuse) && this.SubRights[Fuse] == Sub)
            {
                return true;
            }

            return false;
        }

        public Role GetRole(uint Id)
        {
            if (!ContainsRole(Id))
            {
                return null;
            }

            return Roles[Id];
        }

        public List<string> GetRightsForHabbo(Habbo Habbo)
        {
            List<string> UserRights = new List<string>();

            UserRights.AddRange(GetRightsForRank(Habbo.Rank));

            foreach (string SubscriptionId in Habbo.GetSubscriptionManager().SubList)
            {
                UserRights.AddRange(GetRightsForSub(SubscriptionId));
            }

            return UserRights;
        }

        public List<string> GetRightsForRank(uint RankId)
        {
            List<string> UserRights = new List<string>();

            lock (Rights)
            {
                foreach (KeyValuePair<string, uint> Data in Rights)
                {
                    if (RankId >= Data.Value && !UserRights.Contains(Data.Key))
                    {
                        UserRights.Add(Data.Key);
                    }
                }
            }

            return UserRights;
        }

        public List<string> GetRightsForSub(string SubId)
        {
            List<string> UserRights = new List<string>();

            lock (Rights)
            {
                foreach (KeyValuePair<string, string> Data in SubRights)
                {
                    if (Data.Value == SubId)
                    {
                        UserRights.Add(Data.Key);
                    }
                }
            }

            return UserRights;
        }

        public Boolean ContainsRole(uint Id)
        {
            return Roles.ContainsKey(Id);
        }

        public Boolean ContainsRight(string Right)
        {
            return Rights.ContainsKey(Right);
        }

        public void ClearRoles()
        {
            Roles.Clear();
        }

        public void ClearRights()
        {
            Rights.Clear();
        }
    }
}
