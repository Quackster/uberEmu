using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.Users.Subscriptions
{
    class Subscription
    {
        private string Caption;

        private long TimeActivated;
        private long TimeExpire;

        public string SubscriptionId
        {
            get
            {
                return Caption;
            }
        }

        public long ExpireTime
        {
            get
            {
                return TimeExpire;
            }
        }

        public Subscription(string Caption, long TimeActivated, long TimeExpire)
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
