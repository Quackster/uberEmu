using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.Messages;

namespace Uber.HabboHotel.RoomBots
{
    class GenericBot : BotAI
    {
        private int SpeechTimer;
        private int ActionTimer;

        public GenericBot(int VirtualId)
        {
            this.SpeechTimer = new Random((VirtualId ^ 2) + DateTime.Now.Millisecond).Next(10, 250);
            this.ActionTimer = new Random((VirtualId ^ 2) + DateTime.Now.Millisecond).Next(10, 30);
        }

        public override void OnSelfEnterRoom()
        {
            
        }

        public override void OnSelfLeaveRoom(bool Kicked)
        {
            
        }

        public override void OnUserEnterRoom(Rooms.RoomUser User)
        {
            
        }

        public override void OnUserLeaveRoom(GameClients.GameClient Client)
        {
            
        }

        public override void OnUserSay(Rooms.RoomUser User, string Message)
        {
            if (GetRoom().TileDistance(GetRoomUser().X, GetRoomUser().Y, User.X, User.Y) > 8)
            {
                return;
            }

            BotResponse Response = GetBotData().GetResponse(Message);

            if (Response == null)
            {
                return;
            }

            switch (Response.ResponseType.ToLower())
            {
                case "say":

                    GetRoomUser().Chat(null, Response.ResponseText, false);
                    break;

                case "shout":

                    GetRoomUser().Chat(null, Response.ResponseText, true);
                    break;

                case "whisper":

                    ServerMessage TellMsg = new ServerMessage(25);
                    TellMsg.AppendInt32(GetRoomUser().VirtualId);
                    TellMsg.AppendStringWithBreak(Response.ResponseText);
                    TellMsg.AppendBoolean(false);

                    User.GetClient().SendMessage(TellMsg);
                    break;
            }

            if (Response.ServeId >= 1)
            {
                User.CarryItem(Response.ServeId);
            }
        }

        public override void OnUserShout(Rooms.RoomUser User, string Message)
        {
            if (UberEnvironment.GetRandomNumber(0, 10) >= 5)
            {
                GetRoomUser().Chat(null, "There's no need to shout!", true); // shout nag
            }
        }

        public override void OnTimerTick()
        {
            if (SpeechTimer <= 0)
            {
                if (GetBotData().RandomSpeech.Count > 0)
                {
                    RandomSpeech Speech = GetBotData().GetRandomSpeech();
                    GetRoomUser().Chat(null, Speech.Message, Speech.Shout);
                }

                SpeechTimer = UberEnvironment.GetRandomNumber(10, 300);
            }
            else
            {
                SpeechTimer--;
            }

            if (ActionTimer <= 0)
            {
                int randomX = 0;
                int randomY = 0;

                switch (GetBotData().WalkingMode.ToLower())
                {
                    default:
                    case "stand":

                        // (8) Why is my life so boring?
                        break;

                    case "freeroam":

                        randomX = UberEnvironment.GetRandomNumber(0, GetRoom().Model.MapSizeX);
                        randomY = UberEnvironment.GetRandomNumber(0, GetRoom().Model.MapSizeY);

                        GetRoomUser().MoveTo(randomX, randomY);

                        break;

                    case "specified_range":

                        randomX = UberEnvironment.GetRandomNumber(GetBotData().minX, GetBotData().maxX);
                        randomY = UberEnvironment.GetRandomNumber(GetBotData().minY, GetBotData().maxY);

                        GetRoomUser().MoveTo(randomX, randomY);

                        break;
                }

                ActionTimer = UberEnvironment.GetRandomNumber(1, 30);
            }
            else
            {
                ActionTimer--;
            }
        }
    }
}