package com.jens.automation2.receivers;

import android.annotation.SuppressLint;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.annotation.RequiresApi;

@SuppressLint("OverrideAbstract")
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListener extends NotificationListenerService
{
    static NotificationListener instance;


    public static boolean startNotificationListenerService()
    {
        if(instance == null)
            instance = new NotificationListener();

        instance.c
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.kpbird.nlsexample.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
        registerReceiver(nlservicereciver,filter);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn)
    {
        super.onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap)
    {
        super.onNotificationPosted(sbn, rankingMap);
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
