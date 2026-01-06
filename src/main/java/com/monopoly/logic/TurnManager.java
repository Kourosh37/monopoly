package com.monopoly.logic;

import com.monopoly.datastructures.ArrayList;
import com.monopoly.datastructures.HashTable;
import com.monopoly.model.enums.TurnPhase;
import com.monopoly.model.game.Dice;
import com.monopoly.model.game.GameState;
import com.monopoly.model.player.Player;

/**
 * Manages turn flow and turn phases.
 * Implements State Machine: TURN_START -> ROLL -> MOVE -> DECISION -> TURN_END
 */
public class TurnManager {

    private final GameState gameState;
    private final JailManager jailManager;
    
    // Allowed actions per phase
    private final HashTable<TurnPhase, ArrayList<String>> allowedActions;
    
    /**
     * Creates a TurnManager for a game
     * @param gameState The game state
     */
    public TurnManager(GameState gameState) {
        this.gameState = gameState;
        this.jailManager = new JailManager();
        this.allowedActions = new HashTable<>();
        initializeAllowedActions();
    }
    
    /**
     * Initializes the allowed actions for each phase
     */
    private void initializeAllowedActions() {
        // TURN_START
        ArrayList<String> turnStartActions = new ArrayList<>();
        turnStartActions.add("VIEW_PROPERTIES");
        turnStartActions.add("VIEW_BOARD");
        allowedActions.put(TurnPhase.TURN_START, turnStartActions);
        
        // PRE_ROLL
        ArrayList<String> preRollActions = new ArrayList<>();
        preRollActions.add("ROLL");
        preRollActions.add("USE_JAIL_CARD");
        preRollActions.add("PAY_JAIL_FINE");
        preRollActions.add("BUILD");
        preRollActions.add("SELL_BUILDING");
        preRollActions.add("MORTGAGE");
        preRollActions.add("UNMORTGAGE");
        preRollActions.add("TRADE");
        allowedActions.put(TurnPhase.PRE_ROLL, preRollActions);
        
        // ROLLING
        ArrayList<String> rollingActions = new ArrayList<>();
        rollingActions.add("AWAIT_ROLL");
        allowedActions.put(TurnPhase.ROLLING, rollingActions);
        
        // POST_ROLL
        ArrayList<String> postRollActions = new ArrayList<>();
        postRollActions.add("MOVE");
        allowedActions.put(TurnPhase.POST_ROLL, postRollActions);
        
        // MOVING
        ArrayList<String> movingActions = new ArrayList<>();
        movingActions.add("AWAIT_MOVE");
        allowedActions.put(TurnPhase.MOVING, movingActions);
        
        // LANDED
        ArrayList<String> landedActions = new ArrayList<>();
        landedActions.add("BUY_PROPERTY");
        landedActions.add("AUCTION_PROPERTY");
        landedActions.add("PAY_RENT");
        landedActions.add("DRAW_CARD");
        landedActions.add("PAY_TAX");
        landedActions.add("GO_TO_JAIL");
        landedActions.add("FREE_PARKING");
        landedActions.add("CONTINUE");
        allowedActions.put(TurnPhase.LANDED, landedActions);
        
        // AWAITING_DECISION
        ArrayList<String> decisionActions = new ArrayList<>();
        decisionActions.add("BUY");
        decisionActions.add("DONT_BUY");
        decisionActions.add("ACCEPT_TRADE");
        decisionActions.add("DECLINE_TRADE");
        decisionActions.add("BID");
        decisionActions.add("PASS_BID");
        allowedActions.put(TurnPhase.AWAITING_DECISION, decisionActions);
        
        // DRAWING_CARD
        ArrayList<String> cardActions = new ArrayList<>();
        cardActions.add("EXECUTE_CARD");
        allowedActions.put(TurnPhase.DRAWING_CARD, cardActions);
        
        // IN_DEBT
        ArrayList<String> debtActions = new ArrayList<>();
        debtActions.add("SELL_BUILDING");
        debtActions.add("MORTGAGE");
        debtActions.add("TRADE");
        debtActions.add("DECLARE_BANKRUPTCY");
        allowedActions.put(TurnPhase.IN_DEBT, debtActions);
        
        // POST_ACTION
        ArrayList<String> postActions = new ArrayList<>();
        postActions.add("BUILD");
        postActions.add("SELL_BUILDING");
        postActions.add("MORTGAGE");
        postActions.add("UNMORTGAGE");
        postActions.add("TRADE");
        postActions.add("END_TURN");
        postActions.add("ROLL_AGAIN"); // If rolled doubles
        allowedActions.put(TurnPhase.POST_ACTION, postActions);
        
        // TURN_END
        ArrayList<String> endActions = new ArrayList<>();
        endActions.add("END_TURN");
        allowedActions.put(TurnPhase.TURN_END, endActions);
        
        // WAITING_FOR_PLAYERS
        ArrayList<String> waitingActions = new ArrayList<>();
        waitingActions.add("WAIT");
        allowedActions.put(TurnPhase.WAITING_FOR_PLAYERS, waitingActions);
        
        // GAME_OVER
        ArrayList<String> gameOverActions = new ArrayList<>();
        gameOverActions.add("VIEW_RESULTS");
        allowedActions.put(TurnPhase.GAME_OVER, gameOverActions);
    }
    
    /**
     * Starts a new turn
     */
    public void startTurn() {
        Player currentPlayer = gameState.getCurrentPlayer();
        
        if (currentPlayer == null) {
            return;
        }
        
        gameState.setTurnPhase(TurnPhase.TURN_START);
        gameState.getDice().resetForNewTurn();
        
        // Check if player is in jail
        if (currentPlayer.isInJail()) {
            jailManager.incrementJailTurn(currentPlayer);
        }
        
        // Advance to pre-roll phase
        gameState.setTurnPhase(TurnPhase.PRE_ROLL);
    }
    
    /**
     * Gets the current player's ID
     * @return Current player ID
     */
    public int getCurrentPlayerId() {
        return gameState.getCurrentPlayerId();
    }
    
    /**
     * Gets the current player
     * @return Current player
     */
    public Player getCurrentPlayer() {
        return gameState.getCurrentPlayer();
    }
    
    /**
     * Gets the current turn phase
     * @return Current phase
     */
    public TurnPhase getCurrentPhase() {
        return gameState.getTurnPhase();
    }
    
    /**
     * Sets the turn phase
     * @param phase New phase
     */
    public void setPhase(TurnPhase phase) {
        gameState.setTurnPhase(phase);
    }
    
    /**
     * Advances to the next phase based on current state
     * @return The new phase
     */
    public TurnPhase advancePhase() {
        TurnPhase current = getCurrentPhase();
        TurnPhase next;
        
        switch (current) {
            case TURN_START:
                next = TurnPhase.PRE_ROLL;
                break;
            case PRE_ROLL:
                next = TurnPhase.ROLLING;
                break;
            case ROLLING:
                next = TurnPhase.POST_ROLL;
                break;
            case POST_ROLL:
                next = TurnPhase.MOVING;
                break;
            case MOVING:
                next = TurnPhase.LANDED;
                break;
            case LANDED:
            case DRAWING_CARD:
                next = TurnPhase.POST_ACTION;
                break;
            case AWAITING_DECISION:
                next = TurnPhase.POST_ACTION;
                break;
            case POST_ACTION:
                if (gameState.canRollAgain()) {
                    gameState.setCanRollAgain(false);
                    next = TurnPhase.PRE_ROLL;
                } else {
                    next = TurnPhase.TURN_END;
                }
                break;
            case IN_DEBT:
                next = TurnPhase.POST_ACTION;
                break;
            default:
                next = current;
        }
        
        setPhase(next);
        return next;
    }
    
    /**
     * Ends the current turn
     */
    public void endTurn() {
        gameState.setTurnPhase(TurnPhase.TURN_END);
        gameState.getDice().resetCompletely();
        gameState.setCanRollAgain(false);
    }
    
    /**
     * Advances to the next player
     */
    public void nextPlayer() {
        endTurn();
        gameState.nextPlayer();
        
        // Check if game is over
        if (gameState.getActivePlayerCount() <= 1) {
            gameState.setTurnPhase(TurnPhase.GAME_OVER);
            determineWinner();
        } else {
            startTurn();
        }
    }
    
    /**
     * Skips a player (when they go bankrupt)
     * @param playerId The player to skip
     */
    public void skipPlayer(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player != null) {
            player.setBankrupt(true);
        }
        
        // If it's the current player's turn, move to next
        if (playerId == getCurrentPlayerId()) {
            nextPlayer();
        }
    }
    
    /**
     * Handles rolling doubles
     * @return true if player gets another turn
     */
    public boolean handleDoubles() {
        Dice dice = gameState.getDice();
        Player currentPlayer = getCurrentPlayer();
        
        if (dice.isDoubles()) {
            // Check for speeding (third consecutive doubles)
            if (dice.shouldGoToJail()) {
                jailManager.sendToJail(currentPlayer);
                gameState.setCanRollAgain(false);
                return false;
            }
            
            // If in jail, doubles releases player
            if (currentPlayer.isInJail()) {
                jailManager.attemptReleaseByDoubles(currentPlayer, true);
                gameState.setCanRollAgain(false);
            } else {
                // Normal doubles - player gets another turn
                gameState.setCanRollAgain(true);
            }
            return true;
        }
        
        gameState.setCanRollAgain(false);
        return false;
    }
    
    /**
     * Rolls the dice
     * @return The total rolled
     */
    public int rollDice() {
        Dice dice = gameState.getDice();
        int result = dice.roll();
        
        setPhase(TurnPhase.ROLLING);
        
        return result;
    }
    
    /**
     * Checks if player can roll again (after doubles)
     * @return true if can roll again
     */
    public boolean canRollAgain() {
        return gameState.canRollAgain();
    }
    
    /**
     * Resets the doubles count
     */
    public void resetDoublesCount() {
        gameState.getDice().resetDoubles();
    }
    
    /**
     * Checks if an action is allowed in the current phase
     * @param action The action to check
     * @return true if action is allowed
     */
    public boolean isActionAllowed(String action) {
        TurnPhase phase = getCurrentPhase();
        ArrayList<String> allowed = allowedActions.get(phase);
        
        if (allowed == null) {
            return false;
        }
        
        for (int i = 0; i < allowed.size(); i++) {
            if (allowed.get(i).equals(action)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets allowed actions for the current phase
     * @return ArrayList of allowed action strings
     */
    public ArrayList<String> getAllowedActions() {
        ArrayList<String> allowed = allowedActions.get(getCurrentPhase());
        return allowed != null ? allowed : new ArrayList<>();
    }
    
    /**
     * Gets the remaining active players
     * @return ArrayList of active player IDs
     */
    public ArrayList<Integer> getRemainingPlayers() {
        ArrayList<Integer> remaining = new ArrayList<>();
        ArrayList<Integer> playerOrder = gameState.getPlayerOrder();
        
        for (int i = 0; i < playerOrder.size(); i++) {
            int playerId = playerOrder.get(i);
            Player player = gameState.getPlayer(playerId);
            if (player != null && !player.isBankrupt()) {
                remaining.add(playerId);
            }
        }
        
        return remaining;
    }
    
    /**
     * Determines the winner when game ends
     */
    private void determineWinner() {
        ArrayList<Integer> remaining = getRemainingPlayers();
        
        if (remaining.size() == 1) {
            gameState.endGame(remaining.get(0));
        } else if (remaining.size() > 1) {
            // Multiple players left - highest net worth wins
            int winnerId = -1;
            int highestNetWorth = -1;
            
            for (int i = 0; i < remaining.size(); i++) {
                int playerId = remaining.get(i);
                Player player = gameState.getPlayer(playerId);
                if (player != null) {
                    int netWorth = player.getNetWorth();
                    if (netWorth > highestNetWorth) {
                        highestNetWorth = netWorth;
                        winnerId = playerId;
                    }
                }
            }
            
            gameState.endGame(winnerId);
        }
    }
    
    /**
     * Checks if it's a specific player's turn
     * @param playerId The player to check
     * @return true if it's their turn
     */
    public boolean isPlayersTurn(int playerId) {
        return playerId == getCurrentPlayerId();
    }
    
    /**
     * Gets the dice object
     * @return The dice
     */
    public Dice getDice() {
        return gameState.getDice();
    }
    
    /**
     * Gets the jail manager
     * @return The jail manager
     */
    public JailManager getJailManager() {
        return jailManager;
    }
}
