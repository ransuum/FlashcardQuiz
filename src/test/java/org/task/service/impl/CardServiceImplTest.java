package org.task.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.task.exception.NotFoundException;
import org.task.models.dto.CardRequest;
import org.task.models.entity.Card;
import org.task.repository.CardRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardService Tests")
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card sampleCard;
    private CardRequest sampleCardRequest;

    @BeforeEach
    void setUp() {
        sampleCard = Card.builder()
                .id(1L)
                .question("What is Java?")
                .answer("Java is a programming language")
                .deckId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleCardRequest = new CardRequest("What is Java?", "Java is a programming language", 1L);
    }

    @Test
    @DisplayName("Should create card successfully")
    void create_ShouldCreateCardSuccessfully() {
        // Given
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        // When
        Card result = cardService.create(sampleCardRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuestion()).isEqualTo("What is Java?");
        assertThat(result.getAnswer()).isEqualTo("Java is a programming language");
        assertThat(result.getDeckId()).isEqualTo(1L);

        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Should return all cards")
    void getCards_ShouldReturnAllCards() {
        // Given
        Card card2 = Card.builder()
                .id(2L)
                .question("What is Spring?")
                .answer("Spring is a framework")
                .deckId(1L)
                .build();

        List<Card> expectedCards = Arrays.asList(sampleCard, card2);
        when(cardRepository.findAll()).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.getCards();

        // Then
        assertThat(result)
                .hasSize(2)
                .containsExactly(sampleCard, card2);

        verify(cardRepository).findAll();
    }

    @Test
    @DisplayName("Should return card by ID when valid ID is provided")
    void getCardById_ShouldReturnCard_WhenValidIdProvided() {
        // Given
        Long cardId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(sampleCard));

        // When
        Card result = cardService.getCardById(cardId);

        // Then
        assertThat(result).isEqualTo(sampleCard);
        verify(cardRepository).findById(cardId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when card ID does not exist")
    void getCardById_ShouldThrowNotFoundException_WhenInvalidIdProvided() {
        // Given
        Long invalidId = 999L;
        when(cardRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cardService.getCardById(invalidId))
                .isInstanceOf(NotFoundException.class);

        verify(cardRepository).findById(invalidId);
    }

    @Test
    @DisplayName("Should update card successfully")
    void update_ShouldUpdateCardSuccessfully() {
        // Given
        CardRequest updateRequest = new CardRequest("Updated question", "Updated answer", 1L);
        Card existingCard = Card.builder()
                .id(1L)
                .question("Old question")
                .answer("Old answer")
                .deckId(1L)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        Card updatedCard = Card.builder()
                .id(1L)
                .question("Updated question")
                .answer("Updated answer")
                .deckId(1L)
                .createdAt(existingCard.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(existingCard));
        when(cardRepository.update(any(Card.class))).thenReturn(updatedCard);

        // When
        Card result = cardService.update(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuestion()).isEqualTo("Updated question");
        assertThat(result.getAnswer()).isEqualTo("Updated answer");

        verify(cardRepository).findById(1L);
        verify(cardRepository).update(any(Card.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when updating non-existent card")
    void update_ShouldThrowNotFoundException_WhenCardDoesNotExist() {
        // Given
        Long invalidId = 999L;
        CardRequest updateRequest = new CardRequest("Updated question", "Updated answer", 1L);
        when(cardRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cardService.update(invalidId, updateRequest))
                .isInstanceOf(NotFoundException.class);

        verify(cardRepository).findById(invalidId);
        verify(cardRepository, never()).update(any(Card.class));
    }

    @Test
    @DisplayName("Should delete card successfully")
    void delete_ShouldDeleteSuccessfully() {
        // Given
        Long cardId = 1L;
        when(cardRepository.deleteById(cardId)).thenReturn(true);

        // When
        boolean result = cardService.delete(cardId);

        // Then
        assertThat(result).isTrue();
        verify(cardRepository).deleteById(cardId);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent card")
    void delete_ShouldReturnFalse_WhenCardDoesNotExist() {
        // Given
        Long nonExistentId = 999L;
        when(cardRepository.deleteById(nonExistentId)).thenReturn(false);

        // When
        boolean result = cardService.delete(nonExistentId);

        // Then
        assertThat(result).isFalse();
        verify(cardRepository).deleteById(nonExistentId);
    }

    @Test
    @DisplayName("Should return cards by deck ID")
    void getCardsByDeckId_ShouldReturnCardsForDeck() {
        // Given
        Long deckId = 1L;
        Card card2 = Card.builder()
                .id(2L)
                .question("What is Spring Boot?")
                .answer("Spring Boot is a framework")
                .deckId(deckId)
                .build();

        List<Card> expectedCards = Arrays.asList(sampleCard, card2);
        when(cardRepository.findByDeckId(deckId)).thenReturn(expectedCards);

        // When
        List<Card> result = cardService.getCardsByDeckId(deckId);

        // Then
        assertThat(result)
                .hasSize(2)
                .containsExactly(sampleCard, card2);

        verify(cardRepository).findByDeckId(deckId);
    }

    @Test
    @DisplayName("Should return empty list when deck has no cards")
    void getCardsByDeckId_ShouldReturnEmptyList_WhenDeckHasNoCards() {
        // Given
        Long deckId = 1L;
        when(cardRepository.findByDeckId(deckId)).thenReturn(List.of());

        // When
        List<Card> result = cardService.getCardsByDeckId(deckId);

        // Then
        assertThat(result).isEmpty();
        verify(cardRepository).findByDeckId(deckId);
    }

    @Test
    @DisplayName("Should return correct total card count")
    void getTotalCardCount_ShouldReturnCorrectCount() {
        // Given
        long expectedCount = 10L;
        when(cardRepository.count()).thenReturn(expectedCount);

        // When
        long result = cardService.getTotalCardCount();

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(cardRepository).count();
    }

    @Test
    @DisplayName("Should search cards successfully")
    void searchCards_ShouldReturnMatchingCards() {
        // Given
        String searchText = "Java";
        Long deckId = 1L;
        List<Card> matchingCards = Arrays.asList(sampleCard);
        when(cardRepository.findByTextContaining(searchText)).thenReturn(matchingCards);

        // When
        List<Card> result = cardService.searchCards(searchText, deckId);

        // Then
        assertThat(result)
                .hasSize(1)
                .containsExactly(sampleCard);

        verify(cardRepository).findByTextContaining(searchText);
    }
}
