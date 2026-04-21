package models;

import java.util.TreeMap;

/**
 * Manages the player's resources using a TreeMap to keep items sorted.
 */
public class Inventory {
    private TreeMap<String, Integer> items;

    public Inventory() {
        this.items = new TreeMap<>();
    }

    public void addItem(String name, int amount) {
        items.put(name, items.getOrDefault(name, 0) + amount);
    }

    public boolean removeItem(String name, int amount) {
        if (items.containsKey(name) && items.get(name) >= amount) {
            int current = items.get(name);
            if (current == amount) {
                items.remove(name);
            } else {
                items.put(name, current - amount);
            }
            return true;
        }
        return false;
    }

    public TreeMap<String, Integer> getItems() {
        return items;
    }

    @Override
    public String toString() {
        if (items.isEmpty()) return "Inventory is empty.";
        StringBuilder sb = new StringBuilder("Inventory: ");
        items.forEach((name, qty) -> sb.append(name).append(": ").append(qty).append(", "));
        return sb.substring(0, sb.length() - 2);
    }
}
