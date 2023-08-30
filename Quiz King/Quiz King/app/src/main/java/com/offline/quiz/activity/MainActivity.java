package com.offline.quiz.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.offline.quiz.R;
import com.offline.quiz.adapter.BookmarkDBHelper;
import com.offline.quiz.adapter.DBHelper;
import com.offline.quiz.fragment.FragmentCategory;
import com.offline.quiz.fragment.FragmentComplete;
import com.offline.quiz.fragment.FragmentLock;
import com.offline.quiz.fragment.FragmentMainMenu;
import com.offline.quiz.fragment.FragmentPlay;
import com.offline.quiz.AppController;
import com.offline.quiz.fragment.FragmentSubcategory;
import com.offline.quiz.helper.SettingsPreferences;
import com.offline.quiz.Constant;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;

import static com.offline.quiz.fragment.FragmentPlay.loadRewardedVideoAd;

public class MainActivity extends AppCompatActivity implements FragmentPlay.Callback, FragmentMainMenu.Listener {

    SharedPreferences settings;
    FragmentPlay fragmentPlay;
    FragmentLock fragmentlock;
    FragmentSubcategory fragmentSubcategory;
    FragmentComplete fragmentcomplete;
    FragmentMainMenu fragmentMainMenu;
    FragmentCategory fragmentCategory;
    public static DBHelper DBHelper;
    public static BookmarkDBHelper bookmarkDBHelper;
    public static Context context;
    private final Handler mHandler = new Handler();
    private GoogleSignInClient mGoogleSignInClient;
    private PlayersClient mPlayersClient;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;
    // Client variables
    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private final AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();
    public static RewardedVideoAd rewardedVideoAd;
    private String latestVersion;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_home);

        context = getApplicationContext();
        settings = getSharedPreferences(SettingsPreferences.SETTING_Quiz_PREF, 0);
        DBHelper = new DBHelper(getApplicationContext());
        bookmarkDBHelper = new BookmarkDBHelper(getApplicationContext());

        try {
            DBHelper.createDB();
            bookmarkDBHelper.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }



        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getApplicationContext());
        MobileAds.initialize(context, context.getResources().getString(R.string.admob_app_id));
        loadRewardedVideoAd();

        fragmentCategory = new FragmentCategory();
        fragmentSubcategory = new FragmentSubcategory();
        fragmentPlay = new FragmentPlay();
        fragmentlock = new FragmentLock();
        fragmentcomplete = new FragmentComplete();
        fragmentMainMenu = new FragmentMainMenu();
        fragmentMainMenu.setListener(this);
        fragmentPlay.setCallback(this);


        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

        // Create ad request.
        mHandler.postDelayed(mUpdateUITimerTask, 10 * 1000);
        Handler delayhandler = new Handler();
        delayhandler.postDelayed(stopLoadDataDialogSomeTime, 5000);
        if (SettingsPreferences.getMusicEnableDisable(context)) {
            try {
                AppController.playSound();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragmentMainMenu).commit();

    }

    Runnable stopLoadDataDialogSomeTime = new Runnable() {
        public void run() {

        }
    };

    @Override
    protected void onPause() {
        AppController.StopSound();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppController.playSound();
        signInSilently();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d("", "onConnected(): connected to Google APIs");

        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);

        fragmentMainMenu.setShowSignInButton(false);

        mPlayersClient.getCurrentPlayer()
                .addOnCompleteListener(new OnCompleteListener<Player>() {
                    @Override
                    public void onComplete(@NonNull Task<Player> task) {
                        String displayName;
                        if (task.isSuccessful()) {
                            displayName = task.getResult().getDisplayName();
                        } else {
                            Exception e = task.getException();
                            System.out.println("error**.." + e.getMessage());
                            handleException(e, getString(R.string.players_exception));
                            displayName = "???";
                        }

                    }
                });
    }


    private void switchToFragment(Fragment newFrag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newFrag).commit();
    }

    private void startGame() {


        SettingsPreferences.setLan(context, false);
        if (SettingsPreferences.getSoundEnableDisable(context)) {
            Constant.backSoundonclick(context);
        }
        if (SettingsPreferences.getVibration(context)) {
            Constant.vibrate(context, Constant.VIBRATION_DURATION);
        }
        loadRewardedVideoAd();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.open_next, R.anim.close_next);
        ft.replace(R.id.fragment_container, fragmentCategory, "fragment");
        ft.addToBackStack("tag");
        ft.commit();

    }

    private void onDisconnected() {
        Log.d("", "onDisconnected()");

        mAchievementsClient = null;
        mLeaderboardsClient = null;
        mPlayersClient = null;
        fragmentMainMenu.setShowSignInButton(true);
    }


    private void handleException(Exception e, String details) {
        int status = 0;

        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            status = apiException.getStatusCode();
        }

        String message = getString(R.string.status_exception_error, details, status, e);

        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    private void pushAccomplishments() {
        if (!isSignedIn()) {
            // can't push to the cloud, try again later
            return;
        }
        if (mOutbox.achievement_level_1) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_1));
            mOutbox.achievement_level_1 = false;
        }
        if (mOutbox.achievement_level_2) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_2));
            mOutbox.achievement_level_2 = false;
        }
        if (mOutbox.achievement_level_3) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_3));
            mOutbox.achievement_level_3 = false;
        }
        if (mOutbox.achievement_level_4) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_4));
            mOutbox.achievement_level_4 = false;
        }
        if (mOutbox.achievement_level_5) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_5));
            mOutbox.achievement_level_5 = false;
        }
        if (mOutbox.achievement_level_6) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_6));
            mOutbox.achievement_level_6 = false;
        }
        if (mOutbox.achievement_level_7) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_7));
            mOutbox.achievement_level_7 = false;
        }
        if (mOutbox.achievement_level_8) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_8));
            mOutbox.achievement_level_8 = false;
        }
        if (mOutbox.achievement_level_9) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_9));
            mOutbox.achievement_level_9 = false;
        }
        if (mOutbox.achievement_level_10) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_10));
            mOutbox.achievement_level_10 = false;
        }
        /*  if (mOutbox.achievement_level_11) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_11));
            mOutbox.achievement_level_11 = false;
        }
        if (mOutbox.achievement_level_12) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_12));
            mOutbox.achievement_level_12 = false;
        }
        if (mOutbox.achievement_level_13) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_13));
            mOutbox.achievement_level_13 = false;
        }
        if (mOutbox.achievement_level_14) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_14));
            mOutbox.achievement_level_14 = false;
        }
        if (mOutbox.achievement_level_15) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_15));
            mOutbox.achievement_level_15 = false;
        }
      if (mOutbox.achievement_level_16) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_16));
            mOutbox.achievement_level_16 = false;
        }
        if (mOutbox.achievement_level_17) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_17));
            mOutbox.achievement_level_17 = false;
        }
        if (mOutbox.achievement_level_18) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_18));
            mOutbox.achievement_level_18 = false;
        }
        if (mOutbox.achievement_level_19) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_19));
            mOutbox.achievement_level_19 = false;
        }
        if (mOutbox.achievement_level_20) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_20));
            mOutbox.achievement_level_20 = false;
        }
        if (mOutbox.achievement_level_21) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_21));
            mOutbox.achievement_level_21 = false;
        }
        if (mOutbox.achievement_level_22) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_22));
            mOutbox.achievement_level_22 = false;
        }
        if (mOutbox.achievement_level_23) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_23));
            mOutbox.achievement_level_23 = false;
        }
        if (mOutbox.achievement_level_24) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_24));
            mOutbox.achievement_level_24 = false;
        }
        if (mOutbox.achievement_level_25) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_level_25));
            mOutbox.achievement_level_25 = false;
        }*/

//
        if (mOutbox.achievement_achievement_right_answered__10) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_right_answered__10));
            mOutbox.achievement_achievement_right_answered__10 = false;
        }
        if (mOutbox.achievement_achievement_right_answered__20) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_right_answered__20));
            mOutbox.achievement_achievement_right_answered__20 = false;
        }
        if (mOutbox.achievement_achievement_right_answered__50) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_right_answered_50));
            mOutbox.achievement_achievement_right_answered__50 = false;
        }
        if (mOutbox.achievement_achievement_right_answered__100) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_right_answered__100));
            mOutbox.achievement_achievement_right_answered__100 = false;
        }
        if (mOutbox.achievement_achievement_right_answered__200) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_right_answered__200));
            mOutbox.achievement_achievement_right_answered__200 = false;
        }
        if (mOutbox.achievement_achievement_right_answered__400) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_right_answered__200));
            mOutbox.achievement_achievement_right_answered__400 = false;
        }
        if (mOutbox.achievement_achievement_right_answered__500) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_right_answered__500));
            mOutbox.achievement_achievement_right_answered__500 = false;
        }
        if (mOutbox.achievement_achievement_master_cricket_quiz) {
            mAchievementsClient.unlock(getString(R.string.achievement_achievement_achievement_master_offline_quiz));
            mOutbox.achievement_achievement_master_cricket_quiz = false;
        }
//
        if (mOutbox.mEasyModeScore >= 0) {
            mLeaderboardsClient.submitScore(getString(R.string.leaderboard_offline_quiz_leaderbord), mOutbox.mEasyModeScore);
            mOutbox.mEasyModeScore = -1;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {

                    try {
                        AppController.StopSound();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }

                    super.onBackPressed();
                }
                return true;
            case R.id.setting:
                if (SettingsPreferences.getSoundEnableDisable(context)) {
                    Constant.backSoundonclick(context);
                }
                if (SettingsPreferences.getVibration(context)) {
                    Constant.vibrate(context, Constant.VIBRATION_DURATION);
                }
                Intent playQuiz = new Intent(this, SettingActivity.class);
                startActivity(playQuiz);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);


            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
                Toast.makeText(context, "login Successfully", Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                e.printStackTrace();
                onDisconnected();
            }


        }

    }


    private void signInSilently() {

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            onConnected(task.getResult());
                        } else {
                            onDisconnected();
                        }
                    }
                });
    }

    boolean addList = false;

    @Override
    public void onBackPressed() {
        getSupportFragmentManager().popBackStack();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            try {
                AppController.StopSound();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            super.onBackPressed();
        }

    }

    private final Runnable mUpdateUITimerTask = new Runnable() {
        public void run() {
        }
    };

    @Override
    public void onEnteredScore(int score) {

        // check for achievements
        checkForAchievements();

        // update leaderboardso
        updateLeaderboards(score);

        // push those accomplishments to the cloud, if signed in
        pushAccomplishments();

        // switch to the exciting "you won" screen
        /* switchToFragment(fragmentcomplete);*/
    }

    private void updateLeaderboards(int finalScore) {

        mOutbox.mEasyModeScore = finalScore;

    }


    private void checkForAchievements() {
        // Check if each condition is met; if so, unlock the corresponding
        // achievement.

        int levelNo = SettingsPreferences.getNoCompletedLevel(context);
        System.out.println("chek leavel" +levelNo);
        // here give condition if each condition is met;if so, unlock the corresponding achievement.


        //give achievement when level comleted
        if (levelNo == 1) {
            mOutbox.achievement_level_1 = true;
            System.out.println("chek leavel" +levelNo);
        }
        if (levelNo == 2) {
            System.out.println("chek leavel" +levelNo);
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;

        }
        if (levelNo == 3) {
            System.out.println("chek leavel" +levelNo);
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;

        }
        if (levelNo == 4) {
            System.out.println("chek leavel" +levelNo);
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;

        }
        if (levelNo == 5) {
            System.out.println("chek leavel" +levelNo);
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;

        }
        if (levelNo == 6) {
            System.out.println("chek leavel" +levelNo);
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;

        }
        if (levelNo == 7) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;

        }
        if (levelNo == 8) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;

        }
        if (levelNo == 9) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;

        }
        if (levelNo == 10) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;

        }
  /*      if (levelNo == 11) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;

        }
        if (levelNo == 12) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;

        }
        if (levelNo == 13) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;
            mOutbox.achievement_level_13 = true;

        }
        if (levelNo == 14) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;
            mOutbox.achievement_level_13 = true;
            mOutbox.achievement_level_14 = true;

        }
        if (levelNo == 15) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;
            mOutbox.achievement_level_13 = true;
            mOutbox.achievement_level_14 = true;
            mOutbox.achievement_level_15 = true;

        }*/
       /* if (levelNo == 16) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;
            mOutbox.achievement_level_13 = true;
            mOutbox.achievement_level_14 = true;
            mOutbox.achievement_level_15 = true;
            mOutbox.achievement_level_16 = true;

        }
        if (levelNo == 17) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;
            mOutbox.achievement_level_13 = true;
            mOutbox.achievement_level_14 = true;
            mOutbox.achievement_level_15 = true;
            mOutbox.achievement_level_16 = true;
            mOutbox.achievement_level_17 = true;

        }
        if (levelNo == 18) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;
            mOutbox.achievement_level_13 = true;
            mOutbox.achievement_level_14 = true;
            mOutbox.achievement_level_15 = true;
            mOutbox.achievement_level_16 = true;
            mOutbox.achievement_level_17 = true;
            mOutbox.achievement_level_18 = true;

        }
        if (levelNo == 19) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;
            mOutbox.achievement_level_13 = true;
            mOutbox.achievement_level_14 = true;
            mOutbox.achievement_level_15 = true;
            mOutbox.achievement_level_16 = true;
            mOutbox.achievement_level_17 = true;
            mOutbox.achievement_level_18 = true;
            mOutbox.achievement_level_19 = true;

        }
        if (levelNo == 20) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;
            mOutbox.achievement_level_13 = true;
            mOutbox.achievement_level_14 = true;
            mOutbox.achievement_level_15 = true;
            mOutbox.achievement_level_16 = true;
            mOutbox.achievement_level_17 = true;
            mOutbox.achievement_level_18 = true;
            mOutbox.achievement_level_19 = true;
            mOutbox.achievement_level_20 = true;

        }
        if (levelNo == 21) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;
            mOutbox.achievement_level_13 = true;
            mOutbox.achievement_level_14 = true;
            mOutbox.achievement_level_15 = true;
            mOutbox.achievement_level_16 = true;
            mOutbox.achievement_level_17 = true;
            mOutbox.achievement_level_18 = true;
            mOutbox.achievement_level_19 = true;
            mOutbox.achievement_level_20 = true;
            mOutbox.achievement_level_21 = true;

        }
        if (levelNo == 22) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;
            mOutbox.achievement_level_13 = true;
            mOutbox.achievement_level_14 = true;
            mOutbox.achievement_level_15 = true;
            mOutbox.achievement_level_16 = true;
            mOutbox.achievement_level_17 = true;
            mOutbox.achievement_level_18 = true;
            mOutbox.achievement_level_19 = true;
            mOutbox.achievement_level_20 = true;
            mOutbox.achievement_level_21 = true;
            mOutbox.achievement_level_22 = true;

        }
        if (levelNo == 23) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;
            mOutbox.achievement_level_13 = true;
            mOutbox.achievement_level_14 = true;
            mOutbox.achievement_level_15 = true;
            mOutbox.achievement_level_16 = true;
            mOutbox.achievement_level_17 = true;
            mOutbox.achievement_level_18 = true;
            mOutbox.achievement_level_19 = true;
            mOutbox.achievement_level_20 = true;
            mOutbox.achievement_level_21 = true;
            mOutbox.achievement_level_22 = true;
            mOutbox.achievement_level_23 = true;

        }
        if (levelNo == 24) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;
            mOutbox.achievement_level_13 = true;
            mOutbox.achievement_level_14 = true;
            mOutbox.achievement_level_15 = true;
            mOutbox.achievement_level_16 = true;
            mOutbox.achievement_level_17 = true;
            mOutbox.achievement_level_18 = true;
            mOutbox.achievement_level_19 = true;
            mOutbox.achievement_level_20 = true;
            mOutbox.achievement_level_21 = true;
            mOutbox.achievement_level_22 = true;
            mOutbox.achievement_level_23 = true;
            mOutbox.achievement_level_24 = true;

        }
        if (levelNo == 25) {
            mOutbox.achievement_level_1 = true;
            mOutbox.achievement_level_2 = true;
            mOutbox.achievement_level_3 = true;
            mOutbox.achievement_level_4 = true;
            mOutbox.achievement_level_5 = true;
            mOutbox.achievement_level_6 = true;
            mOutbox.achievement_level_7 = true;
            mOutbox.achievement_level_8 = true;
            mOutbox.achievement_level_9 = true;
            mOutbox.achievement_level_10 = true;
            mOutbox.achievement_level_11 = true;
            mOutbox.achievement_level_12 = true;
            mOutbox.achievement_level_13 = true;
            mOutbox.achievement_level_14 = true;
            mOutbox.achievement_level_15 = true;
            mOutbox.achievement_level_16 = true;
            mOutbox.achievement_level_17 = true;
            mOutbox.achievement_level_18 = true;
            mOutbox.achievement_level_19 = true;
            mOutbox.achievement_level_20 = true;
            mOutbox.achievement_level_21 = true;
            mOutbox.achievement_level_22 = true;
            mOutbox.achievement_level_23 = true;
            mOutbox.achievement_level_24 = true;
            mOutbox.achievement_level_25 = true;

        }*/
        int right = SettingsPreferences.getRightAns(context);
        //give when right answer reached to given figure
        if (right == 10) {
            mOutbox.achievement_achievement_right_answered__10 = true;
        }
        if (right == 20) {
            mOutbox.achievement_achievement_right_answered__10 = true;
            mOutbox.achievement_achievement_right_answered__20 = true;
        }

        if (right == 50) {
            mOutbox.achievement_achievement_right_answered__10 = true;
            mOutbox.achievement_achievement_right_answered__20 = true;
            mOutbox.achievement_achievement_right_answered__50 = true;
        }

        if (right == 100) {
            mOutbox.achievement_achievement_right_answered__10 = true;
            mOutbox.achievement_achievement_right_answered__20 = true;
            mOutbox.achievement_achievement_right_answered__50 = true;
            mOutbox.achievement_achievement_right_answered__100 = true;
        }

        if (right == 200) {
            mOutbox.achievement_achievement_right_answered__10 = true;
            mOutbox.achievement_achievement_right_answered__20 = true;
            mOutbox.achievement_achievement_right_answered__50 = true;
            mOutbox.achievement_achievement_right_answered__100 = true;
            mOutbox.achievement_achievement_right_answered__200 = true;
        }
        if (right == 400) {
            mOutbox.achievement_achievement_right_answered__10 = true;
            mOutbox.achievement_achievement_right_answered__20 = true;
            mOutbox.achievement_achievement_right_answered__50 = true;
            mOutbox.achievement_achievement_right_answered__100 = true;
            mOutbox.achievement_achievement_right_answered__200 = true;
            mOutbox.achievement_achievement_right_answered__400 = true;
        }

        if (right == 500) {
            mOutbox.achievement_achievement_right_answered__10 = true;
            mOutbox.achievement_achievement_right_answered__20 = true;
            mOutbox.achievement_achievement_right_answered__50 = true;
            mOutbox.achievement_achievement_right_answered__100 = true;
            mOutbox.achievement_achievement_right_answered__200 = true;
            mOutbox.achievement_achievement_right_answered__400 = true;
            mOutbox.achievement_achievement_right_answered__500 = true;
        }
        if (right > 500) {
            mOutbox.achievement_achievement_master_cricket_quiz = true;
        }

    }

    private void achievementToast(String achievement) {
        // Only show toast if not signed in. If signed in, the standard Google Play
        // toasts will appear, so we don't need to show our own.
        if (!isSignedIn()) {
            Toast.makeText(this, getString(R.string.achievement) + ": " + achievement,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStartGameRequested() {
        startGame();
    }

    @Override
    public void onShowAchievementsRequested() {

        mAchievementsClient.getAchievementsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_UNUSED);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleException(e, getString(R.string.achievements_exception));
                    }
                });
    }

    @Override
    public void onShowLeaderboardsRequested() {

        mLeaderboardsClient.getLeaderboardIntent(getString(R.string.leaderboard_offline_quiz_leaderbord))
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_UNUSED);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


    }

    @Override
    public void onSignInButtonClicked() {
        startSignInIntent();
    }

    @Override
    public void onSignOutButtonClicked() {
        signOut();
    }


    private void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }


    private void signOut() {


        if (!isSignedIn()) {

            return;
        }

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        boolean successful = task.isSuccessful();
                        if (successful == true)
                            Toast.makeText(context, "logout successfully", Toast.LENGTH_SHORT).show();


                        onDisconnected();
                    }
                });
    }


    private class AccomplishmentsOutbox {
        boolean achievement_level_1 = false;
        boolean achievement_level_2 = false;
        boolean achievement_level_3 = false;
        boolean achievement_level_4 = false;
        boolean achievement_level_5 = false;
        boolean achievement_level_6 = false;
        boolean achievement_level_7 = false;
        boolean achievement_level_8 = false;
        boolean achievement_level_9 = false;
        boolean achievement_level_10 = false;
        boolean achievement_level_11 = false;
        boolean achievement_level_12 = false;
        boolean achievement_level_13 = false;
        boolean achievement_level_14 = false;
        boolean achievement_level_15 = false;
        boolean achievement_level_16 = false;
        boolean achievement_level_17 = false;
        boolean achievement_level_18 = false;
        boolean achievement_level_19 = false;
        boolean achievement_level_20 = false;
        boolean achievement_level_21 = false;
        boolean achievement_level_22 = false;
        boolean achievement_level_23 = false;
        boolean achievement_level_24 = false;
        boolean achievement_level_25 = false;

        boolean achievement_achievement_right_answered__10 = false;
        boolean achievement_achievement_right_answered__20 = false;
        boolean achievement_achievement_right_answered__50 = false;
        boolean achievement_achievement_right_answered__100 = false;
        boolean achievement_achievement_right_answered__200 = false;
        boolean achievement_achievement_right_answered__400 = false;
        boolean achievement_achievement_right_answered__500 = false;
        boolean achievement_achievement_master_cricket_quiz = false;
        int mEasyModeScore = -1;

        /*boolean isEmpty() {
            return !mPrimeAchievement && !mHumbleAchievement && !mLeetAchievement &&
                    !mArrogantAchievement && mBoredSteps == 0;
        }*/

    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
    }
}
