using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.Support
{
    class HelpTopic
    {
        private uint Id;

        public string Caption;
        public string Body;

        public uint CategoryId;

        public uint TopicId
        {
            get
            {
                return Id;
            }
        }

        public HelpTopic(uint Id, string Caption, string Body, uint CategoryId)
        {
            this.Id = Id;
            this.Caption = Caption;
            this.Body = Body;
            this.CategoryId = CategoryId;
        }
    }
}
