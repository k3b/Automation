package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class ActivityManageActionBrightnessSetting extends Activity
{
    public static final String intentNameAutoBrightness = "autoBrightness";
    public static final String intentNameBrightnessValue = "brightnessValue";

    CheckBox chkAutoBrightness;
    SeekBar sbBrightness;
    Button bApplyBrightness;
    TextView tvAutoBrightnessNotice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_manage_action_brightness_settings);
        super.onCreate(savedInstanceState);

        chkAutoBrightness = (CheckBox)findViewById(R.id.chkAutoBrightness);
        sbBrightness = (SeekBar)findViewById(R.id.sbBrightness);
        bApplyBrightness = (Button)findViewById(R.id.bApplyBrightness);
        tvAutoBrightnessNotice = (TextView)findViewById(R.id.tvAutoBrightnessNotice);

        Intent input = getIntent();

        if(input.hasExtra(intentNameAutoBrightness))
            chkAutoBrightness.setChecked(input.getBooleanExtra(intentNameAutoBrightness, false));

        if(input.hasExtra(intentNameBrightnessValue))
            sbBrightness.setProgress(input.getIntExtra(intentNameBrightnessValue, 0));

        bApplyBrightness.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent answer = new Intent();
                answer.putExtra(intentNameAutoBrightness, chkAutoBrightness.isChecked());
                answer.putExtra(intentNameBrightnessValue, sbBrightness.getProgress());
                setResult(RESULT_OK, answer);
                finish();
            }
        });

        chkAutoBrightness.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                    tvAutoBrightnessNotice.setText(R.string.autoBrightnessNotice);
            }
        });
    }
}
