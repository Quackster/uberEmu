using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PluginInterface
{
    public interface IUberPluginHost
    {
        bool RegisterPacketHandler(string PluginName, int HeaderId);

        void UnregisterPacketHandler(string PluginName, int HeaderId);
    }

}
