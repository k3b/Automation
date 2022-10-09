package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

public class ActivityManageTriggerCheckVariable extends Activity
{
    EditText etVariableKeyTrigger, etVariableValueTrigger;
    Button bTriggerVariableSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_trigger_check_variable);

        etVariableKeyTrigger = (EditText) findViewById(R.id.etVariableKeyTrigger);
        etVariableValueTrigger = (EditText) findViewById(R.id.etVariableValueTrigger);
        bTriggerVariableSave = (Button) findViewById(R.id.bTriggerVariableSave);

        Intent input = getIntent();
        if(input.hasExtra(ActivityManageRule.intentNameTriggerParameter2))
        {
            String[] conditions = input.getStringExtra(ActivityManageRule.intentNameTriggerParameter2).split(Trigger.triggerParameter2Split);
            etVariableKeyTrigger.setText(conditions[0]);
            if(conditions.length > 1)
                etVariableValueTrigger.setText(conditions[1]);
        }

        bTriggerVariableSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent response = new Intent();
//                response.putExtra(ActivityManageRule.intentNameTriggerParameter1, rbTetheringOn.isChecked());

                if(StringUtils.isEmpty(etVariableValueTrigger.getText().toString()))
                    response.putExtra(ActivityManageRule.intentNameTriggerParameter2, etVariableKeyTrigger.getText().toString());
                else
                    response.putExtra(ActivityManageRule.intentNameTriggerParameter2, etVariableKeyTrigger.getText().toString() + Trigger.triggerParameter2Split + etVariableValueTrigger.getText().toString());

                setResult(RESULT_OK, response);
                finish();
            }
        });
    }
}