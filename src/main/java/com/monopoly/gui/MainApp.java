package com.monopoly.gui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main JavaFX Application class.
 * Entry point for the Monopoly client GUI.
 */
public class MainApp extends Application {

    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Monopoly");
        // TODO: Load main.fxml
        // TODO: Set up scene and stage
        // TODO: Show connection dialog first
        primaryStage.show();
    }
    
    @Override
    public void stop() {
        // Cleanup on close
    }
    
    /**
     * Shows connection dialog for server address input
     */
    public void showConnectionDialog() {
        // TODO: Implement
    }
    
    /**
     * Switches to game view
     */
    public void showMainGame() {
        // TODO: Implement
    }
    
    /**
     * Shows error dialog
     */
    public void showError(String message) {
        // TODO: Implement
    }
    
    /**
     * Shows info dialog
     */
    public void showInfo(String message) {
        // TODO: Implement
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
