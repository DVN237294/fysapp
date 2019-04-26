package dk.amavin.projectfysapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.util.List;
import java.util.Map;

import dk.amavin.projectfysapp.bodymodel.BodyActivity;
import dk.amavin.projectfysapp.domain.Answer;
import dk.amavin.projectfysapp.domain.Question;
import dk.amavin.projectfysapp.domain.QuestionType;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar tb = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(tb);

        DrawerLayout drawer = findViewById(R.id.drawer_id);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, tb, R.string.open, R.string.close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    protected void onModelOpenClick(View view)
    {
        Intent openModelView = new Intent(this, BodyActivity.class);
        startActivity(openModelView);
        /*
        QuestionActivity.startQuestionIntent(this, new Question("Hello World?", QuestionType.MULTIPLE_CHOICE, 3, new Answer[]
                {
                        new Answer("Yes", null),
                        new Answer("No", null),
                        new Answer("Maybe", new Question("That's not even a valid answer to that question..!",
                                QuestionType.MULTIPLE_CHOICE, 1, new Answer[]
                                {
                                        new Answer("Yes it is, bitch!", new Question("Fuck you, biiitch!", QuestionType.MULTIPLE_CHOICE,
                                                1, new Answer[]
                                                {
                                                        new Answer("yes yes", new Question("you enjoyin' this?", QuestionType.MULTIPLE_CHOICE, 1,
                                                                new Answer[]
                                                                        {
                                                                                new Answer("Yes", null),
                                                                                new Answer("No", null)
                                                                        }, null))
                                                }, null)
                                        ),
                                        new Answer("No, you're right..", null)
                                }, null))
                }, new Question("you still enjoyin' this?", QuestionType.MULTIPLE_CHOICE, 1,
                new Answer[]
                        {
                                new Answer("y", null),
                                new Answer("n", null)
                        }, null)));*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle questionData = data.getExtras();
        Question question = (Question)questionData.get("question");
        Map<Question, List<Answer>> answers = (Map<Question, List<Answer>>)questionData.get("result");

        question = null;
        /*
        if(answers != null)
        {
            for(Answer answer : answers)
                if(answer.getNextQuestion() != null)
                    QuestionActivity.startQuestionIntent(this, answer.getNextQuestion());
        }
        if(question.getNextQuestion() != null)
            QuestionActivity.startQuestionIntent(this, question.getNextQuestion());
*/
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        return true;
    }
}
