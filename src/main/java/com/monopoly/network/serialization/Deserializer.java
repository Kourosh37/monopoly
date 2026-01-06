package com.monopoly.network.serialization;

import com.monopoly.model.player.Player;
import com.monopoly.model.player.PlayerToken;
import com.monopoly.model.property.Property;
import com.monopoly.model.property.ColorGroup;
import com.monopoly.model.enums.TurnPhase;
import com.monopoly.datastructures.ArrayList;
import com.monopoly.datastructures.HashTable;
import com.monopoly.network.protocol.ClientCommand;
import com.monopoly.network.protocol.Message;
import com.monopoly.network.protocol.ServerEvent;

/**
 * Deserializes JSON data back into game objects.
 */
public class Deserializer {

    /**
     * Creates a new Deserializer
     */
    public Deserializer() {
        // No external dependencies needed
    }
    
    /**
     * Validates if a string is valid JSON
     * @param json JSON string
     * @return true if valid JSON
     */
    public boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        json = json.trim();
        
        // Basic check: must start and end with {} or []
        if (json.startsWith("{") && json.endsWith("}")) {
            return validateJsonObject(json);
        } else if (json.startsWith("[") && json.endsWith("]")) {
            return validateJsonArray(json);
        }
        
        return false;
    }
    
    /**
     * Basic JSON object validation
     */
    private boolean validateJsonObject(String json) {
        int braceCount = 0;
        int bracketCount = 0;
        boolean inString = false;
        
        for (int i = 0; i < json.length(); i++) {
            char ch = json.charAt(i);
            
            if (ch == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (ch == '{') braceCount++;
                else if (ch == '}') braceCount--;
                else if (ch == '[') bracketCount++;
                else if (ch == ']') bracketCount--;
                
                if (braceCount < 0 || bracketCount < 0) {
                    return false;
                }
            }
        }
        
        return braceCount == 0 && bracketCount == 0;
    }
    
    /**
     * Basic JSON array validation
     */
    private boolean validateJsonArray(String json) {
        return validateJsonObject(json);
    }
    
    /**
     * Extracts a string field from JSON
     * @param json JSON string
     * @param field Field name
     * @return Field value or null
     */
    public String extractField(String json, String field) {
        return Message.extractJsonField(json, field);
    }
    
    /**
     * Extracts an integer field from JSON
     * @param json JSON string
     * @param field Field name
     * @param defaultValue Default value
     * @return Field value
     */
    public int extractIntField(String json, String field, int defaultValue) {
        String value = extractField(json, field);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Extracts a long field from JSON
     * @param json JSON string
     * @param field Field name
     * @param defaultValue Default value
     * @return Field value
     */
    public long extractLongField(String json, String field, long defaultValue) {
        String value = extractField(json, field);
        if (value == null) return defaultValue;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Extracts a boolean field from JSON
     * @param json JSON string
     * @param field Field name
     * @param defaultValue Default value
     * @return Field value
     */
    public boolean extractBooleanField(String json, String field, boolean defaultValue) {
        String value = extractField(json, field);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Deserializes a Player from JSON
     * @param json JSON string
     * @return Player or null
     */
    public Player deserializePlayer(String json) {
        if (!isValidJson(json)) {
            return null;
        }
        
        try {
            int id = extractIntField(json, "id", -1);
            String name = extractField(json, "name");
            int money = extractIntField(json, "money", 1500);
            int position = extractIntField(json, "position", 0);
            boolean isInJail = extractBooleanField(json, "isInJail", false);
            int turnsInJail = extractIntField(json, "turnsInJail", 0);
            boolean isBankrupt = extractBooleanField(json, "isBankrupt", false);
            int jailFreeCards = extractIntField(json, "jailFreeCards", 0);
            String tokenStr = extractField(json, "token");
            
            if (id < 0 || name == null) {
                return null;
            }
            
            PlayerToken token = PlayerToken.CAR;
            if (tokenStr != null) {
                try {
                    token = PlayerToken.valueOf(tokenStr);
                } catch (IllegalArgumentException e) {
                    // Use default
                }
            }
            
            Player player = new Player(id, name, token);
            player.setMoney(money);
            player.setPosition(position);
            if (isInJail) {
                player.goToJail();
                for (int i = 0; i < turnsInJail; i++) {
                    player.incrementTurnsInJail();
                }
            }
            if (isBankrupt) {
                player.setBankrupt(true);
            }
            for (int i = 0; i < jailFreeCards; i++) {
                player.addJailFreeCard();
            }
            
            return player;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Deserializes a Property from JSON
     * @param json JSON string
     * @return Property or null
     */
    public Property deserializeProperty(String json) {
        if (!isValidJson(json)) {
            return null;
        }
        
        try {
            int id = extractIntField(json, "id", -1);
            String name = extractField(json, "name");
            String groupStr = extractField(json, "group");
            int price = extractIntField(json, "price", 0);
            int baseRent = extractIntField(json, "baseRent", 0);
            
            if (id < 0 || name == null || groupStr == null) {
                return null;
            }
            
            ColorGroup group;
            try {
                group = ColorGroup.valueOf(groupStr);
            } catch (IllegalArgumentException e) {
                return null;
            }
            
            // For color properties
            int houseCost = extractIntField(json, "houseCost", 50);
            int rent1House = extractIntField(json, "rent1House", 0);
            int rent2House = extractIntField(json, "rent2House", 0);
            int rent3House = extractIntField(json, "rent3House", 0);
            int rent4House = extractIntField(json, "rent4House", 0);
            int rentHotel = extractIntField(json, "rentHotel", 0);
            int mortgageValue = extractIntField(json, "mortgageValue", price / 2);
            
            // Use the full constructor: (id, name, group, price, baseRent, rentColorSet, 
            //                            rent1, rent2, rent3, rent4, rentHotel, houseCost, hotelCost, mortgageValue)
            Property property = new Property(id, name, group, price, baseRent, baseRent * 2,
                    rent1House, rent2House, rent3House, rent4House, rentHotel,
                    houseCost, houseCost, mortgageValue);
            
            // Set state
            boolean isMortgaged = extractBooleanField(json, "isMortgaged", false);
            if (isMortgaged) {
                property.mortgage();
            }
            
            int houses = extractIntField(json, "houses", 0);
            for (int i = 0; i < houses; i++) {
                property.addHouse();
            }
            
            boolean hasHotel = extractBooleanField(json, "hasHotel", false);
            if (hasHotel) {
                property.addHotel();
            }
            
            return property;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Deserializes a Message from JSON
     * @param json JSON string
     * @return Message or null
     */
    public Message deserializeMessage(String json) {
        if (!isValidJson(json)) {
            return null;
        }
        
        // Try ClientCommand first
        ClientCommand command = deserializeClientCommand(json);
        if (command != null) {
            return command;
        }
        
        // Try ServerEvent
        return deserializeServerEvent(json);
    }
    
    /**
     * Deserializes a ClientCommand from JSON
     * @param json JSON string
     * @return ClientCommand or null
     */
    public ClientCommand deserializeClientCommand(String json) {
        return ClientCommand.fromJson(json);
    }
    
    /**
     * Deserializes a ServerEvent from JSON
     * @param json JSON string
     * @return ServerEvent or null
     */
    public ServerEvent deserializeServerEvent(String json) {
        return ServerEvent.fromJson(json);
    }
    
    /**
     * Deserializes turn phase from string
     * @param phaseStr Phase string
     * @return TurnPhase or PRE_ROLL as default
     */
    public TurnPhase deserializeTurnPhase(String phaseStr) {
        if (phaseStr == null) {
            return TurnPhase.PRE_ROLL;
        }
        try {
            return TurnPhase.valueOf(phaseStr);
        } catch (IllegalArgumentException e) {
            return TurnPhase.PRE_ROLL;
        }
    }
    
    /**
     * Extracts JSON array from JSON string
     * @param json JSON string
     * @param field Field name
     * @return Array content or null
     */
    public String extractArrayField(String json, String field) {
        String key = "\"" + field + "\":";
        int start = json.indexOf(key);
        if (start < 0) return null;
        
        start = json.indexOf('[', start);
        if (start < 0) return null;
        
        int bracketCount = 1;
        int end = start + 1;
        
        while (end < json.length() && bracketCount > 0) {
            char ch = json.charAt(end);
            if (ch == '[') bracketCount++;
            else if (ch == ']') bracketCount--;
            end++;
        }
        
        return json.substring(start, end);
    }
    
    /**
     * Extracts JSON object from JSON string
     * @param json JSON string
     * @param field Field name
     * @return Object content or null
     */
    public String extractObjectField(String json, String field) {
        String key = "\"" + field + "\":";
        int start = json.indexOf(key);
        if (start < 0) return null;
        
        start = json.indexOf('{', start);
        if (start < 0) return null;
        
        int braceCount = 1;
        int end = start + 1;
        
        while (end < json.length() && braceCount > 0) {
            char ch = json.charAt(end);
            if (ch == '{') braceCount++;
            else if (ch == '}') braceCount--;
            end++;
        }
        
        return json.substring(start, end);
    }
    
    /**
     * Parses a JSON array of integers
     * @param arrayJson JSON array string
     * @return ArrayList of integers
     */
    public ArrayList<Integer> parseIntArray(String arrayJson) {
        ArrayList<Integer> result = new ArrayList<>();
        
        if (arrayJson == null || arrayJson.equals("[]")) {
            return result;
        }
        
        // Remove brackets
        String content = arrayJson.substring(1, arrayJson.length() - 1).trim();
        if (content.isEmpty()) {
            return result;
        }
        
        String[] parts = content.split(",");
        for (String part : parts) {
            try {
                result.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }
        
        return result;
    }
    
    /**
     * Parses a JSON array of objects
     * @param arrayJson JSON array string
     * @return ArrayList of JSON object strings
     */
    public ArrayList<String> parseObjectArray(String arrayJson) {
        ArrayList<String> result = new ArrayList<>();
        
        if (arrayJson == null || arrayJson.equals("[]")) {
            return result;
        }
        
        int braceCount = 0;
        int objectStart = -1;
        
        for (int i = 0; i < arrayJson.length(); i++) {
            char ch = arrayJson.charAt(i);
            
            if (ch == '{') {
                if (braceCount == 0) {
                    objectStart = i;
                }
                braceCount++;
            } else if (ch == '}') {
                braceCount--;
                if (braceCount == 0 && objectStart >= 0) {
                    result.add(arrayJson.substring(objectStart, i + 1));
                    objectStart = -1;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Unescapes JSON string
     * @param text Escaped text
     * @return Unescaped text
     */
    public String unescapeJson(String text) {
        if (text == null) return null;
        return text.replace("\\\"", "\"")
                   .replace("\\\\", "\\")
                   .replace("\\n", "\n")
                   .replace("\\r", "\r")
                   .replace("\\t", "\t");
    }
}
