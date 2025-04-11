using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.Users.Authenticator
{
    public class IncorrectLoginException : Exception
    {
        public IncorrectLoginException(string Reason) : base(Reason) { }
    }
}
