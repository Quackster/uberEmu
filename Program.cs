using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Security.Permissions;

using Uber.Core;

namespace Uber
{
    public class Program
    {
        [SecurityPermission(SecurityAction.Demand, Flags=SecurityPermissionFlag.ControlAppDomain)]
        public static void Main(string[] args)
        {
            AppDomain currentDomain = AppDomain.CurrentDomain;
            currentDomain.UnhandledException += new UnhandledExceptionEventHandler(MyHandler);

            try
            {
                UberEnvironment.Initialize();

                while (true)
                {
                    CommandParser.Parse(Console.ReadLine());
                }
            }

            catch (Exception e)
            {
                Console.Write(e.Message);
                Console.ReadKey(true);
            }
        }

        static void MyHandler(object sender, UnhandledExceptionEventArgs args)
        {
            Exception e = (Exception)args.ExceptionObject;
            Console.WriteLine("MyHandler caught : " + e.ToString());
            Console.ReadKey(true);
        }
    }
}
