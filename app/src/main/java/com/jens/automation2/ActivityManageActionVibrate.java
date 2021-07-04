package com.jens.automation2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class ActivityManageActionVibrate extends Activity
{
    TextView etVibratePattern;
    Button bTestVibratePattern, bSaveVibratePattern;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_action_vibrate);

        etVibratePattern = (EditText)findViewById(R.id.etVibratePattern);
        bTestVibratePattern = (Button)findViewById(R.id.bTestVibratePattern);
        bSaveVibratePattern = (Button)findViewById(R.id.bSaveVibratePattern);

        bTestVibratePattern.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(ActivityPermissions.havePermission(Manifest.permission.VIBRATE, ActivityManageActionVibrate.this))
                {
                    String vibrateDurations[] = etVibratePattern.getText().toString().split(",");

                    for(String duration : vibrateDurations)
                    {
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        {
                            vibrator.vibrate(VibrationEffect.createOneShot(Long.parseLong(duration), VibrationEffect.DEFAULT_AMPLITUDE));
                        }
                        else
                        {
                            vibrator.vibrate(Long.parseLong(duration));
                        }
                    }
                }
            }
        });
    }
}