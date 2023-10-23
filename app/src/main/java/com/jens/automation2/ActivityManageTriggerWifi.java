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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityManageTriggerWifi extends Activity
{
    public final static String intentNameWifiState = "wifiState";
    public final static String intentNameWifiName = "wifiName";

    RadioButton rbTriggerWifiConnected, rbTriggerWifiDisconnected;
    EditText etTriggerWifiName;
    Spinner spinnerWifiList;
    Button bTriggerWifiSave, bLoadWifiList;
    List<String> wifiList = new ArrayList<>();
    ArrayAdapter<String> wifiSpinnerAdapter;
    private final static int requestCodeLocationPermission = 124;
    TextView tvWifiTriggerNameLocationNotice, tvWifiTriggerDisconnectionHint;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Miscellaneous.setDisplayLanguage(this);
        setContentView(R.layout.activity_manage_trigger_wifi);

        rbTriggerWifiConnected = (RadioButton) findViewById(R.id.rbTriggerWifiConnected);
        rbTriggerWifiDisconnected = (RadioButton) findViewById(R.id.rbTriggerWifiDisconnected);
        etTriggerWifiName = (EditText) findViewById(R.id.etTriggerWifiName);
        spinnerWifiList = (Spinner) findViewById(R.id.spinnerWifiList);
        bTriggerWifiSave = (Button) findViewById(R.id.bTriggerWifiSave);
        bLoadWifiList = (Button) findViewById(R.id.bLoadWifiList);
        tvWifiTriggerNameLocationNotice = (TextView)findViewById(R.id.tvWifiTriggerNameLocationNotice);
        tvWifiTriggerDisconnectionHint = (TextView)findViewById(R.id.tvWifiTriggerDisconnectionHint);

        tvWifiTriggerDisconnectionHint.setVisibility(View.GONE);

        wifiSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.text_view_for_poi_listview_mediumtextsize, wifiList);
        spinnerWifiList.setAdapter(wifiSpinnerAdapter);
        spinnerWifiList.setEnabled(false);  // bug in Android; this only works when done in code, not in xml

        if(
                Miscellaneous.getTargetSDK(Miscellaneous.getAnyContext()) >= 29
                        &&
                !ActivityPermissions.isPermissionDeclaratedInManifest(Miscellaneous.getAnyContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        )
            tvWifiTriggerNameLocationNotice.setVisibility(View.VISIBLE);

        if (getIntent().hasExtra("edit"))
        {
            boolean connected = getIntent().getBooleanExtra("wifiState", false);
            String wifiName = getIntent().getStringExtra("wifiName");

            rbTriggerWifiConnected.setChecked(connected);
            rbTriggerWifiDisconnected.setChecked(!connected);

            etTriggerWifiName.setText(wifiName);
        }

        bTriggerWifiSave.setOnClickListener(new View.OnClickListener()
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

                if(etTriggerWifiName.getText().toString().length() > 0 && rbTriggerWifiDisconnected.isChecked())
                    tvWifiTriggerDisconnectionHint.setVisibility(View.VISIBLE);
                else
                    tvWifiTriggerDisconnectionHint.setVisibility(View.GONE);
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

        rbTriggerWifiDisconnected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if(etTriggerWifiName.getText().toString().length() > 0 && b)
                    tvWifiTriggerDisconnectionHint.setVisibility(View.VISIBLE);
                else
                    tvWifiTriggerDisconnectionHint.setVisibility(View.GONE);
            }
        });
        etTriggerWifiName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                if(etTriggerWifiName.getText().toString().length() > 0 && rbTriggerWifiDisconnected.isChecked())
                    tvWifiTriggerDisconnectionHint.setVisibility(View.VISIBLE);
                else
                    tvWifiTriggerDisconnectionHint.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

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
