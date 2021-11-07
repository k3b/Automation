package com.jens.automation2.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
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
//	private static Intent alarmIntent;
//	private static PendingIntent alarmPendingIntent;
	private static boolean alarmListenerActive=false;
	private static ArrayList<Long> alarmCandidates = new ArrayList<Long>();
	
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
		
		ArrayList<Rule> allRulesWithNowInTimeFrame = Rule.findRuleCandidatesByTime(passTime);
		for(int i=0; i<allRulesWithNowInTimeFrame.size(); i++)
		{
			if(allRulesWithNowInTimeFrame.get(i).applies(context))
				allRulesWithNowInTimeFrame.get(i).activate(automationServiceRef, false);
		}
		
		setAlarms();
	}
	
	public static void setAlarms()
	{
		alarmCandidates.clear();
		
		SimpleDateFormat sdf = new SimpleDateFormat("E dd.MM.yyyy HH:mm");
		
		clearAlarms();
		
		int i=0;	

//		// get a Calendar object with current time
//		Calendar cal = Calendar.getInstance();
//		// add 5 minutes to the calendar object
//		cal.add(Calendar.SECOND, 10);
//		String calSetWorkingCopyString2 = null;
//		SimpleDateFormat sdf2 = new SimpleDateFormat("E dd.MM.yyyy HH:mm");
//		if (cal != null)
//		{
//			calSetWorkingCopyString2 = sdf2.format(cal.getTime());
//		}
//		Miscellaneous.logEvent("i", "AlarmManager", "Setting repeating alarm because of hardcoded test: beginning at " + calSetWorkingCopyString2);
//		Intent alarmIntent2 = new Intent(automationServiceRef, AlarmListener.class);
//		PendingIntent alarmPendingIntent2 = PendingIntent.getBroadcast(automationServiceRef, 0, alarmIntent2, 0);
//		centralAlarmManagerInstance.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 5000, alarmPendingIntent2);
//		requestCodeList.add(0);
				 
		ArrayList<Rule> allRulesWithTimeFrames = new ArrayList<Rule>();
		allRulesWithTimeFrames = Rule.findRuleCandidatesByTimeFrame();
		for(Rule oneRule : allRulesWithTimeFrames)
		{
			for(Trigger oneTrigger : oneRule.getTriggerSet())
			{
				if(oneTrigger.getTriggerType() == Trigger_Enum.timeFrame)
				{
					Calendar calNow, calSet;
					Time setTime;
					
					if(oneTrigger.getTriggerParameter())
						setTime = oneTrigger.getTimeFrame().getTriggerTimeStart();
					else
						setTime = oneTrigger.getTimeFrame().getTriggerTimeStop();
					
					calNow = Calendar.getInstance();			
					calSet = (Calendar) calNow.clone();
					calSet.set(Calendar.HOUR_OF_DAY, setTime.getHours());
					calSet.set(Calendar.MINUTE, setTime.getMinutes());
					calSet.set(Calendar.SECOND, 0);
					calSet.set(Calendar.MILLISECOND, 0);
					// At this point calSet would be a scheduling candidate. It's just the day the might not be right, yet.
					
					long milliSecondsInAWeek = 1000 * 60 * 60 * 24 * 7;
					
					for(int dayOfWeek : oneTrigger.getTimeFrame().getDayList())
					{
						Calendar calSetWorkingCopy = (Calendar) calSet.clone();
						
//						calSetWorkingCopy.set(Calendar.HOUR_OF_DAY, setTime.getHours());
//						calSetWorkingCopy.set(Calendar.MINUTE, setTime.getMinutes());
//						calSetWorkingCopy.set(Calendar.SECOND, 0);
//						calSetWorkingCopy.set(Calendar.MILLISECOND, 0);
						
						int diff = dayOfWeek - calNow.get(Calendar.DAY_OF_WEEK);
//						Log.i("AlarmManager", "Today: " + String.valueOf(calNow.get(Calendar.DAY_OF_WEEK)) + " / Sched.Day: " + String.valueOf(dayOfWeek) + " Difference to target day is: " + String.valueOf(diff));
						if(diff == 0) //if we're talking about the current day, is the time still in the future?
						{
							if(calSetWorkingCopy.getTime().getHours() < calNow.getTime().getHours())
							{
//								Log.i("AlarmManager", "calSetWorkingCopy.getTime().getHours(" + String.valueOf(calSetWorkingCopy.getTime().getHours()) + ") < calNow.getTime().getHours(" + String.valueOf(calNow.getTime().getHours()) + ")");
								calSetWorkingCopy.add(Calendar.DAY_OF_MONTH, 7); //add a week
							}
							else if(calSetWorkingCopy.getTime().getHours() == calNow.getTime().getHours())
							{
//								Log.i("AlarmManager", "calSetWorkingCopy.getTime().getHours() == calNow.getTime().getHours()");
								if(calSetWorkingCopy.getTime().getMinutes() <= calNow.getTime().getMinutes())
								{
//									Log.i("AlarmManager", "calSetWorkingCopy.getTime().getMinutes() < calNow.getTime().getMinutes()");
									calSetWorkingCopy.add(Calendar.DAY_OF_MONTH, 7); //add a week
								}
							}
						}
						else if(diff < 0)
						{
//							Miscellaneous.logEvent("i", "AlarmManager", "Adding " + String.valueOf(diff+7) + " on top of " + String.valueOf(calSetWorkingCopy.get(Calendar.DAY_OF_WEEK)));
							calSetWorkingCopy.add(Calendar.DAY_OF_WEEK, diff+7);	// it's a past weekday, schedule for next week
						}
						else
						{
//							Miscellaneous.logEvent("i", "AlarmManager", "Adding " + String.valueOf(diff) + " on top of " + String.valueOf(calSetWorkingCopy.get(Calendar.DAY_OF_WEEK)));
							calSetWorkingCopy.add(Calendar.DAY_OF_WEEK, diff);		// it's a future weekday, schedule for that day
						}
						
						i++;
						i=(int)System.currentTimeMillis();
						String calSetWorkingCopyString = sdf.format(calSetWorkingCopy.getTime()) + " RequestCode: " + String.valueOf(i);
//						Miscellaneous.logEvent("i", "AlarmManager", "Setting repeating alarm because of rule: " + oneRule.getName() + " beginning at " + calSetWorkingCopyString);

						alarmCandidates.add(calSetWorkingCopy.getTimeInMillis());
//						Intent alarmIntent = new Intent(automationServiceRef, AlarmListener.class);
//						alarmIntent.setData(Uri.parse("myalarms://" + i));
//						PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(automationServiceRef, i, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//						centralAlarmManagerInstance.setInexactRepeating(AlarmManager.RTC_WAKEUP, calSetWorkingCopy.getTimeInMillis(), milliSecondsInAWeek, alarmPendingIntent);
//						requestCodeList.add(i);
					}
				}
			}
		}
		
//		// get a Calendar object with current time
//		Calendar cal = Calendar.getInstance();
//		cal.add(Calendar.SECOND, 10);
//		String calSetWorkingCopyString2 = sdf.format(cal.getTime());
//		Miscellaneous.logEvent("i", "AlarmManager", "Setting repeating alarm because of hardcoded test: beginning at " + calSetWorkingCopyString2);
//		Intent alarmIntent2 = new Intent(automationServiceRef, AlarmListener.class);
//		PendingIntent alarmPendingIntent2 = PendingIntent.getBroadcast(automationServiceRef, 0, alarmIntent2, 0);
//		centralAlarmManagerInstance.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 5000, alarmPendingIntent2);
//		requestCodeList.add(0);
		
		scheduleNextAlarm();
	}
	
	private static void scheduleNextAlarm()
	{
		Long currentTime = System.currentTimeMillis();
		Long scheduleCandidate = null;
		
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
			
			for(long alarmCandidate : alarmCandidates)
			{
				if(Math.abs(currentTime - alarmCandidate) < Math.abs(currentTime - scheduleCandidate))
					scheduleCandidate = alarmCandidate;
			}
		}
		
		Intent alarmIntent = new Intent(automationServiceRef, DateTimeListener.class);
		PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(automationServiceRef, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		centralAlarmManagerInstance.set(AlarmManager.RTC_WAKEUP, scheduleCandidate, alarmPendingIntent);
		

		SimpleDateFormat sdf = new SimpleDateFormat("E dd.MM.yyyy HH:mm");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(scheduleCandidate);        
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

}
