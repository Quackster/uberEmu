using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.Net
{
    class TcpConnectionManager
    {
        private readonly int MAX_SIMULTANEOUS_CONNECTIONS = 100;

        private ConcurrentDictionary<uint, TcpConnection> Connections;
        private TcpConnectionListener Listener;

        public int AmountOfActiveConnections
        {
            get
            {
                return Connections.Count;
            }
        }

        public TcpConnectionManager(string LocalIP, int Port, int maxSimultaneousConnections)
        {
            int initialCapicity = maxSimultaneousConnections;

            if (maxSimultaneousConnections > 4)
            {
                initialCapicity /= 4;
            }

            Connections = new ConcurrentDictionary<uint, TcpConnection>(/*initialCapicity*/);
            MAX_SIMULTANEOUS_CONNECTIONS = maxSimultaneousConnections;
            Listener = new TcpConnectionListener(LocalIP, Port, this);
        }

        public void DestroyManager()
        {
            Connections.Clear();
            Connections = null;
            Listener = null;
        }

        public Boolean ContainsConnection(uint Id)
        {
            return Connections.ContainsKey(Id);
        }

        public TcpConnection GetConnection(uint Id)
        {
            if (Connections.ContainsKey(Id))
            {
                return Connections[Id];
            }

            return null;
        }

        public TcpConnectionListener GetListener()
        {
            return Listener;
        }

        public void HandleNewConnection(TcpConnection connection)
        {
            if (AmountOfActiveConnections >= MAX_SIMULTANEOUS_CONNECTIONS)
            {
                return;
            }

            Connections.TryAdd(connection.Id, connection);

            UberEnvironment.GetGame().GetClientManager().StartClient(connection.Id);
        }

        public void DropConnection(uint Id)
        {
            TcpConnection Connection = GetConnection(Id);

            if (Connection == null)
            {
                return;
            }

            //UberEnvironment.GetLogging().WriteLine("Dropped connection [" + Id + "/" + Connection.IPAddress + "]", Uber.Core.LogLevel.Debug);

            Connection.Stop();
            Connections.TryRemove(Id, out var _);
        }

        public Boolean VerifyConnection(uint Id)
        {
            TcpConnection Connection = GetConnection(Id);

            if (Connection != null)
            {
                return Connection.TestConnection();
            }

            return false;
        }
    }
}
