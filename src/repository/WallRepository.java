package repository;

import models.Wall;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton repository for Wall persistence.
 */
public class WallRepository extends BaseRepository<Wall> {
    private static WallRepository instance;

    private WallRepository() {
        super();
    }

    public static WallRepository getInstance() {
        if (instance == null) {
            instance = new WallRepository();
        }
        return instance;
    }

    public void saveWall(int chunkId, int x, int y, String type) throws SQLException {
        String sql = "INSERT OR REPLACE INTO walls (chunk_id, x, y, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chunkId);
            pstmt.setInt(2, x);
            pstmt.setInt(3, y);
            pstmt.setString(4, type);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void create(Wall obj, int chunkId) throws SQLException {}

    @Override
    public List<Wall> readAll(int chunkId) throws SQLException {
        return new ArrayList<>();
    }

    @Override
    public void update(Wall obj, int chunkId) throws SQLException {}

    @Override
    public void delete(Wall obj, int chunkId) throws SQLException {}
}
