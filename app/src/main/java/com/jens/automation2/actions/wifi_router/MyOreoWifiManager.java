package com.jens.automation2.actions.wifi_router;

/*
    Class taken from here:
    https://github.com/aegis1980/WifiHotSpot
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.RequiresApi;

import com.android.dx.stock.ProxyBuilder;
import com.jens.automation2.Miscellaneous;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by jonro on 19/03/2018.
 */

@RequiresApi(api = Build.VERSION_CODES.O)
public class MyOreoWifiManager
{
    private static final String TAG = MyOreoWifiManager.class.getSimpleName();

    private Context mContext;
    private WifiManager mWifiManager;
    private ConnectivityManager mConnectivityManager;

    public MyOreoWifiManager(Context c)
    {
        mContext = c;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(ConnectivityManager.class);
    }

    /**
     * This sets the Wifi SSID and password
     * Call this before {@code startTethering} if app is a system/privileged app
     * Requires: android.permission.TETHER_PRIVILEGED which is only granted to system apps
     */
    public void configureHotspot(String name, String password)
    {
        WifiConfiguration apConfig = new WifiConfiguration();
        apConfig.SSID = name;
        apConfig.preSharedKey = password;
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        try
        {
            Method setConfigMethod = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            boolean status = (boolean) setConfigMethod.invoke(mWifiManager, apConfig);
            Miscellaneous.logEvent("i", "configureHotspot()", "setWifiApConfiguration - success? " + status, 2);
        }
        catch (Exception e)
        {
            Miscellaneous.logEvent("e", "configureHotspot()", "Error in configureHotspot: " + Log.getStackTraceString(e), 2);
        }
    }

    /**
     * Checks where tethering is on.
     * This is determined by the getTetheredIfaces() method,
     * that will return an empty array if not devices are tethered
     *
     * @return true if a tethered device is found, false if not found
     */
    public boolean isTetherActive()
    {
        try
        {
            Method method = mConnectivityManager.getClass().getDeclaredMethod("getTetheredIfaces");
            if (method == null)
            {
                Miscellaneous.logEvent("i", "getTetheredIfaces()", "getTetheredIfaces is null", 2);
            }
            else
            {
                String res[] = (String []) method.invoke(mConnectivityManager, null);
                Miscellaneous.logEvent("i", "isTetherActive()", "getTetheredIfaces invoked", 5);
                Miscellaneous.logEvent("i", "isTetherActive()", Arrays.toString(res), 4);

                if (res.length > 0)
                {
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            Miscellaneous.logEvent("e", "isTetherActive()", "Error in getTetheredIfaces: " + Log.getStackTraceString(e), 2);
        }
        return false;
    }

    /**
     * This enables tethering using the ssid/password defined in Settings App>Hotspot & tethering
     * Does not require app to have system/privileged access
     * Credit: Vishal Sharma - https://stackoverflow.com/a/52219887
     */
    public boolean startTethering(final MyOnStartTetheringCallback callback)
    {
        // On Pie if we try to start tethering while it is already on, it will
        // be disabled. This is needed when startTethering() is called programmatically.
        if (isTetherActive())
        {
            Miscellaneous.logEvent("i", "startTethering()", "Tether already active, returning", 2);
            return false;
        }

        File outputDir = mContext.getCodeCacheDir();
        Object proxy;
        try
        {
            proxy = ProxyBuilder.forClass(OnStartTetheringCallbackClass())
                    .dexCache(outputDir).handler(new InvocationHandler()
                    {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                        {
                            switch (method.getName())
                            {
                                case "onTetheringStarted":
                                    callback.onTetheringStarted();
                                    break;
                                case "onTetheringFailed":
                                    callback.onTetheringFailed();
                                    break;
                                default:
                                    ProxyBuilder.callSuper(proxy, method, args);
                            }
                            return null;
                        }

                    }).build();
        }
        catch (Exception e)
        {
            Miscellaneous.logEvent("e", "startTethering()", "Error in enableTethering ProxyBuilder", 2);
            return false;
        }

        Method method = null;
        try
        {
            method = mConnectivityManager.getClass().getDeclaredMethod("startTethering", int.class, boolean.class, OnStartTetheringCallbackClass(), Handler.class);
            if (method == null)
            {
                Miscellaneous.logEvent("w", "startTethering()", "startTetheringMethod is null", 2);
            }
            else
            {
                method.invoke(mConnectivityManager, ConnectivityManager.TYPE_MOBILE, false, proxy, null);
                Miscellaneous.logEvent("i", "startTethering()", "startTethering invoked", 5);
            }
            return true;
        }
        catch (Exception e)
        {
            Miscellaneous.logEvent("w", "startTethering()", "Error in enableTethering: " + Log.getStackTraceString(e), 2);
        }
        return false;
    }

    public void stopTethering()
    {
        try
        {
            Method method = mConnectivityManager.getClass().getDeclaredMethod("stopTethering", int.class);
            if (method == null)
            {
                Miscellaneous.logEvent("w", "stopTethering", "stopTetheringMethod is null", 2);
            }
            else
            {
                method.invoke(mConnectivityManager, ConnectivityManager.TYPE_MOBILE);
                Miscellaneous.logEvent("i", "stopTethering", "stopTethering invoked", 5);
            }
        }
        catch (Exception e)
        {
            Miscellaneous.logEvent("e", "stopTethering", "stopTethering error: " + Log.getStackTraceString(e), 1);
        }
    }

    private Class OnStartTetheringCallbackClass()
    {
        try
        {
            return Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
        }
        catch (ClassNotFoundException e)
        {
            Miscellaneous.logEvent("e", "OnStartTetheringCallbackClass()", "OnStartTetheringCallbackClass error: " + Log.getStackTraceString(e), 1);
        }
        return null;
    }
}