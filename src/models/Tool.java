package models;

/**
 * Represents a tool with durability and specific effectiveness.
 * Part of the requirement for at least 8 types of objects.
 */
public class Tool extends GameObject {
    private int durability;
    private int power;

    public Tool(int id, String name, int durability, int power) {
        super(id, name);
        this.durability = durability;
        this.power = power;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    @Override
    public String toString() {
        return name + " (Durability: " + durability + ")";
    }
}
