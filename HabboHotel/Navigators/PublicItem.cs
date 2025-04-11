using System;

using Uber.Messages;
using Uber.HabboHotel.Rooms;

namespace Uber.HabboHotel.Navigators
{
    enum PublicImageType
    {
        INTERNAL = 0,
        EXTERNAL = 1
    }

    class PublicItem
    {
        private int BannerId;

        public int Type;

        public string Caption;
        public string Image;
        public PublicImageType ImageType;

        public uint RoomId;
        public int CategoryId;

        public int ParentId;

        public int Id
        {
            get
            {
                return BannerId;
            }
        }

        public Boolean IsCategory
        {
            get
            {
                if (CategoryId > 0)
                {
                    return true;
                }

                return false;
            }
        }

        public RoomData RoomData
        {
            get
            {
                if (IsCategory)
                {
                    return new RoomData();
                }

                return UberEnvironment.GetGame().GetRoomManager().GenerateRoomData(RoomId);
            }
        }

        public PublicItem(int Id, int Type, string Caption, string Image, PublicImageType ImageType, uint RoomId, int CategoryId, int ParentId)
        {
            this.BannerId = Id;
            this.Type = Type;
            this.Caption = Caption;
            this.Image = Image;
            this.ImageType = ImageType;
            this.RoomId = RoomId;
            this.CategoryId = CategoryId;
            this.ParentId = ParentId;
        }

        public void Serialize(ServerMessage Message)
        {
            Message.AppendInt32(Id);

            if (IsCategory)
            {
                Message.AppendStringWithBreak(Caption);
            }
            else
            {
                Message.AppendStringWithBreak(RoomData.Name);
            }

            Message.AppendStringWithBreak(RoomData.Description);
            Message.AppendInt32(Type);
            Message.AppendStringWithBreak(Caption);
            Message.AppendStringWithBreak((ImageType == PublicImageType.EXTERNAL) ? Image : "");

            if (!IsCategory)
            {
                Message.AppendUInt(0);
                Message.AppendInt32(RoomData.UsersNow);
                Message.AppendInt32(3);
                Message.AppendStringWithBreak((ImageType == PublicImageType.INTERNAL) ? Image : "");
                Message.AppendUInt(1337);
                Message.AppendInt32(0);
                Message.AppendStringWithBreak(RoomData.CCTs);
                Message.AppendInt32(RoomData.UsersMax);
                Message.AppendUInt(RoomId);
            }
            else
            {
                Message.AppendInt32(0);
                Message.AppendInt32(4);
                Message.AppendInt32(CategoryId);
            }
        }
    }
}
