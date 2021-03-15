package com.jens.automation2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.core.text.HtmlCompat;

import com.jens.automation2.AutomationService.serviceCommands;
import com.jens.automation2.Trigger.Trigger_Enum;
import com.jens.automation2.location.LocationProvider;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;

@SuppressLint("NewApi")
public class ActivityMainScreen extends ActivityGeneric
{
	private static boolean guiChangeInProgress = false;

	private static ActivityMainScreen activityMainScreenInstance = null;
	private ToggleButton toggleService, tbLockSound;
	private Button bShowHelp, bPrivacy, bSettingsErase, bSettingsSetToDefault, bVolumeTest, bAddSoundLockTIme;
	private TextView tvActivePoi, tvClosestPoi, tvLastRule, tvMainScreenNote1, tvMainScreenNote2, tvMainScreenNote3, tvlockSoundDuration;

	private ListView lvRuleHistory;
	private ArrayAdapter<Rule> ruleHistoryListViewAdapter;

	private static boolean uiUpdateRunning = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_overview_layout);

		activityMainScreenInstance = this;

		if(ActivityPermissions.needMorePermissions(ActivityMainScreen.this))
		{
			Intent permissionsIntent = new Intent(ActivityMainScreen.this, ActivityPermissions.class);
			startActivityForResult(permissionsIntent, 7000);
		}

		Settings.readFromPersistentStorage(this);

		guiChangeInProgress = true;

		tvActivePoi = (TextView) findViewById(R.id.tvActivePoi);
		tvClosestPoi = (TextView) findViewById(R.id.tvClosestPoi);
		lvRuleHistory = (ListView) findViewById(R.id.lvRuleHistory);
		tvLastRule = (TextView) findViewById(R.id.tvTimeFrameHelpText);
		tvMainScreenNote1 = (TextView) findViewById(R.id.tvMainScreenNote1);
		tvMainScreenNote2 = (TextView) findViewById(R.id.tvMainScreenNote2);
		tvMainScreenNote3 = (TextView) findViewById(R.id.tvMainScreenNote3);
		tvlockSoundDuration = (TextView)findViewById(R.id.tvlockSoundDuration);
		tbLockSound = (ToggleButton) findViewById(R.id.tbLockSound);
		toggleService = (ToggleButton) findViewById(R.id.tbArmMastListener);
		toggleService.setChecked(AutomationService.isMyServiceRunning(this));
		toggleService.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (!ActivityMainScreen.this.uiUpdateRunning)
				{
					if (toggleService.isChecked())
					{
						startAutomationService(getBaseContext(), false);
					} else
					{
						stopAutomationService();
					}
				}
			}
		});

		tvMainScreenNote1.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(ActivityMainScreen.this, ActivityPermissions.class);
				startActivityForResult(intent, ActivityPermissions.requestCodeForPermissions);
			}
		});

		tbLockSound.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				Settings.lockSoundChanges = isChecked;

				if(!isChecked)
				{
					AutomationService.getInstance().nullLockSoundChangesEnd();
					updateMainScreen();
				}

				if (!guiChangeInProgress)
					Settings.writeSettings(ActivityMainScreen.this);
			}
		});

		Button bSettings = (Button) findViewById(R.id.bSettings);
		bSettings.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent myIntent = new Intent(ActivityMainScreen.this, ActivitySettings.class);
				startActivityForResult(myIntent, 6000);
			}
		});

		Button bVolumeTest = (Button) findViewById(R.id.bVolumeTest);
		bVolumeTest.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(ActivityMainScreen.this, ActivityVolumeTest.class);
				startActivity(intent);
			}
		});

		bShowHelp = (Button) findViewById(R.id.bShowHelp);
		bShowHelp.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				Intent showHelpIntent = new Intent(ActivityMainScreen.this, ActivityHelp.class);
				startActivity(showHelpIntent);
			}
		});

		bPrivacy = (Button) findViewById(R.id.bPrivacy);
		bPrivacy.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMainScreen.this);
				builder.setMessage(getResources().getString(R.string.privacyConfirmationText));
				builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						openPrivacyPolicy();
					}
				});
				builder.setNegativeButton(getResources().getString(R.string.no), null);
				builder.create().show();
			}
		});
		
		/*bSettingsErase = (Button)findViewById(R.id.bSettingsErase);
		bSettingsErase.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				getEraseSettingsDialog(ActivityMainScreen.this).show();
			}
		});*/
		bSettingsSetToDefault = (Button) findViewById(R.id.bSettingsSetToDefault);
		bSettingsSetToDefault.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				getDefaultSettingsDialog(ActivityMainScreen.this).show();
			}
		});

		lvRuleHistory.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				v.getParent().requestDisallowInterceptTouchEvent(true);
				return false;
			}
		});

		bAddSoundLockTIme = (Button)findViewById(R.id.bAddSoundLockTIme);
		bAddSoundLockTIme.setText("+" + Settings.lockSoundChangesInterval + " min");
		bAddSoundLockTIme.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(AutomationService.isMyServiceRunning(ActivityMainScreen.this))
                {
                    AutomationService.getInstance().lockSoundChangesEndAddTime();
                    ActivityMainScreen.updateMainScreen();
                }
				else
				    Toast.makeText(ActivityMainScreen.this, getResources().getString(R.string.serviceNotRunning), Toast.LENGTH_LONG).show();
			}
		});

		ruleHistoryListViewAdapter = new ArrayAdapter<Rule>(this, R.layout.text_view_for_poi_listview_mediumtextsize, Rule.getRuleRunHistory());

		if (PointOfInterest.getPointOfInterestCollection() == null | PointOfInterest.getPointOfInterestCollection().size() == 0)
			PointOfInterest.loadPoisFromFile();
		if (Rule.getRuleCollection() == null | Rule.getRuleCollection().size() == 0)
			Rule.readFromFile();

		ActivityMainScreen.updateMainScreen();

		this.storeServiceReferenceInVariable();

		guiChangeInProgress = false;

//		MyGoogleApiClient.start();
	}

	private static AlertDialog getEraseSettingsDialog(final Context context)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(context.getResources().getString(R.string.areYouSure));
		alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (Settings.eraseSettings(context))
					Toast.makeText(context, context.getResources().getString(R.string.settingsErased), Toast.LENGTH_LONG).show();
			}
		});
		alertDialogBuilder.setNegativeButton(context.getResources().getString(R.string.no), null);
		AlertDialog alertDialog = alertDialogBuilder.create();

		return alertDialog;
	}

	private static AlertDialog getDefaultSettingsDialog(final Context context)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(context.getResources().getString(R.string.areYouSure));
		alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (Settings.initializeSettings(context, true))
					Toast.makeText(context, context.getResources().getString(R.string.settingsSetToDefault), Toast.LENGTH_LONG).show();
			}
		});
		alertDialogBuilder.setNegativeButton(context.getResources().getString(R.string.no), null);
		AlertDialog alertDialog = alertDialogBuilder.create();

		return alertDialog;
	}

	public static ActivityMainScreen getActivityMainScreenInstance()
	{
		return activityMainScreenInstance;
	}

	public static void updateMainScreen()
	{
		Miscellaneous.logEvent("i", "MainScreen", "Request to update notification.", 5);

		if (activityMainScreenInstance != null)
		{
			if(ActivityPermissions.needMorePermissions(activityMainScreenInstance))
			{
				activityMainScreenInstance.tvMainScreenNote1.setText(R.string.mainScreenPermissionNote);
				activityMainScreenInstance.tvMainScreenNote1.setVisibility(View.VISIBLE);
			}
			else
			{
				activityMainScreenInstance.tvMainScreenNote1.setText("");
				activityMainScreenInstance.tvMainScreenNote1.setVisibility(View.GONE);
			}

			if(Miscellaneous.restrictedFeaturesConfigured())
			{
				activityMainScreenInstance.tvMainScreenNote2.setText(R.string.settingsReferringToRestrictedFeatures);
				activityMainScreenInstance.tvMainScreenNote2.setVisibility(View.VISIBLE);
			}
			else
			{
				activityMainScreenInstance.tvMainScreenNote2.setText("");
				activityMainScreenInstance.tvMainScreenNote2.setVisibility(View.GONE);
			}

			if (AutomationService.isMyServiceRunning(activityMainScreenInstance))
			{
				Miscellaneous.logEvent("i", "MainScreen", "Service is running. Updating mainscreen with this info.", 5);
				uiUpdateRunning = true;
				activityMainScreenInstance.toggleService.setChecked(true);
				uiUpdateRunning = false;
				//		if(activityMainScreenInstance.hasWindowFocus())
				//		{
				try
				{
					PointOfInterest activePoi = PointOfInterest.getActivePoi();
					if (activePoi == null)
					{
						PointOfInterest closestPoi = PointOfInterest.getClosestPOI(LocationProvider.getInstance().getCurrentLocation());
						activityMainScreenInstance.tvActivePoi.setText("none");
						activityMainScreenInstance.tvClosestPoi.setText(closestPoi.getName());
					}
					else
					{
						activityMainScreenInstance.tvActivePoi.setText(activePoi.getName());
						activityMainScreenInstance.tvClosestPoi.setText("n./a.");
					}
				}
				catch (NullPointerException e)
				{
					if (PointOfInterest.getPointOfInterestCollection().size() > 0)
					{
						if(
								Rule.isAnyRuleUsing(Trigger_Enum.pointOfInterest)
										&&
								ActivityPermissions.havePermission(ActivityPermissions.permissionNameLocationCoarse, AutomationService.getInstance())
										&&
								ActivityPermissions.havePermission(ActivityPermissions.permissionNameLocationFine, AutomationService.getInstance())
						  )
							activityMainScreenInstance.tvActivePoi.setText(activityMainScreenInstance.getResources().getString(R.string.stillGettingPosition));
						else
							activityMainScreenInstance.tvActivePoi.setText(activityMainScreenInstance.getResources().getString(R.string.locationEngineNotActive));

						activityMainScreenInstance.tvClosestPoi.setText("n./a.");
					}
					else
					{
						activityMainScreenInstance.tvActivePoi.setText(activityMainScreenInstance.getResources().getString(R.string.noPoisDefinedShort));
						activityMainScreenInstance.tvClosestPoi.setText("n./a.");
					}
				}

				try
				{
					activityMainScreenInstance.tvLastRule.setText(Rule.getLastActivatedRule().getName() + " " + activityMainScreenInstance.getResources().getString(R.string.at) + " " + Rule.getLastActivatedRuleActivationTime().toLocaleString());
					activityMainScreenInstance.updateListView();
				}
				catch (Exception e)
				{
					activityMainScreenInstance.tvLastRule.setText("n./a.");
				}
			}
			else
			{
				Miscellaneous.logEvent("i", "MainScreen", "Service not running. Updating mainscreen with this info.", 5);
				activityMainScreenInstance.toggleService.setChecked(false);
				activityMainScreenInstance.tvActivePoi.setText(activityMainScreenInstance.getResources().getString(R.string.serviceNotRunning));
				activityMainScreenInstance.tvClosestPoi.setText("");
				activityMainScreenInstance.tvLastRule.setText("");
			}

//			uiUpdateRunning = true;
			if(AutomationService.isMyServiceRunning(ActivityMainScreen.getActivityMainScreenInstance()) && AutomationService.getInstance() != null)
			{
				AutomationService.getInstance().checkLockSoundChangesTimeElapsed();

				Calendar end = AutomationService.getInstance().getLockSoundChangesEnd();
				activityMainScreenInstance.tbLockSound.setChecked(end != null);
				activityMainScreenInstance.tbLockSound.setEnabled(end != null);

				if(end != null)
				{
					Calendar now = Calendar.getInstance();
					long millis = end.getTimeInMillis() - now.getTimeInMillis();
					long minutes = millis/1000/60;
					if(minutes < 60)
						activityMainScreenInstance.tvlockSoundDuration.setText(String.valueOf(minutes + " min..."));
					else
					{
						double hours = (double)minutes / 60.0;
						activityMainScreenInstance.tvlockSoundDuration.setText(String.valueOf(Math.round(hours * 100.0) / 100.0) + " h...");
					}
				}
				else
					activityMainScreenInstance.tvlockSoundDuration.setText(String.valueOf(""));
			}
			else
			{
				activityMainScreenInstance.tbLockSound.setChecked(false);
				activityMainScreenInstance.tbLockSound.setEnabled(false);
				activityMainScreenInstance.tvlockSoundDuration.setText("");
			}
			Settings.writeSettings(activityMainScreenInstance);
//			uiUpdateRunning = false;
//		}
//		else
//			Miscellaneous.logEvent("i", "ActivityMainScreen", "Window doesn't have focus. We're not updating anything.", 5);
		}
		else
			Miscellaneous.logEvent("i", "ActivityMainScreen", "Activity not running. No need to update.", 5);

		if(activityMainScreenInstance != null)
		{
			if(!Settings.hasBeenDone(Settings.constNewsOptInDone))
				newsOptIn();
			else
				activityMainScreenInstance.checkForNews();

			Settings.considerDone(Settings.constNewsOptInDone);
			Settings.writeSettings(Miscellaneous.getAnyContext());
		}
	}

	static void newsOptIn()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(Miscellaneous.getAnyContext());
		builder.setMessage(Miscellaneous.getAnyContext().getResources().getString(R.string.newsOptIn));
		builder.setPositiveButton(Miscellaneous.getAnyContext().getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Settings.displayNewsOnMainScreen = true;
				Settings.writeSettings(Miscellaneous.getAnyContext());
				activityMainScreenInstance.checkForNews();
			}
		});
		builder.setNegativeButton(Miscellaneous.getAnyContext().getResources().getString(R.string.no), null);

		builder.create().show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

//		Miscellaneous.logEvent("i", "ListView", "Notifying ListViewAdapter", 4);

		if (AutomationService.isMyServiceRunning(this))
			bindToService();

		switch (requestCode)
		{
			case ActivityPermissions.requestCodeForPermissions:
				updateMainScreen();
				break;
			case 6000: //settings
				Settings.readFromPersistentStorage(this);

				if (boundToService && AutomationService.isMyServiceRunning(this))
					myAutomationService.serviceInterface(serviceCommands.reloadSettings);

				if(AutomationService.isMyServiceRunning(ActivityMainScreen.this))
					Toast.makeText(this, getResources().getString(R.string.settingsWillTakeTime), Toast.LENGTH_LONG).show();

				break;
		}

		if (AutomationService.isMyServiceRunning(this))
		{
			// Let service reload via binding interface.
			if (boundToService)
			{
				myAutomationService.serviceInterface(serviceCommands.updateNotification); //in case names got changed.
				unBindFromService();
			}
		}
		else
		{
			// Let service reload classically.
			AutomationService service = AutomationService.getInstance();
			if (service != null)
				service.applySettingsAndRules();
		}
	}

	public static void startAutomationService(Context context, boolean startAtBoot)
	{
		try
		{
			if (Rule.getRuleCollection().size() > 0)
			{
				if (!AutomationService.isMyServiceRunning(context))
				{
//					if(myServiceIntent == null)	//do we need that line?????
					myServiceIntent = new Intent(context, AutomationService.class);
					myServiceIntent.putExtra("startAtBoot", startAtBoot);
					context.startService(myServiceIntent);
				} else
					Miscellaneous.logEvent("w", "Service", context.getResources().getString(R.string.logServiceAlreadyRunning), 3);
			} else
			{
				Toast.makeText(context, context.getResources().getString(R.string.serviceWontStart), Toast.LENGTH_LONG).show();
				activityMainScreenInstance.toggleService.setChecked(false);
			}
		}
		catch (NullPointerException ne)
		{
			Toast.makeText(context, context.getResources().getString(R.string.serviceWontStart), Toast.LENGTH_LONG).show();
			activityMainScreenInstance.toggleService.setChecked(false);
		}
		catch (Exception e)
		{
			Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
			activityMainScreenInstance.toggleService.setChecked(false);
		}
	}

	private void stopAutomationService()
	{
		if (myServiceIntent == null)
			myServiceIntent = new Intent(this, AutomationService.class);
		stopService(myServiceIntent);
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
		toggleService.setChecked(AutomationService.isMyServiceRunning(this));
		ActivityMainScreen.updateMainScreen();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		toggleService.setChecked(AutomationService.isMyServiceRunning(this));
		ActivityMainScreen.updateMainScreen();

		if(Build.VERSION.SDK_INT >= 28 && !Settings.noticeAndroid9MicrophoneShown && Rule.isAnyRuleUsing(Trigger_Enum.noiseLevel))
		{
			Settings.noticeAndroid9MicrophoneShown = true;
			Settings.writeSettings(ActivityMainScreen.this);
			Miscellaneous.messageBox(getResources().getString(R.string.app_name), getResources().getString(R.string.android9RecordAudioNotice) + " " + getResources().getString(R.string.messageNotShownAgain), ActivityMainScreen.this).show();
		}

		if(Build.VERSION.SDK_INT >= 29 && !Settings.noticeAndroid10WifiShown && Rule.isAnyRuleUsing(Action.Action_Enum.setWifi))
		{
			Settings.noticeAndroid10WifiShown = true;
			Settings.writeSettings(ActivityMainScreen.this);
			Miscellaneous.messageBox(getResources().getString(R.string.app_name), getResources().getString(R.string.android10WifiToggleNotice) + " " + getResources().getString(R.string.messageNotShownAgain), ActivityMainScreen.this).show();
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		activityMainScreenInstance = null;
	}
	
	private void openPrivacyPolicy()
	{
		String privacyPolicyUrl = "https://server47.de/automation/privacy.html";
		
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl));
        startActivity(browserIntent);
	}

	private void updateListView()
	{
		Miscellaneous.logEvent("i", "ListView", "Attempting to update lvRuleHistory", 4);
		try
		{
			if (lvRuleHistory.getAdapter() == null)
				lvRuleHistory.setAdapter(ruleHistoryListViewAdapter);

			ruleHistoryListViewAdapter.notifyDataSetChanged();
		}
		catch (NullPointerException e)
		{
		}
	}

	public static void showMessageBox(String title, String text)
	{
		Miscellaneous.messageBox(title, text, ActivityMainScreen.getActivityMainScreenInstance());
	}

	synchronized void checkForNews()
	{
		if(Settings.displayNewsOnMainScreen)
		{
			News.AsyncTaskDownloadNews dnTask = new News.AsyncTaskDownloadNews();
			dnTask.execute(ActivityMainScreen.this);
		}
	}

	public void processNewsResult(ArrayList<News> newsToDisplay)
	{
		try
		{
			if (newsToDisplay.size() > 0)
			{
				activityMainScreenInstance.tvMainScreenNote3.setText(HtmlCompat.fromHtml(newsToDisplay.get(0).toStringHtml(), 0));
				activityMainScreenInstance.tvMainScreenNote3.setVisibility(View.VISIBLE);
			}
			else
			{
				activityMainScreenInstance.tvMainScreenNote3.setText("");
				activityMainScreenInstance.tvMainScreenNote3.setVisibility(View.GONE);
			}
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "Error displaying news", Log.getStackTraceString(e), 3);
		}
	}
}