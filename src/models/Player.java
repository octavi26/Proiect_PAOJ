package models;

/**
 * Represents the player entity.
 */
public class Player extends Entity {
    private Inventory inventory;

    public Player(int id, int x, int y) {
        super(id, x, y);
        this.inventory = new Inventory();
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
