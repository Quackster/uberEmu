using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Messages;
using Uber.HabboHotel.Pets;
using Uber.HabboHotel.GameClients;
using Uber.HabboHotel.Items;
using Uber.Storage;

namespace Uber.HabboHotel.Users.Inventory
{
    class InventoryComponent
    {
        private List<UserItem> InventoryItems;
        private List<Pet> InventoryPets;
        public uint UserId;

        public int ItemCount
        {
            get
            {
                return InventoryItems.Count;
            }
        }

        public int PetCount
        {
            get
            {
                return InventoryPets.Count;
            }
        }

        public InventoryComponent(uint UserId)
        {
            this.UserId = UserId;
            this.InventoryItems = new List<UserItem>();
            this.InventoryPets = new List<Pet>();
        }

        public void ClearItems()
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("userid", UserId);
                dbClient.ExecuteQuery("DELETE FROM user_items WHERE user_id = @userid");
            }

            UpdateItems(true);
        }

        public void ClearPets()
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("userid", UserId);
                dbClient.ExecuteQuery("DELETE FROM user_pets WHERE user_id = @userid AND room_id = 0");
            }

            UpdatePets(true);
        }

        public void LoadInventory()
        {
            lock (this.InventoryItems)
            {
                this.InventoryItems.Clear();
                DataTable Data = null;

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.AddParamWithValue("userid", UserId);
                    Data = dbClient.ReadDataTable("SELECT id,base_item,extra_data FROM user_items WHERE user_id = @userid");
                }

                if (Data != null)
                {
                    foreach (DataRow Row in Data.Rows)
                    {
                        InventoryItems.Add(new UserItem((uint)Row["id"], (uint)Row["base_item"], (string)Row["extra_data"]));
                    }
                }
            }

            lock (this.InventoryPets)
            {
                this.InventoryPets.Clear();
                DataTable Data = null;

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.AddParamWithValue("userid", UserId);
                    Data = dbClient.ReadDataTable("SELECT * FROM user_pets WHERE user_id = @userid AND room_id <= 0");
                }

                if (Data != null)
                {
                    foreach (DataRow Row in Data.Rows)
                    {
                        InventoryPets.Add(UberEnvironment.GetGame().GetCatalog().GeneratePetFromRow(Row));
                    }
                }
            }
        }

        public void UpdateItems(bool FromDatabase)
        {
            if (FromDatabase)
            {
                LoadInventory();
            }

            GetClient().GetMessageHandler().GetResponse().Init(101);
            GetClient().GetMessageHandler().SendResponse();
        }

        public void UpdatePets(bool FromDatabase)
        {
            if (FromDatabase)
            {
                LoadInventory();
            }

            GetClient().SendMessage(SerializePetInventory());
        }

        public Pet GetPet(uint Id)
        {
            lock (this.InventoryPets)
            {
                List<Pet>.Enumerator Pets = this.InventoryPets.GetEnumerator();

                while (Pets.MoveNext())
                {
                    Pet Pet = Pets.Current;

                    if (Pet.PetId == Id)
                    {
                        return Pet;
                    }
                }
            }

            return null;
        }

        public UserItem GetItem(uint Id)
        {
            lock (this.InventoryItems)
            {
                List<UserItem>.Enumerator Items = this.InventoryItems.GetEnumerator();

                while (Items.MoveNext())
                {
                    UserItem Item = Items.Current;

                    if (Item.Id == Id)
                    {
                        return Item;
                    }
                }
            }

            return null;
        }

        public void AddItem(uint Id, uint BaseItem, string ExtraData)
        {
            lock (this.InventoryItems)
            {
                InventoryItems.Add(new UserItem(Id, BaseItem, ExtraData));

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.AddParamWithValue("extra_data", ExtraData);
                    dbClient.ExecuteQuery("INSERT INTO user_items (id,user_id,base_item,extra_data) VALUES ('" + Id + "','" + UserId + "','" + BaseItem + "',@extra_data)");
                }
            }
        }

        public void AddPet(Pet Pet)
        {
            if (Pet == null)
            {
                return;
            }

            Pet.PlacedInRoom = false;

            InventoryPets.Add(Pet);

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("botid", Pet.PetId);
                dbClient.ExecuteQuery("UPDATE user_pets SET room_id = 0, x = 0, y = 0, z = 0 WHERE id = @botid LIMIT 1");
            }

            ServerMessage AddMessage = new ServerMessage(603);
            Pet.SerializeInventory(AddMessage);
            GetClient().SendMessage(AddMessage);
        }

        public bool RemovePet(uint PetId)
        {
            lock (this.InventoryPets)
            {
                foreach (Pet Pet in this.InventoryPets)
                {
                    if (Pet.PetId != PetId)
                    {
                        continue;
                    }

                    this.InventoryPets.Remove(Pet);

                    ServerMessage RemoveMessage = new ServerMessage(604);
                    RemoveMessage.AppendUInt(PetId);
                    GetClient().SendMessage(RemoveMessage);

                    return true;
                }
            }

            return false;
        }

        public void MovePetToRoom(uint PetId, uint RoomId)
        {
            if (RemovePet(PetId))
            {
                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.AddParamWithValue("roomid", RoomId);
                    dbClient.AddParamWithValue("petid", PetId);
                    dbClient.ExecuteQuery("UPDATE user_pets SET room_id = @roomid, x = 0, y = 0, z = 0 WHERE id = @petid LIMIT 1");
                }
            }
        }

        public void RemoveItem(uint Id)
        {
            GetClient().GetMessageHandler().GetResponse().Init(99);
            GetClient().GetMessageHandler().GetResponse().AppendUInt(Id);
            GetClient().GetMessageHandler().SendResponse();

            lock (this.InventoryItems)
            {
                InventoryItems.Remove(GetItem(Id));

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("DELETE FROM user_items WHERE id = '" + Id + "' LIMIT 1");
                }
            }
        }

        public ServerMessage SerializeItemInventory()
        {
            ServerMessage Message = new ServerMessage(140);
            Message.AppendInt32(this.ItemCount);

            lock (this.InventoryItems)
            {
                List<UserItem>.Enumerator eItems = this.InventoryItems.GetEnumerator();

                while (eItems.MoveNext())
                {
                    eItems.Current.Serialize(Message, true);
                }
            }

            Message.AppendInt32(this.ItemCount);
            return Message;
        }

        public ServerMessage SerializePetInventory()
        {
            ServerMessage Message = new ServerMessage(600);
            Message.AppendInt32(InventoryPets.Count);

            foreach (Pet Pet in InventoryPets)
            {
                Pet.SerializeInventory(Message);
            }

            return Message;
        }

        private GameClient GetClient()
        {
            return UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);
        }
    }
}
