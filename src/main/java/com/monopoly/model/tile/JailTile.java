package com.monopoly.model.tile;

import java.io.Serializable;

/**
 * Represents the Jail tile on the board.
 * Players can be "Just Visiting" or "In Jail".
 */
public class JailTile extends Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int JAIL_POSITION = 10;
    public static final int BAIL_AMOUNT = 50;

    /**
     * Creates the Jail tile.
     */
    public JailTile() {
        super(10, "Jail / Just Visiting", JAIL_POSITION, TileType.JAIL);
    }
    
    /**
     * Creates the Jail tile with position.
     * @param position Board position (should always be 10)
     */
    public JailTile(int position) {
        super(10, "Jail / Just Visiting", position, TileType.JAIL);
    }

    public int getBailAmount() {
        return BAIL_AMOUNT;
    }

    @Override
    public void onLand() {
        // Just visiting - no action
    }

    @Override
    public void onPass() {
        // No action when passing
    }

    @Override
    public String toString() {
        return "JailTile{position=" + JAIL_POSITION + ", bail=$" + BAIL_AMOUNT + "}";
    }
}
