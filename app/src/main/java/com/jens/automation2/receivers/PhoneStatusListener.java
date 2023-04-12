package com.jens.automation2.receivers;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.jens.automation2.ActivityPermissions;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.R;
import com.jens.automation2.Rule;
import com.jens.automation2.Trigger;
import com.jens.automation2.Trigger.Trigger_Enum;

import java.util.ArrayList;

public class PhoneStatusListener implements AutomationListenerInterface
{
	static int problematicAndroidLevel = 29;
	protected static String lastPhoneNumber="";
	protected static int lastPhoneDirection = -1; //0=incoming, 1=outgoing
	protected static int currentState = -1;
	
	protected static boolean incomingCallsReceiverActive = false;
	protected static boolean outgoingCallsReceiverActive = false;
	
	protected static IntentFilter outgoingCallsIntentFilter;
	protected static IncomingCallsReceiver incomingCallsReceiverInstance;
	protected static BroadcastReceiver outgoingCallsReceiverInstance;
	
	public static boolean isIncomingCallsReceiverActive()
	{
		return incomingCallsReceiverActive;
	}

	public static boolean isOutgoingCallsReceiverActive()
	{
		return outgoingCallsReceiverActive;
	}

	protected static boolean receivedInitialIncomingSignal = false;
	
	public static int getLastPhoneDirection()
	{
		return lastPhoneDirection;
	}

	protected static void setLastPhoneNumber(String lastPhoneNumber)
	{
		PhoneStatusListener.lastPhoneNumber = lastPhoneNumber;
	}

	public static String getLastPhoneNumber()
	{
		return lastPhoneNumber;
	}

	public static void setCurrentState(int currentState)
	{
		PhoneStatusListener.currentState = currentState;
	}

	public static int getCurrentState()
	{
		return currentState;
	}

	public static interface IncomingCallsReceiver
	{

	}

	protected static void workWithIncomingCallData(int state, String incomingNumber)
	{
		if(lastPhoneDirection == 2 && currentState != TelephonyManager.CALL_STATE_IDLE)
		{
			// This status update is actually for an outgoing call
			setCurrentState(state);

			if(incomingNumber != null && incomingNumber.length() > 0)		// check for null in case call comes in with suppressed number.
				setLastPhoneNumber(incomingNumber);

			switch(state)
			{
				case TelephonyManager.CALL_STATE_IDLE:
					Miscellaneous.logEvent("i", "Call state", "New call state: CALL_STATE_IDLE", 4);
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					Miscellaneous.logEvent("i", "Call state", "New call state: CALL_STATE_OFFHOOK", 4);
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					Miscellaneous.logEvent("i", "Call state", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.outgoingCallTo), incomingNumber), 4);
					break;
			}

			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger_Enum.phoneCall);
			for(int i=0; i<ruleCandidates.size(); i++)
			{
				AutomationService asInstance = AutomationService.getInstance();
				if(asInstance != null)
					if(ruleCandidates.get(i).getsGreenLight(asInstance))
						ruleCandidates.get(i).activate(asInstance, false);
			}
		}
		else
		{
			setCurrentState(state);
			setLastPhoneDirection(1);

			if (incomingNumber != null && incomingNumber.length() > 0)        // check for null in case call comes in with suppressed number.
				setLastPhoneNumber(incomingNumber);

			switch (state)
			{
				case TelephonyManager.CALL_STATE_IDLE:
					Miscellaneous.logEvent("i", "Call state", "New call state: CALL_STATE_IDLE", 4);
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					Miscellaneous.logEvent("i", "Call state", "New call state: CALL_STATE_OFFHOOK", 4);
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					Miscellaneous.logEvent("i", "Call state", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.incomingCallFrom), incomingNumber), 4);
					break;
			}

			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger_Enum.phoneCall);
			for (int i = 0; i < ruleCandidates.size(); i++)
			{
				AutomationService asInstance = AutomationService.getInstance();
				if (asInstance != null)
					if (ruleCandidates.get(i).getsGreenLight(asInstance))
						ruleCandidates.get(i).activate(asInstance, false);
			}
		}
	}

	public static class IncomingCallsReceiverOld extends PhoneStateListener implements IncomingCallsReceiver
	{
		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
//			Miscellaneous.logEvent("i", "Call state", "New call state: " + String.valueOf(state), 4);

			/*
				Unfortunately receivers for incoming and outgoing calls behave pretty differently:

				The Outgoing-Receiver is called when starting a call (ringing)
				It is not called when that outgoing call ends however, only the incoming receiver.

				If the last call was outgoing the state has not changed to idle this is kind of a fake alert.
			 */

			workWithIncomingCallData(state, incomingNumber);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.S)
	public static class IncomingCallsReceiverNew extends BroadcastReceiver implements IncomingCallsReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			/*
				this code detects both incoming and outgoing,
				if the state changes idle => ringing you know it's an incoming call,
				if the state changes idle => offhook, you know it's an outgoing call
			*/

			if (!intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL))
			{
				String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
				int state = 99;

				switch(stateStr)
				{
					case "RINGING":
						state = TelephonyManager.CALL_STATE_RINGING;
						break;
					case "IDLE":
						state = TelephonyManager.CALL_STATE_IDLE;
						break;
					case "OFFHOOK":
						state = TelephonyManager.CALL_STATE_OFFHOOK;
						break;
				}

				String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
				Log.i("test", "test");
			}
		}
	}

	static void setLastPhoneDirection(int i)
	{
		lastPhoneDirection = i;
	}
	
	public static class OutgoingCallsReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			/*
				This receiver is ONLY triggered when outgoing calls ring, not when that call is established or ends.
			 */
			setLastPhoneDirection(2);

			setCurrentState(TelephonyManager.CALL_STATE_RINGING);

			String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			setLastPhoneNumber(phoneNumber);
			Miscellaneous.logEvent("i", "Call state", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.outgoingCallTo), getLastPhoneNumber()), 4);

			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger_Enum.phoneCall);
			for(int i = 0; i < ruleCandidates.size(); i++)
			{
				AutomationService asInstance = AutomationService.getInstance();
				if(asInstance != null)
				if(ruleCandidates.get(i).getsGreenLight(asInstance))
					ruleCandidates.get(i).activate(asInstance, false);
			}
        }		
	}
	
	public static boolean isInACall()
	{
		return getCurrentState() != TelephonyManager.CALL_STATE_IDLE;
	}

	/*
		Future remark:
		Apps that redirect outgoing calls should use the android.telecom.CallRedirectionService API.
		Apps that perform call screening should use the android.telecom.CallScreeningService API.
	 */
	
	public static void startPhoneStatusListener(AutomationService automationService)
	{
		if(outgoingCallsIntentFilter == null)
		{
			outgoingCallsIntentFilter = new IntentFilter();
			outgoingCallsIntentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		}
		
		if(incomingCallsReceiverInstance == null)
		{
			if(Build.VERSION.SDK_INT >= 31)
				incomingCallsReceiverInstance = new IncomingCallsReceiverNew();
			else
				incomingCallsReceiverInstance = new IncomingCallsReceiverOld();
		}
		
		if(outgoingCallsReceiverInstance == null)
			outgoingCallsReceiverInstance = new OutgoingCallsReceiver();
		
		try
		{
			if(!incomingCallsReceiverActive)
			{
				Miscellaneous.logEvent("i", "PhoneStatusListener", "Starting PhoneStatusListener->incomingCallsReceiver", 4);
				if(Build.VERSION.SDK_INT >= problematicAndroidLevel)
				{
					IntentFilter callsFilter = new IntentFilter();
					callsFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
					automationService.registerReceiver((IncomingCallsReceiverNew)incomingCallsReceiverInstance, callsFilter);
				}
				else
				{
					TelephonyManager tm = (TelephonyManager) automationService.getSystemService(Context.TELEPHONY_SERVICE);
					tm.listen((IncomingCallsReceiverOld)incomingCallsReceiverInstance, PhoneStateListener.LISTEN_CALL_STATE);
				}
				incomingCallsReceiverActive = true;
			}
			
			if(!outgoingCallsReceiverActive)
			{
				Miscellaneous.logEvent("i", "PhoneStatusListener", "Starting PhoneStatusListener->outgoingCallsReceiver", 4);
				automationService.registerReceiver(outgoingCallsReceiverInstance, outgoingCallsIntentFilter);
				outgoingCallsReceiverActive = true;
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "PhoneStatusListener", "Error starting PhoneStatusListener: " + Log.getStackTraceString(ex), 3);
		}
	}

	public static void stopPhoneStatusListener(AutomationService automationService)
	{
		try
		{
			if(incomingCallsReceiverActive)
			{
				Miscellaneous.logEvent("i", "PhoneStatusListener", "Stopping phoneStatusListener", 4);
				if(Build.VERSION.SDK_INT >= 31)
				{
					automationService.unregisterReceiver((IncomingCallsReceiverNew)incomingCallsReceiverInstance);
				}
				else
				{
					TelephonyManager tm = (TelephonyManager) automationService.getSystemService(Context.TELEPHONY_SERVICE);
					tm.listen((IncomingCallsReceiverOld)incomingCallsReceiverInstance, PhoneStateListener.LISTEN_NONE);
				}
				incomingCallsReceiverActive = false;
			}
			
			if(outgoingCallsReceiverActive)
			{
				Miscellaneous.logEvent("i", "PhoneStatusListener", "Stopping phoneStatusListener", 4);
				automationService.unregisterReceiver(outgoingCallsReceiverInstance);
				outgoingCallsReceiverActive = false;
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "PhoneStatusListener", "Error stopping phoneStatusListener: " + Log.getStackTraceString(ex), 3);
		}
	}

	@Override
	public void startListener(AutomationService automationService)
	{
		PhoneStatusListener.startPhoneStatusListener(automationService);
	}

	@Override
	public void stopListener(AutomationService automationService)
	{
		PhoneStatusListener.stopPhoneStatusListener(automationService);
	}

	public static boolean haveAllPermission()
	{
		return
			ActivityPermissions.havePermission(Manifest.permission.READ_PHONE_STATE, Miscellaneous.getAnyContext())
						&&
			ActivityPermissions.havePermission(Manifest.permission.PROCESS_OUTGOING_CALLS, Miscellaneous.getAnyContext());
	}

	@Override
	public boolean isListenerRunning()
	{
		return PhoneStatusListener.incomingCallsReceiverActive | PhoneStatusListener.isOutgoingCallsReceiverActive();
	}

	@Override
	public Trigger_Enum[] getMonitoredTrigger()
	{
		return new Trigger_Enum[] { Trigger_Enum.phoneCall };
	}
}