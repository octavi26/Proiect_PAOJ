package main;

import models.*;
import service.TerrariaService;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        TerrariaService service = new TerrariaService();
        System.out.println("=== TERRARIA-LITE: ADVANCED ENGINE DEMO ===");

        // 1. IMPROVED GENERATION (Trees, Grass, Caves)
        int w = 20, h = 10;
        WorldMap map = service.generateWorld(w, h);
        Player player = new Player(0, 5, 9); // Spawn high
        map.getEntities().add(player);
        
        System.out.println("> World generated with Trees (W, L), Grass (G), and Caves (empty spaces underground).");
        service.printRenderSector(0, 0, w, h, map);

        // 2. COLLISION & GRAVITY
        System.out.println("> Applying Physics (Gravity)...");
        while(service.applyGravity(player, map));
        
        System.out.println("> Attempting to move RIGHT into a Tree (Collision check)...");
        service.moveEntity(player, 1, 0, map); // Should fail if tree is there
        service.printRenderSector(0, 0, w, h, map);

        // 3. TOOL UTILITY & MINING
        System.out.println("> Mining Dirt to craft a Pickaxe...");
        service.mineForegroundBlock(player.getX(), player.getY() - 1, player, map);
        service.mineForegroundBlock(player.getX() + 1, player.getY() - 1, player, map);
        service.mineForegroundBlock(player.getX() - 1, player.getY() - 1, player, map);
        
        System.out.println("> Attempting to mine Stone WITHOUT a tool...");
        service.mineForegroundBlock(player.getX(), 2, player, map); 

        System.out.println("> Crafting DirtPickaxe...");
        service.craftTool("DirtPickaxe", player);

        System.out.println("> Attempting to mine Stone WITH DirtPickaxe...");
        service.mineForegroundBlock(player.getX(), 2, player, map);
        service.printRenderSector(0, 0, w, h, map);

        // 4. SURVIVAL & COMBAT
        HostileMob zombie = new HostileMob(666, "Zombie", player.getX() + 2, player.getY());
        map.getEntities().add(zombie);
        System.out.println("> A Zombie (Z) has appeared!");
        service.printRenderSector(0, 0, w, h, map);

        System.out.println("> Combat: Zombie attacks Player, then Player retaliates.");
        service.attack(zombie, player);
        service.attack(player, zombie);
        service.attack(player, zombie);
        service.attack(player, zombie); // Zombie should "die" (health <= 0)

        // Remove dead entities
        map.getEntities().removeIf(e -> !e.isAlive());
        System.out.println("> Zombie defeated and removed.");
        service.printRenderSector(0, 0, w, h, map);

        System.out.println("=== DEMO COMPLETE ===");
        System.out.println("Final Stats: HP=" + player.getHealth() + " | " + player.getInventory().toString());
    }
}
