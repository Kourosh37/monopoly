package com.monopoly.model.enums;

/**
 * Represents the current phase of a player's turn.
 * Different phases allow different actions.
 */
public enum TurnPhase {
    WAITING_FOR_PLAYERS("Waiting for players to join"),
    TURN_START("Turn starting"),
    PRE_ROLL("Before rolling dice"),
    ROLLING("Rolling dice"),
    POST_ROLL("After rolling"),
    MOVING("Moving token"),
    LANDED("Landed on tile"),
    POST_ACTION("After action completed"),
    PROPERTY_DECISION("Deciding to buy property"),
    AWAITING_DECISION("Waiting for player decision"),
    DRAWING_CARD("Drawing a card"),
    AUCTION("Property auction in progress"),
    PAYING_RENT("Paying rent"),
    CARD_ACTION("Processing card action"),
    IN_JAIL("Player is in jail"),
    IN_DEBT("Player owes money"),
    BANKRUPTCY("Player is bankrupt"),
    TRADING("Trade in progress"),
    BUILDING("Building houses/hotels"),
    WAITING("Waiting for other player"),
    TURN_END("Turn ending"),
    GAME_OVER("Game has ended");
    
    private final String description;
    
    TurnPhase(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
