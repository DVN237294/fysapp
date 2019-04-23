package dk.amavin.projectfysapp.domain;

import java.io.Serializable;

public class Answer implements Serializable {
    private String text;
    private Question nextQuestion;

    public Answer(String text, Question nextQuestion) {
        this.text = text;
        this.nextQuestion = nextQuestion;
    }

    public String getText() {
        return text;
    }

    public Question getNextQuestion() {
        return nextQuestion;
    }
}
