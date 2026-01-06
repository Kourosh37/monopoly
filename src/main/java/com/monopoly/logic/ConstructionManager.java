package com.monopoly.logic;

import com.monopoly.datastructures.ArrayList;
import com.monopoly.model.property.ColorGroup;
import com.monopoly.model.game.Bank;
import com.monopoly.model.game.GameState;
import com.monopoly.model.player.Player;
import com.monopoly.model.property.Property;

/**
 * Manages building construction (houses and hotels).
 * Can only build if you own the full Color Group.
 * Enforces even building rule.
 */
public class ConstructionManager {

    private final GameState gameState;
    private final RentCalculator rentCalculator;
    
    /**
     * Creates a ConstructionManager for a game
     * @param gameState The game state
     */
    public ConstructionManager(GameState gameState) {
        this.gameState = gameState;
        this.rentCalculator = new RentCalculator(gameState);
    }
    
    /**
     * Checks if a player can build a house on a property
     * @param playerId The player's ID
     * @param propertyId The property ID
     * @return true if building is allowed
     */
    public boolean canBuildHouse(int playerId, int propertyId) {
        Property property = gameState.getBoard().getProperty(propertyId);
        Player player = gameState.getPlayer(playerId);
        
        if (property == null || player == null) {
            return false;
        }
        
        // Check ownership
        if (property.getOwnerId() != playerId) {
            return false;
        }
        
        // Check if property already has hotel
        if (property.hasHotel()) {
            return false;
        }
        
        // Check if already at max houses
        if (property.getNumberOfHouses() >= 4) {
            return false;
        }
        
        // Check mortgage
        if (property.isMortgaged()) {
            return false;
        }
        
        // Must own complete color group
        if (!rentCalculator.ownsCompleteColorGroup(playerId, property.getColorGroup())) {
            return false;
        }
        
        // Check even building rule
        if (!isEvenBuildRule(playerId, property)) {
            return false;
        }
        
        // Check if player can afford
        if (player.getMoney() < property.getHouseCost()) {
            return false;
        }
        
        // Check if houses are available in bank
        if (!gameState.getBank().hasHousesAvailable()) {
            return false;
        }
        
        // Check no properties in group are mortgaged
        if (hasAnyMortgagedInGroup(playerId, property.getColorGroup())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if a player can build a hotel on a property
     * @param playerId The player's ID
     * @param propertyId The property ID
     * @return true if building hotel is allowed
     */
    public boolean canBuildHotel(int playerId, int propertyId) {
        Property property = gameState.getBoard().getProperty(propertyId);
        Player player = gameState.getPlayer(playerId);
        
        if (property == null || player == null) {
            return false;
        }
        
        // Check ownership
        if (property.getOwnerId() != playerId) {
            return false;
        }
        
        // Must have exactly 4 houses
        if (property.getNumberOfHouses() != 4) {
            return false;
        }
        
        // Must not already have hotel
        if (property.hasHotel()) {
            return false;
        }
        
        // Must own complete color group
        if (!rentCalculator.ownsCompleteColorGroup(playerId, property.getColorGroup())) {
            return false;
        }
        
        // Check even building rule for hotel
        if (!canBuildHotelEvenly(playerId, property)) {
            return false;
        }
        
        // Check if player can afford
        if (player.getMoney() < property.getHotelCost()) {
            return false;
        }
        
        // Check if hotels are available
        if (!gameState.getBank().hasHotelsAvailable()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Builds a house on a property
     * @param playerId The player's ID
     * @param propertyId The property ID
     * @return true if house was built
     */
    public boolean buildHouse(int playerId, int propertyId) {
        if (!canBuildHouse(playerId, propertyId)) {
            return false;
        }
        
        Property property = gameState.getBoard().getProperty(propertyId);
        Player player = gameState.getPlayer(playerId);
        Bank bank = gameState.getBank();
        
        // Take money from player
        int cost = property.getHouseCost();
        player.removeMoney(cost);
        bank.receiveFromPlayer(cost);
        
        // Take house from bank
        bank.takeHouse();
        
        // Add house to property
        property.addHouse();
        
        return true;
    }
    
    /**
     * Builds a hotel on a property
     * @param playerId The player's ID
     * @param propertyId The property ID
     * @return true if hotel was built
     */
    public boolean buildHotel(int playerId, int propertyId) {
        if (!canBuildHotel(playerId, propertyId)) {
            return false;
        }
        
        Property property = gameState.getBoard().getProperty(propertyId);
        Player player = gameState.getPlayer(playerId);
        Bank bank = gameState.getBank();
        
        // Take money from player
        int cost = property.getHotelCost();
        player.removeMoney(cost);
        bank.receiveFromPlayer(cost);
        
        // Return 4 houses to bank, take hotel
        bank.returnHouses(4);
        bank.takeHotel();
        
        // Add hotel to property
        property.addHotel();
        
        return true;
    }
    
    /**
     * Sells a house from a property
     * @param playerId The player's ID
     * @param propertyId The property ID
     * @return true if house was sold
     */
    public boolean sellHouse(int playerId, int propertyId) {
        Property property = gameState.getBoard().getProperty(propertyId);
        Player player = gameState.getPlayer(playerId);
        
        if (property == null || player == null) {
            return false;
        }
        
        if (property.getOwnerId() != playerId) {
            return false;
        }
        
        if (property.getNumberOfHouses() <= 0) {
            return false;
        }
        
        // Check even selling rule
        if (!isEvenSellRule(playerId, property)) {
            return false;
        }
        
        Bank bank = gameState.getBank();
        
        // Return house to bank
        bank.returnHouse();
        
        // Remove house from property
        property.removeHouse();
        
        // Pay player half the cost
        int sellPrice = property.getHouseCost() / 2;
        bank.payToPlayer(sellPrice);
        player.addMoney(sellPrice);
        
        return true;
    }
    
    /**
     * Sells a hotel from a property (converts back to 4 houses if available)
     * @param playerId The player's ID
     * @param propertyId The property ID
     * @return true if hotel was sold
     */
    public boolean sellHotel(int playerId, int propertyId) {
        Property property = gameState.getBoard().getProperty(propertyId);
        Player player = gameState.getPlayer(playerId);
        
        if (property == null || player == null) {
            return false;
        }
        
        if (property.getOwnerId() != playerId) {
            return false;
        }
        
        if (!property.hasHotel()) {
            return false;
        }
        
        Bank bank = gameState.getBank();
        
        // Return hotel to bank
        bank.returnHotel();
        
        // Check if we can downgrade to 4 houses
        if (bank.hasHousesAvailable(4)) {
            bank.takeHouses(4);
            property.removeHotel();
            property.setNumberOfHouses(4);
        } else {
            // No houses available - property goes to 0 houses
            property.removeHotel();
            property.setNumberOfHouses(0);
        }
        
        // Pay player half the hotel cost
        int sellPrice = property.getHotelCost() / 2;
        bank.payToPlayer(sellPrice);
        player.addMoney(sellPrice);
        
        return true;
    }
    
    /**
     * Checks even building rule - can't have more than 1 house difference in a group
     * @param playerId The player's ID
     * @param property The property to build on
     * @return true if building maintains even distribution
     */
    private boolean isEvenBuildRule(int playerId, Property property) {
        ArrayList<Property> groupProperties = gameState.getBoard()
            .getPropertiesInColorGroup(property.getColorGroup());
        
        int minHouses = Integer.MAX_VALUE;
        
        for (int i = 0; i < groupProperties.size(); i++) {
            Property p = groupProperties.get(i);
            if (p.getOwnerId() == playerId && !p.hasHotel()) {
                minHouses = Math.min(minHouses, p.getNumberOfHouses());
            }
        }
        
        // Can build if this property has the minimum houses
        return property.getNumberOfHouses() <= minHouses;
    }
    
    /**
     * Checks if building hotel maintains even distribution
     * @param playerId The player's ID
     * @param property The property
     * @return true if can build hotel evenly
     */
    private boolean canBuildHotelEvenly(int playerId, Property property) {
        ArrayList<Property> groupProperties = gameState.getBoard()
            .getPropertiesInColorGroup(property.getColorGroup());
        
        // All properties must have 4 houses or a hotel
        for (int i = 0; i < groupProperties.size(); i++) {
            Property p = groupProperties.get(i);
            if (p.getOwnerId() == playerId) {
                if (!p.hasHotel() && p.getNumberOfHouses() < 4) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Checks even selling rule - must sell from property with most houses first
     * @param playerId The player's ID
     * @param property The property to sell from
     * @return true if selling maintains even distribution
     */
    private boolean isEvenSellRule(int playerId, Property property) {
        ArrayList<Property> groupProperties = gameState.getBoard()
            .getPropertiesInColorGroup(property.getColorGroup());
        
        int maxHouses = 0;
        
        for (int i = 0; i < groupProperties.size(); i++) {
            Property p = groupProperties.get(i);
            if (p.getOwnerId() == playerId && !p.hasHotel()) {
                maxHouses = Math.max(maxHouses, p.getNumberOfHouses());
            }
        }
        
        // Can sell if this property has the maximum houses
        return property.getNumberOfHouses() >= maxHouses;
    }
    
    /**
     * Checks if any property in a color group is mortgaged
     * @param playerId The player's ID
     * @param colorGroup The color group
     * @return true if any property is mortgaged
     */
    private boolean hasAnyMortgagedInGroup(int playerId, ColorGroup colorGroup) {
        ArrayList<Property> groupProperties = gameState.getBoard()
            .getPropertiesInColorGroup(colorGroup);
        
        for (int i = 0; i < groupProperties.size(); i++) {
            Property p = groupProperties.get(i);
            if (p.getOwnerId() == playerId && p.isMortgaged()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets the cost to build a house
     * @param propertyId The property ID
     * @return House cost
     */
    public int getHouseCost(int propertyId) {
        Property property = gameState.getBoard().getProperty(propertyId);
        return property != null ? property.getHouseCost() : 0;
    }
    
    /**
     * Gets the cost to build a hotel
     * @param propertyId The property ID
     * @return Hotel cost
     */
    public int getHotelCost(int propertyId) {
        Property property = gameState.getBoard().getProperty(propertyId);
        return property != null ? property.getHotelCost() : 0;
    }
    
    /**
     * Gets the sell price for a house (half cost)
     * @param propertyId The property ID
     * @return Sell price
     */
    public int getHouseSellPrice(int propertyId) {
        return getHouseCost(propertyId) / 2;
    }
    
    /**
     * Gets the sell price for a hotel (half cost)
     * @param propertyId The property ID
     * @return Sell price
     */
    public int getHotelSellPrice(int propertyId) {
        return getHotelCost(propertyId) / 2;
    }
    
    /**
     * Checks if houses are available in the bank
     * @return true if houses available
     */
    public boolean areHousesAvailable() {
        return gameState.getBank().hasHousesAvailable();
    }
    
    /**
     * Checks if hotels are available in the bank
     * @return true if hotels available
     */
    public boolean areHotelsAvailable() {
        return gameState.getBank().hasHotelsAvailable();
    }
    
    /**
     * Gets all properties a player can build on
     * @param playerId The player's ID
     * @return ArrayList of buildable properties
     */
    public ArrayList<Property> getBuildableProperties(int playerId) {
        ArrayList<Property> buildable = new ArrayList<>();
        
        Player player = gameState.getPlayer(playerId);
        if (player == null) {
            return buildable;
        }
        
        // Check each color group the player might own
        for (ColorGroup group : ColorGroup.values()) {
            if (!group.isStandardProperty()) continue;
            
            if (rentCalculator.ownsCompleteColorGroup(playerId, group)) {
                ArrayList<Property> groupProps = gameState.getBoard()
                    .getPropertiesInColorGroup(group);
                
                for (int i = 0; i < groupProps.size(); i++) {
                    Property prop = groupProps.get(i);
                    if (canBuildHouse(playerId, prop.getId()) || 
                        canBuildHotel(playerId, prop.getId())) {
                        buildable.add(prop);
                    }
                }
            }
        }
        
        return buildable;
    }
    
    /**
     * Gets the total investment in buildings for a property
     * @param property The property
     * @return Total building investment
     */
    public int getTotalBuildingInvestment(Property property) {
        if (property == null) return 0;
        
        int investment = 0;
        
        if (property.hasHotel()) {
            investment = property.getHouseCost() * 4 + property.getHotelCost();
        } else {
            investment = property.getHouseCost() * property.getNumberOfHouses();
        }
        
        return investment;
    }
}
