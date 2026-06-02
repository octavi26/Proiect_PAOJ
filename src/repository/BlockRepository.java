package repository;

import models.Block;
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
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // We need the x and y. Assuming we pass them or extract from somewhere.
            // For blocks, the service will handle the grid coordinates.
        }
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

    @Override
    public List<Block> readAll(int chunkId) throws SQLException {
        // Blocks need position to be useful in the grid.
        // Returning a list might not be enough without x/y info.
        return new ArrayList<>();
    }

    @Override
    public void update(Block obj, int chunkId) throws SQLException {}

    @Override
    public void delete(Block obj, int chunkId) throws SQLException {}
}
