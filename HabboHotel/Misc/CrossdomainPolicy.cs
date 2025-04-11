using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.Misc
{
    class CrossdomainPolicy
    {
        public static string GetXmlPolicy()
        {
            return "<?xml version=\"1.0\"?>\r\n" +
	               "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">\r\n" +
	               "<cross-domain-policy>\r\n" +
	               "<allow-access-from domain=\"*\" to-ports=\"1-31111\" />\r\n" +
	               "</cross-domain-policy>\x0";
        }
    }
}
