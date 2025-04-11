using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.Support
{
    public class ModerationBanException : Exception
    {
        public ModerationBanException(string Reason) : base(Reason) { }
    }
}
