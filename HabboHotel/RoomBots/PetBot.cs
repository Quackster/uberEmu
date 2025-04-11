using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.Messages;
using Uber.HabboHotel.Pathfinding;

namespace Uber.HabboHotel.RoomBots
{
    class PetBot : BotAI
    {
        private int SpeechTimer;
        private int ActionTimer;

        public PetBot(int VirtualId)
        {
            this.SpeechTimer = new Random((VirtualId ^ 2) + DateTime.Now.Millisecond).Next(10, 250);
            this.ActionTimer = new Random((VirtualId ^ 2) + DateTime.Now.Millisecond).Next(10, 30);
        }

        public override void OnSelfEnterRoom()
        {
            GetRoomUser().Chat(null, "*drool over master*", false);
        }

        public override void OnSelfLeaveRoom(bool Kicked)
        {
            
        }

        public override void OnUserEnterRoom(Rooms.RoomUser User)
        {
            if (User.GetClient().GetHabbo().Username.ToLower() == GetRoomUser().PetData.OwnerName.ToLower())
            {
                GetRoomUser().Chat(null, "*drool over master*", false);
            }
        }

        public override void OnUserLeaveRoom(GameClients.GameClient Client)
        {
            
        }

        public override void OnUserSay(Rooms.RoomUser User, string Message)
        {
            if (Message.ToLower().Equals(GetRoomUser().PetData.Name.ToLower()))
            {
                GetRoomUser().SetRot(Rotation.Calculate(GetRoomUser().X, GetRoomUser().Y, User.X, User.Y));
                return;
            }

            if (Message.ToLower().StartsWith(GetRoomUser().PetData.Name.ToLower() + " "))
            {
                string Command = Message.Substring(GetRoomUser().PetData.Name.ToLower().Length + 1);
                GetRoomUser().Chat(null, "*confused*", false);
            }
        }

        public override void OnUserShout(Rooms.RoomUser User, string Message)
        {

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
                int randomX = UberEnvironment.GetRandomNumber(0, GetRoom().Model.MapSizeX);
                int randomY = UberEnvironment.GetRandomNumber(0, GetRoom().Model.MapSizeY);
                GetRoomUser().MoveTo(randomX, randomY);

                ActionTimer = UberEnvironment.GetRandomNumber(1, 30);
            }
            else
            {
                ActionTimer--;
            }
        }
    }
}