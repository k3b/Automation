package com.jens.automation2.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.Trigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SubSystemStateReceiver extends BroadcastReceiver implements AutomationListenerInterface
{
    public static AutomationService automationServiceRef = null;
    private static IntentFilter subSystemStateIntentFilter = null;
    private static BroadcastReceiver subSystemStateReceiverInstance = null;
    private static Intent subSystemStatusIntent = null;
    private static boolean subSystemStateReceiverActive = false;
    static SubSystemStateReceiver instance;

    final static String stateBluetooth = "android.bluetooth.adapter.action.STATE_CHANGED";
    final static String stateWifi = "android.net.wifi.STATE_CHANGE";
    final static String connectivityBroadcast = "android.net.conn.CONNECTIVITY_CHANGE";

    static Map<String, Boolean> stateMap = null;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent == null)
            return;
        if (context == null)
            return;

        Miscellaneous.logEvent("e", "ScreenStateReceiver", "Received: " + intent.getAction(), 3);

        if(stateMap == null)
            stateMap = new HashMap<>();

        try
        {
            /*if (intent.getAction().equals(stateWifi) || intent.getAction().equals(connectivityBroadcast))
            {
                if(intent.hasExtra(WifiManager.EXTRA_WIFI_STATE))
                {
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

                    if (wifiState == WifiManager.WIFI_STATE_ENABLED)
                        stateMap.put("wifi", true);
                    else if (wifiState == WifiManager.WIFI_STATE_DISABLED)
                        stateMap.put("wifi", false);
                }
            }
            else if (intent.getAction().equals(stateBluetooth))
            {
                if(intent.hasExtra(BluetoothAdapter.EXTRA_STATE))
                {
                    int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                    if (bluetoothState == BluetoothAdapter.STATE_ON)
                        stateMap.put("bluetooth", true);
                    else if (bluetoothState == BluetoothAdapter.STATE_OFF)
                        stateMap.put("bluetooth", false);
                }
            }*/
            if (intent.getAction().equals(stateWifi) || intent.getAction().equals(connectivityBroadcast) || intent.getAction().equals(stateBluetooth))
            {
                ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger.Trigger_Enum.subSystemState);
                for (int i = 0; i < ruleCandidates.size(); i++)
                {
                    if (ruleCandidates.get(i).getsGreenLight(context))
                        ruleCandidates.get(i).activate(automationServiceRef, false);
                }
            }
            else
            {
                Miscellaneous.logEvent("e", "SubSystemStateReceiver", "Unknown state received: " + intent.getAction(), 3);
            }
        }
        catch (Exception e)
        {
            Miscellaneous.logEvent("e", "SubSystemStateReceiver", "Error receiving screen state: " + e.getMessage(), 3);
        }
    }

    public static SubSystemStateReceiver getInstance()
    {
        if(instance == null)
            instance = new SubSystemStateReceiver();

        return instance;
    }

    @Override
    public void startListener(AutomationService automationService)
    {
        if (!subSystemStateReceiverActive)
        {
            automationServiceRef = automationService;

            if (subSystemStateReceiverInstance == null)
                subSystemStateReceiverInstance = new SubSystemStateReceiver();

            if (subSystemStateIntentFilter == null)
            {
                subSystemStateIntentFilter = new IntentFilter();
                subSystemStateIntentFilter.addAction(stateWifi);
                subSystemStateIntentFilter.addAction(connectivityBroadcast);
                subSystemStateIntentFilter.addAction(stateBluetooth);
            }

            subSystemStatusIntent = automationServiceRef.registerReceiver(subSystemStateReceiverInstance, subSystemStateIntentFilter);

            subSystemStateReceiverActive = true;
        }
    }

    @Override
    public void stopListener(AutomationService automationService)
    {
        if (subSystemStateReceiverActive)
        {
            if (subSystemStateReceiverInstance != null)
            {
                automationServiceRef.unregisterReceiver(subSystemStateReceiverInstance);
                subSystemStateReceiverInstance = null;
            }

            subSystemStateReceiverActive = false;
        }
    }

    @Override
    public boolean isListenerRunning()
    {
        return subSystemStateReceiverActive;
    }

    @Override
    public Trigger.Trigger_Enum[] getMonitoredTrigger()
    {
        return new Trigger.Trigger_Enum[]{Trigger.Trigger_Enum.subSystemState};
    }
}