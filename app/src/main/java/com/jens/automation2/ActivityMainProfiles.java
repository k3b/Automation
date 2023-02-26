package com.jens.automation2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.jens.automation2.AutomationService.serviceCommands;

public class ActivityMainProfiles extends ActivityGeneric
{
	private Button bAddProfile;
	ListView profileListView;
	
	ArrayAdapter<Profile> profileListViewAdapter;

	AutomationService myAutomationService;
	
	public static Profile profileToEdit;

	protected static ActivityMainProfiles instance = null;

	public static ActivityMainProfiles getInstance()
	{
		if(instance == null)
			instance = new ActivityMainProfiles();

		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Miscellaneous.setDisplayLanguage(ActivityMainProfiles.this);
		setContentView(R.layout.main_profile_layout);

		instance = this;

		bAddProfile = (Button)findViewById(R.id.bAddProfile);
		bAddProfile.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
//				if(!ActivityPermissions.havePermission(ActivityPermissions.writeExternalStoragePermissionName, ActivityMainProfiles.this))
//				{
//					Toast.makeText(ActivityMainProfiles.this, getResources().getString(R.string.appRequiresPermissiontoAccessExternalStorage), Toast.LENGTH_LONG).show();
//					return;
//				}

				profileToEdit = null;
				Intent manageSpecificProfileIntent = new Intent (ActivityMainProfiles.this, ActivityManageProfile.class);
				manageSpecificProfileIntent.putExtra("action", "create");
				startActivityForResult(manageSpecificProfileIntent, 1000);
			}
		});
		
		profileListView = (ListView)findViewById(R.id.lvProfilesList);
		
		profileListViewAdapter = new ArrayAdapter<Profile>(this, R.layout.text_view_for_poi_listview_mediumtextsize, Profile.getProfileCollection());
		profileListView.setClickable(true);
		/*profileListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				profileToEdit = (Profile)profileListViewAdapter.getItem(arg2);
				Intent manageSpecificProfileIntent = new Intent (ActivityMainProfiles.this, ActivityManageSpecificProfile.class);
				manageSpecificProfileIntent.putExtra("action", "change");
				startActivityForResult(manageSpecificProfileIntent, 2000);
			}
		});*/
		profileListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				getProfileDialog((Profile)profileListView.getItemAtPosition(arg2)).show();
				return false;
			}
		});

		if(Settings.executeRulesAndProfilesWithSingleClick)
		{
			profileListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					if(AutomationService.isMyServiceRunning(ActivityMainProfiles.this))
					{
						AutomationService runContext = AutomationService.getInstance();
						if(runContext != null)
						{
							Profile profile = (Profile)profileListView.getItemAtPosition(position);
							profile.activate(runContext);
						}
					}
				}
			});
		}
		
		updateListView();

		this.storeServiceReferenceInVariable();
	}
	
	public void updateListView()
	{
		Miscellaneous.logEvent("i", "ListView", "Attempting to update ProfileListView", 5);
		try
		{
			if(profileListView.getAdapter() == null)
				profileListView.setAdapter(profileListViewAdapter);

			profileListViewAdapter.notifyDataSetChanged();
		}
		catch(NullPointerException e)
		{}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if(AutomationService.isMyServiceRunning(this))
			bindToService();
		
		if(requestCode == 1000) //add Profile
		{
//			profileToEdit = null; //clear cache
			updateListView();
		}
		
		if(requestCode == 2000) //edit Profile
		{
			profileToEdit = null; //clear cache
			updateListView();
		}

		if(boundToService && AutomationService.isMyServiceRunning(this))
		{
			myAutomationService.serviceInterface(serviceCommands.updateNotification); // in case names got changed.
			unBindFromService();
		}	
	}

	private AlertDialog getProfileDialog(final Profile profile)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getResources().getString(R.string.whatToDoWithProfile));
		alertDialogBuilder.setItems(new String[]{ getResources().getString(R.string.runManually), getResources().getString(R.string.edit), getResources().getString(R.string.deleteCapital) }, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch(which)
				{
					case 0:
						if(AutomationService.isMyServiceRunning(ActivityMainProfiles.this))
						{
							AutomationService runContext = AutomationService.getInstance();
							if(runContext != null)
							{
								profile.activate(runContext);
								break;
							}
						}
						Toast.makeText(ActivityMainProfiles.this, getResources().getString(R.string.serviceHasToRunForThat), Toast.LENGTH_LONG).show();
						break;
					case 1:
						profileToEdit = profile;
						Intent manageSpecificProfileIntent = new Intent (ActivityMainProfiles.this, ActivityManageProfile.class);
						manageSpecificProfileIntent.putExtra("action", "change");
						startActivityForResult(manageSpecificProfileIntent, 2000);
						break;
					case 2:
						Rule user = profile.isInUseByRules();
						if(user == null)
						{
							AlertDialog.Builder deleteDialog = new AlertDialog.Builder(ActivityMainProfiles.this);
							deleteDialog.setMessage(getResources().getString(R.string.areYouSure));
							deleteDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialogInterface, int i)
								{
									if (profile.delete(ActivityMainProfiles.this))
										updateListView();
									else
										Toast.makeText(ActivityMainProfiles.this, getResources().getString(R.string.profileCouldNotBeDeleted), Toast.LENGTH_LONG).show();
								}
							});
							deleteDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialogInterface, int i)
								{

								}
							});

							deleteDialog.show();
						}
						else
							Toast.makeText(ActivityMainProfiles.this, String.format(getResources().getString(R.string.ruleXIsUsingProfileY), user.getName(), profile.getName()), Toast.LENGTH_LONG).show();
						break;
				}
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		
		return alertDialog;
	}
	
}
