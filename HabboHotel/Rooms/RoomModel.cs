using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Uber.Messages;
using Uber.Util;

namespace Uber.HabboHotel.Rooms
{
    public enum SquareState
    {
        OPEN = 0,
        BLOCKED = 1,
        SEAT = 2
    }

    class RoomModel
    {
        public string Name;

        public int DoorX;
        public int DoorY;
        public double DoorZ;
        public int DoorOrientation;

        public string Heightmap;

        public SquareState[,] SqState;
        public double[,] SqFloorHeight;
        public int[,] SqSeatRot;

        public int MapSizeX;
        public int MapSizeY;

        public string StaticFurniMap;

        public bool ClubOnly;

        public RoomModel(string Name, int DoorX, int DoorY, double DoorZ, int DoorOrientation, string Heightmap, string StaticFurniMap, bool ClubOnly)
        {
            this.Name = Name;

            this.DoorX = DoorX;
            this.DoorY = DoorY;
            this.DoorZ = DoorZ;
            this.DoorOrientation = DoorOrientation;

            this.Heightmap = Heightmap.ToLower();
            this.StaticFurniMap = StaticFurniMap;

            string[] tmpHeightmap = Heightmap.Split(Convert.ToChar(13));

            this.MapSizeX = tmpHeightmap[0].Length;
            this.MapSizeY = tmpHeightmap.Length;

            this.ClubOnly = ClubOnly;

            SqState = new SquareState[MapSizeX, MapSizeY];
            SqFloorHeight = new double[MapSizeX, MapSizeY];
            SqSeatRot = new int[MapSizeX, MapSizeY];

            for (int y = 0; y < MapSizeY; y++)
            {
                if (y > 0)
                {
                    tmpHeightmap[y] = tmpHeightmap[y].Substring(1);
                }

                for (int x = 0; x < MapSizeX; x++)
                {
                    string Square = tmpHeightmap[y].Substring(x, 1).Trim().ToLower();

                    if (Square == "x")
                    {
                        SqState[x, y] = SquareState.BLOCKED;
                    }
                    else if (isNumeric(Square, System.Globalization.NumberStyles.Integer))
                    {
                        SqState[x, y] = SquareState.OPEN;
                        SqFloorHeight[x, y] = double.Parse(Square);
                    }
                }
            }

            SqFloorHeight[DoorX, DoorY] = DoorZ;

            // SOHd1017benchQDRBHJHd1019benchSDRBHRAHd1021benchQERBHJHd1023benchSERBHRAHd1117benchQDSBHJHd1119benchSDSBHRAHd1121benchQESBHJHd1123benchSESBHRAHb1132koc_chairPHSBIPAHd1217benchQDPCHJHd1219benchSDPCHRAHd1221benchQEPCHJHd1223benchSEPCHRAHb1231koc_chairSGPCIJHa1232koc_tablePHPCIHHb1233koc_chairQHPCIRAHd1317benchQDQCHJHd1319benchSDQCHRAHd1321benchQEQCHJHd1323benchSEQCHRAHb1325koc_chairQFQCIPAHb1332koc_chairPHQCIHHd1417benchQDRCHJHd1419benchSDRCHRAHd1421benchQERCHJHd1423benchSERCHRAHa1425koc_tableQFRCIHHb1426koc_chairRFRCIRAHd1517benchQDSCHJHd1519benchSDSCHRAHd1521benchQESCHJHd1523benchSESCHRAHb1525koc_chairQFSCIHHb1529koc_chairQGSCIJHa1530koc_tableRGSCIHHb1531koc_chairSGSCIRAHb1630koc_chairRGPDIHHc2425chairf1QFPFIJHc2433chairf1QHPFIRAHd2517benchQDQFHJHd2519benchSDQFHRAHd2521benchQEQFHJHd2523benchSEQFHRAHc2525chairf1QFQFIJHc2533chairf1QHQFIRAHd2617benchQDRFHJHd2619benchSDRFHRAHd2621benchQERFHJHd2623benchSERFHRAHc2625chairf1QFRFIJHc2633chairf1QHRFIRAHd2717benchQDSFHJHd2719benchSDSFHRAHd2721benchQESFHJHd2723benchSESFHRAHd2817benchQDPGHJHd2819benchSDPGHRAHd2821benchQEPGHJHd2823benchSEPGHRAHd2917benchQDQGHJHd2919benchSDQGHRAHd2921benchQEQGHJHd2923benchSEQG`hFFRA

            string tmpFurnimap = StaticFurniMap;
            int pointer = 0;

            int num = OldEncoding.decodeVL64(tmpFurnimap);
            pointer += OldEncoding.encodeVL64(num).Length;

            for (int i = 0; i < num; i++)
            {
                string thisss = tmpFurnimap.Substring(pointer);

                int junk = OldEncoding.decodeVL64(tmpFurnimap.Substring(pointer)); // probably not junk, but dunno what it is
                pointer += OldEncoding.encodeVL64(junk).Length;

                string junk2 = tmpFurnimap.Substring(pointer, 1); // probably not junk, but dunno what it is
                pointer += 1;

                int junk3 = int.Parse(tmpFurnimap.Substring(pointer).Split(Convert.ToChar(2))[0]); // probably not junk, but dunno what it is
                pointer += tmpFurnimap.Substring(pointer).Split(Convert.ToChar(2))[0].Length;

                pointer += 1;

                string name = tmpFurnimap.Substring(pointer).Split(Convert.ToChar(2))[0];
                pointer += tmpFurnimap.Substring(pointer).Split(Convert.ToChar(2))[0].Length;

                pointer += 1;

                int x = OldEncoding.decodeVL64(tmpFurnimap.Substring(pointer));
                pointer += OldEncoding.encodeVL64(x).Length;

                int y = OldEncoding.decodeVL64(tmpFurnimap.Substring(pointer));
                pointer += OldEncoding.encodeVL64(y).Length;

                int junk4 = OldEncoding.decodeVL64(tmpFurnimap.Substring(pointer)); // probably not junk, but dunno what it is
                pointer += OldEncoding.encodeVL64(junk4).Length;

                int junk5 = OldEncoding.decodeVL64(tmpFurnimap.Substring(pointer)); // probably not junk, but dunno what it is
                pointer += OldEncoding.encodeVL64(junk5).Length;

                SqState[x, y] = SquareState.BLOCKED;

                if (name.Contains("bench") || name.Contains("chair") || name.Contains("stool") ||
                    name.Contains("seat") || name.Contains("sofa"))
                {
                    SqState[x, y] = SquareState.SEAT;
                    SqSeatRot[x, y] = junk5;
                }
            }
        }

        public bool isNumeric(string val, System.Globalization.NumberStyles NumberStyle)
        {
            Double result;
            return Double.TryParse(val, NumberStyle,
                System.Globalization.CultureInfo.CurrentCulture, out result);
        }

        public ServerMessage SerializeHeightmap()
        {
            StringBuilder HeightMap = new StringBuilder();

            foreach (string MapBit in Heightmap.Split("\r\n".ToCharArray()))
            {
                if (MapBit == "")
                {
                    continue;
                }

                HeightMap.Append(MapBit);
                HeightMap.Append(Convert.ToChar(13));
            }

            ServerMessage Message = new ServerMessage(31);
            Message.AppendStringWithBreak(HeightMap.ToString());
            return Message;
        }

        public ServerMessage SerializeRelativeHeightmap()
        {
            ServerMessage Message = new ServerMessage(470);

            string[] tmpHeightmap = Heightmap.Split(Convert.ToChar(13));

            for (int y = 0; y < MapSizeY; y++)
            {
                if (y > 0)
                {
                    tmpHeightmap[y] = tmpHeightmap[y].Substring(1);
                }

                for (int x = 0; x < MapSizeX; x++)
                {
                    string Square = tmpHeightmap[y].Substring(x, 1).Trim().ToLower();

                    if (DoorX == x && DoorY == y)
                    {
                        Square = (int)DoorZ + "";
                    }

                    Message.AppendString(Square);
                }

                Message.AppendString("" + Convert.ToChar(13));
            }

            return Message;
        }
    }
}
