using System;

namespace Uber.HabboHotel.Users.Badges
{
    class Badge
    {
        public string Code;
        public int Slot;

        public Badge(string Code, int Slot)
        {
            this.Code = Code;
            this.Slot = Slot;
        }
    }
}
