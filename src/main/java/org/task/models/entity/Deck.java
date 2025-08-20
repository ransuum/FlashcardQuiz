package org.task.models.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deck {
    private Long id;
    private String name;
    private String description;

    @Builder.Default
    private List<Card> cards = new ArrayList<>();

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Deck(String name, String description) {
        this.name = name;
        this.description = description;
        this.cards = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCards(List<Card> cards) {
        this.cards = cards != null ? new ArrayList<>(cards) : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean removeCard(Card card) {
        if (this.cards == null) {
            return false;
        }
        boolean removed = this.cards.remove(card);
        if (removed) {
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }

    public int getCardCount() {
        return this.cards != null ? this.cards.size() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Deck deck)) return false;
        return Objects.equals(getId(), deck.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return String.format("Deck{id=%d, name='%s', cardCount=%d}",
                id, name, getCardCount());
    }
}