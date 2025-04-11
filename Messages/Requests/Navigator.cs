using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.HabboHotel.Rooms;
using Uber.HabboHotel.Catalogs;
using Uber.Storage;

namespace Uber.Messages
{
    partial class GameClientMessageHandler
    {
        private void AddFavorite()
        {
            uint Id = Request.PopWiredUInt();

            RoomData Data = UberEnvironment.GetGame().GetRoomManager().GenerateRoomData(Id);

            if (Data == null || Session.GetHabbo().FavoriteRooms.Count >= 30 || Session.GetHabbo().FavoriteRooms.Contains(Id) || Data.Type == "public")
            {
                GetResponse().Init(33);
                GetResponse().AppendInt32(-9001);
                SendResponse();

                return;
            }

            GetResponse().Init(459);
            GetResponse().AppendUInt(Id);
            GetResponse().AppendBoolean(true);
            SendResponse();

            Session.GetHabbo().FavoriteRooms.Add(Id);

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("INSERT INTO user_favorites (user_id,room_id) VALUES ('" + Session.GetHabbo().Id + "','" + Id + "')");
            }
        }

        private void RemoveFavorite()
        {
            uint Id = Request.PopWiredUInt();

            Session.GetHabbo().FavoriteRooms.Remove(Id);

            GetResponse().Init(459);
            GetResponse().AppendUInt(Id);
            GetResponse().AppendBoolean(false);
            SendResponse();

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("DELETE FROM user_favorites WHERE user_id = '" + Session.GetHabbo().Id + "' AND room_id = '" + Id + "' LIMIT 1");
            }
        }

        private void GoToHotelView()
        {
            if (Session.GetHabbo().InRoom)
            {
                UberEnvironment.GetGame().GetRoomManager().GetRoom(Session.GetHabbo().CurrentRoomId).RemoveUserFromRoom(Session, true, false);
            }
        }

        private void GetFlatCats()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetNavigator().SerializeFlatCategories());
        }

        private void EnterInquiredRoom()
        {
            // ???????????????????????????????
        }

        private void GetPubs()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetNavigator().SerializePublicRooms());
        }

        private void GetRoomInfo()
        {
            uint RoomId = Request.PopWiredUInt();
            bool unk = Request.PopWiredBoolean();
            bool unk2 = Request.PopWiredBoolean();

            RoomData Data = UberEnvironment.GetGame().GetRoomManager().GenerateRoomData(RoomId);

            if (Data == null)
            {
                return;
            }

            // jTX]XHÆ’ S I N G L E S Æ’ C H A T  C L U B Æ’SecuresHRJRLFind your true love in this fantastic dance club.HHX`fRLJÆ’ s m e x y Æ’ s i n g l e s Æ’s e c u r e s Ãµ r Ã¶ Ã¸ m sSDQBJSASFRAPAHI
            // GFHhhbZVHHabbo Staff OfficeLost_WitnessHHQFThe Habbo UK London Office, home to the Habbo Staff.HIX{CPRHHHHII

            GetResponse().Init(454);
            GetResponse().AppendInt32(0);
            Data.Serialize(GetResponse(), false);
            SendResponse();
        }

        private void GetPopularRooms()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetNavigator().SerializeRoomListing(Session, int.Parse(Request.PopFixedString())));
        }

        private void GetHighRatedRooms()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetNavigator().SerializeRoomListing(Session, -2));
        }

        private void GetFriendsRooms()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetNavigator().SerializeRoomListing(Session, -4));
        }

        private void GetRoomsWithFriends()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetNavigator().SerializeRoomListing(Session, -5));
        }

        private void GetOwnRooms()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetNavigator().SerializeRoomListing(Session, -3));
        }

        private void GetFavoriteRooms()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetNavigator().SerializeFavoriteRooms(Session));
        }

        private void GetRecentRooms()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetNavigator().SerializeRecentRooms(Session));
        }

        private void GetEvents()
        {
            int Category = int.Parse(Request.PopFixedString());

            Session.SendMessage(UberEnvironment.GetGame().GetNavigator().SerializeEventListing(Session, Category));
        }

        private void GetPopularTags()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetNavigator().SerializePopularRoomTags());
        }

        private void PerformSearch()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetNavigator().SerializeSearchResults(Request.PopFixedString()));
        }

        private void PerformSearch2()
        {
            int junk = Request.PopWiredInt32();
            Session.SendMessage(UberEnvironment.GetGame().GetNavigator().SerializeSearchResults(Request.PopFixedString()));
        }

        public void RegisterNavigator()
        {
            RequestHandlers[19] = new RequestHandler(AddFavorite);
            RequestHandlers[20] = new RequestHandler(RemoveFavorite);
            RequestHandlers[53] = new RequestHandler(GoToHotelView);
            RequestHandlers[151] = new RequestHandler(GetFlatCats);
            RequestHandlers[233] = new RequestHandler(EnterInquiredRoom);
            RequestHandlers[380] = new RequestHandler(GetPubs);
            RequestHandlers[385] = new RequestHandler(GetRoomInfo);
            RequestHandlers[430] = new RequestHandler(GetPopularRooms);
            RequestHandlers[431] = new RequestHandler(GetHighRatedRooms);
            RequestHandlers[432] = new RequestHandler(GetFriendsRooms);
            RequestHandlers[433] = new RequestHandler(GetRoomsWithFriends);
            RequestHandlers[434] = new RequestHandler(GetOwnRooms);
            RequestHandlers[435] = new RequestHandler(GetFavoriteRooms);
            RequestHandlers[436] = new RequestHandler(GetRecentRooms);
            RequestHandlers[439] = new RequestHandler(GetEvents);
            RequestHandlers[382] = new RequestHandler(GetPopularTags);
            RequestHandlers[437] = new RequestHandler(PerformSearch);
            RequestHandlers[438] = new RequestHandler(PerformSearch2);

        }
    }
}
