using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Storage;

namespace Uber.HabboHotel.Advertisements
{
    class RoomAdvertisement
    {
        public uint Id;
        public string AdImage;
        public string AdLink;
        public int Views;
        public int ViewsLimit;

        public Boolean ExceededLimit
        {
            get
            {
                if (ViewsLimit <= 0)
                {
                    return false;
                }

                if (Views >= ViewsLimit)
                {
                    return true;
                }

                return false;
            }
        }

        public RoomAdvertisement(uint Id, string AdImage, string AdLink, int Views, int ViewsLimit)
        {
            this.Id = Id;
            this.AdImage = AdImage;
            this.AdLink = AdLink;
            this.Views = Views;
            this.ViewsLimit = ViewsLimit;
        }

        public void OnView()
        {
            this.Views++;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("UPDATE room_ads SET views = views + 1 WHERE id = '" + Id + "'");
            }
        }
    }
}
