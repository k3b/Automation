package com.jens.automation2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.jens.automation2.receivers.BluetoothReceiver;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityManageTriggerWifi extends Activity
{
    RadioButton rbTriggerWifiConnected, rbTriggerWifiDisconnected;
    EditText etTriggerWifiName;
    Spinner spinnerWifiList;
    Button btriggerWifiSave, bLoadWifiList;
    List<String> wifiList = new ArrayList<>();
    ArrayAdapter<String> wifiSpinnerAdapter;
    private final static int requestCodeLocationPermission = 124;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_trigger_wifi);

        rbTriggerWifiConnected = (RadioButton) findViewById(R.id.rbTriggerWifiConnected);
        rbTriggerWifiDisconnected = (RadioButton) findViewById(R.id.rbTriggerWifiDisconnected);
        etTriggerWifiName = (EditText) findViewById(R.id.etTriggerWifiName);
        spinnerWifiList = (Spinner) findViewById(R.id.spinnerWifiList);
        btriggerWifiSave = (Button) findViewById(R.id.btriggerWifiSave);
        bLoadWifiList = (Button) findViewById(R.id.bLoadWifiList);

        wifiSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.text_view_for_poi_listview_mediumtextsize, wifiList);
        spinnerWifiList.setAdapter(wifiSpinnerAdapter);
        spinnerWifiList.setEnabled(false);  // bug in Android; this only works when done in code, not in xml

        if (getIntent().hasExtra("edit"))
        {
            boolean connected = getIntent().getBooleanExtra("wifiState", false);
            String wifiName = getIntent().getStringExtra("wifiName");

            rbTriggerWifiConnected.setChecked(connected);
            rbTriggerWifiDisconnected.setChecked(!connected);

            etTriggerWifiName.setText(wifiName);
        }

        btriggerWifiSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent response = new Intent();
                response.putExtra("wifiState", rbTriggerWifiConnected.isChecked());
                response.putExtra("wifiName", etTriggerWifiName.getText().toString());
                setResult(RESULT_OK, response);
                finish();
            }
        });

        spinnerWifiList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                etTriggerWifiName.setText(wifiList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        bLoadWifiList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                loadWifis();
            }
        });
    }

    public void loadWifis()
    {
        if(!ActivityPermissions.havePermission(Manifest.permission.ACCESS_FINE_LOCATION, ActivityManageTriggerWifi.this))
        {
            AlertDialog dialog = Miscellaneous.messageBox(getResources().getString(R.string.permissionsTitle), getResources().getString(R.string.needLocationPermForWifiList), ActivityManageTriggerWifi.this);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialog)
                {
                    ActivityCompat.requestPermissions(ActivityManageTriggerWifi.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, requestCodeLocationPermission);
                }
            });
            dialog.show();
        }
        else
        {
            reallyLoadWifiList();
        }
    }

    void reallyLoadWifiList()
    {
        if(Build.VERSION.SDK_INT >= 30)
        {
            Miscellaneous.messageBox(getResources().getString(R.string.hint), getResources().getString(R.string.wifiApi30), ActivityManageTriggerWifi.this).show();
            loadListOfVisibleWifis();
        }
        else
        {
            WifiManager myWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            for (WifiConfiguration wifi : myWifiManager.getConfiguredNetworks())
                wifiList.add(wifi.SSID.replaceAll("\"+$", "").replaceAll("^\"+", ""));
        }

            if (wifiList.size() > 0)
        {
            spinnerWifiList.setEnabled(true);
            Collections.sort(wifiList);
        }
        else
        {
            spinnerWifiList.setEnabled(false);
            Toast.makeText(ActivityManageTriggerWifi.this, getResources().getString(R.string.noKnownWifis), Toast.LENGTH_SHORT).show();
        }

        wifiSpinnerAdapter.notifyDataSetChanged();
    }

    void loadListOfVisibleWifis()
    {
        List<ScanResult> results = null;

        try
        {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            results = wifiManager.getScanResults();

            for (ScanResult wifi : results)
                wifiList.add(wifi.SSID.replaceAll("\"+$", "").replaceAll("^\"+", ""));
        }
        catch(Exception e)
        {
            Miscellaneous.logEvent("e", "loadListOfVisibleWifis()", Log.getStackTraceString(e), 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case requestCodeLocationPermission:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    reallyLoadWifiList();
                break;
        }
    }
}
