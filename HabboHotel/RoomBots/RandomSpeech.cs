using System;

namespace Uber.HabboHotel.RoomBots
{
    class RandomSpeech
    {
        public string Message;
        public bool Shout;

        public RandomSpeech(string Message, bool Shout)
        {
            this.Message = Message;
            this.Shout = Shout;
        }
    }
}
