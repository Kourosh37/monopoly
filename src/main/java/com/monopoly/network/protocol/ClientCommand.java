package com.monopoly.network.protocol;

import com.monopoly.datastructures.HashTable;

/**
 * Represents a command sent from client to server.
 * Contains the command type and any associated parameters.
 */
public class ClientCommand extends Message {

    // Parameters storage
    private final HashTable<String, Object> parameters;
    
    // Room info
    private String roomId;
    
    /**
     * Creates a new client command
     * @param commandType The command type
     * @param playerId The player sending the command
     */
    public ClientCommand(MessageType commandType, int playerId) {
        super(commandType, playerId);
        this.parameters = new HashTable<>();
        this.roomId = null;
    }
    
    /**
     * Creates a client command for deserialization
     */
    protected ClientCommand(String messageId, MessageType commandType, int playerId, 
                           long timestamp, HashTable<String, Object> parameters) {
        super(messageId, commandType, playerId, timestamp);
        this.parameters = parameters != null ? parameters : new HashTable<>();
    }
    
    /**
     * Gets the command type
     * @return Command type
     */
    public MessageType getCommandType() {
        return messageType;
    }
    
    /**
     * Sets a parameter
     * @param key Parameter name
     * @param value Parameter value
     */
    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }
    
    /**
     * Gets a parameter
     * @param key Parameter name
     * @return Parameter value or null
     */
    public Object getParameter(String key) {
        return parameters.get(key);
    }
    
    /**
     * Gets a parameter as String
     * @param key Parameter name
     * @return String value or null
     */
    public String getStringParameter(String key) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Gets a parameter as Integer
     * @param key Parameter name
     * @return Integer value or default
     */
    public int getIntParameter(String key, int defaultValue) {
        Object value = parameters.get(key);
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
     * Checks if a parameter exists
     * @param key Parameter name
     * @return true if exists
     */
    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
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
        json.append("\"sender\":").append(senderId).append(",");
        json.append("\"timestamp\":").append(timestamp).append(",");
        
        if (roomId != null) {
            json.append("\"roomId\":\"").append(roomId).append("\",");
        }
        
        json.append("\"params\":{");
        
        // Serialize parameters
        // Note: Since we can't iterate HashTable easily, we serialize known parameter keys
        String[] knownKeys = {"playerName", "propertyId", "amount", "targetPlayerId", 
                              "buildingType", "offeredMoney", "requestedMoney"};
        boolean first = true;
        for (String key : knownKeys) {
            Object value = parameters.get(key);
            if (value != null) {
                if (!first) json.append(",");
                first = false;
                json.append("\"").append(key).append("\":");
                if (value instanceof String) {
                    json.append("\"").append(escapeJson(value.toString())).append("\"");
                } else if (value instanceof Number) {
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
     * Creates a ClientCommand from JSON
     * @param json JSON string
     * @return ClientCommand or null
     */
    public static ClientCommand fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        
        try {
            String id = extractJsonField(json, "id");
            String typeStr = extractJsonField(json, "type");
            String senderStr = extractJsonField(json, "sender");
            String timestampStr = extractJsonField(json, "timestamp");
            String roomId = extractJsonField(json, "roomId");
            
            MessageType type = MessageType.valueOf(typeStr);
            int sender = Integer.parseInt(senderStr);
            long timestamp = Long.parseLong(timestampStr);
            
            ClientCommand command = new ClientCommand(id, type, sender, timestamp, new HashTable<>());
            command.setRoomId(roomId);
            
            // Parse parameters
            String paramsJson = extractJsonField(json, "params");
            if (paramsJson != null && !paramsJson.equals("{}")) {
                parseParameters(paramsJson, command);
            }
            
            return command;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Parses parameters from JSON object string
     */
    private static void parseParameters(String paramsJson, ClientCommand command) {
        String[] keys = {"playerName", "propertyId", "amount", "targetPlayerId", 
                         "buildingType", "offeredMoney", "requestedMoney"};
        
        for (String key : keys) {
            String value = extractJsonField(paramsJson, key);
            if (value != null) {
                // Try to parse as number
                try {
                    command.setParameter(key, Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    command.setParameter(key, value);
                }
            }
        }
    }
    
    // ==================== Factory Methods ====================
    
    /**
     * Creates a HELLO command
     * @param playerName Player name
     * @return ClientCommand
     */
    public static ClientCommand createHello(String playerName) {
        ClientCommand cmd = new ClientCommand(MessageType.HELLO, -1);
        cmd.setParameter("playerName", playerName);
        return cmd;
    }
    
    /**
     * Creates a ROLL_DICE command
     * @param playerId Player ID
     * @return ClientCommand
     */
    public static ClientCommand createRollDice(int playerId) {
        return new ClientCommand(MessageType.ROLL_DICE, playerId);
    }
    
    /**
     * Creates a BUY_PROPERTY command
     * @param playerId Player ID
     * @param propertyId Property ID
     * @return ClientCommand
     */
    public static ClientCommand createBuyProperty(int playerId, int propertyId) {
        ClientCommand cmd = new ClientCommand(MessageType.BUY_PROPERTY, playerId);
        cmd.setParameter("propertyId", propertyId);
        return cmd;
    }
    
    /**
     * Creates a DECLINE_BUY command
     * @param playerId Player ID
     * @return ClientCommand
     */
    public static ClientCommand createDeclineBuy(int playerId) {
        return new ClientCommand(MessageType.DECLINE_BUY, playerId);
    }
    
    /**
     * Creates a BID command
     * @param playerId Player ID
     * @param amount Bid amount
     * @return ClientCommand
     */
    public static ClientCommand createBid(int playerId, int amount) {
        ClientCommand cmd = new ClientCommand(MessageType.BID, playerId);
        cmd.setParameter("amount", amount);
        return cmd;
    }
    
    /**
     * Creates a PASS_BID command
     * @param playerId Player ID
     * @return ClientCommand
     */
    public static ClientCommand createPassBid(int playerId) {
        return new ClientCommand(MessageType.PASS_BID, playerId);
    }
    
    /**
     * Creates a BUILD command
     * @param playerId Player ID
     * @param propertyId Property ID
     * @param buildingType "HOUSE" or "HOTEL"
     * @return ClientCommand
     */
    public static ClientCommand createBuild(int playerId, int propertyId, String buildingType) {
        ClientCommand cmd = new ClientCommand(MessageType.BUILD, playerId);
        cmd.setParameter("propertyId", propertyId);
        cmd.setParameter("buildingType", buildingType);
        return cmd;
    }
    
    /**
     * Creates a MORTGAGE command
     * @param playerId Player ID
     * @param propertyId Property ID
     * @return ClientCommand
     */
    public static ClientCommand createMortgage(int playerId, int propertyId) {
        ClientCommand cmd = new ClientCommand(MessageType.MORTGAGE, playerId);
        cmd.setParameter("propertyId", propertyId);
        return cmd;
    }
    
    /**
     * Creates an END_TURN command
     * @param playerId Player ID
     * @return ClientCommand
     */
    public static ClientCommand createEndTurn(int playerId) {
        return new ClientCommand(MessageType.END_TURN, playerId);
    }
    
    /**
     * Creates a JAIL_PAY_FINE command
     * @param playerId Player ID
     * @return ClientCommand
     */
    public static ClientCommand createJailPayFine(int playerId) {
        return new ClientCommand(MessageType.JAIL_PAY_FINE, playerId);
    }
    
    /**
     * Creates a JAIL_USE_CARD command
     * @param playerId Player ID
     * @return ClientCommand
     */
    public static ClientCommand createJailUseCard(int playerId) {
        return new ClientCommand(MessageType.JAIL_USE_CARD, playerId);
    }
    
    /**
     * Creates a DISCONNECT command
     * @param playerId Player ID
     * @return ClientCommand
     */
    public static ClientCommand createDisconnect(int playerId) {
        return new ClientCommand(MessageType.DISCONNECT, playerId);
    }
    
    @Override
    public String toString() {
        return "ClientCommand{" +
                "type=" + messageType +
                ", sender=" + senderId +
                ", roomId='" + roomId + '\'' +
                '}';
    }
}
