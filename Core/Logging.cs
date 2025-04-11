using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace Uber.Core
{
    public enum LogLevel
    {
        Debug = 0,
        Information = 1,
        Warning = 2,
        Error = 3
    }

    public class Logging
    {
        public LogLevel MinimumLogLevel;
        public string LogFileName;

        public Logging() { }

        public void Clear()
        {
            Console.Clear();
        }

        public void WriteLine(string Line)
        {
            WriteLine(Line, LogLevel.Information);
        }

        public void WriteLine(string Line, LogLevel Level)
        {
            if (Level < MinimumLogLevel)
            {
                return;
            }

            SetLogColor(Level);
            Console.WriteLine(Line);
            ResetLogColor();
        }

        private void SetLogColor(LogLevel Level)
        {
            switch (Level)
            {
                case LogLevel.Debug:
                case LogLevel.Information:
                default:

                    Console.ForegroundColor = ConsoleColor.Gray;
                    break;

                case LogLevel.Warning:

                    Console.ForegroundColor = ConsoleColor.Yellow;
                    break;

                case LogLevel.Error:

                    Console.ForegroundColor = ConsoleColor.Red;
                    break;
            }
        }

        private void ResetLogColor()
        {
            Console.ForegroundColor = ConsoleColor.Gray;
        }
    }
}
