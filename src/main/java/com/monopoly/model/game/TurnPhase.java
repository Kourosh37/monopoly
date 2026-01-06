package com.monopoly.model.game;

/**
 * Enum representing the different phases of a player's turn.
 * Implements a State Machine: TURN_START -> ROLL -> MOVE -> DECISION -> TURN_END
 */
public enum TurnPhase {
    
    TURN_START,    // Beginning of turn, before any action
    ROLL,          // Player needs to roll dice
    MOVE,          // Processing player movement
    DECISION,      // Player needs to make a decision (buy, auction, etc.)
    AUCTION,       // Auction in progress
    TRADE,         // Trade negotiation in progress
    JAIL_DECISION, // Player in jail needs to decide action
    TURN_END       // Turn completed, moving to next player

    // TODO: Add helper methods if needed
    // TODO: Implement getNextPhase()
    // TODO: Implement isActionAllowed(String action)

}
