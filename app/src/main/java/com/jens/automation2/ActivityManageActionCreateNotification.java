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

public class ActivityManageActionCreateNotification extends Activity
{
    public static final String intentNameNotificationTitle = "notificationTitle";
    public static final String intentNameNotificationText = "notificationText";

    EditText etNotificationTitle, etNotificationText;
    Button bSaveActionNotification;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Miscellaneous.setDisplayLanguage(this);
        setContentView(R.layout.activity_manage_action_create_notification);

        etNotificationTitle = (EditText) findViewById(R.id.etNotificationTitle);
        etNotificationText = (EditText)findViewById(R.id.etNotificationText);
        bSaveActionNotification = (Button)findViewById(R.id.bSaveActionNotification);

        Intent input = getIntent();

        if(input.hasExtra(intentNameNotificationTitle))
            etNotificationTitle.setText(input.getStringExtra(intentNameNotificationTitle));

        if(input.hasExtra(intentNameNotificationText))
            etNotificationText.setText(input.getStringExtra(intentNameNotificationText));

        bSaveActionNotification.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(StringUtils.isBlank(etNotificationTitle.getText().toString()))
                {
                    Toast.makeText(ActivityManageActionCreateNotification.this, getResources().getString(R.string.enterTitle), Toast.LENGTH_LONG).show();
                    return;
                }

                if(StringUtils.isBlank(etNotificationText.getText().toString()))
                {
                    Toast.makeText(ActivityManageActionCreateNotification.this, getResources().getString(R.string.enterText), Toast.LENGTH_LONG).show();
                    return;
                }

                Intent answer = new Intent();
                answer.putExtra(intentNameNotificationTitle, etNotificationTitle.getText().toString());
                answer.putExtra(intentNameNotificationText, etNotificationText.getText().toString());
                setResult(RESULT_OK, answer);
                finish();
            }
        });
    }
}
