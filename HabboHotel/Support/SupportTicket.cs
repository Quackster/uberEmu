using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Messages;
using Uber.Storage;

namespace Uber.HabboHotel.Support
{
    enum TicketStatus
    {
        OPEN = 0,
        PICKED = 1,
        RESOLVED = 2,
        ABUSIVE = 3,
        INVALID = 4,
        DELETED = 5
    }

    class SupportTicket
    {
        private uint Id;
        public int Score;
        public int Type;

        public TicketStatus Status;

        public uint SenderId;
        public uint ReportedId;
        public uint ModeratorId;

        public String Message;

        public uint RoomId;
        public String RoomName;

        public Double Timestamp;

        public int TabId
        {
            get
            {
                if (Status == TicketStatus.OPEN)
                {
                    return 1;
                }

                if (Status == TicketStatus.PICKED)
                {
                    return 2;
                }

                return 0;
            }
        }

        public uint TicketId
        {
            get
            {
                return Id;
            }
        }

        public SupportTicket(uint Id, int Score, int Type, uint SenderId, uint ReportedId, String Message, uint RoomId, String RoomName, Double Timestamp)
        {
            this.Id = Id;
            this.Score = Score;
            this.Type = Type;
            this.Status = TicketStatus.OPEN;
            this.SenderId = SenderId;
            this.ReportedId = ReportedId;
            this.ModeratorId = 0;
            this.Message = Message;
            this.RoomId = RoomId;
            this.RoomName = RoomName;
            this.Timestamp = Timestamp;
        }

        public void Pick(uint ModeratorId, Boolean UpdateInDb)
        {
            this.Status = TicketStatus.PICKED;
            this.ModeratorId = ModeratorId;

            if (UpdateInDb)
            {
                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE moderation_tickets SET status = 'picked', moderator_id = '" + ModeratorId + "' WHERE id = '" + Id + "' LIMIT 1");
                }
            }
        }

        public void Close(TicketStatus NewStatus, Boolean UpdateInDb)
        {
            this.Status = NewStatus;

            if (UpdateInDb)
            {
                String dbType = "";

                switch (NewStatus)
                {
                    case TicketStatus.ABUSIVE:

                        dbType = "abusive";
                        break;

                    case TicketStatus.INVALID:

                        dbType = "invalid";
                        break;

                    case TicketStatus.RESOLVED:
                    default:

                        dbType = "resolved";
                        break;
                }

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE moderation_tickets SET status = '" + dbType + "' WHERE id = '" + Id + "' LIMIT 1");
                }
            }
        }

        public void Release(Boolean UpdateInDb)
        {
            this.Status = TicketStatus.OPEN;

            if (UpdateInDb)
            {
                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE moderation_tickets SET status = 'open' WHERE id = '" + Id + "' LIMIT 1");
                }
            }
        }

        public void Delete(Boolean UpdateInDb)
        {
            this.Status = TicketStatus.DELETED;

            if (UpdateInDb)
            {
                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE moderation_tickets SET status = 'deleted' WHERE id = '" + Id + "' LIMIT 1");
                }
            }
        }

        public ServerMessage Serialize()
        {
            ServerMessage Message = new ServerMessage(530);
            Message.AppendUInt(Id);
            Message.AppendInt32(TabId);
            Message.AppendInt32(11); // ??
            Message.AppendInt32(Type);
            Message.AppendInt32(11); // ??
            Message.AppendInt32(Score);
            Message.AppendUInt(SenderId);
            Message.AppendStringWithBreak(UberEnvironment.GetGame().GetClientManager().GetNameById(SenderId));
            Message.AppendUInt(ReportedId);
            Message.AppendStringWithBreak(UberEnvironment.GetGame().GetClientManager().GetNameById(ReportedId));
            Message.AppendUInt(ModeratorId);
            Message.AppendStringWithBreak(UberEnvironment.GetGame().GetClientManager().GetNameById(ModeratorId));
            Message.AppendStringWithBreak(this.Message);
            Message.AppendUInt(RoomId);
            Message.AppendStringWithBreak(RoomName);
            return Message;
        }
    }
}
