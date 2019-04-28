package dk.amavin.projectfysapp.domain;

import java.util.ArrayList;

public class Survey {
    private String Name;
    private transient ArrayList<Question> Questions;

    private Survey() {}

    public String getName() {
        return Name;
    }

    public ArrayList<Question> getQuestions() {
        return Questions;
    }
}
