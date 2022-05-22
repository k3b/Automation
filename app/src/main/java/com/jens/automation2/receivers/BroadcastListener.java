package com.jens.automation2.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.jens.automation2.ActivityPermissions;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.Trigger;

import java.util.ArrayList;
import java.util.List;

public class BroadcastListener extends android.content.BroadcastReceiver implements AutomationListenerInterface
{
    ArrayList<String> broadcastsCollection = new ArrayList<>();
    public static AutomationService automationServiceRef = null;
    private static boolean broadcastReceiverActive = false;
    private static BroadcastListener broadcastReceiverInstance = null;
    private static IntentFilter broadcastIntentFilter = null;
    private static Intent broadcastStatus = null;

    public static BroadcastListener getInstance()
    {
        if(broadcastReceiverInstance == null)
            broadcastReceiverInstance = new BroadcastListener();

        return broadcastReceiverInstance;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        broadcastsCollection.add(intent.getAction());

        ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger.Trigger_Enum.broadcastReceived);
        for(int i=0; i<ruleCandidates.size(); i++)
        {
            if(ruleCandidates.get(i).getsGreenLight(context))
                ruleCandidates.get(i).activate(automationServiceRef, false);
        }
    }

    public ArrayList<String> getBroadcastsCollection()
    {
        return broadcastsCollection;
    }

    public boolean broadcastsCollectionContains(String event)
    {
        return broadcastsCollection.contains(event);
    }

    @Override
    public void startListener(AutomationService automationService)
    {
        if(!broadcastReceiverActive)
        {
            BroadcastListener.automationServiceRef = automationService;

            if(broadcastReceiverInstance == null)
                broadcastReceiverInstance = new BroadcastListener();

            if(broadcastIntentFilter == null)
            {
                broadcastIntentFilter = new IntentFilter();

                List<String> actionList = new ArrayList<>();
                ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger.Trigger_Enum.broadcastReceived);
                for(int i=0; i<ruleCandidates.size(); i++)
                {
                    for(Trigger t : ruleCandidates.get(i).getTriggerSet())
                    {
                        if(t.getTriggerType().equals(Trigger.Trigger_Enum.broadcastReceived))
                        {
                            ActivityPermissions.addToArrayListUnique(t.getTriggerParameter2(), actionList);
                        }
                    }
                }

                for(String s : actionList)
                    broadcastIntentFilter.addAction(s);
            }

            try
            {
                broadcastStatus = automationServiceRef.registerReceiver(broadcastReceiverInstance, broadcastIntentFilter);
                broadcastReceiverActive = true;
            }
            catch(Exception e)
            {
                /*
                    We might be confronted with permission issues here.
                 */
                Miscellaneous.logEvent("e", "BroadcastListener", Log.getStackTraceString(e), 1);
                broadcastReceiverActive = false;
            }
        }
    }

    @Override
    public void stopListener(AutomationService automationService)
    {
        if(broadcastReceiverActive)
        {
            if(broadcastReceiverInstance != null)
            {
                automationServiceRef.unregisterReceiver(broadcastReceiverInstance);
                broadcastReceiverInstance = null;
            }

            broadcastReceiverActive = false;
        }
    }

    @Override
    public boolean isListenerRunning()
    {
        return broadcastReceiverActive;
    }

    @Override
    public Trigger.Trigger_Enum[] getMonitoredTrigger()
    {
        return new Trigger.Trigger_Enum[] { Trigger.Trigger_Enum.broadcastReceived };
    }
}