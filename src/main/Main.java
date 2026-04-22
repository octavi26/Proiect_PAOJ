package main;

import gui.GameWindow;
import models.Player;
import models.WorldMap;
import service.TerrariaService;

import javax.swing.*;

/**
 * Updated Main class to launch the GUI.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TerrariaService service = new TerrariaService();
            
            // Generate a slightly larger world for the GUI
            int width = 25;
            int height = 15;
            WorldMap map = service.generateWorld(width, height);
            
            // Initial Player setup
            Player player = new Player(100, 12, 12);
            map.getEntities().add(player);
            
            // Add some mobs
            service.spawnPassiveMob("Bunny", 5, 12, map);
            service.spawnPassiveMob("Bunny", 18, 12, map);

            // Launch GUI
            new GameWindow(map, service, player);
            
            System.out.println("=== GUI Launched (60 FPS) ===");
            System.out.println("Controls: Use LEFT and RIGHT arrows to move.");
        });
    }
}
