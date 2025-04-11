using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.Misc
{
    class AntiMutant
    {
        public static bool ValidateLook(string Look, string Gender)
        {
            bool HasHead = false;

            if (Look.Length < 1)
            {
                return false;
            }

            try
            {
                string[] Sets = Look.Split('.');

                if (Sets.Length < 4)
                {
                    return false;
                }

                foreach (string Set in Sets)
                {
                    string[] Parts = Set.Split('-');

                    if (Parts.Length < 3)
                    {
                        return false;
                    }

                    string Name = Parts[0];
                    int Type = int.Parse(Parts[1]);
                    int Color = int.Parse(Parts[1]);

                    if (Type <= 0 || Color < 0)
                    {
                        return false;
                    }

                    if (Name.Length != 2)
                    {
                        return false;
                    }

                    if (Name == "hd")
                    {
                        HasHead = true;
                    }
                }
            }
            catch (Exception) { return false; }

            if (!HasHead || (Gender != "M" && Gender != "F"))
            {
                return false;
            }

            return true;
        }
    }
}
