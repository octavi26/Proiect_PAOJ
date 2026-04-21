package models;

/**
 * Represents a tool with durability.
 */
public class Tool extends GameObject {
    private int durability;

    public Tool(int id, String name, int durability) {
        super(id, name);
        this.durability = durability;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }
}
