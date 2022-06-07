package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class ActivityManageActionRunExecutable extends Activity
{
    final static int PICKFILE_RESULT_CODE = 4711;

    CheckBox chkRunExecAsRoot;
    EditText etRunExecutablePath, etRunExecutableParameters;
    Button bChooseExecutable, bSaveActionRunExec;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_action_run_executable);

        chkRunExecAsRoot = (CheckBox)findViewById(R.id.chkRunExecAsRoot);
        etRunExecutablePath = (EditText) findViewById(R.id.etRunExecutablePath);
        etRunExecutableParameters = (EditText) findViewById(R.id.etRunExecutableParameters);
        bChooseExecutable = (Button) findViewById(R.id.bChooseExecutable);
        bSaveActionRunExec = (Button) findViewById(R.id.bSaveActionRunExec);

        bChooseExecutable.setOnClickListener(new View.OnClickListener()
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

        bSaveActionRunExec.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                saveExecSettings();
            }
        });
    }

    void saveExecSettings()
    {
        if(etRunExecutablePath.getText().toString() == null || etRunExecutablePath.getText().toString().length() == 0)
        {
            Toast.makeText(ActivityManageActionRunExecutable.this, getResources().getString(R.string.selectValidExecutable), Toast.LENGTH_LONG).show();
            return;
        }
        else
        {
            File executableFile = new File(etRunExecutablePath.getText().toString());
            if(!executableFile.exists())
            {
                Toast.makeText(ActivityManageActionRunExecutable.this, getResources().getString(R.string.fileDoesNotExist), Toast.LENGTH_LONG).show();
                return;
            }
            else
            {
                if(!chkRunExecAsRoot.isChecked() && !executableFile.canExecute())
                {
                    Toast.makeText(ActivityManageActionRunExecutable.this, getResources().getString(R.string.fileNotExecutable), Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        Intent returnData = new Intent();
        returnData.putExtra(ActivityManageRule.intentNameActionParameter1, chkRunExecAsRoot.isChecked());

        if(etRunExecutableParameters.getText() != null && !StringUtils.isEmpty(etRunExecutableParameters.getText().toString()))
            returnData.putExtra(ActivityManageRule.intentNameActionParameter2, etRunExecutablePath.getText().toString() + Action.actionParameter2Split + etRunExecutableParameters.getText().toString());
        else
            returnData.putExtra(ActivityManageRule.intentNameActionParameter2, etRunExecutablePath.getText().toString());

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
                String filePath = CompensateCrappyAndroidPaths.getPath(ActivityManageActionRunExecutable.this, fileUri);
                etRunExecutablePath.setText(filePath);
            }
        }
    }
}