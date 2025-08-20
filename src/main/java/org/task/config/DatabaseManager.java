package org.task.config;

import org.task.exception.DataBaseConnectionException;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DatabaseManager {
    private static DatabaseManager instance;
    private final String url;
    private final String user;
    private final String password;
    private final String driver;

    protected static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());

    protected DatabaseManager() {
        Properties props = loadProperties();
        this.url = props.getProperty("db.url");
        this.user = props.getProperty("db.user");
        this.password = props.getProperty("db.password");
        this.driver = props.getProperty("db.driver");

        try {
            Class.forName(driver);
            logger.log(Level.INFO, "Database driver loaded successfully: {0}", driver);
        } catch (ClassNotFoundException e) {
            throw new DataBaseConnectionException("Database driver не знайдено: " + driver, e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseLoading();
                }
            }
        }
        return instance;
    }

    public final Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            logger.log(Level.FINE, "Database connection established");
            return connection;
        } catch (SQLException e) {
            throw new DataBaseConnectionException("Cannot connect to database", e);
        }
    }


    public final boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Database connection test failed", e);
            return false;
        }
    }


    protected void createTables(Connection conn) throws SQLException {
        logger.log(Level.INFO, "Creating database tables...");

        try (Statement stmt = conn.createStatement()) {
            // Створення таблиці колод
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS decks (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(255) NOT NULL UNIQUE,
                            description TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS cards (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            question TEXT NOT NULL,
                            answer TEXT NOT NULL,
                            deck_id BIGINT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE
                        )
                    """);

            createIndexes(stmt);

            logger.log(Level.INFO, "Database tables created successfully");
        }
    }

    protected void createIndexes(Statement stmt) throws SQLException {
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_cards_deck_id ON cards(deck_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_decks_name ON decks(name)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_decks_created_at ON decks(created_at)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_cards_created_at ON cards(created_at)");

        logger.log(Level.INFO, "Database indexes created successfully");
    }

    public final DatabaseMetaData getDatabaseMetadata() {
        try (Connection conn = getConnection()) {
            return conn.getMetaData();
        } catch (SQLException e) {
            throw new DataBaseConnectionException("Cannot get database metadata", e);
        }
    }

    public final void clearAllTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("TRUNCATE TABLE cards");
            stmt.execute("TRUNCATE TABLE decks");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");

            logger.log(Level.INFO, "All tables cleared successfully");
        } catch (SQLException e) {
            throw new DataBaseConnectionException("Cannot clear tables", e);
        }
    }

    public final void shutdown() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SHUTDOWN");
            logger.log(Level.INFO, "Database shutdown completed successfully");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error during database shutdown: {0}", e.getMessage());
        }
    }

    protected final String getUrl() {
        return url;
    }

    protected final String getUser() {
        return user;
    }

    protected final String getPassword() {
        return password;
    }

    protected final String getDriver() {
        return driver;
    }

    protected abstract Properties loadProperties();

    public abstract void initializeDatabase();

    protected abstract void configureDatabase(Connection conn) throws SQLException;
}
