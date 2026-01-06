package com.monopoly.model.card;

/**
 * Enum representing all possible card effects in the game.
 */
public enum CardEffect {
    
    // Money effects
    RECEIVE_MONEY,           // Receive money from bank
    PAY_MONEY,               // Pay money to bank
    PAY_EACH_PLAYER,         // Pay each player a fixed amount
    RECEIVE_FROM_EACH_PLAYER, // Receive from each player
    
    // Movement effects
    ADVANCE_TO,              // Move to specific location
    ADVANCE_TO_NEAREST_RAILROAD,
    ADVANCE_TO_NEAREST_UTILITY,
    GO_BACK,                 // Move back N spaces
    
    // Jail effects
    GO_TO_JAIL,              // Go directly to jail
    GET_OUT_OF_JAIL_FREE,    // Keep this card until used
    
    // Property effects
    STREET_REPAIRS,          // Pay per house and hotel
    GENERAL_REPAIRS          // Pay per house and hotel (different amounts)

    // TODO: Add any additional effects needed

}
