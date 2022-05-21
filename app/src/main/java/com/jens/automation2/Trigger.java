package com.jens.automation2;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.jens.automation2.location.LocationProvider;
import com.jens.automation2.location.WifiBroadcastReceiver;
import com.jens.automation2.receivers.BatteryReceiver;
import com.jens.automation2.receivers.BluetoothReceiver;
import com.jens.automation2.receivers.ConnectivityReceiver;
import com.jens.automation2.receivers.DeviceOrientationListener;
import com.jens.automation2.receivers.HeadphoneJackListener;
import com.jens.automation2.receivers.MediaPlayerListener;
import com.jens.automation2.receivers.NfcReceiver;
import com.jens.automation2.receivers.NoiseListener;
import com.jens.automation2.receivers.NotificationListener;
import com.jens.automation2.receivers.PhoneStatusListener;
import com.jens.automation2.receivers.ProcessListener;
import com.jens.automation2.receivers.ScreenStateReceiver;

import static com.jens.automation2.Trigger.triggerParameter2Split;
import static com.jens.automation2.receivers.NotificationListener.EXTRA_TEXT;
import static com.jens.automation2.receivers.NotificationListener.EXTRA_TITLE;

import org.apache.commons.lang3.StringUtils;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Trigger
{
	public enum Trigger_Enum {
		pointOfInterest, timeFrame, charging, batteryLevel, usb_host_connection, speed, noiseLevel, wifiConnection, process_started_stopped, airplaneMode, roaming, nfcTag, activityDetection, bluetoothConnection, headsetPlugged, notification, deviceOrientation, profileActive, screenState, musicPlaying, phoneCall; //phoneCall always needs to be at the very end because of Google's shitty so called privacy

		public String getFullName(Context context)
		{
			switch(this)
			{
				case pointOfInterest:
					return context.getResources().getString(R.string.triggerPointOfInterest);
				case timeFrame:
					return context.getResources().getString(R.string.triggerTimeFrame);
				case charging:
					return context.getResources().getString(R.string.triggerCharging);
				case batteryLevel:
					return context.getResources().getString(R.string.batteryLevel);
				case usb_host_connection:
					return context.getResources().getString(R.string.triggerUsb_host_connection);
				case speed:
					return context.getResources().getString(R.string.triggerSpeed);
				case noiseLevel:
					return context.getResources().getString(R.string.triggerNoiseLevel);
				case wifiConnection:
					return context.getResources().getString(R.string.wifiConnection);
				case process_started_stopped:
					return context.getResources().getString(R.string.anotherAppIsRunning);
				case airplaneMode:
					return context.getResources().getString(R.string.airplaneMode);
				case roaming:
					return context.getResources().getString(R.string.roaming);
				case phoneCall:
					return context.getResources().getString(R.string.phoneCall);
				case nfcTag:
					return context.getResources().getString(R.string.nfcTag);
				case activityDetection:
					return context.getResources().getString(R.string.activityDetection);
				case bluetoothConnection:
					return context.getResources().getString(R.string.bluetoothConnection);
				case headsetPlugged:
					return context.getResources().getString(R.string.triggerHeadsetPlugged);
				case notification:
					return context.getResources().getString(R.string.notification);
				case deviceOrientation:
					return context.getResources().getString(R.string.deviceOrientation);
				case profileActive:
					return context.getResources().getString(R.string.profile);
				case musicPlaying:
					return context.getResources().getString(R.string.musicPlaying);
				case screenState:
					return context.getResources().getString(R.string.screenState);
				default:
					return "Unknown";
			}
		}
	};

	Rule parentRule = null;
	Calendar lastTimeNotApplied = null;

	final static String anyAppString = "-1";

	public boolean applies(Object triggeringObject, Context context)
    {
		boolean result = true;

		try
		{
			switch(this.getTriggerType())
			{
				case timeFrame:
					if(!checkDateTime(triggeringObject, false))
						result = false;
					break;
				case pointOfInterest:
					if(!checkLocation())
						result = false;
					break;
				case charging:
					if(!checkCharging())
						result = false;
					break;
				case usb_host_connection:
					if(!checkUsbHostConnection())
						result = false;
					break;
				case batteryLevel:
					if(!checkBatteryLevel())
						result = false;
					break;
				case speed:
					if(!checkSpeed())
						result = false;
					break;
				case noiseLevel:
					if(!checkNoiseLevel())
						result = false;
					break;
				case wifiConnection:
					if(!checkWifiConnection())
						result = false;
					break;
				case process_started_stopped:
					if(!checkProcess())
						result = false;
					break;
				case airplaneMode:
					if(!checkAirplaneMode())
						result = false;
					break;
				case roaming:
					if(!checkRoaming())
						result = false;
					break;
				case phoneCall:
					if(!checkPhoneCall())
						result = false;
					break;
				case nfcTag:
					if(!checkNfc())
						result = false;
					break;
				case bluetoothConnection:
					if(!checkBluetooth())
						result = false;
					break;
				case headsetPlugged:
					if(!checkHeadsetPlugged())
						result = false;
					break;
				case notification:
					if(!checkNotification())
						result = false;
					break;
				case deviceOrientation:
					if(!checkDeviceOrientation())
						result = false;
					break;
				case activityDetection:
					if(!getParentRule().checkActivityDetection(this))
						result = false;
					break;
				case profileActive:
					if(!checkProfileActive())
						result = false;
					break;
				case musicPlaying:
					if(!checkMusicPlaying())
						result = false;
					break;
				case screenState:
					if(!checkScreenState())
						result = false;
					break;
				default:
					break;
			}
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "Trigger", "Error while checking if rule " + getParentRule().getName() + " applies." + Miscellaneous.lineSeparator + Log.getStackTraceString(e), 1);
			result = false;
		}

		if(!result)
			lastTimeNotApplied = Calendar.getInstance();

		return result;
    }

    boolean checkNotification()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			String[] params = this.getTriggerParameter2().split(triggerParameter2Split);

			String myApp = params[0];
			String myTitleDir = params[1];
			String requiredTitle = params[2];
			String myTextDir = params[3];
			String requiredText;
			if (params.length >= 5)
				requiredText = params[4];
			else
				requiredText = "";

			if(this.getTriggerParameter())
			{
				// Check an active notification that is still there

				boolean foundMatch = false;

				for (StatusBarNotification sbn : NotificationListener.getInstance().getActiveNotifications())
				{
					if(getParentRule().getLastExecution() == null || sbn.getPostTime() > this.getParentRule().getLastExecution().getTimeInMillis())
					{
						NotificationListener.SimpleNotification sn = NotificationListener.convertNotificationToSimpleNotification(true, sbn);

						Miscellaneous.logEvent("i", "NotificationCheck", "Checking if this notification matches our rule " + this.getParentRule().getName() + ": " + sn.toString(), 5);

						if (!myApp.equals(anyAppString))
						{
							if (!myApp.equalsIgnoreCase(sn.getApp()))
							{
								Miscellaneous.logEvent("i", "NotificationCheck", "Notification app name does not match rule.", 5);
								continue;
							}
						}
						else
						{
						/*
						 	Notifications from Automation are disregarded to avoid infinite loops.
						 */
							if(myApp.equals(BuildConfig.APPLICATION_ID))
							{
								return false;
							}
						}

					/*
						If there are multiple notifications ("stacked") title or text might be null:
						https://stackoverflow.com/questions/28047767/notificationlistenerservice-not-reading-text-of-stacked-notifications
					 */

						// T I T L E
						if (!StringUtils.isEmpty(requiredTitle))
						{
							if (!Miscellaneous.compare(myTitleDir, requiredTitle, sn.getTitle()))
							{
								Miscellaneous.logEvent("i", "NotificationCheck", "Notification title does not match rule.", 5);
								continue;
							}
						}
						else
							Miscellaneous.logEvent("i", "NotificationCheck", "A required title for a notification trigger was not specified.", 5);

						// T E X T
						if (!StringUtils.isEmpty(requiredText))
						{
							if (!Miscellaneous.compare(myTextDir, requiredText, sn.getText()))
							{
								Miscellaneous.logEvent("i", "NotificationCheck", "Notification text does not match rule.", 5);
								continue;
							}
						}
						else
							Miscellaneous.logEvent("i", "NotificationCheck", "A required text for a notification trigger was not specified.", 5);

						foundMatch = true;
						break;
					}
				}

				if(!foundMatch)
					return false;
			}
			else
			{
				// check a notification that is gone

				if(NotificationListener.getLastNotification() != null)
				{
					if(!NotificationListener.getLastNotification().isCreated())
					{
						String app = NotificationListener.getLastNotification().getApp();
						String title = NotificationListener.getLastNotification().getTitle();
						String text = NotificationListener.getLastNotification().getText();

						if (!myApp.equals(anyAppString))
						{
							if (!app.equalsIgnoreCase(myApp))
								return false;
						}
						else
						{
							if(myApp.equals(BuildConfig.APPLICATION_ID))
							{
								return false;
							}
						}

						if (requiredTitle.length() > 0)
						{
							if (!Miscellaneous.compare(myTitleDir, title, requiredTitle))
								return false;
						}

						if (requiredText.length() > 0)
						{
							if (!Miscellaneous.compare(myTextDir, text, requiredText))
								return false;
						}
					}
					else
						return false;
				}
			}
		}

		return true;
	}

	boolean checkMusicPlaying()
	{
		return triggerParameter == MediaPlayerListener.isAudioPlaying(Miscellaneous.getAnyContext());
	}

	boolean checkProfileActive()
	{
		String demandedProfileName = getTriggerParameter2().split(Trigger.triggerParameter2Split)[0];
		boolean checkSettings = Boolean.parseBoolean(getTriggerParameter2().split(Trigger.triggerParameter2Split)[1]);

		if(checkSettings)
		{
			Profile profile = Profile.getByName(demandedProfileName);
			return profile.areMySettingsCurrentlyActive(Miscellaneous.getAnyContext());
		}
		else
		{
			try
			{
				Profile lastProfile = null;

				if (Profile.profileActivationHistory.size() > 0)
				{
					lastProfile = Profile.profileActivationHistory.get(Profile.profileActivationHistory.size() - 1);

					if (getTriggerParameter())
						return demandedProfileName.equals(lastProfile.getName());
					else
						return !demandedProfileName.equals(lastProfile.getName());
				}
				else
					return !getTriggerParameter();
			}
			catch (Exception e)
			{
				Miscellaneous.logEvent("w", "Trigger", "Error checking profile trigger.", 4);
			}
		}

		return false;
	}

	boolean checkScreenState()
	{
		try
		{
			int desiredState = Integer.parseInt(getTriggerParameter2());
			int currentState = ScreenStateReceiver.getScreenState();

			return desiredState == currentState;
		}
		catch (Exception e)
		{
			Miscellaneous.logEvent("w", "Trigger", "Error checking profile trigger.", 4);
		}

		return false;
	}

	boolean checkDeviceOrientation()
	{
		String deviceOrientationPieces[] = getTriggerParameter2().split(Trigger.triggerParameter2Split);
		float desiredAzimuth = Float.parseFloat(deviceOrientationPieces[0]);
		float desiredAzimuthTolerance = Float.parseFloat(deviceOrientationPieces[1]);
		float desiredPitch = Float.parseFloat(deviceOrientationPieces[2]);
		float desiredPitchTolerance = Float.parseFloat(deviceOrientationPieces[3]);
		float desiredRoll = Float.parseFloat(deviceOrientationPieces[4]);
		float desiredRollTolerance = Float.parseFloat(deviceOrientationPieces[5]);
		float currentAzimuth = DeviceOrientationListener.getInstance().getAzimuth();
		float currentPitch = DeviceOrientationListener.getInstance().getPitch();
		float currentRoll = DeviceOrientationListener.getInstance().getRoll();

		if(desiredAzimuthTolerance < 180)
		{
			if (!(desiredAzimuth - desiredAzimuthTolerance <= currentAzimuth && currentAzimuth <= desiredAzimuth + desiredAzimuthTolerance))
			{
				Miscellaneous.logEvent("i", "DeviceOrientation", "Azimuth outside of tolerance area.", 5);
				if (getTriggerParameter())
					return false;
				else
					return true;
			}
		}

		if(desiredPitchTolerance < 180)
		{
			if (!(desiredPitch - desiredPitchTolerance <= currentPitch && currentPitch <= desiredPitch + desiredPitchTolerance))
			{
				Miscellaneous.logEvent("i", "DeviceOrientation", "Pitch outside of tolerance area.", 5);
				if (getTriggerParameter())
					return false;
				else
					return true;
			}
		}

		if(desiredRollTolerance < 180)
		{
			if (!(desiredRoll - desiredRollTolerance <= currentRoll && currentRoll <= desiredRoll + desiredRollTolerance))
			{
				Miscellaneous.logEvent("i", "DeviceOrientation", "Roll outside of tolerance area.", 5);
				if (getTriggerParameter())
					return false;
				else
					return true;
			}
		}

		if(getTriggerParameter())
			return true;
		else
			return false;
	}

    boolean checkHeadsetPlugged()
	{
		if(HeadphoneJackListener.isHeadsetConnected() != this.getTriggerParameter())
			return false;
		else
		if(this.getHeadphoneType() != 2 && this.getHeadphoneType() != HeadphoneJackListener.getHeadphoneType())
		{
			Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyWrongHeadphoneType), this.getParentRule().getName()), 3);
			return false;
		}

		return true;
	}

    boolean checkBluetooth()
	{
		Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format("Checking for bluetooth...", this.getParentRule().getName()), 4);

		if(this.getBluetoothDeviceAddress().equals("<any>"))
		{
			if(this.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_CONNECTED))
			{
				if(BluetoothReceiver.isAnyDeviceConnected() != this.getTriggerParameter())
					return false;
			}
			else if((this.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)))
			{
				if(BluetoothReceiver.isAnyDeviceConnected() != this.getTriggerParameter())
					return false;
			}
			else
			{
				// range
				if(BluetoothReceiver.isAnyDeviceInRange() != this.getTriggerParameter())
					return false;
			}
		}
		else if(this.getBluetoothDeviceAddress().equals("<none>"))
		{
			if(this.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_CONNECTED))
			{
				if(BluetoothReceiver.isAnyDeviceConnected() == this.getTriggerParameter())
					return false;
			}
			else if((this.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)))
			{
				if(BluetoothReceiver.isAnyDeviceConnected() == this.getTriggerParameter())
					return false;
			}
			else
			{
				// range
				if(BluetoothReceiver.isAnyDeviceInRange() == this.getTriggerParameter())
					return false;
			}
		}
		else if(this.getBluetoothDeviceAddress().length() > 0)
		{
			if(this.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_CONNECTED))
			{
				if(BluetoothReceiver.isDeviceCurrentlyConnected(BluetoothReceiver.getDeviceByAddress(this.getBluetoothDeviceAddress())) != this.getTriggerParameter())
					return false;
			}
			else if((this.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)))
			{
				if(BluetoothReceiver.isDeviceCurrentlyConnected(BluetoothReceiver.getDeviceByAddress(this.getBluetoothDeviceAddress())) != this.getTriggerParameter())
					return false;
			}
			else
			{
				// range
				if(BluetoothReceiver.isDeviceInRange(BluetoothReceiver.getDeviceByAddress(this.getBluetoothDeviceAddress())) != this.getTriggerParameter())
					return false;
			}
		}
		else
		{
			Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyStateNotCorrect), this.getParentRule().getName()), 3);
			return false;
		}

		return true;
	}

    boolean checkNfc()
	{
		if(NfcReceiver.lastReadLabel == null)
		{
			Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyNoTagLabel), this.getParentRule().getName()),3);
			return false;
		}
		else if(!NfcReceiver.lastReadLabel.equals(this.getNfcTagId()))
		{
			Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyWrongTagLabel) + " " + NfcReceiver.lastReadLabel + " / " + this.getNfcTagId(), this.getParentRule().getName()), 3);
			return false;
		}

		return true;
	}

    boolean checkPhoneCall()
	{
		String[] elements = this.getTriggerParameter2().split(triggerParameter2Split);
		// state dir number

		if(elements[2].equals(Trigger.triggerPhoneCallNumberAny) || Miscellaneous.comparePhoneNumbers(PhoneStatusListener.getLastPhoneNumber(), elements[2]) || (Miscellaneous.isRegularExpression(elements[2]) && PhoneStatusListener.getLastPhoneNumber().matches(elements[2])))
		{
			//if(PhoneStatusListener.isInACall() == oneTrigger.getTriggerParameter())
			if(
					(elements[0].equals(Trigger.triggerPhoneCallStateRinging) && PhoneStatusListener.getCurrentState() == TelephonyManager.CALL_STATE_RINGING)
							||
					(elements[0].equals(Trigger.triggerPhoneCallStateStarted) && PhoneStatusListener.getCurrentState() == TelephonyManager.CALL_STATE_OFFHOOK)
							||
					(elements[0].equals(Trigger.triggerPhoneCallStateStopped) && PhoneStatusListener.getCurrentState() == TelephonyManager.CALL_STATE_IDLE)
			)
			{
				if(
						elements[1].equals(Trigger.triggerPhoneCallDirectionAny)
								||
						(elements[1].equals(Trigger.triggerPhoneCallDirectionIncoming) && PhoneStatusListener.getLastPhoneDirection() == 1)
								||
						(elements[1].equals(Trigger.triggerPhoneCallDirectionOutgoing) && PhoneStatusListener.getLastPhoneDirection() == 2)
				)
				{
					// Trigger conditions are met
				}
				else
				{
					Miscellaneous.logEvent("i", "Rule", "A trigger of rule " + getParentRule().getName() + " doesn't apply. Wrong direction. Demanded: " + String.valueOf(this.getPhoneDirection()) + ", got: " + String.valueOf(PhoneStatusListener.getLastPhoneDirection()), 4);
					return false;
				}
			}
			else
			{
				Miscellaneous.logEvent("i", "Rule", "A trigger of rule " + getParentRule().getName() + " doesn't apply. Wrong call status. Demanded: " + elements[0] + ", got: " + String.valueOf(PhoneStatusListener.getCurrentState()) + " (0=idle, 1=ringing, 2=offhook)", 4);
				return false;
			}
		}
		else
		{
			Miscellaneous.logEvent("i", "Rule", "A trigger of rule " + getParentRule().getName() + " doesn't apply. Wrong phone number. Demanded: " + this.getPhoneNumber() + ", got: " + PhoneStatusListener.getLastPhoneNumber(), 4);
			return false;
		}

		return true;
	}

    boolean checkRoaming()
	{
		if(ConnectivityReceiver.isRoaming(Miscellaneous.getAnyContext()) != this.getTriggerParameter())
		{
			return false;
		}

		return true;
	}

    boolean checkAirplaneMode()
	{
		if(ConnectivityReceiver.isAirplaneMode(Miscellaneous.getAnyContext()) != this.getTriggerParameter())
		{
			return false;
		}

		return true;
	}

    boolean checkProcess()
	{
		boolean running = false;

		if(getTriggerParameter2().contains(triggerParameter2Split))
		{
			String parts[] = triggerParameter2.split(triggerParameter2Split);
			for(String appName : ProcessListener.getRunningApps())
			{
				if(appName.startsWith(parts[0]))
					running = true;
			}
		}
		else
			running = ProcessListener.getRunningApps().contains(this.getProcessName());

		if(running)
			Miscellaneous.logEvent("i", "ProcessMonitoring", "App " + this.getProcessName() + " is currently running.", 4);
		else
			Miscellaneous.logEvent("i", "ProcessMonitoring", "App " + this.getProcessName() + " is not running.", 4);

		if(running != this.getTriggerParameter())
		{
			Miscellaneous.logEvent("i", "ProcessMonitoring", "Trigger doesn't apply.", 4);
			return false;
		}

		Miscellaneous.logEvent("i", "ProcessMonitoring", "Trigger applies.", 4);

		return true;
	}

    boolean checkWifiConnection()
	{
		Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format( "Checking for wifi state", this.getParentRule().getName()),4);
		if(this.getTriggerParameter() == WifiBroadcastReceiver.lastConnectedState)	// connected / disconnected
		{
			if(this.getTriggerParameter2().length() > 0)	// only check if any wifi name specified, otherwise any wifi will do
			{
				Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format("Wifi name specified, checking that.", this.getParentRule().getName()), 4);
				if(!WifiBroadcastReceiver.getLastWifiSsid().equals(this.getTriggerParameter2()))
				{
					Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyNotTheCorrectSsid), getParentRule().getName(), this.getTriggerParameter2(), WifiBroadcastReceiver.getLastWifiSsid()),this.getParentRule().getName()), 3);
					return false;
				}
				else
					Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format("Wifi name matches. Rule will apply.", this.getParentRule().getName()), 4);
			}
			else
				Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format("No wifi name specified, any will do.", this.getParentRule().getName()), 4);
		}
		else
		{
			Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format("Wifi state not correct, demanded " + String.valueOf(this.getTriggerParameter() + ", got " + String.valueOf(WifiBroadcastReceiver.lastConnectedState)), this.getParentRule().getName()), 4);
			return false;
		}

		return true;
	}

    boolean checkNoiseLevel()
	{
		if(this.getTriggerParameter())
		{
			if(NoiseListener.getNoiseLevelDb() < this.getNoiseLevelDb())
			{
				Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyItsQuieterThan) + " " + String.valueOf(this.getNoiseLevelDb()), this.getParentRule().getName()), 3);
				return false;
			}
		}
		else
		{
			if(NoiseListener.getNoiseLevelDb() > this.getNoiseLevelDb())
			{
				Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyItsLouderThan) + " " + String.valueOf(this.getNoiseLevelDb()), this.getParentRule().getName()), 3);
				return false;
			}
		}

		return true;
	}

    boolean checkSpeed()
	{
		if(this.getTriggerParameter())
		{
			if(LocationProvider.getSpeed() < this.getSpeed())
			{
				Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyWeAreSlowerThan) + " " + String.valueOf(this.getSpeed()), this.getParentRule().getName()), 3);
				return false;
			}
		}
		else
		{
			if(LocationProvider.getSpeed() > this.getSpeed())
			{
				Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyWeAreFasterThan) + " " + String.valueOf(this.getSpeed()), this.getParentRule().getName()), 3);
				return false;
			}
		}

		return true;
	}

    boolean checkBatteryLevel()
	{
		if(this.getTriggerParameter())
		{
			if(BatteryReceiver.getBatteryLevel() <= this.getBatteryLevel())
			{
				Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyBatteryLowerThan) + " " + String.valueOf(this.getBatteryLevel()), this.getParentRule().getName()), 3);
				return false;
			}
		}
		else
		{
			if(this.getBatteryLevel() >= this.getBatteryLevel())
			{
				Miscellaneous.logEvent("i", Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyBatteryHigherThan) + " " + String.valueOf(this.getBatteryLevel()), this.getParentRule().getName()), 3);
				return false;
			}
		}

		return true;
	}

    boolean checkUsbHostConnection()
	{
		if(BatteryReceiver.isUsbHostConnected() != this.getTriggerParameter())
		{
			return false;
		}

		return true;
	}

    boolean checkLocation()
	{
		// Am I here?
		PointOfInterest activePoi = PointOfInterest.getActivePoi();
		if(activePoi != null)	//entering one
		{
			if(this.getPointOfInterest() != null)
			{
				if(activePoi.equals(this.getPointOfInterest()))
				{
					if(!this.getTriggerParameter())
					{
						Miscellaneous.logEvent("i", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), this.getParentRule().getName()), String.format("Rule %1$s doesn't apply. We're entering POI: " + this.getPointOfInterest().getName() + ", not leaving it.", getParentRule().getName()), 4);
						return false;
					}
				}
				else
				{
					Miscellaneous.logEvent("i", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), this.getParentRule().getName()), String.format("Rule %1$s doesn't apply. This is " + activePoi.getName() + ", not " + this.getPointOfInterest().getName() + ".", getParentRule().getName()), 4);
					return false;
				}
			}
			else if(this.getPointOfInterest() == null)
			{
				if(this.getTriggerParameter())
				{
					Miscellaneous.logEvent("i", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), this.getParentRule().getName()), String.format("Rule %s$s doesn't apply. We're at a POI. Rule specifies not at none, so leaving any.",getParentRule().getName()),  4);
					return false;
				}
			}
		}
		else					//leaving one
		{
			// We are not at any POI. But if this trigger requires us NOT to be there, that may be fine.
			if(this.getPointOfInterest() != null)
			{
//							if(activePoi.equals(oneTrigger.getPointOfInterest()))
//							{
				if(!this.getTriggerParameter())
				{
					Miscellaneous.logEvent("i", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), this.getParentRule().getName()), "We are not at POI \"" + this.getPointOfInterest().getName() + "\". But since that's required by this rule that's fine.", 4);
				}
				else
				{
					Miscellaneous.logEvent("i", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), this.getParentRule().getName()), String.format("Rule %1$s doesn't apply. We're not at POI \"" + this.getPointOfInterest().getName() + "\".", getParentRule().getName()), 3);
					return false;
				}
//							}
			}
			else if(this.getPointOfInterest() == null)
			{
				if(!this.getTriggerParameter())
				{
					Miscellaneous.logEvent("i", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), this.getParentRule().getName()), String.format("Rule %1$s doesn't apply. We're at no POI. Rule specifies to be at anyone.", getParentRule().getName()), 5);
					return false;
				}
			}
		}

		return true;
	}

	public boolean hasStateNotAppliedSinceLastRuleExecution()
	{
		if(getParentRule().getLastExecution() == null)
		{
			Miscellaneous.logEvent("i", "Trigger", "Trigger " + this.toString() + " of rule " + getParentRule().getName() + " has NOT applied since the parent rule\'s last activation.", 4);
			return true;
		}
		else if(lastTimeNotApplied != null)
		{
			if(lastTimeNotApplied.getTimeInMillis() > getParentRule().getLastExecution().getTimeInMillis())
			{
				Miscellaneous.logEvent("i", "Trigger", "Trigger " + this.toString() + " of rule " + getParentRule().getName() + " has NOT applied since the parent rule\'s last activation.", 4);
				return true;
			}
		}

		Miscellaneous.logEvent("i", "Trigger", "Trigger " + this.toString() + " of rule " + getParentRule().getName() + " may apply currently, but has not NOT applied since the rule\'s last execution.", 4);
		return false;
	}

	boolean checkCharging()
	{
		if(BatteryReceiver.isDeviceCharging(Miscellaneous.getAnyContext()) == 0)
		{
			return false; // unknown charging state, can't activate rule under these conditions
		}
		else if(BatteryReceiver.isDeviceCharging(Miscellaneous.getAnyContext()) == 1)
		{
			if(this.getTriggerParameter()) //rule says when charging, but we're currently discharging
				return false;
		}
		else if(BatteryReceiver.isDeviceCharging(Miscellaneous.getAnyContext()) == 2)
		{
			if(!this.getTriggerParameter()) //rule says when discharging, but we're currently charging
				return false;
		}

		return true;
	}

	public boolean checkDateTime(Object triggeringObject, boolean checkifStateChangedSinceLastRuleExecution)
	{
		/*
		 * Use format known from Automation
		 * 07:30:00/17:30:00/23456/300	<-- last parameter is optional: repetition in seconds
		 * Also required: inside or outside that interval
		 */

		Date triggeringTime;
		if(triggeringObject instanceof Date)
			triggeringTime = (Date)triggeringObject;
		else
			triggeringTime = new Date();

		String timeString = String.valueOf(triggeringTime.getHours()) + ":" + String.valueOf(triggeringTime.getMinutes()) + ":" + String.valueOf(triggeringTime.getSeconds());
		Time nowTime = Time.valueOf(timeString);
		Calendar calNow = Calendar.getInstance();

		try
		{
			TimeFrame tf = new TimeFrame(getTriggerParameter2());

			if(tf.getDayList().contains(calNow.get(Calendar.DAY_OF_WEEK)))
			{
				if(
					// Regular case, start time is lower than end time
						(
							Miscellaneous.compareTimes(tf.getTriggerTimeStart(), nowTime) >= 0
									&&
							Miscellaneous.compareTimes(nowTime, tf.getTriggerTimeStop()) > 0
						)
								||
						// Other case, start time higher than end time, timeframe goes over midnight
						(
							Miscellaneous.compareTimes(tf.getTriggerTimeStart(), tf.getTriggerTimeStop()) < 0
									&&
							(Miscellaneous.compareTimes(tf.getTriggerTimeStart(), nowTime) >= 0
											||
							Miscellaneous.compareTimes(nowTime, tf.getTriggerTimeStop()) > 0)
						)
								||
						// further case: start and end times are identical, meaning a 24h window
						(
							Miscellaneous.compareTimes(tf.getTriggerTimeStart(), tf.getTriggerTimeStop()) == 0
						)
				)
				{
					// We are in the timeframe
					Miscellaneous.logEvent("i", "Trigger", "TimeFrame: We're currently (" + calNow.getTime().toString() + ") in the specified TimeFrame (" + tf.toString() + ").", 4);
					if(getTriggerParameter())
					{
						if(checkifStateChangedSinceLastRuleExecution)
						{
							/*
							 * Was there a target repetition time between last execution and now?
							 * If not -> return false.
							 */
							Calendar compareCal = Calendar.getInstance();
							compareCal.setTimeInMillis(triggeringTime.getTime());
							if(tf.getRepetition() > 0)
							{
								if(!isSupposedToRepeatSinceLastExecution(compareCal))
								{
									Miscellaneous.logEvent("i", "TimeFrame", "TimeFrame: Trigger of rule " + this.getParentRule().getName() + " applies, but repeated execution is not due, yet.", 4);
									return false;
								}
							}
							else
							{
								/*
								 * This is not a repeating rule. Have we left
								 * the relevant timeframe since the last run?
								 * Determine if it has ran today already. If yes
								 * return false because every rule that is not
								 * repeating can only be executed once per day.
								 */

								if(
										getParentRule().getLastExecution().get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
												&&
										getParentRule().getLastExecution().get(Calendar.MONTH) == calNow.get(Calendar.MONTH)
												&&
										getParentRule().getLastExecution().get(Calendar.DAY_OF_MONTH) == calNow.get(Calendar.DAY_OF_MONTH)
								)
								{
									Miscellaneous.logEvent("i", "TimeFrame", "TimeFrame: Trigger of rule " + this.getParentRule().getName() + " applies, but it was already executed today.", 4);
									return false;
								}
							}
						}

						Miscellaneous.logEvent("i", "Trigger", "TimeFrame: That's what's specified. Trigger of rule " + this.getParentRule().getName() + " applies.", 4);
						return true;
					}
					else
					{
						Miscellaneous.logEvent("i", "Trigger", "TimeFrame: That's not what's specified. Trigger of rule " + this.getParentRule().getName() + " doesn't apply.", 4);
						return false;
					}
				}
				else
				{
					Miscellaneous.logEvent("i", "Trigger", "TimeFrame: We're currently (" + calNow.getTime().toString() + ", Day: " + String.valueOf(calNow.get(Calendar.DAY_OF_WEEK)) + ") not in the specified TimeFrame (" + tf.toString() + ") because of the time.", 5);
					if(!getTriggerParameter())
					{
						if(checkifStateChangedSinceLastRuleExecution)
						{
							/*
							 * Was there a target repetition time between last execution and now?
							 * If not -> return false.
							 */
							Calendar compareCal = Calendar.getInstance();
							compareCal.setTimeInMillis(triggeringTime.getTime());
							if(tf.getRepetition() > 0)
							{
								if(!isSupposedToRepeatSinceLastExecution(compareCal))
								{
									Miscellaneous.logEvent("i", "Trigger", "TimeFrame: Trigger of rule " + this.getParentRule().getName() + " applies, but repeated execution is not due, yet.", 4);
									return false;
								}
							}
							else
							{
								/*
								 * This is not a repeating rule. Have we left
								 * the relevant timeframe since the last run?
								 * Determine if it has ran today already. If yes
								 * return false because every rule that is not
								 * repeating can only be executed once per day.
								 */

								if(
										getParentRule().getLastExecution().get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
												&&
										getParentRule().getLastExecution().get(Calendar.MONTH) == calNow.get(Calendar.MONTH)
												&&
										getParentRule().getLastExecution().get(Calendar.DAY_OF_MONTH) == calNow.get(Calendar.DAY_OF_MONTH)
								)
								{
									Miscellaneous.logEvent("i", "Trigger", "TimeFrame: Trigger of rule " + this.getParentRule().getName() + " applies, but it was already executed today.", 4);
									return false;
								}
							}
						}
						Miscellaneous.logEvent("i", "Trigger", "TimeFrame: That's what's specified. Trigger of rule " + this.getParentRule().getName() + " applies.", 5);
						return true;
					}
					else
					{
						Miscellaneous.logEvent("i", "Trigger", "TimeFrame: That's not what's specified. Trigger of rule " + this.getParentRule().getName() + " doesn't apply.", 5);
						return false;
					}
				}
			}
			else
			{
				Miscellaneous.logEvent("i", "Trigger", "TimeFrame: We're currently (" + calNow.getTime().toString() + ", Day: " + String.valueOf(calNow.get(Calendar.DAY_OF_WEEK)) + ") not in the specified TimeFrame (" + tf.toString() + ") because of the day.", 5);
				return false;
			}
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "Trigger", "There was an error while checking if the time based trigger applies: " + Log.getStackTraceString(e), 1);
			return false;
		}
	}

	public static Calendar getNextRepeatedExecutionAfter(Trigger trigger, Calendar now)
	{
		Calendar calSet;
		Time setTime;
		TimeFrame tf = new TimeFrame(trigger.getTriggerParameter2());

		if(tf.getRepetition() > 0)
		{
			if(trigger.getTriggerParameter())
				setTime = tf.getTriggerTimeStart();
			else
				setTime = tf.getTriggerTimeStop();

			calSet = (Calendar) now.clone();
			calSet.set(Calendar.HOUR_OF_DAY, setTime.getHours());
			calSet.set(Calendar.MINUTE, setTime.getMinutes());
			calSet.set(Calendar.SECOND, 0);
			calSet.set(Calendar.MILLISECOND, 0);

			// If the starting time is a day ahead remove 1 day.
			if(calSet.getTimeInMillis() > now.getTimeInMillis())
				calSet.add(Calendar.DAY_OF_MONTH, -1);

			long differenceInSeconds = Math.abs(now.getTimeInMillis() - calSet.getTimeInMillis()) / 1000;
			long nextExecutionMultiplier = Math.floorDiv(differenceInSeconds, tf.getRepetition()) + 1;
			long nextScheduleTimestamp = (calSet.getTimeInMillis() / 1000) + (nextExecutionMultiplier * tf.getRepetition());
			Calendar calSchedule = Calendar.getInstance();
			calSchedule.setTimeInMillis(nextScheduleTimestamp * 1000);

			return calSchedule;
		}
		else
			Miscellaneous.logEvent("i", "Trigger", "Trigger " + trigger.toString() + " is not executed repeatedly.", 5);

		return null;
	}

	boolean isSupposedToRepeatSinceLastExecution(Calendar now)
	{
		TimeFrame tf = new TimeFrame(getTriggerParameter2());
		Calendar lastExec = getParentRule().getLastExecution();

		// the simple stuff:

		if(lastExec == null)				// rule never run, go any way
			return true;
		else if(tf.getRepetition() <= 0)	// is not set to repeat at all
			return false;

		/*
		 * We don't need to check if the trigger currently applies, that has
		 * been done externally via the applies() function. We can safely assume
		 * we're inside the specified timeframe.
		 */

		Calendar timeSupposedToRunNext = getNextRepeatedExecutionAfter(this, lastExec);
		if(now.getTimeInMillis() > timeSupposedToRunNext.getTimeInMillis())
			return true;

		return false;
	}

	boolean triggerParameter; //if true->started event, if false->stopped
	String triggerParameter2;

	public static final String triggerParameter2Split = "tp2split";
	
    Trigger_Enum triggerType = null;
    PointOfInterest pointOfInterest = null;
    TimeFrame timeFrame;

    public static String triggerPhoneCallStateRinging = "ringing";
	public static String triggerPhoneCallStateStarted = "started";
	public static String triggerPhoneCallStateStopped = "stopped";
	public static String triggerPhoneCallDirectionIncoming = "incoming";
	public static String triggerPhoneCallDirectionOutgoing = "outgoing";
	public static String triggerPhoneCallDirectionAny = "any";
	public static String triggerPhoneCallNumberAny = "any";

	double speed; //km/h
    long noiseLevelDb;
	String processName = null;
    int batteryLevel;
    int phoneDirection = 0; // 0=any, 1=incoming, 2=outgoing
    String phoneNumber = null;
    String nfcTagId = null;
    String bluetoothEvent = null;
	String bluetoothDeviceAddress = null;
    int activityDetectionType = -1;
    int headphoneType = -1;
    
	public int getHeadphoneType()
	{
		return headphoneType;
	}
	public void setHeadphoneType(int headphoneType)
	{
		this.headphoneType = headphoneType;
	}
	public String getNfcTagId()
	{
		return nfcTagId;
	}
	public void setNfcTagId(String nfcTagId)
	{
		this.nfcTagId = nfcTagId;
	}
	
	public int getActivityDetectionType()
	{
		return activityDetectionType;
	}
	public void setActivityDetectionType(int activityDetectionType)
	{
		this.activityDetectionType = activityDetectionType;
	}
	public String getBluetoothDeviceAddress()
	{
		return bluetoothDeviceAddress;
	}
	public void setBluetoothDeviceAddress(String bluetoothDeviceAddress)
	{
		this.bluetoothDeviceAddress = bluetoothDeviceAddress;
	}
	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}
	public String getPhoneNumber()
	{
		return phoneNumber;
	}

	public void setPhoneDirection(int phoneDirection)
	{
		this.phoneDirection = phoneDirection;
	}
	public int getPhoneDirection()
	{
		return phoneDirection;
	}

	public int getBatteryLevel()
	{
		return batteryLevel;
	}

	public void setBatteryLevel(int batteryLevel)
	{
		this.batteryLevel = batteryLevel;
	}

	public String getProcessName()
	{
		return processName;
	}

	public void setProcessName(String processName)
	{
		this.processName = processName;
	}

	public PointOfInterest getPointOfInterest()
	{
		return pointOfInterest;
	}

	public void setPointOfInterest(PointOfInterest setPointOfInterest)
	{
		this.pointOfInterest = setPointOfInterest;
	}
	
	public double getSpeed()
	{
		return speed;
	}

	public void setSpeed(double speed)
	{
		this.speed = speed;
	}
	
	public long getNoiseLevelDb()
	{
		return noiseLevelDb;
	}

	public void setNoiseLevelDb(long noiseLevelDb)
	{
		this.noiseLevelDb = noiseLevelDb;
	}

	public Trigger_Enum getTriggerType()
	{
		return triggerType;
	}

	public void setTriggerType(Trigger_Enum settriggerType)
	{
		this.triggerType = settriggerType;
	}

	public boolean getTriggerParameter()
	{
		return triggerParameter;
	}

	public void setTriggerParameter(boolean triggerParameter)
	{
		this.triggerParameter = triggerParameter;
	}

	public String getTriggerParameter2()
	{
		return triggerParameter2;
	}

	public void setTriggerParameter2(String triggerParameter2)
	{
		this.triggerParameter2 = triggerParameter2;
	}

	public TimeFrame getTimeFrame()
	{
		return timeFrame;
	}

	public void setTimeFrame(TimeFrame timeFrame)
	{
		this.timeFrame = timeFrame;
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@SuppressWarnings("unused")
	@Override
	public String toString()
	{
		StringBuilder returnString = new StringBuilder();
		
		switch(this.getTriggerType())
		{
			case charging:
				if(getTriggerParameter())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.starting) + " ");
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.stopping) + " ");
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.triggerCharging));
				break;
			case batteryLevel:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.batteryLevel));
				if(getTriggerParameter())
					returnString.append(" " + Miscellaneous.getAnyContext().getResources().getString(R.string.exceeds) + " ");
				else
					returnString.append(" " + Miscellaneous.getAnyContext().getResources().getString(R.string.dropsBelow) + " ");
				returnString.append(String.valueOf(this.getBatteryLevel()) + " %");
				break;
			case usb_host_connection:
				if(getTriggerParameter())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.connecting) + " ");
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.disconnecting) + " ");

				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.triggerUsb_host_connection));
				break;
			case pointOfInterest:
				if(this.getPointOfInterest() != null)
				{
					if(getTriggerParameter())
						returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.entering) + " ");
					else
						returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.leaving) + " ");

					returnString.append(this.getPointOfInterest().getName().toString());
				}
				else
				{
					if(getTriggerParameter())
						returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.leaving) + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.anyLocation));
					else
						returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.entering) + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.anyLocation));
				}
				break;
			case timeFrame:
				if(getTriggerParameter())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.entering) + " ");
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.leaving) + " ");

				String repeat = ", " + Miscellaneous.getAnyContext().getResources().getString(R.string.noRepetition);
				if(this.getTimeFrame().getRepetition() > 0)
					repeat = ", " + String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.repeatEveryXsecondsWithVariable), String.valueOf(this.getTimeFrame().getRepetition()));

				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.triggerTimeFrame) + ": " + this.getTimeFrame().getTriggerTimeStart().toString() + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.until) + " " + this.getTimeFrame().getTriggerTimeStop().toString() + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.onDays) + " " + this.getTimeFrame().getDayList().toString() + repeat);
				break;
			case speed:
				if(getTriggerParameter())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.exceeding) + " ");
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.droppingBelow) + " ");
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.triggerSpeed) + ": " + String.valueOf(this.getSpeed()) + " km/h");
				break;
			case noiseLevel:
				if(getTriggerParameter())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.exceeding) + " ");
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.droppingBelow) + " ");

				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.triggerNoiseLevel) + ": " + String.valueOf(this.getNoiseLevelDb()) + " dB");
				break;
			case wifiConnection:
				String wifiDisplayName = "";				
				if(this.getTriggerParameter2().length() == 0)
					wifiDisplayName += Miscellaneous.getAnyContext().getResources().getString(R.string.anyWifi);
				else
					wifiDisplayName += this.getTriggerParameter2();
				
				if(getTriggerParameter())
					returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.connectedToWifi), wifiDisplayName));
				else
					returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.disconnectedFromWifi), wifiDisplayName));
				
				break;
			case process_started_stopped:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.application) + " " + this.getProcessName() + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.is) + " ");
				if(this.triggerParameter)
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.started));
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.stopped));
				break;
			case airplaneMode:
				if(getTriggerParameter())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.activated) + " ");
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.deactivated) + " ");
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.airplaneMode));
				break;
			case roaming:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.roaming));
				if(getTriggerParameter())
					returnString.append(" " + Miscellaneous.getAnyContext().getResources().getString(R.string.activated));
				else
					returnString.append(" " + Miscellaneous.getAnyContext().getResources().getString(R.string.deactivated));
				break;
			case phoneCall:
				String[] elements = triggerParameter2.split(triggerParameter2Split);

				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.phoneCall));

				returnString.append(" ");

				if(elements[1].equals(triggerPhoneCallDirectionAny))
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.with));
				else if(elements[1].equals(triggerPhoneCallDirectionIncoming))
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.from));
				else if(elements[1].equals(triggerPhoneCallDirectionOutgoing))
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.to));

				returnString.append(" ");

				if(elements[2].equals(Trigger.triggerPhoneCallNumberAny))
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.any) + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.number));
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.number) + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.matching) + " " + elements[2]);

				returnString.append(" ");

				if(elements[0].equals(Trigger.triggerPhoneCallStateRinging))
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.ringing));
				else if(elements[0].equals(Trigger.triggerPhoneCallStateStarted))
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.started));
				else if(elements[0].equals(Trigger.triggerPhoneCallStateStopped))
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.stopped));

				break;
			case nfcTag:
				// This type doesn't have an activate/deactivate equivalent
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.closeTo) + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.nfcTag) + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.withLabel) + " " + this.getNfcTagId());
				break;
			case activityDetection:
				try
				{
					Class testClass = Class.forName(ActivityManageRule.activityDetectionClassPath);
					if (ActivityPermissions.isPermissionDeclaratedInManifest(Miscellaneous.getAnyContext(), "com.google.android.gms.permission.ACTIVITY_RECOGNITION"))
					{
						// This type doesn't have an activate/deactivate equivalent, at least not yet.
						returnString.append(Miscellaneous.runMethodReflective(ActivityManageRule.activityDetectionClassPath, "getDescription", new Object[]{getActivityDetectionType()}));
					}
					else
						returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.featureNotInFdroidVersion));
				}
				catch(ClassNotFoundException e)
				{
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.featureNotInFdroidVersion));
				}
				break;
			case bluetoothConnection:
				String device = Miscellaneous.getAnyContext().getResources().getString(R.string.anyDevice);
				if(bluetoothDeviceAddress.equals("<any>"))
				{
					device = Miscellaneous.getAnyContext().getResources().getString(R.string.any);
				}
				else if(bluetoothDeviceAddress.equals("<none>"))
				{
					device = Miscellaneous.getAnyContext().getResources().getString(R.string.noDevice);
				}
				else
				{
					try
					{
						device = BluetoothReceiver.getDeviceByAddress(bluetoothDeviceAddress).getName() + " (" + this.bluetoothDeviceAddress + ")";
					}
					catch(NullPointerException e)
					{
						device = Miscellaneous.getAnyContext().getResources().getString(R.string.invalidDevice) + ": " + this.bluetoothDeviceAddress;
						Miscellaneous.logEvent("w", "Trigger", device, 3);
					}
				}

				if(bluetoothEvent.equals(BluetoothDevice.ACTION_ACL_CONNECTED) || bluetoothEvent.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED))
				{
					if (this.triggerParameter)
						returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.bluetoothConnectionTo), device));
					else
						returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.bluetoothDisconnectFrom), device));
				}
				else if(bluetoothEvent.equals(BluetoothDevice.ACTION_FOUND))
				{
					if (this.triggerParameter)
						returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.bluetoothDeviceInRange), device));
					else
						returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.bluetoothDeviceOutOfRange), device));
				}
				break;
			case headsetPlugged:
				String type;
				switch(headphoneType)
				{
					case 0:
						type = Miscellaneous.getAnyContext().getResources().getString(R.string.headphoneSimple);
						break;
					case 1:
						type = Miscellaneous.getAnyContext().getResources().getString(R.string.headphoneMicrophone);
						break;
					case 2:
						type = Miscellaneous.getAnyContext().getResources().getString(R.string.headphoneAny);
						break;
					default:
						type = Miscellaneous.getAnyContext().getResources().getString(R.string.notSet);
						break;
				}
				if(getTriggerParameter())
					returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.headsetConnected), type));
				else
					returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.headsetDisconnected), type));
				break;
			case notification:
				if(this.getTriggerParameter2().contains(triggerParameter2Split))
				{
					String[] params = getTriggerParameter2().split(triggerParameter2Split);

					String app = params[0];
					String titleDir = params[1];
					String title = params[2];
					String textDir = params[3];
					String text;
					if (params.length >= 5)
						text = params[4];
					else
						text = "";
					StringBuilder triggerBuilder = new StringBuilder();

					String appString;
					if (app.equalsIgnoreCase(anyAppString))
						appString = Miscellaneous.getAnyContext().getResources().getString(R.string.anyApp);
					else
						appString = "app " + app;

					if(triggerParameter)
						triggerBuilder.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.postsNotification), appString));
					else
						triggerBuilder.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.removedNotification), appString));

					if (title.length() > 0)
						triggerBuilder.append(", " + Miscellaneous.getAnyContext().getString(R.string.title) + " " + Trigger.getMatchString(titleDir) + " " + title);

					if (text.length() > 0)
						triggerBuilder.append(", " + Miscellaneous.getAnyContext().getString(R.string.text) + " " + Trigger.getMatchString(textDir) + " " + text);

					returnString.append(triggerBuilder.toString());
				}
				else
				{
					setTriggerParameter2(anyAppString + triggerParameter2Split + directionEquals + triggerParameter2Split + triggerParameter2Split + directionEquals + triggerParameter2Split + triggerParameter2Split);
				}
				break;
			case deviceOrientation:
				returnString.append(Miscellaneous.getAnyContext().getString(R.string.deviceIsInCertainOrientation));
				break;
			case profileActive:
				if(triggerParameter)
					returnString.append(String.format(Miscellaneous.getAnyContext().getString(R.string.profileActive), getTriggerParameter2().split(Trigger.triggerParameter2Split)[0]));
				else
					returnString.append(String.format(Miscellaneous.getAnyContext().getString(R.string.profileNotActive), getTriggerParameter2().split(Trigger.triggerParameter2Split)[0]));
				break;
			case musicPlaying:
				if(triggerParameter)
					returnString.append(Miscellaneous.getAnyContext().getString(R.string.musicIsPlaying));
				else
					returnString.append(Miscellaneous.getAnyContext().getString(R.string.musicIsNotPlaying));
				break;
			case screenState:
				String state;
				switch(triggerParameter2)
				{
					case "0":
						state = Miscellaneous.getAnyContext().getString(R.string.off);
						break;
					case "1":
						state = Miscellaneous.getAnyContext().getString(R.string.on);
						break;
					case "2":
						state = Miscellaneous.getAnyContext().getString(R.string.unlocked);
						break;
					default:
						state = Miscellaneous.getAnyContext().getString(R.string.unknown);
				}

				returnString.append(String.format(Miscellaneous.getAnyContext().getString(R.string.screenIs), state));
				break;
			default:
				returnString.append("error");
				break;
		}

		return returnString.toString();
	}

	public static final String directionEquals = "eq";
	public static final String directionContains = "ct";
	public static final String directionStartsWith = "sw";
	public static final String directionEndsWith = "ew";
	public static final String directionNotEquals = "ne";

	public static String getMatchString(String direction)
	{
		switch(direction)
		{
			case directionEquals:
				return Miscellaneous.getAnyContext().getString(R.string.directionStringEquals);
			case directionContains:
				return Miscellaneous.getAnyContext().getString(R.string.directionStringContains);
			case directionStartsWith:
				return Miscellaneous.getAnyContext().getString(R.string.directionStringStartsWith);
			case directionEndsWith:
				return Miscellaneous.getAnyContext().getString(R.string.directionStringEndsWith);
			case directionNotEquals:
				return Miscellaneous.getAnyContext().getString(R.string.directionStringNotEquals);
			default:
				return Miscellaneous.getAnyContext().getString(R.string.error);
		}
	}

	public static String getMatchCode(String direction)
	{
		if(direction.equalsIgnoreCase(Miscellaneous.getAnyContext().getString(R.string.directionStringEquals)))
			return directionEquals;
		else if(direction.equalsIgnoreCase(Miscellaneous.getAnyContext().getString(R.string.directionStringContains)))
			return directionContains;
		else if(direction.equalsIgnoreCase(Miscellaneous.getAnyContext().getString(R.string.directionStringStartsWith)))
			return directionStartsWith;
		else if(direction.equalsIgnoreCase(Miscellaneous.getAnyContext().getString(R.string.directionStringEndsWith)))
			return directionEndsWith;
		else if(direction.equalsIgnoreCase(Miscellaneous.getAnyContext().getString(R.string.directionStringNotEquals)))
			return directionNotEquals;
		else
			return Miscellaneous.getAnyContext().getString(R.string.error);
	}

	public static String[] getTriggerTypesAsArray()
	{
		ArrayList<String> triggerTypesList = new ArrayList<String>();

		for(Trigger_Enum triggerType : Trigger_Enum.values())
			triggerTypesList.add(triggerType.name());
		
		return (String[])triggerTypesList.toArray(new String[triggerTypesList.size()]);
	}
	

	public static String[] getTriggerTypesStringAsArray(Context context)
	{
		ArrayList<String> triggerTypesList = new ArrayList<String>();
		
		/*for(int i=0; i<Trigger_Enum.values().length; i++)
		{
			triggerTypesList.add(Trigger_Enum.values()[i].getFullName(context));
		}*/
		for(Trigger_Enum triggerType : Trigger_Enum.values())
			triggerTypesList.add(triggerType.getFullName(context));
		
		return (String[])triggerTypesList.toArray(new String[triggerTypesList.size()]);
	}

	public void setBluetoothEvent(String string)
	{
		this.bluetoothEvent = string;
	}
	public Object getBluetoothEvent()
	{
		return this.bluetoothEvent;
	}

	public Rule getParentRule()
	{
		return parentRule;
	}

	public void setParentRule(Rule parentRule)
	{
		this.parentRule = parentRule;
	}
}