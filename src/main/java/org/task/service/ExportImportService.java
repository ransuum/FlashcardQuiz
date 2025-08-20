package org.task.service;

import org.task.models.entity.Deck;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public interface ExportImportService {

    String CREATED_AT = "Created_At";
    String UPDATED_AT = "Updated_At";
    String[] CSV_HEADERS = {"Question", "Answer", CREATED_AT, UPDATED_AT};
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    void exportDeckToJson(Deck deck, Path filePath) throws IOException;

    Deck importDeckFromJson(Path filePath) throws IOException;

    void exportDeckToCsv(Deck deck, Path filePath) throws IOException;

    Deck importDeckFromCsv(Path filePath, String deckName, String deckDescription) throws IOException;
}
