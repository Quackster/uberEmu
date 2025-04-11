using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Uber.HabboHotel.Pathfinding
{
    class ShitPathfinder
    {
        public static Coord GetNextStep(int X, int Y, int goalX, int goalY)
        {
            Coord Next = new Coord(-1, -1);

            if (X > goalX && Y > goalY)
            {
                Next = new Coord(X - 1, Y - 1);
            }
            else if (X < goalX && Y < goalY)
            {
                Next = new Coord(X + 1, Y + 1);
            }
            else if (X > goalX && Y < goalY)
            {
                Next = new Coord(X - 1, Y + 1);
            }
            else if (X < goalX && Y > goalY)
            {
                Next = new Coord(X + 1, Y - 1);
            }
            else if (X > goalX)
            {
                Next = new Coord(X - 1, Y);
            }
            else if (X < goalX)
            {
                Next = new Coord(X + 1, Y);
            }
            else if (Y < goalY)
            {
                Next = new Coord(X, Y + 1);
            }
            else if (Y > goalY)
            {
                Next = new Coord(X, Y - 1);
            }

            return Next;
        }
    }
}
