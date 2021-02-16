package com.jens.automation2;

import android.annotation.SuppressLint;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.jens.automation2.receivers.NfcReceiver;


@SuppressLint("NewApi")
public class ActivityMainTabLayout extends TabActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tab_layout);
		
		TabHost tabHost = getTabHost();
		
		TabSpec specOverview = tabHost.newTabSpec("overview");
		specOverview.setIndicator(getResources().getString(R.string.overview), getResources().getDrawable(R.drawable.icon_overview_tab));
		Intent overviewIntent = new Intent(this, ActivityMainScreen.class);
		specOverview.setContent(overviewIntent);
		
		TabSpec specPoi = tabHost.newTabSpec("pois");
		specPoi.setIndicator(getResources().getString(R.string.pois), getResources().getDrawable(R.drawable.map));
		Intent mainPoiIntent = new Intent(this, ActivityMainPoi.class);
		specPoi.setContent(mainPoiIntent);
		
		TabSpec specRules = tabHost.newTabSpec("rules");
		specRules.setIndicator(getResources().getString(R.string.rules), getResources().getDrawable(R.drawable.gear));
		Intent mainRulesIntent = new Intent(this, ActivityMainRules.class);
		specRules.setContent(mainRulesIntent);	
		
		TabSpec specProfiles = tabHost.newTabSpec("profiles");
		specProfiles.setIndicator(getResources().getString(R.string.profiles), getResources().getDrawable(R.drawable.sound));
		Intent mainProfilesIntent = new Intent(this, ActivityMainProfiles.class);
		specProfiles.setContent(mainProfilesIntent);		
				
		tabHost.addTab(specOverview);
		tabHost.addTab(specPoi);
		tabHost.addTab(specRules);
		tabHost.addTab(specProfiles);

		tabHost.setCurrentTab(Settings.startScreen);
	}


	@Override
	protected void onResume()
	{
		super.onResume();
//		Miscellaneous.logEvent("i", "NFC", "ActivityMainTabLayout.onResume().", 5);
		NfcReceiver.checkIntentForNFC(this, getIntent());
//		NfcReceiver.checkIntentForNFC(this, new Intent(this.getApplicationContext(), this.getClass()));
	}

	@Override
	protected void onNewIntent(Intent intent)
	{		
//		Miscellaneous.logEvent("i", "NFC", "ActivityMainTabLayout.onNewIntent().", 5);
//	    setIntent(intent);
		NfcReceiver.checkIntentForNFC(this, intent);
	}	
}
