package service;

import models.*;
import repository.*;
import java.sql.SQLException;
import java.util.List;

public class TerrariaService {
    private static TerrariaService instance;
    private int nextObjectId = 1;
    private int nextEntityId = 1;
    private final AuditService audit = AuditService.getInstance();

    private TerrariaService() {}

    public static TerrariaService getInstance() {
        if (instance == null) {
            instance = new TerrariaService();
        }
        return instance;
    }

    /**
     * Loads a chunk from the database or generates it if it doesn't exist.
     */
    public WorldMap regenerateWorldState(int chunkId, int width, int height) {
        audit.logAction("REGENERATE_WORLD");
        try {
            DatabaseManager.getInstance().resetDatabase();
            return loadChunk(chunkId, width, height);
        } catch (SQLException e) {
            System.err.println("Regeneration failed: " + e.getMessage());
            return generateWorld(width, height);
        }
    }

    public WorldMap loadChunk(int chunkId, int width, int height) {
        audit.logAction("LOAD_CHUNK_" + chunkId);
        WorldMap map = new WorldMap(width, height);

        try {
            if (!BlockRepository.getInstance().hasBlocks(chunkId)) {
                // If no blocks, assume chunk is new and generate it
                return generateAndSaveWorld(chunkId, width, height);
            }

            // Load blocks/walls from DB
            BlockRepository.getInstance().loadBlocksIntoLayer(chunkId, map.getForeground());
            WallRepository.getInstance().loadWallsIntoLayer(chunkId, map.getBackground());

            // Load entities
            List<Entity> entities = EntityRepository.getInstance().readAll(chunkId);
            map.getEntities().addAll(entities);

            return map; 
        } catch (SQLException e) {
            System.err.println("Failed to load chunk from DB: " + e.getMessage());
            return generateWorld(width, height);
        }
    }

    private WorldMap generateAndSaveWorld(int chunkId, int width, int height) throws SQLException {
        WorldMap map = generateWorld(width, height);
        // Save blocks to DB
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                GameObject fg = map.getForeground().getObject(x, y);
                if (fg != null) BlockRepository.getInstance().saveBlock(chunkId, x, y, fg.getName());
                
                GameObject bg = map.getBackground().getObject(x, y);
                if (bg != null) WallRepository.getInstance().saveWall(chunkId, x, y, bg.getName());
            }
        }
        
        // Spawn and save initial mobs for this chunk
        spawnPassiveMob("Bunny", 5, height / 2 + 2, map);
        spawnHostileMob("Zombie", 25, height / 2 + 2, map);
        
        for (Entity e : map.getEntities()) {
            if (!(e instanceof Player)) {
                EntityRepository.getInstance().create(e, chunkId);
            }
        }
        
        return map;
    }

    public WorldMap generateWorld(int width, int height) {
        WorldMap map = new WorldMap(width, height);
        WorldLayer fg = map.getForeground();
        WorldLayer bg = map.getBackground();

        for (int x = 0; x < width; x++) {
            int surfaceY = height / 2; // Adjusted surface level
            for (int y = 0; y < height; y++) {
                if (y < surfaceY) bg.setObject(x, y, new Wall(nextObjectId++, "Dirt"));
                if (y == surfaceY) {
                    fg.setObject(x, y, new Block(nextObjectId++, "Grass"));
                } else if (y < surfaceY) {
                    if (Math.random() > 0.15) { 
                        String type = (y < height / 4) ? "Stone" : "Dirt";
                        fg.setObject(x, y, new Block(nextObjectId++, type));
                    }
                }
            }
            if (x > 0 && x < width - 1 && Math.random() > 0.8) {
                fg.setObject(x, surfaceY + 1, new Block(nextObjectId++, "Wood"));
                fg.setObject(x, surfaceY + 2, new Block(nextObjectId++, "Wood"));
                fg.setObject(x, surfaceY + 3, new Block(nextObjectId++, "Leaves"));
            }
        }
        return map;
    }

    public void moveEntity(Movable movable, int deltaX, int deltaY, WorldMap map) {
        int newX = movable.getX() + deltaX;
        int newY = movable.getY() + deltaY;

        if (newX >= 0 && newX < map.getForeground().getWidth() &&
            newY >= 0 && newY < map.getForeground().getHeight()) {
            if (map.getForeground().getObject(newX, newY) == null) {
                movable.setX(newX);
                movable.setY(newY);
            }
        }
        
        // Hostile mob damage logic
        if (movable instanceof HostileMob && map.getEntities().stream().anyMatch(e -> e instanceof Player && e.getX() == newX && e.getY() == newY)) {
            for (Entity e : map.getEntities()) {
                if (e instanceof Player && e.getX() == newX && e.getY() == newY) {
                    e.setHealth(e.getHealth() - 1);
                    System.out.println("Player damaged by hostile mob!");
                }
            }
        }
    }

    public boolean applyGravity(Movable movable, WorldMap map) {
        int x = movable.getX();
        int y = movable.getY();
        if (y > 0 && map.getForeground().getObject(x, y - 1) == null) {
            movable.setY(y - 1);
            return true;
        }
        return false;
    }

    public void attack(Entity attacker, Entity target) {
        audit.logAction("ATTACK_ENTITY_" + target.getId());
        if (target.isAlive()) {
            target.setHealth(target.getHealth() - 2);
            if (!target.isAlive()) {
                System.out.println("Entity " + target.getId() + " died.");
            }
        }
    }

    public void mineForegroundBlock(int chunkId, int x, int y, String heldItem, Player player, WorldMap map) {
        GameObject obj = map.getForeground().getObject(x, y);
        if (obj instanceof Block) {
            boolean canMine = !obj.getName().equals("Stone") || (heldItem != null && heldItem.contains("Pickaxe"));
            if (canMine) {
                audit.logAction("MINE_BLOCK_" + obj.getName());
                player.getInventory().addItem(obj.getName(), 1);
                map.getForeground().setObject(x, y, null);
                // Update DB with correct chunkId
                try { BlockRepository.getInstance().removeBlock(chunkId, x, y); } catch (SQLException ignored) {}
            }
        }
    }

    public void placeForegroundBlock(int chunkId, int x, int y, String blockName, Player player, WorldMap map) {
        if (map.getForeground().getObject(x, y) == null && player.getInventory().removeItem(blockName, 1)) {
            audit.logAction("PLACE_BLOCK_" + blockName);
            map.getForeground().setObject(x, y, new Block(nextObjectId++, blockName));
            // Update DB with correct chunkId
            try { BlockRepository.getInstance().saveBlock(chunkId, x, y, blockName); } catch (SQLException ignored) {}
        }
    }

    public void craftTool(String toolName, Player player) {
        if (player.getInventory().removeItem("Wood", 3)) {
            audit.logAction("CRAFT_TOOL_" + toolName);
            Tool newTool = new Tool(nextObjectId++, toolName, 100, 10);
            player.getInventory().addItem(newTool.getName(), 1);
            System.out.println("Crafted: " + newTool);
        }
    }

    public void spawnPassiveMob(String mobName, int x, int y, WorldMap map) {
        PassiveMob mob = new PassiveMob(nextEntityId++, mobName, x, y);
        map.getEntities().add(mob);
    }

    public void spawnHostileMob(String type, int x, int y, WorldMap map) {
        HostileMob mob = new HostileMob(nextEntityId++, type, x, y);
        map.getEntities().add(mob);
    }

    public void resetPlayer(Player player, int width, int height) {
        player.setX(width / 2);
        player.setY(height - 2);
        player.setHealth(10);
    }
}
