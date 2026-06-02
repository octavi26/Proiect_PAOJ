package repository;

import models.Entity;
import models.HostileMob;
import models.PassiveMob;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton repository for Entity persistence.
 */
public class EntityRepository extends BaseRepository<Entity> {
    private static EntityRepository instance;

    private EntityRepository() {
        super();
    }

    public static EntityRepository getInstance() {
        if (instance == null) {
            instance = new EntityRepository();
        }
        return instance;
    }

    @Override
    public void create(Entity entity, int chunkId) throws SQLException {
        String sql = "INSERT INTO entities (chunk_id, type, x, y, health) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chunkId);
            String type = entity instanceof HostileMob ? "HostileMob" : "PassiveMob";
            pstmt.setString(2, type);
            pstmt.setInt(3, entity.getX());
            pstmt.setInt(4, entity.getY());
            pstmt.setInt(5, entity.getHealth());
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Entity> readAll(int chunkId) throws SQLException {
        List<Entity> entities = new ArrayList<>();
        String sql = "SELECT * FROM entities WHERE chunk_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chunkId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("type");
                    int id = rs.getInt("id");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int hp = rs.getInt("health");

                    if (type.equals("HostileMob")) {
                        HostileMob h = new HostileMob(id, "Zombie", x, y);
                        h.setHealth(hp);
                        entities.add(h);
                    } else {
                        PassiveMob p = new PassiveMob(id, "Bunny", x, y);
                        p.setHealth(hp);
                        entities.add(p);
                    }
                }
            }
        }
        return entities;
    }

    public void clearChunkEntities(int chunkId) throws SQLException {
        String sql = "DELETE FROM entities WHERE chunk_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chunkId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void update(Entity obj, int chunkId) throws SQLException {}

    @Override
    public void delete(Entity obj, int chunkId) throws SQLException {}
}
