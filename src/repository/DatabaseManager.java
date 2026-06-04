package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:terraria.db";

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite Driver not found");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Table for Blocks (Foreground)
            stmt.execute("CREATE TABLE IF NOT EXISTS blocks (" +
                    "chunk_id INTEGER, x INTEGER, y INTEGER, type TEXT, " +
                    "PRIMARY KEY (chunk_id, x, y))");

            // Table for Walls (Background)
            stmt.execute("CREATE TABLE IF NOT EXISTS walls (" +
                    "chunk_id INTEGER, x INTEGER, y INTEGER, type TEXT, " +
                    "PRIMARY KEY (chunk_id, x, y))");

            // Table for Entities (Zombies, Bunnies, etc.)
            stmt.execute("CREATE TABLE IF NOT EXISTS entities (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, chunk_id INTEGER, " +
                    "type TEXT, x INTEGER, y INTEGER, health INTEGER)");

            // Table for Player Stats
            stmt.execute("CREATE TABLE IF NOT EXISTS player (" +
                    "id INTEGER PRIMARY KEY, health INTEGER, x INTEGER, y INTEGER, " +
                    "current_chunk INTEGER)");

            // Table for Inventory (Requirement: many-to-one relationship/persistence)
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory (" +
                    "player_id INTEGER, item_name TEXT, quantity INTEGER, " +
                    "PRIMARY KEY (player_id, item_name))");
        }
    }

    public void resetDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM blocks");
            stmt.execute("DELETE FROM walls");
            stmt.execute("DELETE FROM entities");
            stmt.execute("DELETE FROM inventory");
            // We keep player stats to avoid resetting position
        }
    }
}
