package com.monopoly.model.game;

import com.monopoly.datastructures.ArrayList;
import com.monopoly.datastructures.HashTable;
import com.monopoly.model.property.Property;

/**
 * Represents an auction for a property.
 * When a player lands on an unowned property and chooses not to buy,
 * or when a player goes bankrupt, their properties are auctioned.
 */
public class Auction {

    /**
     * Status of the auction
     */
    public enum AuctionStatus {
        PENDING,    // Waiting to start
        ACTIVE,     // Auction in progress
        COMPLETED,  // Auction finished with winner
        CANCELLED,  // Auction cancelled (no bidders)
        EXPIRED     // Auction timed out
    }
    
    /**
     * Represents a single bid in the auction
     */
    public static class Bid {
        private final int playerId;
        private final int amount;
        private final long timestamp;
        
        public Bid(int playerId, int amount) {
            this.playerId = playerId;
            this.amount = amount;
            this.timestamp = System.currentTimeMillis();
        }
        
        public int getPlayerId() {
            return playerId;
        }
        
        public int getAmount() {
            return amount;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            return "Bid{playerId=" + playerId + ", amount=$" + amount + "}";
        }
    }
    
    // Auction configuration
    public static final int MINIMUM_BID = 1;
    public static final int MINIMUM_INCREMENT = 1;
    public static final long DEFAULT_TIMEOUT_MS = 30000; // 30 seconds
    
    // Auction fields
    private final String auctionId;
    private final Property property;
    private final ArrayList<Integer> eligibleBidders; // Player IDs who can bid
    private final ArrayList<Bid> bidHistory;
    private final HashTable<Integer, Integer> playerBids; // playerId -> highest bid
    private final HashTable<Integer, Boolean> playerPassed; // playerId -> has passed
    
    // Current state
    private int currentHighBid;
    private int currentHighBidder;
    private AuctionStatus status;
    private int currentBidderIndex;
    private final long startTime;
    private long endTime;
    
    /**
     * Creates a new auction for a property
     * @param property The property being auctioned
     * @param eligiblePlayerIds Players who can participate
     */
    public Auction(Property property, ArrayList<Integer> eligiblePlayerIds) {
        this.auctionId = "auction_" + property.getId() + "_" + System.currentTimeMillis();
        this.property = property;
        this.eligibleBidders = new ArrayList<>();
        this.bidHistory = new ArrayList<>();
        this.playerBids = new HashTable<>();
        this.playerPassed = new HashTable<>();
        
        // Copy eligible bidders
        for (int i = 0; i < eligiblePlayerIds.size(); i++) {
            int playerId = eligiblePlayerIds.get(i);
            eligibleBidders.add(playerId);
            playerBids.put(playerId, 0);
            playerPassed.put(playerId, false);
        }
        
        this.currentHighBid = 0;
        this.currentHighBidder = -1;
        this.status = AuctionStatus.PENDING;
        this.currentBidderIndex = 0;
        this.startTime = System.currentTimeMillis();
        this.endTime = -1;
    }
    
    /**
     * Starts the auction
     * @return true if auction started successfully
     */
    public boolean start() {
        if (status != AuctionStatus.PENDING) {
            return false;
        }
        if (eligibleBidders.isEmpty()) {
            status = AuctionStatus.CANCELLED;
            return false;
        }
        status = AuctionStatus.ACTIVE;
        return true;
    }
    
    /**
     * Places a bid
     * @param playerId The bidding player
     * @param amount The bid amount
     * @return true if bid was accepted
     */
    public boolean placeBid(int playerId, int amount) {
        // Validate auction is active
        if (status != AuctionStatus.ACTIVE) {
            return false;
        }
        
        // Validate player is eligible
        if (!isEligible(playerId)) {
            return false;
        }
        
        // Validate player hasn't passed
        if (Boolean.TRUE.equals(playerPassed.get(playerId))) {
            return false;
        }
        
        // Validate bid amount
        if (amount < MINIMUM_BID) {
            return false;
        }
        
        // Must be higher than current high bid
        if (amount <= currentHighBid) {
            return false;
        }
        
        // Record the bid
        Bid bid = new Bid(playerId, amount);
        bidHistory.add(bid);
        playerBids.put(playerId, amount);
        
        // Update high bid
        currentHighBid = amount;
        currentHighBidder = playerId;
        
        // Move to next bidder
        advanceBidder();
        
        // Check if auction is over
        checkAuctionEnd();
        
        return true;
    }
    
    /**
     * Player passes (exits the auction)
     * @param playerId The player passing
     * @return true if pass was recorded
     */
    public boolean pass(int playerId) {
        if (status != AuctionStatus.ACTIVE) {
            return false;
        }
        
        if (!isEligible(playerId)) {
            return false;
        }
        
        playerPassed.put(playerId, true);
        
        // Move to next bidder
        advanceBidder();
        
        // Check if auction is over
        checkAuctionEnd();
        
        return true;
    }
    
    /**
     * Checks if a player is eligible to bid
     * @param playerId The player ID
     * @return true if eligible
     */
    public boolean isEligible(int playerId) {
        for (int i = 0; i < eligibleBidders.size(); i++) {
            if (eligibleBidders.get(i) == playerId) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Advances to the next bidder
     */
    private void advanceBidder() {
        int startIndex = currentBidderIndex;
        do {
            currentBidderIndex = (currentBidderIndex + 1) % eligibleBidders.size();
            int playerId = eligibleBidders.get(currentBidderIndex);
            if (!Boolean.TRUE.equals(playerPassed.get(playerId))) {
                return; // Found active bidder
            }
        } while (currentBidderIndex != startIndex);
    }
    
    /**
     * Checks if the auction should end
     */
    private void checkAuctionEnd() {
        int activeBidders = getActiveBidderCount();
        
        if (activeBidders <= 1 && currentHighBid > 0) {
            // Only one bidder left with a bid - they win
            complete();
        } else if (activeBidders == 0) {
            // No bidders left - cancel
            cancel();
        }
    }
    
    /**
     * Gets the count of active bidders
     * @return Number of players who haven't passed
     */
    public int getActiveBidderCount() {
        int count = 0;
        for (int i = 0; i < eligibleBidders.size(); i++) {
            int playerId = eligibleBidders.get(i);
            if (!Boolean.TRUE.equals(playerPassed.get(playerId))) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Completes the auction
     */
    public void complete() {
        if (currentHighBidder >= 0 && currentHighBid > 0) {
            status = AuctionStatus.COMPLETED;
            endTime = System.currentTimeMillis();
        } else {
            cancel();
        }
    }
    
    /**
     * Cancels the auction
     */
    public void cancel() {
        status = AuctionStatus.CANCELLED;
        endTime = System.currentTimeMillis();
    }
    
    /**
     * Expires the auction (timeout)
     */
    public void expire() {
        if (status == AuctionStatus.ACTIVE) {
            if (currentHighBid > 0) {
                complete();
            } else {
                status = AuctionStatus.EXPIRED;
                endTime = System.currentTimeMillis();
            }
        }
    }
    
    /**
     * Checks if auction has timed out
     * @return true if timed out
     */
    public boolean isTimedOut() {
        return System.currentTimeMillis() - startTime > DEFAULT_TIMEOUT_MS;
    }
    
    /**
     * Checks if auction is currently active
     * @return true if auction is in ACTIVE status
     */
    public boolean isActive() {
        return status == AuctionStatus.ACTIVE;
    }
    
    /**
     * Gets the current bid amount
     * @return current high bid amount
     */
    public int getCurrentBid() {
        return currentHighBid;
    }
    
    /**
     * Gets the current bidder
     * @return Player ID of current bidder, or -1
     */
    public int getCurrentBidder() {
        if (eligibleBidders.isEmpty() || status != AuctionStatus.ACTIVE) {
            return -1;
        }
        return eligibleBidders.get(currentBidderIndex);
    }
    
    // ==================== Getters ====================
    
    public String getAuctionId() {
        return auctionId;
    }
    
    public Property getProperty() {
        return property;
    }
    
    public int getCurrentHighBid() {
        return currentHighBid;
    }
    
    public int getCurrentHighBidder() {
        return currentHighBidder;
    }
    
    public int getWinningBid() {
        return status == AuctionStatus.COMPLETED ? currentHighBid : 0;
    }
    
    public int getWinner() {
        return status == AuctionStatus.COMPLETED ? currentHighBidder : -1;
    }
    
    public AuctionStatus getStatus() {
        return status;
    }
    
    public ArrayList<Bid> getBidHistory() {
        return bidHistory;
    }
    
    public ArrayList<Integer> getEligibleBidders() {
        return eligibleBidders;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    /**
     * Gets a player's highest bid
     * @param playerId The player ID
     * @return Their highest bid, or 0
     */
    public int getPlayerBid(int playerId) {
        Integer bid = playerBids.get(playerId);
        return bid != null ? bid : 0;
    }
    
    /**
     * Checks if a player has passed
     * @param playerId The player ID
     * @return true if passed
     */
    public boolean hasPlayerPassed(int playerId) {
        return Boolean.TRUE.equals(playerPassed.get(playerId));
    }
    
    /**
     * Gets the minimum bid for next bidder
     * @return Minimum acceptable bid
     */
    public int getMinimumNextBid() {
        return Math.max(MINIMUM_BID, currentHighBid + MINIMUM_INCREMENT);
    }
    
    /**
     * Gets remaining time in milliseconds
     * @return Time remaining until timeout
     */
    public long getRemainingTime() {
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0, DEFAULT_TIMEOUT_MS - elapsed);
    }
    
    @Override
    public String toString() {
        return "Auction{" +
                "id='" + auctionId + '\'' +
                ", property=" + property.getName() +
                ", highBid=$" + currentHighBid +
                ", highBidder=" + currentHighBidder +
                ", status=" + status +
                ", activeBidders=" + getActiveBidderCount() +
                '}';
    }
}
