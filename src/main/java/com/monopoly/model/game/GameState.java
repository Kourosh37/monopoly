package com.monopoly.model.game;

import com.monopoly.datastructures.ArrayList;
import com.monopoly.datastructures.HashTable;
import com.monopoly.datastructures.Queue;
import com.monopoly.datastructures.BST;
import com.monopoly.datastructures.Graph;
import com.monopoly.model.card.Card;
import com.monopoly.model.card.ChanceCard;
import com.monopoly.model.card.CommunityChestCard;
import com.monopoly.model.enums.GameStatus;
import com.monopoly.model.enums.TurnPhase;
import com.monopoly.model.player.Player;
import com.monopoly.model.property.Property;

/**
 * Represents the complete state of the game.
 * This is the Single Source of Truth maintained by the server.
 * Contains all game data including players, board, cards, and current turn info.
 */
public class GameState {

    // Constants
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 4;
    
    // Core game components
    private final Board board;
    private final Bank bank;
    private final Dice dice;
    
    // Players
    private final HashTable<Integer, Player> players; // playerId -> Player
    private final ArrayList<Integer> playerOrder; // Order of play
    private int currentPlayerIndex;
    
    // Turn state
    private TurnPhase turnPhase;
    private int turnNumber;
    private boolean canRollAgain; // For doubles
    
    // Card decks
    private final Queue<Card> chanceCards;
    private final Queue<Card> communityChestCards;
    
    // Game status
    private GameStatus gameStatus;
    private int winnerId;
    private String roomId;
    
    // Financial tracking (Graph: nodes are playerIds, edges are transactions)
    private final Graph<Integer> financialGraph;
    
    // Player rankings (BST by net worth)
    private final BST<PlayerRanking> playerRankings;
    
    // Free parking jackpot (optional rule)
    private int freeParkingJackpot;
    private boolean useFreeParkingJackpot;
    
    // Trade related
    private Trade activeTrade;
    
    // Auction related
    private Auction activeAuction;
    
    /**
     * Creates a new GameState with default settings
     */
    public GameState() {
        this.board = new Board();
        this.bank = new Bank();
        this.dice = new Dice();
        this.players = new HashTable<>();
        this.playerOrder = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.turnPhase = TurnPhase.WAITING_FOR_PLAYERS;
        this.turnNumber = 0;
        this.canRollAgain = false;
        this.chanceCards = new Queue<>();
        this.communityChestCards = new Queue<>();
        this.gameStatus = GameStatus.WAITING;
        this.winnerId = -1;
        this.financialGraph = new Graph<>();
        this.playerRankings = new BST<>();
        this.freeParkingJackpot = 0;
        this.useFreeParkingJackpot = false;
        this.activeTrade = null;
        this.activeAuction = null;
        
        initializeCardDecks();
        initializeBankProperties();
    }
    
    /**
     * Creates a GameState for a specific room
     * @param roomId The room identifier
     */
    public GameState(String roomId) {
        this();
        this.roomId = roomId;
    }
    
    /**
     * Initializes the card decks with shuffled standard cards
     */
    private void initializeCardDecks() {
        // Initialize Chance cards
        ArrayList<ChanceCard> chances = ChanceCard.createStandardDeck();
        shuffleAndEnqueueChance(chances, chanceCards);
        
        // Initialize Community Chest cards
        ArrayList<CommunityChestCard> communities = CommunityChestCard.createStandardDeck();
        shuffleAndEnqueueCommunity(communities, communityChestCards);
    }
    
    /**
     * Shuffles an ArrayList of ChanceCards and adds to a Queue
     */
    private void shuffleAndEnqueueChance(ArrayList<ChanceCard> cards, Queue<Card> queue) {
        // Fisher-Yates shuffle
        java.util.Random random = new java.util.Random();
        for (int i = cards.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            ChanceCard temp = cards.get(i);
            cards.set(i, cards.get(j));
            cards.set(j, temp);
        }
        
        // Enqueue all cards
        for (int i = 0; i < cards.size(); i++) {
            queue.enqueue(cards.get(i));
        }
    }
    
    /**
     * Shuffles an ArrayList of CommunityChestCards and adds to a Queue
     */
    private void shuffleAndEnqueueCommunity(ArrayList<CommunityChestCard> cards, Queue<Card> queue) {
        // Fisher-Yates shuffle
        java.util.Random random = new java.util.Random();
        for (int i = cards.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            CommunityChestCard temp = cards.get(i);
            cards.set(i, cards.get(j));
            cards.set(j, temp);
        }
        
        // Enqueue all cards
        for (int i = 0; i < cards.size(); i++) {
            queue.enqueue(cards.get(i));
        }
    }
    
    /**
     * Shuffles an ArrayList and adds to a Queue
     */
    private void shuffleAndEnqueue(ArrayList<Card> cards, Queue<Card> queue) {
        // Fisher-Yates shuffle
        java.util.Random random = new java.util.Random();
        for (int i = cards.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Card temp = cards.get(i);
            cards.set(i, cards.get(j));
            cards.set(j, temp);
        }
        
        // Enqueue all cards
        for (int i = 0; i < cards.size(); i++) {
            queue.enqueue(cards.get(i));
        }
    }
    
    /**
     * Initializes bank with all properties as unowned
     */
    private void initializeBankProperties() {
        HashTable<Integer, Property> allProperties = board.getAllProperties();
        // Add all properties to bank as unowned
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            Property property = allProperties.get(i);
            if (property != null) {
                bank.addUnownedProperty(property);
            }
        }
    }
    
    // ==================== Player Management ====================
    
    /**
     * Adds a player to the game
     * @param player The player to add
     * @return true if player was added successfully
     */
    public boolean addPlayer(Player player) {
        if (players.size() >= MAX_PLAYERS) {
            return false;
        }
        if (gameStatus != GameStatus.WAITING) {
            return false;
        }
        players.put(player.getId(), player);
        playerOrder.add(player.getId());
        financialGraph.addVertex(player.getId());
        updatePlayerRanking(player);
        return true;
    }
    
    /**
     * Removes a player from the game
     * @param playerId The player ID to remove
     * @return The removed player, or null
     */
    public Player removePlayer(int playerId) {
        if (gameStatus == GameStatus.IN_PROGRESS) {
            // Mark as bankrupt instead of removing
            Player player = players.get(playerId);
            if (player != null) {
                player.setBankrupt(true);
            }
            return player;
        }
        
        // Remove from order
        for (int i = 0; i < playerOrder.size(); i++) {
            if (playerOrder.get(i) == playerId) {
                playerOrder.remove(i);
                break;
            }
        }
        
        return players.remove(playerId);
    }
    
    /**
     * Gets a player by ID
     * @param playerId The player ID
     * @return The player, or null
     */
    public Player getPlayer(int playerId) {
        return players.get(playerId);
    }
    
    /**
     * Gets all players
     * @return HashTable of players
     */
    public HashTable<Integer, Player> getPlayers() {
        return players;
    }
    
    /**
     * Gets the player count
     * @return Number of players
     */
    public int getPlayerCount() {
        return players.size();
    }
    
    /**
     * Gets active (non-bankrupt) player count
     * @return Number of active players
     */
    public int getActivePlayerCount() {
        int count = 0;
        for (int i = 0; i < playerOrder.size(); i++) {
            Player player = players.get(playerOrder.get(i));
            if (player != null && !player.isBankrupt()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Checks if game can start
     * @return true if enough players and waiting
     */
    public boolean canStart() {
        return gameStatus == GameStatus.WAITING && players.size() >= MIN_PLAYERS;
    }
    
    // ==================== Current Player Management ====================
    
    /**
     * Gets the current player
     * @return The current player
     */
    public Player getCurrentPlayer() {
        if (playerOrder.isEmpty()) {
            return null;
        }
        int playerId = playerOrder.get(currentPlayerIndex);
        return players.get(playerId);
    }
    
    /**
     * Gets the current player ID
     * @return Current player's ID
     */
    public int getCurrentPlayerId() {
        if (playerOrder.isEmpty()) {
            return -1;
        }
        return playerOrder.get(currentPlayerIndex);
    }
    
    /**
     * Sets the current player by ID
     * @param playerId The player ID to set as current
     */
    public void setCurrentPlayerId(int playerId) {
        for (int i = 0; i < playerOrder.size(); i++) {
            if (playerOrder.get(i) == playerId) {
                currentPlayerIndex = i;
                return;
            }
        }
    }
    
    /**
     * Sets whether the game has started
     * @param started true if game is started
     */
    public void setGameStarted(boolean started) {
        if (started) {
            this.gameStatus = GameStatus.IN_PROGRESS;
        }
    }
    
    /**
     * Checks if the game has started
     * @return true if game is in progress or finished
     */
    public boolean isGameStarted() {
        return gameStatus == GameStatus.IN_PROGRESS || gameStatus == GameStatus.FINISHED;
    }

    /**
     * Advances to the next player
     */
    public void nextPlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % playerOrder.size();
            Player current = getCurrentPlayer();
            if (current != null && !current.isBankrupt()) {
                break;
            }
        } while (getActivePlayerCount() > 1);
        
        turnNumber++;
        turnPhase = TurnPhase.TURN_START;
        canRollAgain = false;
        dice.resetCompletely();
    }
    
    /**
     * Gets the player order
     * @return ArrayList of player IDs in order
     */
    public ArrayList<Integer> getPlayerOrder() {
        return playerOrder;
    }
    
    // ==================== Turn Management ====================
    
    /**
     * Gets the current turn phase
     * @return Current TurnPhase
     */
    public TurnPhase getTurnPhase() {
        return turnPhase;
    }
    
    /**
     * Sets the current turn phase
     * @param phase New phase
     */
    public void setTurnPhase(TurnPhase phase) {
        this.turnPhase = phase;
    }
    
    /**
     * Gets the turn number
     * @return Current turn number
     */
    public int getTurnNumber() {
        return turnNumber;
    }
    
    /**
     * Increments the turn number
     */
    public void incrementTurnNumber() {
        turnNumber++;
    }
    
    /**
     * Gets all players as an ArrayList
     * @return ArrayList of all players
     */
    public ArrayList<Player> getAllPlayers() {
        ArrayList<Player> list = new ArrayList<>();
        for (Integer playerId : players.keySet()) {
            Player p = players.get(playerId);
            if (p != null) {
                list.add(p);
            }
        }
        return list;
    }
    
    /**
     * Sets game over flag
     * @param gameOver true if game is over
     */
    public void setGameOver(boolean gameOver) {
        if (gameOver) {
            this.gameStatus = GameStatus.FINISHED;
        }
    }
    
    /**
     * Sets the winner
     * @param winner The winning player
     */
    public void setWinner(Player winner) {
        if (winner != null) {
            this.winnerId = winner.getId();
        }
    }
    
    /**
     * Checks if player can roll again (doubles)
     * @return true if can roll again
     */
    public boolean canRollAgain() {
        return canRollAgain;
    }
    
    /**
     * Sets whether player can roll again
     * @param canRoll Can roll again
     */
    public void setCanRollAgain(boolean canRoll) {
        this.canRollAgain = canRoll;
    }
    
    // ==================== Game Status ====================
    
    /**
     * Gets the game status
     * @return Current GameStatus
     */
    public GameStatus getGameStatus() {
        return gameStatus;
    }
    
    /**
     * Sets the game status
     * @param status New status
     */
    public void setGameStatus(GameStatus status) {
        this.gameStatus = status;
    }
    
    /**
     * Starts the game
     * @return true if game started successfully
     */
    public boolean startGame() {
        if (!canStart()) {
            return false;
        }
        gameStatus = GameStatus.IN_PROGRESS;
        turnPhase = TurnPhase.TURN_START;
        turnNumber = 1;
        return true;
    }
    
    /**
     * Ends the game with a winner
     * @param winnerId The winning player's ID
     */
    public void endGame(int winnerId) {
        this.winnerId = winnerId;
        this.gameStatus = GameStatus.FINISHED;
        this.turnPhase = TurnPhase.GAME_OVER;
    }
    
    /**
     * Gets the winner ID
     * @return Winner's ID, or -1 if no winner
     */
    public int getWinnerId() {
        return winnerId;
    }
    
    /**
     * Gets the winning player
     * @return Winner Player object, or null if no winner
     */
    public Player getWinner() {
        if (winnerId < 0) {
            return null;
        }
        return getPlayer(winnerId);
    }
    
    /**
     * Checks if game is over
     * @return true if game has ended
     */
    public boolean isGameOver() {
        return gameStatus == GameStatus.FINISHED || getActivePlayerCount() <= 1;
    }
    
    // ==================== Component Accessors ====================
    
    public Board getBoard() {
        return board;
    }
    
    public Bank getBank() {
        return bank;
    }
    
    public Dice getDice() {
        return dice;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    // ==================== Card Management ====================
    
    /**
     * Draws a Chance card
     * @return The drawn card
     */
    public Card drawChanceCard() {
        Card card = chanceCards.dequeue();
        // Put back at bottom unless it's a keep card
        if (!card.getEffect().isKeepable()) {
            chanceCards.enqueue(card);
        }
        return card;
    }
    
    /**
     * Draws a Community Chest card
     * @return The drawn card
     */
    public Card drawCommunityChestCard() {
        Card card = communityChestCards.dequeue();
        // Put back at bottom unless it's a keep card
        if (!card.getEffect().isKeepable()) {
            communityChestCards.enqueue(card);
        }
        return card;
    }
    
    /**
     * Returns a card to its deck
     * @param card The card to return
     */
    public void returnCard(Card card) {
        if (card instanceof ChanceCard) {
            chanceCards.enqueue(card);
        } else if (card instanceof CommunityChestCard) {
            communityChestCards.enqueue(card);
        }
    }
    
    // ==================== Financial Tracking ====================
    
    /**
     * Records a financial transaction between players
     * @param fromPlayerId Paying player
     * @param toPlayerId Receiving player
     * @param amount Transaction amount
     */
    public void recordTransaction(int fromPlayerId, int toPlayerId, int amount) {
        financialGraph.addEdge(fromPlayerId, toPlayerId, amount);
    }
    
    /**
     * Gets the financial graph
     * @return The transaction graph
     */
    public Graph<Integer> getFinancialGraph() {
        return financialGraph;
    }
    
    // ==================== Player Rankings ====================
    
    /**
     * Updates a player's ranking in the BST
     * @param player The player to update
     */
    public void updatePlayerRanking(Player player) {
        // Remove old ranking if exists
        PlayerRanking oldRanking = new PlayerRanking(player.getId(), 0);
        playerRankings.delete(oldRanking);
        
        // Add new ranking
        PlayerRanking newRanking = new PlayerRanking(player.getId(), player.getNetWorth());
        playerRankings.insert(newRanking);
    }
    
    /**
     * Gets player rankings
     * @return BST of player rankings
     */
    public BST<PlayerRanking> getPlayerRankings() {
        return playerRankings;
    }
    
    // ==================== Free Parking Jackpot ====================
    
    /**
     * Adds money to the free parking jackpot
     * @param amount Amount to add
     */
    public void addToFreeParkingJackpot(int amount) {
        if (useFreeParkingJackpot) {
            freeParkingJackpot += amount;
        }
    }
    
    /**
     * Collects the free parking jackpot
     * @return The jackpot amount (resets to 0)
     */
    public int collectFreeParkingJackpot() {
        int amount = freeParkingJackpot;
        freeParkingJackpot = 0;
        return amount;
    }
    
    /**
     * Gets the current free parking jackpot
     * @return Jackpot amount
     */
    public int getFreeParkingJackpot() {
        return freeParkingJackpot;
    }
    
    /**
     * Sets whether to use free parking jackpot rule
     * @param use Whether to use the rule
     */
    public void setUseFreeParkingJackpot(boolean use) {
        this.useFreeParkingJackpot = use;
    }
    
    // ==================== Trade Management ====================
    
    public Trade getActiveTrade() {
        return activeTrade;
    }
    
    public void setActiveTrade(Trade trade) {
        this.activeTrade = trade;
    }
    
    public boolean hasActiveTrade() {
        return activeTrade != null;
    }
    
    public void clearActiveTrade() {
        this.activeTrade = null;
    }
    
    // ==================== Auction Management ====================
    
    public Auction getActiveAuction() {
        return activeAuction;
    }
    
    public void setActiveAuction(Auction auction) {
        this.activeAuction = auction;
    }
    
    public boolean hasActiveAuction() {
        return activeAuction != null;
    }
    
    public void clearActiveAuction() {
        this.activeAuction = null;
    }
    
    // ==================== Serialization ====================
    
    /**
     * Creates a snapshot of the game state for undo/save
     * @return A copy of this GameState
     */
    public GameStateSnapshot createSnapshot() {
        return new GameStateSnapshot(this);
    }
    
    @Override
    public String toString() {
        return "GameState{" +
                "roomId='" + roomId + '\'' +
                ", status=" + gameStatus +
                ", players=" + players.size() +
                ", currentPlayer=" + getCurrentPlayerId() +
                ", turn=" + turnNumber +
                ", phase=" + turnPhase +
                '}';
    }
    
    // ==================== Inner Classes ====================
    
    /**
     * Represents a player's ranking for BST storage
     */
    public static class PlayerRanking implements Comparable<PlayerRanking> {
        private final int playerId;
        private final int netWorth;
        
        public PlayerRanking(int playerId, int netWorth) {
            this.playerId = playerId;
            this.netWorth = netWorth;
        }
        
        public int getPlayerId() {
            return playerId;
        }
        
        public int getNetWorth() {
            return netWorth;
        }
        
        @Override
        public int compareTo(PlayerRanking other) {
            // Higher net worth = higher ranking
            int result = Integer.compare(other.netWorth, this.netWorth);
            if (result == 0) {
                return Integer.compare(this.playerId, other.playerId);
            }
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PlayerRanking that = (PlayerRanking) obj;
            return playerId == that.playerId;
        }
        
        @Override
        public int hashCode() {
            return playerId;
        }
    }
    
    /**
     * Snapshot of game state for undo functionality
     */
    public static class GameStateSnapshot {
        private final int turnNumber;
        private final int currentPlayerIndex;
        private final TurnPhase turnPhase;
        private final GameStatus gameStatus;
        // Add more fields as needed for complete snapshot
        
        public GameStateSnapshot(GameState state) {
            this.turnNumber = state.turnNumber;
            this.currentPlayerIndex = state.currentPlayerIndex;
            this.turnPhase = state.turnPhase;
            this.gameStatus = state.gameStatus;
        }
        
        public int getTurnNumber() {
            return turnNumber;
        }
        
        public int getCurrentPlayerIndex() {
            return currentPlayerIndex;
        }
        
        public TurnPhase getTurnPhase() {
            return turnPhase;
        }
        
        public GameStatus getGameStatus() {
            return gameStatus;
        }
    }
}
