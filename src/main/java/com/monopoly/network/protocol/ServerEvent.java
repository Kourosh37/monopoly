package com.monopoly.network.protocol;

import com.monopoly.datastructures.HashTable;

/**
 * Represents an event sent from server to client.
 * Contains the event type and associated data.
 */
public class ServerEvent extends Message {

    // Event data
    private final HashTable<String, Object> data;
    
    // Targeting
    private int targetPlayerId; // -1 for broadcast
    
    // Room info
    private String roomId;
    
    /**
     * Creates a new server event
     * @param eventType The event type
     */
    public ServerEvent(MessageType eventType) {
        super(eventType, SERVER_ID);
        this.data = new HashTable<>();
        this.targetPlayerId = -1;
        this.roomId = null;
    }
    
    /**
     * Creates a server event with data
     * @param eventType Event type
     * @param key Data key
     * @param value Data value
     */
    public ServerEvent(MessageType eventType, String key, Object value) {
        this(eventType);
        setData(key, value);
    }
    
    /**
     * Creates a server event for deserialization
     */
    protected ServerEvent(String messageId, MessageType eventType, long timestamp,
                         HashTable<String, Object> data, int targetPlayerId) {
        super(messageId, eventType, SERVER_ID, timestamp);
        this.data = data != null ? data : new HashTable<>();
        this.targetPlayerId = targetPlayerId;
    }
    
    /**
     * Gets the event type
     * @return Event type
     */
    public MessageType getEventType() {
        return messageType;
    }
    
    /**
     * Sets data value
     * @param key Key
     * @param value Value
     */
    public void setData(String key, Object value) {
        data.put(key, value);
    }
    
    /**
     * Gets data value
     * @param key Key
     * @return Value or null
     */
    public Object getData(String key) {
        return data.get(key);
    }
    
    /**
     * Gets data as String
     * @param key Key
     * @return String value or null
     */
    public String getStringData(String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Gets data as Integer
     * @param key Key
     * @param defaultValue Default value
     * @return Integer value
     */
    public int getIntData(String key, int defaultValue) {
        Object value = data.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Gets data as Boolean
     * @param key Key
     * @param defaultValue Default value
     * @return Boolean value
     */
    public boolean getBooleanData(String key, boolean defaultValue) {
        Object value = data.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
    
    /**
     * Gets the target player ID
     * @return Target player ID or -1 for broadcast
     */
    public int getTargetPlayerId() {
        return targetPlayerId;
    }
    
    /**
     * Sets the target player ID
     * @param playerId Target player ID
     */
    public void setTargetPlayerId(int playerId) {
        this.targetPlayerId = playerId;
    }
    
    /**
     * Checks if this is a broadcast message
     * @return true if broadcast
     */
    public boolean isBroadcast() {
        return targetPlayerId < 0;
    }
    
    /**
     * Gets the room ID
     * @return Room ID or null
     */
    public String getRoomId() {
        return roomId;
    }
    
    /**
     * Sets the room ID
     * @param roomId Room ID
     */
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    @Override
    public String serialize() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":\"").append(messageId).append("\",");
        json.append("\"type\":\"").append(messageType.name()).append("\",");
        json.append("\"timestamp\":").append(timestamp).append(",");
        json.append("\"target\":").append(targetPlayerId).append(",");
        
        if (roomId != null) {
            json.append("\"roomId\":\"").append(roomId).append("\",");
        }
        
        json.append("\"data\":{");
        
        // Serialize known data keys
        String[] knownKeys = {"message", "description", "playerId", "playerName",
                              "propertyId", "propertyName", "die1", "die2", "total", "isDoubles",
                              "winnerId", "winnerName", "currentBid", "highestBidderId",
                              "cardType", "cardDescription", "turnsRemaining", "reason",
                              "currentPlayerId", "gameState"};
        
        boolean first = true;
        for (String key : knownKeys) {
            Object value = data.get(key);
            if (value != null) {
                if (!first) json.append(",");
                first = false;
                json.append("\"").append(key).append("\":");
                if (value instanceof String) {
                    json.append("\"").append(escapeJson(value.toString())).append("\"");
                } else if (value instanceof Number || value instanceof Boolean) {
                    json.append(value);
                } else {
                    json.append("\"").append(escapeJson(value.toString())).append("\"");
                }
            }
        }
        
        json.append("}");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Escapes special characters for JSON
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * Creates a ServerEvent from JSON
     * @param json JSON string
     * @return ServerEvent or null
     */
    public static ServerEvent fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        
        try {
            String id = extractJsonField(json, "id");
            String typeStr = extractJsonField(json, "type");
            String timestampStr = extractJsonField(json, "timestamp");
            String targetStr = extractJsonField(json, "target");
            String roomId = extractJsonField(json, "roomId");
            
            MessageType type = MessageType.valueOf(typeStr);
            long timestamp = Long.parseLong(timestampStr);
            int target = Integer.parseInt(targetStr);
            
            ServerEvent event = new ServerEvent(id, type, timestamp, new HashTable<>(), target);
            event.setRoomId(roomId);
            
            // Parse data
            String dataJson = extractJsonField(json, "data");
            if (dataJson != null && !dataJson.equals("{}")) {
                parseData(dataJson, event);
            }
            
            return event;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Parses data from JSON object string
     */
    private static void parseData(String dataJson, ServerEvent event) {
        String[] keys = {"message", "description", "playerId", "playerName",
                        "propertyId", "propertyName", "die1", "die2", "total", "isDoubles",
                        "winnerId", "winnerName", "currentBid", "highestBidderId",
                        "cardType", "cardDescription", "turnsRemaining", "reason",
                        "currentPlayerId"};
        
        for (String key : keys) {
            String value = extractJsonField(dataJson, key);
            if (value != null) {
                // Try to parse as number
                try {
                    event.setData(key, Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    // Try boolean
                    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                        event.setData(key, Boolean.parseBoolean(value));
                    } else {
                        event.setData(key, value);
                    }
                }
            }
        }
    }
    
    // ==================== Factory Methods ====================
    
    /**
     * Creates a STATE_UPDATE event
     * @param gameStateJson Game state as JSON
     * @return ServerEvent
     */
    public static ServerEvent createStateUpdate(String gameStateJson) {
        ServerEvent event = new ServerEvent(MessageType.STATE_UPDATE);
        event.setData("gameState", gameStateJson);
        return event;
    }
    
    /**
     * Creates an EVENT_LOG event
     * @param description Event description
     * @return ServerEvent
     */
    public static ServerEvent createEventLog(String description) {
        return new ServerEvent(MessageType.EVENT_LOG, "description", description);
    }
    
    /**
     * Creates an ERROR event
     * @param errorMessage Error message
     * @return ServerEvent
     */
    public static ServerEvent createError(String errorMessage) {
        return new ServerEvent(MessageType.ERROR, "message", errorMessage);
    }
    
    /**
     * Creates a targeted ERROR event
     * @param errorMessage Error message
     * @param playerId Target player
     * @return ServerEvent
     */
    public static ServerEvent createError(String errorMessage, int playerId) {
        ServerEvent event = createError(errorMessage);
        event.setTargetPlayerId(playerId);
        return event;
    }
    
    /**
     * Creates a GAME_START event
     * @return ServerEvent
     */
    public static ServerEvent createGameStart() {
        return new ServerEvent(MessageType.GAME_START);
    }
    
    /**
     * Creates a GAME_END event
     * @param winnerId Winner's ID
     * @param winnerName Winner's name
     * @return ServerEvent
     */
    public static ServerEvent createGameEnd(int winnerId, String winnerName) {
        ServerEvent event = new ServerEvent(MessageType.GAME_END);
        event.setData("winnerId", winnerId);
        event.setData("winnerName", winnerName);
        return event;
    }
    
    /**
     * Creates a TURN_START event
     * @param currentPlayerId Current player's ID
     * @return ServerEvent
     */
    public static ServerEvent createTurnStart(int currentPlayerId) {
        return new ServerEvent(MessageType.TURN_START, "currentPlayerId", currentPlayerId);
    }
    
    /**
     * Creates a DICE_RESULT event
     * @param die1 First die
     * @param die2 Second die
     * @return ServerEvent
     */
    public static ServerEvent createDiceResult(int die1, int die2) {
        ServerEvent event = new ServerEvent(MessageType.DICE_RESULT);
        event.setData("die1", die1);
        event.setData("die2", die2);
        event.setData("total", die1 + die2);
        event.setData("isDoubles", die1 == die2);
        return event;
    }
    
    /**
     * Creates a PLAYER_JOINED event
     * @param playerId Player ID
     * @param playerName Player name
     * @return ServerEvent
     */
    public static ServerEvent createPlayerJoined(int playerId, String playerName) {
        ServerEvent event = new ServerEvent(MessageType.PLAYER_JOINED);
        event.setData("playerId", playerId);
        event.setData("playerName", playerName);
        return event;
    }
    
    /**
     * Creates a PLAYER_LEFT event
     * @param playerId Player ID
     * @param reason Disconnect reason
     * @return ServerEvent
     */
    public static ServerEvent createPlayerLeft(int playerId, String reason) {
        ServerEvent event = new ServerEvent(MessageType.PLAYER_LEFT);
        event.setData("playerId", playerId);
        event.setData("reason", reason);
        return event;
    }
    
    /**
     * Creates a PLAYER_BANKRUPT event
     * @param playerId Bankrupt player's ID
     * @return ServerEvent
     */
    public static ServerEvent createPlayerBankrupt(int playerId) {
        return new ServerEvent(MessageType.PLAYER_BANKRUPT, "playerId", playerId);
    }
    
    /**
     * Creates an AUCTION_START event
     * @param propertyId Property ID
     * @param propertyName Property name
     * @return ServerEvent
     */
    public static ServerEvent createAuctionStart(int propertyId, String propertyName) {
        ServerEvent event = new ServerEvent(MessageType.AUCTION_START);
        event.setData("propertyId", propertyId);
        event.setData("propertyName", propertyName);
        return event;
    }
    
    /**
     * Creates an AUCTION_UPDATE event
     * @param currentBid Current bid
     * @param highestBidderId Highest bidder
     * @return ServerEvent
     */
    public static ServerEvent createAuctionUpdate(int currentBid, int highestBidderId) {
        ServerEvent event = new ServerEvent(MessageType.AUCTION_UPDATE);
        event.setData("currentBid", currentBid);
        event.setData("highestBidderId", highestBidderId);
        return event;
    }
    
    /**
     * Creates an AUCTION_END event
     * @param winnerId Winner's ID
     * @param winningBid Winning bid
     * @return ServerEvent
     */
    public static ServerEvent createAuctionEnd(int winnerId, int winningBid) {
        ServerEvent event = new ServerEvent(MessageType.AUCTION_END);
        event.setData("winnerId", winnerId);
        event.setData("currentBid", winningBid);
        return event;
    }
    
    /**
     * Creates a CARD_DRAWN event
     * @param cardType Card type
     * @param cardDescription Card description
     * @return ServerEvent
     */
    public static ServerEvent createCardDrawn(String cardType, String cardDescription) {
        ServerEvent event = new ServerEvent(MessageType.CARD_DRAWN);
        event.setData("cardType", cardType);
        event.setData("cardDescription", cardDescription);
        return event;
    }
    
    /**
     * Creates a JAIL_STATUS event
     * @param playerId Player ID
     * @param turnsRemaining Turns remaining in jail
     * @return ServerEvent
     */
    public static ServerEvent createJailStatus(int playerId, int turnsRemaining) {
        ServerEvent event = new ServerEvent(MessageType.JAIL_STATUS);
        event.setData("playerId", playerId);
        event.setData("turnsRemaining", turnsRemaining);
        return event;
    }
    
    @Override
    public String toString() {
        return "ServerEvent{" +
                "type=" + messageType +
                ", target=" + (targetPlayerId < 0 ? "broadcast" : targetPlayerId) +
                ", roomId='" + roomId + '\'' +
                '}';
    }
}
