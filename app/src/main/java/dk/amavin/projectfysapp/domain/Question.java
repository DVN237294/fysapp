package dk.amavin.projectfysapp.domain;

import java.io.Serializable;

public class Question implements Serializable {
    private String text;
    private QuestionType type;
    private int maxAnswers;
    private Answer[] answers;
    private Question nextQuestion;

    public Question(String text, QuestionType type, int maxAnswers, Answer[] answers, Question nextQuestion) {
        this.text = text;
        this.type = type;
        this.maxAnswers = maxAnswers;
        this.answers = answers;
        this.nextQuestion = nextQuestion;
    }

    public String getText() {
        return text;
    }

    public QuestionType getType() {
        return type;
    }

    public int getMaxAnswers() {
        return maxAnswers;
    }

    public Answer[] getAnswers() {
        return answers;
    }

    public Question getNextQuestion() {
        return nextQuestion;
    }
}
