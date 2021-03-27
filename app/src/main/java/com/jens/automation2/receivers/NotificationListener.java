package com.jens.automation2.receivers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.annotation.RequiresApi;

import com.jens.automation2.AutomationService;
import com.jens.automation2.Rule;
import com.jens.automation2.Trigger;

import java.util.ArrayList;

// See here for reference: http://gmariotti.blogspot.com/2013/11/notificationlistenerservice-and-kitkat.html

@SuppressLint("OverrideAbstract")
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListener extends NotificationListenerService
{
    //  the title of the notification,
    public static final String EXTRA_TITLE = "android.title";

    //  the main text payload
    public static final String EXTRA_TEXT = "android.text";

    //  a third line of text, as supplied to
    public static final String EXTRA_SUB_TEXT = "android.subText";

    //  a bitmap to be used instead of the small icon when showing the notification payload
    public static final String EXTRA_LARGE_ICON = "android.largeIcon";

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn)
    {
        super.onNotificationPosted(sbn);

        if(AutomationService.isMyServiceRunning(NotificationListener.this))
        {
            String app = sbn.getPackageName();
            String title = sbn.getNotification().extras.getString(EXTRA_TITLE);
            String text = sbn.getNotification().extras.getString(EXTRA_TEXT);

            checkNotification(true, app, title, text);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn)
    {
        super.onNotificationRemoved(sbn);

        if(AutomationService.isMyServiceRunning(NotificationListener.this))
        {
            String app = sbn.getPackageName();
            String title = sbn.getNotification().extras.getString(EXTRA_TITLE);
            String text = sbn.getNotification().extras.getString(EXTRA_TEXT);

            checkNotification(true, app, title, text);
        }
    }

    void checkNotification(boolean created, String appName, String title, String text)
    {
        ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger.Trigger_Enum.notification);
        for(int i=0; i<ruleCandidates.size(); i++)
        {
            if(ruleCandidates.get(i).applies(NotificationListener.this))
                ruleCandidates.get(i).activate(AutomationService.getInstance(), false);
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

    public static void openNotificationAccessWindow(Context context)
    {
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        context.startActivity(intent);
    }
}