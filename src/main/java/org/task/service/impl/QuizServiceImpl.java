package org.task.service.impl;

import org.task.models.entity.Card;
import org.task.models.entity.Deck;
import org.task.service.QuizService;

import java.util.Collections;
import java.util.Scanner;

import static org.task.service.QuizService.isCorrectAnswer;

public class QuizServiceImpl implements QuizService {
    private final Scanner scanner;

    public QuizServiceImpl() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void startQuiz(Deck deck) {
        if (deck.getCards().isEmpty()) {
            System.out.println("The deck doesn't have enough cards for winning!");
            return;
        }

        final var cards = deck.getCards();
        Collections.shuffle(cards);

        int correct = 0;
        final int total = cards.size();

        System.out.println("\n=== Start of quiz: " + deck.getName() + " ===");
        System.out.println("Number of cards: " + total);
        System.out.println("Enter 'quit' to exit the quiz\n");

        for (int i = 0; i < cards.size(); i++) {
            final Card card = cards.get(i);

            System.out.printf("Question %d/%d: %s\n", i + 1, total, card.getQuestion());
            System.out.print("Your answer: ");

            final String userAnswer = scanner.nextLine().trim();

            if ("quit".equalsIgnoreCase(userAnswer)) {
                System.out.println("The quiz was interrupted by the user.");
                return;
            }

            if (isCorrectAnswer(userAnswer, card.getAnswer())) {
                System.out.println("✓ Right!\n");
                correct++;
            } else {
                System.out.println("✗ Wrong!");
                System.out.println("The correct answer is: " + card.getAnswer() + "\n");
            }
        }

        showQuizResults(correct, total);
    }

    private void showQuizResults(int correct, int total) {
        System.out.println("=== Quiz results ===");
        System.out.printf("Correct answers: %d/%d\n", correct, total);

        final double percentage = (double) correct / total * 100;
        System.out.printf("Percent of correct answers: %.1f%%\n", percentage);

        if (percentage >= 90) {
            System.out.println("Excellent! 🌟");
        } else if (percentage >= 70) {
            System.out.println("Good! 👍");
        } else if (percentage >= 50) {
            System.out.println("Satisfied. Need more practice! 📚");
        } else {
            System.out.println("It is necessary to read the material better! 💪");
        }

        System.out.println();
    }
}
