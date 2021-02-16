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

public class ActivityManageBrightnessSetting extends Activity
{
    CheckBox chkAutoBrightness;
    SeekBar sbBrightness;
    Button bApplyBrightness;
    TextView tvAutoBrightnessNotice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_manage_brightness_setting);
        super.onCreate(savedInstanceState);

        chkAutoBrightness = (CheckBox)findViewById(R.id.chkAutoBrightness);
        sbBrightness = (SeekBar)findViewById(R.id.sbBrightness);
        bApplyBrightness = (Button)findViewById(R.id.bApplyBrightness);
        tvAutoBrightnessNotice = (TextView)findViewById(R.id.tvAutoBrightnessNotice);

        Intent input = getIntent();

        if(input.hasExtra("autoBrightness"))
            chkAutoBrightness.setChecked(input.getBooleanExtra("autoBrightness", false));

        if(input.hasExtra("brightnessValue"))
            sbBrightness.setProgress(input.getIntExtra("brightnessValue", 0));

        bApplyBrightness.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent answer = new Intent();
                answer.putExtra("autoBrightness", chkAutoBrightness.isChecked());
                answer.putExtra("brightnessValue", sbBrightness.getProgress());
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
