package com.jens.automation2;

import android.os.Build;
import android.util.Log;

import com.jens.automation2.location.CellLocationChangedReceiver;
import com.jens.automation2.location.WifiBroadcastReceiver;
import com.jens.automation2.receivers.BroadcastListener;
import com.jens.automation2.receivers.DateTimeListener;
import com.jens.automation2.receivers.AutomationListenerInterface;
import com.jens.automation2.receivers.BatteryReceiver;
import com.jens.automation2.receivers.BluetoothReceiver;
import com.jens.automation2.receivers.ConnectivityReceiver;
import com.jens.automation2.receivers.DeviceOrientationListener;
import com.jens.automation2.receivers.HeadphoneJackListener;
import com.jens.automation2.receivers.MediaPlayerListener;
import com.jens.automation2.receivers.NoiseListener;
import com.jens.automation2.receivers.PhoneStatusListener;
import com.jens.automation2.receivers.ProcessListener;
import com.jens.automation2.receivers.ScreenStateReceiver;
import com.jens.automation2.receivers.SubSystemStateReceiver;
import com.jens.automation2.receivers.TetheringReceiver;
import com.jens.automation2.receivers.TimeZoneListener;

import androidx.annotation.RequiresApi;

import static com.jens.automation2.ActivityManageRule.activityDetectionClassPath;

/**
 * Created by jens on 08.03.2017.
 */

public class ReceiverCoordinator
{
	/*
	 * This class will manage getting the device's location. It will utilize the following methods:
	 * - CellLocationListener
	 * - WifiListener
	 * - Accelerometer
	 */

    public static Class[] allImplementers;

    static void fillImplementers()
    {
        try
        {
            Class adClass = Class.forName("ActivityDetectionReceiver");
            allImplementers = new Class[] {
                    adClass,
                    DateTimeListener.class,
                    BatteryReceiver.class,
                    BluetoothReceiver.class,
                    ConnectivityReceiver.class,
                    DeviceOrientationListener.class,
                    HeadphoneJackListener.class,
                    //NfcReceiver.class,
                    NoiseListener.class,
                    //NotificationListener.class,
                    PhoneStatusListener.class,
                    ProcessListener.class,
                    MediaPlayerListener.class,
                    ScreenStateReceiver.class,
                    TimeZoneListener.class,
                    TetheringReceiver.class
             };
        }
        catch (ClassNotFoundException e)
        {
            allImplementers = new Class[] {
                    DateTimeListener.class,
                    BatteryReceiver.class,
                    BluetoothReceiver.class,
                    BroadcastListener.class,
                    ConnectivityReceiver.class,
                    DeviceOrientationListener.class,
                    HeadphoneJackListener.class,
                    //NfcReceiver.class,
                    NoiseListener.class,
                    PhoneStatusListener.class,
                    ProcessListener.class,
                    ScreenStateReceiver.class,
                    TimeZoneListener.class,
                    TetheringReceiver.class
            };
        }
    }

    private static AutomationListenerInterface[] listeners = null;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void startAllReceivers()
    {
		/*
		 * New procedure:
		 * Save instances of Listeners in ArrayList and run them.
		 */

        fillImplementers();

        try
        {
            if(listeners == null)
            {
                listeners = new AutomationListenerInterface[allImplementers.length];
                int i = 0;
                for(Class<AutomationListenerInterface> c : allImplementers)
                {
                    try
                    {
                        listeners[i] = (AutomationListenerInterface) c.newInstance();

                        // UNCOMMENT THE NEXT LINE WHEN THIS PART OF THE CODE GOES ONLINE
//                        listeners[i].startListener(AutomationService.getInstance());
                    }
                    catch (InstantiationException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IllegalAccessException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        i++;
                    }
                }
            }
            for(AutomationListenerInterface listener : listeners)
            {
                if(listener != null && listener.getMonitoredTrigger() != null)
                {
                    String jobDescription = "";
                    for (Trigger.Trigger_Enum name : listener.getMonitoredTrigger())
                        jobDescription += name + ", ";
                    jobDescription = jobDescription.substring(0, jobDescription.length() - 2);
                    Miscellaneous.logEvent("i", "Listener", "Listener instance: " + listener.getClass().getName() + ", monitoring: " + jobDescription, 5);
                }
            }
        }
        catch(Exception e)
        {
            Miscellaneous.logEvent("w", "Error in new model", Log.getStackTraceString(e), 3);
        }

        // startPhoneStateListener
        PhoneStatusListener.startPhoneStatusListener(AutomationService.getInstance());			// also used to mute anouncements during calls

        // startConnectivityReceiver
        ConnectivityReceiver.startConnectivityReceiver(AutomationService.getInstance());

        // startCellLocationChangedReceiver
        if(!ConnectivityReceiver.isAirplaneMode(AutomationService.getInstance()) && WifiBroadcastReceiver.mayCellLocationReceiverBeActivated() && (Rule.isAnyRuleUsing(Trigger.Trigger_Enum.pointOfInterest) || Rule.isAnyRuleUsing(Trigger.Trigger_Enum.speed)))
        {
            if(!Miscellaneous.googleToBlameForLocation(true))
                CellLocationChangedReceiver.startCellLocationChangedReceiver();
        }

        // startBatteryReceiver
        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.charging) || Rule.isAnyRuleUsing(Trigger.Trigger_Enum.usb_host_connection) || Rule.isAnyRuleUsing(Trigger.Trigger_Enum.batteryLevel))
            BatteryReceiver.startBatteryReceiver(AutomationService.getInstance());

        // startAlarmListener
        DateTimeListener.startAlarmListener(AutomationService.getInstance());
        TimeZoneListener.startTimeZoneListener(AutomationService.getInstance());

        // startNoiseListener
        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.noiseLevel))
            NoiseListener.startNoiseListener(AutomationService.getInstance());

        // startBroadcastListener
        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.broadcastReceived))
            BroadcastListener.getInstance().startListener(AutomationService.getInstance());

        // startProcessListener
        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.process_started_stopped))
            ProcessListener.startProcessListener(AutomationService.getInstance());

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.deviceOrientation))
            DeviceOrientationListener.getInstance().startListener(AutomationService.getInstance());

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.tethering))
            TetheringReceiver.getInstance().startListener(AutomationService.getInstance());

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.subSystemState))
            SubSystemStateReceiver.getInstance().startListener(AutomationService.getInstance());

        try
        {
            Class testClass = Class.forName(ActivityManageRule.activityDetectionClassPath);
            //startActivityDetectionReceiver
            if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.activityDetection))
                Miscellaneous.runMethodReflective(activityDetectionClassPath, "startActivityDetectionReceiver", null);
        }
        catch(ClassNotFoundException e)
        {
            // Nothing to do, just not starting this one.
        }

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.bluetoothConnection))
            BluetoothReceiver.startBluetoothReceiver();

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.headsetPlugged))
            HeadphoneJackListener.getInstance().startListener(AutomationService.getInstance());

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.musicPlaying))
            MediaPlayerListener.getInstance().startListener(AutomationService.getInstance());

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.screenState))
            ScreenStateReceiver.startScreenStateReceiver(AutomationService.getInstance());
    }

    public static void stopAllReceivers()
    {
        try
        {
            PhoneStatusListener.stopPhoneStatusListener(AutomationService.getInstance());
            ConnectivityReceiver.stopConnectivityReceiver();
            WifiBroadcastReceiver.stopWifiReceiver();
            BatteryReceiver.stopBatteryReceiver();
            TimeZoneListener.stopTimeZoneListener();
            DateTimeListener.stopAlarmListener(AutomationService.getInstance());
            NoiseListener.stopNoiseListener();
            BroadcastListener.getInstance().stopListener(AutomationService.getInstance());
            ProcessListener.stopProcessListener(AutomationService.getInstance());
            MediaPlayerListener.getInstance().stopListener(AutomationService.getInstance());
            DeviceOrientationListener.getInstance().stopListener(AutomationService.getInstance());
            TetheringReceiver.getInstance().stopListener(AutomationService.getInstance());
            SubSystemStateReceiver.getInstance().stopListener(AutomationService.getInstance());

            try
            {
                Class testClass = Class.forName(ActivityManageRule.activityDetectionClassPath);
                Miscellaneous.runMethodReflective("ActivityDetectionReceiver", "stopActivityDetectionReceiver", null);
            }
            catch(Exception e)
            {
                // Nothing to do, just not stopping this one.
            }

            BluetoothReceiver.stopBluetoothReceiver();
            HeadphoneJackListener.getInstance().stopListener(AutomationService.getInstance());
            DeviceOrientationListener.getInstance().stopListener(AutomationService.getInstance());
        }
        catch(Exception e)
        {
            Miscellaneous.logEvent("e", "cellReceiver", "Error stopping LocationReceiver: " + Log.getStackTraceString(e), 3);
        }
    }

    public static void applySettingsAndRules()
    {
		/*
		 * This method's purpose is to check settings and rules and determine
		 * if changes in them require monitors to be started or stopped.
		 * It takes care only of those which are more expensive.
		 */

        // TextToSpeech is handled in AutomationService class

        Miscellaneous.logEvent("i", "LocationProvider", AutomationService.getInstance().getResources().getString(R.string.applyingSettingsAndRules), 3);

        // *********** RULE CHANGES ***********

        // timeFrame -> too inexpensive to shutdown

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.charging) || Rule.isAnyRuleUsing(Trigger.Trigger_Enum.usb_host_connection) || Rule.isAnyRuleUsing(Trigger.Trigger_Enum.batteryLevel))
        {
            if(BatteryReceiver.haveAllPermission())
                BatteryReceiver.startBatteryReceiver(AutomationService.getInstance());
        }
        else
            BatteryReceiver.stopBatteryReceiver();

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.noiseLevel))
        {
            Miscellaneous.logEvent("i", "LocationProvider", "Starting NoiseListener because used in a new/changed rule.", 4);
            if(NoiseListener.haveAllPermission())
                NoiseListener.startNoiseListener(AutomationService.getInstance());
        }
        else
        {
            Miscellaneous.logEvent("i", "LocationProvider", "Shutting down NoiseListener because not used in any rule.", 4);
            NoiseListener.stopNoiseListener();
        }

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.broadcastReceived))
        {
            Miscellaneous.logEvent("i", "LocationProvider", "Starting BroadcastReceiver because used in a new/changed rule.", 4);
            BroadcastListener.getInstance().startListener(AutomationService.getInstance());
        }
        else
        {
            Miscellaneous.logEvent("i", "LocationProvider", "Shutting down BroadcastReceiver because not used in any rule.", 4);
            BroadcastListener.getInstance().stopListener(AutomationService.getInstance());
        }

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.process_started_stopped))
        {
            Miscellaneous.logEvent("i", "LocationProvider", "Starting ProcessListener because used in a new/changed rule.", 4);
            if(ProcessListener.haveAllPermission())
                ProcessListener.startProcessListener(AutomationService.getInstance());
        }
        else
        {
            Miscellaneous.logEvent("i", "LocationProvider", "Shutting down ProcessListener because not used in any rule.", 4);
            ProcessListener.stopProcessListener(AutomationService.getInstance());
        }

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.screenState))
        {
            Miscellaneous.logEvent("i", "LocationProvider", "Starting ScreenStateListener because used in a new/changed rule.", 4);
            ScreenStateReceiver.startScreenStateReceiver(AutomationService.getInstance());
        }
        else
        {
            Miscellaneous.logEvent("i", "LocationProvider", "Shutting down ScreenStateListener because not used in any rule.", 4);
            ScreenStateReceiver.stopScreenStateReceiver();
        }

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.musicPlaying))
        {
            Miscellaneous.logEvent("i", "LocationProvider", "Starting MediaPlayerListener because used in a new/changed rule.", 4);
            MediaPlayerListener.getInstance().startListener(AutomationService.getInstance());
        }
        else
        {
            Miscellaneous.logEvent("i", "LocationProvider", "Shutting down MediaPlayerListener because not used in any rule.", 4);
            MediaPlayerListener.getInstance().stopListener(AutomationService.getInstance());
        }

        if(!BuildConfig.FLAVOR.equalsIgnoreCase(AutomationService.flavor_name_fdroid))
        {
            if (Rule.isAnyRuleUsing(Trigger.Trigger_Enum.activityDetection))
            {
                Object runResult = Miscellaneous.runMethodReflective(activityDetectionClassPath, "isActivityDetectionReceiverRunning", null);

                if (runResult instanceof Boolean)
                {
                    boolean isRunning = (Boolean) runResult;
                    if (isRunning)
                    {
                        Miscellaneous.logEvent("i", "LocationProvider", "Restarting ActivityDetectionReceiver because used in a new/changed rule.", 4);
                        boolean haveAllPerms = (Boolean) Miscellaneous.runMethodReflective(activityDetectionClassPath, "haveAllPermission", null);
                        if (haveAllPerms)
                            Miscellaneous.runMethodReflective(activityDetectionClassPath, "restartActivityDetectionReceiver", null);
//                    ActivityDetectionReceiver.restartActivityDetectionReceiver();
                    }
                    else
                    {
                        Miscellaneous.logEvent("i", "LocationProvider", "Starting ActivityDetectionReceiver because used in a new/changed rule.", 4);
                        boolean haveAllPerms = (Boolean) Miscellaneous.runMethodReflective(activityDetectionClassPath, "haveAllPermission", null);
                        if (haveAllPerms)
                            Miscellaneous.runMethodReflective(activityDetectionClassPath, "startActivityDetectionReceiver", null);
//                    ActivityDetectionReceiver.startActivityDetectionReceiver();
                    }
                }
            }
            else
            {
                Object runResult = Miscellaneous.runMethodReflective(activityDetectionClassPath, "isActivityDetectionReceiverRunning", null);
                if (runResult instanceof Boolean)
                {
                    boolean isRunning = (Boolean) runResult;
                    if (isRunning)
                    {
                        Miscellaneous.logEvent("i", "LocationProvider", "Shutting down ActivityDetectionReceiver because not used in any rule.", 4);
                        Miscellaneous.runMethodReflective(activityDetectionClassPath, "stopActivityDetectionReceiver", null);
//                ActivityDetectionReceiver.stopActivityDetectionReceiver();
                    }
                }
            }
        }

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.bluetoothConnection))
        {
            if(!BluetoothReceiver.isBluetoothReceiverActive())
            {
                Miscellaneous.logEvent("i", "LocationProvider", "Starting BluetoothReceiver because used in a new/changed rule.", 4);
                if(BluetoothReceiver.haveAllPermission())
                    BluetoothReceiver.startBluetoothReceiver();
            }
        }
        else
        {
            if(BluetoothReceiver.isBluetoothReceiverActive())
            {
                Miscellaneous.logEvent("i", "LocationProvider", "Shutting down BluetoothReceiver because not used in any rule.", 4);
                BluetoothReceiver.stopBluetoothReceiver();
            }
        }

            if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.headsetPlugged))
        {
            if(!HeadphoneJackListener.isHeadphoneJackListenerActive())
            {
                Miscellaneous.logEvent("i", "HeadphoneJackListener", "Starting HeadphoneJackListener because used in a new/changed rule.", 4);
                if(HeadphoneJackListener.getInstance().haveAllPermission())
                    HeadphoneJackListener.getInstance().startListener(AutomationService.getInstance());
            }
        }
        else
        {
            if(HeadphoneJackListener.isHeadphoneJackListenerActive())
            {
                Miscellaneous.logEvent("i", "HeadphoneJackListener", "Shutting down HeadphoneJackListener because not used in any rule.", 4);
                HeadphoneJackListener.getInstance().stopListener(AutomationService.getInstance());
            }
        }

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.deviceOrientation))
        {
            if(!DeviceOrientationListener.getInstance().isListenerRunning())
            {
                Miscellaneous.logEvent("i", "DevicePositionListener", "Starting DevicePositionListener because used in a new/changed rule.", 4);
//                if(DevicePositionListener.getInstance().haveAllPermission())
                    DeviceOrientationListener.getInstance().startListener(AutomationService.getInstance());
            }
        }
        else
        {
            if(DeviceOrientationListener.getInstance().isListenerRunning())
            {
                Miscellaneous.logEvent("i", "DevicePositionListener", "Shutting down DevicePositionListener because not used in any rule.", 4);
                DeviceOrientationListener.getInstance().stopListener(AutomationService.getInstance());
            }
        }

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.tethering))
        {
            if(!TetheringReceiver.getInstance().isListenerRunning())
            {
                Miscellaneous.logEvent("i", "TetheringReceiver", "Starting TetheringReceiver because used in a new/changed rule.", 4);
//                if(DevicePositionListener.getInstance().haveAllPermission())
                TetheringReceiver.getInstance().startListener(AutomationService.getInstance());
            }
        }
        else
        {
            if(TetheringReceiver.getInstance().isListenerRunning())
            {
                Miscellaneous.logEvent("i", "TetheringReceiver", "Shutting down TetheringReceiver because not used in any rule.", 4);
                TetheringReceiver.getInstance().stopListener(AutomationService.getInstance());
            }
        }

        if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.subSystemState))
        {
            if(!SubSystemStateReceiver.getInstance().isListenerRunning())
            {
                Miscellaneous.logEvent("i", "SubSystemStateReceiver", "Starting SubSystemStateReceiver because used in a new/changed rule.", 4);
//                if(DevicePositionListener.getInstance().haveAllPermission())
                TetheringReceiver.getInstance().startListener(AutomationService.getInstance());
            }
        }
        else
        {
            if(SubSystemStateReceiver.getInstance().isListenerRunning())
            {
                Miscellaneous.logEvent("i", "SubSystemStateReceiver", "Shutting down SubSystemStateReceiver because not used in any rule.", 4);
                SubSystemStateReceiver.getInstance().stopListener(AutomationService.getInstance());
            }
        }

        AutomationService.updateNotification();
    }
}
