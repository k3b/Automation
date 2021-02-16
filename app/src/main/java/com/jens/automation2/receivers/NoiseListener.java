package com.jens.automation2.receivers;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jens.automation2.ActivityPermissions;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.Settings;
import com.jens.automation2.Trigger.Trigger_Enum;

import java.util.ArrayList;

public class NoiseListener implements AutomationListenerInterface
{
	private static AutomationService automationService;
	private static boolean isMeasuringActive = false;
	private static boolean isTimerActive = false;
	private static long noiseLevelDb;
	private static Handler workHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			Miscellaneous.logEvent("i", "Noise level", "Message received stating measurement is complete.", 5);
			// This will take care of results delivered by the actual measuring instance
			noiseLevelDb  = msg.getData().getLong("noiseLevelDb");
			
			 // execute matching rules containing noise
			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesByNoiseLevel();
			for(Rule oneRule : ruleCandidates)
			{
				if(oneRule.applies(automationService))
					oneRule.activate(automationService, false);
			}
		}
	};
	private static NoiseListenerMeasuring listener;

	private static boolean stopRequested = false;
	private static Handler schedulingHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if(msg.arg1 == 1)
			{			
				if(!stopRequested)
				{
					if(listener == null)
						listener = new NoiseListenerMeasuring();
					listener.doMeasuring();
					Miscellaneous.logEvent("i", "Noise level", "Rearming noise level message.", 5);
					Message message = new Message();
					message.arg1 = 1;
					schedulingHandler.sendMessageDelayed(message, Settings.timeBetweenNoiseLevelMeasurements * 1000);
				}
				else
					Miscellaneous.logEvent("i", "Noise level", "Not rearming noise level message, stop requested.", 5);
			}
		}
		
	};
		
	private static class NoiseListenerMeasuring
	{
		Thread measuringThread;
		
		public void doMeasuring()
		{
			measuringThread = new Thread()
			{
				@Override
				public void run()
				{
					if(!isMeasuringActive)
					{			
						isMeasuringActive = true;
						
						Miscellaneous.logEvent("i", "Noise level", "Periodic noise level measurement started.", 5);
						
					// Start recording but don't store data
						MediaRecorder mediaRecorder = new MediaRecorder();
						mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
						mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
						mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
						mediaRecorder.setOutputFile("/dev/null");
			//			Date myDate = new Date();
			//			mediaRecorder.setOutputFile("/sdcard/temp/" + String.valueOf(myDate.getTime()) + ".3gpp");
						try
						{
							mediaRecorder.prepare();
						    mediaRecorder.getMaxAmplitude();
							mediaRecorder.start();
						    mediaRecorder.getMaxAmplitude();
							
							long noiseLevel;
			
							try
							{		    
								sleep(Settings.lengthOfNoiseLevelMeasurements * 1000);
								// Obtain maximum amplitude since last call of getMaxAmplitude()
									noiseLevel = mediaRecorder.getMaxAmplitude();
							}
							catch(Exception e)
							{
								noiseLevel = -1;
								Miscellaneous.logEvent("e", "Noise level", "Error getting sound level: " + e.getMessage(), 2);
							}
							
							double db = 20 * Math.log(noiseLevel / Settings.referenceValueForNoiseLevelMeasurements);
							noiseLevelDb = Math.round(db);
							
							Message answer = new Message();
							Bundle answerBundle = new Bundle();
							answerBundle.putLong("noiseLevelDb", noiseLevelDb);
							answer.setData(answerBundle);
							workHandler.sendMessage(answer);
							
							Miscellaneous.logEvent("i", "Noise level", "Measured noise level: " + String.valueOf(noiseLevel) + " / converted to db: " + String.valueOf(db), 3);
							
							// Don't forget to release
							mediaRecorder.reset();
							mediaRecorder.release();
						}
						catch(Exception e)
						{}
						
						isMeasuringActive = false;
						
						Miscellaneous.logEvent("i", "Noise level", "Periodic noise level measurement stopped.", 5);
					}
				}
			};
		
			measuringThread.start();
		}
		
		public void interrupt()
		{
			measuringThread.interrupt();
		}
	}
	
	public static void startNoiseListener(AutomationService newAutomationService)
	{
		automationService = newAutomationService;
		
		if(!isTimerActive)
		{
			Miscellaneous.logEvent("i", "Noise level", "Starting periodic noise level measurement engine.", 2);
			isTimerActive = true;
			
			Message message = new Message();
			message.arg1 = 1;
			schedulingHandler.sendMessageDelayed(message, Settings.timeBetweenNoiseLevelMeasurements * 1000);
		}
		else
			Miscellaneous.logEvent("i", "Noise level", "Periodic noise level measurement is already running. Won't start it again.", 2);
	}
	public static void stopNoiseListener()
	{
		if(isTimerActive)
		{
			stopRequested = true;
			Miscellaneous.logEvent("i", "Noise level", "Stopping periodic noise level measurement engine.", 2);
			
			if(schedulingHandler.hasMessages(1))
				schedulingHandler.removeMessages(1);
			
			if(listener != null)
				listener.interrupt();
			
			isTimerActive = false;
		}
		else
			Miscellaneous.logEvent("i", "Noise level", "Periodic noise level measurement is not active. Can't stop it.", 2);
	}	
	
	public static long getNoiseLevelDb()
	{
		return noiseLevelDb;
	}
	
	@Override
	public void startListener(AutomationService automationService)
	{
		NoiseListener.startNoiseListener(automationService);
	}
	@Override
	public void stopListener(AutomationService automationService)
	{
		NoiseListener.stopNoiseListener();
	}

	public static boolean haveAllPermission()
	{
		return ActivityPermissions.havePermission("android.permission.RECORD_AUDIO", Miscellaneous.getAnyContext());
	}

	@Override
	public boolean isListenerRunning()
	{
		return NoiseListener.isMeasuringActive | NoiseListener.isTimerActive;
	}
	@Override
	public Trigger_Enum[] getMonitoredTrigger()
	{
		return new Trigger_Enum[] { Trigger_Enum.noiseLevel };
	}
	
}
