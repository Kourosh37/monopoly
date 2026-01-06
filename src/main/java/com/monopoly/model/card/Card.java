package com.monopoly.model.card;

import java.io.Serializable;

/**
 * Abstract base class for Chance and Community Chest cards.
 * Cards are managed using a Queue (FIFO) structure.
 */
public abstract class Card implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final int id;
    protected final String description;
    protected final CardType cardType;
    protected final CardEffect effect;
    protected final int primaryValue;    // Main value (money amount, position, etc.)
    protected final int secondaryValue;  // Secondary value (for repairs: hotel cost)

    /**
     * Creates a card with a single value.
     */
    public Card(int id, String description, CardType cardType, CardEffect effect, int primaryValue) {
        this(id, description, cardType, effect, primaryValue, 0);
    }

    /**
     * Creates a card with primary and secondary values.
     */
    public Card(int id, String description, CardType cardType, CardEffect effect, 
                int primaryValue, int secondaryValue) {
        this.id = id;
        this.description = description;
        this.cardType = cardType;
        this.effect = effect;
        this.primaryValue = primaryValue;
        this.secondaryValue = secondaryValue;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public CardType getCardType() {
        return cardType;
    }

    public CardEffect getEffect() {
        return effect;
    }

    public int getPrimaryValue() {
        return primaryValue;
    }

    public int getSecondaryValue() {
        return secondaryValue;
    }

    /**
     * Checks if this card can be kept by the player (Get Out of Jail Free).
     */
    public boolean isKeepable() {
        return effect == CardEffect.GET_OUT_OF_JAIL_FREE;
    }

    /**
     * Checks if this card requires the player to move.
     */
    public boolean requiresMovement() {
        return effect.requiresMovement();
    }

    /**
     * Checks if this card affects other players.
     */
    public boolean affectsOtherPlayers() {
        return effect.affectsOtherPlayers();
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", type=" + cardType +
                ", effect=" + effect +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return id == card.id && cardType == card.cardType;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id) * 31 + cardType.hashCode();
    }
}
