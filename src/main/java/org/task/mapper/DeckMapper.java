package org.task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.task.models.entity.Deck;
import org.task.utils.TimeConverter;

import java.sql.ResultSet;
import java.sql.SQLException;

@Mapper
public interface DeckMapper {
    DeckMapper INSTANCE = Mappers.getMapper(DeckMapper.class);

    default Deck toDeck(ResultSet rs) throws SQLException {
        return Deck.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .createdAt(TimeConverter.mapTimestamp(rs, "created_at"))
                .updatedAt(TimeConverter.mapTimestamp(rs, "updated_at"))
                .build();
    }
}
