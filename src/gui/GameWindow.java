package gui;

import models.Player;
import models.WorldMap;
import service.TerrariaService;

import javax.swing.*;

/**
 * Main window for the GUI.
 */
public class GameWindow extends JFrame {
    public GameWindow(WorldMap worldMap, TerrariaService service, Player player) {
        this.setTitle("Terraria-Lite GUI Implementation");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        GamePanel panel = new GamePanel(worldMap, service, player);
        this.add(panel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
