package com.jens.automation2.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.jens.automation2.ActivityPermissions;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.Trigger.Trigger_Enum;

import java.util.ArrayList;

public class HeadphoneJackListener extends BroadcastReceiver implements AutomationListenerInterface
{
	private static boolean headsetConnected = false;
	private static int headphoneType = -1;

	protected static boolean headphoneJackListenerActive=false;
	protected static IntentFilter headphoneJackListenerIntentFilter = null;
	protected static HeadphoneJackListener instance;

	public static HeadphoneJackListener getInstance()
	{
		if(instance == null)
			instance = new HeadphoneJackListener();

		return instance;
	}

	public static boolean isHeadphoneJackListenerActive()
	{
		return headphoneJackListenerActive;
	}
	

	public static boolean isHeadsetConnected()
	{
		return headsetConnected;
	}
	
	public static int getHeadphoneType()
	{
		return headphoneType;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			/*Broadcast Action: Wired Headset plugged in or unplugged. 
			The intent will have the following extra values: 
	
			state - 0 for unplugged, 1 for plugged. 
			name - Headset type, human readable string 
			microphone - 1 if headset has a microphone, 0 otherwise*/
			
			int state = intent.getExtras().getInt("state");
			String name = intent.getExtras().getString("name");
			headphoneType = intent.getExtras().getInt("microphone");
			
			if(state == 0)
			{
				headsetConnected = false;
				Miscellaneous.logEvent("i", "HeadphoneJackListener", "Headset " + name + " unplugged.", 4);
			}
			else
			{
				headsetConnected = true;
				Miscellaneous.logEvent("i", "HeadphoneJackListener", "Headset " + name + " plugged in.", 4);
			}
			
			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesByHeadphoneJack(isHeadsetConnected());
			for(int i=0; i<ruleCandidates.size(); i++)
			{
				if((ruleCandidates.get(i).applies(context) && ruleCandidates.get(i).hasNotAppliedSinceLastExecution()) || ruleCandidates.get(i).isActuallyToggable())
					ruleCandidates.get(i).activate(AutomationService.getInstance(), false);
			}
		}
		catch(Exception e)
		{
			
		}
	}

	@Override
	public void startListener(AutomationService automationService)
	{
		if(headphoneJackListenerIntentFilter == null)
		{
			headphoneJackListenerIntentFilter = new IntentFilter();
			headphoneJackListenerIntentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		}

		try
		{
			if(!headphoneJackListenerActive && Rule.isAnyRuleUsing(Trigger_Enum.headsetPlugged))
			{
				Miscellaneous.logEvent("i", "HeadsetJackListener", "Starting HeadsetJackListener", 4);
				headphoneJackListenerActive = true;
//				getInstance().startHeadphoneJackListener(AutomationService.getInstance(), headphoneJackListenerIntentFilter);
				automationService.registerReceiver(this, headphoneJackListenerIntentFilter);
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "ActivityDetectionReceiver", "Error starting HeadsetJackListener: " + Log.getStackTraceString(ex), 3);
		}
	}

	@Override
	public void stopListener(AutomationService automationService)
	{
		try
		{
			if(headphoneJackListenerActive)
			{
				Miscellaneous.logEvent("i", "HeadsetJackListener", "Stopping HeadsetJackListener", 4);
//				getInstance().stopHeadphoneJackListener(AutomationService.getInstance());
				automationService.unregisterReceiver(this);
				headphoneJackListenerActive = false;
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "ActivityDetectionReceiver", "Error stopping HeadsetJackListener: " + Log.getStackTraceString(ex), 3);
		}
	}

	public static boolean haveAllPermission()
	{
		return ActivityPermissions.havePermission("android.permission.READ_PHONE_STATE", Miscellaneous.getAnyContext());
	}


	@Override
	public boolean isListenerRunning()
	{
		return HeadphoneJackListener.isHeadphoneJackListenerActive();
	}

	@Override
	public Trigger_Enum[] getMonitoredTrigger()
	{
		return new Trigger_Enum[] { Trigger_Enum.headsetPlugged };
	}

}
