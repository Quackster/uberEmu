using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.HabboHotel.GameClients;
using Uber.HabboHotel.Pathfinding;
using Uber.HabboHotel.Rooms;

namespace Uber.HabboHotel.Items.Interactors
{
    abstract class FurniInteractor
    {
        public abstract void OnPlace(GameClient Session, RoomItem Item);
        public abstract void OnRemove(GameClient Session, RoomItem Item);
        public abstract void OnTrigger(GameClient Session, RoomItem Item, int Request, Boolean UserHasRights);
    }

    class InteractorStatic : FurniInteractor
    {
        public override void OnPlace(GameClient Session, RoomItem Item) { }
        public override void OnRemove(GameClient Session, RoomItem Item) { }
        public override void OnTrigger(GameClient Session, RoomItem Item, int Request, bool UserHasRights) { }
    }

    class InteractorTeleport : FurniInteractor
    {
        public override void OnPlace(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "0";

            if (Item.InteractingUser != 0)
            {
                RoomUser User = Item.GetRoom().GetRoomUserByHabbo(Item.InteractingUser);

                if (User != null)
                {
                    User.ClearMovement(true);
                    User.AllowOverride = false;
                    User.CanWalk = true;
                }

                Item.InteractingUser = 0;
            }

            if (Item.InteractingUser2 != 0)
            {
                RoomUser User = Item.GetRoom().GetRoomUserByHabbo(Item.InteractingUser2);

                if (User != null)
                {
                    User.ClearMovement(true);
                    User.AllowOverride = false;
                    User.CanWalk = true;
                }

                Item.InteractingUser2 = 0;
            }

            Item.GetRoom().RegenerateUserMatrix();
        }

        public override void OnRemove(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "0";

            if (Item.InteractingUser != 0)
            {
                RoomUser User = Item.GetRoom().GetRoomUserByHabbo(Item.InteractingUser);

                if (User != null)
                {
                    User.UnlockWalking();
                }

                Item.InteractingUser = 0;
            }

            if (Item.InteractingUser2 != 0)
            {
                RoomUser User = Item.GetRoom().GetRoomUserByHabbo(Item.InteractingUser2);

                if (User != null)
                {
                    User.UnlockWalking();
                }

                Item.InteractingUser2 = 0;
            }

            Item.GetRoom().RegenerateUserMatrix();
        }

        public override void OnTrigger(GameClient Session, RoomItem Item, int Request, bool UserHasRights)
        {
            // Is this user valid?
            RoomUser User = Item.GetRoom().GetRoomUserByHabbo(Session.GetHabbo().Id);

            if (User == null)
            {
                return;
            }

            // Alright. But is this user in the right position?
            if (User.Coordinate == Item.Coordinate || User.Coordinate == Item.SquareInFront)
            {
                // Fine. But is this tele even free?
                if (Item.InteractingUser != 0)
                {
                    return;
                }

                User.TeleDelay = -1;
                Item.InteractingUser = User.GetClient().GetHabbo().Id;
            }
            else if (User.CanWalk)
            {
                User.MoveTo(Item.SquareInFront);
            }
        }
    }

    class InteractorSpinningBottle : FurniInteractor
    {
        public override void OnPlace(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "0";
            Item.UpdateState(true, false);
        }

        public override void OnRemove(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "0";
        }

        public override void OnTrigger(GameClient Session, RoomItem Item, int Request, bool UserHasRights)
        {
            if (Item.ExtraData != "-1")
            {
                Item.ExtraData = "-1";
                Item.UpdateState(false, true);
                Item.ReqUpdate(3);
            }
        }
    }

    class InteractorDice : FurniInteractor
    {
        public override void OnPlace(GameClient Session, RoomItem Item) { }
        public override void OnRemove(GameClient Session, RoomItem Item) { }

        public override void OnTrigger(GameClient Session, RoomItem Item, int Request, bool UserHasRights)
        {
            RoomUser User = Item.GetRoom().GetRoomUserByHabbo(Session.GetHabbo().Id);

            if (User == null)
            {
                return;
            }

            if (Item.GetRoom().TilesTouching(Item.X, Item.Y, User.X, User.Y))
            {
                if (Item.ExtraData != "-1")
                {
                    if (Request == -1)
                    {
                        Item.ExtraData = "0";
                        Item.UpdateState();
                    }
                    else
                    {
                        Item.ExtraData = "-1";
                        Item.UpdateState(false, true);
                        Item.ReqUpdate(4);
                    }
                }
            }
            else
            {
                User.MoveTo(Item.SquareInFront);
            }
        }
    }

    class InteractorHabboWheel : FurniInteractor
    {
        public override void OnPlace(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "-1";
            Item.ReqUpdate(10);
        }

        public override void OnRemove(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "-1";
        }

        public override void OnTrigger(GameClient Session, RoomItem Item, int Request, bool UserHasRights)
        {
            if (!UserHasRights)
            {
                return;
            }

            if (Item.ExtraData != "-1")
            {
                Item.ExtraData = "-1";
                Item.UpdateState();
                Item.ReqUpdate(10);
            }
        }
    }

    class InteractorLoveShuffler : FurniInteractor
    {
        public override void OnPlace(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "-1";
        }

        public override void OnRemove(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "-1";
        }

        public override void OnTrigger(GameClient Session, RoomItem Item, int Request, bool UserHasRights)
        {
            if (!UserHasRights)
            {
                return;
            }

            if (Item.ExtraData != "0")
            {
                Item.ExtraData = "0";
                Item.UpdateState(false, true);
                Item.ReqUpdate(10);
            }
        }
    }

    class InteractorOneWayGate : FurniInteractor
    {
        public override void OnPlace(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "0";

            if (Item.InteractingUser != 0)
            {
                RoomUser User = Item.GetRoom().GetRoomUserByHabbo(Item.InteractingUser);

                if (User != null)
                {
                    User.ClearMovement(true);
                    User.UnlockWalking();
                }

                Item.InteractingUser = 0;
            }
        }

        public override void OnRemove(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "0";

            if (Item.InteractingUser != 0)
            {
                RoomUser User = Item.GetRoom().GetRoomUserByHabbo(Item.InteractingUser);

                if (User != null)
                {
                    User.ClearMovement(true);
                    User.UnlockWalking();
                }

                Item.InteractingUser = 0;
            }
        }
        
        public override void OnTrigger(GameClient Session, RoomItem Item, int Request, bool UserHasRights)
        {
            RoomUser User = Item.GetRoom().GetRoomUserByHabbo(Session.GetHabbo().Id);

            if (User == null)
            {
                return;
            }

            if (User.Coordinate != Item.SquareInFront && User.CanWalk)
            {
                User.MoveTo(Item.SquareInFront);
                return;
            }

            if (!Item.GetRoom().CanWalk(Item.SquareBehind.x, Item.SquareBehind.y, Item.Z, true))
            {
                return;
            }

            if (Item.InteractingUser == 0)
            {
                Item.InteractingUser = User.HabboId;

                User.CanWalk = false;

                if (User.IsWalking && (User.GoalX != Item.SquareInFront.x || User.GoalY != Item.SquareInFront.y))
                {
                    User.ClearMovement(true);
                }

                User.AllowOverride = true;
                User.MoveTo(Item.Coordinate);

                Item.ReqUpdate(3);
            }
        }
    }

    class InteractorAlert : FurniInteractor
    {
        public override void OnPlace(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "0";
        }

        public override void OnRemove(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "0";
        }

        public override void OnTrigger(GameClient Session, RoomItem Item, int Request, bool UserHasRights)
        {
            if (!UserHasRights)
            {
                return;
            }

            if (Item.ExtraData == "0")
            {
                Item.ExtraData = "1";
                Item.UpdateState(false, true);
                Item.ReqUpdate(4);
            }
        }
    }

    class InteractorVendor : FurniInteractor
    {
        public override void OnPlace(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "0";

            if (Item.InteractingUser > 0)
            {
                RoomUser User = Item.GetRoom().GetRoomUserByHabbo(Item.InteractingUser);

                if (User != null)
                {
                    User.CanWalk = true;
                }
            }
        }
       
        public override void OnRemove(GameClient Session, RoomItem Item)
        {
            Item.ExtraData = "0";

            if (Item.InteractingUser > 0)
            {
                RoomUser User = Item.GetRoom().GetRoomUserByHabbo(Item.InteractingUser);

                if (User != null)
                {
                    User.CanWalk = true;
                }
            }
        }

        public override void OnTrigger(GameClient Session, RoomItem Item, int Request, bool UserHasRights)
        {
            if (Item.ExtraData != "1" && Item.GetBaseItem().VendingIds.Count >= 1 && Item.InteractingUser == 0)
            {
                RoomUser User = Item.GetRoom().GetRoomUserByHabbo(Session.GetHabbo().Id);

                if (User == null)
                {
                    return;
                }

                if (!Item.GetRoom().TilesTouching(User.X, User.Y, Item.X, Item.Y))
                {
                    User.MoveTo(Item.SquareInFront);
                    return;
                }

                Item.InteractingUser = Session.GetHabbo().Id;

                User.CanWalk = false;
                User.ClearMovement(true);
                User.SetRot(Rotation.Calculate(User.X, User.Y, Item.X, Item.Y));

                Item.ReqUpdate(2);

                Item.ExtraData = "1";
                Item.UpdateState(false, true);
            }
        }
    }

    class InteractorGenericSwitch : FurniInteractor
    {
        int Modes;

        public InteractorGenericSwitch(int Modes)
        {
            this.Modes = (Modes - 1);

            if (this.Modes < 0)
            {
                this.Modes = 0;
            }
        }

        public override void OnPlace(GameClient Session, RoomItem Item) { }
        public override void OnRemove(GameClient Session, RoomItem Item) { }

        public override void OnTrigger(GameClient Session, RoomItem Item, int Request, bool UserHasRights)
        {
            if (!UserHasRights)
            {
                return;
            }

            if (this.Modes == 0)
            {
                return;
            }

            int currentMode = 0;
            int newMode = 0;

            try
            {
                currentMode = int.Parse(Item.ExtraData);
            }
            catch (Exception) { }

            if (currentMode <= 0)
            {
                newMode = 1;
            }
            else if (currentMode >= Modes)
            {
                newMode = 0;
            }
            else
            {
                newMode = currentMode + 1;
            }

            Item.ExtraData = newMode.ToString();
            Item.UpdateState();
        }
    }

    class InteractorGate : FurniInteractor
    {
        int Modes;

        public InteractorGate(int Modes)
        {
            this.Modes = (Modes - 1);

            if (this.Modes < 0)
            {
                this.Modes = 0;
            }
        }

        public override void OnPlace(GameClient Session, RoomItem Item) { }
        public override void OnRemove(GameClient Session, RoomItem Item) { }

        public override void OnTrigger(GameClient Session, RoomItem Item, int Request, bool UserHasRights)
        {
            if (!UserHasRights)
            {
                return;
            }

            if (this.Modes == 0)
            {
                Item.UpdateState(false, true);
            }

            int currentMode = 0;
            int newMode = 0;

            try
            {
                currentMode = int.Parse(Item.ExtraData);
            }
            catch (Exception) { }

            if (currentMode <= 0)
            {
                newMode = 1;
            }
            else if (currentMode >= Modes)
            {
                newMode = 0;
            }
            else
            {
                newMode = currentMode + 1;
            }

            if (newMode == 0)
            {
                if (Item.GetRoom().SquareHasUsers(Item.X, Item.Y))
                {
                    return;
                }

                Dictionary<int, Rooms.AffectedTile> Points = Item.GetRoom().GetAffectedTiles(Item.GetBaseItem().Length, Item.GetBaseItem().Width,
                    Item.X, Item.Y, Item.Rot);

                if (Points == null)
                {
                    Points = new Dictionary<int, Rooms.AffectedTile>();
                }

                foreach (Rooms.AffectedTile Tile in Points.Values)
                {
                    if (Item.GetRoom().SquareHasUsers(Tile.X, Tile.Y))
                    {
                        return;
                    }
                }
            }

            Item.ExtraData = newMode.ToString();
            Item.UpdateState();
            Item.GetRoom().GenerateMaps();
        }
    }

    class InteractorScoreboard : FurniInteractor
    {
        public override void OnPlace(GameClient Session, RoomItem Item) { }
        public override void OnRemove(GameClient Session, RoomItem Item) { }

        public override void OnTrigger(GameClient Session, RoomItem Item, int Request, bool UserHasRights)
        {
            if (!UserHasRights)
            {
                return;
            }

            int NewMode = 0;

            try
            {
                NewMode = int.Parse(Item.ExtraData);
            }
            catch (Exception) { }

            if (Request == 0)
            {
                if (NewMode <= -1)
                {
                    NewMode = 0;
                }
                else if (NewMode >= 0)
                {
                    NewMode = -1;
                }
            }
            else if (Request >= 1)
            {
                if (Request == 1)
                {
                    NewMode--;

                    if (NewMode < 0)
                    {
                        NewMode = 0;
                    }
                }
                else if (Request == 2)
                {
                    NewMode++;

                    if (NewMode >= 100)
                    {
                        NewMode = 0;
                    }
                }
            }

            Item.ExtraData = NewMode.ToString();
            Item.UpdateState();
        }
    }
}
