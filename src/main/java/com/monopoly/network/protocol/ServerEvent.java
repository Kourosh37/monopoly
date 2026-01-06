package com.monopoly.network.protocol;

/**
 * Represents an event sent from server to client.
 * Contains the event type and associated data.
 */
public class ServerEvent extends Message {

    // TODO: Implement eventType field (subset of MessageType for server events)
    // TODO: Implement data field (the payload - could be GameState, String, etc.)
    // TODO: Implement targetPlayerId field (optional, for targeted messages)
    
    // Event types and their data:
    // STATE_UPDATE: GameState (full or delta)
    // EVENT_LOG: String (description)
    // ERROR: String (error message)
    // GAME_START: Initial GameState
    // GAME_END: winnerId, winnerName
    // TURN_START: currentPlayerId
    // AUCTION_START: propertyId, propertyName
    // AUCTION_UPDATE: currentBid, highestBidderId
    // AUCTION_END: winnerId, winningBid
    // TRADE_PROPOSAL: TradeProposal
    // PLAYER_JOINED: playerId, playerName
    // PLAYER_LEFT: playerId, reason
    // PLAYER_BANKRUPT: playerId
    // DICE_RESULT: die1, die2, total, isDoubles
    // CARD_DRAWN: cardType, cardDescription
    // JAIL_STATUS: playerId, turnsRemaining
    
    // TODO: Implement constructor(MessageType eventType, Object data)
    // TODO: Implement getEventType()
    // TODO: Implement getData()
    // TODO: Implement getTargetPlayerId()
    // TODO: Implement setTargetPlayerId(int playerId)
    // TODO: Implement isBroadcast() - true if no specific target
    // TODO: Override serialize()
    // TODO: Implement static fromJson(String json) - factory method

}
