package org.task.service.manager;

import org.task.config.DatabaseManager;
import org.task.repository.CardRepository;
import org.task.repository.DeckRepository;
import org.task.service.CardService;
import org.task.service.DeckService;
import org.task.service.impl.CardServiceImpl;
import org.task.service.impl.DeckServiceImpl;

public final class ServiceManagementImpl implements ServiceManagement {
    private final DatabaseManager databaseManager;
    private final CardRepository cardRepository;

    public ServiceManagementImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.cardRepository = new CardRepository(databaseManager);
    }

    @Override
    public CardService getCardService() {
        return new CardServiceImpl(cardRepository);
    }

    @Override
    public DeckService getDeckService() {
        return new DeckServiceImpl(new DeckRepository(databaseManager, cardRepository));
    }
}
