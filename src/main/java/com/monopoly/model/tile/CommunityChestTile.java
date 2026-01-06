package com.monopoly.model.tile;

import java.io.Serializable;

/**
 * Represents a Community Chest tile on the board.
 * Player draws a Community Chest card when landing here.
 */
public class CommunityChestTile extends Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a Community Chest tile.
     * @param id unique identifier
     * @param position board position
     */
    public CommunityChestTile(int id, int position) {
        super(id, "Community Chest", position, TileType.COMMUNITY_CHEST);
    }
    
    /**
     * Creates a Community Chest tile with position as id.
     * @param position board position (also used as id)
     */
    public CommunityChestTile(int position) {
        super(position, "Community Chest", position, TileType.COMMUNITY_CHEST);
    }

    @Override
    public void onLand() {
        // Player draws Community Chest card (handled by GameEngine)
    }

    @Override
    public void onPass() {
        // No action when passing
    }

    @Override
    public String toString() {
        return "CommunityChestTile{position=" + getPosition() + "}";
    }
}
