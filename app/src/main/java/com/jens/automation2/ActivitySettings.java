package com.jens.automation2;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

import com.jens.automation2.R.layout;

public class ActivitySettings extends PreferenceActivity
{
	ListPreference lpStartScreenOptionsValues;
	CheckBoxPreference chkPrefUpdateCheck;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(layout.activity_settings);

		if(BuildConfig.FLAVOR.equals(AutomationService.flavor_name_apk))
		{
			chkPrefUpdateCheck = (CheckBoxPreference) findPreference("automaticUpdateCheck");
			chkPrefUpdateCheck.setEnabled(true);
		}
	}
}
