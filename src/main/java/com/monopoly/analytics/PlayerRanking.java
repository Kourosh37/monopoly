package com.monopoly.analytics;

/**
 * Manages player rankings using BST for sorted data.
 * Allows In-Order Traversal for generating ranked lists.
 */
public class PlayerRanking implements Comparable<PlayerRanking> {

    private final int playerId;
    private String playerName;
    private int netWorth;
    private int totalRentCollected;
    private int propertyCount;
    private int buildingCount;
    
    /**
     * Creates a PlayerRanking instance.
     */
    public PlayerRanking(int playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.netWorth = 0;
        this.totalRentCollected = 0;
        this.propertyCount = 0;
        this.buildingCount = 0;
    }
    
    // Getters
    public int getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public int getNetWorth() { return netWorth; }
    public int getTotalRentCollected() { return totalRentCollected; }
    public int getPropertyCount() { return propertyCount; }
    public int getBuildingCount() { return buildingCount; }
    
    // Setters
    public void setPlayerName(String name) { this.playerName = name; }
    public void setNetWorth(int netWorth) { this.netWorth = netWorth; }
    
    // Update methods
    public void updateNetWorth(int newWorth) {
        this.netWorth = newWorth;
    }
    
    public void updateRentCollected(int additional) {
        this.totalRentCollected += additional;
    }
    
    public void updatePropertyCount(int count) {
        this.propertyCount = count;
    }
    
    public void updateBuildingCount(int count) {
        this.buildingCount = count;
    }
    
    @Override
    public int compareTo(PlayerRanking other) {
        // Higher net worth ranks first (descending order)
        return Integer.compare(other.netWorth, this.netWorth);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PlayerRanking other = (PlayerRanking) obj;
        return playerId == other.playerId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(playerId);
    }
    
    @Override
    public String toString() {
        return "PlayerRanking{" +
                "playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", netWorth=$" + netWorth +
                ", properties=" + propertyCount +
                ", buildings=" + buildingCount +
                '}';
    }
}
