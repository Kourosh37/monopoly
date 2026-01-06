package com.monopoly.model.tile;

import java.io.Serializable;

/**
 * Abstract base class for all tile types on the Monopoly board.
 * Extended by specific tile types (Property, Railroad, Utility, etc.)
 */
public abstract class Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final int id;
    protected final String name;
    protected final int position;
    protected final TileType tileType;

    /**
     * Creates a new tile.
     * @param id unique tile identifier
     * @param name display name
     * @param position board position (0-39)
     * @param tileType type of tile
     */
    public Tile(int id, String name, int position, TileType tileType) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.tileType = tileType;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public TileType getTileType() {
        return tileType;
    }

    /**
     * Called when a player lands on this tile.
     * Must be implemented by subclasses.
     */
    public abstract void onLand();

    /**
     * Called when a player passes over this tile.
     * Default implementation does nothing.
     */
    public void onPass() {
        // Default: do nothing when passing
    }

    /**
     * Checks if this tile is purchasable.
     */
    public boolean isPurchasable() {
        return tileType.isPurchasable();
    }

    @Override
    public String toString() {
        return "Tile{" +
                "position=" + position +
                ", name='" + name + '\'' +
                ", type=" + tileType +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tile tile = (Tile) obj;
        return id == tile.id && position == tile.position;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
