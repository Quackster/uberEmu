using System;
using System.Threading;
using System.Net.Sockets;

using Uber.Core;
using Uber.Util;
using Uber.Messages;

namespace Uber.Net
{
    class TcpConnection
    {
        private readonly int RCV_BUFFER_SIZE = 512;
        private readonly int RCV_MILLI_DELAY = 0;

        public readonly uint Id;
        public readonly DateTime Created;

        private Socket Socket;

        private byte[] Buffer;

        private AsyncCallback DataReceivedCallback;
        private RouteReceivedDataCallback RouteDataCallback;

        public delegate void RouteReceivedDataCallback(ref byte[] Data);

        public int AgeInSeconds
        {
            get
            {
                int s = (int)(DateTime.Now - Created).TotalSeconds;

                if (s < 0)
                {
                    s = 0;
                }

                return s;
            }
        }

        public string IPAddress
        {
            get
            {
                if (Socket == null)
                {
                    return "";
                }

                return Socket.RemoteEndPoint.ToString().Split(':')[0];
            }
        }

        public Boolean IsAlive
        {
            get
            {
                if (Socket == null)
                {
                    return false;
                }

                return true;
            }
        }

        public TcpConnection(uint Id, Socket Sock)
        {
            this.Id = Id;
            this.Socket = Sock;
            this.Created = DateTime.Now;
        }

        public void Start(RouteReceivedDataCallback DataRouter)
        {
            this.Buffer = new byte[RCV_BUFFER_SIZE];
            this.DataReceivedCallback = new AsyncCallback(DataReceived);
            this.RouteDataCallback = DataRouter;

            WaitForData();
        }

        public void Stop()
        {
            if (!this.IsAlive)
            {
                return;
            }

            this.Socket.Close();
            this.Socket = null;
            this.Buffer = null;
            this.DataReceivedCallback = null;
        }

        public Boolean TestConnection()
        {
            try
            {
                return this.Socket.Send(new byte[] { 0 }) > 0;
            }
            catch { }

            return false;
        }

        private void ConnectionDead()
        {
            UberEnvironment.GetGame().GetClientManager().StopClient(Id);
        }

        public void SendData(byte[] Data)
        {
            if (!this.IsAlive)
            {
                return;
            }

            try
            {
                this.Socket.Send(Data);
            }

            catch (SocketException)
            {
                ConnectionDead();
            }

            catch (ObjectDisposedException)
            {
                ConnectionDead();
            }

            catch (Exception e)
            {
                UberEnvironment.GetLogging().WriteLine("[TCPConnection.SendData]: Unhandled exception while attempting to send data: " + e.Message, LogLevel.Error);
                ConnectionDead();
            }
        }

        public void SendData(string Data)
        {
            SendData(UberEnvironment.GetDefaultEncoding().GetBytes(Data));
        }

        public Socket GetSocket()
        {
            return this.Socket;
        }

        public void SendMessage(ServerMessage Message)
        {
            if (Message == null)
            {
                return;
            }

            //UberEnvironment.GetLogging().WriteLine("[" + Id + "] <-- " + Message.ToString(), LogLevel.Debug);

            SendData(Message.GetBytes());
        }

        private void WaitForData()
        {
            if (this.IsAlive)
            {
                try
                {
                    Socket.BeginReceive(this.Buffer, 0, RCV_BUFFER_SIZE, SocketFlags.None, DataReceivedCallback, null);
                }

                catch (SocketException)
                {
                    ConnectionDead();
                }

                catch (ObjectDisposedException)
                {
                    ConnectionDead();
                }

                catch (Exception e)
                {
                    UberEnvironment.GetLogging().WriteLine("[TCPConnection.WaitForData]: Unhandled exception while attempting to receive data: " + e.Message, LogLevel.Error);
                    ConnectionDead();
                }
            }
        }

        private void DataReceived(IAsyncResult iAr)
        {
            if (!this.IsAlive)
            {
                return;
            }

            if (RCV_MILLI_DELAY > 0)
            {
                Thread.Sleep(RCV_MILLI_DELAY);
            }

            int rcvBytesCount = 0;

            try
            {
                rcvBytesCount = Socket.EndReceive(iAr);
            }

            catch (ObjectDisposedException)
            {
                ConnectionDead();
                return;
            }

            catch (Exception e)
            {
                UberEnvironment.GetLogging().WriteLine("[TCPConnection.DataReceived]: Unhandled exception while attempting to complete receive data: " + e.Message, LogLevel.Error);
                ConnectionDead();
                return;
            }

            if (rcvBytesCount < 1)
            {
                ConnectionDead();
                return;
            }

            byte[] toProcess = ByteUtil.ChompBytes(Buffer, 0, rcvBytesCount);
            RouteData(ref toProcess);

            WaitForData();
        }

        private void RouteData(ref Byte[] Data)
        {
            if (this.RouteDataCallback != null)
            {
                this.RouteDataCallback.Invoke(ref Data);
            }
        }
    }




}
