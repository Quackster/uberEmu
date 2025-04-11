using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.HabboHotel.Rooms;
using Uber.Messages;
using Uber.Storage;

namespace Uber.HabboHotel.Pets
{
    class Pet
    {
        public uint PetId;
        public uint OwnerId;
        public int VirtualId;

        public uint Type;
        public string Name;
        public string Race;
        public string Color;

        public int Expirience;
        public int Energy;
        public int Nutrition;

        public uint RoomId;
        public int X;
        public int Y;
        public double Z;

        public int Respect;

        public double CreationStamp;
        public bool PlacedInRoom;

        public Room Room
        {
            get
            {
                if (!IsInRoom)
                {
                    return null;
                }

                return UberEnvironment.GetGame().GetRoomManager().GetRoom(RoomId);
            }
        }

        public bool IsInRoom
        {
            get
            {
                return (RoomId > 0);
            }
        }

        public int Level
        {
            get
            {
                return 1;
            }
        }

        public int MaxLevel
        {
            get
            {
                return 20;
            }
        }

        public int ExpirienceGoal
        {
            get
            {
                return 100;
            }
        }

        public int MaxEnergy
        {
            get
            {
                return 100;
            }
        }

        public int MaxNutrition
        {
            get
            {
                return 150;
            }
        }

        public int Age
        {
            get
            {
                return (int)Math.Floor((UberEnvironment.GetUnixTimestamp() - CreationStamp) / 86400);
            }
        }

        public string Look
        {
            get
            {
                return Type + " " + Race + " " + Color;
            }
        }

        public string OwnerName
        {
            get
            {
                return UberEnvironment.GetGame().GetClientManager().GetNameById(OwnerId);
            }
        }

        public Pet(uint PetId, uint OwnerId, uint RoomId, string Name, uint Type, string Race, string Color, int Expirience, int Energy, int Nutrition, int Respect, double CreationStamp, int X, int Y, double Z)
        {
            this.PetId = PetId;
            this.OwnerId = OwnerId;
            this.RoomId = RoomId;
            this.Name = Name;
            this.Type = Type;
            this.Race = Race;
            this.Color = Color;
            this.Expirience = Expirience;
            this.Energy = Energy;
            this.Nutrition = Nutrition;
            this.Respect = Respect;
            this.CreationStamp = CreationStamp;
            this.X = X;
            this.Y = Y;
            this.Z = Z;
            this.PlacedInRoom = false;
        }

        public void OnRespect()
        {
            Respect++;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("petid", PetId);
                dbClient.ExecuteQuery("UPDATE user_pets SET respect = respect + 1 WHERE id = @petid LIMIT 1");
            }

            AddExpirience(10);
        }

        public void AddExpirience(int Amount)
        {
            Expirience += Amount;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("petid", PetId);
                dbClient.AddParamWithValue("expirience", Expirience);
                dbClient.ExecuteQuery("UPDATE user_pets SET expirience = @expirience WHERE id = @petid LIMIT 1");
            }

            if (Room != null)
            {
                ServerMessage Message = new ServerMessage(609);
                Message.AppendUInt(PetId);
                Message.AppendInt32(VirtualId);
                Message.AppendInt32(Amount);
                Room.SendMessage(Message);
            }

            if (Expirience > ExpirienceGoal)
            {
                // level up
            }
        }

        public void SerializeInventory(ServerMessage Message)
        {
            Message.AppendUInt(PetId);
            Message.AppendStringWithBreak(Name);
            Message.AppendStringWithBreak(Look);
            Message.AppendBoolean(false);
        }

        public ServerMessage SerializeInfo()
        {
            // IYbtmFZoefKPEY]AXdAPhPhHPh0 008 D98961SBhRPZA[lFmybad
            ServerMessage Nfo = new ServerMessage(601);
            Nfo.AppendUInt(PetId);
            Nfo.AppendStringWithBreak(Name);
            Nfo.AppendInt32(Level);
            Nfo.AppendInt32(MaxLevel);
            Nfo.AppendInt32(Expirience);
            Nfo.AppendInt32(ExpirienceGoal);
            Nfo.AppendInt32(Energy);
            Nfo.AppendInt32(MaxEnergy);
            Nfo.AppendInt32(Nutrition);
            Nfo.AppendInt32(MaxNutrition);
            Nfo.AppendStringWithBreak(Look);
            Nfo.AppendInt32(Respect);
            Nfo.AppendUInt(OwnerId);
            Nfo.AppendInt32(Age);
            Nfo.AppendStringWithBreak(OwnerName);
            return Nfo;
        }
    }
}
