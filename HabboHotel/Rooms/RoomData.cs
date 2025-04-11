using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Messages;

namespace Uber.HabboHotel.Rooms
{
    class RoomData
    {
        public uint Id;
        public string Name;
        public string Description;
        public string Type;
        public string Owner;
        public string Password;
        public int State;
        public int Category;
        public int UsersNow;
        public int UsersMax;
        public string ModelName;
        public string CCTs;
        public int Score;
        public List<string> Tags;
        public bool AllowPets;
        public bool AllowPetsEating;
        public bool AllowWalkthrough;
        private RoomIcon myIcon;
        public RoomEvent Event;
        public string Wallpaper;
        public string Floor;
        public string Landscape;

        public Boolean IsPublicRoom
        {
            get
            {
                if (Type.ToLower() == "public")
                {
                    return true;
                }

                return false;
            }
        }

        public RoomIcon Icon
        {
            get
            {
                return myIcon;
            }
        }

        public int TagCount
        {
            get
            {
                return Tags.Count;
            }
        }

        public RoomModel Model
        {
            get
            {
                return UberEnvironment.GetGame().GetRoomManager().GetModel(ModelName);
            }
        }

        public RoomData() { }

        public void FillNull(uint Id)
        {
            this.Id = Id;
            this.Name = "Unknown Room";
            this.Description = "-";
            this.Type = "private";
            this.Owner = "-";
            this.Category = 0;
            this.UsersNow = 0;
            this.UsersMax = 0;
            this.ModelName = "NO_MODEL";
            this.CCTs = "";
            this.Score = 0;
            this.Tags = new List<string>();
            this.AllowPets = true;
            this.AllowPetsEating = false;
            this.AllowWalkthrough = true;
            this.Password = "";
            this.Wallpaper = "0.0";
            this.Floor = "0.0";
            this.Landscape = "0.0";
            this.Event = null;
            this.myIcon = new RoomIcon(1, 1, new Dictionary<int, int>());
        }

        public void Fill(DataRow Row)
        {
            this.Id = (uint)Row["id"];
            this.Name = (string)Row["caption"];
            this.Description = (string)Row["description"];
            this.Type = (string)Row["roomtype"];
            this.Owner = (string)Row["owner"];

            switch (Row["state"].ToString().ToLower())
            {
                case "open":

                    this.State = 0;
                    break;

                case "password":

                    this.State = 2;
                    break;

                case "locked":
                default:

                    this.State = 1;
                    break;
            }

            this.Category = (int)Row["category"];
            this.UsersNow = (int)Row["users_now"];
            this.UsersMax = (int)Row["users_max"];
            this.ModelName = (string)Row["model_name"];
            this.CCTs = (string)Row["public_ccts"];
            this.Score = (int)Row["score"];
            this.Tags = new List<string>();
            this.AllowPets = UberEnvironment.EnumToBool(Row["allow_pets"].ToString());
            this.AllowPetsEating = UberEnvironment.EnumToBool(Row["allow_pets_eat"].ToString());
            this.AllowWalkthrough = UberEnvironment.EnumToBool(Row["allow_walkthrough"].ToString());
            this.Password = (string)Row["password"];
            this.Wallpaper = (string)Row["wallpaper"];
            this.Floor = (string)Row["floor"];
            this.Landscape = (string)Row["landscape"];
            this.Event = null;

            Dictionary<int, int> IconItems = new Dictionary<int,int>();

            if (Row["icon_items"].ToString() != "")
            {
                foreach (string Bit in Row["icon_items"].ToString().Split('|'))
                {
                    IconItems.Add(int.Parse(Bit.Split(',')[0]), int.Parse(Bit.Split(',')[1]));
                }
            }

            this.myIcon = new RoomIcon((int)Row["icon_bg"], (int)Row["icon_fg"], IconItems);

            foreach (string Tag in Row["tags"].ToString().Split(','))
            {
                this.Tags.Add(Tag);
            }
        }

        public void Fill(Room Room)
        {
            this.Id = Room.RoomId;
            this.Name = Room.Name;
            this.Description = Room.Description;
            this.Type = Room.Type;
            this.Owner = Room.Owner;
            this.Category = Room.Category;
            this.State = Room.State;
            this.UsersNow = Room.UsersNow;
            this.UsersMax = Room.UsersMax;
            this.ModelName = Room.ModelName;
            this.CCTs = Room.CCTs;
            this.Score = Room.Score;
            this.Tags = Room.Tags;
            this.AllowPets = Room.AllowPets;
            this.AllowPetsEating = Room.AllowPetsEating;
            this.AllowWalkthrough = Room.AllowWalkthrough;
            this.myIcon = Room.Icon;
            this.Password = Room.Password;
            this.Event = Room.Event;
            this.Wallpaper = Room.Wallpaper;
            this.Floor = Room.Floor;
            this.Landscape = Room.Landscape;
        }

        public void Serialize(ServerMessage Message, Boolean ShowEvents)
        {
            // iC^zGHThe Habbo Tightrope - Entrance SkaterChuIHQFCurrently closed for refurbishment.HIKR]JskaterchumazeQDPBIPAQDI
            // GFHhhbZVHHabbo Staff OfficeLost_WitnessHHQFThe Habbo UK London Office, home to the Habbo Staff.HIX{CPRHHHHII

            Message.AppendUInt(Id);

            if (Event == null || !ShowEvents)
            {
                Message.AppendBoolean(false);
                Message.AppendStringWithBreak(Name);
                Message.AppendStringWithBreak(Owner);
                Message.AppendInt32(State); // room state
                Message.AppendInt32(UsersNow);
                Message.AppendInt32(UsersMax);
                Message.AppendStringWithBreak(Description);
                Message.AppendBoolean(true); // dunno!
                Message.AppendBoolean(true); // can trade?
                Message.AppendInt32(Score);
                Message.AppendInt32(Category);
                Message.AppendStringWithBreak("");
                Message.AppendInt32(TagCount);

                lock (Tags)
                {
                    foreach (string Tag in Tags)
                    {
                        Message.AppendStringWithBreak(Tag);
                    }
                }
            }
            else
            {
                Message.AppendBoolean(true);
                Message.AppendStringWithBreak(Event.Name);
                Message.AppendStringWithBreak(Owner);
                Message.AppendInt32(State);
                Message.AppendInt32(UsersNow);
                Message.AppendInt32(UsersMax);
                Message.AppendStringWithBreak(Event.Description);
                Message.AppendBoolean(true);
                Message.AppendBoolean(true);
                Message.AppendInt32(Score);
                Message.AppendInt32(Event.Category);
                Message.AppendStringWithBreak(Event.StartTime);
                Message.AppendInt32(Event.Tags.Count);

                lock (Event.Tags)
                {
                    foreach (string Tag in Event.Tags)
                    {
                        Message.AppendStringWithBreak(Tag);
                    }
                }
            }

            Icon.Serialize(Message);

            Message.AppendBoolean(true);
            //Message.AppendBoolean(false);
        }
    }
}
