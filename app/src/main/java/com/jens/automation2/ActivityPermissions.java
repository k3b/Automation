package com.jens.automation2;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
    protected String[] specificPermissionsToRequest = null;

    public static String intentExtraName = "permissionsToBeRequested";

    Button bCancelPermissions, bRequestPermissions;
    TextView tvPermissionsExplanation, tvPermissionosExplanationSystemSettings, tvPermissionsExplanationLong;
    static ActivityPermissions instance = null;

    public static final String writeSystemSettingsPermissionName = "android.permission.WRITE_SETTINGS";
    public static final String writeExternalStoragePermissionName = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String accessNotificationPolicyPermissionName = "android.permission.ACCESS_NOTIFICATION_POLICY";
    public static final String permissionNameLocationFine = "android.permission.ACCESS_FINE_LOCATION";
    public static final String permissionNameLocationCoarse = "android.permission.ACCESS_COARSE_LOCATION";
    public static final String permissionNameLocationBackground = "android.permission.ACCESS_BACKGROUND_LOCATION";
    public static final String permissionNameCall = "android.permission.PROCESS_OUTGOING_CALLS";
    public static final String permissionNameStartService = "android.permission.FOREGROUND_SERVICE";
    
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
            setContentView(R.layout.permissions_activity);

            bCancelPermissions = (Button)findViewById(R.id.bCancelPermissions);
            bRequestPermissions = (Button)findViewById(R.id.bRequestPermissions);
            tvPermissionsExplanation = (TextView)findViewById(R.id.tvPermissionsExplanation);
            tvPermissionosExplanationSystemSettings = (TextView)findViewById(R.id.tvPermissionsExplanationSystemSettings);
            tvPermissionsExplanationLong = (TextView)findViewById(R.id.tvPermissionsExplanationLong);

            bCancelPermissions.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    finish();
                }
            });

//            bRequestPermissions.setOnClickListener(new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    finish();
//                }
//            });

            bRequestPermissions.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Request the basic permissions, that are absolutely required.
                    //getRequiredPermissions(true); // request permissions to access sd card access and "receive boot completed"

                    //fillPermissionMaps();


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
//                    requestSpecificPermission(permissionsToRequest);
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
            if(s.equals(permissionNameLocationCoarse) | s.equals(permissionNameLocationFine))
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
                explanation.append(

                        "<br />" +


                                "<u>" +
                                getResources().getString(getResources().getIdentifier(s, "string", getPackageName()))
                                + "</u>"

                                + "<br />");

                for (String reason : getReasonForPermission(s))
                    explanation.append(reason + "<br />");
            }
        }

        tvPermissionsExplanation.setText(Html.fromHtml(explanation.toString()));

        for(String s : requiredPerms)
        {
            if (s.equalsIgnoreCase(writeSystemSettingsPermissionName))
            {
                if (requiredPerms.length == 1)
                    tvPermissionosExplanationSystemSettings.setText(getResources().getString(R.string.systemSettingsNote1));
                else if (requiredPerms.length > 1)
                    tvPermissionosExplanationSystemSettings.setText(getResources().getString(R.string.systemSettingsNote1) + getResources().getString(R.string.systemSettingsNote2));

                break;
            }
        }

        ActivityMainScreen.updateMainScreen();
    }

    protected static void addToArrayListUnique(String value, ArrayList<String> list)
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
                if (!havePermission(s, context))
                    return true;
            }
        }

        return false;
    }

    public static boolean havePermission(String s, Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(s.equals(writeSystemSettingsPermissionName))
                return android.provider.Settings.System.canWrite(context);
            else if (s.equals(accessNotificationPolicyPermissionName))
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    return mNotificationManager.isNotificationPolicyAccessGranted();
                }
                else
                    return true;
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

//            if (!havePermission(ActivityPermissions.writeExternalStoragePermissionName, workingContext))
//                addToArrayListUnique(ActivityPermissions.writeExternalStoragePermissionName, requiredPermissions);

            for(Profile profile : Profile.getProfileCollection())
            {
               if(profile.changeIncomingCallsRingtone)
               {
                   addToArrayListUnique("android.permission.WRITE_SETTINGS", requiredPermissions);
               }
            }

            if (!onlyGeneral)
            {
                for (Rule rule : Rule.getRuleCollection())
                {
                    for (String singlePermission : getPermissionsForRule(rule))
                        if (!havePermission(singlePermission, workingContext))
                            addToArrayListUnique(singlePermission, requiredPermissions);
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
                        addToArrayListUnique("com.google.android.gms.permission.ACTIVITY_RECOGNITION", requiredPermissions);
                        addToArrayListUnique("android.permission.ACTIVITY_RECOGNITION", requiredPermissions);
                        break;
                    case airplaneMode:
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        break;
                    case batteryLevel:
                        addToArrayListUnique("android.permission.READ_PHONE_STATE", requiredPermissions);
//                        addToArrayListUnique("android.permission.BATTERY_STATS", requiredPermissions);
                        break;
                    case bluetoothConnection:
                        addToArrayListUnique("android.permission.BLUETOOTH_ADMIN", requiredPermissions);
                        addToArrayListUnique("android.permission.BLUETOOTH", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        break;
                    case charging:
                        addToArrayListUnique("android.permission.READ_PHONE_STATE", requiredPermissions);
//                        addToArrayListUnique("android.permission.BATTERY_STATS", requiredPermissions);
                        break;
                    case headsetPlugged:
                        addToArrayListUnique("android.permission.READ_PHONE_STATE", requiredPermissions);
                        break;
                    case nfcTag:
                        addToArrayListUnique("android.permission.NFC", requiredPermissions);
                        break;
                    case noiseLevel:
                        addToArrayListUnique("android.permission.RECORD_AUDIO", requiredPermissions);
                        break;
                    case phoneCall:
                        addToArrayListUnique("android.permission.READ_PHONE_STATE", requiredPermissions);
                        addToArrayListUnique(permissionNameCall, requiredPermissions);
                        break;
                    case pointOfInterest:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        {
                            addToArrayListUnique(permissionNameLocationBackground, requiredPermissions);
                            addToArrayListUnique(permissionNameLocationFine, requiredPermissions);
                            addToArrayListUnique(permissionNameLocationCoarse, requiredPermissions);
                        }
                        else
                        {
                            addToArrayListUnique(permissionNameLocationFine, requiredPermissions);
                            addToArrayListUnique(permissionNameLocationCoarse, requiredPermissions);
                        }
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        addToArrayListUnique("android.permission.INTERNET", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_WIFI_STATE", requiredPermissions);
                        break;
                    case process_started_stopped:
                        addToArrayListUnique("android.permission.GET_TASKS", requiredPermissions);
                        break;
                    case roaming:
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        break;
                    case speed:
                        addToArrayListUnique(permissionNameLocationFine, requiredPermissions);
                        addToArrayListUnique(permissionNameLocationCoarse, requiredPermissions);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            addToArrayListUnique(permissionNameLocationBackground, requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        addToArrayListUnique("android.permission.INTERNET", requiredPermissions);
                        break;
                    case timeFrame:
                        break;
                    case usb_host_connection:
                        addToArrayListUnique("android.permission.READ_PHONE_STATE", requiredPermissions);
//                        addToArrayListUnique("android.permission.BATTERY_STATS", requiredPermissions);
                        break;
                    case wifiConnection:
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_WIFI_STATE", requiredPermissions);
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
                        addToArrayListUnique("android.permission.MODIFY_AUDIO_SETTINGS", requiredPermissions);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            addToArrayListUnique(accessNotificationPolicyPermissionName, requiredPermissions);
                        break;
                    case disableScreenRotation:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        break;
                    case enableScreenRotation:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        break;
                    case playMusic:
                        break;
                    case sendTextMessage:
                        addToArrayListUnique("android.permission.SEND_SMS", requiredPermissions);
                        break;
                    case setAirplaneMode:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_SUPERUSER", requiredPermissions);
                        addToArrayListUnique("android.permission.CHANGE_NETWORK_STATE", requiredPermissions);
                        break;
                    case setBluetooth:
                        addToArrayListUnique("android.permission.BLUETOOTH_ADMIN", requiredPermissions);
                        addToArrayListUnique("android.permission.BLUETOOTH", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        break;
                    case setDataConnection:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_SUPERUSER", requiredPermissions);
                        addToArrayListUnique("android.permission.CHANGE_NETWORK_STATE", requiredPermissions);
                        break;
                    case setDisplayRotation:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        break;
                    case setUsbTethering:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        addToArrayListUnique("android.permission.CHANGE_NETWORK_STATE", requiredPermissions);
                        break;
                    case setWifi:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        addToArrayListUnique("android.permission.CHANGE_NETWORK_STATE", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        break;
                    case setWifiTethering:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        addToArrayListUnique("android.permission.CHANGE_NETWORK_STATE", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        break;
                    case speakText:
                        break;
                    case startOtherActivity:
                        break;
                    case triggerUrl:
                        addToArrayListUnique("android.permission.INTERNET", requiredPermissions);
                        //							  Hier m��te ein Hinweis kommen, da� nur die Variablen verwendet werden k�nnen, f�r die es Rechte gibt.
                        break;
                    case turnBluetoothOff:
                        addToArrayListUnique("android.permission.BLUETOOTH_ADMIN", requiredPermissions);
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        addToArrayListUnique("android.permission.BLUETOOTH", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        break;
                    case turnBluetoothOn:
                        addToArrayListUnique("android.permission.BLUETOOTH_ADMIN", requiredPermissions);
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        addToArrayListUnique("android.permission.BLUETOOTH", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        break;
                    case turnUsbTetheringOff:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        addToArrayListUnique("android.permission.CHANGE_NETWORK_STATE", requiredPermissions);
                        break;
                    case turnUsbTetheringOn:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        addToArrayListUnique("android.permission.CHANGE_NETWORK_STATE", requiredPermissions);
                        break;
                    case turnWifiOff:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        addToArrayListUnique("android.permission.CHANGE_NETWORK_STATE", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        break;
                    case turnWifiOn:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        addToArrayListUnique("android.permission.CHANGE_NETWORK_STATE", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        break;
                    case turnWifiTetheringOff:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        addToArrayListUnique("android.permission.CHANGE_NETWORK_STATE", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        break;
                    case turnWifiTetheringOn:
                        addToArrayListUnique(writeSystemSettingsPermissionName, requiredPermissions);
                        addToArrayListUnique("android.permission.CHANGE_NETWORK_STATE", requiredPermissions);
                        addToArrayListUnique("android.permission.ACCESS_NETWORK_STATE", requiredPermissions);
                        break;
                    case waitBeforeNextAction:
                        break;
                    case wakeupDevice:
                        addToArrayListUnique("android.permission.WAKE_LOCK", requiredPermissions);
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
                    if(action.equals(actionType))
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
            case "android.permission.RECEIVE_BOOT_COMPLETED":
                usingElements.add(getResources().getString(R.string.startAtSystemBoot));
                break;
            case accessNotificationPolicyPermissionName:
                usingElements.add(getResources().getString(R.string.actionChangeSoundProfile));
                break;
            case "android.permission.WRITE_EXTERNAL_STORAGE":
                usingElements.add(getResources().getString(R.string.storeSettings));
                break;
            case "com.google.android.gms.permission.ACTIVITY_RECOGNITION":
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.activityDetection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));

                break;
            case "android.permission.ACTIVITY_RECOGNITION":
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.activityDetection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));

                break;
            case permissionNameLocationCoarse:
//                usingElements.add(getResources().getString(R.string.android_permission_ACCESS_COARSE_LOCATION));
                usingElements.add(getResources().getString(R.string.manageLocations));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.pointOfInterest))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.speed))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case permissionNameLocationFine:
                usingElements.add(getResources().getString(R.string.manageLocations));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.pointOfInterest))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.speed))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case permissionNameLocationBackground:
                usingElements.add(getResources().getString(R.string.googleLocationChicanery));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.pointOfInterest))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.speed))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case "android.permission.ACCESS_NETWORK_STATE":
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
            case "android.permission.ACCESS_WIFI_STATE":
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.pointOfInterest))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.wifiConnection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            /*case "android.permission.BATTERY_STATS":
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.batteryLevel))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.charging))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.usb_host_connection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;*/
            case "android.permission.BLUETOOTH_ADMIN":
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.bluetoothConnection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.setBluetooth))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnBluetoothOff))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnBluetoothOn))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case "android.permission.BLUETOOTH":
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.bluetoothConnection))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.setBluetooth))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnBluetoothOff))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.turnBluetoothOn))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case "android.permission.GET_TASKS":
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.process_started_stopped))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case "android.permission.INTERNET":
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.pointOfInterest))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.speed))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                for(String ruleName : getRulesUsing(Action.Action_Enum.triggerUrl))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case "android.permission.NFC":
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.nfcTag))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case permissionNameCall:
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.phoneCall))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case "android.permission.READ_PHONE_STATE":
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
            case "android.permission.RECORD_AUDIO":
                for(String ruleName : getRulesUsing(Trigger.Trigger_Enum.noiseLevel))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case "android.permission.SEND_SMS":
                for(String ruleName : getRulesUsing(Action.Action_Enum.sendTextMessage))
                    usingElements.add(String.format(getResources().getString(R.string.ruleXrequiresThis), ruleName));
                break;
            case "android.permission.FOREGROUND_SERVICE":
                usingElements.add(getResources().getString(R.string.startAutomationAsService));
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

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                if (requestCode == requestCodeForPermissionsBackgroundLocation)
                {
                    NotificationManager mNotificationManager = (NotificationManager) ActivityPermissions.this.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (mNotificationManager.isNotificationPolicyAccessGranted())
                        requestPermissions(cachedPermissionsToRequest, true);
                }
            }
        }
    }

    public static void requestSpecificPermission(String... permissionNames)
    {
        ArrayList<String> permissionList = new ArrayList<String>();
        for(String permission : permissionNames)
        {
            if(permissionNames.equals(permissionNameCall))
            {
                    if(ActivityPermissions.isPermissionDeclaratedInManifest(Miscellaneous.getAnyContext(), permissionNameCall) && !Miscellaneous.isGooglePlayInstalled(Miscellaneous.getAnyContext()))
                    {
                        permissionList.add(permission);
                    }
            }
            else if(permissionNames.equals("android.permission.SEND_SMS"))
            {
                if(ActivityPermissions.isPermissionDeclaratedInManifest(Miscellaneous.getAnyContext(), "android.permission.SEND_SMS") && !Miscellaneous.isGooglePlayInstalled(Miscellaneous.getAnyContext()))
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
                    if (s.equalsIgnoreCase(writeSystemSettingsPermissionName))
                    {
                        requiredPermissions.remove(s);
                        cachedPermissionsToRequest = requiredPermissions;
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, requestCodeForPermissionsWriteSettings);
                        return;
                    }
                    else if (s.equalsIgnoreCase(accessNotificationPolicyPermissionName))
                    {
                        requiredPermissions.remove(s);
                        cachedPermissionsToRequest = requiredPermissions;
                        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
//                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, requestCodeForPermissionsNotificationPolicy);
                        return;
                    }
//                    else if (s.equalsIgnoreCase(permissionNameLocationBackground) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//                    {
//                        requiredPermissions.remove(s);
//                        cachedPermissionsToRequest = requiredPermissions;
//                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        intent.setData(Uri.parse("package:" + getPackageName()));
//                        startActivityForResult(intent, requestCodeForPermissionsBackgroundLocation);
//                        return;
//                    }
                }
            }

            if(requiredPermissions.size() > 0)
            {
                if(requiredPermissions.contains(permissionNameCall))
                {
                    if(!ActivityPermissions.isPermissionDeclaratedInManifest(Miscellaneous.getAnyContext(), "android.permission.SEND_SMS")
                            &&
                      Miscellaneous.isGooglePlayInstalled(Miscellaneous.getAnyContext())
                    )
                    {
                        requiredPermissions.remove(permissionNameCall);
                        Miscellaneous.messageBox("Problem", getResources().getString(R.string.googleSarcasm), ActivityPermissions.this).show();
                    }
                }
                if(requiredPermissions.contains("android.permission.SEND_SMS"))
                {
                    if(!ActivityPermissions.isPermissionDeclaratedInManifest(Miscellaneous.getAnyContext(), "android.permission.SEND_SMS")
                            &&
                            Miscellaneous.isGooglePlayInstalled(Miscellaneous.getAnyContext())
                    )
                    {
                        requiredPermissions.remove("android.permission.SEND_SMS");
                        Miscellaneous.messageBox("Problem", getResources().getString(R.string.googleSarcasm), ActivityPermissions.this).show();
                    }
                }


                StringBuilder permissions = new StringBuilder();
                for (String perm : requiredPermissions)
                    permissions.append(perm + "; ");
                if (permissions.length() > 0)
                    permissions.delete(permissions.length() - 2, permissions.length());

                Miscellaneous.logEvent("i", "Permissions", "Requesting permissions: " + permissions, 2);

//                Toast.makeText(ActivityPermissions.this, "Requesting permissions. Amount: " + String.valueOf(requiredPermissions.size()), Toast.LENGTH_LONG).show();
                requestPermissions(requiredPermissions.toArray(new String[requiredPermissions.size()]), requestCodeForPermissions);
            }
            else
                setHaveAllPermissions();
        }
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
//        Toast.makeText(ActivityPermissions.this, "onRequestPermissionsResult()", Toast.LENGTH_LONG).show();

//        ArrayList<String> disabledFeatures = new ArrayList<String>();
        ArrayList<String> deniedPermissions = new ArrayList<String>();

        if (requestCode == requestCodeForPermissions)
        {
            /*ArrayList<String> affectedGeneralList = new ArrayList<String>();
            ArrayList<String> affectedTriggersList = new ArrayList<String>();
            ArrayList<String> affectedActionList = new ArrayList<String>();*/

            for (int i=0; i < grantResults.length; i++)
            {
                if(permissions[i].equalsIgnoreCase(writeExternalStoragePermissionName) && grantResults[i] == PackageManager.PERMISSION_GRANTED)
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
                    The user denied certain permissions. With the exception of write-storage we need to live with that
                    and simply disable features while keeping the notification alive. The user may dismiss it anyway.
                 */

                Miscellaneous.logEvent("w", "Denied permissions", getResources().getString(R.string.theFollowingPermissionsHaveBeenDenied) + Miscellaneous.explode(deniedPermissions), 3);
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

    private void setHaveAllPermissions()
    {
        setResult(RESULT_OK);
        // All permissions have been granted.
        NotificationManager mNotificationManager = (NotificationManager) Miscellaneous.getAnyContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(notificationIdPermissions);
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
        mapGeneralPermissions.put("general", "android.permission.RECEIVE_BOOT_COMPLETED");
        mapGeneralPermissions.put("general", "android.permission.WRITE_EXTERNAL_STORAGE");

        mapTriggerPermissions = new HashMap<String, String>();
        mapTriggerPermissions.put("activityDetection", "com.google.android.gms.permission.ACTIVITY_RECOGNITION");
        mapTriggerPermissions.put("activityDetection", "android.permission.ACTIVITY_RECOGNITION");
        mapTriggerPermissions.put("airplaneMode", "android.permission.ACCESS_NETWORK_STATE");
        mapTriggerPermissions.put("batteryLevel", "android.permission.READ_PHONE_STATE");
        mapTriggerPermissions.put("batteryLevel", "android.permission.BATTERY_STATS");
        mapTriggerPermissions.put("bluetoothConnection", "android.permission.BLUETOOTH_ADMIN");
        mapTriggerPermissions.put("bluetoothConnection", "android.permission.BLUETOOTH");
        mapTriggerPermissions.put("bluetoothConnection", "android.permission.ACCESS_NETWORK_STATE");
        mapTriggerPermissions.put("charging", "android.permission.READ_PHONE_STATE");
        mapTriggerPermissions.put("charging", "android.permission.BATTERY_STATS");
        mapTriggerPermissions.put("headsetPlugged", "android.permission.READ_PHONE_STATE");
        mapTriggerPermissions.put("nfcTag", "android.permission.NFC");
        mapTriggerPermissions.put("noiseLevel", "android.permission.RECORD_AUDIO");
        mapTriggerPermissions.put("phoneCall", "android.permission.READ_PHONE_STATE");
        mapTriggerPermissions.put("phoneCall", permissionNameCall);
        mapTriggerPermissions.put("pointOfInterest", permissionNameLocationFine);
        mapTriggerPermissions.put("pointOfInterest", permissionNameLocationCoarse);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            mapTriggerPermissions.put("pointOfInterest", permissionNameLocationBackground);
        mapTriggerPermissions.put("pointOfInterest", "android.permission.ACCESS_NETWORK_STATE");
        mapTriggerPermissions.put("pointOfInterest", "android.permission.INTERNET");
        mapTriggerPermissions.put("pointOfInterest", "android.permission.ACCESS_WIFI_STATE");
        mapTriggerPermissions.put("process_started_stopped", "android.permission.GET_TASKS");
        mapTriggerPermissions.put("roaming", "android.permission.ACCESS_NETWORK_STATE");
        mapTriggerPermissions.put("speed", permissionNameLocationFine);
        mapTriggerPermissions.put("speed", permissionNameLocationCoarse);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            mapTriggerPermissions.put("speed", permissionNameLocationBackground);
        mapTriggerPermissions.put("speed", "android.permission.ACCESS_NETWORK_STATE");
        mapTriggerPermissions.put("speed", "android.permission.INTERNET");
//		  map.put("timeFrame", "");
        mapTriggerPermissions.put("usb_host_connection", "android.permission.READ_PHONE_STATE");
        mapTriggerPermissions.put("usb_host_connection", "android.permission.BATTERY_STATS");
        mapTriggerPermissions.put("wifiConnection", "android.permission.ACCESS_NETWORK_STATE");
        mapTriggerPermissions.put("wifiConnection", "android.permission.ACCESS_WIFI_STATE");

        mapActionPermissions = new HashMap<String, String>();
        mapActionPermissions.put("changeSoundProfile", "android.permission.MODIFY_AUDIO_SETTINGS");
        mapActionPermissions.put("changeSoundProfile", accessNotificationPolicyPermissionName);
        mapActionPermissions.put("disableScreenRotation", writeSystemSettingsPermissionName);
        mapActionPermissions.put("enableScreenRotation", writeSystemSettingsPermissionName);
//		  mapActionPermissions.put("playMusic", "");
        mapActionPermissions.put("sendTextMessage", "android.permission.SEND_SMS");
        mapActionPermissions.put("setAirplaneMode", writeSystemSettingsPermissionName);
        mapActionPermissions.put("setAirplaneMode", "android.permission.ACCESS_NETWORK_STATE");
        mapActionPermissions.put("setAirplaneMode", "android.permission.ACCESS_SUPERUSER");
        mapActionPermissions.put("setAirplaneMode", "android.permission.CHANGE_NETWORK_STATE");
        mapActionPermissions.put("setBluetooth", "android.permission.BLUETOOTH_ADMIN");
        mapActionPermissions.put("setBluetooth", "android.permission.BLUETOOTH");
        mapActionPermissions.put("setBluetooth", "android.permission.ACCESS_NETWORK_STATE");
        mapActionPermissions.put("setBluetooth", writeSystemSettingsPermissionName);
        mapActionPermissions.put("setDataConnection", writeSystemSettingsPermissionName);
        mapActionPermissions.put("setDataConnection", "android.permission.ACCESS_NETWORK_STATE");
        mapActionPermissions.put("setDataConnection", "android.permission.ACCESS_SUPERUSER");
        mapActionPermissions.put("setDataConnection", "android.permission.CHANGE_NETWORK_STATE");
        mapActionPermissions.put("setDisplayRotation", writeSystemSettingsPermissionName);
        mapActionPermissions.put("setUsbTethering", writeSystemSettingsPermissionName);
        mapActionPermissions.put("setUsbTethering", "android.permission.CHANGE_NETWORK_STATE");
        mapActionPermissions.put("setWifi", writeSystemSettingsPermissionName);
        mapActionPermissions.put("setWifi", "android.permission.CHANGE_NETWORK_STATE");
        mapActionPermissions.put("setWifi", "android.permission.ACCESS_NETWORK_STATE");
        mapActionPermissions.put("setWifiTethering", writeSystemSettingsPermissionName);
        mapActionPermissions.put("setWifiTethering", "android.permission.CHANGE_NETWORK_STATE");
        mapActionPermissions.put("setWifiTethering", "android.permission.ACCESS_NETWORK_STATE");
//		  mapActionPermissions.put("speakText", accessNotificationPolicyPermissionName);
//		  mapActionPermissions.put("startOtherActivity", "");
        mapActionPermissions.put("triggerUrl", "android.permission.INTERNET");
//			  Hier müßte ein Hinweis kommen, daß nur die Variablen verwendet werden können, für die es Rechte gibt.
        mapActionPermissions.put("turnBluetoothOff", "android.permission.BLUETOOTH_ADMIN");
        mapActionPermissions.put("turnBluetoothOff", "android.permission.BLUETOOTH");
        mapActionPermissions.put("turnBluetoothOff", "android.permission.ACCESS_NETWORK_STATE");
        mapActionPermissions.put("turnBluetoothOff", writeSystemSettingsPermissionName);
        mapActionPermissions.put("turnBluetoothOn", "android.permission.BLUETOOTH_ADMIN");
        mapActionPermissions.put("turnBluetoothOn", "android.permission.BLUETOOTH");
        mapActionPermissions.put("turnBluetoothOn", "android.permission.ACCESS_NETWORK_STATE");
        mapActionPermissions.put("turnBluetoothOn", writeSystemSettingsPermissionName);
        mapActionPermissions.put("turnUsbTetheringOff", writeSystemSettingsPermissionName);
        mapActionPermissions.put("turnUsbTetheringOff", "android.permission.CHANGE_NETWORK_STATE");
        mapActionPermissions.put("turnUsbTetheringOn", writeSystemSettingsPermissionName);
        mapActionPermissions.put("turnUsbTetheringOn", "android.permission.CHANGE_NETWORK_STATE");
        mapActionPermissions.put("turnWifiOff", writeSystemSettingsPermissionName);
        mapActionPermissions.put("turnWifiOff", "android.permission.CHANGE_NETWORK_STATE");
        mapActionPermissions.put("turnWifiOff", "android.permission.ACCESS_NETWORK_STATE");
        mapActionPermissions.put("turnWifiOn", writeSystemSettingsPermissionName);
        mapActionPermissions.put("turnWifiOn", "android.permission.CHANGE_NETWORK_STATE");
        mapActionPermissions.put("turnWifiOn", "android.permission.ACCESS_NETWORK_STATE");
        mapActionPermissions.put("turnWifiTetheringOff", writeSystemSettingsPermissionName);
        mapActionPermissions.put("turnWifiTetheringOff", "android.permission.CHANGE_NETWORK_STATE");
        mapActionPermissions.put("turnWifiTetheringOff", "android.permission.ACCESS_NETWORK_STATE");
        mapActionPermissions.put("turnWifiTetheringOn", writeSystemSettingsPermissionName);
        mapActionPermissions.put("turnWifiTetheringOn", "android.permission.CHANGE_NETWORK_STATE");
        mapActionPermissions.put("turnWifiTetheringOn", "android.permission.ACCESS_NETWORK_STATE");
//		  mapActionPermissions.put("waitBeforeNextAction", "");
        mapActionPermissions.put("wakeupDevice", "android.permission.WAKE_LOCK");
    }

    /*
        <string name="android.permission.SEND_SMS"></string>
        <string name="android.permission.SEND_SMS_NO_CONFIRMATION"></string>
        <string name="android.permission.RECEIVE_SMS"></string>
        <string name="android.permission.RECEIVE_MMS"></string>
        <string name="android.permission.RECEIVE_EMERGENCY_BROADCAST"></string>
        <string name="android.permission.READ_CELL_BROADCASTS"></string>
        <string name="android.permission.READ_SMS"></string>
        <string name="android.permission.WRITE_SMS"></string>
        <string name="android.permission.RECEIVE_WAP_PUSH"></string>
        <string name="android.permission.READ_CONTACTS"></string>
        <string name="android.permission.WRITE_CONTACTS"></string>
        <string name="android.permission.BIND_DIRECTORY_SEARCH"></string>
        <string name="android.permission.READ_CALL_LOG"></string>
        <string name="android.permission.WRITE_CALL_LOG"></string>
        <string name="android.permission.READ_SOCIAL_STREAM"></string>
        <string name="android.permission.WRITE_SOCIAL_STREAM"></string>
        <string name="android.permission.READ_PROFILE"></string>
        <string name="android.permission.WRITE_PROFILE"></string>
        <string name="android.permission.READ_CALENDAR"></string>
        <string name="android.permission.WRITE_CALENDAR"></string>
        <string name="android.permission.READ_USER_DICTIONARY"></string>
        <string name="android.permission.WRITE_USER_DICTIONARY"></string>
        <string name="com.android.browser.permission.READ_HISTORY_BOOKMARKS"></string>
        <string name="com.android.browser.permission.WRITE_HISTORY_BOOKMARKS"></string>
        <string name="com.android.alarm.permission.SET_ALARM"></string>
        <string name="com.android.voicemail.permission.ADD_VOICEMAIL"></string>
        <string name="android.permission.ACCESS_FINE_LOCATION"></string>
        <string name="android.permission.ACCESS_COARSE_LOCATION"></string>
        <string name="android.permission.ACCESS_MOCK_LOCATION"></string>
        <string name="android.permission.ACCESS_LOCATION_EXTRA_COMMANDS"></string>
        <string name="android.permission.INSTALL_LOCATION_PROVIDER"></string>
        <string name="android.permission.INTERNET"></string>
        <string name="android.permission.ACCESS_NETWORK_STATE"></string>
        <string name="android.permission.ACCESS_WIFI_STATE"></string>
        <string name="android.permission.CHANGE_WIFI_STATE"></string>
        <string name="android.permission.ACCESS_WIMAX_STATE"></string>
        <string name="android.permission.CHANGE_WIMAX_STATE"></string>
        <string name="android.permission.BLUETOOTH"></string>
        <string name="android.permission.BLUETOOTH_ADMIN"></string>
        <string name="android.permission.BLUETOOTH_STACK"></string>
        <string name="android.permission.NFC"></string>
        <string name="android.permission.CONNECTIVITY_INTERNAL"></string>
        <string name="android.permission.RECEIVE_DATA_ACTIVITY_CHANGE"></string>
        <string name="android.permission.GET_ACCOUNTS"></string>
        <string name="android.permission.AUTHENTICATE_ACCOUNTS"></string>
        <string name="android.permission.USE_CREDENTIALS"></string>
        <string name="android.permission.MANAGE_ACCOUNTS"></string>
        <string name="android.permission.ACCOUNT_MANAGER"></string>
        <string name="android.permission.CHANGE_WIFI_MULTICAST_STATE"></string>
        <string name="android.permission.VIBRATE"></string>
        <string name="android.permission.FLASHLIGHT"></string>
        <string name="android.permission.WAKE_LOCK"></string>
        <string name="android.permission.MODIFY_AUDIO_SETTINGS"></string>
        <string name="android.permission.MANAGE_USB"></string>
        <string name="android.permission.ACCESS_MTP"></string>
        <string name="android.permission.HARDWARE_TEST"></string>
        <string name="android.permission.NET_ADMIN"></string>
        <string name="android.permission.REMOTE_AUDIO_PLAYBACK"></string>
        <string name="android.permission.RECORD_AUDIO"></string>
        <string name="android.permission.CAMERA"></string>
        <string name="android.permission.PROCESS_OUTGOING_CALLS"></string>
        <string name="android.permission.MODIFY_PHONE_STATE"></string>
        <string name="android.permission.READ_PHONE_STATE"></string>
        <string name="android.permission.READ_PRIVILEGED_PHONE_STATE"></string>
        <string name="android.permission.CALL_PHONE"></string>
        <string name="android.permission.USE_SIP"></string>
        <string name="android.permission.READ_EXTERNAL_STORAGE"></string>
        <string name="android.permission.WRITE_EXTERNAL_STORAGE"></string>
        <string name="android.permission.WRITE_MEDIA_STORAGE"></string>
        <string name="android.permission.DISABLE_KEYGUARD"></string>
        <string name="android.permission.GET_TASKS"></string>
        <string name="android.permission.INTERACT_ACROSS_USERS"></string>
        <string name="android.permission.INTERACT_ACROSS_USERS_FULL"></string>
        <string name="android.permission.MANAGE_USERS"></string>
        <string name="android.permission.GET_DETAILED_TASKS"></string>
        <string name="android.permission.REORDER_TASKS"></string>
        <string name="android.permission.REMOVE_TASKS"></string>
        <string name="android.permission.START_ANY_ACTIVITY"></string>
        <string name="android.permission.RESTART_PACKAGES"></string>
        <string name="android.permission.KILL_BACKGROUND_PROCESSES"></string>
        <string name="android.permission.SYSTEM_ALERT_WINDOW"></string>
        <string name="android.permission.SET_WALLPAPER"></string>
        <string name="android.permission.SET_WALLPAPER_HINTS"></string>
        <string name="android.permission.SET_TIME"></string>
        <string name="android.permission.SET_TIME_ZONE"></string>
        <string name="android.permission.EXPAND_STATUS_BAR"></string>
        <string name="android.permission.READ_SYNC_SETTINGS"></string>
        <string name="android.permission.WRITE_SYNC_SETTINGS"></string>
        <string name="android.permission.READ_SYNC_STATS"></string>
        <string name="android.permission.SET_SCREEN_COMPATIBILITY"></string>
        <string name="android.permission.ACCESS_ALL_EXTERNAL_STORAGE"></string>
        <string name="android.permission.CHANGE_CONFIGURATION"></string>
        <string name="android.permission.WRITE_SETTINGS"></string>
        <string name="android.permission.WRITE_GSERVICES"></string>
        <string name="android.permission.SET_SCREEN_COMPATIBILITY"></string>
        <string name="android.permission.CHANGE_CONFIGURATION"></string>
        <string name="android.permission.FORCE_STOP_PACKAGES"></string>
        <string name="android.permission.RETRIEVE_WINDOW_CONTENT"></string>
        <string name="android.permission.SET_ANIMATION_SCALE"></string>
        <string name="android.permission.PERSISTENT_ACTIVITY"></string>
        <string name="android.permission.GET_PACKAGE_SIZE"></string>
        <string name="android.permission.SET_PREFERRED_APPLICATIONS"></string>
        <string name="android.permission.RECEIVE_BOOT_COMPLETED"></string>
        <string name="android.permission.BROADCAST_STICKY"></string>
        <string name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"></string>
        <string name="android.permission.MOUNT_FORMAT_FILESYSTEMS"></string>
        <string name="android.permission.ASEC_ACCESS"></string>
        <string name="android.permission.ASEC_CREATE"></string>
        <string name="android.permission.ASEC_DESTROY"></string>
        <string name="android.permission.ASEC_MOUNT_UNMOUNT"></string>
        <string name="android.permission.ASEC_RENAME"></string>
        <string name="android.permission.WRITE_APN_SETTINGS"></string>
        <string name="android.permission.SUBSCRIBED_FEEDS_READ"></string>
        <string name="android.permission.SUBSCRIBED_FEEDS_WRITE"></string>
        <string name="android.permission.CHANGE_NETWORK_STATE"></string>
        <string name="android.permission.CLEAR_APP_CACHE"></string>
        <string name="android.permission.ALLOW_ANY_CODEC_FOR_PLAYBACK"></string>
        <string name="android.permission.WRITE_SECURE_SETTINGS"></string>
        <string name="android.permission.DUMP"></string>
        <string name="android.permission.READ_LOGS"></string>
        <string name="android.permission.SET_DEBUG_APP"></string>
        <string name="android.permission.SET_PROCESS_LIMIT"></string>
        <string name="android.permission.SET_ALWAYS_FINISH"></string>
        <string name="android.permission.SIGNAL_PERSISTENT_PROCESSES"></string>
        <string name="android.permission.DIAGNOSTIC"></string>
        <string name="android.permission.STATUS_BAR"></string>
        <string name="android.permission.STATUS_BAR_SERVICE"></string>
        <string name="android.permission.FORCE_BACK"></string>
        <string name="android.permission.UPDATE_DEVICE_STATS"></string>
        <string name="android.permission.INTERNAL_SYSTEM_WINDOW"></string>
        <string name="android.permission.MANAGE_APP_TOKENS"></string>
        <string name="android.permission.FREEZE_SCREEN"></string>
        <string name="android.permission.INJECT_EVENTS"></string>
        <string name="android.permission.FILTER_EVENTS"></string>
        <string name="android.permission.RETRIEVE_WINDOW_INFO"></string>
        <string name="android.permission.TEMPORARY_ENABLE_ACCESSIBILITY"></string>
        <string name="android.permission.MAGNIFY_DISPLAY"></string>
        <string name="android.permission.SET_ACTIVITY_WATCHER"></string>
        <string name="android.permission.SHUTDOWN"></string>
        <string name="android.permission.STOP_APP_SWITCHES"></string>
        <string name="android.permission.READ_INPUT_STATE"></string>
        <string name="android.permission.BIND_INPUT_METHOD"></string>
        <string name="android.permission.BIND_ACCESSIBILITY_SERVICE"></string>
        <string name="android.permission.BIND_TEXT_SERVICE"></string>
        <string name="android.permission.BIND_VPN_SERVICE"></string>
        <string name="android.permission.BIND_WALLPAPER"></string>
        <string name="android.permission.BIND_DEVICE_ADMIN"></string>
        <string name="android.permission.SET_ORIENTATION"></string>
        <string name="android.permission.SET_POINTER_SPEED"></string>
        <string name="android.permission.SET_KEYBOARD_LAYOUT"></string>
        <string name="android.permission.INSTALL_PACKAGES"></string>
        <string name="android.permission.CLEAR_APP_USER_DATA"></string>
        <string name="android.permission.DELETE_CACHE_FILES"></string>
        <string name="android.permission.DELETE_PACKAGES"></string>
        <string name="android.permission.MOVE_PACKAGE"></string>
        <string name="android.permission.CHANGE_COMPONENT_ENABLED_STATE"></string>
        <string name="android.permission.GRANT_REVOKE_PERMISSIONS"></string>
        <string name="android.permission.ACCESS_SURFACE_FLINGER"></string>
        <string name="android.permission.READ_FRAME_BUFFER"></string>
        <string name="android.permission.CONFIGURE_WIFI_DISPLAY"></string>
        <string name="android.permission.CONTROL_WIFI_DISPLAY"></string>
        <string name="android.permission.BRICK"></string>
        <string name="android.permission.REBOOT"></string>
        <string name="android.permission.DEVICE_POWER"></string>
        <string name="android.permission.NET_TUNNELING"></string>
        <string name="android.permission.FACTORY_TEST"></string>
        <string name="android.permission.BROADCAST_PACKAGE_REMOVED"></string>
        <string name="android.permission.BROADCAST_SMS"></string>
        <string name="android.permission.BROADCAST_WAP_PUSH"></string>
        <string name="android.permission.MASTER_CLEAR"></string>
        <string name="android.permission.CALL_PRIVILEGED"></string>
        <string name="android.permission.PERFORM_CDMA_PROVISIONING"></string>
        <string name="android.permission.CONTROL_LOCATION_UPDATES"></string>
        <string name="android.permission.ACCESS_CHECKIN_PROPERTIES"></string>
        <string name="android.permission.PACKAGE_USAGE_STATS"></string>
        <string name="android.permission.BATTERY_STATS"></string>
        <string name="android.permission.BACKUP"></string>
        <string name="android.permission.CONFIRM_FULL_BACKUP"></string>
        <string name="android.permission.BIND_REMOTEVIEWS"></string>
        <string name="android.permission.BIND_APPWIDGET"></string>
        <string name="android.permission.BIND_KEYGUARD_APPWIDGET"></string>
        <string name="android.permission.MODIFY_APPWIDGET_BIND_PERMISSIONS"></string>
        <string name="android.permission.CHANGE_BACKGROUND_DATA_SETTING"></string>
        <string name="android.permission.GLOBAL_SEARCH"></string>
        <string name="android.permission.GLOBAL_SEARCH_CONTROL"></string>
        <string name="android.permission.SET_WALLPAPER_COMPONENT"></string>
        <string name="android.permission.READ_DREAM_STATE"></string>
        <string name="android.permission.WRITE_DREAM_STATE"></string>
        <string name="android.permission.ACCESS_CACHE_FILESYSTEM"></string>
        <string name="android.permission.COPY_PROTECTED_DATA"></string>
        <string name="android.permission.CRYPT_KEEPER"></string>
        <string name="android.permission.READ_NETWORK_USAGE_HISTORY"></string>
        <string name="android.permission.MANAGE_NETWORK_POLICY"></string>
        <string name="android.permission.MODIFY_NETWORK_ACCOUNTING"></string>
        <string name="android.intent.category.MASTER_CLEAR.permission.C2D_MESSAGE"></string>
        <string name="android.permission.PACKAGE_VERIFICATION_AGENT"></string>
        <string name="android.permission.BIND_PACKAGE_VERIFIER"></string>
        <string name="android.permission.SERIAL_PORT"></string>
        <string name="android.permission.ACCESS_CONTENT_PROVIDERS_EXTERNALLY"></string>
        <string name="android.permission.UPDATE_LOCK"></string>
     */

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
//                Log.i(ExConsts.TAG, ""+requestedPermissionsArrayList);
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        return false;
    }
}
