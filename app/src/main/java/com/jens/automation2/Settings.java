package com.jens.automation2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Settings implements SharedPreferences
{
	public static final int rulesThatHaveBeenRanHistorySize = 10;
	public final static int lockSoundChangesInterval = 15;
	public static final int newsPollEveryXDays = 3;
	public static final int newsDisplayForXDays = 3;
	public static final int updateCheckFrequencyDays = 7;
	public static final String folderName = "Automation";
	public static final String zipFileName = "automation.zip";

	public static final String constNewsOptInDone ="newsOptInDone";
	public static final String constNotificationChannelCleanupApk118 ="notificationChannelCleanupApk118";

	public static long minimumDistanceChangeForGpsUpdate;
	public static long minimumDistanceChangeForNetworkUpdate;
	public static long satisfactoryAccuracyGps;
	public static long satisfactoryAccuracyNetwork;
	public static int gpsTimeout;
	public static long minimumTimeBetweenUpdate;
	public static boolean startServiceAtSystemBoot;
	public static boolean writeLogFile;
	public static long logLevel;
	public static int logFileMaxSize;
	public static boolean useTextToSpeechOnNormal;
	public static boolean useTextToSpeechOnVibrate;
	public static boolean useTextToSpeechOnSilent;
	public static boolean muteTextToSpeechDuringCalls;
	public static int positioningEngine;
	public static boolean useWifiForPositioning;
	public static boolean useAccelerometerForPositioning;
	public static long useAccelerometerAfterIdleTime;
	public static long accelerometerMovementThreshold;
	public static long speedMaximumTimeBetweenLocations;
	public static long timeBetweenNoiseLevelMeasurements;
	public static long lengthOfNoiseLevelMeasurements;
	public static long referenceValueForNoiseLevelMeasurements;
	public static boolean hasServiceBeenRunning;
	public static boolean startServiceAfterAppUpdate;
	public static boolean startNewThreadForRuleActivation;
	public static boolean showIconWhenServiceIsRunning;
	public static boolean httpAcceptAllCertificates;
	public static int httpAttempts;
	public static int httpAttemptsTimeout;
	public static int httpAttemptGap;
	public static PointOfInterest lastActivePoi;
	public static boolean rememberLastActivePoi;
	public static int locationRingBufferSize;
	public static long timeBetweenProcessMonitorings;
	public static long acceptDeviceOrientationSignalEveryX_MilliSeconds;
	public static int activityDetectionFrequency;
	public static int activityDetectionRequiredProbability;
	public static boolean privacyLocationing;
	public static int startScreen;
	public static int tabsPlacement;
	public static boolean executeRulesAndProfilesWithSingleClick;
	public static boolean displayNewsOnMainScreen;
	public static boolean automaticUpdateCheck;
	public static long musicCheckFrequency;

	public static boolean lockSoundChanges;
	public static boolean noticeAndroid9MicrophoneShown;
	public static boolean noticeAndroid10WifiShown;
	public static long lastNewsPolltime;
	public static long lastUpdateCheck;

	public static ArrayList<String> whatHasBeenDone;

	/*
		Generic settings valid for all installations and not changable
	 */
	public static final String dateFormat = "E dd.MM.yyyy HH:mm:ss:ssss";

	public static final int default_positioningEngine = 0;
	public static final long default_minimumDistanceChangeForGpsUpdate = 100;
	public static final long default_minimumDistanceChangeForNetworkUpdate = 500; // in Meters
	public static final long default_satisfactoryAccuracyGps = 50;	
	public static final long default_satisfactoryAccuracyNetwork = 1000;
	public static final int default_gpsTimeout = 300;	// seconds
	public static final long default_minimumTimeBetweenUpdate = 30000; // in Milliseconds
	public static final boolean default_startServiceAtSystemBoot = false;
	public static final boolean default_writeLogFile = false;
	public static final long default_logLevel = 2;
	public static final int default_logFileMaxSize = 10;
	public static final boolean default_useTextToSpeechOnNormal = false;
	public static final boolean default_useTextToSpeechOnVibrate = false;
	public static final boolean default_useTextToSpeechOnSilent = false;
	public static final boolean default_muteTextToSpeechDuringCalls = true;
	public static final boolean default_useWifiForPositioning = true;
	public static final boolean default_useAccelerometerForPositioning = true;
	public static final long default_useAccelerometerAfterIdleTime = 5;
	public static final long default_accelerometerMovementThreshold = 2;
	public static final long default_speedMaximumTimeBetweenLocations = 4;
	public static final long default_timeBetweenNoiseLevelMeasurements = 60;
	public static final long default_lengthOfNoiseLevelMeasurements = 5;
	public static final long default_referenceValueForNoiseLevelMeasurements = 20;
	public static final boolean default_hasServiceBeenRunning = false;
	public static final boolean default_startServiceAfterAppUpdate = true;
	public static final boolean default_startNewThreadForRuleActivation = true;
	public static final boolean default_showIconWhenServiceIsRunning = true;
	public static final boolean default_httpAcceptAllCertificates = false;
	public static final int default_httpAttempts = 3;
	public static final int default_httpAttemptsTimeout = 60;
	public static final int default_httpAttemptGap = 2;
	public static final PointOfInterest default_lastActivePoi = null;
	public static final boolean default_rememberLastActivePoi = true;
	public static final int default_locationRingBufferSize=3;
	public static final long default_timeBetweenProcessMonitorings = 60;
	public static final long default_acceptDevicePositionSignalEveryX_MilliSeconds = 1000;
	public static final int default_activityDetectionFrequency = 60;
	public static final int default_activityDetectionRequiredProbability = 75;
	public static final boolean default_privacyLocationing = false;
	public static final int default_startScreen = 0;
	public static final int default_tabsPlacement = 0;
	public static final boolean default_executeRulesAndProfilesWithSingleClick = false;
	public static final boolean default_displayNewsOnMainScreen = false;
	public static final boolean default_automaticUpdateCheck = false;
	public static final boolean default_lockSoundChanges = false;
	public static final long default_lastNewsPolltime = -1;
	public static final long default_lastUpdateCheck = -1;
	public static final long default_musicCheckFrequency = 2500;

    @Override
	public boolean contains(String arg0)
	{
		return false;
	}
	@Override
	public Editor edit()
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, ?> getAll()
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean getBoolean(String arg0, boolean arg1)
	{
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public float getFloat(String arg0, float arg1)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getInt(String arg0, int arg1)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getLong(String arg0, long arg1)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public String getString(String arg0, String arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener arg0)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener arg0)
	{
		// TODO Auto-generated method stub
		
	}
	
	public static void readFromPersistentStorage(Context context)
	{
		try
		{
			Miscellaneous.logEvent("i", context.getResources().getString(R.string.settings), context.getResources().getString(R.string.refreshingSettingsFromFileToMemory), 4);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			
			startServiceAtSystemBoot = prefs.getBoolean("startServiceAtSystemBoot", default_startServiceAtSystemBoot);
			writeLogFile = prefs.getBoolean("writeLogFile", default_writeLogFile);
			
			boolean useTextToSpeech = false;			
			if(prefs.contains("useTextToSpeech"))
			{				
				useTextToSpeech = prefs.getBoolean("useTextToSpeech", false);
			}			
			if(prefs.contains("useTextToSpeech") && !useTextToSpeech)				// until all old users have been upgraded
			{
				useTextToSpeechOnNormal = false;
				useTextToSpeechOnVibrate = false;
				useTextToSpeechOnSilent = false;
			}
			else
			{
				useTextToSpeechOnNormal = prefs.getBoolean("useTextToSpeechOnNormal", default_useTextToSpeechOnNormal);
				useTextToSpeechOnVibrate = prefs.getBoolean("useTextToSpeechOnVibrate", default_useTextToSpeechOnVibrate);
				useTextToSpeechOnSilent = prefs.getBoolean("useTextToSpeechOnSilent", default_useTextToSpeechOnSilent);
			}
			
			muteTextToSpeechDuringCalls = prefs.getBoolean("muteTextToSpeechDuringCalls", default_muteTextToSpeechDuringCalls);

			positioningEngine = Integer.parseInt(prefs.getString("positioningEngineOption", String.valueOf(default_positioningEngine)));
			useWifiForPositioning = prefs.getBoolean("useWifiForPositioning", default_useWifiForPositioning);
			useAccelerometerForPositioning = prefs.getBoolean("useAccelerometerForPositioning", default_useAccelerometerForPositioning);
			useAccelerometerAfterIdleTime = Long.parseLong(prefs.getString("useAccelerometerAfterIdleTime", String.valueOf(default_useAccelerometerAfterIdleTime)));
			accelerometerMovementThreshold = Long.parseLong(prefs.getString("accelerometerMovementThreshold", String.valueOf(default_accelerometerMovementThreshold)));
			speedMaximumTimeBetweenLocations = Long.parseLong(prefs.getString("speedMaximumTimeBetweenLocations", String.valueOf(default_speedMaximumTimeBetweenLocations)));
			hasServiceBeenRunning = prefs.getBoolean("hasServiceBeenRunning", default_hasServiceBeenRunning);
			startServiceAfterAppUpdate = prefs.getBoolean("startServiceAfterAppUpdate", default_startServiceAfterAppUpdate);
			startNewThreadForRuleActivation = prefs.getBoolean("startNewThreadForRuleActivation", default_startNewThreadForRuleActivation);
			showIconWhenServiceIsRunning = prefs.getBoolean("showIconWhenServiceIsRunning", default_showIconWhenServiceIsRunning);
			
			minimumDistanceChangeForGpsUpdate = Long.parseLong(prefs.getString("MINIMUM_DISTANCE_CHANGE_FOR_GPS_UPDATE", String.valueOf(default_minimumDistanceChangeForGpsUpdate)));
			minimumDistanceChangeForNetworkUpdate = Long.parseLong(prefs.getString("MINIMUM_DISTANCE_CHANGE_FOR_NETWORK_UPDATE", String.valueOf(default_minimumDistanceChangeForNetworkUpdate)));
			satisfactoryAccuracyGps = Long.parseLong(prefs.getString("SATISFACTORY_ACCURACY_GPS", String.valueOf(default_satisfactoryAccuracyGps)));
			satisfactoryAccuracyNetwork = Long.parseLong(prefs.getString("SATISFACTORY_ACCURACY_NETWORK", String.valueOf(default_satisfactoryAccuracyNetwork)));
			gpsTimeout = Integer.parseInt(prefs.getString("gpsTimeout", String.valueOf(default_gpsTimeout)));
			minimumTimeBetweenUpdate = Long.parseLong(prefs.getString("MINIMUM_TIME_BETWEEN_UPDATE", String.valueOf(default_minimumTimeBetweenUpdate)));
			timeBetweenNoiseLevelMeasurements = Long.parseLong(prefs.getString("timeBetweenNoiseLevelMeasurements", String.valueOf(default_timeBetweenNoiseLevelMeasurements)));
			lengthOfNoiseLevelMeasurements = Long.parseLong(prefs.getString("lengthOfNoiseLevelMeasurements", String.valueOf(default_lengthOfNoiseLevelMeasurements)));
			referenceValueForNoiseLevelMeasurements = Long.parseLong(prefs.getString("referenceValueForNoiseLevelMeasurements", String.valueOf(default_referenceValueForNoiseLevelMeasurements)));
			timeBetweenProcessMonitorings = Long.parseLong(prefs.getString("timeBetweenProcessMonitorings", String.valueOf(default_timeBetweenProcessMonitorings)));
			acceptDeviceOrientationSignalEveryX_MilliSeconds = Long.parseLong(prefs.getString("acceptDevicePositionSignalEveryX_MilliSeconds", String.valueOf(default_acceptDevicePositionSignalEveryX_MilliSeconds)));
			
			httpAcceptAllCertificates = prefs.getBoolean("httpAcceptAllCertificates", default_httpAcceptAllCertificates);
			httpAttempts = Integer.parseInt(prefs.getString("httpAttempts", String.valueOf(default_httpAttempts)));
			httpAttemptsTimeout = Integer.parseInt(prefs.getString("httpAttemptsTimeout", String.valueOf(default_httpAttemptsTimeout)));
			httpAttemptGap = Integer.parseInt(prefs.getString("httpAttemptGap", String.valueOf(default_httpAttemptGap)));
			
			logLevel = Long.parseLong(prefs.getString("logLevel", String.valueOf(default_logLevel)));
			logFileMaxSize = Integer.parseInt(prefs.getString("logFileMaxSize", String.valueOf(default_logFileMaxSize)));
			
			lastActivePoi = default_lastActivePoi;
			rememberLastActivePoi = prefs.getBoolean("rememberLastActivePoi", default_rememberLastActivePoi);
			
			locationRingBufferSize = Integer.parseInt(prefs.getString("locationRingBufferSize", String.valueOf(default_locationRingBufferSize)));

			activityDetectionFrequency = Integer.parseInt(prefs.getString("activityDetectionFrequency", String.valueOf(default_activityDetectionFrequency)));
			activityDetectionRequiredProbability = Integer.parseInt(prefs.getString("activityDetectionRequiredProbability", String.valueOf(default_activityDetectionRequiredProbability)));

			privacyLocationing = prefs.getBoolean("privacyLocationing", default_privacyLocationing);
			startScreen = Integer.parseInt(prefs.getString("startScreen", String.valueOf(default_startScreen)));
			tabsPlacement = Integer.parseInt(prefs.getString("tabsPlacement", String.valueOf(default_tabsPlacement)));

			musicCheckFrequency = Long.parseLong(prefs.getString("musicCheckFrequency", String.valueOf(default_musicCheckFrequency)));

			if(Settings.musicCheckFrequency == 0)
				Settings.musicCheckFrequency = Settings.default_musicCheckFrequency;

			executeRulesAndProfilesWithSingleClick = prefs.getBoolean("executeRulesAndProfilesWithSingleClick", default_executeRulesAndProfilesWithSingleClick);
			automaticUpdateCheck = prefs.getBoolean("automaticUpdateCheck", default_automaticUpdateCheck);
			displayNewsOnMainScreen = prefs.getBoolean("displayNewsOnMainScreen", default_displayNewsOnMainScreen);

			lockSoundChanges = prefs.getBoolean("lockSoundChanges", default_lockSoundChanges);
			noticeAndroid9MicrophoneShown = prefs.getBoolean("noticeAndroid9MicrophoneShown", false);
			noticeAndroid10WifiShown = prefs.getBoolean("noticeAndroid10WifiShown", false);

			lastNewsPolltime = prefs.getLong("lastNewsPolltime", default_lastNewsPolltime);
			lastUpdateCheck = prefs.getLong("lastUpdateCheck", default_lastUpdateCheck);

			String whbdString = prefs.getString("whatHasBeenDone", "");
			if(whbdString != null && whbdString.length() > 0)
			{
				whatHasBeenDone = new ArrayList<>();
				for(String s : whbdString.split(";"))
				{
					whatHasBeenDone.add(s);
				}
			}
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", context.getResources().getString(R.string.settings), context.getResources().getString(R.string.errorReadingSettings) + " " + Log.getStackTraceString(e), 1);

		}
		finally
		{
			initializeSettings(context, false);
		}
	}

	public static void considerDone(String key)
	{
		if(whatHasBeenDone == null)
			whatHasBeenDone = new ArrayList<>();

		if(!whatHasBeenDone.contains(key))
			whatHasBeenDone.add(key);
	}

	public static boolean hasBeenDone(String key)
	{
		if(whatHasBeenDone != null)
		{
			if(whatHasBeenDone.contains(key))
				return true;
		}

		return false;
	}
	
	/**Makes sure a settings has a valid setting. If not it will assign a reasonable default setting to it.
	 * If force settings will be initialized even if the user has set something.**/
	public static boolean initializeSettings(Context context, boolean force)
	{
		if(force)
			eraseSettings(context);
		
		try
		{
			Miscellaneous.logEvent("i", context.getResources().getString(R.string.settings), context.getResources().getString(R.string.initializingSettingsToPersistentMemory), 5);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			
			Editor editor = prefs.edit();

			if(!prefs.contains("startServiceAtSystemBoot") || force)
				editor.putBoolean("startServiceAtSystemBoot", default_startServiceAtSystemBoot);
			
			if(!prefs.contains("writeLogFile") || force)
				editor.putBoolean("writeLogFile", default_writeLogFile);
			
			if(!prefs.contains("useTextToSpeechOnNormal") || force)
				editor.putBoolean("useTextToSpeechOnNormal", default_useTextToSpeechOnNormal);
			
			if(!prefs.contains("useTextToSpeechOnVibrate") || force)
				editor.putBoolean("useTextToSpeechOnVibrate", default_useTextToSpeechOnVibrate);
			
			if(!prefs.contains("useTextToSpeechOnSilent") || force)
				editor.putBoolean("useTextToSpeechOnSilent", default_useTextToSpeechOnSilent);
			
			if(!prefs.contains("muteTextToSpeechDuringCalls") || force)
				editor.putBoolean("muteTextToSpeechDuringCalls", default_muteTextToSpeechDuringCalls);

			if(!prefs.contains("positioningEngine") || force)
				editor.putString("positioningEngine", String.valueOf(default_positioningEngine));

			if(!prefs.contains("useWifiForPositioning") || force)
				editor.putBoolean("useWifiForPositioning", default_useWifiForPositioning);
			
			if(!prefs.contains("hasServiceBeenRunning") || force)
				editor.putBoolean("hasServiceBeenRunning", default_hasServiceBeenRunning);
			
			if(!prefs.contains("startServiceAfterAppUpdate") || force)
				editor.putBoolean("startServiceAfterAppUpdate", default_startServiceAfterAppUpdate);
			
			if(!prefs.contains("startNewThreadForRuleActivation") || force)
				editor.putBoolean("startNewThreadForRuleActivation", default_startNewThreadForRuleActivation);
			
			if(!prefs.contains("showIconWhenServiceIsRunning") || force)
				editor.putBoolean("showIconWhenServiceIsRunning", default_showIconWhenServiceIsRunning);
			
			if(!prefs.contains("useAccelerometerForPositioning") || force)
				editor.putBoolean("useAccelerometerForPositioning", default_useAccelerometerForPositioning);
			
			if(!prefs.contains("useAccelerometerAfterIdleTime") || force)
				editor.putString("useAccelerometerAfterIdleTime", String.valueOf(default_useAccelerometerAfterIdleTime));
			
			if(!prefs.contains("accelerometerMovementThreshold") || force)
				editor.putString("accelerometerMovementThreshold", String.valueOf(default_accelerometerMovementThreshold));
			
			if(!prefs.contains("speedMaximumTimeBetweenLocations") || force)
				editor.putString("speedMaximumTimeBetweenLocations", String.valueOf(default_speedMaximumTimeBetweenLocations));
			
			if(!prefs.contains("MINIMUM_DISTANCE_CHANGE_FOR_GPS_UPDATE") || force)
				editor.putString("MINIMUM_DISTANCE_CHANGE_FOR_GPS_UPDATE", String.valueOf(default_minimumDistanceChangeForGpsUpdate));
			
			if(!prefs.contains("MINIMUM_DISTANCE_CHANGE_FOR_NETWORK_UPDATE") || force)
				editor.putString("MINIMUM_DISTANCE_CHANGE_FOR_NETWORK_UPDATE", String.valueOf(default_minimumDistanceChangeForNetworkUpdate));
			
			if(!prefs.contains("SATISFACTORY_ACCURACY_GPS") || force)
				editor.putString("SATISFACTORY_ACCURACY_GPS", String.valueOf(default_satisfactoryAccuracyGps));
			
			if(!prefs.contains("SATISFACTORY_ACCURACY_NETWORK") || force)
				editor.putString("SATISFACTORY_ACCURACY_NETWORK", String.valueOf(default_satisfactoryAccuracyNetwork));
			
			if(!prefs.contains("gpsTimeout") || force)
				editor.putString("gpsTimeout", String.valueOf(default_gpsTimeout));
			
			if(!prefs.contains("MINIMUM_TIME_BETWEEN_UPDATE") || force)
				editor.putString("MINIMUM_TIME_BETWEEN_UPDATE", String.valueOf(default_minimumTimeBetweenUpdate));
			
			if(!prefs.contains("timeBetweenNoiseLevelMeasurements") || force)
				editor.putString("timeBetweenNoiseLevelMeasurements", String.valueOf(default_timeBetweenNoiseLevelMeasurements));
			
			if(!prefs.contains("lengthOfNoiseLevelMeasurements") || force)
				editor.putString("lengthOfNoiseLevelMeasurements", String.valueOf(default_lengthOfNoiseLevelMeasurements));
			
			if(!prefs.contains("referenceValueForNoiseLevelMeasurements") || force)
				editor.putString("referenceValueForNoiseLevelMeasurements", String.valueOf(default_referenceValueForNoiseLevelMeasurements));
			
			if(!prefs.contains("logLevel") || force)
				editor.putString("logLevel", String.valueOf(default_logLevel));

			if(!prefs.contains("logFileMaxSize") || force)
				editor.putString("logFileMaxSize", String.valueOf(default_logFileMaxSize));
			
			if(!prefs.contains("httpAcceptAllCertificates") || force)
				editor.putBoolean("httpAcceptAllCertificates", default_httpAcceptAllCertificates);
			
			if(!prefs.contains("httpAttempts") || force)
				editor.putString("httpAttempts", String.valueOf(default_httpAttempts));
			
			if(!prefs.contains("httpAttemptsTimeout") || force)
				editor.putString("httpAttemptsTimeout", String.valueOf(default_httpAttemptsTimeout));
			
			if(!prefs.contains("httpAttemptGap") || force)
				editor.putString("httpAttemptGap", String.valueOf(default_httpAttemptGap));
			
			if(!prefs.contains("lastActivePoi") || force)
				editor.putString("lastActivePoi", "null");
			
			if(!prefs.contains("rememberLastActivePoi") || force)
				editor.putBoolean("rememberLastActivePoi", default_rememberLastActivePoi);
			
			if(!prefs.contains("locationRingBufferSize") || force)
				editor.putString("locationRingBufferSize", String.valueOf(default_locationRingBufferSize));
			
			if(!prefs.contains("timeBetweenProcessMonitorings") || force)
				editor.putString("timeBetweenProcessMonitorings", String.valueOf(default_timeBetweenProcessMonitorings));

			if(!prefs.contains("acceptDevicePositionSignalEveryX_MilliSeconds") || force)
				editor.putString("acceptDevicePositionSignalEveryX_MilliSeconds", String.valueOf(default_acceptDevicePositionSignalEveryX_MilliSeconds));

			if(!prefs.contains("activityDetectionFrequency") || force)
				editor.putString("activityDetectionFrequency", String.valueOf(default_activityDetectionFrequency));

			if(!prefs.contains("activityDetectionRequiredProbability") || force)
				editor.putString("activityDetectionRequiredProbability", String.valueOf(default_activityDetectionRequiredProbability));

			if(!prefs.contains("privacyLocationing") || force)
				editor.putBoolean("privacyLocationing", default_privacyLocationing);

			if(!prefs.contains("startScreen") || force)
				editor.putString("startScreen", String.valueOf(default_startScreen));

			if(!prefs.contains("tabsPlacement") || force)
				editor.putString("tabsPlacement", String.valueOf(default_tabsPlacement));

			if(!prefs.contains("executeRulesAndProfilesWithSingleClick") || force)
				editor.putBoolean("executeRulesAndProfilesWithSingleClick", default_executeRulesAndProfilesWithSingleClick);

			if(!prefs.contains("automaticUpdateCheck") || force)
				editor.putBoolean("automaticUpdateCheck", default_automaticUpdateCheck);

			if(!prefs.contains("displayNewsOnMainScreen") || force)
				editor.putBoolean("displayNewsOnMainScreen", default_displayNewsOnMainScreen);

			if(!prefs.contains("musicCheckFrequency") || force)
				editor.putLong("musicCheckFrequency", default_musicCheckFrequency);

			if(!prefs.contains("lockSoundChanges") || force)
				editor.putBoolean("lockSoundChanges", default_lockSoundChanges);

			if(!prefs.contains("noticeAndroid9MicrophoneShown") || force)
				editor.putBoolean("noticeAndroid9MicrophoneShown", false);

			if(!prefs.contains("lastNewsPolltime") || force)
				editor.putLong("lastNewsPolltime", default_lastNewsPolltime);

			if(!prefs.contains("lastUpdateCheck") || force)
				editor.putLong("lastUpdateCheck", default_lastUpdateCheck);

			if(!prefs.contains("whatHasBeenDone") || force)
				editor.putString("whatHasBeenDone", "");
			
			editor.commit();
			
			return true;
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", context.getResources().getString(R.string.settings), context.getResources().getString(R.string.errorInitializingSettingsToPersistentMemory), 1);
//			eraseSettings(context);
		}
		return false;
	}
	
	public static void writeSettings(Context context)
	{
		try
		{
			Miscellaneous.logEvent("i", context.getResources().getString(R.string.settings), context.getResources().getString(R.string.writingSettingsToPersistentMemory), 5);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			
			Editor editor = prefs.edit();

				editor.putBoolean("startServiceAtSystemBoot", startServiceAtSystemBoot);
				editor.putBoolean("writeLogFile", writeLogFile);
//				editor.putBoolean("useTextToSpeech", useTextToSpeech);
				editor.putBoolean("useTextToSpeechOnNormal", useTextToSpeechOnNormal);
				editor.putBoolean("useTextToSpeechOnVibrate", useTextToSpeechOnVibrate);
				editor.putBoolean("useTextToSpeechOnSilent", useTextToSpeechOnSilent);
				editor.putBoolean("muteTextToSpeechDuringCalls", muteTextToSpeechDuringCalls);

				editor.putString("positioningEngine", String.valueOf(positioningEngine));
				editor.putBoolean("useWifiForPositioning", useWifiForPositioning);
				editor.putBoolean("hasServiceBeenRunning", hasServiceBeenRunning);
				editor.putBoolean("startServiceAfterAppUpdate", startServiceAfterAppUpdate);
				editor.putBoolean("startNewThreadForRuleActivation", startNewThreadForRuleActivation);
				editor.putBoolean("showIconWhenServiceIsRunning", showIconWhenServiceIsRunning);
				editor.putBoolean("useAccelerometerForPositioning", useAccelerometerForPositioning);
				editor.putString("useAccelerometerAfterIdleTime", String.valueOf(useAccelerometerAfterIdleTime));
				editor.putString("accelerometerMovementThreshold", String.valueOf(accelerometerMovementThreshold));
				editor.putString("speedMaximumTimeBetweenLocations", String.valueOf(speedMaximumTimeBetweenLocations));
				editor.putString("MINIMUM_DISTANCE_CHANGE_FOR_GPS_UPDATE", String.valueOf(minimumDistanceChangeForGpsUpdate));
				editor.putString("MINIMUM_DISTANCE_CHANGE_FOR_NETWORK_UPDATE", String.valueOf(minimumDistanceChangeForNetworkUpdate));
				editor.putString("SATISFACTORY_ACCURACY_GPS", String.valueOf(satisfactoryAccuracyGps));
				editor.putString("SATISFACTORY_ACCURACY_NETWORK", String.valueOf(satisfactoryAccuracyNetwork));
				editor.putString("gpsTimeout", String.valueOf(gpsTimeout));
				editor.putString("MINIMUM_TIME_BETWEEN_UPDATE", String.valueOf(minimumTimeBetweenUpdate));
				editor.putString("timeBetweenNoiseLevelMeasurements", String.valueOf(timeBetweenNoiseLevelMeasurements));
				editor.putString("lengthOfNoiseLevelMeasurements", String.valueOf(lengthOfNoiseLevelMeasurements));
				editor.putString("referenceValueForNoiseLevelMeasurements", String.valueOf(referenceValueForNoiseLevelMeasurements));
				editor.putString("logLevel", String.valueOf(logLevel));
				editor.putString("logFileMaxSize", String.valueOf(logFileMaxSize));
				editor.putBoolean("httpAcceptAllCertificates", httpAcceptAllCertificates);
				editor.putString("httpAttempts", String.valueOf(httpAttempts));
				editor.putString("httpAttemptsTimeout", String.valueOf(httpAttemptsTimeout));
				editor.putString("httpAttemptGap", String.valueOf(httpAttemptGap));
				editor.putString("locationRingBufferSize", String.valueOf(locationRingBufferSize));
				editor.putString("timeBetweenProcessMonitorings", String.valueOf(timeBetweenProcessMonitorings));
				editor.putString("acceptDevicePositionSignalEveryX_MilliSeconds", String.valueOf(acceptDeviceOrientationSignalEveryX_MilliSeconds));
				editor.putString("activityDetectionFrequency", String.valueOf(activityDetectionFrequency));
				editor.putString("activityDetectionRequiredProbability", String.valueOf(activityDetectionRequiredProbability));
				editor.putBoolean("privacyLocationing", privacyLocationing);
				editor.putString("startScreen", String.valueOf(startScreen));
				editor.putString("tabsPlacement", String.valueOf(tabsPlacement));
				editor.putBoolean("executeRulesAndProfilesWithSingleClick", executeRulesAndProfilesWithSingleClick);
				editor.putBoolean("automaticUpdateCheck", automaticUpdateCheck);
				editor.putBoolean("displayNewsOnMainScreen", displayNewsOnMainScreen);

				if(Settings.musicCheckFrequency == 0)
					Settings.musicCheckFrequency = Settings.default_musicCheckFrequency;
				editor.putString("musicCheckFrequency", String.valueOf(musicCheckFrequency));

				editor.putBoolean("lockSoundChanges", lockSoundChanges);
				editor.putBoolean("noticeAndroid9MicrophoneShown", noticeAndroid9MicrophoneShown);
				editor.putBoolean("noticeAndroid10WifiShown", noticeAndroid10WifiShown);

				editor.putLong("lastNewsPolltime", lastNewsPolltime);
				editor.putLong("lastUpdateCheck", lastUpdateCheck);

				editor.putString("whatHasBeenDone", Miscellaneous.explode(";", whatHasBeenDone));

				if(lastActivePoi == null)
					editor.putString("lastActivePoi", "null");
				else
					editor.putString("lastActivePoi", lastActivePoi.getName());
					
				editor.putBoolean("rememberLastActivePoi", rememberLastActivePoi);
			
			editor.commit();
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", context.getResources().getString(R.string.settings), context.getResources().getString(R.string.errorWritingSettingsToPersistentMemory), 1);
		}
	}
	
	public static boolean eraseSettings(Context context)
	{
		try
		{			
			Miscellaneous.logEvent("e", context.getResources().getString(R.string.settings), context.getResources().getString(R.string.invalidStuffStoredInSettingsErasing), 1);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			prefs.edit().clear().commit();
			return true;
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", context.getResources().getString(R.string.settings), context.getResources().getString(R.string.errorWritingSettingsToPersistentMemory), 1);
		}
		return false;
	}	
	
	@Override
	public Set<String> getStringSet(String arg0, Set<String> arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}