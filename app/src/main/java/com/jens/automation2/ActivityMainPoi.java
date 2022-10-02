package com.jens.automation2;

import android.Manifest;
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

public class ActivityMainPoi extends ActivityGeneric
{
	private Button bAddPoi;
	ListView poiListView;
	
	ArrayAdapter<PointOfInterest> poiListViewAdapter;

	AutomationService myAutomationService;
	boolean boundToService = false;

	protected static ActivityMainPoi instance = null;

	public static PointOfInterest poiToEdit;

	protected final static int requestCodeForPermission = 1002;

	public static ActivityMainPoi getInstance()
	{
		if(instance == null)
			instance = new ActivityMainPoi();

		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_poi_layout);

		instance = this;

		bAddPoi = (Button)findViewById(R.id.bAddPoi);
		bAddPoi.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				if(Miscellaneous.googleToBlameForLocation(false))
				{
					ActivityMainScreen.openGoogleBlamingWindow();
					return;
				}
				else
				{
					if (!ActivityPermissions.havePermission(Manifest.permission.ACCESS_COARSE_LOCATION, ActivityMainPoi.this) || !ActivityPermissions.havePermission(Manifest.permission.ACCESS_FINE_LOCATION, ActivityMainPoi.this))
					{
						Intent permissionIntent = new Intent(ActivityMainPoi.this, ActivityPermissions.class);

						permissionIntent.putExtra(ActivityPermissions.intentExtraName, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});

						startActivityForResult(permissionIntent, requestCodeForPermission);
					}
					else
					{
						buttonAddPoi();
					}
				}
			}
		});
		
		poiListView = (ListView)findViewById(R.id.lvPoiList);
		
		poiListViewAdapter = new ArrayAdapter<PointOfInterest>(this, R.layout.text_view_for_poi_listview_mediumtextsize, PointOfInterest.getPointOfInterestCollection());
		poiListView.setClickable(true);
		/*poiListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				poiToEdit = (PointOfInterest)poiListViewAdapter.getItem(arg2);
				Intent manageSpecificPoiIntent = new Intent (ActivityMainPoi.this, ActivityManageSpecificPoi.class);
				manageSpecificPoiIntent.putExtra("action", "change");
				startActivityForResult(manageSpecificPoiIntent, 2000);
			}
		});*/
		poiListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				getPoiOptionsDialog((PointOfInterest)poiListView.getItemAtPosition(arg2)).show();
				return false;
			}
		});
		
		updateListView();

		this.storeServiceReferenceInVariable();
	}

	private void buttonAddPoi()
	{
		poiToEdit = null;
		Intent manageSpecificPoiIntent = new Intent(ActivityMainPoi.this, ActivityManagePoi.class);
		manageSpecificPoiIntent.putExtra("action", "create");
		startActivityForResult(manageSpecificPoiIntent, 1000);
	}

	public void updateListView()
	{
		Miscellaneous.logEvent("i", "ListView", "Attempting to update PoiListView", 5);
		try
		{
			if(poiListView.getAdapter() == null)
				poiListView.setAdapter(poiListViewAdapter);

			poiListViewAdapter.notifyDataSetChanged();
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
		
		switch(requestCode)
		{
			case 1000: //add Poi
//				poiToEdit = null; //clear cache
				updateListView();
				break;
			case 2000://edit Poi
				poiToEdit = null; //clear cache
				updateListView();
				break;
			case requestCodeForPermission:
				if(resultCode == RESULT_OK)
					buttonAddPoi();
				break;
		}

		if(boundToService && AutomationService.isMyServiceRunning(this))
		{
			myAutomationService.serviceInterface(serviceCommands.updateNotification); //in case names got changed.
			unBindFromService();
		}	
	}

	private AlertDialog getPoiOptionsDialog(final PointOfInterest pointOfInterest)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getResources().getString(R.string.whatToDoWithPoi));
		alertDialogBuilder.setItems(new String[]{ getResources().getString(R.string.edit), getResources().getString(R.string.deleteCapital) }, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch(which)
				{
					/*case 0:
						if(AutomationService.isMyServiceRunning(ActivityMainPoi.this))
						{
							AutomationService runContext = AutomationService.getInstance();
							if(runContext != null)
							{
								pointOfInterest.activate(runContext);
								break;
							}
						}
						Toast.makeText(ActivityMainPoi.this, getResources().getString(R.string.serviceHasToRunForThat), Toast.LENGTH_LONG).show();
						break;*/
					case 0:
						poiToEdit = pointOfInterest;
						Intent manageSpecificPoiIntent = new Intent (ActivityMainPoi.this, ActivityManagePoi.class);
						manageSpecificPoiIntent.putExtra("action", "change");
						startActivityForResult(manageSpecificPoiIntent, 2000);
						break;
					case 1:
						AlertDialog.Builder deleteDialog = new AlertDialog.Builder(ActivityMainPoi.this);
						deleteDialog.setMessage(getResources().getString(R.string.areYouSure));
						deleteDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialogInterface, int i)
							{
								if(pointOfInterest.delete(Miscellaneous.getAnyContext()))
									updateListView();
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
						break;
				}
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		
		return alertDialog;
	}
	
}
