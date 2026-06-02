package repository;

import models.Player;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton repository for Player persistence.
 */
public class PlayerRepository extends BaseRepository<Player> {
    private static PlayerRepository instance;

    private PlayerRepository() {
        super();
    }

    public static PlayerRepository getInstance() {
        if (instance == null) {
            instance = new PlayerRepository();
        }
        return instance;
    }

    @Override
    public void create(Player player, int chunkId) throws SQLException {
        String sql = "INSERT OR REPLACE INTO player (id, health, x, y, current_chunk) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, player.getId());
            pstmt.setInt(2, player.getHealth());
            pstmt.setInt(3, player.getX());
            pstmt.setInt(4, player.getY());
            pstmt.setInt(5, chunkId);
            pstmt.executeUpdate();
        }
    }

    public Player loadPlayer(int id) throws SQLException {
        String sql = "SELECT * FROM player WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Player p = new Player(rs.getInt("id"), rs.getInt("x"), rs.getInt("y"));
                    p.setHealth(rs.getInt("health"));
                    // We can return the chunk ID separately or store it in the player object if needed
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public List<Player> readAll(int chunkId) throws SQLException {
        return new ArrayList<>();
    }

    @Override
    public void update(Player player, int chunkId) throws SQLException {
        create(player, chunkId);
    }

    @Override
    public void delete(Player player, int chunkId) throws SQLException {
        String sql = "DELETE FROM player WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, player.getId());
            pstmt.executeUpdate();
        }
    }
}
