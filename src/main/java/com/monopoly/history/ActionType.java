package com.monopoly.history;

/**
 * Enum representing types of game actions for history tracking.
 */
public enum ActionType {
    
    // Movement actions
    ROLL_DICE,
    MOVE,
    PASS_GO,
    
    // Property actions
    BUY_PROPERTY,
    AUCTION_WIN,
    MORTGAGE_PROPERTY,
    UNMORTGAGE_PROPERTY,
    
    // Building actions
    BUILD_HOUSE,
    BUILD_HOTEL,
    SELL_HOUSE,
    SELL_HOTEL,
    
    // Transaction actions
    PAY_RENT,
    PAY_TAX,
    RECEIVE_MONEY,
    PAY_MONEY,
    
    // Trade actions
    TRADE_PROPOSE,
    TRADE_ACCEPT,
    TRADE_DECLINE,
    TRADE_COMPLETE,
    
    // Jail actions
    GO_TO_JAIL,
    PAY_JAIL_FINE,
    USE_JAIL_CARD,
    ROLL_DOUBLES_JAIL,
    RELEASE_FROM_JAIL,
    
    // Card actions
    DRAW_CHANCE,
    DRAW_COMMUNITY_CHEST,
    EXECUTE_CARD,
    
    // Game state actions
    BANKRUPTCY,
    END_TURN,
    GAME_START,
    GAME_END

}
