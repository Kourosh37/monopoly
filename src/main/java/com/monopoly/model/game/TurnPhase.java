package com.monopoly.model.game;

import com.monopoly.datastructures.ArrayList;

/**
 * Enum representing the different phases of a player's turn.
 * Implements a State Machine: TURN_START -> ROLL -> MOVE -> DECISION -> TURN_END
 */
public enum TurnPhase {
    
    TURN_START("Turn Start"),
    PRE_ROLL("Before Rolling"),
    ROLL("Rolling Dice"),
    MOVE("Moving"),
    POST_MOVE("After Moving"),
    DECISION("Making Decision"),
    AUCTION("Auction"),
    TRADE("Trading"),
    JAIL_DECISION("Jail Decision"),
    BUILDING("Building"),
    MORTGAGE("Mortgage Management"),
    TURN_END("Turn End");

    private final String description;

    TurnPhase(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canRoll() {
        return this == TURN_START || this == PRE_ROLL || this == ROLL;
    }

    public boolean canTrade() {
        return this == PRE_ROLL || this == POST_MOVE || this == DECISION || this == TURN_END;
    }

    public boolean canBuild() {
        return this == PRE_ROLL || this == POST_MOVE || this == TURN_END;
    }

    public boolean canMortgage() {
        return this == PRE_ROLL || this == POST_MOVE || this == DECISION || this == TURN_END;
    }

    public boolean canEndTurn() {
        return this == POST_MOVE || this == DECISION || this == TURN_END;
    }

    public boolean requiresPlayerAction() {
        return this == ROLL || this == DECISION || this == AUCTION || 
               this == TRADE || this == JAIL_DECISION;
    }

    public ArrayList<String> getAllowedActions() {
        ArrayList<String> actions = new ArrayList<>();
        if (canRoll()) actions.add("ROLL");
        if (canTrade()) actions.add("TRADE");
        if (canBuild()) actions.add("BUILD");
        if (canMortgage()) actions.add("MORTGAGE");
        if (canEndTurn()) actions.add("END_TURN");
        return actions;
    }
}
