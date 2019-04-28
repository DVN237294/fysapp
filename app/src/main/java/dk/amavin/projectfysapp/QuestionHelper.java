package dk.amavin.projectfysapp;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import dk.amavin.projectfysapp.domain.Answer;
import dk.amavin.projectfysapp.domain.Question;

public class QuestionHelper {
    private FirebaseFirestore db;
    private static QuestionHelper instance;
    private QuestionHelper()
    {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }
    public static QuestionHelper getInstance()
    {
        if(instance == null)
            instance = new QuestionHelper();
        return instance;
    }
    public void getQuestionsForSubject(String subject, OnQuestionQueryCompleteHandler handler)
    {
        CollectionReference ref = db.collection("Surveys");
        Query query = ref.whereEqualTo("Name", subject);
        query.get().continueWith(task ->
        {
            if (task.isSuccessful()) {
                ArrayList<String> questionRefs = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult().getDocuments())
                    for(DocumentReference docref : (ArrayList<DocumentReference>)doc.get("Questions"))
                        questionRefs.add(docref.getPath());

                handler.onQueryComplete(questionRefs);
            }
            else
                handler.onQueryComplete(null);
            return true;
        });
    }
    public void getQuestionByReference(String reference, OnQuestionQueryCompleteHandler handler)
    {
        getByReference(reference, Question.class, handler);
    }
    public void getAnswerByReference(String reference, OnQuestionQueryCompleteHandler handler)
    {
        getByReference(reference, Answer.class, handler);
    }
    public <T> void getByReference(String reference, Class<T> c, OnQuestionQueryCompleteHandler handler)
    {
        db.document(reference).get().continueWith(task ->
        {
            if (task.isSuccessful()) {
                T obj = task.getResult().toObject(c);
                handler.onQueryComplete(obj);
            }
            else
                handler.onQueryComplete(null);

            return true;
        });
    }
    public enum QuestionSubject
    {
        Knee("Knee");


        private String subject;

        QuestionSubject(String subject) {
            this.subject = subject;
        }

        public String getSubject() {
            return subject;
        }
    }
}
