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

public class ScreenStateReceiver extends BroadcastReceiver implements AutomationListenerInterface
{
	static int screenState = -1;	// initialize with a better value than this
	public static AutomationService automationServiceRef = null;

	private static boolean screenStateReceiverActive = false;
	private static IntentFilter screenStateIntentFilter = null;
	private static Intent screenStatusIntent = null;
	private static BroadcastReceiver screenStateReceiverInstance = null;

	public static void startScreenStateReceiver(final AutomationService automationServiceRef)
	{
		if(!screenStateReceiverActive)
		{
			ScreenStateReceiver.automationServiceRef = automationServiceRef;
			
			if(screenStateReceiverInstance == null)
				screenStateReceiverInstance = new ScreenStateReceiver();
			
			if(screenStateIntentFilter == null)
			{
				screenStateIntentFilter = new IntentFilter();
				screenStateIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
				screenStateIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
				screenStateIntentFilter.addAction(Intent.ACTION_USER_PRESENT);
//				Intent.ACTION_USER_UNLOCKED
			}
			
			screenStatusIntent = automationServiceRef.registerReceiver(screenStateReceiverInstance, screenStateIntentFilter);
			
			screenStateReceiverActive = true;
		}
	}
	public static void stopScreenStateReceiver()
	{
		if(screenStateReceiverActive)
		{
			if(screenStateReceiverInstance != null)
			{
				automationServiceRef.unregisterReceiver(screenStateReceiverInstance);
				screenStateReceiverInstance = null;
			}
			
			screenStateReceiverActive = false;
		}
	}
	
	public static boolean isScreenStateReceiverActive()
	{
		return screenStateReceiverActive;
	}

	public static int getScreenState()
	{
		return screenState;
	}

	private static int currentChargingState = 0; //0=unknown, 1=no, 2=yes
	
	public static int getCurrentChargingState()
	{
		return currentChargingState;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent == null)
			return;
		if (context == null)
			return;

		Miscellaneous.logEvent("e", "ScreenStateReceiver", "Received: " + intent.getAction(), 3);

		try
		{
			if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
			{
				ScreenStateReceiver.screenState = 1;
			}
			else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
			{
				ScreenStateReceiver.screenState = 0;
			}
			else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT))
			{
				ScreenStateReceiver.screenState = 2;
			}
			else
			{
				Miscellaneous.logEvent("e", "ScreenStateReceiver", "Unknown state received: " + intent.getAction(), 3);
			}
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "ScreenStateReceiver", "Error receiving screen state: " + e.getMessage(), 3);
		}
	}
	
	private void actionCharging(Context context, int state)
	{

		ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger_Enum.screenState);
		for(int i=0; i<ruleCandidates.size(); i++)
		{
			if(ruleCandidates.get(i).getsGreenLight(context))
				ruleCandidates.get(i).activate(automationServiceRef, false);
		}
	}

	@Override
	public void startListener(AutomationService automationService)
	{
		ScreenStateReceiver.startScreenStateReceiver(automationService);
	}
	@Override
	public void stopListener(AutomationService automationService)
	{
		ScreenStateReceiver.stopScreenStateReceiver();
	}

	public static boolean haveAllPermission()
	{
		return ActivityPermissions.havePermission(Manifest.permission.READ_PHONE_STATE, Miscellaneous.getAnyContext()) &&
			ActivityPermissions.havePermission(Manifest.permission.BATTERY_STATS, Miscellaneous.getAnyContext());
	}

	@Override
	public boolean isListenerRunning()
	{
		return ScreenStateReceiver.isScreenStateReceiverActive();
	}

	@Override
	public Trigger_Enum[] getMonitoredTrigger()
	{
		return new Trigger_Enum[] { Trigger_Enum.screenState };
	}
}