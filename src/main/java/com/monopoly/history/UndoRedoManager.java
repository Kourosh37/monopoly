package com.monopoly.history;

import com.monopoly.datastructures.Stack;

/**
 * Manages Undo and Redo functionality using two stacks.
 * Logs reversible actions (Move, Buy, Build).
 * Some actions like Bankruptcy or Card Effects may not be reversible.
 */
public class UndoRedoManager {

    // TODO: Implement undoStack field (Stack<GameAction>)
    // TODO: Implement redoStack field (Stack<GameAction>)
    // TODO: Implement maxHistorySize field (to prevent memory issues)
    
    // TODO: Implement constructor()
    // TODO: Implement recordAction(GameAction action)
    // TODO: Implement canUndo() - checks if undo stack not empty
    // TODO: Implement canRedo() - checks if redo stack not empty
    // TODO: Implement undo() - pops from undo, pushes to redo, returns action
    // TODO: Implement redo() - pops from redo, pushes to undo, returns action
    // TODO: Implement clearRedoStack() - called when new action is recorded
    // TODO: Implement getUndoStackSize()
    // TODO: Implement getRedoStackSize()
    // TODO: Implement peekUndo() - view last action without removing
    // TODO: Implement peekRedo()
    // TODO: Implement clearAll() - clears both stacks

}
