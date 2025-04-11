using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Storage;
using System.Collections.Concurrent;

namespace Uber.HabboHotel.Users.Subscriptions
{
    class SubscriptionManager
    {
        private uint UserId;
        private ConcurrentDictionary<string, Subscription> Subscriptions;

        public SynchronizedCollection<string> SubList
        {
            get
            {
                SynchronizedCollection<string> List = new SynchronizedCollection<string>();

                    foreach (Subscription Subscription in Subscriptions.Values)
                    {
                        List.Add(Subscription.SubscriptionId);
                    }

                return List;
            }
        }

        public SubscriptionManager(uint UserId)
        {
            this.UserId = UserId;

            Subscriptions = new ConcurrentDictionary<string, Subscription>();
        }

        public void LoadSubscriptions()
        {
            DataTable SubscriptionData = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                SubscriptionData = dbClient.ReadDataTable("SELECT * FROM user_subscriptions WHERE user_id = '" + UserId + "'");
            }

            if (SubscriptionData != null)
            {
                foreach (DataRow Row in SubscriptionData.Rows)
                {
                    Subscriptions.TryAdd((string)Row["subscription_id"], new Subscription((string)Row["subscription_id"], (int)Row["timestamp_activated"], (int)Row["timestamp_expire"]));
                }
            }
        }

        public void Clear()
        {
            Subscriptions.Clear();
        }

        public Subscription GetSubscription(string SubscriptionId)
        {
            if (Subscriptions.ContainsKey(SubscriptionId))
            {
                return Subscriptions[SubscriptionId];
            }

            return null;
        }

        public Boolean HasSubscription(string SubscriptionId)
        {
            if (!Subscriptions.ContainsKey(SubscriptionId))
            {
                return false;
            }

            Subscription Sub = Subscriptions[SubscriptionId];

            if (Sub.IsValid())
            {
                return true;
            }

            return false;
        }

        public void AddOrExtendSubscription(string SubscriptionId, int DurationSeconds)
        {
            SubscriptionId = SubscriptionId.ToLower();

            if (Subscriptions.ContainsKey(SubscriptionId))
            {
                Subscription Sub = Subscriptions[SubscriptionId];
                Sub.ExtendSubscription(DurationSeconds);

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE user_subscriptions SET timestamp_expire = '" + Sub.ExpireTime + "' WHERE user_id = '" + UserId + "' AND subscription_id = '" + SubscriptionId + "' LIMIT 1");
                }

                return;
            }

            int TimeCreated = (int)UberEnvironment.GetUnixTimestamp();
            int TimeExpire = ((int)UberEnvironment.GetUnixTimestamp() + DurationSeconds);

            Subscription NewSub = new Subscription(SubscriptionId, TimeCreated, TimeExpire);

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("INSERT INTO user_subscriptions (user_id,subscription_id,timestamp_activated,timestamp_expire) VALUES ('" + UserId + "','" + SubscriptionId + "','" + TimeCreated + "','" + TimeExpire + "')");
            }

            Subscriptions.TryAdd(NewSub.SubscriptionId.ToLower(), NewSub);
        }
    }
}
