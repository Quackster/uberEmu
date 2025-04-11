using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.HabboHotel.Rooms;
using Uber.HabboHotel.GameClients;
using Uber.Storage;
using Uber.Messages;

namespace Uber.HabboHotel.Users.Inventory
{
    class AvatarEffectsInventoryComponent
    {
        private List<AvatarEffect> Effects;
        private uint UserId;
        public int CurrentEffect;

        public int Count
        {
            get
            {
                return Effects.Count;
            }
        }

        public AvatarEffectsInventoryComponent(uint UserId)
        {
            this.Effects = new List<AvatarEffect>();
            this.UserId = UserId;
            this.CurrentEffect = -1;
        }

        public void LoadEffects()
        {
            this.Effects.Clear();

            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT * FROM user_effects WHERE user_id = '" + UserId + "'");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                AvatarEffect Effect = new AvatarEffect((int)Row["effect_id"], (int)Row["total_duration"], UberEnvironment.EnumToBool(Row["is_activated"].ToString()), (double)Row["activated_stamp"]);

                if (Effect.HasExpired)
                {
                    using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                    {
                        dbClient.ExecuteQuery("DELETE FROM user_effects WHERE user_id = '" + UserId + "' AND effect_id = '" + Effect.EffectId + "' LIMIT 1");
                    }

                    continue;
                }

                Effects.Add(Effect);
            }
        }

        public void AddEffect(int EffectId, int Duration)
        {
            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("INSERT INTO user_effects (user_id,effect_id,total_duration,is_activated,activated_stamp) VALUES ('" + UserId + "','" + EffectId + "','" + Duration + "','0','0')");
            }

            Effects.Add(new AvatarEffect(EffectId, Duration, false, 0));

            GetClient().GetMessageHandler().GetResponse().Init(461);
            GetClient().GetMessageHandler().GetResponse().AppendInt32(EffectId);
            GetClient().GetMessageHandler().GetResponse().AppendInt32(Duration);
            GetClient().GetMessageHandler().SendResponse();
        }

        public void StopEffect(int EffectId)
        {
            AvatarEffect Effect = GetEffect(EffectId, true);

            if (Effect == null || !Effect.HasExpired)
            {
                return;
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("DELETE FROM user_effects WHERE user_id = '" + UserId + "' AND effect_id = '" + EffectId + "' AND is_activated = '1' LIMIT 1");
            }

            Effects.Remove(Effect);

            GetClient().GetMessageHandler().GetResponse().Init(463);
            GetClient().GetMessageHandler().GetResponse().AppendInt32(EffectId);
            GetClient().GetMessageHandler().SendResponse();

            if (CurrentEffect >= 0)
            {
                ApplyEffect(-1);
            }
        }

        public void ApplyEffect(int EffectId)
        {
            if (!HasEffect(EffectId, true))
            {
                return;
            }

            Room Room = GetUserRoom();

            if (Room == null)
            {
                return;
            }

            RoomUser User = Room.GetRoomUserByHabbo(GetClient().GetHabbo().Id);

            if (User == null)
            {
                return;
            }

            CurrentEffect = EffectId;

            ServerMessage Message = new ServerMessage(485);
            Message.AppendInt32(User.VirtualId);
            Message.AppendInt32(EffectId);
            Room.SendMessage(Message);
        }

        public void EnableEffect(int EffectId)
        {
            AvatarEffect Effect = GetEffect(EffectId, false);

            if (Effect == null || Effect.HasExpired || Effect.Activated)
            {
                return;
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("UPDATE user_effects SET is_activated = '1', activated_stamp = '" + UberEnvironment.GetUnixTimestamp() + "' WHERE user_id = '" + UserId + "' AND effect_id = '" + EffectId + "' LIMIT 1");
            }

            Effect.Activate();

            GetClient().GetMessageHandler().GetResponse().Init(462);
            GetClient().GetMessageHandler().GetResponse().AppendInt32(Effect.EffectId);
            GetClient().GetMessageHandler().GetResponse().AppendInt32(Effect.TotalDuration);
            GetClient().GetMessageHandler().SendResponse();
        }

        public bool HasEffect(int EffectId, bool IfEnabledOnly)
        {
            if (EffectId == -1)
            {
                return true;
            }

            lock (Effects)
            {
                foreach (AvatarEffect Effect in Effects)
                {
                    if (IfEnabledOnly && !Effect.Activated)
                    {
                        continue;
                    }

                    if (Effect.HasExpired)
                    {
                        continue;
                    }

                    if (Effect.EffectId == EffectId)
                    {
                        return true;
                    }
                }
            }

            return false;
        }

        public AvatarEffect GetEffect(int EffectId, bool IfEnabledOnly)
        {
            lock (Effects)
            {
                foreach (AvatarEffect Effect in Effects)
                {
                    if (IfEnabledOnly && !Effect.Activated)
                    {
                        continue;
                    }

                    if (Effect.EffectId == EffectId)
                    {
                        return Effect;
                    }
                }
            }

            return null;
        }

        public ServerMessage Serialize()
        {
            ServerMessage Message = new ServerMessage(460);
            Message.AppendInt32(Count);

            lock (Effects)
            {
                foreach (AvatarEffect Effect in Effects)
                {
                    Message.AppendInt32(Effect.EffectId);
                    Message.AppendInt32(Effect.TotalDuration);
                    Message.AppendBoolean(!Effect.Activated);
                    Message.AppendInt32(Effect.TimeLeft);
                }
            }

            return Message;
        }

        public void CheckExpired()
        {
            lock (Effects)
            {
                List<int> ToRemove = new List<int>();

                foreach (AvatarEffect Effect in Effects)
                {
                    if (Effect.HasExpired)
                    {
                        ToRemove.Add(Effect.EffectId);
                    }
                }

                foreach (int trmv in ToRemove)
                {
                    StopEffect(trmv);
                }
            }
        }

        private GameClient GetClient()
        {
            return UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);
        }

        private Room GetUserRoom()
        {
            return UberEnvironment.GetGame().GetRoomManager().GetRoom(GetClient().GetHabbo().CurrentRoomId);
        }
    }
}
