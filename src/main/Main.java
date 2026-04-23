package main;

import models.*;
import service.TerrariaService;
import java.util.List;

/**
 * Concise and Logical Demo for Stage I.
 */
public class Main {
    public static void main(String[] args) {
        TerrariaService service = new TerrariaService();
        
        System.out.println("=== TERRARIA-LITE ENGINE: STAGE I DEMO ===");

        // 1. World Creation
        int width = 15;
        int height = 8;
        WorldMap map = service.generateWorld(width, height);
        System.out.println("World generated: " + width + "x" + height);

        // 2. Setup Player and Entities
        Player player = new Player(100, 2, 7);
        map.getEntities().add(player);
        service.spawnPassiveMob("Bunny", 12, 7, map);
        
        System.out.println("Initial State:");
        service.printRenderSector(0, 0, width, height, map);

        // 3. Movement and Gravity
        System.out.println("Applying gravity...");
        for (Entity e : map.getEntities())
            while(service.applyGravity(e, map));
        System.out.println("Moving player right...");
        service.moveEntity(player, 2, 0, map);
        service.printRenderSector(0, 0, width, height, map);

        // 4. Mining for Resources
        System.out.println("Mining resources...");
        int playerY = player.getY();
        int playerX = player.getX();
        service.mineForegroundBlock(playerX, playerY - 1, player, map);
        service.mineForegroundBlock(playerX + 1, playerY - 1, player, map);
        service.mineForegroundBlock(playerX, playerY - 2, player, map);
        service.mineForegroundBlock(playerX, playerY - 3, player, map);
        service.getSortedInventory(player);
        service.printRenderSector(0, 0, width, height, map);

        // 5. Crafting
        System.out.println("Crafting attempt...");
        service.craftTool("DirtPickaxe", player);
        service.getSortedInventory(player);

        // 6. World Manipulation
        System.out.println("Building and Dropping...");
        service.placeBackgroundBlock(playerX, playerY + 1, "Stone", player, map);
        service.dropItem("DirtPickaxe", 1, player, map);
        service.moveEntity(player, -1, 0, map);
        service.printRenderSector(0, 0, width, height, map);

        // 7. Interaction
        System.out.println("Picking up the dropped item...");
        List<Entity> entities = map.getEntities();
        for (Entity e : entities) {
            if (e instanceof DroppedItem) {
                service.pickupItem(player, (DroppedItem) e, map);
                break;
            }
        }

        System.out.println("Final Inventory:");
        service.getSortedInventory(player);
        System.out.println("Final Renbder:");
        service.printRenderSector(0, 0, width, height, map);
        System.out.println("=== DEMO COMPLETE ===");
    }
}
