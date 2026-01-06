package com.monopoly.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.geometry.Pos;

import java.net.URL;
import java.util.*;

import com.monopoly.client.Client;
import com.monopoly.model.Game;
import com.monopoly.model.player.Player;
import com.monopoly.model.property.Property;

/**
 * Controller for the auction dialog.
 * Beautiful dark-themed auction interface with live bidding.
 */
public class AuctionDialogController implements Initializable {

    @FXML private Label propertyNameLabel;
    @FXML private VBox propertyCard;
    @FXML private Rectangle propertyColorBand;
    @FXML private Label propertyPriceLabel;
    @FXML private Label propertyRentLabel;
    @FXML private Label currentBidLabel;
    @FXML private Label highestBidderLabel;
    @FXML private Spinner<Integer> bidAmountSpinner;
    @FXML private Button bidButton;
    @FXML private Button passButton;
    @FXML private VBox biddersBox;
    @FXML private Label timerLabel;
    @FXML private ProgressBar timerProgress;
    @FXML private Label statusLabel;
    @FXML private HBox bidControlsBox;
    
    private Client client;
    private Game game;
    private Player myPlayer;
    private Property auctionProperty;
    private int currentBid = 0;
    private int highestBidderId = -1;
    private int minimumIncrement = 10;
    private boolean hasEnded = false;
    private Runnable onAuctionComplete;
    
    // Bidder tracking
    private Map<Integer, Integer> playerBids = new HashMap<>();
    private Map<Integer, Label> bidderLabels = new HashMap<>();
    
    // Timer
    private Timeline auctionTimer;
    private int timeRemaining = 30; // seconds
    
    // Property colors
    private static final Map<String, String> PROPERTY_COLORS = new HashMap<>();
    static {
        PROPERTY_COLORS.put("BROWN", "#8B4513");
        PROPERTY_COLORS.put("LIGHT_BLUE", "#87CEEB");
        PROPERTY_COLORS.put("PINK", "#FF69B4");
        PROPERTY_COLORS.put("ORANGE", "#FFA500");
        PROPERTY_COLORS.put("RED", "#FF0000");
        PROPERTY_COLORS.put("YELLOW", "#FFFF00");
        PROPERTY_COLORS.put("GREEN", "#00FF00");
        PROPERTY_COLORS.put("DARK_BLUE", "#0000FF");
        PROPERTY_COLORS.put("RAILROAD", "#333333");
        PROPERTY_COLORS.put("UTILITY", "#AAAAAA");
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup bid spinner
        if (bidAmountSpinner != null) {
            bidAmountSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0, minimumIncrement));
            
            // Update spinner when value changes
            bidAmountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                validateBid(newVal);
            });
        }
        
        // Timer progress bar initial state
        if (timerProgress != null) {
            timerProgress.setProgress(1.0);
        }
    }
    
    /**
     * Starts the auction
     */
    public void startAuction(Client client, Game game, Player myPlayer, Property property) {
        this.client = client;
        this.game = game;
        this.myPlayer = myPlayer;
        this.auctionProperty = property;
        this.currentBid = 0;
        this.hasEnded = false;
        
        // Display property info
        displayPropertyInfo();
        
        // Set up bidder list
        setupBidderList();
        
        // Update spinner with player's max money
        if (bidAmountSpinner != null) {
            SpinnerValueFactory.IntegerSpinnerValueFactory factory = 
                (SpinnerValueFactory.IntegerSpinnerValueFactory) bidAmountSpinner.getValueFactory();
            factory.setMax(myPlayer.getMoney());
            factory.setMin(minimumIncrement);
            factory.setValue(minimumIncrement);
        }
        
        // Start timer
        startTimer();
        
        // Animate property card entry
        animatePropertyCardEntry();
    }
    
    /**
     * Displays property information
     */
    private void displayPropertyInfo() {
        if (auctionProperty == null) return;
        
        if (propertyNameLabel != null) {
            propertyNameLabel.setText(auctionProperty.getName());
        }
        
        if (propertyColorBand != null) {
            String color = PROPERTY_COLORS.getOrDefault(auctionProperty.getGroup(), "#666666");
            propertyColorBand.setStyle("-fx-fill: " + color + ";");
        }
        
        if (propertyPriceLabel != null) {
            propertyPriceLabel.setText("List Price: $" + auctionProperty.getPrice());
        }
        
        if (propertyRentLabel != null) {
            propertyRentLabel.setText("Base Rent: $" + auctionProperty.getRent());
        }
        
        if (currentBidLabel != null) {
            currentBidLabel.setText("$0");
        }
        
        if (highestBidderLabel != null) {
            highestBidderLabel.setText("No bids yet");
        }
    }
    
    /**
     * Sets up the bidder list
     */
    private void setupBidderList() {
        if (biddersBox == null || game == null) return;
        
        biddersBox.getChildren().clear();
        bidderLabels.clear();
        
        for (Player p : game.getPlayers()) {
            if (!p.isBankrupt()) {
                HBox bidderRow = new HBox(10);
                bidderRow.setAlignment(Pos.CENTER_LEFT);
                bidderRow.setStyle("-fx-padding: 5; -fx-background-color: #1a1a2e; -fx-background-radius: 5;");
                
                // Player indicator
                Label indicator = new Label("●");
                indicator.setStyle("-fx-text-fill: " + getPlayerColor(p.getId()) + "; -fx-font-size: 14px;");
                
                // Player name
                Label nameLabel = new Label(p.getName());
                nameLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                nameLabel.setMinWidth(100);
                
                // Player's bid
                Label bidLabel = new Label("$0");
                bidLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");
                bidLabel.setMinWidth(60);
                
                bidderLabels.put(p.getId(), bidLabel);
                
                // Player's money
                Label moneyLabel = new Label("(has $" + p.getMoney() + ")");
                moneyLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
                
                bidderRow.getChildren().addAll(indicator, nameLabel, bidLabel, moneyLabel);
                biddersBox.getChildren().add(bidderRow);
            }
        }
    }
    
    /**
     * Gets player color by ID
     */
    private String getPlayerColor(int playerId) {
        String[] colors = {"#e94560", "#00d9ff", "#ffd700", "#00c853", "#ff6b35", "#9c27b0"};
        return colors[playerId % colors.length];
    }
    
    /**
     * Animates the property card entry
     */
    private void animatePropertyCardEntry() {
        if (propertyCard == null) return;
        
        propertyCard.setScaleX(0);
        propertyCard.setScaleY(0);
        propertyCard.setOpacity(0);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(400), propertyCard);
        scale.setFromX(0);
        scale.setFromY(0);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(Interpolator.EASE_OUT);
        
        FadeTransition fade = new FadeTransition(Duration.millis(400), propertyCard);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.play();
    }
    
    /**
     * Starts the auction timer
     */
    private void startTimer() {
        timeRemaining = 30;
        
        auctionTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            updateTimerDisplay();
            
            if (timeRemaining <= 0) {
                endAuction();
            }
        }));
        auctionTimer.setCycleCount(30);
        auctionTimer.play();
    }
    
    /**
     * Updates the timer display
     */
    private void updateTimerDisplay() {
        if (timerLabel != null) {
            timerLabel.setText(timeRemaining + "s");
            
            // Change color as time runs out
            if (timeRemaining <= 5) {
                timerLabel.setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold;");
                // Flash animation
                FadeTransition flash = new FadeTransition(Duration.millis(200), timerLabel);
                flash.setFromValue(1.0);
                flash.setToValue(0.5);
                flash.setAutoReverse(true);
                flash.setCycleCount(2);
                flash.play();
            } else if (timeRemaining <= 10) {
                timerLabel.setStyle("-fx-text-fill: #FFA500; -fx-font-weight: bold;");
            }
        }
        
        if (timerProgress != null) {
            timerProgress.setProgress(timeRemaining / 30.0);
        }
    }
    
    /**
     * Resets timer when a bid is placed
     */
    private void resetTimer() {
        if (auctionTimer != null) {
            auctionTimer.stop();
        }
        timeRemaining = 15; // Shorter time after a bid
        
        if (timerLabel != null) {
            timerLabel.setStyle("-fx-text-fill: #00d9ff;");
        }
        
        auctionTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            updateTimerDisplay();
            
            if (timeRemaining <= 0) {
                endAuction();
            }
        }));
        auctionTimer.setCycleCount(15);
        auctionTimer.play();
    }
    
    /**
     * Validates a bid amount
     */
    private boolean validateBid(int amount) {
        if (myPlayer == null) return false;
        
        boolean valid = amount > currentBid && amount <= myPlayer.getMoney();
        
        if (bidButton != null) {
            bidButton.setDisable(!valid);
        }
        
        return valid;
    }
    
    /**
     * Handles bid button click
     */
    @FXML
    private void handleBid() {
        if (hasEnded || bidAmountSpinner == null || myPlayer == null) return;
        
        int bidAmount = bidAmountSpinner.getValue();
        
        if (!validateBid(bidAmount)) {
            showError("Invalid bid amount");
            return;
        }
        
        // Place the bid
        placeBid(myPlayer.getId(), bidAmount);
        
        // Send to server
        // TODO: client.placeBid(bidAmount);
        
        // Update spinner minimum
        SpinnerValueFactory.IntegerSpinnerValueFactory factory = 
            (SpinnerValueFactory.IntegerSpinnerValueFactory) bidAmountSpinner.getValueFactory();
        factory.setMin(bidAmount + minimumIncrement);
        factory.setValue(bidAmount + minimumIncrement);
        
        // Reset timer
        resetTimer();
        
        // Animate bid
        animateBidPlaced();
    }
    
    /**
     * Records a bid (from any player)
     */
    public void placeBid(int playerId, int amount) {
        if (amount > currentBid) {
            currentBid = amount;
            highestBidderId = playerId;
            playerBids.put(playerId, amount);
            
            // Update UI
            updateBidDisplay(playerId, amount);
        }
    }
    
    /**
     * Updates the bid display
     */
    private void updateBidDisplay(int playerId, int amount) {
        // Update current bid label
        if (currentBidLabel != null) {
            currentBidLabel.setText("$" + amount);
            
            // Animate the label
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), currentBidLabel);
            scale.setFromX(1.2);
            scale.setFromY(1.2);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        }
        
        // Update highest bidder
        if (highestBidderLabel != null && game != null) {
            Player bidder = game.getPlayerById(playerId);
            if (bidder != null) {
                highestBidderLabel.setText(bidder.getName());
                highestBidderLabel.setStyle("-fx-text-fill: " + getPlayerColor(playerId) + ";");
            }
        }
        
        // Update bidder list
        Label bidderLabel = bidderLabels.get(playerId);
        if (bidderLabel != null) {
            bidderLabel.setText("$" + amount);
            bidderLabel.setStyle("-fx-text-fill: #00c853; -fx-font-weight: bold;");
            
            // Flash animation
            FadeTransition flash = new FadeTransition(Duration.millis(200), bidderLabel);
            flash.setFromValue(0.5);
            flash.setToValue(1.0);
            flash.play();
        }
    }
    
    /**
     * Animates when a bid is placed
     */
    private void animateBidPlaced() {
        if (bidButton == null) return;
        
        // Scale animation
        ScaleTransition scale = new ScaleTransition(Duration.millis(100), bidButton);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(0.95);
        scale.setToY(0.95);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }
    
    /**
     * Handles pass button click
     */
    @FXML
    private void handlePass() {
        if (hasEnded) return;
        
        // Disable bidding for this player
        if (bidButton != null) {
            bidButton.setDisable(true);
        }
        if (passButton != null) {
            passButton.setDisable(true);
        }
        if (bidAmountSpinner != null) {
            bidAmountSpinner.setDisable(true);
        }
        
        // Send to server
        // TODO: client.passAuction();
        
        if (statusLabel != null) {
            statusLabel.setText("You passed on this auction");
            statusLabel.setStyle("-fx-text-fill: #888888;");
        }
    }
    
    /**
     * Ends the auction
     */
    private void endAuction() {
        if (hasEnded) return;
        hasEnded = true;
        
        if (auctionTimer != null) {
            auctionTimer.stop();
        }
        
        // Disable controls
        if (bidControlsBox != null) {
            bidControlsBox.setDisable(true);
        }
        
        // Determine winner
        if (highestBidderId >= 0 && game != null) {
            Player winner = game.getPlayerById(highestBidderId);
            showAuctionResult(winner, currentBid);
        } else {
            showNoWinner();
        }
    }
    
    /**
     * Shows the auction result
     */
    private void showAuctionResult(Player winner, int winningBid) {
        if (statusLabel == null) return;
        
        String resultText = winner.getName() + " wins " + auctionProperty.getName() + " for $" + winningBid + "!";
        statusLabel.setText(resultText);
        
        if (winner.getId() == myPlayer.getId()) {
            statusLabel.setStyle("-fx-text-fill: #00c853; -fx-font-weight: bold;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold;");
        }
        
        // Victory animation
        if (propertyCard != null) {
            RotateTransition rotate = new RotateTransition(Duration.millis(500), propertyCard);
            rotate.setByAngle(360);
            rotate.play();
        }
        
        // Close after delay
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(this::closeDialog);
            } catch (InterruptedException ignored) {}
        }).start();
    }
    
    /**
     * Shows when no one bid
     */
    private void showNoWinner() {
        if (statusLabel != null) {
            statusLabel.setText("No bids! Property returned to bank.");
            statusLabel.setStyle("-fx-text-fill: #888888;");
        }
        
        // Fade out property card
        if (propertyCard != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(1000), propertyCard);
            fade.setFromValue(1.0);
            fade.setToValue(0.3);
            fade.play();
        }
        
        // Close after delay
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(this::closeDialog);
            } catch (InterruptedException ignored) {}
        }).start();
    }
    
    /**
     * Shows an error message
     */
    private void showError(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: #e94560;");
            
            // Shake animation
            TranslateTransition shake = new TranslateTransition(Duration.millis(50), statusLabel);
            shake.setFromX(0);
            shake.setByX(10);
            shake.setCycleCount(6);
            shake.setAutoReverse(true);
            shake.play();
        }
    }
    
    /**
     * Closes the dialog
     */
    private void closeDialog() {
        if (auctionTimer != null) {
            auctionTimer.stop();
        }
        
        if (bidButton != null) {
            Stage stage = (Stage) bidButton.getScene().getWindow();
            stage.close();
        }
        
        if (onAuctionComplete != null) {
            onAuctionComplete.run();
        }
    }
    
    /**
     * Sets callback for auction completion
     */
    public void setOnAuctionComplete(Runnable callback) {
        this.onAuctionComplete = callback;
    }
}
