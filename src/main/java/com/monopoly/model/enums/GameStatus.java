package com.monopoly.model.enums;

/**
 * Represents the overall status of the game.
 */
public enum GameStatus {
    WAITING_FOR_PLAYERS("Waiting for players to join"),
    WAITING("Waiting for players to join"),  // Alias for compatibility
    READY_TO_START("Ready to start"),
    IN_PROGRESS("Game in progress"),
    PAUSED("Game paused"),
    ENDED("Game ended"),
    FINISHED("Game finished");  // Alias for compatibility
    
    private final String description;
    
    GameStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
