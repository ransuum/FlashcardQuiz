package org.task.service.impl;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.task.exception.EntityAlreadyExists;
import org.task.exception.NotFoundException;
import org.task.models.dto.DeckRequest;
import org.task.models.entity.Deck;
import org.task.repository.DeckRepository;
import org.task.service.DeckService;

import java.util.List;

public class DeckServiceImpl implements DeckService {
    private final DeckRepository deckRepository;

    public DeckServiceImpl(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    @Override
    public Deck createDeck(DeckRequest request) {
        deckRepository.findByName(request.name()).ifPresent(deck -> {
            throw new EntityAlreadyExists("Deck with name " + request.name() + " already exists");
        });

        return deckRepository.save(new Deck(request.name(), request.description()));
    }

    @Override
    public List<Deck> getAllDecks() {
        return deckRepository.findAll();
    }

    @Override
    public Deck getDeckById(@NotNull Long id) {
        return deckRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(DECK_NOT_FOUND));
    }

    @Override
    public Deck getDeckByName(@NotBlank String name) {
        return deckRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException(DECK_NOT_FOUND));
    }

    @Override
    public boolean deleteDeck(@NotNull Long id) {
        return deckRepository.deleteById(id);
    }

    @Override
    public Deck updateDeck(Deck deck) {
        return deckRepository.update(deck);
    }

    @Override
    public long getDeckCount() {
        return deckRepository.count();
    }
}
