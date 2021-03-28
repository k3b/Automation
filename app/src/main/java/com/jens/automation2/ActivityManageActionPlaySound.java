package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class ActivityManageActionPlaySound extends Activity
{
    final static int PICKFILE_RESULT_CODE = 4711;

    CheckBox chkPlaySoundAlwaysPlay;
    TextView tvSelectedSoundFile;
    Button bSelectSoundFile, bSavePlaySound;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_play_sound);

        chkPlaySoundAlwaysPlay = (CheckBox)findViewById(R.id.chkPlaySoundAlwaysPlay);
        tvSelectedSoundFile = (TextView)findViewById(R.id.tvSelectedSoundFile);
        bSelectSoundFile = (Button)findViewById(R.id.bSelectSoundFile);
        bSavePlaySound = (Button)findViewById(R.id.bSavePlaySound);

        boolean edit = getIntent().getBooleanExtra("edit", false);

        if(edit)
        {
            boolean param1 = getIntent().getBooleanExtra("actionParameter1", false);
            String param2 = getIntent().getStringExtra("actionParameter2");
            chkPlaySoundAlwaysPlay.setChecked(param1);
            tvSelectedSoundFile.setText(param2);
        }

        bSelectSoundFile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Need to check for storage permissions
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, getResources().getString(R.string.selectSoundFile));
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });

        bSavePlaySound.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                savePlaySoundSettings();
            }
        });
    }

    void savePlaySoundSettings()
    {
        if(tvSelectedSoundFile.getText().toString() == null || tvSelectedSoundFile.getText().toString().length() == 0)
        {
            Toast.makeText(ActivityManageActionPlaySound.this, getResources().getString(R.string.selectSoundFile), Toast.LENGTH_LONG).show();
            return;
        }

        Intent returnData = new Intent();
        returnData.putExtra("actionParameter1", chkPlaySoundAlwaysPlay.isChecked());
        returnData.putExtra("actionParameter2", tvSelectedSoundFile.getText().toString());

        setResult(RESULT_OK, returnData);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            if(requestCode == PICKFILE_RESULT_CODE)
            {
                Uri fileUri = data.getData();
                String filePath = fileUri.getPath();
                tvSelectedSoundFile.setText(filePath);
            }
        }
    }
}
