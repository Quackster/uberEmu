using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Storage;

namespace Uber.HabboHotel.Items
{
    class MoodlightData
    {
        public Boolean Enabled;
        public int CurrentPreset;
        public List<MoodlightPreset> Presets;

        public uint ItemId;

        public MoodlightData(uint ItemId)
        {
            this.ItemId = ItemId;

            DataRow Row = null;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                Row = dbClient.ReadDataRow("SELECT enabled,current_preset,preset_one,preset_two,preset_three FROM room_items_moodlight WHERE item_id = '" + ItemId + "' LIMIT 1");
            }

            if (Row == null)
            {
                throw new ArgumentException();
            }

            this.Enabled = UberEnvironment.EnumToBool(Row["enabled"].ToString());
            this.CurrentPreset = (int)Row["current_preset"];
            this.Presets = new List<MoodlightPreset>();

            this.Presets.Add(GeneratePreset((string)Row["preset_one"]));
            this.Presets.Add(GeneratePreset((string)Row["preset_two"]));
            this.Presets.Add(GeneratePreset((string)Row["preset_three"]));
        }

        public void Enable()
        {
            this.Enabled = true;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("UPDATE room_items_moodlight SET enabled = '1' WHERE item_id = '" + ItemId + "' LIMIT 1");
            }
        }

        public void Disable()
        {
            this.Enabled = false;

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("UPDATE room_items_moodlight SET enabled = '0' WHERE item_id = '" + ItemId + "' LIMIT 1");
            }
        }

        public void UpdatePreset(int Preset, string Color, int Intensity, bool BgOnly)
        {
            if (!IsValidColor(Color) || !IsValidIntensity(Intensity))
            {
                return;
            }

            string Pr;

            switch (Preset)
            {
                case 3:

                    Pr = "three";
                    break;

                case 2:

                    Pr = "two";
                    break;

                case 1:
                default:

                    Pr = "one";
                    break;
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.ExecuteQuery("UPDATE room_items_moodlight SET preset_" + Pr + " = '" + Color + "," + Intensity + "," + UberEnvironment.BoolToEnum(BgOnly) + "' WHERE item_id = '" + ItemId + "' LIMIT 1");
            }

            GetPreset(Preset).ColorCode = Color;
            GetPreset(Preset).ColorIntensity = Intensity;
            GetPreset(Preset).BackgroundOnly = BgOnly;
        }

        public MoodlightPreset GeneratePreset(string Data)
        {
            String[] Bits = Data.Split(',');

            if (!IsValidColor(Bits[0]))
            {
                Bits[0] = "#000000";
            }

            return new MoodlightPreset(Bits[0], int.Parse(Bits[1]), UberEnvironment.EnumToBool(Bits[2]));
        }

        public MoodlightPreset GetPreset(int i)
        {
            i--;

            if (Presets[i] != null)
            {
                return Presets[i];
            }

            return new MoodlightPreset("#000000", 255, false);
        }

        public bool IsValidColor(string ColorCode)
        {
            switch (ColorCode)
            {
                case "#000000":
                case "#0053F7":
                case "#EA4532":
                case "#82F349":
                case "#74F5F5":
                case "#E759DE":
                case "#F2F851":

                    return true;

                default:

                    return false;
            }
        }

        public bool IsValidIntensity(int Intensity)
        {
            if (Intensity < 0 || Intensity > 255)
            {
                return false;
            }

            return true;
        }

        public string GenerateExtraData()
        {
            MoodlightPreset Preset = GetPreset(CurrentPreset);
            StringBuilder SB = new StringBuilder();

            if (Enabled)
            {
                SB.Append(2);
            }
            else
            {
                SB.Append(1);
            }

            SB.Append(",");
            SB.Append(CurrentPreset);
            SB.Append(",");

            if (Preset.BackgroundOnly)
            {
                SB.Append(2);
            }
            else
            {
                SB.Append(1);
            }

            SB.Append(",");
            SB.Append(Preset.ColorCode);
            SB.Append(",");
            SB.Append(Preset.ColorIntensity);
            return SB.ToString();
        }
    }

    class MoodlightPreset
    {
        public string ColorCode;
        public int ColorIntensity;
        public bool BackgroundOnly;

        public MoodlightPreset(string ColorCode, int ColorIntensity, bool BackgroundOnly)
        {
            this.ColorCode = ColorCode;
            this.ColorIntensity = ColorIntensity;
            this.BackgroundOnly = BackgroundOnly;
        }
    }
}
