package com.monopoly.network.serialization;

import com.monopoly.model.player.Player;
import com.monopoly.model.property.Property;
import com.monopoly.model.property.ColorGroup;
import com.monopoly.datastructures.ArrayList;
import com.monopoly.datastructures.HashTable;
import com.monopoly.model.game.Bank;
import com.monopoly.model.game.Dice;
import com.monopoly.model.game.GameState;
import com.monopoly.model.game.Trade;
import com.monopoly.model.game.Auction;
import com.monopoly.network.protocol.Message;
import com.monopoly.model.tile.Tile;
import com.monopoly.model.tile.PropertyTile;
import com.monopoly.model.tile.RailroadTile;
import com.monopoly.model.tile.UtilityTile;

/**
 * Serializes game objects to JSON format for network transmission.
 */
public class Serializer {

    /**
     * Creates a new Serializer
     */
    public Serializer() {
        // No external dependencies needed
    }
    
    /**
     * Serializes an object to JSON string
     * @param object Object to serialize
     * @return JSON string
     */
    public String serialize(Object object) {
        if (object == null) {
            return "null";
        }
        
        if (object instanceof GameState) {
            return serializeGameState((GameState) object);
        } else if (object instanceof Player) {
            return serializePlayer((Player) object);
        } else if (object instanceof Property) {
            return serializeProperty((Property) object);
        } else if (object instanceof Tile) {
            return serializeTile((Tile) object);
        } else if (object instanceof Trade) {
            return serializeTrade((Trade) object);
        } else if (object instanceof Auction) {
            return serializeAuction((Auction) object);
        } else if (object instanceof Message) {
            return object.toString();
        } else if (object instanceof String) {
            return "\"" + escapeJson((String) object) + "\"";
        } else if (object instanceof Number || object instanceof Boolean) {
            return object.toString();
        } else {
            return "\"" + escapeJson(object.toString()) + "\"";
        }
    }
    
    /**
     * Serializes GameState
     * @param state GameState to serialize
     * @return JSON string
     */
    public String serializeGameState(GameState state) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // Current player
        json.append("\"currentPlayerId\":").append(state.getCurrentPlayerId()).append(",");
        json.append("\"turnNumber\":").append(state.getTurnNumber()).append(",");
        json.append("\"isGameStarted\":").append(state.isGameStarted()).append(",");
        json.append("\"isGameOver\":").append(state.isGameOver()).append(",");
        
        // Turn phase
        json.append("\"turnPhase\":\"").append(state.getTurnPhase().name()).append("\",");
        
        // Dice
        Dice dice = state.getDice();
        json.append("\"dice\":{");
        json.append("\"die1\":").append(dice.getDie1()).append(",");
        json.append("\"die2\":").append(dice.getDie2()).append(",");
        json.append("\"total\":").append(dice.getTotal()).append(",");
        json.append("\"isDoubles\":").append(dice.isDoubles());
        json.append("},");
        
        // Players
        json.append("\"players\":[");
        ArrayList<Player> players = state.getAllPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (i > 0) json.append(",");
            json.append(serializePlayer(players.get(i)));
        }
        json.append("],");
        
        // Bank
        Bank bank = state.getBank();
        json.append("\"bank\":{");
        json.append("\"availableHouses\":").append(bank.getAvailableHouses()).append(",");
        json.append("\"availableHotels\":").append(bank.getAvailableHotels());
        json.append("},");
        
        // Properties
        json.append("\"properties\":[");
        ArrayList<Property> properties = bank.getAllProperties();
        for (int i = 0; i < properties.size(); i++) {
            if (i > 0) json.append(",");
            json.append(serializeProperty(properties.get(i)));
        }
        json.append("],");
        
        // Active auction
        Auction auction = state.getActiveAuction();
        if (auction != null && auction.isActive()) {
            json.append("\"auction\":").append(serializeAuction(auction)).append(",");
        }
        
        // Active trade
        Trade trade = state.getActiveTrade();
        if (trade != null && trade.isPending()) {
            json.append("\"trade\":").append(serializeTrade(trade)).append(",");
        }
        
        // Winner
        if (state.isGameOver() && state.getWinner() != null) {
            json.append("\"winnerId\":").append(state.getWinner().getId()).append(",");
        }
        
        // Remove trailing comma if exists
        String result = json.toString();
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }
        
        return result + "}";
    }
    
    /**
     * Serializes a Player
     * @param player Player to serialize
     * @return JSON string
     */
    public String serializePlayer(Player player) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(player.getId()).append(",");
        json.append("\"name\":\"").append(escapeJson(player.getName())).append("\",");
        json.append("\"money\":").append(player.getMoney()).append(",");
        json.append("\"position\":").append(player.getPosition()).append(",");
        json.append("\"isInJail\":").append(player.isInJail()).append(",");
        json.append("\"turnsInJail\":").append(player.getTurnsInJail()).append(",");
        json.append("\"isBankrupt\":").append(player.isBankrupt()).append(",");
        json.append("\"jailFreeCards\":").append(player.getJailFreeCards()).append(",");
        json.append("\"token\":\"").append(player.getToken().name()).append("\"");
        json.append("}");
        return json.toString();
    }
    
    /**
     * Serializes a Property
     * @param property Property to serialize
     * @return JSON string
     */
    public String serializeProperty(Property property) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(property.getId()).append(",");
        json.append("\"name\":\"").append(escapeJson(property.getName())).append("\",");
        json.append("\"group\":\"").append(property.getColorGroup().name()).append("\",");
        json.append("\"price\":").append(property.getPrice()).append(",");
        json.append("\"baseRent\":").append(property.getBaseRent()).append(",");
        json.append("\"isMortgaged\":").append(property.isMortgaged()).append(",");
        json.append("\"mortgageValue\":").append(property.getMortgageValue()).append(",");
        
        int ownerId = property.getOwnerId();
        json.append("\"ownerId\":").append(ownerId).append(",");
        
        // Color group specific
        ColorGroup group = property.getColorGroup();
        if (group != ColorGroup.RAILROAD && group != ColorGroup.UTILITY) {
            json.append("\"houses\":").append(property.getNumberOfHouses()).append(",");
            json.append("\"hasHotel\":").append(property.hasHotel()).append(",");
            json.append("\"houseCost\":").append(property.getHouseCost()).append(",");
            json.append("\"rent1House\":").append(property.getRentWithHouses(1)).append(",");
            json.append("\"rent2House\":").append(property.getRentWithHouses(2)).append(",");
            json.append("\"rent3House\":").append(property.getRentWithHouses(3)).append(",");
            json.append("\"rent4House\":").append(property.getRentWithHouses(4)).append(",");
            json.append("\"rentHotel\":").append(property.getRentWithHotel());
        } else {
            json.append("\"type\":\"").append(group.name()).append("\"");
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Serializes a Tile
     * @param tile Tile to serialize
     * @return JSON string
     */
    public String serializeTile(Tile tile) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"position\":").append(tile.getPosition()).append(",");
        json.append("\"name\":\"").append(escapeJson(tile.getName())).append("\",");
        json.append("\"type\":\"").append(tile.getTileType().name()).append("\"");
        
        // Property info if applicable
        if (tile instanceof PropertyTile) {
            PropertyTile pt = (PropertyTile) tile;
            json.append(",\"ownerId\":").append(pt.getOwnerId());
        } else if (tile instanceof RailroadTile) {
            RailroadTile rt = (RailroadTile) tile;
            json.append(",\"ownerId\":").append(rt.getOwnerId());
        } else if (tile instanceof UtilityTile) {
            UtilityTile ut = (UtilityTile) tile;
            json.append(",\"ownerId\":").append(ut.getOwnerId());
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Serializes a Trade
     * @param trade Trade to serialize
     * @return JSON string
     */
    public String serializeTrade(Trade trade) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":\"").append(escapeJson(trade.getTradeId())).append("\",");
        json.append("\"initiatorId\":").append(trade.getInitiatorId()).append(",");
        json.append("\"receiverId\":").append(trade.getReceiverId()).append(",");
        json.append("\"status\":\"").append(trade.getStatus().name()).append("\",");
        
        // Initiator offer
        json.append("\"initiatorMoney\":").append(trade.getInitiatorMoney()).append(",");
        json.append("\"initiatorProperties\":[");
        ArrayList<Property> initProps = trade.getInitiatorProperties();
        for (int i = 0; i < initProps.size(); i++) {
            if (i > 0) json.append(",");
            json.append(initProps.get(i).getId());
        }
        json.append("],");
        json.append("\"initiatorJailCards\":").append(trade.getInitiatorJailCards()).append(",");
        
        // Receiver offer
        json.append("\"receiverMoney\":").append(trade.getReceiverMoney()).append(",");
        json.append("\"receiverProperties\":[");
        ArrayList<Property> recvProps = trade.getReceiverProperties();
        for (int i = 0; i < recvProps.size(); i++) {
            if (i > 0) json.append(",");
            json.append(recvProps.get(i).getId());
        }
        json.append("],");
        json.append("\"receiverJailCards\":").append(trade.getReceiverJailCards());
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Serializes an Auction
     * @param auction Auction to serialize
     * @return JSON string
     */
    public String serializeAuction(Auction auction) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":\"").append(escapeJson(auction.getAuctionId())).append("\",");
        json.append("\"propertyId\":").append(auction.getProperty().getId()).append(",");
        json.append("\"propertyName\":\"").append(escapeJson(auction.getProperty().getName())).append("\",");
        json.append("\"status\":\"").append(auction.getStatus().name()).append("\",");
        json.append("\"currentBid\":").append(auction.getCurrentBid()).append(",");
        
        int highBidderId = auction.getCurrentHighBidder();
        json.append("\"highestBidderId\":").append(highBidderId);
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Serializes a list of objects
     * @param list List to serialize
     * @return JSON array string
     */
    public String serializeList(ArrayList<?> list) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) json.append(",");
            json.append(serialize(list.get(i)));
        }
        
        json.append("]");
        return json.toString();
    }
    
    /**
     * Creates formatted (pretty) JSON
     * @param object Object to serialize
     * @return Formatted JSON string
     */
    public String toPrettyJson(Object object) {
        String json = serialize(object);
        return formatJson(json);
    }
    
    /**
     * Formats JSON with indentation
     * @param json JSON string
     * @return Formatted JSON
     */
    private String formatJson(String json) {
        StringBuilder result = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        
        for (int i = 0; i < json.length(); i++) {
            char ch = json.charAt(i);
            
            if (ch == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
                result.append(ch);
            } else if (!inString) {
                switch (ch) {
                    case '{':
                    case '[':
                        result.append(ch);
                        result.append('\n');
                        indent++;
                        appendIndent(result, indent);
                        break;
                    case '}':
                    case ']':
                        result.append('\n');
                        indent--;
                        appendIndent(result, indent);
                        result.append(ch);
                        break;
                    case ',':
                        result.append(ch);
                        result.append('\n');
                        appendIndent(result, indent);
                        break;
                    case ':':
                        result.append(": ");
                        break;
                    default:
                        result.append(ch);
                        break;
                }
            } else {
                result.append(ch);
            }
        }
        
        return result.toString();
    }
    
    /**
     * Appends indentation
     */
    private void appendIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
    }
    
    /**
     * Escapes special characters for JSON
     * @param text Text to escape
     * @return Escaped text
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
