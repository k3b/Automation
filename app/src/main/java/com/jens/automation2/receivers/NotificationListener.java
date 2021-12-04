package com.jens.automation2.receivers;

import android.annotation.SuppressLint;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

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
public class NotificationListener extends NotificationListenerService
{
    static Calendar lastResponseToNotification = null;
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
            String app = sbn.getPackageName();
            String title = sbn.getNotification().extras.getString(EXTRA_TITLE);
            String text = sbn.getNotification().extras.getString(EXTRA_TEXT);

            lastNotification = new SimpleNotification();
            lastNotification.publishTime = Miscellaneous.calendarFromLong(sbn.getPostTime());
            lastNotification.created = created;
            lastNotification.app = app;
            lastNotification.title = title;
            lastNotification.text = text;

//            if(lastResponseToNotification == null || lastResponseToNotification.getTimeInMillis() < lastNotification.publishTime.getTimeInMillis())
//            {
//                lastResponseToNotification = Calendar.getInstance();

                ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger.Trigger_Enum.notification);
                for (int i = 0; i < ruleCandidates.size(); i++)
                {
                    if (ruleCandidates.get(i).applies(NotificationListener.this) && ruleCandidates.get(i).hasNotAppliedSinceLastExecution())
                        ruleCandidates.get(i).activate(AutomationService.getInstance(), false);
                }
//            }
//            else
//                Miscellaneous.logEvent("e", "NotificationCheck", "Ignoring notification as it is old.", 5);
        }

        return false;
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
}