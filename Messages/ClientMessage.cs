using System;

using System.Linq;
using System.Text;

using Uber.Util;

namespace Uber.Messages
{
    class ClientMessage
    {
        private uint MessageId;
        private byte[] Body;
        private int Pointer;

        public uint Id
        {
            get
            {
                return MessageId;
            }
        }

        public int Length
        {
            get
            {
                return Body.Length;
            }
        }

        public int RemainingLength
        {
            get
            {
                return Body.Length - Pointer;
            }
        }

        public string Header
        {
            get
            {
                return Encoding.Default.GetString(Base64Encoding.Encodeuint(MessageId, 2));
            }
        }

        public ClientMessage(uint _MessageId, byte[] _Body)
        {
            if (_Body == null)
            {
                _Body = new byte[0];
            }

            MessageId = _MessageId;
            Body = _Body;

            Pointer = 0;
        }

        public override string ToString()
        {
            return Header + UberEnvironment.GetDefaultEncoding().GetString(Body);
        }

        public void ResetPointer()
        {
            Pointer = 0;
        }

        public void AdvancePointer(int i)
        {
            Pointer += i;
        }

        public string GetBody()
        {
            return Encoding.Default.GetString(Body);
        }

        public byte[] ReadBytes(int Bytes)
        {
            if (Bytes > this.RemainingLength)
            {
                Bytes = this.RemainingLength;
            }

            byte[] data = new byte[Bytes];

            for (int i = 0; i < Bytes; i++)
            {
                data[i] = Body[Pointer++];
            }

            return data;
        }

        public byte[] PlainReadBytes(int Bytes)
        {
            if (Bytes > RemainingLength)
            {
                Bytes = RemainingLength;
            }

            byte[] data = new byte[Bytes];

            for (int x = 0, y = Pointer; x < Bytes; x++, y++)
            {
                data[x] = Body[y];
            }

            return data;
        }

        public byte[] ReadFixedValue()
        {
            int len = Base64Encoding.DecodeInt32(ReadBytes(2));
            return ReadBytes(len);
        }

        public Boolean PopBase64Boolean()
        {
            if (RemainingLength > 0 && Body[Pointer++] == Base64Encoding.POSITIVE)
            {
                return true;
            }

            return false;
        }

        public Int32 PopInt32()
        {
            return Base64Encoding.DecodeInt32(ReadBytes(2));
        }

        public UInt32 PopUInt32()
        {
            return (UInt32)PopInt32();
        }

        public string PopFixedString()
        {
            return PopFixedString(UberEnvironment.GetDefaultEncoding());
        }

        public string PopFixedString(Encoding encoding)
        {
            return encoding.GetString(ReadFixedValue()).Replace(Convert.ToChar(1), ' ');
        }

        public Int32 PopFixedInt32()
        {
            Int32 i = 0;

            string s = PopFixedString(Encoding.ASCII);

            Int32.TryParse(s, out i);

            return i;
        }

        public UInt32 PopFixedUInt32()
        {
            return (uint)PopFixedInt32();
        }

        public Boolean PopWiredBoolean()
        {
            if (this.RemainingLength > 0 && Body[Pointer++] == WireEncoding.POSITIVE)
            {
                return true;
            }

            return false;
        }

        public Int32 PopWiredInt32()
        {
            if (RemainingLength < 1)
            {
                return 0;
            }

            byte[] Data = PlainReadBytes(WireEncoding.MAX_INTEGER_BYTE_AMOUNT);

            Int32 TotalBytes = 0;
            Int32 i = WireEncoding.DecodeInt32(Data, out TotalBytes);

            Pointer += TotalBytes;

            return i;
        }

        public uint PopWiredUInt()
        {
            return (uint)PopWiredInt32();
        }
    }
}
