package com.monopoly.network.protocol;

/**
 * Enum representing all message types in the protocol.
 */
public enum MessageType {
    
    // Client -> Server commands
    HELLO,              // Register as player
    ROLL_DICE,          // Request movement
    BUY_PROPERTY,       // Buy current tile
    DECLINE_BUY,        // Decline to buy, triggers auction
    BID,                // Place auction bid
    PASS_BID,           // Pass on bidding
    PROPOSE_TRADE,      // Initiate trade
    ACCEPT_TRADE,       // Accept trade proposal
    DECLINE_TRADE,      // Decline trade proposal
    CANCEL_TRADE,       // Cancel own trade proposal
    BUILD,              // Build house/hotel
    SELL_BUILDING,      // Sell house/hotel
    MORTGAGE,           // Mortgage property
    UNMORTGAGE,         // Unmortgage property
    JAIL_PAY_FINE,      // Pay fine to leave jail
    JAIL_USE_CARD,      // Use get out of jail card
    JAIL_ROLL,          // Attempt to roll doubles in jail
    UNDO,               // Request undo
    REDO,               // Request redo
    END_TURN,           // Finish turn
    DISCONNECT,         // Client disconnecting
    
    // Server -> Client events
    STATE_UPDATE,       // Full or delta game state
    EVENT_LOG,          // Text description of events
    ERROR,              // Error message
    GAME_START,         // Game is starting
    GAME_END,           // Game has ended
    TURN_START,         // New turn beginning
    AUCTION_START,      // Auction started
    AUCTION_UPDATE,     // Auction bid update
    AUCTION_END,        // Auction ended
    TRADE_PROPOSAL,     // Trade proposed to you
    TRADE_UPDATE,       // Trade status update
    PLAYER_JOINED,      // New player joined
    PLAYER_LEFT,        // Player disconnected
    PLAYER_BANKRUPT,    // Player went bankrupt
    DICE_RESULT,        // Result of dice roll
    CARD_DRAWN,         // Card was drawn
    JAIL_STATUS         // Jail status update

}
