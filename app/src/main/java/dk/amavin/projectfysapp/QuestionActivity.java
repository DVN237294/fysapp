package dk.amavin.projectfysapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import dk.amavin.projectfysapp.domain.Answer;
import dk.amavin.projectfysapp.domain.Question;

import static android.support.v4.app.ActivityOptionsCompat.makeSceneTransitionAnimation;

public class QuestionActivity extends AppCompatActivity implements View.OnClickListener {
    private Question question;
    private ArrayList<Answer> selectedAnswers;
    private Intent returnIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        returnIntent = getIntent();
        Bundle questionData = returnIntent.getExtras();
        boolean aok = false;
        if(questionData != null)
        {
            question = (Question)questionData.get("question");
            if(question != null) {
                selectedAnswers = new ArrayList<>(question.getMaxAnswers());
                switch (question.getType()) {
                    case MULTIPLE_CHOICE:
                        setupMultipleChoiceQuestion();
                        aok = true;
                        break;
                    default:
                        throw new RuntimeException("Not yet implemented!");
                }
            }
        }
        if(!aok)
        {
            activityFinished(RESULT_CANCELED);
        }
    }

    @Override
    public void onClick(View v) {
        Button buttonClicked = (Button)v;
        Answer selectedAnswer = (Answer)buttonClicked.getTag();
        if(selectedAnswers.contains(selectedAnswer)) {
            selectedAnswers.remove(selectedAnswer);
            buttonClicked.setBackgroundResource(android.R.drawable.btn_default);
        }
        else {
            selectedAnswers.add(selectedAnswer);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(0xFF00FF00); // Changes this drawable to use a single color instead of a gradient
            gd.setCornerRadius(5);
            gd.setStroke(1, 0xFF000000);
            buttonClicked.setBackground(gd);
        }

        if(selectedAnswers.size() >= question.getMaxAnswers())
        {
            activityFinished(RESULT_OK);
        }
    }
    protected void onContinueClick(View v)
    {
        activityFinished(RESULT_OK);
    }

    private void setupMultipleChoiceQuestion()
    {
        setContentView(R.layout.activity_question_mc);
        TextView txt = findViewById(R.id.questionText);
        TextView helper = findViewById(R.id.questionMcHelperText);
        txt.setText(question.getText());
        ConstraintLayout answerLayout = findViewById(R.id.questionAnswersLayout);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(answerLayout);

        if(question.getMaxAnswers() > 1)
            helper.setText(getString(R.string.questionHelperTextMultiple));
        else
        {
            helper.setText(getString(R.string.questionHelperTextSingle));
            findViewById(R.id.questionButtomLinearLayout).setVisibility(View.GONE);
        }

        int idAbove = helper.getId();
        for(Answer answer : question.getAnswers())
        {
            Button button = new Button(this);
            button.setId(View.generateViewId());
            button.setText(answer.getText());
            button.setOnClickListener(this);
            button.setTag(answer);
            button.setBackgroundResource(android.R.drawable.btn_default);

            answerLayout.addView(button);

            constraintSet.connect(button.getId(), ConstraintSet.TOP, idAbove, ConstraintSet.BOTTOM, 5);
            constraintSet.connect(button.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
            constraintSet.connect(button.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
            constraintSet.constrainHeight(button.getId(), 175);
            constraintSet.applyTo(answerLayout);

            idAbove = button.getId();
        }
    }

    Stack<Question> followUpQuestions;
    HashMap<Question, ArrayList<Answer>> result;
    private void activityFinished(int resultCode)
    {
        //make sure we ask any follow-ups
        followUpQuestions = new Stack<>();
        if(resultCode == RESULT_OK) {
            result = new HashMap<>();

            if (question.getNextQuestion() != null)
                followUpQuestions.push(question.getNextQuestion());
            for (Answer answer : selectedAnswers)
                if (answer.getNextQuestion() != null)
                    followUpQuestions.push(answer.getNextQuestion());

            result.put(question, selectedAnswers);
        }

        if(!handleFollowUp()) {
            returnIntent.putExtra("result", result);
            setResult(resultCode, returnIntent);
            finish();
        }
    }

    private boolean handleFollowUp()
    {
        if(followUpQuestions.isEmpty())
            return false;

        Question q = followUpQuestions.pop();
        startQuestionIntent(this, q);
        return true;
    }

    public static void startQuestionIntent(Activity context, Question q)
    {
        Intent openModelView = new Intent(context, QuestionActivity.class);
        openModelView.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        openModelView.putExtra("question", q);
        context.startActivityForResult(openModelView, 0, makeSceneTransitionAnimation(context).toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            HashMap<Question, ArrayList<Answer>> subQuestionResult =
                    (HashMap<Question, ArrayList<Answer>>) data.getExtras().get("result");

            result.putAll(subQuestionResult);
        }

        if(!handleFollowUp()) {
            returnIntent.putExtra("result", result);
            setResult(resultCode, returnIntent);
            finish();
        }
    }
}
