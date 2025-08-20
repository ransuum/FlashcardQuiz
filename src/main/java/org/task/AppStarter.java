package org.task;

import org.task.config.DatabaseManager;
import org.task.controller.ConsoleController;
import org.task.exception.DataBaseConnectionException;
import org.task.service.QuizService;
import org.task.service.ExportImportService;
import org.task.service.impl.QuizServiceImpl;
import org.task.service.impl.ExportImportServiceImpl;
import org.task.service.manager.ServiceManagement;
import org.task.service.manager.ServiceManagementImpl;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AppStarter {
    private static final Logger logger = Logger.getLogger(AppStarter.class.getName());

    private final DatabaseManager databaseManager;
    private final ConsoleController consoleController;

    public AppStarter() {
        this.databaseManager = initializeDatabase();
        final ServiceManagement serviceManagement = new ServiceManagementImpl(databaseManager);
        final QuizService quizService = new QuizServiceImpl();
        final ExportImportService exportImportService = new ExportImportServiceImpl();

        this.consoleController = new ConsoleController(quizService, exportImportService, serviceManagement);
    }

    public void start() {
        logger.info("Starting Flashcards Application...");

        try {
            addShutdownHook(databaseManager);

            consoleController.start();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Application startup failed", e);
            System.err.println("Troubleshooting program launches: " + e.getMessage());
            System.exit(1);
        }

        logger.info("Flashcards Application terminated successfully");
    }

    private static DatabaseManager initializeDatabase() {
        logger.info("Initializing database...");

        DatabaseManager databaseManager = DatabaseManager.getInstance();

        if (!databaseManager.testConnection())
            throw new DataBaseConnectionException("Cannot establish database connection");

        databaseManager.initializeDatabase();

        logger.info("Database initialized successfully");
        return databaseManager;
    }

    private static void addShutdownHook(DatabaseManager databaseManager) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down application...");
            try {
                databaseManager.shutdown();
                logger.info("Application shutdown completed");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error during shutdown", e);
            }
        }));
    }
}
