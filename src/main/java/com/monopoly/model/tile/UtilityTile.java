package com.monopoly.model.tile;

import java.io.Serializable;

/**
 * Represents a Utility tile on the board (Electric Company, Water Works).
 * Rent is calculated based on dice roll.
 */
public class UtilityTile extends Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int PURCHASE_PRICE = 150;
    public static final int MORTGAGE_VALUE = 75;
    public static final int MULTIPLIER_ONE_UTILITY = 4;
    public static final int MULTIPLIER_BOTH_UTILITIES = 10;
    
    // Aliases for compatibility
    public static final int SINGLE_MULTIPLIER = MULTIPLIER_ONE_UTILITY;
    public static final int BOTH_MULTIPLIER = MULTIPLIER_BOTH_UTILITIES;

    private int ownerId;
    private boolean mortgaged;

    /**
     * Creates a new Utility tile.
     * @param id unique identifier
     * @param name utility name
     * @param position board position
     */
    public UtilityTile(int id, String name, int position) {
        super(id, name, position, TileType.UTILITY);
        this.ownerId = -1;
        this.mortgaged = false;
    }
    
    /**
     * Creates a new Utility tile with position as id.
     * @param position board position (also used as id)
     * @param name utility name
     */
    public UtilityTile(int position, String name) {
        super(position, name, position, TileType.UTILITY);
        this.ownerId = -1;
        this.mortgaged = false;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isOwned() {
        return ownerId >= 0;
    }

    public int getPurchasePrice() {
        return PURCHASE_PRICE;
    }

    public int getMortgageValue() {
        return MORTGAGE_VALUE;
    }

    public int getUnmortgageCost() {
        return (int) (MORTGAGE_VALUE * 1.1);
    }

    public boolean isMortgaged() {
        return mortgaged;
    }

    public boolean mortgage() {
        if (!mortgaged) {
            mortgaged = true;
            return true;
        }
        return false;
    }

    public boolean unmortgage() {
        if (mortgaged) {
            mortgaged = false;
            return true;
        }
        return false;
    }

    /**
     * Calculates rent based on dice roll and utilities owned.
     * @param diceRoll the sum of the dice roll
     * @param utilitiesOwned number of utilities owner possesses (1 or 2)
     * @return rent amount
     */
    public int calculateRent(int diceRoll, int utilitiesOwned) {
        if (mortgaged) return 0;
        if (utilitiesOwned == 2) {
            return diceRoll * MULTIPLIER_BOTH_UTILITIES;
        }
        return diceRoll * MULTIPLIER_ONE_UTILITY;
    }

    /**
     * Gets the multiplier based on utilities owned.
     */
    public int getMultiplier(int utilitiesOwned) {
        return utilitiesOwned == 2 ? MULTIPLIER_BOTH_UTILITIES : MULTIPLIER_ONE_UTILITY;
    }

    @Override
    public void onLand() {
        // Logic handled by GameEngine
    }

    @Override
    public void onPass() {
        // Nothing happens when passing a utility
    }

    @Override
    public String toString() {
        return "UtilityTile{" +
                "name='" + getName() + '\'' +
                ", position=" + getPosition() +
                ", owner=" + (ownerId >= 0 ? ownerId : "Bank") +
                ", mortgaged=" + mortgaged +
                '}';
    }
}
