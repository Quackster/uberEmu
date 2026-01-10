package com.uber.server.game.rooms;

import com.uber.server.game.rooms.mapping.SquareState;
import com.uber.server.messages.ServerMessage;
import com.uber.server.util.OldWireEncoding;

/**
 * Represents a room model (heightmap, door position, etc.).
 */
public class RoomModel {
    private final String id;
    private final int doorX;
    private final int doorY;
    private final double doorZ;
    private final int doorDir;
    private final String heightmap;
    private final String publicItems;
    private final boolean clubOnly;
    private final int mapSizeX;
    private final int mapSizeY;
    
    // Parsed heightmap data
    private final SquareState[][] sqState;
    private final double[][] sqFloorHeight;
    private final int[][] sqSeatRot;
    
    public RoomModel(String id, int doorX, int doorY, double doorZ, int doorDir,
                    String heightmap, String publicItems, boolean clubOnly) {
        this.id = id;
        this.doorX = doorX;
        this.doorY = doorY;
        this.doorZ = doorZ;
        this.doorDir = doorDir;
        this.heightmap = heightmap != null ? heightmap.toLowerCase() : "";
        this.publicItems = publicItems != null ? publicItems : "";
        this.clubOnly = clubOnly;
        
        // Calculate map size from heightmap
        // Heightmap is split by carriage return character '\r'
        String[] tmpHeightmap;
        if (heightmap != null && !heightmap.isEmpty()) {
            tmpHeightmap = heightmap.split(String.valueOf((char) 13));
            if (tmpHeightmap.length > 0) {
                this.mapSizeX = tmpHeightmap[0].length();
                this.mapSizeY = tmpHeightmap.length;
            } else {
                this.mapSizeX = 0;
                this.mapSizeY = 0;
                tmpHeightmap = new String[0];
            }
        } else {
            this.mapSizeX = 0;
            this.mapSizeY = 0;
            tmpHeightmap = new String[0];
        }
        
        // Initialize arrays
        this.sqState = new SquareState[mapSizeX][mapSizeY];
        this.sqFloorHeight = new double[mapSizeX][mapSizeY];
        this.sqSeatRot = new int[mapSizeX][mapSizeY];
        
        // Parse heightmap
        if (mapSizeX > 0 && mapSizeY > 0 && tmpHeightmap.length > 0) {
            for (int y = 0; y < mapSizeY; y++) {
                String line = tmpHeightmap[y];
                if (y > 0 && line.length() > 0) {
                    line = line.substring(1); // Remove first character
                }
                
                for (int x = 0; x < mapSizeX && x < line.length(); x++) {
                    String square = line.substring(x, x + 1).trim().toLowerCase();
                    
                    if ("x".equals(square)) {
                        sqState[x][y] = SquareState.BLOCKED;
                    } else if (isNumeric(square)) {
                        sqState[x][y] = SquareState.OPEN;
                        sqFloorHeight[x][y] = Double.parseDouble(square);
                    } else {
                        sqState[x][y] = SquareState.BLOCKED;
                    }
                }
            }
            
            // Set door floor height
            if (doorX >= 0 && doorX < mapSizeX && doorY >= 0 && doorY < mapSizeY) {
                sqFloorHeight[doorX][doorY] = doorZ;
            }
            
            // Parse static furni map
            parseStaticFurniMap(publicItems);
        }
    }
    
    /**
     * Checks if a string is numeric.
     */
    private boolean isNumeric(String val) {
        if (val == null || val.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(val);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Parses the static furni map to identify seats.
     */
    private void parseStaticFurniMap(String staticFurniMap) {
        if (staticFurniMap == null || staticFurniMap.isEmpty()) {
            return;
        }
        
        try {
            String tmpFurnimap = staticFurniMap;
            int pointer = 0;
            
            // Decode number of items
            int num = OldWireEncoding.decodeVL64(tmpFurnimap);
            String encodedNum = OldWireEncoding.encodeVL64(num);
            pointer += encodedNum.length();
            
            for (int i = 0; i < num; i++) {
                if (pointer >= tmpFurnimap.length()) {
                    break;
                }
                
                // Decode junk (item ID or similar)
                String remaining = tmpFurnimap.substring(pointer);
                int junk = OldWireEncoding.decodeVL64(remaining);
                String encodedJunk = OldWireEncoding.encodeVL64(junk);
                pointer += encodedJunk.length();
                
                if (pointer >= tmpFurnimap.length()) {
                    break;
                }
                
                // Read single character (junk2)
                String junk2 = tmpFurnimap.substring(pointer, pointer + 1);
                pointer += 1;
                
                if (pointer >= tmpFurnimap.length()) {
                    break;
                }
                
                // Decode junk3 (sprite ID or similar)
                String[] parts = tmpFurnimap.substring(pointer).split("\u0002");
                if (parts.length == 0) {
                    break;
                }
                int junk3 = Integer.parseInt(parts[0]);
                pointer += parts[0].length() + 1; // +1 for delimiter
                
                if (pointer >= tmpFurnimap.length()) {
                    break;
                }
                
                // Read item name
                parts = tmpFurnimap.substring(pointer).split("\u0002");
                if (parts.length == 0) {
                    break;
                }
                String name = parts[0];
                pointer += name.length() + 1; // +1 for delimiter
                
                if (pointer >= tmpFurnimap.length()) {
                    break;
                }
                
                // Decode X coordinate
                remaining = tmpFurnimap.substring(pointer);
                int x = OldWireEncoding.decodeVL64(remaining);
                String encodedX = OldWireEncoding.encodeVL64(x);
                pointer += encodedX.length();
                
                if (pointer >= tmpFurnimap.length()) {
                    break;
                }
                
                // Decode Y coordinate
                remaining = tmpFurnimap.substring(pointer);
                int y = OldWireEncoding.decodeVL64(remaining);
                String encodedY = OldWireEncoding.encodeVL64(y);
                pointer += encodedY.length();
                
                if (pointer >= tmpFurnimap.length()) {
                    break;
                }
                
                // Decode junk4
                remaining = tmpFurnimap.substring(pointer);
                int junk4 = OldWireEncoding.decodeVL64(remaining);
                String encodedJunk4 = OldWireEncoding.encodeVL64(junk4);
                pointer += encodedJunk4.length();
                
                if (pointer >= tmpFurnimap.length()) {
                    break;
                }
                
                // Decode junk5 (rotation for seats)
                remaining = tmpFurnimap.substring(pointer);
                int junk5 = OldWireEncoding.decodeVL64(remaining);
                String encodedJunk5 = OldWireEncoding.encodeVL64(junk5);
                pointer += encodedJunk5.length();
                
                // Check if coordinates are valid
                if (x >= 0 && x < mapSizeX && y >= 0 && y < mapSizeY) {
                    sqState[x][y] = SquareState.BLOCKED;
                    
                    // Check if this is a seat
                    String nameLower = name.toLowerCase();
                    if (nameLower.contains("bench") || nameLower.contains("chair") || 
                        nameLower.contains("stool") || nameLower.contains("seat") || 
                        nameLower.contains("sofa")) {
                        sqState[x][y] = SquareState.SEAT;
                        sqSeatRot[x][y] = junk5;
                    }
                }
            }
        } catch (Exception e) {
            // If parsing fails, just continue - static furni map is optional
        }
    }
    
    // Getters
    public String getId() { return id; }
    public int getDoorX() { return doorX; }
    public int getDoorY() { return doorY; }
    public double getDoorZ() { return doorZ; }
    public int getDoorDir() { return doorDir; }
    public String getDoorOrientation() { return String.valueOf(doorDir); }
    public String getHeightmap() { return heightmap; }
    public String getPublicItems() { return publicItems; }
    public boolean isClubOnly() { return clubOnly; }
    public int getMapSizeX() { return mapSizeX; }
    public int getMapSizeY() { return mapSizeY; }
    
    // Parsed heightmap data getters
    public SquareState[][] getSqState() { return sqState; }
    public double[][] getSqFloorHeight() { return sqFloorHeight; }
    public int[][] getSqSeatRot() { return sqSeatRot; }
    
    /**
     * Serializes heightmap to a ServerMessage.
     * Uses Split("\r\n".ToCharArray()) which creates char array ['\r', '\n'] and splits on either.
     * @return ServerMessage with heightmap (ID 31)
     */
    public ServerMessage serializeHeightmap() {
        StringBuilder heightMapStr = new StringBuilder();
        
        if (heightmap != null && !heightmap.isEmpty()) {
            // Split on either \r or \n
            String[] lines = heightmap.split("[\r\n]");
            for (String line : lines) {
                if (line.isEmpty()) {
                    continue;
                }
                heightMapStr.append(line);
                heightMapStr.append((char) 13); // Carriage return
            }
        }
        
        ServerMessage message = new ServerMessage(31);
        message.appendStringWithBreak(heightMapStr.toString());
        return message;
    }
    
    /**
     * Serializes relative heightmap to a ServerMessage.
     * @return ServerMessage with relative heightmap (ID 470)
     */
    public ServerMessage serializeRelativeHeightmap() {
        ServerMessage message = new ServerMessage(470);
        
        if (heightmap == null || heightmap.isEmpty()) {
            message.appendStringWithBreak("");
            return message;
        }
        
        // Split heightmap by carriage return character '\r'
        String[] tmpHeightmap = heightmap.split(String.valueOf((char) 13));
        
        for (int y = 0; y < mapSizeY; y++) {
            if (y >= tmpHeightmap.length) {
                break;
            }
            
            String line = tmpHeightmap[y];
            // Remove first character from lines after index 0 (heightmap format requirement)
            if (y > 0 && line.length() > 0) {
                line = line.substring(1);
            }
            
            for (int x = 0; x < mapSizeX; x++) {
                if (x >= line.length()) {
                    break;
                }
                
                String square = line.substring(x, x + 1).trim().toLowerCase();
                
                // Replace door position with door Z value
                if (doorX == x && doorY == y) {
                    square = String.valueOf((int) doorZ);
                }
                
                message.appendString(square);
            }
            
            // Append carriage return after each line
            message.appendString(String.valueOf((char) 13));
        }
        
        return message;
    }
}
