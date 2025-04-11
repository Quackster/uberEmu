using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Data;

using Uber.HabboHotel.Pets;
using Uber.HabboHotel.RoomBots;
using Uber.HabboHotel.Pathfinding;
using Uber.HabboHotel.Items;
using Uber.HabboHotel.GameClients;
using Uber.Messages;
using Uber.Storage;

namespace Uber.HabboHotel.Rooms
{
    enum MatrixState
    {
        BLOCKED = 0,
        WALKABLE = 1,
        WALKABLE_LASTSTEP = 2
    }

    class Room
    {
        private uint Id;

        public string Name;
        public string Description;
        public string Type;
        public string Owner;
        public string Password;
        public int Category;
        public int State;
        public int UsersNow;
        public int UsersMax;
        public string ModelName;
        public string CCTs;
        public int Score;
        public List<string> Tags;
        public bool AllowPets;
        public bool AllowPetsEating;
        public bool AllowWalkthrough;

        public List<RoomUser> UserList;
        public int UserCounter = 0;

        private int IdleTime;

        public RoomIcon myIcon;

        public List<uint> UsersWithRights;
        private Dictionary<uint, Double> Bans;

        public RoomEvent Event;

        public string Wallpaper;
        public string Floor;
        public string Landscape;

        public List<RoomItem> Items;
        public MoodlightData MoodlightData;

        public List<Trade> ActiveTrades;

        public bool KeepAlive;

        public MatrixState[,] Matrix;
        public bool[,] UserMatrix;
        public Coord[,] BedMatrix;
        public double[,] HeightMatrix;
        public double[,] TopStackHeight;

        public Boolean HasOngoingEvent
        {
            get
            {
                if (Event != null)
                {
                    return true;
                }

                return false;
            }
        }

        public RoomIcon Icon
        {
            get
            {
                return myIcon;
            }

            set
            {
                myIcon = value;
            }
        }

        public int UserCount
        {
            get
            {
                int i = 0;

                lock (UserList)
                {
                    foreach (RoomUser User in UserList)
                    {
                        if (User.IsBot)
                        {
                            continue;
                        }

                        i++;
                    }
                }

                return i;
            }
        }

        public int TagCount
        {
            get
            {
                return Tags.Count;
            }
        }

        public RoomModel Model
        {
            get
            {
                return UberEnvironment.GetGame().GetRoomManager().GetModel(ModelName);
            }
        }

        public uint RoomId
        {
            get
            {
                return Id;
            }
        }

        public List<RoomItem> FloorItems
        {
            get
            {
                List<RoomItem> FloorItems = new List<RoomItem>();

                lock (Items)
                {
                    foreach (RoomItem Item in Items)
                    {
                        if (!Item.IsFloorItem)
                        {
                            continue;
                        }

                        FloorItems.Add(Item);
                    }
                }

                return FloorItems;
            }
        }

        public List<RoomItem> WallItems
        {
            get
            {
                List<RoomItem> WallItems = new List<RoomItem>();

                lock (Items)
                {
                    foreach (RoomItem Item in Items)
                    {
                        if (!Item.IsWallItem)
                        {
                            continue;
                        }

                        WallItems.Add(Item);
                    }
                }

                return WallItems;
            }
        }

        public Boolean CanTradeInRoom
        {
            get
            {
                if (IsPublic)
                {
                    return false;
                }

                return true;
            }
        }

        public bool IsPublic
        {
            get
            {
                if (Type == "public")
                {
                    return true;
                }

                return false;
            }
        }

        public int PetCount
        {
            get
            {
                int c = 0;

                lock (this.UserList)
                {
                    List<RoomUser>.Enumerator Users = this.UserList.GetEnumerator();

                    while (Users.MoveNext())
                    {
                        if (Users.Current.IsPet)
                        {
                            c++;
                        }
                    }
                }

                return c;
            }
        }

        public Room(uint Id, string Name, string Description, string Type, string Owner, int Category,
            int State, int UsersMax, string ModelName, string CCTs, int Score, List<string> Tags, bool AllowPets,
            bool AllowPetsEating, bool AllowWalkthrough, RoomIcon Icon, string Password, string Wallpaper, string Floor,
            string Landscape)
        {
            this.Id = Id;
            this.Name = Name;
            this.Description = Description;
            this.Owner = Owner;
            this.Category = Category;
            this.Type = Type;
            this.State = State;
            this.UsersNow = 0;
            this.UsersMax = UsersMax;
            this.ModelName = ModelName;
            this.CCTs = CCTs;
            this.Score = Score;
            this.Tags = Tags;
            this.AllowPets = AllowPets;
            this.AllowPetsEating = AllowPetsEating;
            this.AllowWalkthrough = AllowWalkthrough;
            this.UserCounter = 0;
            this.UserList = new List<RoomUser>();
            this.myIcon = Icon;
            this.Password = Password;
            this.Bans = new Dictionary<uint, double>();
            this.Event = null;
            this.Wallpaper = Wallpaper;
            this.Floor = Floor;
            this.Landscape = Landscape;
            this.Items = new List<RoomItem>();
            this.ActiveTrades = new List<Trade>();
            this.UserMatrix = new bool[Model.MapSizeX, Model.MapSizeY];

            this.IdleTime = 0;

            this.KeepAlive = true;

            LoadRights();
            LoadFurniture();

            GenerateMaps();
        }

        public void InitBots()
        {
            List<RoomBot> Bots = UberEnvironment.GetGame().GetBotManager().GetBotsForRoom(RoomId);

            foreach (RoomBot Bot in Bots)
            {
                DeployBot(Bot);
            }
        }

        public void InitPets()
        {
            List<Pet> Pets = new List<Pet>();
            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("roomid", RoomId);
                Data = dbClient.ReadDataTable("SELECT * FROM user_pets WHERE room_id = @roomid");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                Pet Pet = UberEnvironment.GetGame().GetCatalog().GeneratePetFromRow(Row);
                DeployBot(new RoomBot(Pet.PetId, RoomId, "pet", "freeroam", Pet.Name, "", Pet.Look, Pet.X, Pet.Y, (int)Pet.Z, 0, 0, 0, 0, 0), Pet);
            }
        }

        public RoomUser DeployBot(RoomBot Bot)
        {
            return DeployBot(Bot, null);
        }

        public RoomUser DeployBot(RoomBot Bot, Pet PetData)
        {
            RoomUser BotUser = new RoomUser(0, RoomId, UserCounter++);

            if ((Bot.X > 0 && Bot.Y > 0) && Bot.X < Model.MapSizeX && Bot.Y < Model.MapSizeY)
            {
                BotUser.SetPos(Bot.X, Bot.Y, Bot.Z);
                BotUser.SetRot(Bot.Rot);
            }
            else
            {
                Bot.X = Model.DoorX;
                Bot.Y = Model.DoorY;

                BotUser.SetPos(Model.DoorX, Model.DoorY, Model.DoorZ);
                BotUser.SetRot(Model.DoorOrientation);
            }

            UserMatrix[Bot.X, Bot.Y] = true;

            BotUser.BotData = Bot;
            BotUser.BotAI = Bot.GenerateBotAI(BotUser.VirtualId);

            if (BotUser.IsPet)
            {
                BotUser.BotAI.Init((int)Bot.BotId, BotUser.VirtualId, RoomId);
                BotUser.PetData = PetData;
                BotUser.PetData.VirtualId = BotUser.VirtualId;
            }
            else
            {
                BotUser.BotAI.Init(-1, BotUser.VirtualId, RoomId);
            }

            lock (this.UserList)
            {
                UserList.Add(BotUser);
            }

            UpdateUserStatus(BotUser);
            BotUser.UpdateNeeded = true;

            ServerMessage EnterMessage = new ServerMessage(28);
            EnterMessage.AppendInt32(1);
            BotUser.Serialize(EnterMessage);
            SendMessage(EnterMessage);

            BotUser.BotAI.OnSelfEnterRoom();

            return BotUser;
        }

        public void RemoveBot(int VirtualId, bool Kicked)
        {
            RoomUser User = GetRoomUserByVirtualId(VirtualId);

            if (User == null || !User.IsBot)
            {
                return;
            }

            User.BotAI.OnSelfLeaveRoom(Kicked);

            ServerMessage LeaveMessage = new ServerMessage(29);
            LeaveMessage.AppendRawInt32(User.VirtualId);
            SendMessage(LeaveMessage);

            UserMatrix[User.X, User.Y] = false;

            UserList.Remove(User);
        }

        public void OnUserSay(RoomUser User, string Message, bool Shout)
        {
            lock (UserList)
            {
                foreach (RoomUser Usr in UserList)
                {
                    if (!Usr.IsBot)
                    {
                        continue;
                    }

                    if (Shout)
                    {
                        Usr.BotAI.OnUserShout(User, Message);
                    }
                    else
                    {
                        Usr.BotAI.OnUserSay(User, Message);
                    }
                }
            }
        }

        public void RegenerateUserMatrix()
        {
            lock (this.UserList)
            {
                this.UserMatrix = new bool[Model.MapSizeX, Model.MapSizeY];
                List<RoomUser>.Enumerator eUsers = this.UserList.GetEnumerator();

                while (eUsers.MoveNext())
                {
                    RoomUser User = eUsers.Current;
                    this.UserMatrix[User.X, User.Y] = true;
                }
            }
        }

        public void GenerateMaps()
        {
            // Create matrix arrays
            Matrix = new MatrixState[Model.MapSizeX, Model.MapSizeY];
            BedMatrix = new Coord[Model.MapSizeX, Model.MapSizeY];
            HeightMatrix = new double[Model.MapSizeX, Model.MapSizeY];
            TopStackHeight = new double[Model.MapSizeX, Model.MapSizeY];

            // Fill in the basic data based purely on the heightmap
            for (int line = 0; line < Model.MapSizeY; line++)
            {
                for (int chr = 0; chr < Model.MapSizeX; chr++)
                {
                    Matrix[chr, line] = MatrixState.BLOCKED;
                    BedMatrix[chr, line] = new Coord(chr, line);
                    HeightMatrix[chr, line] = 0;
                    TopStackHeight[chr, line] = 0.0;

                    if (chr == Model.DoorX && line == Model.DoorY)
                    {
                        Matrix[chr, line] = MatrixState.WALKABLE_LASTSTEP;
                    }
                    else if (Model.SqState[chr, line] == SquareState.OPEN)
                    {
                        Matrix[chr, line] = MatrixState.WALKABLE;
                    }
                    else if (Model.SqState[chr, line] == SquareState.SEAT)
                    {
                        Matrix[chr, line] = MatrixState.WALKABLE_LASTSTEP;
                    }
                }
            }

            // Loop through the items in the room
            lock (Items)
            {
                foreach (RoomItem Item in Items)
                {
                    // If we're dealing with anything other than a floor item, skip
                    if (Item.GetBaseItem().Type.ToLower() != "s")
                    {
                        continue;
                    }

                    // If this is a rug, ignore it.
                    if (Item.GetBaseItem().Height <= 0)
                    {
                        continue;
                    }

                    // Make sure we're the highest item here!
                    if (TopStackHeight[Item.X, Item.Y] <= Item.Z)
                    {
                        TopStackHeight[Item.X, Item.Y] = Item.Z;

                        // If this item is walkable and on the floor, allow users to walk here.
                        if (Item.GetBaseItem().Walkable)
                        {
                            Matrix[Item.X, Item.Y] = MatrixState.WALKABLE;
                            HeightMatrix[Item.X, Item.Y] = Item.GetBaseItem().Height;
                        }
                        // If this item is a gate, open, and on the floor, allow users to walk here.
                        else if (Item.Z <= (Model.SqFloorHeight[Item.X, Item.Y] + 0.1) && Item.GetBaseItem().InteractionType.ToLower() == "gate" && Item.ExtraData == "1")
                        {
                            Matrix[Item.X, Item.Y] = MatrixState.WALKABLE;
                        }
                        // If this item is a set or a bed, make it's square walkable (but only if last step)
                        else if (Item.GetBaseItem().IsSeat || Item.GetBaseItem().InteractionType.ToLower() == "bed")
                        {
                            Matrix[Item.X, Item.Y] = MatrixState.WALKABLE_LASTSTEP;
                        }
                        // Finally, if it's none of those, block the square.
                        else
                        {
                            Matrix[Item.X, Item.Y] = MatrixState.BLOCKED;
                        }
                    }

                    Dictionary<int, AffectedTile> Points = GetAffectedTiles(Item.GetBaseItem().Length, Item.GetBaseItem().Width, Item.X, Item.Y, Item.Rot);

                    if (Points == null)
                    {
                        Points = new Dictionary<int, AffectedTile>();
                    }

                    foreach (AffectedTile Tile in Points.Values)
                    {
                        // Make sure we're the highest item here!
                        if (TopStackHeight[Tile.X, Tile.Y] <= Item.Z)
                        {
                            TopStackHeight[Tile.X, Tile.Y] = Item.Z;

                            // If this item is walkable and on the floor, allow users to walk here.
                            if (Item.GetBaseItem().Walkable)
                            {
                                Matrix[Tile.X, Tile.Y] = MatrixState.WALKABLE;
                                HeightMatrix[Tile.X, Tile.Y] = Item.GetBaseItem().Height;
                            }
                            // If this item is a gate, open, and on the floor, allow users to walk here.
                            else if (Item.Z <= (Model.SqFloorHeight[Item.X, Item.Y] + 0.1) && Item.GetBaseItem().InteractionType.ToLower() == "gate" && Item.ExtraData == "1")
                            {
                                Matrix[Tile.X, Tile.Y] = MatrixState.WALKABLE;
                            }
                            // If this item is a set or a bed, make it's square walkable (but only if last step)
                            else if (Item.GetBaseItem().IsSeat || Item.GetBaseItem().InteractionType.ToLower() == "bed")
                            {
                                Matrix[Tile.X, Tile.Y] = MatrixState.WALKABLE_LASTSTEP;
                            }
                            // Finally, if it's none of those, block the square.
                            else
                            {
                                Matrix[Tile.X, Tile.Y] = MatrixState.BLOCKED;
                            }
                        }

                        // Set bad maps
                        if (Item.GetBaseItem().InteractionType.ToLower() == "bed")
                        {
                            if (Item.Rot == 0 || Item.Rot == 4)
                            {
                                BedMatrix[Tile.X, Tile.Y].y = Item.Y;
                            }

                            if (Item.Rot == 2 || Item.Rot == 6)
                            {
                                BedMatrix[Tile.X, Tile.Y].x = Item.X;
                            }
                        }
                    }
                }
            }
        }

        public void LoadRights()
        {
            this.UsersWithRights = new List<uint>();

            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT user_id FROM room_rights WHERE room_id = '" + Id + "'");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                this.UsersWithRights.Add((uint)Row["user_id"]);
            }
        }

        public void LoadFurniture()
        {
            this.Items.Clear();

            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT * FROM room_items WHERE room_id = '" + Id + "'");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                RoomItem Item = new RoomItem((uint)Row["id"], RoomId, (uint)Row["base_item"], (string)Row["extra_data"],
                    (int)Row["x"], (int)Row["y"], (Double)Row["z"], (int)Row["rot"], (string)Row["wall_pos"]);

                switch (Item.GetBaseItem().InteractionType.ToLower())
                {
                    case "dimmer":

                        if (MoodlightData == null)
                        {
                            MoodlightData = new MoodlightData(Item.Id);
                        }

                        break;
                }

                this.Items.Add(Item);
            }
        }

        public Boolean CheckRights(GameClient Session)
        {
            return CheckRights(Session, false);
        }

        public Boolean CheckRights(GameClient Session, bool RequireOwnership)
        {
            if (Session.GetHabbo().Username.ToLower() == Owner.ToLower())
            {
                return true;
            }

            if (Session.GetHabbo().HasFuse("fuse_admin") || Session.GetHabbo().HasFuse("fuse_any_room_controller"))
            {
                return true;
            }

            if (!RequireOwnership)
            {
                if (Session.GetHabbo().HasFuse("fuse_any_room_rights"))
                {
                    return true;
                }

                if (UsersWithRights.Contains(Session.GetHabbo().Id))
                {
                    return true;
                }
            }

            return false;
        }

        public RoomItem GetItem(uint Id)
        {
            lock (Items)
            {
                foreach (RoomItem Item in Items)
                {
                    if (Item.Id == Id)
                    {
                        return Item;
                    }
                }
            }

            return null;
        }

        public void RemoveFurniture(GameClient Session, uint Id)
        {
            RoomItem Item = GetItem(Id);

            if (Item == null)
            {
                return;
            }

            Item.Interactor.OnRemove(Session, Item);

            if (Item.IsWallItem)
            {
                ServerMessage Message = new ServerMessage(84);
                Message.AppendRawUInt(Item.Id);
                Message.AppendStringWithBreak("");
                Message.AppendBoolean(false);
                SendMessage(Message);
            }
            else if (Item.IsFloorItem)
            {
                ServerMessage Message = new ServerMessage(94);
                Message.AppendRawUInt(Item.Id);
                Message.AppendStringWithBreak("");
                Message.AppendBoolean(false);
                SendMessage(Message);
            }

            lock (this.Items)
            {
                Items.Remove(Item);
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("DELETE FROM room_items WHERE id = '" + Id + "' AND room_id = '" + RoomId + "' LIMIT 1");
            }

            GenerateMaps();
            UpdateUserStatusses();
        }

        public bool CanWalk(int X, int Y, Double Z, Boolean LastStep)
        {
            if (X < 0 || X >= Model.MapSizeX || Y >= Model.MapSizeY || Y < 0)
            {
                return false;
            }

            if (SquareHasUsers(X, Y, LastStep))
            {
                return false;
            }

            if (Matrix[X, Y] == MatrixState.BLOCKED)
            {
                return false;
            }
            else if (Matrix[X, Y] == MatrixState.WALKABLE_LASTSTEP && !LastStep)
            {
                return false;
            }

            return true;
        }

        public void ProcessRoom()
        {
            int i = 0;

            // Loop through all furni and process them if they want to be processed
            lock (this.Items)
            {
                List<RoomItem>.Enumerator eItems = this.Items.GetEnumerator();

                while (eItems.MoveNext())
                {
                    RoomItem Item = eItems.Current;

                    if (!Item.UpdateNeeded)
                    {
                        continue;
                    }

                    Item.ProcessUpdates();
                }
            }

            // Loop through all users and bots and process them
            lock (this.UserList)
            {
                List<uint> ToRemove = new List<uint>();
                List<RoomUser>.Enumerator eUsers = this.UserList.GetEnumerator();

                while (eUsers.MoveNext())
                {
                    RoomUser User = eUsers.Current;

                    User.IdleTime++;

                    if (!User.IsAsleep && User.IdleTime >= 600)
                    {
                        User.IsAsleep = true;

                        ServerMessage FallAsleep = new ServerMessage(486);
                        FallAsleep.AppendInt32(User.VirtualId);
                        FallAsleep.AppendBoolean(true);
                        SendMessage(FallAsleep);
                    }

                    if (User.NeedsAutokick && !ToRemove.Contains(User.HabboId))
                    {
                        ToRemove.Add(User.HabboId);
                    }

                    if (User.CarryItemID > 0)
                    {
                        User.CarryTimer--;

                        if (User.CarryTimer <= 0)
                        {
                            User.CarryItem(0);
                        }
                    }

                    bool invalidSetStep = false;

                    if (User.SetStep)
                    {
                        if (CanWalk(User.SetX, User.SetY, 0, true) || User.AllowOverride)
                        {
                            UserMatrix[User.X, User.Y] = false;

                            User.X = User.SetX;
                            User.Y = User.SetY;
                            User.Z = User.SetZ;

                            UserMatrix[User.X, User.Y] = true;

                            UpdateUserStatus(User);
                        }
                        else
                        {
                            invalidSetStep = true;
                        }

                        User.SetStep = false;
                    }

                    if (User.PathRecalcNeeded)
                    {
                        Pathfinder Pathfinder = new Pathfinder(this, User);

                        User.GoalX = User.PathRecalcX;
                        User.GoalY = User.PathRecalcY;

                        User.Path.Clear();
                        User.Path = Pathfinder.FindPath();

                        if (User.Path.Count > 1)
                        {
                            User.PathStep = 1;
                            User.IsWalking = true;
                            User.PathRecalcNeeded = false;
                        }
                        else
                        {
                            User.PathRecalcNeeded = false;
                            User.Path.Clear();
                        }
                    }

                    if (User.IsWalking)
                    {
                        if (invalidSetStep || User.PathStep >= User.Path.Count || User.GoalX == User.X && User.Y == User.GoalY)
                        {
                            User.Path.Clear();
                            User.IsWalking = false;
                            User.RemoveStatus("mv");
                            User.PathRecalcNeeded = false;

                            if (User.X == Model.DoorX && User.Y == Model.DoorY && !ToRemove.Contains(User.HabboId) && !User.IsBot)
                            {
                                ToRemove.Add(User.HabboId);
                            }

                            UpdateUserStatus(User);
                        }
                        else
                        {
                            int k = (User.Path.Count - User.PathStep) - 1;
                            Coord NextStep = User.Path[k];
                            User.PathStep++;

                            int nextX = NextStep.x;
                            int nextY = NextStep.y;

                            User.RemoveStatus("mv");

                            bool LastStep = false;

                            if (nextX == User.GoalX && nextY == User.GoalY)
                            {
                                LastStep = true;
                            }

                            if (CanWalk(nextX, nextY, 0, LastStep) || User.AllowOverride)
                            {
                                double nextZ = SqAbsoluteHeight(nextX, nextY);

                                User.Statusses.Remove("lay");
                                User.Statusses.Remove("sit");
                                User.AddStatus("mv", nextX + "," + nextY + "," + nextZ.ToString().Replace(',','.'));

                                int newRot = Rotation.Calculate(User.X, User.Y, nextX, nextY);

                                User.RotBody = newRot;
                                User.RotHead = newRot;

                                User.SetStep = true;
                                User.SetX = BedMatrix[nextX, nextY].x;
                                User.SetY = BedMatrix[nextX, nextY].y;
                                User.SetZ = nextZ;
                            }
                            else
                            {
                                User.IsWalking = false;
                            }
                        }

                        User.UpdateNeeded = true;
                    }
                    else
                    {
                        if (User.Statusses.ContainsKey("mv"))
                        {
                            User.RemoveStatus("mv");
                            User.UpdateNeeded = true;
                        }
                    }

                    if (User.IsBot) 
                    {
                        User.BotAI.OnTimerTick();
                    }
                    else
                    {
                        i++; // we do not count bots. we do not take kindly to their kind 'round 'ere.
                    }
                }

                foreach (uint toRemove in ToRemove)
                {
                    RemoveUserFromRoom(UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(toRemove), true, false);
                }
            }

            // Update idle time
            if (i >= 1)
            {
                this.IdleTime = 0;
            }
            else
            {
                this.IdleTime++;
            }

            // If room has been idle for a while
            if (this.IdleTime >= 60)
            {
                UberEnvironment.GetLogging().WriteLine("[RoomMgr] Requesting unload of idle room - ID#: " + Id, Uber.Core.LogLevel.Debug);
                UberEnvironment.GetGame().GetRoomManager().RequestRoomUnload(Id);
            }

            ServerMessage Updates = SerializeStatusUpdates(false);

            if (Updates != null)
            {
                SendMessage(Updates);
            }
        }

        #region User handling (General)
        public void AddUserToRoom(GameClient Session, bool Spectator)
        {
            RoomUser User = new RoomUser(Session.GetHabbo().Id, RoomId, UserCounter++);

            if (Spectator)
            {
                User.IsSpectator = true;
            }
            else
            {
                User.SetPos(Model.DoorX, Model.DoorY, Model.DoorZ);
                User.SetRot(Model.DoorOrientation);                

                if (CheckRights(Session, true))
                {
                    User.AddStatus("flatcrtl", "useradmin");
                }
                else if (CheckRights(Session))
                {
                    User.AddStatus("flatcrtl", "");
                }

                if (!User.IsBot && User.GetClient().GetHabbo().IsTeleporting)
                {
                    RoomItem Item = GetItem(User.GetClient().GetHabbo().TeleporterId);

                    if (Item != null)
                    {
                        User.SetPos(Item.X, Item.Y, Item.Z);
                        User.SetRot(Item.Rot);

                        Item.InteractingUser2 = Session.GetHabbo().Id;
                        Item.ExtraData = "2";
                        Item.UpdateState(false, true);
                    }
                }

                User.GetClient().GetHabbo().IsTeleporting = false;
                User.GetClient().GetHabbo().TeleporterId = 0;

                ServerMessage EnterMessage = new ServerMessage(28);
                EnterMessage.AppendInt32(1);
                User.Serialize(EnterMessage);
                SendMessage(EnterMessage);
            }

            UserList.Add(User);
            Session.GetHabbo().OnEnterRoom(Id);

            if (!Spectator)
            {
                UpdateUserCount();

                lock (UserList)
                {
                    foreach (RoomUser Usr in UserList)
                    {
                        if (!Usr.IsBot)
                        {
                            continue;
                        }

                        Usr.BotAI.OnUserEnterRoom(User);
                    }
                }
            }
        }

        public void RemoveUserFromRoom(GameClient Session, Boolean NotifyClient, Boolean NotifyKick)
        {
            if (Session == null)
            {
                return;
            }

            RoomUser User = GetRoomUserByHabbo(Session.GetHabbo().Id);

            lock (this.UserList)
            {
                if (!UserList.Remove(GetRoomUserByHabbo(Session.GetHabbo().Id)))
                {
                    return;
                }
            }

            if (NotifyClient)
            {
                if (NotifyKick)
                {
                    Session.GetMessageHandler().GetResponse().Init(33);
                    Session.GetMessageHandler().GetResponse().AppendInt32(4008);
                    Session.GetMessageHandler().SendResponse();
                }

                Session.GetMessageHandler().GetResponse().Init(18);
                Session.GetMessageHandler().SendResponse();
            }

            List<RoomUser> PetsToRemove = new List<RoomUser>();

            lock (this.UserList)
            {
                if (!User.IsSpectator)
                {
                    if (User != null)
                    {
                        UserMatrix[User.X, User.Y] = false;

                        ServerMessage LeaveMessage = new ServerMessage(29);
                        LeaveMessage.AppendRawInt32(User.VirtualId);
                        SendMessage(LeaveMessage);
                    }

                    if (Session.GetHabbo() != null)
                    {
                        if (HasActiveTrade(Session.GetHabbo().Id))
                        {
                            TryStopTrade(Session.GetHabbo().Id);
                        }

                        if (Session.GetHabbo().Username.ToLower() == Owner.ToLower())
                        {
                            if (HasOngoingEvent)
                            {
                                Event = null;

                                ServerMessage Message = new ServerMessage(370);
                                Message.AppendStringWithBreak("-1");
                                SendMessage(Message);
                            }
                        }

                        Session.GetHabbo().OnLeaveRoom();
                    }
                }

                if (!User.IsSpectator)
                {
                    UpdateUserCount();

                    List<RoomUser> Bots = new List<RoomUser>();

                    foreach (RoomUser Usr in UserList)
                    {
                        if (!Usr.IsBot)
                        {
                            continue;
                        }

                        Bots.Add(Usr);
                    }

                    foreach (RoomUser Bot in Bots)
                    {
                        Bot.BotAI.OnUserLeaveRoom(Session);

                        if (Bot.IsPet && Bot.PetData.OwnerId == Session.GetHabbo().Id && !CheckRights(Session, true))
                        {
                            PetsToRemove.Add(Bot);
                        }
                    }
                }
            }

            foreach (RoomUser toRemove in PetsToRemove)
            {
                Session.GetHabbo().GetInventoryComponent().AddPet(toRemove.PetData);
                RemoveBot(toRemove.VirtualId, false);
            }
        }

        public RoomUser GetPet(uint PetId)
        {
            lock (this.UserList)
            {
                List<RoomUser>.Enumerator Users = this.UserList.GetEnumerator();

                while (Users.MoveNext())
                {
                    RoomUser User = Users.Current;

                    if (User.IsBot && User.IsPet && User.PetData != null && User.PetData.PetId == PetId)
                    {
                        return User;
                    }
                }
            }

            return null;
        }

        public bool RoomContainsPet(uint PetId)
        {
            return (GetPet(PetId) != null);
        }

        public void UpdateUserCount()
        {
            this.UsersNow = UserCount;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("UPDATE rooms SET users_now = '" + this.UsersNow + "' WHERE id = '" + Id + "' LIMIT 1");
            }
        }

        public RoomUser GetRoomUserByVirtualId(int VirtualId)
        {
            lock (UserList)
            {
                foreach (RoomUser User in UserList)
                {
                    if (User.VirtualId == VirtualId)
                    {
                        return User;
                    }
                }
            }

            return null;
        }

        public RoomUser GetRoomUserByHabbo(uint Id)
        {
            lock (UserList)
            {
                foreach (RoomUser User in UserList)
                {
                    if (User.IsBot)
                    {
                        continue;
                    }

                    if (User.HabboId == Id)
                    {
                        return User;
                    }
                }
            }

            return null;
        }

        public RoomUser GetRoomUserByHabbo(string Name)
        {
            lock (UserList)
            {
                foreach (RoomUser User in UserList)
                {
                    if (User.IsBot || User.GetClient().GetHabbo() == null)
                    {
                        continue;
                    }
                    
                    if (User.GetClient().GetHabbo().Username.ToLower() == Name.ToLower())
                    {
                        return User;
                    }
                }
            }

            return null;
        }
        #endregion

        #region Communication
        public void SendMessage(ServerMessage Message)
        {
            try
            {
                lock (this.UserList)
                {
                    foreach (RoomUser User in this.UserList)
                    {
                        if (User.IsBot || User.GetClient() == null)
                        {
                            continue;
                        }

                        User.GetClient().SendMessage(Message);
                    }
                }
            }
            catch (InvalidOperationException) { }
        }

        public void SendMessageToUsersWithRights(ServerMessage Message)
        {
            lock (UserList)
            {
                foreach (RoomUser User in UserList)
                {
                    if (User.IsBot)
                    {
                        continue;
                    }

                    if (!CheckRights(User.GetClient()))
                    {
                        continue;
                    }

                    User.GetClient().SendMessage(Message);
                }
            }
        }
        #endregion

        public void Destroy()
        {
            // Send kick message. No users should be left in this room, but if they are for whatever reason, they should leave.
            SendMessage(new ServerMessage(18));

            // Clear user list and dispose of the engine thread
            this.IdleTime = 0;
            this.KeepAlive = false;
            UserList.Clear();
        }

        public ServerMessage SerializeStatusUpdates(Boolean All)
        {
            List<RoomUser> Users = new List<RoomUser>();

            lock (UserList)
            {
                foreach (RoomUser User in UserList)
                {
                    if (!All)
                    {
                        if (!User.UpdateNeeded)
                        {
                            continue;
                        }

                        User.UpdateNeeded = false;
                    }

                    Users.Add(User);
                }
            }

            if (Users.Count == 0)
            {
                return null;
            }

            ServerMessage Message = new ServerMessage(34);
            Message.AppendInt32(Users.Count);

            foreach (RoomUser User in Users)
            {
                User.SerializeStatus(Message);
            }

            return Message;
        }

        #region Room Bans
        public Boolean UserIsBanned(uint Id)
        {
            return Bans.ContainsKey(Id);
        }

        public void RemoveBan(uint Id)
        {
            Bans.Remove(Id);
        }

        public void AddBan(uint Id)
        {
            Bans.Add(Id, UberEnvironment.GetUnixTimestamp());
        }

        public Boolean HasBanExpired(uint Id)
        {
            if (!UserIsBanned(Id))
            {
                return true;
            }

            Double diff = UberEnvironment.GetUnixTimestamp() - Bans[Id];

            if (diff > 900)
            {
                return true;
            }

            return false;
        }
        #endregion

        public int ItemCountByType(String InteractionType)
        {
            int i = 0;

            lock (Items)
            {
                foreach (RoomItem Item in Items)
                {
                    if (Item.GetBaseItem().InteractionType.ToLower() == InteractionType.ToLower())
                    {
                        i++;
                    }
                }
            }

            return i;
        }

        #region Trading
        public bool HasActiveTrade(RoomUser User)
        {
            if (User.IsBot)
            {
                return false;
            }

            return HasActiveTrade(User.GetClient().GetHabbo().Id);
        }

        public bool HasActiveTrade(uint UserId)
        {
            lock (ActiveTrades)
            {
                foreach (Trade Trade in ActiveTrades)
                {
                    if (Trade.ContainsUser(UserId))
                    {
                        return true;
                    }
                }
            }

            return false;
        }

        public Trade GetUserTrade(RoomUser User)
        {
            if (User.IsBot)
            {
                return null;
            }

            return GetUserTrade(User.GetClient().GetHabbo().Id);
        }

        public Trade GetUserTrade(uint UserId)
        {
            lock (ActiveTrades)
            {
                foreach (Trade Trade in ActiveTrades)
                {
                    if (Trade.ContainsUser(UserId))
                    {
                        return Trade;
                    }
                }
            }

            return null;
        }

        public void TryStartTrade(RoomUser UserOne, RoomUser UserTwo)
        {
            if (UserOne == null || UserTwo == null || UserOne.IsBot || UserTwo.IsBot || UserOne.IsTrading || UserTwo.IsTrading || HasActiveTrade(UserOne) || HasActiveTrade(UserTwo))
            {
                return;
            }

            ActiveTrades.Add(new Trade(UserOne.GetClient().GetHabbo().Id, UserTwo.GetClient().GetHabbo().Id, RoomId));
        }

        public void TryStopTrade(uint UserId)
        {
            Trade Trade = GetUserTrade(UserId);

            if (Trade == null)
            {
                return;
            }

            Trade.CloseTrade(UserId);
            ActiveTrades.Remove(Trade);
        }
        #endregion

        #region Furni handling and stacking
        public bool SetFloorItem(GameClient Session, RoomItem Item, int newX, int newY, int newRot, bool newItem)
        {
            // Find affected tiles
            Dictionary<int, AffectedTile> AffectedTiles = GetAffectedTiles(Item.GetBaseItem().Length, Item.GetBaseItem().Width, newX, newY, newRot);

            // Verify tiles are valid
            if (!ValidTile(newX, newY))
            {
                return false;
            }

            foreach (AffectedTile Tile in AffectedTiles.Values)
            {
                if (!ValidTile(Tile.X, Tile.Y))
                {
                    return false;
                }
            }

            // Start calculating new Z coordinate
            Double newZ = Model.SqFloorHeight[newX, newY];

            // Is the item trying to stack on itself!?
            if (Item.Rot == newRot && Item.X == newX && Item.Y == newY && Item.Z != newZ)
            {
                return false;
            }

            // Make sure this tile is open and there are no users here
            if (Model.SqState[newX, newY] != SquareState.OPEN)
            {
                return false;
            }

            foreach (AffectedTile Tile in AffectedTiles.Values)
            {
                if (Model.SqState[Tile.X, Tile.Y] != SquareState.OPEN)
                {
                    return false;
                }
            }

            // And that we have no users
            if (!Item.GetBaseItem().IsSeat)
            {
                if (SquareHasUsers(newX, newY))
                {
                    return false;
                }

                foreach (AffectedTile Tile in AffectedTiles.Values)
                {
                    if (SquareHasUsers(Tile.X, Tile.Y))
                    {
                        return false;
                    }
                }
            }

            // Find affected objects
            List<RoomItem> ItemsOnTile = GetFurniObjects(newX, newY);
            List<RoomItem> ItemsAffected = new List<RoomItem>();
            List<RoomItem> ItemsComplete = new List<RoomItem>();

            foreach (AffectedTile Tile in AffectedTiles.Values)
            {
                List<RoomItem> Temp = GetFurniObjects(Tile.X, Tile.Y);

                if (Temp != null)
                {
                    ItemsAffected.AddRange(Temp);
                }
            }

            if (ItemsOnTile == null) ItemsOnTile = new List<RoomItem>();

            ItemsComplete.AddRange(ItemsOnTile);
            ItemsComplete.AddRange(ItemsAffected);

            // Check for items in the stack that do not allow stacking on top of them
            foreach (RoomItem I in ItemsComplete)
            {
                if (I.Id == Item.Id)
                {
                    continue;
                }

                if (!I.GetBaseItem().Stackable)
                {
                    return false;
                }
            }

            // If this is a rotating action, maintain item at current height
            if (Item.Rot != newRot && Item.X == newX && Item.Y == newY)
            {
                newZ = Item.Z;
            }

            // Are there any higher objects in the stack!?
            foreach (RoomItem I in ItemsComplete)
            {
                if (I.Id == Item.Id)
                {
                    continue; // cannot stack on self
                }

                if (I.TotalHeight > newZ)
                {
                    newZ = I.TotalHeight;
                }
            }

            // Verify the rotation is correct
            if (newRot != 0 && newRot != 2 && newRot != 4 && newRot != 6 && newRot != 8)
            {
                newRot = 0;
            }

            Item.X = newX;
            Item.Y = newY;
            Item.Z = newZ;
            Item.Rot = newRot;

            Item.Interactor.OnPlace(Session, Item);

            if (newItem)
            {
                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.AddParamWithValue("extra_data", Item.ExtraData);
                    dbClient.ExecuteQuery("INSERT INTO room_items (id,room_id,base_item,extra_data,x,y,z,rot,wall_pos) VALUES ('" + Item.Id + "','" + RoomId + "','" + Item.BaseItem + "',@extra_data,'" + Item.X + "','" + Item.Y + "','" + Item.Z + "','" + Item.Rot + "','')");
                }

                Items.Add(Item);

                ServerMessage Message = new ServerMessage(93);
                Item.Serialize(Message);
                SendMessage(Message);
            }
            else
            {
                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE room_items SET x = '" + Item.X + "', y = '" + Item.Y + "', z = '" + Item.Z + "', rot = '" + Item.Rot + "', wall_pos = '' WHERE id = '" + Item.Id + "' LIMIT 1");
                }

                ServerMessage Message = new ServerMessage(95);
                Item.Serialize(Message);
                SendMessage(Message);
            }

            GenerateMaps();
            UpdateUserStatusses();

            return true;
        }

        public bool SetWallItem(GameClient Session, RoomItem Item)
        {
            Item.Interactor.OnPlace(Session, Item);

            switch (Item.GetBaseItem().InteractionType.ToLower())
            {
                case "dimmer":

                    if (MoodlightData == null)
                    {
                        MoodlightData = new MoodlightData(Item.Id);
                        Item.ExtraData = MoodlightData.GenerateExtraData();
                    }

                    break;
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("extra_data", Item.ExtraData);
                dbClient.ExecuteQuery("INSERT INTO room_items (id,room_id,base_item,extra_data,x,y,z,rot,wall_pos) VALUES ('" + Item.Id + "','" + RoomId + "','" + Item.BaseItem + "',@extra_data,'0','0','0','0','" + Item.WallPos + "')");
            }

            Items.Add(Item);

            ServerMessage Message = new ServerMessage(83);
            Item.Serialize(Message);
            SendMessage(Message);

            return true;
        }
        #endregion

        public void UpdateUserStatusses()
        {
            lock (this.UserList)
            {
                List<RoomUser>.Enumerator Users = UserList.GetEnumerator();

                while (Users.MoveNext())
                {
                    UpdateUserStatus(Users.Current);
                }
            }
        }

        public double SqAbsoluteHeight(int X, int Y)
        {
            List <RoomItem> ItemsOnSquare = GetFurniObjects(X, Y);
            double HighestStack = 0;

            bool deduct = false;
            double deductable = 0.0;

            if (ItemsOnSquare == null)
            {
                ItemsOnSquare = new List<RoomItem>();
            }

            if (ItemsOnSquare != null)
            {
                foreach (RoomItem Item in ItemsOnSquare)
                {
                    if (Item.TotalHeight > HighestStack)
                    {
                        if (Item.GetBaseItem().IsSeat || Item.GetBaseItem().InteractionType.ToLower() == "bed")
                        {
                            deduct = true;
                            deductable = Item.GetBaseItem().Height;
                        }
                        else
                        {
                            deduct = false;
                        }

                        HighestStack = Item.TotalHeight;
                    }
                }
            }

            double floorHeight = Model.SqFloorHeight[X, Y];
            double stackHeight = HighestStack - Model.SqFloorHeight[X, Y];

            if (deduct)
            {
                stackHeight -= deductable;
            }

            if (stackHeight < 0)
            {
                stackHeight = 0;
            }

            return (floorHeight + stackHeight);
        }

        public void UpdateUserStatus(RoomUser User)
        {
            if (User.Statusses.ContainsKey("lay") || User.Statusses.ContainsKey("sit"))
            {
                User.Statusses.Remove("lay");
                User.Statusses.Remove("sit");
                User.UpdateNeeded = true;
            }

            double newZ = SqAbsoluteHeight(User.X, User.Y);

            if (newZ != User.Z)
            {
                User.Z = newZ;
                User.UpdateNeeded = true;
            }

            if (Model.SqState[User.X, User.Y] == SquareState.SEAT)
            {
                if (!User.Statusses.ContainsKey("sit"))
                {
                    User.Statusses.Add("sit", "1.0");
                }

                User.Z = Model.SqFloorHeight[User.X, User.Y];
                User.RotHead = Model.SqSeatRot[User.X, User.Y];
                User.RotBody = Model.SqSeatRot[User.X, User.Y];

                User.UpdateNeeded = true;
            }

            List<RoomItem> ItemsOnSquare = GetFurniObjects(User.X, User.Y);

            if (ItemsOnSquare == null)
            {
                ItemsOnSquare = new List<RoomItem>();
            }

            foreach (RoomItem Item in ItemsOnSquare)
            {
                if (Item.GetBaseItem().IsSeat)
                {
                    if (!User.Statusses.ContainsKey("sit"))
                    {
                        User.Statusses.Add("sit", Item.GetBaseItem().Height.ToString().Replace(',', '.'));
                    }

                    User.Z = Item.Z;
                    User.RotHead = Item.Rot;
                    User.RotBody = Item.Rot;

                    User.UpdateNeeded = true;
                }

                if (Item.GetBaseItem().InteractionType.ToLower() == "bed")
                {
                    if (!User.Statusses.ContainsKey("lay"))
                    {
                        User.Statusses.Add("lay", Item.GetBaseItem().Height.ToString().Replace(',', '.') + " null");
                    }

                    User.Z = Item.Z;
                    User.RotHead = Item.Rot;
                    User.RotBody = Item.Rot;

                    User.UpdateNeeded = true;
                }
            }
        }

        public bool ValidTile(int X, int Y)
        {
            if (X < 0 || Y < 0 || X >= Model.MapSizeX || Y >= Model.MapSizeY)
            {
                return false;
            }

            return true;
        }

        public void TurnHeads(int X, int Y, uint SenderId)
        {
            lock (UserList)
            {
                foreach (RoomUser User in UserList)
                {
                    if (User.HabboId == SenderId)
                    {
                        continue;
                    }

                    User.SetRot(Rotation.Calculate(User.X, User.Y, X, Y), true);
                }
            }
        }

        public List<RoomItem> GetFurniObjects(int X, int Y)
        {
            List<RoomItem> Results = new List<RoomItem>();

            foreach (RoomItem Item in FloorItems)
            {
                if (Item.X == X && Item.Y == Y)
                {
                    Results.Add(Item);
                }

                Dictionary<int, AffectedTile> PointList = GetAffectedTiles(Item.GetBaseItem().Length, Item.GetBaseItem().Width, Item.X, Item.Y, Item.Rot);

                foreach (AffectedTile Tile in PointList.Values)
                {
                    if (Tile.X == X && Tile.Y == Y)
                    {
                        Results.Add(Item);
                    }
                }
            }

            if (Results.Count > 0)
            {
                return Results;
            }

            return null;
        }

        public RoomItem FindItem(uint Id)
        {
            lock (Items)
            {
                foreach (RoomItem Item in Items)
                {
                    if (Item.Id == Id)
                    {
                        return Item;
                    }
                }
            }

            return null;
        }

        public Dictionary<int, AffectedTile> GetAffectedTiles(int Length, int Width, int PosX, int PosY, int Rotation)
        {
            int x = 0;

            Dictionary<int, AffectedTile> PointList = new Dictionary<int, AffectedTile>();

            if (Length > 1)
            {
                if (Rotation == 0 || Rotation == 4)
                {
                    for (int i = 1; i < Length; i++)
                    {
                        PointList.Add(x++, new AffectedTile(PosX, PosY + i, i));

                        for (int j = 1; j < Width; j++)
                        {
                            PointList.Add(x++, new AffectedTile(PosX + j, PosY + i, (i < j) ? j : i));
                        }
                    }
                }
                else if (Rotation == 2 || Rotation == 6)
                {
                    for (int i = 1; i < Length; i++)
                    {
                        PointList.Add(x++, new AffectedTile(PosX + i, PosY, i));

                        for (int j = 1; j < Width; j++)
                        {
                            PointList.Add(x++, new AffectedTile(PosX + i, PosY + j, (i < j) ? j : i));
                        }
                    }
                }
            }

            if (Width > 1)
            {
                if (Rotation == 0 || Rotation == 4)
                {
                    for (int i = 1; i < Width; i++)
                    {
                        PointList.Add(x++, new AffectedTile(PosX + i, PosY, i));

                        for (int j = 1; j < Length; j++)
                        {
                            PointList.Add(x++, new AffectedTile(PosX + i, PosY + j, (i < j) ? j : i));
                        }
                    }
                }
                else if (Rotation == 2 || Rotation == 6)
                {
                    for (int i = 1; i < Width; i++)
                    {
                        PointList.Add(x++, new AffectedTile(PosX, PosY + i, i));

                        for (int j = 1; j < Length; j++)
                        {
                            PointList.Add(x++, new AffectedTile(PosX + j, PosY + i, (i < j) ? j : i));
                        }
                    }
                }
            }

            return PointList;
        }

        public bool SquareHasUsers(int X, int Y, bool LastStep)
        {
            if (AllowWalkthrough && !LastStep)
            {
                return false;
            }

            return SquareHasUsers(X, Y);
        }

        public bool SquareHasUsers(int X, int Y)
        {
            Coord Coord = BedMatrix[X, Y];
            return UserMatrix[Coord.x, Coord.y];
        }

        //This function is based on the one from "Holograph Emulator"
        public string WallPositionCheck(string wallPosition)
        {
            //:w=3,2 l=9,63 l
            try
            {
                if (wallPosition.Contains(Convert.ToChar(13)))
                { return null; }
                if (wallPosition.Contains(Convert.ToChar(9)))
                { return null; }

                string[] posD = wallPosition.Split(' ');
                if (posD[2] != "l" && posD[2] != "r")
                    return null;

                string[] widD = posD[0].Substring(3).Split(',');
                int widthX = int.Parse(widD[0]);
                int widthY = int.Parse(widD[1]);
                if (widthX < 0 || widthY < 0 || widthX > 200 || widthY > 200)
                    return null;

                string[] lenD = posD[1].Substring(2).Split(',');
                int lengthX = int.Parse(lenD[0]);
                int lengthY = int.Parse(lenD[1]);
                if (lengthX < 0 || lengthY < 0 || lengthX > 200 || lengthY > 200)
                    return null;

                return ":w=" + widthX + "," + widthY + " " + "l=" + lengthX + "," + lengthY + " " + posD[2];
            }
            catch
            {
                return null;
            }
        }

        public bool TilesTouching(int X1, int Y1, int X2, int Y2)
        {
            if (!(Math.Abs(X1 - X2) > 1 || Math.Abs(Y1 - Y2) > 1)) return true;
            if (X1 == X2 && Y1 == Y2) return true;
            return false;
        }

        public int TileDistance(int X1, int Y1, int X2, int Y2)
        {
            return Math.Abs(X1 - X2) + Math.Abs(Y1 - Y2);
        }
    }

    public class AffectedTile
    {
        int mX;
        int mY;
        int mI;

        public AffectedTile(int x, int y, int i)
        {
            mX = x;
            mY = y;
            mI = i;
        }

        public int X
        {
            get
            {
                return mX;
            }
        }

        public int Y
        {
            get
            {
                return mY;
            }
        }

        public int I
        {
            get
            {
                return mI;
            }
        }
    }
}
