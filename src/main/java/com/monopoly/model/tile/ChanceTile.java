package com.monopoly.model.tile;

import java.io.Serializable;

/**
 * Represents a Chance tile on the board.
 * Player draws a Chance card when landing here.
 */
public class ChanceTile extends Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a Chance tile.
     * @param id unique identifier
     * @param position board position
     */
    public ChanceTile(int id, int position) {
        super(id, "Chance", position, TileType.CHANCE);
    }
    
    /**
     * Creates a Chance tile with position as id.
     * @param position board position (also used as id)
     */
    public ChanceTile(int position) {
        super(position, "Chance", position, TileType.CHANCE);
    }

    @Override
    public void onLand() {
        // Player draws Chance card (handled by GameEngine)
    }

    @Override
    public void onPass() {
        // No action when passing
    }

    @Override
    public String toString() {
        return "ChanceTile{position=" + getPosition() + "}";
    }
}
