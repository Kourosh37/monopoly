package com.monopoly.model.property;

/**
 * Enum representing the color groups of properties in Monopoly.
 * Each color group has a specific number of properties.
 */
public enum ColorGroup {
    
    BROWN(2),         // Mediterranean Avenue, Baltic Avenue
    LIGHT_BLUE(3),    // Oriental Avenue, Vermont Avenue, Connecticut Avenue
    PINK(3),          // St. Charles Place, States Avenue, Virginia Avenue
    ORANGE(3),        // St. James Place, Tennessee Avenue, New York Avenue
    RED(3),           // Kentucky Avenue, Indiana Avenue, Illinois Avenue
    YELLOW(3),        // Atlantic Avenue, Ventnor Avenue, Marvin Gardens
    GREEN(3),         // Pacific Avenue, North Carolina Avenue, Pennsylvania Avenue
    DARK_BLUE(2),     // Park Place, Boardwalk
    RAILROAD(4),      // 4 Railroads (special group)
    UTILITY(2);       // Electric Company, Water Works (special group)

    private final int propertyCount;

    ColorGroup(int propertyCount) {
        this.propertyCount = propertyCount;
    }

    // TODO: Implement getPropertyCount()
    // TODO: Implement getColorCode() - for GUI display
    // TODO: Implement isStandardProperty() - true for colored, false for railroad/utility

}
