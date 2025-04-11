using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.Support
{
    enum ModerationBanType
    {
        IP = 0,
        USERNAME = 1
    }

    class ModerationBan
    {
        public ModerationBanType Type;
        public string Variable;
        public string ReasonMessage;
        public Double Expire;

        public Boolean Expired
        {
            get
            {
                if (UberEnvironment.GetUnixTimestamp() >= Expire)
                {
                    return true;
                }

                return false;
            }
        }

        public ModerationBan(ModerationBanType Type, string Variable, string ReasonMessage, Double Expire)
        {
            this.Type = Type;
            this.Variable = Variable;
            this.ReasonMessage = ReasonMessage;
            this.Expire = Expire;
        }
    }
}
