using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.HabboHotel.Pathfinding;
using Uber.HabboHotel.Rooms;
using Uber.HabboHotel.Items.Interactors;
using Uber.Messages;
using Uber.Storage;

namespace Uber.HabboHotel.Items
{
    class RoomItem
    {
        public uint Id;
        public uint RoomId;
        public uint BaseItem;
        public string ExtraData;

        public int X;
        public int Y;
        public Double Z;
        public int Rot;

        public string WallPos;

        public bool UpdateNeeded;
        public int UpdateCounter;

        public uint InteractingUser;
        public uint InteractingUser2;

        public Coord Coordinate
        {
            get
            {
                return new Coord(X, Y);
            }
        }

        public double TotalHeight
        {
            get
            {
                return Z + GetBaseItem().Height;
            }
        }

        public bool IsWallItem
        {
            get
            {
                if (GetBaseItem().Type.ToLower() == "i")
                {
                    return true;
                }

                return false;
            }
        }

        public bool IsFloorItem
        {
            get
            {
                if (GetBaseItem().Type.ToLower() == "s")
                {
                    return true;
                }

                return false;
            }
        }

        public Coord SquareInFront
        {
            get
            {
                Coord Sq = new Coord(X, Y);

                if (Rot == 0)
                {
                    Sq.y--;
                }
                else if (Rot == 2)
                {
                    Sq.x++;
                }
                else if (Rot == 4)
                {
                    Sq.y++;
                }
                else if (Rot == 6)
                {
                    Sq.x--;
                }

                return Sq;
            }
        }

        public Coord SquareBehind
        {
            get
            {
                Coord Sq = new Coord(X, Y);

                if (Rot == 0)
                {
                    Sq.y++;
                }
                else if (Rot == 2)
                {
                    Sq.x--;
                }
                else if (Rot == 4)
                {
                    Sq.y--;
                }
                else if (Rot == 6)
                {
                    Sq.x++;
                }

                return Sq;
            }
        }

        public FurniInteractor Interactor
        {
            get
            {
                switch (GetBaseItem().InteractionType.ToLower())
                {
                    case "teleport":

                        return new InteractorTeleport();

                    case "bottle":

                        return new InteractorSpinningBottle();

                    case "dice":

                        return new InteractorDice();

                    case "habbowheel":

                        return new InteractorHabboWheel();

                    case "loveshuffler":

                        return new InteractorLoveShuffler();

                    case "onewaygate":

                        return new InteractorOneWayGate();

                    case "alert":

                        return new InteractorAlert();

                    case "vendingmachine":

                        return new InteractorVendor();

                    case "gate":

                        return new InteractorGate(GetBaseItem().Modes);

                    case "scoreboard":

                        return new InteractorScoreboard();

                    case "default":
                    default:

                        return new InteractorGenericSwitch(GetBaseItem().Modes);
                }
            }
        }

        public RoomItem(uint Id, uint RoomId, uint BaseItem, string ExtraData, int X, int Y, Double Z, int Rot, string WallPos)
        {
            this.Id = Id;
            this.RoomId = RoomId;
            this.BaseItem = BaseItem;
            this.ExtraData = ExtraData;
            this.X = X;
            this.Y = Y;
            this.Z = Z;
            this.Rot = Rot;
            this.WallPos = WallPos;
            this.UpdateNeeded = false;
            this.UpdateCounter = 0;
            this.InteractingUser = 0;
            this.InteractingUser2 = 0;

            switch (GetBaseItem().InteractionType.ToLower())
            {
                case "teleport":

                    ReqUpdate(0);

                    break;
            }
        }

        public void ProcessUpdates()
        {
            this.UpdateCounter--;

            if (this.UpdateCounter <= 0)
            {
                this.UpdateNeeded = false;
                this.UpdateCounter = 0;

                RoomUser User = null;
                RoomUser User2 = null;

                switch (GetBaseItem().InteractionType.ToLower())
                {
                    case "onewaygate":

                        User = null;

                        if (InteractingUser > 0)
                        {
                            User = GetRoom().GetRoomUserByHabbo(InteractingUser);
                        }

                        if (User != null && User.X == X && User.Y == Y)
                        {
                            ExtraData = "1";                         

                            User.MoveTo(SquareBehind);

                            ReqUpdate(0);
                            UpdateState(false, true);
                        }
                        else if (User != null && User.Coordinate == SquareBehind)
                        {
                            User.UnlockWalking();

                            ExtraData = "0";
                            InteractingUser = 0;

                            UpdateState(false, true);
                        }
                        else if (ExtraData == "1")
                        {
                            ExtraData = "0";
                            UpdateState(false, true);
                        }

                        if (User == null)
                        {
                            InteractingUser = 0;
                        }

                        break;

                    case "teleport":

                        User = null;
                        User2 = null;

                        bool keepDoorOpen = false;
                        bool showTeleEffect = false;

                        // Do we have a primary user that wants to go somewhere?
                        if (InteractingUser > 0)
                        {
                            User = GetRoom().GetRoomUserByHabbo(InteractingUser);

                            // Is this user okay?
                            if (User != null)
                            {
                                // Is he in the tele?
                                if (User.Coordinate == Coordinate)
                                {
                                    if (User.TeleDelay == -1)
                                    {
                                        User.TeleDelay = 1;
                                    }

                                    if (TeleHandler.IsTeleLinked(Id))
                                    {
                                        showTeleEffect = true;

                                        if (User.TeleDelay == 0)
                                        {
                                            // Woop! No more delay.
                                            uint TeleId = TeleHandler.GetLinkedTele(Id);
                                            uint RoomId = TeleHandler.GetTeleRoomId(TeleId);

                                            // Do we need to tele to the same room or gtf to another?
                                            if (RoomId == this.RoomId)
                                            {
                                                RoomItem Item = GetRoom().GetItem(TeleId);

                                                if (Item == null)
                                                {
                                                    User.UnlockWalking();
                                                }
                                                else
                                                {
                                                    // Set pos
                                                    User.SetPos(Item.X, Item.Y, Item.Z);
                                                    User.SetRot(Item.Rot);

                                                    // Force tele effect update (dirty)
                                                    Item.ExtraData = "2";
                                                    Item.UpdateState(false, true);

                                                    // Set secondary interacting user
                                                    Item.InteractingUser2 = InteractingUser;
                                                }
                                            }
                                            else
                                            {
                                                // Let's run the teleport delegate to take futher care of this..
                                                UberEnvironment.GetGame().GetRoomManager().AddTeleAction(new TeleUserData(User, RoomId, TeleId));
                                            }

                                            // We're done with this tele. We have another one to bother.
                                            InteractingUser = 0;
                                        }
                                        else
                                        {
                                            // We're linked, but there's a delay, so decrease the delay and wait it out.
                                            User.TeleDelay--;
                                        }
                                    }
                                    else
                                    {
                                        // This tele is not linked, so let's gtfo.
                                        User.UnlockWalking();
                                        InteractingUser = 0;
                                    }
                                }
                                // Is he in front of the tele?
                                else if (User.Coordinate == SquareInFront)
                                {
                                    // Open the door
                                    keepDoorOpen = true;

                                    // Lock his walking. We're taking control over him. Allow overriding so he can get in the tele.
                                    if (User.IsWalking && (User.GoalX != X || User.GoalY != Y))
                                    {
                                        User.ClearMovement(true);
                                    }

                                    User.CanWalk = false;
                                    User.AllowOverride = true;

                                    // Move into the tele
                                    User.MoveTo(Coordinate);
                                }
                                // Not even near, do nothing and move on for the next user.
                                else
                                {
                                    InteractingUser = 0;
                                }
                            }
                            else
                            {
                                // Invalid user, do nothing and move on for the next user. 
                                InteractingUser = 0;
                            }
                        }

                        // Do we have a secondary user that wants to get out of the tele?
                        if (InteractingUser2 > 0)
                        {
                            User2 = GetRoom().GetRoomUserByHabbo(InteractingUser2);

                            // Is this user okay?
                            if (User2 != null)
                            {
                                // If so, open the door, unlock the user's walking, and try to push him out in the right direction. We're done with him!
                                keepDoorOpen = true;
                                User2.UnlockWalking();
                                User2.MoveTo(SquareInFront);
                            }

                            // This is a one time thing, whether the user's valid or not.
                            InteractingUser2 = 0;
                        }

                        // Set the new item state, by priority
                        if (keepDoorOpen)
                        {
                            if (ExtraData != "1")
                            {
                                ExtraData = "1";
                                UpdateState(false, true);
                            }
                        }
                        else if (showTeleEffect)
                        {
                            if (ExtraData != "2")
                            {
                                ExtraData = "2";
                                UpdateState(false, true);
                            }
                        }
                        else
                        {
                            if (ExtraData != "0")
                            {
                                ExtraData = "0";
                                UpdateState(false, true);
                            }
                        }

                        // We're constantly going!
                        ReqUpdate(1);

                        break;

                    case "bottle":

                        ExtraData = UberEnvironment.GetRandomNumber(0, 7).ToString();
                        UpdateState();
                        break;

                    case "dice":

                        ExtraData = UberEnvironment.GetRandomNumber(1, 6).ToString();
                        UpdateState();
                        break;

                    case "habbowheel":

                        ExtraData = UberEnvironment.GetRandomNumber(1, 10).ToString();
                        UpdateState();
                        break;

                    case "loveshuffler":

                        if (ExtraData == "0")
                        {
                            ExtraData = UberEnvironment.GetRandomNumber(1, 4).ToString();
                            ReqUpdate(20);
                        }
                        else if (ExtraData != "-1")
                        {
                            ExtraData = "-1";
                        }

                        UpdateState(false, true);
                        break;

                    case "alert":

                        if (this.ExtraData == "1")
                        {
                            this.ExtraData = "0";
                            this.UpdateState(false, true);
                        }

                        break;

                    case "vendingmachine":

                        if (this.ExtraData == "1")
                        {
                            User = GetRoom().GetRoomUserByHabbo(InteractingUser);

                            if (User != null)
                            {
                                User.UnlockWalking();

                                int randomDrink = GetBaseItem().VendingIds[UberEnvironment.GetRandomNumber(0, (GetBaseItem().VendingIds.Count - 1))];
                                User.CarryItem(randomDrink);
                            }

                            this.InteractingUser = 0;
                            this.ExtraData = "0";

                            UpdateState(false, true);
                        }

                        break;
                }
            }
        }

        public void ReqUpdate(int Cycles)
        {
            this.UpdateCounter = Cycles;
            this.UpdateNeeded = true;
        }

        public void UpdateState()
        {
            UpdateState(true, true);
        }

        public void UpdateState(bool inDb, bool inRoom)
        {
            if (inDb)
            {
                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.AddParamWithValue("extra_data", this.ExtraData);
                    dbClient.ExecuteQuery("UPDATE room_items SET extra_data = @extra_data WHERE id = '" + Id + "' LIMIT 1");
                }
            }

            if (inRoom)
            {
                ServerMessage Message = new ServerMessage();

                if (IsFloorItem)
                {
                    Message.Init(88);
                    Message.AppendStringWithBreak(Id.ToString());
                    Message.AppendStringWithBreak(ExtraData);
                }
                else
                {
                    Message.Init(85);
                    Serialize(Message);
                }

                GetRoom().SendMessage(Message);
            }
        }

        public void Serialize(ServerMessage Message)
        {
            // AU33614959XEP:w=3,11 l=31,83 l0
            // A]r|ebuAQsPAQAJ0.0q|ebuA0MC[!`A

            if (IsFloorItem)
            {
                Message.AppendUInt(Id);
                Message.AppendInt32(GetBaseItem().SpriteId);
                Message.AppendInt32(X);
                Message.AppendInt32(Y);
                Message.AppendInt32(Rot);
                Message.AppendStringWithBreak(Z.ToString().Replace(',', '.'));
                Message.AppendInt32(0);
                Message.AppendStringWithBreak(ExtraData);
                Message.AppendInt32(-1);
            }
            else if (IsWallItem)
            {
                Message.AppendStringWithBreak(Id + "");
                Message.AppendInt32(GetBaseItem().SpriteId);
                Message.AppendStringWithBreak(WallPos);

                switch (GetBaseItem().InteractionType.ToLower())
                {
                    case "postit":

                        Message.AppendStringWithBreak(ExtraData.Split(' ')[0]);
                        break;

                    default:

                        Message.AppendStringWithBreak(ExtraData);
                        break;
                }
            }
        }

        public Item GetBaseItem()
        {
            return UberEnvironment.GetGame().GetItemManager().GetItem(BaseItem);
        }

        public Room GetRoom()
        {
            return UberEnvironment.GetGame().GetRoomManager().GetRoom(RoomId);
        }
    }
}
