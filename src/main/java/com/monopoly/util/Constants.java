package com.monopoly.util;

/**
 * Game constants and configuration values.
 */
public final class Constants {

    private Constants() {} // Prevent instantiation

    // Network Constants
    public static final int DEFAULT_SERVER_PORT = 12345;
    public static final int MAX_PLAYERS = 4;
    public static final int CONNECTION_TIMEOUT_MS = 30000;
    public static final int HEARTBEAT_INTERVAL_MS = 5000;
    
    // Game Constants
    public static final int BOARD_SIZE = 40;
    public static final int STARTING_MONEY = 1500;
    public static final int GO_REWARD = 200;
    public static final int JAIL_POSITION = 10;
    public static final int GO_TO_JAIL_POSITION = 30;
    public static final int FREE_PARKING_POSITION = 20;
    public static final int GO_POSITION = 0;
    
    // Jail Constants
    public static final int JAIL_FINE = 50;
    public static final int MAX_TURNS_IN_JAIL = 2;
    public static final int MAX_DOUBLES_BEFORE_JAIL = 3;
    
    // Building Constants
    public static final int MAX_HOUSES_PER_PROPERTY = 4;
    public static final int TOTAL_HOUSES_IN_GAME = 32;
    public static final int TOTAL_HOTELS_IN_GAME = 12;
    
    // Tax Constants
    public static final int INCOME_TAX_AMOUNT = 200;
    public static final int LUXURY_TAX_AMOUNT = 100;
    
    // Railroad Rent Values
    public static final int[] RAILROAD_RENT = {25, 50, 100, 200};
    
    // Utility Rent Multipliers
    public static final int UTILITY_MULTIPLIER_ONE = 4;
    public static final int UTILITY_MULTIPLIER_BOTH = 10;
    
    // Special IDs
    public static final int BANK_ID = -1;
    public static final int SERVER_ID = 0;
    
    // UI Constants
    public static final int TILE_WIDTH = 80;
    public static final int TILE_HEIGHT = 100;
    public static final int TOKEN_SIZE = 30;
    public static final int ANIMATION_DURATION_MS = 300;

}
