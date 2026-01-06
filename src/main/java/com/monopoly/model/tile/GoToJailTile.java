package com.monopoly.model.tile;

import java.io.Serializable;

/**
 * Represents the "Go To Jail" tile on the board.
 * Sends player directly to jail without passing GO.
 */
public class GoToJailTile extends Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int GO_TO_JAIL_POSITION = 30;
    public static final int JAIL_POSITION = 10;

    /**
     * Creates the Go To Jail tile with default position.
     */
    public GoToJailTile() {
        super(30, "Go To Jail", GO_TO_JAIL_POSITION, TileType.GO_TO_JAIL);
    }
    
    /**
     * Creates the Go To Jail tile at specified position.
     * @param position board position
     */
    public GoToJailTile(int position) {
        super(position, "Go To Jail", position, TileType.GO_TO_JAIL);
    }

    public int getJailPosition() {
        return JAIL_POSITION;
    }

    @Override
    public void onLand() {
        // Player is sent to jail (handled by GameEngine)
    }

    @Override
    public void onPass() {
        // Cannot pass this tile - you land on it and go to jail
    }

    @Override
    public String toString() {
        return "GoToJailTile{position=" + getPosition() + "}";
    }
}
