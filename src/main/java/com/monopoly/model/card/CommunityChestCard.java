package com.monopoly.model.card;

import com.monopoly.datastructures.ArrayList;
import java.io.Serializable;

/**
 * Represents a Community Chest card.
 * Effects can include: pay/receive money, go to jail, etc.
 */
public class CommunityChestCard extends Card implements Serializable {
    private static final long serialVersionUID = 1L;

    public CommunityChestCard(int id, String description, CardEffect effect, int primaryValue) {
        super(id, description, CardType.COMMUNITY_CHEST, effect, primaryValue);
    }

    public CommunityChestCard(int id, String description, CardEffect effect, int primaryValue, int secondaryValue) {
        super(id, description, CardType.COMMUNITY_CHEST, effect, primaryValue, secondaryValue);
    }

    /**
     * Creates the standard set of Community Chest cards.
     */
    public static ArrayList<CommunityChestCard> createStandardDeck() {
        ArrayList<CommunityChestCard> cards = new ArrayList<>();
        
        // Movement cards
        cards.add(new CommunityChestCard(1, "Advance to GO. Collect $200.", 
                CardEffect.ADVANCE_TO_GO, 0));
        
        // Money cards - receive
        cards.add(new CommunityChestCard(2, "Bank error in your favor. Collect $200.", 
                CardEffect.RECEIVE_MONEY, 200));
        cards.add(new CommunityChestCard(3, "Doctor's fees. Pay $50.", 
                CardEffect.PAY_MONEY, 50));
        cards.add(new CommunityChestCard(4, "From sale of stock you get $50.", 
                CardEffect.RECEIVE_MONEY, 50));
        cards.add(new CommunityChestCard(5, "Holiday fund matures. Collect $100.", 
                CardEffect.RECEIVE_MONEY, 100));
        cards.add(new CommunityChestCard(6, "Income tax refund. Collect $20.", 
                CardEffect.RECEIVE_MONEY, 20));
        cards.add(new CommunityChestCard(7, "Life insurance matures. Collect $100.", 
                CardEffect.RECEIVE_MONEY, 100));
        cards.add(new CommunityChestCard(8, "Receive $25 consultancy fee.", 
                CardEffect.RECEIVE_MONEY, 25));
        cards.add(new CommunityChestCard(9, "You inherit $100.", 
                CardEffect.RECEIVE_MONEY, 100));
        cards.add(new CommunityChestCard(10, "You have won second prize in a beauty contest. Collect $10.", 
                CardEffect.RECEIVE_MONEY, 10));
        
        // Money cards - pay
        cards.add(new CommunityChestCard(11, "Hospital fees. Pay $100.", 
                CardEffect.PAY_MONEY, 100));
        cards.add(new CommunityChestCard(12, "School fees. Pay $50.", 
                CardEffect.PAY_MONEY, 50));
        
        // Cards involving other players
        cards.add(new CommunityChestCard(13, "It is your birthday. Collect $10 from every player.", 
                CardEffect.RECEIVE_FROM_EACH_PLAYER, 10));
        cards.add(new CommunityChestCard(14, "Grand Opera Night. Collect $50 from every player.", 
                CardEffect.RECEIVE_FROM_EACH_PLAYER, 50));
        
        // Jail cards
        cards.add(new CommunityChestCard(15, "Go to Jail. Do not pass GO. Do not collect $200.", 
                CardEffect.GO_TO_JAIL, 10));
        cards.add(new CommunityChestCard(16, "Get Out of Jail Free. This card may be kept until needed or sold.", 
                CardEffect.GET_OUT_OF_JAIL_FREE, 0));
        
        // Repairs
        cards.add(new CommunityChestCard(17, "You are assessed for street repairs. Pay $40 per house and $115 per hotel.", 
                CardEffect.STREET_REPAIRS, 40, 115));
        
        return cards;
    }

    @Override
    public String toString() {
        return "CommunityChestCard{" + description + "}";
    }
}
