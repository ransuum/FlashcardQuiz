package org.task.service.manager;

import org.task.service.CardService;
import org.task.service.DeckService;

public sealed interface ServiceManagement permits ServiceManagementImpl {
    CardService getCardService();

    DeckService getDeckService();
}
