﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.HabboHotel.Rooms;
using Uber.HabboHotel.GameClients;
using Uber.Messages;
using Uber.Storage;

namespace Uber.HabboHotel.Support
{
    class ModerationTool
    {
        #region General

        private List<SupportTicket> Tickets;

        public List<string> UserMessagePresets;
        public List<string> RoomMessagePresets;

        public ModerationTool()
        {
            Tickets = new List<SupportTicket>();
            UserMessagePresets = new List<string>();
            RoomMessagePresets = new List<string>();
        }

        public ServerMessage SerializeTool()
        {
            ServerMessage Message = new ServerMessage(531);
            Message.AppendInt32(-1);
            Message.AppendInt32(UserMessagePresets.Count);

            lock (UserMessagePresets)
            {
                foreach (String Preset in UserMessagePresets)
                {
                    Message.AppendStringWithBreak(Preset);
                }
            }

            Message.AppendInt32(0);
            Message.AppendInt32(14);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);

            Message.AppendInt32(RoomMessagePresets.Count);

            lock (RoomMessagePresets)
            {
                foreach (String Preset in RoomMessagePresets)
                {
                    Message.AppendStringWithBreak(Preset);
                }
            }

            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendStringWithBreak("test");
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendStringWithBreak("test");
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendInt32(1);
            Message.AppendStringWithBreak("test");

            return Message;
        }

        #endregion

        #region Message Presets

        public void LoadMessagePresets()
        {
            UserMessagePresets.Clear();
            RoomMessagePresets.Clear();

            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT type,message FROM moderation_presets WHERE enabled = '1'");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                String Message = (String)Row["message"];

                switch (Row["type"].ToString().ToLower())
                {
                    case "message":

                        UserMessagePresets.Add(Message);
                        break;

                    case "roommessage":

                        RoomMessagePresets.Add(Message);
                        break;
                }
            }
        }

        #endregion

        #region Support Tickets

        public void LoadPendingTickets()
        {
            Tickets.Clear();

            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT id,score,type,status,sender_id,reported_id,moderator_id,message,room_id,room_name,timestamp FROM moderation_tickets WHERE status = 'open' OR status = 'picked'");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                SupportTicket Ticket = new SupportTicket((uint)Row["id"], (int)Row["score"], (int)Row["type"], (uint)Row["sender_id"], (uint)Row["reported_id"], (String)Row["message"], (uint)Row["room_id"], (String)Row["room_name"], (Double)Row["timestamp"]);

                if (Row["status"].ToString().ToLower() == "picked")
                {
                    Ticket.Pick((uint)Row["moderator_id"], false);
                }

                Tickets.Add(Ticket);
            }
        }

        public void SendNewTicket(GameClient Session, int Category, uint ReportedUser, String Message)
        {
            if (Session.GetHabbo().CurrentRoomId <= 0)
            {
                return;
            }

            RoomData Data = UberEnvironment.GetGame().GetRoomManager().GenerateNullableRoomData(Session.GetHabbo().CurrentRoomId);

            uint TicketId = 0;
            
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("message", Message);
                dbClient.AddParamWithValue("name", Data.Name);

                dbClient.ExecuteQuery("INSERT INTO moderation_tickets (score,type,status,sender_id,reported_id,moderator_id,message,room_id,room_name,timestamp) VALUES (1,'" + Category + "','open','" + Session.GetHabbo().Id + "','" + ReportedUser + "','0',@message,'" + Data.Id + "',@name,'" + UberEnvironment.GetUnixTimestamp() + "')");
                dbClient.ExecuteQuery("UPDATE user_info SET cfhs = cfhs + 1 WHERE user_id = '" + Session.GetHabbo().Id + "' LIMIT 1");

                TicketId = (uint)dbClient.ReadDataRow("SELECT id FROM moderation_tickets WHERE sender_id = '" + Session.GetHabbo().Id + "' ORDER BY id DESC LIMIT 1")[0];
            }

            SupportTicket Ticket = new SupportTicket(TicketId, 1, Category, Session.GetHabbo().Id, ReportedUser, Message, Data.Id, Data.Name, UberEnvironment.GetUnixTimestamp());

            Tickets.Add(Ticket);

            SendTicketToModerators(Ticket);
        }

        public void SendOpenTickets(GameClient Session)
        {
            lock (Tickets)
            {
                foreach (SupportTicket Ticket in Tickets)
                {
                    if (Ticket.Status != TicketStatus.OPEN && Ticket.Status != TicketStatus.PICKED)
                    {
                        continue;
                    }

                    Session.SendMessage(Ticket.Serialize());
                }
            }
        }

        public SupportTicket GetTicket(uint TicketId)
        {
            lock (Tickets)
            {
                foreach (SupportTicket Ticket in Tickets)
                {
                    if (Ticket.TicketId == TicketId)
                    {
                        return Ticket;
                    }
                }
            }

            return null;
        }

        public void PickTicket(GameClient Session, uint TicketId)
        {
            SupportTicket Ticket = GetTicket(TicketId);

            if (Ticket == null || Ticket.Status != TicketStatus.OPEN)
            {
                return;
            }

            Ticket.Pick(Session.GetHabbo().Id, true);
            SendTicketToModerators(Ticket);
        }

        public void ReleaseTicket(GameClient Session, uint TicketId)
        {
            SupportTicket Ticket = GetTicket(TicketId);

            if (Ticket == null || Ticket.Status != TicketStatus.PICKED || Ticket.ModeratorId != Session.GetHabbo().Id)
            {
                return;
            }

            Ticket.Release(true);
            SendTicketToModerators(Ticket);
        }

        public void CloseTicket(GameClient Session, uint TicketId, int Result)
        {
            SupportTicket Ticket = GetTicket(TicketId);

            if (Ticket == null || Ticket.Status != TicketStatus.PICKED || Ticket.ModeratorId != Session.GetHabbo().Id)
            {
                return;
            }

            GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(Ticket.SenderId);

            TicketStatus NewStatus;
            int ResultCode;

            switch (Result)
            {
                case 1:

                    ResultCode = 1;
                    NewStatus = TicketStatus.INVALID;
                    break;

                case 2:

                    ResultCode = 2;
                    NewStatus = TicketStatus.ABUSIVE;

                    using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                    {
                        dbClient.ExecuteQuery("UPDATE user_info SET cfhs_abusive = cfhs_abusive + 1 WHERE user_id = '" + Ticket.SenderId + "' LIMIT 1");
                    }

                    break;

                case 3:
                default:

                    ResultCode = 0;
                    NewStatus = TicketStatus.RESOLVED;
                    break;
            }

            if (Client != null)
            {
                Client.GetMessageHandler().GetResponse().Init(540);
                Client.GetMessageHandler().GetResponse().AppendInt32(ResultCode);
                Client.GetMessageHandler().SendResponse();
            }

            Ticket.Close(NewStatus, true);
            SendTicketToModerators(Ticket);
        }

        public Boolean UsersHasPendingTicket(uint Id)
        {
            lock (Tickets)
            {
                foreach (SupportTicket Ticket in Tickets)
                {
                    if (Ticket.SenderId == Id && Ticket.Status == TicketStatus.OPEN)
                    {
                        return true;
                    }
                }
            }

            return false;
        }

        public void DeletePendingTicketForUser(uint Id)
        {
            lock (Tickets)
            {
                foreach (SupportTicket Ticket in Tickets)
                {
                    if (Ticket.SenderId == Id)
                    {
                        Ticket.Delete(true);
                        SendTicketToModerators(Ticket);
                        return;
                    }
                }
            }
        }

        public void SendTicketToModerators(SupportTicket Ticket)
        {
            UberEnvironment.GetGame().GetClientManager().BroadcastMessage(Ticket.Serialize(), "fuse_mod");
        }

        #endregion

        #region Room Moderation

        public void PerformRoomAction(GameClient ModSession, uint RoomId, Boolean KickUsers, Boolean LockRoom, Boolean InappropriateRoom)
        {
            Room Room = UberEnvironment.GetGame().GetRoomManager().GetRoom(RoomId);

            if (Room == null)
            {
                return;
            }

            if (LockRoom)
            {
                Room.State = 1;

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE rooms SET state = 'locked' WHERE id = '" + Room.RoomId + "' LIMIT 1");
                }
            }

            if (InappropriateRoom)
            {
                Room.Name = "Inappropriate to Hotel Managament";
                Room.Description = "Inappropriate to Hotel Management";
                Room.Tags.Clear();

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE rooms SET caption = 'Inappropriate to Hotel Management', description = 'Inappropriate to Hotel Management', tags = '' WHERE id = '" + Room.RoomId + "' LIMIT 1");
                }
            }

            if (KickUsers)
            {
                lock (Room.UserList)
                {
                    List<RoomUser> ToRemove = new List<RoomUser>();

                    foreach (RoomUser User in Room.UserList)
                    {
                        if (User.IsBot || User.GetClient().GetHabbo().Rank >= ModSession.GetHabbo().Rank)
                        {
                            continue;
                        }

                        ToRemove.Add(User);
                    }

                    for (int i = 0; i < ToRemove.Count; i++)
                    {
                        Room.RemoveUserFromRoom(ToRemove[i].GetClient(), true, false);
                    }
                }
            }
        }

        public void RoomAlert(uint RoomId, Boolean Caution, String Message)
        {
            Room Room = UberEnvironment.GetGame().GetRoomManager().GetRoom(RoomId);

            if (Room == null || Message.Length <= 1)
            {
                return;
            }

            StringBuilder QueryBuilder = new StringBuilder();
            int j = 0;

            lock (Room.UserList)
            {
                foreach (RoomUser User in Room.UserList)
                {
                    if (User.IsBot)
                    {
                        continue;
                    }

                    User.GetClient().SendNotif(Message, Caution);

                    if (j > 0)
                    {
                        QueryBuilder.Append(" OR ");
                    }

                    QueryBuilder.Append("user_id = '" + User.GetClient().GetHabbo().Id + "'");
                    j++;
                }
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("UPDATE user_info SET cautions = cautions + 1 WHERE " + QueryBuilder.ToString() + " LIMIT " + j);
            }
        }

        public ServerMessage SerializeRoomTool(RoomData Data)
        {
            Room Room = UberEnvironment.GetGame().GetRoomManager().GetRoom(Data.Id);
            uint OwnerId = 0;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                try
                {
                    OwnerId = (uint)dbClient.ReadDataRow("SELECT id FROM users WHERE username = '" + Data.Owner + "' LIMIT 1")[0];
                }
                catch (Exception) { }
            }

            ServerMessage Message = new ServerMessage(538);
            Message.AppendUInt(Data.Id);
            Message.AppendInt32(Data.UsersNow); // user count

            if (Room != null)
            {
                Message.AppendBoolean((Room.GetRoomUserByHabbo(Data.Owner) != null));
            }
            else
            {
                Message.AppendBoolean(false);
            }

            Message.AppendUInt(OwnerId);
            Message.AppendStringWithBreak(Data.Owner);
            Message.AppendUInt(Data.Id);
            Message.AppendStringWithBreak(Data.Name);
            Message.AppendStringWithBreak(Data.Description);
            Message.AppendInt32(Data.TagCount);

            foreach (string Tag in Data.Tags)
            {
                Message.AppendStringWithBreak(Tag);
            }

            if (Room != null)
            {
                Message.AppendBoolean(Room.HasOngoingEvent);

                if (Room.Event != null)
                {
                    Message.AppendStringWithBreak(Room.Event.Name);
                    Message.AppendStringWithBreak(Room.Event.Description);
                    Message.AppendInt32(Room.Event.Tags.Count);

                    lock (Room.Event.Tags)
                    {
                        foreach (string Tag in Room.Event.Tags)
                        {
                            Message.AppendStringWithBreak(Tag);
                        }
                    }
                }
            }
            else
            {
                Message.AppendBoolean(false);
            }

            return Message;
        }

        #endregion

        #region User Moderation

        public void KickUser(GameClient ModSession, uint UserId, String Message, Boolean Soft)
        {
            GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);

            if (Client == null || Client.GetHabbo().CurrentRoomId < 1 || Client.GetHabbo().Id == ModSession.GetHabbo().Id)
            {
                return;
            }

            if (Client.GetHabbo().Rank >= ModSession.GetHabbo().Rank)
            {
                ModSession.SendNotif("You do not have permission to kick that user.");
                return;
            }

            Room Room = UberEnvironment.GetGame().GetRoomManager().GetRoom(Client.GetHabbo().CurrentRoomId);

            if (Room == null)
            {
                return;
            }

            Room.RemoveUserFromRoom(Client, true, false);

            if (!Soft)
            {
                Client.SendNotif(Message);

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE user_info SET cautions = cautions + 1 WHERE user_id = '" + UserId + "' LIMIT 1");
                }
            }
        }

        public void AlertUser(GameClient ModSession, uint UserId, String Message, Boolean Caution)
        {
            GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);

            if (Client == null || Client.GetHabbo().Id == ModSession.GetHabbo().Id)
            {
                return;
            }

            if (Caution && Client.GetHabbo().Rank >= ModSession.GetHabbo().Rank)
            {
                ModSession.SendNotif("You do not have permission to caution that user, sending as a regular message instead.");
                Caution = false;
            }

            Client.SendNotif(Message, Caution);

            if (Caution)
            {
                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE user_info SET cautions = cautions + 1 WHERE user_id = '" + UserId + "' LIMIT 1");
                }
            }
        }

        public void BanUser(GameClient ModSession, uint UserId, int Length, String Message)
        {
            GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);

            if (Client == null || Client.GetHabbo().Id == ModSession.GetHabbo().Id)
            {
                return;
            }

            if (Client.GetHabbo().Rank >= ModSession.GetHabbo().Rank)
            {
                ModSession.SendNotif("You do not have permission to ban that user.");
                return;
            }

            Double dLength = Length;

            UberEnvironment.GetGame().GetBanManager().BanUser(Client, ModSession.GetHabbo().Username, dLength, Message, false);
        }

        #endregion

        #region User Info

        public ServerMessage SerializeUserInfo(uint UserId)
        {
            DataRow User = null;
            DataRow Info = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                User = dbClient.ReadDataRow("SELECT * FROM users WHERE id = '" + UserId + "' LIMIT 1");
                Info = dbClient.ReadDataRow("SELECT * FROM user_info WHERE user_id = '" + UserId + "' LIMIT 1");
            }

            if (User == null)
            {
                throw new ArgumentException();
            }

            ServerMessage Message = new ServerMessage(533);

            Message.AppendUInt((uint)User["id"]);
            Message.AppendStringWithBreak((string)User["username"]);

            if (Info != null)
            {
                Message.AppendInt32((int)Math.Ceiling((UberEnvironment.GetUnixTimestamp() - (Double)Info["reg_timestamp"]) / 60)); 
                Message.AppendInt32((int)Math.Ceiling((UberEnvironment.GetUnixTimestamp() - (Double)Info["login_timestamp"]) / 60));
            }
            else
            {
                Message.AppendInt32(0);
                Message.AppendInt32(0);
            }

            if (User["online"].ToString() == "1")
            {
                Message.AppendBoolean(true);
            }
            else
            {
                Message.AppendBoolean(false);
            }

            if (Info != null)
            {
                Message.AppendInt32((int)Info["cfhs"]);
                Message.AppendInt32((int)Info["cfhs_abusive"]);
                Message.AppendInt32((int)Info["cautions"]);
                Message.AppendInt32((int)Info["bans"]); 
            }
            else
            {
                Message.AppendInt32(0); // cfhs
                Message.AppendInt32(0); // abusive cfhs
                Message.AppendInt32(0); // cautions
                Message.AppendInt32(0); // bans
            }

            return Message;
        }

        public ServerMessage SerializeRoomVisits(uint UserId)
        {
            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT room_id,hour,minute FROM user_roomvisits WHERE user_id = '" + UserId + "' ORDER BY entry_timestamp DESC LIMIT 50");
            }

            ServerMessage Message = new ServerMessage(537);
            Message.AppendUInt(UserId);
            Message.AppendStringWithBreak(UberEnvironment.GetGame().GetClientManager().GetNameById(UserId));

            if (Data != null)
            {
                Message.AppendInt32(Data.Rows.Count);

                foreach (DataRow Row in Data.Rows)
                {
                    RoomData RoomData = UberEnvironment.GetGame().GetRoomManager().GenerateNullableRoomData((uint)Row["room_id"]);

                    Message.AppendBoolean(RoomData.IsPublicRoom);
                    Message.AppendUInt(RoomData.Id);
                    Message.AppendStringWithBreak(RoomData.Name);
                    Message.AppendInt32((int)Row["hour"]);
                    Message.AppendInt32((int)Row["minute"]);
                }
            }
            else
            {
                Message.AppendInt32(0);
            }

            return Message;
        }

        #endregion

        #region Chatlogs

        public ServerMessage SerializeUserChatlog(uint UserId)
        {
            DataTable Visits = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Visits = dbClient.ReadDataTable("SELECT room_id,entry_timestamp,exit_timestamp FROM user_roomvisits WHERE user_id = '" + UserId  + "' ORDER BY entry_timestamp DESC LIMIT 5");
            }

            ServerMessage Message = new ServerMessage(536);
            Message.AppendUInt(UserId);
            Message.AppendStringWithBreak(UberEnvironment.GetGame().GetClientManager().GetNameById(UserId));

            if (Visits != null)
            {
                Message.AppendInt32(Visits.Rows.Count);

                foreach (DataRow Visit in Visits.Rows)
                {
                    DataTable Chatlogs = null;

                    if ((Double)Visit["exit_timestamp"] <= 0.0)
                    {
                        Visit["exit_timestamp"] = UberEnvironment.GetUnixTimestamp();
                    }

                    using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                    {
                        Chatlogs = dbClient.ReadDataTable("SELECT user_id,user_name,hour,minute,message FROM chatlogs WHERE room_id = '" + (uint)Visit["room_id"] + "' AND timestamp > " + (Double)Visit["entry_timestamp"] + " AND timestamp < " + (Double)Visit["exit_timestamp"] + " ORDER BY timestamp DESC");
                    }

                    RoomData RoomData = UberEnvironment.GetGame().GetRoomManager().GenerateNullableRoomData((uint)Visit["room_id"]);

                    Message.AppendBoolean(RoomData.IsPublicRoom);
                    Message.AppendUInt(RoomData.Id);
                    Message.AppendStringWithBreak(RoomData.Name);

                    if (Chatlogs != null)
                    {
                        Message.AppendInt32(Chatlogs.Rows.Count);

                        foreach (DataRow Log in Chatlogs.Rows)
                        {
                            Message.AppendInt32((int)Log["hour"]);
                            Message.AppendInt32((int)Log["minute"]);
                            Message.AppendUInt((uint)Log["user_id"]);
                            Message.AppendStringWithBreak((string)Log["user_name"]);
                            Message.AppendStringWithBreak((string)Log["message"]);
                        }
                    }
                    else
                    {
                        Message.AppendInt32(0);
                    }
                }
            }
            else
            {
                Message.AppendInt32(0);
            }

            return Message;
        }

        public ServerMessage SerializeTicketChatlog(SupportTicket Ticket, RoomData RoomData, Double Timestamp)
        {
            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT user_id,user_name,hour,minute,message FROM chatlogs WHERE room_id = '" + RoomData.Id + "' AND timestamp >= " + (Timestamp - 300) + " AND timestamp <= " + Timestamp + " ORDER BY timestamp DESC");
            }

            ServerMessage Message = new ServerMessage(534);
            Message.AppendUInt(Ticket.TicketId);
            Message.AppendUInt(Ticket.SenderId);
            Message.AppendUInt(Ticket.ReportedId);
            Message.AppendBoolean(RoomData.IsPublicRoom);
            Message.AppendUInt(RoomData.Id);
            Message.AppendStringWithBreak(RoomData.Name);

            if (Data != null)
            {
                Message.AppendInt32(Data.Rows.Count);

                foreach (DataRow Row in Data.Rows)
                {
                    Message.AppendInt32((int)Row["hour"]);
                    Message.AppendInt32((int)Row["minute"]);
                    Message.AppendUInt((uint)Row["user_id"]);
                    Message.AppendStringWithBreak((String)Row["user_name"]);
                    Message.AppendStringWithBreak((String)Row["message"]);
                }
            }
            else
            {
                Message.AppendInt32(0);
            }

            return Message;
        }

        public ServerMessage SerializeRoomChatlog(uint RoomId)
        {
            Room Room = UberEnvironment.GetGame().GetRoomManager().GetRoom(RoomId);

            if (Room == null)
            {
                throw new ArgumentException();
            }

            Boolean IsPublic = false;

            if (Room.Type.ToLower() == "public")
            {
                IsPublic = true;
            }

            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT user_id,user_name,hour,minute,message FROM chatlogs WHERE room_id = '" + Room.RoomId + "' ORDER BY timestamp DESC LIMIT 150");
            }

            ServerMessage Message = new ServerMessage(535);
            Message.AppendBoolean(IsPublic);
            Message.AppendUInt(Room.RoomId);
            Message.AppendStringWithBreak(Room.Name);

            if (Data != null)
            {
                Message.AppendInt32(Data.Rows.Count);

                foreach (DataRow Row in Data.Rows)
                {
                    Message.AppendInt32((int)Row["hour"]);
                    Message.AppendInt32((int)Row["minute"]);
                    Message.AppendUInt((uint)Row["user_id"]);
                    Message.AppendStringWithBreak((string)Row["user_name"]);
                    Message.AppendStringWithBreak((string)Row["message"]);
                }
            }
            else
            {
                Message.AppendInt32(0);
            }

            return Message;
        }

        #endregion
    }
}
