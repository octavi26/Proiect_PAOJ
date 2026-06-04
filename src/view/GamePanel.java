package view;

import models.*;
import service.TerrariaService;
import repository.*;
import java.sql.SQLException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom JPanel that renders the game world and handles input.
 */
public class GamePanel extends JPanel {
    private WorldMap map;
    private final Player player;
    private final TerrariaService service;
    private final int TILE_SIZE = 32;
    private final int REACH_LIMIT = 5;
    private boolean isInventoryOpen = false;
    private int selectedItemIndex = 0;
    private int currentChunkId = 0;

    public GamePanel(WorldMap map, Player player, TerrariaService service) {
        this.map = map;
        this.player = player;
        this.service = service;

        setFocusable(true);
        requestFocusInWindow();
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_I) {
                    isInventoryOpen = !isInventoryOpen;
                } else if (e.getKeyCode() == KeyEvent.VK_R) {
                    regenerateWorld();
                } else if (!isInventoryOpen) {
                    handleMovement(e.getKeyCode());
                }
                repaint();
            }
        });

        addMouseWheelListener(e -> {
            List<String> items = new ArrayList<>(player.getInventory().getItems().keySet());
            if (!items.isEmpty()) {
                selectedItemIndex = (selectedItemIndex + e.getWheelRotation()) % items.size();
                if (selectedItemIndex < 0) selectedItemIndex += items.size();
            }
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isInventoryOpen) {
                    if (e.getX() > getWidth() - 200) {
                        service.craftTool("WoodenPickaxe", player);
                    }
                } else {
                    handleMouseClick(e);
                }
                repaint();
            }
        });

        // Game loop (Gravity and Repaint)
        Timer timer = new Timer(200, e -> {
            if (this.map != null) {
                service.applyGravity(player, this.map);
                // Move mobs randomly
                for (int i = 0; i < this.map.getEntities().size(); i++) {
                    Entity entity = this.map.getEntities().get(i);
                    if (entity instanceof HostileMob || entity instanceof PassiveMob) {
                        int dir = Math.random() > 0.5 ? 1 : -1;
                        service.moveEntity(entity, dir, 0, this.map);
                        service.applyGravity(entity, this.map);
                    }
                }
            }
            repaint();
        });
        timer.start();
    }

    private void regenerateWorld() {
        // 1. Wipe database
        try { DatabaseManager.getInstance().resetDatabase(); } catch (SQLException ignored) {}
        
        // 2. Reset player position
        service.resetPlayer(player, map.getForeground().getWidth(), map.getForeground().getHeight());

        // 3. Clear current map manually to be 100% safe
        this.map = service.loadChunk(currentChunkId, map.getForeground().getWidth(), map.getForeground().getHeight());
        
        // 4. Re-add player
        this.map.getEntities().add(player);
        System.out.println("Database reset. Current chunk regenerated. Player reset.");
    }

    private void handleMovement(int keyCode) {
        int width = this.map.getForeground().getWidth();
        
        if (keyCode == KeyEvent.VK_A) {
            if (player.getX() == 0) {
                changeChunk(currentChunkId - 1, width - 1);
            } else {
                service.moveEntity(player, -1, 0, this.map);
            }
        } else if (keyCode == KeyEvent.VK_D) {
            if (player.getX() == width - 1) {
                changeChunk(currentChunkId + 1, 0);
            } else {
                service.moveEntity(player, 1, 0, this.map);
            }
        } else if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_SPACE) {
            if (this.map.getForeground().getObject(player.getX(), player.getY() - 1) != null) {
                service.moveEntity(player, 0, 2, this.map);
            }
        }
    }

    private void changeChunk(int newChunkId, int newPlayerX) {
        // Save current chunk entities before leaving
        try {
            EntityRepository.getInstance().clearChunkEntities(currentChunkId);
            for (Entity e : this.map.getEntities()) {
                if (!(e instanceof Player)) {
                    EntityRepository.getInstance().create(e, currentChunkId);
                }
            }
            PlayerRepository.getInstance().create(player, currentChunkId);
        } catch (SQLException e) {
            System.err.println("Failed to save chunk state: " + e.getMessage());
        }

        this.currentChunkId = newChunkId;
        // Load new map from DB
        this.map = service.loadChunk(currentChunkId, this.map.getForeground().getWidth(), this.map.getForeground().getHeight());
        // Teleport player
        player.setX(newPlayerX);
        this.map.getEntities().add(player);
        System.out.println("Switched to Chunk " + currentChunkId);
    }

    private void handleMouseClick(MouseEvent e) {
        int worldX = e.getX() / TILE_SIZE;
        int worldY = this.map.getForeground().getHeight() - 1 - (e.getY() / TILE_SIZE);

        double dist = Math.sqrt(Math.pow(worldX - player.getX(), 2) + Math.pow(worldY - player.getY(), 2));
        if (dist > REACH_LIMIT) return;

        List<String> items = new ArrayList<>(player.getInventory().getItems().keySet());
        String heldItem = (items.isEmpty()) ? null : items.get(selectedItemIndex);

        if (SwingUtilities.isLeftMouseButton(e)) {
            Entity target = null;
            for (Entity entity : this.map.getEntities()) {
                if (entity.getX() == worldX && entity.getY() == worldY && entity != player) {
                    target = entity;
                    break;
                }
            }

            if (target != null) {
                service.attack(player, target);
                if (!target.isAlive()) this.map.getEntities().remove(target);
            } else {
                service.mineForegroundBlock(currentChunkId, worldX, worldY, heldItem, player, this.map);
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            if (heldItem != null && !heldItem.contains("Pickaxe")) {
                service.placeForegroundBlock(currentChunkId, worldX, worldY, heldItem, player, this.map);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (map == null) return;

        int height = map.getForeground().getHeight();
        int width = map.getForeground().getWidth();

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

        for (Entity e : map.getEntities()) {
            int drawY = (height - 1 - e.getY()) * TILE_SIZE;
            int drawX = e.getX() * TILE_SIZE;
            if (e instanceof Player) g.setColor(Color.BLUE);
            else if (e instanceof HostileMob) g.setColor(Color.RED);
            else if (e instanceof PassiveMob) g.setColor(Color.PINK);
            g.fillOval(drawX + 4, drawY + 4, TILE_SIZE - 8, TILE_SIZE - 8);
        }

        // HUD
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(10, 10, 250, 30);
        g.setColor(Color.WHITE);
        List<String> currentItems = new ArrayList<>(player.getInventory().getItems().keySet());
        String held = currentItems.isEmpty() ? "Empty" : currentItems.get(selectedItemIndex);
        g.drawString("Chunk: " + currentChunkId + " | Held: " + held, 20, 30);

        if (isInventoryOpen) {
            drawInventory(g, currentItems);
        }
    }

    private void drawInventory(Graphics g, List<String> currentItems) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("INVENTORY", 50, 50);
        int yPos = 80;
        for (int i = 0; i < currentItems.size(); i++) {
            String item = currentItems.get(i);
            int qty = player.getInventory().getItems().get(item);
            if (i == selectedItemIndex) g.setColor(Color.YELLOW);
            else g.setColor(Color.WHITE);
            g.drawString("- " + item + ": " + qty, 60, yPos);
            yPos += 25;
        }
        g.setColor(Color.WHITE);
        int craftingX = getWidth() - 250;
        g.drawString("CRAFTING", craftingX, 50);
        g.setColor(Color.YELLOW);
        g.drawRect(craftingX, 70, 200, 60);
        g.setColor(Color.WHITE);
        g.drawString("WoodenPickaxe", craftingX + 10, 95);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("(Cost: 3 Wood)", craftingX + 10, 115);
        g.setFont(new Font("Arial", Font.ITALIC, 12));
        g.drawString("Click to Craft", craftingX + 120, 125);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(map.getForeground().getWidth() * TILE_SIZE, 
                             map.getForeground().getHeight() * TILE_SIZE);
    }
}
