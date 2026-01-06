package com.monopoly.client;

import java.util.Scanner;

/**
 * Entry point for starting the Monopoly client with GUI.
 * Launches the JavaFX application.
 */
public class ClientMain {

    /**
     * Main entry point for client
     * @param args Command line arguments: [host] [port] [playerName]
     */
    public static void main(String[] args) {
        // For now, start a simple console client
        // GUI version will use: com.monopoly.ui.MainApp.main(args);
        
        String host = Client.DEFAULT_HOST;
        int port = Client.DEFAULT_PORT;
        String playerName = "Player";
        
        // Parse arguments
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port, using default: " + Client.DEFAULT_PORT);
            }
        }
        if (args.length >= 3) {
            playerName = args[2];
        }
        
        System.out.println("=== Monopoly Client ===");
        System.out.println("Connecting to " + host + ":" + port);
        
        // Create client with console listener
        Client client = new Client();
        client.setEventListener(new ConsoleEventListener());
        
        // Get player name if not provided
        Scanner scanner = new Scanner(System.in);
        if (args.length < 3) {
            System.out.print("Enter your name: ");
            playerName = scanner.nextLine().trim();
            if (playerName.isEmpty()) {
                playerName = "Player";
            }
        }
        
        // Connect
        if (!client.connect(host, port, playerName)) {
            System.err.println("Failed to connect to server");
            return;
        }
        
        System.out.println("Connected as: " + playerName);
        System.out.println("Commands: roll, buy, decline, end, bid <amount>, pass, quit");
        
        // Command loop
        while (client.isConnected()) {
            System.out.print("> ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.isEmpty()) continue;
            
            String[] parts = input.split("\\s+");
            String command = parts[0];
            
            switch (command) {
                case "roll":
                    client.rollDice();
                    break;
                case "buy":
                    if (parts.length > 1) {
                        try {
                            int propertyId = Integer.parseInt(parts[1]);
                            client.buyProperty(propertyId);
                        } catch (NumberFormatException e) {
                            System.out.println("Usage: buy <propertyId>");
                        }
                    } else {
                        // Buy current position
                        client.buyProperty(-1);
                    }
                    break;
                case "decline":
                    client.declineBuy();
                    break;
                case "bid":
                    if (parts.length > 1) {
                        try {
                            int amount = Integer.parseInt(parts[1]);
                            client.bid(amount);
                        } catch (NumberFormatException e) {
                            System.out.println("Usage: bid <amount>");
                        }
                    } else {
                        System.out.println("Usage: bid <amount>");
                    }
                    break;
                case "pass":
                    client.passBid();
                    break;
                case "payjail":
                    client.payJailFine();
                    break;
                case "usejailcard":
                    client.useJailCard();
                    break;
                case "end":
                    client.endTurn();
                    break;
                case "quit":
                case "exit":
                    client.disconnect();
                    break;
                default:
                    System.out.println("Unknown command: " + command);
                    break;
            }
        }
        
        scanner.close();
        System.out.println("Client terminated");
    }
    
    /**
     * Simple console-based event listener
     */
    static class ConsoleEventListener implements ClientEventListener {
        @Override
        public void onConnected() {
            System.out.println("[System] Connected to server");
        }
        
        @Override
        public void onDisconnected() {
            System.out.println("[System] Disconnected from server");
        }
        
        @Override
        public void onStateUpdate(String stateJson) {
            // Don't print full state, too verbose
            System.out.println("[State] Updated");
        }
        
        @Override
        public void onEventLog(String event) {
            System.out.println("[Event] " + event);
        }
        
        @Override
        public void onError(String errorMessage) {
            System.out.println("[Error] " + errorMessage);
        }
        
        @Override
        public void onGameStart() {
            System.out.println("[Game] Game has started!");
        }
        
        @Override
        public void onGameEnd(int winnerId, String winnerName) {
            System.out.println("[Game] Game over! Winner: " + winnerName + " (ID: " + winnerId + ")");
        }
        
        @Override
        public void onTurnStart(int currentPlayerId) {
            System.out.println("[Turn] Player " + currentPlayerId + "'s turn");
        }
        
        @Override
        public void onDiceRolled(int die1, int die2, boolean isDoubles) {
            String doublesStr = isDoubles ? " (DOUBLES!)" : "";
            System.out.println("[Dice] Rolled " + die1 + " + " + die2 + " = " + (die1 + die2) + doublesStr);
        }
        
        @Override
        public void onPlayerJoined(int playerId, String playerName) {
            System.out.println("[Player] " + playerName + " (ID: " + playerId + ") joined");
        }
        
        @Override
        public void onPlayerLeft(int playerId, String reason) {
            System.out.println("[Player] Player " + playerId + " left: " + reason);
        }
        
        @Override
        public void onAuctionStart(int propertyId, String propertyName) {
            System.out.println("[Auction] Started for " + propertyName);
        }
        
        @Override
        public void onAuctionUpdate(int currentBid, int highestBidderId) {
            System.out.println("[Auction] Current bid: $" + currentBid + " by player " + highestBidderId);
        }
        
        @Override
        public void onAuctionEnd(int winnerId, int winningBid) {
            System.out.println("[Auction] Ended - Winner: Player " + winnerId + " for $" + winningBid);
        }
        
        @Override
        public void onCardDrawn(String cardType, String description) {
            System.out.println("[Card] " + cardType + ": " + description);
        }
        
        @Override
        public void onPlayerBankrupt(int playerId) {
            System.out.println("[Bankrupt] Player " + playerId + " is bankrupt!");
        }
        
        @Override
        public void onTradeProposed(int initiatorId, int receiverId) {
            System.out.println("[Trade] Player " + initiatorId + " proposed trade to Player " + receiverId);
        }
        
        @Override
        public void onTradeCompleted(boolean accepted) {
            System.out.println("[Trade] Trade " + (accepted ? "accepted" : "declined"));
        }
    }
}
