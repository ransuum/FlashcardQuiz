package org.task.repository;

import org.task.config.DatabaseManager;
import org.task.mapper.CardMapper;
import org.task.models.entity.Card;
import org.task.repository.configuration.AbstractRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CardRepository extends AbstractRepository<Card, Long> {
    private static final Logger logger = Logger.getLogger(CardRepository.class.getName());

    public CardRepository(DatabaseManager databaseManager) {
        super(databaseManager);
    }

    @Override
    public Card save(Card entity) {
        final String sql = """
                    INSERT INTO cards (question, answer, deck_id, created_at, updated_at) 
                    VALUES (?, ?, ?, ?, ?)
                """;

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, entity.getQuestion());
                stmt.setString(2, entity.getAnswer());
                stmt.setLong(3, entity.getDeckId());
                stmt.setTimestamp(4, Timestamp.valueOf(entity.getCreatedAt()));
                stmt.setTimestamp(5, Timestamp.valueOf(entity.getUpdatedAt()));

                final int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) throw new SQLException("Creating card failed, no rows affected");

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                        logger.log(Level.INFO, "Card saved with ID: {0}", entity.getId());
                        return entity;
                    } else {
                        throw new SQLException("Creating card failed, no ID obtained");
                    }
                }
            }
        });
    }

    @Override
    public Card update(Card entity) {
        final String sql = """
                    UPDATE cards 
                    SET question = ?, answer = ?, updated_at = ? 
                    WHERE id = ?
                """;

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                entity.setUpdatedAt(LocalDateTime.now());

                stmt.setString(1, entity.getQuestion());
                stmt.setString(2, entity.getAnswer());
                stmt.setTimestamp(3, Timestamp.valueOf(entity.getUpdatedAt()));
                stmt.setLong(4, entity.getId());

                final int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0)
                    throw new SQLException("Updating card failed, card not found with ID: " + entity.getId());

                logger.log(Level.INFO, "Card updated with ID: {0}", entity.getId());
                return entity;
            }
        });
    }

    @Override
    public Optional<Card> findById(Long id) {
        final String sql = "SELECT * FROM cards WHERE id = ?";

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return Optional.of(CardMapper.INSTANCE.toCard(rs));
                }
                return Optional.empty();
            }
        });
    }

    @Override
    public List<Card> findAll() {
        final String sql = "SELECT * FROM cards ORDER BY created_at DESC";

        return executeWithConnection(conn -> {
            List<Card> cards = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) cards.add(CardMapper.INSTANCE.toCard(rs));
            }
            return cards;
        });
    }

    @Override
    public boolean deleteById(Long id) {
        final String sql = "DELETE FROM cards WHERE id = ?";

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);

                final int affectedRows = stmt.executeUpdate();
                final boolean deleted = affectedRows > 0;

                if (deleted) logger.log(Level.INFO, "Card deleted with ID: {0}", id);

                return deleted;
            }
        });
    }

    @Override
    public boolean delete(Card entity) {
        return deleteById(entity.getId());
    }

    @Override
    public boolean existsById(Long id) {
        final String sql = "SELECT 1 FROM cards WHERE id = ? LIMIT 1";

        return executeWithConnection(conn -> {
            try (final PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);

                try (final ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public long count() {
        final String sql = "SELECT COUNT(*) FROM cards";

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) return rs.getLong(1);

                return 0L;
            }
        });
    }

    public long countByDeckId(Long deckId) {
        final String sql = "SELECT COUNT(*) FROM cards WHERE deck_id = ?";

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, deckId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                    return 0L;
                }
            }
        });
    }

    public boolean deleteByDeckId(Long deckId) {
        final String sql = "DELETE FROM cards WHERE deck_id = ?";

        return executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, deckId);

                final int affectedRows = stmt.executeUpdate();
                final boolean deleted = affectedRows > 0;

                if (deleted) logger.log(Level.INFO, "Deleted {0} cards from deck ID: {1}",
                        new Object[]{affectedRows, deckId});

                return deleted;
            }
        });
    }

    public List<Card> findByDeckId(Long deckId) {
        final String sql = "SELECT * FROM cards WHERE deck_id = ? ORDER BY created_at ASC";

        return executeWithConnection(conn -> {
            List<Card> cards = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, deckId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) cards.add(CardMapper.INSTANCE.toCard(rs));
                }
            }
            return cards;
        });
    }

    public List<Card> findByTextContaining(String searchText) {
        final String sql = """
                    SELECT * FROM cards 
                    WHERE LOWER(question) LIKE LOWER(?) OR LOWER(answer) LIKE LOWER(?)
                    ORDER BY created_at DESC
                """;

        return executeWithConnection(conn -> {
            List<Card> cards = new ArrayList<>();
            String searchPattern = "%" + searchText + "%";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) cards.add(CardMapper.INSTANCE.toCard(rs));
                }
            }
            return cards;
        });
    }
}
