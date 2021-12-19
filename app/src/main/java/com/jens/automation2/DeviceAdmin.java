package com.jens.automation2;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceAdmin extends DeviceAdminReceiver
{
    @Override
    public void onEnabled (Context context , Intent intent)
    {
        super.onEnabled(context , intent) ;
        Miscellaneous.logEvent("i", "DeviceAdmin", "Got permission BIND_DEVICE_ADMIN.", 3);
    }

    @Override
    public void onDisabled (Context context , Intent intent)
    {
        super.onDisabled(context , intent) ;
        Miscellaneous.logEvent("i", "DeviceAdmin", "Permission BIND_DEVICE_ADMIN taken.", 3);
    }
}