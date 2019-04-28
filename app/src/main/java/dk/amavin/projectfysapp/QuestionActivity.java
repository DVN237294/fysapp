package dk.amavin.projectfysapp;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

import dk.amavin.projectfysapp.domain.Answer;
import dk.amavin.projectfysapp.domain.Question;

public class QuestionActivity extends BaseActivity implements View.OnClickListener {
    private Question question;
    private ArrayList<Answer> selectedAnswers;
    private Intent returnIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        returnIntent = getIntent();
        Bundle questionData = returnIntent.getExtras();
        if(questionData != null)
        {
            followUpQuestions = new Stack<>();
            Runnable runActivity = () -> getAndDisplayQuestion(followUpQuestions.pop());
            if(questionData.containsKey("subject"))
            {
                QuestionHelper.getInstance().getQuestionsForSubject((String)questionData.get("subject"), result ->
                {
                    if(result.length > 0)
                    {
                        String[] stringArray = Arrays.copyOf(result, result.length, String[].class);
                        Collections.addAll(followUpQuestions, stringArray);
                        runOnUiThread(runActivity);
                    }
                    else
                        questionFinished(RESULT_CANCELED);
                });
            }
        }

        getLayoutInflater().inflate(R.layout.activity_question_mc, findViewById(R.id.content_frame));

    }

    private void getAndDisplayQuestion(String ref)
    {
        QuestionHelper.getInstance().getQuestionByReference(ref, result ->
        {
            if(result != null)
                question = (Question)result[0];
            if(question != null) {
                selectedAnswers = new ArrayList<>(question.getMaxAnswers());
                setupMultipleChoiceQuestion();
            }
            else
                questionFinished(RESULT_CANCELED);
        });
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
            questionFinished(RESULT_OK);
        }
    }
    protected void onContinueClick(View v)
    {
        questionFinished(RESULT_OK);
    }

    private void setupMultipleChoiceQuestion() {
        TextView txt = findViewById(R.id.questionText);
        TextView helper = findViewById(R.id.questionMcHelperText);
        toolbar.setTitle(question.getSubject());
        txt.setText(question.getText());
        ConstraintLayout answerLayout = findViewById(R.id.questionAnswersLayout);
        answerLayout.removeViews(1, answerLayout.getChildCount() - 1);
        //answerLayout.removeAllViews();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(answerLayout);

        if (question.getMaxAnswers() > 1)
            helper.setText(getString(R.string.questionHelperTextMultiple));
        else {
            helper.setText(getString(R.string.questionHelperTextSingle));
            findViewById(R.id.questionButtomLinearLayout).setVisibility(View.GONE);
        }

        int idAbove = helper.getId();
        for (Answer answer : question.getAnswers()) {
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

    Stack<String> followUpQuestions;
    HashMap<Question, ArrayList<Answer>> result = new HashMap<>();;
    private void questionFinished(int resultCode)
    {
        //make sure we ask any follow-ups
        if(resultCode == RESULT_OK) {
            if (question.getFollowUpQuestion() != null)
                followUpQuestions.push(question.getFollowUpQuestion().getPath());
            for (Answer answer : selectedAnswers)
                if (answer.getFollowUpQuestion() != null)
                    followUpQuestions.push(answer.getFollowUpQuestion().getPath());

            result.put(question, selectedAnswers);
        }

        if(resultCode != RESULT_OK || !handleFollowUp()) {
            returnIntent.putExtra("result", result);
            setResult(resultCode, returnIntent);
            finish();
        }
    }

    private boolean handleFollowUp()
    {
        if(followUpQuestions.isEmpty())
            return false;

        String qRef = followUpQuestions.pop();
        getAndDisplayQuestion(qRef);
        return true;
    }
}
