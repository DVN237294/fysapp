package dk.amavin.projectfysapp.domain;

import com.google.firebase.firestore.DocumentReference;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;


public class Answer implements Serializable {
    private String Text;
    private transient DocumentReference FollowUpQuestion;
    private transient ArrayList<DocumentReference> LinkedDiagnoses;
    private ArrayList<String> linkedDiagnoses;

    private Answer() {}

    public String getText() {
        return Text;
    }

    public DocumentReference getFollowUpQuestion() {
        return FollowUpQuestion;
    }

    public ArrayList<String> getLinkedDiagnoses() {
        return linkedDiagnoses;
    }


    //I have to do this because DocumentReference is not serializable..
    private void setlinkedDiagnoses(ArrayList<DocumentReference> diagnoses) {
        if(diagnoses != null) {
            linkedDiagnoses = new ArrayList<>();
            for (DocumentReference ref : diagnoses)
                linkedDiagnoses.add(ref.getPath());
        }
    }
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException
    {
        setlinkedDiagnoses(LinkedDiagnoses);
        out.defaultWriteObject();
    }
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {

        in.defaultReadObject();
        setlinkedDiagnoses(LinkedDiagnoses);
    }
}
