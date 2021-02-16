package com.jens.automation2.location;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.jens.automation2.PointOfInterest;

import java.util.ArrayList;
import java.util.List;

public class GeofenceIntentService extends IntentService
{
    private GeofencingClient mGeofencingClient;
    protected static GoogleApiClient googleApiClient = null;
    PendingIntent geofencePendingIntent;

    static GeofenceIntentService instance;

    List<com.google.android.gms.location.Geofence> geoFenceList = new ArrayList<>();

    public static GeofenceIntentService getInstance()
    {
        if (instance == null)
            instance = new GeofenceIntentService("Automation");

        return instance;
    }

    public GeofenceIntentService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {

    }

    @Override
    public void onCreate()
    {
        mGeofencingClient = LocationServices.getGeofencingClient(this);

    }

    public void addFence(PointOfInterest poi)
    {
        com.google.android.gms.location.Geofence geofence = new com.google.android.gms.location.Geofence.Builder()
                .setRequestId(poi.getName()) // Geofence ID
                .setCircularRegion(poi.getLocation().getLatitude(), poi.getLocation().getLongitude(), (float) poi.getRadius()) // defining fence region
                .setExpirationDuration(com.google.android.gms.location.Geofence.NEVER_EXPIRE) // expiring date
                // Transition types that it should look for
                .setTransitionTypes(com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER | com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        geoFenceList.add(geofence);

        GeofencingRequest request = new GeofencingRequest.Builder()
                // Notification to trigger when the Geofence is created
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence) // add a Geofence
                .build();

        mGeofencingClient.removeGeofences(getGeofencePendingIntent());
    }

    public static void startService()
    {
        for (PointOfInterest poi : PointOfInterest.getPointOfInterestCollection())
            getInstance().addFence(poi);
    }

    public static void stopService()
    {
        for (PointOfInterest poi : PointOfInterest.getPointOfInterestCollection())
            getInstance().addFence(poi);
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */

    private PendingIntent getGeofencePendingIntent()
    {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null)
        {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;

    }
}