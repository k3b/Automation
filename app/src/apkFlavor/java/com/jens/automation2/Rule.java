package com.jens.automation2;

import static com.jens.automation2.Trigger.triggerParameter2Split;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.DetectedActivity;
import com.jens.automation2.receivers.ActivityDetectionReceiver;
import com.jens.automation2.receivers.BroadcastListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class Rule implements Comparable<Rule>
{
	protected static ArrayList<Rule> ruleCollection = new ArrayList<Rule>();

	protected static List<Rule> ruleRunHistory = new ArrayList<Rule>();
	
	public static List<Rule> getRuleRunHistory()
	{
		return ruleRunHistory;
	}
	
	protected ArrayList<Trigger> triggerSet;
	protected ArrayList<Action> actionSet;
	protected String name;
	protected boolean ruleActive = true;		// rules can be deactivated, so they won't fire if you don't want them temporarily
	protected boolean ruleToggle = false;		// rule will run again and do the opposite of its actions if applicable
	protected Calendar lastExecution;

	protected static Date lastActivatedRuleActivationTime;

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
		{
			for (Rule rule : Rule.ruleCollection)
			{
				if (rule.getName().equals(this.getName()))
				{
					Toast.makeText(context, context.getResources().getString(R.string.anotherRuleByThatName), Toast.LENGTH_LONG).show();
					return false;
				}
			}
		}

		if(this.getTriggerSet().size() == 0)
		{
			Toast.makeText(context, context.getResources().getString(R.string.pleaseSpecifiyTrigger), Toast.LENGTH_LONG).show();
			return false;
		}
		
		if(this.getActionSet().size() == 0)
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
				case setBluetooth:
				case setDataConnection:
				case setDisplayRotation:
				case setUsbTethering:
				case setWifi:
				case setWifiTethering:
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

			/*
				Workaround for repetition in TimeFrame triggers
			 */
			if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.timeFrame))
			{
				if(oneTrigger.getTimeFrame().repetition > 0)
					return true;
			}
			else if(oneTrigger.getTriggerType().equals(Trigger.Trigger_Enum.broadcastReceived))
			{
				return oneTrigger.getTriggerParameter() == BroadcastListener.getInstance().hasBroadcastOccurredSince(oneTrigger.getTriggerParameter2(), getLastExecution());
			}
		}

		return false;
	}

	public boolean getsGreenLight(Context context)
	{
		if(applies(context))
		{
			if(hasNotAppliedSinceLastExecution())
			{
				Miscellaneous.logEvent("i", "getsGreenLight()", "Rule " + getName() + " applies and has flipped since its last execution.", 4);
				return true;
			}
			else
				Miscellaneous.logEvent("i", "getsGreenLight()", "Rule " + getName() + " has not flipped since its last execution.", 4);
		}
		else
			Miscellaneous.logEvent("i", "getsGreenLight()", "Rule " + getName() + " does not apply.", 4);

		return false;
	}
	
	public boolean applies(Context context)
	{
		if(AutomationService.getInstance() == null)
		{
			Miscellaneous.logEvent("i", "RuleCheck", "Automation service not running. Rule " + getName() + " cannot apply.", 3);
			return false;
		}
		
		if(this.ruleActive)
		{
			for(Trigger oneTrigger : this.getTriggerSet())
			{
				if (!oneTrigger.applies(null, context))
					return false;
			}

			Miscellaneous.logEvent("i", String.format(context.getResources().getString(R.string.ruleCheckOf), this.getName()), String.format("Rule %1$s generally applies currently. Checking if it's really due, yet will be done separately.", this.getName()), 3);
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
				Miscellaneous.logEvent("i", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), this.getName()), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyActivityNotPresent), getName(), ActivityDetectionReceiver.getDescription(oneTrigger.getActivityDetectionType())), 3);
				return false;
			}
			else
			{
				for (DetectedActivity oneDetectedActivity : ActivityDetectionReceiver.getActivityDetectionLastResult().getProbableActivities())
				{
					if (oneDetectedActivity.getType() == oneTrigger.getActivityDetectionType() && oneDetectedActivity.getConfidence() < Settings.activityDetectionRequiredProbability)
					{
						Miscellaneous.logEvent("i", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleCheckOf), this.getName()), String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.ruleDoesntApplyActivityGivenButTooLowProbability), getName(), ActivityDetectionReceiver.getDescription(oneDetectedActivity.getType()), String.valueOf(oneDetectedActivity.getConfidence()), String.valueOf(Settings.activityDetectionRequiredProbability)), 3);
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

			// without this line the debugger will - for some reason - skip all breakpoints in this class
			if(android.os.Debug.isDebuggerConnected())
				android.os.Debug.waitForDebugger();
			
	        if (Looper.myLooper() == null)
	        	Looper.prepare();

			setLastExecution(Calendar.getInstance());
			wasActivated = activateInternally((AutomationService)params[0]);

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
		protected boolean activateInternally(AutomationService automationService)
		{
			boolean isActuallyToggleable = isActuallyToggable();

			boolean notLastActive = getLastActivatedRule() == null || !getLastActivatedRule().equals(Rule.this);
			boolean doToggle = ruleToggle && isActuallyToggleable;

			String message;
			if(!doToggle)
				message = String.format(automationService.getResources().getString(R.string.ruleActivate), Rule.this.getName());
			else
				message = String.format(automationService.getResources().getString(R.string.ruleActivateToggle), Rule.this.getName());

			Miscellaneous.logEvent("i", "Rule", message, 2);

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

			return true;
		}
	}
	
	public void activate(AutomationService automationService, boolean force)
	{
		ActivateRuleTask task = new ActivateRuleTask();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, automationService, force);
		else
			task.execute(automationService, force);
	}

	public static ArrayList<Rule> findRuleCandidates(Trigger.Trigger_Enum triggerType)
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();

		for(Rule oneRule : ruleCollection)
		{
			innerLoop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType().equals(triggerType))
				{
					ruleCandidates.add(oneRule);
					break innerLoop; // we don't need to check the other triggers in the same rule
				}
			}
		}

		return ruleCandidates;
	}

	public static ArrayList<Rule> findRuleCandidates(Action.Action_Enum actionType)
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();

		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Action oneAction : oneRule.getActionSet())
			{
				if(oneAction.getAction().equals(actionType))
				{
					ruleCandidates.add(oneRule);
					break innerloop; // we don't need to check the other actions in the same rule
				}
			}
		}

		return ruleCandidates;
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

	public static ArrayList<Rule> findRuleCandidatesByTriggerProfile(Profile profile)
	{
		ArrayList<Rule> ruleCandidates = new ArrayList<Rule>();

		for(Rule oneRule : ruleCollection)
		{
			innerloop:
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger.Trigger_Enum.profileActive)
				{
					String profileName = oneTrigger.getTriggerParameter2().split(triggerParameter2Split)[0];
					if(profileName.equals(profile.getName()))
					{
						ruleCandidates.add(oneRule);
						break innerloop; //if the profile is found we don't need to search the other triggers in the same rule
					}
				}
			}
		}

		return ruleCandidates;
	}
	
	public static ArrayList<Rule> findRuleCandidatesByActionProfile(Profile profile)
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

	public static Rule getByName(String ruleName)
	{
		for(Rule r : Rule.getRuleCollection())
		{
			if(r.getName().equals(ruleName))
				return r;
		}

		return null;
	}
}