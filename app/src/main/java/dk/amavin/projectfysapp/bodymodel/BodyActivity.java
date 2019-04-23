package dk.amavin.projectfysapp.bodymodel;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import dk.amavin.projectfysapp.R;

public class BodyActivity extends AppCompatActivity {

    private GLSurfaceView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bodymodel);

        view = new MainGLView(this);
        ConstraintLayout v = findViewById(R.id.main_view);
        v.addView(view);

        Toolbar toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        toolbar.setTitle("Select area of pain");
        setSupportActionBar(toolbar);
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        view.onResume();
    }
    @Override
    protected void onPause()
    {
        super.onPause();
        view.onPause();
    }

}
