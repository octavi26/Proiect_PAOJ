package models;

/**
 * Represents a dangerous entity that can damage the player.
 */
public class HostileMob extends Entity {
    private String type;

    public HostileMob(int id, String type, int x, int y) {
        super(id, x, y);
        this.type = type;
        this.health = 5; // Zombies have less health than players
    }

    public String getType() { return type; }
}
