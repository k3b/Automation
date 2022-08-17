package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

public class ActivityManageTriggerTethering extends Activity
{
    RadioButton rbTetheringOn, rbTetheringOff, rbTetheringTypeAny, rbTetheringTypeWifi, rbTetheringTypeBluetooth, rbTetheringTypeUsb, rbTetheringTypeCable;
    Button bTriggerTetheringSave;

    public final static String tetheringTypeAny = "tetheringTypeAny";
    public final static String tetheringTypeWifi = "tetheringTypeWifi";
    public final static String tetheringTypeBluetooth = "tetheringTypeBluetooth";
    public final static String tetheringTypeUsb = "tetheringTypeUsb";
    public final static String tetheringTypeCable = "tetheringTypeCable";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_trigger_tethering);

        rbTetheringOn = (RadioButton) findViewById(R.id.rbTetheringOn);
        rbTetheringOff = (RadioButton)findViewById(R.id.rbTetheringOff);
        rbTetheringTypeAny = (RadioButton) findViewById(R.id.rbTetheringTypeAny);
        rbTetheringTypeWifi = (RadioButton) findViewById(R.id.rbTetheringTypeWifi);
        rbTetheringTypeBluetooth = (RadioButton) findViewById(R.id.rbTetheringTypeBluetooth);
        rbTetheringTypeUsb = (RadioButton) findViewById(R.id.rbTetheringTypeUsb);
        rbTetheringTypeCable = (RadioButton) findViewById(R.id.rbTetheringTypeCable);
        bTriggerTetheringSave = (Button) findViewById(R.id.bTriggerTetheringSave);

        Intent input = getIntent();
        if(input.hasExtra(ActivityManageRule.intentNameTriggerParameter1))
        {
            rbTetheringOn.setChecked(input.getBooleanExtra(ActivityManageRule.intentNameTriggerParameter1, true));
            rbTetheringOff.setChecked(!input.getBooleanExtra(ActivityManageRule.intentNameTriggerParameter1, false));
        }

        if(input.hasExtra(ActivityManageRule.intentNameTriggerParameter2))
        {
            String type = input.getStringExtra(ActivityManageRule.intentNameTriggerParameter2);

            if(!StringUtils.isEmpty(type))
            {
                switch(type)
                {
                    case tetheringTypeAny:
                        rbTetheringTypeAny.setChecked(true);
                    case tetheringTypeWifi:
                        rbTetheringTypeWifi.setChecked(true);
                    case tetheringTypeBluetooth:
                        rbTetheringTypeBluetooth.setChecked(true);
                    case tetheringTypeUsb:
                        rbTetheringTypeUsb.setChecked(true);
                    case tetheringTypeCable:
                        rbTetheringTypeCable.setChecked(true);
                    default:
                }
            }
        }
        else
            rbTetheringTypeAny.setChecked(true);

        bTriggerTetheringSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent response = new Intent();
                response.putExtra(ActivityManageRule.intentNameTriggerParameter1, rbTetheringOn.isChecked());

                if(rbTetheringTypeAny.isChecked())
                    response.putExtra(ActivityManageRule.intentNameTriggerParameter2, tetheringTypeAny);
                else if(rbTetheringTypeWifi.isChecked())
                    response.putExtra(ActivityManageRule.intentNameTriggerParameter2, tetheringTypeWifi);
                else if(rbTetheringTypeBluetooth.isChecked())
                    response.putExtra(ActivityManageRule.intentNameTriggerParameter2, tetheringTypeBluetooth);
                else if(rbTetheringTypeUsb.isChecked())
                    response.putExtra(ActivityManageRule.intentNameTriggerParameter2, tetheringTypeUsb);
                else if(rbTetheringTypeCable.isChecked())
                    response.putExtra(ActivityManageRule.intentNameTriggerParameter2, tetheringTypeCable);

                setResult(RESULT_OK, response);
                finish();
            }
        });
    }
}