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

        // 2. Setup Player & Entities
        Player player = new Player(100, 2, 7); // Spawn high
        map.getEntities().add(player);
        service.spawnPassiveMob("Bunny", 12, 5, map);
        
        System.out.println("Initial State:");
        service.printRenderSector(0, 0, width, height, map);

        // 3. Movement and Gravity
        System.out.println("Applying gravity...");
        service.applyGravity(player, map); // falls to 6
        System.out.println("Moving player right...");
        service.moveEntity(player, 2, 0, map); // (4, 6)
        service.printRenderSector(0, 0, width, height, map);

        // 4. Mining for Resources
        System.out.println("Mining resources...");
        // Mine the Dirt block player is standing on (y=5 is the top Dirt layer)
        service.mineForegroundBlock(4, 5, player, map);
        service.mineForegroundBlock(4, 4, player, map);
        service.mineForegroundBlock(4, 3, player, map); // This is Stone (y < 4)
        
        service.getSortedInventory(player);

        // 5. Crafting (Using gathered resources)
        System.out.println("Crafting attempt...");
        // Recipe: 3 Dirt for a tool
        service.craftTool("DirtPickaxe", player);
        service.getSortedInventory(player);

        // 6. World Manipulation
        System.out.println("Building and Dropping...");
        // Place a background wall where we mined
        service.placeBackgroundBlock(4, 5, "DirtWall", player, map);
        // Drop an item
        service.dropItem("Stone", 1, player, map);
        
        service.printRenderSector(0, 0, width, height, map);

        // 7. Interaction
        System.out.println("Picking up the dropped item...");
        List<Entity> entities = map.getEntities();
        for (int i = entities.size() - 1; i >= 0; i--) {
            if (entities.get(i) instanceof DroppedItem) {
                service.pickupItem(player, (DroppedItem) entities.get(i), map);
                break;
            }
        }

        System.out.println("Final Inventory:");
        service.getSortedInventory(player);
        System.out.println("=== DEMO COMPLETE ===");
    }
}
