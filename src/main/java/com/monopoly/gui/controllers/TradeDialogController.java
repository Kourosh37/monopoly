package com.monopoly.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.net.URL;
import java.util.*;

import com.monopoly.client.Client;
import com.monopoly.model.Game;
import com.monopoly.model.player.Player;
import com.monopoly.model.property.Property;
import com.monopoly.protocol.TradeProposal;

/**
 * Controller for the trade dialog.
 * Beautiful dark-themed trade interface with drag-drop style interaction.
 */
public class TradeDialogController implements Initializable {

    @FXML private ComboBox<String> targetPlayerComboBox;
    @FXML private VBox myPropertiesBox;
    @FXML private VBox theirPropertiesBox;
    @FXML private VBox offeredPropertiesBox;
    @FXML private VBox requestedPropertiesBox;
    @FXML private Spinner<Integer> offeredMoneySpinner;
    @FXML private Spinner<Integer> requestedMoneySpinner;
    @FXML private Label myMoneyLabel;
    @FXML private Label theirMoneyLabel;
    @FXML private Label tradeStatusLabel;
    @FXML private Button proposeButton;
    @FXML private Button cancelButton;
    @FXML private Button acceptButton;
    @FXML private Button declineButton;
    @FXML private HBox responseButtonsBox;
    @FXML private HBox proposeButtonsBox;
    
    private Client client;
    private Game game;
    private Player myPlayer;
    private Player targetPlayer;
    private List<Property> offeredProperties = new ArrayList<>();
    private List<Property> requestedProperties = new ArrayList<>();
    private TradeProposal incomingProposal;
    private Runnable onTradeComplete;
    
    // Property card colors
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
        // Setup money spinners
        if (offeredMoneySpinner != null) {
            offeredMoneySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0, 50));
        }
        if (requestedMoneySpinner != null) {
            requestedMoneySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0, 50));
        }
        
        // Target player selection listener
        if (targetPlayerComboBox != null) {
            targetPlayerComboBox.setOnAction(e -> onTargetPlayerSelected());
        }
        
        // Initially hide response buttons
        if (responseButtonsBox != null) {
            responseButtonsBox.setVisible(false);
            responseButtonsBox.setManaged(false);
        }
    }
    
    /**
     * Sets up the trade dialog for proposing a trade
     */
    public void setupForProposal(Client client, Game game, Player myPlayer) {
        this.client = client;
        this.game = game;
        this.myPlayer = myPlayer;
        
        // Show propose buttons, hide response buttons
        if (proposeButtonsBox != null) {
            proposeButtonsBox.setVisible(true);
            proposeButtonsBox.setManaged(true);
        }
        if (responseButtonsBox != null) {
            responseButtonsBox.setVisible(false);
            responseButtonsBox.setManaged(false);
        }
        
        // Populate target players
        populateTargetPlayers();
        
        // Populate my properties
        populateMyProperties();
        
        // Update money label
        if (myMoneyLabel != null) {
            myMoneyLabel.setText("$" + myPlayer.getMoney());
        }
        
        // Update spinner max
        if (offeredMoneySpinner != null) {
            ((SpinnerValueFactory.IntegerSpinnerValueFactory) offeredMoneySpinner.getValueFactory())
                .setMax(myPlayer.getMoney());
        }
    }
    
    /**
     * Sets up the trade dialog for responding to an incoming trade
     */
    public void setupForResponse(Client client, Game game, Player myPlayer, TradeProposal proposal) {
        this.client = client;
        this.game = game;
        this.myPlayer = myPlayer;
        this.incomingProposal = proposal;
        
        // Hide propose buttons, show response buttons
        if (proposeButtonsBox != null) {
            proposeButtonsBox.setVisible(false);
            proposeButtonsBox.setManaged(false);
        }
        if (responseButtonsBox != null) {
            responseButtonsBox.setVisible(true);
            responseButtonsBox.setManaged(true);
        }
        
        // Disable editing
        if (targetPlayerComboBox != null) {
            targetPlayerComboBox.setDisable(true);
        }
        if (offeredMoneySpinner != null) {
            offeredMoneySpinner.setDisable(true);
        }
        if (requestedMoneySpinner != null) {
            requestedMoneySpinner.setDisable(true);
        }
        
        // Show incoming trade details
        displayIncomingTrade(proposal);
    }
    
    /**
     * Populates the target player dropdown
     */
    private void populateTargetPlayers() {
        if (targetPlayerComboBox == null || game == null) return;
        
        ObservableList<String> players = FXCollections.observableArrayList();
        for (Player p : game.getPlayers()) {
            if (!p.equals(myPlayer) && !p.isBankrupt()) {
                players.add(p.getName() + " (ID: " + p.getId() + ")");
            }
        }
        targetPlayerComboBox.setItems(players);
    }
    
    /**
     * Populates my properties list
     */
    private void populateMyProperties() {
        if (myPropertiesBox == null || myPlayer == null) return;
        
        myPropertiesBox.getChildren().clear();
        
        for (Property prop : myPlayer.getProperties()) {
            Button propButton = createPropertyButton(prop, true);
            myPropertiesBox.getChildren().add(propButton);
        }
    }
    
    /**
     * Called when target player is selected
     */
    private void onTargetPlayerSelected() {
        if (targetPlayerComboBox == null || game == null) return;
        
        String selected = targetPlayerComboBox.getValue();
        if (selected == null) return;
        
        // Parse player ID from selection
        int startIdx = selected.lastIndexOf("ID: ") + 4;
        int endIdx = selected.lastIndexOf(")");
        if (startIdx > 3 && endIdx > startIdx) {
            int playerId = Integer.parseInt(selected.substring(startIdx, endIdx));
            targetPlayer = game.getPlayerById(playerId);
            
            if (targetPlayer != null) {
                populateTheirProperties();
                
                // Update their money label
                if (theirMoneyLabel != null) {
                    theirMoneyLabel.setText("$" + targetPlayer.getMoney());
                }
                
                // Update request spinner max
                if (requestedMoneySpinner != null) {
                    ((SpinnerValueFactory.IntegerSpinnerValueFactory) requestedMoneySpinner.getValueFactory())
                        .setMax(targetPlayer.getMoney());
                }
            }
        }
    }
    
    /**
     * Populates target player's properties
     */
    private void populateTheirProperties() {
        if (theirPropertiesBox == null || targetPlayer == null) return;
        
        theirPropertiesBox.getChildren().clear();
        
        for (Property prop : targetPlayer.getProperties()) {
            Button propButton = createPropertyButton(prop, false);
            theirPropertiesBox.getChildren().add(propButton);
        }
    }
    
    /**
     * Creates a property button for trade lists
     */
    private Button createPropertyButton(Property prop, boolean isMyProperty) {
        Button btn = new Button(prop.getName());
        btn.getStyleClass().add("trade-property-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        
        // Set color based on property group
        String color = PROPERTY_COLORS.getOrDefault(prop.getGroup(), "#666666");
        btn.setStyle("-fx-background-color: " + color + "33; -fx-border-color: " + color + "; " +
                    "-fx-border-width: 0 0 0 4; -fx-padding: 8;");
        
        // Click to add/remove from offer/request
        btn.setOnAction(e -> {
            if (isMyProperty) {
                if (offeredProperties.contains(prop)) {
                    removeFromOffered(prop);
                } else {
                    addToOffered(prop);
                }
            } else {
                if (requestedProperties.contains(prop)) {
                    removeFromRequested(prop);
                } else {
                    addToRequested(prop);
                }
            }
        });
        
        return btn;
    }
    
    /**
     * Adds a property to offered list
     */
    private void addToOffered(Property prop) {
        if (!offeredProperties.contains(prop)) {
            offeredProperties.add(prop);
            updateOfferedDisplay();
        }
    }
    
    /**
     * Removes a property from offered list
     */
    private void removeFromOffered(Property prop) {
        offeredProperties.remove(prop);
        updateOfferedDisplay();
    }
    
    /**
     * Adds a property to requested list
     */
    private void addToRequested(Property prop) {
        if (!requestedProperties.contains(prop)) {
            requestedProperties.add(prop);
            updateRequestedDisplay();
        }
    }
    
    /**
     * Removes a property from requested list
     */
    private void removeFromRequested(Property prop) {
        requestedProperties.remove(prop);
        updateRequestedDisplay();
    }
    
    /**
     * Updates the offered properties display
     */
    private void updateOfferedDisplay() {
        if (offeredPropertiesBox == null) return;
        
        offeredPropertiesBox.getChildren().clear();
        
        for (Property prop : offeredProperties) {
            Label label = new Label("✓ " + prop.getName());
            label.getStyleClass().add("trade-offered-item");
            label.setStyle("-fx-text-fill: #00c853; -fx-padding: 4;");
            offeredPropertiesBox.getChildren().add(label);
        }
        
        validateTrade();
    }
    
    /**
     * Updates the requested properties display
     */
    private void updateRequestedDisplay() {
        if (requestedPropertiesBox == null) return;
        
        requestedPropertiesBox.getChildren().clear();
        
        for (Property prop : requestedProperties) {
            Label label = new Label("✓ " + prop.getName());
            label.getStyleClass().add("trade-requested-item");
            label.setStyle("-fx-text-fill: #e94560; -fx-padding: 4;");
            requestedPropertiesBox.getChildren().add(label);
        }
        
        validateTrade();
    }
    
    /**
     * Validates the current trade
     */
    private boolean validateTrade() {
        boolean hasOffer = !offeredProperties.isEmpty() || 
                          (offeredMoneySpinner != null && offeredMoneySpinner.getValue() > 0);
        boolean hasRequest = !requestedProperties.isEmpty() || 
                            (requestedMoneySpinner != null && requestedMoneySpinner.getValue() > 0);
        
        boolean valid = hasOffer || hasRequest;
        valid = valid && targetPlayer != null;
        
        if (proposeButton != null) {
            proposeButton.setDisable(!valid);
        }
        
        return valid;
    }
    
    /**
     * Builds a trade proposal from current selections
     */
    private TradeProposal buildTradeProposal() {
        List<Integer> offeredIds = new ArrayList<>();
        for (Property p : offeredProperties) {
            offeredIds.add(p.getId());
        }
        
        List<Integer> requestedIds = new ArrayList<>();
        for (Property p : requestedProperties) {
            requestedIds.add(p.getId());
        }
        
        int offeredMoney = offeredMoneySpinner != null ? offeredMoneySpinner.getValue() : 0;
        int requestedMoney = requestedMoneySpinner != null ? requestedMoneySpinner.getValue() : 0;
        
        return new TradeProposal(
            myPlayer.getId(),
            targetPlayer.getId(),
            offeredIds,
            requestedIds,
            offeredMoney,
            requestedMoney,
            0, 0 // Get out of jail cards
        );
    }
    
    /**
     * Displays an incoming trade proposal
     */
    private void displayIncomingTrade(TradeProposal proposal) {
        // TODO: Parse proposal and display in UI
        if (tradeStatusLabel != null) {
            tradeStatusLabel.setText("Trade offer from Player " + proposal.getFromPlayerId());
        }
    }
    
    /**
     * Handles propose button click
     */
    @FXML
    private void handlePropose() {
        if (!validateTrade()) {
            showError("Invalid trade");
            return;
        }
        
        TradeProposal proposal = buildTradeProposal();
        
        // Send to server
        // TODO: client.sendTradeProposal(proposal);
        
        // Show waiting state
        if (tradeStatusLabel != null) {
            tradeStatusLabel.setText("Waiting for response...");
        }
        if (proposeButton != null) {
            proposeButton.setDisable(true);
        }
        
        // Animation
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(500), proposeButton);
        fade.setFromValue(1.0);
        fade.setToValue(0.5);
        fade.setCycleCount(javafx.animation.Animation.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
    }
    
    /**
     * Handles cancel button click
     */
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    /**
     * Handles accept button click (for incoming trades)
     */
    @FXML
    private void handleAccept() {
        if (incomingProposal != null) {
            // TODO: client.acceptTrade(incomingProposal);
            
            if (tradeStatusLabel != null) {
                tradeStatusLabel.setText("Trade accepted!");
                tradeStatusLabel.setStyle("-fx-text-fill: #00c853;");
            }
            
            // Close after delay
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    Platform.runLater(this::closeDialog);
                } catch (InterruptedException ignored) {}
            }).start();
        }
    }
    
    /**
     * Handles decline button click (for incoming trades)
     */
    @FXML
    private void handleDecline() {
        if (incomingProposal != null) {
            // TODO: client.declineTrade(incomingProposal);
            
            if (tradeStatusLabel != null) {
                tradeStatusLabel.setText("Trade declined");
                tradeStatusLabel.setStyle("-fx-text-fill: #e94560;");
            }
            
            // Close after delay
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(this::closeDialog);
                } catch (InterruptedException ignored) {}
            }).start();
        }
    }
    
    /**
     * Shows an error message
     */
    private void showError(String message) {
        if (tradeStatusLabel != null) {
            tradeStatusLabel.setText(message);
            tradeStatusLabel.setStyle("-fx-text-fill: #e94560;");
        }
    }
    
    /**
     * Closes the dialog
     */
    private void closeDialog() {
        if (cancelButton != null) {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        }
        
        if (onTradeComplete != null) {
            onTradeComplete.run();
        }
    }
    
    /**
     * Sets callback for trade completion
     */
    public void setOnTradeComplete(Runnable callback) {
        this.onTradeComplete = callback;
    }
}
