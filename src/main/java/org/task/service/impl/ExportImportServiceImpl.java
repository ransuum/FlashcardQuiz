package org.task.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.task.exception.FileParserException;
import org.task.models.entity.Card;
import org.task.models.entity.Deck;
import org.task.service.ExportImportService;
import org.task.utils.FileParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExportImportServiceImpl implements ExportImportService {

    private final ObjectMapper objectMapper;

    private static final Logger logger = Logger.getLogger(ExportImportServiceImpl.class.getName());

    public ExportImportServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void exportDeckToJson(Deck deck, Path filePath) throws IOException {
        Files.createDirectories(filePath.getParent());

        try {
            CompletableFuture<Void> writeOperation = CompletableFuture.runAsync(() -> {
                try {
                    String jsonContent = objectMapper.writeValueAsString(deck);

                    Files.write(filePath, jsonContent.getBytes(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.SYNC);

                } catch (IOException e) {
                    throw new FileParserException("Failed to write JSON file: " + e.getMessage());
                }
            });

            writeOperation.get();

            if (!Files.exists(filePath) || Files.size(filePath) == 0)
                throw new IOException("File was not created successfully: " + filePath);
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "Failed to export deck to JSON: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Export was interrupted " + e.getMessage(), e);
        }
    }

    @Override
    public Deck importDeckFromJson(Path filePath) throws IOException {
        if (!Files.exists(filePath)) throw new IOException("File not found: " + filePath);

        return objectMapper.readValue(filePath.toFile(), Deck.class);
    }

    @Override
    public void exportDeckToCsv(Deck deck, Path filePath) throws IOException {
        Files.createDirectories(filePath.getParent());

        try {
            CompletableFuture<Void> writeOperation = CompletableFuture.runAsync(() ->
                    writeCsvFile(deck, filePath)
            );

            writeOperation.get();
            FileParser.validateFileCreation(filePath, "CSV");

        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "Failed to export deck to CSV: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Export was interrupted" + e.getMessage(), e);
        }
    }

    private void writeCsvFile(Deck deck, Path filePath) {
        try (CSVPrinter csvPrinter = createCsvPrinter(filePath)) {
            FileParser.writeCsvHeader(deck, csvPrinter);
            FileParser.writeCsvCards(deck, csvPrinter);
            csvPrinter.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to write CSV file: " + e.getMessage(), e);
        }
    }

    private CSVPrinter createCsvPrinter(Path filePath) throws IOException {
        return new CSVPrinter(
                Files.newBufferedWriter(filePath, StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING),
                CSVFormat.DEFAULT.builder()
                        .setHeader(CSV_HEADERS)
                        .build());
    }

    @Override
    public Deck importDeckFromCsv(Path filePath, String deckName, String deckDescription) throws IOException {
        if (!Files.exists(filePath)) throw new IOException("File not found: " + filePath);

        List<Card> cards = new LinkedList<>();

        try (var csvParser = FileParser.createCSVParser(filePath)) {
            csvParser.forEach(csv -> {
                final var card = FileParser.parseCardFromRecord(csv);
                if (card != null) cards.add(card);
            });
        }

        return Deck.builder()
                .name(deckName)
                .description(deckDescription)
                .cards(cards)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
