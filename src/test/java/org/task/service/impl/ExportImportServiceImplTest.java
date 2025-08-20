package org.task.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.task.models.entity.Card;
import org.task.models.entity.Deck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ExportImportService Tests")
class ExportImportServiceImplTest {

    private ExportImportServiceImpl exportImportService;
    private Deck sampleDeck;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        exportImportService = new ExportImportServiceImpl();

        Card card1 = Card.builder()
                .id(1L)
                .question("What is Java?")
                .answer("Java is a programming language")
                .createdAt(LocalDateTime.of(2023, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2023, 1, 1, 10, 0))
                .build();

        Card card2 = Card.builder()
                .id(2L)
                .question("What is Spring?")
                .answer("Spring is a framework")
                .createdAt(LocalDateTime.of(2023, 1, 2, 11, 0))
                .updatedAt(LocalDateTime.of(2023, 1, 2, 11, 0))
                .build();

        sampleDeck = Deck.builder()
                .id(1L)
                .name("Java Basics")
                .description("Basic Java concepts")
                .cards(Arrays.asList(card1, card2))
                .createdAt(LocalDateTime.of(2023, 1, 1, 9, 0))
                .updatedAt(LocalDateTime.of(2023, 1, 1, 9, 0))
                .build();
    }

    @Test
    @DisplayName("Should export deck to JSON successfully")
    void exportDeckToJson_ShouldExportSuccessfully() throws IOException {
        // Given
        Path jsonFile = tempDir.resolve("test-deck.json");

        // When
        exportImportService.exportDeckToJson(sampleDeck, jsonFile);

        // Then
        assertThat(jsonFile).exists();
        assertThat(Files.size(jsonFile)).isGreaterThan(0);

        String jsonContent = Files.readString(jsonFile);
        assertThat(jsonContent)
                .contains("Java Basics")
                .contains("Basic Java concepts")
                .contains("What is Java?")
                .contains("Java is a programming language");
    }

    @Test
    @DisplayName("Should import deck from JSON successfully")
    void importDeckFromJson_ShouldImportSuccessfully() throws IOException {
        // Given
        Path jsonFile = tempDir.resolve("test-deck.json");
        exportImportService.exportDeckToJson(sampleDeck, jsonFile);

        // When
        Deck importedDeck = exportImportService.importDeckFromJson(jsonFile);

        // Then
        assertThat(importedDeck).isNotNull();
        assertThat(importedDeck.getName()).isEqualTo("Java Basics");
        assertThat(importedDeck.getDescription()).isEqualTo("Basic Java concepts");
        assertThat(importedDeck.getCards()).hasSize(2);

        Card firstCard = importedDeck.getCards().getFirst();
        assertThat(firstCard.getQuestion()).isEqualTo("What is Java?");
        assertThat(firstCard.getAnswer()).isEqualTo("Java is a programming language");
    }

    @Test
    @DisplayName("Should throw IOException when importing non-existent JSON file")
    void importDeckFromJson_ShouldThrowIOException_WhenFileDoesNotExist() {
        // Given
        Path nonExistentFile = tempDir.resolve("non-existent.json");

        // When & Then
        assertThatThrownBy(() -> exportImportService.importDeckFromJson(nonExistentFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("File not found");
    }

    @Test
    @DisplayName("Should export deck to CSV successfully")
    void exportDeckToCsv_ShouldExportSuccessfully() throws IOException {
        // Given
        Path csvFile = tempDir.resolve("test-deck.csv");

        // When
        exportImportService.exportDeckToCsv(sampleDeck, csvFile);

        // Then
        assertThat(csvFile).exists();
        assertThat(Files.size(csvFile)).isGreaterThan(0);

        String csvContent = Files.readString(csvFile);
        assertThat(csvContent)
                .contains("Question,Answer,Created_At,Updated_At")
                .contains("What is Java?")
                .contains("Java is a programming language")
                .contains("What is Spring?")
                .contains("Spring is a framework");
    }

    @Test
    @DisplayName("Should import deck from CSV successfully")
    void importDeckFromCsv_ShouldImportSuccessfully() throws IOException {
        // Given
        Path csvFile = tempDir.resolve("test-deck.csv");
        String csvContent = """
                Question,Answer,Created_At,Updated_At
                "What is Java?","Java is a programming language","2023-01-01 10:00:00","2023-01-01 10:00:00"
                "What is Spring?","Spring is a framework","2023-01-02 11:00:00","2023-01-02 11:00:00"
                """;
        Files.writeString(csvFile, csvContent);

        // When
        Deck importedDeck = exportImportService.importDeckFromCsv(csvFile, "Imported Deck", "Imported from CSV");

        // Then
        assertThat(importedDeck).isNotNull();
        assertThat(importedDeck.getName()).isEqualTo("Imported Deck");
        assertThat(importedDeck.getDescription()).isEqualTo("Imported from CSV");
        assertThat(importedDeck.getCards()).hasSize(2);

        Card firstCard = importedDeck.getCards().getFirst();
        assertThat(firstCard.getQuestion()).isEqualTo("What is Java?");
        assertThat(firstCard.getAnswer()).isEqualTo("Java is a programming language");
    }

    @Test
    @DisplayName("Should throw IOException when importing non-existent CSV file")
    void importDeckFromCsv_ShouldThrowIOException_WhenFileDoesNotExist() {
        // Given
        Path nonExistentFile = tempDir.resolve("non-existent.csv");

        // When & Then
        assertThatThrownBy(() -> exportImportService.importDeckFromCsv(nonExistentFile, "Test", "Test"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("File not found");
    }

    @Test
    @DisplayName("Should handle empty deck export to JSON")
    void exportDeckToJson_ShouldHandleEmptyDeck() throws IOException {
        // Given
        Deck emptyDeck = Deck.builder()
                .id(1L)
                .name("Empty Deck")
                .description("A deck with no cards")
                .cards(List.of())
                .build();
        Path jsonFile = tempDir.resolve("empty-deck.json");

        // When
        exportImportService.exportDeckToJson(emptyDeck, jsonFile);

        // Then
        assertThat(jsonFile).exists();
        String jsonContent = Files.readString(jsonFile);
        assertThat(jsonContent)
                .contains("Empty Deck")
                .contains("A deck with no cards");
    }

    @Test
    @DisplayName("Should handle empty deck export to CSV")
    void exportDeckToCsv_ShouldHandleEmptyDeck() throws IOException {
        // Given
        Deck emptyDeck = Deck.builder()
                .id(1L)
                .name("Empty Deck")
                .description("A deck with no cards")
                .cards(List.of())
                .build();
        Path csvFile = tempDir.resolve("empty-deck.csv");

        // When
        exportImportService.exportDeckToCsv(emptyDeck, csvFile);

        // Then
        assertThat(csvFile).exists();
        String csvContent = Files.readString(csvFile);
        assertThat(csvContent)
                .contains("Question,Answer,Created_At,Updated_At");

        // Verify it's essentially just the header with no data rows
        String[] lines = csvContent.trim().split("\n");
        assertThat(lines).hasSize(1); // Only header line
    }

    @Test
    @DisplayName("Should skip invalid CSV rows during import")
    void importDeckFromCsv_ShouldSkipInvalidRows() throws IOException {
        // Given
        Path csvFile = tempDir.resolve("test-deck-with-invalid.csv");
        String csvContent = """
                Question,Answer,Created_At,Updated_At
                "What is Java?","Java is a programming language","2023-01-01 10:00:00","2023-01-01 10:00:00"
                "","","2023-01-02 11:00:00","2023-01-02 11:00:00"
                "What is Spring?","Spring is a framework","2023-01-02 11:00:00","2023-01-02 11:00:00"
                """;
        Files.writeString(csvFile, csvContent);

        // When
        Deck importedDeck = exportImportService.importDeckFromCsv(csvFile, "Test Deck", "Test");

        // Then
        assertThat(importedDeck.getCards()).hasSize(2); // Should skip the empty row
        assertThat(importedDeck.getCards().get(0).getQuestion()).isEqualTo("What is Java?");
        assertThat(importedDeck.getCards().get(1).getQuestion()).isEqualTo("What is Spring?");
    }
}
