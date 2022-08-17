package com.jens.automation2.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.Trigger;

import java.util.ArrayList;
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
        for(String key : intent.getExtras().keySet())
        {
//            Miscellaneous.logEvent("i", "Broadcast extra", "Broadcast " + intent.getAction() + " has extra " + key + " and type " + intent.getExtras().get(key).getClass().getName(), 4);
            Object ob = intent.getExtras().get(key);

            String target = null;

            if(Build.VERSION.SDK_INT >= 26)
                target = "tetherArray";
            else
                target = "activeArray";

            if(key.equals(target) && ob instanceof ArrayList)
            {
                if(((ArrayList<String>)ob).size() > 0)
                {
                    tetheringActive = true;

                    for(String adapterName : (ArrayList<String>)ob)
                    {
                        String test = adapterName;
                    }
                }
                else
                    tetheringActive = false;
            }

//            Miscellaneous.logEvent("i", "Broadcast extra", "Broadcast " + intent.getAction() + " has extra " + key + " and type " + intent.getExtras().get(key).getClass().getName(), 4);
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
