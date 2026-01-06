package com.monopoly.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

import com.monopoly.gui.controllers.MainController;
import com.monopoly.gui.controllers.ConnectionDialogController;
import com.monopoly.client.Client;

/**
 * Main JavaFX Application class.
 * Entry point for the Monopoly client GUI.
 * Beautiful dark-themed modern interface.
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private Scene mainScene;
    private MainController mainController;
    private Client client;
    
    // CSS stylesheet path
    private static final String DARK_THEME_CSS = "/css/dark-theme.css";
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Configure primary stage
        primaryStage.setTitle("🎲 Monopoly - The Classic Board Game");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        
        // Try to load app icon
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/monopoly_icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        
        // Show splash/connection dialog first
        showConnectionDialog();
    }
    
    /**
     * Shows connection dialog for server address input
     */
    public void showConnectionDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/connection_dialog.fxml"));
            Parent root = loader.load();
            
            ConnectionDialogController controller = loader.getController();
            
            // Set up callbacks
            controller.setOnConnectSuccess(() -> {
                // Transition to main game
                showMainGame();
            });
            
            controller.setOnHostGame(() -> {
                // Start server and then show main game
                startLocalServer();
                showMainGame();
            });
            
            // Create and style scene
            Scene scene = new Scene(root, 500, 600);
            applyDarkTheme(scene);
            
            // Configure stage for connection dialog
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            
            // Fade in animation
            root.setOpacity(0);
            primaryStage.show();
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            
        } catch (IOException e) {
            showError("Failed to load connection dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Starts a local server for hosting games
     */
    private void startLocalServer() {
        // TODO: Start server in background thread
        // Server server = new Server(8888);
        // new Thread(server::start).start();
    }
    
    /**
     * Switches to main game view
     */
    public void showMainGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            
            mainController = loader.getController();
            mainController.setMainApp(this);
            
            // Create and style scene
            mainScene = new Scene(root, 1400, 900);
            applyDarkTheme(mainScene);
            
            // Enable window resizing
            primaryStage.setResizable(true);
            
            // Fade transition from connection to main game
            root.setOpacity(0);
            primaryStage.setScene(mainScene);
            primaryStage.centerOnScreen();
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            
        } catch (IOException e) {
            showError("Failed to load main game: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows trade dialog
     */
    public void showTradeDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/trade_dialog.fxml"));
            Parent root = loader.load();
            
            // Controller is auto-loaded from FXML
            // TODO: Set up controller with game state
            
            Stage dialogStage = createDialogStage("Propose Trade", root, 700, 550);
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            showError("Failed to load trade dialog: " + e.getMessage());
        }
    }
    
    /**
     * Shows auction dialog
     */
    public void showAuctionDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auction_dialog.fxml"));
            Parent root = loader.load();
            
            // Controller is auto-loaded from FXML
            // TODO: Set up controller with auction data
            
            Stage dialogStage = createDialogStage("Property Auction", root, 500, 600);
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            showError("Failed to load auction dialog: " + e.getMessage());
        }
    }
    
    /**
     * Creates a styled dialog stage
     */
    private Stage createDialogStage(String title, Parent root, double width, double height) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.setResizable(false);
        
        Scene scene = new Scene(root, width, height);
        applyDarkTheme(scene);
        
        dialogStage.setScene(scene);
        dialogStage.centerOnScreen();
        
        return dialogStage;
    }
    
    /**
     * Applies dark theme CSS to a scene
     */
    private void applyDarkTheme(Scene scene) {
        URL cssUrl = getClass().getResource(DARK_THEME_CSS);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        
        // Set dark background color as fallback
        scene.setFill(Color.web("#0f0f23"));
    }
    
    /**
     * Shows error dialog with dark theme
     */
    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            styleAlert(alert);
            alert.showAndWait();
        });
    }
    
    /**
     * Shows info dialog with dark theme
     */
    public void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            styleAlert(alert);
            alert.showAndWait();
        });
    }
    
    /**
     * Shows confirmation dialog with dark theme
     */
    public boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    /**
     * Applies dark theme styling to alerts
     */
    private void styleAlert(Alert alert) {
        alert.initOwner(primaryStage);
        
        // Apply dark theme to dialog
        try {
            URL cssUrl = getClass().getResource(DARK_THEME_CSS);
            if (cssUrl != null) {
                alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }
            alert.getDialogPane().setStyle(
                "-fx-background-color: #1a1a2e; " +
                "-fx-text-fill: #ffffff;"
            );
        } catch (Exception ignored) {}
    }
    
    /**
     * Gets the main controller
     */
    public MainController getMainController() {
        return mainController;
    }
    
    /**
     * Gets the primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Sets the client instance
     */
    public void setClient(Client client) {
        this.client = client;
    }
    
    /**
     * Gets the client instance
     */
    public Client getClient() {
        return client;
    }
    
    @Override
    public void stop() {
        // Cleanup on close
        if (client != null) {
            // TODO: Disconnect client
        }
        Platform.exit();
        System.exit(0);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
