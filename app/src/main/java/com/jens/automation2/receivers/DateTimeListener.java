package com.jens.automation2.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.TimeFrame;
import com.jens.automation2.Trigger;
import com.jens.automation2.Trigger.Trigger_Enum;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DateTimeListener extends BroadcastReceiver implements AutomationListenerInterface
{
	private static AutomationService automationServiceRef;
	private static AlarmManager centralAlarmManagerInstance;
	private static boolean alarmListenerActive=false;
	private static ArrayList<ScheduleElement> alarmCandidates = new ArrayList<>();
	
	private static ArrayList<Integer> requestCodeList = new ArrayList<Integer>();

	public static void startAlarmListener(final AutomationService automationServiceRef)
	{		
		DateTimeListener.startAlarmListenerInternal(automationServiceRef);
	}
	public static void stopAlarmListener(Context context)
	{
		DateTimeListener.stopAlarmListenerInternal();
	}

	public static boolean isAlarmListenerActive()
	{
		return alarmListenerActive;
	}
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Miscellaneous.logEvent("i", "AlarmListener", "Alarm received", 2);
		Date now = new Date();
		String timeString = String.valueOf(now.getHours()) + ":" + String.valueOf(now.getMinutes()) + ":" + String.valueOf(now.getSeconds());
		Time passTime = Time.valueOf(timeString);

		ArrayList<Rule> allRulesWithNowInTimeFrame = Rule.findRuleCandidates(Trigger_Enum.timeFrame);
//		ArrayList<Rule> allRulesWithNowInTimeFrame = Rule.findRuleCandidatesByTime(passTime);
		for(int i=0; i<allRulesWithNowInTimeFrame.size(); i++)
		{
			if(allRulesWithNowInTimeFrame.get(i).getsGreenLight(context))
				allRulesWithNowInTimeFrame.get(i).activate(automationServiceRef, false);
		}
		
		setAlarms();
	}
	
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public static void setAlarms()
	{
		alarmCandidates.clear();

		Calendar calNow = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("E dd.MM.yyyy HH:mm");
		
		clearAlarms();
		
		int i=0;
				 
		ArrayList<Rule> allRulesWithTimeFrames = new ArrayList<Rule>();
		allRulesWithTimeFrames = Rule.findRuleCandidates(Trigger_Enum.timeFrame);
//		allRulesWithTimeFrames = Rule.findRuleCandidatesByTimeFrame();
		/*
		 * Take care of regular executions, no repetitions in between.
		 */
		Miscellaneous.logEvent("i", "DateTimeListener", "Checking rules for single run alarm candidates.", 5);
		for(Rule oneRule : allRulesWithTimeFrames)
		{
			Miscellaneous.logEvent("i", "DateTimeListener","Checking rule " + oneRule.getName() + " for single run alarm candidates.", 5);
			if(oneRule.isRuleActive())
			{
				try
				{
					for(Trigger oneTrigger : oneRule.getTriggerSet())
					{
						Miscellaneous.logEvent("i", "DateTimeListener","Checking trigger " + oneTrigger.toString() + " for single run alarm candidates.", 5);

						if(oneTrigger.getTriggerType().equals(Trigger_Enum.timeFrame))
						{
							TimeFrame tf = new TimeFrame(oneTrigger.getTriggerParameter2());

							Calendar calSet;
							Time setTime;

							if(oneTrigger.getTriggerParameter())
								setTime = tf.getTriggerTimeStart();
							else
								setTime = tf.getTriggerTimeStop();

							calSet = (Calendar) calNow.clone();
							calSet.set(Calendar.HOUR_OF_DAY, setTime.getHours());
							calSet.set(Calendar.MINUTE, setTime.getMinutes());
							calSet.set(Calendar.SECOND, 0);
							calSet.set(Calendar.MILLISECOND, 0);
							// At this point calSet would be a scheduling candidate. It's just the day that might not be right, yet.

							for(int dayOfWeek : tf.getDayList())
							{
								Calendar calSetWorkingCopy = (Calendar) calSet.clone();

								int diff = dayOfWeek - calNow.get(Calendar.DAY_OF_WEEK);
								if(diff == 0) // We're talking about the current weekday, but is the time still in the future?
								{
									if(calSetWorkingCopy.getTime().getHours() < calNow.getTime().getHours())
									{
										calSetWorkingCopy.add(Calendar.DAY_OF_MONTH, 7); //add a week
									}
									else if(calSetWorkingCopy.getTime().getHours() == calNow.getTime().getHours())
									{
										if(calSetWorkingCopy.getTime().getMinutes() <= calNow.getTime().getMinutes())
										{
											calSetWorkingCopy.add(Calendar.DAY_OF_MONTH, 7); //add a week
										}
									}
								}
								else if(diff < 0)
								{
									calSetWorkingCopy.add(Calendar.DAY_OF_WEEK, diff+7);	// it's a past weekday, schedule for next week
								}
								else
								{
									calSetWorkingCopy.add(Calendar.DAY_OF_WEEK, diff);		// it's a future weekday, schedule for that day
								}

								i++;
								i=(int)System.currentTimeMillis();
								sdf.format(calSetWorkingCopy.getTime());
								String.valueOf(i);

								alarmCandidates.add(new ScheduleElement(calSetWorkingCopy, "Rule " + oneRule.getName() + ", trigger " + oneTrigger.toString()));
							}
						}
					}
				}
				catch(Exception e)
				{
					Miscellaneous.logEvent("e", "DateTimeListener","Error checking anything for rule " + oneRule.toString() + " needs to be added to candicates list: " + Log.getStackTraceString(e), 1);
				}
			}
		}

		/*
		 * Only take care of repeated executions.
		 */
		Miscellaneous.logEvent("i", "DateTimeListener","Checking rules for repeated run alarm candidates.", 5);
		for(Rule oneRule : allRulesWithTimeFrames)
		{
			Miscellaneous.logEvent("i", "DateTimeListener","Checking rule " + oneRule.getName() + " for repeated run alarm candidates.", 5);
			if(oneRule.isRuleActive())
			{
				try
				{
					Miscellaneous.logEvent("i", "DateTimeListener","Checking rule " + oneRule.toString() , 5);

					for(Trigger oneTrigger : oneRule.getTriggerSet())
					{
						Miscellaneous.logEvent("i", "DateTimeListener","Checking trigger " + oneTrigger.toString() + " for repeated run alarm candidates.", 5);
						if(oneTrigger.getTriggerType().equals(Trigger_Enum.timeFrame))
						{
							Miscellaneous.logEvent("i", "DateTimeListener","Checking rule trigger " + oneTrigger.toString() , 5);

							/*
							 * Check for next repeated execution:
							 *
							 * Check if the rule currently applies....
							 *
							 * If no -> do nothing
							 * If yes -> Take starting time and calculate the next repeated execution
							 * 	1. Take starting time
							 * 	2. Take current time
							 * 	3. Calculate difference, but include check to see if we're after that time,
							 * 		be it start or end of the timeframe.
							 * 	4. Take div result +1 and add this on top of starting time
							 * 	5. Is this next possible execution still inside timeframe? Also consider timeframes spanning over midnight
							 */
							Calendar calSet;
							Time setTime;
							TimeFrame tf = new TimeFrame(oneTrigger.getTriggerParameter2());

							if(tf.getRepetition() > 0)
							{
								if(oneTrigger.applies(calNow, Miscellaneous.getAnyContext()))
								{
									Calendar calSchedule = getNextRepeatedExecutionAfter(oneTrigger, calNow);

									alarmCandidates.add(new ScheduleElement(calSchedule, "Rule " + oneRule.getName() + ", trigger " + oneTrigger.toString()));
								}
							}
						}
					}
				}
				catch(Exception e)
				{
					Miscellaneous.logEvent("e", "DateTimeListener","Error checking anything for rule " + oneRule.toString() + " needs to be added to candicates list: " + Log.getStackTraceString(e), 1);
				}
			}
		}
		
		scheduleNextAlarm();
	}
	
	private static void scheduleNextAlarm()
	{
		Long currentTime = System.currentTimeMillis();
		ScheduleElement scheduleCandidate = null;
		
		if(alarmCandidates.size() == 0)
		{
			Miscellaneous.logEvent("i", "AlarmManager", "No alarms to be scheduled.", 3);
			return;
		}
		else if(alarmCandidates.size() == 1)
		{
			// only one alarm, schedule that
			scheduleCandidate = alarmCandidates.get(0);
		}
		else if(alarmCandidates.size() > 1)
		{
			scheduleCandidate = alarmCandidates.get(0);
			
			for(ScheduleElement alarmCandidate : alarmCandidates)
			{
				if(Math.abs(currentTime - alarmCandidate.time.getTimeInMillis()) < Math.abs(currentTime - scheduleCandidate.time.getTimeInMillis()))
					scheduleCandidate = alarmCandidate;
			}
		}
		
		Intent alarmIntent = new Intent(automationServiceRef, DateTimeListener.class);
		PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(automationServiceRef, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		centralAlarmManagerInstance.set(AlarmManager.RTC_WAKEUP, scheduleCandidate.time.getTimeInMillis(), alarmPendingIntent);

		SimpleDateFormat sdf = new SimpleDateFormat("E dd.MM.yyyy HH:mm");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(scheduleCandidate.time.getTimeInMillis());
        Miscellaneous.logEvent("i", "AlarmManager", "Chose " + sdf.format(calendar.getTime()) + " as next scheduled alarm.", 4);
	}
	
	public static void clearAlarms()
	{
		Miscellaneous.logEvent("i", "AlarmManager", "Clearing possibly standing alarms.", 4);
		for(int requestCode : requestCodeList)
		{
			Intent alarmIntent = new Intent(automationServiceRef, DateTimeListener.class);
			PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(automationServiceRef, requestCode, alarmIntent, 0);
//			Miscellaneous.logEvent("i", "AlarmManager", "Clearing alarm with request code: " + String.valueOf(requestCode));
			centralAlarmManagerInstance.cancel(alarmPendingIntent);
			centralAlarmManagerInstance.
		}
		requestCodeList.clear();
	}
	
	private static void startAlarmListenerInternal(AutomationService givenAutomationServiceRef)
	{
		if(!alarmListenerActive)
		{
			Miscellaneous.logEvent("i", "AlarmListener", "Starting alarm listener.", 4);
			DateTimeListener.automationServiceRef = givenAutomationServiceRef;
			centralAlarmManagerInstance = (AlarmManager)automationServiceRef.getSystemService(automationServiceRef.ALARM_SERVICE);
//			alarmIntent = new Intent(automationServiceRef, AlarmListener.class);
//			alarmPendingIntent = PendingIntent.getBroadcast(automationServiceRef, 0, alarmIntent, 0);
			alarmListenerActive = true;
			Miscellaneous.logEvent("i", "AlarmListener", "Alarm listener started.", 4);
			DateTimeListener.setAlarms();
			
//			// get a Calendar object with current time
//			 Calendar cal = Calendar.getInstance();
//			 // add 5 minutes to the calendar object
//			 cal.add(Calendar.SECOND, 10);
//			 centralAlarmManagerInstance.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 5000, alarmPendingIntent);
		}
		else
			Miscellaneous.logEvent("i", "AlarmListener", "Request to start AlarmListener. But it's already active.", 5);
	}
	
	private static void stopAlarmListenerInternal()
	{
		if(alarmListenerActive)
		{
			Miscellaneous.logEvent("i", "AlarmListener", "Stopping alarm listener.", 4);
			clearAlarms();
//			centralAlarmManagerInstance.cancel(alarmPendingIntent);
			alarmListenerActive = false;
		}
		else
			Miscellaneous.logEvent("i", "AlarmListener", "Request to stop AlarmListener. But it's not running.", 5);
	}
	public static void reloadAlarms()
	{
		DateTimeListener.setAlarms();
	}
	@Override
	public void startListener(AutomationService automationService)
	{
		DateTimeListener.startAlarmListener(automationService);
	}
	@Override
	public void stopListener(AutomationService automationService)
	{
		DateTimeListener.stopAlarmListener(automationService);
	}

	public static boolean haveAllPermission()
	{
		return true;
	}

	@Override
	public boolean isListenerRunning()
	{
		return isAlarmListenerActive();
	}
	@Override
	public Trigger_Enum[] getMonitoredTrigger()
	{
		return new Trigger_Enum[] { Trigger_Enum.timeFrame };
	}

	static class ScheduleElement implements Comparable<ScheduleElement>
	{
		Calendar time;
		String reason;

		public ScheduleElement(Calendar timestamp, String reason)
		{
			super();
			this.time = timestamp;
			this.reason = reason;
		}

		@Override
		public int compareTo(ScheduleElement o)
		{
			if(time.getTimeInMillis() == o.time.getTimeInMillis())
				return 0;
			if(time.getTimeInMillis() < o.time.getTimeInMillis())
				return -1;
			else
				return 1;
		}

		@Override
		public String toString()
		{
			return Miscellaneous.formatDate(time.getTime()) + ", reason : " + reason;
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
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

//				if(this.applies(null))
//				{
			// If the starting time is a day ahead remove 1 day.
			if(calSet.getTimeInMillis() > now.getTimeInMillis())
				calSet.add(Calendar.DAY_OF_MONTH, -1);

			long differenceInSeconds = Math.abs(now.getTimeInMillis() - calSet.getTimeInMillis()) / 1000;
			long nextExecutionMultiplier = Math.floorDiv(differenceInSeconds, tf.getRepetition()) + 1;
			long nextScheduleTimestamp = (calSet.getTimeInMillis() / 1000) + (nextExecutionMultiplier * tf.getRepetition());
			Calendar calSchedule = Calendar.getInstance();
			calSchedule.setTimeInMillis(nextScheduleTimestamp * 1000);

			/*
			 * Das war mal aktiviert. Allerdings: Die ganze Funktion liefert zurück, wenn die Regel NOCH nicht
			 * zutrifft, aber wir z.B. gleich den zeitlichen Bereich betreten.
			 */
//					if(trigger.checkDateTime(calSchedule.getTime(), false))
//					{
			return calSchedule;
//					}
//				}
		}
		else
			Miscellaneous.logEvent("i", "DateTimeListener", "Trigger " + trigger.toString() + " is not executed repeatedly.", 5);

		return null;
	}
}