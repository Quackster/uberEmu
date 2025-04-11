using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.Users.Subscriptions
{
    class Subscription
    {
        private string Caption;

        private int TimeActivated;
        private int TimeExpire;

        public string SubscriptionId
        {
            get
            {
                return Caption;
            }
        }

        public int ExpireTime
        {
            get
            {
                return TimeExpire;
            }
        }

        public Subscription(string Caption, int TimeActivated, int TimeExpire)
        {
            this.Caption = Caption;
            this.TimeActivated = TimeActivated;
            this.TimeExpire = TimeExpire;
        }

        public Boolean IsValid()
        {
            if (TimeExpire <= UberEnvironment.GetUnixTimestamp())
            {
                return false;
            }

            return true;
        }

        public void ExtendSubscription(int Time)
        {
            TimeExpire += Time;
        }
    }
}
