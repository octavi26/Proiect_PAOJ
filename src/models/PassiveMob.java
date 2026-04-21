package models;

/**
 * Represents a peaceful animal entity.
 */
public class PassiveMob extends Entity {
    private String name;

    public PassiveMob(int id, String name, int x, int y) {
        super(id, x, y);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
