package com.monopoly.model.player;

/**
 * Represents the playing pieces/tokens in Monopoly.
 */
public enum PlayerToken {
    CAR("Car", "🚗"),
    DOG("Dog", "🐕"),
    HAT("Hat", "🎩"),
    SHIP("Ship", "🚢"),
    BOOT("Boot", "👢"),
    THIMBLE("Thimble", "🧵"),
    WHEELBARROW("Wheelbarrow", "🛒"),
    CAT("Cat", "🐈");
    
    private final String displayName;
    private final String emoji;
    
    PlayerToken(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
