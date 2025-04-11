using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Net;
using Uber.Messages;
using Uber.HabboHotel.Users;
using Uber.HabboHotel.Users.Authenticator;
using Uber.HabboHotel.Support;
using Uber.HabboHotel.Misc;
using Uber.Storage;
using Uber.Util;

namespace Uber.HabboHotel.GameClients
{
    class GameClient
    {
        private uint Id;

        private TcpConnection Connection;
        private GameClientMessageHandler MessageHandler;

        private Habbo Habbo;

        public Boolean PongOK;

        public uint ClientId
        {
            get
            {
                return Id;
            }
        }

        public Boolean LoggedIn
        {
            get
            {
                if (Habbo == null)
                {
                    return false;
                }

                return true;
            }
        }

        public GameClient(uint ClientId)
        {
            Id = ClientId;
            Connection = UberEnvironment.GetConnectionManager().GetConnection(ClientId);
            MessageHandler = new GameClientMessageHandler(this);
        }

        public TcpConnection GetConnection()
        {
            return Connection;
        }

        public GameClientMessageHandler GetMessageHandler()
        {
            return MessageHandler;
        }

        public Habbo GetHabbo()
        {
            return Habbo;
        }

        public void StartConnection()
        {
            if (Connection == null)
            {
                return;
            }

            PongOK = true;

            MessageHandler.RegisterGlobal();
            MessageHandler.RegisterHandshake();
            MessageHandler.RegisterHelp();

            TcpConnection.RouteReceivedDataCallback DataRouter = new TcpConnection.RouteReceivedDataCallback(HandleConnectionData);
            Connection.Start(DataRouter);
        }

        public void Login(string AuthTicket)
        {
            try
            {
                Habbo NewHabbo = Authenticator.TryLoginHabbo(AuthTicket);

                UberEnvironment.GetGame().GetClientManager().LogClonesOut(NewHabbo.Username);

                this.Habbo = NewHabbo;
                this.Habbo.LoadData();
            }
            catch (IncorrectLoginException e)
            {
                SendNotif("Login error: " + e.Message);
                Disconnect();

                return;
            }

            try
            {
                UberEnvironment.GetGame().GetBanManager().CheckForBanConflicts(this);
            }
            catch (ModerationBanException e)
            {
                SendBanMessage(e.Message);
                Disconnect();

                return;
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("UPDATE users SET online = '1', auth_ticket = '', ip_last = '" + GetConnection().IPAddress + "' WHERE id = '" + GetHabbo().Id + "' LIMIT 1");
                dbClient.ExecuteQuery("UPDATE user_info SET login_timestamp = '" + UberEnvironment.GetUnixTimestamp() + "' WHERE user_id = '" + GetHabbo().Id + "' LIMIT 1");
            }

            List<string> Rights = UberEnvironment.GetGame().GetRoleManager().GetRightsForHabbo(GetHabbo());

            GetMessageHandler().GetResponse().Init(2);
            GetMessageHandler().GetResponse().AppendInt32(Rights.Count);

            foreach (string Right in Rights)
            {
                GetMessageHandler().GetResponse().AppendStringWithBreak(Right);
            }

            GetMessageHandler().SendResponse();

            if (GetHabbo().HasFuse("fuse_mod"))
            {
                SendMessage(UberEnvironment.GetGame().GetModerationTool().SerializeTool());
                UberEnvironment.GetGame().GetModerationTool().SendOpenTickets(this);
            }

            SendMessage(GetHabbo().GetAvatarEffectsInventoryComponent().Serialize());

            MessageHandler.GetResponse().Init(290);
            MessageHandler.GetResponse().AppendBoolean(true);
            MessageHandler.GetResponse().AppendBoolean(false);
            MessageHandler.SendResponse();

            MessageHandler.GetResponse().Init(3);
            MessageHandler.SendResponse();

            MessageHandler.GetResponse().Init(517);
            MessageHandler.GetResponse().AppendBoolean(true);
            MessageHandler.SendResponse();

            if (UberEnvironment.GetGame().GetPixelManager().NeedsUpdate(this))
            {
                UberEnvironment.GetGame().GetPixelManager().GivePixels(this);
            }

            MessageHandler.GetResponse().Init(455);
            MessageHandler.GetResponse().AppendUInt(GetHabbo().HomeRoom);
            MessageHandler.SendResponse();

            MessageHandler.GetResponse().Init(458);
            MessageHandler.GetResponse().AppendInt32(30);
            MessageHandler.GetResponse().AppendInt32(GetHabbo().FavoriteRooms.Count);

            foreach (uint Id in GetHabbo().FavoriteRooms)
            {
                MessageHandler.GetResponse().AppendUInt(Id);
            }

            MessageHandler.SendResponse();

            SendNotif("Thank you for helping us test the new Uber. Please submit feedback to the UserVoice forum:", "http://uber.uservoice.com/forums/45577-general");
            UberEnvironment.GetGame().GetAchievementManager().UnlockAchievement(this, 11, 1);

            if (GetHabbo().HasFuse("fuse_use_club_badge") && !GetHabbo().GetBadgeComponent().HasBadge("HC1"))
            {
                GetHabbo().GetBadgeComponent().GiveBadge("HC1", true);
            }
            else if (!GetHabbo().HasFuse("fuse_use_club_badge") && GetHabbo().GetBadgeComponent().HasBadge("HC1"))
            {
                GetHabbo().GetBadgeComponent().RemoveBadge("HC1");
            }

            MessageHandler.RegisterUsers();
            MessageHandler.RegisterMessenger();
            MessageHandler.RegisterCatalog();
            MessageHandler.RegisterNavigator();
            MessageHandler.RegisterRooms();
        }

        public void SendBanMessage(string Message)
        {
            ServerMessage BanMessage = new ServerMessage(35);
            BanMessage.AppendStringWithBreak("A moderator has kicked you from the hotel:", 13);
            BanMessage.AppendStringWithBreak(Message);
            GetConnection().SendMessage(BanMessage);
        }

        public void SendNotif(string Message)
        {
            SendNotif(Message, false);
        }

        public void SendNotif(string Message, Boolean FromHotelManager)
        {
            ServerMessage nMessage = new ServerMessage();

            if (FromHotelManager)
            {
                nMessage.Init(139);
            }
            else
            {
                nMessage.Init(161);
            }

            nMessage.AppendStringWithBreak(Message);
            GetConnection().SendMessage(nMessage);
        }

        public void SendNotif(string Message, string Url)
        {
            ServerMessage nMessage = new ServerMessage(161);
            nMessage.AppendStringWithBreak(Message);
            nMessage.AppendStringWithBreak(Url);
            GetConnection().SendMessage(nMessage);
        }

        public void Stop()
        {
            if (GetHabbo() != null)
            {
                Habbo.OnDisconnect();
                Habbo = null;
            }

            if (GetConnection() != null)
            {
                Connection = null;
            }

            if (GetMessageHandler() != null)
            {
                MessageHandler.Destroy();
                MessageHandler = null;
            }
        }

        public void Disconnect()
        {
            UberEnvironment.GetGame().GetClientManager().StopClient(Id);
        }

        public void HandleConnectionData(ref byte[] data)
        {
            if (data[0] == 64)
            {
                int pos = 0;

                while (pos < data.Length)
                {
                    try
                    {
                        int MessageLength = Base64Encoding.DecodeInt32(new byte[] { data[pos++], data[pos++], data[pos++] });
                        uint MessageId = Base64Encoding.DecodeUInt32(new byte[] { data[pos++], data[pos++] });

                        byte[] Content = new byte[MessageLength - 2];

                        for (int i = 0; i < Content.Length; i++)
                        {
                            Content[i] = data[pos++];
                        }

                        // Plugin stuff
                        if (UberEnvironment.GetPluginHandler().HasPacketHandler((int)MessageId))
                        {
                            uint Id = 0;

                            if (GetHabbo() != null)
                            {
                                Id = GetHabbo().Id;
                            }

                            if (!UberEnvironment.GetPluginHandler().ExecutePacketHandler(Id, (int)MessageId, Content))
                            {
                                continue;
                            }
                        }

                        ClientMessage Message = new ClientMessage(MessageId, Content);
                        MessageHandler.HandleRequest(Message);
                    }
                    catch (EntryPointNotFoundException e)
                    {
                        UberEnvironment.GetLogging().WriteLine("User D/C: " + e.Message, Core.LogLevel.Error);
                        Disconnect();
                    }
                }
            }
            else
            {
                Connection.SendData(CrossdomainPolicy.GetXmlPolicy());
            }
        }

        public void SendMessage(ServerMessage Message)
        {
            if (Message == null)
            {
                return;
            }

            GetConnection().SendMessage(Message);
        }
    }
}
