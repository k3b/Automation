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

public class ActivityManageActionWifi extends Activity
{
    CheckBox chkWifiRunAsRoot;
    RadioButton rbActionWifiOn, rbActionWifiOff;
    Button bActionWifiSave;
    TextView tvWifiExplanation1, tvWifiExplanation2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_action_wifi);

        chkWifiRunAsRoot = (CheckBox)findViewById(R.id.chkWifiRunAsRoot);
        rbActionWifiOn = (RadioButton) findViewById(R.id.rbActionWifiOn);
        rbActionWifiOff = (RadioButton)findViewById(R.id.rbActionWifiOff);
        bActionWifiSave = (Button) findViewById(R.id.bActionWifiSave);
        tvWifiExplanation1 = (TextView)findViewById(R.id.tvWifiExplanation1);
        tvWifiExplanation2 = (TextView)findViewById(R.id.tvWifiExplanation2);

        Intent input = getIntent();
        if(input.hasExtra(ActivityManageRule.intentNameActionParameter1))
            rbActionWifiOn.setChecked(input.getBooleanExtra(ActivityManageRule.intentNameActionParameter1, true));

        if(input.hasExtra(ActivityManageRule.intentNameActionParameter2))
            chkWifiRunAsRoot.setChecked(Boolean.parseBoolean(input.getStringExtra(ActivityManageRule.intentNameActionParameter2)));

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//            Miscellaneous.messageBox(getResources().getString(R.string.app_name), getResources().getString(R.string.android10WifiToggleNotice), ActivityManageActionWifi.this).show();

        if(getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.Q)
            tvWifiExplanation1.setVisibility(View.VISIBLE);
        else
            tvWifiExplanation1.setVisibility(View.GONE);

        bActionWifiSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent response = new Intent();
                response.putExtra(ActivityManageRule.intentNameActionParameter1, rbActionWifiOn.isChecked());
                response.putExtra(ActivityManageRule.intentNameActionParameter2, String.valueOf(chkWifiRunAsRoot.isChecked()));
                setResult(RESULT_OK, response);
                finish();
            }
        });
    }
}
