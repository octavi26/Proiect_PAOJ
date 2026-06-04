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
        saveInventory(player);
    }

    private void saveInventory(Player player) throws SQLException {
        String deleteSql = "DELETE FROM inventory WHERE player_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
            pstmt.setInt(1, player.getId());
            pstmt.executeUpdate();
        }

        // Insert current items
        String insertSql = "INSERT INTO inventory (player_id, item_name, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            for (var entry : player.getInventory().getItems().entrySet()) {
                pstmt.setInt(1, player.getId());
                pstmt.setString(2, entry.getKey());
                pstmt.setInt(3, entry.getValue());
                pstmt.executeUpdate();
            }
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
                    loadInventory(p);
                    return p;
                }
            }
        }
        return null;
    }

    private void loadInventory(Player player) throws SQLException {
        String sql = "SELECT * FROM inventory WHERE player_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, player.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    player.getInventory().addItem(rs.getString("item_name"), rs.getInt("quantity"));
                }
            }
        }
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
