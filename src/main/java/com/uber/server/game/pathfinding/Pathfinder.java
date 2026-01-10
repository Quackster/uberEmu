package com.uber.server.game.pathfinding;

import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomModel;
import com.uber.server.game.rooms.RoomUser;
import com.uber.server.game.rooms.mapping.RoomMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Pathfinding implementation using a distance-based algorithm.
 * Enhanced to be more Habbo-like with better diagonal movement and height consideration.
 */
public class Pathfinder {
    private static class Point {
        int x;
        int y;
        
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    private static class CompleteSquare {
        int x;
        int y;
        int distanceSteps = 100;
        boolean isPath = false;
        
        CompleteSquare(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    private Point[] movements;
    private CompleteSquare[][] squares;
    
    private Room room;
    private RoomModel model;
    private RoomUser user;
    private RoomMapping roomMapping;
    
    private int mapSizeX;
    private int mapSizeY;
    
    // Maximum height difference allowed for pathfinding (in tiles)
    private static final double MAX_HEIGHT_DIFF = 1.5;
    
    public Pathfinder(Room room, RoomUser user) {
        this.room = room;
        if (room != null) {
            this.model = room.getModel();
            this.roomMapping = room.getRoomMapping();
        }
        this.user = user;
        
        if (room == null || model == null || user == null) {
            return;
        }
        
        // Use 8-directional movement for smoother, more Habbo-like paths
        initMovements(8);
        
        mapSizeX = model.getMapSizeX();
        mapSizeY = model.getMapSizeY();
        
        squares = new CompleteSquare[mapSizeX][mapSizeY];
        
        for (int x = 0; x < mapSizeX; x++) {
            for (int y = 0; y < mapSizeY; y++) {
                squares[x][y] = new CompleteSquare(x, y);
            }
        }
    }
    
    private List<Point> getSquares() {
        List<Point> result = new ArrayList<>();
        for (int x = 0; x < mapSizeX; x++) {
            for (int y = 0; y < mapSizeY; y++) {
                result.add(new Point(x, y));
            }
        }
        return result;
    }
    
    private List<Point> validMoves(int x, int y) {
        List<Point> result = new ArrayList<>();
        for (Point movePoint : movements) {
            int newX = x + movePoint.x;
            int newY = y + movePoint.y;
            
            if (validCoordinates(newX, newY) && isSquareOpen(newX, newY, true)) {
                result.add(new Point(newX, newY));
            }
        }
        return result;
    }
    
    public List<Coord> findPath() {
        if (room == null || model == null || user == null) {
            return null;
        }
        
        // Locate the user, and set the distance to zero
        int userX = user.getX();
        int userY = user.getY();
        
        squares[user.getX()][user.getY()].distanceSteps = 0;
        
        // Find all possible moves
        while (true) {
            boolean madeProgress = false;
            
            for (Point mainPoint : getSquares()) {
                int x = mainPoint.x;
                int y = mainPoint.y;
                
                if (isSquareOpen(x, y, true)) {
                    int passHere = squares[x][y].distanceSteps;
                    
                    for (Point movePoint : validMoves(x, y)) {
                        int newX = movePoint.x;
                        int newY = movePoint.y;
                        int newPass = passHere + 1;
                        
                        if (squares[newX][newY].distanceSteps > newPass) {
                            squares[newX][newY].distanceSteps = newPass;
                            madeProgress = true;
                        }
                    }
                }
            }
            
            if (!madeProgress) {
                break;
            }
        }
        
        // Locate the goal
        int goalX = user.getGoalX();
        int goalY = user.getGoalY();
        
        if (goalX == -1 || goalY == -1) {
            return null;
        }
        
        // Now trace the shortest possible route to our goal
        List<Coord> path = new ArrayList<>();
        
        path.add(new Coord(user.getGoalX(), user.getGoalY()));
        
        while (true) {
            Point lowestPoint = null;
            int lowest = 100;
            
            for (Point movePoint : validMoves(goalX, goalY)) {
                int count = squares[movePoint.x][movePoint.y].distanceSteps;
                
                if (count < lowest) {
                    lowest = count;
                    lowestPoint = new Point(movePoint.x, movePoint.y);
                }
            }
            
            if (lowest != 100 && lowestPoint != null) {
                squares[lowestPoint.x][lowestPoint.y].isPath = true;
                goalX = lowestPoint.x;
                goalY = lowestPoint.y;
                
                path.add(new Coord(lowestPoint.x, lowestPoint.y));
            } else {
                break;
            }
            
            if (goalX == userX && goalY == userY) {
                break;
            }
        }
        
        return path;
    }
    
    private boolean isSquareOpen(int x, int y, boolean checkHeight) {
        if (room.validTile(x, y) && user.isAllowOverride()) {
            return true;
        }
        
        if (user.getX() == x && user.getY() == y) {
            return true;
        }
        
        boolean isLastStep = false;
        
        if (user.getGoalX() == x && user.getGoalY() == y) {
            isLastStep = true;
        }
        
        if (!room.canWalk(x, y, 0, isLastStep)) {
            return false;
        }
        
        // Check height difference if RoomMapping is available and checkHeight is true
        if (checkHeight && roomMapping != null) {
            double[][] heightMatrix = roomMapping.getHeightMatrix();
            double[][] topStackHeight = roomMapping.getTopStackHeight();
            double[][] sqFloorHeight = model.getSqFloorHeight();
            
            int userX = user.getX();
            int userY = user.getY();
            
            if (userX >= 0 && userX < mapSizeX && userY >= 0 && userY < mapSizeY &&
                x >= 0 && x < mapSizeX && y >= 0 && y < mapSizeY) {
                
                // Calculate current height
                double currentHeight = sqFloorHeight[userX][userY] + topStackHeight[userX][userY] + heightMatrix[userX][userY];
                
                // Calculate target height
                double targetHeight = sqFloorHeight[x][y] + topStackHeight[x][y] + heightMatrix[x][y];
                
                // Check if height difference is too large
                double heightDiff = Math.abs(targetHeight - currentHeight);
                if (heightDiff > MAX_HEIGHT_DIFF) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private boolean validCoordinates(int x, int y) {
        // Uses x > mapSizeX (not >=)
        if (x < 0 || y < 0 || x > mapSizeX || y > mapSizeY) {
            return false;
        }
        
        return true;
    }
    
    public void initMovements(int movementCount) {
        if (movementCount == 4) {
            movements = new Point[] {
                new Point(0, -1),
                new Point(1, 0),
                new Point(0, 1),
                new Point(-1, 0)
            };
        } else {
            movements = new Point[] {
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
