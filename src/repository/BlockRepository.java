package repository;

import models.Block;
import models.WorldLayer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton repository for Block persistence.
 */
public class BlockRepository extends BaseRepository<Block> {
    private static BlockRepository instance;

    private BlockRepository() {
        super();
    }

    public static BlockRepository getInstance() {
        if (instance == null) {
            instance = new BlockRepository();
        }
        return instance;
    }

    @Override
    public void create(Block block, int chunkId) throws SQLException {
        String sql = "INSERT OR REPLACE INTO blocks (chunk_id, x, y, type) VALUES (?, ?, ?, ?)";
    }

    // Specialized methods for world loading
    public void saveBlock(int chunkId, int x, int y, String type) throws SQLException {
        String sql = "INSERT OR REPLACE INTO blocks (chunk_id, x, y, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chunkId);
            pstmt.setInt(2, x);
            pstmt.setInt(3, y);
            pstmt.setString(4, type);
            pstmt.executeUpdate();
        }
    }

    public void removeBlock(int chunkId, int x, int y) throws SQLException {
        String sql = "DELETE FROM blocks WHERE chunk_id = ? AND x = ? AND y = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chunkId);
            pstmt.setInt(2, x);
            pstmt.setInt(3, y);
            pstmt.executeUpdate();
        }
    }

    public boolean hasBlocks(int chunkId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM blocks WHERE chunk_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chunkId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public List<Block> readAll(int chunkId) throws SQLException {
        return new ArrayList<>();
    }

    public void loadBlocksIntoLayer(int chunkId, WorldLayer layer) throws SQLException {
        String sql = "SELECT * FROM blocks WHERE chunk_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, chunkId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    String type = rs.getString("type");
                    layer.setObject(x, y, new Block(0, type));
                }
            }
        }
    }

    @Override
    public void update(Block obj, int chunkId) throws SQLException {}

    @Override
    public void delete(Block obj, int chunkId) throws SQLException {}
}
