package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

public class ActivityManageActionWakeLock extends Activity
{
    RadioButton rbWakeLockActivate, rbWakeLockDeactivate;
    CheckBox chkWakeLockTimeout;
    EditText etWakeLockDuration;
    Button bSaveWakelock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_action_wakelock);

        rbWakeLockActivate = (RadioButton)findViewById(R.id.rbWakeLockActivate);
        rbWakeLockDeactivate = (RadioButton)findViewById(R.id.rbWakeLockDeactivate);
        chkWakeLockTimeout = (CheckBox)findViewById(R.id.chkWakeLockTimeout);
        etWakeLockDuration = (EditText)findViewById(R.id.etWakeLockDuration);
        bSaveWakelock = (Button)findViewById(R.id.bSaveWakelock);

        etWakeLockDuration.setEnabled(chkWakeLockTimeout.isChecked());

        chkWakeLockTimeout.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean wakeLockTimeoutSet)
            {
                etWakeLockDuration.setEnabled(wakeLockTimeoutSet);

                if(wakeLockTimeoutSet)
                    etWakeLockDuration.setText(String.valueOf(Actions.wakeLockTimeoutDisabled));
            }
        });

        if(getIntent().hasExtra(ActivityManageRule.intentNameActionParameter1))
        {
            rbWakeLockActivate.setChecked(getIntent().getBooleanExtra(ActivityManageRule.intentNameActionParameter1, true));
            rbWakeLockDeactivate.setChecked(!getIntent().getBooleanExtra(ActivityManageRule.intentNameActionParameter1, false));

            if(getIntent().hasExtra(ActivityManageRule.intentNameActionParameter2))
            {
                if(Miscellaneous.isNumeric(getIntent().getStringExtra(ActivityManageRule.intentNameActionParameter2)))
                {
                    long timeout = Long.parseLong((getIntent().getStringExtra(ActivityManageRule.intentNameActionParameter2)));
                    chkWakeLockTimeout.setChecked(timeout != Actions.wakeLockTimeoutDisabled);
                    etWakeLockDuration.setText(String.valueOf(timeout));
                }
                else
                {
                    chkWakeLockTimeout.setChecked(false);
                    etWakeLockDuration.setText(String.valueOf(Actions.wakeLockTimeoutDisabled));
                }
            }
        }

        bSaveWakelock.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(chkWakeLockTimeout.isChecked())
                {
                    if((StringUtils.isEmpty(etWakeLockDuration.getText().toString()) || Integer.parseInt(etWakeLockDuration.getText().toString()) <= 0))
                    {
                        Toast.makeText(ActivityManageActionWakeLock.this, getResources().getString(R.string.enterAPositiveValidNonDecimalNumber), Toast.LENGTH_LONG).show();
                        return;
                    }

                }

                Intent response = new Intent();
                response.putExtra(ActivityManageRule.intentNameActionParameter1, rbWakeLockActivate.isChecked());
                if(chkWakeLockTimeout.isChecked())
                    response.putExtra(ActivityManageRule.intentNameActionParameter2, etWakeLockDuration.getText().toString());
                else
                    response.putExtra(ActivityManageRule.intentNameActionParameter2, String.valueOf(Actions.wakeLockTimeoutDisabled));
                setResult(RESULT_OK, response);
                finish();
            }
        });
    }
}
