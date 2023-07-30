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
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jens.automation2.receivers.ConnectivityReceiver;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityManagePoi extends Activity
{
	public LocationManager myLocationManager;
	MyLocationListenerGps myLocationListenerGps = new MyLocationListenerGps();
	Location locationGps = null, locationNetwork = null;
	MyLocationListenerNetwork myLocationListenerNetwork = new MyLocationListenerNetwork();
	Button bGetPosition, bSavePoi;
	ImageButton ibShowOnMap;
    EditText guiPoiName, guiPoiLatitude, guiPoiLongitude, guiPoiRadius;
    Calendar locationSearchStart = null;
	Timer timer = null;

    final static int defaultRadius = 250;
    final static int searchTimeout = 120;

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
		Miscellaneous.setDisplayLanguage(this);
		this.setContentView(R.layout.activity_manage_specific_poi);
		
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
		if(loadFormValuesToVariable(false))
			if(ActivityMainPoi.poiToEdit.create(this))
			{
				this.setResult(RESULT_OK);
				finish();
			}
	}
	private void changePoi()
	{
		myLocationManager.removeUpdates(myLocationListenerGps);
		if(loadFormValuesToVariable(false))
			if(ActivityMainPoi.poiToEdit.change(this))
			{
				this.setResult(RESULT_OK);
				finish();
			}
	}
	
	private void getLocation()
	{
		Criteria criteriaNetwork = new Criteria();
		criteriaNetwork.setPowerRequirement(Criteria.POWER_LOW);
		criteriaNetwork.setAltitudeRequired(false);
		criteriaNetwork.setSpeedRequired(false);
		criteriaNetwork.setBearingRequired(false);
		criteriaNetwork.setCostAllowed(false);
		criteriaNetwork.setAccuracy(Criteria.ACCURACY_COARSE);

		Criteria criteriaGps = new Criteria();
		criteriaGps.setAltitudeRequired(false);
		criteriaGps.setSpeedRequired(false);
		criteriaGps.setBearingRequired(false);
		criteriaGps.setCostAllowed(true);
		criteriaGps.setAccuracy(Criteria.ACCURACY_FINE);
		
		String provider1 = myLocationManager.getBestProvider(criteriaNetwork, true);
		String provider2 = myLocationManager.getBestProvider(criteriaGps, true);
//		String provider3 = myLocationManager.getProvider("wifi");
		
		if(provider1 == null || provider2 == null)
		{
			Toast.makeText(this, getResources().getString(R.string.logNoSuitableProvider), Toast.LENGTH_LONG).show();
			return;
		}
		else
		{
			if(provider1.equals(provider2))
				Miscellaneous.logEvent("i", "POI Manager", "Both location providers are equal. Only one will be used.", 4);

			locationSearchStart = Calendar.getInstance();
			startTimeout();

			if(!Settings.privacyLocationing && !ConnectivityReceiver.isDataConnectionAvailable(Miscellaneous.getAnyContext()) && !provider1.equals(provider2))
			{
				Miscellaneous.logEvent("i", "POI Manager", getResources().getString(R.string.logGettingPositionWithProvider) + " " + provider1, 3);
				myLocationManager.requestLocationUpdates(provider1, 500, Settings.satisfactoryAccuracyNetwork, myLocationListenerNetwork);
			}
			else
				Miscellaneous.logEvent("i", "POI Manager", "Skipping network location.", 4);

			Miscellaneous.logEvent("i", "POI Manager", getResources().getString(R.string.logGettingPositionWithProvider) + " " + provider2, 3);
			myLocationManager.requestLocationUpdates(provider2, 500, Settings.satisfactoryAccuracyGps, myLocationListenerGps);
		}
	}

	private void startTimeout()
	{
		if(timer != null)
			stopTimeout();

		timer = new Timer();

		class TimeoutTask extends TimerTask
		{
			public void run()
			{
				evaluateLocationResults();
			}
		}

		Miscellaneous.logEvent("i", "POI Manager", "Starting timeout for location search: " + String.valueOf(searchTimeout) + " seconds", 5);

		TimerTask timeoutTask = new TimeoutTask();
		timer.schedule(timeoutTask, searchTimeout * 1000);
	}

	private void stopTimeout()
	{
		Miscellaneous.logEvent("i", "POI Manager", "Stopping timeout for location search.", 5);

		if(timer != null)
		{
			timer.purge();
			timer.cancel();
		}
	}

	private void evaluateLocationResults()
	{
		/*
			Procedure:
			If we get a GPS result we take it and suggest a default minimum radius.
			If private locationing is active that's the only possible outcome other than a timeout.

			If private locationing is not active
			If we get a network
		 */

		// We have GPS
		if(locationGps != null)
		{
			myLocationManager.removeUpdates(myLocationListenerNetwork);

			guiPoiLatitude.setText(String.valueOf(locationGps.getLatitude()));
			guiPoiLongitude.setText(String.valueOf(locationGps.getLongitude()));

			String text;
			if(locationNetwork != null)
			{
				Miscellaneous.logEvent("i", "POI Manager", getResources().getString(R.string.comparing), 4);
				double variance = locationGps.distanceTo(locationNetwork);
				text = String.format(getResources().getString(R.string.distanceBetween), Math.round(variance));
				getRadiusConfirmationDialog(text, Math.round(variance) + 1).show();
			}
			else
			{
				text = String.format(getResources().getString(R.string.locationFound), defaultRadius);
				getRadiusConfirmationDialog(text, defaultRadius).show();
			}
			Miscellaneous.logEvent("i", "POI Manager", text, 4);
		}	// we have a great network signal:
		else if(locationNetwork != null && locationNetwork.getAccuracy() <= Settings.satisfactoryAccuracyGps && locationNetwork.getAccuracy() <= defaultRadius)
		{
			/*
				We do not yet have a GPS result. But we have a network result that is good enough
				to accept it a sole result. In that case we suggest a default radius, no variance.
			 */

			guiPoiLatitude.setText(String.valueOf(locationNetwork.getLatitude()));
			guiPoiLongitude.setText(String.valueOf(locationNetwork.getLongitude()));

			String text = String.format(getResources().getString(R.string.locationFound), defaultRadius);
			Miscellaneous.logEvent("i", "POI Manager", text, 4);

			getRadiusConfirmationDialog(text, defaultRadius).show();
		}
		else if(	// we have a bad network signal and nothing else, GPS result may still come in
				locationNetwork != null
						&&
				Calendar.getInstance().getTimeInMillis()
						<
				(locationSearchStart.getTimeInMillis() + ((long)searchTimeout * 1000))
			)
		{
			// Only a network location was found and it is also not very accurate.
		}
		else if(	// we have a bad network signal and nothing else, timeout has expired, nothing else can possibly come in
				locationNetwork != null
						&&
				Calendar.getInstance().getTimeInMillis()
						>
				(locationSearchStart.getTimeInMillis() + ((long)searchTimeout * 1000))
		)
		{
			// Only a network location was found and it is also not very accurate.

			guiPoiLatitude.setText(String.valueOf(locationNetwork.getLatitude()));
			guiPoiLongitude.setText(String.valueOf(locationNetwork.getLongitude()));

			String text = String.format(getResources().getString(R.string.locationFoundInaccurate), defaultRadius);
			getRadiusConfirmationDialog(text, defaultRadius).show();
			Miscellaneous.logEvent("i", "POI Manager", text, 4);
		}
		else
		{
			String text = String.format(getResources().getString(R.string.noLocationCouldBeFound), String.valueOf(searchTimeout));
			Miscellaneous.logEvent("i", "POI Manager", text, 2);

			if(myLocationListenerNetwork != null)
				myLocationManager.removeUpdates(myLocationListenerNetwork);

			myLocationManager.removeUpdates(myLocationListenerGps);
			progressDialog.dismiss();
			getErrorDialog(text).show();
		}
	}
	
	private AlertDialog getNotificationDialog(String text)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				progressDialog = ProgressDialog.show(ActivityManagePoi.this, "", getResources().getString(R.string.gettingPosition), true, true);
				if(Build.VERSION.SDK_INT >= 31)
				{
					AlertDialog dia = Miscellaneous.messageBox(getResources().getString(R.string.info), getResources().getString(R.string.locationNotWorkingOn12), ActivityManagePoi.this);
					dia.setOnDismissListener(new DialogInterface.OnDismissListener()
					{
						@Override
						public void onDismiss(DialogInterface dialogInterface)
						{
							getLocation();
						}
					});
					dia.show();
				}
				else
					getLocation();
			}
		};
		alertDialogBuilder.setMessage(text).setPositiveButton("Ok", dialogClickListener);
											//.setNegativeButton("No", dialogClickListener);
		AlertDialog alertDialog = alertDialogBuilder.create();
		
		return alertDialog;
	}

	private AlertDialog getRadiusConfirmationDialog(String text, final double value)
	{
		stopTimeout();

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

	private AlertDialog getErrorDialog(String text)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				progressDialog.dismiss();
			}
		};
		alertDialogBuilder.setMessage(text);
		alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), null);

		if (Looper.myLooper() == null)
			Looper.prepare();

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

				Miscellaneous.logEvent("i", "POI Manager", "Unsubscribing from GPS location updates.", 5);
				myLocationManager.removeUpdates(this);
				locationGps = location;
				
				evaluateLocationResults();
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

	public class MyLocationListenerNetwork implements LocationListener
	{
		@Override
		public void onLocationChanged(Location location)
		{			
			Miscellaneous.logEvent("i", "POI Manager", getResources().getString(R.string.logGotNetworkUpdate) + " " + String.valueOf(location.getAccuracy()), 3);

			myLocationManager.removeUpdates(this);
			locationNetwork = location;

			// Deactivate when accuracy reached
			if(location.getAccuracy() <= Settings.satisfactoryAccuracyGps)
			{
				// Accuracy is so good that we don't need to wait for GPS result
				Miscellaneous.logEvent("i", "POI Manager", "Unsubscribing from network location updates.", 5);
				myLocationManager.removeUpdates(myLocationListenerGps);
			}

			evaluateLocationResults();
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
	
	public boolean loadFormValuesToVariable(boolean checkOnlyCoordinates)
	{
		if(ActivityMainPoi.poiToEdit == null)
			ActivityMainPoi.poiToEdit = new PointOfInterest();

		if(!checkOnlyCoordinates)
		{
			if (guiPoiName.getText().length() == 0)
			{
				Toast.makeText(this, getResources().getString(R.string.pleaseEnterValidName), Toast.LENGTH_LONG).show();
				return false;
			}
			else
				ActivityMainPoi.poiToEdit.setName(guiPoiName.getText().toString());
		}

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

		if(!checkOnlyCoordinates)
		{
			try
			{
				ActivityMainPoi.poiToEdit.setRadius(Double.parseDouble(guiPoiRadius.getText().toString()), this);
			}
			catch (NumberFormatException e)
			{
				Toast.makeText(this, getResources().getString(R.string.pleaseEnterValidRadius), Toast.LENGTH_LONG).show();
				return false;
			}
			catch (Exception e)
			{
				Toast.makeText(this, getResources().getString(R.string.unknownError), Toast.LENGTH_LONG).show();
				return false;
			}
		}

		return true;
	}
	
	private void showOnMap()
	{
		if(loadFormValuesToVariable(true))
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
