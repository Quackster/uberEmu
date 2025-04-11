using System;
using System.Collections.Generic;
using System.Threading;
using System.Data;

using Uber.Messages;
using Uber.Storage;
using Uber.HabboHotel.Support;

namespace Uber.HabboHotel.GameClients
{
    partial class GameClientManager
    {
        private Thread ConnectionChecker;
        private Dictionary<uint, GameClient> Clients;

        public int ClientCount
        {
            get
            {
                return this.Clients.Count;
            }
        }

        public GameClientManager()
        {
            this.Clients = new Dictionary<uint, GameClient>();
        }

        public void Clear()
        {
            lock (this.Clients)
            {
                Clients.Clear();
            }
        }

        public GameClient GetClient(uint ClientId)
        {
            if (Clients.ContainsKey(ClientId))
            {
                return Clients[ClientId];
            }

            return null;
        }

        public bool RemoveClient(uint ClientId)
        {
            lock (this.Clients)
            {
                return Clients.Remove(ClientId);
            }
        }

        public void StartClient(uint ClientId)
        {
            lock (this.Clients)
            {
                Clients.Add(ClientId, new GameClient(ClientId));
                Clients[ClientId].StartConnection();
            }
        }

        public void StopClient(uint ClientId)
        {
            GameClient Client = GetClient(ClientId);

            if (Client == null)
            {
                return;
            }

            UberEnvironment.GetConnectionManager().DropConnection(ClientId);

            Client.Stop();
            RemoveClient(ClientId);
        }

        public void StartConnectionChecker()
        {
            if (ConnectionChecker != null)
            {
                return;
            }

            ConnectionChecker = new Thread(TestClientConnections);
            ConnectionChecker.Name = "Connection Checker";
            ConnectionChecker.Priority = ThreadPriority.Lowest;
            ConnectionChecker.Start();
        }

        public void StopConnectionChecker()
        {
            if (ConnectionChecker == null)
            {
                return;
            }

            try
            {
                ConnectionChecker.Abort();
            }
            catch (ThreadAbortException) { }

            ConnectionChecker = null;
        }

        private void TestClientConnections()
        {
            int interval = int.Parse(UberEnvironment.GetConfig().data["client.ping.interval"]);

            if (interval <= 100)
            {
                throw new ArgumentException("Invalid configuration value for ping interval! Must be above 100 miliseconds.");
            }

            while (true)
            {
                ServerMessage PingMessage = new ServerMessage(50);

                try
                {              
                    List<uint> TimedOutClients = new List<uint>();
                    List<GameClient> ToPing = new List<GameClient>();

                    lock (this.Clients)
                    {
                        Dictionary<uint, GameClient>.Enumerator eClients = this.Clients.GetEnumerator();                     

                        while (eClients.MoveNext())
                        {
                            GameClient Client = eClients.Current.Value;

                            if (Client.PongOK)
                            {
                                Client.PongOK = false;
                                ToPing.Add(Client);
                            }
                            else
                            {
                                TimedOutClients.Add(Client.ClientId);
                            }
                        }
                    }

                    foreach (uint ClientId in TimedOutClients)
                    {
                        UberEnvironment.GetGame().GetClientManager().StopClient(ClientId);
                    }

                    foreach (GameClient Client in ToPing)
                    {
                        try
                        {
                            Client.GetConnection().SendMessage(PingMessage);
                        }
                        catch (Exception) { }
                    }

                    Thread.Sleep(interval);
                }
                catch (ThreadAbortException) { }
            }
        }

        public GameClient GetClientByHabbo(uint HabboId)
        {
            lock (this.Clients)
            {
                Dictionary<uint, GameClient>.Enumerator eClients = this.Clients.GetEnumerator();

                while (eClients.MoveNext())
                {
                    GameClient Client = eClients.Current.Value;

                    if (Client.GetHabbo() == null)
                    {
                        continue;
                    }

                    if (Client.GetHabbo().Id == HabboId)
                    {
                        return Client;
                    }
                }
            }

            return null;
        }

        public GameClient GetClientByHabbo(string Name)
        {
            lock (this.Clients)
            {
                Dictionary<uint, GameClient>.Enumerator eClients = this.Clients.GetEnumerator();

                while (eClients.MoveNext())
                {
                    GameClient Client = eClients.Current.Value;

                    if (Client.GetHabbo() == null)
                    {
                        continue;
                    }

                    if (Client.GetHabbo().Username.ToLower() == Name.ToLower())
                    {
                        return Client;
                    }
                }
            }

            return null;
        }

        public void BroadcastMessage(ServerMessage Message)
        {
            this.BroadcastMessage(Message, "");
        }

        public void BroadcastMessage(ServerMessage Message, String FuseRequirement)
        {
            lock (this.Clients)
            {
                Dictionary<uint, GameClient>.Enumerator eClients = this.Clients.GetEnumerator();

                while (eClients.MoveNext())
                {
                    GameClient Client = eClients.Current.Value;

                    try
                    {
                        if (FuseRequirement.Length > 0)
                        {
                            if (Client.GetHabbo() == null || !Client.GetHabbo().HasFuse(FuseRequirement))
                            {
                                continue;
                            }
                        }

                        Client.SendMessage(Message);
                    }
                    catch (Exception) { }
                }
            }
        }

        public void CheckEffects()
        {
            lock (this.Clients)
            {
                Dictionary<uint, GameClient>.Enumerator eClients = this.Clients.GetEnumerator();

                while (eClients.MoveNext())
                {
                    GameClient Client = eClients.Current.Value;

                    if (Client.GetHabbo() == null || Client.GetHabbo().GetAvatarEffectsInventoryComponent() == null)
                    {
                        continue;
                    }

                    Client.GetHabbo().GetAvatarEffectsInventoryComponent().CheckExpired();
                }
            }
        }
    }
}
