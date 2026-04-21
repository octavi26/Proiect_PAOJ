package main;

import models.*;
import service.TerrariaService;

/**
 * Main class to demonstrate the functionality of the Terraria-Lite Backend Engine.
 */
public class Main {
    public static void main(String[] args) {
        TerrariaService service = new TerrariaService();
        
        System.out.println("--- Stage I: Terraria-Lite Backend Engine ---");

        // 1. Generate World
        int width = 20;
        int height = 10;
        WorldMap map = service.generateWorld(width, height);
        System.out.println("World generated: " + width + "x" + height);

        // 2. Initialize Player
        Player player = new Player(0, 5, 8); // Spawned in the air
        map.getEntities().add(player);
        System.out.println("Player spawned at (5, 8)");

        // Initial render
        service.printRenderSector(0, 0, width, height, map);

        // 3. Apply Gravity
        System.out.println("Applying gravity...");
        service.applyGravity(player, map);
        System.out.println("Player position: (" + player.getX() + ", " + player.getY() + ")");
        
        // Move player down to ground level (ground starts at y < 7.5, so y=7 is dirt)
        player.setY(8);
        service.applyGravity(player, map); // should go to 7
        service.printRenderSector(0, 0, width, height, map);

        // 4. Mining
        System.out.println("\n--- Player Actions ---");
        // Mine the block below the player (y=6 is dirt)
        service.mineForegroundBlock(5, 6, player, map);
        service.mineForegroundBlock(5, 5, player, map);
        
        // 5. Sorted Inventory
        service.getSortedInventory(player);

        // 6. Placement
        service.placeBackgroundBlock(5, 6, "DirtWall", player, map);
        service.placeForegroundBlock(6, 7, "Dirt", player, map);

        // 7. Crafting
        System.out.println("\n--- Crafting ---");
        service.craftTool("DirtPickaxe", player); // Requires 3 Dirt, we only have 2
        service.mineForegroundBlock(6, 6, player, map); // Get one more Dirt
        service.craftTool("DirtPickaxe", player); // Now it should work
        
        service.getSortedInventory(player);

        // 8. Entities (Mobs and Drops)
        System.out.println("\n--- Mobs and Drops ---");
        service.spawnPassiveMob("Bunny", 10, 7, map);
        service.dropItem("Stone", 5, player, map); // We don't have stone yet
        
        // Let's mine some stone (y < 5)
        service.mineForegroundBlock(10, 4, player, map);
        service.mineForegroundBlock(11, 4, player, map);
        service.mineForegroundBlock(12, 4, player, map);
        service.mineForegroundBlock(13, 4, player, map);
        service.mineForegroundBlock(14, 4, player, map);
        
        service.dropItem("Stone", 3, player, map);
        
        // Render again
        service.printRenderSector(0, 0, width, height, map);

        // 9. Pickup
        if (!map.getEntities().isEmpty()) {
            Entity lastEntity = map.getEntities().get(map.getEntities().size() - 1);
            if (lastEntity instanceof DroppedItem) {
                service.pickupItem(player, (DroppedItem) lastEntity, map);
            }
        }

        service.getSortedInventory(player);
        
        System.out.println("\n--- Stage I Demo Completed ---");
    }
}
