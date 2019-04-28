package dk.amavin.projectfysapp.domain;

import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.util.ArrayList;

public class Question implements Serializable {
    private String Text;
    private QuestionType type;
    private int MaxAnswers;
    private transient ArrayList<DocumentReference> Answers;
    private ArrayList<Answer> localAnswers;
    private transient DocumentReference FollowUpQuestion;
    private String Subject;

    private Question()
    {}

    public String getText() {
        return Text;
    }

    public QuestionType getType() {
        return type;
    }

    public int getMaxAnswers() {
        return MaxAnswers;
    }

    public DocumentReference getFollowUpQuestion() {
        return FollowUpQuestion;
    }
    public String getSubject() {
        return Subject;
    }

    public ArrayList<DocumentReference> getAnswers() {
        return Answers;
    }

    public ArrayList<Answer> getLocalAnswers() {
        return localAnswers;
    }

    public void setLocalAnswers(ArrayList<Answer> localAnswers) {
        this.localAnswers = localAnswers;
    }
}
