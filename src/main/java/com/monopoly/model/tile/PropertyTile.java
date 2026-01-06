package com.monopoly.model.tile;

import com.monopoly.model.property.Property;
import java.io.Serializable;

/**
 * Represents a property tile on the board.
 * Can be owned, developed with houses/hotels, and collects rent.
 */
public class PropertyTile extends Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    private Property property;

    /**
     * Creates a new property tile.
     * @param id unique identifier
     * @param position board position
     * @param property the associated property
     */
    public PropertyTile(int id, int position, Property property) {
        super(id, property.getName(), position, TileType.PROPERTY);
        this.property = property;
    }
    
    /**
     * Creates a new property tile with position as id.
     * @param position board position (also used as id)
     * @param name property name
     * @param property the associated property
     */
    public PropertyTile(int position, String name, Property property) {
        super(position, name, position, TileType.PROPERTY);
        this.property = property;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public boolean isOwned() {
        return property != null && property.isOwned();
    }

    public int getOwnerId() {
        return property != null ? property.getOwnerId() : -1;
    }

    @Override
    public void onLand() {
        // Logic handled by GameEngine - either pay rent or offer to buy
    }

    @Override
    public void onPass() {
        // No action when passing a property
    }

    @Override
    public String toString() {
        return "PropertyTile{" +
                "position=" + getPosition() +
                ", property=" + (property != null ? property.getName() : "null") +
                ", owned=" + isOwned() +
                '}';
    }
}
