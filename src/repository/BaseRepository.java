package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic Singleton Repository for basic database operations.
 * Requirement: Stage II - Generic Singleton Services for DB.
 */
public abstract class BaseRepository<T> {
    protected Connection connection;

    protected BaseRepository() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // Abstract methods to be implemented by specific repositories
    public abstract void create(T obj, int chunkId) throws SQLException;
    public abstract List<T> readAll(int chunkId) throws SQLException;
    public abstract void update(T obj, int chunkId) throws SQLException;
    public abstract void delete(T obj, int chunkId) throws SQLException;
}
