package service;

import models.*;
import java.util.List;

/**
 * Service class handling the business logic of the game.
 */
public class TerrariaService {

    private int nextObjectId = 1;
    private int nextEntityId = 1;

    /**
     * Initializes the world map with layers, grass, and occasional caves.
     */
    public WorldMap generateWorld(int width, int height) {
        WorldMap map = new WorldMap(width, height);
        WorldLayer fg = map.getForeground();
        WorldLayer bg = map.getBackground();

        for (int x = 0; x < width; x++) {
            int surfaceY = height * 3 / 4;
            for (int y = 0; y < height; y++) {
                // Background walls everywhere underground
                if (y < surfaceY) bg.setObject(x, y, new Wall(nextObjectId++, "Dirt"));

                // Foreground logic
                if (y == surfaceY) {
                    fg.setObject(x, y, new Block(nextObjectId++, "Grass"));
                } else if (y < surfaceY) {
                    // Simple Cave Generation (random pockets)
                    if (Math.random() > 0.15) { 
                        String type = (y < height / 3) ? "Stone" : "Dirt";
                        fg.setObject(x, y, new Block(nextObjectId++, type));
                    }
                }
            }
            // Occasional Trees
            if (x > 0 && x < width - 1 && Math.random() > 0.8) {
                fg.setObject(x, surfaceY + 1, new Block(nextObjectId++, "Wood"));
                fg.setObject(x, surfaceY + 2, new Block(nextObjectId++, "Wood"));
                fg.setObject(x, surfaceY + 3, new Block(nextObjectId++, "Leaves"));
            }
        }
        return map;
    }

    /**
     * Moves a movable object with COLLISION DETECTION.
     */
    public void moveEntity(Movable movable, int deltaX, int deltaY, WorldMap map) {
        int newX = movable.getX() + deltaX;
        int newY = movable.getY() + deltaY;

        // Check Bounds
        if (newX >= 0 && newX < map.getForeground().getWidth() &&
            newY >= 0 && newY < map.getForeground().getHeight()) {
            
            // Check Collision (is the foreground empty?)
            if (map.getForeground().getObject(newX, newY) == null) {
                movable.setX(newX);
                movable.setY(newY);
            }
        }
    }

    /**
     * Applies gravity to a movable object if there is no solid block below.
     */
    public boolean applyGravity(Movable movable, WorldMap map) {
        int x = movable.getX();
        int y = movable.getY();

        if (y > 0 && map.getForeground().getObject(x, y - 1) == null) {
            movable.setY(y - 1);
            return true;
        }

        return false;
    }

    /**
     * Attacks a target entity.
     */
    public void attack(Entity attacker, Entity target) {
        if (target.isAlive()) {
            target.setHealth(target.getHealth() - 2);
            System.out.println("Entity " + attacker.getId() + " attacked " + target.getId() + ". Target HP: " + target.getHealth());
        }
    }

    /**
     * Mines a block - now requires a tool for hard blocks.
     */
    public void mineForegroundBlock(int x, int y, Player player, WorldMap map) {
        GameObject obj = map.getForeground().getObject(x, y);
        if (obj instanceof Block) {
            boolean canMine = true;
            if (obj.getName().equals("Stone")) {
                // Simplified tool check: does inventory have anything with "Pickaxe" in name?
                canMine = player.getInventory().getItems().keySet().stream().anyMatch(k -> k.contains("Pickaxe"));
            }

            if (canMine) {
                player.getInventory().addItem(obj.getName(), 1);
                map.getForeground().setObject(x, y, null);
                System.out.println("Mined: " + obj.getName());
            } else {
                System.out.println("You need a Pickaxe to mine Stone!");
            }
        }
    }

    /**
     * Places a block from the inventory into the foreground.
     */
    public void placeForegroundBlock(int x, int y, String blockName, Player player, WorldMap map) {
        if (map.getForeground().getObject(x, y) == null) {
            if (player.getInventory().removeItem(blockName, 1)) {
                map.getForeground().setObject(x, y, new Block(nextObjectId++, blockName));
                System.out.println("Placed foreground: " + blockName);
            } else {
                System.out.println("Not enough " + blockName + " in inventory.");
            }
        }
    }

    /**
     * Mines a background block if the foreground is empty.
     */
    public void mineBackgroundBlock(int x, int y, Player player, WorldMap map) {
        if (map.getForeground().getObject(x, y) == null) {
            GameObject obj = map.getBackground().getObject(x, y);
            if (obj instanceof Wall) {
                player.getInventory().addItem(obj.getName(), 1);
                map.getBackground().setObject(x, y, null);
                System.out.println("Mined background: " + obj.getName());
            }
        }
    }

    /**
     * Places a wall from the inventory into the background.
     */
    public void placeBackgroundBlock(int x, int y, String wallName, Player player, WorldMap map) {
        if (map.getBackground().getObject(x, y) == null) {
            if (player.getInventory().removeItem(wallName, 1)) {
                map.getBackground().setObject(x, y, new Wall(nextObjectId++, wallName));
                System.out.println("Placed background: " + wallName);
            }
        }
    }

    /**
     * Drops an item from the player's inventory into the world.
     */
    public void dropItem(String itemName, int amount, Player player, WorldMap map) {
        if (player.getInventory().removeItem(itemName, amount)) {
            DroppedItem item = new DroppedItem(nextEntityId++, itemName, amount, player.getX(), player.getY());
            map.getEntities().add(item);
            System.out.println("Dropped " + amount + " " + itemName);
        }
    }

    /**
     * Picks up a dropped item from the world.
     */
    public void pickupItem(Player player, DroppedItem item, WorldMap map) {
//        if (item.getX() == player.getX() && item.getY() == player.getY()) {
            player.getInventory().addItem(item.getItemName(), item.getAmount());
            map.getEntities().remove(item);
            System.out.println("Picked up " + item.getAmount() + " " + item.getItemName());
//        }
    }

    /**
     * Spawns a passive mob at specific coordinates.
     */
    public void spawnPassiveMob(String mobName, int x, int y, WorldMap map) {
        PassiveMob mob = new PassiveMob(nextEntityId++, mobName, x, y);
        map.getEntities().add(mob);
        System.out.println("Spawned " + mobName + " at (" + x + "," + y + ")");
    }

    /**
     * Crafts a tool by consuming specific resources (e.g., 3 Dirt for a DirtPickaxe).
     */
    public void craftTool(String toolName, Player player) {
        if (player.getInventory().removeItem("Dirt", 3)) {
            player.getInventory().addItem(toolName, 1);
            System.out.println("Crafted " + toolName);
        } else {
            System.out.println("Need 3 Dirt to craft a tool.");
        }
    }

    /**
     * Prints the player's sorted inventory.
     */
    public void getSortedInventory(Player player) {
        System.out.println(player.getInventory().toString());
    }

    /**
     * Renders a portion of the map in ASCII format.
     * Legend: P=Player, M=Mob, I=Item, D=Dirt, S=Stone (Caps=Block, small=Wall)
     */
    public void printRenderSector(int startX, int startY, int width, int height, WorldMap map) {
        System.out.println("--- Render Sector (" + startX + "," + startY + ") ---");
        for (int y = startY + height - 1; y >= startY; y--) {
            for (int x = startX; x < startX + width; x++) {
                char symbol = ' '; // Air

                // Check for entities - Entities take priority
                boolean entityFound = false;
                for (Entity e : map.getEntities()) {
                    if (e.getX() == x && e.getY() == y) {
                        if (e instanceof Player) symbol = 'P';
                        else if (e instanceof HostileMob) symbol = 'Z';
                        else if (e instanceof PassiveMob) symbol = 'M';
                        else if (e instanceof DroppedItem) symbol = 'I';
                        entityFound = true;
                        break;
                    }
                }

                if (!entityFound) {
                    GameObject fg = map.getForeground().getObject(x, y);
                    GameObject bg = map.getBackground().getObject(x, y);

                    if (fg != null) {
                        if (fg.getName().equals("Dirt")) symbol = 'D';
                        else if (fg.getName().equals("Stone")) symbol = 'S';
                        else if (fg.getName().equals("Grass")) symbol = 'G';
                        else if (fg.getName().equals("Wood")) symbol = 'W';
                        else if (fg.getName().equals("Leaves")) symbol = 'L';
                        else symbol = '#';
                    } else if (bg != null) {
                        if (bg.getName().equals("Dirt")) symbol = 'd';
                        else if (bg.getName().equals("Stone")) symbol = 's';
                        else symbol = '.';
                    }
                }
                System.out.print(symbol + " ");
            }
            System.out.println();
        }
        System.out.println("-----------------------------------");
    }
}
