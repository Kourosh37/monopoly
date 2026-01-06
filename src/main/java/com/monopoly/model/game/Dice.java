package com.monopoly.model.game;

import java.util.Random;

/**
 * Represents the dice used in the game.
 * Server generates random values (2-12).
 * Handles doubles detection for extra turns and jail release.
 */
public class Dice {

    // Constants
    public static final int MIN_VALUE = 1;
    public static final int MAX_VALUE = 6;
    public static final int MAX_DOUBLES = 3; // Go to jail on third consecutive doubles
    
    // Fields
    private int die1;
    private int die2;
    private int consecutiveDoubles;
    private final Random random;
    private boolean hasRolled;
    
    /**
     * Creates a new Dice instance
     */
    public Dice() {
        this.random = new Random();
        this.die1 = 0;
        this.die2 = 0;
        this.consecutiveDoubles = 0;
        this.hasRolled = false;
    }
    
    /**
     * Creates a Dice instance with a specific seed for testing
     * @param seed The seed for the random number generator
     */
    public Dice(long seed) {
        this.random = new Random(seed);
        this.die1 = 0;
        this.die2 = 0;
        this.consecutiveDoubles = 0;
        this.hasRolled = false;
    }
    
    /**
     * Rolls both dice and generates new random values
     * @return The total of both dice
     */
    public int roll() {
        die1 = random.nextInt(MAX_VALUE) + MIN_VALUE;
        die2 = random.nextInt(MAX_VALUE) + MIN_VALUE;
        hasRolled = true;
        
        if (isDoubles()) {
            consecutiveDoubles++;
        } else {
            consecutiveDoubles = 0;
        }
        
        return getTotal();
    }
    
    /**
     * Sets dice values manually (for testing or network sync)
     * @param value1 First die value
     * @param value2 Second die value
     */
    public void setValues(int value1, int value2) {
        if (value1 < MIN_VALUE || value1 > MAX_VALUE || 
            value2 < MIN_VALUE || value2 > MAX_VALUE) {
            throw new IllegalArgumentException("Dice values must be between " + MIN_VALUE + " and " + MAX_VALUE);
        }
        this.die1 = value1;
        this.die2 = value2;
        this.hasRolled = true;
        
        if (isDoubles()) {
            consecutiveDoubles++;
        } else {
            consecutiveDoubles = 0;
        }
    }
    
    /**
     * Gets the total of both dice
     * @return Sum of die1 and die2
     */
    public int getTotal() {
        return die1 + die2;
    }
    
    /**
     * Gets the value of the first die
     * @return Value of die1
     */
    public int getDie1() {
        return die1;
    }
    
    /**
     * Gets the value of the second die
     * @return Value of die2
     */
    public int getDie2() {
        return die2;
    }
    
    /**
     * Checks if both dice show the same value
     * @return true if both dice are equal
     */
    public boolean isDoubles() {
        return die1 == die2 && die1 > 0;
    }
    
    /**
     * Resets the consecutive doubles counter
     */
    public void resetDoubles() {
        consecutiveDoubles = 0;
    }
    
    /**
     * Gets the current count of consecutive doubles
     * @return Number of consecutive doubles rolled
     */
    public int getConsecutiveDoubles() {
        return consecutiveDoubles;
    }
    
    /**
     * Checks if the player should go to jail (third consecutive doubles)
     * @return true if player has rolled three doubles in a row
     */
    public boolean shouldGoToJail() {
        return consecutiveDoubles >= MAX_DOUBLES;
    }
    
    /**
     * Checks if the dice have been rolled this turn
     * @return true if roll() has been called
     */
    public boolean hasRolled() {
        return hasRolled;
    }
    
    /**
     * Resets the dice for a new turn
     */
    public void resetForNewTurn() {
        hasRolled = false;
    }
    
    /**
     * Resets the dice completely (including doubles counter)
     */
    public void resetCompletely() {
        die1 = 0;
        die2 = 0;
        consecutiveDoubles = 0;
        hasRolled = false;
    }
    
    /**
     * Gets the minimum possible roll
     * @return The minimum total (2)
     */
    public static int getMinRoll() {
        return MIN_VALUE * 2;
    }
    
    /**
     * Gets the maximum possible roll
     * @return The maximum total (12)
     */
    public static int getMaxRoll() {
        return MAX_VALUE * 2;
    }
    
    @Override
    public String toString() {
        return "Dice{" +
                "die1=" + die1 +
                ", die2=" + die2 +
                ", total=" + getTotal() +
                ", doubles=" + isDoubles() +
                ", consecutiveDoubles=" + consecutiveDoubles +
                '}';
    }
}
