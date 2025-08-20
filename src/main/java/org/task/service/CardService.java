package org.task.service;

import org.task.models.dto.CardRequest;
import org.task.models.entity.Card;

import java.util.List;

public interface CardService {
    String CARD_NOT_FOUND = "Card Not Found";

    Card create(CardRequest request);

    List<Card> getCards();

    Card getCardById(Long cardId);

    Card update(Long cardId, CardRequest request);

    boolean delete(Long cardId);

    List<Card> getCardsByDeckId(Long id);

    long getTotalCardCount();

    List<Card> searchCards(String searchText, Long id);
}
