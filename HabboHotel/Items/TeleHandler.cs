using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Storage;

namespace Uber.HabboHotel.Items
{
    class TeleHandler
    {
        public static uint GetLinkedTele(uint TeleId)
        {
            DataRow Row = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Row = dbClient.ReadDataRow("SELECT tele_two_id FROM tele_links WHERE tele_one_id = '" + TeleId + "' LIMIT 1");
            }

            if (Row == null)
            {
                return 0;
            }

            return (uint)Row[0];
        }

        public static uint GetTeleRoomId(uint TeleId)
        {
            DataRow Row = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Row = dbClient.ReadDataRow("SELECT room_id FROM room_items WHERE id = '" + TeleId + "' LIMIT 1");
            }

            if (Row == null)
            {
                return 0;
            }

            return (uint)Row[0];
        }

        public static bool IsTeleLinked(uint TeleId)
        {
            uint LinkId = GetLinkedTele(TeleId);

            if (LinkId == 0)
            {
                return false;
            }

            uint RoomId = GetTeleRoomId(LinkId);

            if (RoomId == 0)
            {
                return false;
            }

            return true;
        }
    }
}
