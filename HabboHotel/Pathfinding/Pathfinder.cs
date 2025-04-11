using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Drawing;

using Uber.HabboHotel.GameClients;
using Uber.HabboHotel.Rooms;

namespace Uber.HabboHotel.Pathfinding
{
    class Pathfinder
    {
        Point[] Movements;
        CompleteSquare[,] Squares;

        Room Room;
        RoomModel Model;
        RoomUser User;

        int mapSizeX;
        int mapSizeY;

        public Pathfinder(Room Room, RoomUser User)
        {
            this.Room = Room;
            this.Model = Room.Model;
            this.User = User;

            if (Room == null || Model == null || User == null)
            {
                return;
            }

            InitMovements(4);

            mapSizeX = Model.MapSizeX;
            mapSizeY = Model.MapSizeY;

            Squares = new CompleteSquare[mapSizeX, mapSizeY];

            for (int x = 0; x < mapSizeX; x++)
            {
                for (int y = 0; y < mapSizeY; y++)
                {
                    Squares[x, y] = new CompleteSquare(x, y);
                }
            }
        }

        private IEnumerable<Point> GetSquares()
        {
            for (int x = 0; x < mapSizeX; x++)
            {
                for (int y = 0; y < mapSizeY; y++)
                {
                    yield return new Point(x, y);
                }
            }
        }

        private IEnumerable<Point> ValidMoves(int x, int y)
        {
            foreach (Point movePoint in Movements)
            {
                int newX = x + movePoint.X;
                int newY = y + movePoint.Y;

                if (ValidCoordinates(newX, newY) &&
                    IsSquareOpen(newX, newY, true))
                {
                    yield return new Point(newX, newY);
                }
            }
        }

        public List<Coord> FindPath()
        {
            // Locate the user, and set the distance to zero
            int UserX = User.X;
            int UserY = User.Y;

            Squares[User.X, User.Y].DistanceSteps = 0;

            // Find all possible moves
            while (true)
            {
                Boolean MadeProgress = false;

                foreach (Point MainPoint in GetSquares())
                {
                    int x = MainPoint.X;
                    int y = MainPoint.Y;

                    if (IsSquareOpen(x, y, true))
                    {
                        int passHere = Squares[x, y].DistanceSteps;

                        foreach (Point movePoint in ValidMoves(x, y))
                        {
                            int newX = movePoint.X;
                            int newY = movePoint.Y;
                            int newPass = passHere + 1;

                            if (Squares[newX, newY].DistanceSteps > newPass)
                            {
                                Squares[newX, newY].DistanceSteps = newPass;
                                MadeProgress = true;
                            }
                        }
                    }
                }

                if (!MadeProgress)
                {
                    break;
                }
            }

            // Locate the goal
            int goalX = User.GoalX;
            int goalY = User.GoalY;

            if (goalX == -1 || goalY == -1)
            {
                return null;
            }

            // Now trace the shortest possible route to our goal
            List<Coord> Path = new List<Coord>();

            Path.Add(new Coord(User.GoalX, User.GoalY));

            while (true)
            {
                Point lowestPoint = Point.Empty;
                int lowest = 100;

                foreach (Point movePoint in ValidMoves(goalX, goalY))
                {
                    int count = Squares[movePoint.X, movePoint.Y].DistanceSteps;

                    if (count < lowest)
                    {
                        lowest = count;

                        lowestPoint.X = movePoint.X;
                        lowestPoint.Y = movePoint.Y;
                    }
                }

                if (lowest != 100)
                {
                    Squares[lowestPoint.X, lowestPoint.Y].IsPath = true;
                    goalX = lowestPoint.X;
                    goalY = lowestPoint.Y;

                    Path.Add(new Coord(lowestPoint.X, lowestPoint.Y));
                }
                else
                {
                    break;
                }

                if (goalX == UserX && goalY == UserY)
                {
                    break;
                }
            }

            return Path;
        }

        private Boolean IsSquareOpen(int x, int y, Boolean CheckHeight)
        {
            if (Room.ValidTile(x, y) && User.AllowOverride)
            {
                return true;
            }

            if (User.X == x && User.Y == y)
            {
                return true;
            }

            bool isLastStep = false;

            if (User.GoalX == x && User.GoalY == y)
            {
                isLastStep = true;
            }

            if (!Room.CanWalk(x, y, 0, isLastStep))
            {
                return false;
            }

            return true;
        }

        private Boolean ValidCoordinates(int x, int y)
        {
            if (x < 0 || y < 0 || x > mapSizeX || y > mapSizeY)
            {
                return false;
            }

            return true;
        }

        public void InitMovements(int movementCount)
        {
            if (movementCount == 4)
            {
                Movements = new Point[]
                {
                    new Point(0, -1),
                    new Point(1, 0),
                    new Point(0, 1),
                    new Point(-1, 0)
                };
            }
            else
            {
                Movements = new Point[]
                {
                    new Point(-1, -1),
                    new Point(0, -1),
                    new Point(1, -1),
                    new Point(1, 0),
                    new Point(1, 1),
                    new Point(0, 1),
                    new Point(-1, 1),
                    new Point(-1, 0)
                };
            }
        }
    }

    class CompleteSquare
    {
        public int x = 0;
        public int y = 0;

        int _distanceSteps = 100;

        public int DistanceSteps
        {
            get { return _distanceSteps; }
            set { _distanceSteps = value; }
        }

        bool _isPath = false;

        public bool IsPath
        {
            get { return _isPath; }
            set { _isPath = value; }
        }

        public CompleteSquare(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
    }
}
