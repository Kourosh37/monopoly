package com.monopoly.model.enums;

/**
 * Represents the different types of tiles on the board.
 */
public enum TileType {
    GO("GO"),
    PROPERTY("Property"),
    COMMUNITY_CHEST("Community Chest"),
    CHANCE("Chance"),
    TAX("Tax"),
    RAILROAD("Railroad"),
    UTILITY("Utility"),
    JAIL("Jail"),
    FREE_PARKING("Free Parking"),
    GO_TO_JAIL("Go To Jail");
    
    private final String displayName;
    
    TileType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
