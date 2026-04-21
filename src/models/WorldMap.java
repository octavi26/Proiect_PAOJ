package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Main map class containing layers and entities.
 */
public class WorldMap {
    private WorldLayer foreground;
    private WorldLayer background;
    private List<Entity> entities;

    public WorldMap(int width, int height) {
        this.foreground = new WorldLayer(width, height);
        this.background = new WorldLayer(width, height);
        this.entities = new ArrayList<>();
    }

    public WorldLayer getForeground() {
        return foreground;
    }

    public WorldLayer getBackground() {
        return background;
    }

    public List<Entity> getEntities() {
        return entities;
    }
}
