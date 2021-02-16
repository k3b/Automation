package com.jens.automation2.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.R;
import com.jens.automation2.Settings;

public class StartupIntentReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Settings.readFromPersistentStorage(context);

		Miscellaneous.logEvent("i", "Boot event", "Received event: " + intent.getAction(), 5);

		if(Settings.startServiceAtSystemBoot)
		{
			Miscellaneous.logEvent("i", "Service", context.getResources().getString(R.string.logStartingServiceAtPhoneBoot), 1);
//			Settings.readFromPersistentStorage(context);
			AutomationService.startAutomationService(context, true);
		}
		else
		{
			Miscellaneous.logEvent("i", "Service", context.getResources().getString(R.string.logNotStartingServiceAtPhoneBoot), 2);
		}
	}

}
