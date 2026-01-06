package com.monopoly.model.game;

import com.monopoly.datastructures.ArrayList;
import com.monopoly.model.property.Property;

/**
 * Represents a trade offer between two players.
 * Players can trade properties, money, and Get Out of Jail cards.
 */
public class Trade {

    /**
     * Status of the trade
     */
    public enum TradeStatus {
        PENDING,    // Waiting for response
        ACCEPTED,   // Trade accepted
        REJECTED,   // Trade rejected
        CANCELLED,  // Trade cancelled by initiator
        COMPLETED,  // Trade executed
        EXPIRED     // Trade timed out
    }
    
    // Trade participants
    private final int initiatorId;
    private final int receiverId;
    
    // What initiator is offering
    private final ArrayList<Property> initiatorProperties;
    private int initiatorMoney;
    private int initiatorJailCards;
    
    // What receiver is offering
    private final ArrayList<Property> receiverProperties;
    private int receiverMoney;
    private int receiverJailCards;
    
    // Trade state
    private TradeStatus status;
    private final long createdTime;
    private String tradeId;
    
    /**
     * Creates a new trade offer
     * @param initiatorId The player initiating the trade
     * @param receiverId The player receiving the trade offer
     */
    public Trade(int initiatorId, int receiverId) {
        this.initiatorId = initiatorId;
        this.receiverId = receiverId;
        this.initiatorProperties = new ArrayList<>();
        this.receiverProperties = new ArrayList<>();
        this.initiatorMoney = 0;
        this.receiverMoney = 0;
        this.initiatorJailCards = 0;
        this.receiverJailCards = 0;
        this.status = TradeStatus.PENDING;
        this.createdTime = System.currentTimeMillis();
        this.tradeId = "trade_" + initiatorId + "_" + receiverId + "_" + createdTime;
    }
    
    // ==================== Initiator Offers ====================
    
    /**
     * Adds a property to the initiator's offer
     * @param property The property to offer
     */
    public void addInitiatorProperty(Property property) {
        if (property != null && property.getOwnerId() == initiatorId) {
            initiatorProperties.add(property);
        }
    }
    
    /**
     * Removes a property from the initiator's offer
     * @param property The property to remove
     */
    public void removeInitiatorProperty(Property property) {
        for (int i = 0; i < initiatorProperties.size(); i++) {
            if (initiatorProperties.get(i).getId() == property.getId()) {
                initiatorProperties.remove(i);
                break;
            }
        }
    }
    
    /**
     * Sets the money the initiator is offering
     * @param amount The amount
     */
    public void setInitiatorMoney(int amount) {
        this.initiatorMoney = Math.max(0, amount);
    }
    
    /**
     * Sets the jail cards the initiator is offering
     * @param count Number of cards
     */
    public void setInitiatorJailCards(int count) {
        this.initiatorJailCards = Math.max(0, count);
    }
    
    // ==================== Receiver Offers ====================
    
    /**
     * Adds a property to the receiver's offer
     * @param property The property to offer
     */
    public void addReceiverProperty(Property property) {
        if (property != null && property.getOwnerId() == receiverId) {
            receiverProperties.add(property);
        }
    }
    
    /**
     * Removes a property from the receiver's offer
     * @param property The property to remove
     */
    public void removeReceiverProperty(Property property) {
        for (int i = 0; i < receiverProperties.size(); i++) {
            if (receiverProperties.get(i).getId() == property.getId()) {
                receiverProperties.remove(i);
                break;
            }
        }
    }
    
    /**
     * Sets the money the receiver is offering
     * @param amount The amount
     */
    public void setReceiverMoney(int amount) {
        this.receiverMoney = Math.max(0, amount);
    }
    
    /**
     * Sets the jail cards the receiver is offering
     * @param count Number of cards
     */
    public void setReceiverJailCards(int count) {
        this.receiverJailCards = Math.max(0, count);
    }
    
    // ==================== Trade Status ====================
    
    /**
     * Accepts the trade
     */
    public void accept() {
        if (status == TradeStatus.PENDING) {
            status = TradeStatus.ACCEPTED;
        }
    }
    
    /**
     * Rejects the trade
     */
    public void reject() {
        if (status == TradeStatus.PENDING) {
            status = TradeStatus.REJECTED;
        }
    }
    
    /**
     * Cancels the trade
     */
    public void cancel() {
        if (status == TradeStatus.PENDING) {
            status = TradeStatus.CANCELLED;
        }
    }
    
    /**
     * Marks the trade as completed
     */
    public void complete() {
        if (status == TradeStatus.ACCEPTED) {
            status = TradeStatus.COMPLETED;
        }
    }
    
    /**
     * Marks the trade as expired
     */
    public void expire() {
        if (status == TradeStatus.PENDING) {
            status = TradeStatus.EXPIRED;
        }
    }
    
    // ==================== Validation ====================
    
    /**
     * Validates that this trade is valid
     * @param initiator The initiating player
     * @param receiver The receiving player
     * @return true if valid
     */
    public boolean isValid(com.monopoly.model.player.Player initiator, 
                          com.monopoly.model.player.Player receiver) {
        // Check participants
        if (initiator == null || receiver == null) return false;
        if (initiator.getId() != initiatorId) return false;
        if (receiver.getId() != receiverId) return false;
        if (initiator.isBankrupt() || receiver.isBankrupt()) return false;
        
        // Check initiator can afford
        if (initiatorMoney > initiator.getMoney()) return false;
        if (initiatorJailCards > initiator.getGetOutOfJailCards()) return false;
        
        // Check receiver can afford
        if (receiverMoney > receiver.getMoney()) return false;
        if (receiverJailCards > receiver.getGetOutOfJailCards()) return false;
        
        // Check property ownership and no buildings
        for (int i = 0; i < initiatorProperties.size(); i++) {
            Property prop = initiatorProperties.get(i);
            if (prop.getOwnerId() != initiatorId) return false;
            if (prop.hasBuildings()) return false; // Can't trade with buildings
        }
        
        for (int i = 0; i < receiverProperties.size(); i++) {
            Property prop = receiverProperties.get(i);
            if (prop.getOwnerId() != receiverId) return false;
            if (prop.hasBuildings()) return false;
        }
        
        return true;
    }
    
    /**
     * Checks if the trade is empty (nothing being traded)
     * @return true if empty
     */
    public boolean isEmpty() {
        return initiatorProperties.isEmpty() && 
               receiverProperties.isEmpty() && 
               initiatorMoney == 0 && 
               receiverMoney == 0 &&
               initiatorJailCards == 0 &&
               receiverJailCards == 0;
    }
    
    // ==================== Getters ====================
    
    public int getInitiatorId() {
        return initiatorId;
    }
    
    public int getReceiverId() {
        return receiverId;
    }
    
    public ArrayList<Property> getInitiatorProperties() {
        return initiatorProperties;
    }
    
    public int getInitiatorMoney() {
        return initiatorMoney;
    }
    
    public int getInitiatorJailCards() {
        return initiatorJailCards;
    }
    
    public ArrayList<Property> getReceiverProperties() {
        return receiverProperties;
    }
    
    public int getReceiverMoney() {
        return receiverMoney;
    }
    
    public int getReceiverJailCards() {
        return receiverJailCards;
    }
    
    public TradeStatus getStatus() {
        return status;
    }
    
    /**
     * Checks if the trade is pending
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return status == TradeStatus.PENDING;
    }
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public String getTradeId() {
        return tradeId;
    }
    
    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }
    
    /**
     * Gets the total value being offered by the initiator
     * @return Total value
     */
    public int getInitiatorTotalValue() {
        int total = initiatorMoney;
        for (int i = 0; i < initiatorProperties.size(); i++) {
            total += initiatorProperties.get(i).getPrice();
        }
        return total;
    }
    
    /**
     * Gets the total value being offered by the receiver
     * @return Total value
     */
    public int getReceiverTotalValue() {
        int total = receiverMoney;
        for (int i = 0; i < receiverProperties.size(); i++) {
            total += receiverProperties.get(i).getPrice();
        }
        return total;
    }
    
    @Override
    public String toString() {
        return "Trade{" +
                "id='" + tradeId + '\'' +
                ", initiator=" + initiatorId +
                " offers: " + initiatorProperties.size() + " props, $" + initiatorMoney + ", " + initiatorJailCards + " jail cards" +
                ", receiver=" + receiverId +
                " offers: " + receiverProperties.size() + " props, $" + receiverMoney + ", " + receiverJailCards + " jail cards" +
                ", status=" + status +
                '}';
    }
}
