package org.task.repository.configuration;

import org.task.config.DatabaseManager;
import org.task.exception.RepositoryException;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractRepository<T, I> implements BaseRepository<T, I> {
    protected final DatabaseManager databaseManager;

    protected AbstractRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    protected <R> R executeWithConnection(DatabaseOperation<R> operation) {
        try (Connection conn = databaseManager.getConnection()) {
            return operation.execute(conn);
        } catch (SQLException e) {
            throw new RepositoryException("Database operation failed", e);
        }
    }

    protected void executeWithConnection(VoidDatabaseOperation operation) {
        try (Connection conn = databaseManager.getConnection()) {
            operation.execute(conn);
        } catch (SQLException e) {
            throw new RepositoryException("Database operation failed", e);
        }
    }

    @FunctionalInterface
    protected interface DatabaseOperation<R> {
        R execute(Connection conn) throws SQLException;
    }

    @FunctionalInterface
    protected interface VoidDatabaseOperation {
        void execute(Connection conn) throws SQLException;
    }
}
