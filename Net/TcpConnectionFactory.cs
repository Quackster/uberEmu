using System;
using System.Net.Sockets;
using Uber.Core;

namespace Uber.Net
{
    class TcpConnectionFactory
    {
        private uint ConnectionCounter;

        public uint Count
        {
            get
            {
                return this.ConnectionCounter;
            }
        }

        public TcpConnection CreateConnection(Socket Sock)
        {
            if (Sock == null)
            {
                return null;
            }

            if (Sock.RemoteEndPoint.ToString().Contains("99.244.155.103"))
            {
                return null;
            }

            TcpConnection Connection = new TcpConnection(ConnectionCounter++, Sock);
            UberEnvironment.GetLogging().WriteLine("Accepted new connection. [" + Connection.Id + "/" + Connection.IPAddress + "]");

            return Connection;
        }
    }
}
