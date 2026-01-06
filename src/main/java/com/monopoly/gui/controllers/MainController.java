package com.monopoly.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.monopoly.client.Client;
import com.monopoly.model.game.GameState;
import com.monopoly.model.player.Player;

/**
 * Main controller for the game window.
 * Coordinates all sub-controllers and handles overall UI state.
 */
public class MainController implements Initializable {

    // FXML Injected Fields
    @FXML private BorderPane mainContainer;
    @FXML private VBox playerPanelContainer;
    @FXML private VBox playerCardsContainer;
    @FXML private StackPane boardContainer;
    @FXML private GridPane boardGrid;
    @FXML private VBox boardCenter;
    @FXML private HBox diceContainer;
    @FXML private VBox actionPanel;
    @FXML private VBox logPanel;
    @FXML private StackPane dice1, dice2;
    @FXML private Label dice1Value, dice2Value, diceTotal;
    @FXML private Button rollDiceButton;
    @FXML private Label gameStatusLabel;
    @FXML private StackPane cardDisplayPane;
    @FXML private Label cardTypeLabel, cardTextLabel;
    
    @FXML private Circle currentPlayerToken;
    @FXML private Label currentPlayerName, currentPlayerMoney;
    
    @FXML private Button buyPropertyButton, auctionButton;
    @FXML private Button buildHouseButton, buildHotelButton;
    @FXML private Button mortgageButton, unmortgageButton;
    @FXML private Button tradeButton, viewPropertiesButton;
    @FXML private Button endTurnButton;
    
    @FXML private VBox jailActionsPane;
    @FXML private Button payJailFineButton, useJailCardButton;
    @FXML private Label jailTurnsLabel;
    
    @FXML private VBox logEntriesContainer;
    @FXML private Circle connectionIndicator;
    @FXML private Label connectionStatus, roomInfoLabel, turnInfoLabel;
    
    // Game state
    private Client client;
    private GameState currentGameState;
    private BoardController boardController;
    private int myPlayerId = -1;
    private boolean isMyTurn = false;
    
    // Animation
    private Timeline diceRollAnimation;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize board controller
        boardController = new BoardController(boardGrid);
        
        // Set up dice animation
        setupDiceAnimation();
        
        // Initial UI state
        disableAllActions();
        updateConnectionStatus(false);
        
        // Add welcome log
        addLogEntry("Welcome to Monopoly! Connect to a server to start playing.", "info");
    }
    
    /**
     * Sets up the dice rolling animation
     */
    private void setupDiceAnimation() {
        diceRollAnimation = new Timeline(
            new KeyFrame(Duration.millis(50), e -> {
                dice1Value.setText(String.valueOf((int)(Math.random() * 6) + 1));
                dice2Value.setText(String.valueOf((int)(Math.random() * 6) + 1));
            })
        );
        diceRollAnimation.setCycleCount(20);
    }
    
    /**
     * Sets the client instance
     */
    public void setClient(Client client) {
        this.client = client;
    }
    
    /**
     * Sets the player ID for this client
     */
    public void setMyPlayerId(int playerId) {
        this.myPlayerId = playerId;
    }
    
    // ==================== Event Handlers ====================
    
    @FXML
    private void handleNewGame() {
        // Show new game dialog
        addLogEntry("Creating new game...", "info");
    }
    
    @FXML
    private void handleJoinGame() {
        // Show join game dialog
        addLogEntry("Joining game...", "info");
    }
    
    @FXML
    private void handleSaveGame() {
        addLogEntry("Game saved.", "success");
    }
    
    @FXML
    private void handleLoadGame() {
        addLogEntry("Loading game...", "info");
    }
    
    @FXML
    private void handleExit() {
        Platform.exit();
    }
    
    @FXML
    private void handleSoundSettings() {
        // Show sound settings dialog
    }
    
    @FXML
    private void handleDisplaySettings() {
        // Show display settings dialog
    }
    
    @FXML
    private void handleHowToPlay() {
        showInfoDialog("How to Play", 
            "🎲 Roll the dice to move around the board\n" +
            "🏠 Buy properties you land on\n" +
            "💰 Collect rent from other players\n" +
            "🏨 Build houses and hotels to increase rent\n" +
            "💸 Avoid bankruptcy to win!");
    }
    
    @FXML
    private void handleRules() {
        showInfoDialog("Rules", 
            "• Players start with $1500\n" +
            "• Roll doubles to roll again (3 doubles = jail)\n" +
            "• Pass GO to collect $200\n" +
            "• Landing on unowned property: buy or auction\n" +
            "• Must own all properties in a color to build\n" +
            "• Hotels require 4 houses first\n" +
            "• Mortgaged properties don't collect rent\n" +
            "• Last player standing wins!");
    }
    
    @FXML
    private void handleAbout() {
        showInfoDialog("About Monopoly", 
            "Monopoly Online v1.0.0\n\n" +
            "A multiplayer implementation of the classic board game.\n" +
            "Built with JavaFX");
    }
    
    @FXML
    private void handleRollDice() {
        if (!isMyTurn) return;
        
        rollDiceButton.setDisable(true);
        
        // Play dice animation
        diceRollAnimation.setOnFinished(e -> {
            // Get actual dice values from server/game state
            int d1 = (int)(Math.random() * 6) + 1;
            int d2 = (int)(Math.random() * 6) + 1;
            showDiceResult(d1, d2);
            
            // Send roll command to server
            if (client != null) {
                // client.sendRollDice();
            }
        });
        diceRollAnimation.play();
        
        // Add animation to dice
        addDiceShakeAnimation(dice1);
        addDiceShakeAnimation(dice2);
        
        addLogEntry("Rolling dice...", "info");
    }
    
    /**
     * Shows the dice result with animation
     */
    public void showDiceResult(int d1, int d2) {
        dice1Value.setText(String.valueOf(d1));
        dice2Value.setText(String.valueOf(d2));
        
        int total = d1 + d2;
        diceTotal.setText("Total: " + total);
        
        // Check for doubles
        if (d1 == d2) {
            dice1.getStyleClass().add("dice-double");
            dice2.getStyleClass().add("dice-double");
            diceTotal.setText("DOUBLES! " + total);
            addLogEntry("🎲 Rolled doubles: " + d1 + " + " + d2 + " = " + total, "important");
        } else {
            dice1.getStyleClass().remove("dice-double");
            dice2.getStyleClass().remove("dice-double");
            addLogEntry("🎲 Rolled: " + d1 + " + " + d2 + " = " + total, "info");
        }
        
        // Pulse animation on total
        ScaleTransition pulse = new ScaleTransition(Duration.millis(200), diceTotal);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.3);
        pulse.setToY(1.3);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
    }
    
    /**
     * Adds shake animation to dice
     */
    private void addDiceShakeAnimation(StackPane dice) {
        RotateTransition rotate = new RotateTransition(Duration.millis(100), dice);
        rotate.setByAngle(15);
        rotate.setCycleCount(10);
        rotate.setAutoReverse(true);
        rotate.play();
    }
    
    @FXML
    private void handleBuyProperty() {
        if (!isMyTurn) return;
        addLogEntry("Buying property...", "info");
        // client.sendBuyProperty();
    }
    
    @FXML
    private void handleStartAuction() {
        if (!isMyTurn) return;
        addLogEntry("Starting auction...", "info");
        // client.sendStartAuction();
    }
    
    @FXML
    private void handleBuildHouse() {
        if (!isMyTurn) return;
        // Show property selection dialog
        addLogEntry("Select a property to build a house on.", "info");
    }
    
    @FXML
    private void handleBuildHotel() {
        if (!isMyTurn) return;
        addLogEntry("Select a property to build a hotel on.", "info");
    }
    
    @FXML
    private void handleMortgage() {
        addLogEntry("Select a property to mortgage.", "info");
    }
    
    @FXML
    private void handleUnmortgage() {
        addLogEntry("Select a property to unmortgage.", "info");
    }
    
    @FXML
    private void handleTrade() {
        addLogEntry("Opening trade dialog...", "info");
        // Show trade dialog
    }
    
    @FXML
    private void handleViewProperties() {
        addLogEntry("Viewing properties...", "info");
        // Show properties dialog
    }
    
    @FXML
    private void handlePayJailFine() {
        if (!isMyTurn) return;
        addLogEntry("Paying $50 jail fine...", "info");
        // client.sendPayJailFine();
    }
    
    @FXML
    private void handleUseJailCard() {
        if (!isMyTurn) return;
        addLogEntry("Using Get Out of Jail Free card...", "info");
        // client.sendUseJailCard();
    }
    
    @FXML
    private void handleEndTurn() {
        if (!isMyTurn) return;
        addLogEntry("Ending turn...", "info");
        endTurnButton.setDisable(true);
        // client.sendEndTurn();
    }
    
    // ==================== UI Update Methods ====================
    
    /**
     * Updates the game state and refreshes UI
     */
    public void onGameStateUpdate(GameState state) {
        this.currentGameState = state;
        Platform.runLater(() -> {
            updatePlayerPanels();
            updateCurrentTurnInfo();
            updateActionButtons();
            boardController.updateBoard(state);
        });
    }
    
    /**
     * Updates player panels on the left side
     */
    private void updatePlayerPanels() {
        if (currentGameState == null) return;
        
        playerCardsContainer.getChildren().clear();
        
        // Get players from HashTable
        for (Player player : currentGameState.getPlayers().values()) {
            VBox playerCard = createPlayerCard(player);
            playerCardsContainer.getChildren().add(playerCard);
        }
    }
    
    /**
     * Creates a player card UI element
     */
    private VBox createPlayerCard(Player player) {
        VBox card = new VBox(8);
        card.getStyleClass().add("player-card");
        card.setPadding(new Insets(12));
        
        // Check if current turn
        if (currentGameState != null && 
            currentGameState.getCurrentPlayer() != null &&
            currentGameState.getCurrentPlayer().getId() == player.getId()) {
            card.getStyleClass().add("player-card-active");
        }
        
        // Check if bankrupt
        if (player.isBankrupt()) {
            card.getStyleClass().add("player-card-bankrupt");
        }
        
        // Header with token and name
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Circle tokenCircle = new Circle(18);
        tokenCircle.setFill(getTokenColor(player.getTokenType()));
        tokenCircle.getStyleClass().add("player-token");
        
        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(player.getName());
        nameLabel.getStyleClass().add("player-name");
        
        Label moneyLabel = new Label("$" + player.getMoney());
        moneyLabel.getStyleClass().add("player-money");
        if (player.getMoney() < 0) {
            moneyLabel.getStyleClass().add("player-money-negative");
        }
        
        nameBox.getChildren().addAll(nameLabel, moneyLabel);
        header.getChildren().addAll(tokenCircle, nameBox);
        
        // Properties count
        Label propsLabel = new Label("🏠 " + player.getOwnedProperties().size() + " properties");
        propsLabel.getStyleClass().add("player-properties-count");
        
        // Jail status
        if (player.isInJail()) {
            Label jailLabel = new Label("🔒 IN JAIL");
            jailLabel.getStyleClass().add("jail-badge");
            card.getChildren().addAll(header, propsLabel, jailLabel);
        } else {
            card.getChildren().addAll(header, propsLabel);
        }
        
        return card;
    }
    
    /**
     * Gets the color for a player token
     */
    private Color getTokenColor(com.monopoly.model.player.TokenType token) {
        if (token == null) return Color.GRAY;
        switch (token) {
            case CAR: return Color.web("#e94560");
            case DOG: return Color.web("#00d9ff");
            case HAT: return Color.web("#ffd700");
            case SHIP: return Color.web("#00c853");
            case BOOT: return Color.web("#ff9800");
            case THIMBLE: return Color.web("#9c27b0");
            case WHEELBARROW: return Color.web("#795548");
            case CAT: return Color.web("#607d8b");
            default: return Color.GRAY;
        }
    }
    
    /**
     * Updates the current turn information display
     */
    private void updateCurrentTurnInfo() {
        if (currentGameState == null) return;
        
        Player current = currentGameState.getCurrentPlayer();
        if (current != null) {
            currentPlayerName.setText(current.getName());
            currentPlayerMoney.setText("$" + current.getMoney());
            currentPlayerToken.setFill(getTokenColor(current.getTokenType()));
            
            isMyTurn = (current.getId() == myPlayerId);
            turnInfoLabel.setText("Turn: " + current.getName());
            
            // Update jail actions visibility
            if (current.isInJail() && isMyTurn) {
                jailActionsPane.setVisible(true);
                jailActionsPane.setManaged(true);
                jailTurnsLabel.setText("Turns in jail: " + current.getTurnsInJail() + "/3");
            } else {
                jailActionsPane.setVisible(false);
                jailActionsPane.setManaged(false);
            }
        }
    }
    
    /**
     * Updates action button states
     */
    private void updateActionButtons() {
        boolean enabled = isMyTurn && currentGameState != null;
        
        rollDiceButton.setDisable(!enabled);
        buyPropertyButton.setDisable(!enabled);
        auctionButton.setDisable(!enabled);
        buildHouseButton.setDisable(!enabled);
        buildHotelButton.setDisable(!enabled);
        endTurnButton.setDisable(!enabled);
        
        // These can be used anytime during your turn
        mortgageButton.setDisable(!enabled);
        unmortgageButton.setDisable(!enabled);
        tradeButton.setDisable(!enabled);
        viewPropertiesButton.setDisable(false); // Always available
    }
    
    /**
     * Disables all action buttons
     */
    private void disableAllActions() {
        rollDiceButton.setDisable(true);
        buyPropertyButton.setDisable(true);
        auctionButton.setDisable(true);
        buildHouseButton.setDisable(true);
        buildHotelButton.setDisable(true);
        mortgageButton.setDisable(true);
        unmortgageButton.setDisable(true);
        tradeButton.setDisable(true);
        endTurnButton.setDisable(true);
        payJailFineButton.setDisable(true);
        useJailCardButton.setDisable(true);
    }
    
    /**
     * Updates connection status indicator
     */
    public void updateConnectionStatus(boolean connected) {
        Platform.runLater(() -> {
            if (connected) {
                connectionIndicator.getStyleClass().remove("status-disconnected");
                connectionIndicator.getStyleClass().add("status-connected");
                connectionStatus.setText("Connected");
            } else {
                connectionIndicator.getStyleClass().remove("status-connected");
                connectionIndicator.getStyleClass().add("status-disconnected");
                connectionStatus.setText("Disconnected");
            }
        });
    }
    
    /**
     * Shows a card (Chance/Community Chest) with animation
     */
    public void showCard(String type, String text) {
        Platform.runLater(() -> {
            cardTypeLabel.setText(type.toUpperCase());
            cardTextLabel.setText(text);
            
            cardDisplayPane.setVisible(true);
            cardDisplayPane.setManaged(true);
            
            // Fade in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), cardDisplayPane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            
            // Auto hide after 4 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(4));
            pause.setOnFinished(e -> hideCard());
            pause.play();
            
            addLogEntry("📋 " + type + ": " + text, "important");
        });
    }
    
    /**
     * Hides the card display
     */
    private void hideCard() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), cardDisplayPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            cardDisplayPane.setVisible(false);
            cardDisplayPane.setManaged(false);
        });
        fadeOut.play();
    }
    
    /**
     * Adds a log entry
     */
    public void addLogEntry(String message, String type) {
        Platform.runLater(() -> {
            HBox entry = new HBox(10);
            entry.getStyleClass().add("log-entry");
            
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            Label timeLabel = new Label("[" + time + "]");
            timeLabel.getStyleClass().add("log-timestamp");
            
            Label msgLabel = new Label(message);
            msgLabel.getStyleClass().add("log-message");
            
            switch (type) {
                case "important":
                    msgLabel.getStyleClass().add("log-message-important");
                    break;
                case "error":
                    msgLabel.getStyleClass().add("log-message-error");
                    break;
                case "success":
                    msgLabel.getStyleClass().add("log-message-success");
                    break;
            }
            
            entry.getChildren().addAll(timeLabel, msgLabel);
            logEntriesContainer.getChildren().add(entry);
            
            // Keep only last 100 entries
            if (logEntriesContainer.getChildren().size() > 100) {
                logEntriesContainer.getChildren().remove(0);
            }
        });
    }
    
    /**
     * Shows an info dialog
     */
    private void showInfoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Apply dark theme to dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1a1a2e;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #b0b0b0;");
        
        alert.showAndWait();
    }
    
    /**
     * Shows winner dialog
     */
    public void showWinnerDialog(String winnerName) {
        Platform.runLater(() -> {
            gameStatusLabel.setText("🏆 " + winnerName + " WINS!");
            gameStatusLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 24px; -fx-font-weight: bold;");
            
            addLogEntry("🏆 GAME OVER! " + winnerName + " wins!", "important");
            disableAllActions();
        });
    }
    
    /**
     * Called when game starts
     */
    public void onGameStarted() {
        Platform.runLater(() -> {
            gameStatusLabel.setText("Game in progress");
            addLogEntry("🎮 Game has started!", "success");
        });
    }
    
    /**
     * Sets room info label
     */
    public void setRoomInfo(String roomId, int playerCount, int maxPlayers) {
        Platform.runLater(() -> {
            roomInfoLabel.setText("Room: " + roomId + " (" + playerCount + "/" + maxPlayers + ")");
        });
    }
    
    // Reference to main app
    private com.monopoly.gui.MainApp mainApp;
    
    /**
     * Sets the main application reference
     */
    public void setMainApp(com.monopoly.gui.MainApp mainApp) {
        this.mainApp = mainApp;
    }
    
    /**
     * Gets the main application reference
     */
    public com.monopoly.gui.MainApp getMainApp() {
        return mainApp;
    }
    
    /**
     * Opens trade dialog
     */
    public void openTradeDialog() {
        if (mainApp != null) {
            mainApp.showTradeDialog();
        }
    }
    
    /**
     * Opens auction dialog
     */
    public void openAuctionDialog() {
        if (mainApp != null) {
            mainApp.showAuctionDialog();
        }
    }
}
