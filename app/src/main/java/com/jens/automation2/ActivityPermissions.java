package com.jens.automation2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jens.automation2.receivers.NotificationListener;

import org.w3c.dom.DOMImplementationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ActivityPermissions extends Activity
{
    static Map<String, String> mapGeneralPermissions = null;
    static Map<String, String> mapTriggerPermissions = null;
    static Map<String, String> mapActionPermissions = null;

    public static final int notificationIdPermissions = 1001;

    public static final int requestCodeForPermissions = 12042;
    private static final int requestCodeForPermissionsWriteSettings = 12043;
    private static final int requestCodeForPermissionsNotificationPolicy = 12044;
    private static final int requestCodeForPermissionsBackgroundLocation = 12045;
    private static final int requestCodeForPermissionsNotifications = 12046;
    private static final int requestCodeForPermissionsDeviceAdmin = 12047;
    private static final int requestCodeForPermissionsBatteryOptimization = 12048;
    private static final int requestCodeForPermissionNotificationAccessAndroid13 = 12049;
    private static final int requestCodeForPermissionsManageOverlay = 12050;
    protected String[] specificPermissionsToRequest = null;

    public static String intentExtraName = "permissionsToBeRequested";

    Button bCancelPermissions, bRequestPermissions;
    TextView tvPermissionsExplanation, tvPermissionsExplanationSystemSettings, tvPermissionsExplanationLong;
    static ActivityPermissions instance = null;

    public final static String permissionNameWireguard = "com.wireguard.android.permission.CONTROL_TUNNELS";
    public final static String permissionNameGoogleActivityDetection = "com.google.android.gms.permission.ACTIVITY_RECOGNITION";
    public final static String permissionNameSuperuser = "android.permission.ACCESS_SUPERUSER";

    public static ActivityPermissions getInstance()
    {
        if(instance == null)
            instance = new ActivityPermissions();

        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        instance = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            super.onCreate(savedInstanceState);
            Miscellaneous.setDisplayLanguage(this);
            setContentView(R.layout.permissions_activity);

            bCancelPermissions = (Button)findViewById(R.id.bCancelPermissions);
            bRequestPermissions = (Button)findViewById(R.id.bRequestPermissions);
            tvPermissionsExplanation = (TextView)findViewById(R.id.tvPermissionsExplanation);
            tvPermissionsExplanationSystemSettings = (TextView)findViewById(R.id.tvPermissionsExplanationSystemSettings);
            tvPermissionsExplanationLong = (TextView)findViewById(R.id.tvPermissionsExplanationLong);

            bCancelPermissions.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    finish();
                }
            });

            bRequestPermissions.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Request the basic permissions, that are absolutely required.
                    //getRequiredPermissions(true); // request permissions to access sd card access and "receive boot completed"

                    if(specificPermissionsToRequest != null)
                        requestSpecificPermission(specificPermissionsToRequest);
                    else
                    {
                        ArrayList<String> list = new ArrayList<String>();
                        for(String s : getRequiredPermissions(false))
                            list.add(s);
                        requestPermissions(list, false); // request all other permissions, based on the rules setup
                    }
                }
            });

            if(savedInstanceState == null)
            {
                Bundle extras = getIntent().getExtras();
                if(extras != null)
                {
                    specificPermissionsToRequest = extras.getStringArray(ActivityPermissions.intentExtraName);;
                    tvPermissionsExplanationLong.setText(R.string.permissionsExplanationSmall);
                    tvPermissionsExplanation.setText("");
                    fillExplanationText();
                    return;
                }
            }

            // Don't have to request specific permissions, so search for generally remaining ones.
            if(needMorePermissions(ActivityPermissions.this))
            {
                fillExplanationText();
            }
            else
            {
                setHaveAllPermissions();
                finish();
            }
        }
        else
            finish();
    }

    protected void fillExplanationText()
    {
        StringBuilder explanation = new StringBuilder();
        String[] requiredPerms;
        if(specificPermissionsToRequest != null)
            requiredPerms = specificPermissionsToRequest;
        else
            requiredPerms = getRequiredPermissions(false);

        boolean locationPermissionExplained = false;

        for(String s : requiredPerms)
        {
            /*
                Filter location permission and only name it once
             */
            if(s.equals(Manifest.permission.ACCESS_COARSE_LOCATION) | s.equals(Manifest.permission.ACCESS_FINE_LOCATION))
            {
                if(!locationPermissionExplained)
                {
                    explanation.append(

                            "<br />" +
                                    "<u>" +
                                    getResources().getString(R.string.readLocation)
                                    + "</u>"

                                    + "<br />"
                    );

                    for (String reason : getReasonForPermission(s))
                        explanation.append(reason + "<br />");

                    locationPermissionExplained = true;
                }
            }
            else
            {
                explanation.append("<br /><u>");

                try
                {
                    explanation.append(getResources().getString(getResources().getIdentifier(s, "string", getPackageName())));
                }
                catch(Resources.NotFoundException e)
                {
                    Miscellaneous.logEvent("w", "ActivityPermissions", "Could not find translation for " + s, 4);
                    explanation.append(s);
                }

                explanation.append("</u><br />");

                for (String reason : getReasonForPermission(s))
                    explanation.append(reason + "<br />");
            }
        }

        tvPermissionsExplanation.setText(Html.fromHtml(explanation.toString()));

        for(String s : requiredPerms)
        {
            if (s.equalsIgnoreCase(Manifest.permission.WRITE_SETTINGS))
            {
                if (requiredPerms.length == 1)
                    tvPermissionsExplanationSystemSettings.setText(getResources().getString(R.string.systemSettingsNote1));
                else if (requiredPerms.length > 1)
                    tvPermissionsExplanationSystemSettings.setText(getResources().getString(R.string.systemSettingsNote1) + getResources().getString(R.string.systemSettingsNote2));

                break;
            }
        }

        ActivityMainScreen.updateMainScreen();

        try
        {
            ActivityMainRules.getInstance().updateListView();
        }
        catch (IllegalStateException e)
        {
            // Activity may not have been loaded, yet.
        }
    }

    public static void addToArrayListUnique(String value, List<String> list)
    {
        if (!list.contains(value))
            list.add(value);
    }

    public static boolean needMorePermissions(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            for (String s : getRequiredPermissions(false))
            {
                if(
                        s.equalsIgnoreCase(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                ||
                        s.equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION)
                                ||
                        s.equalsIgnoreCase(Manifest.permission.ACCESS_COARSE_LOCATION)
                )
                {
                    if (!Miscellaneous.googleToBlameForLocation(true))
                        if (!havePermission(s, context))
                            return true;
                }
                else if((s.equalsIgnoreCase(Manifest.permission.CALL_PHONE) || s.equalsIgnoreCase(Manifest.permission.ANSWER_PHONE_CALLS)) && BuildConfig.FLAVOR.equalsIgnoreCase(AutomationService.flavor_name_googleplay))
                {
                    return false;
                }
                else if(s.equalsIgnoreCase(Manifest.permission.ACTIVITY_RECOGNITION) || s.equalsIgnoreCase(permissionNameGoogleActivityDetection))
                {
                    if(!BuildConfig.FLAVOR.equalsIgnoreCase(AutomationService.flavor_name_fdroid))
                        if (!havePermission(s, context))
                            return true;
                }
                else if (!havePermission(s, context))
                    return true;
            }
        }

        return false;
    }

    public static boolean havePermission(String s, Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(s.equals(Manifest.permission.WRITE_SETTINGS))
                return android.provider.Settings.System.canWrite(context);
            else if (s.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY))
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    return mNotificationManager.isNotificationPolicyAccessGranted();
                }
                else
                    return true;
            }
            else if (s.equals(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE))
            {
                return verifyNotificationPermission();
            }
            else if (s.equals(Manifest.permission.BIND_DEVICE_ADMIN))
            {
                return haveDeviceAdmin();
            }
            else if (s.equals(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS))
            {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                String packageName = context.getApplicationContext().getPackageName();
                return pm.isIgnoringBatteryOptimizations(packageName);
            }
            else if (s.equals(Manifest.permission.SYSTEM_ALERT_WINDOW))
            {
                return android.provider.Settings.canDrawOverlays(Miscellaneous.getAnyContext());
            }
            else
            {
                int res = context.checkCallingOrSelfPermission(s);
                return (res == PackageManager.PERMISSION_GRANTED);
            }
        }
        else
            return true;
    }

    public static boolean haveDeviceAdmin()
    {
        DevicePolicyManager deviceManger = (DevicePolicyManager)Miscellaneous.getAnyContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName compName = new ComponentName(Miscellaneous.getAnyContext(), DeviceAdmin.class);
        boolean active = deviceManger.isAdminActive(compName);
        return active;
    }

    public static void requestOverlay()
    {
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        ActivityPermissions.getInstance().startActivityForResult(intent, requestCodeForPermissionsManageOverlay);
    }
    public static void requestDeviceAdmin()
    {
        if(!haveDeviceAdmin())
        {
//            DevicePolicyManager deviceManger = (DevicePolicyManager)Miscellaneous.getAnyContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName compName = new ComponentName(ActivityPermissions.getInstance(), DeviceAdmin.class) ;

            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN );
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN , compName );
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION , Miscellaneous.getAnyContext().getResources().getString(R.string.deviceAdminNote));
            ActivityPermissions.getInstance().startActivityForResult(intent, requestCodeForPermissionsDeviceAdmin);
        }
    }

    public static String[] getRequiredPermissions(boolean onlyGeneral)
    {
        ArrayList<String> requiredPermissions = new ArrayList<String>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
//        Toast.makeText(Miscellaneous.getAnyContext(), "Checking permissions.", Toast.LENGTH_LONG).show();

            Context workingContext = Miscellaneous.getAnyContext();

            // We do not need to ask for RECEIVE_BOOT_COMPLETED permission. It is always granted.
        /*
        if(!havePermission("android.permission.RECEIVE_BOOT_COMPLETED", workingContext))
            addToArrayListUnique("android.permission.RECEIVE_BOOT_COMPLETED", requiredPermissions);
        */

            if(!havePermission(Manifest.permission.WRITE_SETTINGS, workingContext))
            {
                for (Profile profile : Profile.getProfileCollection())
                {
                    if (profile.changeIncomingCallsRingtone)
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                }
            }

            if(!havePermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, workingContext))
                addToArrayListUnique(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, requiredPermissions);

            for(Profile p : Profile.getProfileCollection())
            {
                if(p.changeIncomingCallsRingtone || p.changeNotificationRingtone)
                    addToArrayListUnique(Manifest.permission.READ_EXTERNAL_STORAGE, requiredPermissions);
            }

            if (!onlyGeneral)
            {
                for (Rule rule : Rule.getRuleCollection())
                {
                    for (String singlePermission : getPermissionsForRule(rule))
                    {
                        if (!havePermission(singlePermission, workingContext))
                        {
                            if (

                                    singlePermission.equalsIgnoreCase(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                            ||
                                    singlePermission.equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION)
                                            ||
                                    singlePermission.equalsIgnoreCase(Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                            {
                                if (!Miscellaneous.googleToBlameForLocation(true))
                                    addToArrayListUnique(singlePermission, requiredPermissions);
                            }
                            else if (singlePermission.equalsIgnoreCase(Manifest.permission.ACTIVITY_RECOGNITION) || singlePermission.equalsIgnoreCase(permissionNameGoogleActivityDetection))
                            {
                                if (!BuildConfig.FLAVOR.equalsIgnoreCase(AutomationService.flavor_name_fdroid))
                                    addToArrayListUnique(singlePermission, requiredPermissions);
                            }
                            else
                                addToArrayListUnique(singlePermission, requiredPermissions);
                        }
                    }
                }
            }

          /*
            Not all permissions need to be asked for.
           */

          /*if(shouldShowRequestPermissionRationale("android.permission.RECORD_AUDIO"))
              Toast.makeText(ActivityMainScreen.this, "shouldShowRequestPermissionRationale", Toast.LENGTH_LONG).show();
          else
              Toast.makeText(ActivityMainScreen.this, "not shouldShowRequestPermissionRationale", Toast.LENGTH_LONG).show();*/

//			  addToArrayListUnique("Manifest.permission.RECORD_AUDIO", requiredPermissions);
        /*int hasPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
        if (hasPermission == PackageManager.PERMISSION_DENIED)
        {
            Toast.makeText(ActivityMainScreen.this, "Don't have record_audio. Requesting...", Toast.LENGTH_LONG).show();
//				  requestPermissions(new String[]{"Manifest.permission.CAMERA"}, requestCodeForPermissions);
            ActivityCompat.requestPermissions(ActivityMainScreen.this, new String[]{"Manifest.permission.CAMERA"}, requestCodeForPermissions);
        }
        else
            Toast.makeText(ActivityMainScreen.this, "Have record_audio.", Toast.LENGTH_LONG).show();*/

        }

        return requiredPermissions.toArray(new String[requiredPermissions.size()]);
    }

    public static boolean havePermissionsForRule(Rule rule, Context context)
    {
        for(String perm : getPermissionsForRule(rule))
        {
            if(!havePermission(perm, context))
                return false;
        }

        return true;
    }

    protected static ArrayList<String> getPermissionsForRule(Rule rule)
    {
        ArrayList<String> requiredPermissions = new ArrayList<>();

        if (rule.isRuleActive())
        {
            for (Trigger trigger : rule.getTriggerSet())
            {
                switch (trigger.getTriggerType())
                {
                    case activityDetection:
                        addToArrayListUnique(permissionNameGoogleActivityDetection, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACTIVITY_RECOGNITION, requiredPermissions);
                        break;
                    case airplaneMode:
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        break;
                    case batteryLevel:
//                        addToArrayListUnique(Manifest.permission.READ_PHONE_STATE, requiredPermissions);
//                        addToArrayListUnique(Manifest.permission.BATTERY_STATS, requiredPermissions);
                        break;
                    case bluetoothConnection:
                        addToArrayListUnique(Manifest.permission.BLUETOOTH_ADMIN, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.BLUETOOTH, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        break;
                    case charging:
//                        addToArrayListUnique(Manifest.permission.READ_PHONE_STATE, requiredPermissions);
//                        addToArrayListUnique("android.permission.BATTERY_STATS", requiredPermissions);
                        break;
                    case headsetPlugged:
//                        addToArrayListUnique(Manifest.permission.READ_PHONE_STATE, requiredPermissions);
                        break;
                    case nfcTag:
                        addToArrayListUnique(Manifest.permission.NFC, requiredPermissions);
                        break;
                    case noiseLevel:
                        addToArrayListUnique(Manifest.permission.RECORD_AUDIO, requiredPermissions);
                        break;
                    case phoneCall:
                        addToArrayListUnique(Manifest.permission.READ_PHONE_STATE, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.PROCESS_OUTGOING_CALLS, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.READ_CALL_LOG, requiredPermissions);
                        break;
                    case pointOfInterest:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        {
                            addToArrayListUnique(Manifest.permission.ACCESS_BACKGROUND_LOCATION, requiredPermissions);
                            addToArrayListUnique(Manifest.permission.ACCESS_FINE_LOCATION, requiredPermissions);
                            addToArrayListUnique(Manifest.permission.ACCESS_COARSE_LOCATION, requiredPermissions);
                        }
                        else
                        {
                            addToArrayListUnique(Manifest.permission.ACCESS_FINE_LOCATION, requiredPermissions);
                            addToArrayListUnique(Manifest.permission.ACCESS_COARSE_LOCATION, requiredPermissions);
                        }
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.INTERNET, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_WIFI_STATE, requiredPermissions);
                        break;
                    case process_started_stopped:
                        addToArrayListUnique(Manifest.permission.GET_TASKS, requiredPermissions);
                        break;
                    case roaming:
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        break;
                    case speed:
                        addToArrayListUnique(Manifest.permission.ACCESS_FINE_LOCATION, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_COARSE_LOCATION, requiredPermissions);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            addToArrayListUnique(Manifest.permission.ACCESS_BACKGROUND_LOCATION, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.INTERNET, requiredPermissions);
                        break;
                    case timeFrame:
                        break;
                    case usb_host_connection:
                        addToArrayListUnique(Manifest.permission.READ_PHONE_STATE, requiredPermissions);
//                        addToArrayListUnique("android.permission.BATTERY_STATS", requiredPermissions);
                        break;
                    case wifiConnection:
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_WIFI_STATE, requiredPermissions);
                        if(
                                (
                                Miscellaneous.getTargetSDK(Miscellaneous.getAnyContext()) >= 29
                                        &&
                                isPermissionDeclaratedInManifest(Miscellaneous.getAnyContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                )
                                    ||
                                Build.VERSION.SDK_INT >= 33
                        )
                            addToArrayListUnique(Manifest.permission.ACCESS_BACKGROUND_LOCATION, requiredPermissions);
                        break;
                    case notification:
                        addToArrayListUnique(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, requiredPermissions);
                        break;
                    default:
                        break;
                }
            }

            for (Action action : rule.getActionSet())
            {
                switch (action.getAction())
                {
                    case changeSoundProfile:
                        addToArrayListUnique(Manifest.permission.MODIFY_AUDIO_SETTINGS, requiredPermissions);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            addToArrayListUnique(Manifest.permission.ACCESS_NOTIFICATION_POLICY, requiredPermissions);
                        Profile targetProfile = Profile.getByName(action.getParameter2());
                        if(targetProfile.changeIncomingCallsRingtone || targetProfile.changeNotificationRingtone)
                            addToArrayListUnique(Manifest.permission.READ_EXTERNAL_STORAGE, requiredPermissions);
                        break;
                    case disableScreenRotation:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        break;
                    case enableScreenRotation:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        break;
                    case playMusic:
                        break;
                    case controlMediaPlayback:
//                        addToArrayListUnique(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, requiredPermissions);
//                        addToArrayListUnique(Manifest.permission.MEDIA_CONTENT_CONTROL, requiredPermissions);
                        break;
                    case sendTextMessage:
                        addToArrayListUnique(Manifest.permission.SEND_SMS, requiredPermissions);
                        getPermissionsForVariablesInUse(action.getParameter2(), requiredPermissions);
                        break;
                    case setAirplaneMode:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
//                        https://stackoverflow.com/questions/32185628/connectivitymanager-requestnetwork-in-android-6-0
//                        addToArrayListUnique(Manifest.permission.CHANGE_NETWORK_STATE, requiredPermissions);
                        /* Permission was not required anymore, even before Android 6: https://su.chainfire.eu/#updates-permission
                        addToArrayListUnique(permissionNameSuperuser, requiredPermissions);*/
                        break;
                    case setBluetooth:
                        addToArrayListUnique(Manifest.permission.BLUETOOTH_ADMIN, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.BLUETOOTH, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        break;
                    case setDataConnection:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
//                        https://stackoverflow.com/questions/32185628/connectivitymanager-requestnetwork-in-android-6-0
//                        addToArrayListUnique(Manifest.permission.CHANGE_NETWORK_STATE, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.READ_PHONE_STATE, requiredPermissions);
                        /* Permission was not required anymore, even before Android 6: https://su.chainfire.eu/#updates-permission
                        addToArrayListUnique(permissionNameSuperuser, requiredPermissions);*/
                        break;
                    case setDisplayRotation:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        break;
                    case setUsbTethering:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
//                        https://stackoverflow.com/questions/32185628/connectivitymanager-requestnetwork-in-android-6-0
//                        addToArrayListUnique(Manifest.permission.CHANGE_NETWORK_STATE, requiredPermissions);
                        break;
                    case setBluetoothTethering:
                        //addToArrayListUnique(Manifest.permission.CHANGE_NETWORK_STATE, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.BLUETOOTH, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.BLUETOOTH_ADMIN, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        break;
                    case setWifi:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
//                        https://stackoverflow.com/questions/32185628/connectivitymanager-requestnetwork-in-android-6-0
//                        addToArrayListUnique(Manifest.permission.CHANGE_NETWORK_STATE, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        break;
                    case setWifiTethering:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
//                        https://stackoverflow.com/questions/32185628/connectivitymanager-requestnetwork-in-android-6-0
//                        addToArrayListUnique(Manifest.permission.CHANGE_NETWORK_STATE, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);

                    /*
                       https://stackoverflow.com/questions/46284914/how-to-enable-android-o-wifi-hotspot-programmatically
                       Unfortunately when requesting this permission it will be rejected automatically.
                     */
//                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                            addToArrayListUnique("android.permission.TETHER_PRIVILEGED", requiredPermissions);
                        break;
                    case speakText:
                        getPermissionsForVariablesInUse(action.getParameter2(), requiredPermissions);
                        break;
                    case startOtherActivity:
                        if(
                                action.getParameter2().contains(Actions.wireguard_tunnel_up)
                                        ||
                                action.getParameter2().contains(Actions.wireguard_tunnel_down)
                                        ||
                                action.getParameter2().contains(Actions.wireguard_tunnel_refresh)
                        )
                            addToArrayListUnique(ActivityPermissions.permissionNameWireguard, requiredPermissions);
//                        if(
//                                action.getParameter2().contains("eu.faircode.netguard.START_PORT_FORWARD")
//                                    ||
//                                action.getParameter2().contains("eu.faircode.netguard.STOP_PORT_FORWARD")
//                        )
//                            addToArrayListUnique("net.kollnig.missioncontrol.permission.ADMIN", requiredPermissions);
                        if(Build.VERSION.SDK_INT >= 29)
                            addToArrayListUnique(Manifest.permission.SYSTEM_ALERT_WINDOW, requiredPermissions);
                        break;
                    case triggerUrl:
                        addToArrayListUnique(Manifest.permission.INTERNET, requiredPermissions);
                        getPermissionsForVariablesInUse(action.getParameter2(), requiredPermissions);
                        break;
                    case turnBluetoothOff:
                        addToArrayListUnique(Manifest.permission.BLUETOOTH_ADMIN, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.BLUETOOTH, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        break;
                    case turnBluetoothOn:
                        addToArrayListUnique(Manifest.permission.BLUETOOTH_ADMIN, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.BLUETOOTH, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        break;
                    case turnUsbTetheringOff:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        break;
                    case turnUsbTetheringOn:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        break;
                    case turnWifiOff:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        break;
                    case turnWifiOn:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        break;
                    case turnWifiTetheringOff:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        break;
                    case turnWifiTetheringOn:
                        addToArrayListUnique(Manifest.permission.WRITE_SETTINGS, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, requiredPermissions);
                        break;
                    case waitBeforeNextAction:
                        break;
                    case playSound:
                        addToArrayListUnique(Manifest.permission.READ_EXTERNAL_STORAGE, requiredPermissions);
                        break;
                    case closeNotification:
                        addToArrayListUnique(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, requiredPermissions);
                        break;
                    case turnScreenOnOrOff:
                        if(action.getParameter1())
                            addToArrayListUnique(Manifest.permission.WAKE_LOCK, requiredPermissions);
                        else
                            addToArrayListUnique(Manifest.permission.BIND_DEVICE_ADMIN, requiredPermissions);
                        break;
                    case startPhoneCall:
                        addToArrayListUnique(Manifest.permission.CALL_PHONE, requiredPermissions);
//                        addToArrayListUnique(Manifest.permission.SYSTEM_ALERT_WINDOW, requiredPermissions);
                        addToArrayListUnique(Manifest.permission.BIND_DEVICE_ADMIN, requiredPermissions);
                        break;
                    case stopPhoneCall:
                        addToArrayListUnique(Manifest.permission.ANSWER_PHONE_CALLS, requiredPermissions);
                        break;
                    default:
                        break;
                }
            }
        }

        return requiredPermissions;
    }

    protected ArrayList<String> getRulesUsing(Trigger.Trigger_Enum triggerType)
    {
        ArrayList<String> returnList = new ArrayList<>();

        for (Rule rule : Rule.getRuleCollection())
        {
            if (rule.isRuleActive())
            {
                for (Trigger trigger : rule.getTriggerSet())
                {
                    if(trigger.getTriggerType().equals(triggerType))
                        addToArrayListUnique(rule.getName(), returnList);
                }
            }
        }

        return returnList;
    }

    protected ArrayList<String> getRulesUsing(Action.Action_Enum actionType)
    {
        ArrayList<String> returnList = new ArrayList<>();

        for (Rule rule : Rule.getRuleCollection())
        {
            if (rule.isRuleActive())
            {
                for (Action action : rule.getActionSet())
                {
                    if(action.getAction().equals(actionType))
                        addToArrayListUnique(rule.getName(), returnList);
                }
            }
        }

        return returnList;
    }

    public ArrayList<String> getReasonForPermission(String permission)
    {
        ArrayList<String> usingElements = new ArrayList<String>();

        switch(permission)
        {
            case Manifest.permission.RECEIVE_BOOT_COMPLETED:
                usingElements.add(getResources().getString(R.string.startAtSystemBoot));
                break;
            case Manifest.permission.ACCESS_NOTIFICATION_POLICY:
                usingElements.add(getResources().getString(R.string.actionChangeSoundProfile));
                break;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                usingElements.add(getResources().getString(R.string.storeSettings));
                break;
            case Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE:
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.notification))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.closeNotification))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case permissionNameGoogleActivityDetection:
            case Manifest.permission.ACTIVITY_RECOGNITION:
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.activityDetection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.ACCESS_COARSE_LOCATION:
            case Manifest.permission.ACCESS_FINE_LOCATION:
                usingElements.add(getResources().getString(R.string.manageLocations));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.pointOfInterest))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.speed))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.ACCESS_BACKGROUND_LOCATION:
                usingElements.add(getResources().getString(R.string.googleLocationChicanery));
                usingElements.add(getResources().getString(R.string.wifiMonitoringAlsoRequiresThis));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.pointOfInterest))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.speed))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.ACCESS_NETWORK_STATE:
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.airplaneMode))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.bluetoothConnection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.pointOfInterest))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.roaming))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.speed))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.wifiConnection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.setAirplaneMode))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.setBluetooth))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.setDataConnection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.setWifi))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.setWifiTethering))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnBluetoothOff))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnBluetoothOn))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnWifiOff))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnWifiOn))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnWifiTetheringOff))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnWifiTetheringOn))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.ACCESS_WIFI_STATE:
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.pointOfInterest))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.wifiConnection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.BLUETOOTH_ADMIN:
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.bluetoothConnection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.setBluetooth))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnBluetoothOff))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnBluetoothOn))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.BLUETOOTH:
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.bluetoothConnection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.setBluetooth))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnBluetoothOff))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnBluetoothOn))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.GET_TASKS:
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.process_started_stopped))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.INTERNET:
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.pointOfInterest))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.speed))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.triggerUrl))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.NFC:
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.nfcTag))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.PROCESS_OUTGOING_CALLS:
            case Manifest.permission.READ_CALL_LOG:
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.phoneCall))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.READ_PHONE_STATE:
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.batteryLevel))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.charging))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.headsetPlugged))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.phoneCall))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.usb_host_connection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.RECORD_AUDIO:
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.noiseLevel))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.SEND_SMS:
                for(String ruleName : getRulesUsing(Action.Action_Enum.sendTextMessage))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.CALL_PHONE:
                for(String ruleName : getRulesUsing(Action.Action_Enum.startPhoneCall))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.SYSTEM_ALERT_WINDOW:
                for(String ruleName : getRulesUsing(Action.Action_Enum.startOtherActivity))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.ANSWER_PHONE_CALLS:
                for(String ruleName : getRulesUsing(Action.Action_Enum.stopPhoneCall))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.FOREGROUND_SERVICE:
                usingElements.add(getResources().getString(R.string.startAutomationAsService));
                break;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                for(String ruleName : getRulesUsing(Action.Action_Enum.playSound))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));

                for(String ruleName : getRulesUsing(Action.Action_Enum.changeSoundProfile))
                {
                    Rule tempRule = Rule.getByName(ruleName);
                    if(tempRule != null)
                    {
                        for (Action a : tempRule.getActionSet())
                        {
                            if (a.getAction().equals(Action.Action_Enum.changeSoundProfile))
                            {
                                Profile p = Profile.getByName(a.getParameter2());
                                if (p.changeIncomingCallsRingtone || p.changeNotificationRingtone)
                                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                            }
                        }
                    }
                }

                for(Profile p : Profile.getProfileCollection())
                {
                    if(p.changeIncomingCallsRingtone || p.changeNotificationRingtone)
                    {
                        usingElements.add(String.format(getResources().getString(R.string.profileXrequiresThis), p.getName()));
                    }
                }

                break;
            case Manifest.permission.BIND_DEVICE_ADMIN:
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnScreenOnOrOff))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.startPhoneCall))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS:
                usingElements.add(getResources().getString(R.string.recommendedForBetterReliability));
                break;
            case Manifest.permission.QUERY_ALL_PACKAGES:
                usingElements.add(getResources().getString(R.string.queryAllPackages));
                break;
        }

        return usingElements;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (requestCode == requestCodeForPermissionsWriteSettings)
                if(android.provider.Settings.System.canWrite(ActivityPermissions.this))
                    requestPermissions(cachedPermissionsToRequest, true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                if (requestCode == requestCodeForPermissionsNotificationPolicy)
                {
                    NotificationManager mNotificationManager = (NotificationManager) ActivityPermissions.this.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (mNotificationManager.isNotificationPolicyAccessGranted())
                        requestPermissions(cachedPermissionsToRequest, true);
                }
            }

            if (requestCode == requestCodeForPermissionsDeviceAdmin)
            {
                NotificationManager mNotificationManager = (NotificationManager) ActivityPermissions.this.getSystemService(Context.NOTIFICATION_SERVICE);

                if (mNotificationManager.isNotificationPolicyAccessGranted())
                    requestPermissions(cachedPermissionsToRequest, true);
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                if (requestCode == requestCodeForPermissionsBackgroundLocation)
                {
                    if (havePermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION, ActivityPermissions.this))
                        requestPermissions(cachedPermissionsToRequest, true);
                }
            }

            if (requestCode == requestCodeForPermissionsNotifications)
                if(havePermission(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, ActivityPermissions.this))
                    requestPermissions(cachedPermissionsToRequest, true);

            if (requestCode == requestCodeForPermissionsBatteryOptimization)
                if(havePermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, ActivityPermissions.this))
                    requestPermissions(cachedPermissionsToRequest, true);

            if (requestCode == requestCodeForPermissionsManageOverlay)
                if(havePermission(Manifest.permission.SYSTEM_ALERT_WINDOW, ActivityPermissions.this))
                    requestPermissions(cachedPermissionsToRequest, true);
        }
    }

    public static void requestSpecificPermission(String... permissionNames)
    {
        ArrayList<String> permissionList = new ArrayList<String>();
        for(String permission : permissionNames)
        {
            if(permissionNames.equals(Manifest.permission.PROCESS_OUTGOING_CALLS))
            {
                if(ActivityPermissions.isPermissionDeclaratedInManifest(Miscellaneous.getAnyContext(), Manifest.permission.PROCESS_OUTGOING_CALLS) && !Miscellaneous.isGooglePlayInstalled(Miscellaneous.getAnyContext()))
                {
                    permissionList.add(permission);
                }
            }
            else if(permissionNames.equals(Manifest.permission.SEND_SMS))
            {
                if(ActivityPermissions.isPermissionDeclaratedInManifest(Miscellaneous.getAnyContext(), Manifest.permission.SEND_SMS) && !Miscellaneous.isGooglePlayInstalled(Miscellaneous.getAnyContext()))
                {
                    permissionList.add(permission);
                }
            }
            else
            {
                if(!havePermission(permission, Miscellaneous.getAnyContext()))
                    permissionList.add(permission);
            }
        }

        getInstance().requestPermissions(permissionList, true);
    }

    ArrayList<String> cachedPermissionsToRequest = null;
    protected void requestPermissions(ArrayList<String> requiredPermissions, boolean continueWithRemainingPermissions)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(!continueWithRemainingPermissions)
            {
                for (String s : requiredPermissions)
                {
                    if (s.equalsIgnoreCase(Manifest.permission.WRITE_SETTINGS))
                    {
                        requiredPermissions.remove(s);
                        cachedPermissionsToRequest = requiredPermissions;
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, requestCodeForPermissionsWriteSettings);
                        return;
                    }
                    else if (s.equalsIgnoreCase(Manifest.permission.BIND_DEVICE_ADMIN))
                    {
                        requiredPermissions.remove(s);
                        cachedPermissionsToRequest = requiredPermissions;
                        requestDeviceAdmin();
                        return;
                    }
                    else if (s.equalsIgnoreCase(Manifest.permission.ACCESS_NOTIFICATION_POLICY))
                    {
                        requiredPermissions.remove(s);
                        cachedPermissionsToRequest = requiredPermissions;
                        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        startActivityForResult(intent, requestCodeForPermissionsNotificationPolicy);
                        return;
                    }
                    else if (s.equalsIgnoreCase(Manifest.permission.SYSTEM_ALERT_WINDOW))
                    {
                        AlertDialog diag = Miscellaneous.messageBox(getResources().getString(R.string.info), getResources().getString(R.string.overlayPermissionHint), ActivityPermissions.this);
                        diag.setOnDismissListener(new DialogInterface.OnDismissListener()
                        {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface)
                            {
                                requiredPermissions.remove(s);
                                cachedPermissionsToRequest = requiredPermissions;
                                requestOverlay();
                            }
                        });
                        diag.show();
                        return;
                    }
                    else if (s.equalsIgnoreCase(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE))
                    {
                        if(Build.VERSION.SDK_INT >= 33)
                        {
                            AlertDialog dialog = Miscellaneous.messageBox(getResources().getString(R.string.info), getResources().getString(R.string.notificationAccessAndroid13), ActivityPermissions.this);
                            dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
                            {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface)
                                {
                                    requiredPermissions.remove(s);
                                    cachedPermissionsToRequest = requiredPermissions;
                                    requestNotificationAccess();
                                }
                            });
                            dialog.show();
                        }
                        else
                        {
                            requiredPermissions.remove(s);
                            cachedPermissionsToRequest = requiredPermissions;
                            requestNotificationAccess();
                        }

                        return;
                    }
                    else if(s.equals(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS))
                    {
                        requiredPermissions.remove(s);
                        cachedPermissionsToRequest = requiredPermissions;
                        String packageName = getApplicationContext().getPackageName();
                        Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + packageName));
                        startActivityForResult(intent, requestCodeForPermissionsBatteryOptimization);
                        return;
                    }
                    else if (s.equalsIgnoreCase(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    {
                        AlertDialog dialog = Miscellaneous.messageBox(getResources().getString(R.string.readLocation), getResources().getString(R.string.pleaseGiveBgLocation), ActivityPermissions.this);
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
                        {
                            @Override
                            public void onDismiss(DialogInterface dialog)
                            {
                                requiredPermissions.remove(s);
                                cachedPermissionsToRequest = requiredPermissions;
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivityForResult(intent, requestCodeForPermissionsBackgroundLocation);
                            }
                        });
                        dialog.show();

                        return;
                    }
                }
            }

            if(requiredPermissions.size() > 0)
            {
                if(requiredPermissions.contains(Manifest.permission.PROCESS_OUTGOING_CALLS))
                {
                    if(!ActivityPermissions.isPermissionDeclaratedInManifest(Miscellaneous.getAnyContext(), Manifest.permission.SEND_SMS)
                            &&
                        Miscellaneous.isGooglePlayInstalled(Miscellaneous.getAnyContext())
                    )
                    {
                        requiredPermissions.remove(Manifest.permission.PROCESS_OUTGOING_CALLS);
                        Miscellaneous.messageBox("Problem", getResources().getString(R.string.googleSarcasm), ActivityPermissions.this).show();
                    }
                }
                if(requiredPermissions.contains(Manifest.permission.SEND_SMS))
                {
                    if(!ActivityPermissions.isPermissionDeclaratedInManifest(Miscellaneous.getAnyContext(), Manifest.permission.SEND_SMS)
                    )
                    {
                        requiredPermissions.remove(Manifest.permission.SEND_SMS);
                        Miscellaneous.messageBox("Problem", getResources().getString(R.string.googleSarcasm), ActivityPermissions.this).show();
                    }
                }


                StringBuilder permissions = new StringBuilder();
                for (String perm : requiredPermissions)
                    permissions.append(perm + "; ");
                if (permissions.length() > 0)
                    permissions.delete(permissions.length() - 2, permissions.length());

                Miscellaneous.logEvent("i", "Permissions", "Requesting permissions: " + permissions, 2);

                if(requiredPermissions.size() > 0)
                    requestPermissions(requiredPermissions.toArray(new String[requiredPermissions.size()]), requestCodeForPermissions);
            }
            else
                setHaveAllPermissions();
        }
    }

    void requestNotificationAccess()
    {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivityForResult(intent, requestCodeForPermissionsNotifications);
    }

    protected void applyChanges()
    {
        AutomationService service = AutomationService.getInstance();
        if(service != null)
            service.applySettingsAndRules();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        Miscellaneous.logEvent("i", "onRequestPermissionsResult()", "onRequestPermissionsResult()", 3);

        ArrayList<String> deniedPermissions = new ArrayList<String>();

        if (requestCode == requestCodeForPermissions)
        {
            for (int i=0; i < grantResults.length; i++)
            {
                if(permissions[i].equalsIgnoreCase(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED)
                {
                    // We just got permission to read the config file. Read again.
                    try
                    {
                        XmlFileInterface.readFile();
                        ActivityMainScreen.updateMainScreen();
                        ActivityMainPoi.getInstance().updateListView();
                        ActivityMainRules.getInstance().updateListView();
                        ActivityMainProfiles.getInstance().updateListView();
                    }
                    catch(Exception e)
                    {
                        Log.e("Error", Log.getStackTraceString(e));
                    }
                }

                if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                {
//					User didn't allow at least 1 permission. What do we do now?
//					Display the corresponding rules and otherwise deactivate them.

                   /* for (String s : getGeneralAffectedFromDenial(permissions[i]))
                        addToArrayListUnique(s, affectedGeneralList);

                    for (String s : getTriggerAffectedFromDenial(permissions[i]))
                        addToArrayListUnique(s, affectedTriggersList);

                    for (String s : getActionAffectedFromDenial(permissions[i]))
                        addToArrayListUnique(s, affectedActionList);*/

                    deniedPermissions.add(permissions[i]);
                }
            }

            /*
             * In theory we now have 3 arrays that hold the features which can't function.
             */
            /*StringBuilder message = new StringBuilder();

            for (String s : affectedGeneralList)
                message.append(Miscellaneous.lineSeparator + s);

            for (String s : affectedTriggersList)
                message.append(Miscellaneous.lineSeparator + s);

            for (String s : affectedActionList)
                message.append(Miscellaneous.lineSeparator + s);*/

            if(deniedPermissions.size() < permissions.length)
            {
                // At least one permission was granted. Apply settings.
                applyChanges();
            }
            if(deniedPermissions.size() > 0)
            {
                /*
                    The user denied certain permissions. We need to live with that and simply disable
                    features while keeping the notification alive. The user may dismiss it anyway.
                 */

                Miscellaneous.logEvent("w", "Denied permissions", getResources().getString(R.string.theFollowingPermissionsHaveBeenDenied) + Miscellaneous.explode(", ", deniedPermissions), 3);
//                this.finish();
            }
            else
            {
                // All permissions have been granted.
                setHaveAllPermissions();
            }
        }
        else
        {
//			I don't remember asking for permissions....
        }
    }

    static ArrayList<String> getPermissionsForVariablesInUse(String text, ArrayList<String> permsList)
    {
        /*
             [uniqueid]
             [serialnr]
             [latitude]
             [longitude]
             [phonenr]
             [d]
             [m]
             [Y]
             [h]
             [H]
             [i]
             [s]
             [ms]
             [notificationTitle]
             [notificationText]
         */

        if(text.contains("[uniqueid]"))
        {

        }
        else if(text.contains("[serialnr]"))
        {

        }
        else if(text.contains("[latitude]") || text.contains("[longitude]"))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                addToArrayListUnique(Manifest.permission.ACCESS_BACKGROUND_LOCATION, permsList);
                addToArrayListUnique(Manifest.permission.ACCESS_FINE_LOCATION, permsList);
                addToArrayListUnique(Manifest.permission.ACCESS_COARSE_LOCATION, permsList);
            }
            else
            {
                addToArrayListUnique(Manifest.permission.ACCESS_FINE_LOCATION, permsList);
                addToArrayListUnique(Manifest.permission.ACCESS_COARSE_LOCATION, permsList);
            }
            addToArrayListUnique(Manifest.permission.ACCESS_NETWORK_STATE, permsList);
            addToArrayListUnique(Manifest.permission.INTERNET, permsList);
            addToArrayListUnique(Manifest.permission.ACCESS_WIFI_STATE, permsList);
        }
        else if(text.contains("[phonenr]"))
        {
            addToArrayListUnique(Manifest.permission.READ_PHONE_STATE, permsList);
            addToArrayListUnique(Manifest.permission.PROCESS_OUTGOING_CALLS, permsList);
        }
        else if(text.contains("[notificationTitle]") || text.contains("[notificationTitle]"))
        {
            addToArrayListUnique(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, permsList);
        }

        return permsList;
    }

    private void setHaveAllPermissions()
    {
        setResult(RESULT_OK);

        try
        {
            ActivityMainRules.getInstance().updateListView();
        }
        catch (IllegalStateException e)
        {
            // Activity may not have been loaded, yet.
        }

        // All permissions have been granted.
        NotificationManager mNotificationManager = (NotificationManager) Miscellaneous.getAnyContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(notificationIdPermissions);
        if(AutomationService.getInstance() != null)
            AutomationService.getInstance().cancelNotification();

        ActivityMainScreen.updateMainScreen();

        this.finish();
    }

    private String[] getGeneralAffectedFromDenial(String permissionName)
    {
        ArrayList<String> returnList = new ArrayList<String>();

        Iterator it = mapGeneralPermissions.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
            if (pair.getValue().equals(permissionName))
                addToArrayListUnique(pair.getKey(), returnList);

            it.remove(); // avoids a ConcurrentModificationException
        }

        return returnList.toArray(new String[returnList.size()]);
    }

    private String[] getTriggerAffectedFromDenial(String permissionName)
    {
        ArrayList<String> returnList = new ArrayList<String>();

        Iterator it = mapTriggerPermissions.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
            if (pair.getValue().equals(permissionName))
                addToArrayListUnique(pair.getKey(), returnList);

            it.remove(); // avoids a ConcurrentModificationException
        }

        return returnList.toArray(new String[returnList.size()]);
    }

    private String[] getActionAffectedFromDenial(String permissionName)
    {
        ArrayList<String> returnList = new ArrayList<String>();

        Iterator it = mapActionPermissions.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
            if (pair.getValue().equals(permissionName))
                addToArrayListUnique(pair.getKey(), returnList);

            it.remove(); // avoids a ConcurrentModificationException
        }

        return returnList.toArray(new String[returnList.size()]);
    }


    protected static void fillPermissionMaps()
    {
        mapGeneralPermissions = new HashMap<String, String>();
        mapGeneralPermissions.put("general", Manifest.permission.RECEIVE_BOOT_COMPLETED);
        mapGeneralPermissions.put("general", Manifest.permission.WRITE_EXTERNAL_STORAGE);

        mapTriggerPermissions = new HashMap<String, String>();
        mapTriggerPermissions.put("activityDetection", permissionNameGoogleActivityDetection);
        mapTriggerPermissions.put("activityDetection", Manifest.permission.ACTIVITY_RECOGNITION);
        mapTriggerPermissions.put("airplaneMode", Manifest.permission.ACCESS_NETWORK_STATE);
        mapTriggerPermissions.put("batteryLevel", Manifest.permission.READ_PHONE_STATE);
        mapTriggerPermissions.put("batteryLevel", Manifest.permission.BATTERY_STATS);
        mapTriggerPermissions.put("bluetoothConnection", Manifest.permission.BLUETOOTH_ADMIN);
        mapTriggerPermissions.put("bluetoothConnection", Manifest.permission.BLUETOOTH);
        mapTriggerPermissions.put("bluetoothConnection", Manifest.permission.ACCESS_NETWORK_STATE);
        mapTriggerPermissions.put("charging", Manifest.permission.READ_PHONE_STATE);
        mapTriggerPermissions.put("charging", Manifest.permission.BATTERY_STATS);
        mapTriggerPermissions.put("headsetPlugged", Manifest.permission.READ_PHONE_STATE);
        mapTriggerPermissions.put("nfcTag", Manifest.permission.NFC);
        mapTriggerPermissions.put("noiseLevel", Manifest.permission.RECORD_AUDIO);
        mapTriggerPermissions.put("phoneCall", Manifest.permission.READ_PHONE_STATE);
        mapTriggerPermissions.put("phoneCall", Manifest.permission.PROCESS_OUTGOING_CALLS);
        mapTriggerPermissions.put("pointOfInterest", Manifest.permission.ACCESS_FINE_LOCATION);
        mapTriggerPermissions.put("pointOfInterest", Manifest.permission.ACCESS_COARSE_LOCATION);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            mapTriggerPermissions.put("pointOfInterest", Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        mapTriggerPermissions.put("pointOfInterest", Manifest.permission.ACCESS_NETWORK_STATE);
        mapTriggerPermissions.put("pointOfInterest", Manifest.permission.INTERNET);
        mapTriggerPermissions.put("pointOfInterest", Manifest.permission.ACCESS_WIFI_STATE);
        mapTriggerPermissions.put("process_started_stopped", Manifest.permission.GET_TASKS);
        mapTriggerPermissions.put("roaming", Manifest.permission.ACCESS_NETWORK_STATE);
        mapTriggerPermissions.put("speed", Manifest.permission.ACCESS_FINE_LOCATION);
        mapTriggerPermissions.put("speed", Manifest.permission.ACCESS_COARSE_LOCATION);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            mapTriggerPermissions.put("speed", Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        mapTriggerPermissions.put("speed", Manifest.permission.ACCESS_NETWORK_STATE);
        mapTriggerPermissions.put("speed", Manifest.permission.INTERNET);
//		  map.put("timeFrame", "");
        mapTriggerPermissions.put("usb_host_connection", Manifest.permission.READ_PHONE_STATE);
        mapTriggerPermissions.put("usb_host_connection", Manifest.permission.BATTERY_STATS);
        mapTriggerPermissions.put("wifiConnection", Manifest.permission.ACCESS_NETWORK_STATE);
        mapTriggerPermissions.put("wifiConnection", Manifest.permission.ACCESS_WIFI_STATE);

        mapActionPermissions = new HashMap<String, String>();
        mapActionPermissions.put("changeSoundProfile", Manifest.permission.MODIFY_AUDIO_SETTINGS);
        mapActionPermissions.put("changeSoundProfile", Manifest.permission.ACCESS_NOTIFICATION_POLICY);
        mapActionPermissions.put("disableScreenRotation", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("enableScreenRotation", Manifest.permission.WRITE_SETTINGS);
//		  mapActionPermissions.put("playMusic", "");
        mapActionPermissions.put("sendTextMessage", Manifest.permission.SEND_SMS);
        mapActionPermissions.put("setAirplaneMode", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("setAirplaneMode", Manifest.permission.ACCESS_NETWORK_STATE);
        /* Permission was not required anymore, even before Android 6: https://su.chainfire.eu/#updates-permission
        mapActionPermissions.put("setAirplaneMode", permissionNameSuperuser);*/
        mapActionPermissions.put("setAirplaneMode", Manifest.permission.CHANGE_NETWORK_STATE);
        mapActionPermissions.put("setBluetooth", Manifest.permission.BLUETOOTH_ADMIN);
        mapActionPermissions.put("setBluetooth", Manifest.permission.BLUETOOTH);
        mapActionPermissions.put("setBluetooth", Manifest.permission.ACCESS_NETWORK_STATE);
        mapActionPermissions.put("setBluetooth", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("setDataConnection", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("setDataConnection", Manifest.permission.ACCESS_NETWORK_STATE);
        /* Permission was not required anymore, even before Android 6: https://su.chainfire.eu/#updates-permission
        mapActionPermissions.put("setDataConnection", permissionNameSuperuser);*/
        mapActionPermissions.put("setDataConnection", Manifest.permission.CHANGE_NETWORK_STATE);
        mapActionPermissions.put("setDisplayRotation", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("setUsbTethering", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("setUsbTethering", Manifest.permission.CHANGE_NETWORK_STATE);
        mapActionPermissions.put("setWifi", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("setWifi", Manifest.permission.CHANGE_NETWORK_STATE);
        mapActionPermissions.put("setWifi", Manifest.permission.ACCESS_NETWORK_STATE);
        mapActionPermissions.put("setWifiTethering", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("setWifiTethering", Manifest.permission.CHANGE_NETWORK_STATE);
        mapActionPermissions.put("setWifiTethering", Manifest.permission.ACCESS_NETWORK_STATE);
        mapActionPermissions.put("triggerUrl", Manifest.permission.INTERNET);
        mapActionPermissions.put("turnBluetoothOff", Manifest.permission.BLUETOOTH_ADMIN);
        mapActionPermissions.put("turnBluetoothOff", Manifest.permission.BLUETOOTH);
        mapActionPermissions.put("turnBluetoothOff", Manifest.permission.ACCESS_NETWORK_STATE);
        mapActionPermissions.put("turnBluetoothOff", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("turnBluetoothOn", Manifest.permission.BLUETOOTH_ADMIN);
        mapActionPermissions.put("turnBluetoothOn", Manifest.permission.BLUETOOTH);
        mapActionPermissions.put("turnBluetoothOn", Manifest.permission.ACCESS_NETWORK_STATE);
        mapActionPermissions.put("turnBluetoothOn", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("turnUsbTetheringOff", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("turnUsbTetheringOff", Manifest.permission.CHANGE_NETWORK_STATE);
        mapActionPermissions.put("turnUsbTetheringOn", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("turnUsbTetheringOn", Manifest.permission.CHANGE_NETWORK_STATE);
        mapActionPermissions.put("turnWifiOff", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("turnWifiOff", Manifest.permission.CHANGE_NETWORK_STATE);
        mapActionPermissions.put("turnWifiOff", Manifest.permission.ACCESS_NETWORK_STATE);
        mapActionPermissions.put("turnWifiOn", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("turnWifiOn", Manifest.permission.CHANGE_NETWORK_STATE);
        mapActionPermissions.put("turnWifiOn", Manifest.permission.ACCESS_NETWORK_STATE);
        mapActionPermissions.put("turnWifiTetheringOff", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("turnWifiTetheringOff", Manifest.permission.CHANGE_NETWORK_STATE);
        mapActionPermissions.put("turnWifiTetheringOff", Manifest.permission.ACCESS_NETWORK_STATE);
        mapActionPermissions.put("turnWifiTetheringOn", Manifest.permission.WRITE_SETTINGS);
        mapActionPermissions.put("turnWifiTetheringOn", Manifest.permission.CHANGE_NETWORK_STATE);
        mapActionPermissions.put("turnWifiTetheringOn", Manifest.permission.ACCESS_NETWORK_STATE);
        mapActionPermissions.put("playSound", Manifest.permission.READ_EXTERNAL_STORAGE);
//		  mapActionPermissions.put("waitBeforeNextAction", "");
        mapActionPermissions.put("wakeupDevice", Manifest.permission.WAKE_LOCK);
    }

    public static boolean isPermissionDeclaratedInManifest(Context context, String permission)
    {
        PackageManager pm = context.getPackageManager();
        try
        {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = null;
            if (packageInfo != null)
            {
                requestedPermissions = packageInfo.requestedPermissions;
            }

            if (requestedPermissions != null && requestedPermissions.length > 0)
            {
                List<String> requestedPermissionsList = Arrays.asList(requestedPermissions);
                ArrayList<String> requestedPermissionsArrayList = new ArrayList<String>();
                requestedPermissionsArrayList.addAll(requestedPermissionsList);
                return (requestedPermissionsArrayList.contains(permission));
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Miscellaneous.logEvent("w", "ActivityPermissions", Log.getStackTraceString(e), 2);
        }

        return false;
    }

    public static Boolean verifyNotificationPermission()
    {
        try
        {
            String theList = android.provider.Settings.Secure.getString(Miscellaneous.getAnyContext().getContentResolver(), "enabled_notification_listeners");
            String[] theListList = theList.split(":");
            String me = (new ComponentName(Miscellaneous.getAnyContext(), NotificationListener.class)).flattenToString();
            for (String next : theListList)
            {
                if (me.equals(next))
                    return true;
            }
            return false;
        }
        catch(Exception e)
        {
            return false;
        }
    }
}