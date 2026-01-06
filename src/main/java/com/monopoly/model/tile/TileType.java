package com.monopoly.model.tile;

/**
 * Enum representing all types of tiles on the Monopoly board.
 */
public enum TileType {
    
    GO("GO", true),
    PROPERTY("Property", false),
    RAILROAD("Railroad", false),
    UTILITY("Utility", false),
    CHANCE("Chance", true),
    COMMUNITY_CHEST("Community Chest", true),
    INCOME_TAX("Income Tax", true),
    LUXURY_TAX("Luxury Tax", true),
    TAX("Tax", true),
    JAIL("Jail", true),
    GO_TO_JAIL("Go To Jail", true),
    FREE_PARKING("Free Parking", true);

    private final String displayName;
    private final boolean isSpecialTile;

    TileType(String displayName, boolean isSpecialTile) {
        this.displayName = displayName;
        this.isSpecialTile = isSpecialTile;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSpecialTile() {
        return isSpecialTile;
    }

    public boolean isPurchasable() {
        return this == PROPERTY || this == RAILROAD || this == UTILITY;
    }

    public boolean drawsCard() {
        return this == CHANCE || this == COMMUNITY_CHEST;
    }
}
