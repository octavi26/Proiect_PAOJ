package gui;

import models.*;
import service.TerrariaService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * JPanel that handles rendering and the 60 FPS game loop.
 */
public class GamePanel extends JPanel implements ActionListener {
    private final int TILE_SIZE = 40;
    private final WorldMap worldMap;
    private final TerrariaService service;
    private final Player player;
    private final Timer timer;

    public GamePanel(WorldMap worldMap, TerrariaService service, Player player) {
        this.worldMap = worldMap;
        this.service = service;
        this.player = player;

        // Set up 60 FPS Timer (1000ms / 60 approx 16ms)
        this.timer = new Timer(16, this);
        this.timer.start();

        // Configure Panel
        this.setPreferredSize(new Dimension(
                worldMap.getForeground().getWidth() * TILE_SIZE,
                worldMap.getForeground().getHeight() * TILE_SIZE
        ));
        this.setBackground(new Color(135, 206, 235)); // Sky Blue

        setupControls();
    }

    private void setupControls() {
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");

        actionMap.put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                service.moveEntity(player, -1, 0, worldMap);
            }
        });

        actionMap.put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                service.moveEntity(player, 1, 0, worldMap);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int worldHeight = worldMap.getForeground().getHeight();

        // Render Background (Walls)
        for (int y = 0; y < worldHeight; y++) {
            for (int x = 0; x < worldMap.getBackground().getWidth(); x++) {
                GameObject bg = worldMap.getBackground().getObject(x, y);
                if (bg != null) {
                    g.setColor(bg.getName().contains("Stone") ? new Color(100, 100, 100) : new Color(101, 67, 33));
                    // Invert Y for Swing
                    g.fillRect(x * TILE_SIZE, (worldHeight - 1 - y) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Render Foreground (Blocks)
        for (int y = 0; y < worldHeight; y++) {
            for (int x = 0; x < worldMap.getForeground().getWidth(); x++) {
                GameObject fg = worldMap.getForeground().getObject(x, y);
                if (fg != null) {
                    g.setColor(fg.getName().contains("Stone") ? Color.GRAY : new Color(139, 69, 19));
                    g.fillRect(x * TILE_SIZE, (worldHeight - 1 - y) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(x * TILE_SIZE, (worldHeight - 1 - y) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Render Entities
        for (Entity entity : worldMap.getEntities()) {
            if (entity instanceof Player) {
                g.setColor(Color.BLUE);
            } else if (entity instanceof PassiveMob) {
                g.setColor(Color.GREEN);
            } else if (entity instanceof DroppedItem) {
                g.setColor(Color.YELLOW);
            }
            g.fillOval(entity.getX() * TILE_SIZE + 5, (worldHeight - 1 - entity.getY()) * TILE_SIZE + 5, TILE_SIZE - 10, TILE_SIZE - 10);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Game Loop Update (60 times per second)
        service.applyGravity(player, worldMap);
        repaint();
    }
}
