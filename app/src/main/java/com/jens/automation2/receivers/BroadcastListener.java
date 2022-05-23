package com.jens.automation2.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.jens.automation2.ActivityPermissions;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.Trigger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BroadcastListener extends android.content.BroadcastReceiver implements AutomationListenerInterface
{
    ArrayList<EventOccurrence> broadcastsCollection = new ArrayList<>();
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

    public static class EventOccurrence
    {
        Calendar time;
        String event;

        public EventOccurrence(Calendar time, String event)
        {
            this.time = time;
            this.event = event;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        broadcastsCollection.add(new EventOccurrence(Calendar.getInstance(), intent.getAction()));

        for(String key : intent.getExtras().keySet())
        {
            Miscellaneous.logEvent("i", "Broadcast extra", "Broadcast " + intent.getAction() + " has extra " + key + " and type " + intent.getExtras().get(key).getClass().getName(), 4);
//            Object ob = intent.getExtras().get(key);
//            Miscellaneous.logEvent("i", "Broadcast extra", "Broadcast " + intent.getAction() + " has extra " + key + " and type " + intent.getExtras().get(key).getClass().getName(), 4);
        }

        ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger.Trigger_Enum.broadcastReceived);
        for(int i=0; i<ruleCandidates.size(); i++)
        {
            if(ruleCandidates.get(i).getsGreenLight(context))
                ruleCandidates.get(i).activate(automationServiceRef, false);
        }
    }

    public ArrayList<EventOccurrence> getBroadcastsCollection()
    {
        return broadcastsCollection;
    }

    public boolean hasBroadcastOccurred(String event)
    {
        for(EventOccurrence eo : broadcastsCollection)
        {
            if(eo.event.equalsIgnoreCase(event))
                return true;
        }

        return false;
    }

    public boolean hasBroadcastOccurredSince(String event, Calendar timeLimit)
    {
        for(EventOccurrence eo : broadcastsCollection)
        {
            if(eo.event.equalsIgnoreCase(event) && (timeLimit == null || eo.time.getTimeInMillis() > timeLimit.getTimeInMillis()))
                return true;
        }

        return false;
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