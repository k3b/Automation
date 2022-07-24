package com.jens.automation2.receivers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.Trigger;

import java.util.ArrayList;
import java.util.Calendar;

// See here for reference: http://gmariotti.blogspot.com/2013/11/notificationlistenerservice-and-kitkat.html

@SuppressLint("OverrideAbstract")
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListener extends NotificationListenerService// implements AutomationListenerInterface
{
    static Calendar lastResponseToNotification = null;
    static boolean listenerRunning = false;
    static NotificationListener instance;
    static SimpleNotification lastNotification = null;

    //  the title of the notification,
    public static final String EXTRA_TITLE = "android.title";

    //  the main text payload
    public static final String EXTRA_TEXT = "android.text";

    //  a third line of text, as supplied to
    public static final String EXTRA_SUB_TEXT = "android.subText";

    //  a bitmap to be used instead of the small icon when showing the notification payload
    public static final String EXTRA_LARGE_ICON = "android.largeIcon";

    protected static IntentFilter notificationReceiverIntentFilter = null;

    public static SimpleNotification getLastNotification()
    {
        return lastNotification;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
    }

    public static NotificationListener getInstance()
    {
        return instance;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn)
    {
        super.onNotificationPosted(sbn);

        if(AutomationService.isMyServiceRunning(NotificationListener.this))
            checkNotification(true, sbn);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn)
    {
        super.onNotificationRemoved(sbn);

        if(AutomationService.isMyServiceRunning(NotificationListener.this))
            checkNotification(false, sbn);
    }

    synchronized boolean checkNotification(boolean created, StatusBarNotification sbn)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
        {
            lastNotification = convertNotificationToSimpleNotification(created, sbn);

            if(created)
                Miscellaneous.logEvent("i", "New notification", lastNotification.toString(), 5);
            else
                Miscellaneous.logEvent("i", "Notification removed", lastNotification.toString(), 5);

            ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger.Trigger_Enum.notification);
            for (int i = 0; i < ruleCandidates.size(); i++)
            {
                if(ruleCandidates.get(i).getsGreenLight(NotificationListener.this))
                    ruleCandidates.get(i).activate(AutomationService.getInstance(), false);
            }
        }

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static SimpleNotification convertNotificationToSimpleNotification(boolean created, StatusBarNotification input)
    {
        String app = input.getPackageName();
        String title = "";
        String text = "";

        Bundle extras = input.getNotification().extras;

        try
        {
            if (extras.containsKey(EXTRA_TITLE))
                title = extras.getString(EXTRA_TITLE).toString();
        }
        catch (NullPointerException e)
        {
//            https://www.b4x.com/android/forum/threads/solved-reading-statusbarnotifications-extras.64416/
            // Some notifications have an empty title, like KDE connect
            if(extras.containsKey(EXTRA_TITLE) && extras.get(EXTRA_TITLE) != null)
                title = extras.get(EXTRA_TITLE).toString();
        }

        try
        {
            if (extras.containsKey(EXTRA_TEXT))
                text = extras.getString(EXTRA_TEXT).toString();
        }
        catch (NullPointerException e)
        {
            // in stacked notifications the "surrounding" element has no text, only a title
            if (extras.containsKey(EXTRA_TEXT) && extras.get(EXTRA_TEXT) != null)
                text = extras.get(EXTRA_TEXT).toString();
        }

        SimpleNotification returnNotification = new SimpleNotification();
        returnNotification.publishTime = Miscellaneous.calendarFromLong(input.getPostTime());
        returnNotification.created = created;
        returnNotification.app = app;
        returnNotification.title = title;
        returnNotification.text = text;

        return returnNotification;
    }

    public static class SimpleNotification
    {
        boolean created;
        Calendar publishTime;
        String app, title, text;

        public Calendar getPublishTime()
        {
            return publishTime;
        }

        public void setPublishTime(Calendar publishTime)
        {
            this.publishTime = publishTime;
        }

        public boolean isCreated()
        {
            return created;
        }

        public void setCreated(boolean created)
        {
            this.created = created;
        }

        public String getApp()
        {
            return app;
        }

        public void setApp(String app)
        {
            this.app = app;
        }

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public String getText()
        {
            return text;
        }

        public void setText(String text)
        {
            this.text = text;
        }

        @Override
        public String toString()
        {
            return "SimpleNotification{" +
                    "created=" + created +
                    ", publishTime=" + publishTime +
                    ", app='" + app + '\'' +
                    ", title='" + title + '\'' +
                    ", text='" + text + '\'' +
                    '}';
        }
    }

    @Override
    public void onListenerConnected()
    {
        super.onListenerConnected();
    }

    @Override
    public void onListenerDisconnected()
    {
        super.onListenerDisconnected();
    }

    public void dismissNotification(StatusBarNotification sbn)
    {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
        else
            cancelNotification(sbn.getKey());

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void clickNotificationButton(StatusBarNotification sbn, String buttonText)
    {
        boolean buttonFound = false;

        if(sbn.getNotification().actions != null)
        {
            for (Notification.Action a : sbn.getNotification().actions)
            {
                if((Miscellaneous.isRegularExpression(buttonText) && a.title.toString().matches(buttonText)) || a.title.toString().equalsIgnoreCase(buttonText))
                {
                    if (!buttonFound)
                        buttonFound = true;

                    try
                    {
                        Miscellaneous.logEvent("w", "clickNotificationButton()", "Pressing button with text \"" + a.title.toString() + "\".", 2);
                        a.actionIntent.send();
                    }
                    catch (PendingIntent.CanceledException e)
                    {
                        Miscellaneous.logEvent("w", "clickNotificationButton()", Log.getStackTraceString(e), 2);
                    }
                }
            }
        }

        if(!buttonFound)
            Miscellaneous.logEvent("w", "clickNotificationButton()", "Button with text \n" + buttonText + "\n could not found.", 2);
    }
}