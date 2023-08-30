package com.offline.quiz;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;


public class Constant {


    public static int NO_OF_QUIZ_LEVEL = 10; //total level
    public static int NO_OF_QUESTIONS_PER_LEVEL = 10; //max que per level

    public static String PROGRESS_COLOR = "#121149"; // change progress color in play area while choose audience pole
    public static String DOT_COLOR = "#121149"; // change dot color
    public static String AUD_PROGRESS_COLOR="#009dde";
    public static String AUD_PROGRESS_BG_COLOR="#8fd1f2";


    public static int categoryId;
    public static int subCategoryId;
    public static int levelNo;
    public static int totalLevel;

    public static String FragmentTitle;
    public static int CIRCULAR_MAX_PROGRESS = 25; // max progress of circular progress
    public static int TIME_PER_QUESTION = 25000;  //here we set 25 second
    public static int COUNT_DOWN_TIMER = 1000; //here we set 1 second



    private static Vibrator sVibrator;
    public static int TotalQuestion = 1;
    public static int CoreectQuetion = 1;
    public static int WrongQuation = 1;
    public static int level_coin = 1;
    public static final long VIBRATION_DURATION = 100;
    public static final String PLAYSTORE_URL = "http://play.google.com/store/apps/details?id=";
    public static int RequestlevelNo = 1;
    public static final boolean DEFAULT_SOUND_SETTING = true;
    public static final boolean DEFAULT_VIBRATION_SETTING = true;
    public static final boolean DEFAULT_MUSIC_SETTING = false;
    public static final boolean DEFAULT_LAN_SETTING = true;
    public static final String FONTS = "fonts/";


    public static void backSoundonclick(Context mContext) {
        try {
            int resourceId = R.raw.click;
            MediaPlayer mediaplayer = MediaPlayer.create(mContext, resourceId);
            mediaplayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setrightAnssound(Context mContext) {
        try {
            int resourceId = R.raw.right;
            MediaPlayer mediaplayer = MediaPlayer.create(mContext, resourceId);
            mediaplayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setwronAnssound(Context mContext) {
        try {
            int resourceId = R.raw.wrong;
            MediaPlayer mediaplayer = MediaPlayer.create(mContext, resourceId);
            mediaplayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void vibrate(Context context, long duration) {
        if (sVibrator == null) {
            sVibrator = (Vibrator) context
                    .getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (sVibrator != null) {
            if (duration == 0) {
                duration = 50;
            }
            sVibrator.vibrate(duration);
        }
    }
}
