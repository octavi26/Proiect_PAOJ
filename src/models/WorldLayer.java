package models;

/**
 * Manages a 2D layer of the world.
 */
public class WorldLayer {
    private GameObject[][] grid;
    private int width;
    private int height;

    public WorldLayer(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new GameObject[height][width];
    }

    public GameObject getObject(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return grid[y][x];
        }
        return null;
    }

    public void setObject(int x, int y, GameObject obj) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            grid[y][x] = obj;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
