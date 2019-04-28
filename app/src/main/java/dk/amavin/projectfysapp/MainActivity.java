package dk.amavin.projectfysapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import dk.amavin.projectfysapp.bodymodel.BodyActivity;
import dk.amavin.projectfysapp.domain.Answer;
import dk.amavin.projectfysapp.domain.Question;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_main, findViewById(R.id.content_frame));
    }

    protected void onModelOpenClick(View view)
    {
        Intent openModelView = new Intent(this, BodyActivity.class);
        startActivityForResult(openModelView, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0)
        {
            Bundle questionData = data.getExtras();

            Intent openQuestionIntent = new Intent(this, QuestionActivity.class);
            openQuestionIntent.putExtra("subject", (String)questionData.get("subject"));
            startActivityForResult(openQuestionIntent, 1);
        }
        else if(requestCode == 1) {
            if(resultCode == RESULT_OK) {
                Bundle questionData = data.getExtras();
                Question question = (Question) questionData.get("question");
                Map<Question, List<Answer>> answers = (Map<Question, List<Answer>>) questionData.get("result");
            }
            else
                Toast.makeText(this, "Sorry! Not currently supported", Toast.LENGTH_LONG).show();
        }

    }
}
