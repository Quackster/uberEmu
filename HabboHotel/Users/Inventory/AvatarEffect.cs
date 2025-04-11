using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.Users.Inventory
{
    class AvatarEffect
    {
        public int EffectId;
        public int TotalDuration;
        public bool Activated;
        public double StampActivated;

        public int TimeLeft
        {
            get
            {
                if (!Activated)
                {
                    return -1;
                }

                double diff = UberEnvironment.GetUnixTimestamp() - StampActivated;

                if (diff >= TotalDuration)
                {
                    return 0;
                }

                return (int)(TotalDuration - diff);
            }
        }

        public Boolean HasExpired
        {
            get
            {
                if (TimeLeft == -1)
                {
                    return false;
                }

                if (TimeLeft <= 0)
                {
                    return true;
                }

                return false;
            }
        }

        public AvatarEffect(int EffectId, int TotalDuration, bool Activated, double ActivateTimestamp)
        {
            this.EffectId = EffectId;
            this.TotalDuration = TotalDuration;
            this.Activated = Activated;
            this.StampActivated = ActivateTimestamp;
        }

        public void Activate()
        {
            this.Activated = true;
            this.StampActivated = UberEnvironment.GetUnixTimestamp();
        }
    }
}
