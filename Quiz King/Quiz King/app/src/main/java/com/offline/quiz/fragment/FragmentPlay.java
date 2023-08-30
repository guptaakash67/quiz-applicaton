package com.offline.quiz.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.offline.quiz.activity.MainActivity;
import com.offline.quiz.R;
import com.offline.quiz.activity.SettingActivity;
import com.offline.quiz.AppController;
import com.offline.quiz.helper.CheckNetworkConnection;
import com.offline.quiz.helper.CircularProgressIndicator;
import com.offline.quiz.helper.CircularProgressIndicator2;

import com.offline.quiz.helper.SettingsPreferences;
import com.offline.quiz.Constant;
import com.offline.quiz.model.QuizLevel;
import com.offline.quiz.model.Review;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.offline.quiz.activity.MainActivity.context;
import static com.offline.quiz.activity.MainActivity.rewardedVideoAd;

public class FragmentPlay extends Fragment implements OnClickListener {


    public static AdRequest adRequest;
    private static int levelNo = 1;
    public static QuizLevel level;
    public static QuizLevel ReviewList;
    private int quextionIndex = 0;
    private int btnposition = 0;

    private int totalScore = 0;
    private int count_question_completed = 0;
    private int score = 0;
    public int coin = 6;
    public int level_coin = 6;
    private int correctQuestion = 0;
    private int inCorrectQuestion = 0;
    int rightAns;
    public FragmentComplete fragmentComplete;

    public ImageView fifty_fifty, skip_quation, resettimer, audience_poll, back, setting;
    private TextView txtQuestionIndex, txtQuestion, btnOpt1, btnOpt2, btnOpt3, btnOpt4, tvLevel;
    TextView txtScore, txtTrueQuestion, txtFalseQuestion, coin_count;

    public SharedPreferences settings;
    RelativeLayout layout_A, layout_B, layout_C, layout_D, layout_A1, layout_B1, layout_C1, layout_D1;
    private Animation animation;
    private final Handler mHandler = new Handler();
    public static SharedPreferences.Editor editor;
    private View v;
    Animation RightSwipe_A, RightSwipe_B, RightSwipe_C, RightSwipe_D, Fade_in, fifty_fifty_anim;
    private CircularProgressIndicator2 progressBarTwo_A, progressBarTwo_B, progressBarTwo_C, progressBarTwo_D;
    CircularProgressIndicator pwOne;
    public static MyCountDownTimer myCountDownTimer;
    public static Context mcontex;
    private static InterstitialAd interstitial;
    public static Callback mCallback = null;
    public static ArrayList<String> rightList = new ArrayList<String>();
    public static ArrayList<String> wrongList = new ArrayList<String>();
    public static ArrayList<String> options;
    public static ArrayList<Review> reviews = new ArrayList<>();
    MyCountDownTimer myCountDownTimer1;


    public long leftTime = 0;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onEnteredScore(int score);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.play, container, false);
        fragmentComplete = new FragmentComplete();
        final int[] CLICKABLES = new int[]{R.id.a_layout, R.id.b_layout, R.id.c_layout, R.id.d_layout};
        for (int i : CLICKABLES) {
            v.findViewById(i).setOnClickListener(this);
        }
        mcontex = getActivity().getBaseContext();


        RightSwipe_A = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_right_a);
        RightSwipe_B = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_right_b);
        RightSwipe_C = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_right_c);
        RightSwipe_D = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_right_d);
        Fade_in = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        fifty_fifty_anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fifty_fifty);
        settings = getActivity().getSharedPreferences(SettingsPreferences.SETTING_Quiz_PREF, 0);
        resetAllValue();
        wrongList.clear();
        reviews.clear();
        rewardedVideoAd.setRewardedVideoAdListener(rewardedVideoAdListener);
        pwOne = (CircularProgressIndicator) v.findViewById(R.id.progressBarTwo);
        pwOne.setMaxProgress(Constant.CIRCULAR_MAX_PROGRESS);
        pwOne.setCurrentProgress(Constant.CIRCULAR_MAX_PROGRESS);

        try {
            interstitial = new InterstitialAd(getActivity());
            interstitial.setAdUnitId(getString(R.string.admob_interstitial_id));
            adRequest = new AdRequest.Builder().build();
            interstitial.loadAd(adRequest);

            interstitial.setAdListener(new AdListener() {

                //when interstitial ad shown ,timer must be stop
                @Override
                public void onAdOpened() {
                    if (myCountDownTimer != null) {
                        myCountDownTimer.cancel();
                    }
                }

                //after ad close or remove ,timer should be start
                @Override
                public void onAdClosed() {
                    myCountDownTimer1 = new MyCountDownTimer(Constant.TIME_PER_QUESTION, Constant.COUNT_DOWN_TIMER);
                    myCountDownTimer1.start();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    public void ShowRewarded(final Context context) {
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
        }
        if (myCountDownTimer1 != null) {
            myCountDownTimer1.cancel();
        }
        if (!CheckNetworkConnection.isConnectionAvailable(getActivity())) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert);
            dialog.setTitle("Internet Connection Error!");
            dialog.setMessage("Internet Connection Error! Please connect to working Internet connection");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();
            return;
        } else {

            final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View dialogView = inflater.inflate(R.layout.dialog_layout, null);
            dialog.setView(dialogView);
            TextView skip = (TextView) dialogView.findViewById(R.id.skip);
            TextView watchNow = (TextView) dialogView.findViewById(R.id.watch_now);
            final AlertDialog alertDialog = dialog.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            alertDialog.show();

            alertDialog.setCancelable(false);
            skip.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                    if (leftTime != 0) {
                        myCountDownTimer1 = new MyCountDownTimer(leftTime, Constant.COUNT_DOWN_TIMER);
                        myCountDownTimer1.start();
                    }
                }
            });
            watchNow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showRewardedVideo();
                    alertDialog.dismiss();
                }
            });

        }
    }

    public void showRewardedVideo() {

        if (rewardedVideoAd.isLoaded()) {
            rewardedVideoAd.show();

        } else if (!rewardedVideoAd.isLoaded()) {
            loadRewardedVideoAd();
            if (rewardedVideoAd.isLoaded()) {
                rewardedVideoAd.show();
            }
        }
    }

    public static void loadRewardedVideoAd() {

        if (!rewardedVideoAd.isLoaded()) {
            rewardedVideoAd.loadAd(context.getResources().getString(R.string.admob_Rewarded_Video_Ads), new AdRequest.Builder().build());
        }
    }

    RewardedVideoAdListener rewardedVideoAdListener = new RewardedVideoAdListener() {
        @Override
        public void onRewardedVideoAdLoaded() {
        }

        @Override
        public void onRewardedVideoAdOpened() {
        }

        @Override
        public void onRewardedVideoStarted() {
        }

        @Override
        public void onRewardedVideoAdClosed() {
            loadRewardedVideoAd();
        }

        @Override
        public void onRewarded(RewardItem reward) {
            // Reward the user.
            coin = coin + 4;
        }

        @Override
        public void onRewardedVideoAdLeftApplication() {
        }

        @Override
        public void onRewardedVideoAdFailedToLoad(int i) {
        }

        @Override
        public void onRewardedVideoCompleted() {

        }
    };

    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            leftTime = millisUntilFinished;
            int progress = (int) (millisUntilFinished / Constant.COUNT_DOWN_TIMER);
            pwOne.setCurrentProgress(progress);
            //pwOne.setText("" + progress);
        }

        @Override
        public void onFinish() {
            if (quextionIndex >= Constant.NO_OF_QUESTIONS_PER_LEVEL - 1) {
                levelCompleted();

            } else {
                quextionIndex++;
                //wrongeQuestion();
                mHandler.postDelayed(mUpdateUITimerTask, 100);
            }

        }
    }


    @Override
    public void onStart() {
        super.onStart();
        updateUi();
    }


    private void nextQuizQuestion() {
        setAgain();
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
            myCountDownTimer.start();
        } else {
            myCountDownTimer.start();
        }
        if (myCountDownTimer1 != null) {
            myCountDownTimer1.cancel();
            leftTime = 0;
        }
        leftTime = 0;
        if (quextionIndex >= Constant.NO_OF_QUESTIONS_PER_LEVEL) {
            levelCompleted();
        }
        layout_A.setClickable(true);
        layout_B.setClickable(true);
        layout_C.setClickable(true);
        layout_D.setClickable(true);
        layout_A.startAnimation(RightSwipe_A);
        layout_B.startAnimation(RightSwipe_B);
        layout_C.startAnimation(RightSwipe_C);
        layout_D.startAnimation(RightSwipe_D);
        txtQuestion.startAnimation(Fade_in);
        if (quextionIndex < level.getNoOfQuestion()) {
            int temp = quextionIndex;
            txtQuestionIndex.setText(++temp + "/" + Constant.NO_OF_QUESTIONS_PER_LEVEL);
            String imgName = level.getQuestion().get(quextionIndex).getQuestion();
            Pattern p = Pattern.compile(" \r\n");
            Matcher m = p.matcher(imgName);
            imgName = m.replaceAll("_");
            txtQuestion.setText(level.getQuestion().get(quextionIndex).getQuestion());
            options = new ArrayList<String>();
            options.addAll(level.getQuestion().get(quextionIndex).getOptions());
            Collections.shuffle(options);

            btnOpt1.setText("" + options.get(0).trim());
            btnOpt2.setText("" + options.get(1).trim());
            btnOpt3.setText("" + options.get(2).trim());
            btnOpt4.setText("" + options.get(3).trim());

        }

    }

    public void levelCompleted() {
        Constant.TotalQuestion = Constant.NO_OF_QUESTIONS_PER_LEVEL;
        Constant.CoreectQuetion = correctQuestion;
        Constant.WrongQuation = inCorrectQuestion;
        myCountDownTimer.cancel();
        editor = settings.edit();
        if (correctQuestion >= 3 && levelNo == Constant.RequestlevelNo) {
            levelNo = levelNo + 1;
            if (MainActivity.DBHelper.isExist(Constant.categoryId, Constant.subCategoryId)) {
                MainActivity.DBHelper.UpdateLevel(Constant.categoryId, Constant.subCategoryId, levelNo);
            } else {
                MainActivity.DBHelper.insertIntoDB(Constant.categoryId, Constant.subCategoryId, levelNo);
            }
//            if (MainActivity.DBHelper.isExistSingleCat(Constant.categoryId)) {
//
//                MainActivity.DBHelper.UpdateLevelSingleCat(Constant.categoryId, levelNo);
//            } else {
//                MainActivity.DBHelper.insertIntoDBSingleCat(Constant.categoryId, levelNo);
//            }
            SettingsPreferences.setNoCompletedLevel(mcontex, levelNo);
        }
        if (correctQuestion >= 3) {
            editor.putBoolean(SettingsPreferences.IS_LAST_LEVEL_COMPLETED, true);
        } else {
            editor.putBoolean(SettingsPreferences.IS_LAST_LEVEL_COMPLETED, false);
        }
        if (correctQuestion >= 3 && correctQuestion <= 4) {
            coin = coin + 1;
            level_coin = 1;
            Constant.level_coin = level_coin;
        } else if (correctQuestion >= 4 && correctQuestion <= 5) {
            coin = coin + 2;
            level_coin = 2;
            Constant.level_coin = level_coin;
        } else if (correctQuestion >= 5 && correctQuestion <= 6) {
            coin = coin + 3;
            level_coin = 3;
            Constant.level_coin = level_coin;
        } else if (correctQuestion == 6) {
            coin = coin + 4;
            level_coin = 4;
            Constant.level_coin = level_coin;
        }
        SettingsPreferences.setPoint(mcontex, coin);
        coin_count.setText("" + coin);
        editor.commit();

        mcontex = getActivity().getBaseContext();
        saveScore();
        getActivity().getSupportFragmentManager().popBackStack();
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragmentComplete)
                .addToBackStack("tag").commit();
        blankAllValue();
        return;
    }

    public static void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();

        } else {
            adRequest = new AdRequest.Builder().build();
            interstitial.loadAd(adRequest);
            interstitial.show();

        }

    }

    @Override
    public void onClick(View v) {
        if (quextionIndex < level.getNoOfQuestion()) {
            layout_A.setClickable(false);
            layout_B.setClickable(false);
            layout_C.setClickable(false);
            layout_D.setClickable(false);
            if (progressBarTwo_A.getVisibility() == (v.VISIBLE)) {
                progressBarTwo_A.setVisibility(v.GONE);
                progressBarTwo_B.setVisibility(v.GONE);
                progressBarTwo_C.setVisibility(v.GONE);
                progressBarTwo_D.setVisibility(v.GONE);
            }

            if (myCountDownTimer1 != null) {
                myCountDownTimer1.cancel();
                leftTime = 0;
            }
            switch (v.getId()) {
                case R.id.a_layout:
                    reviews.add(new Review(level.getQuestion().get(quextionIndex).getId(),
                            level.getQuestion().get(quextionIndex).getQuestion(),
                            level.getQuestion().get(quextionIndex).getTrueAns(),
                            btnOpt1.getText().toString(),
                            level.getQuestion().get(quextionIndex).getOptions()));


                    if (btnOpt1.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {
                        quextionIndex++;
                        addScore();
                        layout_A.setBackgroundResource(R.drawable.right_gradient);
                        layout_A.startAnimation(animation);
                        layout_B.setBackgroundResource(R.drawable.answer_bg);
                        layout_C.setBackgroundResource(R.drawable.answer_bg);
                        layout_D.setBackgroundResource(R.drawable.answer_bg);

                    } else if (!btnOpt1.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {

                        layout_A.setBackgroundResource(R.drawable.wrong_gradient);
                        wrongeQuestion();
                        String trueAns = level.getQuestion().get(quextionIndex).getTrueAns().trim();
                        if (btnOpt2.getText().toString().trim().equals(trueAns)) {
                            layout_B.startAnimation(animation);
                            layout_B.setBackgroundResource(R.drawable.right_gradient);
                            layout_C.setBackgroundResource(R.drawable.answer_bg);
                            layout_D.setBackgroundResource(R.drawable.answer_bg);


                        } else if (btnOpt3.getText().toString().trim().equals(trueAns)) {
                            layout_C.setBackgroundResource(R.drawable.right_gradient);
                            layout_C.startAnimation(animation);
                            layout_B.setBackgroundResource(R.drawable.answer_bg);
                            layout_D.setBackgroundResource(R.drawable.answer_bg);

                        } else if (btnOpt4.getText().toString().trim().equals(trueAns)) {
                            layout_D.setBackgroundResource(R.drawable.right_gradient);
                            layout_D.startAnimation(animation);
                            layout_B.setBackgroundResource(R.drawable.answer_bg);
                            layout_C.setBackgroundResource(R.drawable.answer_bg);
                        }


                        quextionIndex++;
                    }
                    if (myCountDownTimer != null) {
                        myCountDownTimer.cancel();

                    }
                    break;

                case R.id.b_layout:
                    reviews.add(new Review(level.getQuestion().get(quextionIndex).getId(),
                            level.getQuestion().get(quextionIndex).getQuestion(),
                            level.getQuestion().get(quextionIndex).getTrueAns(),
                            btnOpt2.getText().toString(),
                            level.getQuestion().get(quextionIndex).getOptions()));
                    if (btnOpt2.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {
                        quextionIndex++;
                        addScore();
                        layout_B.setBackgroundResource(R.drawable.right_gradient);
                        layout_B.startAnimation(animation);
                        layout_A.setBackgroundResource(R.drawable.answer_bg);
                        layout_C.setBackgroundResource(R.drawable.answer_bg);
                        layout_D.setBackgroundResource(R.drawable.answer_bg);

                    } else if (!btnOpt2.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {

                        String trueAns = level.getQuestion().get(quextionIndex).getTrueAns().trim();
                        layout_B.setBackgroundResource(R.drawable.wrong_gradient);
                        wrongeQuestion();

                        if (btnOpt1.getText().toString().trim().equals(trueAns)) {
                            layout_A.startAnimation(animation);
                            layout_A.setBackgroundResource(R.drawable.right_gradient);
                            layout_C.setBackgroundResource(R.drawable.answer_bg);
                            layout_D.setBackgroundResource(R.drawable.answer_bg);

                        } else if (btnOpt3.getText().toString().trim().equals(trueAns)) {
                            layout_C.setBackgroundResource(R.drawable.right_gradient);
                            layout_C.startAnimation(animation);
                            layout_A.setBackgroundResource(R.drawable.answer_bg);
                            layout_D.setBackgroundResource(R.drawable.answer_bg);

                        } else if (btnOpt4.getText().toString().trim().equals(trueAns)) {
                            layout_D.setBackgroundResource(R.drawable.right_gradient);
                            layout_D.startAnimation(animation);
                            layout_A.setBackgroundResource(R.drawable.answer_bg);
                            layout_C.setBackgroundResource(R.drawable.answer_bg);
                        }

                        quextionIndex++;
                    }
                    if (myCountDownTimer != null) {
                        myCountDownTimer.cancel();

                    }

                    break;
                case R.id.c_layout:
                    reviews.add(new Review(level.getQuestion().get(quextionIndex).getId(),
                            level.getQuestion().get(quextionIndex).getQuestion(),
                            level.getQuestion().get(quextionIndex).getTrueAns(),
                            btnOpt3.getText().toString(),
                            level.getQuestion().get(quextionIndex).getOptions()));
                    if (btnOpt3.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {
                        quextionIndex++;
                        addScore();
                        layout_C.setBackgroundResource(R.drawable.right_gradient);
                        layout_C.startAnimation(animation);
                        layout_A.setBackgroundResource(R.drawable.answer_bg);
                        layout_B.setBackgroundResource(R.drawable.answer_bg);
                        layout_D.setBackgroundResource(R.drawable.answer_bg);


                    } else if (!btnOpt3.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {
                        layout_C.setBackgroundResource(R.drawable.wrong_gradient);
                        String trueAns = level.getQuestion().get(quextionIndex).getTrueAns().trim();

                        wrongeQuestion();
                        if (btnOpt1.getText().toString().trim().equals(trueAns)) {
                            layout_A.startAnimation(animation);
                            layout_A.setBackgroundResource(R.drawable.right_gradient);
                            layout_B.setBackgroundResource(R.drawable.answer_bg);
                            layout_D.setBackgroundResource(R.drawable.answer_bg);

                        } else if (btnOpt2.getText().toString().trim().equals(trueAns)) {
                            layout_B.startAnimation(animation);
                            layout_B.setBackgroundResource(R.drawable.right_gradient);
                            layout_A.setBackgroundResource(R.drawable.answer_bg);
                            layout_D.setBackgroundResource(R.drawable.answer_bg);

                        } else if (btnOpt4.getText().toString().trim().equals(trueAns)) {
                            layout_D.setBackgroundResource(R.drawable.right_gradient);
                            layout_D.startAnimation(animation);
                            layout_A.setBackgroundResource(R.drawable.answer_bg);
                            layout_B.setBackgroundResource(R.drawable.answer_bg);
                        }


                        quextionIndex++;
                    }
                    if (myCountDownTimer != null) {
                        myCountDownTimer.cancel();

                    }

                    break;
                case R.id.d_layout:
                    reviews.add(new Review(level.getQuestion().get(quextionIndex).getId(),
                            level.getQuestion().get(quextionIndex).getQuestion(),
                            level.getQuestion().get(quextionIndex).getTrueAns(),
                            btnOpt4.getText().toString(),
                            level.getQuestion().get(quextionIndex).getOptions()));

                    if (btnOpt4.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {
                        layout_D.setBackgroundResource(R.drawable.right_gradient);
                        layout_D.startAnimation(animation);
                        quextionIndex++;
                        layout_A.setBackgroundResource(R.drawable.answer_bg);
                        layout_B.setBackgroundResource(R.drawable.answer_bg);
                        layout_C.setBackgroundResource(R.drawable.answer_bg);
                        addScore();

                    } else if (!btnOpt4.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {
                        layout_D.setBackgroundResource(R.drawable.wrong_gradient);
                        wrongeQuestion();
                        String trueAns = level.getQuestion().get(quextionIndex).getTrueAns().trim();

                        if (btnOpt1.getText().toString().trim().equals(trueAns)) {
                            layout_A.startAnimation(animation);
                            layout_A.setBackgroundResource(R.drawable.right_gradient);
                            layout_B.setBackgroundResource(R.drawable.answer_bg);
                            layout_C.setBackgroundResource(R.drawable.answer_bg);

                        } else if (btnOpt2.getText().toString().trim().equals(trueAns)) {
                            layout_B.startAnimation(animation);
                            layout_B.setBackgroundResource(R.drawable.right_gradient);
                            layout_A.setBackgroundResource(R.drawable.answer_bg);
                            layout_C.setBackgroundResource(R.drawable.answer_bg);

                        } else if (btnOpt3.getText().toString().trim().equals(trueAns)) {
                            layout_C.setBackgroundResource(R.drawable.right_gradient);
                            layout_C.startAnimation(animation);
                            layout_A.setBackgroundResource(R.drawable.answer_bg);
                            layout_B.setBackgroundResource(R.drawable.answer_bg);

                        }

                        quextionIndex++;
                    }

                    if (myCountDownTimer != null) {
                        myCountDownTimer.cancel();

                    }

                    break;
            }

        } else {
            mHandler.postDelayed(mUpdateUITimerTask, 100);


        }
        mHandler.postDelayed(mUpdateUITimerTask, 1000);
        txtScore.setText("" + totalScore);
    }

    private final Runnable mUpdateUITimerTask = new Runnable() {
        public void run() {
            layout_A.setBackgroundResource(R.drawable.answer_bg);
            layout_B.setBackgroundResource(R.drawable.answer_bg);
            layout_C.setBackgroundResource(R.drawable.answer_bg);
            layout_D.setBackgroundResource(R.drawable.answer_bg);
            layout_A.clearAnimation();
            layout_B.clearAnimation();
            layout_C.clearAnimation();
            layout_D.clearAnimation();
            layout_A1.clearAnimation();
            layout_B1.clearAnimation();
            layout_C1.clearAnimation();
            layout_D1.clearAnimation();

            if (getActivity() != null) {
                nextQuizQuestion();
            }
        }
    };

    private void addScore() {

        rightSound();
        correctQuestion++;
        txtTrueQuestion.setText(" " + correctQuestion + " ");
        totalScore = totalScore + 5;
        count_question_completed = count_question_completed + 5;
        score = score + 5;
        txtScore.setText("" + totalScore);
        rightAns = SettingsPreferences.getRightAns(mcontex);
        rightAns++;
        SettingsPreferences.setRightAns(mcontex, rightAns);
        SettingsPreferences.setScore(mcontex, totalScore);
        SettingsPreferences.setCountQuestionCompleted(mcontex, count_question_completed);
    }

    private void wrongeQuestion() {
        setAgain();
        playWrongSound();
        saveScore();
        inCorrectQuestion++;
        totalScore = totalScore - 2;
        count_question_completed = count_question_completed - 2;
        score = score - 2;
        txtFalseQuestion.setText(" " + inCorrectQuestion + " ");
        txtScore.setText("" + totalScore);
    }

    private void saveScore() {
        editor = settings.edit();
        SettingsPreferences.setCountQuestionCompleted(mcontex, count_question_completed);
        editor.putInt(SettingsPreferences.TOTAL_SCORE, totalScore);
        editor.putInt(SettingsPreferences.LAST_LEVEL_SCORE, score);
        editor.putInt(SettingsPreferences.COUNT_QUESTION_COMPLETED, count_question_completed);
        editor.commit();

        try {
            mCallback.onEnteredScore(totalScore);
            System.out.println("-----/-"+totalScore);
        } catch (Exception e) {

        }

    }

    public void rightSound() {
        if (SettingsPreferences.getSoundEnableDisable(getActivity())) {
            Constant.setrightAnssound(getActivity());
        }
        if (SettingsPreferences.getVibration(getActivity())) {
            Constant.vibrate(getActivity(), Constant.VIBRATION_DURATION);
        }
    }

    private void playWrongSound() {
        if (SettingsPreferences.getSoundEnableDisable(getActivity())) {
            Constant.setwronAnssound(getActivity());
        }
        if (SettingsPreferences.getVibration(getActivity())) {
            Constant.vibrate(getActivity(), Constant.VIBRATION_DURATION);
        }
    }

    private void setAgain() {
        if (progressBarTwo_A.getVisibility() == (v.VISIBLE)) {
            progressBarTwo_A.setVisibility(v.GONE);
            progressBarTwo_B.setVisibility(v.GONE);
            progressBarTwo_C.setVisibility(v.GONE);
            progressBarTwo_D.setVisibility(v.GONE);
        }
    }

    private void resetAllValue() {
        levelNo = SettingsPreferences.getNoCompletedLevel(mcontex);
        txtTrueQuestion = (TextView) v.findViewById(R.id.txtTrueQuestion);
        txtFalseQuestion = (TextView) v.findViewById(R.id.txtFalseQuestion);
        txtQuestion = (TextView) v.findViewById(R.id.txtQuestion);
        layout_A = (RelativeLayout) v.findViewById(R.id.a_layout);
        layout_B = (RelativeLayout) v.findViewById(R.id.b_layout);
        layout_C = (RelativeLayout) v.findViewById(R.id.c_layout);
        layout_D = (RelativeLayout) v.findViewById(R.id.d_layout);
        layout_A1 = (RelativeLayout) v.findViewById(R.id.layout_A);
        layout_B1 = (RelativeLayout) v.findViewById(R.id.layout_B);
        layout_C1 = (RelativeLayout) v.findViewById(R.id.layout_C);
        layout_D1 = (RelativeLayout) v.findViewById(R.id.layout_D);
        txtScore = (TextView) v.findViewById(R.id.txtScore);
        txtQuestionIndex = (TextView) v.findViewById(R.id.txt_question);
        coin_count = (TextView) v.findViewById(R.id.coin_count);

        btnOpt1 = (TextView) v.findViewById(R.id.btnOpt1);
        btnOpt2 = (TextView) v.findViewById(R.id.btnOpt2);
        btnOpt3 = (TextView) v.findViewById(R.id.btnOpt3);
        btnOpt4 = (TextView) v.findViewById(R.id.btnOpt4);

        back = (ImageView) v.findViewById(R.id.back);
        setting = (ImageView) v.findViewById(R.id.setting);

        tvLevel = (TextView) v.findViewById(R.id.tvLevel);
        fifty_fifty = (ImageView) v.findViewById(R.id.fifty_fifty);
        skip_quation = (ImageView) v.findViewById(R.id.skip_quation);
        resettimer = (ImageView) v.findViewById(R.id.reset_timer);
        audience_poll = (ImageView) v.findViewById(R.id.audience_poll);
        progressBarTwo_A = (CircularProgressIndicator2) v.findViewById(R.id.progress_A);
        progressBarTwo_B = (CircularProgressIndicator2) v.findViewById(R.id.progress_B);
        progressBarTwo_C = (CircularProgressIndicator2) v.findViewById(R.id.progress_C);
        progressBarTwo_D = (CircularProgressIndicator2) v.findViewById(R.id.progress_D);
        progressBarTwo_A.SetAttributes(Color.parseColor(Constant.AUD_PROGRESS_COLOR), Color.parseColor(Constant.AUD_PROGRESS_COLOR), Color.parseColor(Constant.AUD_PROGRESS_BG_COLOR), 10);
        progressBarTwo_B.SetAttributes(Color.parseColor(Constant.AUD_PROGRESS_COLOR), Color.parseColor(Constant.AUD_PROGRESS_COLOR), Color.parseColor(Constant.AUD_PROGRESS_BG_COLOR), 10);
        progressBarTwo_C.SetAttributes(Color.parseColor(Constant.AUD_PROGRESS_COLOR), Color.parseColor(Constant.AUD_PROGRESS_COLOR), Color.parseColor(Constant.AUD_PROGRESS_BG_COLOR), 10);
        progressBarTwo_D.SetAttributes(Color.parseColor(Constant.AUD_PROGRESS_COLOR), Color.parseColor(Constant.AUD_PROGRESS_COLOR), Color.parseColor(Constant.AUD_PROGRESS_BG_COLOR), 10);

        coin_count.setText("" + coin);
        //setoptenabledisable();
        audience_poll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SettingsPreferences.getSoundEnableDisable(getActivity())) {
                    Constant.backSoundonclick(getActivity());
                }
                if (SettingsPreferences.getVibration(getActivity())) {
                    Constant.vibrate(getActivity(), Constant.VIBRATION_DURATION);
                }
                if (coin >= 4) {
                    btnposition = 0;
                    //audience_poll.setImageResource(R.drawable.dec_audiance);
                    coin = coin - 4;
                    coin_count.setText("" + coin);
                    int min = 45;
                    int max = 70;
                    Random r = new Random();
                    int A = r.nextInt(max - min + 1) + min;
                    int remain1 = 100 - A;
                    int B = r.nextInt(((remain1 - 10) - 0) + 1) + 0;
                    int remain2 = remain1 - B;
                    int C = r.nextInt(((remain2 - 5) - 0) + 1) + 0;
                    int D = remain2 - C;
                    progressBarTwo_A.setVisibility(v.VISIBLE);
                    progressBarTwo_B.setVisibility(v.VISIBLE);
                    progressBarTwo_C.setVisibility(v.VISIBLE);
                    progressBarTwo_D.setVisibility(v.VISIBLE);

                    if (btnOpt1.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {

                        btnposition = 1;
                    }

                    if (btnOpt2.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {

                        btnposition = 2;
                    }

                    if (btnOpt3.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {

                        btnposition = 3;
                    }

                    if (btnOpt4.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {
                        btnposition = 4;
                    }

                    if (btnposition == 1) {
                        progressBarTwo_A.setCurrentProgress(A);
                        progressBarTwo_B.setCurrentProgress(B);
                        progressBarTwo_C.setCurrentProgress(C);
                        progressBarTwo_D.setCurrentProgress(D);

                    } else if (btnposition == 2) {
                        progressBarTwo_B.setCurrentProgress(A);
                        progressBarTwo_C.setCurrentProgress(C);
                        progressBarTwo_D.setCurrentProgress(D);
                        progressBarTwo_A.setCurrentProgress(B);

                    } else if (btnposition == 3) {
                        progressBarTwo_C.setCurrentProgress(A);
                        progressBarTwo_B.setCurrentProgress(C);
                        progressBarTwo_D.setCurrentProgress(D);
                        progressBarTwo_A.setCurrentProgress(B);

                    } else if (btnposition == 4) {
                        progressBarTwo_D.setCurrentProgress(A);
                        progressBarTwo_B.setCurrentProgress(C);
                        progressBarTwo_C.setCurrentProgress(D);
                        progressBarTwo_A.setCurrentProgress(B);

                    }
                } else {
                    ShowRewarded(getActivity());
                }
            }
        });
        resettimer.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (SettingsPreferences.getSoundEnableDisable(getActivity())) {
                    Constant.backSoundonclick(getActivity());
                }
                if (SettingsPreferences.getVibration(getActivity())) {
                    Constant.vibrate(getActivity(), Constant.VIBRATION_DURATION);
                }
                if (coin >= 4) {
                    //resettimer.setImageResource(R.drawable.dec_timer);
                    coin = coin - 4;
                    coin_count.setText("" + coin);
                    // TODO Auto-generated method stub

                    if (myCountDownTimer1 != null) {
                        myCountDownTimer1.cancel();
                        leftTime = 0;
                    }
                    if (myCountDownTimer != null) {
                        myCountDownTimer.cancel();
                        myCountDownTimer.start();
                    } else {
                        myCountDownTimer.start();
                    }
                } else {
                    ShowRewarded(getActivity());

                }
            }
        });
        skip_quation.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (SettingsPreferences.getSoundEnableDisable(getActivity())) {
                    Constant.backSoundonclick(getActivity());
                }
                if (SettingsPreferences.getVibration(getActivity())) {
                    Constant.vibrate(getActivity(), Constant.VIBRATION_DURATION);
                }
                if (coin >= 4) {

                    //skip_quation.setImageResource(R.drawable.dec_skip);
                    coin = coin - 4;
                    coin_count.setText("" + coin);
                    quextionIndex++;
                    layout_A.setBackgroundResource(R.drawable.answer_bg);
                    layout_B.setBackgroundResource(R.drawable.answer_bg);
                    layout_C.setBackgroundResource(R.drawable.answer_bg);
                    layout_D.setBackgroundResource(R.drawable.answer_bg);
                    nextQuizQuestion();
                } else {
                    ShowRewarded(getActivity());
                }
            }
        });
        fifty_fifty.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (SettingsPreferences.getSoundEnableDisable(getActivity())) {
                    Constant.backSoundonclick(getActivity());
                }
                if (SettingsPreferences.getVibration(getActivity())) {
                    Constant.vibrate(getActivity(), Constant.VIBRATION_DURATION);
                }
                if (coin >= 4) {
                    btnposition = 0;
                    //fifty_fifty.setImageResource(R.drawable.dec_fifty);
                    // TODO Auto-generated method stub
                    coin = coin - 4;
                    coin_count.setText("" + coin);
                    if (btnOpt1.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {
                        btnposition = 1;
                    }

                    if (btnOpt2.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {
                        btnposition = 2;
                    }

                    if (btnOpt3.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {
                        btnposition = 3;
                    }

                    if (btnOpt4.getText().toString().trim().equalsIgnoreCase(level.getQuestion().get(quextionIndex).getTrueAns().trim())) {
                        btnposition = 4;
                    }

                    if (btnposition == 1) {
                        layout_B.startAnimation(fifty_fifty_anim);
                        layout_C.startAnimation(fifty_fifty_anim);
                        layout_B1.startAnimation(fifty_fifty_anim);
                        layout_C1.startAnimation(fifty_fifty_anim);
                    } else if (btnposition == 2) {
                        layout_C.startAnimation(fifty_fifty_anim);
                        layout_D.startAnimation(fifty_fifty_anim);
                        layout_C1.startAnimation(fifty_fifty_anim);
                        layout_D1.startAnimation(fifty_fifty_anim);
                    } else if (btnposition == 3) {
                        layout_D.startAnimation(fifty_fifty_anim);
                        layout_A.startAnimation(fifty_fifty_anim);
                        layout_D1.startAnimation(fifty_fifty_anim);
                        layout_A1.startAnimation(fifty_fifty_anim);
                    } else if (btnposition == 4) {
                        layout_A.startAnimation(fifty_fifty_anim);
                        layout_B.startAnimation(fifty_fifty_anim);
                        layout_A1.startAnimation(fifty_fifty_anim);
                        layout_B1.startAnimation(fifty_fifty_anim);
                    }
                } else {
                    ShowRewarded(getActivity());
                }
            }
        });

        txtTrueQuestion.setText("0");

        txtFalseQuestion.setText("0");

        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.right_ans_anim); // Change alpha from fully visible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the
        totalScore = SettingsPreferences.getScore(mcontex);
        count_question_completed = SettingsPreferences.getCountQuestionCompleted(mcontex);
        coin = SettingsPreferences.getPoint(mcontex);

        txtScore.setText("" + totalScore);
        coin_count.setText("" + coin);
        level = new QuizLevel(Constant.RequestlevelNo, Constant.NO_OF_QUESTIONS_PER_LEVEL, MainActivity.DBHelper);
        level.setQuestionGuj();
        //ReviewList.setQuestionGuj();

        tvLevel.setText("Level" + " : " + Constant.RequestlevelNo);
        myCountDownTimer = new MyCountDownTimer(Constant.TIME_PER_QUESTION, Constant.COUNT_DOWN_TIMER);
        nextQuizQuestion();


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
                if (getActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {

                    try {
                        AppController.StopSound();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }


                }
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SettingsPreferences.getSoundEnableDisable(getActivity())) {
                    Constant.backSoundonclick(getActivity());
                }
                if (SettingsPreferences.getVibration(getActivity())) {
                    Constant.vibrate(getActivity(), Constant.VIBRATION_DURATION);
                }
                Intent playQuiz = new Intent(getActivity(), SettingActivity.class);
                startActivity(playQuiz);
                getActivity().overridePendingTransition(R.anim.open_next, R.anim.close_next);
            }
        });


    }


    public void blankAllValue() {
        quextionIndex = 0;
        totalScore = 0;
        count_question_completed = 0;
        score = 0;
        correctQuestion = 0;
        inCorrectQuestion = 0;
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        blankAllValue();
        super.onDestroyView();
    }

    @Override
    public void onResume() {

        System.out.println("left time : " + leftTime);
        if (leftTime != 0) {
            myCountDownTimer1 = new MyCountDownTimer(leftTime, Constant.COUNT_DOWN_TIMER);
            myCountDownTimer1.start();
        }
        super.onResume();
        updateUi();

     /*  if (myCountDownTimer != null) {
           // myCountDownTimer.cancel();
            myCountDownTimer.start();
        }*/
        coin_count.setText("" + coin);
    }

    @Override
    public void onPause() {
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
        }
        super.onPause();
    }

    void updateUi() {
        if (getActivity() == null)
            return;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
        }
        super.onDestroy();
    }

}
