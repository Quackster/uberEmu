using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Data;

using Uber.Storage;

namespace Uber.HabboHotel.Users.Authenticator
{
    class Authenticator
    {
        public static Habbo TryLoginHabbo(string AuthTicket)
        {
            DataRow Row = null;

            if (AuthTicket.Length < 10)
            {
                throw new IncorrectLoginException("Invalid authorization/SSO ticket");
            }

            using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
            {
                dbClient.AddParamWithValue("auth_ticket", AuthTicket);
                Row = dbClient.ReadDataRow("SELECT * FROM users WHERE auth_ticket = @auth_ticket LIMIT 1");
            }

            if (Row == null)
            {
                throw new IncorrectLoginException("Invalid authorization/SSO ticket");
            }

            if (!UberEnvironment.GetGame().GetRoleManager().RankHasRight((uint)Row["rank"], "fuse_login"))
            {
                throw new IncorrectLoginException("Not permitted to log in due to role/right restriction (fuse_login is missing)");
            }

            if (Row["newbie_status"].ToString() == "0")
            {
                throw new IncorrectLoginException("Not permitted to log in; you are still a noob.");
            }

            return GenerateHabbo(Row, AuthTicket);
        }

        public static Habbo GenerateHabbo(DataRow Data, string AuthTicket)
        {
            return new Habbo((uint)Data["id"], (string)Data["username"], (string)Data["real_name"], AuthTicket, (uint)Data["rank"], (string)Data["motto"], (string)Data["look"], (string)Data["gender"], (int)Data["credits"], (int)Data["activity_points"], (Double)Data["activity_points_lastupdate"], UberEnvironment.EnumToBool(Data["is_muted"].ToString()), (uint)Data["home_room"], (int)Data["respect"], (int)Data["daily_respect_points"], (int)Data["daily_pet_respect_points"], (int)Data["newbie_status"], (Data["mutant_penalty"].ToString() != "0"), UberEnvironment.EnumToBool(Data["block_newfriends"].ToString()));
        }
    }
}
