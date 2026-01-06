package com.monopoly.gui.controllers;

import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.application.Platform;

import com.monopoly.model.game.GameState;
import com.monopoly.model.game.Board;
import com.monopoly.model.tile.*;
import com.monopoly.model.player.Player;
import com.monopoly.model.property.Property;
import com.monopoly.model.property.ColorGroup;
import com.monopoly.datastructures.HashTable;

/**
 * Controller for the game board display.
 * Renders the board, tiles, and player tokens with beautiful animations.
 */
public class BoardController {

    private static final int BOARD_SIZE = 11; // 11x11 grid for monopoly board
    private static final int TILE_WIDTH = 65;
    private static final int TILE_HEIGHT = 85;
    private static final int CORNER_SIZE = 85;
    
    private GridPane boardGrid;
    private StackPane[][] tileViews;
    private HashTable<Integer, Circle> playerTokens;
    private HashTable<Integer, Integer> playerPositions;
    private GameState currentGameState;
    
    // Tile data for rendering
    private static final String[] TILE_NAMES = {
        "GO", "Mediterranean Ave", "Community Chest", "Baltic Ave", "Income Tax",
        "Reading Railroad", "Oriental Ave", "Chance", "Vermont Ave", "Connecticut Ave",
        "Jail", "St. Charles Place", "Electric Company", "States Ave", "Virginia Ave",
        "Pennsylvania RR", "St. James Place", "Community Chest", "Tennessee Ave", "New York Ave",
        "Free Parking", "Kentucky Ave", "Chance", "Indiana Ave", "Illinois Ave",
        "B&O Railroad", "Atlantic Ave", "Ventnor Ave", "Water Works", "Marvin Gardens",
        "Go To Jail", "Pacific Ave", "North Carolina Ave", "Community Chest", "Pennsylvania Ave",
        "Short Line", "Chance", "Park Place", "Luxury Tax", "Boardwalk"
    };
    
    private static final int[] TILE_PRICES = {
        0, 60, 0, 60, 200, 200, 100, 0, 100, 120,
        0, 140, 150, 140, 160, 200, 180, 0, 180, 200,
        0, 220, 0, 220, 240, 200, 260, 260, 150, 280,
        0, 300, 300, 0, 320, 200, 0, 350, 100, 400
    };
    
    private static final String[] TILE_COLORS = {
        "corner", "brown", "chest", "brown", "tax",
        "railroad", "light-blue", "chance", "light-blue", "light-blue",
        "corner", "pink", "utility", "pink", "pink",
        "railroad", "orange", "chest", "orange", "orange",
        "corner", "red", "chance", "red", "red",
        "railroad", "yellow", "yellow", "utility", "yellow",
        "corner", "green", "green", "chest", "green",
        "railroad", "chance", "dark-blue", "tax", "dark-blue"
    };
    
    /**
     * Creates a new BoardController with the given grid pane
     */
    public BoardController(GridPane boardGrid) {
        this.boardGrid = boardGrid;
        this.tileViews = new StackPane[40][1];
        this.playerTokens = new HashTable<>();
        this.playerPositions = new HashTable<>();
        
        createBoardLayout();
    }
    
    /**
     * Creates the initial board layout
     */
    private void createBoardLayout() {
        boardGrid.getChildren().clear();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(1);
        boardGrid.setVgap(1);
        
        // Create 11x11 grid
        for (int i = 0; i < BOARD_SIZE; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(i == 0 || i == BOARD_SIZE - 1 ? CORNER_SIZE : TILE_WIDTH);
            col.setMaxWidth(i == 0 || i == BOARD_SIZE - 1 ? CORNER_SIZE : TILE_WIDTH);
            boardGrid.getColumnConstraints().add(col);
            
            RowConstraints row = new RowConstraints();
            row.setMinHeight(i == 0 || i == BOARD_SIZE - 1 ? CORNER_SIZE : TILE_WIDTH);
            row.setMaxHeight(i == 0 || i == BOARD_SIZE - 1 ? CORNER_SIZE : TILE_WIDTH);
            boardGrid.getRowConstraints().add(row);
        }
        
        // Create tiles
        // Bottom row (GO to Jail) - positions 0-10
        for (int i = 0; i <= 10; i++) {
            StackPane tile = createTile(i);
            tileViews[i][0] = tile;
            boardGrid.add(tile, BOARD_SIZE - 1 - i, BOARD_SIZE - 1);
        }
        
        // Left column (St. Charles to Free Parking) - positions 11-20
        for (int i = 11; i <= 20; i++) {
            StackPane tile = createTile(i);
            tileViews[i][0] = tile;
            boardGrid.add(tile, 0, BOARD_SIZE - 1 - (i - 10));
        }
        
        // Top row (Kentucky to Go To Jail) - positions 21-30
        for (int i = 21; i <= 30; i++) {
            StackPane tile = createTile(i);
            tileViews[i][0] = tile;
            boardGrid.add(tile, i - 20, 0);
        }
        
        // Right column (Pacific to Boardwalk) - positions 31-39
        for (int i = 31; i <= 39; i++) {
            StackPane tile = createTile(i);
            tileViews[i][0] = tile;
            boardGrid.add(tile, BOARD_SIZE - 1, i - 30);
        }
    }
    
    /**
     * Creates a single tile view
     */
    private StackPane createTile(int position) {
        StackPane tile = new StackPane();
        tile.getStyleClass().add("tile");
        
        boolean isCorner = (position == 0 || position == 10 || position == 20 || position == 30);
        
        if (isCorner) {
            tile.getStyleClass().add("tile-corner");
            tile.setMinSize(CORNER_SIZE, CORNER_SIZE);
            tile.setMaxSize(CORNER_SIZE, CORNER_SIZE);
        } else {
            tile.setMinSize(TILE_WIDTH, TILE_HEIGHT);
            tile.setMaxSize(TILE_WIDTH, TILE_HEIGHT);
        }
        
        VBox content = new VBox(2);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(3));
        
        String tileColor = TILE_COLORS[position];
        String tileName = TILE_NAMES[position];
        int price = TILE_PRICES[position];
        
        // Add color band for properties
        if (isPropertyColor(tileColor)) {
            Rectangle colorBand = new Rectangle();
            colorBand.setWidth(isCorner ? CORNER_SIZE - 6 : TILE_WIDTH - 6);
            colorBand.setHeight(16);
            colorBand.setFill(getColorForGroup(tileColor));
            colorBand.setArcWidth(3);
            colorBand.setArcHeight(3);
            content.getChildren().add(colorBand);
        }
        
        // Tile icon for special tiles
        if (tileColor.equals("corner") || tileColor.equals("chance") || 
            tileColor.equals("chest") || tileColor.equals("tax") ||
            tileColor.equals("railroad") || tileColor.equals("utility")) {
            Label icon = new Label(getTileIcon(position));
            icon.setStyle("-fx-font-size: " + (isCorner ? "24" : "16") + "px;");
            content.getChildren().add(icon);
        }
        
        // Tile name
        Label nameLabel = new Label(getShortName(tileName));
        nameLabel.getStyleClass().add("tile-name");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(isCorner ? CORNER_SIZE - 10 : TILE_WIDTH - 10);
        content.getChildren().add(nameLabel);
        
        // Price for purchasable properties
        if (price > 0 && !tileColor.equals("tax")) {
            Label priceLabel = new Label("$" + price);
            priceLabel.getStyleClass().add("tile-price");
            content.getChildren().add(priceLabel);
        }
        
        // House/hotel indicator container
        HBox buildingIndicators = new HBox(2);
        buildingIndicators.setAlignment(Pos.CENTER);
        buildingIndicators.setId("buildings-" + position);
        content.getChildren().add(buildingIndicators);
        
        tile.getChildren().add(content);
        
        // Add style class for special tiles
        addSpecialTileStyle(tile, position, tileColor);
        
        // Tooltip with full info
        Tooltip tooltip = new Tooltip(tileName + (price > 0 ? "\nPrice: $" + price : ""));
        tooltip.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: white; -fx-font-size: 12px;");
        Tooltip.install(tile, tooltip);
        
        // Hover effect
        tile.setOnMouseEntered(e -> {
            tile.setEffect(new Glow(0.3));
            ScaleTransition st = new ScaleTransition(Duration.millis(100), tile);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        
        tile.setOnMouseExited(e -> {
            tile.setEffect(null);
            ScaleTransition st = new ScaleTransition(Duration.millis(100), tile);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        return tile;
    }
    
    /**
     * Returns shortened tile name for display
     */
    private String getShortName(String name) {
        if (name.length() <= 12) return name;
        // Abbreviate common words
        return name.replace("Avenue", "Ave")
                   .replace("Place", "Pl")
                   .replace("Railroad", "RR")
                   .replace("Company", "Co");
    }
    
    /**
     * Gets icon for special tiles
     */
    private String getTileIcon(int position) {
        switch (position) {
            case 0: return "➡️"; // GO
            case 10: return "🔒"; // Jail
            case 20: return "🅿️"; // Free Parking
            case 30: return "👮"; // Go To Jail
            case 2: case 17: case 33: return "📦"; // Community Chest
            case 7: case 22: case 36: return "❓"; // Chance
            case 4: return "💰"; // Income Tax
            case 38: return "💎"; // Luxury Tax
            case 5: case 15: case 25: case 35: return "🚂"; // Railroads
            case 12: return "💡"; // Electric Company
            case 28: return "💧"; // Water Works
            default: return "";
        }
    }
    
    /**
     * Checks if color represents a property
     */
    private boolean isPropertyColor(String color) {
        return color.equals("brown") || color.equals("light-blue") || 
               color.equals("pink") || color.equals("orange") ||
               color.equals("red") || color.equals("yellow") ||
               color.equals("green") || color.equals("dark-blue");
    }
    
    /**
     * Gets color for a property group
     */
    private Color getColorForGroup(String group) {
        switch (group) {
            case "brown": return Color.web("#8B4513");
            case "light-blue": return Color.web("#87CEEB");
            case "pink": return Color.web("#FF69B4");
            case "orange": return Color.web("#FFA500");
            case "red": return Color.web("#FF0000");
            case "yellow": return Color.web("#FFD700");
            case "green": return Color.web("#228B22");
            case "dark-blue": return Color.web("#00008B");
            default: return Color.GRAY;
        }
    }
    
    /**
     * Adds special styling for corner and special tiles
     */
    private void addSpecialTileStyle(StackPane tile, int position, String tileColor) {
        switch (position) {
            case 0: // GO
                tile.setStyle("-fx-background-color: linear-gradient(to bottom right, #00c853, #00a844);");
                break;
            case 10: // Jail
                tile.setStyle("-fx-background-color: linear-gradient(to bottom right, #ff9800, #f57c00);");
                break;
            case 20: // Free Parking
                tile.setStyle("-fx-background-color: linear-gradient(to bottom right, #e94560, #d63850);");
                break;
            case 30: // Go To Jail
                tile.setStyle("-fx-background-color: linear-gradient(to bottom right, #2196f3, #1976d2);");
                break;
            case 2: case 17: case 33: // Community Chest
                tile.setStyle("-fx-background-color: linear-gradient(to bottom right, #00bcd4, #0097a7);");
                break;
            case 7: case 22: case 36: // Chance
                tile.setStyle("-fx-background-color: linear-gradient(to bottom right, #9c27b0, #7b1fa2);");
                break;
            case 4: case 38: // Tax
                tile.setStyle("-fx-background-color: linear-gradient(to bottom right, #607d8b, #455a64);");
                break;
        }
    }
    
    /**
     * Updates the board with current game state
     */
    public void updateBoard(GameState state) {
        if (state == null) return;
        this.currentGameState = state;
        
        Platform.runLater(() -> {
            updatePlayerPositions(state);
            // updateTileOwnership() - update property ownership indicators
            // updateBuildings() - update house/hotel indicators
        });
    }
    
    /**
     * Updates player token positions
     */
    private void updatePlayerPositions(GameState state) {
        int playerIndex = 0;
        for (Player player : state.getPlayers().values()) {
            int playerId = player.getId();
            int position = player.getPosition();
            
            // Create token if not exists
            if (!playerTokens.containsKey(playerId)) {
                Circle token = createPlayerToken(player);
                playerTokens.put(playerId, token);
            }
            
            Circle token = playerTokens.get(playerId);
            
            // Check if position changed
            Integer oldPos = playerPositions.get(playerId);
            if (oldPos == null || oldPos != position) {
                // Animate move if old position exists
                if (oldPos != null) {
                    animateTokenMove(token, oldPos, position);
                } else {
                    // Just place token
                    placeTokenOnTile(token, position, playerIndex);
                }
                playerPositions.put(playerId, position);
            }
            playerIndex++;
        }
    }
    
    /**
     * Creates a player token
     */
    private Circle createPlayerToken(Player player) {
        Circle token = new Circle(10);
        token.setFill(getTokenColor(player.getTokenType()));
        token.setStroke(Color.WHITE);
        token.setStrokeWidth(2);
        token.setEffect(new DropShadow(5, Color.BLACK));
        token.getStyleClass().add("player-token");
        return token;
    }
    
    /**
     * Gets color for player token
     */
    private Color getTokenColor(com.monopoly.model.player.TokenType tokenType) {
        if (tokenType == null) return Color.GRAY;
        switch (tokenType) {
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
     * Places a token on a tile
     */
    private void placeTokenOnTile(Circle token, int position, int playerIndex) {
        if (tileViews[position][0] != null) {
            StackPane tile = tileViews[position][0];
            
            // Remove from previous tile if exists
            for (int i = 0; i < 40; i++) {
                if (tileViews[i][0] != null) {
                    tileViews[i][0].getChildren().remove(token);
                }
            }
            
            // Offset tokens so they don't overlap
            double offsetX = (playerIndex % 2) * 15 - 7;
            double offsetY = (playerIndex / 2) * 15 - 7;
            token.setTranslateX(offsetX);
            token.setTranslateY(offsetY);
            
            tile.getChildren().add(token);
        }
    }
    
    /**
     * Animates token moving from one position to another
     */
    public void animateTokenMove(Circle token, int fromPos, int toPos) {
        // For now, just place at new position
        // TODO: Implement step-by-step animation around the board
        int playerIndex = 0; // Calculate based on token
        placeTokenOnTile(token, toPos, playerIndex);
        
        // Add arrival animation
        ScaleTransition bounce = new ScaleTransition(Duration.millis(200), token);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.5);
        bounce.setToY(1.5);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(2);
        bounce.play();
    }
    
    /**
     * Highlights a tile
     */
    public void highlightTile(int position) {
        if (tileViews[position][0] != null) {
            StackPane tile = tileViews[position][0];
            tile.setEffect(new Glow(0.5));
            
            // Pulsing animation
            Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(tile.scaleXProperty(), 1.0)),
                new KeyFrame(Duration.millis(500), new KeyValue(tile.scaleXProperty(), 1.1)),
                new KeyFrame(Duration.millis(1000), new KeyValue(tile.scaleXProperty(), 1.0))
            );
            pulse.setCycleCount(Timeline.INDEFINITE);
            pulse.play();
        }
    }
    
    /**
     * Clears all tile highlights
     */
    public void clearHighlights() {
        for (int i = 0; i < 40; i++) {
            if (tileViews[i][0] != null) {
                tileViews[i][0].setEffect(null);
                tileViews[i][0].setScaleX(1.0);
                tileViews[i][0].setScaleY(1.0);
            }
        }
    }
    
    /**
     * Updates tile ownership indicator
     */
    public void updateTileOwnership(int position, Color ownerColor) {
        if (tileViews[position][0] != null) {
            StackPane tile = tileViews[position][0];
            tile.setBorder(new Border(new BorderStroke(
                ownerColor, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(3)
            )));
        }
    }
    
    /**
     * Updates building indicators on a tile
     */
    public void updateBuildings(int position, int houses, boolean hasHotel) {
        if (tileViews[position][0] != null) {
            StackPane tile = tileViews[position][0];
            HBox buildingContainer = (HBox) tile.lookup("#buildings-" + position);
            
            if (buildingContainer != null) {
                buildingContainer.getChildren().clear();
                
                if (hasHotel) {
                    Rectangle hotel = new Rectangle(12, 12);
                    hotel.setFill(Color.RED);
                    hotel.setStroke(Color.WHITE);
                    hotel.setStrokeWidth(1);
                    buildingContainer.getChildren().add(hotel);
                } else {
                    for (int i = 0; i < houses; i++) {
                        Rectangle house = new Rectangle(8, 8);
                        house.setFill(Color.LIMEGREEN);
                        house.setStroke(Color.WHITE);
                        house.setStrokeWidth(0.5);
                        buildingContainer.getChildren().add(house);
                    }
                }
            }
        }
    }
}
