using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Storage;
using Uber.Messages;
using Uber.HabboHotel.GameClients;

namespace Uber.HabboHotel.Users.Messenger
{
    class HabboMessenger
    {
        private uint UserId;

        private List<MessengerBuddy> Buddies;
        private List<MessengerRequest> Requests;

        public bool AppearOffline;

        public HabboMessenger(uint UserId)
        {
            this.Buddies = new List<MessengerBuddy>();
            this.Requests = new List<MessengerRequest>();
            this.UserId = UserId;
        }

        public void LoadBuddies()
        {
            Buddies = new List<MessengerBuddy>();
            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT user_two_id FROM messenger_friendships WHERE user_one_id = '" + UserId + "'");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                Buddies.Add(new MessengerBuddy((uint)Row["user_two_id"]));
            }
        }

        public void LoadRequests()
        {
            Requests = new List<MessengerRequest>();
            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT * FROM messenger_requests WHERE to_id = '" + UserId + "'");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                Requests.Add(new MessengerRequest((uint)Row["id"], (uint)Row["to_id"], (uint)Row["from_id"]));
            }
        }

        public void ClearBuddies()
        {
            Buddies.Clear();
        }

        public void ClearRequests()
        {
            Requests.Clear();
        }

        public MessengerRequest GetRequest(uint RequestId)
        {
            lock (this.Requests)
            {
                List<MessengerRequest>.Enumerator eRequests = this.Requests.GetEnumerator();

                while (eRequests.MoveNext())
                {
                    MessengerRequest Request = eRequests.Current;

                    if (Request.RequestId == RequestId)
                    {
                        return Request;
                    }
                }
            }

            return null;
        }

        public void OnStatusChanged(bool instantUpdate)
        {
            lock (this.Buddies)
            {
                List<MessengerBuddy>.Enumerator eBuddies = this.Buddies.GetEnumerator();

                while (eBuddies.MoveNext())
                {
                    MessengerBuddy Buddy = eBuddies.Current;
                    GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(Buddy.Id);

                    if (Client == null || Client.GetHabbo() == null || Client.GetHabbo().GetMessenger() == null)
                    {
                        continue;
                    }

                    Client.GetHabbo().GetMessenger().SetUpdateNeeded(UserId);

                    if (instantUpdate)
                    {
                        Client.GetHabbo().GetMessenger().ForceUpdate();
                    }
                }
            }
        }

        public bool SetUpdateNeeded(uint UserId)
        {
            lock (this.Buddies)
            {
                List<MessengerBuddy>.Enumerator eBuddies = this.Buddies.GetEnumerator();

                while (eBuddies.MoveNext())
                {
                    MessengerBuddy Buddy = eBuddies.Current;

                    if (Buddy.Id == UserId)
                    {
                        Buddy.UpdateNeeded = true;
                        return true;
                    }
                }
            }

            return false;
        }

        public void ForceUpdate()
        {
            this.GetClient().SendMessage(this.SerializeUpdates());
        }

        public Boolean RequestExists(uint UserOne, uint UserTwo)
        {
            if (UserOne == UserTwo)
            {
                return true;
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                if (dbClient.ReadDataRow("SELECT * FROM messenger_requests WHERE to_id = '" + UserOne + "' AND from_id = '" + UserTwo + "' LIMIT 1") != null)
                {
                    return true;
                }
                
                if (dbClient.ReadDataRow("SELECT * FROM messenger_requests WHERE to_id = '" + UserTwo + "' AND from_id = '" + UserOne + "' LIMIT 1") != null)
                {
                    return true;
                }
            }

            return false;
        }

        public Boolean FriendshipExists(uint UserOne, uint UserTwo)
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                if (dbClient.ReadDataRow("SELECT * FROM messenger_friendships WHERE user_one_id = '" + UserOne + "' AND user_two_id = '" + UserTwo + "' LIMIT 1") != null)
                {
                    return true;
                }
                
                if (dbClient.ReadDataRow("SELECT * FROM messenger_friendships WHERE user_one_id = '" + UserTwo + "' AND user_two_id = '" + UserOne + "' LIMIT 1") != null)
                {
                    return true;
                }
            }

            return false;
        }

        public void HandleAllRequests()
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("DELETE FROM messenger_requests WHERE to_id = '" + UserId + "'");
            }

            ClearRequests();
        }

        public void HandleRequest(uint FromId)
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("userid", UserId);
                dbClient.AddParamWithValue("fromid", FromId);
                dbClient.ExecuteQuery("DELETE FROM messenger_requests WHERE to_id = @userid AND from_id = @fromid LIMIT 1");
            }

            if (GetRequest(FromId) != null)
            {
                Requests.Remove(GetRequest(FromId));
            }
        }

        public void CreateFriendship(uint UserTwo)
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("toid", UserTwo);
                dbClient.AddParamWithValue("userid", UserId);
                dbClient.ExecuteQuery("INSERT INTO messenger_friendships (user_one_id,user_two_id) VALUES (@userid,@toid)");
                dbClient.ExecuteQuery("INSERT INTO messenger_friendships (user_one_id,user_two_id) VALUES (@toid,@userid)");
            }

            OnNewFriendship(UserTwo);

            GameClient User = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserTwo);

            if (User != null && User.GetHabbo().GetMessenger() != null)
            {
                User.GetHabbo().GetMessenger().OnNewFriendship(UserId);
            }
        }

        public void DestroyFriendship(uint UserTwo)
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("toid", UserTwo);
                dbClient.AddParamWithValue("userid", UserId);
                dbClient.ExecuteQuery("DELETE FROM messenger_friendships WHERE user_one_id = @toid AND user_two_id = @userid LIMIT 1");
                dbClient.ExecuteQuery("DELETE FROM messenger_friendships WHERE user_one_id = @userid AND user_two_id = @toid LIMIT 1");
            }

            OnDestroyFriendship(UserTwo);

            GameClient User = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserTwo);

            if (User != null && User.GetHabbo().GetMessenger() != null)
            {
                User.GetHabbo().GetMessenger().OnDestroyFriendship(UserId);
            }
        }

        public void OnNewFriendship(uint Friend)
        {
            MessengerBuddy Buddy = new MessengerBuddy(Friend);
            Buddy.UpdateNeeded = true;

            Buddies.Add(Buddy);

            ForceUpdate();
        }

        public void OnDestroyFriendship(uint Friend)
        {
            lock (Buddies)
            {
                foreach (MessengerBuddy Buddy in Buddies)
                {
                    if (Buddy.Id == Friend)
                    {
                        Buddies.Remove(Buddy);
                        break;
                    }
                }
            }

            GetClient().GetMessageHandler().GetResponse().Init(13);
            GetClient().GetMessageHandler().GetResponse().AppendInt32(0);
            GetClient().GetMessageHandler().GetResponse().AppendInt32(1);
            GetClient().GetMessageHandler().GetResponse().AppendInt32(-1);
            GetClient().GetMessageHandler().GetResponse().AppendUInt(Friend);
            GetClient().GetMessageHandler().SendResponse();
        }

        public void RequestBuddy(string UserQuery)
        {
            DataRow Row = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("query", UserQuery.ToLower());
                Row = dbClient.ReadDataRow("SELECT id,block_newfriends FROM users WHERE username = @query LIMIT 1");
            }

            if (Row == null)
            {
                return;
            }
            else if (UberEnvironment.EnumToBool(Row["block_newfriends"].ToString()))
            {
                GetClient().GetMessageHandler().GetResponse().Init(260);
                GetClient().GetMessageHandler().GetResponse().AppendInt32(39);
                GetClient().GetMessageHandler().GetResponse().AppendInt32(3);
                GetClient().GetMessageHandler().SendResponse();
                return;
            }

            uint ToId = (uint)Row["id"];

            if (RequestExists(UserId, ToId))
            {
                return;
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("toid", ToId);
                dbClient.AddParamWithValue("userid", UserId);
                dbClient.ExecuteQuery("INSERT INTO messenger_requests (to_id,from_id) VALUES (@toid,@userid)");
            }

            GameClient ToUser = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(ToId);

            if (ToUser == null || ToUser.GetHabbo() == null)
            {
                return;
            }

            uint RequestId = 0;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("toid", ToId);
                dbClient.AddParamWithValue("userid", UserId);
                RequestId = (uint)dbClient.ReadInt32("SELECT id FROM messenger_requests WHERE to_id = @toid AND from_id = @userid ORDER BY id DESC LIMIT 1");
            }

            MessengerRequest Request = new MessengerRequest(RequestId, ToId, UserId);

            ToUser.GetHabbo().GetMessenger().OnNewRequest(RequestId, ToId, UserId);

            ServerMessage NewFriendNotif = new ServerMessage(132);
            Request.Serialize(NewFriendNotif);
            ToUser.SendMessage(NewFriendNotif);
        }

        public void OnNewRequest(uint Request, uint ToId, uint UserId)
        {
            Requests.Add(new MessengerRequest(Request, ToId, UserId));
        }

        public void SendInstantMessage(uint ToId, string Message)
        {
            if (!FriendshipExists(ToId, UserId))
            {
                DeliverInstantMessageError(6, ToId);
                return;
            }

            GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(ToId);

            if (Client == null || Client.GetHabbo().GetMessenger() == null)
            {
                DeliverInstantMessageError(5, ToId);
                return;
            }

            if (GetClient().GetHabbo().Muted)
            {
                DeliverInstantMessageError(4, ToId);
                return;
            }

            if (Client.GetHabbo().Muted)
            {
                DeliverInstantMessageError(3, ToId); // No return, as this is just a warning.
            }

            // todo: is busy??

            Client.GetHabbo().GetMessenger().DeliverInstantMessage(Message, UserId);
        }

        public void DeliverInstantMessage(string Message, uint ConversationId)
        {
            ServerMessage InstantMessage = new ServerMessage(134);
            InstantMessage.AppendUInt(ConversationId);
            InstantMessage.AppendString(Message);
            GetClient().SendMessage(InstantMessage);
        }

        public void DeliverInstantMessageError(int ErrorId, uint ConversationId)
        {
            /*
             * 3                =     Your friend is muted and cannot reply.
             * 4                =     Your message was not sent because you are muted.
             * 5                =     Your friend is not online.
             * 6                =     Receiver is not your friend anymore.
             * 7                =     Your friend is busy.
             * Anything else    =     Unknown im error <error no.>
             */

            ServerMessage Error = new ServerMessage(261);
            Error.AppendInt32(ErrorId);
            Error.AppendUInt(ConversationId);
            GetClient().SendMessage(Error);
        }

        public ServerMessage SerializeFriends()
        {
            ServerMessage Friends = new ServerMessage(12);
            Friends.AppendInt32(600);
            Friends.AppendInt32(200);
            Friends.AppendInt32(600);
            Friends.AppendInt32(900);
            Friends.AppendBoolean(false);
            Friends.AppendInt32(Buddies.Count);

            lock (Buddies)
            {
                foreach (MessengerBuddy Buddy in Buddies)
                {
                    Buddy.Serialize(Friends, false);
                }
            }

            return Friends;
        }

        public ServerMessage SerializeUpdates()
        {
            List<MessengerBuddy> UpdateBuddies = new List<MessengerBuddy>();
            int UpdateCount = 0;

            lock (Buddies)
            {
                foreach (MessengerBuddy Buddy in Buddies)
                {
                    if (Buddy.UpdateNeeded)
                    {
                        UpdateCount++;
                        UpdateBuddies.Add(Buddy);
                        Buddy.UpdateNeeded = false;
                    }
                }
            }

            ServerMessage Updates = new ServerMessage(13);
            Updates.AppendInt32(0);
            Updates.AppendInt32(UpdateCount);
            Updates.AppendInt32(0);

            foreach (MessengerBuddy Buddy in UpdateBuddies)
            {
                Buddy.Serialize(Updates, false);
                Updates.AppendBoolean(false);
            }

            return Updates;
        }

        public ServerMessage SerializeRequests()
        {
            ServerMessage Reqs = new ServerMessage(314);
            Reqs.AppendInt32(Requests.Count);
            Reqs.AppendInt32(Requests.Count);

            lock (Requests)
            {
                foreach (MessengerRequest Request in Requests)
                {
                    Request.Serialize(Reqs);
                }
            }

            return Reqs;
        }

        public ServerMessage PerformSearch(string SearchQuery)
        {
            DataTable Results = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("query", SearchQuery + "%");
                Results = dbClient.ReadDataTable("SELECT id FROM users WHERE username LIKE @query LIMIT 50");
            }

            List<DataRow> friendData = new List<DataRow>();
            List<DataRow> othersData = new List<DataRow>();

            if (Results != null)
            {
                foreach (DataRow Row in Results.Rows)
                {
                    if (FriendshipExists(UserId, (uint)Row["id"]))
                    {
                        friendData.Add(Row);

                        continue;
                    }

                    othersData.Add(Row);
                }
            }

            ServerMessage Search = new ServerMessage(435);

            Search.AppendInt32(friendData.Count);

            foreach (DataRow Row in friendData)
            {
                new MessengerBuddy((uint)Row["id"]).Serialize(Search, true);
            }

            Search.AppendInt32(othersData.Count);

            foreach (DataRow Row in othersData)
            {
                new MessengerBuddy((uint)Row["id"]).Serialize(Search, true);
            }

            return Search;
        }

        private GameClient GetClient()
        {
            return UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);
        }

        public List<MessengerBuddy> GetBuddies()
        {
            return Buddies;
        }
    }
}