package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

public class ActivityManageActionCopyToClipboard extends Activity
{
    private Button bSaveCopyToClipboard;
    private EditText etCopyToClipboard;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_manage_action_copy_to_clipboard);

        bSaveCopyToClipboard = (Button) findViewById(R.id.bSaveCopyToClipboard);
        etCopyToClipboard = (EditText)findViewById(R.id.etCopyToClipboard);

        bSaveCopyToClipboard.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(StringUtils.isEmpty(etCopyToClipboard.getText().toString()))
                {
                    Toast.makeText(ActivityManageActionCopyToClipboard.this, getResources().getString(R.string.enterText), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent response = new Intent();
                    response.putExtra(ActivityManageRule.intentNameActionParameter2, etCopyToClipboard.getText().toString());
                    setResult(RESULT_OK, response);
                    finish();
                }
            }
        });

        if(getIntent().hasExtra(ActivityManageRule.intentNameActionParameter2))
        {
            String text = getIntent().getStringExtra(ActivityManageRule.intentNameActionParameter2);
            etCopyToClipboard.setText(text);
        }
    }
}