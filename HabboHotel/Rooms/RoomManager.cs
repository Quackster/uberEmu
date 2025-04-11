using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;
using System.Threading;

using Uber.HabboHotel.Items;
using Uber.HabboHotel.GameClients;
using Uber.Storage;

namespace Uber.HabboHotel.Rooms
{
    class RoomManager
    {
        public readonly int MAX_PETS_PER_ROOM = 15;

        private Dictionary<uint, Room> Rooms;
        private Dictionary<string, RoomModel> Models;
        private List<TeleUserData> TeleActions;
        private Thread EngineThread;

        private List<uint> RoomsToUnload;

        public int LoadedRoomsCount
        {
            get
            {
                return this.Rooms.Count;
            }
        }

        public RoomManager()
        {
            this.Rooms = new Dictionary<uint, Room>();
            this.Models = new Dictionary<string, RoomModel>();
            this.TeleActions = new List<TeleUserData>();

            this.EngineThread = new Thread(ProcessEngine);
            this.EngineThread.Name = "Room Engine";
            this.EngineThread.Priority = ThreadPriority.AboveNormal;
            this.EngineThread.Start();

            this.RoomsToUnload = new List<uint>();
        }

        public void AddTeleAction(TeleUserData Act)
        {
            lock (this.TeleActions)
            {
                this.TeleActions.Add(Act);
            }
        }

        public List<Room> GetEventRoomsForCategory(int Category)
        {
            List<Room> EventRooms = new List<Room>();

            lock (this.Rooms)
            {
                Dictionary<uint, Room>.Enumerator eRooms = this.Rooms.GetEnumerator();

                while (eRooms.MoveNext())
                {
                    Room Room = eRooms.Current.Value;

                    if (Room.Event == null)
                    {
                        continue;
                    }

                    if (Category > 0 && Room.Event.Category != Category)
                    {
                        continue;
                    }

                    EventRooms.Add(Room);
                }
            }

            return EventRooms;
        }

        public void ProcessEngine()
        {
            Thread.Sleep(5000);

            while (true)
            {
                DateTime ExecutionStart = DateTime.Now;

                try
                {
                    lock (this.Rooms)
                    {
                        Dictionary<uint, Room>.Enumerator eRooms = this.Rooms.GetEnumerator();

                        while (eRooms.MoveNext())
                        {
                            Room Room = eRooms.Current.Value;

                            if (!Room.KeepAlive)
                            {
                                continue;
                            }

                            Room.ProcessRoom();
                        }
                    }

                    lock (this.RoomsToUnload)
                    {
                        foreach (uint RoomId in this.RoomsToUnload)
                        {
                            UnloadRoom(RoomId);
                        }

                        this.RoomsToUnload.Clear();
                    }

                    lock (this.TeleActions)
                    {
                        List<TeleUserData>.Enumerator eTeleActions = this.TeleActions.GetEnumerator();

                        while (eTeleActions.MoveNext())
                        {
                            eTeleActions.Current.Execute();
                        }

                        this.TeleActions.Clear();
                    }
                }

                catch (InvalidOperationException)
                {
                    UberEnvironment.GetLogging().WriteLine("InvalidOpException in Room Manager..", Core.LogLevel.Error);
                }

                finally
                {
                    DateTime ExecutionComplete = DateTime.Now;
                    TimeSpan Diff = ExecutionComplete - ExecutionStart;

                    double sleepTime = 500 - Diff.TotalMilliseconds;

                    if (sleepTime < 0)
                    {
                        sleepTime = 0;
                    }

                    if (sleepTime > 500)
                    {
                        sleepTime = 500;
                    }

                    Thread.Sleep((int)Math.Floor(sleepTime));
                }
            }
        }

        public void LoadModels()
        {
            DataTable Data = null;
            Models.Clear();

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT id,door_x,door_y,door_z,door_dir,heightmap,public_items,club_only FROM room_models");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                Models.Add((string)Row["id"], new RoomModel((string)Row["id"], (int)Row["door_x"],
                    (int)Row["door_y"], (Double)Row["door_z"], (int)Row["door_dir"], (string)Row["heightmap"],
                    (string)Row["public_items"], UberEnvironment.EnumToBool(Row["club_only"].ToString())));
            }
        }

        public RoomModel GetModel(string Model)
        {
            if (Models.ContainsKey(Model))
            {
                return Models[Model];
            }

            return null;
        }

        public RoomData GenerateNullableRoomData(uint RoomId)
        {
            if (GenerateRoomData(RoomId) != null)
            {
                return GenerateRoomData(RoomId);
            }

            RoomData Data = new RoomData();
            Data.FillNull(RoomId);
            return Data;
        }

        public RoomData GenerateRoomData(uint RoomId)
        {
            RoomData Data = new RoomData();

            if (IsRoomLoaded(RoomId))
            {
                Data.Fill(GetRoom(RoomId));
            }
            else
            {
                DataRow Row = null;

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    Row = dbClient.ReadDataRow("SELECT * FROM rooms WHERE id = '" + RoomId + "' LIMIT 1");
                }

                if (Row == null)
                {
                    return null;
                }

                Data.Fill(Row);
            }

            return Data;
        }

        public Boolean IsRoomLoaded(uint RoomId)
        {
            if (GetRoom(RoomId) != null)
            {
                return true;
            }

            return false;
        }

        public void LoadRoom(uint Id)
        {
            if (IsRoomLoaded(Id))
            {
                return;
            }

            RoomData Data = GenerateRoomData(Id);

            if (Data == null)
            {
                return;
            }

            Room Room = new Room(Data.Id, Data.Name, Data.Description, Data.Type, Data.Owner, Data.Category, Data.State,
                Data.UsersMax, Data.ModelName, Data.CCTs, Data.Score, Data.Tags, Data.AllowPets, Data.AllowPetsEating,
                Data.AllowWalkthrough, Data.Icon, Data.Password, Data.Wallpaper, Data.Floor, Data.Landscape);

            lock (this.Rooms)
            {
                Rooms.Add(Room.RoomId, Room);
            }

            Room.InitBots();
            Room.InitPets();

            UberEnvironment.GetLogging().WriteLine("[RoomMgr] Loaded room: \"" + Room.Name + "\" (ID: " + Id + ")", Uber.Core.LogLevel.Information);
        }

        public void RequestRoomUnload(uint Id)
        {
            if (!IsRoomLoaded(Id))
            {
                return;
            }

            lock (this.RoomsToUnload)
            {
                GetRoom(Id).KeepAlive = false;
                RoomsToUnload.Add(Id);
            }
        }

        public void UnloadRoom(uint Id)
        {
            Room Room = GetRoom(Id);

            if (Room == null)
            {
                return;
            }

            lock (this.Rooms)
            {
                Room.Destroy();
                Rooms.Remove(Id);
            }

            UberEnvironment.GetLogging().WriteLine("[RoomMgr] Unloaded room: \"" + Room.Name + "\" (ID: " + Id + ")", Uber.Core.LogLevel.Information);
        }

        public Room GetRoom(uint RoomId)
        {
            lock (this.Rooms)
            {
                Dictionary<uint, Room>.Enumerator eRooms = this.Rooms.GetEnumerator();

                while (eRooms.MoveNext())
                {
                    Room Room = eRooms.Current.Value;

                    if (Room.RoomId == RoomId)
                    {
                        return Room;
                    }
                }
            }

            return null;
        }

        public RoomData CreateRoom(GameClient Session, string Name, string Model)
        {
            Name = UberEnvironment.FilterInjectionChars(Name);

            if (!Models.ContainsKey(Model))
            {
                Session.SendNotif("Sorry, this room model has not been added yet. Try again later.");
                return null;
            }

            if (Models[Model].ClubOnly && !Session.GetHabbo().HasFuse("fuse_use_special_room_layouts"))
            {
                Session.SendNotif("You must be an Uber Club member to use that room layout.");
                return null;
            }

            if (Name.Length < 3)
            {
                Session.SendNotif("Room name is too short for room creation!");
                return null;
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("caption", Name);
                dbClient.AddParamWithValue("model", Model);
                dbClient.AddParamWithValue("username", Session.GetHabbo().Username);
                dbClient.ExecuteQuery("INSERT INTO rooms (roomtype,caption,owner,model_name) VALUES ('private',@caption,@username,@model)");
            }

            uint RoomId = 0;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("caption", Name);
                dbClient.AddParamWithValue("username", Session.GetHabbo().Username);
                RoomId = (uint)dbClient.ReadDataRow("SELECT id FROM rooms WHERE owner = @username AND caption = @caption ORDER BY id DESC")[0];
            }

            return GenerateRoomData(RoomId);
        }
    }

    class TeleUserData
    {
        private RoomUser User;
        private uint RoomId;
        private uint TeleId;

        public TeleUserData(RoomUser User, uint RoomId, uint TeleId)
        {
            this.User = User;
            this.RoomId = RoomId;
            this.TeleId = TeleId;
        }

        public void Execute()
        {
            if (User == null || User.IsBot)
            {
                return;
            }

            User.GetClient().GetHabbo().IsTeleporting = true;
            User.GetClient().GetHabbo().TeleporterId = TeleId;
            User.GetClient().GetMessageHandler().PrepareRoomForUser(RoomId, "");
        }
    }
}
