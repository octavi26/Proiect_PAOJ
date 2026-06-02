package models;

/**
 * Abstract class representing any entity in the game world with coordinates and an ID.
 */
public abstract class Entity implements Identifiable, Movable {
    protected int id;
    protected int x;
    protected int y;
    protected int health = 10;
    protected boolean alive = true;

    public Entity(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getHealth() { return health; }
    public void setHealth(int health) { 
        this.health = health; 
        if (this.health <= 0) this.alive = false;
    }
    public boolean isAlive() { return alive; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
