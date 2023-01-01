package com.jens.automation2;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.jens.automation2.Trigger.Trigger_Enum;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

public class PointOfInterest implements Comparable<PointOfInterest>
{
	// The array containing all POIs
	private static ArrayList<PointOfInterest> pointOfInterestCollection = new ArrayList<PointOfInterest>();
	
	public static ArrayList<PointOfInterest> getPointOfInterestCollection()
	{
		Collections.sort(pointOfInterestCollection);
		return pointOfInterestCollection;
	}

	public static void setPointOfInterestCollection(ArrayList<PointOfInterest> pointOfInterestCollection)
	{
		Collections.sort(pointOfInterestCollection);
		PointOfInterest.pointOfInterestCollection = pointOfInterestCollection;
	}
	
	// name and location
	private String name;
	private Location location;
	private double radius;

	private String oldName;
	private boolean activated=false;
	
	private static Location[] locationRingBuffer = new Location[Settings.locationRingBufferSize];
	private static int locationRingBufferLastPosition = -1;
	
	private static boolean gpsLocationListenerArmed = false;
	private static LocationManager gpsComparisonLocationManager;
	private static GpsComparisonLocationListener gpsComparisonLocationListener;
	private static TimeoutHandler timeoutHandler = new TimeoutHandler();
	private static boolean timeoutHandlerActive = false;
	public String getName()
	{
		return name;
	}
	
	public static void stopRoutine()
	{
		if(gpsLocationListenerArmed)
			stopGpsMeasurement();
	}

	public void setName(String desiredName)
	{
		this.oldName = this.name;
		this.name = desiredName.trim();
	}

	public Location getLocation()
	{
		return location;
	}

	public void setLocation(Location location)
	{
		this.location = location;
	}

	public double getRadius()
	{
		return radius;
	}

	public void setRadius(double radius, Context context) throws Exception
	{
		if(radius <= 0)
			throw new Exception(context.getResources().getString(R.string.radiusHasToBePositive));
		
		this.radius = radius;
	}
	
	public void setActivated(boolean value)
	{
		this.activated = value;
	}	
	public boolean isActivated()
	{
		return activated;
	}
	
	public static void positionUpdate(Location newLocation, AutomationService parentService, boolean forceApply, boolean skipVerfication)
	{		
//		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
//		for(StackTraceElement element : trace)
//		{
//			Log.i("Trace", Arrays.toString(trace));
//		}
		
		// Assumption "active POI = closest POI" is wrong!
		
		if(newLocation != null)
		{
			String accuracyString = "n./a.";
			if(newLocation.hasAccuracy())
				accuracyString = String.valueOf(newLocation.getAccuracy() + " m");
			Miscellaneous.logEvent("i", "POI", "Got position update (" + String.valueOf(newLocation.getLatitude()) + " / " + String.valueOf(newLocation.getLongitude()) + " / provider: " + newLocation.getProvider() + " / Accuracy: " + accuracyString + "), checking rules.", 2);
			
			PointOfInterest closestPoi = PointOfInterest.getClosestPOI(newLocation);

			if(getActivePoi() != null)
				Miscellaneous.logEvent("i", "POI", "Active POI: " + getActivePoi().getName() + ", distance : " + String.valueOf(newLocation.distanceTo(getActivePoi().getLocation())), 4);
			
			if(closestPoi == null)
			{
				// There are no POIs defined. Not much we can do.
	//			Miscellaneous.logEvent("i", "POI", "Closest POI: n/a, distance : n/a", 4);
				Miscellaneous.logEvent("i", "POI", "Got position update, but there are no POIs defined. Can't trigger a rule.", 3);
	//			return;
			}
			else
				Miscellaneous.logEvent("i", "POI", "Closest POI: " + closestPoi.getName() + ", distance : " + String.valueOf(newLocation.distanceTo(closestPoi.getLocation())), 4);

			if(
					(getActivePoi() != null && getActivePoi().isActivated() && !getActivePoi().reachedPoiArea(newLocation))
																	|
					(closestPoi != null && !closestPoi.isActivated() && closestPoi.reachedPoiArea(newLocation))
			  )
			{
				// only an active POI can be left while only a closestPOI can be entered, hence the complex if/else
				if(getActivePoi() != null && getActivePoi().isActivated() && !getActivePoi().reachedPoiArea(newLocation))
					Miscellaneous.logEvent("i", "POI", "May have left POI " + getActivePoi().getName() + ", checking location accuracy...", 4);
				if(closestPoi != null && !closestPoi.isActivated() && closestPoi.reachedPoiArea(newLocation))
					Miscellaneous.logEvent("i", "POI", "May have entered POI " + closestPoi.getName() + ", checking location accuracy...", 4);
				
				if(forceApply)
				{
					Miscellaneous.logEvent("i", parentService.getResources().getString(R.string.forcedLocationUpdate), parentService.getResources().getString(R.string.forcedLocationUpdateLong), 4);
					
					// only an active POI can be left while only a closestPOI can be entered, hence the complex if/else
					if(getActivePoi() != null && getActivePoi().isActivated() && !getActivePoi().reachedPoiArea(newLocation))
					{
						addPositionToRingBuffer(newLocation);
						getActivePoi().deactivate(parentService);
					}
					if(closestPoi != null && !closestPoi.isActivated() && closestPoi.reachedPoiArea(newLocation))
					{
						addPositionToRingBuffer(newLocation);
						closestPoi.activate(parentService);
					}
				}
				else if(newLocation.hasAccuracy() && newLocation.getAccuracy() > Settings.satisfactoryAccuracyNetwork && !newLocation.getProvider().equals(LocationManager.GPS_PROVIDER))
				{
					Miscellaneous.logEvent("i", "POI", "Location update with unsatisfactory accuracy: " + String.valueOf(newLocation.getAccuracy()) + ", demanded: " + String.valueOf(Settings.satisfactoryAccuracyNetwork), 4);
					if(!skipVerfication)
					{
						if(PointOfInterest.isPoiInRelevantRange(newLocation))
							startGpsMeasurement(parentService);
						else
						{
							Miscellaneous.logEvent("i", "POI", "Applying update with unsatisfactory accuracy because no defined location is in a relevant range.", 4);
							positionUpdate(newLocation, parentService, true, false);
						}
					}
					else
					{
						Miscellaneous.logEvent("i", "POI", "Location update with unsatisfactory accuracy, but skipping verfication as requested. Effectively ignoring this update. It's probably from a passive source. Verifying it would cost battery.", 4);
					}
				}
				else
				{
					Miscellaneous.logEvent("i", "POI", "Location update with acceptable accuracy.", 4);
					/* It may be that a previous location wasn't accurate enough, we now got a better location via network,
					 * but the GPS listener is still active and trying to find out a precise location. We need to deactivate
					 * it if we are here
					 */
					if(gpsLocationListenerArmed)
						stopGpsMeasurement();

					// only an active POI can be left while only a closestPOI can be entered, hence the complex if/else
					if(getActivePoi() != null && getActivePoi().isActivated() && !getActivePoi().reachedPoiArea(newLocation))
					{
						addPositionToRingBuffer(newLocation);
						getActivePoi().deactivate(parentService);
					}
					if(closestPoi != null && !closestPoi.isActivated() && closestPoi.reachedPoiArea(newLocation))
					{
						addPositionToRingBuffer(newLocation);
						closestPoi.activate(parentService);
					}
				}
			}
		}
		else
			Miscellaneous.logEvent("e", "POI", "Given location is null. Aborting.", 3);
	}

	public Boolean reachedPoiArea(Location currentLocation)
	{
		float distance = this.location.distanceTo(currentLocation);

		if(distance < this.radius)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void activate(AutomationService parentService)
	{
		if(!this.isActivated())
		{
			// Deactivate all others in case nobody deactivated them
			for(int i = 0; i < pointOfInterestCollection.size(); i++)
				pointOfInterestCollection.get(i).deactivate(parentService);

			/*
				ConcurrentModificationErrors have been seen when using this method: for(PointOfInterest onePoi : pointOfInterestCollection)

				Tue Nov 20 19:21:50 GMT+01:00 2018: e / Automation / java.util.ConcurrentModificationException
				at java.util.ArrayList$Itr.next(ArrayList.java:860)
				at com.jens.automation2.PointOfInterest.activate(PointOfInterest.java:227)
				at com.jens.automation2.PointOfInterest.positionUpdate(PointOfInterest.java:199)
				at com.jens.automation2.location.LocationProvider.setCurrentLocation(LocationProvider.java:126)
				at com.jens.automation2.location.LocationProvider$MyPassiveLocationListener.onLocationChanged(LocationProvider.java:289)
				at android.location.LocationManager$ListenerTransport._handleMessage(LocationManager.java:291)
				at android.location.LocationManager$ListenerTransport.-wrap0(Unknown Source:0)
				at android.location.LocationManager$ListenerTransport$1.handleMessage(LocationManager.java:236)
				at android.os.Handler.dispatchMessage(Handler.java:105)
				at android.os.Looper.loop(Looper.java:164)
				at android.app.ActivityThread.main(ActivityThread.java:6944)
				at java.lang.reflect.Method.invoke(Native Method)
				at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:327)
				at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1374)
			*/

			this.activated = true;
			Settings.lastActivePoi = this;
			Settings.writeSettings(parentService);
			
			Miscellaneous.logEvent("i", "POI", "Reached POI " + this.getName() + ". Checking if there's a rule that applies to that.", 2);

			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger_Enum.pointOfInterest);
//			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesByPoi(this);
			if(ruleCandidates.size()==0)
			{
				Miscellaneous.logEvent("i", "POI", "POI " + this.getName() + " not found in ANY rule.", 2);
			}
			else
			{
				Miscellaneous.logEvent("i", "POI", "POI " + this.getName() + " found in " + ruleCandidates.size() + " rule(s).", 2);
				
				for(int i=0; i<ruleCandidates.size(); i++)
				{
					if(ruleCandidates.get(i).haveEnoughPermissions() && ruleCandidates.get(i).getsGreenLight(parentService))
					{
						Miscellaneous.logEvent("i", "POI", "Rule " + ruleCandidates.get(i).getName() + " applies for entering POI " + this.getName() + ".", 2);
						ruleCandidates.get(i).activate(parentService, false);
					}
				}
			}

			Miscellaneous.logEvent("i", "POI", "Reached POI " + this.getName() + ". Done checking POI rules.", 2);


			parentService.updateNotification();
			ActivityMainScreen.updateMainScreen();
		}
	}

	public void deactivate(AutomationService parentService)
	{
		if(this.isActivated())
		{
			this.activated=false; //has to stay before Rule.applies()
			Settings.lastActivePoi = null;
			Settings.writeSettings(parentService);
			
			Miscellaneous.logEvent("i", "POI", "Left POI " + this.getName() + ". Checking if there's a rule that applies to that.", 2);

			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger_Enum.pointOfInterest);
//			ArrayList<Rule> ruleCandidates = Rule.findRuleCandidatesByPoi(this);
			if(ruleCandidates.size()==0)
			{
				Miscellaneous.logEvent("i", "POI", "POI " + this.getName() + " not found in ANY rule.", 2);
			}
			else
			{
				Miscellaneous.logEvent("i", "POI", "POI " + this.getName() + " found in " + ruleCandidates.size() + " rule(s).", 2);
				for(int i=0; i<ruleCandidates.size(); i++)
				{
					if(ruleCandidates.get(i).haveEnoughPermissions() && ruleCandidates.get(i).getsGreenLight(parentService))
					{
						Miscellaneous.logEvent("i", "POI", "Rule " + ruleCandidates.get(i).getName() + " applies for leaving POI " + this.getName() + ".", 2);
						ruleCandidates.get(i).activate(parentService, false);
					}
				}
			}
			parentService.updateNotification();
			ActivityMainScreen.updateMainScreen();
		}
	}
	
	public static PointOfInterest getClosestPOI(Location currentLocation)// throws Exception
	{
		// return the currently closed one of all saved points of interest
		
		if(pointOfInterestCollection.size() == 0)
		{
			//throw new Exception("No points of interest defined.");
			//Toast.makeText(context, "No points of interest defined.", Toast.LENGTH_LONG).show();
			return null;
		}
		else if(pointOfInterestCollection.size() == 1)
			return pointOfInterestCollection.get(0);
		else
		{
			double distance = pointOfInterestCollection.get(0).location.distanceTo(currentLocation);
			PointOfInterest closestPoi = pointOfInterestCollection.get(0);
			
			distance = (int) currentLocation.distanceTo(pointOfInterestCollection.get(0).location);
			
			for(int i=1; i<pointOfInterestCollection.size(); i++)
			{
				if(currentLocation.distanceTo(pointOfInterestCollection.get(i).location) < distance)
				{
					distance = currentLocation.distanceTo(pointOfInterestCollection.get(i).location);
					closestPoi = pointOfInterestCollection.get(i);
				}
			}
			
			return closestPoi;
		}
	}
	
	/** Determines if a POI is in any relevant range. Doesn't say if one is
	 * reached, but can help decide if it's worth activating GPS to be sure.
	 * @param currentLocation
	 * @return
	 */
	public static boolean isPoiInRelevantRange(Location currentLocation)
	{
		/*
		 * Radius
		 * + Precision
		 * + Self defined value
		 */
		
		double distance;
		double minimumDistance;
		
		for(PointOfInterest poi : PointOfInterest.getPointOfInterestCollection())
		{
			distance = poi.getLocation().distanceTo(currentLocation);
			if(currentLocation.hasAccuracy())
				minimumDistance = currentLocation.getAccuracy();
			else
				minimumDistance = 0;
			
			minimumDistance += poi.getRadius();
			
			if(distance < minimumDistance)
			{
				Miscellaneous.logEvent("i", "POI", String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.poiCouldBeInRange), poi.getName()), 4);
				return true;
			}
		}
		
		
		return false;
	}
	
	public static void loadPoisFromFile()
	{
		try
		{
			if(XmlFileInterface.settingsFile.exists())
			{
				Miscellaneous.logEvent("i", "POI", "SettingsFile " + XmlFileInterface.settingsFile.getPath() + " exists. Loading POIs from File.", 4);
				XmlFileInterface.readFile();
			}
			else
			{
				Miscellaneous.logEvent("w", "POI", "SettingsFile " + XmlFileInterface.settingsFile.getPath() + " doesn't exist.", 4);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	public static boolean writePoisToFile()
	{
		return XmlFileInterface.writeFile();
	}
	

	@Override
	public String toString()
	{
		return this.getName();
	}
	public String toStringLong()
	{
		return this.name + ": " + String.valueOf(this.radius) + " meters around " + String.valueOf(this.location.getLatitude() + " / " + String.valueOf(this.location.getLongitude()));
	}

	public boolean create(Context context)
	{
		for(PointOfInterest poi : PointOfInterest.pointOfInterestCollection)
			if(poi.getName().equals(this.getName()))
			{
				Toast.makeText(context, context.getResources().getString(R.string.anotherPoiByThatName), Toast.LENGTH_LONG).show();
				return false;
			}
		
		if(plausibilityCheck())
		{
			PointOfInterest.pointOfInterestCollection.add(this);
			PointOfInterest.writePoisToFile();
			
			AutomationService service = AutomationService.getInstance();
			if(service != null)
			{
				service.applySettingsAndRules();
				//Easiest way to check for changes in location, reset the last known location.
				service.getLocationProvider().setCurrentLocation(service.getLocationProvider().getCurrentLocation(), true);
			}
					
			return true;
		}

		return false;
	}
	public boolean change(Context context)
	{
		try
		{
            /*
                Check for change of rule name START
             */
			if (this.oldName != null && !this.oldName.equals(this.name))        // catch oldName being null
			{
				//Name has changed. We need to look for rules that reference it by its name and update those references

				// Check if the name is still available
				int counter = 0;    // this method should only be a temporary workaround, directly editing the referenced object may cause problems until reloading the config file
				for (PointOfInterest poi : PointOfInterest.pointOfInterestCollection)
					if (poi.getName().equals(this.getName()))
					{
						counter++;
					}
				if (counter > 1)
				{
					Toast.makeText(context, context.getResources().getString(R.string.anotherPoiByThatName), Toast.LENGTH_LONG).show();
					return false;
				}

				// Check if rules reference this poi
				ArrayList<Rule> rulesThatReferenceMe = Rule.findRuleCandidatesByPoi(this);
				if (rulesThatReferenceMe.size() > 0)
				{
					for (Rule oneRule : rulesThatReferenceMe)
					{
						for (Trigger oneTrigger : oneRule.getTriggerSet())
						{
							if (oneTrigger.getTriggerType() == Trigger_Enum.pointOfInterest)
							{
								oneTrigger.setPointOfInterest(this);
								// We don't need to save the file. This will happen anyway in PointOfInterest.writePoisToFile() below.
							}
						}
					}
				}
			}
            /*
                Check for change of rule name END
             */

			if (plausibilityCheck())
			{
				if(PointOfInterest.writePoisToFile())
				{
					AutomationService service = AutomationService.getInstance();
					if (service != null)
					{
						try
						{
							service.applySettingsAndRules();
							//Easiest way to check for changes in location, reset the last known location.
							service.getLocationProvider().setCurrentLocation(service.getLocationProvider().getCurrentLocation(), true);
						}
						catch(Exception e)
						{
							// Just log the event. This should not cause an interruption in the program flow.
							Miscellaneous.logEvent("e", "save POI", "Error when trying to apply settings and rules: " + Log.getStackTraceString(e), 2);
						}
					}

					return true;
				}
				else
					return false;
			}
		}
		catch(Exception e)
		{
			Toast.makeText(context, context.getResources().getString(R.string.unknownError), Toast.LENGTH_LONG).show();
		}

		return false;
	}
	public boolean delete(Context context)
	{
		//Check if there's a rule that contains this poi
		ArrayList<Rule> rulesThatReferenceMe = Rule.findRuleCandidatesByPoi(this);
		if(rulesThatReferenceMe.size() > 0)
		{
			String rulesString = "";
			for(Rule rule : rulesThatReferenceMe)
				rulesString += rule.getName() + "; ";
			
			rulesString = rulesString.substring(0, rulesString.length()-2);
			
			Toast.makeText(context, String.format(context.getResources().getString(R.string.poiStillReferenced), rulesString), Toast.LENGTH_LONG).show();
			return false;
		}
		else		
		{
			PointOfInterest.pointOfInterestCollection.remove(this);
			PointOfInterest.writePoisToFile();
			
			AutomationService service = AutomationService.getInstance();

			try
			{
				service.applySettingsAndRules();
				//Easiest way to check for changes in location, reset the last known location.
				service.getLocationProvider().setCurrentLocation(service.getLocationProvider().getCurrentLocation(), true);
			}
			catch(Exception e)
			{
				// Just log the event. This should not cause an interruption in the program flow.
				Miscellaneous.logEvent("e", "save POI", "Error when trying to apply settings and rules: " + Log.getStackTraceString(e), 2);
			}
			
			return true;
		}
	}
	
	public static PointOfInterest getByName(String searchName) throws Exception
	{
		for(PointOfInterest poi : pointOfInterestCollection)
		{
			if(poi.name.equals(searchName))
				return poi;
		}
		
		throw new Exception("PointOfInterest with name " + searchName + " not found.");
	}
	
	public static String[] getNamesInArray()
	{
		ArrayList<String> nameList = new ArrayList<String>();
		for(PointOfInterest poi : pointOfInterestCollection)
		{
			nameList.add(poi.name);
		}
		
		return (String[])nameList.toArray(new String[pointOfInterestCollection.size()]);
	}

	public static PointOfInterest getActivePoi()
	{
		for(PointOfInterest poi : PointOfInterest.pointOfInterestCollection)
		{
			if(poi.isActivated())
				return poi;
		}
		
		return null;
	}

	@Override
	public int compareTo(PointOfInterest another)
	{
		return this.getName().compareTo(another.getName());
	}
	
	private static boolean addPositionToRingBuffer(Location newLocation)
	{
		/*
		 * This method's purpose is to record the last n positions and check if they are different.
		 * In reality you will never get the exact same position twice. If you do the location engine
		 * seems to have hung up.
		 */
		
		try
		{
			if(++locationRingBufferLastPosition > locationRingBuffer.length-1)
				locationRingBufferLastPosition = 0;			

			Miscellaneous.logEvent("i", "Ringbuffer.", "Adding location " + String.valueOf(newLocation.getLatitude()) + " / " + String.valueOf(newLocation.getLongitude()) + " to ringbuffer at index " + String.valueOf(locationRingBufferLastPosition), 5);
			locationRingBuffer[locationRingBufferLastPosition] = newLocation;
			
			/*
			 * Return values:
			 * true if the new location is different to the last one
			 * false if we get repeated values, comparing all values
			 * 
			 * false indicates problems with hangups in getting locations.
			 */
			
			int counter = locationRingBufferLastPosition+1-1; // make a copy, not a reference
			int previousIndex;
			do
			{
				if(counter>0)
					previousIndex = counter-1;
				else
					previousIndex = Settings.locationRingBufferSize-1;
				
				try
				{
					if(locationRingBuffer[counter].getLatitude() != locationRingBuffer[previousIndex].getLatitude()	| locationRingBuffer[counter].getLongitude() != locationRingBuffer[previousIndex].getLongitude())
					{
						// If location different from last one we're fine.
						Miscellaneous.logEvent("w", "Ringbuffer.", "Location has changed from the last one. We\'re fine.", 5);
						return true;
					}
				}
				catch(NullPointerException ne)
				{
					/*
					 * Just null pointer exception. Ringbuffer isn't filled to its maximum, yet.
					 */
					return true;
				}
				
				if(counter>0)
					counter--;
				else
					counter = Settings.locationRingBufferSize-1;
			} while(counter != locationRingBufferLastPosition);

			Miscellaneous.logEvent("w", "Ringbuffer", "Location has not changed from the last one. Something\'s odd. Maybe the location engine kind of hung up.", 2);
			return false;
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			Miscellaneous.logEvent("e", "Ringbuffer", "Probably not enough values, yet.", 5);
			return true;
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "Ringbuffer", "Error in ringbuffer: " + Log.getStackTraceString(e), 4);
			return true;
		}
	}
	
	public static class GpsComparisonLocationListener implements LocationListener
	{
		public AutomationService parent = null;
		
		@Override
		public void onLocationChanged(Location up2DateLocation)
		{			
			stopGpsMeasurement();
			
			PointOfInterest.positionUpdate(up2DateLocation, parent, true, false);
			
			Miscellaneous.logEvent("i", "LocationListener", "Disarmed location listener, accuracy reached", 4);
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
	
	private static void startGpsMeasurement(AutomationService parentService)
	{
		// Arm location updates
		if(!gpsLocationListenerArmed)
		{
			Miscellaneous.logEvent("i", "PointOfInterest", "Unsatisfactory accuracy of network location. Performing comparison measurement via GPS.", 3);
			
			String myGpsComparisonProviderName;
			
			if(Settings.privacyLocationing)
			{
				Miscellaneous.logEvent("i", "PointOfInterest", parentService.getResources().getString(R.string.enforcingGps), 4);
				myGpsComparisonProviderName = LocationManager.GPS_PROVIDER;
			}
			else
			{
//				Miscellaneous.logEvent("i", "PointOfInterest", parentService.getResources().getString(R.string.notEnforcingGps), 4);
				Criteria crit = new Criteria();		
		//		crit.setPowerRequirement(Criteria.POWER_LOW);
		//		crit.setAltitudeRequired(false);
		//		crit.setSpeedRequired(false);
		//		crit.setBearingRequired(false);
				crit.setCostAllowed(true);
				crit.setAccuracy(Criteria.ACCURACY_FINE);
				gpsComparisonLocationManager = (LocationManager)parentService.getSystemService(parentService.LOCATION_SERVICE);
				myGpsComparisonProviderName = gpsComparisonLocationManager.getBestProvider(crit, true);
			}
			
			Miscellaneous.logEvent("i", "LocationListener", "Arming location listener, Provider: " + myGpsComparisonProviderName, 4);
			gpsComparisonLocationListener = new GpsComparisonLocationListener();
			gpsComparisonLocationListener.parent = parentService;
			gpsComparisonLocationManager.requestLocationUpdates(myGpsComparisonProviderName, Settings.minimumTimeBetweenUpdate, Settings.minimumDistanceChangeForNetworkUpdate, gpsComparisonLocationListener);
			gpsLocationListenerArmed = true;
			
			// set timeout
			Message message = new Message();
			message.what = 1;
			Miscellaneous.logEvent("i", parentService.getResources().getString(R.string.gpsComparison), parentService.getResources().getString(R.string.startingGpsTimeout), 4);
			if(timeoutHandler.parentService == null)
				timeoutHandler.parentService = parentService;
			timeoutHandler.sendMessageDelayed(message, Settings.gpsTimeout * 1000);
			timeoutHandlerActive = true;
		}
		else
			Miscellaneous.logEvent("i", "PointOfInterest", "Comparison measurement via GPS requested, but already active.", 3);
	}
	
	private static void stopGpsMeasurement()
	{
		if(gpsLocationListenerArmed)
		{
			gpsComparisonLocationManager.removeUpdates(gpsComparisonLocationListener);
			gpsLocationListenerArmed = false;
		}
		
		if(timeoutHandlerActive)
		{
			timeoutHandler.removeMessages(1);
			timeoutHandlerActive = false;
		}
		
	}
	
	private static class TimeoutHandler extends Handler
	{
		public AutomationService parentService = null;
		public Location locationToApplyIfGpsFails = null;
		
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			
			if(msg.what == 1)
			{
				Miscellaneous.logEvent("i", parentService.getResources().getString(R.string.gpsComparison), parentService.getResources().getString(R.string.gpsComparisonTimeoutStop), 4);
				stopGpsMeasurement();
			}
		}		
	}
	
	private boolean plausibilityCheck()
	{		
		double distance, minimumDistance, overlap;
		
		if(this.getName().equals("null"))
		{
			// Invalid name
			String text = Miscellaneous.getAnyContext().getResources().getString(R.string.invalidPoiName);
			Miscellaneous.logEvent("w", "POI", text, 2);
			Toast.makeText(Miscellaneous.getAnyContext(), text, Toast.LENGTH_LONG).show();
			return false;
		}
		
		for(PointOfInterest otherPoi : this.getPointOfInterestCollection())
		{
			distance = otherPoi.getLocation().distanceTo(this.getLocation());
			minimumDistance = otherPoi.getRadius()/2 + this.getRadius()/2;
			overlap = Math.round(Math.abs(distance - minimumDistance));
			
			if(distance <= minimumDistance && !otherPoi.getName().equals(this.getName()))
			{
				String text = String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.overlapBetweenPois), otherPoi.getName(), String.valueOf(overlap));
				Miscellaneous.logEvent("w", "POI", text, 2);
//				Miscellaneous.messageBox("POI", text, Miscellaneous.getAnyContext()).show();
				Toast.makeText(Miscellaneous.getAnyContext(), text, Toast.LENGTH_LONG).show();
				return false;
			}
		}
		Miscellaneous.logEvent("w", "POI", Miscellaneous.getAnyContext().getResources().getString(R.string.noOverLap), 2);
		
		return true;
	}

	public static boolean reachedPoiWithActivateWifiRule()
	{
		PointOfInterest activePoi = PointOfInterest.getActivePoi();
		if(activePoi != null)
		{
			for(Rule rule : Rule.findRuleCandidatesByPoi(activePoi, true))
			{
				for(Action action : rule.getActionSet())
				{
					if(action.getAction().equals(Action.Action_Enum.setWifi) && action.getParameter1())
					{
						// We are at a POI that specifies to enable wifi.
						return true;
					}
				}
			}
		}
		
		return false;
	}
}