package com.uber.server.rooms;

import com.uber.server.messages.ServerMessage;

/**
 * Represents a room model (heightmap, door position, etc.).
 * Ported from HabboHotel/Rooms/RoomModel.cs
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
        if (heightmap != null && !heightmap.isEmpty()) {
            String[] lines = heightmap.split("\r\n");
            if (lines.length > 0) {
                this.mapSizeX = lines[0].length();
                this.mapSizeY = lines.length;
            } else {
                this.mapSizeX = 0;
                this.mapSizeY = 0;
            }
        } else {
            this.mapSizeX = 0;
            this.mapSizeY = 0;
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
    
    /**
     * Serializes heightmap to a ServerMessage.
     * Ported from RoomModel.cs SerializeHeightmap()
     * @return ServerMessage with heightmap (ID 31)
     */
    public ServerMessage serializeHeightmap() {
        StringBuilder heightMapStr = new StringBuilder();
        
        if (heightmap != null && !heightmap.isEmpty()) {
            String[] lines = heightmap.split("\r\n");
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
     * Ported from RoomModel.cs SerializeRelativeHeightmap()
     * @return ServerMessage with relative heightmap (ID 470)
     */
    public ServerMessage serializeRelativeHeightmap() {
        ServerMessage message = new ServerMessage(470);
        
        if (heightmap == null || heightmap.isEmpty()) {
            message.appendStringWithBreak("");
            return message;
        }
        
        String[] lines = heightmap.split("\r\n");
        for (int y = 0; y < mapSizeY && y < lines.length; y++) {
            String line = lines[y];
            if (y > 0 && line.length() > 0) {
                line = line.substring(1); // Remove first character
            }
            
            for (int x = 0; x < mapSizeX && x < line.length(); x++) {
                String square = line.substring(x, x + 1).trim().toLowerCase();
                
                // Replace door position with door Z
                if (doorX == x && doorY == y) {
                    square = String.valueOf((int) doorZ);
                }
                
                message.appendString(square);
            }
            
            message.appendString(String.valueOf((char) 13)); // Carriage return
        }
        
        return message;
    }
}
