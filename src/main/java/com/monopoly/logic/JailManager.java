package com.monopoly.logic;

import com.monopoly.model.game.Board;
import com.monopoly.model.game.GameState;
import com.monopoly.model.player.Player;

/**
 * Manages jail mechanics.
 * Players can be released via: doubles roll, paying fine, or using a card.
 * Maximum 3 turns in jail (standard Monopoly rules).
 */
public class JailManager {

    // Constants
    public static final int JAIL_FINE = 50;
    public static final int MAX_TURNS_IN_JAIL = 3;
    public static final int JAIL_POSITION = Board.JAIL_POSITION;
    public static final int GO_TO_JAIL_POSITION = Board.GO_TO_JAIL_POSITION;
    
    // Game state reference (optional)
    private GameState gameState;
    
    /**
     * Creates a new JailManager
     */
    public JailManager() {
        // No state needed - all operations on Player objects
    }
    
    /**
     * Creates a new JailManager with GameState reference
     * @param gameState the game state
     */
    public JailManager(GameState gameState) {
        this.gameState = gameState;
    }
    
    /**
     * Handles jail roll for current player (if has game state)
     * @return true if player was released from jail
     */
    public boolean handleJailRoll() {
        if (gameState == null) {
            return false;
        }
        Player player = gameState.getCurrentPlayer();
        if (player == null || !player.isInJail()) {
            return false;
        }
        // Check if doubles were rolled - this is handled externally
        return false;
    }

    /**
     * Sends a player to jail
     * @param player The player to send to jail
     */
    public void sendToJail(Player player) {
        if (player != null && !player.isBankrupt()) {
            player.sendToJail();
        }
    }
    
    /**
     * Attempts to release a player by rolling doubles
     * @param player The player attempting release
     * @param rolledDoubles Whether the player rolled doubles
     * @return true if player was released
     */
    public boolean attemptReleaseByDoubles(Player player, boolean rolledDoubles) {
        if (player == null || !player.isInJail()) {
            return false;
        }
        
        if (rolledDoubles) {
            player.releaseFromJail();
            return true;
        }
        
        return false;
    }
    
    /**
     * Releases a player by paying the fine
     * @param player The player paying fine
     * @return true if successful
     */
    public boolean releaseByFine(Player player) {
        if (player == null || !player.isInJail()) {
            return false;
        }
        
        if (!canPayFine(player)) {
            return false;
        }
        
        player.payBail();
        return true;
    }
    
    /**
     * Releases a player by using a Get Out of Jail Free card
     * @param player The player using the card
     * @return true if successful
     */
    public boolean releaseByCard(Player player) {
        if (player == null || !player.isInJail()) {
            return false;
        }
        
        if (!canUseCard(player)) {
            return false;
        }
        
        player.useGetOutOfJailCard();
        player.releaseFromJail();
        return true;
    }
    
    /**
     * Increments a player's turn count in jail
     * Called at the start of each turn while in jail
     * @param player The player in jail
     */
    public void incrementJailTurn(Player player) {
        if (player != null && player.isInJail()) {
            player.incrementTurnsInJail();
        }
    }
    
    /**
     * Checks if a player must leave jail (max turns reached)
     * @param player The player to check
     * @return true if must leave
     */
    public boolean mustLeaveJail(Player player) {
        if (player == null || !player.isInJail()) {
            return false;
        }
        return player.getTurnsInJail() >= MAX_TURNS_IN_JAIL;
    }
    
    /**
     * Forces release after maximum turns - must pay fine
     * @param player The player to release
     * @return true if released (false if can't pay and goes bankrupt)
     */
    public boolean forceRelease(Player player) {
        if (player == null || !player.isInJail()) {
            return false;
        }
        
        if (player.getMoney() >= JAIL_FINE) {
            player.payBail();
            return true;
        }
        
        // Player can't pay - may need to mortgage/sell to pay
        // Return false to indicate forced payment needed
        return false;
    }
    
    /**
     * Checks if a player can pay the jail fine
     * @param player The player
     * @return true if can afford fine
     */
    public boolean canPayFine(Player player) {
        return player != null && player.getMoney() >= JAIL_FINE;
    }
    
    /**
     * Checks if a player has a Get Out of Jail Free card
     * @param player The player
     * @return true if has card
     */
    public boolean canUseCard(Player player) {
        return player != null && player.hasGetOutOfJailCard();
    }
    
    /**
     * Checks if a player is in jail
     * @param player The player
     * @return true if in jail
     */
    public boolean isInJail(Player player) {
        return player != null && player.isInJail();
    }
    
    /**
     * Gets the jail fine amount
     * @return Fine amount
     */
    public int getJailFine() {
        return JAIL_FINE;
    }
    
    /**
     * Gets the jail position on the board
     * @return Jail position
     */
    public int getJailPosition() {
        return JAIL_POSITION;
    }
    
    /**
     * Gets the Go To Jail position
     * @return Go To Jail position
     */
    public int getGoToJailPosition() {
        return GO_TO_JAIL_POSITION;
    }
    
    /**
     * Gets maximum turns allowed in jail
     * @return Max turns
     */
    public int getMaxTurnsInJail() {
        return MAX_TURNS_IN_JAIL;
    }
    
    /**
     * Gets player's remaining turns in jail before forced release
     * @param player The player
     * @return Remaining turns (0 if not in jail or must leave)
     */
    public int getRemainingJailTurns(Player player) {
        if (player == null || !player.isInJail()) {
            return 0;
        }
        return Math.max(0, MAX_TURNS_IN_JAIL - player.getTurnsInJail());
    }
    
    /**
     * Gets available release options for a player
     * @param player The player in jail
     * @return String description of options
     */
    public String getReleaseOptions(Player player) {
        if (player == null || !player.isInJail()) {
            return "Not in jail";
        }
        
        StringBuilder options = new StringBuilder();
        options.append("Options: 1) Roll doubles");
        
        if (canPayFine(player)) {
            options.append(", 2) Pay $").append(JAIL_FINE).append(" fine");
        }
        
        if (canUseCard(player)) {
            options.append(", 3) Use Get Out of Jail Free card");
        }
        
        int remaining = getRemainingJailTurns(player);
        if (remaining > 0) {
            options.append(". Turns remaining: ").append(remaining);
        } else {
            options.append(". Must pay fine this turn!");
        }
        
        return options.toString();
    }
}
