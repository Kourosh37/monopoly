package com.monopoly.network.protocol;

import com.monopoly.model.enums.TurnPhase;
import com.monopoly.datastructures.HashTable;

/**
 * Handles protocol-level message processing.
 * Parses incoming messages and creates outgoing messages.
 */
public class ProtocolHandler {

    /** Message delimiter for stream separation */
    public static final String MESSAGE_DELIMITER = "\n";
    
    /** Protocol version */
    public static final String PROTOCOL_VERSION = "1.0";
    
    /** Maximum message size in bytes */
    public static final int MAX_MESSAGE_SIZE = 1024 * 64; // 64KB
    
    /** Actions allowed in each phase */
    private final HashTable<TurnPhase, HashTable<MessageType, Boolean>> allowedActions;
    
    /**
     * Creates a new protocol handler
     */
    public ProtocolHandler() {
        this.allowedActions = new HashTable<>();
        initializeAllowedActions();
    }
    
    /**
     * Initializes the allowed actions for each turn phase
     */
    private void initializeAllowedActions() {
        // PRE_ROLL phase
        HashTable<MessageType, Boolean> preRoll = new HashTable<>();
        preRoll.put(MessageType.ROLL_DICE, true);
        preRoll.put(MessageType.JAIL_USE_CARD, true);
        preRoll.put(MessageType.JAIL_PAY_FINE, true);
        preRoll.put(MessageType.PROPOSE_TRADE, true);
        preRoll.put(MessageType.BUILD, true);
        preRoll.put(MessageType.MORTGAGE, true);
        preRoll.put(MessageType.UNMORTGAGE, true);
        allowedActions.put(TurnPhase.PRE_ROLL, preRoll);
        
        // ROLLING phase
        HashTable<MessageType, Boolean> rolling = new HashTable<>();
        // No actions during rolling animation
        allowedActions.put(TurnPhase.ROLLING, rolling);
        
        // POST_ROLL phase
        HashTable<MessageType, Boolean> postRoll = new HashTable<>();
        postRoll.put(MessageType.END_TURN, true);
        postRoll.put(MessageType.PROPOSE_TRADE, true);
        postRoll.put(MessageType.BUILD, true);
        postRoll.put(MessageType.MORTGAGE, true);
        postRoll.put(MessageType.UNMORTGAGE, true);
        allowedActions.put(TurnPhase.POST_ROLL, postRoll);
        
        // PROPERTY_DECISION phase
        HashTable<MessageType, Boolean> propertyDecision = new HashTable<>();
        propertyDecision.put(MessageType.BUY_PROPERTY, true);
        propertyDecision.put(MessageType.DECLINE_BUY, true);
        allowedActions.put(TurnPhase.PROPERTY_DECISION, propertyDecision);
        
        // AUCTION phase
        HashTable<MessageType, Boolean> auction = new HashTable<>();
        auction.put(MessageType.BID, true);
        auction.put(MessageType.PASS_BID, true);
        allowedActions.put(TurnPhase.AUCTION, auction);
        
        // PAYING_RENT phase
        HashTable<MessageType, Boolean> payingRent = new HashTable<>();
        payingRent.put(MessageType.MORTGAGE, true);
        payingRent.put(MessageType.SELL_BUILDING, true);
        allowedActions.put(TurnPhase.PAYING_RENT, payingRent);
        
        // CARD_ACTION phase
        HashTable<MessageType, Boolean> cardAction = new HashTable<>();
        // Auto-handled
        allowedActions.put(TurnPhase.CARD_ACTION, cardAction);
        
        // IN_JAIL phase
        HashTable<MessageType, Boolean> inJail = new HashTable<>();
        inJail.put(MessageType.ROLL_DICE, true);
        inJail.put(MessageType.JAIL_PAY_FINE, true);
        inJail.put(MessageType.JAIL_USE_CARD, true);
        inJail.put(MessageType.PROPOSE_TRADE, true);
        inJail.put(MessageType.BUILD, true);
        inJail.put(MessageType.MORTGAGE, true);
        allowedActions.put(TurnPhase.IN_JAIL, inJail);
        
        // BANKRUPTCY phase
        HashTable<MessageType, Boolean> bankruptcy = new HashTable<>();
        bankruptcy.put(MessageType.MORTGAGE, true);
        bankruptcy.put(MessageType.SELL_BUILDING, true);
        allowedActions.put(TurnPhase.BANKRUPTCY, bankruptcy);
        
        // TRADING phase
        HashTable<MessageType, Boolean> trading = new HashTable<>();
        trading.put(MessageType.ACCEPT_TRADE, true);
        trading.put(MessageType.DECLINE_TRADE, true);
        allowedActions.put(TurnPhase.TRADING, trading);
        
        // WAITING phase
        HashTable<MessageType, Boolean> waiting = new HashTable<>();
        // Can only wait
        allowedActions.put(TurnPhase.WAITING, waiting);
        
        // GAME_OVER phase
        HashTable<MessageType, Boolean> gameOver = new HashTable<>();
        gameOver.put(MessageType.DISCONNECT, true);
        allowedActions.put(TurnPhase.GAME_OVER, gameOver);
        
        // BUILDING phase
        HashTable<MessageType, Boolean> building = new HashTable<>();
        building.put(MessageType.BUILD, true);
        building.put(MessageType.END_TURN, true);
        allowedActions.put(TurnPhase.BUILDING, building);
    }
    
    /**
     * Parses a raw message string into a Message object
     * @param rawMessage Raw message string (JSON)
     * @return Parsed Message or null if invalid
     */
    public Message parseMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            return null;
        }
        
        // Check size limit
        if (rawMessage.length() > MAX_MESSAGE_SIZE) {
            return null;
        }
        
        // Determine if it's a command or event by checking type
        String typeStr = Message.extractJsonField(rawMessage, "type");
        if (typeStr == null) {
            return null;
        }
        
        try {
            MessageType type = MessageType.valueOf(typeStr);
            
            // Check if it's a client command or server event
            if (isClientCommand(type)) {
                return ClientCommand.fromJson(rawMessage);
            } else {
                return ServerEvent.fromJson(rawMessage);
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Checks if a message type is a client command
     * @param type Message type
     * @return true if client command
     */
    private boolean isClientCommand(MessageType type) {
        switch (type) {
            case HELLO:
            case ROLL_DICE:
            case BUY_PROPERTY:
            case DECLINE_BUY:
            case BID:
            case PASS_BID:
            case BUILD:
            case SELL_BUILDING:
            case MORTGAGE:
            case UNMORTGAGE:
            case PROPOSE_TRADE:
            case ACCEPT_TRADE:
            case DECLINE_TRADE:
            case JAIL_PAY_FINE:
            case JAIL_USE_CARD:
            case END_TURN:
            case DISCONNECT:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Serializes a message to string
     * @param message Message to serialize
     * @return JSON string
     */
    public String serializeMessage(Message message) {
        if (message == null) {
            return null;
        }
        return message.serialize() + MESSAGE_DELIMITER;
    }
    
    /**
     * Validates a message for integrity
     * @param message Message to validate
     * @return true if valid
     */
    public boolean validateMessage(Message message) {
        if (message == null) {
            return false;
        }
        
        if (message.getMessageId() == null || message.getMessageId().isEmpty()) {
            return false;
        }
        
        if (message.getMessageType() == null) {
            return false;
        }
        
        if (message.getTimestamp() <= 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if a command is valid in the given turn phase
     * @param command Command to check
     * @param phase Current turn phase
     * @return true if valid
     */
    public boolean isValidCommand(ClientCommand command, TurnPhase phase) {
        if (command == null || phase == null) {
            return false;
        }
        
        MessageType commandType = command.getCommandType();
        
        // Some commands are always allowed
        if (commandType == MessageType.DISCONNECT) {
            return true;
        }
        
        // Check phase-specific rules
        HashTable<MessageType, Boolean> allowed = allowedActions.get(phase);
        if (allowed == null) {
            return false;
        }
        
        Boolean isAllowed = allowed.get(commandType);
        return isAllowed != null && isAllowed;
    }
    
    // ==================== Factory Methods for Commands ====================
    
    /**
     * Creates a HELLO command
     * @param playerName Player name
     * @return ClientCommand
     */
    public ClientCommand createHelloCommand(String playerName) {
        return ClientCommand.createHello(playerName);
    }
    
    /**
     * Creates a ROLL_DICE command
     * @return ClientCommand
     */
    public ClientCommand createRollDiceCommand() {
        return ClientCommand.createRollDice(-1);
    }
    
    /**
     * Creates a BUY_PROPERTY command
     * @param propertyId Property ID
     * @return ClientCommand
     */
    public ClientCommand createBuyPropertyCommand(int propertyId) {
        return ClientCommand.createBuyProperty(-1, propertyId);
    }
    
    /**
     * Creates a DECLINE_BUY command
     * @return ClientCommand
     */
    public ClientCommand createDeclineBuyCommand() {
        return ClientCommand.createDeclineBuy(-1);
    }
    
    /**
     * Creates a BID command
     * @param amount Bid amount
     * @return ClientCommand
     */
    public ClientCommand createBidCommand(int amount) {
        return ClientCommand.createBid(-1, amount);
    }
    
    /**
     * Creates a PASS_BID command
     * @return ClientCommand
     */
    public ClientCommand createPassBidCommand() {
        return ClientCommand.createPassBid(-1);
    }
    
    /**
     * Creates a BUILD command
     * @param propertyId Property ID
     * @param buildingType Building type (house or hotel)
     * @return ClientCommand
     */
    public ClientCommand createBuildCommand(int propertyId, String buildingType) {
        return ClientCommand.createBuild(-1, propertyId, buildingType);
    }
    
    /**
     * Creates a MORTGAGE command
     * @param propertyId Property ID
     * @return ClientCommand
     */
    public ClientCommand createMortgageCommand(int propertyId) {
        return ClientCommand.createMortgage(-1, propertyId);
    }
    
    /**
     * Creates an END_TURN command
     * @return ClientCommand
     */
    public ClientCommand createEndTurnCommand() {
        return ClientCommand.createEndTurn(-1);
    }
    
    /**
     * Creates a PAY_JAIL_FINE command
     * @return ClientCommand
     */
    public ClientCommand createPayJailFineCommand() {
        return ClientCommand.createJailPayFine(-1);
    }
    
    /**
     * Creates a USE_JAIL_CARD command
     * @return ClientCommand
     */
    public ClientCommand createUseJailCardCommand() {
        return ClientCommand.createJailUseCard(-1);
    }
    
    /**
     * Creates a DISCONNECT command
     * @return ClientCommand
     */
    public ClientCommand createDisconnectCommand() {
        return ClientCommand.createDisconnect(-1);
    }
    
    // ==================== Factory Methods for Events ====================
    
    /**
     * Creates a STATE_UPDATE event
     * @param gameStateJson Game state as JSON
     * @return ServerEvent
     */
    public ServerEvent createStateUpdateEvent(String gameStateJson) {
        return ServerEvent.createStateUpdate(gameStateJson);
    }
    
    /**
     * Creates an EVENT_LOG event
     * @param description Event description
     * @return ServerEvent
     */
    public ServerEvent createEventLogEvent(String description) {
        return ServerEvent.createEventLog(description);
    }
    
    /**
     * Creates an ERROR event
     * @param error Error message
     * @return ServerEvent
     */
    public ServerEvent createErrorEvent(String error) {
        return ServerEvent.createError(error);
    }
    
    /**
     * Creates a targeted ERROR event
     * @param error Error message
     * @param playerId Target player
     * @return ServerEvent
     */
    public ServerEvent createErrorEvent(String error, int playerId) {
        return ServerEvent.createError(error, playerId);
    }
    
    /**
     * Creates a GAME_START event
     * @return ServerEvent
     */
    public ServerEvent createGameStartEvent() {
        return ServerEvent.createGameStart();
    }
    
    /**
     * Creates a GAME_END event
     * @param winnerId Winner's ID
     * @param winnerName Winner's name
     * @return ServerEvent
     */
    public ServerEvent createGameEndEvent(int winnerId, String winnerName) {
        return ServerEvent.createGameEnd(winnerId, winnerName);
    }
    
    /**
     * Creates a TURN_START event
     * @param currentPlayerId Current player's ID
     * @return ServerEvent
     */
    public ServerEvent createTurnStartEvent(int currentPlayerId) {
        return ServerEvent.createTurnStart(currentPlayerId);
    }
    
    /**
     * Creates a DICE_RESULT event
     * @param die1 First die
     * @param die2 Second die
     * @return ServerEvent
     */
    public ServerEvent createDiceResultEvent(int die1, int die2) {
        return ServerEvent.createDiceResult(die1, die2);
    }
    
    /**
     * Creates a PLAYER_JOINED event
     * @param playerId Player ID
     * @param playerName Player name
     * @return ServerEvent
     */
    public ServerEvent createPlayerJoinedEvent(int playerId, String playerName) {
        return ServerEvent.createPlayerJoined(playerId, playerName);
    }
    
    /**
     * Creates a PLAYER_LEFT event
     * @param playerId Player ID
     * @param reason Disconnect reason
     * @return ServerEvent
     */
    public ServerEvent createPlayerLeftEvent(int playerId, String reason) {
        return ServerEvent.createPlayerLeft(playerId, reason);
    }
    
    /**
     * Creates an AUCTION_START event
     * @param propertyId Property ID
     * @param propertyName Property name
     * @return ServerEvent
     */
    public ServerEvent createAuctionStartEvent(int propertyId, String propertyName) {
        return ServerEvent.createAuctionStart(propertyId, propertyName);
    }
    
    /**
     * Creates an AUCTION_UPDATE event
     * @param currentBid Current bid
     * @param highestBidderId Highest bidder
     * @return ServerEvent
     */
    public ServerEvent createAuctionUpdateEvent(int currentBid, int highestBidderId) {
        return ServerEvent.createAuctionUpdate(currentBid, highestBidderId);
    }
    
    /**
     * Creates an AUCTION_END event
     * @param winnerId Winner's ID
     * @param winningBid Winning bid
     * @return ServerEvent
     */
    public ServerEvent createAuctionEndEvent(int winnerId, int winningBid) {
        return ServerEvent.createAuctionEnd(winnerId, winningBid);
    }
    
    /**
     * Creates a CARD_DRAWN event
     * @param cardType Card type
     * @param cardDescription Card description
     * @return ServerEvent
     */
    public ServerEvent createCardDrawnEvent(String cardType, String cardDescription) {
        return ServerEvent.createCardDrawn(cardType, cardDescription);
    }
    
    /**
     * Creates a PLAYER_BANKRUPT event
     * @param playerId Bankrupt player's ID
     * @return ServerEvent
     */
    public ServerEvent createPlayerBankruptEvent(int playerId) {
        return ServerEvent.createPlayerBankrupt(playerId);
    }
    
    @Override
    public String toString() {
        return "ProtocolHandler{version=" + PROTOCOL_VERSION + "}";
    }
}
