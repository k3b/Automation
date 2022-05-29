package com.jens.automation2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

public class ActivityManageActionSendBroadcast extends Activity
{
    EditText etBroadcastToSend;
    Button bBroadcastSendShowSuggestions, bSaveSendBroadcast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_action_send_broadcast);

        etBroadcastToSend = (EditText)findViewById(R.id.etBroadcastToSend);
        bBroadcastSendShowSuggestions = (Button)findViewById(R.id.bBroadcastSendShowSuggestions);
        bSaveSendBroadcast = (Button)findViewById(R.id.bSaveSendBroadcast);

        bSaveSendBroadcast.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(checkInput())
                {
                    Intent answer = new Intent();
                    answer.putExtra(ActivityManageRule.intentNameActionParameter2, etBroadcastToSend.getText().toString());
                    setResult(RESULT_OK, answer);
                    finish();
                }
            }
        });

        bBroadcastSendShowSuggestions.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityManageActionSendBroadcast.this);
                builder.setTitle(getResources().getString(R.string.selectBroadcast));
                builder.setItems(ActivityManageTriggerBroadcast.broadcastSuggestions, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which)
                    {
                        etBroadcastToSend.setText(ActivityManageTriggerBroadcast.broadcastSuggestions[which]);
                    }
                });
                builder.create().show();
            }
        });

        Intent input = getIntent();

        if(input.hasExtra(ActivityManageRule.intentNameActionParameter2))
            etBroadcastToSend.setText(input.getStringExtra(ActivityManageRule.intentNameActionParameter2));
    }

    boolean checkInput()
    {
        String broadcastToSend = etBroadcastToSend.getText().toString();
        if(StringUtils.isEmpty(broadcastToSend))
        {
            Toast.makeText(ActivityManageActionSendBroadcast.this, getResources().getString(R.string.enterBroadcast), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}