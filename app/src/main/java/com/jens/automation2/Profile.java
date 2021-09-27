package com.jens.automation2;

import android.content.ContentValues;
import android.content.Context;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.jens.automation2.Action.Action_Enum;

import java.io.File;
import java.util.ArrayList;

public class Profile implements Comparable<Profile>
{
	protected static ArrayList<Profile> profileCollection = new ArrayList<Profile>();
	
	protected String name;
	protected String oldName;
	
    protected boolean changeSoundMode;
    protected int soundMode;

	protected boolean changeDndMode;
	protected int dndMode;

    boolean changeVolumeMusicVideoGameMedia;
    protected int volumeMusic;

    protected boolean changeVolumeNotifications;
    protected int volumeNotifications;

    protected boolean changeVolumeAlarms;
    protected int volumeAlarms;

    protected boolean changeIncomingCallsRingtone;
    protected File incomingCallsRingtone;
    
    protected boolean changeVibrateWhenRinging;
    protected boolean vibrateWhenRinging;
    
    protected boolean changeNotificationRingtone;
    protected File notificationRingtone;
    
    protected boolean changeAudibleSelection;
    protected boolean audibleSelection;

    protected boolean changeScreenLockUnlockSound;
    boolean screenLockUnlockSound;
    
    protected boolean changeHapticFeedback;
    protected boolean hapticFeedback;


	public void setName(String name)
	{
		this.oldName = this.name;
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}

	public void setChangeSoundMode(boolean changeSoundMode)
	{
		this.changeSoundMode = changeSoundMode;
	}
	public boolean getChangeSoundMode()
	{
		return changeSoundMode;
	}

	public void setSoundMode(int soundMode)
	{
		this.soundMode = soundMode;
	}
	public int getSoundMode()
	{
		return soundMode;
	}

	public boolean getChangeDndMode()
	{
		return changeDndMode;
	}

	public void setChangeDndMode(boolean changeDndMode)
	{
		this.changeDndMode = changeDndMode;
	}

	public int getDndMode()
	{
		return dndMode;
	}

	public void setDndMode(int dndMode)
	{
		this.dndMode = dndMode;
	}

	public void setChangeVolumeMusicVideoGameMedia(boolean changeVolumeMusicVideoGameMedia)
	{
		this.changeVolumeMusicVideoGameMedia = changeVolumeMusicVideoGameMedia;
	}
	public boolean getChangeVolumeMusicVideoGameMedia()
	{
		return changeVolumeMusicVideoGameMedia;
	}

	public void setVolumeMusic(int volumeMusic)
	{
		this.volumeMusic = volumeMusic;
	}
	public int getVolumeMusic()
	{
		return volumeMusic;
	}

	public void setChangeVolumeNotifications(boolean changeVolumeRingtoneNotifications)
	{
		this.changeVolumeNotifications = changeVolumeRingtoneNotifications;
	}
	public boolean getChangeVolumeNotifications()
	{
		return changeVolumeNotifications;
	}

	public void setVolumeNotifications(int volumeNotifications)
	{
		this.volumeNotifications = volumeNotifications;
	}
	public int getVolumeNotifications()
	{
		return volumeNotifications;
	}

	public void setChangeVolumeAlarms(boolean changeVolumeAlarms)
	{
		this.changeVolumeAlarms = changeVolumeAlarms;
	}
	public boolean getChangeVolumeAlarms()
	{
		return changeVolumeAlarms;
	}

	public void setVolumeAlarms(int volumeAlarms)
	{
		this.volumeAlarms = volumeAlarms;
	}
	public int getVolumeAlarms()
	{
		return volumeAlarms;
	}

	public void setChangeIncomingCallsRingtone(boolean changeIncomingCallsRingtone)
	{
		this.changeIncomingCallsRingtone = changeIncomingCallsRingtone;
	}
	public boolean getChangeIncomingCallsRingtone()
	{
		return changeIncomingCallsRingtone;
	}

	public void setIncomingCallsRingtone(File incomingCallsRingtone)
	{
		this.incomingCallsRingtone = incomingCallsRingtone;
	}
	public File getIncomingCallsRingtone()
	{
		return incomingCallsRingtone;
	}

	public void setChangeVibrateWhenRinging(boolean changeVibrateWhenRinging)
	{
		this.changeVibrateWhenRinging = changeVibrateWhenRinging;
	}
	public boolean getChangeVibrateWhenRinging()
	{
		return changeVibrateWhenRinging;
	}

	public void setVibrateWhenRinging(boolean vibrateWhenRinging)
	{
		this.vibrateWhenRinging = vibrateWhenRinging;
	}
	public boolean getVibrateWhenRinging()
	{
		return vibrateWhenRinging;
	}

	public void setChangeNotificationRingtone(boolean changeNotificationRingtone)
	{
		this.changeNotificationRingtone = changeNotificationRingtone;
	}
	public boolean getChangeNotificationRingtone()
	{
		return changeNotificationRingtone;
	}

	public void setNotificationRingtone(File notificationsRingtone)
	{
		this.notificationRingtone = notificationsRingtone;
	}
	public File getNotificationRingtone()
	{
		return notificationRingtone;
	}

	public void setChangeAudibleSelection(boolean changeAudibleSelection)
	{
		this.changeAudibleSelection = changeAudibleSelection;
	}
	public boolean getChangeAudibleSelection()
	{
		return changeAudibleSelection;
	}

	public void setAudibleSelection(boolean audibleSelection)
	{
		this.audibleSelection = audibleSelection;
	}
	public boolean getAudibleSelection()
	{
		return audibleSelection;
	}

	public void setChangeScreenLockUnlockSound(boolean changeScreenLockUnlockSound)
	{
		this.changeScreenLockUnlockSound = changeScreenLockUnlockSound;
	}
	public boolean getChangeScreenLockUnlockSound()
	{
		return changeScreenLockUnlockSound;
	}

	public void setScreenLockUnlockSound(boolean screenLockUnlockSound)
	{
		this.screenLockUnlockSound = screenLockUnlockSound;
	}
	public boolean getScreenLockUnlockSound()
	{
		return screenLockUnlockSound;
	}

	public void setChangeHapticFeedback(boolean changeHapticFeedback)
	{
		this.changeHapticFeedback = changeHapticFeedback;
	}
	public boolean getChangeHapticFeedback()
	{
		return changeHapticFeedback;
	}

	public void setHapticFeedback(boolean hapticFeedback)
	{
		this.hapticFeedback = hapticFeedback;
	}
	public boolean getHapticFeedback()
	{
		return hapticFeedback;
	}

	public static ArrayList<Profile> getProfileCollection()
	{
		return profileCollection;
	}
	public static ArrayList<String> getProfileCollectionString()
	{
		ArrayList<String> returnList = new ArrayList<String>();
		for(Profile p : profileCollection)
			returnList.add(p.getName());
		
		return returnList;
	}
	
	public static Profile getByName(String name)
	{
		for(Profile p : Profile.getProfileCollection())
			if(p.getName().equals(name))
				return p;
		
		return null;
	}

	public boolean delete(AutomationService myAutomationService)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	private boolean applyRingTone(File ringtoneFile, int ringtoneType, Context context)
	{
		Miscellaneous.logEvent("i", "Profile", "Request to set ringtone to " + ringtoneFile.getAbsolutePath(), 3);

		if(!ringtoneFile.exists() | !ringtoneFile.canRead())
		{
			String message = "Ringtone file does not exist or cannot read it: " + ringtoneFile.getAbsolutePath();
			Miscellaneous.logEvent("i", "Profile", message, 3);
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			return false;
		}

		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, ringtoneFile.getAbsolutePath());
//		values.put(MediaStore.MediaColumns.TITLE, context.getResources().getString(R.string.app_name) + " ringtone");
//		values.put(MediaStore.MediaColumns.TITLE, ringtoneFile.getName().replace(".mp3", "").replace(".", ""));
		values.put(MediaStore.MediaColumns.TITLE, ringtoneFile.getName());
//		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
		values.put(MediaStore.MediaColumns.SIZE, ringtoneFile.length());
//		values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name);
		values.put(MediaStore.Audio.Media.IS_RINGTONE, ringtoneType == RingtoneManager.TYPE_RINGTONE);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, ringtoneType == RingtoneManager.TYPE_NOTIFICATION);
		values.put(MediaStore.Audio.Media.IS_ALARM, false);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);

		Uri existingRingTone = MediaStore.Audio.Media.getContentUriForPath(ringtoneFile.getAbsolutePath());
		if(existingRingTone != null)
			context.getContentResolver().delete(existingRingTone, MediaStore.MediaColumns.DATA + "=\"" + ringtoneFile.getAbsolutePath() + "\"", null);
		Uri newRingTone = context.getContentResolver().insert(existingRingTone, values);

		try
		{
			RingtoneManager.setActualDefaultRingtoneUri(context, ringtoneType, newRingTone);
			Miscellaneous.logEvent("i", "Profile", "Ringtone set to: " + newRingTone.toString(), 1);
//			Ringtone tone = RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
//			tone.play();
			return true;
		}
		catch (Throwable t)
		{
			String message = "Error setting ringtone: " + Log.getStackTraceString(t);
			Miscellaneous.logEvent("e", "Profile", message, 1);
		}
		
		return false;
	}

	public boolean create(Context context, boolean writeToFile)
	{
		for(Profile profile : Profile.profileCollection)
		{
			if (profile.getName().equals(this.getName()))
			{
				Toast.makeText(context, context.getResources().getString(R.string.anotherProfileByThatName), Toast.LENGTH_LONG).show();
				return false;
			}
		}
		
		if(plausibilityCheck())
		{
			// add to collection
			Profile.getProfileCollection().add(this);
			
			// write to file
			if(writeToFile)
				return XmlFileInterface.writeFile();
		}
		
		return false;
	}

	public boolean change(Context context)
	{		
		if(this.oldName != null && !this.oldName.equals(this.name))
		{
			//Name has changed. We need to look for rules that reference it by its name and update those references
			
			// Check if the name is still available
			int counter = 0;	// this method should only be a temporary workaround, directly editing the referenced object may cause problems until reloading the config file
			for(Profile profile : Profile.profileCollection)
			{
				if (profile.getName().equals(this.getName()))
				{
					counter++;
				}
			}

			if(counter > 1)
			{
				Toast.makeText(context, context.getResources().getString(R.string.anotherProfileByThatName), Toast.LENGTH_LONG).show();
				return false;
			}
			
			// Check if rules reference this profile
			ArrayList<Rule> rulesThatReferenceMe = Rule.findRuleCandidatesByProfile(this);
			if(rulesThatReferenceMe.size() > 0)
			{
				for(Rule oneRule : rulesThatReferenceMe)
				{
					for(Action oneAction : oneRule.getActionSet())
					{
						if(oneAction.getAction() == Action_Enum.changeSoundProfile)
						{
							oneAction.setParameter2(this.name);
							// We don't need to save the file. This will happen anyway in PointOfInterest.writePoisToFile() below.
						}
					}
				}
			}
		}
		
		if(plausibilityCheck())
		{		
			// write to file
			if(XmlFileInterface.writeFile())
			{			
				AutomationService service = AutomationService.getInstance();
				if(service != null)
					service.applySettingsAndRules();
				
				return true;
			}
		}
		
		return false;
	}

	public boolean delete()
	{		
		for(int i = 0; i< Profile.getProfileCollection().size(); i++)
		{
			if(Profile.getProfileCollection().get(i).getName().equals(this.getName()))
			{
				Profile.getProfileCollection().remove(0);
				
				// write to file
				return XmlFileInterface.writeFile();
			}
		}
		
		return false;
	}

	private boolean plausibilityCheck()
	{
		if(this.getName().equals("null"))
		{
			// Invalid name
			String text = Miscellaneous.getAnyContext().getResources().getString(R.string.invalidProfileName);
			Miscellaneous.logEvent("w", "Profile", text, 2);
			Toast.makeText(Miscellaneous.getAnyContext(), text, Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}

	@Override
	public int compareTo(Profile another)
	{
		return this.getName().compareTo(another.getName());
	}
	
	public void activate(Context context)
	{
		Miscellaneous.logEvent("i", "Profile " + this.getName(), String.format(context.getResources().getString(R.string.profileActivate), this.getName()), 3);

		AutomationService.getInstance().checkLockSoundChangesTimeElapsed();

		if(AutomationService.getInstance().getLockSoundChangesEnd() == null)
		{
			try
			{
				AudioManager am = (AudioManager) Miscellaneous.getAnyContext().getSystemService(Context.AUDIO_SERVICE);
				
			    if(changeSoundMode)
			    	Actions.setSound(context, soundMode);

				if(changeDndMode)
					Actions.setDND(context, dndMode);
		
			    if(changeVolumeMusicVideoGameMedia)
			    	am.setStreamVolume(AudioManager.STREAM_MUSIC, volumeMusic, AudioManager.FLAG_PLAY_SOUND);
			    
			    if(changeVolumeNotifications)
			    	am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volumeNotifications, AudioManager.FLAG_PLAY_SOUND);
		
			    if(changeVolumeAlarms)
			    	am.setStreamVolume(AudioManager.STREAM_ALARM, volumeAlarms, AudioManager.FLAG_PLAY_SOUND);
		
			    if(changeIncomingCallsRingtone)
			    	if(incomingCallsRingtone != null)
			    		applyRingTone(incomingCallsRingtone, RingtoneManager.TYPE_RINGTONE, context);
			    
			    if(changeVibrateWhenRinging)
				{
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
					{
						android.provider.Settings.System.putInt(context.getContentResolver(), "vibrate_when_ringing", vibrateWhenRinging?1:0);
					}
					else
					{
						if (vibrateWhenRinging)
							am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
						else
							am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
					}
				}
			    
			    if(changeNotificationRingtone)
			       	if(notificationRingtone != null)
			    		applyRingTone(notificationRingtone, RingtoneManager.TYPE_NOTIFICATION, context);

				if(changeScreenLockUnlockSound)
				{
					android.provider.Settings.System.putInt(context.getContentResolver(), "lockscreen_sounds_enabled" , screenLockUnlockSound ? 1 : 0);
				}

			    if(changeAudibleSelection)
			    {
			    	if(audibleSelection)
			    		android.provider.Settings.System.putInt(context.getContentResolver(), android.provider.Settings.System.SOUND_EFFECTS_ENABLED, 1); // enable
			    	else
			    		android.provider.Settings.System.putInt(context.getContentResolver(), android.provider.Settings.System.SOUND_EFFECTS_ENABLED, 0); // dissable
			    }

			    if(changeHapticFeedback)
			    {
			    	if(hapticFeedback)
			    		android.provider.Settings.System.putInt(context.getContentResolver(), android.provider.Settings.System.HAPTIC_FEEDBACK_ENABLED, 1); // enable
			    	else
			    		android.provider.Settings.System.putInt(context.getContentResolver(), android.provider.Settings.System.HAPTIC_FEEDBACK_ENABLED, 0); // disable
			    }
			}
			catch(Exception e)
			{
				Miscellaneous.logEvent("e", "Profile " + this.getName(), context.getResources().getString(R.string.errorActivatingProfile) + " " + Log.getStackTraceString(e), 1);
			}
		}
		else
		{
			Miscellaneous.logEvent("i", "Profile " + this.getName(), context.getResources().getString(R.string.noProfileChangeSoundLocked), 3);
		}
	}

	@Override
	public String toString()
	{
		return this.getName();
	}

	public String toStringLong()
	{
		return "no implemented, yet";
	}

	public static boolean createDummyProfile(Context context, String tagContent)
	{
		Profile newProfile = new Profile();
		
		newProfile.setName(tagContent);
		newProfile.setChangeSoundMode(true);
		
		if(tagContent.equals("silent"))
			newProfile.setSoundMode(AudioManager.RINGER_MODE_SILENT);
		else if(tagContent.equals("vibrate"))
			newProfile.setSoundMode(AudioManager.RINGER_MODE_VIBRATE);
		else if(tagContent.equals("normal"))
			newProfile.setSoundMode(AudioManager.RINGER_MODE_NORMAL);
		else
			return false;
		
		return newProfile.create(context, false);
	}

	public String getOldName()
	{
		return this.oldName;
	}

}
