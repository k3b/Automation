package com.jens.automation2.receivers;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
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

	public final static String broadcastScreenLocked = "automation.system.screen_locked";

	public static BroadcastReceiver getScreenStateReceiverInstance()
	{
		if(screenStateReceiverInstance == null)
			screenStateReceiverInstance = new ScreenStateReceiver();

		return screenStateReceiverInstance;
	}

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
				screenStateIntentFilter.addAction(Intent.ACTION_USER_PRESENT);	// also fired when device is unlocked
				screenStateIntentFilter.addAction(broadcastScreenLocked);
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
			if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
			{
				ScreenStateReceiver.screenState = 0;

//				if(LockScreenHelper.isScreenUnlocked(context))
//					;

//				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//				KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//				if (pm.isInteractive() && pm.isScreenOn() && keyguardManager.isKeyguardLocked() && keyguardManager.isDeviceLocked())
//				{
//					//do your stuff
//				}

				KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
				boolean unlocked = kgMgr.inKeyguardRestrictedInputMode();
				if(!unlocked)
				{
					Intent lockedBroadcastIntent = new Intent();
					lockedBroadcastIntent.setAction(broadcastScreenLocked);
					context.sendBroadcast(lockedBroadcastIntent);
				}
			}
			else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
			{
				ScreenStateReceiver.screenState = 1;
			}
			else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT))
			{
				ScreenStateReceiver.screenState = 2;
			}
			else if(intent.getAction().equals(broadcastScreenLocked))
			{
				ScreenStateReceiver.screenState = 3;
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

	public static class LockScreenHelper
	{
		private static final String TAG = LockScreenHelper.class.getCanonicalName();

		/**
		 * Determine if the screen is on and the device is unlocked;
		 * i.e. the user will see what is going on in the main activity.
		 *
		 * @param context Context
		 * @return boolean
		 */
		public static boolean isScreenUnlocked(Context context)
		{
			if (!isInteractive(context))
			{
				Log.i(TAG, "device is NOT interactive");
				return false;
			}
			else
			{
				Log.i(TAG, "device is interactive");
			}

			if (!isDeviceProvisioned(context))
			{
				Log.i(TAG, "device is not provisioned");
				return true;
			}

			Object keyguardService = context.getSystemService(Context.KEYGUARD_SERVICE);
			return !((KeyguardManager) keyguardService).inKeyguardRestrictedInputMode();
		}

		/**
		 * @return Whether the screen of the device is interactive (screen may or may not be locked at the time).
		 */
		@SuppressWarnings("deprecation")
		public static boolean isInteractive(Context context)
		{
			PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH)
			{
				return manager.isInteractive();
			}
			else
			{
				return manager.isScreenOn();
			}
		}

		/**
		 * @return Whether the device has been provisioned (0 = false, 1 = true).
		 * On a multiuser device with a separate system user, the screen may be locked as soon as this
		 * is set to true and further activities cannot be launched on the system user unless they are
		 * marked to show over keyguard.
		 */
		private static boolean isDeviceProvisioned(Context context)
		{
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				return true;
			}

			if (context == null)
			{
				return true;
			}

			if (context.getContentResolver() == null)
			{
				return true;
			}

			return android.provider.Settings.Global.getInt(context.getContentResolver(), android.provider.Settings.Global.DEVICE_PROVISIONED, 0) != 0;
		}
	}
}