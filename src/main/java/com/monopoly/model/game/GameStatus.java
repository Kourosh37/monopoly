package com.monopoly.model.game;

/**
 * Enum representing the overall status of the game.
 */
public enum GameStatus {
    
    WAITING,       // Waiting for players to connect
    STARTING,      // All players connected, game starting
    IN_PROGRESS,   // Game is being played
    PAUSED,        // Game paused (e.g., player disconnected)
    FINISHED       // Game ended, winner determined

    // TODO: Add helper methods if needed

}
