package com.jens.automation2;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class ActivityDisplayLongMessage extends Activity
{
    TextView tvLongMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_long_message);

        tvLongMessage = (TextView)findViewById(R.id.tvLongMessage);

        tvLongMessage.setText(getIntent().getStringExtra("longMessage"));
    }
}
