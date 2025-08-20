package org.task.models.dto;

import jakarta.validation.constraints.NotBlank;

public record DeckRequest(
        @NotBlank(message = "name is blank") String name,
        @NotBlank(message = "description is blank") String description
) {
}
