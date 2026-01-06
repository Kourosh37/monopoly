package com.monopoly.model.player;

/**
 * Enum representing the different token types players can choose.
 */
public enum TokenType {
    
    CAR("Car", "/images/tokens/car.png"),
    DOG("Dog", "/images/tokens/dog.png"),
    HAT("Top Hat", "/images/tokens/hat.png"),
    SHIP("Battleship", "/images/tokens/ship.png"),
    THIMBLE("Thimble", "/images/tokens/thimble.png"),
    WHEELBARROW("Wheelbarrow", "/images/tokens/wheelbarrow.png"),
    BOOT("Boot", "/images/tokens/boot.png"),
    CAT("Cat", "/images/tokens/cat.png");

    private final String displayName;
    private final String imagePath;

    TokenType(String displayName, String imagePath) {
        this.displayName = displayName;
        this.imagePath = imagePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public static TokenType fromDisplayName(String name) {
        for (TokenType token : values()) {
            if (token.displayName.equalsIgnoreCase(name)) {
                return token;
            }
        }
        return null;
    }
}
