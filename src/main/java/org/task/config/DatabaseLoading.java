package org.task.config;

import org.task.exception.DataBaseConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;

public class DatabaseLoading extends DatabaseManager {

    @Override
    protected Properties loadProperties() {
        final var props = new Properties();

        final String[] possiblePaths = {
                "application.properties",
                "config/application.properties",
                "database.properties"
        };

        boolean loaded = false;
        for (String path : possiblePaths) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
                if (input != null) {
                    props.load(input);
                    logger.log(Level.INFO, "Properties loaded from: {0}", path);
                    loaded = true;
                    break;
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Cannot load properties from: {0}", path);
            }
        }

        if (!loaded) logger.log(Level.WARNING, "No properties file found, using defaults");

        setDefaultProperties(props);
        validateProperties(props);

        return props;
    }

    private void setDefaultProperties(Properties props) {
        props.putIfAbsent("db.url", "jdbc:h2:file:./data/flashcards;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1");
        props.putIfAbsent("db.user", "sa");
        props.putIfAbsent("db.password", "");
        props.putIfAbsent("db.driver", "org.h2.Driver");

        props.putIfAbsent("db.pool.maxConnections", "10");
        props.putIfAbsent("db.pool.timeout", "30000");
    }

    private void validateProperties(Properties props) {
        final String[] requiredProps = {"db.url", "db.user", "db.driver"};

        for (String prop : requiredProps) {
            if (props.getProperty(prop) == null || props.getProperty(prop).trim().isEmpty()) {
                throw new DataBaseConnectionException("Required property missing: " + prop);
            }
        }
    }

    @Override
    public void initializeDatabase() {
        logger.log(Level.INFO, "Starting database initialization...");

        try (Connection conn = getConnection()) {
            configureDatabase(conn);
            createTables(conn);
            performAdditionalSetup(conn);

            logger.log(Level.INFO, "Database initialization completed successfully");
        } catch (SQLException e) {
            throw new DataBaseConnectionException("Exception while initializing database", e);
        }
    }

    @Override
    protected void configureDatabase(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET DB_CLOSE_DELAY -1");
            stmt.execute("SET CACHE_SIZE 32768");
            stmt.execute("SET LOCK_TIMEOUT 10000");

            stmt.execute("SET MODE REGULAR");

            logger.log(Level.INFO, "Database configured successfully");
        }
    }

    private void performAdditionalSetup(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            final var rs = stmt.executeQuery("SELECT COUNT(*) FROM decks WHERE name = 'Demo Deck'");
            rs.next();
            final int count = rs.getInt(1);

            if (count == 0) {
                stmt.execute("""
                            INSERT INTO decks (name, description) 
                            VALUES ('Demo Deck', 'Демонстраційна колода для ознайомлення з додатком')
                        """);

                logger.log(Level.INFO, "Demo deck created");

                insertDemoCards(stmt);
            } else logger.log(Level.INFO, "Demo deck already exists, skipping creation");

            logger.log(Level.INFO, "Additional database setup completed");
        }
    }

    private void insertDemoCards(Statement stmt) throws SQLException {
        var rs = stmt.executeQuery("SELECT id FROM decks WHERE name = 'Demo Deck'");
        if (rs.next()) {
            long deckId = rs.getLong("id");

            String[] demoCards = {
                    "('Що таке Java?', 'Об''єктно-орієнтована мова програмування', " + deckId + ")",
                    "('Що таке JVM?', 'Java Virtual Machine - віртуальна машина Java', " + deckId + ")",
                    "('Що таке ООП?', 'Об''єктно-орієнтоване програмування', " + deckId + ")",
                    "('Що таке клас?', 'Шаблон для створення об''єктів', " + deckId + ")",
                    "('Що таке інкапсуляція?', 'Приховування внутрішньої реалізації класу', " + deckId + ")"
            };

            for (String cardData : demoCards)
                stmt.execute("INSERT INTO cards (question, answer, deck_id) VALUES " + cardData);


            logger.log(Level.INFO, "Demo cards created: {0}", demoCards.length);
        }
    }
}