package com.monopoly.model.tile;

import java.io.Serializable;

/**
 * Represents a Tax tile on the board (Income Tax, Luxury Tax).
 */
public class TaxTile extends Tile implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int INCOME_TAX_AMOUNT = 200;
    public static final int LUXURY_TAX_AMOUNT = 100;

    private final int taxAmount;
    private final boolean isIncomeTax;

    /**
     * Creates a Tax tile.
     * @param id unique identifier
     * @param name tile name
     * @param position board position
     * @param taxAmount amount of tax to pay
     * @param isIncomeTax true for income tax, false for luxury tax
     */
    public TaxTile(int id, String name, int position, int taxAmount, boolean isIncomeTax) {
        super(id, name, position, isIncomeTax ? TileType.INCOME_TAX : TileType.LUXURY_TAX);
        this.taxAmount = taxAmount;
        this.isIncomeTax = isIncomeTax;
    }
    
    /**
     * Creates a Tax tile with simplified parameters.
     * @param position board position (also used as id)
     * @param name tile name
     * @param taxAmount amount of tax to pay
     */
    public TaxTile(int position, String name, int taxAmount) {
        super(position, name, position, name.contains("Income") ? TileType.INCOME_TAX : TileType.LUXURY_TAX);
        this.taxAmount = taxAmount;
        this.isIncomeTax = name.contains("Income");
    }

    /**
     * Creates an Income Tax tile.
     */
    public static TaxTile createIncomeTax() {
        return new TaxTile(4, "Income Tax", 4, INCOME_TAX_AMOUNT, true);
    }

    /**
     * Creates a Luxury Tax tile.
     */
    public static TaxTile createLuxuryTax() {
        return new TaxTile(38, "Luxury Tax", 38, LUXURY_TAX_AMOUNT, false);
    }

    public int getTaxAmount() {
        return taxAmount;
    }

    public boolean isIncomeTax() {
        return isIncomeTax;
    }

    @Override
    public void onLand() {
        // Player pays tax (handled by GameEngine)
    }

    @Override
    public void onPass() {
        // No action when passing
    }

    @Override
    public String toString() {
        return "TaxTile{" +
                "name='" + getName() + '\'' +
                ", position=" + getPosition() +
                ", tax=$" + taxAmount +
                '}';
    }
}
