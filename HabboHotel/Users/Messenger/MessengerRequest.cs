using System;
using System.Data;

using Uber.Messages;
using Uber.HabboHotel.GameClients;
using Uber.Storage;

namespace Uber.HabboHotel.Users.Messenger
{
    class MessengerRequest
    {
        private uint xRequestId;

        private uint ToUser;
        private uint FromUser;

        public uint RequestId
        {
            get
            {
                return FromUser;
            }
        }

        public uint To
        {
            get
            {
                return ToUser;
            }
        }

        public uint From
        {
            get
            {
                return FromUser;
            }
        }

        public string SenderUsername
        {
            get
            {
                GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(From);

                if (Client != null)
                {
                    return Client.GetHabbo().Username;
                }
                else
                {
                    using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                    {
                        return dbClient.ReadString("SELECT username FROM users WHERE id = '" + From + "' LIMIT 1");
                    }
                }
            }
        }

        public MessengerRequest(uint RequestId, uint ToUser, uint FromUser)
        {
            this.xRequestId = RequestId;
            this.ToUser = ToUser;
            this.FromUser = FromUser;
        }

        public void Serialize(ServerMessage Request)
        {
            // BDhqu@UMeth0d1322033860

            Request.AppendUInt(FromUser);
            Request.AppendStringWithBreak(SenderUsername);
            Request.AppendStringWithBreak(FromUser.ToString());
        }
    }
}
