package com.monopoly.logic;

import com.monopoly.model.game.TurnPhase;

/**
 * Manages turn flow and turn phases.
 * Implements State Machine: TURN_START -> ROLL -> MOVE -> DECISION -> TURN_END
 */
public class TurnManager {

    // TODO: Implement currentPlayerIndex field
    // TODO: Implement currentPhase field (TurnPhase enum)
    // TODO: Implement playersInGame field (list of active player IDs)
    // TODO: Implement dice field
    // TODO: Implement lastRollWasDoubles field
    // TODO: Implement doublesCount field
    
    // TODO: Implement constructor(int playerCount)
    // TODO: Implement startTurn() - begins a new turn
    // TODO: Implement getCurrentPlayerId()
    // TODO: Implement getCurrentPhase()
    // TODO: Implement setPhase(TurnPhase phase)
    // TODO: Implement advancePhase() - moves to next phase
    // TODO: Implement endTurn() - completes current turn
    // TODO: Implement nextPlayer() - advances to next player
    // TODO: Implement skipPlayer(int playerId) - removes from turn order (bankruptcy)
    // TODO: Implement handleDoubles() - checks for extra turn or jail
    // TODO: Implement rollDice() - returns dice result
    // TODO: Implement canRollAgain() - true if rolled doubles and not going to jail
    // TODO: Implement resetDoublesCount()
    // TODO: Implement isActionAllowed(String action) - validates against current phase
    // TODO: Implement getRemainingPlayers()

}
