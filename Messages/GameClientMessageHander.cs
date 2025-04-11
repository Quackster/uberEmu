using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.Messages;
using Uber.HabboHotel.GameClients;

namespace Uber.Messages
{
    partial class GameClientMessageHandler
    {
        private const int HIGHEST_MESSAGE_ID = 4004;

        private GameClient Session;

        private ClientMessage Request;
        private ServerMessage Response;

        private delegate void RequestHandler();
        private RequestHandler[] RequestHandlers;

        public GameClientMessageHandler(GameClient Session)
        {
            this.Session = Session;

            RequestHandlers = new RequestHandler[HIGHEST_MESSAGE_ID];

            Response = new ServerMessage(0);
        }

        public ServerMessage GetResponse()
        {
            return Response;
        }

        public void Destroy()
        {
            Session = null;
            RequestHandlers = null;

            Request = null;
            Response = null;
        }

        public void HandleRequest(ClientMessage Request)
        {
            //UberEnvironment.GetLogging().WriteLine("[" + Session.ClientId + "] --> " + Request.Header + Request.GetBody(), Uber.Core.LogLevel.Debug);

            if (Request.Id < 0 || Request.Id > HIGHEST_MESSAGE_ID)
            {
                UberEnvironment.GetLogging().WriteLine("Warning - out of protocol request: " + Request.Header, Uber.Core.LogLevel.Warning);
                return;
            }

            if (RequestHandlers[Request.Id] == null)
            {
                return;
            }

            this.Request = Request;
            RequestHandlers[Request.Id].Invoke();
            this.Request = null;
        }

        public void SendResponse()
        {
            if (Response.Id > 0)
            {
                Session.GetConnection().SendMessage(Response);
            }
        }
    }
}
