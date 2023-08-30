package com.jens.automation2.receivers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

import com.jens.automation2.ActivityPermissions;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.Trigger.Trigger_Enum;

import java.util.ArrayList;

public class BatteryReceiver extends BroadcastReceiver implements AutomationListenerInterface
{
	public static AutomationService automationServiceRef = null;
	static int batteryLevel = -1;	// initialize with a better value than this
	static boolean usbHostConnected = false;
	static boolean batteryReceiverActive = false;
	static IntentFilter batteryIntentFilter = null;
	static Intent batteryStatus = null;
	static BroadcastReceiver batteryInfoReceiverInstance = null;

	public static void startBatteryReceiver(final AutomationService automationServiceRef)
	{
		if(!batteryReceiverActive)
		{
			BatteryReceiver.automationServiceRef = automationServiceRef;

			if(batteryInfoReceiverInstance == null)
				batteryInfoReceiverInstance = new BatteryReceiver();

			if(batteryIntentFilter == null)
			{
				batteryIntentFilter = new IntentFilter();
				batteryIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
				batteryIntentFilter.addAction(Intent.ACTION_BATTERY_LOW);
				//		batteryIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
				//		batteryIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
			}

			batteryStatus = automationServiceRef.registerReceiver(batteryInfoReceiverInstance, batteryIntentFilter);

			batteryReceiverActive = true;
		}
	}
	public static void stopBatteryReceiver()
	{
		if(batteryReceiverActive)
		{
			if(batteryInfoReceiverInstance != null)
			{
				automationServiceRef.unregisterReceiver(batteryInfoReceiverInstance);
				batteryInfoReceiverInstance = null;
			}

			batteryReceiverActive = false;
		}
	}

	public static boolean isBatteryReceiverActive()
	{
		return batteryReceiverActive;
	}

	public static boolean isUsbHostConnected()
	{
		return usbHostConnected;
	}

	public static int getBatteryLevel()
	{
		return batteryLevel;
	}

	private static int currentChargingState = 0; //0=unknown, 1=no, 2=yes

	public static int getCurrentChargingState()
	{
		return currentChargingState;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Miscellaneous.logEvent("i", "BatteryReceiver", "Received event " + intent.getAction(), 5);

		if (intent == null)
			return;
		if (context == null)
			return;

		if(intent.getAction().equals(Intent.ACTION_BATTERY_LOW))
		{
			Miscellaneous.logEvent("i", "Battery", "Low battery event", 5);
		}
		else
		{
			try
			{
				batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				//		int scale = -1;
				//	    int voltage = -1;
				//	    int temp = -1;
				//      scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				//      temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
				//      voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
				Log.i("Battery", "Level: " + String.valueOf(batteryLevel));
				this.actionBatteryLevel(context);

				int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
				int statusPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
				Miscellaneous.logEvent("i", "BatteryReceiver", "Status: " + String.valueOf(statusPlugged), 5);

				switch(statusPlugged)
				{
					case BatteryManager.BATTERY_PLUGGED_AC:
						//					Toast.makeText(context, "Regular charging", Toast.LENGTH_LONG).show();
						Miscellaneous.logEvent("i", "BatteryReceiver", "Regular charging.", 5);
						this.actionCharging(context);
						break;
					case BatteryManager.BATTERY_PLUGGED_USB:
						this.actionUsbConnected(context);
						break;
				}

				switch(status)
				{
					case BatteryManager.BATTERY_STATUS_CHARGING:
					case BatteryManager.BATTERY_STATUS_FULL:
						Miscellaneous.logEvent("i", "BatteryReceiver", "Device has been fully charged.", 5);
						this.actionCharging(context);
						break;
					case BatteryManager.BATTERY_STATUS_DISCHARGING:
					case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
						this.actionDischarging(context);
						break;
				}
			}
			catch(Exception e)
			{
				Miscellaneous.logEvent("e", "BatteryReceiver", "Error receiving battery status: " + e.getMessage(), 3);
			}
		}
	}

	public static int isDeviceCharging(Context context)
	{
		switch(currentChargingState)
		{
			case 0:
				Miscellaneous.logEvent("w", "ChargingInfo", "Status of device charging was requested. Information isn't available, yet.", 4);
				break;
			case 1:
				Miscellaneous.logEvent("i", "ChargingInfo", "Status of device charging was requested. Device is discharging.", 3);
				break;
			case BatteryManager.BATTERY_STATUS_CHARGING:
				Miscellaneous.logEvent("i", "ChargingInfo", "Status of device charging was requested. Device is charging.", 3);
				break;
		}

		return currentChargingState;
	}

	private void actionCharging(Context context)
	{
		if(currentChargingState != BatteryManager.BATTERY_STATUS_CHARGING) // Avoid flooding the log. This event will occur on a regular basis even though charging state wasn't changed.
		{
			Miscellaneous.logEvent("i", "BatteryReceiver", "Battery is charging or full.", 3);
			currentChargingState = BatteryManager.BATTERY_STATUS_CHARGING;
			//activate rule(s)
			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger_Enum.charging);
//			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesByCharging(true);
			for(int i=0; i<ruleCandidates.size(); i++)
			{
				if(ruleCandidates.get(i).getsGreenLight(context))
					ruleCandidates.get(i).activate(automationServiceRef, false);
			}
		}
	}

	private void actionBatteryLevel(Context context)
	{
		Miscellaneous.logEvent("i", "BatteryReceiver", "Battery level has changed.", 3);
		//activate rule(s)
		ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger_Enum.batteryLevel);
		for(int i=0; i<ruleCandidates.size(); i++)
		{
			if(ruleCandidates.get(i).getsGreenLight(context))
				ruleCandidates.get(i).activate(automationServiceRef, false);
		}
	}

	private void actionDischarging(Context context)
	{
		if(currentChargingState != BatteryManager.BATTERY_STATUS_UNKNOWN) // Avoid flooding the log. This event will occur on a regular basis even though charging state wasn't changed.
		{
			Miscellaneous.logEvent("i", "BatteryReceiver", "Battery is discharging.", 3);
			currentChargingState = BatteryManager.BATTERY_STATUS_UNKNOWN;
			//activate rule(s)
			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger_Enum.charging);
//			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesByCharging(false);
			for(int i=0; i<ruleCandidates.size(); i++)
			{
				if(ruleCandidates.get(i).getsGreenLight(context))
					ruleCandidates.get(i).activate(automationServiceRef, false);
			}

			this.actionUsbDisconnected(context);
		}
	}

	private void actionUsbConnected(Context context)
	{
		// Event usbConnected

//		Miscellaneous.logEvent("i", "BatteryReceiver", "BATTERY_PLUGGED_USB");

		if(!usbHostConnected)
		{
			usbHostConnected = true;
			Miscellaneous.logEvent("i", "BatteryReceiver", "Connected to computer.", 3);
//			Toast.makeText(context, "Connected to computer.", Toast.LENGTH_LONG).show();

			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger_Enum.usb_host_connection);
//			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesByUsbHost(true);
			for(Rule oneRule : ruleCandidates)
			{
				if(oneRule.getsGreenLight(context))
					oneRule.activate(automationServiceRef, false);
			}

			this.actionCharging(context);
		}
	}

	private void actionUsbDisconnected(Context context)
	{
		// Event usbDisConnected

		if(usbHostConnected)
		{
			usbHostConnected = false;
			Miscellaneous.logEvent("i", "BatteryReceiver", "Disconnected from computer.", 3);
//			Toast.makeText(context, "Disconnected from computer.", Toast.LENGTH_LONG).show();

			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger_Enum.usb_host_connection);
//			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesByUsbHost(false);
			for(Rule oneRule : ruleCandidates)
			{
				if(oneRule.getsGreenLight(context))
					oneRule.activate(automationServiceRef, false);
			}
		}
	}

	@Override
	public void startListener(AutomationService automationService)
	{
		BatteryReceiver.startBatteryReceiver(automationService);
	}

	@Override
	public void stopListener(AutomationService automationService)
	{
		BatteryReceiver.stopBatteryReceiver();
	}

	public static boolean haveAllPermission()
	{
		return ActivityPermissions.havePermission(Manifest.permission.READ_PHONE_STATE, Miscellaneous.getAnyContext()) &&
				ActivityPermissions.havePermission(Manifest.permission.BATTERY_STATS, Miscellaneous.getAnyContext());
	}

	@Override
	public boolean isListenerRunning()
	{
		return BatteryReceiver.isBatteryReceiverActive();
	}
	@Override
	public Trigger_Enum[] getMonitoredTrigger()
	{
		// actually monitores several
		return new Trigger_Enum[] { Trigger_Enum.batteryLevel, Trigger_Enum.charging, Trigger_Enum.usb_host_connection };
	}
}