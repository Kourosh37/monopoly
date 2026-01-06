package com.monopoly.logic;

import com.monopoly.datastructures.ArrayList;
import com.monopoly.model.game.GameState;
import com.monopoly.model.game.Trade;
import com.monopoly.model.player.Player;
import com.monopoly.model.property.Property;

/**
 * Manages trade negotiations between players.
 * Trades require mutual acceptance and are atomic.
 */
public class TradeManager {

    private final GameState gameState;
    
    /**
     * Creates a TradeManager for a game
     * @param gameState The game state
     */
    public TradeManager(GameState gameState) {
        this.gameState = gameState;
    }
    
    /**
     * Proposes a new trade
     * @param proposerId The proposing player's ID
     * @param targetId The target player's ID
     * @return The created Trade, or null if invalid
     */
    public Trade proposeTrade(int proposerId, int targetId) {
        // Validate players
        Player proposer = gameState.getPlayer(proposerId);
        Player target = gameState.getPlayer(targetId);
        
        if (proposer == null || target == null) {
            return null;
        }
        
        if (proposer.isBankrupt() || target.isBankrupt()) {
            return null;
        }
        
        if (proposerId == targetId) {
            return null;
        }
        
        // Can't start trade if one is already active
        if (gameState.hasActiveTrade()) {
            return null;
        }
        
        // Create the trade
        Trade trade = new Trade(proposerId, targetId);
        gameState.setActiveTrade(trade);
        
        return trade;
    }
    
    /**
     * Adds an item to the current trade
     * @param playerId The player adding the item
     * @param property Property to add (can be null)
     * @param money Money amount to add
     * @param jailCards Jail cards to add
     * @return true if item was added
     */
    public boolean addToTrade(int playerId, Property property, int money, int jailCards) {
        Trade trade = gameState.getActiveTrade();
        
        if (trade == null || trade.getStatus() != Trade.TradeStatus.PENDING) {
            return false;
        }
        
        if (playerId == trade.getInitiatorId()) {
            if (property != null) {
                trade.addInitiatorProperty(property);
            }
            if (money > 0) {
                trade.setInitiatorMoney(trade.getInitiatorMoney() + money);
            }
            if (jailCards > 0) {
                trade.setInitiatorJailCards(trade.getInitiatorJailCards() + jailCards);
            }
            return true;
        } else if (playerId == trade.getReceiverId()) {
            if (property != null) {
                trade.addReceiverProperty(property);
            }
            if (money > 0) {
                trade.setReceiverMoney(trade.getReceiverMoney() + money);
            }
            if (jailCards > 0) {
                trade.setReceiverJailCards(trade.getReceiverJailCards() + jailCards);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Accepts the current trade
     * @param playerId Must be the target player
     * @return true if trade was accepted
     */
    public boolean acceptTrade(int playerId) {
        Trade trade = gameState.getActiveTrade();
        
        if (trade == null) {
            return false;
        }
        
        // Only receiver can accept
        if (playerId != trade.getReceiverId()) {
            return false;
        }
        
        // Validate the trade
        Player initiator = gameState.getPlayer(trade.getInitiatorId());
        Player receiver = gameState.getPlayer(trade.getReceiverId());
        
        if (!trade.isValid(initiator, receiver)) {
            return false;
        }
        
        // Accept and execute
        trade.accept();
        return executeTrade();
    }
    
    /**
     * Declines the current trade
     * @param playerId The declining player
     * @return true if trade was declined
     */
    public boolean declineTrade(int playerId) {
        Trade trade = gameState.getActiveTrade();
        
        if (trade == null) {
            return false;
        }
        
        // Either party can decline
        if (playerId != trade.getInitiatorId() && playerId != trade.getReceiverId()) {
            return false;
        }
        
        trade.reject();
        gameState.clearActiveTrade();
        
        return true;
    }
    
    /**
     * Cancels the current trade (by proposer)
     * @param playerId Must be the proposer
     * @return true if trade was cancelled
     */
    public boolean cancelTrade(int playerId) {
        Trade trade = gameState.getActiveTrade();
        
        if (trade == null) {
            return false;
        }
        
        // Only initiator can cancel
        if (playerId != trade.getInitiatorId()) {
            return false;
        }
        
        trade.cancel();
        gameState.clearActiveTrade();
        
        return true;
    }
    
    /**
     * Creates a counter proposal
     * @param playerId The player counter-proposing
     * @param newTrade The new trade proposal
     * @return true if counter proposal was set
     */
    public boolean counterProposal(int playerId, Trade newTrade) {
        Trade currentTrade = gameState.getActiveTrade();
        
        if (currentTrade == null) {
            return false;
        }
        
        // Only receiver can counter
        if (playerId != currentTrade.getReceiverId()) {
            return false;
        }
        
        // Cancel current trade
        currentTrade.cancel();
        
        // Set new trade (roles reversed - receiver becomes initiator)
        gameState.setActiveTrade(newTrade);
        
        return true;
    }
    
    /**
     * Executes the current trade atomically
     * @return true if trade was executed successfully
     */
    private boolean executeTrade() {
        Trade trade = gameState.getActiveTrade();
        
        if (trade == null || trade.getStatus() != Trade.TradeStatus.ACCEPTED) {
            return false;
        }
        
        Player initiator = gameState.getPlayer(trade.getInitiatorId());
        Player receiver = gameState.getPlayer(trade.getReceiverId());
        
        if (initiator == null || receiver == null) {
            return false;
        }
        
        // === Transfer from Initiator to Receiver ===
        
        // Transfer money
        if (trade.getInitiatorMoney() > 0) {
            initiator.removeMoney(trade.getInitiatorMoney());
            receiver.addMoney(trade.getInitiatorMoney());
            gameState.recordTransaction(trade.getInitiatorId(), trade.getReceiverId(), trade.getInitiatorMoney());
        }
        
        // Transfer properties
        ArrayList<Property> initiatorProps = trade.getInitiatorProperties();
        for (int i = 0; i < initiatorProps.size(); i++) {
            Property prop = initiatorProps.get(i);
            initiator.removeProperty(prop);
            receiver.addProperty(prop);
            prop.setOwnerId(trade.getReceiverId());
        }
        
        // Transfer jail cards
        for (int i = 0; i < trade.getInitiatorJailCards(); i++) {
            initiator.useGetOutOfJailCard();
            receiver.addGetOutOfJailCard();
        }
        
        // === Transfer from Receiver to Initiator ===
        
        // Transfer money
        if (trade.getReceiverMoney() > 0) {
            receiver.removeMoney(trade.getReceiverMoney());
            initiator.addMoney(trade.getReceiverMoney());
            gameState.recordTransaction(trade.getReceiverId(), trade.getInitiatorId(), trade.getReceiverMoney());
        }
        
        // Transfer properties
        ArrayList<Property> receiverProps = trade.getReceiverProperties();
        for (int i = 0; i < receiverProps.size(); i++) {
            Property prop = receiverProps.get(i);
            receiver.removeProperty(prop);
            initiator.addProperty(prop);
            prop.setOwnerId(trade.getInitiatorId());
        }
        
        // Transfer jail cards
        for (int i = 0; i < trade.getReceiverJailCards(); i++) {
            receiver.useGetOutOfJailCard();
            initiator.addGetOutOfJailCard();
        }
        
        // Mark trade as complete
        trade.complete();
        gameState.clearActiveTrade();
        
        // Update rankings
        gameState.updatePlayerRanking(initiator);
        gameState.updatePlayerRanking(receiver);
        
        return true;
    }
    
    /**
     * Validates a trade proposal
     * @param trade The trade to validate
     * @return true if trade is valid
     */
    public boolean validateTrade(Trade trade) {
        if (trade == null) {
            return false;
        }
        
        Player initiator = gameState.getPlayer(trade.getInitiatorId());
        Player receiver = gameState.getPlayer(trade.getReceiverId());
        
        return trade.isValid(initiator, receiver);
    }
    
    /**
     * Checks if a player can initiate a trade
     * @param playerId The player's ID
     * @return true if player can trade
     */
    public boolean canTrade(int playerId) {
        // Can't trade if a trade is already active
        if (gameState.hasActiveTrade()) {
            return false;
        }
        
        // Can't trade during auction
        if (gameState.hasActiveAuction()) {
            return false;
        }
        
        Player player = gameState.getPlayer(playerId);
        if (player == null || player.isBankrupt()) {
            return false;
        }
        
        // Need something to trade
        return player.getMoney() > 0 || 
               player.getPropertyCount() > 0 || 
               player.getGetOutOfJailCards() > 0;
    }
    
    /**
     * Checks if a trade is currently active
     * @return true if trade is active
     */
    public boolean isTradeActive() {
        return gameState.hasActiveTrade();
    }
    
    /**
     * Gets the current trade proposal
     * @return Current trade, or null
     */
    public Trade getCurrentTrade() {
        return gameState.getActiveTrade();
    }
    
    /**
     * Gets the proposer's ID
     * @return Proposer ID, or -1
     */
    public int getProposerId() {
        Trade trade = gameState.getActiveTrade();
        return trade != null ? trade.getInitiatorId() : -1;
    }
    
    /**
     * Gets the target's ID
     * @return Target ID, or -1
     */
    public int getTargetId() {
        Trade trade = gameState.getActiveTrade();
        return trade != null ? trade.getReceiverId() : -1;
    }
    
    /**
     * Gets players that a player can trade with
     * @param playerId The player's ID
     * @return ArrayList of tradeable player IDs
     */
    public ArrayList<Integer> getTradeablePlayerIds(int playerId) {
        ArrayList<Integer> tradeable = new ArrayList<>();
        ArrayList<Integer> playerOrder = gameState.getPlayerOrder();
        
        for (int i = 0; i < playerOrder.size(); i++) {
            int otherId = playerOrder.get(i);
            if (otherId != playerId) {
                Player other = gameState.getPlayer(otherId);
                if (other != null && !other.isBankrupt()) {
                    tradeable.add(otherId);
                }
            }
        }
        
        return tradeable;
    }
}
