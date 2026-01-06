package com.monopoly.client;

import com.monopoly.network.protocol.Message;
import com.monopoly.network.protocol.ProtocolHandler;
import com.monopoly.network.protocol.ServerEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles the network connection to the server.
 * Manages sending commands and receiving state updates.
 */
public class ServerConnection implements Runnable {

    // Socket and streams
    private Socket socket;
    private BufferedReader inputStream;
    private PrintWriter outputStream;
    
    // Parent client
    private final Client client;
    
    // Server info
    private String serverHost;
    private int serverPort;
    
    // Protocol handler
    private final ProtocolHandler protocolHandler;
    
    // Connection state
    private volatile boolean isConnected;
    private volatile boolean isRunning;
    
    /**
     * Creates a new server connection
     * @param client Parent client
     */
    public ServerConnection(Client client) {
        this.client = client;
        this.protocolHandler = new ProtocolHandler();
        this.isConnected = false;
        this.isRunning = false;
    }
    
    /**
     * Connects to the server
     * @param host Server host
     * @param port Server port
     * @return true if successful
     */
    public boolean connect(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
        
        try {
            socket = new Socket(host, port);
            socket.setKeepAlive(true);
            
            inputStream = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            outputStream = new PrintWriter(socket.getOutputStream(), true);
            
            isConnected = true;
            isRunning = true;
            
            System.out.println("Connected to server at " + host + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Disconnects from the server
     */
    public void disconnect() {
        isRunning = false;
        isConnected = false;
        
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
            System.err.println("Error disconnecting: " + e.getMessage());
        }
        
        System.out.println("Disconnected from server");
    }
    
    @Override
    public void run() {
        System.out.println("Server connection thread started");
        
        try {
            StringBuilder messageBuffer = new StringBuilder();
            
            while (isRunning && isConnected) {
                String line = inputStream.readLine();
                
                if (line == null) {
                    // Server closed connection
                    break;
                }
                
                // Add to buffer
                messageBuffer.append(line);
                
                // Check for complete JSON message
                String buffered = messageBuffer.toString().trim();
                if (isCompleteJson(buffered)) {
                    processMessage(buffered);
                    messageBuffer = new StringBuilder();
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Connection error: " + e.getMessage());
            }
        } finally {
            isConnected = false;
            client.onConnectionLost();
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
     * Processes a raw message from server
     * @param rawMessage Raw message string
     */
    private void processMessage(String rawMessage) {
        // Parse message
        Message message = protocolHandler.parseMessage(rawMessage);
        
        if (message == null) {
            System.err.println("Failed to parse message: " + rawMessage);
            return;
        }
        
        // Must be a ServerEvent
        if (message instanceof ServerEvent) {
            ServerEvent event = (ServerEvent) message;
            client.onServerEvent(event);
        } else {
            System.err.println("Unexpected message type from server");
        }
    }
    
    /**
     * Sends a message to the server
     * @param message Message to send
     */
    public void sendMessage(Message message) {
        if (!isConnected || outputStream == null) {
            System.err.println("Not connected, cannot send message");
            return;
        }
        
        try {
            String json = protocolHandler.serializeMessage(message);
            outputStream.println(json);
            outputStream.flush();
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
    
    /**
     * Checks if connected
     * @return true if connected
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Gets the server host
     * @return Server host
     */
    public String getServerHost() {
        return serverHost;
    }
    
    /**
     * Gets the server port
     * @return Server port
     */
    public int getServerPort() {
        return serverPort;
    }
    
    /**
     * Gets the socket
     * @return Socket
     */
    public Socket getSocket() {
        return socket;
    }
}
