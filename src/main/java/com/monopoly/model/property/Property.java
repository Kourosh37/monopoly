package com.monopoly.model.property;

import java.io.Serializable;

/**
 * Represents a property that can be owned, developed, and generates rent.
 */
public class Property implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final ColorGroup colorGroup;
    private final int purchasePrice;
    private int baseRent;
    private int rentWithColorSet;
    private int[] rentWithHouses; // rent for 1, 2, 3, 4 houses
    private int rentWithHotel;
    private final int houseCost;
    private final int hotelCost;
    private final int mortgageValue;
    
    // Mutable state
    private int ownerId;
    private int numberOfHouses;
    private boolean hasHotel;
    private boolean mortgaged;

    /**
     * Full constructor for standard properties.
     */
    public Property(int id, String name, ColorGroup colorGroup, int purchasePrice,
                   int baseRent, int rentColorSet, int rent1House, int rent2Houses,
                   int rent3Houses, int rent4Houses, int rentHotel,
                   int houseCost, int hotelCost, int mortgageValue) {
        this.id = id;
        this.name = name;
        this.colorGroup = colorGroup;
        this.purchasePrice = purchasePrice;
        this.baseRent = baseRent;
        this.rentWithColorSet = rentColorSet;
        this.rentWithHouses = new int[]{rent1House, rent2Houses, rent3Houses, rent4Houses};
        this.rentWithHotel = rentHotel;
        this.houseCost = houseCost;
        this.hotelCost = hotelCost;
        this.mortgageValue = mortgageValue;
        
        this.ownerId = -1; // Unowned
        this.numberOfHouses = 0;
        this.hasHotel = false;
        this.mortgaged = false;
    }

    /**
     * Simplified constructor (calculates some values automatically).
     */
    public Property(int id, String name, ColorGroup colorGroup, int purchasePrice,
                   int baseRent, int houseCost) {
        this(id, name, colorGroup, purchasePrice,
             baseRent, baseRent * 2,
             baseRent * 5, baseRent * 15, baseRent * 45, baseRent * 80, baseRent * 125,
             houseCost, houseCost, purchasePrice / 2);
    }
    
    /**
     * Constructor without rent (rent must be set via setRentValues).
     * Used for initial property creation before rent configuration.
     */
    public Property(int id, String name, ColorGroup colorGroup, int purchasePrice, int houseCost) {
        this.id = id;
        this.name = name;
        this.colorGroup = colorGroup;
        this.purchasePrice = purchasePrice;
        this.baseRent = 0; // Will be set via setRentValues
        this.rentWithColorSet = 0;
        this.rentWithHouses = new int[4];
        this.rentWithHotel = 0;
        this.houseCost = houseCost;
        this.hotelCost = houseCost;
        this.mortgageValue = purchasePrice / 2;
        
        this.ownerId = -1;
        this.numberOfHouses = 0;
        this.hasHotel = false;
        this.mortgaged = false;
    }
    
    /**
     * Sets all rent values for this property.
     * Must be called after using the constructor without rent values.
     */
    public void setRentValues(int baseRent, int rent1House, int rent2Houses, 
                              int rent3Houses, int rent4Houses, int rentHotel) {
        this.baseRent = baseRent;
        this.rentWithColorSet = baseRent * 2;
        this.rentWithHouses = new int[]{rent1House, rent2Houses, rent3Houses, rent4Houses};
        this.rentWithHotel = rentHotel;
    }
    
    /**
     * Resets all buildings on this property.
     */
    public void resetBuildings() {
        numberOfHouses = 0;
        hasHotel = false;
    }

    // === Getters ===

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ColorGroup getColorGroup() {
        return colorGroup;
    }
    
    /**
     * Alias for getColorGroup() for Serializer compatibility.
     */
    public ColorGroup getGroup() {
        return colorGroup;
    }

    public int getPurchasePrice() {
        return purchasePrice;
    }
    
    /**
     * Alias for getPurchasePrice() for compatibility.
     */
    public int getPrice() {
        return purchasePrice;
    }

    public int getBaseRent() {
        return baseRent;
    }

    public int getHouseCost() {
        return houseCost;
    }

    public int getHotelCost() {
        return hotelCost;
    }

    public int getMortgageValue() {
        return mortgageValue;
    }

    public int getUnmortgageCost() {
        return (int) (mortgageValue * 1.1); // 10% interest
    }

    // === Ownership ===

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isOwned() {
        return ownerId >= 0;
    }

    // === Houses and Hotels ===

    public int getNumberOfHouses() {
        return numberOfHouses;
    }

    public boolean hasHotel() {
        return hasHotel;
    }

    public boolean canBuildHouse(boolean ownsColorSet) {
        if (!colorGroup.isStandardProperty()) return false;
        if (!ownsColorSet) return false;
        if (mortgaged) return false;
        if (hasHotel) return false;
        return numberOfHouses < 4;
    }

    public boolean addHouse() {
        if (numberOfHouses < 4 && !hasHotel) {
            numberOfHouses++;
            return true;
        }
        return false;
    }

    public boolean removeHouse() {
        if (numberOfHouses > 0) {
            numberOfHouses--;
            return true;
        }
        return false;
    }

    public boolean canBuildHotel(boolean ownsColorSet) {
        if (!colorGroup.isStandardProperty()) return false;
        if (!ownsColorSet) return false;
        if (mortgaged) return false;
        return numberOfHouses == 4 && !hasHotel;
    }

    public boolean buildHotel() {
        if (numberOfHouses == 4 && !hasHotel) {
            numberOfHouses = 0;
            hasHotel = true;
            return true;
        }
        return false;
    }

    /**
     * Alias for buildHotel().
     */
    public boolean addHotel() {
        return buildHotel();
    }

    public boolean removeHotel() {
        if (hasHotel) {
            hasHotel = false;
            numberOfHouses = 4; // Convert back to 4 houses
            return true;
        }
        return false;
    }

    public void removeAllBuildings() {
        numberOfHouses = 0;
        hasHotel = false;
    }

    /**
     * Sets the number of houses directly (for hotel downgrade scenarios).
     */
    public void setNumberOfHouses(int count) {
        this.numberOfHouses = Math.max(0, Math.min(4, count));
    }

    public int getTotalBuildings() {
        return hasHotel ? 5 : numberOfHouses;
    }

    // === Mortgage ===

    public boolean isMortgaged() {
        return mortgaged;
    }

    public boolean canMortgage() {
        return !mortgaged && numberOfHouses == 0 && !hasHotel;
    }

    public boolean mortgage() {
        if (canMortgage()) {
            mortgaged = true;
            return true;
        }
        return false;
    }

    public boolean unmortgage() {
        if (mortgaged) {
            mortgaged = false;
            return true;
        }
        return false;
    }

    // === Rent Calculation ===

    /**
     * Calculates current rent based on development level.
     * @param ownsColorSet whether owner has full color set
     * @return rent amount
     */
    public int getCurrentRent(boolean ownsColorSet) {
        if (mortgaged) return 0;
        
        if (hasHotel) {
            return rentWithHotel;
        } else if (numberOfHouses > 0) {
            return rentWithHouses[numberOfHouses - 1];
        } else if (ownsColorSet) {
            return rentWithColorSet;
        }
        return baseRent;
    }

    /**
     * Gets rent with houses array.
     */
    public int getRentWithHouses(int houses) {
        if (houses < 1 || houses > 4) return baseRent;
        return rentWithHouses[houses - 1];
    }

    public int getRentWithHotel() {
        return rentWithHotel;
    }

    /**
     * Gets rent when owner owns the complete color set.
     */
    public int getRentWithColorSet() {
        return rentWithColorSet;
    }

    /**
     * Checks if property has any buildings (houses or hotel).
     */
    public boolean hasBuildings() {
        return numberOfHouses > 0 || hasHotel;
    }

    // === Value Calculations ===

    /**
     * Calculates total value of property including buildings.
     */
    public int getTotalValue() {
        if (mortgaged) {
            return mortgageValue;
        }
        int value = purchasePrice;
        value += numberOfHouses * houseCost;
        if (hasHotel) {
            value += hotelCost;
        }
        return value;
    }

    /**
     * Calculates value of buildings only.
     */
    public int getBuildingValue() {
        int value = numberOfHouses * houseCost;
        if (hasHotel) {
            value += hotelCost;
        }
        return value;
    }

    /**
     * Calculates sale value of buildings (half price).
     */
    public int getBuildingSaleValue() {
        return getBuildingValue() / 2;
    }

    // === Utility Methods ===

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Property property = (Property) obj;
        return id == property.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Property{id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", colorGroup=").append(colorGroup);
        sb.append(", price=$").append(purchasePrice);
        sb.append(", owner=").append(ownerId >= 0 ? ownerId : "Bank");
        if (mortgaged) {
            sb.append(", MORTGAGED");
        } else {
            if (hasHotel) {
                sb.append(", hotel");
            } else if (numberOfHouses > 0) {
                sb.append(", houses=").append(numberOfHouses);
            }
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Creates a display string for the property.
     */
    public String getDisplayInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n");
        sb.append("Price: $").append(purchasePrice).append("\n");
        sb.append("Rent: $").append(baseRent).append("\n");
        if (colorGroup.isStandardProperty()) {
            sb.append("With Color Set: $").append(rentWithColorSet).append("\n");
            sb.append("With 1 House: $").append(rentWithHouses[0]).append("\n");
            sb.append("With 2 Houses: $").append(rentWithHouses[1]).append("\n");
            sb.append("With 3 Houses: $").append(rentWithHouses[2]).append("\n");
            sb.append("With 4 Houses: $").append(rentWithHouses[3]).append("\n");
            sb.append("With Hotel: $").append(rentWithHotel).append("\n");
            sb.append("House Cost: $").append(houseCost).append("\n");
        }
        sb.append("Mortgage: $").append(mortgageValue);
        return sb.toString();
    }
}
