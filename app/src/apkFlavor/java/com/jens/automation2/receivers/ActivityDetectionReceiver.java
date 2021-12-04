package com.jens.automation2.receivers;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionApi;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.jens.automation2.ActivityPermissions;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.R;
import com.jens.automation2.Rule;
import com.jens.automation2.Settings;
import com.jens.automation2.Trigger.Trigger_Enum;

import java.util.ArrayList;
import java.util.Date;

// See also: http://developer.android.com/reference/com/google/android/gms/location/ActivityRecognitionClient.html
// https://www.sitepoint.com/google-play-services-location-activity-recognition/

public class ActivityDetectionReceiver extends IntentService implements AutomationListenerInterface, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	protected static ActivityRecognitionApi activityRecognitionClient = null;
	protected static boolean connected = false;
	protected static enum LastRequestEnum { start, stop, restart };
	protected static LastRequestEnum lastRequest = null;
	protected static GoogleApiClient googleApiClient = null;
	protected static ActivityRecognitionResult activityDetectionLastResult = null;
	protected static long lastUpdate = 0;
	protected static Date currentTime;
	protected static ActivityDetectionReceiver instance = null;

	protected static ActivityDetectionReceiver getInstance()
	{
		if(instance == null)
			instance = new ActivityDetectionReceiver();

		return instance;
	}

	protected static boolean activityDetectionReceiverRunning = false;
	protected static ActivityDetectionReceiver activityDetectionReceiverInstance = null;

	public static boolean isActivityDetectionReceiverRunning()
	{
		return activityDetectionReceiverRunning;
	}

	public static ActivityRecognitionResult getActivityDetectionLastResult()
	{
		return activityDetectionLastResult;
	}

	public static GoogleApiClient getApiClient()
	{
		if(googleApiClient == null)
		{
			googleApiClient = new GoogleApiClient.Builder(AutomationService.getInstance())
					.addConnectionCallbacks(getInstance())
					.addOnConnectionFailedListener(getInstance())
					.addApi(ActivityRecognition.API)
					.build();
		}

		return googleApiClient;
	}

	private static void requestUpdates()
	{
		long frequency = Settings.activityDetectionFrequency * 1000;
		Miscellaneous.logEvent("i", "ActivityDetectionReceiver", "Requesting ActivityDetection updates with frequency " + String.valueOf(frequency) + " milliseconds.", 4);


		ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(getApiClient(), 1000, getInstance().getActivityDetectionPendingIntent());
	}
	private void reloadUpdates()
	{
		long frequency = Settings.activityDetectionFrequency * 1000;
		Miscellaneous.logEvent("i", "ActivityDetectionReceiver", "Re-requesting ActivityDetection updates with frequency " + String.valueOf(frequency) + " milliseconds.", 4);

		activityRecognitionClient.removeActivityUpdates(getApiClient(), getInstance().getActivityDetectionPendingIntent());
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			Miscellaneous.logEvent("i", "ActivityDetectionReceiver", "Error reloading updates for ActivityDetectionReceiver: " + Log.getStackTraceString(e), 5);
		}

		ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(getApiClient(), frequency, getInstance().getActivityDetectionPendingIntent());
	}

	private static void stopUpdates()
	{
		Miscellaneous.logEvent("i", "ActivityDetectionReceiver", "Unsubscribing from ActivityDetection-updates.", 4);

		ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(getApiClient(), getInstance().getActivityDetectionPendingIntent());
//		activityRecognitionClient.removeActivityUpdates(getApiClient(), getInstance().getActivityDetectionPendingIntent());
//		activityRecognitionClient.disconnect();
	}
	public static void startActivityDetectionReceiver()
	{
		try
		{
			Miscellaneous.logEvent("i", "ActivityDetectionReceiver", "Starting ActivityDetectionReceiver", 3);

			if(activityDetectionReceiverInstance == null)
				activityDetectionReceiverInstance = new ActivityDetectionReceiver();

			if(!activityDetectionReceiverRunning && Rule.isAnyRuleUsing(Trigger_Enum.activityDetection))
			{
				if(isPlayServiceAvailable())
				{
					/*if(activityRecognitionClient == null)
						activityRecognitionClient = new ActivityRecognitionClient(Miscellaneous.getAnyContext(), activityDetectionReceiverInstance, activityDetectionReceiverInstance);*/

					lastRequest = LastRequestEnum.start;

					if(!connected)
						getApiClient().connect();
					else
						requestUpdates();

					activityDetectionReceiverRunning = true;
				}
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "ActivityDetectionReceiver", "Error starting ActivityDetectionReceiver: " + Log.getStackTraceString(ex), 3);
		}
	}

	public static void restartActivityDetectionReceiver()
	{
		try
		{
			if(!activityDetectionReceiverRunning && Rule.isAnyRuleUsing(Trigger_Enum.activityDetection))
			{
				Miscellaneous.logEvent("i", "ActivityDetectionReceiver", "Restarting ActivityDetectionReceiver", 3);

				if(activityDetectionReceiverInstance == null)
					activityDetectionReceiverInstance = new ActivityDetectionReceiver();

				if(isPlayServiceAvailable())
				{
//					if(activityRecognitionClient == null)
//						activityRecognitionClient = new ActivityRecognitionClient(Miscellaneous.getAnyContext(), activityDetectionReceiverInstance, activityDetectionReceiverInstance);

					lastRequest = LastRequestEnum.restart;

					if(!connected)
						getApiClient().connect();
					else
						requestUpdates();
				}
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "ActivityDetectionReceiver", "Error starting ActivityDetectionReceiver: " + Log.getStackTraceString(ex), 3);
		}

	}

	public static void stopActivityDetectionReceiver()
	{
		try
		{
			if(activityDetectionReceiverRunning)
			{
				Miscellaneous.logEvent("i", "ActivityDetectionReceiver", "Stopping ActivityDetectionReceiver", 3);

				if(isPlayServiceAvailable())
				{
					lastRequest = LastRequestEnum.stop;

					if(!connected)
						getApiClient().connect();
					else
						stopUpdates();

					activityDetectionReceiverRunning = false;
				}
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "ActivityDetectionReceiver", "Error stopping ActivityDetectionReceiver: " + Log.getStackTraceString(ex), 3);
		}
	}

	public static boolean isPlayServiceAvailable()
	{
		if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(Miscellaneous.getAnyContext()) == ConnectionResult.SUCCESS)
			return true;
		else
			return false;
	}


	@Override
	public void onConnectionFailed(ConnectionResult arg0)
	{
		Miscellaneous.logEvent("i", "ActivityDetectionReceiver", "Connection to Play Services failed.", 4);
		if(connected && getApiClient().isConnected())
		{
			connected = false;
		}
	}

	@Override
	public void onConnected(Bundle arg0)
	{
		Miscellaneous.logEvent("i", "ActivityDetectionReceiver", "Connected to Play Services.", 4);

		connected = true;

		if(lastRequest == null)
		{
			Miscellaneous.logEvent("w", "ActivityDetectionReceiver", "Request type not specified. Start or stop listening to activity detection updates?", 4);
			return;
		}

		if(lastRequest.equals(LastRequestEnum.start))
			requestUpdates();
		else if(lastRequest.equals(LastRequestEnum.stop))
			stopUpdates();
		else //reload, e.g. to set a new update time
			reloadUpdates();
	}

	@Override
	public void onConnectionSuspended(int arg0)
	{
		Miscellaneous.logEvent("i", "ActivityDetectionReceiver", "Connection to Play Services suspended.", 4);
//		activityRecognitionClient.disconnect();
		connected = false;
	}

	public ActivityDetectionReceiver()
	{
		super("ActivityDetectionIntentService");
		if(instance == null)
			instance = this;
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Miscellaneous.logEvent("i", "ActivityDetectionReceiver", "onHandleIntent(): Received some status.", 5);

		try
		{
			if(isActivityDetectionReceiverRunning())
			{
				currentTime = new Date();

				if(lastUpdate == 0 | currentTime.getTime() >= lastUpdate + Settings.activityDetectionFrequency * 1000 - 1000) // -1000 to include updates only marginaly below the threshold
				{
					lastUpdate = currentTime.getTime();

					if(ActivityRecognitionResult.hasResult(intent))
					{
						activityDetectionLastResult = ActivityRecognitionResult.extractResult(intent);

						for(DetectedActivity activity : activityDetectionLastResult.getProbableActivities())
						{
							int loglevel = 3;
							if(activity.getConfidence() < Settings.activityDetectionRequiredProbability)
								loglevel = 4;

							Miscellaneous.logEvent("i", "ActivityDetectionReceiver", "Detected activity (probability " + String.valueOf(activity.getConfidence()) + "%): " + getDescription(activity.getType()), loglevel);
						}

						/*
						 * Returns the list of activities that where detected with the confidence value associated with each activity.
						 * The activities are sorted by most probable activity first.
			 			 * The sum of the confidences of all detected activities this method returns does not have to be <= 100
			 			 * since some activities are not mutually exclusive (for example, you can be walking while in a bus)
			 			 * and some activities are hierarchical (ON_FOOT is a generalization of WALKING and RUNNING).
						*/

						ArrayList<Rule> allRulesWithActivityDetection = Rule.findRuleCandidatesByActivityDetection();
						for(int i=0; i<allRulesWithActivityDetection.size(); i++)
						{
							if(allRulesWithActivityDetection.get(i).applies(Miscellaneous.getAnyContext()) && allRulesWithActivityDetection.get(i).hasNotAppliedSinceLastExecution())
								allRulesWithActivityDetection.get(i).activate(AutomationService.getInstance(), false);
						}
					}
				}
				else
					Miscellaneous.logEvent("w", "ActivityDetectionReceiver", String.format(getResources().getString(R.string.ignoringActivityDetectionUpdateTooSoon), String.valueOf(Settings.activityDetectionFrequency)), 5);
			}
			else
				Miscellaneous.logEvent("w", "ActivityDetectionReceiver", "I am not running. I shouldn't be getting updates. Ignoring it.", 5);
		}
		catch(Exception e)
		{
			// some error, don't care.
			Miscellaneous.logEvent("e", "ActivityDetectionReceiver", "onHandleIntent(): Error while receiving status: " + Log.getStackTraceString(e), 4);
		}
	}

	public static int[] getAllTypes()
	{
		return new int[] {
				DetectedActivity.IN_VEHICLE,
				DetectedActivity.ON_BICYCLE,
				DetectedActivity.ON_FOOT,
				DetectedActivity.STILL,
				DetectedActivity.TILTING,
				DetectedActivity.WALKING,
				DetectedActivity.RUNNING,
				DetectedActivity.UNKNOWN
		};
	}

	public static String getDescription(int type)
	{
		switch(type)
		{
			case(DetectedActivity.IN_VEHICLE):
				return Miscellaneous.getAnyContext().getResources().getString(R.string.detectedActivityInVehicle);
			case(DetectedActivity.ON_BICYCLE):
				return Miscellaneous.getAnyContext().getResources().getString(R.string.detectedActivityOnBicycle);
			case(DetectedActivity.ON_FOOT):
				return Miscellaneous.getAnyContext().getResources().getString(R.string.detectedActivityOnFoot);
			case(DetectedActivity.STILL):
				return Miscellaneous.getAnyContext().getResources().getString(R.string.detectedActivityStill);
			case(DetectedActivity.TILTING):
				return Miscellaneous.getAnyContext().getResources().getString(R.string.detectedActivityTilting);
			case(DetectedActivity.WALKING):
				return Miscellaneous.getAnyContext().getResources().getString(R.string.detectedActivityWalking);
			case(DetectedActivity.RUNNING):
				return Miscellaneous.getAnyContext().getResources().getString(R.string.detectedActivityRunning);
			case(DetectedActivity.UNKNOWN):
				return Miscellaneous.getAnyContext().getResources().getString(R.string.detectedActivityUnknown);
			default:
				return Miscellaneous.getAnyContext().getResources().getString(R.string.detectedActivityInvalidStatus);
		}
	}

	public static String[] getAllDescriptions()
	{
		ArrayList<String> types = new ArrayList<String>();

		for(int type : getAllTypes())
			types.add(getDescription(type));

		return types.toArray(new String[types.size()]);
	}

	@Override
	public void startListener(AutomationService automationService)
	{
		ActivityDetectionReceiver.startActivityDetectionReceiver();
	}

	@Override
	public void stopListener(AutomationService automationService)
	{
		ActivityDetectionReceiver.stopActivityDetectionReceiver();
	}

	public static boolean haveAllPermission()
	{
		return ActivityPermissions.havePermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION", Miscellaneous.getAnyContext());
	}

	@Override
	public boolean isListenerRunning()
	{
		return ActivityDetectionReceiver.isActivityDetectionReceiverRunning();
	}

	@Override
	public Trigger_Enum[] getMonitoredTrigger()
	{
		return new Trigger_Enum[] { Trigger_Enum.activityDetection };
	}

	private PendingIntent getActivityDetectionPendingIntent()
	{
		Intent intent = new Intent(AutomationService.getInstance(), ActivityDetectionReceiver.class);
		PendingIntent returnValue = PendingIntent.getService(AutomationService.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return returnValue;
	}
}