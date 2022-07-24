package com.jens.automation2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.jens.automation2.Trigger.Trigger_Enum;
import com.jens.automation2.location.LocationProvider;
import com.jens.automation2.receivers.DateTimeListener;
import com.jens.automation2.receivers.PackageReplacedReceiver;
import com.jens.automation2.receivers.PhoneStatusListener;

import java.util.Calendar;
import java.util.Set;

@SuppressLint("NewApi")
public class AutomationService extends Service implements OnInitListener
{
	protected TextToSpeech ttsEngine = null;
	protected final static int notificationId = 1000;
	protected final static int notificationIdRestrictions = 1005;
	protected final static int notificationIdLocationRestriction = 1006;

	public static final String flavor_name_apk = "apkFlavor";
	public static final String flavor_name_fdroid = "fdroidFlavor";
	public static final String flavor_name_googleplay = "googlePlayFlavor";

	final static String NOTIFICATION_CHANNEL_ID_SERVICE = "com.jens.automation2_service";
	final static String NOTIFICATION_CHANNEL_NAME_SERVICE = "Service notification";

	final static String NOTIFICATION_CHANNEL_ID_FUNCTIONALITY = "com.jens.automation2_functionality";
	final static String NOTIFICATION_CHANNEL_NAME_FUNCTIONALITY = "Functionality information";

	final static String NOTIFICATION_CHANNEL_ID_RULES = "com.jens.automation2_rules";
	final static String NOTIFICATION_CHANNEL_NAME_RULES = "Rule notifications";

	protected static Notification myNotification;
	protected static NotificationCompat.Builder notificationBuilder = null;
	protected static PendingIntent myPendingIntent;

	protected Calendar lockSoundChangesEnd = null;
    protected boolean isRunning;

	protected static AutomationService centralInstance = null;

    public void nullLockSoundChangesEnd()
	{
		lockSoundChangesEnd = null;
	}
    public Calendar getLockSoundChangesEnd()
	{
		return lockSoundChangesEnd;
	}
	public void lockSoundChangesEndAddTime()
	{
		if(lockSoundChangesEnd == null)
			lockSoundChangesEnd = Calendar.getInstance();

		lockSoundChangesEnd.add(Calendar.MINUTE, Settings.lockSoundChangesInterval);
	}

	public void checkLockSoundChangesTimeElapsed()
	{
		Calendar now = Calendar.getInstance();
		if(getLockSoundChangesEnd() != null && getLockSoundChangesEnd().getTimeInMillis() <= now.getTimeInMillis())
			lockSoundChangesEnd = null;
	}

	public void setLockSoundChangesEnd(Calendar lockSoundChangesEnd)
	{
		this.lockSoundChangesEnd = lockSoundChangesEnd;
	}

	protected final IBinder myBinder = new LocalBinder();

	protected LocationProvider myLocationProvider;

	public LocationProvider getLocationProvider()
	{
		return myLocationProvider;
	}

	public static AutomationService getInstance()
	{
		return centralInstance;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		Thread.setDefaultUncaughtExceptionHandler(Miscellaneous.uncaughtExceptionHandler);

		// Store a reference to myself. Other classes often need a context or something, this can provide that.
		centralInstance = this;
	}

	public boolean checkStartupRequirements(Context context, boolean startAtBoot)
	{
		if(Build.VERSION.SDK_INT >= 28)
		{
			if (!ActivityPermissions.havePermission(Manifest.permission.FOREGROUND_SERVICE, AutomationService.this))
			{
			/*
				Don't have permission to start service. This is a show stopper.
			 */
				Miscellaneous.logEvent("e", "Permission", "Don't have permission to start foreground service. Will request it now.", 4);
//			Toast.makeText(AutomationService.this, getResources().getString(R.string.appRequiresPermissiontoAccessExternalStorage), Toast.LENGTH_LONG).show();
				ActivityPermissions.requestSpecificPermission(Manifest.permission.FOREGROUND_SERVICE);
				return false;
			}
		}

		if (
				PointOfInterest.getPointOfInterestCollection() == null
							||
				PointOfInterest.getPointOfInterestCollection().size() == 0
							||
				Rule.getRuleCollection() == null
							||
				Rule.getRuleCollection().size() == 0
			)
		{
			if (startAtBoot)
			{
				/*
				 * In case we start at boot the sd card may not have been mounted, yet.
				 * We will wait 3 seconds and check and do this 3 times.
				 */
				if (!XmlFileInterface.settingsFile.exists())
				{
					for (int i = 0; i < 3; i++)
					{
						String state = Environment.getExternalStorageState();
						if (!state.equals(Environment.MEDIA_MOUNTED))
						{
							try
							{
								Miscellaneous.logEvent("w", "AutoStart", "Service is started via boot. Settingsfile not available because storage is not mounted, yet. Waiting for 3 seconds.", 4);
								Thread.sleep(3000);
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
						if (XmlFileInterface.settingsFile.exists())
							break;
					}
				}
			}
			PointOfInterest.loadPoisFromFile();
			Rule.readFromFile();
		}

		//if still no POIs...
		if (Rule.getRuleCollection() == null || Rule.getRuleCollection().size() == 0)
		{
			Miscellaneous.logEvent("w", "AutomationService", context.getResources().getString(R.string.serviceWontStart), 1);
			Toast.makeText(context, context.getResources().getString(R.string.serviceWontStart), Toast.LENGTH_LONG).show();
			return false;
		}
		else
		{
			return true;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		boolean startAtBoot = false;

		if (intent != null)
		{
			Bundle b = intent.getExtras();
			startAtBoot = b.getBoolean("startAtBoot", false);

			if(startAtBoot)
				Settings.deviceStartDone = false;
		}

		if (checkStartupRequirements(this, startAtBoot))
		{
			Miscellaneous.logEvent("i", "Service", this.getResources().getString(R.string.logServiceStarting) + " VERSION_CODE: " + BuildConfig.VERSION_CODE + ", VERSION_NAME: " + BuildConfig.VERSION_NAME + ", flavor: " + BuildConfig.FLAVOR, 1);
			Miscellaneous.logEvent("i", "Service", ActivityControlCenter.getSystemInfo(), 1);

			startUpRoutine();

			Intent myIntent = new Intent(this, ActivityMainTabLayout.class);
			myPendingIntent = PendingIntent.getActivity(this, 0, myIntent, 0);
			notificationBuilder = createServiceNotificationBuilder();

			updateNotification();

			if (isMainActivityRunning(this))
				ActivityMainScreen.updateMainScreen();

            this.isRunning = true;

			Miscellaneous.logEvent("i", "Service", this.getResources().getString(R.string.serviceStarted) + " VERSION_CODE: " + BuildConfig.VERSION_CODE + ", VERSION_NAME: " + BuildConfig.VERSION_NAME + ", flavor: " + BuildConfig.FLAVOR, 1);
			Toast.makeText(this, this.getResources().getString(R.string.serviceStarted), Toast.LENGTH_LONG).show();

			/*
				On normal phones the app is supposed to automatically restart in case of any problems.
				In the emulator we want it to stop to be able to better pinpoint the root cause.
			 */
			if(Miscellaneous.isAndroidEmulator())
				return START_NOT_STICKY;
			else
				return START_STICKY;
		}
		else
		{
			Miscellaneous.logEvent("e", "Service", "checkStartupRequirements() delivered false. Stopping service...", 1);
			this.stopSelf();
			return START_NOT_STICKY;
		}
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		return myBinder;
	}

	public enum serviceCommands
	{
		reloadSettings, reloadPointsOfInterest, reloadRules, updateNotification
	}

	public void serviceInterface(serviceCommands command)
	{
		Miscellaneous.logEvent("i", "Bind", "Ahhhh, customers... How can I help you?", 5);

		Miscellaneous.logEvent("i", "ServiceBind", "Request to " + command.toString(), 5);

		switch (command)
		{
			case reloadPointsOfInterest:
				PointOfInterest.loadPoisFromFile();
				break;
			case reloadRules:
				Rule.readFromFile();
				break;
			case reloadSettings:
				Settings.readFromPersistentStorage(this);
				applySettingsAndRules();
				if(myLocationProvider != null)
					myLocationProvider.applySettingsAndRules();
				break;
			case updateNotification:
				this.updateNotification();
				ActivityMainScreen.updateMainScreen();
				break;
			default:
				break;
		}
	}

	public void applySettingsAndRules()
	{
		checkForTtsEngine();

		startLocationProvider();
		ReceiverCoordinator.startAllReceivers();
		if(myLocationProvider != null)					// This condition can be met if the user has no locations defined.
			myLocationProvider.applySettingsAndRules();

		ReceiverCoordinator.applySettingsAndRules();

		DateTimeListener.reloadAlarms();
	}

	@Override
	public void onDestroy()
	{
		Miscellaneous.logEvent("i", "Service", getResources().getString(R.string.logServiceStopping), 1);

		stopRoutine();
        this.isRunning = false;
		Toast.makeText(this, getResources().getString(R.string.serviceStopped), Toast.LENGTH_LONG).show();
		Miscellaneous.logEvent("i", "Service", getResources().getString(R.string.serviceStopped), 1);
	}

	public void checkForTtsEngine()
	{
		if (Settings.useTextToSpeechOnNormal || Settings.useTextToSpeechOnSilent || Settings.useTextToSpeechOnVibrate || Rule.isAnyRuleUsing(Action.Action_Enum.speakText))
		{
			if (ttsEngine == null)
				ttsEngine = new TextToSpeech(this, this);
		} else
		{
			if (ttsEngine != null)
				ttsEngine.shutdown();
		}
	}

	private void startUpRoutine()
	{
		Settings.serviceStartDone = false;

		checkForTtsEngine();
		checkForPermissions();
		checkForRestrictedFeatures();
		checkForMissingBackgroundLocationPermission();

		Actions.context = this;
		Actions.automationServerRef = this;

		startLocationProvider();
		ReceiverCoordinator.startAllReceivers();

		PackageReplacedReceiver.setHasServiceBeenRunning(true, this);

		for(Rule r : Rule.getRuleCollection())
		{
			if(r.getsGreenLight(AutomationService.this))
				r.activate(AutomationService.this, false);
		}

		Settings.serviceStartDone = true;
		Settings.deviceStartDone = true;
	}

	protected void startLocationProvider()
	{
		if(ActivityPermissions.havePermission(Manifest.permission.ACCESS_COARSE_LOCATION, AutomationService.this))
			myLocationProvider = new LocationProvider(this); //autostart with this (only) constructor
	}

	protected void checkForPermissions()
	{
		if(ActivityPermissions.needMorePermissions(AutomationService.this))
		{
			boolean displayNotification = false;

			String rule = "";

			outerLoop:
			for(Rule r : Rule.getRuleCollection())
			{
				if(r.isRuleActive())
				{
					if(!r.haveEnoughPermissions())
					{
						{
							if(!displayNotification)
							{
								displayNotification = true;
								rule = r.getName();
								break outerLoop;
							}
						}
					}
				}
			}

			if(displayNotification)
			{
				Intent intent = new Intent(AutomationService.this, ActivityPermissions.class);
				PendingIntent pi = PendingIntent.getActivity(AutomationService.this, 0, intent, 0);

				Miscellaneous.logEvent("w", "Features disabled", "Features disabled because of rule " + rule, 5);

				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
					Miscellaneous.createDismissibleNotificationWithDelay(1010, null, getResources().getString(R.string.featuresDisabled), ActivityPermissions.notificationIdPermissions, AutomationService.NOTIFICATION_CHANNEL_ID_SERVICE, pi);
				else
					Miscellaneous.createDismissibleNotification(null, getResources().getString(R.string.featuresDisabled), ActivityPermissions.notificationIdPermissions, false, AutomationService.NOTIFICATION_CHANNEL_ID_SERVICE, pi);
			}
		}
	}

	protected void checkForRestrictedFeatures()
	{
		if(Rule.isAnyRuleUsing(Trigger_Enum.activityDetection))
		{
			try
			{
				Class testClass = Class.forName(ActivityManageRule.activityDetectionClassPath);
			}
			catch (ClassNotFoundException e)
			{
				Intent intent = new Intent(AutomationService.this, ActivityMainTabLayout.class);
				PendingIntent pi = PendingIntent.getActivity(AutomationService.this, 0, intent, 0);
//				Miscellaneous.createDismissableNotification(getResources().getString(R.string.settingsReferringToRestrictedFeatures), ActivityPermissions.notificationIdPermissions, pi);

				Miscellaneous.logEvent("w", "Features disabled", "Background location disabled because Google to blame.", 5);

				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
					Miscellaneous.createDismissibleNotificationWithDelay(3300, null, getResources().getString(R.string.featuresDisabled), notificationIdRestrictions, AutomationService.NOTIFICATION_CHANNEL_ID_SERVICE, pi);
				else
					Miscellaneous.createDismissibleNotification(null, getResources().getString(R.string.featuresDisabled), notificationIdRestrictions, false, AutomationService.NOTIFICATION_CHANNEL_ID_SERVICE, pi);
			}
		}
	}

	public void cancelNotification()
	{
		NotificationManagerCompat.from(AutomationService.this).cancelAll();
	}

	protected void checkForMissingBackgroundLocationPermission()
	{
		if(Miscellaneous.googleToBlameForLocation(true))
		{
			Intent intent = new Intent(AutomationService.this, ActivityMainTabLayout.class);
			PendingIntent pi = PendingIntent.getActivity(AutomationService.this, 0, intent, 0);

			Miscellaneous.logEvent("w", "Features disabled", "Background location disabled because Google to blame.", 5);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
				Miscellaneous.createDismissibleNotificationWithDelay(2200, null, getResources().getString(R.string.featuresDisabled), notificationIdLocationRestriction, AutomationService.NOTIFICATION_CHANNEL_ID_SERVICE, pi);
			else
				Miscellaneous.createDismissibleNotification(null, getResources().getString(R.string.featuresDisabled), notificationIdLocationRestriction, false, AutomationService.NOTIFICATION_CHANNEL_ID_SERVICE, pi);
		}
	}

	public static void startAutomationService(Context context, boolean startAtBoot)
	{
		if(!(isMyServiceRunning(context)))
		{
			Intent myServiceIntent = new Intent(context, AutomationService.class);
			myServiceIntent.putExtra("startAtBoot", startAtBoot);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				context.startForegroundService(myServiceIntent);
			else
				context.startService(myServiceIntent);
		}
		else
			Miscellaneous.logEvent("i", "Service", "Service is already running.", 1);
	}
	
	private void stopRoutine()
	{
		Miscellaneous.logEvent("i", "Service", "Stopping service...", 3);
		try
		{
			myLocationProvider.stopLocationService();
			ReceiverCoordinator.stopAllReceivers();
		}
		catch(NullPointerException e)
		{
			Miscellaneous.logEvent("e", getResources().getString(R.string.serviceNotRunning), getResources().getString(R.string.serviceNotRunning) + ". " + getResources().getString(R.string.cantStopIt), 3);
		}

		if(ttsEngine != null)
			ttsEngine.shutdown();
		
		PackageReplacedReceiver.setHasServiceBeenRunning(false, this);

		centralInstance = null;
		Settings.serviceStartDone = false;
	}

	protected static Builder createDefaultNotificationBuilderOld()
	{
		Builder builder = new Builder(AutomationService.getInstance());
		builder.setContentTitle("Automation");

		if(Settings.showIconWhenServiceIsRunning)
			builder.setSmallIcon(R.drawable.ic_launcher);

		builder.setCategory(Notification.CATEGORY_SERVICE);
		builder.setWhen(System.currentTimeMillis());
		builder.setContentIntent(myPendingIntent);

		Notification defaultNotification = builder.build();

		defaultNotification.icon = R.drawable.ic_launcher;
		defaultNotification.when = System.currentTimeMillis();

//		defaultNotification.defaults |= Notification.DEFAULT_VIBRATE;
//		defaultNotification.defaults |= Notification.DEFAULT_LIGHTS;

		defaultNotification.flags |= Notification.FLAG_AUTO_CANCEL;
//		defaultNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
		defaultNotification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

//		defaultNotification.ledARGB = Color.YELLOW;
//		defaultNotification.ledOnMS = 1500;
//		defaultNotification.ledOffMS = 1500;

		return builder;
	}
	
	protected static NotificationCompat.Builder createServiceNotificationBuilder()
	{
		NotificationManager mNotificationManager = (NotificationManager) AutomationService.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder builder;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			NotificationChannel channel = Miscellaneous.getNotificationChannel(AutomationService.NOTIFICATION_CHANNEL_ID_SERVICE);
//			channel.setLightColor(Color.BLUE);
			channel.enableVibration(false);
			channel.setSound(null, null);
			channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
			mNotificationManager.createNotificationChannel(channel);

			builder = new NotificationCompat.Builder(AutomationService.getInstance(), NOTIFICATION_CHANNEL_ID_SERVICE);
		}
		else
			builder = new NotificationCompat.Builder(AutomationService.getInstance());

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			builder.setCategory(Notification.CATEGORY_SERVICE);

		builder.setWhen(System.currentTimeMillis());
		builder.setContentIntent(myPendingIntent);

		builder.setContentTitle(AutomationService.getInstance().getResources().getString(R.string.app_name));
		builder.setOnlyAlertOnce(true);

		if(Settings.showIconWhenServiceIsRunning)
			builder.setSmallIcon(R.drawable.ic_launcher);

//		builder.setContentText(textToDisplay);
//		builder.setSmallIcon(icon);
//		builder.setStyle(new NotificationCompat.BigTextStyle().bigText(textToDisplay));

		return builder;
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static void updateNotification()
	{
		AutomationService instance = getInstance();
		
		if(instance != null)
		{
			Miscellaneous.logEvent("i", "Notification", "Request to update notification.", 4);

			String bodyText="";
			String lastRuleString = "";

			if(PointOfInterest.getPointOfInterestCollection() != null && PointOfInterest.getPointOfInterestCollection().size() > 0)
			{
				try
				{
					PointOfInterest activePoi = PointOfInterest.getActivePoi();
					if(activePoi == null)
					{
						PointOfInterest closestPoi = PointOfInterest.getClosestPOI(instance.getLocationProvider().getCurrentLocation());
						bodyText = AutomationService.getInstance().getResources().getString(R.string.activePoi) + " " + AutomationService.getInstance().getResources().getString(R.string.none) + "\n" + AutomationService.getInstance().getResources().getString(R.string.closestPoi) + ": " + closestPoi.getName() + lastRuleString;
					}
					else
					{
						bodyText = AutomationService.getInstance().getResources().getString(R.string.activePoi) + " " + activePoi.getName() + lastRuleString;
					}
				}
				catch(NullPointerException e)
				{
					if(
							Rule.isAnyRuleUsing(Trigger_Enum.pointOfInterest)
									&&
							ActivityPermissions.havePermission(Manifest.permission.ACCESS_COARSE_LOCATION, AutomationService.getInstance())
									&&
							ActivityPermissions.havePermission(Manifest.permission.ACCESS_FINE_LOCATION, AutomationService.getInstance())
					  )
						bodyText = instance.getResources().getString(R.string.stillGettingPosition);
					else
						bodyText = instance.getResources().getString(R.string.locationEngineNotActive);
				}
			}

			try
			{
				lastRuleString = instance.getResources().getString(R.string.lastRule) + " " + Rule.getLastActivatedRule().getName()  + " " + instance.getResources().getString(R.string.at) + " " + Rule.getLastActivatedRuleActivationTime().toLocaleString();
			}
			catch(Exception e)
			{
				lastRuleString = instance.getResources().getString(R.string.lastRule) + " n./a.";
			}

			String textToDisplay = bodyText + " " + lastRuleString;

			if(notificationBuilder == null)
					notificationBuilder = createServiceNotificationBuilder();

			notificationBuilder.setContentText(textToDisplay);
			notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(textToDisplay));

			myNotification = notificationBuilder.build();
			myNotification.defaults = 0;

//				NotificationManager notificationManager = (NotificationManager) instance.getSystemService(NOTIFICATION_SERVICE);
			// hide the notification after its selected
//				myNotification.flags |= Notification.FLAG_AUTO_CANCEL;
			myNotification.flags |= Notification.FLAG_NO_CLEAR;
//			    notificationManager.notify(notificationId, myNotification);

			instance.startForeground(notificationId, myNotification);
		}
	}

	public class LocalBinder extends Binder
	{
		public AutomationService getService()
		{
			return AutomationService.this;
		}
	}

	@Override
	public void onInit(int status)
	{
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * force will skip volume settings and stuff
	 **/
	public void speak(String text, boolean force)
	{
		if(text.length() > 0 && (force || Settings.useTextToSpeechOnNormal || Settings.useTextToSpeechOnSilent || Settings.useTextToSpeechOnVibrate))
		{
			AudioManager myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			int mode = myAudioManager.getRingerMode();
			
			if(
					(mode == AudioManager.RINGER_MODE_NORMAL && Settings.useTextToSpeechOnNormal)
												||
					(mode == AudioManager.RINGER_MODE_VIBRATE && Settings.useTextToSpeechOnVibrate)
												||
					(mode == AudioManager.RINGER_MODE_SILENT && Settings.useTextToSpeechOnSilent)
												||
											  force
				)
			{
				if(Settings.muteTextToSpeechDuringCalls && PhoneStatusListener.isInACall() && !force)
				{
					Miscellaneous.logEvent("i", "TextToSpeech", "Currently in a call. Not speaking as requested.", 4);
					return;
				}
				else
				{
					try
					{
						for(int i = 0; i < 5; i++)
						{								
							if(ttsEngine != null)
							{
								break;
							}
							else
							{
								try
								{
									Miscellaneous.logEvent("i", "TTS", "Waiting for a moment to give the TTS service time to load...", 4);
									Thread.sleep(1000);	// give the tts engine time to load
								}
								catch(Exception e)
								{}
							}
						}
						Miscellaneous.logEvent("i", "TextToSpeech", "Speaking " + text + " in language " + ttsEngine.getLanguage().toLanguageTag(), 3);
						this.ttsEngine.speak(text, TextToSpeech.QUEUE_ADD, null);
					}
					catch(Exception e)
					{
						Miscellaneous.logEvent("e", "TextToSpeech", Log.getStackTraceString(e), 3);
					}
				}
			}
		}
	}
	
	 public static boolean isMainActivityRunning(Context context)
	 {
		 if(ActivityMainScreen.getActivityMainScreenInstance() == null)
			 return false;
		 else
			 return true;
	 }
	
	public static boolean isMyServiceRunning(Context context)
	{
		try
		{
		    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
		    {
		        if(AutomationService.class.getName().equals(service.service.getClassName()))
		        {
		            return true;
		        }
		    }
		}
		catch(NullPointerException e)
		{
			if(Log.getStackTraceString(e).contains("activate"))	// Means that a poi has been activated/deactivated. Service is running.
				return true;
		}

	    return false;
	}
}