using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.Support
{
    class HelpCategory
    {
        private uint Id;
        public string Caption;

        public uint CategoryId
        {
            get
            {
                return Id;
            }
        }

        public HelpCategory(uint Id, string Caption)
        {
            this.Id = Id;
            this.Caption = Caption;
        }
    }
}
