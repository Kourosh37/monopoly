package com.monopoly.model.card;

import com.monopoly.datastructures.ArrayList;
import java.io.Serializable;

/**
 * Represents a Chance card.
 * Effects can include: move to location, pay/receive money, go to jail, etc.
 */
public class ChanceCard extends Card implements Serializable {
    private static final long serialVersionUID = 1L;

    public ChanceCard(int id, String description, CardEffect effect, int primaryValue) {
        super(id, description, CardType.CHANCE, effect, primaryValue);
    }

    public ChanceCard(int id, String description, CardEffect effect, int primaryValue, int secondaryValue) {
        super(id, description, CardType.CHANCE, effect, primaryValue, secondaryValue);
    }

    /**
     * Creates the standard set of Chance cards.
     */
    public static ArrayList<ChanceCard> createStandardDeck() {
        ArrayList<ChanceCard> cards = new ArrayList<>();
        
        // Movement cards
        cards.add(new ChanceCard(1, "Advance to GO. Collect $200.", 
                CardEffect.ADVANCE_TO_GO, 0));
        cards.add(new ChanceCard(2, "Advance to Illinois Avenue.", 
                CardEffect.ADVANCE_TO, 24));
        cards.add(new ChanceCard(3, "Advance to St. Charles Place.", 
                CardEffect.ADVANCE_TO, 11));
        cards.add(new ChanceCard(4, "Advance to nearest Utility. If unowned, you may buy it. If owned, throw dice and pay owner 10 times the amount thrown.", 
                CardEffect.ADVANCE_TO_NEAREST_UTILITY, 0));
        cards.add(new ChanceCard(5, "Advance to nearest Railroad. If unowned, you may buy it. If owned, pay owner twice the rental.", 
                CardEffect.ADVANCE_TO_NEAREST_RAILROAD, 0));
        cards.add(new ChanceCard(6, "Advance to nearest Railroad. If unowned, you may buy it. If owned, pay owner twice the rental.", 
                CardEffect.ADVANCE_TO_NEAREST_RAILROAD, 0));
        cards.add(new ChanceCard(7, "Advance to Boardwalk.", 
                CardEffect.ADVANCE_TO, 39));
        cards.add(new ChanceCard(8, "Advance to Reading Railroad.", 
                CardEffect.ADVANCE_TO, 5));
        cards.add(new ChanceCard(9, "Go Back 3 Spaces.", 
                CardEffect.GO_BACK, 3));
        
        // Money cards
        cards.add(new ChanceCard(10, "Bank pays you dividend of $50.", 
                CardEffect.RECEIVE_MONEY, 50));
        cards.add(new ChanceCard(11, "Your building loan matures. Collect $150.", 
                CardEffect.RECEIVE_MONEY, 150));
        cards.add(new ChanceCard(12, "Pay poor tax of $15.", 
                CardEffect.PAY_MONEY, 15));
        cards.add(new ChanceCard(13, "Speeding fine $15.", 
                CardEffect.PAY_MONEY, 15));
        
        // Jail cards
        cards.add(new ChanceCard(14, "Go to Jail. Do not pass GO. Do not collect $200.", 
                CardEffect.GO_TO_JAIL, 10));
        cards.add(new ChanceCard(15, "Get Out of Jail Free. This card may be kept until needed or sold.", 
                CardEffect.GET_OUT_OF_JAIL_FREE, 0));
        
        // Repairs
        cards.add(new ChanceCard(16, "Make general repairs on all your property. Pay $25 per house and $100 per hotel.", 
                CardEffect.GENERAL_REPAIRS, 25, 100));
        
        return cards;
    }

    @Override
    public String toString() {
        return "ChanceCard{" + description + "}";
    }
}
