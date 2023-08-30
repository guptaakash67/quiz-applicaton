package com.offline.quiz;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import androidx.multidex.MultiDex;
import androidx.appcompat.app.AppCompatDelegate;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.offline.quiz.helper.SettingsPreferences;


public class AppController extends Application {

	private static Context mContext;
	private static String mAppUrl;
	public static MediaPlayer player;
	public static Activity currentActivity;
	@Override
	public void onCreate() {
		super.onCreate();
		setContext(getApplicationContext());
		mAppUrl = Constant.PLAYSTORE_URL + mContext.getPackageName();
		setTelephoneListener();
		player = new MediaPlayer();
		mediaPlayerInitializer();
		//AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}

	public static void mediaPlayerInitializer(){
		try {
			player = MediaPlayer.create(getAppContext(), R.raw.snd_bg);
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setLooping(true);
			player.setVolume(1f, 1f);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public static String getAppUrl() {
		return mAppUrl;
	}
	private static void setContext(Context context) {
		mContext = context;
	}
	public static Context getAppContext() {
		return mContext;
	}

	
	public static void playSound() 
	{
		try {
			if (SettingsPreferences.getMusicEnableDisable(mContext)&&!player.isPlaying()) {
				player.start();
			}else{
			}

		} catch (IllegalStateException e) {
			e.printStackTrace();
			mediaPlayerInitializer();
			player.start();
		}
	}
	public static void StopSound() {
		if (player.isPlaying()) {
			player.pause();
		}

	}
	
	private void setTelephoneListener() {
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                	StopSound();
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                	StopSound();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };

        TelephonyManager telephoneManager = (TelephonyManager) getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (telephoneManager != null) {
            telephoneManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
	static
	{
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}
}
