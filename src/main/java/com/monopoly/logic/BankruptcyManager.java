package com.monopoly.logic;

import com.monopoly.datastructures.ArrayList;
import com.monopoly.datastructures.HashTable;
import com.monopoly.model.game.Bank;
import com.monopoly.model.game.GameState;
import com.monopoly.model.player.Player;
import com.monopoly.model.property.Property;

/**
 * Manages player bankruptcy.
 * When a player cannot pay, they are removed and assets transferred.
 */
public class BankruptcyManager {

    private final GameState gameState;
    private final ConstructionManager constructionManager;
    
    /**
     * Creates a BankruptcyManager for a game
     * @param gameState The game state
     */
    public BankruptcyManager(GameState gameState) {
        this.gameState = gameState;
        this.constructionManager = new ConstructionManager(gameState);
    }
    
    /**
     * Checks if a player is bankrupt (cannot pay an amount)
     * @param playerId The player's ID
     * @param amountOwed The amount they need to pay
     * @return true if player cannot pay even after liquidation
     */
    public boolean checkBankruptcy(int playerId, int amountOwed) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) {
            return true;
        }
        
        // Already bankrupt
        if (player.isBankrupt()) {
            return true;
        }
        
        // Can pay without liquidation
        if (player.getMoney() >= amountOwed) {
            return false;
        }
        
        // Check if liquidation would cover the debt
        return getTotalLiquidationValue(playerId) < amountOwed;
    }
    
    /**
     * Checks if player can avoid bankruptcy by selling/mortgaging
     * @param playerId The player's ID
     * @param amountOwed Amount needed
     * @return true if player can raise enough money
     */
    public boolean canAvoidBankruptcy(int playerId, int amountOwed) {
        return !checkBankruptcy(playerId, amountOwed);
    }
    
    /**
     * Declares bankruptcy for a player
     * @param playerId The bankrupt player's ID
     * @param creditorId The creditor's ID (-1 for bank)
     */
    public void declareBankruptcy(int playerId, int creditorId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null || player.isBankrupt()) {
            return;
        }
        
        // Sell all buildings first
        sellAllBuildings(playerId);
        
        if (creditorId >= 0) {
            // Transfer assets to another player
            transferAssetsToCreditor(playerId, creditorId);
        } else {
            // Return assets to bank
            returnAssetsToBank(playerId);
        }
        
        // Mark player as bankrupt
        player.setBankrupt(true);
        
        // Remove from game
        removePlayerFromGame(playerId);
    }
    
    /**
     * Transfers all assets from bankrupt player to creditor
     * @param bankruptPlayerId The bankrupt player's ID
     * @param creditorId The creditor's ID
     */
    public void transferAssetsToCreditor(int bankruptPlayerId, int creditorId) {
        Player bankrupt = gameState.getPlayer(bankruptPlayerId);
        Player creditor = gameState.getPlayer(creditorId);
        
        if (bankrupt == null || creditor == null) {
            return;
        }
        
        // Transfer money
        int money = bankrupt.getMoney();
        bankrupt.removeMoney(money);
        creditor.addMoney(money);
        
        // Transfer properties
        ArrayList<Property> properties = bankrupt.getOwnedProperties();
        ArrayList<Property> propertiesToTransfer = new ArrayList<>();
        
        // Collect properties first to avoid modification during iteration
        for (int i = 0; i < properties.size(); i++) {
            propertiesToTransfer.add(properties.get(i));
        }
        
        // Transfer each property
        for (int i = 0; i < propertiesToTransfer.size(); i++) {
            Property prop = propertiesToTransfer.get(i);
            if (prop != null) {
                // Creditor gets mortgaged properties but must pay 10% interest immediately
                // or unmortgage within their turn
                bankrupt.removeProperty(prop);
                creditor.addProperty(prop);
                prop.setOwnerId(creditorId);
            }
        }
        
        // Transfer Get Out of Jail cards
        while (bankrupt.getGetOutOfJailCards() > 0) {
            bankrupt.useGetOutOfJailCard();
            creditor.addGetOutOfJailCard();
        }
    }
    
    /**
     * Returns all assets from bankrupt player to bank
     * @param playerId The bankrupt player's ID
     */
    public void returnAssetsToBank(int playerId) {
        Player player = gameState.getPlayer(playerId);
        Bank bank = gameState.getBank();
        
        if (player == null) {
            return;
        }
        
        // Money goes back to bank
        int money = player.getMoney();
        player.removeMoney(money);
        bank.receiveFromPlayer(money);
        
        // Properties return to bank (unmortgaged)
        ArrayList<Property> properties = player.getOwnedProperties();
        ArrayList<Property> propertiesToReturn = new ArrayList<>();
        
        for (int i = 0; i < properties.size(); i++) {
            propertiesToReturn.add(properties.get(i));
        }
        
        for (int i = 0; i < propertiesToReturn.size(); i++) {
            Property prop = propertiesToReturn.get(i);
            if (prop != null) {
                player.removeProperty(prop);
                bank.returnProperty(prop);
            }
        }
        
        // Return Get Out of Jail cards to decks
        while (player.getGetOutOfJailCards() > 0) {
            player.useGetOutOfJailCard();
            // Cards return to respective decks (handled by GameState)
        }
    }
    
    /**
     * Sells all buildings on player's properties
     * @param playerId The player's ID
     */
    public void sellAllBuildings(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) {
            return;
        }
        
        ArrayList<Property> properties = player.getOwnedProperties();
        
        // Keep selling until no buildings remain
        boolean soldSomething;
        do {
            soldSomething = false;
            for (int i = 0; i < properties.size(); i++) {
                Property prop = properties.get(i);
                if (prop != null && prop.hasBuildings()) {
                    if (prop.hasHotel()) {
                        constructionManager.sellHotel(playerId, prop.getId());
                        soldSomething = true;
                    } else if (prop.getNumberOfHouses() > 0) {
                        constructionManager.sellHouse(playerId, prop.getId());
                        soldSomething = true;
                    }
                }
            }
        } while (soldSomething);
    }
    
    /**
     * Sells all assets to raise money
     * @param playerId The player's ID
     * @return Total money raised
     */
    public int sellAllAssets(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) {
            return 0;
        }
        
        int initialMoney = player.getMoney();
        
        // Sell all buildings first
        sellAllBuildings(playerId);
        
        // Mortgage all properties
        ArrayList<Property> properties = player.getOwnedProperties();
        for (int i = 0; i < properties.size(); i++) {
            Property prop = properties.get(i);
            if (prop != null && !prop.isMortgaged()) {
                mortgageProperty(playerId, prop.getId());
            }
        }
        
        return player.getMoney() - initialMoney;
    }
    
    /**
     * Mortgages a property
     * @param playerId The player's ID
     * @param propertyId The property ID
     * @return true if mortgaged successfully
     */
    public boolean mortgageProperty(int playerId, int propertyId) {
        Property property = gameState.getBoard().getProperty(propertyId);
        Player player = gameState.getPlayer(playerId);
        Bank bank = gameState.getBank();
        
        if (property == null || player == null) {
            return false;
        }
        
        if (property.getOwnerId() != playerId) {
            return false;
        }
        
        if (property.isMortgaged()) {
            return false;
        }
        
        // Can't mortgage with buildings
        if (property.hasBuildings()) {
            return false;
        }
        
        // Mortgage the property
        property.mortgage();
        
        // Get mortgage value from bank
        int value = property.getMortgageValue();
        bank.payToPlayer(value);
        player.addMoney(value);
        
        return true;
    }
    
    /**
     * Unmortgages a property
     * @param playerId The player's ID
     * @param propertyId The property ID
     * @return true if unmortgaged successfully
     */
    public boolean unmortgageProperty(int playerId, int propertyId) {
        Property property = gameState.getBoard().getProperty(propertyId);
        Player player = gameState.getPlayer(playerId);
        Bank bank = gameState.getBank();
        
        if (property == null || player == null) {
            return false;
        }
        
        if (property.getOwnerId() != playerId) {
            return false;
        }
        
        if (!property.isMortgaged()) {
            return false;
        }
        
        int unmortgageCost = property.getUnmortgageCost();
        
        if (player.getMoney() < unmortgageCost) {
            return false;
        }
        
        // Pay unmortgage cost
        player.removeMoney(unmortgageCost);
        bank.receiveFromPlayer(unmortgageCost);
        
        // Unmortgage
        property.unmortgage();
        
        return true;
    }
    
    /**
     * Gets the total value if all assets are liquidated
     * @param playerId The player's ID
     * @return Total liquidation value
     */
    public int getTotalLiquidationValue(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) {
            return 0;
        }
        
        int total = player.getMoney();
        
        ArrayList<Property> properties = player.getOwnedProperties();
        
        for (int i = 0; i < properties.size(); i++) {
            Property prop = properties.get(i);
            if (prop != null) {
                // Buildings sell for half price
                if (prop.hasHotel()) {
                    total += (prop.getHouseCost() * 4 + prop.getHotelCost()) / 2;
                } else {
                    total += (prop.getHouseCost() * prop.getNumberOfHouses()) / 2;
                }
                
                // Property mortgage value (if not already mortgaged)
                if (!prop.isMortgaged()) {
                    total += prop.getMortgageValue();
                }
            }
        }
        
        return total;
    }
    
    /**
     * Removes a player from the game
     * @param playerId The player's ID
     */
    public void removePlayerFromGame(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player != null) {
            player.setBankrupt(true);
        }
        // Player remains in HashTable but is marked bankrupt
        // Game skips bankrupt players in turn order
    }
    
    /**
     * Gets a list of mortgageable properties for a player
     * @param playerId The player's ID
     * @return ArrayList of properties that can be mortgaged
     */
    public ArrayList<Property> getMortgageableProperties(int playerId) {
        ArrayList<Property> mortgageable = new ArrayList<>();
        Player player = gameState.getPlayer(playerId);
        
        if (player == null) {
            return mortgageable;
        }
        
        ArrayList<Property> properties = player.getOwnedProperties();
        
        for (int i = 0; i < properties.size(); i++) {
            Property prop = properties.get(i);
            if (prop != null && !prop.isMortgaged() && !prop.hasBuildings()) {
                mortgageable.add(prop);
            }
        }
        
        return mortgageable;
    }
    
    /**
     * Gets a list of unmortgageable properties for a player
     * @param playerId The player's ID
     * @return ArrayList of mortgaged properties
     */
    public ArrayList<Property> getUnmortgageableProperties(int playerId) {
        ArrayList<Property> unmortgageable = new ArrayList<>();
        Player player = gameState.getPlayer(playerId);
        
        if (player == null) {
            return unmortgageable;
        }
        
        ArrayList<Property> properties = player.getOwnedProperties();
        
        for (int i = 0; i < properties.size(); i++) {
            Property prop = properties.get(i);
            if (prop != null && prop.isMortgaged()) {
                // Check if player can afford unmortgage
                if (player.getMoney() >= prop.getUnmortgageCost()) {
                    unmortgageable.add(prop);
                }
            }
        }
        
        return unmortgageable;
    }
}
