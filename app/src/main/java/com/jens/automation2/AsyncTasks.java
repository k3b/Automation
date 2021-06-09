package com.jens.automation2;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

public class AsyncTasks
{
    public static class AsyncTaskUpdateCheck extends AsyncTask<Context, Void, Boolean>
    {
        public static boolean checkRunning = false;

        @Override
        protected Boolean doInBackground(Context... contexts)
        {
            if(checkRunning)
                return false;
            else
                checkRunning = true;

            try
            {
                String result = Miscellaneous.downloadURL("https://server47.de/automation/?action=getLatestVersionCode", null, null).trim();
                int latestVersion = Integer.parseInt(result);

                // At this point the update check itself has already been successful.

                Settings.lastUpdateCheck = Calendar.getInstance().getTimeInMillis();
                Settings.writeSettings(contexts[0]);

                if (latestVersion > BuildConfig.VERSION_CODE)
                {
                    // There's a new update
                    return true;
                }
            }
            catch (Exception e)
            {
                Miscellaneous.logEvent("e", "Error checking for update", Log.getStackTraceString(e), 3);
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            try
            {
                ActivityMainScreen.getActivityMainScreenInstance().processUpdateCheckResult(result);
            }
            catch (NullPointerException e)
            {
                Miscellaneous.logEvent("e", "NewsDownload", "There was a problem displaying the update check result, probably ActivityMainScreen isn't currently shown: " + Log.getStackTraceString(e), 2);
            }
            catch (Exception e)
            {
                Miscellaneous.logEvent("e", "NewsDownload", "There was a problem displaying the update check result: " + Log.getStackTraceString(e), 2);
            }
        }
    }
}