package main;

import models.Player;
import models.WorldMap;
import service.TerrariaService;
import view.GamePanel;

import javax.swing.*;

/**
 * Entry point for the GUI version of the game.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TerrariaService service = new TerrariaService();
            int width = 30;
            int height = 15;
            WorldMap map = service.generateWorld(width, height);
            
            Player player = new Player(1, width / 2, height - 2);
            map.getEntities().add(player);
            
            // Add some mobs for life
            service.spawnPassiveMob("Bunny", 5, height - 2, map);
            service.spawnPassiveMob("Bunny", 25, height - 2, map);

            JFrame frame = new JFrame("Terraria-Lite GUI");
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
