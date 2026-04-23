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
     * Initializes the world map with basic blocks and walls.
     */
    public WorldMap generateWorld(int width, int height) {
        WorldMap map = new WorldMap(width, height);
        WorldLayer fg = map.getForeground();
        WorldLayer bg = map.getBackground();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (y < height / 2) {
                    fg.setObject(x, y, new Block(nextObjectId++, "Stone"));
                    bg.setObject(x, y, new Wall(nextObjectId++, "Stone"));
                } else if (y < height * 3 / 4) {
                    fg.setObject(x, y, new Block(nextObjectId++, "Dirt"));
                    bg.setObject(x, y, new Wall(nextObjectId++, "Dirt"));
                }
            }
        }
        return map;
    }

    /**
     * Moves an entity by deltaX and deltaY if within bounds.
     */
    public void moveEntity(Entity entity, int deltaX, int deltaY, WorldMap map) {
        int newX = entity.getX() + deltaX;
        int newY = entity.getY() + deltaY;

        if (newX >= 0 && newX < map.getForeground().getWidth() &&
            newY >= 0 && newY < map.getForeground().getHeight()) {
            entity.setX(newX);
            entity.setY(newY);
        }
    }

    /**
     * Applies gravity to an entity if there is no solid block below.
     */
    public boolean applyGravity(Entity entity, WorldMap map) {
        int x = entity.getX();
        int y = entity.getY();

        if (y > 0 && map.getForeground().getObject(x, y - 1) == null) {
            entity.setY(y - 1);
            return true;
        }

        return false;
    }

    /**
     * Mines a foreground block and adds it to the player's inventory.
     */
    public void mineForegroundBlock(int x, int y, Player player, WorldMap map) {
        GameObject obj = map.getForeground().getObject(x, y);
        if (obj instanceof Block) {
            player.getInventory().addItem(obj.getName(), 1);
            map.getForeground().setObject(x, y, null);
            System.out.println("Mined foreground: " + obj.getName());
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
