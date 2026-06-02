package view;

import models.*;
import service.TerrariaService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Custom JPanel that renders the game world and handles input.
 */
public class GamePanel extends JPanel {
    private final WorldMap map;
    private final Player player;
    private final TerrariaService service;
    private final int TILE_SIZE = 32;
    private boolean isInventoryOpen = false;

    public GamePanel(WorldMap map, Player player, TerrariaService service) {
        this.map = map;
        this.player = player;
        this.service = service;

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    isInventoryOpen = !isInventoryOpen;
                } else if (!isInventoryOpen) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_A -> service.moveEntity(player, -1, 0, map);
                        case KeyEvent.VK_D -> service.moveEntity(player, 1, 0, map);
                        case KeyEvent.VK_W, KeyEvent.VK_SPACE -> {
                            if (map.getForeground().getObject(player.getX(), player.getY() - 1) != null) {
                                service.moveEntity(player, 0, 2, map);
                            }
                        }
                    }
                }
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isInventoryOpen) {
                    // Handle Crafting Click (Simplified: click on the right side to craft)
                    if (e.getX() > getWidth() - 200) {
                        service.craftTool("DirtPickaxe", player);
                    }
                } else {
                    int worldX = e.getX() / TILE_SIZE;
                    int worldY = map.getForeground().getHeight() - 1 - (e.getY() / TILE_SIZE);

                    Entity target = null;
                    for (Entity entity : map.getEntities()) {
                        if (entity.getX() == worldX && entity.getY() == worldY && entity != player) {
                            target = entity;
                            break;
                        }
                    }

                    if (target != null) {
                        service.attack(player, target);
                        if (!target.isAlive()) {
                            map.getEntities().remove(target);
                        }
                    } else {
                        service.mineForegroundBlock(worldX, worldY, player, map);
                    }
                }
                repaint();
            }
        });

        // Game loop (Gravity and Repaint)
        Timer timer = new Timer(200, e -> {
            service.applyGravity(player, map);
            // Move mobs randomly
            for (Entity entity : map.getEntities()) {
                if (entity instanceof HostileMob || entity instanceof PassiveMob) {
                    int dir = Math.random() > 0.5 ? 1 : -1;
                    service.moveEntity(entity, dir, 0, map);
                    service.applyGravity(entity, map);
                }
            }
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int height = map.getForeground().getHeight();
        int width = map.getForeground().getWidth();

        // Draw Background and Foreground
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int drawY = (height - 1 - y) * TILE_SIZE;
                int drawX = x * TILE_SIZE;

                GameObject bg = map.getBackground().getObject(x, y);
                GameObject fg = map.getForeground().getObject(x, y);

                if (bg != null) {
                    g.setColor(new Color(100, 70, 40)); 
                    g.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                }

                if (fg != null) {
                    switch (fg.getName()) {
                        case "Grass" -> g.setColor(Color.GREEN);
                        case "Dirt" -> g.setColor(new Color(139, 69, 19));
                        case "Stone" -> g.setColor(Color.GRAY);
                        case "Wood" -> g.setColor(new Color(101, 67, 33));
                        case "Leaves" -> g.setColor(new Color(34, 139, 34));
                        default -> g.setColor(Color.BLACK);
                    }
                    g.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Draw Entities
        for (Entity e : map.getEntities()) {
            int drawY = (height - 1 - e.getY()) * TILE_SIZE;
            int drawX = e.getX() * TILE_SIZE;

            if (e instanceof Player) g.setColor(Color.BLUE);
            else if (e instanceof HostileMob) g.setColor(Color.RED);
            else if (e instanceof PassiveMob) g.setColor(Color.PINK);
            else if (e instanceof DroppedItem) g.setColor(Color.YELLOW);

            g.fillOval(drawX + 4, drawY + 4, TILE_SIZE - 8, TILE_SIZE - 8);
        }

        // Draw Inventory and Crafting Overlay
        if (isInventoryOpen) {
            // Semi-transparent background
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            
            // 1. Draw Inventory List
            g.drawString("INVENTORY", 50, 50);
            int yPos = 80;
            String invText = player.getInventory().toString(); // e.g., "Inventory: Dirt: 5, Stone: 2"
            if (invText.startsWith("Inventory: ")) {
                String[] items = invText.substring(11).split(", ");
                for (String item : items) {
                    g.drawString("- " + item, 60, yPos);
                    yPos += 25;
                }
            } else {
                g.drawString(invText, 60, yPos);
            }

            // 2. Draw Crafting Recipes
            int craftingX = getWidth() - 250;
            g.drawString("CRAFTING", craftingX, 50);
            g.setColor(Color.YELLOW);
            g.drawRect(craftingX, 70, 200, 60);
            g.setColor(Color.WHITE);
            g.drawString("DirtPickaxe", craftingX + 10, 95);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("(Cost: 3 Dirt)", craftingX + 10, 115);
            g.setFont(new Font("Arial", Font.ITALIC, 12));
            g.drawString("Click to Craft", craftingX + 120, 125);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(map.getForeground().getWidth() * TILE_SIZE, 
                             map.getForeground().getHeight() * TILE_SIZE);
    }
}
