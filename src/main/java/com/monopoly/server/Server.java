package com.monopoly.server;

import com.monopoly.datastructures.ArrayList;
import com.monopoly.datastructures.HashTable;
import com.monopoly.network.protocol.Message;
import com.monopoly.network.protocol.ServerEvent;
import com.monopoly.network.protocol.ProtocolHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main server class for the Monopoly game.
 * Acts as the Single Source of Truth for all game logic.
 * Supports multiple game rooms with 2-4 players each.
 */
public class Server {

    /** Default server port */
    public static final int DEFAULT_PORT = 12345;
    
    /** Minimum players to start a game */
    public static final int MIN_PLAYERS = 2;
    
    /** Maximum players per room */
    public static final int MAX_PLAYERS = 4;
    
    /** Maximum concurrent rooms */
    public static final int MAX_ROOMS = 10;
    
    // Server socket
    private ServerSocket serverSocket;
    
    // Port number
    private final int port;
    
    // Game rooms (roomId -> GameController)
    private final HashTable<String, GameController> gameRooms;
    
    // All client handlers (playerId -> ClientHandler)
    private final HashTable<Integer, ClientHandler> clientHandlers;
    
    // Player to room mapping
    private final HashTable<Integer, String> playerRooms;
    
    // Protocol handler
    private final ProtocolHandler protocolHandler;
    
    // Thread pool for client handlers
    private final ExecutorService executorService;
    
    // Server state
    private volatile boolean isRunning;
    
    // Next player ID
    private int nextPlayerId;
    
    // Room counter for unique IDs
    private int roomCounter;
    
    // Lobby room (waiting for game start)
    private GameController lobbyRoom;
    
    /**
     * Creates a new server with default port
     */
    public Server() {
        this(DEFAULT_PORT);
    }
    
    /**
     * Creates a new server
     * @param port Port to listen on
     */
    public Server(int port) {
        this.port = port;
        this.gameRooms = new HashTable<>();
        this.clientHandlers = new HashTable<>();
        this.playerRooms = new HashTable<>();
        this.protocolHandler = new ProtocolHandler();
        this.executorService = Executors.newCachedThreadPool();
        this.isRunning = false;
        this.nextPlayerId = 1;
        this.roomCounter = 1;
        this.lobbyRoom = createNewRoom("lobby");
    }
    
    /**
     * Creates a new game room
     * @param roomId Room ID
     * @return GameController for the room
     */
    private GameController createNewRoom(String roomId) {
        GameController controller = new GameController(roomId, this);
        gameRooms.put(roomId, controller);
        return controller;
    }
    
    /**
     * Gets the next unique room ID
     * @return Room ID
     */
    private synchronized String getNextRoomId() {
        return "room_" + (roomCounter++);
    }
    
    /**
     * Gets the next unique player ID
     * @return Player ID
     */
    public synchronized int getNextPlayerId() {
        return nextPlayerId++;
    }
    
    /**
     * Starts the server
     */
    public void start() {
        if (isRunning) {
            System.out.println("Server is already running");
            return;
        }
        
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("Monopoly Server started on port " + port);
            System.out.println("Waiting for connections...");
            
            acceptConnections();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
    
    /**
     * Accepts incoming connections
     */
    private void acceptConnections() {
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleNewConnection(clientSocket);
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Handles a new client connection
     * @param socket Client socket
     */
    private void handleNewConnection(Socket socket) {
        int playerId = getNextPlayerId();
        System.out.println("New connection from " + socket.getInetAddress() + 
                          " assigned ID: " + playerId);
        
        // Create client handler
        ClientHandler handler = new ClientHandler(socket, playerId, this);
        clientHandlers.put(playerId, handler);
        
        // Assign to lobby room
        playerRooms.put(playerId, "lobby");
        lobbyRoom.addPlayer(playerId, handler);
        
        // Start handler thread
        executorService.submit(handler);
    }
    
    /**
     * Broadcasts a message to all players in a room
     * @param roomId Room ID
     * @param message Message to broadcast
     */
    public void broadcastToRoom(String roomId, Message message) {
        GameController room = gameRooms.get(roomId);
        if (room != null) {
            room.broadcastToAll(message);
        }
    }
    
    /**
     * Broadcasts a message to all connected clients
     * @param message Message to broadcast
     */
    public void broadcastToAll(Message message) {
        ArrayList<Integer> playerIds = new ArrayList<>();
        // Get all player IDs
        for (int i = 1; i < nextPlayerId; i++) {
            if (clientHandlers.get(i) != null) {
                playerIds.add(i);
            }
        }
        
        for (int i = 0; i < playerIds.size(); i++) {
            sendToPlayer(playerIds.get(i), message);
        }
    }
    
    /**
     * Sends a message to a specific player
     * @param playerId Player ID
     * @param message Message to send
     */
    public void sendToPlayer(int playerId, Message message) {
        ClientHandler handler = clientHandlers.get(playerId);
        if (handler != null) {
            handler.sendMessage(message);
        }
    }
    
    /**
     * Handles client disconnection
     * @param playerId Player ID
     */
    public void onClientDisconnect(int playerId) {
        System.out.println("Player " + playerId + " disconnected");
        
        // Remove from handler map
        ClientHandler handler = clientHandlers.get(playerId);
        if (handler != null) {
            clientHandlers.put(playerId, null);
        }
        
        // Get room
        String roomId = playerRooms.get(playerId);
        if (roomId != null) {
            GameController room = gameRooms.get(roomId);
            if (room != null) {
                room.onPlayerDisconnect(playerId);
            }
            playerRooms.put(playerId, null);
        }
        
        // Broadcast disconnect event
        ServerEvent event = ServerEvent.createPlayerLeft(playerId, "Disconnected");
        if (roomId != null) {
            broadcastToRoom(roomId, event);
        }
    }
    
    /**
     * Moves a player to a new room
     * @param playerId Player ID
     * @param newRoomId New room ID
     */
    public void movePlayerToRoom(int playerId, String newRoomId) {
        String currentRoomId = playerRooms.get(playerId);
        ClientHandler handler = clientHandlers.get(playerId);
        
        if (handler == null) return;
        
        // Remove from current room
        if (currentRoomId != null) {
            GameController currentRoom = gameRooms.get(currentRoomId);
            if (currentRoom != null) {
                currentRoom.removePlayer(playerId);
            }
        }
        
        // Create new room if needed
        GameController newRoom = gameRooms.get(newRoomId);
        if (newRoom == null) {
            newRoom = createNewRoom(newRoomId);
        }
        
        // Add to new room
        newRoom.addPlayer(playerId, handler);
        playerRooms.put(playerId, newRoomId);
    }
    
    /**
     * Creates a new game room and moves player to it
     * @param playerId Creator's player ID
     * @return New room ID
     */
    public String createRoom(int playerId) {
        String roomId = getNextRoomId();
        GameController room = createNewRoom(roomId);
        movePlayerToRoom(playerId, roomId);
        return roomId;
    }
    
    /**
     * Joins a player to an existing room
     * @param playerId Player ID
     * @param roomId Room ID
     * @return true if successful
     */
    public boolean joinRoom(int playerId, String roomId) {
        GameController room = gameRooms.get(roomId);
        if (room == null) {
            return false;
        }
        
        if (room.getPlayerCount() >= MAX_PLAYERS) {
            return false;
        }
        
        if (room.isGameStarted()) {
            return false;
        }
        
        movePlayerToRoom(playerId, roomId);
        return true;
    }
    
    /**
     * Starts the game in a room
     * @param roomId Room ID
     * @return true if started successfully
     */
    public boolean startGame(String roomId) {
        GameController room = gameRooms.get(roomId);
        if (room == null) {
            return false;
        }
        
        if (room.getPlayerCount() < MIN_PLAYERS) {
            return false;
        }
        
        if (room.isGameStarted()) {
            return false;
        }
        
        room.startGame();
        return true;
    }
    
    /**
     * Gets the protocol handler
     * @return Protocol handler
     */
    public ProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }
    
    /**
     * Gets a game controller by room ID
     * @param roomId Room ID
     * @return GameController or null
     */
    public GameController getGameController(String roomId) {
        return gameRooms.get(roomId);
    }
    
    /**
     * Gets the room ID for a player
     * @param playerId Player ID
     * @return Room ID or null
     */
    public String getPlayerRoom(int playerId) {
        return playerRooms.get(playerId);
    }
    
    /**
     * Checks if the server is running
     * @return true if running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Gets the connected player count
     * @return Number of connected players
     */
    public int getConnectedPlayerCount() {
        int count = 0;
        for (int i = 1; i < nextPlayerId; i++) {
            if (clientHandlers.get(i) != null) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Gets the number of active rooms
     * @return Number of rooms
     */
    public int getActiveRoomCount() {
        return roomCounter - 1;
    }
    
    /**
     * Stops the server gracefully
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        System.out.println("Stopping server...");
        
        // Close all client connections
        for (int i = 1; i < nextPlayerId; i++) {
            ClientHandler handler = clientHandlers.get(i);
            if (handler != null) {
                handler.stop();
            }
        }
        
        // Shutdown executor
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        System.out.println("Server stopped");
    }
    
    /**
     * Gets the server port
     * @return Port number
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Main entry point
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // Parse command line arguments
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default: " + DEFAULT_PORT);
            }
        }
        
        Server server = new Server(port);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutdown signal received");
            server.stop();
        }));
        
        // Start server
        server.start();
    }
}
