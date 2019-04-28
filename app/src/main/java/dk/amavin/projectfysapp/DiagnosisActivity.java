package dk.amavin.projectfysapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dk.amavin.projectfysapp.domain.Diagnosis;


public class DiagnosisActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        if(data != null)
        {
            HashMap<String, Float> map = (HashMap<String, Float>)data.get("data");
            if(map.size() > 0) {
                getLayoutInflater().inflate(R.layout.activity_diagnosis, findViewById(R.id.content_frame));
                List<Map.Entry<String, Float>> list = new ArrayList<>(map.entrySet());
                Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
                for (Map.Entry<String, Float> d : list) {
                    DatabaseHelper.getInstance().getByReference(d.getKey(), Diagnosis.class, res ->
                    {
                        Diagnosis diag = (Diagnosis) res;
                        addProgressBar(String.format(Locale.US, "%s - %2.2f%%", diag.getText(), d.getValue()), Math.round(d.getValue()));
                    });
                }
            }
            else
                finish();
        }

    }

    private void addProgressBar(String text, int progress)
    {
        LinearLayout layout = findViewById(R.id.progress_container);
        View pbar = LayoutInflater.from(this).inflate(R.layout.progressbar, layout);

        ProgressBar progressBar = pbar.findViewById(R.id.PROGRESS_BAR);
        progressBar.setId(View.generateViewId());
        progressBar.setProgress(progress);
        progressBar.setScaleY(10.0f);

        TextView textView = pbar.findViewById(R.id.progressBar_text);
        textView.setId(View.generateViewId());
        textView.setText(text);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)textView.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_START, progressBar.getId());
        params.addRule(RelativeLayout.ALIGN_TOP, progressBar.getId());
        params.addRule(RelativeLayout.ALIGN_END, progressBar.getId());
        params.addRule(RelativeLayout.ALIGN_BOTTOM, progressBar.getId());
        textView.setLayoutParams(params);
    }
}
