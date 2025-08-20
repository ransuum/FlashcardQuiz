package org.task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.task.models.entity.Card;
import org.task.utils.TimeConverter;

import java.sql.ResultSet;
import java.sql.SQLException;

@Mapper
public interface CardMapper {
    CardMapper INSTANCE = Mappers.getMapper(CardMapper.class);

    default Card toCard(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        if (resultSet.wasNull()) id = null;

        return Card.builder()
                .id(id)
                .question(resultSet.getString("question"))
                .answer(resultSet.getString("answer"))
                .deckId(resultSet.getLong("deck_id"))
                .createdAt(TimeConverter.mapTimestamp(resultSet, "created_at"))
                .updatedAt(TimeConverter.mapTimestamp(resultSet, "updated_at"))
                .build();
    }
}
