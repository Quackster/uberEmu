using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.HabboHotel.GameClients;
using Uber.HabboHotel.Items;
using Uber.Messages;

namespace Uber.HabboHotel.Rooms
{
    class Trade
    {
        private ConcurrentDictionary<uint, TradeUser> Users;
        private int TradeStage;
        private uint RoomId;

        private uint oneId;
        private uint twoId;

        public Trade(uint UserOneId, uint UserTwoId, uint RoomId)
        {
            this.oneId = UserOneId;
            this.twoId = UserTwoId;

            this.Users = new ConcurrentDictionary<uint, TradeUser>();
            this.Users.TryAdd(UserOneId, new TradeUser(UserOneId, RoomId));
            this.Users.TryAdd(UserTwoId, new TradeUser(UserTwoId, RoomId));
            this.TradeStage = 1;
            this.RoomId = RoomId;

            foreach (var kvp in Users)
            {
                TradeUser User = kvp.Value;

                if (!User.GetRoomUser().Statusses.ContainsKey("trd"))
                {
                    User.GetRoomUser().AddStatus("trd", "");
                    User.GetRoomUser().UpdateNeeded = true;
                }
            }

            ServerMessage Message = new ServerMessage(104);
            Message.AppendUInt(UserOneId);
            Message.AppendBoolean(true);
            Message.AppendUInt(UserTwoId);
            Message.AppendBoolean(true);
            SendMessageToUsers(Message);
        }

        public bool AllUsersAccepted
        {
            get
            {
                foreach (var kvp in Users)
                {
                    TradeUser User = kvp.Value;

                    if (!User.HasAccepted)
                        {
                            return false;
                        }
                    }

                return true;
            }
        }

        public bool ContainsUser(uint Id)
        {
            foreach (var kvp in Users)
            {
                TradeUser User = kvp.Value;

                if (User.UserId == Id)
                    {
                        return true;
                    }
                }

            return false;
        }

        public TradeUser GetTradeUser(uint Id)
        {
            foreach (var kvp in Users)
            {
                TradeUser User = kvp.Value;

                if (User.UserId == Id)
                    {
                        return User;
                    }
                }

            return null;
        }

        public void OfferItem(uint UserId, UserItem Item)
        {
            TradeUser User = GetTradeUser(UserId);

            if (User == null || Item == null || !Item.GetBaseItem().AllowTrade || User.HasAccepted || TradeStage != 1)
            {
                return;
            }

            ClearAccepted();

            User.OfferedItems.TryAdd(Item.Id, Item);
            UpdateTradeWindow();
        }

        public void TakeBackItem(uint UserId, UserItem Item)
        {
            TradeUser User = GetTradeUser(UserId);

            if (User == null || Item == null || User.HasAccepted || TradeStage != 1)
            {
                return;
            }

            ClearAccepted();

            User.OfferedItems.TryRemove(Item.Id, out var _);
            UpdateTradeWindow();
        }

        public void Accept(uint UserId)
        {
            TradeUser User = GetTradeUser(UserId);

            if (User == null || TradeStage != 1)
            {
                return;
            }

            User.HasAccepted = true;

            ServerMessage Message = new ServerMessage(109);
            Message.AppendUInt(UserId);
            Message.AppendBoolean(true);
            SendMessageToUsers(Message);

            if (AllUsersAccepted)
            {
                SendMessageToUsers(new ServerMessage(111));
                TradeStage++;
                ClearAccepted();
            }
        }

        public void Unaccept(uint UserId)
        {
            TradeUser User = GetTradeUser(UserId);

            if (User == null || TradeStage != 1 || AllUsersAccepted)
            {
                return;
            }

            User.HasAccepted = false;

            ServerMessage Message = new ServerMessage(109);
            Message.AppendUInt(UserId);
            Message.AppendBoolean(false);
            SendMessageToUsers(Message);
        }

        public void CompleteTrade(uint UserId)
        {
            TradeUser User = GetTradeUser(UserId);

            if (User == null || TradeStage != 2)
            {
                return;
            }

            User.HasAccepted = true;

            ServerMessage Message = new ServerMessage(109);
            Message.AppendUInt(UserId);
            Message.AppendBoolean(true);
            SendMessageToUsers(Message);

            if (AllUsersAccepted)
            {
                TradeStage = 999;
                DeliverItems();
                CloseTradeClean();
            }
        }

        public void ClearAccepted()
        {
            foreach (var kvp in Users)
            {
                TradeUser User = kvp.Value;

                User.HasAccepted = false;
            }
        }

        public void UpdateTradeWindow()
        {
            ServerMessage Message = new ServerMessage(108);

            foreach (var kvp in Users)
            {
                TradeUser User = kvp.Value;

                Message.AppendUInt(User.UserId);
                Message.AppendInt32(User.OfferedItems.Count);

                foreach (var offeredItems in User.OfferedItems)
                {
                    UserItem Item = offeredItems.Value;

                    Message.AppendUInt(Item.Id);
                    Message.AppendStringWithBreak(Item.GetBaseItem().Type.ToLower());
                    Message.AppendUInt(Item.Id);
                    Message.AppendInt32(Item.GetBaseItem().SpriteId);
                    Message.AppendBoolean(true);
                    Message.AppendBoolean(true);
                    Message.AppendStringWithBreak("");
                    Message.AppendBoolean(false); // xmas 09 furni had a special furni tag here, with wired day (wat?)
                    Message.AppendBoolean(false); // xmas 09 furni had a special furni tag here, wired month (wat?)
                    Message.AppendBoolean(false); // xmas 09 furni had a special furni tag here, wired year (wat?)

                    if (Item.GetBaseItem().Type.ToLower() == "s")
                    {
                        Message.AppendInt32(-1);
                    }
                }
            }

            SendMessageToUsers(Message);
        }

        public void DeliverItems()
        {
            // List items
            ConcurrentDictionary<uint, UserItem> ItemsOne = GetTradeUser(oneId).OfferedItems;
            ConcurrentDictionary<uint, UserItem> ItemsTwo = GetTradeUser(twoId).OfferedItems;

            // Verify they are still in user inventory
            foreach (var kvp in ItemsOne)
            {
                UserItem I = kvp.Value;

                if (GetTradeUser(oneId).GetClient().GetHabbo().GetInventoryComponent().GetItem(I.Id) == null)
                {
                    GetTradeUser(oneId).GetClient().SendNotif("Trade failed.");
                    GetTradeUser(twoId).GetClient().SendNotif("Trade failed.");

                    return;
                }
            }

            foreach (var kvp in ItemsTwo)
            {
                UserItem I = kvp.Value;

                if (GetTradeUser(twoId).GetClient().GetHabbo().GetInventoryComponent().GetItem(I.Id) == null)
                {
                    GetTradeUser(oneId).GetClient().SendNotif("Trade failed.");
                    GetTradeUser(twoId).GetClient().SendNotif("Trade failed.");

                    return;
                }
            }

            // Deliver them
            foreach (var kvp in ItemsOne)
            {
                UserItem I = kvp.Value;

                GetTradeUser(oneId).GetClient().GetHabbo().GetInventoryComponent().RemoveItem(I.Id);
                GetTradeUser(twoId).GetClient().GetHabbo().GetInventoryComponent().AddItem(I.Id, I.BaseItem, I.ExtraData);
            }

            foreach (var kvp in ItemsTwo)
            {
                UserItem I = kvp.Value;

                GetTradeUser(twoId).GetClient().GetHabbo().GetInventoryComponent().RemoveItem(I.Id);
                GetTradeUser(oneId).GetClient().GetHabbo().GetInventoryComponent().AddItem(I.Id, I.BaseItem, I.ExtraData);
            }

            // Update inventories
            GetTradeUser(oneId).GetClient().GetHabbo().GetInventoryComponent().UpdateItems(false);
            GetTradeUser(twoId).GetClient().GetHabbo().GetInventoryComponent().UpdateItems(false);
        }

        public void CloseTradeClean()
        {
            foreach (var kvp in Users)
            {
                TradeUser User = kvp.Value;

                User.GetRoomUser().RemoveStatus("trd");
                    User.GetRoomUser().UpdateNeeded = true;
                }

            SendMessageToUsers(new ServerMessage(112));
            GetRoom().ActiveTrades.Remove(this);
        }

        public void CloseTrade(uint UserId)
        {
            foreach (var kvp in Users)
            {
                TradeUser User = kvp.Value;

                if (User.GetRoomUser() == null)
                    {
                        continue;
                    }

                    User.GetRoomUser().RemoveStatus("trd");
                    User.GetRoomUser().UpdateNeeded = true;
                }

            ServerMessage Message = new ServerMessage(110);
            Message.AppendUInt(UserId);
            SendMessageToUsers(Message);
        }

        public void SendMessageToUsers(ServerMessage Message)
        {
            foreach (var kvp in Users)
            {
                TradeUser User = kvp.Value;

                User.GetClient().SendMessage(Message);
            }
        }

        private Room GetRoom()
        {
            return UberEnvironment.GetGame().GetRoomManager().GetRoom(RoomId);
        }
    }

    class TradeUser
    {
        public uint UserId;
        private uint RoomId;
        private bool Accepted;

        public ConcurrentDictionary<uint, UserItem> OfferedItems;

        public bool HasAccepted
        {
            get
            {
                return Accepted;
            }

            set
            {
                Accepted = value;
            }
        }

        public TradeUser(uint UserId, uint RoomId)
        {
            this.UserId = UserId;
            this.RoomId = RoomId;
            this.Accepted = false;
            this.OfferedItems = new ConcurrentDictionary<uint, UserItem>();
        }

        public RoomUser GetRoomUser()
        {
            Room Room = UberEnvironment.GetGame().GetRoomManager().GetRoom(RoomId);

            if (Room == null)
            {
                return null;
            }
            
            return Room.GetRoomUserByHabbo(UserId);
        }

        public GameClient GetClient()
        {
            return UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);
        }
    }
}
