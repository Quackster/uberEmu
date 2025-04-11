using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PluginInterface
{
    public interface IUberPlugin
    {
        IUberPluginHost Host { get; set; }

        string Name { get; }

        string Description { get; }

        string Author { get; }

        string Version { get; }

        void Initialize();

        void Dispose();

        bool HandlePacket(uint HabboId, int HeaderId, byte[] Data);
    }
}
