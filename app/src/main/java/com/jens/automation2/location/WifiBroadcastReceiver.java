package com.jens.automation2.location;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.PointOfInterest;
import com.jens.automation2.R;
import com.jens.automation2.Rule;
import com.jens.automation2.Settings;
import com.jens.automation2.Trigger;

import java.util.ArrayList;

public class WifiBroadcastReceiver extends BroadcastReceiver
{
	public static LocationProvider parentLocationProvider;
	public static Boolean wasConnected = false;
	protected static String lastWifiSsid = "";
	public static boolean lastConnectedState = false;
	protected static boolean mayCellLocationChangedReceiverBeActivatedFromWifiPointOfView = true;
	protected static WifiBroadcastReceiver wifiBrInstance;
	protected static IntentFilter wifiListenerIntentFilter;
	protected static boolean wifiListenerActive=false;

	public static String getLastWifiSsid()
	{
		return lastWifiSsid;
	}

	public static void setLastWifiSsid(String newWifiSsid)
	{
		if(newWifiSsid.startsWith("\"") && newWifiSsid.endsWith("\""))
			newWifiSsid = newWifiSsid.substring(1, newWifiSsid.length()-1);

		if(newWifiSsid.length() > 0)
			WifiBroadcastReceiver.lastWifiSsid = newWifiSsid;
	}

	public static boolean isWifiListenerActive()
	{
		return wifiListenerActive;
	}

	public static boolean mayCellLocationReceiverBeActivated()
	{
		return mayCellLocationChangedReceiverBeActivatedFromWifiPointOfView;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		try
		{
	//		int state = -1;
			NetworkInfo myWifi = null;
			
			if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) // fired upon disconnection
			{
	//			state = intent.getIntExtra(WifiManager.NETWORK_STATE_CHANGED_ACTION, -1);
	//			Miscellaneous.logEvent("i", "WifiReceiver", "NETWORK_STATE_CHANGED_ACTION: " + String.valueOf(state));
				myWifi = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			}
			
			WifiManager myWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	//		ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
	//		myWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	//		myWifi = state
	//		WifiInfo wifiInfo = myWifiManager.getConnectionInfo();
			
	//		SupplicantState supState = wifiInfo.getSupplicantState();
	
			if(intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) // fired upon connection
			{
				String ssid = myWifiManager.getConnectionInfo().getSSID();
				setLastWifiSsid(ssid);
				Miscellaneous.logEvent("i", "WifiReceiver", String.format(context.getResources().getString(R.string.connectedToWifi), getLastWifiSsid()), 2);
				wasConnected = true;
				lastConnectedState = true;
				
				if(Settings.useWifiForPositioning && PointOfInterest.reachedPoiWithActivateWifiRule())	// Poi has wifi
				{
					Miscellaneous.logEvent("i", "WifiReceiver", context.getResources().getString(R.string.poiHasWifiStoppingCellLocationListener), 2);
					mayCellLocationChangedReceiverBeActivatedFromWifiPointOfView = false;
					CellLocationChangedReceiver.stopCellLocationChangedReceiver();

					/*
						TODO: Every time the screen is turned on, we receiver a "wifi has been connected"-event.
						This is technically wrong and not really any changed to when the screen was off. It has
						to be filtered.
					 */
				}
				else
				{
					if(!PointOfInterest.reachedPoiWithActivateWifiRule())	// Poi has no wifi
						Miscellaneous.logEvent("i", "WifiReceiver", context.getResources().getString(R.string.poiHasNoWifiNotStoppingCellLocationListener), 2);
				}
				
				findRules(AutomationService.getInstance());
			}
			else if(myWifi.isConnectedOrConnecting()) // first time connect from wifi-listener-perspective
			{
				wasConnected = true;
				Miscellaneous.logEvent("i", "WifiReceiver", "WifiReceiver just activated. Wifi already connected. Stopping CellLocationReceiver", 3);
				mayCellLocationChangedReceiverBeActivatedFromWifiPointOfView = false;
				CellLocationChangedReceiver.stopCellLocationChangedReceiver();
				SensorActivity.stopAccelerometerTimer();
				String ssid = myWifiManager.getConnectionInfo().getSSID();
				setLastWifiSsid(ssid);
				lastConnectedState = true;
				findRules(AutomationService.getInstance());
			}			
			else if(!myWifi.isConnectedOrConnecting()) // really disconnected? because sometimes also fires on connect
			{
				if(wasConnected) // wir könnten einfach noch nicht daheim sein
				{
					try
					{
						wasConnected = false;
						Miscellaneous.logEvent("i", "WifiReceiver", String.format(context.getResources().getString(R.string.disconnectedFromWifi), getLastWifiSsid()) + " Switching to CellLocationChangedReceiver.", 3);
						mayCellLocationChangedReceiverBeActivatedFromWifiPointOfView = true;
						CellLocationChangedReceiver.startCellLocationChangedReceiver();
						lastConnectedState = false;
						findRules(AutomationService.getInstance());
					}
					catch(Exception e)
					{
						Miscellaneous.logEvent("e", "WifiReceiver", "Error starting CellLocationChangedReceiver", 3);
					}
				}
			}
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "WifiReceiver", "Error in WifiReceiver->onReceive(): " + e.getMessage(), 3);
		}
	}
	
	public static void findRules(AutomationService automationServiceInstance)
	{		
		ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger.Trigger_Enum.wifiConnection);
		for(Rule oneRule : ruleCandidates)
		{
			if(oneRule.getsGreenLight(automationServiceInstance))
				oneRule.activate(automationServiceInstance, false);
		}
	}

	public static boolean isWifiEnabled(Context context)
	{
		try
		{
			WifiManager myWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			return myWifiManager.isWifiEnabled();
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "WifiReceiver->isWifiEnabled()", Log.getStackTraceString(e), 3);
			return false;
		}
	}

	public static boolean isWifiConnected(Context context)
	{
		try
		{
			ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo myWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			return myWifi.isConnected();
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "WifiReceiver->isWifiConnected()", Log.getStackTraceString(e), 3);
			return false;
		}
	}
	
	public static void startWifiReceiver(LocationProvider loc)
	{
		try
		{
			if(!wifiListenerActive)
			{
				Miscellaneous.logEvent("i", "Wifi Listener", "Starting wifiListener", 4);
				if(wifiListenerIntentFilter == null)
				{
					wifiListenerIntentFilter = new IntentFilter();
					wifiListenerIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
					wifiListenerIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
				}
				if(wifiBrInstance == null)
				{
					wifiBrInstance = new WifiBroadcastReceiver();
					WifiBroadcastReceiver.parentLocationProvider = loc;
				}
				loc.getParentService().registerReceiver(wifiBrInstance, wifiListenerIntentFilter);
				wifiListenerActive = true;
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "Wifi Listener", "Error starting wifiListener: " + Log.getStackTraceString(ex), 3);
		}
	}
	public static void stopWifiReceiver()
	{
		try
		{
			if(wifiListenerActive)
			{
				Miscellaneous.logEvent("i", "Wifi Listener", "Stopping wifiListener", 4);
				wifiListenerActive = false;
				parentLocationProvider.getParentService().unregisterReceiver(wifiBrInstance);
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "Wifi Listener", "Error stopping wifiListener: " + Log.getStackTraceString(ex), 3);
		}
	}
	
}