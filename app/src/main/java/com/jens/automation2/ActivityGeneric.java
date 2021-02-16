package com.jens.automation2;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.jens.automation2.AutomationService.LocalBinder;

public class ActivityGeneric extends Activity
{
	public static Intent myServiceIntent = null;
	AutomationService myAutomationService = null;
	boolean boundToService = false;
	
	public void storeServiceReferenceInVariable()
	{
		if(AutomationService.isMyServiceRunning(getApplicationContext()) && myAutomationService == null)
		{
			bindToService();
		}
	}
	
	public void bindToService()
	{
		Log.i("service", "binding to service");
		if(!boundToService)
		{
//			if(myServiceIntent == null)
				myServiceIntent = new Intent(this, AutomationService.class);
			
			Miscellaneous.logEvent("i", "Service", getResources().getString(R.string.logAttemptingToBindToService) + String.valueOf(bindService(myServiceIntent, myServiceConnection, Context.BIND_AUTO_CREATE)), 5);
		}
	}
	public void unBindFromService()
	{
		Miscellaneous.logEvent("i", "Service", getResources().getString(R.string.logAttemptingToUnbindFromService), 5);
		if(boundToService)
		{
			unbindService(myServiceConnection);
			boundToService = false;
			Miscellaneous.logEvent("i", "Service", getResources().getString(R.string.logUnboundFromService), 5);
		}
	}
	
	private ServiceConnection myServiceConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			Miscellaneous.logEvent("i", "Service", getResources().getString(R.string.logBoundToService), 5);
			LocalBinder binder = (LocalBinder)service;
			myAutomationService = binder.getService();
			boundToService = true;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			Miscellaneous.logEvent("i", "Service", getResources().getString(R.string.logUnboundFromService), 5);
			boundToService = false;
		}
	};
}
