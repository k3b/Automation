package com.jens.automation2;

import static com.jens.automation2.Trigger.triggerParameter2Split;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.DetectedActivity;
import com.jens.automation2.receivers.ActivityDetectionReceiver;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


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

			try
			{
				XmlFileInterface.readFile();
			}
			catch(Exception e)
			{
				Miscellaneous.logEvent("w", "Read file", Log.getStackTraceString(e), 3);
			}
			
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

	public boolean cloneRule(Context context)
	{
		Rule newRule = new Rule();
		newRule.setName(this.getName() + " - clone");
		newRule.setRuleActive(this.isRuleActive());
		newRule.setRuleToggle(this.isRuleToggle());

		newRule.setTriggerSet(this.getTriggerSet());
		newRule.setActionSet(this.getActionSet());

		return newRule.create(context);
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
				case setBluetoothTethering:
					return true;
				default:
					break;					
			}
		}
		
		return false;
	}

	public boolean hasNotAppliedSinceLastExecution()
	{
		for(Trigger oneTrigger : this.getTriggerSet())
		{
			if (oneTrigger.hasStateNotAppliedSinceLastRuleExecution())
				return true;
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
				if (!oneTrigger.applies(null, context))
					return false;
			}
			
			return true;
		}
		
		Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), String.format(context.getResources().getString(R.string.ruleIsDeactivatedCantApply), this.getName()), 3);
		return false;
	}

	/**
	 * This is actually a function of the class Trigger, but Rule is already distinguished by flavors, Trigger is not.
	 * Hence it is here.
	 * @param oneTrigger
	 * @return
	 */
	boolean checkActivityDetection(Trigger oneTrigger)
	{
		if (ActivityDetectionReceiver.getActivityDetectionLastResult() != null)
		{
			boolean found = false;
			for (DetectedActivity oneDetectedActivity : ActivityDetectionReceiver.getActivityDetectionLastResult().getProbableActivities())
			{
				if (oneDetectedActivity.getType() == oneTrigger.getActivityDetectionType())
					found = true;
			}

			if (!found)
			{
				Miscellaneous.logEvent("i", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), this.getName()), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyActivityNotPresent), ActivityDetectionReceiver.getDescription(oneTrigger.getActivityDetectionType())), 3);
				return false;
			}
			else
			{
				for (DetectedActivity oneDetectedActivity : ActivityDetectionReceiver.getActivityDetectionLastResult().getProbableActivities())
				{
					if (oneDetectedActivity.getType() == oneTrigger.getActivityDetectionType() && oneDetectedActivity.getConfidence() < Settings.activityDetectionRequiredProbability)
					{
						Miscellaneous.logEvent("i", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), this.getName()), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyActivityGivenButTooLowProbability), ActivityDetectionReceiver.getDescription(oneDetectedActivity.getType()), String.valueOf(oneDetectedActivity.getConfidence()), String.valueOf(Settings.activityDetectionRequiredProbability)), 3);
						return false;
					}
				}
			}
		}

		return true;
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

			setLastExecution(Calendar.getInstance());
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
//				setLastExecution(Calendar.getInstance());
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

			//if(notLastActive || force || doToggle)
//			if(force || doToggle)
//			{
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
//			}
//			else
//			{
//				Miscellaneous.logEvent("i", "Rule", "Request to activate rule " + Rule.this.getName() + ", but it is the last one that was activated. Won't do it again.", 3);
//				return false;
//			}

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
