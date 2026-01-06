package com.monopoly.model.player;

import com.monopoly.datastructures.ArrayList;
import com.monopoly.datastructures.HashTable;
import com.monopoly.model.property.Property;
import com.monopoly.model.property.ColorGroup;

import java.io.Serializable;

/**
 * Represents a player in the Monopoly game.
 * Contains player information, position, money, and owned assets.
 */
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final int STARTING_MONEY = 1500;
    public static final int GO_SALARY = 200;
    public static final int MAX_JAIL_TURNS = 3;
    public static final int JAIL_BAIL = 50;

    private final int id;
    private final String name;
    private int money;
    private int position;
    private boolean inJail;
    private int turnsInJail;
    private boolean bankrupt;
    private TokenType tokenType;
    private int getOutOfJailCards;
    
    // Statistics
    private int totalRentCollected;
    private int totalRentPaid;
    private int timesPassedGo;
    private int propertiesBought;
    
    // Owned properties (propertyId -> Property)
    private HashTable<Integer, Property> ownedProperties;

    /**
     * Creates a new player with starting money.
     * @param id unique player identifier
     * @param name player's display name
     */
    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.money = STARTING_MONEY;
        this.position = 0; // Start at GO
        this.inJail = false;
        this.turnsInJail = 0;
        this.bankrupt = false;
        this.tokenType = TokenType.CAR;
        this.getOutOfJailCards = 0;
        this.totalRentCollected = 0;
        this.totalRentPaid = 0;
        this.timesPassedGo = 0;
        this.propertiesBought = 0;
        this.ownedProperties = new HashTable<>();
    }

    /**
     * Creates a new player with specified token type.
     */
    public Player(int id, String name, TokenType tokenType) {
        this(id, name);
        this.tokenType = tokenType;
    }
    
    /**
     * Creates a new player with specified player token.
     */
    public Player(int id, String name, PlayerToken token) {
        this(id, name);
        // Map PlayerToken to TokenType if possible
        this.tokenType = TokenType.values()[token.ordinal() % TokenType.values().length];
    }

    // === Basic Getters ===
    
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }

    public int getPosition() {
        return position;
    }

    public TokenType getTokenType() {
        return tokenType;
    }
    
    /**
     * Alias for getTokenType() for Serializer compatibility.
     */
    public TokenType getToken() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    // === Money Management ===

    public void setMoney(int amount) {
        this.money = amount;
    }

    public void addMoney(int amount) {
        if (amount > 0) {
            this.money += amount;
        }
    }

    /**
     * Subtracts money from player.
     * @param amount amount to subtract
     * @return true if successful, false if insufficient funds
     */
    public boolean subtractMoney(int amount) {
        if (amount <= 0) return true;
        if (this.money >= amount) {
            this.money -= amount;
            return true;
        }
        return false;
    }

    /**
     * Removes money from player (alias for subtractMoney).
     * @param amount amount to remove
     * @return true if successful
     */
    public boolean removeMoney(int amount) {
        return subtractMoney(amount);
    }

    /**
     * Forces subtraction (can go negative for debt tracking).
     */
    public void forceSubtractMoney(int amount) {
        this.money -= amount;
    }

    public boolean canAfford(int amount) {
        return this.money >= amount;
    }

    // === Position Management ===

    public void setPosition(int position) {
        this.position = position % 40; // Wrap around board
    }

    /**
     * Moves player by specified steps.
     * @param steps number of steps to move
     * @return true if passed GO
     */
    public boolean move(int steps) {
        int oldPosition = this.position;
        this.position = (this.position + steps) % 40;
        
        // Check if passed GO
        boolean passedGo = (oldPosition + steps >= 40);
        if (passedGo && !inJail) {
            addMoney(GO_SALARY);
            timesPassedGo++;
        }
        return passedGo;
    }

    /**
     * Teleports player to position without GO bonus.
     */
    public void teleportTo(int position) {
        this.position = position % 40;
    }

    // === Jail Management ===

    public boolean isInJail() {
        return inJail;
    }

    /**
     * Sets jail status directly
     */
    public void setInJail(boolean inJail) {
        this.inJail = inJail;
        if (inJail) {
            this.turnsInJail = 0;
        }
    }

    public void sendToJail() {
        this.inJail = true;
        this.turnsInJail = 0;
        this.position = 10; // Jail position
    }

    public void releaseFromJail() {
        this.inJail = false;
        this.turnsInJail = 0;
    }

    public void incrementTurnsInJail() {
        this.turnsInJail++;
    }

    public int getTurnsInJail() {
        return turnsInJail;
    }

    public boolean mustLeaveJail() {
        return turnsInJail >= MAX_JAIL_TURNS;
    }

    /**
     * Pays bail to leave jail.
     * @return true if successful
     */
    public boolean payBail() {
        if (canAfford(JAIL_BAIL)) {
            subtractMoney(JAIL_BAIL);
            releaseFromJail();
            return true;
        }
        return false;
    }

    // === Get Out of Jail Cards ===

    public int getGetOutOfJailCards() {
        return getOutOfJailCards;
    }
    
    /**
     * Alias for getGetOutOfJailCards() for Serializer compatibility.
     */
    public int getJailFreeCards() {
        return getOutOfJailCards;
    }

    public void addGetOutOfJailCard() {
        this.getOutOfJailCards++;
    }

    /**
     * Alias for addGetOutOfJailCard
     */
    public void addJailFreeCard() {
        addGetOutOfJailCard();
    }

    /**
     * Sends player to jail
     */
    public void goToJail() {
        setInJail(true);
        setPosition(10); // Jail position
    }

    public boolean useGetOutOfJailCard() {
        if (getOutOfJailCards > 0) {
            getOutOfJailCards--;
            releaseFromJail();
            return true;
        }
        return false;
    }

    public boolean hasGetOutOfJailCard() {
        return getOutOfJailCards > 0;
    }

    // === Bankruptcy ===

    public boolean isBankrupt() {
        return bankrupt;
    }
    
    public void setBankrupt(boolean bankrupt) {
        this.bankrupt = bankrupt;
        if (bankrupt) {
            declareBankruptcy();
        }
    }

    public void declareBankruptcy() {
        this.bankrupt = true;
        // Release all properties
        for (Property property : ownedProperties.values()) {
            property.setOwnerId(-1);
            property.unmortgage();
            property.removeAllBuildings();
        }
        ownedProperties.clear();
        this.money = 0;
    }

    // === Property Management ===

    public void addProperty(Property property) {
        ownedProperties.put(property.getId(), property);
        property.setOwnerId(this.id);
        propertiesBought++;
    }

    public void removeProperty(Property property) {
        ownedProperties.remove(property.getId());
        property.setOwnerId(-1);
    }

    public boolean ownsProperty(int propertyId) {
        return ownedProperties.containsKey(propertyId);
    }

    public Property getProperty(int propertyId) {
        return ownedProperties.get(propertyId);
    }

    public ArrayList<Property> getOwnedProperties() {
        return ownedProperties.values();
    }

    public int getPropertyCount() {
        return ownedProperties.size();
    }

    /**
     * Counts how many properties player owns in a color group.
     */
    public int getPropertiesInColorGroup(ColorGroup colorGroup) {
        int count = 0;
        for (Property property : ownedProperties.values()) {
            if (property.getColorGroup() == colorGroup) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks if player owns all properties in a color group.
     */
    public boolean ownsColorGroup(ColorGroup colorGroup) {
        return getPropertiesInColorGroup(colorGroup) == colorGroup.getPropertyCount();
    }

    /**
     * Gets all properties in a specific color group that player owns.
     */
    public ArrayList<Property> getPropertiesByColorGroup(ColorGroup colorGroup) {
        ArrayList<Property> properties = new ArrayList<>();
        for (Property property : ownedProperties.values()) {
            if (property.getColorGroup() == colorGroup) {
                properties.add(property);
            }
        }
        return properties;
    }

    // === Statistics ===

    public void recordRentCollected(int amount) {
        this.totalRentCollected += amount;
    }

    /**
     * Alias for recordRentCollected.
     */
    public void addRentCollected(int amount) {
        recordRentCollected(amount);
    }

    public void recordRentPaid(int amount) {
        this.totalRentPaid += amount;
    }

    /**
     * Alias for recordRentPaid.
     */
    public void addRentPaid(int amount) {
        recordRentPaid(amount);
    }

    public int getTotalRentCollected() {
        return totalRentCollected;
    }

    public int getTotalRentPaid() {
        return totalRentPaid;
    }

    public int getTimesPassedGo() {
        return timesPassedGo;
    }

    public int getPropertiesBought() {
        return propertiesBought;
    }

    /**
     * Handles passing GO and collecting salary.
     */
    public void passGo() {
        addMoney(GO_SALARY);
        timesPassedGo++;
    }

    // === Net Worth Calculation ===

    /**
     * Calculates total net worth (money + property values + buildings).
     */
    public int getNetWorth() {
        int netWorth = money;
        for (Property property : ownedProperties.values()) {
            netWorth += property.getTotalValue();
        }
        return netWorth;
    }

    /**
     * Calculates liquidation value (what player can raise by mortgaging/selling).
     */
    public int getLiquidationValue() {
        int value = money;
        for (Property property : ownedProperties.values()) {
            if (!property.isMortgaged()) {
                value += property.getMortgageValue();
            }
            // Buildings can be sold at half price
            value += property.getBuildingValue() / 2;
        }
        return value;
    }

    /**
     * Checks if player can raise a specific amount through liquidation.
     */
    public boolean canRaise(int amount) {
        return getLiquidationValue() >= amount;
    }

    // === Utility Methods ===

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return id == player.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", money=$" + money +
                ", position=" + position +
                ", inJail=" + inJail +
                ", bankrupt=" + bankrupt +
                ", properties=" + ownedProperties.size() +
                ", netWorth=$" + getNetWorth() +
                '}';
    }

    /**
     * Creates a summary for network transmission.
     */
    public String toSummary() {
        return name + " ($" + money + ")";
    }
}
