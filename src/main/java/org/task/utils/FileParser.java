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
import org.task.service.ExportImportService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileParser {
    private static final Logger logger = Logger.getLogger(FileParser.class.getName());

    public static CSVParser createCSVParser(Path filePath) throws IOException {
        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .get().parse(Files.newBufferedReader(filePath));
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
            if (csvRecord.isMapped(ExportImportService.CREATED_AT) && !csvRecord.get(ExportImportService.CREATED_AT).isEmpty())
                createdAt = LocalDateTime.parse(csvRecord.get(ExportImportService.CREATED_AT), ExportImportService.DATE_FORMATTER);

            if (csvRecord.isMapped(ExportImportService.UPDATED_AT) && !csvRecord.get(ExportImportService.UPDATED_AT).isEmpty())
                updatedAt = LocalDateTime.parse(csvRecord.get(ExportImportService.UPDATED_AT), ExportImportService.DATE_FORMATTER);
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

            csvPrinter.printComment("Exported: " + LocalDateTime.now().format(ExportImportService.DATE_FORMATTER));
            csvPrinter.printComment("Total Cards: " + (deck.getCards() != null ? deck.getCards().size() : 0));
            csvPrinter.println();

            csvPrinter.printRecord(Arrays.stream(ExportImportService.CSV_HEADERS));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to write header for deck: " + deck.getName(), e);
        }
    }

    public static void writeCsvCards(Deck deck, CSVPrinter csvPrinter) {
        if (deck.getCards() != null) {
            deck.getCards().stream()
                    .filter(Objects::nonNull)
                    .forEach(card -> {
                        try {
                            csvPrinter.printRecord(
                                    card.getQuestion(),
                                    card.getAnswer(),
                                    card.getCreatedAt() != null ? card.getCreatedAt().format(ExportImportService.DATE_FORMATTER) : "",
                                    card.getUpdatedAt() != null ? card.getUpdatedAt().format(ExportImportService.DATE_FORMATTER) : ""
                            );
                        } catch (IOException e) {
                            throw new FileParserException("Cannot write csv cards: " + e.getMessage());
                        }
                    });
        }
    }

    public static void validateFileCreation(Path filePath, String fileType) throws IOException {
        if (!Files.exists(filePath) || Files.size(filePath) == 0) {
            throw new IOException(fileType + " file was not created successfully: " + filePath);
        }
    }
}
