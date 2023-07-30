package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.Nullable;

import com.jens.automation2.Trigger.subSystemStates;

public class ActivityManageTriggerSubSystemState extends Activity
{
    RadioButton rbSubSystemStateWifi, rbSubSystemStateBluetooth;
    RadioButton rbSubSystemStateEnabled, rbSubSystemStateDisabled;
    Button bSubSystemStateSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Miscellaneous.setDisplayLanguage(this);
        setContentView(R.layout.activity_manage_trigger_subsystemstate);

        rbSubSystemStateWifi = (RadioButton)findViewById(R.id.rbSubSystemStateWifi);
        rbSubSystemStateBluetooth = (RadioButton)findViewById(R.id.rbSubSystemStateBluetooth);
        rbSubSystemStateEnabled = (RadioButton)findViewById(R.id.rbSubSystemStateEnabled);
        rbSubSystemStateDisabled = (RadioButton)findViewById(R.id.rbSubSystemStateDisabled);
        bSubSystemStateSave = (Button)findViewById(R.id.bSubSystemStateSave);

        if(getIntent().hasExtra(ActivityManageRule.intentNameTriggerParameter1) && getIntent().hasExtra(ActivityManageRule.intentNameTriggerParameter2))
        {
            subSystemStates desiredState = subSystemStates.valueOf(getIntent().getStringExtra(ActivityManageRule.intentNameTriggerParameter2));

            switch(desiredState)
            {
                case wifi:
                    rbSubSystemStateWifi.setChecked(true);
                    break;
                case bluetooth:
                    rbSubSystemStateBluetooth.setChecked(true);
                    break;
                default:
            }

            if(getIntent().getBooleanExtra(ActivityManageRule.intentNameTriggerParameter1, true))
                rbSubSystemStateEnabled.setChecked(true);
            else
                rbSubSystemStateDisabled.setChecked(true);
        }

        bSubSystemStateSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent data = new Intent();

                data.putExtra(ActivityManageRule.intentNameTriggerParameter1, rbSubSystemStateEnabled.isChecked());

                if(rbSubSystemStateWifi.isChecked())
                    data.putExtra(ActivityManageRule.intentNameTriggerParameter2, subSystemStates.wifi.name());
                else if(rbSubSystemStateBluetooth.isChecked())
                    data.putExtra(ActivityManageRule.intentNameTriggerParameter2, subSystemStates.bluetooth.name());

                ActivityManageTriggerSubSystemState.this.setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}
