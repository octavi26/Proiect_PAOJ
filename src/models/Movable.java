package models;

/**
 * Interface representing any object that has a position in the world and can move.
 * It defines the behavior for coordinate management.
 */
public interface Movable {
    int getX();
    void setX(int x);
    int getY();
    void setY(int y);
}
