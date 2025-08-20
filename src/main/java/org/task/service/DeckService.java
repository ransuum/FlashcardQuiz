package org.task.service;

import org.task.models.dto.DeckRequest;
import org.task.models.entity.Deck;

import java.util.List;

public interface DeckService {
    String DECK_NOT_FOUND = "Deck not found";

    Deck createDeck(DeckRequest request);

    List<Deck> getAllDecks();

    Deck getDeckById(Long id);

    Deck getDeckByName(String name);

    boolean deleteDeck(Long id);

    Deck updateDeck(Deck deck);

    long getDeckCount();
}
