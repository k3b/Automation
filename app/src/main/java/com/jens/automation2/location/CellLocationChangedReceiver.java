package com.jens.automation2.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.jens.automation2.Action;
import com.jens.automation2.ActivityPermissions;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.PointOfInterest;
import com.jens.automation2.R;
import com.jens.automation2.Rule;
import com.jens.automation2.Settings;
import com.jens.automation2.receivers.ConnectivityReceiver;

import java.util.Date;

public class CellLocationChangedReceiver extends PhoneStateListener
{
	private LocationManager myLocationManager;
	private Location currentLocation;
	public MyLocationListener myLocationListener = new MyLocationListener();
	public Boolean locationListenerArmed = false;
	public Date lastCellLocationUpdate;
	protected static boolean followUpdate = true;
	protected static TimeoutHandler timeoutHandler = null;
	protected static boolean timeoutHandlerActive = false;
	protected static boolean cellLocationListenerActive = false;
	protected static CellLocationChangedReceiver instance;
	protected static TelephonyManager telephonyManager;
	
	public static boolean isCellLocationListenerActive()
	{
		return cellLocationListenerActive;
	}

	protected static CellLocationChangedReceiver getInstance()
	{
		if(instance == null)
			instance = new CellLocationChangedReceiver();
		
		return instance;
	}

	@Override
	public void onCellLocationChanged(CellLocation location)
	{
		super.onCellLocationChanged(location);
		
		if(Settings.useAccelerometerForPositioning)
			SensorActivity.startAccelerometerTimer();
		
		if(followUpdate)
		{
			Date currentDate = new Date();
			
			Miscellaneous.logEvent("i", "CellLocation", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.cellMastChanged), location.toString()), 3);
			
			if(Settings.useAccelerometerForPositioning) //and last cell mast change longer than x minutes in the past
			{
				PointOfInterest possiblyActivePoi = PointOfInterest.getActivePoi();
				if(	possiblyActivePoi != null ) //if any poi is active
				{
					// Did the last activated rule activate wifi? Then we don't need accelerometer, we'll use wifiReceiver
					try
					{
						for(Action action : Rule.getLastActivatedRule().getActionSet())
						{
							if(action.getAction() == Action.Action_Enum.turnWifiOn)
							{
								// we will be using wifiReceiver, deactivate AccelerometerTimer if applicable
								SensorActivity.stopAccelerometerTimer();
							}
						}					
					}
					catch(NullPointerException ne)
					{
						// Nothing to do, there is no last activated rule. Wifi hasn't been activated so we don't
						// deactive accelerometer receiver.
					}
				}
				else
				{
					if(lastCellLocationUpdate == null)
						SensorActivity.startAccelerometerTimer();
					else
					{
						long timeSinceLastUpdate = currentDate.getTime() - lastCellLocationUpdate.getTime(); //in milliseconds
						if(timeSinceLastUpdate > Settings.useAccelerometerAfterIdleTime*60*1000)
						{
							SensorActivity.startAccelerometerTimer();
						}
						else
						{
							//reset timer
							SensorActivity.resetAccelerometerTimer();
						}
					}
				}
			}
			lastCellLocationUpdate = currentDate;
			
			myLocationManager = (LocationManager) AutomationService.getInstance().getSystemService(Context.LOCATION_SERVICE);
			currentLocation = getLocation("coarse");
			try
			{
				AutomationService.getInstance().getLocationProvider().setCurrentLocation(currentLocation, false);
			}
			catch(NullPointerException e)
			{
				Miscellaneous.logEvent("e", "LocationProvider", "Location provider is null: " + Log.getStackTraceString(e), 1);
			}
		}
		else
		{
			Miscellaneous.logEvent("i", "CellLocation", "Cell mast changed, but only initial update, ignoring this one.", 4);
			followUpdate = true; //for next run
		}
	}

	public static boolean isCellLocationChangedReceiverPossible()
	{
		if(telephonyManager == null)
			telephonyManager = (TelephonyManager) AutomationService.getInstance().getSystemService(Context.TELEPHONY_SERVICE);

		if(
			ConnectivityReceiver.isAirplaneMode(AutomationService.getInstance())
										||
			telephonyManager.getSimState() != TelephonyManager.SIM_STATE_READY
		)
			return false;
		else
			return true;
	}

	public Location getLocation(String accuracy)
	{
		Criteria crit = new Criteria();
		
		String myProviderName;
		
		// If privacy mode or no data connection available
		if(Settings.privacyLocationing || !ConnectivityReceiver.isDataConnectionAvailable(AutomationService.getInstance()))
		{
			Miscellaneous.logEvent("i", "CellLocation", Miscellaneous.getAnyContext().getResources().getString(R.string.enforcingGps), 4);
			myProviderName = LocationManager.GPS_PROVIDER;
		}
		else
		{
			Miscellaneous.logEvent("i", "CellLocation", Miscellaneous.getAnyContext().getResources().getString(R.string.notEnforcingGps), 4);
			
			if(accuracy.equals("coarse"))
			{
				crit.setPowerRequirement(Criteria.POWER_LOW);
				crit.setAltitudeRequired(false);
				crit.setSpeedRequired(false);
				crit.setBearingRequired(false);
				crit.setCostAllowed(false);
				crit.setAccuracy(Criteria.ACCURACY_COARSE);
			}
			else //equals "fine"
			{
				crit.setPowerRequirement(Criteria.POWER_LOW);
				crit.setAltitudeRequired(false);
				crit.setSpeedRequired(false);
				crit.setBearingRequired(false);
				//crit.setCostAllowed(false);
				crit.setAccuracy(Criteria.ACCURACY_FINE);
			}
			
			myProviderName = myLocationManager.getBestProvider(crit, true);
		}
		
		if(myProviderName == null)
		{
			Toast.makeText(Miscellaneous.getAnyContext(), "No suitable location provider could be used.", Toast.LENGTH_LONG).show();
			return null;
		}
		else
		{
			if(myLocationManager == null)
				myLocationManager = (LocationManager) AutomationService.getInstance().getSystemService(Context.LOCATION_SERVICE);

			if(!myLocationManager.isProviderEnabled(myProviderName))
			{
				if(myProviderName.equals(LocationManager.NETWORK_PROVIDER))
					myProviderName = LocationManager.GPS_PROVIDER;
			}

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
				myProviderName = LocationManager.GPS_PROVIDER;

			// Arm location updates
			if(!locationListenerArmed)
				startLocationListener(myProviderName);
			
			try
			{
				return myLocationManager.getLastKnownLocation(myProviderName);
			}
			catch(NullPointerException e)
			{
				Toast.makeText(Miscellaneous.getAnyContext(), "No last known location. Aborting...", Toast.LENGTH_LONG).show();
				return null;
			}
		}
	}
	
	private void startLocationListener(String providerToBeUsed)
	{		
		Miscellaneous.logEvent("i", "LocationListener", "Arming location listener, Provider " + providerToBeUsed, 4);
		myLocationManager.requestLocationUpdates(providerToBeUsed, Settings.minimumTimeBetweenUpdate, Settings.minimumDistanceChangeForNetworkUpdate, myLocationListener);
		locationListenerArmed = true;

		// (re)set timeout
		if(timeoutHandlerActive)
			stopTimeOutHandler();
		startTimeOutHandler();
	}
	private void stopLocationListener()
	{
		Miscellaneous.logEvent("i", "LocationListener", "Disarming location listener.", 4);
		myLocationManager.removeUpdates(myLocationListener);
		locationListenerArmed = false;

		if(timeoutHandlerActive)
			stopTimeOutHandler();
	}
	
	public Location getCurrentLocation()
	{
		return currentLocation;
	}

	public void setCurrentLocation(Location currentLocation)
	{
		this.currentLocation = currentLocation;
	}

	public class MyLocationListener implements LocationListener
	{
		@Override
		public void onLocationChanged(Location up2DateLocation)
		{	
			if(timeoutHandlerActive)
			{
				stopTimeOutHandler();
			}
			
			setCurrentLocation(up2DateLocation);
			AutomationService.getInstance().getLocationProvider().setCurrentLocation(up2DateLocation, false);
			// This is relevant if the program just started, knows where it is, but hasn't reached any POI.
			// The below PointOfInterest.positionUpdate() will not update the notification in that case.
//			if(!currentLocation.equals(up2DateLocation))
//				parentLocationProvider.parentService.updateNotification();
			
			if(up2DateLocation.getAccuracy() < Settings.satisfactoryAccuracyNetwork)
			{
				myLocationManager.removeUpdates(this);
				locationListenerArmed = false;
				Miscellaneous.logEvent("i", "LocationListener", "Disarmed location listener, accuracy reached", 4);
			}

//			Miscellaneous.logEvent("i", "LocationListener", "Giving update to POI class");
//			PointOfInterest.positionUpdate(up2DateLocation, parentLocationProvider.parentService);
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
	
	class TimeoutHandler extends Handler
	{		
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			
			if(msg.what == 1)
			{
				Context context = Miscellaneous.getAnyContext();
				Miscellaneous.logEvent("i", context.getResources().getString(R.string.gpsMeasurement), context.getResources().getString(R.string.gpsMeasurementTimeout), 4);
				stopLocationListener();
			}
		}		
	}
	
	private void startTimeOutHandler()
	{
		if(timeoutHandler == null)
			timeoutHandler = new TimeoutHandler();
		
		Message message = new Message();
		message.what = 1;
		timeoutHandler.sendMessageDelayed(message, Settings.gpsTimeout * 1000);
		timeoutHandlerActive = true;
	}
	private void stopTimeOutHandler()
	{
		if(timeoutHandler == null)
			timeoutHandler = new TimeoutHandler();
		
		timeoutHandler.removeMessages(1);
		timeoutHandlerActive = false;
	}	

	public static void startCellLocationChangedReceiver()
	{
		if(telephonyManager == null)
			telephonyManager = (TelephonyManager) AutomationService.getInstance().getSystemService(Context.TELEPHONY_SERVICE);

		try
		{
			if(!cellLocationListenerActive)
			{				
				if(!ConnectivityReceiver.isAirplaneMode(AutomationService.getInstance()) && telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY)
				{
					if(WifiBroadcastReceiver.mayCellLocationReceiverBeActivated())
					{
	//					if(!ConnectivityReceiver.isDataConnectionAvailable(parentService))
	//					{
							telephonyManager.listen(getInstance(), PhoneStateListener.LISTEN_CELL_LOCATION);
							cellLocationListenerActive = true;
							Miscellaneous.logEvent("i", "cellReceiver", "Starting cellLocationListener", 4);
							
							SensorActivity.stopAccelerometerTimer();
							SensorActivity.stopAccelerometerReceiver();
		//					this.stopWifiReceiver();

                         /*
                            We could now set a timer when we could activate a location check.
                            If that fires we need to check if maybe another location check has been performed.
                         */

                        if(!LocationProvider.speedTimerActive)
                            LocationProvider.startSpeedTimer(LocationProvider.getEtaAtNextPoi());
	//					}
	//					else
	//						Miscellaneous.logEvent("i", "cellReceiver", "Not starting cellLocationListener because we have no data connection.", 4);
					}
					else
						Miscellaneous.logEvent("w", "cellReceiver", "Wanted to activate CellLocationChangedReceiver,  but Wifi-Receiver says not to.", 4);
				}
				else
					Miscellaneous.logEvent("i", "cellReceiver", "Not starting cellLocationListener because Airplane mode is active or SIM_STATE is not ready.", 4);
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "Wifi Listener", "Error starting cellLocationListener: " + Log.getStackTraceString(ex), 3);
		}
	}
	
	public static void stopCellLocationChangedReceiver()
	{
		try
		{
			if(cellLocationListenerActive)
			{
				Miscellaneous.logEvent("i", "cellReceiver", "Stopping cellLocationListener", 4);
				
				getInstance().stopTimeOutHandler();
				getInstance().stopLocationListener();

				if(LocationProvider.speedTimerActive)
				    LocationProvider.stopSpeedTimer();
				
				telephonyManager.listen(instance, PhoneStateListener.LISTEN_NONE);
				cellLocationListenerActive = false;
				
				// May have comparison measurements active.
				PointOfInterest.stopRoutine();
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "Wifi Listener", "Error stopping cellLocationListener: " + Log.getStackTraceString(ex), 3);
		}
	}

	public static void resetFollowUpdate()
	{
		followUpdate = false;
	}

	public static boolean haveAllPermission()
	{
		return ActivityPermissions.havePermission("android.permission.ACCESS_FINE_LOCATION", Miscellaneous.getAnyContext())
				&&
				ActivityPermissions.havePermission("android.permission.ACCESS_COARSE_LOCATION", Miscellaneous.getAnyContext())
				&&
				ActivityPermissions.havePermission("android.permission.ACCESS_NETWORK_STATE", Miscellaneous.getAnyContext())
				&&
				ActivityPermissions.havePermission("android.permission.INTERNET", Miscellaneous.getAnyContext())
				&&
				ActivityPermissions.havePermission("android.permission.ACCESS_WIFI_STATE", Miscellaneous.getAnyContext());
	}
}