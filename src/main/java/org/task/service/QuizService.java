package org.task.service;

import org.task.models.entity.Deck;

public interface QuizService {
    void startQuiz(Deck deck);

    static boolean isCorrectAnswer(String userAnswer, String correctAnswer) {
        return userAnswer.equalsIgnoreCase(correctAnswer.trim());
    }
}
