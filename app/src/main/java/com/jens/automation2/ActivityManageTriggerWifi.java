package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.annotation.Nullable;

public class ActivityManageTriggerWifi extends Activity
{
    RadioButton rbTriggerWifiConnected, rbTriggerWifiDisconnected;
    EditText etTriggerWifiName;
    Spinner spinnerWifiList;
    Button btriggerWifiSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_trigger_wifi);

        rbTriggerWifiConnected = (RadioButton)findViewById(R.id.rbTriggerWifiConnected);
        rbTriggerWifiDisconnected = (RadioButton)findViewById(R.id.rbTriggerWifiDisconnected);
        etTriggerWifiName = (EditText) findViewById(R.id.etTriggerWifiName);
        spinnerWifiList = (Spinner) findViewById(R.id. spinnerWifiList);
        btriggerWifiSave = (Button) findViewById(R.id. btriggerWifiSave);

        if(getIntent().hasExtra("edit"))
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
            }
        });
    }
}
