using System;
using System.Collections.Generic;
using System.Text;
using System.Data;

using Uber.Messages;
using Uber.HabboHotel.GameClients;
using Uber.Storage;

namespace Uber.HabboHotel.Users.Messenger
{
    class MessengerBuddy
    {
        private uint UserId;

        public Boolean UpdateNeeded;

        public uint Id
        {
            get
            {
                return UserId;
            }
        }

        public string Username
        {
            get
            {
                GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);

                if (Client != null)
                {
                    return Client.GetHabbo().Username;
                }
                else
                {
                    using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                    {
                        return dbClient.ReadString("SELECT username FROM users WHERE id = '" + UserId + "' LIMIT 1");
                    }
                }
            }
        }

        public string RealName
        {
            get
            {
                GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);

                if (Client != null)
                {
                    return Client.GetHabbo().RealName;
                }
                else
                {
                    using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                    {
                        return dbClient.ReadString("SELECT real_name FROM users WHERE id = '" + UserId + "' LIMIT 1");
                    }
                }
            }
        }

        public string Look
        {
            get
            {
                GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);

                if (Client != null)
                {
                    return Client.GetHabbo().Look;
                }

                return "";
            }
        }

        public string Motto
        {
            get
            {
                GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);

                if (Client != null)
                {
                    return Client.GetHabbo().Motto;
                }

                return "";
            }
        }

        public string LastOnline
        {
            get
            {
                GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);

                if (Client == null)
                {
                    using (DatabaseClient dbClient = UberEnvironment.GetDatabase().GetClient())
                    {
                        return dbClient.ReadString("SELECT last_online FROM users WHERE id = '" + UserId + "' LIMIT 1");
                    }
                }

                return "";
            }
        }

        public Boolean IsOnline
        {
            get
            {
                GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);

                if (Client != null && !Client.GetHabbo().GetMessenger().AppearOffline)
                {
                    return true;
                }

                return false;
            }
        }

        public Boolean InRoom
        {
            get
            {
                if (!IsOnline)
                {
                    return false;
                }

                GameClient Client = UberEnvironment.GetGame().GetClientManager().GetClientByHabbo(UserId);

                if (Client == null)
                {
                    return false;
                }

                if (Client.GetHabbo().InRoom)
                {
                    return true;
                }

                return false;
            }
        }

        public MessengerBuddy(uint UserId)
        {
            this.UserId = UserId;
            this.UpdateNeeded = false;
        }

        public void Serialize(ServerMessage Message, Boolean Search)
        {
            /*Fs
             * H
             * RG
             * 
             * hCNZ
             * name
             * motto
             * H
             * H
             * blank
             * I
             * look
             * laston
             * hBDCRbobbya12HHIkP`wUbobbya12174HHIjiu@Pbobbya123HHIhmT\Xbobbya1234HHIhkLLUbobbya12345HHIkDaXPbobbyaanlandHHIj|yHQbobbyabcHHIiSklTbobbyabcdefHHIhGkIQBobbyAbreuHHIjIo`Ybobbyace123HHHkVemUbobbyacebestHHIibWTFbobbyackda cool 1!!!!!!!!HHIiuQ^SBobbyage13HHIjZTsPBobbyaid1HHIkyVOUbobbyakabobHHIjQoUTBOBBYANDGREGHHIiHqJFbobbyandkurtMutant:Snow.(Blizzard).MMHHIhe\yObobbyandro1HHIhA[qXbobbyandsamHHIiWAjQBobbyAndyHHIjcKwWbobbyangleHHHhsvOXBobbyanna[RAF] Leading AircraftmanHHHiyoWYbobbyannaDevilHHHieZAUBobbyapebang baby bangHHIjIRMYbobbyape001HHIkBEiUbobbyape1HHIiQWzWBOBBYAUSTIN50HHHi~LHZBobbyAvacardoHHIhupsTBobbyAwsomeN:Bobby A:13HHI
             */

            /*
             * @LXVBPrXVBXaCHQAkP\USGammer12345IHHH07-04-2010 18:59:45jMI_SHensedIHHH09-04-2010 14:02:47kFzYZOskrarIHHH12-12-2009 17:28:07kk}[Z0skr4rIHHH10-04-2010 09:32:11kTDyZguy72IHHH23-02-2010 10:58:35PYH
             * 
             * 
             * 
             */

            if (Search)
            {
                Message.AppendUInt(UserId);
                Message.AppendStringWithBreak(Username);
                Message.AppendStringWithBreak(Motto);
                Message.AppendBoolean(IsOnline);
                Message.AppendBoolean(InRoom);
                Message.AppendStringWithBreak("");
                Message.AppendBoolean(false);
                Message.AppendStringWithBreak(Look);
                Message.AppendStringWithBreak(LastOnline);
                Message.AppendStringWithBreak(RealName); // ????????????
            }
            else
            {
                Message.AppendUInt(UserId);
                Message.AppendStringWithBreak(Username);
                Message.AppendBoolean(true);
                Message.AppendBoolean(IsOnline);
                Message.AppendBoolean(InRoom);
                Message.AppendStringWithBreak(Look);
                Message.AppendBoolean(false);
                Message.AppendStringWithBreak(Motto);
                Message.AppendStringWithBreak(LastOnline);
                Message.AppendStringWithBreak(RealName);
            }
        }
    }
}
