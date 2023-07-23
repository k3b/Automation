package com.jens.automation2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

public class ActivityManageActionVibrate extends Activity
{
    TextView etVibratePattern;
    Button bTestVibratePattern, bSaveVibratePattern;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Miscellaneous.setDisplayLanguage(this);
        setContentView(R.layout.activity_manage_action_vibrate);

        etVibratePattern = (EditText)findViewById(R.id.etVibratePattern);
        bTestVibratePattern = (Button)findViewById(R.id.bTestVibratePattern);
        bSaveVibratePattern = (Button)findViewById(R.id.bSaveVibratePattern);

        bSaveVibratePattern.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(checkInput())
                {
                    Intent answer = new Intent();
                    answer.putExtra("vibratePattern", etVibratePattern.getText().toString());
                    setResult(RESULT_OK, answer);
                    finish();
                }
            }
        });

        bTestVibratePattern.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(checkInput())
                {
                    if (ActivityPermissions.havePermission(Manifest.permission.VIBRATE, ActivityManageActionVibrate.this))
                    {
                        String pattern = etVibratePattern.getText().toString();
                        Actions.vibrate(false, pattern);
                    }
                }
            }
        });

        Intent input = getIntent();

        if(input.hasExtra("vibratePattern"))
            etVibratePattern.setText(input.getStringExtra("vibratePattern"));
    }

    boolean checkInput()
    {
        String vibratePattern = etVibratePattern.getText().toString();
        String regex = "^[0-9,]+$";
        if(StringUtils.isEmpty(vibratePattern) || !vibratePattern.matches(regex) || vibratePattern.substring(0, 1).equals(",") || vibratePattern.substring(vibratePattern.length()-1).equals(","))
        {
            Toast.makeText(ActivityManageActionVibrate.this, getResources().getString(R.string.pleaseEnterValidVibrationPattern), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}