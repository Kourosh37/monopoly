package com.monopoly.model.game;

/**
 * Enum representing the overall status of the game.
 */
public enum GameStatus {
    
    WAITING("Waiting for players"),
    STARTING("Game starting"),
    IN_PROGRESS("Game in progress"),
    PAUSED("Game paused"),
    FINISHED("Game finished");

    private final String description;

    GameStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canJoin() {
        return this == WAITING;
    }

    public boolean isPlayable() {
        return this == IN_PROGRESS;
    }

    public boolean isEnded() {
        return this == FINISHED;
    }
}
