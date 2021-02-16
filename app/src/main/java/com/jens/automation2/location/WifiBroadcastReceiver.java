package com.jens.automation2.location;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.jens.automation2.Miscellaneous;
import com.jens.automation2.PointOfInterest;
import com.jens.automation2.R;
import com.jens.automation2.Rule;
import com.jens.automation2.Settings;

import java.util.ArrayList;

public class WifiBroadcastReceiver extends BroadcastReceiver
{
	public static LocationProvider parentLocationProvider;
	public static Boolean wasConnected = false;
	protected static String lastWifiSsid = "";
	public static boolean lastConnectedState = false;
	protected static boolean mayCellLocationChangedReceiverBeActivatedFromWifiPointOfWifi = true;
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
		
		WifiBroadcastReceiver.lastWifiSsid = newWifiSsid;
	}

	public static boolean isWifiListenerActive()
	{
		return wifiListenerActive;
	}

	public static boolean mayCellLocationReceiverBeActivated()
	{
		return mayCellLocationChangedReceiverBeActivatedFromWifiPointOfWifi;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		try
		{
	//		int state = -1;
			NetworkInfo myWifi = null;
			
	//		if(intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) //gefeuert bei Verbindung
	//		{
	//			Miscellaneous.logEvent("i", "WifiReceiver", "RSSI_CHANGED_ACTION: " + String.valueOf(intent.getIntExtra(WifiManager.RSSI_CHANGED_ACTION, -1)));
	//		}
	//		else 
			if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) //gefeuert bei Trennung
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
					mayCellLocationChangedReceiverBeActivatedFromWifiPointOfWifi = false;
					CellLocationChangedReceiver.stopCellLocationChangedReceiver();
				}
				else
				{
					if(!PointOfInterest.reachedPoiWithActivateWifiRule())	// Poi has no wifi
						Miscellaneous.logEvent("i", "WifiReceiver", context.getResources().getString(R.string.poiHasNoWifiNotStoppingCellLocationListener), 2);
				}
				
				findRules(parentLocationProvider);
			}
			else if(myWifi.isConnectedOrConnecting()) // first time connect from wifi-listener-perspective
			{
				wasConnected = true;
				Miscellaneous.logEvent("i", "WifiReceiver", "WifiReceiver just activated. Wifi already connected. Stopping CellLocationReceiver", 3);
				mayCellLocationChangedReceiverBeActivatedFromWifiPointOfWifi = false;
				CellLocationChangedReceiver.stopCellLocationChangedReceiver();
				SensorActivity.stopAccelerometerTimer();
				String ssid = myWifiManager.getConnectionInfo().getSSID();
				setLastWifiSsid(ssid);
				lastConnectedState = true;
				findRules(parentLocationProvider);
			}			
			else if(!myWifi.isConnectedOrConnecting()) // really disconnected? because sometimes also fires on connect
			{
				if(wasConnected) // wir könnten einfach noch nicht daheim sein
				{
					try
					{
						wasConnected = false;
						Miscellaneous.logEvent("i", "WifiReceiver", String.format(context.getResources().getString(R.string.disconnectedFromWifi), getLastWifiSsid()) + " Switching to CellLocationChangedReceiver.", 3);
						mayCellLocationChangedReceiverBeActivatedFromWifiPointOfWifi = true;
						CellLocationChangedReceiver.startCellLocationChangedReceiver();
						lastConnectedState = false;
						findRules(parentLocationProvider);
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
	
	public static void findRules(LocationProvider parentLocationProvider)
	{		
		ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesByWifiConnection();
		for(Rule oneRule : ruleCandidates)
		{
			if(oneRule.applies(parentLocationProvider.parentService))
				oneRule.activate(parentLocationProvider.parentService, false);
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