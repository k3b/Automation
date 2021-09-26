package com.jens.automation2;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.File;

public class ActivityManageProfile extends Activity
{
	private static ProgressDialog progressDialog;
	final static int intentCodeRingtonePickerCallsFile = 9010;
	final static int intentCodeRingtonePickerCallsRingtone = 9011;
	final static int intentCodeRingtonePickerNotificationsFile = 9020;
	final static int intentCodeRingtonePickerNotificationsRingtone = 9021;
	
	CheckBox checkBoxChangeSoundMode, checkBoxChangeVolumeMusicVideoGameMedia, checkBoxChangeVolumeNotifications, checkBoxChangeVolumeAlarms, checkBoxChangeIncomingCallsRingtone, checkBoxChangeNotificationRingtone, checkBoxChangeAudibleSelection, checkBoxChangeScreenLockUnlockSound, checkBoxChangeHapticFeedback, checkBoxChangeVibrateWhenRinging, checkBoxVibrateWhenRinging, checkBoxAudibleSelection, checkBoxScreenLockUnlockSound, checkBoxHapticFeedback, checkBoxChangeDnd;
	Spinner spinnerSoundMode, spinnerDndMode;
	SeekBar seekBarVolumeMusic, seekBarVolumeNotifications, seekBarVolumeAlarms;		
	Button bChangeSoundIncomingCalls, bChangeSoundNotifications, bSaveProfile;
	TextView tvIncomingCallsRingtone, tvNotificationsRingtone;
	EditText etName;
	
	File incomingCallsRingtone = null, notificationsRingtone = null;
	
	ArrayAdapter<String> soundModeAdapter;
	ArrayAdapter<String> dndModeAdapter;

	public void setIncomingCallsRingtone(File incomingCallsRingtone)
	{
		this.incomingCallsRingtone = incomingCallsRingtone;
		
		if(incomingCallsRingtone != null)
			tvIncomingCallsRingtone.setText(this.incomingCallsRingtone.getAbsolutePath());
		else
			tvIncomingCallsRingtone.setText(getResources().getString(R.string.none));
	}

	public File getIncomingCallsRingtone()
	{
		return incomingCallsRingtone;
	}

	public void setNotificationsRingtone(File notificationsRingtone)
	{		
		this.notificationsRingtone = notificationsRingtone;
		
		if(this.notificationsRingtone != null)
			tvNotificationsRingtone.setText(this.notificationsRingtone.getAbsolutePath());
		else
			tvNotificationsRingtone.setText(getResources().getString(R.string.none));
	}

	public File getNotificationsRingtone()
	{
		return notificationsRingtone;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_manage_specific_profile);
		
		checkBoxChangeSoundMode = (CheckBox)findViewById(R.id.checkBoxChangeSoundMode);
		checkBoxChangeDnd = (CheckBox)findViewById(R.id.checkBoxChangeDnd);
		checkBoxChangeVolumeMusicVideoGameMedia = (CheckBox)findViewById(R.id.checkBoxChangeVolumeMusicVideoGameMedia);
		checkBoxChangeVolumeNotifications = (CheckBox)findViewById(R.id.checkBoxChangeVolumeNotifications);
		checkBoxChangeVolumeAlarms = (CheckBox)findViewById(R.id.checkBoxChangeVolumeAlarms);
		checkBoxChangeIncomingCallsRingtone = (CheckBox)findViewById(R.id.checkBoxChangeIncomingCallsRingtone);
		checkBoxChangeNotificationRingtone = (CheckBox)findViewById(R.id.checkBoxChangeNotificationRingtone);
		checkBoxChangeAudibleSelection = (CheckBox)findViewById(R.id.checkBoxChangeAudibleSelection);
		checkBoxChangeScreenLockUnlockSound = (CheckBox)findViewById(R.id.checkBoxChangeScreenLockUnlockSound);
		checkBoxChangeHapticFeedback = (CheckBox)findViewById(R.id.checkBoxChangeHapticFeedback);
		checkBoxChangeVibrateWhenRinging = (CheckBox)findViewById(R.id.checkBoxChangeVibrateWhenRinging);
		checkBoxAudibleSelection = (CheckBox)findViewById(R.id.checkBoxAudibleSelection);
		checkBoxScreenLockUnlockSound = (CheckBox)findViewById(R.id.checkBoxScreenLockUnlockSound);
		checkBoxHapticFeedback = (CheckBox)findViewById(R.id.checkBoxHapticFeedback);
		checkBoxVibrateWhenRinging = (CheckBox)findViewById(R.id.checkBoxVibrateWhenRinging);
		spinnerSoundMode = (Spinner)findViewById(R.id.spinnerSoundMode);
		spinnerDndMode = (Spinner)findViewById(R.id.spinnerDndMode);
		seekBarVolumeMusic = (SeekBar)findViewById(R.id.seekBarVolumeMusic);
		seekBarVolumeNotifications = (SeekBar)findViewById(R.id.seekBarVolumeNotifications);
		seekBarVolumeAlarms = (SeekBar)findViewById(R.id.seekBarVolumeAlarms);
		bChangeSoundIncomingCalls = (Button)findViewById(R.id.bChangeSoundIncomingCalls);
		bChangeSoundNotifications = (Button)findViewById(R.id.bChangeSoundNotifications);
		tvIncomingCallsRingtone = (TextView)findViewById(R.id.tvIncomingCallsRingtone);
		tvNotificationsRingtone = (TextView)findViewById(R.id.tvNotificationsRingtone);
		bSaveProfile = (Button)findViewById(R.id.bSaveProfile);
		etName = (EditText)findViewById(R.id.etName);
		
		checkBoxVibrateWhenRinging.setEnabled(false);
		checkBoxAudibleSelection.setEnabled(false);
		checkBoxScreenLockUnlockSound.setEnabled(false);
		checkBoxHapticFeedback.setEnabled(false);
		spinnerSoundMode.setEnabled(false);
		spinnerDndMode.setEnabled(false);
		seekBarVolumeMusic.setEnabled(false);
		seekBarVolumeNotifications.setEnabled(false);
		seekBarVolumeAlarms.setEnabled(false);
		bChangeSoundIncomingCalls.setEnabled(false);
		bChangeSoundNotifications.setEnabled(false);
		
		spinnerSoundMode.setSelection(0);
		spinnerDndMode.setSelection(0);

		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
		{
			// Disable DND controls
			checkBoxChangeDnd.setEnabled(false);
			spinnerDndMode.setEnabled(false);
		}
		
		// Scale SeekBars to the system's maximum volume values
		AudioManager am = (AudioManager) Miscellaneous.getAnyContext().getSystemService(Context.AUDIO_SERVICE);
		seekBarVolumeMusic.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		seekBarVolumeNotifications.setMax(am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
		seekBarVolumeAlarms.setMax(am.getStreamMaxVolume(AudioManager.STREAM_ALARM));
		
		soundModeAdapter = new ArrayAdapter<String>(this, R.layout.text_view_for_poi_listview_mediumtextsize, new String[]
																				{
																						getResources().getString(R.string.soundModeSilent),
																						getResources().getString(R.string.soundModeVibrate),
																						getResources().getString(R.string.soundModeNormal)
																				});
		spinnerSoundMode.setAdapter(soundModeAdapter);

		dndModeAdapter = new ArrayAdapter<String>(this, R.layout.text_view_for_poi_listview_mediumtextsize, new String[]
																				{
																						getResources().getString(R.string.dndOff),
																						getResources().getString(R.string.dndPriority),
																						getResources().getString(R.string.dndNothing),
																						getResources().getString(R.string.dndAlarms)
																				});
		spinnerDndMode.setAdapter(dndModeAdapter);
		/*
			Order in spinner: 1, 2, 4, 3
			NotificationManager.INTERRUPTION_FILTER_UNKNOWN	-> Returned when the value is unavailable for any reason.
			NotificationManager.INTERRUPTION_FILTER_ALL -> 1 -> Normal interruption filter - no notifications are suppressed. -> essentially turn off DND
			NotificationManager.INTERRUPTION_FILTER_PRIORITY -> 2 ->  Priority interruption filter - all notifications are suppressed except those that match the priority criteria.
			NotificationManager.INTERRUPTION_FILTER_ALARMS -> 4 -> Alarms only interruption filter - all notifications except those of category
			NotificationManager.INTERRUPTION_FILTER_NONE -> 3 -> No interruptions filter - all notifications are suppressed and all audio streams (except those used for phone calls) and vibrations are muted.
		*/
		
		checkBoxChangeSoundMode.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				spinnerSoundMode.setEnabled(isChecked);
			}
		});
		checkBoxChangeDnd.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				spinnerDndMode.setEnabled(isChecked);
			}
		});
		checkBoxChangeVolumeMusicVideoGameMedia.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				seekBarVolumeMusic.setEnabled(isChecked);
			}
		});
		checkBoxChangeVolumeNotifications.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				seekBarVolumeNotifications.setEnabled(isChecked);
			}
		});
		checkBoxChangeVolumeAlarms.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				seekBarVolumeAlarms.setEnabled(isChecked);
			}
		});
		checkBoxChangeIncomingCallsRingtone.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				bChangeSoundIncomingCalls.setEnabled(isChecked);
			}
		});
		checkBoxChangeNotificationRingtone.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				bChangeSoundNotifications.setEnabled(isChecked);
			}
		});
		checkBoxChangeAudibleSelection.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				checkBoxAudibleSelection.setEnabled(isChecked);
			}
		});
		checkBoxChangeScreenLockUnlockSound.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				checkBoxScreenLockUnlockSound.setEnabled(isChecked);

				if(isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
					Miscellaneous.messageBox("Info", getResources().getString(R.string.screenLockSoundNotice), ActivityManageProfile.this).show();
			}
		});
		checkBoxChangeHapticFeedback.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				checkBoxHapticFeedback.setEnabled(isChecked);				
			}
		});
		checkBoxChangeVibrateWhenRinging.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				checkBoxVibrateWhenRinging.setEnabled(isChecked);				
			}
		});
		
		bSaveProfile.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				try
				{
					if(ActivityMainProfiles.profileToEdit == null)
						createProfile(ActivityManageProfile.this);
					else
						changeProfile();
				}
				catch(Exception ex)
				{
					Toast.makeText(ActivityManageProfile.this, getResources().getString(R.string.errorWritingFile) + " " + ex.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
		
		bChangeSoundIncomingCalls.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				try
				{
					Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
					fileIntent.setType("audio/*");
					startActivityForResult(Intent.createChooser(fileIntent, "Select a ringtone"), intentCodeRingtonePickerCallsFile);
				}
				catch(ActivityNotFoundException e)
				{
					// Use media browser instead
					Intent fileSelectionIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);

					if(ActivityMainProfiles.profileToEdit != null)
					{
						Uri currenturi = Uri.parse(ActivityMainProfiles.profileToEdit.incomingCallsRingtone.getAbsolutePath());
						if(ActivityMainProfiles.profileToEdit.changeIncomingCallsRingtone)
							fileSelectionIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currenturi);
					}

					startActivityForResult(fileSelectionIntent, intentCodeRingtonePickerCallsRingtone);
				}
			}
		});
		bChangeSoundNotifications.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				try
				{
					Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
					fileIntent.setType("audio/*");
					startActivityForResult(Intent.createChooser(fileIntent, "Select a ringtone"), intentCodeRingtonePickerNotificationsFile);
				}
				catch(ActivityNotFoundException e)
				{
					// Use media browser instead
					Intent fileSelectionIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);

					if(ActivityMainProfiles.profileToEdit != null)
					{
						Uri currenturi = Uri.parse(ActivityMainProfiles.profileToEdit.notificationRingtone.getAbsolutePath());
						if(ActivityMainProfiles.profileToEdit.changeNotificationRingtone)
							fileSelectionIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currenturi);
					}

					startActivityForResult(fileSelectionIntent, intentCodeRingtonePickerNotificationsRingtone);
				}
			}
		});
		
		if(ActivityMainProfiles.profileToEdit != null)
			editProfile(ActivityMainProfiles.profileToEdit);
		//else
		//	new Profile to be created
		
//		Toast.makeText(this, getResources().getString(R.string.someOptionsNotAvailableYet), Toast.LENGTH_LONG).show();
	}
	
	private void createProfile(Context context)
	{
		if(plausibilityCheck())
		{
			if(loadFormValuesToVariable())
				if(ActivityMainProfiles.profileToEdit.create(context, true))
				{
					this.setResult(RESULT_OK);
					finish();
				}			
		}
	}
	private void changeProfile()
	{
		if(plausibilityCheck())
		{
			loadFormValuesToVariable();
			
			if(ActivityMainProfiles.profileToEdit.change(this))
			{
				this.setResult(RESULT_OK);
				finish();
			}
		}
	}
	
	public void editProfile(Profile profileToEdit)
	{
		etName.setText(ActivityMainProfiles.profileToEdit.getName());
		checkBoxChangeSoundMode.setChecked(ActivityMainProfiles.profileToEdit.getChangeSoundMode());
		checkBoxChangeDnd.setChecked(ActivityMainProfiles.profileToEdit.getChangeDndMode());
		checkBoxChangeVolumeMusicVideoGameMedia.setChecked(ActivityMainProfiles.profileToEdit.getChangeVolumeMusicVideoGameMedia());
		checkBoxChangeVolumeNotifications.setChecked(ActivityMainProfiles.profileToEdit.getChangeVolumeNotifications());
		checkBoxChangeVolumeAlarms.setChecked(ActivityMainProfiles.profileToEdit.getChangeVolumeAlarms());
		checkBoxChangeIncomingCallsRingtone.setChecked(ActivityMainProfiles.profileToEdit.getChangeIncomingCallsRingtone());
		checkBoxChangeNotificationRingtone.setChecked(ActivityMainProfiles.profileToEdit.getChangeNotificationRingtone());
		checkBoxChangeAudibleSelection.setChecked(ActivityMainProfiles.profileToEdit.getChangeAudibleSelection());
		checkBoxChangeScreenLockUnlockSound.setChecked(ActivityMainProfiles.profileToEdit.getChangeScreenLockUnlockSound());
		checkBoxChangeHapticFeedback.setChecked(ActivityMainProfiles.profileToEdit.getChangeHapticFeedback());
		checkBoxChangeVibrateWhenRinging.setChecked(ActivityMainProfiles.profileToEdit.getChangeVibrateWhenRinging());
		
		spinnerSoundMode.setSelection(ActivityMainProfiles.profileToEdit.getSoundMode());
		spinnerDndMode.setSelection(ActivityMainProfiles.profileToEdit.getDndMode()-1);
		seekBarVolumeMusic.setProgress(ActivityMainProfiles.profileToEdit.getVolumeMusic());
		seekBarVolumeNotifications.setProgress(ActivityMainProfiles.profileToEdit.getVolumeNotifications());
		seekBarVolumeAlarms.setProgress(ActivityMainProfiles.profileToEdit.getVolumeAlarms());
		checkBoxAudibleSelection.setChecked(ActivityMainProfiles.profileToEdit.audibleSelection);
		checkBoxScreenLockUnlockSound.setChecked(ActivityMainProfiles.profileToEdit.screenLockUnlockSound);
		checkBoxHapticFeedback.setChecked(ActivityMainProfiles.profileToEdit.hapticFeedback);
		checkBoxVibrateWhenRinging.setChecked(ActivityMainProfiles.profileToEdit.vibrateWhenRinging);
		
		setIncomingCallsRingtone(ActivityMainProfiles.profileToEdit.getIncomingCallsRingtone());		
		setNotificationsRingtone(ActivityMainProfiles.profileToEdit.getNotificationRingtone());
	}
	
	private boolean loadFormValuesToVariable()
	{
		if(plausibilityCheck())
		{			
			if(ActivityMainProfiles.profileToEdit == null)
				ActivityMainProfiles.profileToEdit = new Profile();
			
			ActivityMainProfiles.profileToEdit.setName(etName.getText().toString());
			ActivityMainProfiles.profileToEdit.setChangeSoundMode(checkBoxChangeSoundMode.isChecked());
			ActivityMainProfiles.profileToEdit.setChangeDndMode(checkBoxChangeDnd.isChecked());
			ActivityMainProfiles.profileToEdit.setChangeVolumeMusicVideoGameMedia(checkBoxChangeVolumeMusicVideoGameMedia.isChecked());
			ActivityMainProfiles.profileToEdit.setChangeVolumeNotifications(checkBoxChangeVolumeNotifications.isChecked());
			ActivityMainProfiles.profileToEdit.setChangeVolumeAlarms(checkBoxChangeVolumeAlarms.isChecked());
			ActivityMainProfiles.profileToEdit.setChangeIncomingCallsRingtone(checkBoxChangeIncomingCallsRingtone.isChecked());
			ActivityMainProfiles.profileToEdit.setChangeNotificationRingtone(checkBoxChangeNotificationRingtone.isChecked());
			ActivityMainProfiles.profileToEdit.setChangeAudibleSelection(checkBoxChangeAudibleSelection.isChecked());
			ActivityMainProfiles.profileToEdit.setChangeScreenLockUnlockSound(checkBoxChangeScreenLockUnlockSound.isChecked());
			ActivityMainProfiles.profileToEdit.setChangeHapticFeedback(checkBoxChangeHapticFeedback.isChecked());
			ActivityMainProfiles.profileToEdit.setChangeVibrateWhenRinging(checkBoxChangeVibrateWhenRinging.isChecked());
			
			ActivityMainProfiles.profileToEdit.setAudibleSelection(checkBoxAudibleSelection.isChecked());
			ActivityMainProfiles.profileToEdit.setScreenLockUnlockSound(checkBoxScreenLockUnlockSound.isChecked());
			ActivityMainProfiles.profileToEdit.setHapticFeedback(checkBoxHapticFeedback.isChecked());
			ActivityMainProfiles.profileToEdit.setVibrateWhenRinging(checkBoxVibrateWhenRinging.isChecked());
			ActivityMainProfiles.profileToEdit.setSoundMode(spinnerSoundMode.getSelectedItemPosition());
			ActivityMainProfiles.profileToEdit.setDndMode(spinnerDndMode.getSelectedItemPosition()+1);
			ActivityMainProfiles.profileToEdit.setVolumeMusic(seekBarVolumeMusic.getProgress());
			ActivityMainProfiles.profileToEdit.setVolumeNotifications(seekBarVolumeNotifications.getProgress());
			ActivityMainProfiles.profileToEdit.setVolumeAlarms(seekBarVolumeAlarms.getProgress());
			ActivityMainProfiles.profileToEdit.setIncomingCallsRingtone(incomingCallsRingtone);
			ActivityMainProfiles.profileToEdit.setNotificationRingtone(notificationsRingtone);
			
			return true;
		}
		
		return false;
	}
	
	private boolean plausibilityCheck()
	{
		if(etName.getText().toString().length() > 0)
		{
//			Check for duplicates
//			for(Profile.)
//			if(etName.getText().toString()
		}
		else
		{
			Toast.makeText(this, getResources().getString(R.string.enterAname), Toast.LENGTH_LONG).show();
			return false;
		}
		
		if(!checkBoxChangeSoundMode.isChecked()
				&&
			!checkBoxChangeDnd.isChecked()
				&&
			!checkBoxChangeVolumeMusicVideoGameMedia.isChecked()
				&&
			!checkBoxChangeVolumeNotifications.isChecked()
				&&
			!checkBoxChangeVolumeAlarms.isChecked()
				&&
			!checkBoxChangeIncomingCallsRingtone.isChecked()
				&&
			!checkBoxChangeNotificationRingtone.isChecked()
				&&
			!checkBoxChangeAudibleSelection.isChecked()
				&&
			!checkBoxChangeScreenLockUnlockSound.isChecked()
				&&
			!checkBoxChangeHapticFeedback.isChecked()
			)
		{
			Toast.makeText(this, getResources().getString(R.string.noChangeSelectedProfileDoesntMakeSense), Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode == RESULT_OK)
		{
			switch (requestCode)
			{
				case intentCodeRingtonePickerCallsRingtone:    // incoming calls
				{
					// Method for ringtone selection
					Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
					if (uri != null)
					{
						String ringTonePath = CompensateCrappyAndroidPaths.getPath(ActivityManageProfile.this, uri);
						setIncomingCallsRingtone(new File(ringTonePath));
					}
					break;
				}
				case intentCodeRingtonePickerCallsFile:
				{
					String ringTonePath = CompensateCrappyAndroidPaths.getPath(ActivityManageProfile.this, data.getData());
					setIncomingCallsRingtone(new File(ringTonePath));
					break;
				}
				case intentCodeRingtonePickerNotificationsRingtone:    // notifications
				{
					Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
					if (uri != null)
					{
						String ringTonePath = CompensateCrappyAndroidPaths.getPath(ActivityManageProfile.this, data.getData());
						setNotificationsRingtone(new File(ringTonePath));
					}
					break;
				}
				case intentCodeRingtonePickerNotificationsFile:
				{
					String ringTonePath = CompensateCrappyAndroidPaths.getPath(ActivityManageProfile.this, data.getData());
					setNotificationsRingtone(new File(ringTonePath));
					break;
				}
				default:
					;
			}
		}
	}

	public String getRealPathFromURI(Uri uri)
	{
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		if (cursor == null) return null;
		int column_index =             cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String s=cursor.getString(column_index);
		cursor.close();
		return s;
	}
}