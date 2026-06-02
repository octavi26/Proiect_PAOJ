package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton class to manage the SQLite database connection.
 * Requirement: Stage II - JDBC Persistence.
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:terraria.db";

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
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
        }
    }
}
