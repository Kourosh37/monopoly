package com.monopoly.network.protocol;

import java.util.UUID;

/**
 * Base class for all network messages.
 * Used for communication between client and server.
 */
public abstract class Message {

    public static final int SERVER_ID = -1;
    
    // Message metadata
    protected final String messageId;
    protected final MessageType messageType;
    protected final long timestamp;
    protected int senderId;
    
    /**
     * Creates a new message
     * @param messageType The type of message
     * @param senderId The sender's ID (player ID or SERVER_ID)
     */
    public Message(MessageType messageType, int senderId) {
        this.messageId = UUID.randomUUID().toString();
        this.messageType = messageType;
        this.timestamp = System.currentTimeMillis();
        this.senderId = senderId;
    }
    
    /**
     * Creates a message with a specific ID (for deserialization)
     * @param messageId The message ID
     * @param messageType The message type
     * @param senderId The sender ID
     * @param timestamp The timestamp
     */
    protected Message(String messageId, MessageType messageType, int senderId, long timestamp) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }
    
    /**
     * Gets the unique message ID
     * @return Message ID
     */
    public String getMessageId() {
        return messageId;
    }
    
    /**
     * Gets the message type
     * @return MessageType enum value
     */
    public MessageType getMessageType() {
        return messageType;
    }
    
    /**
     * Gets the message timestamp
     * @return Timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the sender ID
     * @return Sender's player ID or SERVER_ID
     */
    public int getSenderId() {
        return senderId;
    }
    
    /**
     * Sets the sender ID
     * @param senderId The sender ID
     */
    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
    
    /**
     * Checks if message is from server
     * @return true if from server
     */
    public boolean isFromServer() {
        return senderId == SERVER_ID;
    }
    
    /**
     * Serializes the message to JSON string
     * @return JSON representation
     */
    public abstract String serialize();
    
    /**
     * Deserializes a JSON string to a Message
     * @param json The JSON string
     * @return The deserialized Message
     */
    public static Message deserialize(String json) {
        // Parse JSON to determine message type
        // Then delegate to appropriate subclass
        if (json == null || json.isEmpty()) {
            return null;
        }
        
        // Simple JSON parsing (extract type)
        String typeStr = extractJsonField(json, "type");
        if (typeStr == null) {
            return null;
        }
        
        try {
            MessageType type = MessageType.valueOf(typeStr);
            
            // Determine if client command or server event
            if (isClientCommand(type)) {
                return ClientCommand.fromJson(json);
            } else {
                return ServerEvent.fromJson(json);
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Checks if a message type is a client command
     * @param type The message type
     * @return true if client command
     */
    public static boolean isClientCommand(MessageType type) {
        switch (type) {
            case HELLO:
            case ROLL_DICE:
            case BUY_PROPERTY:
            case DECLINE_BUY:
            case BID:
            case PASS_BID:
            case PROPOSE_TRADE:
            case ACCEPT_TRADE:
            case DECLINE_TRADE:
            case CANCEL_TRADE:
            case BUILD:
            case SELL_BUILDING:
            case MORTGAGE:
            case UNMORTGAGE:
            case JAIL_PAY_FINE:
            case JAIL_USE_CARD:
            case JAIL_ROLL:
            case UNDO:
            case REDO:
            case END_TURN:
            case DISCONNECT:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Simple JSON field extraction
     * @param json JSON string
     * @param field Field name
     * @return Field value or null
     */
    public static String extractJsonField(String json, String field) {
        String searchPattern = "\"" + field + "\":";
        int startIndex = json.indexOf(searchPattern);
        if (startIndex < 0) {
            return null;
        }
        
        startIndex += searchPattern.length();
        
        // Skip whitespace
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }
        
        if (startIndex >= json.length()) {
            return null;
        }
        
        char firstChar = json.charAt(startIndex);
        
        if (firstChar == '"') {
            // String value
            int endIndex = json.indexOf('"', startIndex + 1);
            if (endIndex < 0) return null;
            return json.substring(startIndex + 1, endIndex);
        } else if (firstChar == '{' || firstChar == '[') {
            // Object or array - find matching bracket
            int depth = 1;
            int endIndex = startIndex + 1;
            char openBracket = firstChar;
            char closeBracket = (firstChar == '{') ? '}' : ']';
            
            while (endIndex < json.length() && depth > 0) {
                char c = json.charAt(endIndex);
                if (c == openBracket) depth++;
                else if (c == closeBracket) depth--;
                endIndex++;
            }
            return json.substring(startIndex, endIndex);
        } else {
            // Number or boolean
            int endIndex = startIndex;
            while (endIndex < json.length()) {
                char c = json.charAt(endIndex);
                if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                    break;
                }
                endIndex++;
            }
            return json.substring(startIndex, endIndex);
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + messageId + '\'' +
                ", type=" + messageType +
                ", sender=" + senderId +
                ", time=" + timestamp +
                '}';
    }
}
