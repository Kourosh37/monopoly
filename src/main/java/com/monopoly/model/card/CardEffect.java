package com.monopoly.model.card;

/**
 * Enum representing all possible card effects in the game.
 */
public enum CardEffect {
    
    // Money effects
    RECEIVE_MONEY("Receive money from bank"),
    PAY_MONEY("Pay money to bank"),
    PAY_EACH_PLAYER("Pay each player"),
    RECEIVE_FROM_EACH_PLAYER("Receive from each player"),
    
    // Aliases for compatibility
    COLLECT_FROM_BANK("Collect money from bank"),
    PAY_BANK("Pay money to bank"),
    COLLECT_FROM_EACH_PLAYER("Collect from each player"),
    
    // Movement effects
    ADVANCE_TO("Advance to location"),
    ADVANCE_TO_GO("Advance to GO"),
    ADVANCE_TO_NEAREST_RAILROAD("Advance to nearest railroad"),
    ADVANCE_TO_NEAREST_UTILITY("Advance to nearest utility"),
    GO_BACK("Go back spaces"),
    
    // Jail effects
    GO_TO_JAIL("Go directly to jail"),
    GET_OUT_OF_JAIL_FREE("Get out of jail free card"),
    
    // Property effects
    STREET_REPAIRS("Pay for street repairs"),
    GENERAL_REPAIRS("Pay for general repairs"),
    REPAIRS("Pay for repairs"),
    
    // Special effects
    CHAIRMAN_OF_BOARD("Pay each player as chairman"),
    BUILDING_LOAN_MATURES("Building loan matures");

    private final String description;

    CardEffect(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresMovement() {
        return this == ADVANCE_TO || this == ADVANCE_TO_GO || 
               this == ADVANCE_TO_NEAREST_RAILROAD || 
               this == ADVANCE_TO_NEAREST_UTILITY || this == GO_BACK;
    }

    public boolean isKeepable() {
        return this == GET_OUT_OF_JAIL_FREE;
    }

    public boolean affectsOtherPlayers() {
        return this == PAY_EACH_PLAYER || this == RECEIVE_FROM_EACH_PLAYER ||
               this == CHAIRMAN_OF_BOARD || this == COLLECT_FROM_EACH_PLAYER;
    }
}
