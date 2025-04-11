using System;
using System.Collections.Generic;
using System.Text;

using Uber.HabboHotel.Items;
using Uber.HabboHotel.GameClients;
using Uber.Messages;
using Uber.HabboHotel.Rooms;

namespace Uber.HabboHotel.Misc
{
    class ChatCommandHandler
    {
        public static Boolean Parse(GameClient Session, string Input)
        {
            string[] Params = Input.Split(' ');

            string TargetUser = null;
            GameClient TargetClient = null;
            Room TargetRoom = null;
            RoomUser TargetRoomUser = null;

            try
            {
                switch (Params[0].ToLower())
                {
                    #region Debugging/Development
                    case "update_inventory":

                        if (Session.GetHabbo().HasFuse("fuse_admin"))
                        {
                            Session.GetHabbo().GetInventoryComponent().UpdateItems(true);
                            return true;
                        }

                        return false;

                    case "update_bots":

                        if (Session.GetHabbo().HasFuse("fuse_admin"))
                        {
                            UberEnvironment.GetGame().GetBotManager().LoadBots();
                            return true;
                        }

                        return false;

                    case "update_catalog":

                        if (Session.GetHabbo().HasFuse("fuse_admin"))
                        {
                            UberEnvironment.GetGame().GetCatalog().Initialize();
                            UberEnvironment.GetGame().GetClientManager().BroadcastMessage(new ServerMessage(441));

                            return true;
                        }

                        return false;

                    case "idletime":

                        if (Session.GetHabbo().HasFuse("fuse_admin"))
                        {
                            TargetRoom = UberEnvironment.GetGame().GetRoomManager().GetRoom(Session.GetHabbo().CurrentRoomId);
                            TargetRoomUser = TargetRoom.GetRoomUserByHabbo(Session.GetHabbo().Id);

                            TargetRoomUser.IdleTime = 600;

                            return true;
                        }

                        return false;

                    case "t":

                        if (Session.GetHabbo().HasFuse("fuse_admin"))
                        {
                            TargetRoom = UberEnvironment.GetGame().GetRoomManager().GetRoom(Session.GetHabbo().CurrentRoomId);

                            if (TargetRoom == null)
                            {
                                return false;
                            }

                            TargetRoomUser = TargetRoom.GetRoomUserByHabbo(Session.GetHabbo().Id);

                            if (TargetRoomUser == null)
                            {
                                return false;
                            }

                            Session.SendNotif("X: " + TargetRoomUser.X + " - Y: " + TargetRoomUser.Y + " - Z: " + TargetRoomUser.Z + " - Rot: " + TargetRoomUser.RotBody);

                            return true;
                        }

                        return false;

                    case "override":

                        if (Session.GetHabbo().HasFuse("fuse_admin"))
                        {
                            TargetRoom = UberEnvironment.GetGame().GetRoomManager().GetRoom(Session.GetHabbo().CurrentRoomId);

                            if (TargetRoom == null)
                            {
                                return false;
                            }

                            TargetRoomUser = TargetRoom.GetRoomUserByHabbo(Session.GetHabbo().Id);

                            if (TargetRoomUser == null)
                            {
                                return false;
                            }

                            if (TargetRoomUser.AllowOverride)
                            {
                                TargetRoomUser.AllowOverride = false;
                                Session.SendNotif("Walking override disabled.");
                            }
                            else
                            {
                                TargetRoomUser.AllowOverride = true;
                                Session.SendNotif("Walking override enabled.");
                            }

                            return true;
                        }

                        return false;

                    case "drink":

                        if (Session.GetHabbo().HasFuse("fuse_admin"))
                        {
                            TargetRoom = UberEnvironment.GetGame().GetRoomManager().GetRoom(Session.GetHabbo().CurrentRoomId);

                            if (TargetRoom == null)
                            {
                                return false;
                            }

                            TargetRoomUser = TargetRoom.GetRoomUserByHabbo(Session.GetHabbo().Id);

                            if (TargetRoomUser == null)
                            {
                                return false;
                            }

                            try
                            {
                                TargetRoomUser.CarryItem(int.Parse(Params[1]));
                            }
                            catch (Exception) { }

                            return true;
                        }

                        return false;

                    case "update_defs":

                        if (Session.GetHabbo().HasFuse("fuse_admin"))
                        {
                            UberEnvironment.GetGame().GetItemManager().LoadItems();
                            Session.SendNotif("Item defenitions reloaded successfully.");
                            return true;
                        }

                        return false;
                    #endregion

                    #region General Commands
                    case "pickall":

                        TargetRoom = UberEnvironment.GetGame().GetRoomManager().GetRoom(Session.GetHabbo().CurrentRoomId);

                        if (TargetRoom != null && TargetRoom.CheckRights(Session, true))
                        {
                            List<RoomItem> ToRemove = new List<RoomItem>();

                            lock (TargetRoom.Items)
                            {
                                ToRemove.AddRange(TargetRoom.Items);
                            }

                            foreach (RoomItem Item in ToRemove)
                            {
                                TargetRoom.RemoveFurniture(Session, Item.Id);
                                Session.GetHabbo().GetInventoryComponent().AddItem(Item.Id, Item.BaseItem, Item.ExtraData);
                            }

                            Session.GetHabbo().GetInventoryComponent().UpdateItems(true);
                            return true;
                        }

                        return false;

                    case "commands":
                    case "help":
                    case "info":
                    case "details":
                    case "about":

                        Session.SendNotif("This server is proudly powered by uberEmulator.\nCopyright (c) 2009, Roy 'Meth0d'\n\nhttp://www.uberemu.info", "http://www.uberemu.info");

                        return true;

                    case "empty":

                        Session.GetHabbo().GetInventoryComponent().ClearItems();

                        return true;
                    #endregion

                    #region Moderation Commands
                    case "bustest":

                        if (Session.GetHabbo().HasFuse("fuse_admin"))
                        {
                            ServerMessage Message = new ServerMessage(79);

                            Message.AppendStringWithBreak("This is a test poll!");
                            Message.AppendInt32(5);

                            Message.AppendInt32(133333);
                            Message.AppendStringWithBreak("Some option");

                            Message.AppendInt32(2);
                            Message.AppendStringWithBreak("Don't select me");

                            Message.AppendInt32(3);
                            Message.AppendStringWithBreak("Meh!");

                            Message.AppendInt32(4);
                            Message.AppendStringWithBreak("............");

                            Message.AppendInt32(5);
                            Message.AppendStringWithBreak("FUKKEN RAGE");

                            Session.GetHabbo().CurrentRoom.SendMessage(Message);                            

                            return true;
                        }

                        break;

                    case "invisible":

                        if (Session.GetHabbo().HasFuse("fuse_admin"))
                        {
                            if (Session.GetHabbo().SpectatorMode)
                            {
                                Session.GetHabbo().SpectatorMode = false;
                                Session.SendNotif("Spectator mode disabled. Reload the room to apply changes.");
                            }
                            else
                            {
                                Session.GetHabbo().SpectatorMode = true;
                                Session.SendNotif("Spectator mode enabled. Reload the room to apply changes.");
                            }

                            return true;
                        }

                        return false;

                    case "ban":

                        if (Session.GetHabbo().HasFuse("fuse_ban"))
                        {
                            TargetClient = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(Params[1]);

                            if (TargetClient == null)
                            {
                                Session.SendNotif("User not found.");
                                return true;
                            }

                            if (TargetClient.GetHabbo().Rank >= Session.GetHabbo().Rank)
                            {
                                Session.SendNotif("You are not allowed to ban that user.");
                                return true;
                            }

                            int BanTime = 0;
                                
                            try                                    
                            {
                                BanTime = int.Parse(Params[2]);
                            }
                            catch (FormatException) { }

                            if (BanTime <= 600)
                            {
                                Session.SendNotif("Ban time is in seconds and must be at least than 600 seconds (ten minutes). For more specific preset ban times, use the mod tool.");
                            }

                            UberEnvironment.GetGame().GetBanManager().BanUser(TargetClient, Session.GetHabbo().Username, BanTime, MergeParams(Params, 3), false);
                            return true;
                        }

                        return false;

                    case "superban":

                        if (Session.GetHabbo().HasFuse("fuse_superban"))
                        {
                            TargetClient = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(Params[1]);

                            if (TargetClient == null)
                            {
                                Session.SendNotif("User not found.");
                                return true;
                            }

                            if (TargetClient.GetHabbo().Rank >= Session.GetHabbo().Rank)
                            {
                                Session.SendNotif("You are not allowed to ban that user.");
                                return true;
                            }

                            UberEnvironment.GetGame().GetBanManager().BanUser(TargetClient, Session.GetHabbo().Username, 360000000, MergeParams(Params, 2), false);
                            return true;
                        }

                        return false;

                    case "roomkick":

                        if (Session.GetHabbo().HasFuse("fuse_roomkick"))
                        {
                            TargetRoom = UberEnvironment.GetGame().GetRoomManager().GetRoom(Session.GetHabbo().CurrentRoomId);

                            if (TargetRoom == null)
                            {
                                return false;
                            }

                            bool GenericMsg = true;
                            string ModMsg = MergeParams(Params, 1);

                            if (ModMsg.Length > 0)
                            {
                                GenericMsg = false;
                            }

                            foreach (RoomUser RoomUser in TargetRoom.UserList)
                            {
                                if (RoomUser.GetClient().GetHabbo().Rank >= Session.GetHabbo().Rank)
                                {
                                    continue;
                                }

                                if (!GenericMsg)
                                {
                                    RoomUser.GetClient().SendNotif("You have been kicked by an moderator: " + ModMsg);
                                }

                                TargetRoom.RemoveUserFromRoom(RoomUser.GetClient(), true, GenericMsg);
                            }

                            return true;
                        }

                        return false;

                    case "roomalert":

                        if (Session.GetHabbo().HasFuse("fuse_roomalert"))
                        {
                            TargetRoom = UberEnvironment.GetGame().GetRoomManager().GetRoom(Session.GetHabbo().CurrentRoomId);

                            if (TargetRoom == null)
                            {
                                return false;
                            }

                            string Msg = MergeParams(Params, 1);

                            foreach (RoomUser RoomUser in TargetRoom.UserList)
                            {
                                RoomUser.GetClient().SendNotif(Msg);
                            }

                            return true;
                        }

                        return false;

                    case "mute":

                        if (Session.GetHabbo().HasFuse("fuse_mute"))
                        {
                            TargetUser = Params[1];
                            TargetClient = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(TargetUser);

                            if (TargetClient == null || TargetClient.GetHabbo() == null)
                            {
                                Session.SendNotif("Could not find user: " + TargetUser);
                                return true;
                            }

                            if (TargetClient.GetHabbo().Rank >= Session.GetHabbo().Rank)
                            {
                                Session.SendNotif("You are not allowed to (un)mute that user.");
                                return true;
                            }

                            TargetClient.GetHabbo().Mute();
                            return true;
                        }

                        return false;

                    case "unmute":

                        if (Session.GetHabbo().HasFuse("fuse_mute"))
                        {
                            TargetUser = Params[1];
                            TargetClient = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(TargetUser);

                            if (TargetClient == null || TargetClient.GetHabbo() == null)
                            {
                                Session.SendNotif("Could not find user: " + TargetUser);
                                return true;
                            }

                            if (TargetClient.GetHabbo().Rank >= Session.GetHabbo().Rank)
                            {
                                Session.SendNotif("You are not allowed to (un)mute that user.");
                                return true;
                            }

                            TargetClient.GetHabbo().Unmute();
                            return true;
                        }

                        return false;

                    case "alert":

                        if (Session.GetHabbo().HasFuse("fuse_alert"))
                        {
                            TargetUser = Params[1];
                            TargetClient = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(TargetUser);

                            if (TargetClient == null)
                            {
                                Session.SendNotif("Could not find user: " + TargetUser);
                                return true;
                            }

                            TargetClient.SendNotif(MergeParams(Params, 2), Session.GetHabbo().HasFuse("fuse_admin"));
                            return true;
                        }

                        return false;

                    case "softkick":
                    case "kick":

                        if (Session.GetHabbo().HasFuse("fuse_kick"))
                        {
                            TargetUser = Params[1];
                            TargetClient = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(TargetUser);

                            if (TargetClient == null)
                            {
                                Session.SendNotif("Could not find user: " + TargetUser);
                                return true;
                            }

                            if (Session.GetHabbo().Rank <= TargetClient.GetHabbo().Rank)
                            {
                                Session.SendNotif("You are not allowed to kick that user.");
                                return true;
                            }

                            if (TargetClient.GetHabbo().CurrentRoomId < 1)
                            {
                                Session.SendNotif("That user is not in a room and can not be kicked.");
                                return true;
                            }

                            TargetRoom = UberEnvironment.GetGame().GetRoomManager().GetRoom(TargetClient.GetHabbo().CurrentRoomId);

                            if (TargetRoom == null)
                            {
                                return true;
                            }

                            TargetRoom.RemoveUserFromRoom(TargetClient, true, false);

                            if (Params.Length > 2)
                            {
                                TargetClient.SendNotif("A moderator has kicked you from the room for the following reason: " + MergeParams(Params, 2));
                            }
                            else
                            {
                                TargetClient.SendNotif("A moderator has kicked you from the room.");
                            }

                            return true;
                        }

                        return false;
                    #endregion
                }
            }
            catch (ExecutionEngineException) { }

            return false;
        }

        public static string MergeParams(string[] Params, int Start)
        {
            StringBuilder MergedParams = new StringBuilder();

            for (int i = 0; i < Params.Length; i++)
            {
                if (i < Start)
                {
                    continue;
                }

                if (i > Start)
                {
                    MergedParams.Append(" ");
                }

                MergedParams.Append(Params[i]);
            }

            return MergedParams.ToString();
        }
    }
}
