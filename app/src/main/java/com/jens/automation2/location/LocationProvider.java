package com.jens.automation2.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.jens.automation2.ActivityMainScreen;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.PointOfInterest;
import com.jens.automation2.R;
import com.jens.automation2.Rule;
import com.jens.automation2.Settings;
import com.jens.automation2.Trigger.Trigger_Enum;
import com.jens.automation2.receivers.ConnectivityReceiver;
import com.jens.automation2.receivers.PhoneStatusListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class LocationProvider
{
	protected static boolean passiveLocationListenerActive = false;
	protected static LocationListener passiveLocationListener;
	protected static LocationProvider locationProviderInstance = null;
	protected AutomationService parentService;
	public AutomationService getParentService()
	{
		return parentService;
	}

	protected Location currentLocation;
	protected static Location currentLocationStaticCopy;
	protected static double speed;
	protected ArrayList<Location> locationList = new ArrayList<Location>();
	protected static Handler speedHandler = null;
	protected static boolean speedTimerActive = false;
	protected static Calendar etaAtNextPoi = null;

	public static Calendar getEtaAtNextPoi()
	{
		return etaAtNextPoi;
	}

	public LocationProvider(AutomationService parent)
	{
		parentService = parent;
		locationProviderInstance = this;

		startLocationService();
	}

	public static LocationProvider getInstance()
	{
		return locationProviderInstance;
	}

	public Location getCurrentLocation()
	{
		return currentLocation;
	}
	public static Location getLastKnownLocation()
	{
		return currentLocationStaticCopy;
	}

	public static double getSpeed()
	{
		return speed;
	}

	public static void setSpeed(double speed)
	{
		LocationProvider.speed = speed;

		/*
			Check if the last location update may be to old.
			It could be that for whatever reason we didn't get a recent location, but the current speed
			indicates we have moved quite a bit.
		 */

		Calendar now = Calendar.getInstance();

		float distanceToClosestPoi = PointOfInterest.getClosestPOI(getLastKnownLocation()).getLocation().distanceTo(getLastKnownLocation());
		long timeInSecondsPassedSinceLastLocationUpdate = (now.getTimeInMillis() - getLastKnownLocation().getTime()) / 1000;

		// Could be we were driving towards it instead of away, but we'll ignore that for the moment.

		long secondsRequiredForArrival = now.getTimeInMillis()/1000 / (1000 / 60 * 60);
		now.add(Calendar.SECOND, (int)secondsRequiredForArrival);

		etaAtNextPoi = now;

		if(speedTimerActive)
			resetSpeedTimer(etaAtNextPoi);
		else
			startSpeedTimer(etaAtNextPoi);
	}

	public void setCurrentLocation(Location newLocation, boolean skipVerification)
	{
		if(newLocation != null)
		{
			Miscellaneous.logEvent("i", "Location", "Setting location.", 4);

			currentLocation = newLocation;
			currentLocationStaticCopy = newLocation;

			Miscellaneous.logEvent("i", "LocationListener", "Giving update to POI class", 4);
			PointOfInterest.positionUpdate(newLocation, parentService, false, skipVerification);

			try
			{
				if (
						locationList.size() >= 1
								&&
								locationList.get(locationList.size() - 1).getTime() == newLocation.getTime()
								&&
								locationList.get(locationList.size() - 1).getProvider().equals(newLocation.getProvider())
				)
				{
					// This is a duplicate update, do not store it
					Miscellaneous.logEvent("i", "LocationListener", "Duplicate location, ignoring.", 4);
				}
				else
				{
					Miscellaneous.logEvent("i", "Speed", "Commencing speed calculation.", 4);
					// This part keeps the last two location entries to determine the current speed.

					locationList.add(newLocation);

					if (newLocation.hasSpeed())
					{
						Miscellaneous.logEvent("i", "Speed", "Location has speed, taking that: " + String.valueOf(newLocation.getSpeed()) + " km/h", 4);
						setSpeed(newLocation.getSpeed());    // Take the value that came with the location, that should be more precise
					}
					else
					{
						speedCalculation:
						if (locationList.size() >= 2)
						{
							while (locationList.size() > 2)
							{
								// Remove all entries except for the last 2
								Miscellaneous.logEvent("i", "Speed", "About to delete oldest position record until only 2 left. Currently have " + String.valueOf(locationList.size()) + " records.", 4);
								locationList.remove(0);
							}

						/*
							The two most recent locations in the list must have a usable accuracy.
						 */
							for (int i = 0; i < 2; i++)
							{
								if
								(
										(locationList.get(i).getProvider().equals(LocationManager.GPS_PROVIDER) && locationList.get(i).getAccuracy() > Settings.satisfactoryAccuracyGps)
												||
												(locationList.get(i).getProvider().equals(LocationManager.NETWORK_PROVIDER) && locationList.get(i).getAccuracy() > Settings.satisfactoryAccuracyNetwork)
								)
								{
									Miscellaneous.logEvent("i", "Speed", "Not using 2 most recent locations for speed calculation because at least one does not have a satisfactory accuracy: " + locationList.get(i).toString(), 4);
									break speedCalculation;
								}
							}

							Miscellaneous.logEvent("i", "Speed", "Trying to calculate speed based on the last locations.", 4);

							double currentSpeed;
							long timeDifferenceInSeconds = (Math.abs(locationList.get(locationList.size() - 2).getTime() - locationList.get(locationList.size() - 1).getTime())) / 1000; //milliseconds
							if (timeDifferenceInSeconds <= Settings.speedMaximumTimeBetweenLocations * 60)
							{
								double distanceTraveled = locationList.get(locationList.size() - 2).distanceTo(locationList.get(locationList.size() - 1)); //results in meters

								if (timeDifferenceInSeconds == 0)
								{
									Miscellaneous.logEvent("w", "Speed", "No time passed since last position. Can't calculate speed here.", 4);
									return;
								}

								currentSpeed = distanceTraveled / timeDifferenceInSeconds * 3.6;    // convert m/s --> km/h

                            /*
                                Due to strange factors the time difference might be 0 resulting in mathematical error.
                             */
								if (Double.isInfinite(currentSpeed) | Double.isNaN(currentSpeed))
									Miscellaneous.logEvent("i", "Speed", "Error while calculating speed.", 4);
								else
								{
									Miscellaneous.logEvent("i", "Speed", "Current speed: " + String.valueOf(currentSpeed) + " km/h", 2);

									setSpeed(currentSpeed);

									// execute matching rules containing speed
									ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesBySpeed();
									for (Rule oneRule : ruleCandidates)
									{
										if(oneRule.getsGreenLight(this.getParentService()))
											oneRule.activate(getParentService(), false);
									}
								}
							}
							else
								Miscellaneous.logEvent("i", "Speed", "Last two locations are too far apart in terms of time. Cannot use them for speed calculation.", 4);
						}
						else
						{
							Miscellaneous.logEvent("w", "Speed", "Don't have enough values for speed calculation, yet.", 3);
						}
					}
				}
			}
			catch (Exception e)
			{
				Miscellaneous.logEvent("e", "Speed", "Error during speed calculation: " + Log.getStackTraceString(e), 3);
			}

			AutomationService.updateNotification();

			if (AutomationService.isMainActivityRunning(parentService))
				ActivityMainScreen.updateMainScreen();
		}
		else
			Miscellaneous.logEvent("w", "Location", "New location given is null. Ignoring.", 5);
	}

	public void startLocationService()
	{
//		if(Settings.useAccelerometerForPositioning && !Miscellaneous.isAndroidEmulator())
//		{
//			accelerometerHandler = new AccelerometerHandler();
//			mySensorActivity = new SensorActivity(this);
//		}

		// startPhoneStateListener
			PhoneStatusListener.startPhoneStatusListener(parentService);			// also used to mute anouncements during calls

		// startConnectivityReceiver
			ConnectivityReceiver.startConnectivityReceiver(parentService);

		if(Settings.positioningEngine == 0)
		{
			if(Rule.isAnyRuleUsing(Trigger_Enum.pointOfInterest) | Rule.isAnyRuleUsing(Trigger_Enum.speed))
			{
//				TelephonyManager telephonyManager = (TelephonyManager) AutomationService.getInstance().getSystemService(Context.TELEPHONY_SERVICE);

				// startCellLocationChangedReceiver
				if (CellLocationChangedReceiver.isCellLocationChangedReceiverPossible())
				{
					if (WifiBroadcastReceiver.mayCellLocationReceiverBeActivated())
						CellLocationChangedReceiver.startCellLocationChangedReceiver();
				}
				else
				{
				/*
					Reasons why we may end up here:
					- Airplane mode is active
					- No phone module present (pure wifi device)
					- No SIM card is inserted or it's not unlocked

					We'd have to try GPS now to get an initial position.
					For permanent use there is no way we could know when it
					would make sense to check the position again.
				 */

					// Trigger a one-time-position-search
					Location loc = CellLocationChangedReceiver.getInstance().getLocation("fine");
					LocationProvider.getInstance().setCurrentLocation(loc, true);
				}

				// startPassiveLocationListener
				startPassiveLocationListener();
			}
		}
		else
		{
//			if(Rule.isAnyRuleUsing(Trigger_Enum.pointOfInterest))
//				GeofenceIntentService.startService();
		}
	}

	public void stopLocationService()
	{
		try
		{
			PhoneStatusListener.stopPhoneStatusListener(parentService);
			CellLocationChangedReceiver.stopCellLocationChangedReceiver();
			SensorActivity.stopAccelerometerReceiver();
			WifiBroadcastReceiver.stopWifiReceiver();
			SensorActivity.stopAccelerometerReceiver();
			stopPassiveLocationListener();
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "cellReceiver", "Error stopping LocationReceiver: " + Log.getStackTraceString(e), 3);
		}
	}

	public void startPassiveLocationListener()
	{
		if(!passiveLocationListenerActive)
		{
			Miscellaneous.logEvent("i", "LocationListener", "Arming passive location listener.", 4);
			LocationManager myLocationManager = (LocationManager) parentService.getSystemService(Context.LOCATION_SERVICE);
			passiveLocationListener = new MyPassiveLocationListener();
			try
			{
				myLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, Settings.minimumTimeBetweenUpdate, Settings.minimumDistanceChangeForNetworkUpdate, passiveLocationListener);
			}
			catch(SecurityException e)
			{}
			passiveLocationListenerActive = true;
		}
	}
	public void stopPassiveLocationListener()
	{
		if(passiveLocationListenerActive)
		{
			Miscellaneous.logEvent("i", "LocationListener", "Disarming passive location listener.", 4);
			LocationManager myLocationManager = (LocationManager) parentService.getSystemService(Context.LOCATION_SERVICE);
			myLocationManager.removeUpdates(passiveLocationListener);
			passiveLocationListenerActive = false;
		}
	}

	public class MyPassiveLocationListener implements LocationListener
	{
		@Override
		public void onLocationChanged(Location up2DateLocation)
		{
			Miscellaneous.logEvent("i", "Location", "Got passive location update, provider: " + up2DateLocation.getProvider(), 3);
			setCurrentLocation(up2DateLocation, true);
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			// TODO Auto-generated method stub

		}
	}

	public void handleAirplaneMode(boolean state)
	{
		if(state)
		{
			Miscellaneous.logEvent("i", "Airplane mode", "CellLocationChangedReceiver will be deactivated due to Airplane mode.", 2);
			CellLocationChangedReceiver.stopCellLocationChangedReceiver();
		}
		else
		{
			Miscellaneous.logEvent("i", "Airplane mode", "CellLocationChangedReceiver will be activated due to end of Airplane mode.", 2);
			CellLocationChangedReceiver.startCellLocationChangedReceiver();
		}
	}

	public void handleRoaming(Boolean roaming)
	{
		if(roaming)
		{
			Miscellaneous.logEvent("i", "Roaming", "We're on roaming.", 4);
			if(CellLocationChangedReceiver.isCellLocationListenerActive())
			{
				Miscellaneous.logEvent("i", "Roaming", "Disabling CellLocationChangedReceiver because we're on roaming.", 3);
				CellLocationChangedReceiver.stopCellLocationChangedReceiver();
			}
		}
		else
		{
			Miscellaneous.logEvent("i", "Roaming", "We're not on roaming.", 4);
			if(!CellLocationChangedReceiver.isCellLocationListenerActive())
			{
				Miscellaneous.logEvent("i", "Roaming", "Enabling CellLocationChangedReceiver because we're not on roaming.", 3);
				CellLocationChangedReceiver.startCellLocationChangedReceiver();
			}
		}
	}

	public void applySettingsAndRules()
	{
		/*
		 * This method's purpose is to check settings and rules and determine
		 * if changes in them require monitors to be started or stopped.
		 * It takes care only of those which are more expensive.
		 */

		// TextToSpeech is handled in AutomationService class

		Miscellaneous.logEvent("i", "LocationProvider", this.getParentService().getResources().getString(R.string.applyingSettingsAndRules), 3);

		// *********** SETTING CHANGES ***********
		if(Settings.useWifiForPositioning && !WifiBroadcastReceiver.isWifiListenerActive())
		{
			Miscellaneous.logEvent("i", "LocationProvider", "Starting WifiReceiver because settings now allow to.", 4);
			WifiBroadcastReceiver.startWifiReceiver(this);
		}
		else if(!Settings.useWifiForPositioning && WifiBroadcastReceiver.isWifiListenerActive())
		{
			Miscellaneous.logEvent("i", "LocationProvider", "Shutting down WifiReceiver because settings forbid to.", 4);
			WifiBroadcastReceiver.stopWifiReceiver();
		}

		if(Settings.useAccelerometerForPositioning && !SensorActivity.isAccelerometerReceiverActive())
		{
			Miscellaneous.logEvent("i", "LocationProvider", "Starting accelerometerReceiver because settings now allow to.", 4);
			SensorActivity.startAccelerometerReceiver();
		}
		else if(!Settings.useAccelerometerForPositioning && SensorActivity.isAccelerometerReceiverActive())
		{
			Miscellaneous.logEvent("i", "LocationProvider", "Shutting down accelerometerReceiver because settings forbid to.", 4);
			SensorActivity.stopAccelerometerReceiver();
		}

		// *********** RULE CHANGES ***********
		if(!CellLocationChangedReceiver.isCellLocationListenerActive() && (Rule.isAnyRuleUsing(Trigger_Enum.pointOfInterest) | Rule.isAnyRuleUsing(Trigger_Enum.speed)))
		{
			Miscellaneous.logEvent("i", "LocationProvider", "Starting NoiseListener CellLocationChangedReceiver because used in a new/changed rule.", 4);
			if(CellLocationChangedReceiver.haveAllPermission())
				CellLocationChangedReceiver.startCellLocationChangedReceiver();
		}
		else
		{
			Miscellaneous.logEvent("i", "LocationProvider", "Shutting down CellLocationChangedReceiver because not used in any rule.", 4);
			CellLocationChangedReceiver.stopCellLocationChangedReceiver();
		}

		AutomationService.updateNotification();
	}

	public static void startSpeedTimer(Calendar timeOfForcedLocationCheck)
	{
		if(!speedTimerActive)
		{
			if(timeOfForcedLocationCheck == null)
			{
				Miscellaneous.logEvent("i", "SpeedTimer", "Have no value for speed timer. Using 5 minutes in the future.", 4);
				timeOfForcedLocationCheck = Calendar.getInstance();
				timeOfForcedLocationCheck.add(Calendar.MINUTE, 5);
			}

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(Settings.dateFormat);

			Miscellaneous.logEvent("i", "SpeedTimer", "Starting SpeedTimer. Next forced location check would be at " + sdf.format(calendar.getTime()), 4);

			Message msg = new Message();
			msg.what = 1;

			if(speedHandler == null)
				speedHandler = new SpeedHandler();

			speedHandler.sendMessageAtTime(msg, timeOfForcedLocationCheck.getTimeInMillis());
//				speedHandler.sendMessageDelayed(msg, delayTime);
			speedTimerActive = true;
		}
	}

	public static void stopSpeedTimer()
	{
		if(speedTimerActive)
		{
			Miscellaneous.logEvent("i", "SpeedTimer", "Stopping SpeedTimer.", 4);

// 			Message msg = new Message();
// 			msg.what = 0;

			if(speedHandler == null)
				speedHandler = new SpeedHandler();
			else
				speedHandler.removeMessages(1);

			speedTimerActive = false;
		}
	}


	public static void resetSpeedTimer(Calendar timeOfForcedLocationCheck)
	{
		if(speedTimerActive)
		{
			if(timeOfForcedLocationCheck == null)
			{
				Miscellaneous.logEvent("i", "SpeedTimer", "Have no value for speed timer. Using 5 minutes in the future.", 4);
				timeOfForcedLocationCheck = Calendar.getInstance();
				timeOfForcedLocationCheck.add(Calendar.MINUTE, 5);
			}

			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(Settings.dateFormat);

			Miscellaneous.logEvent("i", "SpeedTimer", "Resetting SpeedTimer. Next forced location check would be at " + sdf.format(calendar.getTime()), 5);
			speedHandler.removeMessages(1);

			Message msg = new Message();
			msg.what = 1;
			speedHandler.sendMessageAtTime(msg, timeOfForcedLocationCheck.getTimeInMillis());
//			speedHandler.sendMessageDelayed(msg, delayTime);
			speedTimerActive = true;
		}
		else
			startSpeedTimer(timeOfForcedLocationCheck);
	}

	static class SpeedHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);

			if(msg.what == 1)
			{
				// time is up, no cell location updates since x minutes, start accelerometer
				String text = "Timer triggered. Based on the last location and speed we may be at a POI. Forcing location update in case CellLocationChangedReceiver didn\'t fire.";

				Location currentLocation = CellLocationChangedReceiver.getInstance().getLocation("coarse");
				AutomationService.getInstance().getLocationProvider().setCurrentLocation(currentLocation, false);
			}
		}
	}
}