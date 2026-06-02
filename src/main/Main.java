package main;

import models.Player;
import models.WorldMap;
import service.TerrariaService;
import view.GamePanel;

import javax.swing.*;

/**
 * Main entry point for the Terraria-Lite game (Stage II version).
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Requirement: Stage II - Singleton usage
            TerrariaService service = TerrariaService.getInstance();
            
            int width = 30;
            int height = 15;
            
            // Load initial chunk from DB
            WorldMap map = service.loadChunk(0, width, height);
            
            Player player = new Player(1, width / 2, height - 2);
            map.getEntities().add(player);
            
            // Add initial mobs
            service.spawnPassiveMob("Bunny", 5, height - 2, map);
            service.spawnPassiveMob("Bunny", 25, height - 2, map);

            JFrame frame = new JFrame("Terraria-Lite Stage II (JDBC + Audit)");
            GamePanel panel = new GamePanel(map, player, service);
            
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}
