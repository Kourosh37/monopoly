package com.monopoly.network.protocol;

/**
 * Represents a command sent from client to server.
 * Contains the command type and any associated parameters.
 */
public class ClientCommand extends Message {

    // TODO: Implement commandType field (subset of MessageType for client commands)
    // TODO: Implement parameters field (HashTable for key-value parameters)
    
    // Commands and their parameters:
    // HELLO: playerName
    // ROLL_DICE: (no params)
    // BUY_PROPERTY: propertyId
    // DECLINE_BUY: (no params)
    // BID: amount
    // PASS_BID: (no params)
    // PROPOSE_TRADE: targetPlayerId, offeredProperties[], requestedProperties[], offeredMoney, requestedMoney
    // ACCEPT_TRADE: (no params)
    // DECLINE_TRADE: (no params)
    // BUILD: propertyId, buildingType (HOUSE/HOTEL)
    // SELL_BUILDING: propertyId, buildingType
    // MORTGAGE: propertyId
    // UNMORTGAGE: propertyId
    // JAIL_PAY_FINE: (no params)
    // JAIL_USE_CARD: (no params)
    // JAIL_ROLL: (no params)
    // UNDO: (no params)
    // REDO: (no params)
    // END_TURN: (no params)
    
    // TODO: Implement constructor(MessageType commandType)
    // TODO: Implement getCommandType()
    // TODO: Implement setParameter(String key, Object value)
    // TODO: Implement getParameter(String key)
    // TODO: Implement hasParameter(String key)
    // TODO: Override serialize()
    // TODO: Implement static fromJson(String json) - factory method

}
