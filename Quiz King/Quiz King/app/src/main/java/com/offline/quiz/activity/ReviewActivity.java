package com.offline.quiz.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.offline.quiz.R;
import com.offline.quiz.fragment.FragmentPlay;
import com.offline.quiz.model.Review;

import java.util.ArrayList;
import java.util.Collections;

import static com.offline.quiz.activity.MainActivity.bookmarkDBHelper;

public class ReviewActivity extends AppCompatActivity {

    public CardView cardView_A, cardView_B, cardView_C, cardView_D;
    public TextView txtQuestion, btnOpt1, btnOpt2, btnOpt3, btnOpt4, tvLevel, tvQuestionNo;
    public ImageView prev, next, back, setting, bookmark;
    public RelativeLayout layout_A, layout_B, layout_C, layout_D;
    private int questionIndex = 0;
    // QuizLevel level;
    private int NO_OF_QUESTION;
    public Button btnExtra;
    public TextView tvNote;
    public CardView cardView;
    public ArrayList<Review> reviews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review);
        btnOpt1 = (TextView) findViewById(R.id.btnOpt1);
        btnOpt2 = (TextView) findViewById(R.id.btnOpt2);
        btnOpt3 = (TextView) findViewById(R.id.btnOpt3);
        btnOpt4 = (TextView) findViewById(R.id.btnOpt4);
        cardView_A = (CardView) findViewById(R.id.cardView_A);
        cardView_B = (CardView) findViewById(R.id.cardView_B);
        cardView_C = (CardView) findViewById(R.id.cardView_C);
        cardView_D = (CardView) findViewById(R.id.cardView_D);
        txtQuestion = (TextView) findViewById(R.id.question);
        tvLevel = (TextView) findViewById(R.id.tvLevel);
        layout_A = (RelativeLayout) findViewById(R.id.a_layout);
        layout_B = (RelativeLayout) findViewById(R.id.b_layout);
        layout_C = (RelativeLayout) findViewById(R.id.c_layout);
        layout_D = (RelativeLayout) findViewById(R.id.d_layout);
        prev = (ImageView) findViewById(R.id.prev);
        next = (ImageView) findViewById(R.id.next);
        back = (ImageView) findViewById(R.id.back);
        setting = (ImageView) findViewById(R.id.setting);
        bookmark = (ImageView) findViewById(R.id.bookmark);
        tvQuestionNo = (TextView) findViewById(R.id.questionNo);
        btnExtra = (Button) findViewById(R.id.btnExtra);
        tvNote = (TextView) findViewById(R.id.tvNote);
cardView=(CardView)findViewById(R.id.cardView1) ;
        bookmark.setVisibility(View.VISIBLE);
        setting.setVisibility(View.INVISIBLE);
        tvLevel.setText(getString(R.string.review_answer));
        reviews = FragmentPlay.reviews;
        // level = FragmentPlay.level;
        NO_OF_QUESTION = reviews.size();
        ReviewQuestion();
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (questionIndex > 0) {
                    questionIndex--;
                    ReviewQuestion();
                }

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (questionIndex < (reviews.size() - 1)) {
                    System.out.println("*** no " + questionIndex);
                    questionIndex++;
                    ReviewQuestion();

                }

            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    public void ReviewQuestion() {
        if (questionIndex < reviews.size()) {
            txtQuestion.setText(reviews.get(questionIndex).getQuestion());
            ArrayList<String> options = new ArrayList<String>();
            options.addAll(reviews.get(questionIndex).getOptionList());
            Collections.shuffle(options);
            btnOpt1.setText("" + options.get(0).trim());
            btnOpt2.setText("" + options.get(1).trim());
            btnOpt3.setText("" + options.get(2).trim());
            btnOpt4.setText("" + options.get(3).trim());

            tvQuestionNo.setText(" " + (questionIndex + 1) + "/" + reviews.size());
            String note = MainActivity.DBHelper.getQuestionSolution(reviews.get(questionIndex).getQueId());
            if (note.isEmpty()){
                btnExtra.setVisibility(View.GONE);
            }
            else{
                btnExtra.setVisibility(View.VISIBLE);
            }

            bookmark.setTag("unmark");
            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (bookmark.getTag().equals("unmark")) {
                        String solution = MainActivity.DBHelper.getQuestionSolution(reviews.get(questionIndex).getQueId());
                        System.out.println("get---" + reviews.get(questionIndex).getQueId());
                        MainActivity.bookmarkDBHelper.insertIntoDB(reviews.get(questionIndex).getQueId(),
                                reviews.get(questionIndex).getQuestion(),
                                reviews.get(questionIndex).getRightAns(),solution);
                        System.out.println("ans---" + reviews.get(questionIndex).getRightAns()+"---"+solution + "=---" + reviews.get(questionIndex).getQuestion());
                        bookmark.setImageResource(R.drawable.ic_mark);
                        bookmark.setTag("mark");
                    } else {
                        MainActivity.bookmarkDBHelper.delete_id(reviews.get(questionIndex).getQueId());
                        bookmark.setImageResource(R.drawable.ic_unmark);
                        bookmark.setTag("unmark");
                    }

                }
            });

            int isfav = bookmarkDBHelper.getBookmarks(reviews.get(questionIndex).getQueId());
            if (isfav == reviews.get(questionIndex).getQueId()) {
                bookmark.setImageResource(R.drawable.ic_mark);
                bookmark.setTag("mark");
            } else {
                bookmark.setImageResource(R.drawable.ic_unmark);
                bookmark.setTag("unmark");
            }
            tvNote.setVisibility(View.INVISIBLE);
            cardView.setVisibility(View.INVISIBLE);
            btnExtra.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String solution = MainActivity.DBHelper.getQuestionSolution(reviews.get(questionIndex).getQueId());
                    cardView.setVisibility(View.VISIBLE);
                    tvNote.setVisibility(View.VISIBLE);
                    tvNote.setText(solution);

                }
            });

            if (btnOpt1.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getRightAns().trim())) {

                layout_A.setBackgroundResource(R.drawable.right_gradient);
                layout_B.setBackgroundResource(R.drawable.answer_bg);
                layout_C.setBackgroundResource(R.drawable.answer_bg);
                layout_D.setBackgroundResource(R.drawable.answer_bg);
                if (btnOpt2.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getWrongAns())) {
                    layout_B.setBackgroundResource(R.drawable.wrong_gradient);
                    // layout_A.setBackgroundResource(R.drawable.answer_bg);
                    layout_C.setBackgroundResource(R.drawable.answer_bg);
                    layout_D.setBackgroundResource(R.drawable.answer_bg);
                } else if (btnOpt3.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getWrongAns())) {
                    layout_C.setBackgroundResource(R.drawable.wrong_gradient);
                    //  layout_A.setBackgroundResource(R.drawable.answer_bg);
                    layout_B.setBackgroundResource(R.drawable.answer_bg);
                    layout_D.setBackgroundResource(R.drawable.answer_bg);
                } else if (btnOpt4.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getWrongAns())) {
                    layout_D.setBackgroundResource(R.drawable.wrong_gradient);
                    //   layout_A.setBackgroundResource(R.drawable.answer_bg);
                    layout_B.setBackgroundResource(R.drawable.answer_bg);
                    layout_C.setBackgroundResource(R.drawable.answer_bg);
                }

            } else if (btnOpt2.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getRightAns().trim())) {

                layout_B.setBackgroundResource(R.drawable.right_gradient);
                layout_A.setBackgroundResource(R.drawable.answer_bg);
                layout_C.setBackgroundResource(R.drawable.answer_bg);
                layout_D.setBackgroundResource(R.drawable.answer_bg);
                if (btnOpt1.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getWrongAns())) {
                    layout_A.setBackgroundResource(R.drawable.wrong_gradient);
                    // layout_B.setBackgroundResource(R.drawable.answer_bg);
                    layout_C.setBackgroundResource(R.drawable.answer_bg);
                    layout_D.setBackgroundResource(R.drawable.answer_bg);
                } else if (btnOpt3.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getWrongAns())) {
                    layout_C.setBackgroundResource(R.drawable.wrong_gradient);
                    layout_A.setBackgroundResource(R.drawable.answer_bg);
                    //  layout_B.setBackgroundResource(R.drawable.answer_bg);
                    layout_D.setBackgroundResource(R.drawable.answer_bg);
                } else if (btnOpt4.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getWrongAns())) {
                    layout_D.setBackgroundResource(R.drawable.wrong_gradient);
                    layout_A.setBackgroundResource(R.drawable.answer_bg);
                    // layout_B.setBackgroundResource(R.drawable.answer_bg);
                    layout_C.setBackgroundResource(R.drawable.answer_bg);
                }

            } else if (btnOpt3.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getRightAns().trim())) {

                layout_C.setBackgroundResource(R.drawable.right_gradient);
                layout_A.setBackgroundResource(R.drawable.answer_bg);
                layout_B.setBackgroundResource(R.drawable.answer_bg);
                layout_D.setBackgroundResource(R.drawable.answer_bg);
                if (btnOpt1.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getWrongAns())) {
                    layout_A.setBackgroundResource(R.drawable.wrong_gradient);
                    layout_B.setBackgroundResource(R.drawable.answer_bg);
                    // layout_C.setBackgroundResource(R.drawable.answer_bg);
                    layout_D.setBackgroundResource(R.drawable.answer_bg);
                } else if (btnOpt2.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getWrongAns())) {
                    layout_B.setBackgroundResource(R.drawable.wrong_gradient);
                    layout_A.setBackgroundResource(R.drawable.answer_bg);
                    // layout_C.setBackgroundResource(R.drawable.answer_bg);
                    layout_D.setBackgroundResource(R.drawable.answer_bg);
                } else if (btnOpt4.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getWrongAns())) {
                    layout_D.setBackgroundResource(R.drawable.wrong_gradient);
                    layout_A.setBackgroundResource(R.drawable.answer_bg);
                    layout_B.setBackgroundResource(R.drawable.answer_bg);
                    //  layout_C.setBackgroundResource(R.drawable.answer_bg);
                }

            } else if (btnOpt4.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getRightAns().trim())) {

                layout_D.setBackgroundResource(R.drawable.right_gradient);
                layout_A.setBackgroundResource(R.drawable.answer_bg);
                layout_C.setBackgroundResource(R.drawable.answer_bg);
                layout_B.setBackgroundResource(R.drawable.answer_bg);
                if (btnOpt1.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getWrongAns())) {
                    layout_A.setBackgroundResource(R.drawable.wrong_gradient);
                    layout_B.setBackgroundResource(R.drawable.answer_bg);
                    layout_C.setBackgroundResource(R.drawable.answer_bg);
                    // layout_D.setBackgroundResource(R.drawable.answer_bg);
                } else if (btnOpt2.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getWrongAns())) {
                    layout_B.setBackgroundResource(R.drawable.wrong_gradient);
                    layout_A.setBackgroundResource(R.drawable.answer_bg);
                    layout_C.setBackgroundResource(R.drawable.answer_bg);
                    // layout_D.setBackgroundResource(R.drawable.answer_bg);
                } else if (btnOpt3.getText().toString().trim().equalsIgnoreCase(reviews.get(questionIndex).getWrongAns())) {
                    layout_C.setBackgroundResource(R.drawable.wrong_gradient);
                    layout_A.setBackgroundResource(R.drawable.answer_bg);
                    layout_B.setBackgroundResource(R.drawable.answer_bg);

                }


            }
        }

    }
}
