package com.monopoly.logic;

import com.monopoly.datastructures.ArrayList;
import com.monopoly.datastructures.HashTable;
import com.monopoly.model.property.ColorGroup;
import com.monopoly.model.game.Board;
import com.monopoly.model.game.GameState;
import com.monopoly.model.player.Player;
import com.monopoly.model.property.Property;
import com.monopoly.model.tile.RailroadTile;
import com.monopoly.model.tile.Tile;
import com.monopoly.model.tile.UtilityTile;

/**
 * Calculates rent for properties, railroads, and utilities.
 */
public class RentCalculator {

    private final GameState gameState;
    
    /**
     * Creates a RentCalculator for a game
     * @param gameState The game state
     */
    public RentCalculator(GameState gameState) {
        this.gameState = gameState;
    }
    
    /**
     * Calculates rent for a property
     * @param property The property
     * @return The rent amount
     */
    public int calculatePropertyRent(Property property) {
        if (property == null || property.isMortgaged()) {
            return 0;
        }
        
        int ownerId = property.getOwnerId();
        if (ownerId < 0) {
            return 0; // Unowned
        }
        
        // Check for hotel first
        if (property.hasHotel()) {
            return property.getRentWithHotel();
        }
        
        // Check for houses
        int houses = property.getNumberOfHouses();
        if (houses > 0) {
            return property.getRentWithHouses(houses);
        }
        
        // Check for complete color set (doubles base rent)
        if (ownsCompleteColorGroup(ownerId, property.getColorGroup())) {
            return property.getRentWithColorSet();
        }
        
        // Base rent
        return property.getBaseRent();
    }
    
    /**
     * Calculates rent for a railroad
     * @param ownerId The owner's ID
     * @return The rent amount
     */
    public int calculateRailroadRent(int ownerId) {
        int railroadsOwned = getRailroadsOwnedCount(ownerId);
        if (railroadsOwned <= 0 || railroadsOwned > 4) {
            return 0;
        }
        return RailroadTile.RENT_BY_COUNT[railroadsOwned - 1];
    }
    
    /**
     * Calculates rent for a railroad, considering mortgage status
     * @param ownerId The owner's ID
     * @param railroadPosition The position of the railroad landed on
     * @return The rent amount
     */
    public int calculateRailroadRent(int ownerId, int railroadPosition) {
        // Check if this specific railroad is mortgaged
        Tile tile = gameState.getBoard().getTile(railroadPosition);
        if (tile instanceof RailroadTile) {
            RailroadTile railroad = (RailroadTile) tile;
            if (railroad.isMortgaged()) {
                return 0;
            }
        }
        return calculateRailroadRent(ownerId);
    }
    
    /**
     * Calculates rent for a utility
     * @param ownerId The owner's ID
     * @param diceRoll The dice roll value
     * @return The rent amount
     */
    public int calculateUtilityRent(int ownerId, int diceRoll) {
        int utilitiesOwned = getUtilitiesOwnedCount(ownerId);
        
        if (utilitiesOwned <= 0) {
            return 0;
        }
        
        int multiplier = (utilitiesOwned >= 2) ? 
            UtilityTile.BOTH_MULTIPLIER : UtilityTile.SINGLE_MULTIPLIER;
        
        return diceRoll * multiplier;
    }
    
    /**
     * Calculates utility rent, considering mortgage status
     * @param ownerId The owner's ID
     * @param diceRoll The dice roll
     * @param utilityPosition The position of the utility landed on
     * @return The rent amount
     */
    public int calculateUtilityRent(int ownerId, int diceRoll, int utilityPosition) {
        Tile tile = gameState.getBoard().getTile(utilityPosition);
        if (tile instanceof UtilityTile) {
            UtilityTile utility = (UtilityTile) tile;
            if (utility.isMortgaged()) {
                return 0;
            }
        }
        return calculateUtilityRent(ownerId, diceRoll);
    }
    
    /**
     * Checks if a player owns all properties in a color group
     * @param playerId The player's ID
     * @param colorGroup The color group
     * @return true if player owns all properties in the group
     */
    public boolean ownsCompleteColorGroup(int playerId, ColorGroup colorGroup) {
        if (colorGroup == null || !colorGroup.isStandardProperty()) {
            return false;
        }
        
        Player player = gameState.getPlayer(playerId);
        if (player == null) {
            return false;
        }
        
        ArrayList<Property> groupProperties = gameState.getBoard().getPropertiesInColorGroup(colorGroup);
        int ownedCount = 0;
        
        for (int i = 0; i < groupProperties.size(); i++) {
            Property prop = groupProperties.get(i);
            if (prop.getOwnerId() == playerId) {
                ownedCount++;
            }
        }
        
        return ownedCount == colorGroup.getPropertyCount();
    }
    
    /**
     * Counts railroads owned by a player
     * @param playerId The player's ID
     * @return Number of railroads owned
     */
    public int getRailroadsOwnedCount(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) {
            return 0;
        }
        
        int count = 0;
        ArrayList<Integer> railroadPositions = gameState.getBoard().getRailroadPositions();
        
        for (int i = 0; i < railroadPositions.size(); i++) {
            int pos = railroadPositions.get(i);
            Tile tile = gameState.getBoard().getTile(pos);
            if (tile instanceof RailroadTile) {
                RailroadTile railroad = (RailroadTile) tile;
                if (railroad.getOwnerId() == playerId) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    /**
     * Counts utilities owned by a player
     * @param playerId The player's ID
     * @return Number of utilities owned
     */
    public int getUtilitiesOwnedCount(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) {
            return 0;
        }
        
        int count = 0;
        ArrayList<Integer> utilityPositions = gameState.getBoard().getUtilityPositions();
        
        for (int i = 0; i < utilityPositions.size(); i++) {
            int pos = utilityPositions.get(i);
            Tile tile = gameState.getBoard().getTile(pos);
            if (tile instanceof UtilityTile) {
                UtilityTile utility = (UtilityTile) tile;
                if (utility.getOwnerId() == playerId) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    /**
     * Gets the base rent for a property
     * @param property The property
     * @return Base rent amount
     */
    public int getBaseRent(Property property) {
        return property != null ? property.getBaseRent() : 0;
    }
    
    /**
     * Gets rent with complete color set
     * @param property The property
     * @return Rent with color set (double base)
     */
    public int getRentWithColorSet(Property property) {
        return property != null ? property.getRentWithColorSet() : 0;
    }
    
    /**
     * Gets rent with specific number of houses
     * @param property The property
     * @param houseCount Number of houses
     * @return Rent amount
     */
    public int getRentWithHouses(Property property, int houseCount) {
        if (property == null || houseCount < 1 || houseCount > 4) {
            return 0;
        }
        return property.getRentWithHouses(houseCount);
    }
    
    /**
     * Gets rent with hotel
     * @param property The property
     * @return Rent with hotel
     */
    public int getRentWithHotel(Property property) {
        return property != null ? property.getRentWithHotel() : 0;
    }
    
    /**
     * Gets potential rent if player builds to maximum
     * @param property The property
     * @return Maximum possible rent
     */
    public int getMaxPotentialRent(Property property) {
        return property != null ? property.getRentWithHotel() : 0;
    }
}
