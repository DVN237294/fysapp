package dk.amavin.projectfysapp.domain;

import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.util.ArrayList;


public class Answer implements Serializable {
    private String Text;
    private transient DocumentReference FollowUpQuestion;
    private transient ArrayList<DocumentReference> LinkedDiagnoses;

    private Answer() {}

    public Answer(String text) {
        this.Text = text;
    }

    public String getText() {
        return Text;
    }

    public DocumentReference getFollowUpQuestion() {
        return FollowUpQuestion;
    }

    public ArrayList<DocumentReference> getLinkedDiagnoses() {
        return LinkedDiagnoses;
    }
}
