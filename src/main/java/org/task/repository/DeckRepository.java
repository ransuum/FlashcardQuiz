package org.task.repository;

import org.task.config.DatabaseManager;
import org.task.mapper.DeckMapper;
import org.task.models.entity.Deck;
import org.task.repository.configuration.AbstractRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeckRepository extends AbstractRepository<Deck, Long> {
    private static final Logger logger = Logger.getLogger(DeckRepository.class.getName());
    private final CardRepository cardRepository;

    public DeckRepository(DatabaseManager databaseManager, CardRepository cardRepository) {
        super(databaseManager);
        this.cardRepository = cardRepository;
    }

    @Override
    public Deck save(Deck entity) {
        final String sql = """
                    INSERT INTO decks (name, description, created_at, updated_at) 
                    VALUES (?, ?, ?, ?)
                """;

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, entity.getName());
                stmt.setString(2, entity.getDescription());
                stmt.setTimestamp(3, Timestamp.valueOf(entity.getCreatedAt()));
                stmt.setTimestamp(4, Timestamp.valueOf(entity.getUpdatedAt()));

                if (stmt.executeUpdate() == 0)
                    throw new SQLException("Creating deck failed, no rows affected");

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                        logger.log(Level.INFO, "Deck saved with ID: {0}", entity.getId());
                        return entity;
                    } else throw new SQLException("Creating deck failed, no ID obtained");
                }
            }
        });
    }

    @Override
    public Deck update(Deck entity) {
        final String sql = """
                    UPDATE decks 
                    SET name = ?, description = ?, updated_at = ? 
                    WHERE id = ?
                """;

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                entity.setUpdatedAt(LocalDateTime.now());

                stmt.setString(1, entity.getName());
                stmt.setString(2, entity.getDescription());
                stmt.setTimestamp(3, Timestamp.valueOf(entity.getUpdatedAt()));
                stmt.setLong(4, entity.getId());

                if (stmt.executeUpdate() == 0)
                    throw new SQLException("Updating deck failed, deck not found with ID: " + entity.getId());


                logger.log(Level.INFO, "Deck updated with ID: {0}", entity.getId());
                return entity;
            }
        });
    }

    @Override
    public Optional<Deck> findById(Long id) {
        final String sql = "SELECT * FROM decks WHERE id = ?";

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Deck deck = DeckMapper.INSTANCE.toDeck(rs);
                        deck.setCards(cardRepository.findByDeckId(deck.getId()));
                        return Optional.of(deck);
                    }
                }
                return Optional.empty();
            }
        });
    }

    @Override
    public List<Deck> findAll() {
        final String sql = "SELECT * FROM decks ORDER BY created_at DESC";

        return executeWithConnection(conn -> {
            List<Deck> decks = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Deck deck = DeckMapper.INSTANCE.toDeck(rs);
                    deck.setCards(cardRepository.findByDeckId(deck.getId()));
                    decks.add(deck);
                }
            }
            return decks;
        });
    }

    @Override
    public boolean deleteById(Long id) {
        final String sql = "DELETE FROM decks WHERE id = ?";

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);

                final boolean deleted = stmt.executeUpdate() > 0;

                if (deleted) logger.log(Level.INFO, "Deck deleted with ID: {0}", id);

                return deleted;
            }
        });
    }

    @Override
    public boolean delete(Deck deck) {
        return deleteById(deck.getId());
    }

    @Override
    public boolean existsById(Long id) {
        final String sql = "SELECT 1 FROM decks WHERE id = ? LIMIT 1";

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);

                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public long count() {
        final String sql = "SELECT COUNT(*) FROM decks";

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) return rs.getLong(1);
                return 0L;
            }
        });
    }

    public Optional<Deck> findByName(String name) {
        final String sql = "SELECT * FROM decks WHERE name = ?";

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        final var deck = DeckMapper.INSTANCE.toDeck(rs);
                        deck.setCards(cardRepository.findByDeckId(deck.getId()));
                        return Optional.of(deck);
                    }
                }
                return Optional.empty();
            }
        });
    }
}
