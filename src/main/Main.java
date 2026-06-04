package main;

import models.Player;
import models.WorldMap;
import service.TerrariaService;
import view.GamePanel;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TerrariaService service = TerrariaService.getInstance();
            
            int width = 30;
            int height = 15;
            
            // Load initial chunk from DB
            WorldMap map = service.loadChunk(0, width, height);
            
            // Try to load player from DB, otherwise create new
            Player player = null;
            try {
                player = repository.PlayerRepository.getInstance().loadPlayer(1);
            } catch (Exception ignored) {System.out.println("No player in DB");}

            if (player == null) {
                player = new Player(1, width / 2, height / 2 + 2);
                try {
                    repository.PlayerRepository.getInstance().create(player, 0);
                } catch (Exception ignored) {System.out.println("Player can not be created");}
            }
            
            map.getEntities().add(player);


            JFrame frame = new JFrame("Terraria-Lite");
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
