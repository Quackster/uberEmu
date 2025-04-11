using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Storage;
using System.Collections.Concurrent;

namespace Uber.HabboHotel.Advertisements
{
    class AdvertisementManager
    {
        public ConcurrentDictionary<uint, RoomAdvertisement> RoomAdvertisements;

        public AdvertisementManager()
        {
            RoomAdvertisements = new ConcurrentDictionary<uint, RoomAdvertisement>();
        }

        public void LoadRoomAdvertisements()
        {
            RoomAdvertisements.Clear();

            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT * FROM room_ads WHERE enabled = '1'");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                RoomAdvertisements.TryAdd((uint)Row["id"], new RoomAdvertisement((uint)Row["id"], (string)Row["ad_image"],
                    (string)Row["ad_link"], (int)Row["views"], (int)Row["views_limit"]));
            }
        }

        public RoomAdvertisement GetRandomRoomAdvertisement()
        {
            if (RoomAdvertisements.Count <= 0)
            {
                return null;
            }

            while (true)
            {
                var snapshotRoomAdvertisements = RoomAdvertisements.Values.ToList();
                int RndId = UberEnvironment.GetRandomNumber(0, (RoomAdvertisements.Count - 1));

                if (snapshotRoomAdvertisements[RndId] != null && !snapshotRoomAdvertisements[RndId].ExceededLimit)
                {
                    return snapshotRoomAdvertisements[RndId];
                }
            }
        }
    }
}
