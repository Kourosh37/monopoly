package com.monopoly.logic;

import com.monopoly.datastructures.ArrayList;
import com.monopoly.model.game.Auction;
import com.monopoly.model.game.Bank;
import com.monopoly.model.game.GameState;
import com.monopoly.model.player.Player;
import com.monopoly.model.property.Property;

/**
 * Manages property auctions when a player declines to buy.
 * All players can bid, highest bidder wins.
 */
public class AuctionManager {

    private final GameState gameState;
    
    /**
     * Creates an AuctionManager for a game
     * @param gameState The game state
     */
    public AuctionManager(GameState gameState) {
        this.gameState = gameState;
    }
    
    /**
     * Starts an auction for a property
     * @param propertyId The property ID to auction
     * @return true if auction started successfully
     */
    public boolean startAuction(int propertyId) {
        Property property = gameState.getBoard().getProperty(propertyId);
        
        if (property == null) {
            return false;
        }
        
        // Property must be unowned
        if (property.getOwnerId() >= 0) {
            return false;
        }
        
        // Can't start if auction already active
        if (gameState.hasActiveAuction()) {
            return false;
        }
        
        // Get eligible bidders (all active players)
        ArrayList<Integer> eligibleBidders = new ArrayList<>();
        ArrayList<Integer> playerOrder = gameState.getPlayerOrder();
        
        for (int i = 0; i < playerOrder.size(); i++) {
            int playerId = playerOrder.get(i);
            Player player = gameState.getPlayer(playerId);
            if (player != null && !player.isBankrupt()) {
                eligibleBidders.add(playerId);
            }
        }
        
        if (eligibleBidders.isEmpty()) {
            return false;
        }
        
        // Create and start auction
        Auction auction = new Auction(property, eligibleBidders);
        auction.start();
        gameState.setActiveAuction(auction);
        
        return true;
    }
    
    /**
     * Places a bid in the current auction
     * @param playerId The bidding player's ID
     * @param amount The bid amount
     * @return true if bid was accepted
     */
    public boolean placeBid(int playerId, int amount) {
        Auction auction = gameState.getActiveAuction();
        
        if (auction == null) {
            return false;
        }
        
        // Validate player can afford bid
        Player player = gameState.getPlayer(playerId);
        if (player == null || player.getMoney() < amount) {
            return false;
        }
        
        // Place the bid
        return auction.placeBid(playerId, amount);
    }
    
    /**
     * Player passes on the current auction
     * @param playerId The player's ID
     * @return true if pass was recorded
     */
    public boolean passBid(int playerId) {
        Auction auction = gameState.getActiveAuction();
        
        if (auction == null) {
            return false;
        }
        
        return auction.pass(playerId);
    }
    
    /**
     * Validates a bid
     * @param playerId The bidding player
     * @param amount The bid amount
     * @return true if bid is valid
     */
    public boolean isValidBid(int playerId, int amount) {
        Auction auction = gameState.getActiveAuction();
        
        if (auction == null) {
            return false;
        }
        
        // Check player is eligible
        if (!auction.isEligible(playerId)) {
            return false;
        }
        
        // Check player hasn't passed
        if (auction.hasPlayerPassed(playerId)) {
            return false;
        }
        
        // Check bid amount
        if (amount < auction.getMinimumNextBid()) {
            return false;
        }
        
        // Check player can afford
        Player player = gameState.getPlayer(playerId);
        if (player == null || player.getMoney() < amount) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the current highest bid
     * @return Highest bid amount, or 0
     */
    public int getHighestBid() {
        Auction auction = gameState.getActiveAuction();
        return auction != null ? auction.getCurrentHighBid() : 0;
    }
    
    /**
     * Gets the current highest bidder's ID
     * @return Highest bidder's ID, or -1
     */
    public int getHighestBidderId() {
        Auction auction = gameState.getActiveAuction();
        return auction != null ? auction.getCurrentHighBidder() : -1;
    }
    
    /**
     * Checks if the auction is complete
     * @return true if auction has ended
     */
    public boolean isAuctionComplete() {
        Auction auction = gameState.getActiveAuction();
        
        if (auction == null) {
            return true;
        }
        
        return auction.getStatus() == Auction.AuctionStatus.COMPLETED ||
               auction.getStatus() == Auction.AuctionStatus.CANCELLED ||
               auction.getStatus() == Auction.AuctionStatus.EXPIRED;
    }
    
    /**
     * Checks if the auction should be ended (only one bidder remaining or all passed)
     * @return true if auction should end
     */
    public boolean shouldEndAuction() {
        Auction auction = gameState.getActiveAuction();
        
        if (auction == null) {
            return true;
        }
        
        // Check if only one or zero bidders left
        return auction.getActiveBidderCount() <= 1;
    }

    /**
     * Ends the auction and transfers property to winner
     * @return The winner's ID, or -1 if no winner
     */
    public int endAuction() {
        Auction auction = gameState.getActiveAuction();
        
        if (auction == null) {
            return -1;
        }
        
        // Complete the auction if still active
        if (auction.getStatus() == Auction.AuctionStatus.ACTIVE) {
            auction.complete();
        }
        
        int winnerId = auction.getWinner();
        int winningBid = auction.getWinningBid();
        
        if (winnerId >= 0 && winningBid > 0) {
            // Transfer property to winner
            Player winner = gameState.getPlayer(winnerId);
            Property property = auction.getProperty();
            Bank bank = gameState.getBank();
            
            if (winner != null && property != null) {
                // Take money from winner
                winner.removeMoney(winningBid);
                bank.receiveFromPlayer(winningBid);
                
                // Transfer property
                bank.removeUnownedProperty(property.getId());
                winner.addProperty(property);
                property.setOwnerId(winnerId);
            }
        }
        
        // Clear the auction
        gameState.clearActiveAuction();
        
        return winnerId;
    }
    
    /**
     * Cancels the current auction
     */
    public void cancelAuction() {
        Auction auction = gameState.getActiveAuction();
        
        if (auction != null) {
            auction.cancel();
            gameState.clearActiveAuction();
        }
    }
    
    /**
     * Gets the property being auctioned
     * @return The property, or null
     */
    public Property getPropertyBeingAuctioned() {
        Auction auction = gameState.getActiveAuction();
        return auction != null ? auction.getProperty() : null;
    }
    
    /**
     * Checks if an auction is currently active
     * @return true if auction is active
     */
    public boolean isAuctionActive() {
        return gameState.hasActiveAuction() && 
               gameState.getActiveAuction().getStatus() == Auction.AuctionStatus.ACTIVE;
    }
    
    /**
     * Gets the current auction
     * @return The active auction, or null
     */
    public Auction getCurrentAuction() {
        return gameState.getActiveAuction();
    }
    
    /**
     * Gets the minimum next bid
     * @return Minimum acceptable bid
     */
    public int getMinimumBid() {
        Auction auction = gameState.getActiveAuction();
        return auction != null ? auction.getMinimumNextBid() : Auction.MINIMUM_BID;
    }
    
    /**
     * Gets the current bidder (whose turn to bid)
     * @return Current bidder's ID, or -1
     */
    public int getCurrentBidder() {
        Auction auction = gameState.getActiveAuction();
        return auction != null ? auction.getCurrentBidder() : -1;
    }
    
    /**
     * Gets number of active bidders remaining
     * @return Active bidder count
     */
    public int getActiveBidderCount() {
        Auction auction = gameState.getActiveAuction();
        return auction != null ? auction.getActiveBidderCount() : 0;
    }
    
    /**
     * Checks if auction has timed out
     * @return true if timed out
     */
    public boolean isTimedOut() {
        Auction auction = gameState.getActiveAuction();
        return auction != null && auction.isTimedOut();
    }
    
    /**
     * Handles auction timeout
     */
    public void handleTimeout() {
        Auction auction = gameState.getActiveAuction();
        if (auction != null && auction.isTimedOut()) {
            auction.expire();
            endAuction();
        }
    }
}
