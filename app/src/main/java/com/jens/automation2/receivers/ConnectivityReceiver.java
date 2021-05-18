package com.jens.automation2.receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.jens.automation2.ActivityPermissions;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.Trigger.Trigger_Enum;
import com.jens.automation2.location.WifiBroadcastReceiver;

import java.util.ArrayList;

public class ConnectivityReceiver extends BroadcastReceiver implements AutomationListenerInterface
{
	protected static boolean connectivityReceiverActive = false;
	private static ConnectivityReceiver connectivityReceiverInstance = null;
	private static IntentFilter connectivityIntentFilter = null;
	private static AutomationService automationServiceRef = null;
	protected static boolean dataConnectionLastState = false;	
	protected static boolean roamingLastState = false;
	
	public static boolean isConnectivityReceiverActive()
	{
		return connectivityReceiverActive;
	}

	public static void startConnectivityReceiver(AutomationService ref)
	{
		automationServiceRef = ref;
		
		if(connectivityReceiverInstance == null)
			connectivityReceiverInstance = new ConnectivityReceiver();
		
		
		if(connectivityIntentFilter == null)
		{
			connectivityIntentFilter = new IntentFilter();
			connectivityIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			connectivityIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		}
		
		try
		{
			if(!connectivityReceiverActive)
			{
				Miscellaneous.logEvent("i", "Wifi Listener", "Starting connectivityReceiver", 4);
				connectivityReceiverActive = true;
				automationServiceRef.registerReceiver(connectivityReceiverInstance, connectivityIntentFilter);
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "Wifi Listener", "Error starting connectivityReceiver: " + Log.getStackTraceString(ex), 3);
		}
	}


	public static void stopConnectivityReceiver()
	{
		try
		{
			if(connectivityReceiverActive)
			{
				Miscellaneous.logEvent("i", "Wifi Listener", "Stopping connectivityReceiver", 4);
				connectivityReceiverActive = false;
				automationServiceRef.unregisterReceiver(connectivityReceiverInstance);
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "Wifi Listener", "Error stopping connectivityReceiver: " + Log.getStackTraceString(ex), 3);
		}
	}

	// Get roaming state from telephony manager
	public static Boolean isRoaming(Context context)
	{
	  TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	  return telephonyManager.isNetworkRoaming();
	}

	public static void setDataConnectionLastState(boolean newState)
	{
		if(dataConnectionLastState != newState)
		{
			dataConnectionLastState = newState;
			
			// Run rules if I decide to create such a trigger
//			automationServiceRef.getLocationProvider().handleDataConnectionChange(newState);
		}
	}
	public static Boolean isDataConnectionAvailable(Context context)
	{
	  ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	  NetworkInfo ni = connManager.getActiveNetworkInfo();
	  return ni != null && ni.isConnected();
	}

	// Get airplane mode state from system settings
	@SuppressLint("NewApi")
	public static boolean isAirplaneMode(Context context)
	{
	  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
		  	int value = android.provider.Settings.System.getInt(context.getContentResolver(), android.provider.Settings.System.AIRPLANE_MODE_ON, 0);
			return value != 0;
	    }
		else
		{
			return android.provider.Settings.Global.getInt(context.getContentResolver(), android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
	    }
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{		
		try
		{
			if (context == null)
				return;

			if(intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED))
			{
				// Airplane mode status has changed.
				Miscellaneous.logEvent("i", "Connectivity", "Airplane mode changed.", 2);
				boolean isAirplaneMode = isAirplaneMode(context);
				automationServiceRef.getLocationProvider().handleAirplaneMode(isAirplaneMode);

				ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesByAirplaneMode(isAirplaneMode);
				for(int i=0; i<ruleCandidates.size(); i++)
				{
					if(ruleCandidates.get(i).applies(automationServiceRef))
						ruleCandidates.get(i).activate(automationServiceRef, false);
				}
			}
			else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
			{
				ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//				NetworkInfo ni = connManager.getActiveNetworkInfo();
				NetworkInfo ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				if(ni != null)
				{
					Miscellaneous.logEvent("i", "Connectivity", "Change of network with type " + ni.getType() + " noticed.", 4);
					
					switch(ni.getType())
					{
						case ConnectivityManager.TYPE_WIFI:
							WifiBroadcastReceiver.lastConnectedState = ni.isConnected();
							WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
						    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
							WifiBroadcastReceiver.setLastWifiSsid(wifiInfo.getSSID());
							WifiBroadcastReceiver.findRules(automationServiceRef);
							break;
						case ConnectivityManager.TYPE_MOBILE:
							boolean isRoaming = isRoaming(context);
							if(isRoaming != roamingLastState)
							{
								roamingLastState = isRoaming;
								
								automationServiceRef.getLocationProvider().handleRoaming(isRoaming);
	
								ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesByRoaming(isRoaming);
								for(int i=0; i<ruleCandidates.size(); i++)
								{
									if(ruleCandidates.get(i).applies(automationServiceRef))
										ruleCandidates.get(i).activate(automationServiceRef, false);
								}
							}
							break;
//						case ConnectivityManager.TYPE_BLUETOOTH:
							
							
							/*
							 * BluetoothAdapter.ACTION_STATE_CHANGED ("android.bluetooth.adapter.action.STATE_CHANGED")

								Broadcast Action: The state of the local Bluetooth adapter has been changed. For example, Bluetooth has been turned on or off.
								
								and for Ringer mode changes:
								
								AudioManager.RINGER_MODE_CHANGED_ACTION ("android.media.RINGER_MODE_CHANGED")
								
								Sticky broadcast intent action indicating that the ringer mode has changed. Includes the new ringer mode.
								
								Not a ringer mode change, but this can be good to have also AudioManager.VIBRATE_SETTING_CHANGED_ACTION ("android.media.VIBRATE_SETTING_CHANGED")
								
								Broadcast intent action indicating that the vibrate setting has changed. Includes the vibrate type and its new setting.
							 */
//							BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//							if(bluetoothDevice.getBondState()
//							if(BluetoothDevice.)
//							ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesByRoaming(isRoaming);
//							for(int i=0; i<ruleCandidates.size(); i++)
//							{
//								if(ruleCandidates.get(i).applies(parentLocationProvider.getParentService()))
//									ruleCandidates.get(i).activate(parentLocationProvider.getParentService());
//							}
//							break;
						default:
							Miscellaneous.logEvent("i", "Connectivity", "Type of changed network not specified. Doing nothing.", 4);
					}
				}
				else
				{					
					NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					if(!wifiInfo.isAvailable() && WifiBroadcastReceiver.lastConnectedState)
					{
						// This will serve as a disconnected event. Happens if wifi is connected, then module deactivated.
						Miscellaneous.logEvent("i", "Connectivity", "Wifi deactivated while having been connected before.", 4);
						WifiBroadcastReceiver.lastConnectedState = false;
						WifiBroadcastReceiver.findRules(automationServiceRef);
					}
				}
			}
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "Connectivity", "Error in ConnectivityReceiver->onReceive(): " + Log.getStackTraceString(e), 3);
		}
	}

	@Override
	public void startListener(AutomationService automationService)
	{
		ConnectivityReceiver.startConnectivityReceiver(automationService);
	}

	@Override
	public void stopListener(AutomationService automationService)
	{
		ConnectivityReceiver.stopConnectivityReceiver();
	}

	public static boolean haveAllPermission()
	{
		return ActivityPermissions.havePermission("android.permission.ACCESS_NETWORK_STATE", Miscellaneous.getAnyContext()) &&
				ActivityPermissions.havePermission("android.permission.ACCESS_WIFI_STATE", Miscellaneous.getAnyContext()) &&
				ActivityPermissions.havePermission("android.permission.ACCESS_NETWORK_STATE", Miscellaneous.getAnyContext());
	}

	@Override
	public boolean isListenerRunning()
	{
		return ConnectivityReceiver.isConnectivityReceiverActive();
	}

	@Override
	public Trigger_Enum[] getMonitoredTrigger()
	{
		return new Trigger_Enum[] { Trigger_Enum.airplaneMode, Trigger_Enum.roaming, Trigger_Enum.wifiConnection };
	}
}
