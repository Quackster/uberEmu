using System;
using System.Threading;

using Uber.Storage;
using Uber.HabboHotel.GameClients;

namespace Uber.HabboHotel.Misc
{
    public class LowPriorityWorker
    {
        public static void Process()
        {
            // allow 10 seconds for Uber to finish anything it may be doing, then enter regular loop
            Thread.Sleep(10000);

            while (true)
            {
                #region Garbage Collection
                GC.Collect();
                GC.WaitForPendingFinalizers();
                #endregion

                #region Statistics
                int Status = 1;
                int UsersOnline = UberEnvironment.GetGame().GetClientManager().ClientCount;
                int RoomsLoaded = UberEnvironment.GetGame().GetRoomManager().LoadedRoomsCount;

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE server_status SET stamp = '" + UberEnvironment.GetUnixTimestamp() + "', status = '" + Status + "', users_online = '" + UsersOnline + "', rooms_loaded = '" + RoomsLoaded + "', server_ver = '" + UberEnvironment.PrettyVersion + "' LIMIT 1");
                }
                #endregion

                #region Effects
                UberEnvironment.GetGame().GetClientManager().CheckEffects();
                #endregion

                Thread.Sleep(30000);
            }
        }
    }
}