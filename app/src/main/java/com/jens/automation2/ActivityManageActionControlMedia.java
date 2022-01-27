package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class ActivityManageActionControlMedia extends Activity
{
    RadioButton rbMediaPlayPause, rbMediaPlay, rbMediaPause, rbMediaStop, rbMediaPrevious, rbMediaNext;
    Button bSaveControlMediaAction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_action_control_media);

        rbMediaPlayPause = (RadioButton)findViewById(R.id.rbMediaPlayPause);
        rbMediaPlay = (RadioButton)findViewById(R.id.rbMediaPlay);
        rbMediaPause = (RadioButton)findViewById(R.id.rbMediaPause);
        rbMediaStop = (RadioButton)findViewById(R.id.rbMediaStop);
        rbMediaPrevious = (RadioButton)findViewById(R.id.rbMediaPrevious);
        rbMediaNext = (RadioButton)findViewById(R.id.rbMediaNext);

        bSaveControlMediaAction = (Button)findViewById(R.id.bSaveControlMediaAction);

        bSaveControlMediaAction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(checkInput())
                {
                    Intent answer = new Intent();

                    if(rbMediaPlayPause.isChecked())
                        answer.putExtra(ActivityManageRule.intentNameActionParameter2, "0");
                    else if(rbMediaPlay.isChecked())
                        answer.putExtra(ActivityManageRule.intentNameActionParameter2, "1");
                    else if(rbMediaPause.isChecked())
                        answer.putExtra(ActivityManageRule.intentNameActionParameter2, "2");
                    else if(rbMediaStop.isChecked())
                        answer.putExtra(ActivityManageRule.intentNameActionParameter2, "3");
                    else if(rbMediaPrevious.isChecked())
                        answer.putExtra(ActivityManageRule.intentNameActionParameter2, "4");
                    else if(rbMediaNext.isChecked())
                        answer.putExtra(ActivityManageRule.intentNameActionParameter2, "5");

                    setResult(RESULT_OK, answer);
                    finish();
                }
            }
        });

        Intent input = getIntent();

        if(input.hasExtra(ActivityManageRule.intentNameActionParameter2))
        {
            String existing = input.getStringExtra(ActivityManageRule.intentNameActionParameter2);
            switch (existing)
            {
                case "0":
                    rbMediaPlayPause.setChecked(true);
                    break;
                case "1":
                    rbMediaPlay.setChecked(true);
                    break;
                case "2":
                    rbMediaPause.setChecked(true);
                    break;
                case "3":
                    rbMediaStop.setChecked(true);
                    break;
                case "4":
                    rbMediaPrevious.setChecked(true);
                    break;
                case "5":
                    rbMediaNext.setChecked(true);
                    break;
            }
        }
    }

    boolean checkInput()
    {
        if(
                !rbMediaPlayPause.isChecked()
                    &&
                !rbMediaPlay.isChecked()
                    &&
                !rbMediaPause.isChecked()
                    &&
                !rbMediaStop.isChecked()
                    &&
                !rbMediaPrevious.isChecked()
                    &&
                !rbMediaNext.isChecked()
        )
        {
            Toast.makeText(ActivityManageActionControlMedia.this, getResources().getString(R.string.pleaseSelectActionValue), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}