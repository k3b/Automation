package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.List;

public class ActivityManageTriggerProfile extends Activity
{
    public static final String profileFieldName = "profileName";

    boolean editMode = false;

    Button bSaveTriggerProfile;
    Spinner spinnerProfiles;
    CheckBox chkProfileActive, chkProfileCheckSettings;

    ArrayAdapter<Profile> profileSpinnerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_trigger_profile);

        bSaveTriggerProfile = (Button)findViewById(R.id.bSaveTriggerProfile);
        spinnerProfiles = (Spinner)findViewById(R.id.spinnerProfiles);
        chkProfileActive = (CheckBox)findViewById(R.id.chkProfileActive);
        chkProfileCheckSettings = (CheckBox)findViewById(R.id.chkProfileCheckSettings);

        try
        {
            profileSpinnerAdapter = new ArrayAdapter<Profile>(this, R.layout.text_view_for_poi_listview_mediumtextsize, Profile.getProfileCollection());
            loadProfileItems();
        }
        catch (Exception e)
        {
            Miscellaneous.logEvent("w", "ActivityManageTriggerProfile", Log.getStackTraceString(e), 1);
        }

        if(getIntent().hasExtra(ActivityManageRule.intentNameTriggerParameter2))
        {
            editMode = true;

            boolean active = getIntent().getBooleanExtra(ActivityManageRule.intentNameTriggerParameter1, true);
            chkProfileActive.setChecked(active);

            try
            {
                String values[] = getIntent().getStringExtra(ActivityManageRule.intentNameTriggerParameter2).split(Trigger.triggerParameter2Split);
                if(values.length >= 2)
                {
                    boolean checkSettings = Boolean.parseBoolean(values[0]);
                    chkProfileCheckSettings.setChecked(checkSettings);

                    String profileName = values[0];

                    List<Profile> profileList = Profile.getProfileCollection();
                    for(int i = 0; i < profileList.size(); i++)
                    {
                        if(profileList.get(i).getName().equals(profileName))
                        {
                            spinnerProfiles.setSelection(i);
                            break;
                        }
                    }
                }
            }
            catch(Exception e)
            {
                Toast.makeText(ActivityManageTriggerProfile.this, getResources().getString(R.string.triggerWrong), Toast.LENGTH_SHORT).show();
                Miscellaneous.logEvent("e", "ActivityManageTriggerProfile", "There\'s something wrong with parameters. Content: " + getIntent().getStringExtra(ActivityManageRule.intentNameActionParameter2) + ", " + Log.getStackTraceString(e), 1);
            }
        }

        bSaveTriggerProfile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent returnData = new Intent();
                returnData.putExtra(ActivityManageRule.intentNameTriggerParameter1, chkProfileActive.isChecked());
                returnData.putExtra(ActivityManageRule.intentNameTriggerParameter2,
                                spinnerProfiles.getSelectedItem().toString() + Trigger.triggerParameter2Split +
                                chkProfileCheckSettings.isChecked());

                setResult(RESULT_OK, returnData);
                finish();
            }
        });
    }

    private void loadProfileItems()
    {
        try
        {
            if(spinnerProfiles.getAdapter() == null)
                spinnerProfiles.setAdapter(profileSpinnerAdapter);

            profileSpinnerAdapter.notifyDataSetChanged();
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
        }
    }
}