package org.task.service.impl;

import org.task.exception.NotFoundException;
import org.task.models.dto.CardRequest;
import org.task.models.entity.Card;
import org.task.repository.CardRepository;
import org.task.service.CardService;

import java.util.List;

public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;

    public CardServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }


    @Override
    public Card create(CardRequest request) {
        return cardRepository.save(new Card(request.question(), request.answer(), request.deckId()));
    }

    @Override
    public List<Card> getCards() {
        return cardRepository.findAll();
    }

    @Override
    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(CARD_NOT_FOUND));
    }

    @Override
    public Card update(Long cardId, CardRequest request) {
        Card existingCard = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(CARD_NOT_FOUND));

        existingCard.setQuestion(request.question());
        existingCard.setAnswer(request.answer());

        return cardRepository.update(existingCard);
    }

    @Override
    public boolean delete(Long cardId) {
        return cardRepository.deleteById(cardId);
    }

    @Override
    public List<Card> getCardsByDeckId(Long id) {
        return cardRepository.findByDeckId(id);
    }

    @Override
    public long getTotalCardCount() {
        return cardRepository.count();
    }

    @Override
    public List<Card> searchCards(String searchText, Long id) {
        return cardRepository.findByTextContaining(searchText);
    }
}
