package com.monopoly.server;

/**
 * Entry point for starting the Monopoly server.
 * Can be run independently to host a game.
 */
public class ServerMain {

    /** Default server port */
    public static final int DEFAULT_PORT = Server.DEFAULT_PORT;
    
    /**
     * Main entry point for server
     * @param args Command line arguments: [port]
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // Parse command line arguments
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                if (port < 1024 || port > 65535) {
                    System.err.println("Port must be between 1024 and 65535");
                    System.err.println("Using default port: " + DEFAULT_PORT);
                    port = DEFAULT_PORT;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default: " + DEFAULT_PORT);
            }
        }
        
        System.out.println("=================================");
        System.out.println("   Monopoly Game Server v1.0");
        System.out.println("=================================");
        System.out.println();
        
        // Create server
        Server server = new Server(port);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            System.out.println("Shutdown signal received...");
            server.stop();
        }));
        
        // Print instructions
        System.out.println("Server Configuration:");
        System.out.println("  - Port: " + port);
        System.out.println("  - Min Players: " + Server.MIN_PLAYERS);
        System.out.println("  - Max Players: " + Server.MAX_PLAYERS);
        System.out.println("  - Max Rooms: " + Server.MAX_ROOMS);
        System.out.println();
        System.out.println("Press Ctrl+C to stop the server");
        System.out.println();
        
        // Start server (blocking)
        server.start();
    }
}
