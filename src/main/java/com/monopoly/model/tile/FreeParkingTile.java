package com.monopoly.model.tile;

import java.io.Serializable;

/**
 * Represents the Free Parking tile on the board.
 * Standard rules: no action. House rules may vary.
 */
public class FreeParkingTile extends Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int FREE_PARKING_POSITION = 20;

    /**
     * Creates the Free Parking tile with default position.
     */
    public FreeParkingTile() {
        super(20, "Free Parking", FREE_PARKING_POSITION, TileType.FREE_PARKING);
    }
    
    /**
     * Creates the Free Parking tile at specified position.
     * @param position board position
     */
    public FreeParkingTile(int position) {
        super(position, "Free Parking", position, TileType.FREE_PARKING);
    }

    @Override
    public void onLand() {
        // Standard rules: no action
        // House rules variant could collect taxes paid and award to player landing here
    }

    @Override
    public void onPass() {
        // No action when passing
    }

    @Override
    public String toString() {
        return "FreeParkingTile{position=" + getPosition() + "}";
    }
}
