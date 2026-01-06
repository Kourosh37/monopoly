package com.monopoly.server;

import com.monopoly.model.player.Player;
import com.monopoly.model.player.PlayerToken;
import com.monopoly.model.property.Property;
import com.monopoly.model.enums.TurnPhase;
import com.monopoly.datastructures.ArrayList;
import com.monopoly.datastructures.HashTable;
import com.monopoly.model.game.Auction;
import com.monopoly.model.game.GameState;
import com.monopoly.model.game.Trade;
import com.monopoly.logic.AuctionManager;
import com.monopoly.logic.BankruptcyManager;
import com.monopoly.logic.ConstructionManager;
import com.monopoly.logic.GameLogic;
import com.monopoly.logic.JailManager;
import com.monopoly.logic.RentCalculator;
import com.monopoly.logic.TradeManager;
import com.monopoly.logic.TurnManager;
import com.monopoly.network.protocol.ClientCommand;
import com.monopoly.network.protocol.Message;
import com.monopoly.network.protocol.MessageType;
import com.monopoly.network.protocol.ProtocolHandler;
import com.monopoly.network.protocol.ServerEvent;
import com.monopoly.network.serialization.Serializer;

/**
 * Main game controller on the server side.
 * Coordinates all game logic, enforces rules, and manages game state.
 * This is where all game decisions are made (clients only display).
 */
public class GameController {

    // Room identification
    private final String roomId;
    
    // Server reference
    private final Server server;
    
    // Game state
    private GameState gameState;
    
    // Logic managers
    private GameLogic gameLogic;
    private TurnManager turnManager;
    private AuctionManager auctionManager;
    private TradeManager tradeManager;
    private JailManager jailManager;
    private ConstructionManager constructionManager;
    private BankruptcyManager bankruptcyManager;
    private RentCalculator rentCalculator;
    
    // Protocol and serialization
    private final ProtocolHandler protocolHandler;
    private final Serializer serializer;
    
    // Player tracking
    private final HashTable<Integer, ClientHandler> playerHandlers;
    private final HashTable<Integer, String> playerNames;
    private final ArrayList<Integer> playerOrder;
    
    // Available tokens
    private final ArrayList<PlayerToken> availableTokens;
    
    // Game state flags
    private boolean gameStarted;
    private boolean gameEnded;
    
    /**
     * Creates a new game controller for a room
     * @param roomId Room ID
     * @param server Server reference
     */
    public GameController(String roomId, Server server) {
        this.roomId = roomId;
        this.server = server;
        this.protocolHandler = server.getProtocolHandler();
        this.serializer = new Serializer();
        
        this.playerHandlers = new HashTable<>();
        this.playerNames = new HashTable<>();
        this.playerOrder = new ArrayList<>();
        
        this.availableTokens = new ArrayList<>();
        for (PlayerToken token : PlayerToken.values()) {
            availableTokens.add(token);
        }
        
        this.gameStarted = false;
        this.gameEnded = false;
    }
    
    /**
     * Initializes the game with all players
     */
    private void initializeGame() {
        // Create game state
        gameState = new GameState();
        
        // Create players from connected clients
        for (int i = 0; i < playerOrder.size(); i++) {
            int playerId = playerOrder.get(i);
            String name = playerNames.get(playerId);
            if (name == null) name = "Player " + playerId;
            
            PlayerToken token = availableTokens.get(i % availableTokens.size());
            Player player = new Player(playerId, name, token);
            gameState.addPlayer(player);
        }
        
        // Initialize game logic
        gameLogic = new GameLogic(gameState);
        
        // Get managers from game logic
        turnManager = new TurnManager(gameState);
        auctionManager = new AuctionManager(gameState);
        tradeManager = new TradeManager(gameState);
        jailManager = new JailManager(gameState);
        constructionManager = new ConstructionManager(gameState);
        bankruptcyManager = new BankruptcyManager(gameState);
        rentCalculator = new RentCalculator(gameState);
        
        // Set first player
        if (playerOrder.size() > 0) {
            gameState.setCurrentPlayerId(playerOrder.get(0));
        }
        
        System.out.println("Game initialized for room " + roomId + 
                          " with " + playerOrder.size() + " players");
    }
    
    /**
     * Starts the game
     */
    public void startGame() {
        if (gameStarted) {
            return;
        }
        
        initializeGame();
        gameStarted = true;
        gameState.setGameStarted(true);
        
        // Broadcast game start
        ServerEvent startEvent = ServerEvent.createGameStart();
        broadcastToAll(startEvent);
        
        // Send initial state
        broadcastStateUpdate();
        
        // Start first turn
        int currentPlayerId = gameState.getCurrentPlayerId();
        ServerEvent turnEvent = ServerEvent.createTurnStart(currentPlayerId);
        broadcastToAll(turnEvent);
        
        System.out.println("Game started in room " + roomId);
    }
    
    /**
     * Processes a command from a player
     * @param playerId Player ID
     * @param command Client command
     */
    public void processCommand(int playerId, ClientCommand command) {
        if (!gameStarted || gameEnded) {
            sendError(playerId, "Game is not active");
            return;
        }
        
        MessageType type = command.getCommandType();
        
        // Validate it's the player's turn for turn-based actions
        if (requiresTurn(type) && playerId != gameState.getCurrentPlayerId()) {
            sendError(playerId, "Not your turn");
            return;
        }
        
        // Validate action is allowed in current phase
        TurnPhase phase = gameState.getTurnPhase();
        if (!protocolHandler.isValidCommand(command, phase)) {
            sendError(playerId, "Action not allowed in phase: " + phase);
            return;
        }
        
        // Handle command
        try {
            switch (type) {
                case ROLL_DICE:
                    handleRollDice(playerId);
                    break;
                case BUY_PROPERTY:
                    handleBuyProperty(playerId);
                    break;
                case DECLINE_BUY:
                    handleDeclineBuy(playerId);
                    break;
                case BID:
                    handleBid(playerId, command.getIntParameter("amount", 0));
                    break;
                case PASS_BID:
                    handlePassBid(playerId);
                    break;
                case BUILD:
                    handleBuild(playerId, 
                               command.getIntParameter("propertyId", -1),
                               command.getStringParameter("buildingType"));
                    break;
                case SELL_BUILDING:
                    handleSellBuilding(playerId,
                                      command.getIntParameter("propertyId", -1));
                    break;
                case MORTGAGE:
                    handleMortgage(playerId,
                                  command.getIntParameter("propertyId", -1));
                    break;
                case UNMORTGAGE:
                    handleUnmortgage(playerId,
                                    command.getIntParameter("propertyId", -1));
                    break;
                case PROPOSE_TRADE:
                    handleProposeTrade(playerId, command);
                    break;
                case ACCEPT_TRADE:
                    handleAcceptTrade(playerId);
                    break;
                case DECLINE_TRADE:
                    handleDeclineTrade(playerId);
                    break;
                case JAIL_PAY_FINE:
                    handleJailPayFine(playerId);
                    break;
                case JAIL_USE_CARD:
                    handleJailUseCard(playerId);
                    break;
                case END_TURN:
                    handleEndTurn(playerId);
                    break;
                default:
                    sendError(playerId, "Unknown command");
                    break;
            }
        } catch (Exception e) {
            sendError(playerId, "Error processing command: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Checks if command requires it to be player's turn
     */
    private boolean requiresTurn(MessageType type) {
        switch (type) {
            case BID:
            case PASS_BID:
            case ACCEPT_TRADE:
            case DECLINE_TRADE:
                return false; // These can be done when it's not your turn
            default:
                return true;
        }
    }
    
    /**
     * Handles roll dice command
     */
    private void handleRollDice(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) return;
        
        // Roll dice using game logic
        GameLogic.RollResult rollResult = gameLogic.rollAndMove();
        
        int die1 = rollResult.getDie1();
        int die2 = rollResult.getDie2();
        
        // Broadcast dice result
        ServerEvent diceEvent = ServerEvent.createDiceResult(die1, die2);
        broadcastToAll(diceEvent);
        
        // Process the roll through game logic
        // The game logic will handle movement, landing, rent, cards, etc.
        
        // Check for jail
        if (player.isInJail()) {
            boolean released = jailManager.attemptReleaseByDoubles(player, rollResult.isDoubles());
            if (released) {
                broadcastEventLog(player.getName() + " rolled doubles and is released from jail!");
            } else {
                player.incrementTurnsInJail();
                broadcastEventLog(player.getName() + " stays in jail.");
                gameState.setTurnPhase(TurnPhase.POST_ROLL);
            }
        } else {
            // Normal movement handled by rollAndMove
            String result = gameLogic.handleTileLanding(playerId);
            if (result != null && !result.isEmpty()) {
                broadcastEventLog(result);
            }
        }
        
        // Check for doubles (three doubles = jail)
        if (rollResult.isDoubles() && !player.isInJail()) {
            if (rollResult.getConsecutiveDoubles() >= 3) {
                jailManager.sendToJail(player);
                broadcastEventLog(player.getName() + " rolled three doubles and goes to jail!");
            } else {
                broadcastEventLog(player.getName() + " rolled doubles and gets another turn!");
            }
        }
        
        broadcastStateUpdate();
        checkGameEnd();
    }
    
    /**
     * Handles buy property command
     */
    private void handleBuyProperty(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) return;
        
        Property property = gameLogic.getPropertyAtPosition(player.getPosition());
        if (property == null) {
            sendError(playerId, "No property to buy here");
            return;
        }
        
        if (!gameLogic.canBuyProperty(playerId, property.getId())) {
            sendError(playerId, "Cannot buy this property");
            return;
        }
        
        gameLogic.buyProperty(playerId, property.getId());
        broadcastEventLog(player.getName() + " bought " + property.getName() + 
                         " for $" + property.getPrice());
        
        gameState.setTurnPhase(TurnPhase.POST_ROLL);
        broadcastStateUpdate();
    }
    
    /**
     * Handles decline buy command (starts auction)
     */
    private void handleDeclineBuy(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) return;
        
        Property property = gameLogic.getPropertyAtPosition(player.getPosition());
        if (property == null) {
            sendError(playerId, "No property here");
            return;
        }
        
        // Start auction
        auctionManager.startAuction(property.getId());
        gameState.setTurnPhase(TurnPhase.AUCTION);
        
        ServerEvent auctionEvent = ServerEvent.createAuctionStart(
            property.getId(), property.getName());
        broadcastToAll(auctionEvent);
        
        broadcastEventLog(player.getName() + " declined to buy " + property.getName() + 
                         ". Auction started!");
        broadcastStateUpdate();
    }
    
    /**
     * Handles bid command
     */
    private void handleBid(int playerId, int amount) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) return;
        
        Auction auction = gameState.getActiveAuction();
        if (auction == null || !auction.isActive()) {
            sendError(playerId, "No active auction");
            return;
        }
        
        boolean success = auctionManager.placeBid(playerId, amount);
        if (!success) {
            sendError(playerId, "Invalid bid");
            return;
        }
        
        ServerEvent updateEvent = ServerEvent.createAuctionUpdate(
            auction.getCurrentBid(), playerId);
        broadcastToAll(updateEvent);
        
        broadcastEventLog(player.getName() + " bids $" + amount);
        broadcastStateUpdate();
    }
    
    /**
     * Handles pass bid command
     */
    private void handlePassBid(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) return;
        
        Auction auction = gameState.getActiveAuction();
        if (auction == null || !auction.isActive()) {
            sendError(playerId, "No active auction");
            return;
        }
        
        auctionManager.passBid(playerId);
        broadcastEventLog(player.getName() + " passed on the auction");
        
        // Check if auction ended
        if (!auction.isActive() || auctionManager.shouldEndAuction()) {
            endAuction();
        }
        
        broadcastStateUpdate();
    }
    
    /**
     * Ends the current auction
     */
    private void endAuction() {
        Auction auction = gameState.getActiveAuction();
        if (auction == null) return;
        
        int winnerId = auctionManager.endAuction();
        
        if (winnerId >= 0) {
            Player winner = gameState.getPlayer(winnerId);
            ServerEvent endEvent = ServerEvent.createAuctionEnd(
                winnerId, auction.getCurrentBid());
            broadcastToAll(endEvent);
            
            String winnerName = winner != null ? winner.getName() : "Player " + winnerId;
            broadcastEventLog(winnerName + " wins the auction for $" + 
                             auction.getCurrentBid());
        } else {
            broadcastEventLog("Auction ended with no winner");
        }
        
        gameState.setTurnPhase(TurnPhase.POST_ROLL);
    }
    
    /**
     * Handles build command
     */
    private void handleBuild(int playerId, int propertyId, String buildingType) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) return;
        
        Property property = gameState.getBoard().getProperty(propertyId);
        if (property == null) {
            sendError(playerId, "Property not found");
            return;
        }
        
        boolean isHotel = "hotel".equalsIgnoreCase(buildingType);
        boolean success;
        
        if (isHotel) {
            success = constructionManager.buildHotel(playerId, propertyId);
        } else {
            success = constructionManager.buildHouse(playerId, propertyId);
        }
        
        if (!success) {
            sendError(playerId, "Cannot build here");
            return;
        }
        
        String building = isHotel ? "hotel" : "house";
        broadcastEventLog(player.getName() + " built a " + building + " on " + property.getName());
        broadcastStateUpdate();
    }
    
    /**
     * Handles sell building command
     */
    private void handleSellBuilding(int playerId, int propertyId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) return;
        
        Property property = gameState.getBoard().getProperty(propertyId);
        if (property == null) {
            sendError(playerId, "Property not found");
            return;
        }
        
        boolean success = constructionManager.sellHouse(playerId, propertyId);
        if (!success) {
            sendError(playerId, "Cannot sell building");
            return;
        }
        
        broadcastEventLog(player.getName() + " sold a building on " + property.getName());
        broadcastStateUpdate();
    }
    
    /**
     * Handles mortgage command
     */
    private void handleMortgage(int playerId, int propertyId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) return;
        
        Property property = gameState.getBoard().getProperty(propertyId);
        if (property == null) {
            sendError(playerId, "Property not found");
            return;
        }
        
        if (property.getOwnerId() != playerId) {
            sendError(playerId, "You don't own this property");
            return;
        }
        
        if (property.isMortgaged()) {
            sendError(playerId, "Property is already mortgaged");
            return;
        }
        
        if (property.getNumberOfHouses() > 0 || property.hasHotel()) {
            sendError(playerId, "Must sell all buildings first");
            return;
        }
        
        property.mortgage();
        player.addMoney(property.getMortgageValue());
        
        broadcastEventLog(player.getName() + " mortgaged " + property.getName() + 
                         " for $" + property.getMortgageValue());
        broadcastStateUpdate();
    }
    
    /**
     * Handles unmortgage command
     */
    private void handleUnmortgage(int playerId, int propertyId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) return;
        
        Property property = gameState.getBoard().getProperty(propertyId);
        if (property == null) {
            sendError(playerId, "Property not found");
            return;
        }
        
        if (property.getOwnerId() != playerId) {
            sendError(playerId, "You don't own this property");
            return;
        }
        
        if (!property.isMortgaged()) {
            sendError(playerId, "Property is not mortgaged");
            return;
        }
        
        int cost = (int) (property.getMortgageValue() * 1.1);
        if (player.getMoney() < cost) {
            sendError(playerId, "Not enough money");
            return;
        }
        
        player.subtractMoney(cost);
        property.unmortgage();
        
        broadcastEventLog(player.getName() + " unmortgaged " + property.getName() + 
                         " for $" + cost);
        broadcastStateUpdate();
    }
    
    /**
     * Handles propose trade command
     */
    private void handleProposeTrade(int playerId, ClientCommand command) {
        Player initiator = gameState.getPlayer(playerId);
        int receiverId = command.getIntParameter("receiverId", -1);
        Player receiver = gameState.getPlayer(receiverId);
        
        if (initiator == null || receiver == null) {
            sendError(playerId, "Invalid trade participants");
            return;
        }
        
        // Set trade offers from command parameters
        int initiatorMoney = command.getIntParameter("offerMoney", 0);
        int receiverMoney = command.getIntParameter("requestMoney", 0);
        
        // Validate and set as active
        Trade trade = tradeManager.proposeTrade(playerId, receiverId);
        if (trade == null) {
            sendError(playerId, "Invalid trade proposal");
            return;
        }
        
        trade.setInitiatorMoney(initiatorMoney);
        trade.setReceiverMoney(receiverMoney);
        
        gameState.setTurnPhase(TurnPhase.TRADING);
        
        broadcastEventLog(initiator.getName() + " proposed a trade to " + receiver.getName());
        broadcastStateUpdate();
    }
    
    /**
     * Handles accept trade command
     */
    private void handleAcceptTrade(int playerId) {
        Trade trade = gameState.getActiveTrade();
        if (trade == null || !trade.isPending()) {
            sendError(playerId, "No active trade");
            return;
        }
        
        if (trade.getReceiverId() != playerId) {
            sendError(playerId, "You are not the trade receiver");
            return;
        }
        
        tradeManager.acceptTrade(playerId);
        
        Player receiver = gameState.getPlayer(playerId);
        String receiverName = receiver != null ? receiver.getName() : "Player " + playerId;
        broadcastEventLog(receiverName + " accepted the trade!");
        gameState.setTurnPhase(TurnPhase.POST_ROLL);
        broadcastStateUpdate();
    }
    
    /**
     * Handles decline trade command
     */
    private void handleDeclineTrade(int playerId) {
        Trade trade = gameState.getActiveTrade();
        if (trade == null || !trade.isPending()) {
            sendError(playerId, "No active trade");
            return;
        }
        
        if (trade.getReceiverId() != playerId) {
            sendError(playerId, "You are not the trade receiver");
            return;
        }
        
        tradeManager.declineTrade(playerId);
        
        Player receiver = gameState.getPlayer(playerId);
        String receiverName = receiver != null ? receiver.getName() : "Player " + playerId;
        broadcastEventLog(receiverName + " declined the trade");
        gameState.setTurnPhase(TurnPhase.POST_ROLL);
        broadcastStateUpdate();
    }
    
    /**
     * Handles jail pay fine command
     */
    private void handleJailPayFine(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) return;
        
        if (!player.isInJail()) {
            sendError(playerId, "You are not in jail");
            return;
        }
        
        if (!jailManager.canPayFine(player)) {
            sendError(playerId, "Not enough money to pay fine");
            return;
        }
        
        jailManager.releaseByFine(player);
        broadcastEventLog(player.getName() + " paid $50 to get out of jail");
        broadcastStateUpdate();
    }
    
    /**
     * Handles jail use card command
     */
    private void handleJailUseCard(int playerId) {
        Player player = gameState.getPlayer(playerId);
        if (player == null) return;
        
        if (!player.isInJail()) {
            sendError(playerId, "You are not in jail");
            return;
        }
        
        if (!jailManager.canUseCard(player)) {
            sendError(playerId, "You don't have a Get Out of Jail Free card");
            return;
        }
        
        jailManager.releaseByCard(player);
        broadcastEventLog(player.getName() + " used a Get Out of Jail Free card");
        broadcastStateUpdate();
    }
    
    /**
     * Handles end turn command
     */
    private void handleEndTurn(int playerId) {
        if (playerId != gameState.getCurrentPlayerId()) {
            sendError(playerId, "Not your turn");
            return;
        }
        
        advanceToNextPlayer();
    }
    
    /**
     * Advances to the next player
     */
    private void advanceToNextPlayer() {
        // Reset dice doubles counter
        gameState.getDice().resetDoubles();
        
        // Find next active player
        int currentIdx = -1;
        for (int i = 0; i < playerOrder.size(); i++) {
            if (playerOrder.get(i) == gameState.getCurrentPlayerId()) {
                currentIdx = i;
                break;
            }
        }
        
        // Find next non-bankrupt player
        int nextIdx = (currentIdx + 1) % playerOrder.size();
        int attempts = 0;
        while (attempts < playerOrder.size()) {
            int nextPlayerId = playerOrder.get(nextIdx);
            Player nextPlayer = gameState.getPlayer(nextPlayerId);
            if (nextPlayer != null && !nextPlayer.isBankrupt()) {
                break;
            }
            nextIdx = (nextIdx + 1) % playerOrder.size();
            attempts++;
        }
        
        if (attempts >= playerOrder.size()) {
            // All players bankrupt except winner
            checkGameEnd();
            return;
        }
        
        int nextPlayerId = playerOrder.get(nextIdx);
        gameState.setCurrentPlayerId(nextPlayerId);
        gameState.incrementTurnNumber();
        gameState.setTurnPhase(TurnPhase.PRE_ROLL);
        
        ServerEvent turnEvent = ServerEvent.createTurnStart(nextPlayerId);
        broadcastToAll(turnEvent);
        
        broadcastStateUpdate();
    }
    
    /**
     * Checks if game should end
     */
    private void checkGameEnd() {
        Player winner = checkWinCondition();
        if (winner != null) {
            endGame(winner.getId());
        }
    }
    
    /**
     * Checks win condition
     * @return Winner or null
     */
    private Player checkWinCondition() {
        // Count non-bankrupt players
        ArrayList<Player> activePlayers = new ArrayList<>();
        ArrayList<Player> allPlayers = gameState.getAllPlayers();
        
        for (int i = 0; i < allPlayers.size(); i++) {
            Player player = allPlayers.get(i);
            if (!player.isBankrupt()) {
                activePlayers.add(player);
            }
        }
        
        // If only one player remains, they win
        if (activePlayers.size() == 1) {
            return activePlayers.get(0);
        }
        
        return null;
    }
    
    /**
     * Ends the game
     * @param winnerId Winner's ID
     */
    private void endGame(int winnerId) {
        gameEnded = true;
        gameState.setGameOver(true);
        gameState.setTurnPhase(TurnPhase.GAME_OVER);
        
        Player winner = gameState.getPlayer(winnerId);
        if (winner != null) {
            gameState.setWinner(winner);
        }
        
        ServerEvent endEvent = ServerEvent.createGameEnd(winnerId, 
            winner != null ? winner.getName() : "Unknown");
        broadcastToAll(endEvent);
        
        System.out.println("Game ended in room " + roomId + ". Winner: " + 
                          (winner != null ? winner.getName() : "Unknown"));
    }
    
    /**
     * Broadcasts state update to all players
     */
    private void broadcastStateUpdate() {
        String stateJson = serializer.serializeGameState(gameState);
        ServerEvent event = ServerEvent.createStateUpdate(stateJson);
        broadcastToAll(event);
    }
    
    /**
     * Broadcasts an event log message
     * @param description Event description
     */
    private void broadcastEventLog(String description) {
        ServerEvent event = ServerEvent.createEventLog(description);
        broadcastToAll(event);
    }
    
    /**
     * Broadcasts a message to all players in this room
     * @param message Message to broadcast
     */
    public void broadcastToAll(Message message) {
        for (int i = 0; i < playerOrder.size(); i++) {
            int playerId = playerOrder.get(i);
            ClientHandler handler = playerHandlers.get(playerId);
            if (handler != null && handler.isConnected()) {
                handler.sendMessage(message);
            }
        }
    }
    
    /**
     * Sends an error to a specific player
     * @param playerId Player ID
     * @param errorMessage Error message
     */
    private void sendError(int playerId, String errorMessage) {
        ClientHandler handler = playerHandlers.get(playerId);
        if (handler != null) {
            handler.sendError(errorMessage);
        }
    }
    
    /**
     * Adds a player to this room
     * @param playerId Player ID
     * @param handler Client handler
     */
    public void addPlayer(int playerId, ClientHandler handler) {
        playerHandlers.put(playerId, handler);
        playerOrder.add(playerId);
        System.out.println("Player " + playerId + " added to room " + roomId);
    }
    
    /**
     * Removes a player from this room
     * @param playerId Player ID
     */
    public void removePlayer(int playerId) {
        playerHandlers.put(playerId, null);
        
        // Remove from order list
        for (int i = 0; i < playerOrder.size(); i++) {
            if (playerOrder.get(i) == playerId) {
                playerOrder.remove(i);
                break;
            }
        }
    }
    
    /**
     * Called when a player joins with a name
     * @param playerId Player ID
     * @param playerName Player name
     */
    public void onPlayerJoined(int playerId, String playerName) {
        playerNames.put(playerId, playerName);
        
        ClientHandler handler = playerHandlers.get(playerId);
        if (handler != null) {
            handler.setPlayerName(playerName);
        }
    }
    
    /**
     * Called when a player disconnects
     * @param playerId Player ID
     */
    public void onPlayerDisconnect(int playerId) {
        removePlayer(playerId);
        
        if (gameStarted && !gameEnded) {
            // Mark player as bankrupt
            Player player = gameState.getPlayer(playerId);
            if (player != null) {
                bankruptcyManager.declareBankruptcy(playerId, -1);
                broadcastEventLog(player.getName() + " has disconnected and is eliminated");
                
                // If it was their turn, advance
                if (gameState.getCurrentPlayerId() == playerId) {
                    advanceToNextPlayer();
                }
                
                checkGameEnd();
            }
        }
    }
    
    /**
     * Gets the player count
     * @return Number of players
     */
    public int getPlayerCount() {
        return playerOrder.size();
    }
    
    /**
     * Checks if game has started
     * @return true if started
     */
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    /**
     * Checks if game has ended
     * @return true if ended
     */
    public boolean isGameEnded() {
        return gameEnded;
    }
    
    /**
     * Gets the room ID
     * @return Room ID
     */
    public String getRoomId() {
        return roomId;
    }
    
    /**
     * Gets the game state
     * @return Game state
     */
    public GameState getGameState() {
        return gameState;
    }
}
