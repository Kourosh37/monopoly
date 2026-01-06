package com.monopoly.logic;

import com.monopoly.datastructures.ArrayList;
import com.monopoly.model.card.Card;
import com.monopoly.model.card.CardEffect;
import com.monopoly.model.card.CardType;
import com.monopoly.model.enums.GameStatus;
import com.monopoly.model.enums.TileType;
import com.monopoly.model.enums.TurnPhase;
import com.monopoly.model.game.Bank;
import com.monopoly.model.game.Board;
import com.monopoly.model.game.Dice;
import com.monopoly.model.game.GameState;
import com.monopoly.model.player.Player;
import com.monopoly.model.property.Property;
import com.monopoly.model.tile.*;

/**
 * Core game logic handler.
 * Contains the main game rules and mechanics.
 * Acts as the main coordinator for all game operations.
 */
public class GameLogic {

    // Game state
    private final GameState gameState;
    
    // Managers
    private final TurnManager turnManager;
    private final RentCalculator rentCalculator;
    private final ConstructionManager constructionManager;
    private final TradeManager tradeManager;
    private final AuctionManager auctionManager;
    private final BankruptcyManager bankruptcyManager;
    private final JailManager jailManager;
    
    // Last action info
    private String lastActionDescription;
    private int lastRentPaid;
    private Card lastCardDrawn;
    
    /**
     * Creates a GameLogic instance for a game
     * @param gameState The game state to manage
     */
    public GameLogic(GameState gameState) {
        this.gameState = gameState;
        this.turnManager = new TurnManager(gameState);
        this.rentCalculator = new RentCalculator(gameState);
        this.constructionManager = new ConstructionManager(gameState);
        this.tradeManager = new TradeManager(gameState);
        this.auctionManager = new AuctionManager(gameState);
        this.bankruptcyManager = new BankruptcyManager(gameState);
        this.jailManager = new JailManager();
        this.lastActionDescription = "";
        this.lastRentPaid = 0;
        this.lastCardDrawn = null;
    }
    
    // ==================== Game Flow ====================
    
    /**
     * Starts the game
     * @return true if game started successfully
     */
    public boolean startGame() {
        if (!gameState.canStart()) {
            return false;
        }
        
        gameState.startGame();
        turnManager.startTurn();
        lastActionDescription = "Game started!";
        
        return true;
    }
    
    /**
     * Rolls dice and moves the current player
     * @return Result containing roll info and movement result
     */
    public RollResult rollAndMove() {
        Player currentPlayer = turnManager.getCurrentPlayer();
        
        if (currentPlayer == null) {
            return new RollResult(false, 0, 0, "No current player");
        }
        
        if (!turnManager.isActionAllowed("ROLL")) {
            return new RollResult(false, 0, 0, "Cannot roll now");
        }
        
        Dice dice = gameState.getDice();
        int total = turnManager.rollDice();
        boolean isDoubles = dice.isDoubles();
        
        // Handle jail
        if (currentPlayer.isInJail()) {
            return handleJailRoll(currentPlayer, total, isDoubles);
        }
        
        // Check for speeding (3 doubles)
        if (isDoubles && dice.shouldGoToJail()) {
            jailManager.sendToJail(currentPlayer);
            turnManager.setPhase(TurnPhase.POST_ACTION);
            return new RollResult(true, dice.getDie1(), dice.getDie2(), 
                "Speeding! Go to Jail!");
        }
        
        // Normal movement
        int oldPosition = currentPlayer.getPosition();
        int newPosition = movePlayer(currentPlayer.getId(), total);
        
        // Handle doubles (extra turn)
        turnManager.handleDoubles();
        
        // Set phase to landed
        turnManager.setPhase(TurnPhase.LANDED);
        
        String message = String.format("Rolled %d+%d=%d. Moved to %s", 
            dice.getDie1(), dice.getDie2(), total, 
            gameState.getBoard().getTile(newPosition).getName());
        
        if (isDoubles && !dice.shouldGoToJail()) {
            message += " (Doubles - roll again!)";
        }
        
        return new RollResult(true, dice.getDie1(), dice.getDie2(), message);
    }
    
    /**
     * Handles dice roll while in jail
     */
    private RollResult handleJailRoll(Player player, int total, boolean isDoubles) {
        Dice dice = gameState.getDice();
        
        if (isDoubles) {
            // Released by doubles
            jailManager.attemptReleaseByDoubles(player, true);
            int newPosition = movePlayer(player.getId(), total);
            turnManager.setPhase(TurnPhase.LANDED);
            return new RollResult(true, dice.getDie1(), dice.getDie2(),
                "Doubles! Released from jail. Moved to " + 
                gameState.getBoard().getTile(newPosition).getName());
        }
        
        // Still in jail
        if (jailManager.mustLeaveJail(player)) {
            // Must pay fine and move
            if (player.getMoney() >= JailManager.JAIL_FINE) {
                jailManager.releaseByFine(player);
                int newPosition = movePlayer(player.getId(), total);
                turnManager.setPhase(TurnPhase.LANDED);
                return new RollResult(true, dice.getDie1(), dice.getDie2(),
                    "Paid $50 fine. Moved to " + 
                    gameState.getBoard().getTile(newPosition).getName());
            } else {
                // Player must raise money or go bankrupt
                turnManager.setPhase(TurnPhase.IN_DEBT);
                return new RollResult(true, dice.getDie1(), dice.getDie2(),
                    "Must pay $50 fine but insufficient funds!");
            }
        }
        
        turnManager.setPhase(TurnPhase.POST_ACTION);
        return new RollResult(true, dice.getDie1(), dice.getDie2(),
            "No doubles. Still in jail. Turns remaining: " + 
            jailManager.getRemainingJailTurns(player));
    }
    
    /**
     * Moves a player by steps
     * @param playerId The player to move
     * @param steps Number of steps
     * @return New position
     */
    public int movePlayer(int playerId, int steps) {
        Player player = gameState.getPlayer(playerId);
        Board board = gameState.getBoard();
        
        if (player == null) {
            return -1;
        }
        
        int oldPosition = player.getPosition();
        int newPosition = board.movePlayer(oldPosition, steps);
        
        // Check if passed GO
        if (board.passedGo(oldPosition, newPosition)) {
            passedGo(playerId);
        }
        
        // Check if landed on GO
        if (board.isOnGo(newPosition)) {
            passedGo(playerId);
        }
        
        // Move the player
        player.move(steps);
        
        return newPosition;
    }
    
    /**
     * Teleports a player to a specific position
     * @param playerId The player to move
     * @param position Target position
     * @param collectGo Whether to collect GO if passed
     * @return The new position
     */
    public int teleportPlayer(int playerId, int position, boolean collectGo) {
        Player player = gameState.getPlayer(playerId);
        Board board = gameState.getBoard();
        
        if (player == null) {
            return -1;
        }
        
        int oldPosition = player.getPosition();
        
        // Check if collecting GO
        if (collectGo && position < oldPosition) {
            passedGo(playerId);
        }
        
        player.teleportTo(position);
        
        return position;
    }
    
    /**
     * Gets the property at a specific position, if any
     * @param position The board position
     * @return The property at that position, or null if no property there
     */
    public Property getPropertyAtPosition(int position) {
        Board board = gameState.getBoard();
        Tile tile = board.getTile(position);
        
        if (tile == null) {
            return null;
        }
        
        if (tile instanceof PropertyTile) {
            return ((PropertyTile) tile).getProperty();
        }
        
        return null;
    }

    /**
     * Handles landing on a tile
     * @param playerId The player
     * @return Description of what happened
     */
    public String handleTileLanding(int playerId) {
        Player player = gameState.getPlayer(playerId);
        Board board = gameState.getBoard();
        
        if (player == null) {
            return "Invalid player";
        }
        
        int position = player.getPosition();
        Tile tile = board.getTile(position);
        
        if (tile == null) {
            return "Invalid tile";
        }
        
        lastActionDescription = "";
        
        switch (tile.getTileType()) {
            case PROPERTY:
                return handlePropertyLanding(player, (PropertyTile) tile);
            case RAILROAD:
                return handleRailroadLanding(player, (RailroadTile) tile);
            case UTILITY:
                return handleUtilityLanding(player, (UtilityTile) tile);
            case GO:
                return handleGoLanding(player);
            case JAIL:
                return handleJailLanding(player);
            case GO_TO_JAIL:
                return handleGoToJailLanding(player);
            case FREE_PARKING:
                return handleFreeParkingLanding(player);
            case CHANCE:
                return handleChanceLanding(player);
            case COMMUNITY_CHEST:
                return handleCommunityChestLanding(player);
            case TAX:
                return handleTaxLanding(player, (TaxTile) tile);
            default:
                return "Landed on " + tile.getName();
        }
    }
    
    /**
     * Handles landing on a property
     */
    private String handlePropertyLanding(Player player, PropertyTile tile) {
        Property property = tile.getProperty();
        
        if (property == null) {
            return "Invalid property";
        }
        
        int ownerId = property.getOwnerId();
        
        if (ownerId < 0) {
            // Unowned
            turnManager.setPhase(TurnPhase.AWAITING_DECISION);
            return String.format("%s is unowned. Price: $%d. Buy or Auction?", 
                property.getName(), property.getPrice());
        } else if (ownerId == player.getId()) {
            // Own property
            return "You own " + property.getName();
        } else if (property.isMortgaged()) {
            // Mortgaged - no rent
            return property.getName() + " is mortgaged. No rent due.";
        } else {
            // Pay rent
            int rent = rentCalculator.calculatePropertyRent(property);
            return collectRent(player.getId(), ownerId, rent, property.getName());
        }
    }
    
    /**
     * Handles landing on a railroad
     */
    private String handleRailroadLanding(Player player, RailroadTile tile) {
        int ownerId = tile.getOwnerId();
        
        if (ownerId < 0) {
            turnManager.setPhase(TurnPhase.AWAITING_DECISION);
            return String.format("%s is unowned. Price: $%d. Buy or Auction?",
                tile.getName(), RailroadTile.PURCHASE_PRICE);
        } else if (ownerId == player.getId()) {
            return "You own " + tile.getName();
        } else if (tile.isMortgaged()) {
            return tile.getName() + " is mortgaged. No rent due.";
        } else {
            int rent = rentCalculator.calculateRailroadRent(ownerId);
            return collectRent(player.getId(), ownerId, rent, tile.getName());
        }
    }
    
    /**
     * Handles landing on a utility
     */
    private String handleUtilityLanding(Player player, UtilityTile tile) {
        int ownerId = tile.getOwnerId();
        
        if (ownerId < 0) {
            turnManager.setPhase(TurnPhase.AWAITING_DECISION);
            return String.format("%s is unowned. Price: $%d. Buy or Auction?",
                tile.getName(), UtilityTile.PURCHASE_PRICE);
        } else if (ownerId == player.getId()) {
            return "You own " + tile.getName();
        } else if (tile.isMortgaged()) {
            return tile.getName() + " is mortgaged. No rent due.";
        } else {
            int diceRoll = gameState.getDice().getTotal();
            int rent = rentCalculator.calculateUtilityRent(ownerId, diceRoll);
            return collectRent(player.getId(), ownerId, rent, tile.getName());
        }
    }
    
    /**
     * Handles landing on GO
     */
    private String handleGoLanding(Player player) {
        // Already collected when passing/landing
        return "Landed on GO! Collected $200.";
    }
    
    /**
     * Handles landing on Jail (Just Visiting)
     */
    private String handleJailLanding(Player player) {
        return "Just visiting jail.";
    }
    
    /**
     * Handles landing on Go To Jail
     */
    private String handleGoToJailLanding(Player player) {
        sendToJail(player.getId());
        return "Go to Jail!";
    }
    
    /**
     * Handles landing on Free Parking
     */
    private String handleFreeParkingLanding(Player player) {
        if (gameState.getFreeParkingJackpot() > 0) {
            int jackpot = gameState.collectFreeParkingJackpot();
            player.addMoney(jackpot);
            return "Free Parking! Collected jackpot of $" + jackpot;
        }
        return "Free Parking. Rest here.";
    }
    
    /**
     * Handles landing on Chance
     */
    private String handleChanceLanding(Player player) {
        turnManager.setPhase(TurnPhase.DRAWING_CARD);
        return "Draw a Chance card!";
    }
    
    /**
     * Handles landing on Community Chest
     */
    private String handleCommunityChestLanding(Player player) {
        turnManager.setPhase(TurnPhase.DRAWING_CARD);
        return "Draw a Community Chest card!";
    }
    
    /**
     * Handles landing on Tax
     */
    private String handleTaxLanding(Player player, TaxTile tile) {
        int tax = tile.getTaxAmount();
        
        if (player.getMoney() >= tax) {
            player.removeMoney(tax);
            gameState.getBank().receiveFromPlayer(tax);
            gameState.addToFreeParkingJackpot(tax);
            return String.format("Paid %s: $%d", tile.getName(), tax);
        } else {
            // Need to raise money
            turnManager.setPhase(TurnPhase.IN_DEBT);
            return String.format("Must pay %s: $%d. Insufficient funds!", tile.getName(), tax);
        }
    }
    
    // ==================== Property Purchase ====================
    
    /**
     * Checks if a player can buy a property
     * @param playerId The player
     * @param propertyId The property
     * @return true if can buy
     */
    public boolean canBuyProperty(int playerId, int propertyId) {
        Player player = gameState.getPlayer(playerId);
        Property property = gameState.getBoard().getProperty(propertyId);
        
        if (player == null || property == null) {
            return false;
        }
        
        // Must be unowned
        if (property.getOwnerId() >= 0) {
            return false;
        }
        
        // Must have enough money
        if (player.getMoney() < property.getPrice()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Buys a property
     * @param playerId The buying player
     * @param propertyId The property to buy
     * @return true if purchase successful
     */
    public boolean buyProperty(int playerId, int propertyId) {
        if (!canBuyProperty(playerId, propertyId)) {
            return false;
        }
        
        Player player = gameState.getPlayer(playerId);
        Property property = gameState.getBoard().getProperty(propertyId);
        Bank bank = gameState.getBank();
        
        // Transaction
        int price = property.getPrice();
        player.removeMoney(price);
        bank.receiveFromPlayer(price);
        bank.removeUnownedProperty(propertyId);
        
        // Transfer ownership
        player.addProperty(property);
        property.setOwnerId(playerId);
        
        lastActionDescription = player.getName() + " bought " + property.getName() + " for $" + price;
        turnManager.setPhase(TurnPhase.POST_ACTION);
        
        return true;
    }
    
    /**
     * Checks if player can buy a railroad
     */
    public boolean canBuyRailroad(int playerId, int position) {
        Player player = gameState.getPlayer(playerId);
        Tile tile = gameState.getBoard().getTile(position);
        
        if (player == null || !(tile instanceof RailroadTile)) {
            return false;
        }
        
        RailroadTile railroad = (RailroadTile) tile;
        return railroad.getOwnerId() < 0 && player.getMoney() >= RailroadTile.PURCHASE_PRICE;
    }
    
    /**
     * Buys a railroad
     */
    public boolean buyRailroad(int playerId, int position) {
        if (!canBuyRailroad(playerId, position)) {
            return false;
        }
        
        Player player = gameState.getPlayer(playerId);
        RailroadTile railroad = (RailroadTile) gameState.getBoard().getTile(position);
        Bank bank = gameState.getBank();
        
        player.removeMoney(RailroadTile.PURCHASE_PRICE);
        bank.receiveFromPlayer(RailroadTile.PURCHASE_PRICE);
        railroad.setOwnerId(playerId);
        
        lastActionDescription = player.getName() + " bought " + railroad.getName();
        turnManager.setPhase(TurnPhase.POST_ACTION);
        
        return true;
    }
    
    /**
     * Checks if player can buy a utility
     */
    public boolean canBuyUtility(int playerId, int position) {
        Player player = gameState.getPlayer(playerId);
        Tile tile = gameState.getBoard().getTile(position);
        
        if (player == null || !(tile instanceof UtilityTile)) {
            return false;
        }
        
        UtilityTile utility = (UtilityTile) tile;
        return utility.getOwnerId() < 0 && player.getMoney() >= UtilityTile.PURCHASE_PRICE;
    }
    
    /**
     * Buys a utility
     */
    public boolean buyUtility(int playerId, int position) {
        if (!canBuyUtility(playerId, position)) {
            return false;
        }
        
        Player player = gameState.getPlayer(playerId);
        UtilityTile utility = (UtilityTile) gameState.getBoard().getTile(position);
        Bank bank = gameState.getBank();
        
        player.removeMoney(UtilityTile.PURCHASE_PRICE);
        bank.receiveFromPlayer(UtilityTile.PURCHASE_PRICE);
        utility.setOwnerId(playerId);
        
        lastActionDescription = player.getName() + " bought " + utility.getName();
        turnManager.setPhase(TurnPhase.POST_ACTION);
        
        return true;
    }
    
    // ==================== Rent Collection ====================
    
    /**
     * Collects rent from one player to another
     * @param payerId The paying player
     * @param ownerId The owner receiving rent
     * @param amount The rent amount
     * @param propertyName Property name for description
     * @return Description of transaction
     */
    public String collectRent(int payerId, int ownerId, int amount, String propertyName) {
        Player payer = gameState.getPlayer(payerId);
        Player owner = gameState.getPlayer(ownerId);
        
        if (payer == null || owner == null || amount <= 0) {
            return "Invalid rent collection";
        }
        
        lastRentPaid = amount;
        
        if (payer.getMoney() >= amount) {
            // Can pay
            payer.removeMoney(amount);
            owner.addMoney(amount);
            payer.addRentPaid(amount);
            owner.addRentCollected(amount);
            gameState.recordTransaction(payerId, ownerId, amount);
            
            turnManager.setPhase(TurnPhase.POST_ACTION);
            return String.format("Paid $%d rent to %s for %s", amount, owner.getName(), propertyName);
        } else {
            // Can't pay - check if can liquidate
            if (bankruptcyManager.canAvoidBankruptcy(payerId, amount)) {
                turnManager.setPhase(TurnPhase.IN_DEBT);
                return String.format("Must pay $%d rent to %s but have only $%d. Sell or mortgage to pay!",
                    amount, owner.getName(), payer.getMoney());
            } else {
                // Bankruptcy
                bankruptcyManager.declareBankruptcy(payerId, ownerId);
                return payer.getName() + " went bankrupt paying rent to " + owner.getName();
            }
        }
    }
    
    // ==================== Jail ====================
    
    /**
     * Sends a player to jail
     * @param playerId The player to jail
     */
    public void sendToJail(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player != null) {
            jailManager.sendToJail(player);
            gameState.getDice().resetDoubles();
            turnManager.setPhase(TurnPhase.POST_ACTION);
        }
    }
    
    /**
     * Releases a player from jail
     * @param playerId The player to release
     * @param method "fine", "card", or "doubles"
     * @return true if released successfully
     */
    public boolean releaseFromJail(int playerId, String method) {
        Player player = gameState.getPlayer(playerId);
        if (player == null || !player.isInJail()) {
            return false;
        }
        
        switch (method.toLowerCase()) {
            case "fine":
                return jailManager.releaseByFine(player);
            case "card":
                boolean released = jailManager.releaseByCard(player);
                if (released) {
                    // Return card to deck (handled by GameState)
                }
                return released;
            case "doubles":
                // Doubles handled during roll
                return false;
            default:
                return false;
        }
    }
    
    // ==================== Cards ====================
    
    /**
     * Draws a card
     * @param playerId The player drawing
     * @param cardType CHANCE or COMMUNITY_CHEST
     * @return The drawn card
     */
    public Card drawCard(int playerId, CardType cardType) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) {
            return null;
        }
        
        Card card;
        if (cardType == CardType.CHANCE) {
            card = gameState.drawChanceCard();
        } else {
            card = gameState.drawCommunityChestCard();
        }
        
        lastCardDrawn = card;
        return card;
    }
    
    /**
     * Executes a card effect
     * @param playerId The player
     * @param card The card to execute
     * @return Description of effect
     */
    public String executeCard(int playerId, Card card) {
        Player player = gameState.getPlayer(playerId);
        if (player == null || card == null) {
            return "Invalid card execution";
        }
        
        CardEffect effect = card.getEffect();
        int primaryValue = card.getPrimaryValue();
        int secondaryValue = card.getSecondaryValue();
        
        String result = card.getDescription();
        
        switch (effect) {
            case COLLECT_FROM_BANK:
                player.addMoney(primaryValue);
                gameState.getBank().payToPlayer(primaryValue);
                break;
                
            case PAY_BANK:
                if (player.getMoney() >= primaryValue) {
                    player.removeMoney(primaryValue);
                    gameState.getBank().receiveFromPlayer(primaryValue);
                    gameState.addToFreeParkingJackpot(primaryValue);
                } else {
                    turnManager.setPhase(TurnPhase.IN_DEBT);
                    return "Must pay $" + primaryValue + " but insufficient funds!";
                }
                break;
                
            case ADVANCE_TO:
                int oldPos = player.getPosition();
                teleportPlayer(playerId, primaryValue, true);
                result += handleTileLanding(playerId);
                break;
                
            case ADVANCE_TO_NEAREST_RAILROAD:
                int nearestRR = gameState.getBoard().findNearestRailroad(player.getPosition());
                teleportPlayer(playerId, nearestRR, true);
                result += " " + handleTileLanding(playerId);
                break;
                
            case ADVANCE_TO_NEAREST_UTILITY:
                int nearestUtil = gameState.getBoard().findNearestUtility(player.getPosition());
                teleportPlayer(playerId, nearestUtil, true);
                result += " " + handleTileLanding(playerId);
                break;
                
            case GO_BACK:
                int newPos = gameState.getBoard().normalizePosition(player.getPosition() - primaryValue);
                player.teleportTo(newPos);
                result += " " + handleTileLanding(playerId);
                break;
                
            case GO_TO_JAIL:
                sendToJail(playerId);
                break;
                
            case GET_OUT_OF_JAIL_FREE:
                player.addGetOutOfJailCard();
                // Don't return card to deck
                break;
                
            case REPAIRS:
                // primaryValue = per house, secondaryValue = per hotel
                int houses = countPlayerHouses(playerId);
                int hotels = countPlayerHotels(playerId);
                int repairCost = houses * primaryValue + hotels * secondaryValue;
                if (player.getMoney() >= repairCost) {
                    player.removeMoney(repairCost);
                    gameState.getBank().receiveFromPlayer(repairCost);
                    gameState.addToFreeParkingJackpot(repairCost);
                    result += " Paid $" + repairCost;
                } else {
                    turnManager.setPhase(TurnPhase.IN_DEBT);
                    return "Must pay $" + repairCost + " for repairs but insufficient funds!";
                }
                break;
                
            case COLLECT_FROM_EACH_PLAYER:
                int collected = 0;
                ArrayList<Integer> playerOrder = gameState.getPlayerOrder();
                for (int i = 0; i < playerOrder.size(); i++) {
                    int otherId = playerOrder.get(i);
                    if (otherId != playerId) {
                        Player other = gameState.getPlayer(otherId);
                        if (other != null && !other.isBankrupt()) {
                            if (other.getMoney() >= primaryValue) {
                                other.removeMoney(primaryValue);
                                collected += primaryValue;
                                gameState.recordTransaction(otherId, playerId, primaryValue);
                            }
                        }
                    }
                }
                player.addMoney(collected);
                result += " Collected $" + collected + " total";
                break;
                
            case PAY_EACH_PLAYER:
                int totalPaid = 0;
                ArrayList<Integer> players = gameState.getPlayerOrder();
                for (int i = 0; i < players.size(); i++) {
                    int otherId = players.get(i);
                    if (otherId != playerId) {
                        Player other = gameState.getPlayer(otherId);
                        if (other != null && !other.isBankrupt()) {
                            if (player.getMoney() >= primaryValue) {
                                player.removeMoney(primaryValue);
                                other.addMoney(primaryValue);
                                totalPaid += primaryValue;
                                gameState.recordTransaction(playerId, otherId, primaryValue);
                            }
                        }
                    }
                }
                result += " Paid $" + totalPaid + " total";
                break;
                
            default:
                break;
        }
        
        if (turnManager.getCurrentPhase() != TurnPhase.IN_DEBT) {
            turnManager.setPhase(TurnPhase.POST_ACTION);
        }
        
        return result;
    }
    
    // ==================== Helpers ====================
    
    /**
     * Counts houses owned by a player
     */
    private int countPlayerHouses(int playerId) {
        int count = 0;
        Player player = gameState.getPlayer(playerId);
        if (player == null) return 0;
        
        for (int i = 0; i < 40; i++) {
            Property prop = player.getOwnedProperties().get(i);
            if (prop != null && !prop.hasHotel()) {
                count += prop.getNumberOfHouses();
            }
        }
        return count;
    }
    
    /**
     * Counts hotels owned by a player
     */
    private int countPlayerHotels(int playerId) {
        int count = 0;
        Player player = gameState.getPlayer(playerId);
        if (player == null) return 0;
        
        for (int i = 0; i < 40; i++) {
            Property prop = player.getOwnedProperties().get(i);
            if (prop != null && prop.hasHotel()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Handles player receiving GO salary
     * @param playerId The player
     */
    public void passedGo(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player != null) {
            player.passGo();
            gameState.getBank().payToPlayer(Player.GO_SALARY);
        }
    }
    
    /**
     * Ends the current player's turn
     */
    public void endTurn() {
        turnManager.nextPlayer();
    }
    
    /**
     * Checks if the game is over
     * @return true if game has ended
     */
    public boolean isGameOver() {
        return gameState.isGameOver();
    }
    
    /**
     * Gets the winner
     * @return The winning player, or null
     */
    public Player getWinner() {
        int winnerId = gameState.getWinnerId();
        return winnerId >= 0 ? gameState.getPlayer(winnerId) : null;
    }
    
    // ==================== Accessors ====================
    
    public GameState getGameState() {
        return gameState;
    }
    
    public TurnManager getTurnManager() {
        return turnManager;
    }
    
    public RentCalculator getRentCalculator() {
        return rentCalculator;
    }
    
    public ConstructionManager getConstructionManager() {
        return constructionManager;
    }
    
    public TradeManager getTradeManager() {
        return tradeManager;
    }
    
    public AuctionManager getAuctionManager() {
        return auctionManager;
    }
    
    public BankruptcyManager getBankruptcyManager() {
        return bankruptcyManager;
    }
    
    public JailManager getJailManager() {
        return jailManager;
    }
    
    public String getLastActionDescription() {
        return lastActionDescription;
    }
    
    public int getLastRentPaid() {
        return lastRentPaid;
    }
    
    public Card getLastCardDrawn() {
        return lastCardDrawn;
    }
    
    // ==================== Result Classes ====================
    
    /**
     * Result of a dice roll
     */
    public static class RollResult {
        private final boolean success;
        private final int die1;
        private final int die2;
        private final String message;
        private final int consecutiveDoubles;
        
        public RollResult(boolean success, int die1, int die2, String message) {
            this(success, die1, die2, message, 0);
        }
        
        public RollResult(boolean success, int die1, int die2, String message, int consecutiveDoubles) {
            this.success = success;
            this.die1 = die1;
            this.die2 = die2;
            this.message = message;
            this.consecutiveDoubles = consecutiveDoubles;
        }
        
        public boolean isSuccess() { return success; }
        public int getDie1() { return die1; }
        public int getDie2() { return die2; }
        public int getTotal() { return die1 + die2; }
        public boolean isDoubles() { return die1 == die2 && die1 > 0; }
        public String getMessage() { return message; }
        public int getConsecutiveDoubles() { return consecutiveDoubles; }
    }
}
