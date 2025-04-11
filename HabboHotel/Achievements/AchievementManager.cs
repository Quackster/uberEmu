using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Messages;
using Uber.Storage;
using Uber.HabboHotel.GameClients;
using Uber.HabboHotel.Users.Badges;

namespace Uber.HabboHotel.Achievements
{
    class AchievementManager
    {
        public Dictionary<uint, Achievement> Achievements;

        public AchievementManager()
        {
            this.Achievements = new Dictionary<uint, Achievement>();
        }

        public void LoadAchievements()
        {
            Achievements.Clear();
            DataTable Data = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Data = dbClient.ReadDataTable("SELECT * FROM achievements");
            }

            if (Data == null)
            {
                return;
            }

            foreach (DataRow Row in Data.Rows)
            {
                Achievements.Add((uint)Row["id"], new Achievement((uint)Row["id"], (int)Row["levels"], (string)Row["badge"], (int)Row["pixels_base"], (double)Row["pixels_multiplier"], UberEnvironment.EnumToBool(Row["dynamic_badgelevel"].ToString())));
            }
        }

        public Boolean UserHasAchievement(GameClient Session, uint Id, int MinLevel)
        {
            if (!Session.GetHabbo().Achievements.ContainsKey(Id))
            {
                return false;
            }

            if (Session.GetHabbo().Achievements[Id] >= MinLevel)
            {
                return true;
            }

            return false;
        }

        public ServerMessage SerializeAchievementList(GameClient Session)
        {
            List<Achievement> AchievementsToList = new List<Achievement>();
            Dictionary<uint, int> NextAchievementLevels = new Dictionary<uint, int>();

            lock (Achievements)
            {
                foreach (Achievement Achievement in Achievements.Values)
                {
                    if (!Session.GetHabbo().Achievements.ContainsKey(Achievement.Id))
                    {
                        AchievementsToList.Add(Achievement);
                        NextAchievementLevels.Add(Achievement.Id, 1);
                    }
                    else
                    {
                        if (Session.GetHabbo().Achievements[Achievement.Id] >= Achievement.Levels)
                        {
                            continue;
                        }

                        AchievementsToList.Add(Achievement);
                        NextAchievementLevels.Add(Achievement.Id, Session.GetHabbo().Achievements[Achievement.Id] + 1);
                    }
                }
            }

            ServerMessage Message = new ServerMessage(436);
            Message.AppendInt32(AchievementsToList.Count);

            foreach (Achievement Achievement in AchievementsToList)
            {
                int Level = NextAchievementLevels[Achievement.Id];

                Message.AppendUInt(Achievement.Id);
                Message.AppendInt32(Level);
                Message.AppendStringWithBreak(FormatBadgeCode(Achievement.BadgeCode, Level, Achievement.DynamicBadgeLevel));
            }

            return Message;
        }

        public void UnlockAchievement(GameClient Session, uint AchievementId, int Level)
        {
            // Get the achievement
            Achievement Achievement = Achievements[AchievementId];

            // Make sure the achievement is valid and has not already been unlocked
            if (Achievement == null || UserHasAchievement(Session, Achievement.Id, Level) || Level < 1 || Level > Achievement.Levels)
            {
                return;
            }

            // Calculate the pixel value for this achievement
            int Value = CalculateAchievementValue(Achievement.PixelBase, Achievement.PixelMultiplier, Level);

            // Remove any previous badges for this achievement (old levels)
            lock (Session.GetHabbo().GetBadgeComponent().BadgeList)
            {
                List<string> BadgesToRemove = new List<string>();

                foreach (Badge Badge in Session.GetHabbo().GetBadgeComponent().BadgeList)
                {
                    if (Badge.Code.StartsWith(Achievement.BadgeCode))
                    {
                        BadgesToRemove.Add(Badge.Code);
                    }
                }

                foreach (string Badge in BadgesToRemove)
                {
                    Session.GetHabbo().GetBadgeComponent().RemoveBadge(Badge);
                }
            }

            // Give the user the new badge
            Session.GetHabbo().GetBadgeComponent().GiveBadge(FormatBadgeCode(Achievement.BadgeCode, Level, Achievement.DynamicBadgeLevel), true);

            // Update or set the achievement level for the user
            if (Session.GetHabbo().Achievements.ContainsKey(Achievement.Id))
            {
                Session.GetHabbo().Achievements[Achievement.Id] = Level;

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("UPDATE user_achievements SET achievement_level = '" + Level + "' WHERE user_id = '" + Session.GetHabbo().Id + "' AND achievement_id = '" + Achievement.Id + "' LIMIT 1");
                }
            }
            else
            {
                Session.GetHabbo().Achievements.Add(Achievement.Id, Level);

                using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                {
                    dbClient.ExecuteQuery("INSERT INTO user_achievements (user_id,achievement_id,achievement_level) VALUES ('" + Session.GetHabbo().Id + "','" + Achievement.Id + "','" + Level + "')");
                }
            }

            // Notify the user of the achievement gain
            Session.GetMessageHandler().GetResponse().Init(437);
            Session.GetMessageHandler().GetResponse().AppendUInt(Achievement.Id);
            Session.GetMessageHandler().GetResponse().AppendInt32(Level);
            Session.GetMessageHandler().GetResponse().AppendStringWithBreak(FormatBadgeCode(Achievement.BadgeCode, Level, Achievement.DynamicBadgeLevel));

            if (Level > 1)
            {
                Session.GetMessageHandler().GetResponse().AppendStringWithBreak(FormatBadgeCode(Achievement.BadgeCode, (Level - 1), Achievement.DynamicBadgeLevel));
            }
            else
            {
                Session.GetMessageHandler().GetResponse().AppendStringWithBreak("");
            }

            Session.GetMessageHandler().SendResponse();

            // Give the user the pixels he deserves
            Session.GetHabbo().ActivityPoints += Value;
            Session.GetHabbo().UpdateActivityPointsBalance(true, Value);

            // [13-12-2009 17:03:34 from 62.50.35.181:30000] 
            // FvXXDRB

            // [13-12-2009 17:03:34 from 62.50.35.181:30000] 
            // FuQBIACH_Motto1

            // FuPBJACH_RoomEntry2ACH_RoomEntry1
        }

        public int CalculateAchievementValue(int BaseValue, Double Multiplier, int Level)
        {
            return (BaseValue + (50 * Level));
        }

        public string FormatBadgeCode(string BadgeTemplate, int Level, bool Dyn)
        {
            if (!Dyn)
            {
                return BadgeTemplate;
            }

            return BadgeTemplate + Level;
        }
    }
}
