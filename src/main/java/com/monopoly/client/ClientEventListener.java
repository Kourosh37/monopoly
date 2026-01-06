package com.monopoly.client;

/**
 * Interface for listening to client events.
 * Implemented by the GUI to receive updates from the client.
 */
public interface ClientEventListener {

    /**
     * Called when connected to server
     */
    void onConnected();
    
    /**
     * Called when disconnected from server
     */
    void onDisconnected();
    
    /**
     * Called when game state is updated
     * @param stateJson Game state as JSON
     */
    void onStateUpdate(String stateJson);
    
    /**
     * Called when an event log is received
     * @param event Event description
     */
    void onEventLog(String event);
    
    /**
     * Called when an error occurs
     * @param errorMessage Error message
     */
    void onError(String errorMessage);
    
    /**
     * Called when game starts
     */
    void onGameStart();
    
    /**
     * Called when game ends
     * @param winnerId Winner's player ID
     * @param winnerName Winner's name
     */
    void onGameEnd(int winnerId, String winnerName);
    
    /**
     * Called when a turn starts
     * @param currentPlayerId Current player's ID
     */
    void onTurnStart(int currentPlayerId);
    
    /**
     * Called when dice are rolled
     * @param die1 First die value
     * @param die2 Second die value
     * @param isDoubles true if doubles
     */
    void onDiceRolled(int die1, int die2, boolean isDoubles);
    
    /**
     * Called when a player joins
     * @param playerId Player's ID
     * @param playerName Player's name
     */
    void onPlayerJoined(int playerId, String playerName);
    
    /**
     * Called when a player leaves
     * @param playerId Player's ID
     * @param reason Disconnect reason
     */
    void onPlayerLeft(int playerId, String reason);
    
    /**
     * Called when auction starts
     * @param propertyId Property ID
     * @param propertyName Property name
     */
    void onAuctionStart(int propertyId, String propertyName);
    
    /**
     * Called when auction is updated
     * @param currentBid Current bid
     * @param highestBidderId Highest bidder's ID
     */
    void onAuctionUpdate(int currentBid, int highestBidderId);
    
    /**
     * Called when auction ends
     * @param winnerId Winner's ID
     * @param winningBid Winning bid
     */
    void onAuctionEnd(int winnerId, int winningBid);
    
    /**
     * Called when a card is drawn
     * @param cardType Card type (Chance/Community Chest)
     * @param description Card description
     */
    void onCardDrawn(String cardType, String description);
    
    /**
     * Called when a player goes bankrupt
     * @param playerId Bankrupt player's ID
     */
    void onPlayerBankrupt(int playerId);
    
    /**
     * Called when trade is proposed
     * @param initiatorId Initiator's ID
     * @param receiverId Receiver's ID
     */
    void onTradeProposed(int initiatorId, int receiverId);
    
    /**
     * Called when trade is completed
     * @param accepted true if accepted
     */
    void onTradeCompleted(boolean accepted);
}
