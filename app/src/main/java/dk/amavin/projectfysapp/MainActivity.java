package dk.amavin.projectfysapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
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
        setProgressbarVisible(true);
        Intent openModelView = new Intent(this, BodyActivity.class);
        startActivityForResult(openModelView, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            if (requestCode == 0) {
                Bundle questionData = data.getExtras();

                Intent openQuestionIntent = new Intent(this, QuestionActivity.class);
                openQuestionIntent.putExtra("subject", (String) questionData.get("subject"));
                startActivityForResult(openQuestionIntent, 1);
            } else if (requestCode == 1) {
                if (resultCode == RESULT_OK) {
                    Bundle questionData = data.getExtras();
                    HashMap<Question, List<Answer>> answers = (HashMap<Question, List<Answer>>) questionData.get("result");

                    Intent diagViewIntent = new Intent(this, DiagnosisActivity.class);
                    diagViewIntent.putExtra("data", computeDiagnosis(answers));
                    startActivity(diagViewIntent);
                } else
                    Toast.makeText(this, "Sorry! Not currently supported", Toast.LENGTH_LONG).show();
            }
        }
    }

    private HashMap<String, Float> computeDiagnosis(Map<Question, List<Answer>> answers)
    {
        HashMap<String, Float> diagnosisMap = new HashMap<>();
        for (Map.Entry<Question, List<Answer>> entry : answers.entrySet())
            for(Answer ansv : entry.getValue())
                if(ansv.getLinkedDiagnoses() != null)
                    for(String ref : ansv.getLinkedDiagnoses())
                    {
                        Float count = diagnosisMap.get(ref);
                        if(count == null)
                            diagnosisMap.put(ref, 1.0f);
                        else
                            diagnosisMap.put(ref, count + 1.0f);
                    }

        float sum = 0;
        for (Map.Entry<String, Float> entry : diagnosisMap.entrySet())
            sum += entry.getValue();

        for (Map.Entry<String, Float> entry : diagnosisMap.entrySet()) {
            diagnosisMap.put(entry.getKey(), entry.getValue() * 100 / sum);
        }
        return diagnosisMap;
    }

    @Override
    protected void onStop() {
        super.onStop();
        setProgressbarVisible(false);
    }



    private void setProgressbarVisible(boolean visible)
    {
        ProgressBar bar = findViewById(R.id.main_progressbar);
        TextView txt = findViewById(R.id.main_progressbar_text);
        if(visible)
        {
            bar.setVisibility(View.VISIBLE);
            txt.setVisibility(View.VISIBLE);
        }
        else
        {
            bar.setVisibility(View.GONE);
            txt.setVisibility(View.GONE);
        }
    }
}
