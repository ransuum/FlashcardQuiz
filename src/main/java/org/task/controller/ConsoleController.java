package org.task.controller;

import org.task.models.dto.CardRequest;
import org.task.models.dto.DeckRequest;
import org.task.models.entity.Card;
import org.task.models.entity.Deck;
import org.task.service.CardService;
import org.task.service.DeckService;
import org.task.service.QuizService;
import org.task.service.ExportImportService;
import org.task.service.manager.ServiceManagement;

import java.util.List;
import java.util.Scanner;
import java.nio.file.Path;

public class ConsoleController {
    private final Scanner scanner;
    private final QuizService quizService;
    private final ExportImportService exportImportService;
    private final DeckService deckService;
    private final CardService cardService;

    private static final String PATH_NAME = "exports";

    public ConsoleController(QuizService quizService,
                             ExportImportService exportImportService,
                             ServiceManagement serviceManagement) {
        this.deckService = serviceManagement.getDeckService();
        this.cardService = serviceManagement.getCardService();
        this.scanner = new Scanner(System.in);
        this.quizService = quizService;
        this.exportImportService = exportImportService;
    }

    public void start() {
        System.out.println("=== Welcome to Flashcards App! ===\n");

        loadDataOnStartup();

        boolean running = true;
        while (running) {
            showMainMenu();
            int choice = getIntInput();

            switch (choice) {
                case 1:
                    startLearningMode();
                    break;
                case 2:
                    manageDecks();
                    break;
                case 3:
                    showExportImportMenu();
                    break;
                case 4:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice! Try again.\n");
            }
        }

        saveDataOnExit();
        System.out.println("Goodbye!");
    }

    private void showMainMenu() {
        System.out.println("=== Main Menu ===");
        System.out.println("1. Start Learning");
        System.out.println("2. Manage Decks");
        System.out.println("3. Import/Export");
        System.out.println("4. Exit");
        System.out.print("Your choice: ");
    }

    private void startLearningMode() {
        List<Deck> decks = deckService.getAllDecks();

        if (decks.isEmpty()) {
            System.out.println("No available decks. Create a deck first.\n");
            return;
        }

        System.out.println("\n=== Select Deck for Learning ===");
        showDecksList(decks);
        System.out.print("Enter deck number: ");

        int choice = getIntInput();
        if (choice < 1 || choice > decks.size()) {
            System.out.println("Invalid choice!\n");
            return;
        }

        final var selectedDeck = decks.get(choice - 1);
        List<Card> cards = cardService.getCardsByDeckId(selectedDeck.getId());
        selectedDeck.setCards(cards);

        quizService.startQuiz(selectedDeck);
    }

    private void manageDecks() {
        boolean managing = true;
        while (managing) {
            showDeckManagementMenu();
            int choice = getIntInput();

            switch (choice) {
                case 1:
                    createNewDeck();
                    break;
                case 2:
                    viewAllDecks();
                    break;
                case 3:
                    manageCards();
                    break;
                case 4:
                    deleteDeck();
                    break;
                case 5:
                    managing = false;
                    break;
                default:
                    System.out.println("Invalid choice!\n");
            }
        }
    }

    private void showDeckManagementMenu() {
        System.out.println("\n=== Deck Management ===");
        System.out.println("1. Create New Deck");
        System.out.println("2. View All Decks");
        System.out.println("3. Manage Cards");
        System.out.println("4. Delete Deck");
        System.out.println("5. Back to Main Menu");
        System.out.print("Your choice: ");
    }

    private int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private void loadDataOnStartup() {
        try {
            System.out.println("Loading data...");
            final long deckCount = deckService.getDeckCount();
            final long cardCount = cardService.getTotalCardCount();

            System.out.printf("Loaded: %d decks, %d cards\n\n", deckCount, cardCount);
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    private void saveDataOnExit() {
        try {
            System.out.println("Data saved successfully!");
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private void showExportImportMenu() {
        boolean managing = true;
        while (managing) {
            System.out.println("\n=== Import/Export ===");
            System.out.println("1. Export Deck to JSON");
            System.out.println("2. Import Deck from JSON");
            System.out.println("3. Export Deck to CSV");
            System.out.println("4. Import Deck from CSV");
            System.out.println("5. Back to Main Menu");
            System.out.print("Your choice: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    exportDeckToJson();
                    break;
                case 2:
                    importDeckFromJson();
                    break;
                case 3:
                    exportDeckToCsv();
                    break;
                case 4:
                    importDeckFromCsv();
                    break;
                case 5:
                    managing = false;
                    break;
                default:
                    System.out.println("Invalid choice!\n");
            }
        }
    }

    private void showDecksList(List<Deck> decks) {
        if (decks.isEmpty()) {
            System.out.println("No available decks.");
            return;
        }

        System.out.println("Available decks:");
        for (int i = 0; i < decks.size(); i++) {
            Deck deck = decks.get(i);
            System.out.printf("%d. %s (%d cards) - %s\n",
                    i + 1,
                    deck.getName(),
                    deck.getCardCount(),
                    deck.getDescription() != null ? deck.getDescription() : "No description"
            );
        }
        System.out.println();
    }

    private void createNewDeck() {
        System.out.println("\n=== Creating New Deck ===");

        String name = getStringInput("Enter deck name: ");
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty!\n");
            return;
        }

        final String description = getStringInput("Enter deck description (optional): ");

        try {
            Deck newDeck = deckService.createDeck(new DeckRequest(name, description));
            System.out.printf("Deck '%s' successfully created with ID: %d\n\n",
                    newDeck.getName(), newDeck.getId());
        } catch (Exception e) {
            System.err.println("Error creating deck: " + e.getMessage() + "\n");
        }
    }

    private void viewAllDecks() {
        System.out.println("\n=== All Decks ===");

        try {
            List<Deck> decks = deckService.getAllDecks();

            if (decks.isEmpty()) {
                System.out.println("No decks created.\n");
                return;
            }

            showDecksList(decks);

            System.out.println("Press Enter to continue...");
            scanner.nextLine();
        } catch (Exception e) {
            System.err.println("Error getting decks: " + e.getMessage() + "\n");
        }
    }

    private void manageCards() {
        List<Deck> decks = deckService.getAllDecks();

        if (decks.isEmpty()) {
            System.out.println("No available decks. Create a deck first.\n");
            return;
        }

        System.out.println("\n=== Select Deck for Card Management ===");
        showDecksList(decks);
        System.out.print("Enter deck number: ");

        final int choice = getIntInput();
        if (choice < 1 || choice > decks.size()) {
            System.out.println("Invalid choice!\n");
            return;
        }

        final Deck selectedDeck = decks.get(choice - 1);
        manageCardsInDeck(selectedDeck);
    }

    private void manageCardsInDeck(Deck deck) {
        boolean managing = true;
        while (managing) {
            System.out.printf("\n=== Managing Cards in Deck '%s' ===\n", deck.getName());
            System.out.println("1. Add Card");
            System.out.println("2. View All Cards");
            System.out.println("3. Edit Card");
            System.out.println("4. Delete Card");
            System.out.println("5. Search Cards");
            System.out.println("6. Back to Deck Management");
            System.out.print("Your choice: ");

            final int choice = getIntInput();

            switch (choice) {
                case 1:
                    addCardToDeck(deck);
                    break;
                case 2:
                    viewCardsInDeck(deck);
                    break;
                case 3:
                    editCardInDeck(deck);
                    break;
                case 4:
                    deleteCardFromDeck(deck);
                    break;
                case 5:
                    searchCardsInDeck(deck);
                    break;
                case 6:
                    managing = false;
                    break;
                default:
                    System.out.println("Invalid choice!\n");
            }
        }
    }

    private void addCardToDeck(Deck deck) {
        System.out.printf("\n=== Adding Card to Deck '%s' ===\n", deck.getName());

        String question = getStringInput("Enter question: ");
        if (question.isEmpty()) {
            System.out.println("Question cannot be empty!\n");
            return;
        }

        String answer = getStringInput("Enter answer: ");
        if (answer.isEmpty()) {
            System.out.println("Answer cannot be empty!\n");
            return;
        }

        try {
            Card newCard = cardService.create(new CardRequest(question, answer, deck.getId()));
            System.out.printf("Card successfully added with ID: %d\n\n", newCard.getId());
        } catch (Exception e) {
            System.err.println("Error adding card: " + e.getMessage() + "\n");
        }
    }

    private void viewCardsInDeck(Deck deck) {
        System.out.printf("\n=== Cards in Deck '%s' ===\n", deck.getName());

        try {
            List<Card> cards = cardService.getCardsByDeckId(deck.getId());

            if (cards.isEmpty()) {
                System.out.println("No cards in the deck.\n");
                return;
            }

            for (int i = 0; i < cards.size(); i++) {
                Card card = cards.get(i);
                System.out.printf("%d. Question: %s\n", i + 1, card.getQuestion());
                System.out.printf("   Answer: %s\n", card.getAnswer());
                System.out.printf("   ID: %d, Created: %s\n\n",
                        card.getId(),
                        card.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                );
            }

            System.out.println("Press Enter to continue...");
            scanner.nextLine();

        } catch (Exception e) {
            System.err.println("Error getting cards: " + e.getMessage() + "\n");
        }
    }

    private void editCardInDeck(Deck deck) {
        try {
            List<Card> cards = cardService.getCardsByDeckId(deck.getId());

            if (cards.isEmpty()) {
                System.out.println("No cards in the deck to edit.\n");
                return;
            }

            System.out.printf("\n=== Editing Card in Deck '%s' ===\n", deck.getName());
            showCardsList(cards);
            System.out.print("Enter card number to edit: ");

            final int choice = getIntInput();
            if (choice < 1 || choice > cards.size()) {
                System.out.println("Invalid choice!\n");
                return;
            }

            final Card cardToEdit = cards.get(choice - 1);
            System.out.printf("Current question: %s\n", cardToEdit.getQuestion());
            System.out.printf("Current answer: %s\n\n", cardToEdit.getAnswer());

            String newQuestion = getStringInput("Enter new question (or Enter to keep current): ");
            String newAnswer = getStringInput("Enter new answer (or Enter to keep current): ");

            String finalQuestion = newQuestion.isEmpty() ? cardToEdit.getQuestion() : newQuestion;
            String finalAnswer = newAnswer.isEmpty() ? cardToEdit.getAnswer() : newAnswer;

            cardService.update(cardToEdit.getId(), new CardRequest(finalQuestion, finalAnswer, deck.getId()));
            System.out.printf("Card successfully updated!\n\n");

        } catch (Exception e) {
            System.err.println("Error editing card: " + e.getMessage() + "\n");
        }
    }

    private void deleteCardFromDeck(Deck deck) {
        try {
            List<Card> cards = cardService.getCardsByDeckId(deck.getId());

            if (cards.isEmpty()) {
                System.out.println("No cards in the deck to delete.\n");
                return;
            }

            System.out.printf("\n=== Deleting Card from Deck '%s' ===\n", deck.getName());
            showCardsList(cards);
            System.out.print("Enter card number to delete: ");

            final int choice = getIntInput();
            if (choice < 1 || choice > cards.size()) {
                System.out.println("Invalid choice!\n");
                return;
            }

            final Card cardToDelete = cards.get(choice - 1);
            System.out.printf("Are you sure you want to delete card:\n");
            System.out.printf("Question: %s\n", cardToDelete.getQuestion());
            System.out.printf("Answer: %s\n", cardToDelete.getAnswer());
            System.out.print("Enter 'yes' to confirm: ");

            String confirmation = scanner.nextLine().trim().toLowerCase();
            if ("yes".equals(confirmation)) {
                cardService.delete(cardToDelete.getId());
                System.out.println("Card successfully deleted!\n");
            } else {
                System.out.println("Deletion cancelled.\n");
            }

        } catch (Exception e) {
            System.err.println("Error deleting card: " + e.getMessage() + "\n");
        }
    }

    private void searchCardsInDeck(Deck deck) {
        System.out.printf("\n=== Search Cards in Deck '%s' ===\n", deck.getName());

        String searchTerm = getStringInput("Enter search term: ");
        if (searchTerm.isEmpty()) {
            System.out.println("Search term cannot be empty!\n");
            return;
        }

        try {
            List<Card> allCards = cardService.getCardsByDeckId(deck.getId());
            List<Card> matchingCards = allCards.stream()
                    .filter(card -> card.getQuestion().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                   card.getAnswer().toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();

            if (matchingCards.isEmpty()) {
                System.out.printf("No cards found containing '%s'.\n\n", searchTerm);
                return;
            }

            System.out.printf("Found %d cards:\n\n", matchingCards.size());
            for (int i = 0; i < matchingCards.size(); i++) {
                Card card = matchingCards.get(i);
                System.out.printf("%d. Question: %s\n", i + 1, card.getQuestion());
                System.out.printf("   Answer: %s\n", card.getAnswer());
                System.out.printf("   ID: %d\n\n", card.getId());
            }

            System.out.println("Press Enter to continue...");
            scanner.nextLine();

        } catch (Exception e) {
            System.err.println("Error searching cards: " + e.getMessage() + "\n");
        }
    }

    private void showCardsList(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            System.out.printf("%d. %s -> %s\n", i + 1, card.getQuestion(), card.getAnswer());
        }
        System.out.println();
    }

    private void deleteDeck() {
        List<Deck> decks = deckService.getAllDecks();

        if (decks.isEmpty()) {
            System.out.println("No decks to delete.\n");
            return;
        }

        System.out.println("\n=== Delete Deck ===");
        showDecksList(decks);
        System.out.print("Enter deck number to delete: ");

        int choice = getIntInput();
        if (choice < 1 || choice > decks.size()) {
            System.out.println("Invalid choice!\n");
            return;
        }

        final Deck deckToDelete = decks.get(choice - 1);
        System.out.printf("Are you sure you want to delete deck '%s'?\n", deckToDelete.getName());
        System.out.print("This will also delete all cards in the deck. Enter 'yes' to confirm: ");

        final String confirmation = scanner.nextLine().trim().toLowerCase();
        if ("yes".equals(confirmation)) {
            try {
                deckService.deleteDeck(deckToDelete.getId());
                System.out.printf("Deck '%s' successfully deleted!\n\n", deckToDelete.getName());
            } catch (Exception e) {
                System.err.println("Error deleting deck: " + e.getMessage() + "\n");
            }
        } else {
            System.out.println("Deletion cancelled.\n");
        }
    }

    private void exportDeckToJson() {
        List<Deck> decks = deckService.getAllDecks();

        if (decks.isEmpty()) {
            System.out.println("No decks available for export.\n");
            return;
        }

        System.out.println("\n=== Export Deck to JSON ===");
        showDecksList(decks);
        System.out.print("Enter deck number to export: ");

        final int choice = getIntInput();
        if (choice < 1 || choice > decks.size()) {
            System.out.println("Invalid choice!\n");
            return;
        }

        final var selectedDeck = decks.get(choice - 1);

        List<Card> cards = cardService.getCardsByDeckId(selectedDeck.getId());
        selectedDeck.setCards(cards);

        String fileName = getStringInput("Enter file name (without extension): ");
        if (fileName.isEmpty()) {
            System.out.println("File name cannot be empty!\n");
            return;
        }

        try {
            Path filePath = Path.of(PATH_NAME, fileName + ".json");
            exportImportService.exportDeckToJson(selectedDeck, filePath);
            System.out.printf("Deck '%s' successfully exported to %s\n\n",
                    selectedDeck.getName(), filePath);
        } catch (Exception e) {
            System.err.println("Export error: " + e.getMessage() + "\n");
        }
    }

    private void importDeckFromJson() {
        System.out.println("\n=== Import Deck from JSON ===");
        String fileName = getStringInput("Enter file name (with extension): ");

        if (fileName.isEmpty()) {
            System.out.println("File name cannot be empty!\n");
            return;
        }

        try {
            final Path filePath = Path.of(PATH_NAME, fileName);
            final var importedDeck = exportImportService.importDeckFromJson(filePath);

            final var createdDeck = deckService.createDeck(new DeckRequest(
                    importedDeck.getName(),
                    importedDeck.getDescription()
            ));

            int cardsCreated = 0;
            if (importedDeck.getCards() != null) {
                for (Card card : importedDeck.getCards()) {
                    cardService.create(new CardRequest(
                            card.getQuestion(),
                            card.getAnswer(),
                            createdDeck.getId()
                    ));
                    cardsCreated++;
                }
            }

            System.out.printf("Deck '%s' successfully imported with %d cards from %s\n\n",
                    createdDeck.getName(), cardsCreated, filePath);

        } catch (Exception e) {
            System.err.println("Import error: " + e.getMessage() + "\n");
        }
    }

    private void exportDeckToCsv() {
        List<Deck> decks = deckService.getAllDecks();

        if (decks.isEmpty()) {
            System.out.println("No decks available for export.\n");
            return;
        }

        System.out.println("\n=== Export Deck to CSV ===");
        showDecksList(decks);
        System.out.print("Enter deck number to export: ");

        int choice = getIntInput();
        if (choice < 1 || choice > decks.size()) {
            System.out.println("Invalid choice!\n");
            return;
        }

        Deck selectedDeck = decks.get(choice - 1);

        // Load cards for the deck
        List<Card> cards = cardService.getCardsByDeckId(selectedDeck.getId());
        selectedDeck.setCards(cards);

        String fileName = getStringInput("Enter file name (without extension): ");
        if (fileName.isEmpty()) {
            System.out.println("File name cannot be empty!\n");
            return;
        }

        try {
            Path filePath = Path.of("exports", fileName + ".csv");
            exportImportService.exportDeckToCsv(selectedDeck, filePath);
            System.out.printf("Deck '%s' successfully exported to %s\n\n",
                    selectedDeck.getName(), filePath);
        } catch (Exception e) {
            System.err.println("Export error: " + e.getMessage() + "\n");
        }
    }

    private void importDeckFromCsv() {
        System.out.println("\n=== Import Deck from CSV ===");
        String fileName = getStringInput("Enter file name (with extension): ");

        if (fileName.isEmpty()) {
            System.out.println("File name cannot be empty!\n");
            return;
        }

        String deckName = getStringInput("Enter name for the new deck: ");
        if (deckName.isEmpty()) {
            System.out.println("Deck name cannot be empty!\n");
            return;
        }

        String deckDescription = getStringInput("Enter description for the new deck (optional): ");

        try {
            Path filePath = Path.of("exports", fileName);
            Deck importedDeck = exportImportService.importDeckFromCsv(filePath, deckName, deckDescription);

            final var createdDeck = deckService.createDeck(new DeckRequest(
                    importedDeck.getName(),
                    importedDeck.getDescription()
            ));

            int cardsCreated = 0;
            if (importedDeck.getCards() != null) {
                for (Card card : importedDeck.getCards()) {
                    cardService.create(new CardRequest(
                            card.getQuestion(),
                            card.getAnswer(),
                            createdDeck.getId()
                    ));
                    cardsCreated++;
                }
            }

            System.out.printf("Deck '%s' successfully imported with %d cards from %s\n\n",
                    createdDeck.getName(), cardsCreated, filePath);

        } catch (Exception e) {
            System.err.println("Import error: " + e.getMessage() + "\n");
        }
    }
}
