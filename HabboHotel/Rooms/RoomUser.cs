using System;
using System.Collections.Generic;
using System.Text;

using Uber.Storage;
using Uber.Messages;
using Uber.HabboHotel.GameClients;
using Uber.HabboHotel.Pathfinding;
using Uber.HabboHotel.RoomBots;
using Uber.HabboHotel.Misc;
using Uber.HabboHotel.Pets;

namespace Uber.HabboHotel.Rooms
{
    class RoomUser
    {
        public uint HabboId;
        public int VirtualId;
        public uint RoomId;

        public int IdleTime;

        public int X;
        public int Y;
        public double Z;

        public int CarryItemID;
        public int CarryTimer;

        public int RotHead;
        public int RotBody;

        public bool CanWalk;
        public bool AllowOverride;

        public int GoalX;
        public int GoalY;

        public Boolean SetStep;
        public int SetX;
        public int SetY;
        public double SetZ;

        public RoomBot BotData;
        public BotAI BotAI;

        public Coord Coordinate
        {
            get
            {
                return new Coord(X, Y);
            }
        }

        public bool IsPet
        {
            get
            {
                return (IsBot && BotData.IsPet);
            }
        }

        public Pet PetData;

        public Boolean IsWalking;
        public Boolean UpdateNeeded;
        public Boolean IsAsleep;

        public Dictionary<string, string> Statusses;

        public int DanceId;

        public List<Coord> Path;
        public int PathStep;

        public bool PathRecalcNeeded;
        public int PathRecalcX;
        public int PathRecalcY;

        public int TeleDelay;

        public Boolean IsDancing
        {
            get
            {
                if (DanceId >= 1)
                {
                    return true;
                }

                return false;
            }
        }

        public Boolean NeedsAutokick
        {
            get
            {
                if (IsBot)
                {
                    return false;
                }

                if (IdleTime >= 1800)
                {
                    return true;
                }

                return false;
            }
        }

        public bool IsTrading
        {
            get
            {
                if (IsBot)
                {
                    return false;
                }

                if (Statusses.ContainsKey("trd"))
                {
                    return true;
                }

                return false;
            }
        }

        public bool IsBot
        {
            get
            {
                if (this.BotData != null)
                {
                    return true;
                }

                return false;
            }
        }

        public bool IsSpectator;

        public RoomUser(uint HabboId, uint RoomId, int VirtualId)
        {
            this.HabboId = HabboId;
            this.RoomId = RoomId;
            this.VirtualId = VirtualId;
            this.IdleTime = 0;
            this.X = 0;
            this.Y = 0;
            this.Z = 0;
            this.RotHead = 0;
            this.RotBody = 0;
            this.UpdateNeeded = true;
            this.Statusses = new Dictionary<string, string>();
            this.Path = new List<Coord>();
            this.PathStep = 0;
            this.TeleDelay = -1;

            this.AllowOverride = false;
            this.CanWalk = true;

            this.IsSpectator = false;
        }

        public void Unidle()
        {
            this.IdleTime = 0;

            if (this.IsAsleep)
            {
                this.IsAsleep = false;

                ServerMessage Message = new ServerMessage(486);
                Message.AppendInt32(VirtualId);
                Message.AppendBoolean(false);

                GetRoom().SendMessage(Message);
            }
        }

        public void Chat(GameClient Session, string Message, bool Shout)
        {
            Unidle();


           /* if (!IsBot)
            {
                if (GetClient().GetHabbo().NewbieStatus == 1)
                {
                    if (Message == "Yes, I verify that I will only speak English in the hotel." || Message == "Yes, I verify that I will only speak English in the hotel")
                    {
                        GetClient().GetHabbo().NewbieStatus = 2;

                        using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                        {
                            dbClient.ExecuteQuery("UPDATE users SET newbie_status = '2' WHERE id = '" + GetClient().GetHabbo().Id + "' LIMIT 1");
                        }

                        GetClient().SendNotif("Thank you. You can now use chat.");
                        return;
                    }
                    else
                    {
                        GetClient().SendNotif("We would like to urge that Uber is an English hotel. Before you are allowed to use chat, please verify you will speak English by typing \"Yes, I verify that I will only speak English in the hotel.\" in the chat. This is case-sensitive.");
                        return;
                    }
                }
            }*/

            if (!IsBot && GetClient().GetHabbo().Muted)
            {
                GetClient().SendNotif("You are muted.");
                return;
            }

            if (Message.StartsWith(":") && Session != null && ChatCommandHandler.Parse(Session, Message.Substring(1)))
            {
                return;
            }

            uint ChatHeader = 24;

            if (Shout)
            {
                ChatHeader = 26;
            }

            ServerMessage ChatMessage = new ServerMessage(ChatHeader);
            ChatMessage.AppendInt32(VirtualId);
            ChatMessage.AppendStringWithBreak(Message);
            ChatMessage.AppendInt32(GetSpeechEmotion(Message));

            GetRoom().TurnHeads(X, Y, HabboId);
            GetRoom().SendMessage(ChatMessage);

            if (!IsBot)
            {
                GetRoom().OnUserSay(this, Message, Shout);
            }

            if (!IsBot)
            {
                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.AddParamWithValue("message", Message);
                    dbClient.ExecuteQuery("INSERT INTO chatlogs (user_id,room_id,hour,minute,timestamp,message,user_name,full_date) VALUES ('" + Session.GetHabbo().Id + "','" + GetRoom().RoomId + "','" + DateTime.Now.Hour + "','" + DateTime.Now.Minute + "','" + UberEnvironment.GetUnixTimestamp() + "',@message,'" + Session.GetHabbo().Username + "','" + DateTime.Now.ToLongDateString() + "')");
                }
            }
        }

        public int GetSpeechEmotion(string Message)
        {
            Message = Message.ToLower();

            if (Message.Contains(":)") || Message.Contains(":d") || Message.Contains("=]") || 
                Message.Contains("=d") || Message.Contains(":>"))
            {
                return 1;
            }

            if (Message.Contains(">:(") || Message.Contains(":@"))
            {
                return 2;
            }

            if (Message.Contains(":o"))
            {
                return 3;
            }

            if (Message.Contains(":(") || Message.Contains("=[") || Message.Contains(":'(") || Message.Contains("='["))
            {
                return 4;
            }

            return 0;
        }

        public void ClearMovement(bool Update)
        {
            IsWalking = false;
            PathRecalcNeeded = false;
            Path = new List<Coord>();
            Statusses.Remove("mv");
            GoalX = 0;
            GoalY = 0;
            SetStep = false;
            SetX = 0;
            SetY = 0;
            SetZ = 0;

            if (Update)
            {
                UpdateNeeded = true;
            }
        }

        public void MoveTo(Coord c)
        {
            MoveTo(c.x, c.y);
        }

        public void MoveTo(int X, int Y)
        {
            Unidle();

            PathRecalcNeeded = true;
            PathRecalcX = X;
            PathRecalcY = Y;
        }

        public void UnlockWalking()
        {
            this.AllowOverride = false;
            this.CanWalk = true;
        }

        public void SetPos(int X, int Y, double Z)
        {
            this.X = X;
            this.Y = Y;
            this.Z = Z;
        }

        public void CarryItem(int Item)
        {
            this.CarryItemID = Item;

            if (Item > 0)
            {
                this.CarryTimer = 240;
            }
            else
            {
                this.CarryTimer = 0;
            }

            ServerMessage Message = new ServerMessage(482);
            Message.AppendInt32(VirtualId);
            Message.AppendInt32(Item);
            GetRoom().SendMessage(Message);
        }

        public void SetRot(int Rotation)
        {
            SetRot(Rotation, false);
        }

        public void SetRot(int Rotation, bool HeadOnly)
        {
            if (Statusses.ContainsKey("lay") || IsWalking)
            {
                return;
            }

            int diff = this.RotBody - Rotation;

            this.RotHead = this.RotBody;

            if (Statusses.ContainsKey("sit") || HeadOnly)
            {
                if (RotBody == 2 || RotBody == 4)
                {
                    if (diff > 0)
                    {
                        RotHead = RotBody - 1;
                    }
                    else if (diff < 0)
                    {
                        RotHead = RotBody + 1;
                    }
                }
                else if (RotBody == 0 || RotBody == 6)
                {
                    if (diff > 0)
                    {
                        RotHead = RotBody - 1;
                    }
                    else if (diff < 0)
                    {
                        RotHead = RotBody + 1;
                    }
                }
            }
            else if (diff <= -2 || diff >= 2)
            {
                this.RotHead = Rotation;
                this.RotBody = Rotation;
            }
            else
            {
                this.RotHead = Rotation;
            }

            this.UpdateNeeded = true;
        }

        public void AddStatus(string Key, string Value)
        {
            Statusses[Key] = Value;
        }

        public void RemoveStatus(string Key)
        {
            if (Statusses.ContainsKey(Key))
            {
                Statusses.Remove(Key);
            }
        }

        public void ResetStatus()
        {
            Statusses = new Dictionary<string, string>();
        }

        public void Serialize(ServerMessage Message)
        {
            // @\Ihqu@UMeth0d13haiihr-893-45.hd-180-8.ch-875-62.lg-280-62.sh-290-62.ca-1813-.he-1601-[IMRAPD4.0JImMcIrDK
            // MSadiePull up a pew and have a brew!hr-500-45.hd-600-1.ch-823-75.lg-716-76.sh-730-62.he-1602-75IRBPA2.0PAK

            if (IsSpectator)
            {
                return;
            }

            if (!IsBot)
            {
                Message.AppendUInt(GetClient().GetHabbo().Id);
                Message.AppendStringWithBreak(GetClient().GetHabbo().Username);
                Message.AppendStringWithBreak(GetClient().GetHabbo().Motto);
                Message.AppendStringWithBreak(GetClient().GetHabbo().Look);
                Message.AppendInt32(VirtualId);
                Message.AppendInt32(X);
                Message.AppendInt32(Y);
                Message.AppendStringWithBreak(Z.ToString().Replace(',', '.'));
                Message.AppendInt32(2);
                Message.AppendInt32(1);
                Message.AppendStringWithBreak(GetClient().GetHabbo().Gender.ToLower());
                Message.AppendInt32(-1);
                Message.AppendInt32(-1);
                Message.AppendInt32(-1);
                Message.AppendStringWithBreak("");
            }
            else
            {
                //btmFZoef0 008 D98961JRBQA0.0PAJH
                Message.AppendInt32(BotAI.BaseId);
                Message.AppendStringWithBreak(BotData.Name);
                Message.AppendStringWithBreak(BotData.Motto);
                Message.AppendStringWithBreak(BotData.Look);
                Message.AppendInt32(VirtualId);
                Message.AppendInt32(X);
                Message.AppendInt32(Y);
                Message.AppendStringWithBreak(Z.ToString().Replace(',', '.'));
                Message.AppendInt32(4);
                Message.AppendInt32((BotData.AiType.ToLower() == "pet") ? 2 : 3);

                if (BotData.AiType.ToLower() == "pet")
                {
                    Message.AppendInt32(0);
                }
            }
        }

        public void SerializeStatus(ServerMessage Message)
        {
            if (IsSpectator)
            {
                return;
            }

            Message.AppendInt32(VirtualId);
            Message.AppendInt32(X);
            Message.AppendInt32(Y);
            Message.AppendStringWithBreak(Z.ToString().Replace(',','.'));
            Message.AppendInt32(RotHead);
            Message.AppendInt32(RotBody);
            Message.AppendString("/");

            foreach (KeyValuePair<string, string> Status in Statusses)
            {
                Message.AppendString(Status.Key);
                Message.AppendString(" ");
                Message.AppendString(Status.Value);
                Message.AppendString("/");
            }

            Message.AppendStringWithBreak("/");
        }

        public GameClient GetClient()
        {
            if (IsBot)
            {
                return null;
            }

            return UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(HabboId);
        }

        private Room GetRoom()
        {
            return UberEnvironment.GetGame().GetRoomManager().GetRoom(RoomId);
        }
    }
}
