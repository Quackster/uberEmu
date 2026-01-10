package com.uber.server.game.pathfinding;

/**
 * Calculates rotation values for pathfinding.
 */
public class Rotation {
    /**
     * Calculates the rotation value between two coordinates.
     * @param x1 Source X coordinate
     * @param y1 Source Y coordinate
     * @param x2 Target X coordinate
     * @param y2 Target Y coordinate
     * @return Rotation value (0-7)
     */
    public static int calculate(int x1, int y1, int x2, int y2) {
        int rotation = 0;

        if (x1 > x2 && y1 > y2) {
            rotation = 7;
        } else if (x1 < x2 && y1 < y2) {
            rotation = 3;
        } else if (x1 > x2 && y1 < y2) {
            rotation = 5;
        } else if (x1 < x2 && y1 > y2) {
            rotation = 1;
        } else if (x1 > x2) {
            rotation = 6;
        } else if (x1 < x2) {
            rotation = 2;
        } else if (y1 < y2) {
            rotation = 4;
        } else if (y1 > y2) {
            rotation = 0;
        }

        return rotation;
    }
}
