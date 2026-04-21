package models;

/**
 * Represents an item dropped in the world.
 */
public class DroppedItem extends Entity {
    private String itemName;
    private int amount;

    public DroppedItem(int id, String itemName, int amount, int x, int y) {
        super(id, x, y);
        this.itemName = itemName;
        this.amount = amount;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
