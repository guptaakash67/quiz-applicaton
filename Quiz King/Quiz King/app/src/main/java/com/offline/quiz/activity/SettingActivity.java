package com.offline.quiz.activity;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.offline.quiz.R;
import com.offline.quiz.AppController;
import com.offline.quiz.helper.SettingsPreferences;

import static com.offline.quiz.AppController.StopSound;
import static com.offline.quiz.AppController.getAppContext;

public class SettingActivity extends AppCompatActivity {
	private Context mContext;
	private Dialog mCustomDialog;
	private SwitchCompat mSoundCheckBox,mVibrationCheckBox,mMusicCheckBox;
	private TextView ok_btn;
	private boolean isSoundOn;
	private boolean isVibrationOn;
	private boolean isMusicOn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		setContentView(R.layout.setting);
		
		mContext = SettingActivity.this;
		AppController.currentActivity = this;
		initViews();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	private void initViews() {
		mSoundCheckBox = (SwitchCompat) findViewById(R.id.sound_checkbox);
		mVibrationCheckBox = (SwitchCompat) findViewById(R.id.vibration_checkbox);
		mMusicCheckBox = (SwitchCompat) findViewById(R.id.show_music_checkbox);
		ok_btn=(TextView) findViewById(R.id.ok);
		populateSoundContents();
		populateVibrationContents();
		populateMusicEnableContents();
		ok_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
				overridePendingTransition(R.anim.open_next, R.anim.close_next);
			}
		});
	}
	
	private void moreAppClicked() {
		try {
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://search?q=pub:Amazing%20Facts&hl=en	")));
		} catch (ActivityNotFoundException anfe) {
			startActivity(new Intent(
					Intent.ACTION_VIEW,
					Uri.parse(String
							.format("https://play.google.com/store/apps/developer?id=%s&hl=en",
									"Amazing%20Facts&hl=en"))));
		}
	}

	private void switchSoundCheckbox() {
		isSoundOn = !isSoundOn;
		SettingsPreferences.setSoundEnableDisable(mContext, isSoundOn);
		populateSoundContents();
	}

	private void switchVibrationCheckbox() {
		isVibrationOn = !isVibrationOn;
		SettingsPreferences.setVibration(mContext, isVibrationOn);
		populateVibrationContents();
	}
	private void switchMusicEnableCheckbox() {
		isMusicOn = !isMusicOn;
		if (isMusicOn) {
			SettingsPreferences.setMusicEnableDisable(mContext, true);
			AppController.playSound();
			
		} else {
			SettingsPreferences.setMusicEnableDisable(mContext, false);
			StopSound();
		}
		populateMusicEnableContents();
	}
	
	protected void populateSoundContents() {
		if (SettingsPreferences.getSoundEnableDisable(mContext)) {
			mSoundCheckBox.setChecked(true);
		} else {
			mSoundCheckBox.setChecked(false);
		}
		isSoundOn = SettingsPreferences.getSoundEnableDisable(mContext);
	}
	protected void populateVibrationContents() {
		if (SettingsPreferences.getVibration(mContext)) {
			mVibrationCheckBox.setChecked(true);
		} else {
			mVibrationCheckBox.setChecked(false);
		}
		isVibrationOn = SettingsPreferences.getVibration(mContext);
	}
	protected void populateMusicEnableContents() {
		if (SettingsPreferences.getMusicEnableDisable(mContext)) {
			AppController.playSound();
			mMusicCheckBox.setChecked(true);
		} else {
			StopSound();
			mMusicCheckBox.setChecked(false);
		}
		isMusicOn = SettingsPreferences.getMusicEnableDisable(mContext);
	}
	private void updateClicked() {
		try {
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id="
							+ mContext.getPackageName())));
		} catch (ActivityNotFoundException anfe) {
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://play.google.com/store/apps/details?id="
							+ mContext.getPackageName())));
		}
	}
	private void shareClicked(String subject, String text) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(android.content.Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id="+getPackageName());
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		
		startActivity(Intent.createChooser(intent,
				getString(R.string.share_via)));
	}
	public void viewClickHandler(View view) {
		switch (view.getId()) {
		case R.id.share_layout:
			shareClicked(getString(R.string.share_subject),
					AppController.getAppUrl());
			break;
		case R.id.sound_layout:
			switchSoundCheckbox();
			break;
		case R.id.sound_checkbox:
			switchSoundCheckbox();
			break;
		case R.id.vibration_layout:
			switchVibrationCheckbox();
			break;
		case R.id.vibration_checkbox:
			switchVibrationCheckbox();
			break;
		case R.id.show_hint_layout:
			switchMusicEnableCheckbox();
			break;
		case R.id.show_music_checkbox:
			String[] LOCATION_PERMS = {
					android.Manifest.permission.READ_PHONE_STATE
			};

			if (ContextCompat.checkSelfPermission(SettingActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(SettingActivity.this, LOCATION_PERMS, 0);
			}else {

				setTelephoneListener();

			}

			switchMusicEnableCheckbox();
			break;
		case R.id.moreapp_layout:
			moreAppClicked();
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
			break;
		case R.id.rate_layout:
			updateClicked();
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
			break;
		case R.id.ok:
			onBackPressed();
			break;
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case 0:

				setTelephoneListener();

				break;
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
	public void onBackPressed() {
		// TODO Auto-generated method stub
		overridePendingTransition(R.anim.close_next, R.anim.open_next);
		super.onBackPressed();
	}

	@Override
	public void onDestroy() {

		if (mContext != null) {
			if (mCustomDialog != null) {
				mCustomDialog.dismiss();
				mCustomDialog = null;
			}
			mVibrationCheckBox = null;
			mMusicCheckBox = null;
			mSoundCheckBox = null;
			mContext = null;
			super.onDestroy();
		}
	}


}
