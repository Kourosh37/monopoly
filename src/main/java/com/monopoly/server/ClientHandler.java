package com.monopoly.server;

import com.monopoly.network.protocol.ClientCommand;
import com.monopoly.network.protocol.Message;
import com.monopoly.network.protocol.MessageType;
import com.monopoly.network.protocol.ProtocolHandler;
import com.monopoly.network.protocol.ServerEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles communication with a single client.
 * Each connected player has their own ClientHandler running in a separate thread.
 */
public class ClientHandler implements Runnable {

    // Socket and streams
    private final Socket socket;
    private BufferedReader inputStream;
    private PrintWriter outputStream;
    
    // Player info
    private final int playerId;
    private String playerName;
    
    // Server reference
    private final Server server;
    
    // Protocol handler
    private final ProtocolHandler protocolHandler;
    
    // Connection state
    private volatile boolean isConnected;
    private volatile boolean isRunning;
    
    /**
     * Creates a new client handler
     * @param socket Client socket
     * @param playerId Assigned player ID
     * @param server Server reference
     */
    public ClientHandler(Socket socket, int playerId, Server server) {
        this.socket = socket;
        this.playerId = playerId;
        this.server = server;
        this.protocolHandler = server.getProtocolHandler();
        this.isConnected = true;
        this.isRunning = true;
        this.playerName = "Player " + playerId;
        
        try {
            this.inputStream = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            this.outputStream = new PrintWriter(
                socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error creating streams for player " + playerId + 
                              ": " + e.getMessage());
            isConnected = false;
        }
    }
    
    @Override
    public void run() {
        System.out.println("ClientHandler started for player " + playerId);
        
        try {
            // Main message loop
            StringBuilder messageBuffer = new StringBuilder();
            
            while (isRunning && isConnected) {
                String line = inputStream.readLine();
                
                if (line == null) {
                    // Connection closed
                    break;
                }
                
                // Add to buffer
                messageBuffer.append(line);
                
                // Check for complete message (JSON object)
                String buffered = messageBuffer.toString().trim();
                if (isCompleteJson(buffered)) {
                    processMessage(buffered);
                    messageBuffer = new StringBuilder();
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Error reading from player " + playerId + 
                                  ": " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }
    
    /**
     * Checks if a string is a complete JSON object
     * @param json JSON string
     * @return true if complete
     */
    private boolean isCompleteJson(String json) {
        if (json.isEmpty()) return false;
        if (!json.startsWith("{")) return false;
        
        int braceCount = 0;
        boolean inString = false;
        
        for (int i = 0; i < json.length(); i++) {
            char ch = json.charAt(i);
            
            if (ch == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (ch == '{') braceCount++;
                else if (ch == '}') braceCount--;
            }
        }
        
        return braceCount == 0;
    }
    
    /**
     * Processes a raw message
     * @param rawMessage Raw message string
     */
    private void processMessage(String rawMessage) {
        // Parse message
        Message message = protocolHandler.parseMessage(rawMessage);
        
        if (message == null) {
            sendError("Invalid message format");
            return;
        }
        
        // Must be a ClientCommand
        if (!(message instanceof ClientCommand)) {
            sendError("Expected client command");
            return;
        }
        
        ClientCommand command = (ClientCommand) message;
        handleCommand(command);
    }
    
    /**
     * Handles a client command
     * @param command Client command
     */
    private void handleCommand(ClientCommand command) {
        MessageType type = command.getCommandType();
        
        // Get room controller
        String roomId = server.getPlayerRoom(playerId);
        GameController controller = server.getGameController(roomId);
        
        switch (type) {
            case HELLO:
                handleHello(command);
                break;
                
            case DISCONNECT:
                handleDisconnect();
                break;
                
            case ROLL_DICE:
            case BUY_PROPERTY:
            case DECLINE_BUY:
            case BID:
            case PASS_BID:
            case BUILD:
            case SELL_BUILDING:
            case MORTGAGE:
            case UNMORTGAGE:
            case PROPOSE_TRADE:
            case ACCEPT_TRADE:
            case DECLINE_TRADE:
            case JAIL_PAY_FINE:
            case JAIL_USE_CARD:
            case END_TURN:
                // Forward to game controller
                if (controller != null) {
                    controller.processCommand(playerId, command);
                } else {
                    sendError("Not in a game room");
                }
                break;
                
            default:
                sendError("Unknown command: " + type);
                break;
        }
    }
    
    /**
     * Handles HELLO command (player joining)
     * @param command Hello command
     */
    private void handleHello(ClientCommand command) {
        String name = command.getStringParameter("playerName");
        if (name != null && !name.trim().isEmpty()) {
            this.playerName = name.trim();
        }
        
        System.out.println("Player " + playerId + " joined as: " + playerName);
        
        // Get room and notify
        String roomId = server.getPlayerRoom(playerId);
        GameController controller = server.getGameController(roomId);
        
        if (controller != null) {
            controller.onPlayerJoined(playerId, playerName);
        }
        
        // Send welcome event
        ServerEvent welcomeEvent = ServerEvent.createPlayerJoined(playerId, playerName);
        server.broadcastToRoom(roomId, welcomeEvent);
    }
    
    /**
     * Handles DISCONNECT command
     */
    private void handleDisconnect() {
        System.out.println("Player " + playerId + " requested disconnect");
        isRunning = false;
    }
    
    /**
     * Sends a message to this client
     * @param message Message to send
     */
    public void sendMessage(Message message) {
        if (!isConnected || outputStream == null) {
            return;
        }
        
        try {
            String json = protocolHandler.serializeMessage(message);
            outputStream.println(json);
            outputStream.flush();
        } catch (Exception e) {
            System.err.println("Error sending to player " + playerId + 
                              ": " + e.getMessage());
        }
    }
    
    /**
     * Sends an error message to this client
     * @param errorMessage Error description
     */
    public void sendError(String errorMessage) {
        ServerEvent event = ServerEvent.createError(errorMessage, playerId);
        sendMessage(event);
    }
    
    /**
     * Disconnects the client
     */
    public void disconnect() {
        if (!isConnected) {
            return;
        }
        
        isConnected = false;
        isRunning = false;
        
        // Notify server
        server.onClientDisconnect(playerId);
        
        // Close streams
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection for player " + playerId + 
                              ": " + e.getMessage());
        }
        
        System.out.println("Player " + playerId + " disconnected");
    }
    
    /**
     * Stops the handler
     */
    public void stop() {
        isRunning = false;
        disconnect();
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
     * Sets the player name
     * @param name Player name
     */
    public void setPlayerName(String name) {
        this.playerName = name;
    }
    
    /**
     * Checks if connected
     * @return true if connected
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Gets the socket
     * @return Socket
     */
    public Socket getSocket() {
        return socket;
    }
    
    @Override
    public String toString() {
        return "ClientHandler{" +
                "playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", connected=" + isConnected +
                '}';
    }
}
