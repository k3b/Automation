package com.jens.automation2.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.jens.automation2.Miscellaneous;

import java.util.List;

import static eu.chainfire.libsuperuser.Debug.TAG;

public class GeofenceBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError())
        {
//            Miscellaneous.logEvent("i", "Geofence", geofenceTransitionDetails, 2);
//            String errorMessage = GeofenceStatusCodes.getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, "Geofence error");
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = "something happened";//getGeofenceTransitionDetails(this, geofenceTransition, triggeringGeofences);

            // Send notification and log the transition details.
            Miscellaneous.logEvent("i", "Geofence", geofenceTransitionDetails, 2);
            Log.i(TAG, geofenceTransitionDetails);
        }
        else
        {
            // Log the error.
//            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
            Log.e("Geofence", String.valueOf(geofenceTransition));
        }
    }
}
