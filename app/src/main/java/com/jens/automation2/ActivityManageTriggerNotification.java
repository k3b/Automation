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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.jens.automation2.Action.Action_Enum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.jens.automation2.Trigger.triggerParameter2Split;

public class ActivityManageTriggerNotification extends Activity
{
	public static Trigger editedNotificationTrigger;
	EditText etNotificationTitle, etNotificationText;
	Button bSelectApp, bSaveTriggerNotification;
	Spinner spinnerTitleDirection, spinnerTextDirection;
	TextView tvSelectedApplication;
	CheckBox chkNotificationDirection;
	boolean edit = false;
	ProgressDialog progressDialog = null;
	
	private static List<PackageInfo> pInfos = null;
	public static Trigger resultingTrigger;

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
		final String[] applicationArray = ActivityManageTriggerNotification.getApplicationNameListString(this);
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
		final String[] packageArray = ActivityManageTriggerNotification.getPackageListString(this, applicationName);
		alertDialogBuilder.setItems(packageArray, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				//getActionStartActivityDialog3(packageArray[which]).show();
				//Miscellaneous.messageBox(getResources().getString(R.string.hint), getResources().getString(R.string.chooseActivityHint), ActivityManageNotificationTrigger.this).show();
				tvSelectedApplication.setText(packageArray[which]);
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		
		return alertDialog;
	}
	private AlertDialog getActionStartActivityDialog3(final String packageName)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getResources().getString(R.string.selectActivityToBeStarted));
		final String activityArray[] = ActivityManageTriggerNotification.getActivityListForPackageName(packageName);
		alertDialogBuilder.setItems(activityArray, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				ActivityInfo ai = ActivityManageTriggerNotification.getActivityInfoForPackageNameAndActivityName(packageName, activityArray[which]);
				tvSelectedApplication.setText(ai.packageName + ";" + ai.name);
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		
		return alertDialog;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_trigger_notification);

		etNotificationTitle = (EditText)findViewById(R.id.etNotificationTitle);
		etNotificationText = (EditText)findViewById(R.id.etNotificationText);
		bSelectApp = (Button)findViewById(R.id.bSelectApp);
		bSaveTriggerNotification = (Button)findViewById(R.id.bSaveTriggerNotification);
		spinnerTitleDirection = (Spinner)findViewById(R.id.spinnerTitleDirection);
		spinnerTextDirection = (Spinner)findViewById(R.id.spinnerTextDirection);
		tvSelectedApplication = (TextView)findViewById(R.id.tvSelectedApplication);
		chkNotificationDirection = (CheckBox)findViewById(R.id.chkNotificationDirection);

		directions = new String[] {
									getResources().getString(R.string.directionStringEquals),
									getResources().getString(R.string.directionStringContains),
									getResources().getString(R.string.directionStringStartsWith),
									getResources().getString(R.string.directionStringEndsWith),
									getResources().getString(R.string.directionStringNotEquals)
								};

		directionSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.text_view_for_poi_listview_mediumtextsize, ActivityManageTriggerNotification.directions);
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
				progressDialog = ProgressDialog.show(ActivityManageTriggerNotification.this, "", ActivityManageTriggerNotification.this.getResources().getString(R.string.gettingListOfInstalledApplications));
			}
		});

		chkNotificationDirection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if(isChecked)
					chkNotificationDirection.setText(getResources().getString(R.string.notificationAppears));
				else
					chkNotificationDirection.setText(getResources().getString(R.string.notificationDisappears));
			}
		});
		
		bSaveTriggerNotification.setOnClickListener(new OnClickListener()
		{		
			@Override
			public void onClick(View v)
			{
				String app;
				if(tvSelectedApplication.getText().toString().equalsIgnoreCase(getResources().getString(R.string.anyApp)))
					app = "-1";
				else
					app = tvSelectedApplication.getText().toString();

				String titleDir = Trigger.getMatchCode(spinnerTitleDirection.getSelectedItem().toString());
				String title = etNotificationTitle.getText().toString();
				String textDir = Trigger.getMatchCode(spinnerTextDirection.getSelectedItem().toString());
				String text = etNotificationText.getText().toString();

				if(edit)
				{
					editedNotificationTrigger.setTriggerParameter(chkNotificationDirection.isChecked());
					editedNotificationTrigger.setTriggerParameter2(app + triggerParameter2Split + titleDir + triggerParameter2Split + title + triggerParameter2Split + textDir + triggerParameter2Split + text);
					ActivityManageTriggerNotification.this.setResult(RESULT_OK);
				}
				else
				{
					Intent data = new Intent();
					data.putExtra("direction", chkNotificationDirection.isChecked());
					data.putExtra("app", app);
					data.putExtra("titleDir", titleDir);
					data.putExtra("title", title);
					data.putExtra("textDir", textDir);
					data.putExtra("text", text);
					ActivityManageTriggerNotification.this.setResult(RESULT_OK, data);
				}

				finish();
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
		chkNotificationDirection.setChecked(editedNotificationTrigger.getTriggerParameter());

		String[] params = editedNotificationTrigger.getTriggerParameter2().split(triggerParameter2Split);

		String app = params[0];
		String titleDir = params[1];
		String title = params[2];
		String textDir = params[3];
		String text;
		if (params.length >= 5)
			text = params[4];
		else
			text = "";

		if(!app.equals("-1"))
			tvSelectedApplication.setText(app);

		for(int i = 0; i < directions.length; i++)
		{
			if(Trigger.getMatchCode(directions[i]).equalsIgnoreCase(titleDir))
				spinnerTitleDirection.setSelection(i);

			if(Trigger.getMatchCode(directions[i]).equalsIgnoreCase(textDir))
				spinnerTextDirection.setSelection(i);
		}

		if(title.length() > 0)
			etNotificationTitle.setText(title);

		if(text.length() > 0)
			etNotificationText.setText(text);
	}
	
	private class GetActivityListTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			getActivityList(ActivityManageTriggerNotification.this);
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
