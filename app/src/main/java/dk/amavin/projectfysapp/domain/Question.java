package dk.amavin.projectfysapp.domain;

import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.util.ArrayList;

public class Question implements Serializable {
    private String Text;
    private QuestionType type;
    private int MaxAnswers;
    private ArrayList<Answer> Answers;
    private transient DocumentReference FollowUpQuestion;
    private String Subject;

    private Question()
    {}

    public Question(String text, QuestionType type, int maxAnswers, ArrayList<Answer> answers) {
        this.Text = text;
        this.type = type;
        this.MaxAnswers = maxAnswers;
        this.Answers = answers;
    }

    public String getText() {
        return Text;
    }

    public QuestionType getType() {
        return type;
    }

    public int getMaxAnswers() {
        return MaxAnswers;
    }

    public ArrayList<Answer> getAnswers() {
        return Answers;
    }

    public DocumentReference getFollowUpQuestion() {
        return FollowUpQuestion;
    }
    public String getSubject() {
        return Subject;
    }
}
