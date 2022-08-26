package com.jens.automation2.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.jens.automation2.ActivityManageTriggerTethering;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.Trigger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class TetheringReceiver extends android.content.BroadcastReceiver implements AutomationListenerInterface
{
    public static AutomationService automationServiceRef = null;
    private static boolean receiverActive = false;
    private static TetheringReceiver receiverInstance = null;
    private static IntentFilter intentFilter = null;

    private static List<String> lastTetheringTypes = null;
    private static boolean tetheringActive = false;

    public static List<String> getLastTetheringTypes()
    {
        return lastTetheringTypes;
    }

    public static TetheringReceiver getInstance()
    {
        if(receiverInstance == null)
            receiverInstance = new TetheringReceiver();

        return receiverInstance;
    }

    public static boolean isTetheringActive()
    {
        return tetheringActive;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Miscellaneous.logEvent("i", "TetheringReceiver", "Received " + intent.getAction(), 5);

        String searchArray = null;

        if(Build.VERSION.SDK_INT >= 26)
            searchArray = "tetherArray";
        else
            searchArray = "activeArray";

        for(String key : intent.getExtras().keySet())
        {
//            Miscellaneous.logEvent("i", "Broadcast extra", "Broadcast " + intent.getAction() + " has extra " + key + " and type " + intent.getExtras().get(key).getClass().getName(), 4);
            Object ob = intent.getExtras().get(key);

            if(key.equals(searchArray) && ob instanceof ArrayList)
            {
                if(((ArrayList<String>)ob).size() > 0)
                {
                    tetheringActive = true;
                    if(lastTetheringTypes == null)
                        lastTetheringTypes = new ArrayList<>();
                    else
                        lastTetheringTypes.clear();

                    for(String adapterName : (ArrayList<String>)ob)
                    {
                        if(adapterName.contains("wlan"))
                            lastTetheringTypes.add(ActivityManageTriggerTethering.tetheringTypeWifi);
                        else if(adapterName.contains("bluetooth"))
                            lastTetheringTypes.add(ActivityManageTriggerTethering.tetheringTypeBluetooth);
                        else if(adapterName.contains("rndis"))
                            lastTetheringTypes.add(ActivityManageTriggerTethering.tetheringTypeUsb);
                        else if(adapterName.contains("ndis"))
                            lastTetheringTypes.add(ActivityManageTriggerTethering.tetheringTypeCable);
                    }
                }
                else
                    tetheringActive = false;
            }

//            Miscellaneous.logEvent("i", "Broadcast extra", "Broadcast " + intent.getAction() + " has extra " + key + " and type " + intent.getExtras().get(key).getClass().getName(), 4);
        }

        try
        {
            for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for(Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if(!intf.isLoopback())
                    {
                        if(intf.getName().contains("rndis"))
                        {
                            tetheringActive = true;
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            Miscellaneous.logEvent("e", "TetheringReceiver", Log.getStackTraceString(e), 1);
        }

        ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger.Trigger_Enum.tethering);
        for(int i=0; i<ruleCandidates.size(); i++)
        {
            if(ruleCandidates.get(i).getsGreenLight(context))
                ruleCandidates.get(i).activate(automationServiceRef, false);
        }
    }

    @Override
    public void startListener(AutomationService automationService)
    {
        if(!receiverActive)
        {
            TetheringReceiver.automationServiceRef = automationService;

            if(receiverInstance == null)
                receiverInstance = new TetheringReceiver();

            if(intentFilter == null)
            {
                intentFilter = new IntentFilter();
                intentFilter.addAction("android.net.conn.TETHER_STATE_CHANGED");
            }

            try
            {
                automationServiceRef.registerReceiver(receiverInstance, intentFilter);
                receiverActive = true;
            }
            catch(Exception e)
            {
                    /*
                        We might be confronted with permission issues here.
                     */
                Miscellaneous.logEvent("e", "TetheringReceiver", Log.getStackTraceString(e), 1);
                receiverActive = false;
            }
        }
    }

    @Override
    public void stopListener(AutomationService automationService)
    {
        if(receiverActive)
        {
            if(receiverInstance != null)
            {
                automationServiceRef.unregisterReceiver(receiverInstance);
                receiverInstance = null;
            }

            receiverActive = false;
        }
    }

    @Override
    public boolean isListenerRunning()
    {
        return receiverActive;
    }

    @Override
    public Trigger.Trigger_Enum[] getMonitoredTrigger()
    {
        return new Trigger.Trigger_Enum[] { Trigger.Trigger_Enum.tethering};
    }
}
