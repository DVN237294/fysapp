package dk.amavin.projectfysapp.domain;

import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;


public class Answer implements Serializable {
    private String Text;
    private transient DocumentReference FollowUpQuestion;

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
}
