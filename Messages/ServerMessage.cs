using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.Util;

namespace Uber.Messages
{
    class ServerMessage
    {
        private uint MessageId;

        public uint Id
        {
            get
            {
                return MessageId;
            }
        }

        private List<byte> Body;

        public string Header
        {
            get
            {
                return UberEnvironment.GetDefaultEncoding().GetString(Base64Encoding.Encodeuint(MessageId, 2));
            }
        }

        public int Length
        {
            get
            {
                return Body.Count;
            }
        }

        public ServerMessage() { }

        public ServerMessage(uint _MessageId)
        {
            Init(_MessageId);    
        }

        public override string ToString()
        {
            return Header + UberEnvironment.GetDefaultEncoding().GetString(Body.ToArray());
        }

        public string ToBodyString()
        {
            return UberEnvironment.GetDefaultEncoding().GetString(Body.ToArray());
        }

        public void Clear()
        {
            Body.Clear();
        }

        public void Init(uint _MessageId)
        {
            MessageId = _MessageId;
            Body = new List<byte>();
        }

        public void AppendByte(byte b)
        {
            Body.Add(b);
        }

        public void AppendBytes(byte[] Data)
        {
            if (Data == null || Data.Length == 0)
            {
                return;
            }

            Body.AddRange(Data);
        }

        public void AppendString(string s, Encoding encoding)
        {
            if (s == null || s.Length == 0)
            {
                return;
            }

            AppendBytes(encoding.GetBytes(s));
        }

        public void AppendString(string s)
        {
            AppendString(s, UberEnvironment.GetDefaultEncoding());
        }

        public void AppendStringWithBreak(string s)
        {
            AppendStringWithBreak(s, 2);
        }

        public void AppendStringWithBreak(string s, byte BreakChar)
        {
            AppendString(s);
            AppendByte(BreakChar);
        }

        public void AppendInt32(Int32 i)
        {
            AppendBytes(WireEncoding.EncodeInt32(i));
        }

        public void AppendRawInt32(Int32 i)
        {
            AppendString(i.ToString(), Encoding.ASCII);
        }

        public void AppendUInt(uint i)
        {
            Int32 _i = (Int32)i;
            AppendInt32(_i);
        }

        public void AppendRawUInt(uint i)
        {
            Int32 _i = (Int32)i;
            AppendRawInt32(_i);
        }

        public void AppendBoolean(Boolean Bool)
        {
            if (Bool)
            {
                Body.Add(WireEncoding.POSITIVE);
                return;
            }

            Body.Add(WireEncoding.NEGATIVE);
        }

        public byte[] GetBytes()
        {
            byte[] Data = new byte[Length + 3];
            byte[] Header = Base64Encoding.Encodeuint(MessageId, 2);

            Data[0] = Header[0];
            Data[1] = Header[1];

            for (int i = 0; i < Length; i++)
            {
                Data[i + 2] = Body[i];
            }

            Data[Data.Length - 1] = 1;

            return Data;
        }
    }
}
