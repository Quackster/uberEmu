using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.Roles
{
    class Role
    {
        private uint Id;
        public string Caption;

        public uint RoleId
        {
            get
            {
                return Id;
            }
        }

        public Role(uint Id, string Caption)
        {
            this.Id = Id;
            this.Caption = Caption;
        }
    }
}
