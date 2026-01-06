package com.monopoly.client;

import com.monopoly.model.game.GameState;
import com.monopoly.network.protocol.ClientCommand;
import com.monopoly.network.protocol.ServerEvent;
import com.monopoly.network.serialization.Deserializer;

/**
 * Main client class for connecting to the Monopoly server.
 * Clients are "dumb" terminals - they only display what the server tells them.
 * No game logic calculations are performed client-side.
 */
public class Client {

    /** Default server port */
    public static final int DEFAULT_PORT = 12345;
    
    /** Default server host */
    public static final String DEFAULT_HOST = "localhost";
    
    // Server connection
    private ServerConnection serverConnection;
    
    // Player info
    private int playerId;
    private String playerName;
    
    // Local state (copy from server)
    private GameState localGameState;
    
    // Deserializer for parsing
    private final Deserializer deserializer;
    
    // Event listener for GUI
    private ClientEventListener eventListener;
    
    // Connection state
    private volatile boolean isConnected;
    
    /**
     * Creates a new client
     */
    public Client() {
        this.deserializer = new Deserializer();
        this.isConnected = false;
        this.playerId = -1;
    }
    
    /**
     * Connects to the server
     * @param host Server host
     * @param port Server port
     * @param playerName Player name
     * @return true if connection successful
     */
    public boolean connect(String host, int port, String playerName) {
        if (isConnected) {
            disconnect();
        }
        
        this.playerName = playerName;
        this.serverConnection = new ServerConnection(this);
        
        if (!serverConnection.connect(host, port)) {
            return false;
        }
        
        isConnected = true;
        
        // Start receiver thread
        Thread receiverThread = new Thread(serverConnection);
        receiverThread.setDaemon(true);
        receiverThread.start();
        
        // Send hello
        ClientCommand helloCommand = ClientCommand.createHello(playerName);
        sendCommand(helloCommand);
        
        return true;
    }
    
    /**
     * Connects to default server
     * @param playerName Player name
     * @return true if successful
     */
    public boolean connect(String playerName) {
        return connect(DEFAULT_HOST, DEFAULT_PORT, playerName);
    }
    
    /**
     * Disconnects from the server
     */
    public void disconnect() {
        if (!isConnected) {
            return;
        }
        
        // Send disconnect command
        ClientCommand disconnectCommand = ClientCommand.createDisconnect(playerId);
        sendCommand(disconnectCommand);
        
        isConnected = false;
        
        if (serverConnection != null) {
            serverConnection.disconnect();
            serverConnection = null;
        }
        
        if (eventListener != null) {
            eventListener.onDisconnected();
        }
    }
    
    /**
     * Sends a command to the server
     * @param command Command to send
     */
    public void sendCommand(ClientCommand command) {
        if (!isConnected || serverConnection == null) {
            System.err.println("Not connected to server");
            return;
        }
        
        serverConnection.sendMessage(command);
    }
    
    // ==================== Game Commands ====================
    
    /**
     * Sends roll dice command
     */
    public void rollDice() {
        sendCommand(ClientCommand.createRollDice(playerId));
    }
    
    /**
     * Sends buy property command
     * @param propertyId Property ID
     */
    public void buyProperty(int propertyId) {
        sendCommand(ClientCommand.createBuyProperty(playerId, propertyId));
    }
    
    /**
     * Sends decline buy command (start auction)
     */
    public void declineBuy() {
        sendCommand(ClientCommand.createDeclineBuy(playerId));
    }
    
    /**
     * Sends bid command
     * @param amount Bid amount
     */
    public void bid(int amount) {
        sendCommand(ClientCommand.createBid(playerId, amount));
    }
    
    /**
     * Sends pass bid command
     */
    public void passBid() {
        sendCommand(ClientCommand.createPassBid(playerId));
    }
    
    /**
     * Sends build command
     * @param propertyId Property ID
     * @param isHotel true for hotel, false for house
     */
    public void build(int propertyId, boolean isHotel) {
        String type = isHotel ? "hotel" : "house";
        sendCommand(ClientCommand.createBuild(playerId, propertyId, type));
    }
    
    /**
     * Sends mortgage command
     * @param propertyId Property ID
     */
    public void mortgage(int propertyId) {
        sendCommand(ClientCommand.createMortgage(playerId, propertyId));
    }
    
    /**
     * Sends pay jail fine command
     */
    public void payJailFine() {
        sendCommand(ClientCommand.createJailPayFine(playerId));
    }
    
    /**
     * Sends use jail card command
     */
    public void useJailCard() {
        sendCommand(ClientCommand.createJailUseCard(playerId));
    }
    
    /**
     * Sends end turn command
     */
    public void endTurn() {
        sendCommand(ClientCommand.createEndTurn(playerId));
    }
    
    // ==================== Event Handlers (called by ServerConnection) ====================
    
    /**
     * Called when state update received
     * @param event Server event
     */
    void onServerEvent(ServerEvent event) {
        if (event == null) return;
        
        switch (event.getEventType()) {
            case STATE_UPDATE:
                onStateUpdate(event);
                break;
            case EVENT_LOG:
                onEventLog(event.getStringData("description"));
                break;
            case ERROR:
                onError(event.getStringData("message"));
                break;
            case GAME_START:
                if (eventListener != null) {
                    eventListener.onGameStart();
                }
                break;
            case GAME_END:
                if (eventListener != null) {
                    int winnerId = event.getIntData("winnerId", -1);
                    String winnerName = event.getStringData("winnerName");
                    eventListener.onGameEnd(winnerId, winnerName);
                }
                break;
            case TURN_START:
                if (eventListener != null) {
                    int currentPlayerId = event.getIntData("currentPlayerId", -1);
                    eventListener.onTurnStart(currentPlayerId);
                }
                break;
            case DICE_RESULT:
                if (eventListener != null) {
                    int die1 = event.getIntData("die1", 0);
                    int die2 = event.getIntData("die2", 0);
                    boolean isDoubles = event.getBooleanData("isDoubles", false);
                    eventListener.onDiceRolled(die1, die2, isDoubles);
                }
                break;
            case PLAYER_JOINED:
                if (eventListener != null) {
                    int joinedPlayerId = event.getIntData("playerId", -1);
                    String joinedPlayerName = event.getStringData("playerName");
                    eventListener.onPlayerJoined(joinedPlayerId, joinedPlayerName);
                }
                break;
            case PLAYER_LEFT:
                if (eventListener != null) {
                    int leftPlayerId = event.getIntData("playerId", -1);
                    String reason = event.getStringData("reason");
                    eventListener.onPlayerLeft(leftPlayerId, reason);
                }
                break;
            case AUCTION_START:
                if (eventListener != null) {
                    int propertyId = event.getIntData("propertyId", -1);
                    String propertyName = event.getStringData("propertyName");
                    eventListener.onAuctionStart(propertyId, propertyName);
                }
                break;
            case AUCTION_UPDATE:
                if (eventListener != null) {
                    int currentBid = event.getIntData("currentBid", 0);
                    int highestBidderId = event.getIntData("highestBidderId", -1);
                    eventListener.onAuctionUpdate(currentBid, highestBidderId);
                }
                break;
            case AUCTION_END:
                if (eventListener != null) {
                    int winnerId = event.getIntData("winnerId", -1);
                    int winningBid = event.getIntData("currentBid", 0);
                    eventListener.onAuctionEnd(winnerId, winningBid);
                }
                break;
            case CARD_DRAWN:
                if (eventListener != null) {
                    String cardType = event.getStringData("cardType");
                    String description = event.getStringData("cardDescription");
                    eventListener.onCardDrawn(cardType, description);
                }
                break;
            case PLAYER_BANKRUPT:
                if (eventListener != null) {
                    int bankruptPlayerId = event.getIntData("playerId", -1);
                    eventListener.onPlayerBankrupt(bankruptPlayerId);
                }
                break;
            default:
                break;
        }
    }
    
    /**
     * Handles state update
     * @param event State update event
     */
    private void onStateUpdate(ServerEvent event) {
        // Parse game state from JSON
        String stateJson = event.getStringData("gameState");
        
        // For now, just store the raw JSON or parse it
        // A full implementation would deserialize into GameState
        if (eventListener != null) {
            eventListener.onStateUpdate(stateJson);
        }
    }
    
    /**
     * Handles event log
     * @param eventDescription Event description
     */
    private void onEventLog(String eventDescription) {
        System.out.println("[Event] " + eventDescription);
        
        if (eventListener != null) {
            eventListener.onEventLog(eventDescription);
        }
    }
    
    /**
     * Handles error
     * @param errorMessage Error message
     */
    private void onError(String errorMessage) {
        System.err.println("[Error] " + errorMessage);
        
        if (eventListener != null) {
            eventListener.onError(errorMessage);
        }
    }
    
    /**
     * Called when connection is lost
     */
    void onConnectionLost() {
        isConnected = false;
        
        if (eventListener != null) {
            eventListener.onDisconnected();
        }
    }
    
    // ==================== Getters/Setters ====================
    
    /**
     * Sets the player ID (called by server connection)
     * @param playerId Player ID
     */
    void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
    
    /**
     * Sets the event listener
     * @param listener Event listener
     */
    public void setEventListener(ClientEventListener listener) {
        this.eventListener = listener;
    }
    
    /**
     * Gets the event listener
     * @return Event listener
     */
    public ClientEventListener getEventListener() {
        return eventListener;
    }
    
    /**
     * Gets the local game state
     * @return Game state
     */
    public GameState getLocalGameState() {
        return localGameState;
    }
    
    /**
     * Gets the player ID
     * @return Player ID
     */
    public int getPlayerId() {
        return playerId;
    }
    
    /**
     * Gets the player name
     * @return Player name
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Checks if connected
     * @return true if connected
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Checks if it's this player's turn
     * @return true if my turn
     */
    public boolean isMyTurn() {
        if (localGameState == null) {
            return false;
        }
        return localGameState.getCurrentPlayerId() == playerId;
    }
    
    /**
     * Gets the server connection
     * @return Server connection
     */
    public ServerConnection getServerConnection() {
        return serverConnection;
    }
}
