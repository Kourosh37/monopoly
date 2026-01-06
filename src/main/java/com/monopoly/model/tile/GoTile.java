package com.monopoly.model.tile;

import java.io.Serializable;

/**
 * Represents the GO tile on the board.
 * Players receive money when passing or landing on GO.
 */
public class GoTile extends Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int PASS_REWARD = 200;
    public static final int LAND_REWARD = 200; // Some variants give double for landing

    /**
     * Creates the GO tile.
     */
    public GoTile() {
        super(0, "GO", 0, TileType.GO);
    }
    
    /**
     * Creates the GO tile with position.
     * @param position Board position (should always be 0)
     */
    public GoTile(int position) {
        super(0, "GO", position, TileType.GO);
    }

    public int getPassReward() {
        return PASS_REWARD;
    }

    public int getLandReward() {
        return LAND_REWARD;
    }

    @Override
    public void onLand() {
        // Player receives GO salary (handled by Player.move() or GameEngine)
    }

    @Override
    public void onPass() {
        // Player receives $200 (handled by Player.move())
    }

    @Override
    public String toString() {
        return "GoTile{position=0, reward=$" + PASS_REWARD + "}";
    }
}
