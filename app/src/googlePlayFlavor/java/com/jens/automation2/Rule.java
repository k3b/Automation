package com.jens.automation2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.DetectedActivity;
import com.jens.automation2.location.LocationProvider;
import com.jens.automation2.location.WifiBroadcastReceiver;
import com.jens.automation2.receivers.ActivityDetectionReceiver;
import com.jens.automation2.receivers.BatteryReceiver;
import com.jens.automation2.receivers.BluetoothReceiver;
import com.jens.automation2.receivers.ConnectivityReceiver;
import com.jens.automation2.receivers.HeadphoneJackListener;
import com.jens.automation2.receivers.NfcReceiver;
import com.jens.automation2.receivers.NoiseListener;
import com.jens.automation2.receivers.NotificationListener;
import com.jens.automation2.receivers.PhoneStatusListener;
import com.jens.automation2.receivers.ProcessListener;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.jens.automation2.Trigger.triggerParameter2Split;
import static com.jens.automation2.receivers.NotificationListener.EXTRA_TEXT;
import static com.jens.automation2.receivers.NotificationListener.EXTRA_TITLE;

public class Rule implements Comparable<Rule>
{
	private static ArrayList<Rule> ruleCollection = new ArrayList<Rule>();
	public static boolean isAnyRuleActive = false;
	
	private static ArrayList<Rule> ruleRunHistory = new ArrayList<Rule>();
	
	public static ArrayList<Rule> getRuleRunHistory()
	{
		return ruleRunHistory;
	}
	
	private ArrayList<Trigger> triggerSet;
	private ArrayList<Action> actionSet;
	private String name;
	private boolean ruleActive = true;		// rules can be deactivated, so they won't fire if you don't want them temporarily
	private boolean ruleToggle = false;		// rule will run again and do the opposite of its actions if applicable
	private Calendar lastExecution;
	
	private static Date lastActivatedRuleActivationTime;

	public Calendar getLastExecution()
	{
		return lastExecution;
	}

	public void setLastExecution(Calendar lastExecution)
	{
		this.lastExecution = lastExecution;
	}

	public boolean isRuleToggle()
	{
		return ruleToggle;
	}
	public void setRuleToggle(boolean ruleToggle)
	{
		this.ruleToggle = ruleToggle;
	}
	public static ArrayList<Rule> getRuleCollection()
	{
		return ruleCollection;
	}
	public boolean isRuleActive()
	{
		return ruleActive;
	}
	public void setRuleActive(boolean ruleActive)
	{
		this.ruleActive = ruleActive;
	}
	public static void setRuleCollection(ArrayList<Rule> ruleCollection)
	{
		Rule.ruleCollection = ruleCollection;
	}
	public static Date getLastActivatedRuleActivationTime()
	{
		return lastActivatedRuleActivationTime;
	}
	public static Rule getLastActivatedRule()
	{
		if(ruleRunHistory.size() > 0)
			return ruleRunHistory.get(0);
		else
			return null;
	}
	public ArrayList<Trigger> getTriggerSet()
	{
		return triggerSet;
	}
	public void setTriggerSet(ArrayList<Trigger> triggerSet)
	{
		this.triggerSet = triggerSet;
	}
	public ArrayList<Action> getActionSet()
	{
		return actionSet;
	}
	public void setActionSet(ArrayList<Action> actionSet)
	{
		this.actionSet = actionSet;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}

	public static void readFromFile()
	{
		ruleCollection = XmlFileInterface.ruleCollection;
	}
	@Override
	public String toString()
	{
		return this.getName();
	}
	@SuppressLint("NewApi")
	public String toStringLong()
	{
		String returnString = "";
		
		if(isRuleActive())
			returnString += "Active: ";
		else
			returnString += "Inactive: ";

		returnString += this.getName() + ": If ";
		
		for(int i=0; i<this.getTriggerSet().size(); i++)
		{
			returnString += this.getTriggerSet().get(i).toString();
			
			if(i != this.getTriggerSet().size()-1) //if not the last loop
				returnString += " and ";
		}

		returnString += " then ";
		
		for(int i=0; i<this.getActionSet().size(); i++)
		{
			returnString += this.getActionSet().get(i).toString();
			
			if(i != this.getActionSet().size()-1) //if not the last loop
				returnString += " and ";
		}
		
		return returnString;
	}
	
	public boolean create(Context context)
	{
		if(this.checkBeforeSaving(context, false))
		{
			Miscellaneous.logEvent("i", "Rule", "Creating rule: " + this.toString(), 3);
			ruleCollection.add(this);
			boolean returnValue = XmlFileInterface.writeFile();
			
			if(returnValue)
			{
				AutomationService service = AutomationService.getInstance();
				if(service != null)
					service.applySettingsAndRules();
			}
			
			return returnValue;
		}
		else
			return false;
	}
	public boolean change(Context context)
	{
		if(this.checkBeforeSaving(context, true))
		{
			Miscellaneous.logEvent("i", "Rule", "Changing rule: " + this.toString(), 3);
			boolean returnValue = XmlFileInterface.writeFile();
			
			if(returnValue)
			{
				AutomationService service = AutomationService.getInstance();
				if(service != null)
					service.applySettingsAndRules();
			}
			
			return returnValue;
		}
		else
			return false;
	}
	public boolean delete()
	{
		Miscellaneous.logEvent("i", "Rule", "Deleting rule: " + this.toString(), 3);
		ruleCollection.remove(this);
		
		AutomationService service = AutomationService.getInstance();
		if(service != null)
			service.applySettingsAndRules();
		
		return XmlFileInterface.writeFile();
	}
	
	private boolean checkBeforeSaving(Context context, boolean changeExistingRule)
	{
		if(this.getName() == null || this.getName().length()==0)
		{
			Toast.makeText(context, context.getResources().getString(R.string.pleaseEnterValidName), Toast.LENGTH_LONG).show();
			return false;
		}
		
		if(!changeExistingRule)
			for(Rule rule : Rule.ruleCollection)
				if(rule.getName().equals(this.getName()))
				{
					Toast.makeText(context, context.getResources().getString(R.string.anotherRuleByThatName), Toast.LENGTH_LONG).show();
					return false;
				}
		
		if(this.getTriggerSet().size()==0)
		{
			Toast.makeText(context, context.getResources().getString(R.string.pleaseSpecifiyTrigger), Toast.LENGTH_LONG).show();
			return false;
		}
		
		if(this.getActionSet().size()==0)
		{
			Toast.makeText(context, context.getResources().getString(R.string.pleaseSpecifiyAction), Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}
	
	public boolean isActuallyToggable()
	{		
		boolean result = hasToggableTrigger() && hasToggableAction();
		if(result)
			Miscellaneous.logEvent("i", "Rule", String.format(Miscellaneous.getAnyContext().getString(R.string.ruleToggable), this.getName()), 4);
		else
			Miscellaneous.logEvent("i", "Rule", String.format(Miscellaneous.getAnyContext().getString(R.string.ruleNotToggable), this.getName()), 4);
		
		return result;
	}
	private boolean hasToggableTrigger()
	{
		for(Trigger trigger : this.getTriggerSet())
		{
			switch(trigger.getTriggerType())
			{
				case airplaneMode:
					break;
				case batteryLevel:
					break;
				case charging:
					break;
				case nfcTag:
					return true;
				case noiseLevel:
					break;
				case phoneCall:
					break;
				case pointOfInterest:
					break;
				case process_started_stopped:
					break;
				case roaming:
					break;
				case speed:
					break;
				case timeFrame:
					break;
				case usb_host_connection:
					break;
				case wifiConnection:
					break;
				default:
					break;			
			}
		}
		
		return false;
	}
	private boolean hasToggableAction()
	{
		for(Action action : this.getActionSet())
		{
			// Is there any action that can just be switched?
			switch(action.getAction())
			{
				case setAirplaneMode:
					return true;
				case setBluetooth:
					return true;
				case setDataConnection:
					return true;
				case setDisplayRotation:
					return true;
				case setUsbTethering:
					return true;
				case setWifi:
					return true;
				case setWifiTethering:
					return true;
				default:
					break;					
			}
		}
		
		return false;
	}
	
	public boolean applies(Context context)
	{
		if(AutomationService.getInstance() == null)
		{
			Miscellaneous.logEvent("i", "RuleCheck", "Automation service not running. Rule cannot apply.", 3);
			return false;
		}
		
		if(this.ruleActive)
		{
			for(Trigger oneTrigger : this.getTriggerSet())
			{
				if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.pointOfInterest))
				{
					// Am I here?
					PointOfInterest activePoi = PointOfInterest.getActivePoi();
					if(activePoi != null)	//entering one
					{
						if(oneTrigger.getPointOfInterest() != null)
						{
							if(activePoi.equals(oneTrigger.getPointOfInterest()))
							{
								if(!oneTrigger.getTriggerParameter())
								{
									Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), "Rule doesn't apply. We're entering POI: " + oneTrigger.getPointOfInterest().getName() + ", not leaving it.", 4);
									return false;
								}
							}
							else
							{
								Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), "Rule doesn't apply. This is " + activePoi.getName() + ", not " + oneTrigger.getPointOfInterest().getName() + ".", 4);
								return false;
							}
						}
						else if(oneTrigger.getPointOfInterest() == null)
						{
							if(oneTrigger.getTriggerParameter())
							{
								Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), "Rule doesn't apply. We're at a POI. Rule specifies not at none, so leaving any.", 4);
								return false;
							}
						}
					}
					else					//leaving one
					{
						// We are not at any POI. But if this trigger requires us NOT to be there, that may be fine.
						if(oneTrigger.getPointOfInterest() != null)
						{
//							if(activePoi.equals(oneTrigger.getPointOfInterest()))
//							{
								if(!oneTrigger.getTriggerParameter())
								{
									Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), "We are not at POI \"" + oneTrigger.getPointOfInterest().getName() + "\". But since that's required by this rule that's fine.", 4);
								}
								else
								{
									Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), "Rule doesn't apply. We're not at POI \"" + oneTrigger.getPointOfInterest().getName() + "\".", 3);
									return false;
								}
//							}
						}
						else if(oneTrigger.getPointOfInterest() == null)
						{
							if(!oneTrigger.getTriggerParameter())
							{
								Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), "Rule doesn't apply. We're at no POI. Rule specifies to be at anyone.", 5);
								return false;
							}
						}
					}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.timeFrame))
				{
					Date now = new Date();
					String timeString = String.valueOf(now.getHours()) + ":" + String.valueOf(now.getMinutes()) + ":" + String.valueOf(now.getSeconds());
					Time nowTime = Time.valueOf(timeString);
					Calendar calNow = Calendar.getInstance();
	
					
					if(oneTrigger.getTimeFrame().getDayList().contains(calNow.get(Calendar.DAY_OF_WEEK)))
					{
						if(	
									// Regular case, start time is lower than end time
								(
										Miscellaneous.compareTimes(oneTrigger.getTimeFrame().getTriggerTimeStart(), nowTime) >= 0
													&&
										Miscellaneous.compareTimes(nowTime, oneTrigger.getTimeFrame().getTriggerTimeStop()) > 0
								)
									||
									// Other case, start time higher than end time, timeframe goes over midnight
								(
										Miscellaneous.compareTimes(oneTrigger.getTimeFrame().getTriggerTimeStart(), oneTrigger.getTimeFrame().getTriggerTimeStop()) < 0
											&&
										(Miscellaneous.compareTimes(oneTrigger.getTimeFrame().getTriggerTimeStart(), nowTime) >= 0
											||
										Miscellaneous.compareTimes(nowTime, oneTrigger.getTimeFrame().getTriggerTimeStop()) > 0)
								)
							
							)
						{
							// We are in the timeframe
							Miscellaneous.logEvent("i", "TimeFrame", "We're currently (" + calNow.getTime().toString() + ") in the specified TimeFrame (" + oneTrigger.getTimeFrame().toString() + "). Trigger  of Rule " + this.getName() + " applies.", 3);
							if(oneTrigger.getTriggerParameter())
							{
								Miscellaneous.logEvent("i", "TimeFrame", "That's what's specified. Trigger of Rule " + this.getName() + " applies.", 3);
								//return true;
							}
							else
							{
								Miscellaneous.logEvent("i", "TimeFrame", "That's not what's specified. Trigger of Rule " + this.getName() + " doesn't apply.", 3);
								return false;
							}
						}
						else
						{
							Miscellaneous.logEvent("i", "TimeFrame", "We're currently (" + calNow.getTime().toString() + ", Day: " + String.valueOf(calNow.get(Calendar.DAY_OF_WEEK)) + ") not in the specified TimeFrame (" + oneTrigger.getTimeFrame().toString() + ") because of the time. Trigger of Rule " + this.getName() + " doesn\'t apply..", 5);
							if(!oneTrigger.getTriggerParameter())
							{
								Miscellaneous.logEvent("i", "TimeFrame", "That's what's specified. Trigger of Rule " + this.getName() + " applies.", 5);
								//return true;
							}
							else
							{
								Miscellaneous.logEvent("i", "TimeFrame", "That's not what's specified. Trigger of Rule " + this.getName() + " doesn't apply.", 5);
								return false;
							}
	//						return false;
						}
					}
					else
					{
						Miscellaneous.logEvent("i", "TimeFrame", "We're currently (" + calNow.getTime().toString() + ", Day: " + String.valueOf(calNow.get(Calendar.DAY_OF_WEEK)) + ") not in the specified TimeFrame (" + oneTrigger.getTimeFrame().toString() + ") because of the day. Trigger of Rule " + this.getName() + " doesn\'t apply.", 5);
						return false;
					}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.charging))
				{
					if(BatteryReceiver.isDeviceCharging(context) == 0)
					{
						return false; // unknown charging state, can't activate rule under these conditions
					}
					else if(BatteryReceiver.isDeviceCharging(context) == 1)
					{
						if(oneTrigger.getTriggerParameter()) //rule says when charging, but we're currently discharging
							return false;
					}
					else if(BatteryReceiver.isDeviceCharging(context) == 2)
					{
						if(!oneTrigger.getTriggerParameter()) //rule says when discharging, but we're currently charging
							return false;
					}				
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.usb_host_connection))
				{
					if(BatteryReceiver.isUsbHostConnected() != oneTrigger.getTriggerParameter())
					{
						return false;
					}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.batteryLevel))
				{
					if(oneTrigger.getTriggerParameter())
					{
						if(BatteryReceiver.getBatteryLevel() <= oneTrigger.getBatteryLevel())
						{
							Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), context.getResources().getString(R.string.ruleDoesntApplyBatteryLowerThan) + " " + String.valueOf(oneTrigger.getBatteryLevel()), 3);
							return false;
						}
					}
					else
					{
						if(oneTrigger.getBatteryLevel() >= oneTrigger.getBatteryLevel())
						{
							Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), context.getResources().getString(R.string.ruleDoesntApplyBatteryHigherThan) + " " + String.valueOf(oneTrigger.getBatteryLevel()), 3);
							return false;
						}
					}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.speed))
				{
					if(oneTrigger.getTriggerParameter())
					{
						if(LocationProvider.getSpeed() < oneTrigger.getSpeed())
						{
							Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), context.getResources().getString(R.string.ruleDoesntApplyWeAreSlowerThan) + " " + String.valueOf(oneTrigger.getSpeed()), 3);
							return false;
						}
					}
					else
					{
						if(LocationProvider.getSpeed() > oneTrigger.getSpeed())
						{
							Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), context.getResources().getString(R.string.ruleDoesntApplyWeAreFasterThan) + " " + String.valueOf(oneTrigger.getSpeed()), 3);
							return false;
						}
					}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.noiseLevel))
				{
					if(oneTrigger.getTriggerParameter())
					{
						if(NoiseListener.getNoiseLevelDb() < oneTrigger.getNoiseLevelDb())
						{
							Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), context.getResources().getString(R.string.ruleDoesntApplyItsQuieterThan) + " " + String.valueOf(oneTrigger.getNoiseLevelDb()), 3);
							return false;
						}
					}
					else
					{
						if(NoiseListener.getNoiseLevelDb() > oneTrigger.getNoiseLevelDb())
						{
							Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), context.getResources().getString(R.string.ruleDoesntApplyItsLouderThan) + " " + String.valueOf(oneTrigger.getNoiseLevelDb()), 3);
							return false;
						}
					}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.wifiConnection))
				{
					Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), "Checking for wifi state", 4);
					if(oneTrigger.getTriggerParameter() == WifiBroadcastReceiver.lastConnectedState)	// connected / disconnected
					{
						if(oneTrigger.getWifiName().length() > 0)	// only check if any wifi name specified, otherwise any wifi will do
						{
							Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), "Wifi name specified, checking that.", 4);
							if(!WifiBroadcastReceiver.getLastWifiSsid().equals(oneTrigger.getWifiName()))
							{
								Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), String.format(context.getResources().getString(R.string.ruleDoesntApplyNotTheCorrectSsid), oneTrigger.getWifiName(), WifiBroadcastReceiver.getLastWifiSsid()), 3);
								return false;
							}
							else
								Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), "Wifi name matches. Rule will apply.", 4);
						}
						else
							Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), "No wifi name specified, any will do.", 4);
					}
					else
					{
						Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), "Wifi state not correct, demanded " + String.valueOf(oneTrigger.getTriggerParameter() + ", got " + String.valueOf(WifiBroadcastReceiver.lastConnectedState)), 4);
						return false;
					}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.process_started_stopped))
				{
					boolean running = ProcessListener.getRunningApps().contains(oneTrigger.getProcessName());
					
					if(running)
						Miscellaneous.logEvent("i", "ProcessMonitoring", "App " + oneTrigger.getProcessName() + " is currently running.", 4);
					else
						Miscellaneous.logEvent("i", "ProcessMonitoring", "App " + oneTrigger.getProcessName() + " is not running.", 4);
					
					if(running != oneTrigger.getTriggerParameter())
					{
						Miscellaneous.logEvent("i", "ProcessMonitoring", "Trigger doesn't apply.", 4);
						return false;
					}
					
					Miscellaneous.logEvent("i", "ProcessMonitoring", "Trigger applies.", 4);
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.airplaneMode))
				{
					if(ConnectivityReceiver.isAirplaneMode(context) != oneTrigger.getTriggerParameter())
					{
						return false;
					}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.roaming))
				{
					if(ConnectivityReceiver.isRoaming(context) != oneTrigger.getTriggerParameter())
					{
						return false;
					}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.phoneCall))
				{
					String[] elements = oneTrigger.getTriggerParameter2().split(triggerParameter2Split);
					// state dir number

					if(elements[2].equals(Trigger.triggerPhoneCallNumberAny) || PhoneStatusListener.getLastPhoneNumber().matches(elements[2]))
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
								Miscellaneous.logEvent("i", "Rule", "Rule doesn't apply. Wrong direction. Demanded: " + String.valueOf(oneTrigger.getPhoneDirection()) + ", got: " + String.valueOf(PhoneStatusListener.getLastPhoneDirection()), 4);
								return false;	
							}
						}
						else
						{
							Miscellaneous.logEvent("i", "Rule", "Rule doesn't apply. Wrong call status. Demanded: " + String.valueOf(oneTrigger.getTriggerParameter()) + ", got: " + String.valueOf(PhoneStatusListener.isInACall()), 4);
							return false;
						}
					}
					else
					{
						Miscellaneous.logEvent("i", "Rule", "Rule doesn't apply. Wrong phone number. Demanded: " + oneTrigger.getPhoneNumber() + ", got: " + PhoneStatusListener.getLastPhoneNumber(), 4);
						return false;
					}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.nfcTag))
				{
					if(NfcReceiver.lastReadLabel == null)
					{
						Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), context.getResources().getString(R.string.ruleDoesntApplyNoTagLabel), 3);
						return false;
					}
					else if(!NfcReceiver.lastReadLabel.equals(oneTrigger.getNfcTagId()))
					{
						Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), context.getResources().getString(R.string.ruleDoesntApplyWrongTagLabel) + " " + NfcReceiver.lastReadLabel + " / " + oneTrigger.getNfcTagId(), 3);
						return false;
					}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.activityDetection))
				{
					if(ActivityDetectionReceiver.getActivityDetectionLastResult() != null)
					{
						boolean found = false;
						for(DetectedActivity oneDetectedActivity : ActivityDetectionReceiver.getActivityDetectionLastResult().getProbableActivities())
						{
							if(oneDetectedActivity.getType() == oneTrigger.getActivityDetectionType())
								found = true;
						}

						if(!found)
						{
							Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), String.format(context.getResources().getString(R.string.ruleDoesntApplyActivityNotPresent), ActivityDetectionReceiver.getDescription(oneTrigger.getActivityDetectionType())), 3);
							return false;
						}
						else
						{
							for(DetectedActivity oneDetectedActivity : ActivityDetectionReceiver.getActivityDetectionLastResult().getProbableActivities())
							{							
								if(oneDetectedActivity.getType() == oneTrigger.getActivityDetectionType() && oneDetectedActivity.getConfidence() < Settings.activityDetectionRequiredProbability)
								{
									Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), String.format(context.getResources().getString(R.string.ruleDoesntApplyActivityGivenButTooLowProbability), ActivityDetectionReceiver.getDescription(oneDetectedActivity.getType()), String.valueOf(oneDetectedActivity.getConfidence()), String.valueOf(Settings.activityDetectionRequiredProbability)), 3);
									return false;
								}
							}
						}
					}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.bluetoothConnection))
				{
					Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), "Checking for bluetooth...", 4);

					if(oneTrigger.getBluetoothDeviceAddress().equals("<any>"))
					{
						if(oneTrigger.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_CONNECTED))
						{
							if(BluetoothReceiver.isAnyDeviceConnected() != oneTrigger.getTriggerParameter())
								return false;
						}
						else if((oneTrigger.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)))
						{
							if(BluetoothReceiver.isAnyDeviceConnected() != oneTrigger.getTriggerParameter())
								return false;
						}
						else
						{
							// range
							if(BluetoothReceiver.isAnyDeviceInRange() != oneTrigger.getTriggerParameter())
								return false;
						}
					}
					else if(oneTrigger.getBluetoothDeviceAddress().equals("<none>"))
					{
						if(oneTrigger.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_CONNECTED))
						{
							if(BluetoothReceiver.isAnyDeviceConnected() == oneTrigger.getTriggerParameter())
								return false;
						}
						else if((oneTrigger.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)))
						{
							if(BluetoothReceiver.isAnyDeviceConnected() == oneTrigger.getTriggerParameter())
								return false;
						}
						else
						{
							// range
							if(BluetoothReceiver.isAnyDeviceInRange() == oneTrigger.getTriggerParameter())
								return false;
						}
					}
					else if(oneTrigger.getBluetoothDeviceAddress().length() > 0)
					{
						if(oneTrigger.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_CONNECTED))
						{
							if(BluetoothReceiver.isDeviceCurrentlyConnected(BluetoothReceiver.getDeviceByAddress(oneTrigger.getBluetoothDeviceAddress())) != oneTrigger.getTriggerParameter())
								return false;
						}
						else if((oneTrigger.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)))
						{
							if(BluetoothReceiver.isDeviceCurrentlyConnected(BluetoothReceiver.getDeviceByAddress(oneTrigger.getBluetoothDeviceAddress())) != oneTrigger.getTriggerParameter())
								return false;
						}
						else
						{
							// range
							if(BluetoothReceiver.isDeviceInRange(BluetoothReceiver.getDeviceByAddress(oneTrigger.getBluetoothDeviceAddress())) != oneTrigger.getTriggerParameter())
								return false;
						}
					}
					else
					{
						Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), context.getResources().getString(R.string.ruleDoesntApplyStateNotCorrect), 3);
						return false;
					}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.headsetPlugged))
				{
					if(HeadphoneJackListener.isHeadsetConnected() != oneTrigger.getTriggerParameter())
						return false;
					else
						if(oneTrigger.getHeadphoneType() != 2 && oneTrigger.getHeadphoneType() != HeadphoneJackListener.getHeadphoneType())
						{
							Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), context.getResources().getString(R.string.ruleDoesntApplyWrongHeadphoneType), 3);
							return false;
						}
				}
				else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.notification))
				{
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
					{
						String[] params = oneTrigger.getTriggerParameter2().split(triggerParameter2Split);

						String myApp = params[0];
						String myTitleDir = params[1];
						String myTitle = params[2];
						String myTextDir = params[3];
						String myText;
						if (params.length >= 5)
							myText = params[4];
						else
							myText = "";

						if(oneTrigger.getTriggerParameter())
						{
							// Check an active notification that is still there

							boolean foundMatch = false;

							for (StatusBarNotification sbn : NotificationListener.getInstance().getActiveNotifications())
							{
								if(getLastExecution() == null || sbn.getPostTime() > this.lastExecution.getTimeInMillis())
								{
									String app = sbn.getPackageName();
									String title = sbn.getNotification().extras.getString(EXTRA_TITLE);
									String text = sbn.getNotification().extras.getString(EXTRA_TEXT);

									Miscellaneous.logEvent("i", "NotificationCheck", "Checking if this notification matches our rule " + this.getName() + ". App: " + app + ", title: " + title + ", text: " + text, 5);

									if (!myApp.equals("-1"))
									{
										if (!app.equalsIgnoreCase(myApp))
										{
											Miscellaneous.logEvent("i", "NotificationCheck", "Notification app name does not match rule.", 5);
											continue;
										}
									}

									if (myTitle.length() > 0)
									{
										if (!Miscellaneous.compare(myTitleDir, myTitle, title))
										{
											Miscellaneous.logEvent("i", "NotificationCheck", "Notification title does not match rule.", 5);
											continue;
										}
									}

									if (myText.length() > 0)
									{
										if (!Miscellaneous.compare(myTextDir, myText, text))
										{
											Miscellaneous.logEvent("i", "NotificationCheck", "Notification text does not match rule.", 5);
											continue;
										}
									}

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

									if (!myApp.equals("-1"))
									{
										if (!app.equalsIgnoreCase(myApp))
											return false;
									}

									if (myTitle.length() > 0)
									{
										if (!Miscellaneous.compare(myTitleDir, title, myTitle))
											return false;
									}

									if (myText.length() > 0)
									{
										if (!Miscellaneous.compare(myTextDir, text, myText))
											return false;
									}
								}
								else
									return false;
							}
						}
					}
				}
			}
			
			return true;
		}
		
		Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), String.format(context.getResources().getString(R.string.ruleIsDeactivatedCantApply), this.getName()), 3);
		return false;
	}
	
	private class ActivateRuleTask extends AsyncTask<Object, String, Void>
	{
		boolean wasActivated = false;

		@Override
		protected Void doInBackground(Object... params)
		{
//			Miscellaneous.logEvent("i", "Rule", ((Context) params[0]).getResources().getString(R.string.usingNewThreadForRuleExecution), 5);
			
			Thread.setDefaultUncaughtExceptionHandler(Miscellaneous.uncaughtExceptionHandler);

			// without this line debugger will - for some reason - skip all breakpoints in this class
			if(android.os.Debug.isDebuggerConnected())
				android.os.Debug.waitForDebugger();
			
	        if (Looper.myLooper() == null)
	        	Looper.prepare();
	        
			wasActivated = activateInternally((AutomationService)params[0], (Boolean)params[1]);

			return null;
		}

		@Override
		protected void onProgressUpdate(String... messages)
		{
			AutomationService service = AutomationService.getInstance();
			service.speak(messages[0], false);
			Toast.makeText(service, messages[0], Toast.LENGTH_LONG).show();
			
			super.onProgressUpdate(messages);
		}

		@Override
		protected void onPostExecute(Void result)
		{
			/*
			 	Only update if the rules was actually executed. Became necessary for the notification trigger. If a user created a rule
			 	with a notification trigger and this app creates a notification itself this will otherwise end in an infinite loop.
			 */
			if(wasActivated)
			{
				setLastExecution(Calendar.getInstance());
				AutomationService.updateNotification();
				ActivityMainScreen.updateMainScreen();
				super.onPostExecute(result);
			}
		}

		/**
		 * Will activate the rule. Should be called by a separate execution thread
		 * @param automationService
		 */
		protected boolean activateInternally(AutomationService automationService, boolean force)
		{
			boolean isActuallyToggable = isActuallyToggable();

			boolean notLastActive = getLastActivatedRule() == null || !getLastActivatedRule().equals(Rule.this);
			boolean doToggle = ruleToggle && isActuallyToggable;

			if(notLastActive || force || doToggle)
			{
				String message;
				if(!doToggle)
					message = String.format(automationService.getResources().getString(R.string.ruleActivate), Rule.this.getName());
				else
					message = String.format(automationService.getResources().getString(R.string.ruleActivateToggle), Rule.this.getName());
				Miscellaneous.logEvent("i", "Rule", message, 2);
//				automationService.speak(message);
//				Toast.makeText(automationService, message, Toast.LENGTH_LONG).show();
				if(Settings.startNewThreadForRuleActivation)
					publishProgress(message);

				for(int i = 0; i< Rule.this.getActionSet().size(); i++)
				{
					try
					{
						Rule.this.getActionSet().get(i).run(automationService, doToggle);
					}
					catch(Exception e)
					{
						Miscellaneous.logEvent("e", "RuleExecution", "Error running action of rule " + Rule.this.getName() + ": " + Log.getStackTraceString(e), 1);
					}
				}

				// Keep log of last x rule activations (Settings)
				try
				{
					Rule.ruleRunHistory.add(0, Rule.this);		// add at beginning for better visualization
					Rule.lastActivatedRuleActivationTime = new Date();
					while(ruleRunHistory.size() > Settings.rulesThatHaveBeenRanHistorySize)
						ruleRunHistory.remove(ruleRunHistory.size()-1);
					String history = "";
					for(Rule rule : ruleRunHistory)
						history += rule.getName() + ", ";
					if(history.length() > 0)
						history = history.substring(0, history.length()-2);
					Miscellaneous.logEvent("i", "Rule history", "Most recent first: " + history, 4);
				}
				catch(Exception e)
				{
					Miscellaneous.logEvent("e", "Rule history error", Log.getStackTraceString(e), 3);
				}

				Miscellaneous.logEvent("i", "Rule", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleActivationComplete), Rule.this.getName()), 2);
			}
			else
			{
				Miscellaneous.logEvent("i", "Rule", "Request to activate rule " + Rule.this.getName() + ", but it is the last one that was activated. Won't do it again.", 3);
				return false;
			}

			return true;
		}
	}
	
	public void activate(AutomationService automationService, boolean force)
	{
		ActivateRuleTask task = new ActivateRuleTask();
		
//		if(Settings.startNewThreadForRuleActivation)
			task.execute(automationService, force);
//		else
//		{
//			task.activateInternally(automationService, force);
//			AutomationService.updateNotification();
//			ActivityMainScreen.updateMainScreen();
//		}		
	}
	
	public static ArrayList<Rule> findRuleCandidatesByPoi(PointOfInterest searchPoi, boolean triggerParameter)
	{
		Miscellaneous.logEvent("i", "RuleSearch", "Searching for rules referencing POI " + searchPoi.getName() + ". Total size of ruleset: " + String.valueOf(ruleCollection.size()), 4);
		
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(int i=0; i<ruleCollection.size(); i++)
		{
			innerloop:
			for(int j=0; j<ruleCollection.get(i).getTriggerSet().size(); j++)
			{
				if(ruleCollection.get(i).getTriggerSet().get(j).getTriggerType() == Trigger.Trigger_Enum.pointOfInterest)
				{
					if(ruleCollection.get(i).getTriggerSet().get(j).getTriggerParameter() == triggerParameter)
					{
						if(ruleCollection.get(i).getTriggerSet().get(j).getPointOfInterest() != null && ruleCollection.get(i).getTriggerSet().get(j).getPointOfInterest().equals(searchPoi))
						{
	//						Miscellaneous.logEvent("i", "RuleSearch", "Rule found with POI " + searchPoi.getName() + ". Checking if parameter is correct");
							ruleCandidates.add(ruleCollection.get(i));
							break innerloop; //if the poi is found we don't need to search the other triggers in the same rule
						}
					}
					else
					{
						if(ruleCollection.get(i).getTriggerSet().get(j).getPointOfInterest() == null)
						{
	//						Miscellaneous.logEvent("i", "RuleSearch", "Rule found with POI " + searchPoi.getName() + ". Checking if parameter is correct");
							ruleCandidates.add(ruleCollection.get(i));
							break innerloop; //if the poi is found we don't need to search the other triggers in the same rule
						}
					}
				}
			}
		}
		
		if(ruleCandidates.size() == 0)
			Miscellaneous.logEvent("i", "RuleSearch", "No rule with Poi " + searchPoi.getName() + " found.", 3);
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByTimeFrame(TimeFrame searchTimeFrame, boolean triggerParameter)
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(int i=0; i<ruleCollection.size(); i++)
		{
			innerloop:
			for(int j=0; j<ruleCollection.get(i).getTriggerSet().size(); j++)
			{
				if(ruleCollection.get(i).getTriggerSet().get(j).getTriggerType() == Trigger.Trigger_Enum.timeFrame)
				{
					if(ruleCollection.get(i).getTriggerSet().get(j).getTimeFrame().equals(searchTimeFrame) && ruleCollection.get(i).getTriggerSet().get(j).getTriggerParameter() == triggerParameter)
					{
						ruleCandidates.add(ruleCollection.get(i));
						break innerloop; //if the poi is found we don't need to search the other triggers in the same rule
					}
				}
			}
		}
		
		return ruleCandidates;
	}
	public static ArrayList<Rule> findRuleCandidatesByTime(Time searchTime)
	{
		Miscellaneous.logEvent("i", "RuleSearch", "Searching for rules with TimeFrame with time " + searchTime.toString() + ". RuleCollection-Size: " + String.valueOf(ruleCollection.size()), 3);;
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.timeFrame)
				{
					Miscellaneous.logEvent("i", "RuleSearch", "Searching interval: " + oneTrigger.getTimeFrame().getTriggerTimeStart().toString() + " to " + oneTrigger.getTimeFrame().getTriggerTimeStop().toString(), 5);
					Miscellaneous.logEvent("i", "RuleSearch", "interval start: " + String.valueOf(oneTrigger.getTimeFrame().getTriggerTimeStart().getTime()), 5);
					Miscellaneous.logEvent("i", "RuleSearch", "search time: " + String.valueOf(searchTime.getTime()), 5);
					Miscellaneous.logEvent("i", "RuleSearch", "interval stop: " + String.valueOf(oneTrigger.getTimeFrame().getTriggerTimeStop().getTime()), 5);
					
					if(oneTrigger.getTimeFrame().getTriggerTimeStart().getTime() > oneTrigger.getTimeFrame().getTriggerTimeStop().getTime())
					{
						Miscellaneous.logEvent("i", "Timeframe search", "Rule goes over midnight.", 5);
						if(oneTrigger.getTimeFrame().getTriggerTimeStart().getTime() <= searchTime.getTime() || searchTime.getTime() <= oneTrigger.getTimeFrame().getTriggerTimeStop().getTime()+20000) //add 20 seconds because of delay
						{
							ruleCandidates.add(oneRule);
							break innerloop; //if the poi is found we don't need to search the other triggers in the same rule
						}
					}
					else if(oneTrigger.getTimeFrame().getTriggerTimeStart().getTime() <= searchTime.getTime() && searchTime.getTime() <= oneTrigger.getTimeFrame().getTriggerTimeStop().getTime()+20000) //add 20 seconds because of delay
					{
						Miscellaneous.logEvent("i", "RuleSearch", "Rule found with TimeFrame with time " + searchTime.toString(), 3);
						ruleCandidates.add(oneRule);
						break innerloop; //if the poi is found we don't need to search the other triggers in the same rule
					}
				}
			}
		}

		Miscellaneous.logEvent("i", "RuleSearch", String.valueOf(ruleCandidates.size()) + " Rule(s) found with TimeFrame with time " + searchTime.toString(), 3);
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByTimeFrame()
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.timeFrame)
				{
					ruleCandidates.add(oneRule);
					break innerloop; //if the poi is found we don't need to search the other triggers in the same rule
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByCharging(boolean triggerParameter)
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.charging)
				{
					if(oneTrigger.getTriggerParameter() == triggerParameter)
					{
						ruleCandidates.add(oneRule);
						break innerloop; //if the poi is found we don't need to search the other triggers in the same rule
					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByUsbHost(boolean triggerParameter)
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.usb_host_connection)
				{
					if(oneTrigger.getTriggerParameter() == triggerParameter)
					{
						ruleCandidates.add(oneRule);
						break innerloop; //if the poi is found we don't need to search the other triggers in the same rule
					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByBatteryLevel()
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.batteryLevel)
				{
//					if(oneTrigger.getTriggerParameter() == triggerParameter)
//					{
						ruleCandidates.add(oneRule);
						break innerloop; //if the poi is found we don't need to search the other triggers in the same rule
//					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesBySpeed()
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.speed)
				{
//					if(oneTrigger.getTriggerParameter() == triggerParameter)
//					{
						ruleCandidates.add(oneRule);
						break innerloop; //if the poi is found we don't need to search the other triggers in the same rule
//					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByNoiseLevel()
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.noiseLevel)
				{
//					if(oneTrigger.getTriggerParameter() == triggerParameter)
//					{
						ruleCandidates.add(oneRule);
						break innerloop; //if the poi is found we don't need to search the other triggers in the same rule
//					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByWifiConnection()
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.wifiConnection)
				{
//					if(oneTrigger.getTriggerParameter() == triggerParameter)
//					{
						ruleCandidates.add(oneRule);
						break innerloop; //we don't need to search the other triggers in the same rule
//					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByBluetoothConnection()
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.bluetoothConnection)
				{
//					if(oneTrigger.getTriggerParameter() == triggerParameter)
//					{
						ruleCandidates.add(oneRule);
						break innerloop; //we don't need to search the other triggers in the same rule
//					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByProcess()
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.process_started_stopped)
				{
//					if(oneTrigger.getTriggerParameter() == triggerParameter)
//					{
						ruleCandidates.add(oneRule);
						break innerloop; //we don't need to search the other triggers in the same rule
//					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByAirplaneMode(boolean triggerParameter)
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.airplaneMode)
				{
					if(oneTrigger.getTriggerParameter() == triggerParameter)
					{
						ruleCandidates.add(oneRule);
						break innerloop; //we don't need to search the other triggers in the same rule
					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByRoaming(boolean triggerParameter)
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.roaming)
				{
					if(oneTrigger.getTriggerParameter() == triggerParameter)
					{
						ruleCandidates.add(oneRule);
						break innerloop; //we don't need to search the other triggers in the same rule
					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByPhoneCall(String direction)
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();

		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.phoneCall)
				{
					String[] elements = oneTrigger.getTriggerParameter2().split(triggerParameter2Split);
					if(elements[1].equals(Trigger.triggerPhoneCallDirectionAny) || elements[1].equals(direction))
					{
						ruleCandidates.add(oneRule);
						break innerloop; //we don't need to search the other triggers in the same rule
					}
				}
			}
		}

		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByNfc()
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.nfcTag)
				{
//					if(oneTrigger.getTriggerParameter() == triggerParameter)
//					{
						ruleCandidates.add(oneRule);
						break innerloop; //we don't need to search the other triggers in the same rule
//					}
				}
			}
		}
		
		return ruleCandidates;
	}

	public static ArrayList<Rule> findRuleCandidates(Trigger.Trigger_Enum triggerType)
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();

		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == triggerType)
				{
					ruleCandidates.add(oneRule);
					break innerloop; //we don't need to search the other triggers in the same rule
				}
			}
		}

		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByActivityDetection()
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.activityDetection)
				{
//					if(oneTrigger.getTriggerParameter() == triggerParameter)
//					{
						ruleCandidates.add(oneRule);
						break innerloop; //we don't need to search the other triggers in the same rule
//					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByPoi(PointOfInterest searchPoi)
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.pointOfInterest)
				{
					if(oneTrigger.getPointOfInterest() != null && oneTrigger.getPointOfInterest().equals(searchPoi))	// != null to exclude those who are referring all locations ("entering any location")
					{
						ruleCandidates.add(oneRule);
						break innerloop; //if the poi is found we don't need to search the other triggers in the same rule
					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByHeadphoneJack(boolean triggerParameter)
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.headsetPlugged)
				{
					if(oneTrigger.getTriggerParameter() == triggerParameter)
					{
						ruleCandidates.add(oneRule);
						break innerloop; //we don't need to search the other triggers in the same rule
					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByProfile(Profile profile)
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();
		
		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Action oneAction : oneRule.getActionSet())
			{
				if(oneAction.getAction() == Action.Action_Enum.changeSoundProfile)
				{
					if(oneAction.getParameter2().equals(profile.getOldName()))	// != null to exclude those who are referring all locations ("entering any location")
					{
						ruleCandidates.add(oneRule);
						break innerloop; //if the profile is found we don't need to search the other triggers in the same rule
					}
				}
			}
		}
		
		return ruleCandidates;
	}
	
	public static boolean isAnyRuleUsing(Trigger.Trigger_Enum triggerType)
	{
		for(Rule rule: ruleCollection)
		{
			if(rule.isRuleActive())
			{
				for(Trigger trigger : rule.getTriggerSet())
				{
					if(trigger.getTriggerType().equals(triggerType))
					{
						Miscellaneous.logEvent("i", "Rule->isAnyRuleUsing()", String.format(Miscellaneous.getAnyContext().getString(R.string.atLeastRuleXisUsingY), rule.getName(), triggerType.getFullName(Miscellaneous.getAnyContext())), 5);
						return true;
					}
				}
			}
		}
		
		return false;
	}	

	public static boolean isAnyRuleUsing(Action.Action_Enum actionType)
	{
		for(Rule rule: ruleCollection)
		{
			if(rule.isRuleActive())
			{
				for(Action action : rule.getActionSet())
				{
					if(action.getAction().equals(actionType))
					{
						Miscellaneous.logEvent("i", "Rule->isAnyRuleUsing()", String.format(Miscellaneous.getAnyContext().getString(R.string.atLeastRuleXisUsingY), rule.getName(), actionType.getFullName(Miscellaneous.getAnyContext())), 5);
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	@Override
	public int compareTo(Rule another)
	{
		return this.getName().compareTo(another.getName());
	}

	public boolean haveEnoughPermissions()
	{
		return ActivityPermissions.havePermissionsForRule(this, Miscellaneous.getAnyContext());
	}
}
