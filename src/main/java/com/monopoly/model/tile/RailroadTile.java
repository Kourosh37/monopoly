package com.monopoly.model.tile;

import java.io.Serializable;

/**
 * Represents a Railroad tile on the board.
 * Rent depends on how many railroads the owner possesses.
 */
public class RailroadTile extends Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int PURCHASE_PRICE = 200;
    public static final int MORTGAGE_VALUE = 100;
    public static final int[] RENT_BY_COUNT = {25, 50, 100, 200}; // 1, 2, 3, 4 railroads

    private int ownerId;
    private boolean mortgaged;

    /**
     * Creates a new Railroad tile.
     * @param id unique identifier
     * @param name railroad name
     * @param position board position
     */
    public RailroadTile(int id, String name, int position) {
        super(id, name, position, TileType.RAILROAD);
        this.ownerId = -1;
        this.mortgaged = false;
    }
    
    /**
     * Creates a new Railroad tile with position as id.
     * @param position board position (also used as id)
     * @param name railroad name
     */
    public RailroadTile(int position, String name) {
        super(position, name, position, TileType.RAILROAD);
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
     * Calculates rent based on number of railroads owned.
     * @param railroadsOwned number of railroads owner possesses (1-4)
     * @return rent amount
     */
    public int calculateRent(int railroadsOwned) {
        if (mortgaged) return 0;
        if (railroadsOwned < 1 || railroadsOwned > 4) return 0;
        return RENT_BY_COUNT[railroadsOwned - 1];
    }

    @Override
    public void onLand() {
        // Logic handled by GameEngine
    }

    @Override
    public void onPass() {
        // Nothing happens when passing a railroad
    }

    @Override
    public String toString() {
        return "RailroadTile{" +
                "name='" + getName() + '\'' +
                ", position=" + getPosition() +
                ", owner=" + (ownerId >= 0 ? ownerId : "Bank") +
                ", mortgaged=" + mortgaged +
                '}';
    }
}
