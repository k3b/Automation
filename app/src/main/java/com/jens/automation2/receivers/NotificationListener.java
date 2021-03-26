package com.jens.automation2.receivers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.annotation.RequiresApi;

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
        String app = sbn.getPackageName();
        String title = sbn.getNotification().extras.getString(EXTRA_TITLE);
        String text = sbn.getNotification().extras.getString(EXTRA_TEXT);
    }

//    @Override
//    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap)
//    {
//        super.onNotificationPosted(sbn, rankingMap);
//        sbn.getNotification().extras.getString(EXTRA_TITLE);
//        sbn.getNotification().extras.getString(EXTRA_TEXT;
//    }

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