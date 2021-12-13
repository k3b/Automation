package com.jens.automation2.receivers;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.jens.automation2.ActivityPermissions;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.R;
import com.jens.automation2.Rule;
import com.jens.automation2.Settings;
import com.jens.automation2.Trigger.Trigger_Enum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ProcessListener implements AutomationListenerInterface
{
	private static ArrayList<String> runningAppsList1 = new ArrayList<String>();
	private static ArrayList<String> runningAppsList2 = new ArrayList<String>();
	private static int lastWritten = 2;
	private static int runCounter = 0;
	private static AutomationService automationService;
	private static boolean isMonitoringActive = false;
	private static boolean isTimerActive = false;
	private static ArrayList<RunningAppProcessInfo> runningAppProcessInfoList;
	private static ProcessListenerMonitoring listener = null;
	
	public static boolean isProcessListenerActive()
	{
		return isMonitoringActive;
	}

	private static Handler workHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			Miscellaneous.logEvent("i", automationService.getResources().getString(R.string.processMonitoring), automationService.getResources().getString(R.string.messageReceivedStatingProcessMonitoringIsComplete), 5);
			// This will take care of results delivered by the actual monitoring instance

			for(String entry : getRunningApps())
				Miscellaneous.logEvent("i", automationService.getResources().getString(R.string.runningApp), entry, 5);

			// execute matching rules containing processes
			if(getRecentlyStartedApps().size()>0 | getRecentlyStoppedApps().size()>0)
			{
				for(String entry : getRecentlyStartedApps())
					Miscellaneous.logEvent("i", automationService.getResources().getString(R.string.appStarted), entry, 3);
				for(String entry : getRecentlyStoppedApps())
					Miscellaneous.logEvent("i", automationService.getResources().getString(R.string.appStopped), entry, 3);

				ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger_Enum.process_started_stopped);
				for(int i=0; i<ruleCandidates.size(); i++)
				{
					if(ruleCandidates.get(i).getsGreenLight(automationService))
						ruleCandidates.get(i).activate(automationService, false);
				}
			}
		}
	};

	public static ArrayList<String> getRunningApps()
	{
    	if(runningAppsList1.size() == 0 && runningAppsList2.size() == 0)
    		ProcessListenerMonitoring.refreshRunningAppsList();
    	
		ArrayList<String> runningAppsListReference;
		
        if(lastWritten == 1)
        {
        	runningAppsListReference = runningAppsList1;
        }
        else
        {
        	runningAppsListReference = runningAppsList2;
        }
       
		return runningAppsListReference;
	}
	
	public static ArrayList<String> getRecentlyStartedApps()
	{
		ArrayList<String> returnList = new ArrayList<String>();
		
		if(runCounter == 0) // Nothing ever happened.
			return returnList;
		
		if(runCounter == 1)
			// Only one run so far, all running apps are considered to have just started.
			return runningAppsList1;
			
		ArrayList<String> oldOne = null, newOne = null;
		if(lastWritten == 1)
		{
			oldOne = runningAppsList2;
			newOne = runningAppsList1;
		}
		else if(lastWritten == 2)
		{
			oldOne = runningAppsList1;
			newOne = runningAppsList2;
		}
			
		for(String runningApp : newOne)
		{
			if(!oldOne.contains(runningApp))
				//Started
				returnList.add(runningApp);
		}
		
		return returnList;
	}
	
	public static ArrayList<String> getRecentlyStoppedApps()
	{
		ArrayList<String> returnList = new ArrayList<String>();
		
		if(runCounter == 1) // Nothing ever happened.
			return returnList;
		
		if(runCounter == 1)
			// Only one run so far, all running apps are considered to have just started, so return empty list.
			return returnList;
			
		ArrayList<String> oldOne = null, newOne = null;
		if(lastWritten == 1)
		{
			oldOne = runningAppsList2;
			newOne = runningAppsList1;
		}
		else if(lastWritten == 2)
		{
			oldOne = runningAppsList1;
			newOne = runningAppsList2;
		}
			
		for(String runningApp : oldOne)
		{
			if(!newOne.contains(runningApp))
				//Stopped
				returnList.add(runningApp);
		}

		return returnList;
	}

	private static boolean stopRequested = false;
	private static Handler schedulingHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
//			try
//			{
				if(msg.arg1 == 1)
				{			
					if(!stopRequested)
					{
						listener = new ProcessListenerMonitoring();
						listener.doMonitoring();
						Miscellaneous.logEvent("i", automationService.getResources().getString(R.string.processMonitoring), automationService.getResources().getString(R.string.rearmingProcessMonitoringMessage), 5);
						Message message = new Message();
						message.arg1 = 1;
						schedulingHandler.sendMessageDelayed(message, Settings.timeBetweenProcessMonitorings * 1000);
					}
					else
						Miscellaneous.logEvent("i", automationService.getResources().getString(R.string.processMonitoring), automationService.getResources().getString(R.string.notRearmingProcessMonitoringMessageStopRequested), 5);
				}
//			}
//			catch(Exception e)
//			{
//				Miscellaneous.logEvent("e", "Noise level", "Error in schedulingHandler->handleMessage(): " + e.getMessage());
//			}
		}
		
	};
		
	private static class ProcessListenerMonitoring
	{
		Thread monitoringThread;
		
		public void doMonitoring()
		{
			monitoringThread = new Thread()
			{
				@Override
				public void run()
				{
					if(!isMonitoringActive)
					{			
						isMonitoringActive = true;
						
						Miscellaneous.logEvent("i", automationService.getResources().getString(R.string.processMonitoring), automationService.getResources().getString(R.string.periodicProcessMonitoringStarted), 5);
						
						refreshRunningAppsList();
		                
						Message answer = new Message();
//						Bundle answerBundle = new Bundle();
//						answer.setData(answerBundle);
						workHandler.sendMessage(answer);
						
						//activate rule(s)
						/*ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesByProcess();
						for(int i=0; i<ruleCandidates.size(); i++)
						{
							if(ruleCandidates.get(i).applies(automationService))
								ruleCandidates.get(i).activate(automationService);
						}*/
						
						isMonitoringActive = false;
						
						Miscellaneous.logEvent("i", automationService.getResources().getString(R.string.processMonitoring), automationService.getResources().getString(R.string.periodicProcessMonitoringStopped), 5);
					}
				}
			};
		
			monitoringThread.start();
		}
		
		public static void refreshRunningAppsList()
		{
			Miscellaneous.logEvent("i", automationService.getResources().getString(R.string.processes), automationService.getResources().getString(R.string.refreshingProcessList), 5);
			
			final ActivityManager activityManager  =  (ActivityManager)automationService.getSystemService(Context.ACTIVITY_SERVICE);
            final List<RunningTaskInfo> services  =  activityManager.getRunningTasks(Integer.MAX_VALUE);
            
            ArrayList<String> runningAppsListReference;
            if(lastWritten == 1)
            {
//            	Log.i("Processes", "Writing var 2");
            	runningAppsListReference = runningAppsList2;
            }
            else
            {
//            	Log.i("Processes", "Writing var 1");
            	runningAppsListReference = runningAppsList1;
            }
            
            runningAppsListReference.clear();
            
            for (int i = 0; i < services.size(); i++)
            {
                if(!runningAppsListReference.contains(services.get(i).baseActivity.getClassName()))
                {
                      // you may broadcast a new application launch here.
                	runningAppsListReference.add(services.get(i).baseActivity.getClassName());
                }
            }

//            for(String runningApp : runningAppsListReference)
//            {		                	
//            	Miscellaneous.logEvent("i", "Running app", runningApp, 5);
//            }
            
//            List<RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
//            for(int i = 0; i < procInfos.size(); i++)
//            {
//                ArrayList<String> runningPkgs = new ArrayList<String>(Arrays.asList(procInfos.get(i).pkgList));
//
//                Collection diff = subtractSets(runningPkgs, stalkList); 
//
//                if(diff != null)
//                {
//                    stalkList.removeAll(diff);
//                }
//           }
            
            // Set marker to the one to be written next.
            if(lastWritten == 1)
            	lastWritten = 2;
            else if(lastWritten == 2)
            	lastWritten = 1;
            else
            	lastWritten = -1;
            
            if(runCounter == 0 | runCounter == 1)
            	runCounter++;
		}
		
		public void interrupt()
		{
			monitoringThread.interrupt();
		}

		private RunningAppProcessInfo getForegroundApp()
		{
		    RunningAppProcessInfo result = null, info = null;

		    final ActivityManager activityManager  =  (ActivityManager)automationService.getSystemService(Context.ACTIVITY_SERVICE);

		    List <RunningAppProcessInfo> l = activityManager.getRunningAppProcesses();
		    Iterator <RunningAppProcessInfo> i = l.iterator();
		    while(i.hasNext())
		    {
		        info = i.next();
		        if(info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
		                && !isRunningService(info.processName))
		        {
		            result = info;
		            break;
		        }
		    }
		    return result;
		}    

		private boolean isRunningService(String processName)
		{
		    if(processName == null)
		        return false;

		    RunningServiceInfo service;

		    final ActivityManager activityManager = (ActivityManager)automationService.getSystemService(Context.ACTIVITY_SERVICE);

		    List <RunningServiceInfo> l = activityManager.getRunningServices(9999);
		    Iterator <RunningServiceInfo> i = l.iterator();
		    while(i.hasNext())
		    {
		        service = i.next();
		        if(service.process.equals(processName))
		            return true;
		    }
		    return false;
		}    

		private boolean isRunningApp(String processName)
		{
		    if(processName == null)
		        return false;

		    RunningAppProcessInfo app;

		    final ActivityManager activityManager = (ActivityManager)automationService.getSystemService(Context.ACTIVITY_SERVICE);

		    List <RunningAppProcessInfo> l = activityManager.getRunningAppProcesses();
		    Iterator <RunningAppProcessInfo> i = l.iterator();
		    while(i.hasNext())
		    {
		        app = i.next();
		        if(app.processName.equals(processName) && app.importance != RunningAppProcessInfo.IMPORTANCE_SERVICE)
		            return true;
		    }
		    return false;
		}

		private boolean checkifThisIsActive(RunningAppProcessInfo target)
		{
		    boolean result = false;
		    RunningTaskInfo info;

		    if(target == null)
		        return false;

		    final ActivityManager activityManager = (ActivityManager)automationService.getSystemService(Context.ACTIVITY_SERVICE);

		    List <RunningTaskInfo> l = activityManager.getRunningTasks(9999);
		    Iterator <RunningTaskInfo> i = l.iterator();

		    while(i.hasNext())
		    {
		        info=i.next();
		        if(info.baseActivity.getPackageName().equals(target.processName))
		        {
		            result = true;
		            break;
		        }
		    }

		    return result;
		}  
		
		// what is in b that is not in a ?
		public static Collection subtractSets(Collection a, Collection b)
		{
		    Collection result = new ArrayList(b);
		    result.removeAll(a);
		    return result;
		}
	}
	
	public static void startProcessListener(AutomationService newAutomationService)
	{
		automationService = newAutomationService;
		
		if(!isTimerActive)
		{
			Miscellaneous.logEvent("i", automationService.getResources().getString(R.string.processMonitoring), automationService.getResources().getString(R.string.startingPeriodicProcessMonitoringEngine), 2);
			isTimerActive = true;
			
			Message message = new Message();
			message.arg1 = 1;
//			schedulingHandler.sendMessageDelayed(message, Settings.timeBetweenNoiseLevelMeasurements * 1000);
			schedulingHandler.sendMessageDelayed(message, 10000);
		}
		else
			Miscellaneous.logEvent("i", automationService.getResources().getString(R.string.processMonitoring), automationService.getResources().getString(R.string.periodicProcessMonitoringIsAlreadyRunning), 2);
	}
	public static void stopProcessListener(AutomationService newAutomationService)
	{
		if(isTimerActive)
		{
			stopRequested = true;
			Miscellaneous.logEvent("i", automationService.getResources().getString(R.string.processMonitoring), automationService.getResources().getString(R.string.stoppingPeriodicProcessMonitoringEngine), 2);
			
			if(schedulingHandler.hasMessages(1))
				schedulingHandler.removeMessages(1);
			
			if(listener != null)
				listener.interrupt();
			
			isTimerActive = false;
		}
		else
		{
			automationService = newAutomationService;
			Miscellaneous.logEvent("i", automationService.getResources().getString(R.string.processMonitoring), automationService.getResources().getString(R.string.periodicProcessMonitoringIsNotActive), 2);
		}
	}	
	
	public static ArrayList<RunningAppProcessInfo> getRunningAppProcessInfo()
	{
		return runningAppProcessInfoList;
	}

	@Override
	public void startListener(AutomationService automationService)
	{
		ProcessListener.startProcessListener(automationService);
	}

	@Override
	public void stopListener(AutomationService automationService)
	{
		ProcessListener.stopProcessListener(automationService);
	}

	public static boolean haveAllPermission()
	{
		return ActivityPermissions.havePermission("android.permission.GET_TASKS", Miscellaneous.getAnyContext());
	}

	@Override
	public boolean isListenerRunning()
	{
		return ProcessListener.isProcessListenerActive();
	}

	@Override
	public Trigger_Enum[] getMonitoredTrigger()
	{
		return new Trigger_Enum[] { Trigger_Enum.process_started_stopped };
	}
	
	
}
