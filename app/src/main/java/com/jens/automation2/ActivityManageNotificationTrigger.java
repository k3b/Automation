package com.jens.automation2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jens.automation2.Action.Action_Enum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ActivityManageNotificationTrigger extends Activity
{
	EditText etNotificationTitle, etNotificationText;
	Button bSelectApp, bSaveTriggerNotification;
	Spinner spinnerTitleDirection, spinnerTextDirection;
	TextView tvSelectedActivity;
	boolean edit = false;
	ProgressDialog progressDialog = null;
	
	private static List<PackageInfo> pInfos = null;
	public static Action resultingAction;

	private static String[] directions;

	ArrayAdapter<String> directionSpinnerAdapter;
	
	public static void getActivityList(final Context context)
	{
		if(pInfos == null)
		{
			pInfos = context.getPackageManager().getInstalledPackages(PackageManager.GET_ACTIVITIES);
			Collections.sort(pInfos, new Comparator<PackageInfo>()
			{
			    public int compare(PackageInfo obj1, PackageInfo obj2)
			    {
			    	String name1 = "";
					String name2 = "";

					ApplicationInfo aInfo1 = obj1.applicationInfo;
					if (aInfo1 != null)
					{
						name1 = (String) context.getPackageManager().getApplicationLabel(aInfo1);
					}
					ApplicationInfo aInfo2 = obj2.applicationInfo;
					if (aInfo2 != null)
					{
						name2 = (String) context.getPackageManager().getApplicationLabel(aInfo2);
					}

					return name1.compareTo(name2);
			    }
			});
		}
	}
 
	public static String[] getApplicationNameListString(Context myContext)
	{
		// Generate the actual list
		getActivityList(myContext);
		
		ArrayList<String> returnList = new ArrayList<String>();
		
		for (PackageInfo pInfo : pInfos)
		{
			ApplicationInfo aInfo = pInfo.applicationInfo;
			if (aInfo != null)
			{
				String aLabel;
	
				aLabel = (String) myContext.getPackageManager().getApplicationLabel(aInfo);

				ActivityInfo[] aInfos = pInfo.activities;
				if (aInfos != null && aInfos.length > 0)		// Only put Applications into the list that have packages.
				{
					if(!returnList.contains(aLabel))
						returnList.add(aLabel);
				}
			}
		}
		
		return returnList.toArray(new String[returnList.size()]);
	}

	public static String[] getPackageListString(Context myContext, String applicationLabel)
	{
		// Generate the actual list
		getActivityList(myContext);
		
		ArrayList<String> returnList = new ArrayList<String>();
		
		for (PackageInfo pInfo : pInfos)
		{
			if(myContext.getPackageManager().getApplicationLabel(pInfo.applicationInfo).equals(applicationLabel))
			{
				ActivityInfo[] aInfos = pInfo.activities;
				if (aInfos != null && aInfos.length > 0)
				{
					returnList.add(pInfo.packageName);
				}
			}
		}
		
		return returnList.toArray(new String[returnList.size()]);
	}

	public static String[] getPackageListString(Context myContext)
	{
		// Generate the actual list
		getActivityList(myContext);
		
		ArrayList<String> returnList = new ArrayList<String>();
		
		for (PackageInfo pInfo : pInfos)
		{
			ActivityInfo[] aInfos = pInfo.activities;
			if (aInfos != null && aInfos.length > 0)
			{
				returnList.add(pInfo.packageName);
			}
			else
				Miscellaneous.logEvent("w", "Empty Application", "Application " + myContext.getPackageManager().getApplicationLabel(pInfo.applicationInfo) + " doesn\'t have packages.", 5);
		}
		
		return returnList.toArray(new String[returnList.size()]);
	}
	
	public static String[] getActivityListForPackageName(String packageName)
	{
		ArrayList<String> returnList = new ArrayList<String>();
		
		for (PackageInfo pInfo : pInfos)
		{
			if(pInfo.packageName.equals(packageName))
			{
				ActivityInfo[] aInfos = pInfo.activities;
				if (aInfos != null)
				{
					for (ActivityInfo activityInfo : aInfos)
					{
						returnList.add(activityInfo.name);
					}
				}
			}
		}
		
		return returnList.toArray(new String[returnList.size()]);
	}
	
	public static ActivityInfo getActivityInfoForPackageNameAndActivityName(String packageName, String activityName)
	{
		for (PackageInfo pInfo : pInfos)
		{
			if(pInfo.packageName.equals(packageName))
			{
				ActivityInfo[] aInfos = pInfo.activities;
				if (aInfos != null)
				{
					for (ActivityInfo activityInfo : aInfos)
					{
						if(activityInfo.name.equals(activityName))
							return activityInfo;
					}
				}
			}
		}
		
		return null;
	}
	
	private AlertDialog getActionStartActivityDialog1()
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getResources().getString(R.string.selectApplication));
		final String[] applicationArray = ActivityManageNotificationTrigger.getApplicationNameListString(this);
		alertDialogBuilder.setItems(applicationArray, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				getActionStartActivityDialog2(applicationArray[which]).show();
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		
		return alertDialog;
	}
	private AlertDialog getActionStartActivityDialog2(String applicationName)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getResources().getString(R.string.selectPackageOfApplication));
		final String[] packageArray = ActivityManageNotificationTrigger.getPackageListString(this, applicationName);
		alertDialogBuilder.setItems(packageArray, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				//getActionStartActivityDialog3(packageArray[which]).show();
				//Miscellaneous.messageBox(getResources().getString(R.string.hint), getResources().getString(R.string.chooseActivityHint), ActivityManageNotificationTrigger.this).show();
				tvSelectedActivity.setText(packageArray[which]);
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		
		return alertDialog;
	}
	private AlertDialog getActionStartActivityDialog3(final String packageName)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getResources().getString(R.string.selectActivityToBeStarted));
		final String activityArray[] = ActivityManageNotificationTrigger.getActivityListForPackageName(packageName);
		alertDialogBuilder.setItems(activityArray, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				ActivityInfo ai = ActivityManageNotificationTrigger.getActivityInfoForPackageNameAndActivityName(packageName, activityArray[which]);
				tvSelectedActivity.setText(ai.packageName + ";" + ai.name);
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		
		return alertDialog;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trigger_notification);

		etNotificationTitle = (EditText)findViewById(R.id.etNotificationTitle);
		etNotificationText = (EditText)findViewById(R.id.etNotificationText);
		bSelectApp = (Button)findViewById(R.id.bSelectApp);
		bSaveTriggerNotification = (Button)findViewById(R.id.bSaveTriggerNotification);
		spinnerTitleDirection = (Spinner)findViewById(R.id.spinnerTitleDirection);
		spinnerTextDirection = (Spinner)findViewById(R.id.spinnerTextDirection);
		tvSelectedActivity = (TextView)findViewById(R.id.tvSelectedActivity);

		directions = new String[] {
									getResources().getString(R.string.directionStringEquals),
									getResources().getString(R.string.directionStringContains),
									getResources().getString(R.string.directionStringStartsWidth),
									getResources().getString(R.string.directionStringEndsWith),
									getResources().getString(R.string.directionStringNotEquals)
								};

		directionSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.text_view_for_poi_listview_mediumtextsize, ActivityManageNotificationTrigger.directions);
		spinnerTitleDirection.setAdapter(directionSpinnerAdapter);
		spinnerTextDirection.setAdapter(directionSpinnerAdapter);
		directionSpinnerAdapter.notifyDataSetChanged();
		
		bSelectApp.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				GetActivityListTask getActivityListTask = new GetActivityListTask();
				getActivityListTask.execute();
				progressDialog = ProgressDialog.show(ActivityManageNotificationTrigger.this, "", ActivityManageNotificationTrigger.this.getResources().getString(R.string.gettingListOfInstalledApplications));
			}
		});
		
		bSaveTriggerNotification.setOnClickListener(new OnClickListener()
		{		
			@Override
			public void onClick(View v)
			{
				if(saveAction())
				{
					String app = tvSelectedActivity.getText().toString();
					String titleDir = spinnerTitleDirection.getSelectedItem().toString();
					String title = etNotificationTitle.getText().toString();
					String textDir = spinnerTextDirection.getSelectedItem().toString();
					String text = etNotificationText.getText().toString();
					ActivityManageNotificationTrigger.this.setResult(RESULT_OK);
					finish();
				}
			}
		});

		Intent i = getIntent();
		if(i.getBooleanExtra("edit", false) == true)
		{
			edit = true;
			loadValuesIntoGui();
		}
	}
	
	private void loadValuesIntoGui()
	{
//		String[] params = resultingAction.getParameter2().split(";");
//		if(params.length >= 2)
//		{
//			tvSelectedActivity.setText(params[0] + ";" + params[1]);
//
//			if(params.length > 2)
//			{
//				intentPairList.clear();
//
//				for(int i=2; i<params.length; i++)
//				{
//					intentPairList.add(params[i]);
//				}
//
//				updateIntentPairList();
//			}
//		}
	}
	
	private boolean saveAction()
	{
		if(tvSelectedActivity.getText().toString().length() == 0)
		{
			Toast.makeText(ActivityManageNotificationTrigger.this, getResources().getString(R.string.selectApplication), Toast.LENGTH_LONG).show();
			return false;
		}

		if(tvSelectedActivity.getText().toString().equals(getResources().getString(R.string.selectApplication)))
		{
			Toast.makeText(this, getResources().getString(R.string.selectApplication), Toast.LENGTH_LONG).show();
			return false;
		}
		
		if(resultingAction == null)
			resultingAction = new Action();
		
		resultingAction.setAction(Action_Enum.startOtherActivity);
		
		String parameter2 = tvSelectedActivity.getText().toString();
		
		resultingAction.setParameter2(parameter2);
		
		return true;
	}
	
	private class GetActivityListTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			getActivityList(ActivityManageNotificationTrigger.this);
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			progressDialog.dismiss();
			getActionStartActivityDialog1().show();
		}
		
		
	}
}
