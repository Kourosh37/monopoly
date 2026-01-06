package com.monopoly.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a trade proposal between two players.
 */
public class TradeProposal {

    private int proposerId;
    private int targetId;
    private List<Integer> offeredProperties;
    private List<Integer> requestedProperties;
    private int offeredMoney;
    private int requestedMoney;
    private int offeredGetOutOfJailCards;
    private int requestedGetOutOfJailCards;
    
    /**
     * Creates a new trade proposal.
     */
    public TradeProposal(int proposerId, int targetId, 
                         List<Integer> offeredProperties, List<Integer> requestedProperties,
                         int offeredMoney, int requestedMoney,
                         int offeredGetOutOfJailCards, int requestedGetOutOfJailCards) {
        this.proposerId = proposerId;
        this.targetId = targetId;
        this.offeredProperties = offeredProperties != null ? new ArrayList<>(offeredProperties) : new ArrayList<>();
        this.requestedProperties = requestedProperties != null ? new ArrayList<>(requestedProperties) : new ArrayList<>();
        this.offeredMoney = offeredMoney;
        this.requestedMoney = requestedMoney;
        this.offeredGetOutOfJailCards = offeredGetOutOfJailCards;
        this.requestedGetOutOfJailCards = requestedGetOutOfJailCards;
    }
    
    /**
     * Default constructor for empty trade proposal.
     */
    public TradeProposal() {
        this.offeredProperties = new ArrayList<>();
        this.requestedProperties = new ArrayList<>();
    }
    
    public int getProposerId() {
        return proposerId;
    }
    
    public void setProposerId(int proposerId) {
        this.proposerId = proposerId;
    }
    
    public int getTargetId() {
        return targetId;
    }
    
    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }
    
    /**
     * For backward compatibility - alias for getProposerId()
     */
    public int getFromPlayerId() {
        return proposerId;
    }
    
    public List<Integer> getOfferedProperties() {
        return offeredProperties;
    }
    
    public List<Integer> getRequestedProperties() {
        return requestedProperties;
    }
    
    public int getOfferedMoney() {
        return offeredMoney;
    }
    
    public void setOfferedMoney(int amount) {
        this.offeredMoney = amount;
    }
    
    public int getRequestedMoney() {
        return requestedMoney;
    }
    
    public void setRequestedMoney(int amount) {
        this.requestedMoney = amount;
    }
    
    public int getOfferedGetOutOfJailCards() {
        return offeredGetOutOfJailCards;
    }
    
    public void setOfferedGetOutOfJailCards(int count) {
        this.offeredGetOutOfJailCards = count;
    }
    
    public int getRequestedGetOutOfJailCards() {
        return requestedGetOutOfJailCards;
    }
    
    public void setRequestedGetOutOfJailCards(int count) {
        this.requestedGetOutOfJailCards = count;
    }
    
    public void addOfferedProperty(int propertyId) {
        if (!offeredProperties.contains(propertyId)) {
            offeredProperties.add(propertyId);
        }
    }
    
    public void addRequestedProperty(int propertyId) {
        if (!requestedProperties.contains(propertyId)) {
            requestedProperties.add(propertyId);
        }
    }
    
    public void removeOfferedProperty(int propertyId) {
        offeredProperties.remove(Integer.valueOf(propertyId));
    }
    
    public void removeRequestedProperty(int propertyId) {
        requestedProperties.remove(Integer.valueOf(propertyId));
    }
    
    /**
     * Checks if the trade is valid (has at least something to trade).
     */
    public boolean isValid() {
        boolean hasOffer = !offeredProperties.isEmpty() || offeredMoney > 0 || offeredGetOutOfJailCards > 0;
        boolean hasRequest = !requestedProperties.isEmpty() || requestedMoney > 0 || requestedGetOutOfJailCards > 0;
        return hasOffer || hasRequest;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TradeProposal{");
        sb.append("from=").append(proposerId);
        sb.append(", to=").append(targetId);
        if (!offeredProperties.isEmpty()) {
            sb.append(", offeredProps=").append(offeredProperties);
        }
        if (!requestedProperties.isEmpty()) {
            sb.append(", requestedProps=").append(requestedProperties);
        }
        if (offeredMoney > 0) {
            sb.append(", offeredMoney=$").append(offeredMoney);
        }
        if (requestedMoney > 0) {
            sb.append(", requestedMoney=$").append(requestedMoney);
        }
        if (offeredGetOutOfJailCards > 0) {
            sb.append(", offeredJailCards=").append(offeredGetOutOfJailCards);
        }
        if (requestedGetOutOfJailCards > 0) {
            sb.append(", requestedJailCards=").append(requestedGetOutOfJailCards);
        }
        sb.append('}');
        return sb.toString();
    }
}
