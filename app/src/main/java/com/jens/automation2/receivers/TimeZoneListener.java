package com.jens.automation2.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.Trigger.Trigger_Enum;

public class TimeZoneListener extends BroadcastReceiver implements AutomationListenerInterface
{		
	private static TimeZoneListener timeZoneListenerInstance = null;
	protected static boolean timeZoneListenerActive = false;
	protected static AutomationService automationServiceRef = null;
	protected static IntentFilter timeZoneListenerIntentFilter = null;

	
	public static boolean isTimeZoneListenerActive()
	{
		return timeZoneListenerActive;
	}
	
	public static void startTimeZoneListener(AutomationService automationService)
	{
		if(timeZoneListenerInstance == null)
			timeZoneListenerInstance = new TimeZoneListener();
		
		automationServiceRef = automationService;
		
		try
		{
			if(!timeZoneListenerActive && Rule.isAnyRuleUsing(Trigger_Enum.timeFrame))
			{
				Miscellaneous.logEvent("i", "TimeZoneListener", "Starting TimeZoneListener", 4);
				timeZoneListenerActive = true;

				if(timeZoneListenerIntentFilter == null)
				{
					timeZoneListenerIntentFilter = new IntentFilter();
					timeZoneListenerIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
					timeZoneListenerIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
				}
				
				automationService.registerReceiver(timeZoneListenerInstance, timeZoneListenerIntentFilter);
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "TimeZoneListener", "Error starting TimeZoneListener: " + Log.getStackTraceString(ex), 3);
		}
	}
	public static void stopTimeZoneListener()
	{
		try
		{
			if(timeZoneListenerActive)
			{
				Miscellaneous.logEvent("i", "TimeZoneListener", "Stopping TimeZoneListener", 4);
				automationServiceRef.unregisterReceiver(timeZoneListenerInstance);
				timeZoneListenerActive = false;
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "TimeZoneListener", "Error stopping TimeZoneListener: " + Log.getStackTraceString(ex), 3);
		}
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		if(action.equals(Intent.ACTION_TIMEZONE_CHANGED))
		{
			Miscellaneous.logEvent("i", "TimeZoneListener", "Device timezone changed. Reloading alarms.", 3);
			AlarmListener.reloadAlarms();
		}
		else if(action.equals(Intent.ACTION_TIME_CHANGED))
		{
			Miscellaneous.logEvent("i", "TimeZoneListener", "Device time changed. Reloading alarms.", 4);
			AlarmListener.reloadAlarms();
		}		
	}
	@Override
	public void startListener(AutomationService automationService)
	{
		TimeZoneListener.startTimeZoneListener(automationService);
	}
	@Override
	public void stopListener(AutomationService automationService)
	{
		TimeZoneListener.stopTimeZoneListener();
	}

	public static boolean haveAllPermission()
	{
		return true;
	}

	@Override
	public boolean isListenerRunning()
	{
		return TimeZoneListener.isTimeZoneListenerActive();
	}
	@Override
	public Trigger_Enum[] getMonitoredTrigger()
	{
		return null;
	}
}
