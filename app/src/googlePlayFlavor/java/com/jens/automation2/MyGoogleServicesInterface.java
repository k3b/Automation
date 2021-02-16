package com.jens.automation2;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MiscellaneousWithGoogleServices
{
    public static boolean isPlayServiceAvailable()
    {
        GoogleApiAvailability aa = new GoogleApiAvailability();
        if(aa.isGooglePlayServicesAvailable(Miscellaneous.getAnyContext()) == ConnectionResult.SUCCESS)
            return true;
        else
            return false;
    }
}