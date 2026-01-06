package com.monopoly.model.property;

/**
 * Enum representing the color groups of properties in Monopoly.
 * Each color group has a specific number of properties.
 */
public enum ColorGroup {
    
    BROWN(2, "#8B4513"),           // Mediterranean Avenue, Baltic Avenue
    LIGHT_BLUE(3, "#87CEEB"),      // Oriental Avenue, Vermont Avenue, Connecticut Avenue
    PINK(3, "#FF69B4"),            // St. Charles Place, States Avenue, Virginia Avenue
    ORANGE(3, "#FFA500"),          // St. James Place, Tennessee Avenue, New York Avenue
    RED(3, "#FF0000"),             // Kentucky Avenue, Indiana Avenue, Illinois Avenue
    YELLOW(3, "#FFFF00"),          // Atlantic Avenue, Ventnor Avenue, Marvin Gardens
    GREEN(3, "#008000"),           // Pacific Avenue, North Carolina Avenue, Pennsylvania Avenue
    DARK_BLUE(2, "#00008B"),       // Park Place, Boardwalk
    RAILROAD(4, "#000000"),        // 4 Railroads (special group)
    UTILITY(2, "#808080");         // Electric Company, Water Works (special group)

    private final int propertyCount;
    private final String colorCode;

    ColorGroup(int propertyCount, String colorCode) {
        this.propertyCount = propertyCount;
        this.colorCode = colorCode;
    }

    public int getPropertyCount() {
        return propertyCount;
    }

    public String getColorCode() {
        return colorCode;
    }

    public boolean isStandardProperty() {
        return this != RAILROAD && this != UTILITY;
    }

    public boolean isRailroad() {
        return this == RAILROAD;
    }

    public boolean isUtility() {
        return this == UTILITY;
    }
}
