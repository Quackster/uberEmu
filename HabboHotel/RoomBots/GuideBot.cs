using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.Messages;

namespace Uber.HabboHotel.RoomBots
{
    class GuideBot : BotAI
    {
        private int SpeechTimer;
        private int ActionTimer;

        public GuideBot()
        {
            this.SpeechTimer = 0;
            this.ActionTimer = 0;
        }

        public override void OnSelfEnterRoom()
        {
            GetRoomUser().Chat(null, "Hi and welcome to Uber! I am a bot Guide and I'm here to help you.", false);
            GetRoomUser().Chat(null, "This is your own room, you can always come back to room by clicking the nest icon on the left.", false);
            GetRoomUser().Chat(null, "If you want to explore the Habbo by yourself, click on the orange hotel icon on the left (we call it navigator).", false);
            GetRoomUser().Chat(null, "You will find cool rooms and fun events with other people in them, feel free to visit them.", false);
            GetRoomUser().Chat(null, "I can give you tips and hints on what to do here, just ask me a question :)", false);
        }

        public override void OnSelfLeaveRoom(bool Kicked) { }
        public override void OnUserEnterRoom(Rooms.RoomUser User) { }

        public override void OnUserLeaveRoom(GameClients.GameClient Client)
        {
            if (GetRoom().Owner.ToLower() == Client.GetHabbo().Username.ToLower())
            {
                GetRoom().RemoveBot(GetRoomUser().VirtualId, false);
            }
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

        public override void OnUserShout(Rooms.RoomUser User, string Message) { }

        public override void OnTimerTick()
        {
            if (SpeechTimer <= 0)
            {
                if (GetBotData().RandomSpeech.Count > 0)
                {
                    RandomSpeech Speech = GetBotData().GetRandomSpeech();
                    GetRoomUser().Chat(null, Speech.Message, Speech.Shout);
                }

                SpeechTimer = UberEnvironment.GetRandomNumber(0, 150);
            }
            else
            {
                SpeechTimer--;
            }

            if (ActionTimer <= 0)
            {
                int randomX = UberEnvironment.GetRandomNumber(0, GetRoom().Model.MapSizeX);
                int randomY = UberEnvironment.GetRandomNumber(0, GetRoom().Model.MapSizeY);

                GetRoomUser().MoveTo(randomX, randomY);

                ActionTimer = UberEnvironment.GetRandomNumber(0, 30);
            }
            else
            {
                ActionTimer--;
            }
        }
    }
}
