package com.jens.automation2.location;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Settings;

public class SensorActivity implements SensorEventListener
{
	 protected static SensorActivity instance;
	 private final SensorManager mSensorManager;
     private final Sensor mAccelerometer;
     public LocationProvider parentLocationProvider;
     public static boolean mInitialized = false;
     public static float lastX, lastY, lastZ, deltaX, deltaY, deltaZ;
 	 protected static Handler accelerometerHandler = null;
 	 protected static boolean accelerometerReceiverActive = false;
 	 protected static boolean accelerometerTimerActive = false;

     public static boolean isAccelerometerReceiverActive()
	{
		return accelerometerReceiverActive;
	}

	public static boolean isAccelerometerTimerActive()
	{
		return accelerometerTimerActive;
	}

	public static SensorActivity getInstance()
	 {
    	 if(instance == null)
    		 instance = new SensorActivity(AutomationService.getInstance().getLocationProvider());
    	 
		 return instance;
	 }

	public SensorActivity(LocationProvider parent)
     {
    	 this.parentLocationProvider = parent;
         mSensorManager = (SensorManager)parentLocationProvider.parentService.getSystemService(parentLocationProvider.parentService.SENSOR_SERVICE);
         mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
     }

     protected void start()
     {
         mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
     }

     protected void stop()
     {
         mSensorManager.unregisterListener(this);
     }

     public void onAccuracyChanged(Sensor sensor, int accuracy)
     {
     }

     public void onSensorChanged(SensorEvent event)
     {
    	 // Device has been moved
    	 
    	 float x = event.values[0];
    	 float y = event.values[1];
    	 float z = event.values[2];
    	 
    	 if(mInitialized)
		 {
    		 deltaX = Math.abs(lastX-x);
    		 deltaY = Math.abs(lastY-y);
    		 deltaZ = Math.abs(lastZ-z);
    		 //Wenn das jetzt einen gewissen Grenzwert übersteigt, müßten wir den CellListener wieder aktivieren
    		 if(deltaX > Settings.accelerometerMovementThreshold | deltaY > Settings.accelerometerMovementThreshold | deltaZ > Settings.accelerometerMovementThreshold)
    		 {
    			 String text = "Device has been moved. " + String.valueOf(deltaX)+" / "+String.valueOf(deltaY)+" / "+String.valueOf(deltaZ);
    			 Miscellaneous.logEvent("i", "Accelerometer", text, 5);
	    		 CellLocationChangedReceiver.resetFollowUpdate();
	    		 CellLocationChangedReceiver.startCellLocationChangedReceiver();
    		 }
		 }
    	 else
    	 {
    		 lastX = x;
    		 lastY = y;
    		 lastZ = z;
    		 mInitialized = true;
    	 }
     }
 	
 	protected static void startAccelerometerReceiver()
 	{
 		if(Settings.useAccelerometerForPositioning && !Miscellaneous.isAndroidEmulator())
 		{
 			if(!accelerometerReceiverActive)
 			{
 				try
 				{
 					getInstance().start();
 					accelerometerReceiverActive = true;
 					Miscellaneous.logEvent("i", "AccelerometerReceiver", "Starting AccelerometerReceiver", 4);
 				}
 				catch(Exception ex)
 				{
 					Miscellaneous.logEvent("e", "AccelerometerReceiver", "Error starting AccelerometerReceiver: " + Log.getStackTraceString(ex), 3);
 				}
 			}
 		}
 	}
 	protected static void stopAccelerometerReceiver()
 	{
 		if(Settings.useAccelerometerForPositioning && !Miscellaneous.isAndroidEmulator() && accelerometerReceiverActive)
 		{
 			try
 			{
 				getInstance().stop();
 				accelerometerReceiverActive = false;
 				Miscellaneous.logEvent("i", "AccelerometerReceiver", "Stopping AccelerometerReceiver", 4);
 			}
 			catch(Exception ex)
 			{
 				Miscellaneous.logEvent("e", "AccelerometerReceiver", "Error stopping AccelerometerReceiver: " + Log.getStackTraceString(ex), 3);
 			}
 		}
 	}
 	
 	public static void startAccelerometerTimer()
 	{
 		if(Settings.useAccelerometerForPositioning && !Miscellaneous.isAndroidEmulator())
 		{
 			if(!accelerometerTimerActive)
 			{
 				Miscellaneous.logEvent("i", "AccelerometerTimer", "Starting AccelerometerTimer", 4);
 				
 				long delayTime = Settings.useAccelerometerAfterIdleTime * 60 * 1000;
 				
 				Message msg = new Message();
 				msg.what = 1;
 				
 				if(accelerometerHandler == null)
 					accelerometerHandler = new AccelerometerHandler();
 				
 				accelerometerHandler.sendMessageDelayed(msg, delayTime);
 				accelerometerTimerActive = true;
 			}
 		}
 		/*
 		 * else
 		 * 		reset timer
 		 */
 	}	
 	public static void stopAccelerometerTimer()
 	{
 		if(accelerometerTimerActive)
 		{
 			Miscellaneous.logEvent("i", "AccelerometerTimer", "Stopping AccelerometerTimer", 4);
 			
// 			Message msg = new Message();
// 			msg.what = 0;
 			
 			if(accelerometerHandler == null)
 				accelerometerHandler = new AccelerometerHandler();
 			else
 				accelerometerHandler.removeMessages(1);
 			
// 			accelerometerHandler.sendMessageDelayed(msg, 0);
 			accelerometerTimerActive = false;
 		}
 	}


 	public static void resetAccelerometerTimer()
 	{
 		if(accelerometerTimerActive)
 		{
 			Miscellaneous.logEvent("i", "AccelerometerTimer", "Resetting AccelerometerTimer", 5);
 			accelerometerHandler.removeMessages(1);
 			
 			long delayTime = Settings.useAccelerometerAfterIdleTime * 60 * 1000;
// 			Toast.makeText(parentService, "Sending message, delayed for " + String.valueOf(delayTime), Toast.LENGTH_LONG).show();
 			
 			Message msg = new Message();
 			msg.what = 1;
 			accelerometerHandler.sendMessageDelayed(msg, delayTime);
 			accelerometerTimerActive = true;
 		}
 	}
 	
 	static class AccelerometerHandler extends Handler
 	{
 		@Override
 		public void handleMessage(Message msg)
 		{
 			super.handleMessage(msg);

 			if(msg.what == 1)
 			{
 				// time is up, no cell location updates since x minutes, start accelerometer
 				String text = String.valueOf(Settings.useAccelerometerAfterIdleTime) + " minutes passed";
 				Miscellaneous.logEvent("i", "AccelerometerHandler", text, 5);
 				CellLocationChangedReceiver.stopCellLocationChangedReceiver();
 				startAccelerometerReceiver();
 			}
 			else if(msg.what == 0)
 			{
 				String text = "Abort command received, deactivating accelerometerReceiver";
 				Miscellaneous.logEvent("i", "AccelerometerHandler", text, 4);
 				stopAccelerometerReceiver();
 			}
 		}
 		
 	}
}
