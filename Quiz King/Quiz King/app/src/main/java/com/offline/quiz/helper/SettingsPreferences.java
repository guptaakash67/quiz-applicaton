package com.offline.quiz.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.offline.quiz.Constant;

public class SettingsPreferences {


    public static final String SETTING_Quiz_PREF = "setting_quiz_pref";

    private static final String SOUND_ONOFF = "sound_enable_disable";

    private static final String SHOW_MUSIC_ONOFF = "showmusic_enable_disable";

    private static final String LANG = "language_enable_disable";

    private static final String VIBRATION = "vibrate_status";

    public static final String TOTAL_SCORE = "total_score";

    public static final String POINT = "no_of_point";

    public static final String LEVEL_COMPLETED = "level_completed";

    public static final String IS_LAST_LEVEL_COMPLETED = "is_last_level_completed";

    public static final String LAST_LEVEL_SCORE = "last_level_score";

    public static final String COUNT_QUESTION_COMPLETED = "count_question_completed";

    public static final String COUNT_RIGHT_ANSWARE_QUESTIONS = "count_right_answare_questions";

    public static void setVibration(Context context, Boolean result) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putBoolean(VIBRATION, result);
        prefEditor.commit();
    }

    public static boolean getVibration(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(VIBRATION, Constant.DEFAULT_VIBRATION_SETTING);
    }

    public static void setSoundEnableDisable(Context context, Boolean result) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putBoolean(SOUND_ONOFF, result);
        prefEditor.commit();
    }

    public static boolean getSoundEnableDisable(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(SOUND_ONOFF, Constant.DEFAULT_SOUND_SETTING);
    }

    public static void setMusicEnableDisable(Context context, Boolean result) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putBoolean(SHOW_MUSIC_ONOFF, result);
        prefEditor.commit();
    }

    public static boolean getMusicEnableDisable(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(SHOW_MUSIC_ONOFF,
                Constant.DEFAULT_MUSIC_SETTING);
    }

    public static void setLan(Context context, Boolean result) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putBoolean(LANG, result);
        prefEditor.commit();
    }


    public static boolean getLan(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(LANG,
                Constant.DEFAULT_LAN_SETTING);
    }

    public static void setScore(Context context, int totalScore) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(TOTAL_SCORE, totalScore);
        prefEditor.commit();
    }


    public static int getScore(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(TOTAL_SCORE, 0);
    }

    public static void setPoint(Context context, int point) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(POINT, point);
        prefEditor.commit();
    }


    public static int getPoint(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(POINT, 6);

    }

    public static void setNoCompletedLevel(Context context, int completedLevel) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(LEVEL_COMPLETED, completedLevel);
        prefEditor.commit();
    }


    public static int getNoCompletedLevel(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(LEVEL_COMPLETED, 1);

    }

    public static void setRightAns(Context context, int NoofrightAns) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(COUNT_RIGHT_ANSWARE_QUESTIONS, NoofrightAns);
        prefEditor.commit();
    }

    public static int getRightAns(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(COUNT_RIGHT_ANSWARE_QUESTIONS, 1);

    }

    public static void setCountQuestionCompleted(Context context, int countquetioncompleted) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putInt(COUNT_QUESTION_COMPLETED, countquetioncompleted);
        prefEditor.commit();
    }

    public static int getCountQuestionCompleted(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(COUNT_QUESTION_COMPLETED, 1);

    }

}
