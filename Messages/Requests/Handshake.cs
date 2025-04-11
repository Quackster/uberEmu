using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.Messages
{
    partial class GameClientMessageHandler
    {
        private void SendSessionParams()
        {
            Response.Init(257);
            Response.AppendInt32(9);
            Response.AppendInt32(0);
            Response.AppendInt32(0);
            Response.AppendInt32(1);
            Response.AppendInt32(1);
            Response.AppendInt32(3);
            Response.AppendInt32(0);
            Response.AppendInt32(2);
            Response.AppendInt32(1);
            Response.AppendInt32(4);
            Response.AppendInt32(0);
            Response.AppendInt32(5);
            Response.AppendStringWithBreak("dd-MM-yyyy");
            Response.AppendInt32(7);
            Response.AppendBoolean(false);
            Response.AppendInt32(8);
            Response.AppendStringWithBreak("hotel-co.uk");
            Response.AppendInt32(9);
            Response.AppendBoolean(false);

            SendResponse();
        }

        private void SSOLogin()
        {
            if (Session.GetHabbo() == null)
            {
                Session.Login(Request.PopFixedString());
            }
            else
            {
                Session.SendNotif("You are already logged in!");
            }
        }

        public void RegisterHandshake()
        {
            RequestHandlers[206] = new RequestHandler(SendSessionParams);
            RequestHandlers[415] = new RequestHandler(SSOLogin);
        }
    }
}
