package com.monopoly.model.game;

import com.monopoly.datastructures.HashTable;
import com.monopoly.model.property.Property;

/**
 * Represents the Bank in Monopoly.
 * Manages money supply and unowned properties.
 * Handles transactions between bank and players.
 */
public class Bank {

    // Constants - Standard Monopoly supply
    public static final int TOTAL_HOUSES = 32;
    public static final int TOTAL_HOTELS = 12;
    public static final int STARTING_BANK_MONEY = 20580; // Standard Monopoly bank supply
    
    // Fields
    private int totalMoney;
    private int availableHouses;
    private int availableHotels;
    private final HashTable<Integer, Property> unownedProperties; // propertyId -> Property
    
    /**
     * Creates a new Bank with standard Monopoly supply
     */
    public Bank() {
        this.totalMoney = STARTING_BANK_MONEY;
        this.availableHouses = TOTAL_HOUSES;
        this.availableHotels = TOTAL_HOTELS;
        this.unownedProperties = new HashTable<>();
    }
    
    /**
     * Creates a Bank with custom starting values
     * @param startingMoney Initial bank money
     * @param houses Number of houses available
     * @param hotels Number of hotels available
     */
    public Bank(int startingMoney, int houses, int hotels) {
        this.totalMoney = startingMoney;
        this.availableHouses = houses;
        this.availableHotels = hotels;
        this.unownedProperties = new HashTable<>();
    }
    
    // ==================== Money Management ====================
    
    /**
     * Pays money from the bank to a player
     * @param amount The amount to pay
     * @return true if payment was successful
     */
    public boolean payToPlayer(int amount) {
        if (amount <= 0) {
            return false;
        }
        // In Monopoly, the bank never runs out of money (can issue IOUs)
        // But we track it for accuracy
        totalMoney -= amount;
        return true;
    }
    
    /**
     * Receives money from a player to the bank
     * @param amount The amount received
     * @return true if transaction was successful
     */
    public boolean receiveFromPlayer(int amount) {
        if (amount <= 0) {
            return false;
        }
        totalMoney += amount;
        return true;
    }
    
    /**
     * Gets the current bank money supply
     * @return Total money in the bank
     */
    public int getTotalMoney() {
        return totalMoney;
    }
    
    // ==================== Property Management ====================
    
    /**
     * Adds a property to the bank's unowned properties
     * @param property The property to add
     */
    public void addUnownedProperty(Property property) {
        if (property != null) {
            property.setOwnerId(-1); // No owner
            unownedProperties.put(property.getId(), property);
        }
    }
    
    /**
     * Gets an unowned property by ID
     * @param propertyId The property ID
     * @return The property if unowned, null otherwise
     */
    public Property getUnownedProperty(int propertyId) {
        return unownedProperties.get(propertyId);
    }
    
    /**
     * Removes a property from unowned (when purchased)
     * @param propertyId The property ID to remove
     * @return The removed property, or null if not found
     */
    public Property removeUnownedProperty(int propertyId) {
        return unownedProperties.remove(propertyId);
    }
    
    /**
     * Returns a property to the bank (from foreclosure, etc.)
     * @param property The property to return
     */
    public void returnProperty(Property property) {
        if (property != null) {
            // Clear any buildings
            if (property.hasHotel()) {
                returnHotel();
            }
            returnHouses(property.getNumberOfHouses());
            property.resetBuildings();
            
            // Clear mortgage
            property.unmortgage();
            
            // Clear owner
            property.setOwnerId(-1);
            
            // Add to unowned
            unownedProperties.put(property.getId(), property);
        }
    }
    
    /**
     * Checks if a property is unowned
     * @param propertyId The property ID to check
     * @return true if the property is unowned
     */
    public boolean isPropertyUnowned(int propertyId) {
        return unownedProperties.containsKey(propertyId);
    }
    
    /**
     * Gets the count of unowned properties
     * @return Number of unowned properties
     */
    public int getUnownedPropertyCount() {
        return unownedProperties.size();
    }
    
    // ==================== House Management ====================
    
    /**
     * Takes a house from the bank supply
     * @return true if a house was available and taken
     */
    public boolean takeHouse() {
        if (availableHouses > 0) {
            availableHouses--;
            return true;
        }
        return false;
    }
    
    /**
     * Takes multiple houses from the bank supply
     * @param count Number of houses to take
     * @return true if all houses were available and taken
     */
    public boolean takeHouses(int count) {
        if (count <= 0 || count > availableHouses) {
            return false;
        }
        availableHouses -= count;
        return true;
    }
    
    /**
     * Returns a house to the bank supply
     */
    public void returnHouse() {
        if (availableHouses < TOTAL_HOUSES) {
            availableHouses++;
        }
    }
    
    /**
     * Returns multiple houses to the bank supply
     * @param count Number of houses to return
     */
    public void returnHouses(int count) {
        availableHouses = Math.min(TOTAL_HOUSES, availableHouses + count);
    }
    
    /**
     * Checks if houses are available
     * @return true if at least one house is available
     */
    public boolean hasHousesAvailable() {
        return availableHouses > 0;
    }
    
    /**
     * Checks if a specific number of houses are available
     * @param count Number of houses needed
     * @return true if enough houses are available
     */
    public boolean hasHousesAvailable(int count) {
        return availableHouses >= count;
    }
    
    /**
     * Gets the number of available houses
     * @return Available house count
     */
    public int getAvailableHouses() {
        return availableHouses;
    }
    
    // ==================== Hotel Management ====================
    
    /**
     * Takes a hotel from the bank supply
     * @return true if a hotel was available and taken
     */
    public boolean takeHotel() {
        if (availableHotels > 0) {
            availableHotels--;
            return true;
        }
        return false;
    }
    
    /**
     * Returns a hotel to the bank supply
     */
    public void returnHotel() {
        if (availableHotels < TOTAL_HOTELS) {
            availableHotels++;
        }
    }
    
    /**
     * Checks if hotels are available
     * @return true if at least one hotel is available
     */
    public boolean hasHotelsAvailable() {
        return availableHotels > 0;
    }
    
    /**
     * Gets the number of available hotels
     * @return Available hotel count
     */
    public int getAvailableHotels() {
        return availableHotels;
    }
    
    // ==================== Upgrade/Downgrade Management ====================
    
    /**
     * Handles house to hotel upgrade (returns 4 houses, takes 1 hotel)
     * @return true if upgrade was successful
     */
    public boolean upgradeToHotel() {
        if (!hasHotelsAvailable()) {
            return false;
        }
        returnHouses(4);
        takeHotel();
        return true;
    }
    
    /**
     * Handles hotel to houses downgrade (returns 1 hotel, takes 4 houses)
     * @return true if downgrade was successful
     */
    public boolean downgradeFromHotel() {
        if (!hasHousesAvailable(4)) {
            return false;
        }
        returnHotel();
        takeHouses(4);
        return true;
    }
    
    // ==================== Statistics ====================
    
    /**
     * Gets the total number of houses in play
     * @return Houses currently on properties
     */
    public int getHousesInPlay() {
        return TOTAL_HOUSES - availableHouses;
    }
    
    /**
     * Gets the total number of hotels in play
     * @return Hotels currently on properties
     */
    public int getHotelsInPlay() {
        return TOTAL_HOTELS - availableHotels;
    }
    
    /**
     * Gets all unowned properties as an ArrayList
     * @return ArrayList of unowned properties
     */
    public com.monopoly.datastructures.ArrayList<Property> getAllProperties() {
        return unownedProperties.values();
    }
    
    /**
     * Resets the bank to starting state
     */
    public void reset() {
        this.totalMoney = STARTING_BANK_MONEY;
        this.availableHouses = TOTAL_HOUSES;
        this.availableHotels = TOTAL_HOTELS;
        this.unownedProperties.clear();
    }
    
    @Override
    public String toString() {
        return "Bank{" +
                "totalMoney=$" + totalMoney +
                ", availableHouses=" + availableHouses + "/" + TOTAL_HOUSES +
                ", availableHotels=" + availableHotels + "/" + TOTAL_HOTELS +
                ", unownedProperties=" + unownedProperties.size() +
                '}';
    }
}
