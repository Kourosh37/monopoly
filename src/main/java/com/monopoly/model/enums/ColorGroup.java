package com.monopoly.model.enums;

/**
 * Alias for model.property.ColorGroup for backward compatibility.
 * Prefer using com.monopoly.model.property.ColorGroup directly.
 */
public enum ColorGroup {
    BROWN(2),
    LIGHT_BLUE(3),
    PINK(3),
    ORANGE(3),
    RED(3),
    YELLOW(3),
    GREEN(3),
    DARK_BLUE(2),
    RAILROAD(4),
    UTILITY(2);
    
    private final int propertyCount;
    
    ColorGroup(int propertyCount) {
        this.propertyCount = propertyCount;
    }
    
    public int getPropertyCount() {
        return propertyCount;
    }
    
    public int getPropertiesInGroup() {
        return propertyCount;
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
