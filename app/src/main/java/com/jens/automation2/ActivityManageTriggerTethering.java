package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class ActivityManageTriggerTethering extends Activity
{
    RadioButton rbTetheringOn, rTetheringOff;
    Button bTriggerTetheringSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_trigger_tethering);

        rbTetheringOn = (RadioButton) findViewById(R.id.rbTetheringOn);
        rTetheringOff = (RadioButton)findViewById(R.id.rTetheringOff);
        bTriggerTetheringSave = (Button) findViewById(R.id.bTriggerTetheringSave);

        Intent input = getIntent();
        if(input.hasExtra(ActivityManageRule.intentNameTriggerParameter1))
            rbTetheringOn.setChecked(input.getBooleanExtra(ActivityManageRule.intentNameTriggerParameter1, true));

        bTriggerTetheringSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent response = new Intent();
                response.putExtra(ActivityManageRule.intentNameTriggerParameter1, rbTetheringOn.isChecked());
                setResult(RESULT_OK, response);
                finish();
            }
        });
    }
}