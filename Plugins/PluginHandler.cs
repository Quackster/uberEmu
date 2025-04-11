using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using PluginInterface;

using Uber.Core;

namespace Uber.Plugins
{
    class PluginHandler: IUberPluginHost
    {
        public Types.AvailablePlugins AvailablePlugins;
        public Dictionary<int, string> PluginHandlers;

        public PluginHandler()
        {
            AvailablePlugins = new Types.AvailablePlugins();
            PluginHandlers = new Dictionary<int, string>();
        }

        public void LoadPlugins()
        {
            LoadPlugins(AppDomain.CurrentDomain.BaseDirectory);
        }

        public void LoadPlugins(string Path)
        {
            AvailablePlugins.Clear();
            PluginHandlers.Clear();

            foreach (string fileOn in Directory.GetFiles(Path))
            {
                FileInfo File = new FileInfo(fileOn);

                if (File.Extension.Equals(".dll"))
                {
                    InitPlugin(fileOn);
                }
            }
        }

        public void UnloadPlugins()
        {
            lock (AvailablePlugins)
            {
                foreach (Types.AvailablePlugin Plugin in AvailablePlugins)
                {
                    Plugin.Instance.Dispose();
                    Plugin.Instance = null;
                }
            }

            AvailablePlugins.Clear();
            PluginHandlers.Clear();
        }

        public Boolean UnloadPlugin(string Name)
        {
            lock (AvailablePlugins)
            {
                foreach (Types.AvailablePlugin Plugin in AvailablePlugins)
                {
                    if (Plugin.Instance.Name.ToLower() == Name.ToLower())
                    {
                        Plugin.Instance.Dispose();
                        Plugin.Instance = null;

                        AvailablePlugins.Remove(Plugin);

                        return true;
                    }
                }
            }

            return false;
        }

        public void InitPlugin(string FileName)
        {
            Assembly PluginAssembly = Assembly.LoadFrom(FileName);

            try
            {
                foreach (Type PluginType in PluginAssembly.GetTypes())
                {
                    if (PluginType.IsPublic)
                    {
                        if (!PluginType.IsAbstract)
                        {
                            Type TypeInterface = PluginType.GetInterface("PluginInterface.IUberPlugin", true);

                            if (TypeInterface != null)
                            {
                                Types.AvailablePlugin newPlugin = new Types.AvailablePlugin();

                                newPlugin.AssemblyPath = FileName;

                                newPlugin.Instance = (IUberPlugin)Activator.CreateInstance(PluginAssembly.GetType(PluginType.ToString()));
                                newPlugin.Instance.Host = this;
                                newPlugin.Instance.Initialize();

                                UberEnvironment.GetLogging().WriteLine("Initialized plugin " + newPlugin.Instance.Name, LogLevel.Debug);

                                this.AvailablePlugins.Add(newPlugin);

                                newPlugin = null;
                            }

                            TypeInterface = null;
                        }
                    }
                }
            }

            catch (Exception e)
            {
                UberEnvironment.GetLogging().WriteLine("Could not load plugin " + FileName + ": " + e.Message, LogLevel.Error);
            }

            PluginAssembly = null;
        }

        public void Destroy()
        {
            UnloadPlugins();

            this.AvailablePlugins.Clear();
            this.AvailablePlugins = null;
        }

        public Types.AvailablePlugin GetPlugin(string Plugin)
        {
            lock (AvailablePlugins)
            {
                foreach (Types.AvailablePlugin P in AvailablePlugins)
                {
                    if (P.Instance.Name.ToLower() == Plugin.ToLower())
                    {
                        return P;
                    }
                }
            }

            return null;
        }

        public void UnregisterPacketHandler(string Plugin, int Header)
        {
            PluginHandlers.Remove(Header);
        }

        public Boolean RegisterPacketHandler(string Plugin, int Header)
        {
            if (HasPacketHandler(Header))
            {
                return false;
            }

            PluginHandlers.Add(Header, Plugin);

            return true;
        }

        public Boolean HasPacketHandler(int HeaderId)
        {
            if (PluginHandlers.ContainsKey(HeaderId))
            {
                return true;
            }

            return false;
        }

        public Boolean ExecutePacketHandler(uint HabboId, int HeaderId, byte[] Data)
        {
            if (!HasPacketHandler(HeaderId))
            {
                return true;
            }

            Types.AvailablePlugin Plugin = GetPlugin(PluginHandlers[HeaderId]);

            if (Plugin == null)
            {
                return true;
            }

            return Plugin.Instance.HandlePacket(HabboId, HeaderId, Data);
        }
    }

    namespace Types
    {
        public class AvailablePlugins : System.Collections.CollectionBase
        {
            public void Add(Types.AvailablePlugin pluginToAdd)
            {
                this.List.Add(pluginToAdd);
            }

            public void Remove(Types.AvailablePlugin pluginToRemove)
            {
                this.List.Remove(pluginToRemove);
            }

            public Types.AvailablePlugin Find(string pluginNameOrPath)
            {
                Types.AvailablePlugin toReturn = null;

                foreach (Types.AvailablePlugin pluginOn in this.List)
                {
                    if ((pluginOn.Instance.Name.Equals(pluginNameOrPath)) || pluginOn.AssemblyPath.Equals(pluginNameOrPath))
                    {
                        toReturn = pluginOn;
                        break;
                    }
                }

                return toReturn;
            }
        }

        public class AvailablePlugin
        {
            private IUberPlugin myInstance = null;
            private string myAssemblyPath = "";

            public IUberPlugin Instance
            {
                get
                {
                    return myInstance;
                }
                set
                {
                    myInstance = value;
                }
            }

            public string AssemblyPath
            {
                get
                {
                    return myAssemblyPath;
                }

                set
                {
                    myAssemblyPath = value;
                }
            }
        }
    }	
}
