using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.Navigators
{
    class FlatCat
    {
        public int Id;
        public string Caption;
        public int MinRank;

        public FlatCat(int Id, string Caption, int MinRank)
        {
            this.Id = Id;
            this.Caption = Caption;
            this.MinRank = MinRank;
        }
    }
}
