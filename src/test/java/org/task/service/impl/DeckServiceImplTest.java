package org.task.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.task.exception.EntityAlreadyExists;
import org.task.exception.NotFoundException;
import org.task.models.dto.DeckRequest;
import org.task.models.entity.Deck;
import org.task.repository.DeckRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeckService Tests")
class DeckServiceImplTest {

    @Mock
    private DeckRepository deckRepository;

    @InjectMocks
    private DeckServiceImpl deckService;

    private Deck sampleDeck;
    private DeckRequest sampleDeckRequest;

    @BeforeEach
    void setUp() {
        sampleDeck = Deck.builder()
                .id(1L)
                .name("Java Basics")
                .description("Basic Java concepts")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleDeckRequest = new DeckRequest("Java Basics", "Basic Java concepts");
    }

    @Test
    @DisplayName("Should create deck successfully when deck name doesn't exist")
    void createDeck_ShouldCreateDeckSuccessfully_WhenDeckNameDoesNotExist() {
        // Given
        when(deckRepository.findByName(sampleDeckRequest.name())).thenReturn(Optional.empty());
        when(deckRepository.save(any(Deck.class))).thenReturn(sampleDeck);

        // When
        Deck result = deckService.createDeck(sampleDeckRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Java Basics");
        assertThat(result.getDescription()).isEqualTo("Basic Java concepts");

        verify(deckRepository).findByName("Java Basics");
        verify(deckRepository).save(any(Deck.class));
    }

    @Test
    @DisplayName("Should throw EntityAlreadyExists when deck name already exists")
    void createDeck_ShouldThrowEntityAlreadyExists_WhenDeckNameAlreadyExists() {
        // Given
        when(deckRepository.findByName(sampleDeckRequest.name())).thenReturn(Optional.of(sampleDeck));

        // When & Then
        assertThatThrownBy(() -> deckService.createDeck(sampleDeckRequest))
                .isInstanceOf(EntityAlreadyExists.class)
                .hasMessageContaining("Deck with name Java Basics already exists");

        verify(deckRepository).findByName("Java Basics");
        verify(deckRepository, never()).save(any(Deck.class));
    }

    @Test
    @DisplayName("Should return all decks when getAllDecks is called")
    void getAllDecks_ShouldReturnAllDecks() {
        // Given
        Deck deck2 = Deck.builder()
                .id(2L)
                .name("Spring Boot")
                .description("Spring Boot concepts")
                .build();

        List<Deck> expectedDecks = Arrays.asList(sampleDeck, deck2);
        when(deckRepository.findAll()).thenReturn(expectedDecks);

        // When
        List<Deck> result = deckService.getAllDecks();

        // Then
        assertThat(result)
                .hasSize(2)
                .containsExactly(sampleDeck, deck2);

        verify(deckRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no decks exist")
    void getAllDecks_ShouldReturnEmptyList_WhenNoDecksExist() {
        // Given
        when(deckRepository.findAll()).thenReturn(List.of());

        // When
        List<Deck> result = deckService.getAllDecks();

        // Then
        assertThat(result).isEmpty();
        verify(deckRepository).findAll();
    }

    @Test
    @DisplayName("Should return deck when getDeckById is called with valid ID")
    void getDeckById_ShouldReturnDeck_WhenValidIdProvided() {
        // Given
        Long deckId = 1L;
        when(deckRepository.findById(deckId)).thenReturn(Optional.of(sampleDeck));

        // When
        Deck result = deckService.getDeckById(deckId);

        // Then
        assertThat(result).isEqualTo(sampleDeck);
        verify(deckRepository).findById(deckId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when getDeckById is called with invalid ID")
    void getDeckById_ShouldThrowNotFoundException_WhenInvalidIdProvided() {
        // Given
        Long invalidId = 999L;
        when(deckRepository.findById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> deckService.getDeckById(invalidId))
                .isInstanceOf(NotFoundException.class);

        verify(deckRepository).findById(invalidId);
    }

    @Test
    @DisplayName("Should return deck when getDeckByName is called with valid name")
    void getDeckByName_ShouldReturnDeck_WhenValidNameProvided() {
        // Given
        String deckName = "Java Basics";
        when(deckRepository.findByName(deckName)).thenReturn(Optional.of(sampleDeck));

        // When
        Deck result = deckService.getDeckByName(deckName);

        // Then
        assertThat(result).isEqualTo(sampleDeck);
        verify(deckRepository).findByName(deckName);
    }

    @Test
    @DisplayName("Should throw NotFoundException when getDeckByName is called with invalid name")
    void getDeckByName_ShouldThrowNotFoundException_WhenInvalidNameProvided() {
        // Given
        String invalidName = "Non-existent Deck";
        when(deckRepository.findByName(invalidName)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> deckService.getDeckByName(invalidName))
                .isInstanceOf(NotFoundException.class);

        verify(deckRepository).findByName(invalidName);
    }

    @Test
    @DisplayName("Should delete deck successfully when valid ID is provided")
    void deleteDeck_ShouldDeleteSuccessfully_WhenValidIdProvided() {
        // Given
        Long deckId = 1L;
        when(deckRepository.deleteById(deckId)).thenReturn(true);

        // When
        boolean result = deckService.deleteDeck(deckId);

        // Then
        assertThat(result).isTrue();
        verify(deckRepository).deleteById(deckId);
    }

    @Test
    @DisplayName("Should return false when trying to delete non-existent deck")
    void deleteDeck_ShouldReturnFalse_WhenDeckDoesNotExist() {
        // Given
        Long nonExistentId = 999L;
        when(deckRepository.deleteById(nonExistentId)).thenReturn(false);

        // When
        boolean result = deckService.deleteDeck(nonExistentId);

        // Then
        assertThat(result).isFalse();
        verify(deckRepository).deleteById(nonExistentId);
    }

    @Test
    @DisplayName("Should update deck successfully")
    void updateDeck_ShouldUpdateSuccessfully() {
        // Given
        Deck updatedDeck = Deck.builder()
                .id(1L)
                .name("Java Advanced")
                .description("Advanced Java concepts")
                .createdAt(sampleDeck.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(deckRepository.update(sampleDeck)).thenReturn(updatedDeck);

        // When
        Deck result = deckService.updateDeck(sampleDeck);

        // Then
        assertThat(result).isEqualTo(updatedDeck);
        verify(deckRepository).update(sampleDeck);
    }

    @Test
    @DisplayName("Should return correct deck count")
    void getDeckCount_ShouldReturnCorrectCount() {
        // Given
        long expectedCount = 5L;
        when(deckRepository.count()).thenReturn(expectedCount);

        // When
        long result = deckService.getDeckCount();

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(deckRepository).count();
    }
}