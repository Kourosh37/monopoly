package com.monopoly.model.game;

import com.monopoly.datastructures.ArrayList;
import com.monopoly.datastructures.HashTable;
import com.monopoly.model.property.ColorGroup;
import com.monopoly.model.tile.TileType;
import com.monopoly.model.property.Property;
import com.monopoly.model.tile.*;

/**
 * Represents the Monopoly game board.
 * Uses ArrayList to store tiles with circular navigation.
 * Standard Monopoly board has 40 tiles.
 */
public class Board {

    // Constants
    public static final int BOARD_SIZE = 40;
    public static final int GO_POSITION = 0;
    public static final int JAIL_POSITION = 10;
    public static final int FREE_PARKING_POSITION = 20;
    public static final int GO_TO_JAIL_POSITION = 30;
    
    // Special position constants
    public static final int MEDITERRANEAN_AVE = 1;
    public static final int BALTIC_AVE = 3;
    public static final int READING_RAILROAD = 5;
    public static final int ORIENTAL_AVE = 6;
    public static final int VERMONT_AVE = 8;
    public static final int CONNECTICUT_AVE = 9;
    public static final int ST_CHARLES_PLACE = 11;
    public static final int ELECTRIC_COMPANY = 12;
    public static final int STATES_AVE = 13;
    public static final int VIRGINIA_AVE = 14;
    public static final int PENNSYLVANIA_RAILROAD = 15;
    public static final int ST_JAMES_PLACE = 16;
    public static final int TENNESSEE_AVE = 18;
    public static final int NEW_YORK_AVE = 19;
    public static final int KENTUCKY_AVE = 21;
    public static final int INDIANA_AVE = 23;
    public static final int ILLINOIS_AVE = 24;
    public static final int B_AND_O_RAILROAD = 25;
    public static final int ATLANTIC_AVE = 26;
    public static final int VENTNOR_AVE = 27;
    public static final int WATER_WORKS = 28;
    public static final int MARVIN_GARDENS = 29;
    public static final int PACIFIC_AVE = 31;
    public static final int NORTH_CAROLINA_AVE = 32;
    public static final int PENNSYLVANIA_AVE = 34;
    public static final int SHORT_LINE_RAILROAD = 35;
    public static final int PARK_PLACE = 37;
    public static final int BOARDWALK = 39;
    
    // Fields
    private final ArrayList<Tile> tiles;
    private final HashTable<Integer, Property> allProperties; // propertyId -> Property
    
    /**
     * Creates a new Board with standard Monopoly layout
     */
    public Board() {
        this.tiles = new ArrayList<>(BOARD_SIZE);
        this.allProperties = new HashTable<>();
        initializeStandardBoard();
    }
    
    /**
     * Initializes the standard Monopoly board with all 40 tiles
     */
    private void initializeStandardBoard() {
        // Position 0: GO
        tiles.add(new GoTile(0));
        
        // Position 1: Mediterranean Avenue (Brown)
        Property mediterranean = createProperty(1, "Mediterranean Avenue", ColorGroup.BROWN, 60, 50, 2, 10, 30, 90, 160, 250);
        tiles.add(new PropertyTile(1, "Mediterranean Avenue", mediterranean));
        
        // Position 2: Community Chest
        tiles.add(new CommunityChestTile(2));
        
        // Position 3: Baltic Avenue (Brown)
        Property baltic = createProperty(3, "Baltic Avenue", ColorGroup.BROWN, 60, 50, 4, 20, 60, 180, 320, 450);
        tiles.add(new PropertyTile(3, "Baltic Avenue", baltic));
        
        // Position 4: Income Tax
        tiles.add(new TaxTile(4, "Income Tax", 200));
        
        // Position 5: Reading Railroad
        tiles.add(new RailroadTile(5, "Reading Railroad"));
        
        // Position 6: Oriental Avenue (Light Blue)
        Property oriental = createProperty(6, "Oriental Avenue", ColorGroup.LIGHT_BLUE, 100, 50, 6, 30, 90, 270, 400, 550);
        tiles.add(new PropertyTile(6, "Oriental Avenue", oriental));
        
        // Position 7: Chance
        tiles.add(new ChanceTile(7));
        
        // Position 8: Vermont Avenue (Light Blue)
        Property vermont = createProperty(8, "Vermont Avenue", ColorGroup.LIGHT_BLUE, 100, 50, 6, 30, 90, 270, 400, 550);
        tiles.add(new PropertyTile(8, "Vermont Avenue", vermont));
        
        // Position 9: Connecticut Avenue (Light Blue)
        Property connecticut = createProperty(9, "Connecticut Avenue", ColorGroup.LIGHT_BLUE, 120, 50, 8, 40, 100, 300, 450, 600);
        tiles.add(new PropertyTile(9, "Connecticut Avenue", connecticut));
        
        // Position 10: Jail / Just Visiting
        tiles.add(new JailTile(10));
        
        // Position 11: St. Charles Place (Pink)
        Property stCharles = createProperty(11, "St. Charles Place", ColorGroup.PINK, 140, 100, 10, 50, 150, 450, 625, 750);
        tiles.add(new PropertyTile(11, "St. Charles Place", stCharles));
        
        // Position 12: Electric Company
        tiles.add(new UtilityTile(12, "Electric Company"));
        
        // Position 13: States Avenue (Pink)
        Property states = createProperty(13, "States Avenue", ColorGroup.PINK, 140, 100, 10, 50, 150, 450, 625, 750);
        tiles.add(new PropertyTile(13, "States Avenue", states));
        
        // Position 14: Virginia Avenue (Pink)
        Property virginia = createProperty(14, "Virginia Avenue", ColorGroup.PINK, 160, 100, 12, 60, 180, 500, 700, 900);
        tiles.add(new PropertyTile(14, "Virginia Avenue", virginia));
        
        // Position 15: Pennsylvania Railroad
        tiles.add(new RailroadTile(15, "Pennsylvania Railroad"));
        
        // Position 16: St. James Place (Orange)
        Property stJames = createProperty(16, "St. James Place", ColorGroup.ORANGE, 180, 100, 14, 70, 200, 550, 750, 950);
        tiles.add(new PropertyTile(16, "St. James Place", stJames));
        
        // Position 17: Community Chest
        tiles.add(new CommunityChestTile(17));
        
        // Position 18: Tennessee Avenue (Orange)
        Property tennessee = createProperty(18, "Tennessee Avenue", ColorGroup.ORANGE, 180, 100, 14, 70, 200, 550, 750, 950);
        tiles.add(new PropertyTile(18, "Tennessee Avenue", tennessee));
        
        // Position 19: New York Avenue (Orange)
        Property newYork = createProperty(19, "New York Avenue", ColorGroup.ORANGE, 200, 100, 16, 80, 220, 600, 800, 1000);
        tiles.add(new PropertyTile(19, "New York Avenue", newYork));
        
        // Position 20: Free Parking
        tiles.add(new FreeParkingTile(20));
        
        // Position 21: Kentucky Avenue (Red)
        Property kentucky = createProperty(21, "Kentucky Avenue", ColorGroup.RED, 220, 150, 18, 90, 250, 700, 875, 1050);
        tiles.add(new PropertyTile(21, "Kentucky Avenue", kentucky));
        
        // Position 22: Chance
        tiles.add(new ChanceTile(22));
        
        // Position 23: Indiana Avenue (Red)
        Property indiana = createProperty(23, "Indiana Avenue", ColorGroup.RED, 220, 150, 18, 90, 250, 700, 875, 1050);
        tiles.add(new PropertyTile(23, "Indiana Avenue", indiana));
        
        // Position 24: Illinois Avenue (Red)
        Property illinois = createProperty(24, "Illinois Avenue", ColorGroup.RED, 240, 150, 20, 100, 300, 750, 925, 1100);
        tiles.add(new PropertyTile(24, "Illinois Avenue", illinois));
        
        // Position 25: B&O Railroad
        tiles.add(new RailroadTile(25, "B&O Railroad"));
        
        // Position 26: Atlantic Avenue (Yellow)
        Property atlantic = createProperty(26, "Atlantic Avenue", ColorGroup.YELLOW, 260, 150, 22, 110, 330, 800, 975, 1150);
        tiles.add(new PropertyTile(26, "Atlantic Avenue", atlantic));
        
        // Position 27: Ventnor Avenue (Yellow)
        Property ventnor = createProperty(27, "Ventnor Avenue", ColorGroup.YELLOW, 260, 150, 22, 110, 330, 800, 975, 1150);
        tiles.add(new PropertyTile(27, "Ventnor Avenue", ventnor));
        
        // Position 28: Water Works
        tiles.add(new UtilityTile(28, "Water Works"));
        
        // Position 29: Marvin Gardens (Yellow)
        Property marvin = createProperty(29, "Marvin Gardens", ColorGroup.YELLOW, 280, 150, 24, 120, 360, 850, 1025, 1200);
        tiles.add(new PropertyTile(29, "Marvin Gardens", marvin));
        
        // Position 30: Go To Jail
        tiles.add(new GoToJailTile(30));
        
        // Position 31: Pacific Avenue (Green)
        Property pacific = createProperty(31, "Pacific Avenue", ColorGroup.GREEN, 300, 200, 26, 130, 390, 900, 1100, 1275);
        tiles.add(new PropertyTile(31, "Pacific Avenue", pacific));
        
        // Position 32: North Carolina Avenue (Green)
        Property northCarolina = createProperty(32, "North Carolina Avenue", ColorGroup.GREEN, 300, 200, 26, 130, 390, 900, 1100, 1275);
        tiles.add(new PropertyTile(32, "North Carolina Avenue", northCarolina));
        
        // Position 33: Community Chest
        tiles.add(new CommunityChestTile(33));
        
        // Position 34: Pennsylvania Avenue (Green)
        Property pennsylvania = createProperty(34, "Pennsylvania Avenue", ColorGroup.GREEN, 320, 200, 28, 150, 450, 1000, 1200, 1400);
        tiles.add(new PropertyTile(34, "Pennsylvania Avenue", pennsylvania));
        
        // Position 35: Short Line Railroad
        tiles.add(new RailroadTile(35, "Short Line"));
        
        // Position 36: Chance
        tiles.add(new ChanceTile(36));
        
        // Position 37: Park Place (Dark Blue)
        Property parkPlace = createProperty(37, "Park Place", ColorGroup.DARK_BLUE, 350, 200, 35, 175, 500, 1100, 1300, 1500);
        tiles.add(new PropertyTile(37, "Park Place", parkPlace));
        
        // Position 38: Luxury Tax
        tiles.add(new TaxTile(38, "Luxury Tax", 100));
        
        // Position 39: Boardwalk (Dark Blue)
        Property boardwalk = createProperty(39, "Boardwalk", ColorGroup.DARK_BLUE, 400, 200, 50, 200, 600, 1400, 1700, 2000);
        tiles.add(new PropertyTile(39, "Boardwalk", boardwalk));
    }
    
    /**
     * Creates a property with all rent values
     */
    private Property createProperty(int id, String name, ColorGroup colorGroup, int price, int buildingCost,
                                    int baseRent, int rent1House, int rent2Houses, int rent3Houses, 
                                    int rent4Houses, int rentHotel) {
        Property property = new Property(id, name, colorGroup, price, buildingCost);
        property.setRentValues(baseRent, rent1House, rent2Houses, rent3Houses, rent4Houses, rentHotel);
        allProperties.put(id, property);
        return property;
    }
    
    /**
     * Gets a tile by position
     * @param position The board position (0-39)
     * @return The tile at that position
     */
    public Tile getTile(int position) {
        int normalizedPosition = normalizePosition(position);
        return tiles.get(normalizedPosition);
    }
    
    /**
     * Calculates new position after moving
     * @param currentPosition Current position
     * @param steps Number of steps to move
     * @return New position after moving
     */
    public int movePlayer(int currentPosition, int steps) {
        return normalizePosition(currentPosition + steps);
    }
    
    /**
     * Checks if player passed GO
     * @param oldPosition Position before move
     * @param newPosition Position after move
     * @return true if player passed GO
     */
    public boolean passedGo(int oldPosition, int newPosition) {
        return newPosition < oldPosition && newPosition != GO_POSITION;
    }
    
    /**
     * Checks if player landed on GO
     * @param position Current position
     * @return true if on GO
     */
    public boolean isOnGo(int position) {
        return position == GO_POSITION;
    }
    
    /**
     * Gets the distance to GO from a position
     * @param position Current position
     * @return Number of spaces to GO
     */
    public int getDistanceToGo(int position) {
        if (position == GO_POSITION) {
            return 0;
        }
        return BOARD_SIZE - position;
    }
    
    /**
     * Gets the distance between two positions
     * @param from Starting position
     * @param to Ending position
     * @return Distance (always positive, moving forward)
     */
    public int getDistance(int from, int to) {
        if (to >= from) {
            return to - from;
        }
        return BOARD_SIZE - from + to;
    }
    
    /**
     * Normalizes a position to be within board bounds
     * @param position Raw position
     * @return Position between 0 and BOARD_SIZE-1
     */
    public int normalizePosition(int position) {
        while (position < 0) {
            position += BOARD_SIZE;
        }
        return position % BOARD_SIZE;
    }
    
    /**
     * Gets a property by ID
     * @param propertyId The property ID
     * @return The property, or null if not found
     */
    public Property getProperty(int propertyId) {
        return allProperties.get(propertyId);
    }
    
    /**
     * Gets all properties as a HashTable
     * @return HashTable of all properties
     */
    public HashTable<Integer, Property> getAllProperties() {
        return allProperties;
    }
    
    /**
     * Gets all tiles
     * @return ArrayList of all tiles
     */
    public ArrayList<Tile> getTiles() {
        return tiles;
    }
    
    /**
     * Gets the tile count
     * @return Number of tiles (40)
     */
    public int getTileCount() {
        return tiles.size();
    }
    
    /**
     * Finds the nearest tile of a specific type
     * @param currentPosition Starting position
     * @param tileType Type to find
     * @return Position of nearest matching tile
     */
    public int findNearestTile(int currentPosition, TileType tileType) {
        for (int i = 1; i < BOARD_SIZE; i++) {
            int checkPos = normalizePosition(currentPosition + i);
            Tile tile = getTile(checkPos);
            if (tile.getTileType() == tileType) {
                return checkPos;
            }
        }
        return currentPosition; // Not found
    }
    
    /**
     * Finds the nearest Railroad
     * @param currentPosition Starting position
     * @return Position of nearest Railroad
     */
    public int findNearestRailroad(int currentPosition) {
        return findNearestTile(currentPosition, TileType.RAILROAD);
    }
    
    /**
     * Finds the nearest Utility
     * @param currentPosition Starting position
     * @return Position of nearest Utility
     */
    public int findNearestUtility(int currentPosition) {
        return findNearestTile(currentPosition, TileType.UTILITY);
    }
    
    /**
     * Gets all properties in a color group
     * @param colorGroup The color group
     * @return ArrayList of properties in that group
     */
    public ArrayList<Property> getPropertiesInColorGroup(ColorGroup colorGroup) {
        ArrayList<Property> result = new ArrayList<>();
        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = tiles.get(i);
            if (tile instanceof PropertyTile) {
                Property property = ((PropertyTile) tile).getProperty();
                if (property.getColorGroup() == colorGroup) {
                    result.add(property);
                }
            }
        }
        return result;
    }
    
    /**
     * Gets all railroad positions
     * @return ArrayList of railroad positions
     */
    public ArrayList<Integer> getRailroadPositions() {
        ArrayList<Integer> positions = new ArrayList<>();
        positions.add(READING_RAILROAD);
        positions.add(PENNSYLVANIA_RAILROAD);
        positions.add(B_AND_O_RAILROAD);
        positions.add(SHORT_LINE_RAILROAD);
        return positions;
    }
    
    /**
     * Gets all utility positions
     * @return ArrayList of utility positions
     */
    public ArrayList<Integer> getUtilityPositions() {
        ArrayList<Integer> positions = new ArrayList<>();
        positions.add(ELECTRIC_COMPANY);
        positions.add(WATER_WORKS);
        return positions;
    }
    
    @Override
    public String toString() {
        return "Board{" +
                "tiles=" + tiles.size() +
                ", properties=" + allProperties.size() +
                '}';
    }
}
