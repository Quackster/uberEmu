using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.RoomBots
{
    class BotResponse
    {
        private uint Id;
        public uint BotId;

        public List<string> Keywords;

        public string ResponseText;
        public string ResponseType;

        public int ServeId;

        public BotResponse(uint Id, uint BotId, string Keywords, string ResponseText, string ResponseType, int ServeId)
        {
            this.Id = Id;
            this.BotId = BotId;
            this.Keywords = new List<string>();
            this.ResponseText = ResponseText;
            this.ResponseType = ResponseType;
            this.ServeId = ServeId;

            foreach (string Keyword in Keywords.Split(';'))
            {
                this.Keywords.Add(Keyword.ToLower());
            }
        }

        public bool KeywordMatched(string Message)
        {
            lock (Keywords)
            {
                foreach (string Keyword in Keywords)
                {
                    if (Message.ToLower().Contains(Keyword.ToLower()))
                    {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
