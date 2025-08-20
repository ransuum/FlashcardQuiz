package org.task.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CardRequest(
        @NotBlank(message = "question is blank") String question,
        @NotBlank(message = "answer is blank") String answer,
        @NotNull Long deckId
) {
}
