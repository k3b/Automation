package com.jens.automation2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class ActivityManagePoi extends Activity
{
	public LocationManager myLocationManager;
	MyLocationListenerGps myLocationListenerGps = new MyLocationListenerGps();
	Location locationGps = null, locationNetwork = null;
//	Location locationWifi = null;
	MyLocationListenerNetwork myLocationListenerNetwork = new MyLocationListenerNetwork();
	Button bGetPosition, bSavePoi;
	ImageButton ibShowOnMap;
    EditText guiPoiName, guiPoiLatitude, guiPoiLongitude, guiPoiRadius;

    private static ProgressDialog progressDialog;

	@Override
	protected void onPause()
	{
		super.onPause();
		Miscellaneous.logEvent("i", "ActivityManageSpecificPoi", getResources().getString(R.string.logClearingBothLocationListeners) , 5);
		myLocationManager.removeUpdates(myLocationListenerGps);
		myLocationManager.removeUpdates(myLocationListenerNetwork);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.manage_specific_poi);
		
		myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		bGetPosition = (Button)findViewById(R.id.bGetPosition);
		ibShowOnMap = (ImageButton)findViewById(R.id.ibShowOnMap);
		
		guiPoiName = (EditText)findViewById(R.id.etPoiName);
		guiPoiLatitude = (EditText)findViewById(R.id.etPoiLatitude);
		guiPoiLongitude = (EditText)findViewById(R.id.etPoiLongitude);
		guiPoiRadius = (EditText)findViewById(R.id.etPoiRadius);
				
		bGetPosition.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				hideKeyboard();
				getNotificationDialog(getResources().getString(R.string.positioningWindowNotice)).show();
			}
		});
		
		bSavePoi = (Button)findViewById(R.id.bSavePoi);
		bSavePoi.setOnClickListener(new OnClickListener()
		{
        @Override
        public void onClick(View v)
        {
			hideKeyboard();

            if(ActivityMainPoi.poiToEdit == null)
				createPoi();
            else
				changePoi();
        }
    });
		
		ibShowOnMap.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				hideKeyboard();
				showOnMap();				
			}
		});
		
		if(ActivityMainPoi.poiToEdit != null)
			editPoi(ActivityMainPoi.poiToEdit);
		//else
		//	new Poi to be created
	}
	
	private void createPoi()
	{
		myLocationManager.removeUpdates(myLocationListenerGps);
		ActivityMainPoi.poiToEdit = new PointOfInterest();
		ActivityMainPoi.poiToEdit.setLocation(new Location("POINT_LOCATION"));
		if(loadFormValuesToVariable())
			if(ActivityMainPoi.poiToEdit.create(this))
			{
				this.setResult(RESULT_OK);
				finish();
			}
	}
	private void changePoi()
	{
		myLocationManager.removeUpdates(myLocationListenerGps);
		if(loadFormValuesToVariable())
			if(ActivityMainPoi.poiToEdit.change(this))
			{
				this.setResult(RESULT_OK);
				finish();
			}
	}
	
	private void getLocation()
	{
		Criteria critNetwork = new Criteria();
		critNetwork.setPowerRequirement(Criteria.POWER_LOW);
		critNetwork.setAltitudeRequired(false);
		critNetwork.setSpeedRequired(false);
		critNetwork.setBearingRequired(false);
		critNetwork.setCostAllowed(false);
		critNetwork.setAccuracy(Criteria.ACCURACY_COARSE);
		
		Criteria critGps = new Criteria();
		critGps.setAltitudeRequired(false);
		critGps.setSpeedRequired(false);
		critGps.setBearingRequired(false);
		critGps.setCostAllowed(true);
		critGps.setAccuracy(Criteria.ACCURACY_FINE);
		
		String provider1 = myLocationManager.getBestProvider(critNetwork, true);
		String provider2 = myLocationManager.getBestProvider(critGps, true);
//		String provider3 = myLocationManager.getProvider("wifi");
		
		if(provider1 == null | provider2 == null)
		{
			Toast.makeText(this, getResources().getString(R.string.logNoSuitableProvider), Toast.LENGTH_LONG).show();
			return;
		}
		else
		{
			Miscellaneous.logEvent("i", "POI Manager", getResources().getString(R.string.logGettingPositionWithProvider) + " " + provider1, 3);
			myLocationManager.requestLocationUpdates(provider1, 500, Settings.satisfactoryAccuracyNetwork, myLocationListenerNetwork);
			
			Miscellaneous.logEvent("i", "POI Manager", getResources().getString(R.string.logGettingPositionWithProvider) + " " + provider2, 3);
			myLocationManager.requestLocationUpdates(provider2, 500, Settings.satisfactoryAccuracyGps, myLocationListenerGps);
		}
		
	}
	
	private void compareLocations()
	{
		if(locationGps != null)
		{
			guiPoiLatitude.setText(String.valueOf(locationGps.getLatitude()));
			guiPoiLongitude.setText(String.valueOf(locationGps.getLongitude()));

			if(locationNetwork != null)
			{
				Miscellaneous.logEvent("i", "POI Manager", getResources().getString(R.string.comparing), 4);

				double variance = locationGps.distanceTo(locationNetwork);

				String text = getResources().getString(R.string.distanceBetween) + " " + String.valueOf(Math.round(variance)) + " " + getResources().getString(R.string.radiusSuggestion);
//			Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG).show();
				Miscellaneous.logEvent("i", "POI Manager", text, 4);
//			if(variance > 50 && guiPoiRadius.getText().toString().length()>0 && Integer.parseInt(guiPoiRadius.getText().toString())<variance)
//			{
//				String text = "Positioning via network is off by " + variance + " meters. The radius you specify shouldn't be smaller than that.";
				getDialog(text, Math.round(variance) + 1).show();
//				Toast.makeText(getBaseContext(), "Positioning via network is off by " + variance + " meters. The radius you specify shouldn't be smaller than that.", Toast.LENGTH_LONG).show();
//			}
			}
			else
			{
				progressDialog.dismiss();
				myLocationManager.removeUpdates(myLocationListenerNetwork);
				guiPoiRadius.setText("250");
			}
		}
		else
			Miscellaneous.logEvent("i", "POI Manager", getResources().getString(R.string.logNotAllMeasurings), 4);
	}
	
	private AlertDialog getNotificationDialog(String text)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
//				switch(which)
//				{
//					case DialogInterface.BUTTON_POSITIVE:
//						guiPoiRadius.setText(String.valueOf(value));
//						break;
//					case DialogInterface.BUTTON_NEGATIVE:
//						break;
//				}
				
				progressDialog = ProgressDialog.show(ActivityManagePoi.this, "", getResources().getString(R.string.gettingPosition), true, true);
				getLocation();
			}
		};
		alertDialogBuilder.setMessage(text).setPositiveButton("Ok", dialogClickListener);
											//.setNegativeButton("No", dialogClickListener);
		AlertDialog alertDialog = alertDialogBuilder.create();
		
		return alertDialog;
	}
	private AlertDialog getDialog(String text, final double value)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{				
				switch(which)
				{
					case DialogInterface.BUTTON_POSITIVE:
						guiPoiRadius.setText(String.valueOf(value));
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						break;
				}

				progressDialog.dismiss();
			}
		};
		alertDialogBuilder.setMessage(text).setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
											.setNegativeButton(getResources().getString(R.string.no), dialogClickListener);
		AlertDialog alertDialog = alertDialogBuilder.create();
		
		return alertDialog;
	}
	
	public class MyLocationListenerGps implements LocationListener
	{

		@Override
		public void onLocationChanged(Location location)
		{
			Miscellaneous.logEvent("i", "POI Manager", getResources().getString(R.string.logGotGpsUpdate) + " " + String.valueOf(location.getAccuracy()), 3);
			// Deactivate when accuracy reached
//			if(location.getAccuracy() < Settings.SATISFACTORY_ACCURACY_GPS)
//			{
//				Miscellaneous.logEvent("i", "POI Manager", "satisfactoryNetworkAccuracy of " + String.valueOf(Settings.SATISFACTORY_ACCURACY_GPS) + "m reached. Removing location updates...");

				myLocationManager.removeUpdates(this);
				locationGps = location;
				
				compareLocations();
//			}
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			// TODO Auto-generated method stub
			
		}
		
	}
//	public class MyLocationListenerWifi implements LocationListener
//	{
//
//		@Override
//		public void onLocationChanged(Location location)
//		{
//			Miscellaneous.logEvent("i", "POI Manager", getResources().getString(R.string.gotGpsUpdate) + " " + String.valueOf(location.getAccuracy()));
//			// Deactivate when accuracy reached
////			if(location.getAccuracy() < Settings.SATISFACTORY_ACCURACY_GPS)
////			{
////				Miscellaneous.logEvent("i", "POI Manager", "satisfactoryNetworkAccuracy of " + String.valueOf(Settings.SATISFACTORY_ACCURACY_GPS) + "m reached. Removing location updates...");
//
//				myLocationManager.removeUpdates(this);
//				locationGps = location;
//				
//				compareLocations();
////			}
//		}
//
//		@Override
//		public void onProviderDisabled(String provider)
//		{
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void onProviderEnabled(String provider)
//		{
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void onStatusChanged(String provider, int status, Bundle extras)
//		{
//			// TODO Auto-generated method stub
//			
//		}
//		
//	}
	public class MyLocationListenerNetwork implements LocationListener
	{

		@Override
		public void onLocationChanged(Location location)
		{			
			Miscellaneous.logEvent("i", "POI Manager", getResources().getString(R.string.logGotNetworkUpdate) + " " + String.valueOf(location.getAccuracy()), 3);
			// Deactivate when accuracy reached
//			if(location.getAccuracy() < Settings.SATISFACTORY_ACCURACY_GPS)
//			{
//				String text = "Network position found. satisfactoryNetworkAccuracy of " + String.valueOf(Settings.SATISFACTORY_ACCURACY_NETWORK) + "m reached. Removing location updates...";
//				Miscellaneous.logEvent("i", "POI Manager", text);
				myLocationManager.removeUpdates(this);
				locationNetwork = location;
				
				compareLocations();
//			}
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public void editPoi(PointOfInterest poi)
	{
		guiPoiName.setText(poi.getName());
		guiPoiLatitude.setText(String.valueOf(poi.getLocation().getLatitude()));
		guiPoiLongitude.setText(String.valueOf(poi.getLocation().getLongitude()));
		guiPoiRadius.setText(String.valueOf(poi.getRadius()));
	}
	
	public boolean loadFormValuesToVariable()
	{
		if(ActivityMainPoi.poiToEdit == null)
			ActivityMainPoi.poiToEdit = new PointOfInterest();
		
		if(guiPoiName.getText().length() == 0)
		{
			Toast.makeText(this, getResources().getString(R.string.pleaseEnterValidName), Toast.LENGTH_LONG).show();
			return false;
		}
		else
			ActivityMainPoi.poiToEdit.setName(guiPoiName.getText().toString());
		
		if(ActivityMainPoi.poiToEdit.getLocation() == null)
			ActivityMainPoi.poiToEdit.setLocation(new Location("POINT_LOCATION"));
		
		try
		{
			ActivityMainPoi.poiToEdit.getLocation().setLatitude(Double.parseDouble(guiPoiLatitude.getText().toString()));
		}
		catch(NumberFormatException e)
		{
			Toast.makeText(this, getResources().getString(R.string.pleaseEnterValidLatitude), Toast.LENGTH_LONG).show();
			return false;
		}
		
		try
		{
			ActivityMainPoi.poiToEdit.getLocation().setLongitude(Double.parseDouble(guiPoiLongitude.getText().toString()));
		}
		catch(NumberFormatException e)
		{
			Toast.makeText(this, getResources().getString(R.string.pleaseEnterValidLongitude), Toast.LENGTH_LONG).show();
			return false;
		}
		
		try
		{
			ActivityMainPoi.poiToEdit.setRadius(Double.parseDouble(guiPoiRadius.getText().toString()), this);
		}
		catch(NumberFormatException e)
		{
			Toast.makeText(this, getResources().getString(R.string.pleaseEnterValidRadius), Toast.LENGTH_LONG).show();
			return false;
		}
		catch (Exception e)
		{
			Toast.makeText(this, getResources().getString(R.string.unknownError), Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}
	
	private void showOnMap()
	{
		if(loadFormValuesToVariable())
		{
			try
			{
				String uri = "geo:" + guiPoiLatitude.getText().toString() + "," + guiPoiLongitude.getText().toString();
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
		
				// You can also choose to place a point like so:
		//		String uri = "geo:"+ latitude + "," + longitude + "?q=my+street+address";
		//		startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
		
				/*
				* The Possible Query params options are the following:
				*
				* Show map at location: geo:latitude,longitude
				* Show zoomed map at location: geo:latitude,longitude?z=zoom
				* Show map at locaiton with point: geo:0,0?q=my+street+address
				* Show map of businesses in area: geo:0,0?q=business+near+city
				*
				*/
			}
			catch(ActivityNotFoundException e)
			{
				Toast.makeText(this, getResources().getString(R.string.noMapsApplicationFound), Toast.LENGTH_LONG).show();
			}
		}
	}

	protected void hideKeyboard()
	{
		View view = this.getCurrentFocus();
		if (view != null)
		{
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
}
