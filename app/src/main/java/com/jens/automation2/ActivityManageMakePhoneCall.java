package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

public class ActivityManageMakePhoneCall extends Activity
{
    EditText etTargetPhoneNumber;
    Button bActionMakePhoneCallSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_action_make_phone_call);

        etTargetPhoneNumber = (EditText)findViewById(R.id.etTargetPhoneNumber);
        bActionMakePhoneCallSave = (Button) findViewById(R.id.bActionMakePhoneCallSave);

        Intent input = getIntent();
        /*if(input.hasExtra(ActivityManageRule.intentNameActionParameter1))
            rbActionWifiOn.setChecked(input.getBooleanExtra(ActivityManageRule.intentNameActionParameter1, true));
*/
        if(input.hasExtra(ActivityManageRule.intentNameActionParameter2))
            etTargetPhoneNumber.setText(input.getStringExtra(ActivityManageRule.intentNameActionParameter2));

        bActionMakePhoneCallSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!StringUtils.isEmpty(etTargetPhoneNumber.getText()))
                {
                    Intent response = new Intent();
                    response.putExtra(ActivityManageRule.intentNameActionParameter1, false);
                    response.putExtra(ActivityManageRule.intentNameActionParameter2, etTargetPhoneNumber.getText().toString());
                    setResult(RESULT_OK, response);
                    finish();
                }
                else
                    Toast.makeText(ActivityManageMakePhoneCall.this, getResources().getText(R.string.enterPhoneNumber), Toast.LENGTH_SHORT).show();
            }
        });
    }
}