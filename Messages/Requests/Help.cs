using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.HabboHotel.Rooms;
using Uber.HabboHotel.Users.Messenger;
using Uber.HabboHotel.Support;
using Uber.HabboHotel.Pathfinding;

namespace Uber.Messages
{
    partial class GameClientMessageHandler
    {
        private void InitHelpTool()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetHelpTool().SerializeFrontpage());
        }

        private void GetHelpCategories()
        {
            Session.SendMessage(UberEnvironment.GetGame().GetHelpTool().SerializeIndex());
        }

        private void ViewHelpTopic()
        {
            uint TopicId = Request.PopWiredUInt();

            HelpTopic Topic = UberEnvironment.GetGame().GetHelpTool().GetTopic(TopicId);

            if (Topic == null)
            {
                return;
            }

            Session.SendMessage(UberEnvironment.GetGame().GetHelpTool().SerializeTopic(Topic));
        }

        private void SearchHelpTopics()
        {
            string SearchQuery = Request.PopFixedString();

            if (SearchQuery.Length < 3)
            {
                return;
            }

            Session.SendMessage(UberEnvironment.GetGame().GetHelpTool().SerializeSearchResults(SearchQuery));
        }

        private void GetTopicsInCategory()
        {
            uint Id = Request.PopWiredUInt();

            HelpCategory Category = UberEnvironment.GetGame().GetHelpTool().GetCategory(Id);

            if (Category == null)
            {
                return;
            }

            Session.SendMessage(UberEnvironment.GetGame().GetHelpTool().SerializeCategory(Category));
        }

        private void SubmitHelpTicket()
        {
            Boolean errorOccured = false;

            if (UberEnvironment.GetGame().GetModerationTool().UsersHasPendingTicket(Session.GetHabbo().Id))
            {
                errorOccured = true;
            }

            if (!errorOccured)
            {
                String Message = UberEnvironment.FilterInjectionChars(Request.PopFixedString());

                int Junk = Request.PopWiredInt32();
                int Type = Request.PopWiredInt32();
                uint ReportedUser = Request.PopWiredUInt();

                UberEnvironment.GetGame().GetModerationTool().SendNewTicket(Session, Type, ReportedUser, Message);
            }

            GetResponse().Init(321);
            GetResponse().AppendBoolean(errorOccured);
            SendResponse();
        }

        private void DeletePendingCFH()
        {
            if (!UberEnvironment.GetGame().GetModerationTool().UsersHasPendingTicket(Session.GetHabbo().Id))
            {
                return;
            }

            UberEnvironment.GetGame().GetModerationTool().DeletePendingTicketForUser(Session.GetHabbo().Id);

            GetResponse().Init(320);
            SendResponse();
        }

        private void ModGetUserInfo()
        {
            if (!Session.GetHabbo().HasFuse("fuse_mod"))
            {
                return;
            }

            uint UserId = Request.PopWiredUInt();

            if (UberEnvironment.GetGame().GetClientManager().GetNameById(UserId) != "Unknown User")
            {
                Session.SendMessage(UberEnvironment.GetGame().GetModerationTool().SerializeUserInfo(UserId));
            }
            else
            {
                Session.SendNotif("Could not load user info; invalid user.");
            }
        }

        private void ModGetUserChatlog()
        {
            if (!Session.GetHabbo().HasFuse("fuse_chatlogs"))
            {
                return;
            }

            Session.SendMessage(UberEnvironment.GetGame().GetModerationTool().SerializeUserChatlog(Request.PopWiredUInt()));
        }

        private void ModGetRoomChatlog()
        {
            if (!Session.GetHabbo().HasFuse("fuse_chatlogs"))
            {
                return;
            }

            int Junk = Request.PopWiredInt32();
            uint RoomId = Request.PopWiredUInt();

            if (UberEnvironment.GetGame().GetRoomManager().GetRoom(RoomId) != null)
            {
                Session.SendMessage(UberEnvironment.GetGame().GetModerationTool().SerializeRoomChatlog(RoomId));
            }
        }

        private void ModGetRoomTool()
        {
            if (!Session.GetHabbo().HasFuse("fuse_mod"))
            {
                return;
            }

            uint RoomId = Request.PopWiredUInt();
            RoomData Data = UberEnvironment.GetGame().GetRoomManager().GenerateNullableRoomData(RoomId);

            Session.SendMessage(UberEnvironment.GetGame().GetModerationTool().SerializeRoomTool(Data));
        }

        private void ModPickTicket()
        {
            if (!Session.GetHabbo().HasFuse("fuse_mod"))
            {
                return;
            }

            int Junk = Request.PopWiredInt32();
            uint TicketId = Request.PopWiredUInt();
            UberEnvironment.GetGame().GetModerationTool().PickTicket(Session, TicketId);
        }

        private void ModReleaseTicket()
        {
            if (!Session.GetHabbo().HasFuse("fuse_mod"))
            {
                return;
            }

            int amount = Request.PopWiredInt32();

            for (int i = 0; i < amount; i++)
            {
                uint TicketId = Request.PopWiredUInt();

                UberEnvironment.GetGame().GetModerationTool().ReleaseTicket(Session, TicketId);
            }
        }

        private void ModCloseTicket()
        {
            if (!Session.GetHabbo().HasFuse("fuse_mod"))
            {
                return;
            }

            int Result = Request.PopWiredInt32(); // result, 1 = useless, 2 = abusive, 3 = resolved
            int Junk = Request.PopWiredInt32(); // ? 
            uint TicketId = Request.PopWiredUInt(); // id

            UberEnvironment.GetGame().GetModerationTool().CloseTicket(Session, TicketId, Result);
        }

        private void ModGetTicketChatlog()
        {
            if (!Session.GetHabbo().HasFuse("fuse_mod"))
            {
                return;
            }

            SupportTicket Ticket = UberEnvironment.GetGame().GetModerationTool().GetTicket(Request.PopWiredUInt());

            if (Ticket == null)
            {
                return;
            }

            RoomData Data = UberEnvironment.GetGame().GetRoomManager().GenerateNullableRoomData(Ticket.RoomId);

            if (Data == null)
            {
                return;
            }

            Session.SendMessage(UberEnvironment.GetGame().GetModerationTool().SerializeTicketChatlog(Ticket, Data, Ticket.Timestamp));
        }

        private void ModGetRoomVisits()
        {
            if (!Session.GetHabbo().HasFuse("fuse_mod"))
            {
                return;
            }

            uint UserId = Request.PopWiredUInt();

            Session.SendMessage(UberEnvironment.GetGame().GetModerationTool().SerializeRoomVisits(UserId));
        }

        private void ModSendRoomAlert()
        {
            if (!Session.GetHabbo().HasFuse("fuse_alert"))
            {
                return;
            }

            int One = Request.PopWiredInt32();
            int Two = Request.PopWiredInt32();
            String Message = Request.PopFixedString();

            UberEnvironment.GetGame().GetModerationTool().RoomAlert(Session.GetHabbo().CurrentRoomId, !Two.Equals(3), Message);
        }

        private void ModPerformRoomAction()
        {
            if (!Session.GetHabbo().HasFuse("fuse_mod"))
            {
                return;
            }

            uint RoomId = Request.PopWiredUInt();
            Boolean ActOne = Request.PopWiredBoolean(); // set room lock to doorbell
            Boolean ActTwo = Request.PopWiredBoolean(); // set room to inappropiate
            Boolean ActThree = Request.PopWiredBoolean(); // kick all users

            UberEnvironment.GetGame().GetModerationTool().PerformRoomAction(Session, RoomId, ActThree, ActOne, ActTwo);
        }

        private void ModSendUserCaution()
        {
            if (!Session.GetHabbo().HasFuse("fuse_alert"))
            {
                return;
            }

            uint UserId = Request.PopWiredUInt();
            String Message = Request.PopFixedString();

            UberEnvironment.GetGame().GetModerationTool().AlertUser(Session, UserId, Message, true);
        }

        private void ModSendUserMessage()
        {
            if (!Session.GetHabbo().HasFuse("fuse_alert"))
            {
                return;
            }

            uint UserId = Request.PopWiredUInt();
            String Message = Request.PopFixedString();

            UberEnvironment.GetGame().GetModerationTool().AlertUser(Session, UserId, Message, false);
        }

        private void ModKickUser()
        {
            if (!Session.GetHabbo().HasFuse("fuse_kick"))
            {
                return;
            }

            uint UserId = Request.PopWiredUInt();
            String Message = Request.PopFixedString();

            UberEnvironment.GetGame().GetModerationTool().KickUser(Session, UserId, Message, false);
        }

        private void ModBanUser()
        {
            if (!Session.GetHabbo().HasFuse("fuse_ban"))
            {
                return;
            }

            uint UserId = Request.PopWiredUInt();
            String Message = Request.PopFixedString();
            int Length = Request.PopWiredInt32() * 3600;

            UberEnvironment.GetGame().GetModerationTool().BanUser(Session, UserId, Length, Message);
        }

        private void CallGuideBot()
        {
            Room Room = UberEnvironment.GetGame().GetRoomManager().GetRoom(Session.GetHabbo().CurrentRoomId);

            if (Room == null || !Room.CheckRights(Session, true))
            {
                return;
            }

            lock (Room.UserList)
            {
                List<RoomUser>.Enumerator Users = Room.UserList.GetEnumerator();

                while (Users.MoveNext())
                {
                    RoomUser User = Users.Current;

                    if (User.IsBot && User.BotData.AiType == "guide")
                    {
                        Session.GetMessageHandler().GetResponse().Init(33);
                        Session.GetMessageHandler().GetResponse().AppendInt32(4009);
                        Session.GetMessageHandler().SendResponse();

                        return;
                    }
                }
            }

            if (Session.GetHabbo().CalledGuideBot)
            {
                Session.GetMessageHandler().GetResponse().Init(33);
                Session.GetMessageHandler().GetResponse().AppendInt32(4010);
                Session.GetMessageHandler().SendResponse();

                return;
            }

            RoomUser NewUser = Room.DeployBot(UberEnvironment.GetGame().GetBotManager().GetBot(55));
            NewUser.SetPos(Room.Model.DoorX, Room.Model.DoorY, Room.Model.DoorZ);
            NewUser.UpdateNeeded = true;

            RoomUser RoomOwner = Room.GetRoomUserByHabbo(Room.Owner);

            if (RoomOwner != null)
            {
                NewUser.MoveTo(RoomOwner.Coordinate);
                NewUser.SetRot(Rotation.Calculate(NewUser.X, NewUser.Y, RoomOwner.X, RoomOwner.Y));
            }

            UberEnvironment.GetGame().GetAchievementManager().UnlockAchievement(Session, 6, 1);
            Session.GetHabbo().CalledGuideBot = true;
        }

        public void RegisterHelp()
        {
            RequestHandlers[416] = new RequestHandler(InitHelpTool);
            RequestHandlers[417] = new RequestHandler(GetHelpCategories);
            RequestHandlers[418] = new RequestHandler(ViewHelpTopic);
            RequestHandlers[419] = new RequestHandler(SearchHelpTopics);
            RequestHandlers[420] = new RequestHandler(GetTopicsInCategory);
            RequestHandlers[453] = new RequestHandler(SubmitHelpTicket);
            RequestHandlers[238] = new RequestHandler(DeletePendingCFH);
            RequestHandlers[440] = new RequestHandler(CallGuideBot);
            RequestHandlers[200] = new RequestHandler(ModSendRoomAlert);
            RequestHandlers[450] = new RequestHandler(ModPickTicket);
            RequestHandlers[451] = new RequestHandler(ModReleaseTicket);
            RequestHandlers[452] = new RequestHandler(ModCloseTicket);
            RequestHandlers[454] = new RequestHandler(ModGetUserInfo);
            RequestHandlers[455] = new RequestHandler(ModGetUserChatlog);
            RequestHandlers[456] = new RequestHandler(ModGetRoomChatlog);
            RequestHandlers[457] = new RequestHandler(ModGetTicketChatlog);
            RequestHandlers[458] = new RequestHandler(ModGetRoomVisits);
            RequestHandlers[459] = new RequestHandler(ModGetRoomTool);
            RequestHandlers[460] = new RequestHandler(ModPerformRoomAction);
            RequestHandlers[461] = new RequestHandler(ModSendUserCaution);
            RequestHandlers[462] = new RequestHandler(ModSendUserMessage);
            RequestHandlers[463] = new RequestHandler(ModKickUser);
            RequestHandlers[464] = new RequestHandler(ModBanUser);

        }
    }
}
