using System;
using System.Collections.Generic;
using System.Text;
using System.Data;
using System.Net;
using System.Net.Sockets;

using Uber.Storage;
using Uber.HabboHotel.GameClients;
using Uber.HabboHotel.Rooms;
using Uber.Messages;

namespace Uber.Net
{
    class MusSocket
    {
        public Socket msSocket;

        public String musIp;
        public int musPort;

        public HashSet<String> allowedIps;

        public MusSocket(String _musIp, int _musPort, String[] _allowedIps, int backlog)
        {
            musIp = _musIp;
            musPort = _musPort;

            allowedIps = new HashSet<String>();

            foreach (String ip in _allowedIps)
            {
                allowedIps.Add(ip);
            }

            try
            {
                msSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

                msSocket.Bind(new IPEndPoint(IPAddress.Parse(musIp), musPort));
                msSocket.Listen(backlog);

                msSocket.BeginAccept(OnEvent_NewConnection, msSocket);

                UberEnvironment.GetLogging().WriteLine("MUS socket listening on " + musIp + ":" + musPort + ".");
            }

            catch (Exception e)
            {
                throw new Exception("Could not set up MUS socket:\n" + e.Message);
            }
        }

        public void OnEvent_NewConnection(IAsyncResult iAr)
        {
            Socket socket = ((Socket)iAr.AsyncState).EndAccept(iAr);
            MusConnection nC = new MusConnection(socket);
            msSocket.BeginAccept(OnEvent_NewConnection, msSocket);
        }
    }

    class MusConnection
    {
        private Socket socket;
        private byte[] buffer = new byte[1024];

        public MusConnection(Socket _socket)
        {
            socket = _socket;

            try
            {
                socket.BeginReceive(buffer, 0, buffer.Length, SocketFlags.None, OnEvent_RecieveData, null);
            }

            catch (Exception)
            {
                tryClose();
            }
        }

        public void tryClose()
        {
            try
            {
                socket.Close();
            }
            catch (Exception) { }
        }

        public void OnEvent_RecieveData(IAsyncResult iAr)
        {
            try
            {
                int bytes = socket.EndReceive(iAr);
                String data = Encoding.Default.GetString(buffer, 0, bytes);

                if (data.Length > 0)
                {
                    processCommand(data);
                }
            }
            catch (Exception) { }

            tryClose();
        }

        public void processCommand(String data)
        {
            String header = data.Split(Convert.ToChar(1))[0];
            String param = data.Split(Convert.ToChar(1))[1];

            UberEnvironment.GetLogging().WriteLine("[MUSConnection.ProcessCommand]: " + data);

            uint userId = 0;
            GameClient Client = null;
            Room Room = null;
            RoomUser RoomUser = null;
            DataRow Row = null;
            ServerMessage Message = null;

            switch (header.ToLower())
            {
                case "updatecredits":
                {
                    if (param == "ALL")
                    {
                        UberEnvironment.GetGame().GetClientManager().DeployHotelCreditsUpdate();
                    }
                    else
                    {
                        Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(uint.Parse(param));

                        if (Client == null)
                        {
                            return;
                        }

                        int newCredits = 0;

                        using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                        {
                            newCredits = (int)dbClient.ReadDataRow("SELECT credits FROM users WHERE id = '" + Client.GetHabbo().Id + "' LIMIT 1")[0];
                        }

                        Client.GetHabbo().Credits = newCredits;
                        Client.GetHabbo().UpdateCreditsBalance(false);                        
                    }

                    break;
                }
                case "reloadbans":
                {
                    UberEnvironment.GetGame().GetBanManager().LoadBans();
                    UberEnvironment.GetGame().GetClientManager().CheckForAllBanConflicts();
                    break;
                }
                case "signout":
                {
                    UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(uint.Parse(param)).Disconnect();
                    break;
                }
                case "updatetags":
                {
                    Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(uint.Parse(param));
                    Client.GetHabbo().LoadTags();
                    break;
                }
                case "ha":
                {
                    ServerMessage HotelAlert = new ServerMessage(139);
                    HotelAlert.AppendStringWithBreak("Message from the Hotel Management: " + param);
                    UberEnvironment.GetGame().GetClientManager().BroadcastMessage(HotelAlert);
                    break;
                }
                case "updatemotto":
                case "updatelook":
                {
                    userId = uint.Parse(param);
                    Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(userId);

                    using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                    {
                        Row = dbClient.ReadDataRow("SELECT look,gender,motto,mutant_penalty,block_newfriends FROM users WHERE id = '" + Client.GetHabbo().Id + "' LIMIT 1");
                    }

                    Client.GetHabbo().Look = (string)Row["look"];
                    Client.GetHabbo().Gender = Row["gender"].ToString().ToLower();
                    Client.GetHabbo().Motto = UberEnvironment.FilterInjectionChars((string)Row["motto"]);
                    Client.GetHabbo().BlockNewFriends = UberEnvironment.EnumToBool(Row["block_newfriends"].ToString());

                    if (Row["mutant_penalty"].ToString() != "0")
                    {
                        if (!Client.GetHabbo().MutantPenalty)
                        {
                            Client.SendNotif("For scripting and/or manipulating your look, we have decided to punish you, by changing and locking your look and motto for a week (or perhaps permanently, depending on our mood). Enjoy!");
                            Client.GetHabbo().MutantPenalty = true;
                        }
                    }

                    // DJHhr-890-39.hd-600-1.sh-907-84.lg-715-84.ch-650-84.ca-1819-.fa-1212-f?
                    Client.GetMessageHandler().GetResponse().Init(266);
                    Client.GetMessageHandler().GetResponse().AppendInt32(-1);
                    Client.GetMessageHandler().GetResponse().AppendStringWithBreak(Client.GetHabbo().Look);
                    Client.GetMessageHandler().GetResponse().AppendStringWithBreak(Client.GetHabbo().Gender.ToLower());
                    Client.GetMessageHandler().GetResponse().AppendStringWithBreak(Client.GetHabbo().Motto);
                    Client.GetMessageHandler().SendResponse();

                    if (Client.GetHabbo().InRoom)
                    {
                        Room = UberEnvironment.GetGame().GetRoomManager().GetRoom(Client.GetHabbo().CurrentRoomId);
                        RoomUser = Room.GetRoomUserByHabbo(Client.GetHabbo().Id);

                        Message = new ServerMessage(266);
                        Message.AppendInt32(RoomUser.VirtualId);
                        Message.AppendStringWithBreak(Client.GetHabbo().Look);
                        Message.AppendStringWithBreak(Client.GetHabbo().Gender.ToLower());
                        Message.AppendStringWithBreak(Client.GetHabbo().Motto);

                        Room.SendMessage(Message);
                    }

                    // Unlock achievements
                    switch (header.ToLower())
                    {
                        case "updatemotto":

                            UberEnvironment.GetGame().GetAchievementManager().UnlockAchievement(Client, 5, 1);
                            break;

                        case "updatelook":

                            if (!Client.GetHabbo().MutantPenalty)
                            {
                                UberEnvironment.GetGame().GetAchievementManager().UnlockAchievement(Client, 1, 1);
                            }

                            break;
                    }

                    break;
                }
                default:
                {
                    UberEnvironment.GetLogging().WriteLine("Unrecognized MUS packet: " + data, Core.LogLevel.Error);
                    break;
                }
            }
        }
    }
}
