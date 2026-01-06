package com.monopoly.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

import com.monopoly.client.Client;
import com.monopoly.model.player.PlayerToken;

/**
 * Controller for the connection dialog.
 * Beautiful dark-themed connection screen with animations.
 */
public class ConnectionDialogController implements Initializable {

    @FXML private TextField playerNameField;
    @FXML private TextField serverAddressField;
    @FXML private TextField portField;
    @FXML private HBox tokenSelectionBox;
    @FXML private ToggleButton tokenCar, tokenDog, tokenHat, tokenShip, tokenBoot;
    @FXML private Label errorLabel;
    @FXML private Button connectButton;
    @FXML private Button hostButton;
    @FXML private HBox loadingBox;
    @FXML private Label loadingLabel;
    
    private Client client;
    private Runnable onConnectSuccess;
    private Runnable onHostGame;
    private ToggleGroup tokenGroup;
    private PlayerToken selectedToken = PlayerToken.CAR;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up token selection toggle group
        tokenGroup = new ToggleGroup();
        if (tokenCar != null) tokenCar.setToggleGroup(tokenGroup);
        if (tokenDog != null) tokenDog.setToggleGroup(tokenGroup);
        if (tokenHat != null) tokenHat.setToggleGroup(tokenGroup);
        if (tokenShip != null) tokenShip.setToggleGroup(tokenGroup);
        if (tokenBoot != null) tokenBoot.setToggleGroup(tokenGroup);
        
        // Select car by default
        if (tokenCar != null) tokenCar.setSelected(true);
        
        // Token selection listener
        tokenGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ToggleButton selected = (ToggleButton) newVal;
                String tokenName = (String) selected.getUserData();
                selectedToken = PlayerToken.valueOf(tokenName);
                
                // Update button styles
                updateTokenButtonStyles();
            }
        });
        
        // Input validation
        playerNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearError();
            validateInput();
        });
        
        serverAddressField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearError();
        });
        
        portField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Only allow numbers
            if (!newVal.matches("\\d*")) {
                portField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            clearError();
        });
    }
    
    /**
     * Updates token button visual styles
     */
    private void updateTokenButtonStyles() {
        String selectedStyle = "-fx-font-size: 24px; -fx-min-width: 50; -fx-min-height: 50; " +
                              "-fx-background-color: #e94560; -fx-background-radius: 10; " +
                              "-fx-border-color: #ffffff; -fx-border-width: 2; -fx-border-radius: 10;";
        String normalStyle = "-fx-font-size: 24px; -fx-min-width: 50; -fx-min-height: 50; " +
                            "-fx-background-color: #1f3460; -fx-background-radius: 10;";
        
        ToggleButton[] buttons = {tokenCar, tokenDog, tokenHat, tokenShip, tokenBoot};
        for (ToggleButton btn : buttons) {
            if (btn != null) {
                btn.setStyle(btn.isSelected() ? selectedStyle : normalStyle);
            }
        }
    }
    
    /**
     * Validates input fields
     */
    private boolean validateInput() {
        String name = playerNameField.getText().trim();
        String address = serverAddressField.getText().trim();
        String port = portField.getText().trim();
        
        boolean valid = !name.isEmpty() && !address.isEmpty() && !port.isEmpty();
        connectButton.setDisable(!valid);
        
        return valid;
    }
    
    /**
     * Handles connect button click
     */
    @FXML
    private void handleConnect() {
        if (!validateInput()) {
            showError("Please fill in all fields");
            return;
        }
        
        String name = playerNameField.getText().trim();
        String address = serverAddressField.getText().trim();
        int port;
        
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Invalid port number");
            return;
        }
        
        if (port < 1 || port > 65535) {
            showError("Port must be between 1 and 65535");
            return;
        }
        
        // Show loading state
        showLoading("Connecting to " + address + ":" + port + "...");
        
        // Connect in background thread
        new Thread(() -> {
            try {
                // Simulate connection delay for now
                Thread.sleep(1000);
                
                // TODO: Actually connect to server
                // client = new Client(address, port);
                // client.connect();
                // client.setPlayerName(name);
                // client.setToken(selectedToken);
                
                Platform.runLater(() -> {
                    hideLoading();
                    if (onConnectSuccess != null) {
                        onConnectSuccess.run();
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Failed to connect: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Handles host game button click
     */
    @FXML
    private void handleHostGame() {
        if (playerNameField.getText().trim().isEmpty()) {
            showError("Please enter your name");
            return;
        }
        
        showLoading("Starting server...");
        
        new Thread(() -> {
            try {
                Thread.sleep(500);
                
                // TODO: Start server and connect
                
                Platform.runLater(() -> {
                    hideLoading();
                    if (onHostGame != null) {
                        onHostGame.run();
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideLoading();
                    showError("Failed to start server: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Shows loading state
     */
    private void showLoading(String message) {
        loadingLabel.setText(message);
        loadingBox.setVisible(true);
        loadingBox.setManaged(true);
        connectButton.setDisable(true);
        hostButton.setDisable(true);
        clearError();
    }
    
    /**
     * Hides loading state
     */
    private void hideLoading() {
        loadingBox.setVisible(false);
        loadingBox.setManaged(false);
        connectButton.setDisable(false);
        hostButton.setDisable(false);
    }
    
    /**
     * Shows an error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        
        // Shake animation for error
        javafx.animation.TranslateTransition shake = new javafx.animation.TranslateTransition(
            javafx.util.Duration.millis(50), errorLabel);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
    
    /**
     * Clears error message
     */
    private void clearError() {
        errorLabel.setText("");
    }
    
    /**
     * Sets callback for successful connection
     */
    public void setOnConnectSuccess(Runnable callback) {
        this.onConnectSuccess = callback;
    }
    
    /**
     * Sets callback for hosting a game
     */
    public void setOnHostGame(Runnable callback) {
        this.onHostGame = callback;
    }
    
    /**
     * Gets the entered player name
     */
    public String getPlayerName() {
        return playerNameField.getText().trim();
    }
    
    /**
     * Gets the selected token
     */
    public PlayerToken getSelectedToken() {
        return selectedToken;
    }
    
    /**
     * Gets the server address
     */
    public String getServerAddress() {
        return serverAddressField.getText().trim();
    }
    
    /**
     * Gets the server port
     */
    public int getServerPort() {
        try {
            return Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            return 8888;
        }
    }
}
