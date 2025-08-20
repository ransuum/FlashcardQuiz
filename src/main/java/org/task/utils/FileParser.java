package org.task.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.task.exception.FileParserException;
import org.task.models.entity.Card;
import org.task.models.entity.Deck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.task.service.ExportImportService.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileParser {
    private static final Logger logger = Logger.getLogger(FileParser.class.getName());

    public static CSVParser createCSVParser(Path filePath) throws IOException {
        return new CSVParser(Files.newBufferedReader(filePath),
                CSVFormat.DEFAULT.builder()
                        .setIgnoreHeaderCase(true)
                        .setTrim(true)
                        .build());
    }

    public static Card parseCardFromRecord(CSVRecord csvRecord) {
        final var question = csvRecord.get("Question");
        final var answer = csvRecord.get("Answer");

        if (StringUtils.isBlank(question) && StringUtils.isBlank(answer)) return null;

        final var timestamps = parseTimestamps(csvRecord);

        return Card.builder()
                .question(question.trim())
                .answer(answer.trim())
                .createdAt(timestamps[0])
                .updatedAt(timestamps[1])
                .build();
    }

    public static LocalDateTime[] parseTimestamps(CSVRecord csvRecord) {
        var createdAt = LocalDateTime.now();
        var updatedAt = LocalDateTime.now();

        try {
            if (csvRecord.isMapped(CREATED_AT) && !csvRecord.get(CREATED_AT).isEmpty())
                createdAt = LocalDateTime.parse(csvRecord.get(CREATED_AT), DATE_FORMATTER);

            if (csvRecord.isMapped(UPDATED_AT) && !csvRecord.get(UPDATED_AT).isEmpty())
                updatedAt = LocalDateTime.parse(csvRecord.get(UPDATED_AT), DATE_FORMATTER);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to parse timestamps for csvRecord: " + csvRecord);
        }

        return new LocalDateTime[]{createdAt, updatedAt};
    }

    public static void writeCsvHeader(Deck deck, CSVPrinter csvPrinter) {
        try {
            csvPrinter.printComment("Deck: " + deck.getName());
            if (StringUtils.isNotBlank(deck.getDescription()))
                csvPrinter.printComment("Description: " + deck.getDescription());

            csvPrinter.printComment("Exported: " + LocalDateTime.now().format(DATE_FORMATTER));
            csvPrinter.printComment("Total Cards: " + (deck.getCards() != null ? deck.getCards().size() : 0));
            csvPrinter.println();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to write header for deck: " + deck.getName(), e);
        }
    }

    public static void writeCsvCards(Deck deck, CSVPrinter csvPrinter) {
        deck.getCards().stream()
                .filter(Objects::nonNull)
                .forEach(card -> {
                    try {
                        csvPrinter.printRecord(
                                card.getQuestion(),
                                card.getAnswer(),
                                card.getCreatedAt() != null ? card.getCreatedAt().format(DATE_FORMATTER) : "",
                                card.getUpdatedAt() != null ? card.getUpdatedAt().format(DATE_FORMATTER) : ""
                        );
                    } catch (IOException e) {
                        throw new FileParserException("Cannot write csv cards: " + e.getMessage());
                    }
                });
    }

    public static void validateFileCreation(Path filePath, String fileType) throws IOException {
        if (!Files.exists(filePath) || Files.size(filePath) == 0) {
            throw new IOException(fileType + " file was not created successfully: " + filePath);
        }
    }
}
