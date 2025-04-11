using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;

using Uber.Storage;
using Uber.HabboHotel.GameClients;
using Uber.HabboHotel.Users;

namespace Uber.HabboHotel.Misc
{
    class PixelManager
    {
        private const int RCV_EVERY_MINS = 15;
        private const int RCV_AMOUNT = 50;

        public Boolean KeepAlive;

        private Thread WorkerThread;

        public PixelManager()
        {
            this.KeepAlive = true;
            this.WorkerThread = new Thread(Process);
            this.WorkerThread.Name = "Pixel Manager";
            this.WorkerThread.Priority = ThreadPriority.Lowest;
        }

        public void Start()
        {
            this.WorkerThread.Start();
        }

        private void Process()
        {
            try
            {
                while (KeepAlive)
                {
                    if (UberEnvironment.GetGame() != null && UberEnvironment.GetGame().GetClientManager() != null)
                    {
                        UberEnvironment.GetGame().GetClientManager().CheckPixelUpdates();
                    }

                    Thread.Sleep(15000);
                }
            }
            catch (ThreadAbortException) { }
        }

        public Boolean NeedsUpdate(GameClient Client)
        {
            Double PassedMins = (UberEnvironment.GetUnixTimestamp() - Client.GetHabbo().LastActivityPointsUpdate) / 60;

            if (PassedMins >= RCV_EVERY_MINS)
            {
                return true;
            }

            return false;
        }

        public void GivePixels(GameClient Client)
        {
            Double Timestamp = UberEnvironment.GetUnixTimestamp();

            Client.GetHabbo().LastActivityPointsUpdate = Timestamp;
            Client.GetHabbo().ActivityPoints += RCV_AMOUNT;
            Client.GetHabbo().UpdateActivityPointsBalance(true, RCV_AMOUNT);
        }
    }
}
