package com.jens.automation2.actions.wifi_router;

/*
    Class taken from here:
    https://github.com/aegis1980/WifiHotSpot
 */

public abstract class MyOnStartTetheringCallback
{
        /**
         * Called when tethering has been successfully started.
         */
        public abstract void onTetheringStarted();

        /**
         * Called when starting tethering failed.
         */
        public abstract void onTetheringFailed();
}