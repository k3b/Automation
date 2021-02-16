package com.jens.automation2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.jens.automation2.location.LocationProvider;
import com.jens.automation2.receivers.PhoneStatusListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.jens.automation2.AutomationService.NOTIFICATION_CHANNEL_ID;
import static com.jens.automation2.AutomationService.channelName;

public class Miscellaneous extends Service
{	
	protected static String writeableFolderStringCache = null;
	public static final String lineSeparator = System.getProperty("line.separator");
	
	public static String downloadURL(String url, String username, String password)
	{
		HttpClient httpclient = new DefaultHttpClient();
		StringBuilder responseBody = new StringBuilder();
		boolean errorFound = false;
		
        try
        {
        	try
        	{
                URL urlObject = new URL(url);
                HttpURLConnection connection;
                
                if(url.toLowerCase().contains("https"))
                {
                	connection = (HttpsURLConnection) urlObject.openConnection();
                	
//                	if(Settings.httpAcceptAllCertificates)
//                	{
//                		SSLContext sc = SSLContext.getInstance("TLS");
//        	            sc.init(null, getInsecureTrustManager(), new java.security.SecureRandom());
//        	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//    	                Miscellaneous.disableSSLCertificateChecking();
//        	            HttpsURLConnection.setDefaultHostnameVerifier(getInsecureHostnameVerifier());
//                	}
                }
                else
                	connection = (HttpURLConnection) urlObject.openConnection();
                                
                // Add http simple authentication if specified
                if(username != null && password != null)
                {
                	String encodedCredentials = Base64.encodeToString(new String(username + ":" + password).getBytes(), Base64.DEFAULT);
	                connection.setRequestMethod("POST");
	                connection.setDoOutput(true);
	                connection.setRequestProperty  ("Authorization", "Basic " + encodedCredentials);
                }
                
                InputStream content = (InputStream)connection.getInputStream();
                BufferedReader in = new BufferedReader (new InputStreamReader (content));
                String line;
                while ((line = in.readLine()) != null)
                	responseBody.append(line + Miscellaneous.lineSeparator);
            } 
            catch(Exception e)
        	{
        	    Miscellaneous.logEvent("e", "HTTP error", Log.getStackTraceString(e), 3);
        	    errorFound = true;
            }
        }
        finally
        {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
            if(errorFound)
                return "httpError";
            else
                return responseBody.toString();
        }
	}
	
	public static String downloadURLwithoutCertificateChecking(String url, String username, String password)
	{
//		HttpClient httpclient = new DefaultHttpClient();
//		StringBuilder responseBody = new StringBuilder();
		boolean errorFound = false;

        try
        {
      	    HttpParams params = new BasicHttpParams();
      	    params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
      	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
      	    HttpClient httpclient = new DefaultHttpClient(params);
			httpclient = Actions.getInsecureSslClient(httpclient);
        	
      	    HttpPost httppost = new HttpPost(url);
      	    
            // Add http simple authentication if specified
            if(username != null && password != null)
            {
            	String encodedCredentials = Base64.encodeToString(new String(username + ":" + password).getBytes(), Base64.DEFAULT);
//	      	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	      	    httppost.addHeader("Authorization", "Basic " + encodedCredentials);
//	      	    nameValuePairs.add(new BasicNameValuePair("Authorization", "Basic " + encodedCredentials));
//	      	    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            }
            
      	    HttpResponse response = httpclient.execute(httppost);
      	    HttpEntity entity = response.getEntity();
      	    if (entity != null)
      	    {
//      	      System.out.println(EntityUtils.toString(entity));
      	    	return EntityUtils.toString(entity);
      	    }
        }
        catch(Exception e)
        {
            Miscellaneous.logEvent("e", "HTTP error", Log.getStackTraceString(e), 3);
            errorFound = true;
            return "httpError";
        }
//        finally
//        {
//            // When HttpClient instance is no longer needed,
//            // shut down the connection manager to ensure
//            // immediate deallocation of all system resources
//            httpclient.getConnectionManager().shutdown();
//            return responseBody.toString();
//        }
        
        return null;
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
//	public static void logEvent(String type, String header, String description)
//	{
//		if(type.equals("e"))
//			Log.e(header, description);
//		
//		if(type.equals("w"))
//			Log.w(header, description);
//		
//		if(type.equals("i"))
//			Log.i(header, description);
//		
//		if(Settings.writeLogFile)
//			writeToLogFile(type, header, description);
//	}
	
	public static void logEvent(String type, String header, String description, int logLevel)
	{
	    try
        {
            header = getAnyContext().getResources().getString(R.string.app_name);
        }
        catch(NullPointerException e)
        {
            header = "Automation";
        }

		if(type.equals("e"))
			Log.e(header, description);

		if(type.equals("w"))
			Log.w(header, description);
		
		if(type.equals("i"))
			Log.i(header, description);
		
		if(Settings.writeLogFile && Settings.logLevel >= logLevel)
		{
			writeToLogFile(type, header, description);

			if(!logCleanerRunning && Math.random() < 0.1)	// tidy up with 10% probability
			{
				rotateLogFile(getLogFile());
			}
		}
	}

	protected static boolean logCleanerRunning = false;
	protected static void rotateLogFile(File logFile)
	{
			logCleanerRunning = true;


			long maxSizeInBytes = (long)Settings.logFileMaxSize * 1024 * 1024;

			if(logFile.exists() && logFile.length() > (maxSizeInBytes))
			{
				Miscellaneous.logEvent("i", "Logfile", "Cleaning up log file.", 3);
				File archivedLogFile = new File(getWriteableFolder() + "/" + logFileName + "-old");
				logFile.renameTo(archivedLogFile);
				Miscellaneous.logEvent("i", "Logfile", "Cleaning up log file finished. Old log renamed to " + archivedLogFile.getAbsolutePath(), 3);
			}

			logCleanerRunning = false;
	}

	protected static boolean testFolder(String folderPath)
	{
		File folder = new File(folderPath + "/" + Settings.folderName);
		final String testFileName = "AutomationTestFile.txt";

		try
		{
			if(folder.exists() || folder.mkdirs())
			{
				XmlFileInterface.migrateFilesFromRootToFolder(folderPath, folder.getAbsolutePath());

				File testFile = new File(folder + "/" + testFileName);
				if(!testFile.exists())
					testFile.createNewFile();

				if(testFile.canRead() && testFile.canWrite())
				{
					testFile.delete();
					writeableFolderStringCache = testFile.getParent();
					Miscellaneous.logEvent("i", "File", "Test of " + folder.getAbsolutePath() + " succeeded.", 3);
					return true;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		Miscellaneous.logEvent("w", "File", "Test of " + folder.getAbsolutePath() + " failed.", 3);
		return false;
	}

	public static String getWriteableFolder()
	{
		if(writeableFolderStringCache == null)
		{
			String testPath = null;
			File folder = null;

			try
			{
				String[] foldersToTestArray = new String[]
						{
								Environment.getExternalStorageDirectory().getAbsolutePath(),
								"/storage/emulated/0",
								"/HWUserData",
								"/mnt/sdcard"
						};

				for(String f : foldersToTestArray)
				{
					if (testFolder(f))
					{
						String pathToUse = f + "/" + Settings.folderName;
						Miscellaneous.logEvent("i", "Path", "Using " + pathToUse + " to store settings and log.", 2);
//						Toast.makeText(getAnyContext(), "Using " + pathToUse + " to store settings and log.", Toast.LENGTH_LONG).show();
						return pathToUse;
					}
					else
						Miscellaneous.logEvent("e", "getWritableFolder", folder.getAbsolutePath() + " does not exist and could not be created.", 3);
				}
			}
			catch(Exception e)
			{
				Log.w("getWritableFolder", folder + " not writable.");
			}
			
			// do not change to logEvent() - we can't write
			Toast.makeText(getAnyContext(), "No writable folder could be found.", Toast.LENGTH_LONG).show();
			Log.e("getWritableFolder", "No writable folder could be found.");
			
			return null;
		}
		else
			return writeableFolderStringCache;
	}

	protected final static String logFileName = "Automation_logfile.txt";
	protected static File getLogFile()
	{
		File logFile = null;
		logFile = new File(getWriteableFolder() + "/" + logFileName);
		if(!logFile.exists())
		{
			Log.i("LogFile", "Creating new logfile: " + logFile.getAbsolutePath());
			try
			{
				logFile.createNewFile();
			}
			catch(Exception e)
			{
				Log.e("LogFile", "Error writing logs to file: " + e.getMessage());
			}
		}

		return logFile;
	}
	private static void writeToLogFile(String type, String header, String description)
	{
		try
		{
			FileWriter fileWriter = new FileWriter(getLogFile(), true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			Date date = new Date();
			
			bufferedWriter.write("\n" + date + ": " + type + " / " + header + " / " + description);
			bufferedWriter.close();
			
//			Log.i("LogFile", "Log entry written.");
		}
		catch(Exception e)
		{
			Log.e("LogFile", "Error writing logs to file: " + e.getMessage());
		}
	}
	
	public static boolean isAndroidEmulator()
	{
		String TAG = "EmulatorTest";
	    String model = Build.MODEL;
//	    Miscellaneous.logEvent("i", TAG, "model=" + model);
	    String product = Build.PRODUCT;
//	    Miscellaneous.logEvent("i", TAG, "product=" + product);
	    boolean isEmulator = false;
	    if (product != null)
	    {
	        isEmulator = product.equals("sdk") || product.contains("_sdk") || product.contains("sdk_");
	    }
//	    Miscellaneous.logEvent("i", TAG, "isEmulator=" + isEmulator);
	    return isEmulator;
	}
	
	public static int compareTimes(Time time1, Time time2)
	{
//		Miscellaneous.logEvent("i", "TimeCompare", "To compare: " + time1.toString() + " / " + time2.toString());
		
		if(time1.getHours() == time2.getHours() && time1.getMinutes() == time2.getMinutes())
		{
//			Miscellaneous.logEvent("i", "TimeCompare", "Times are equal.");
			return 0;
		}
		
		if(time1.getHours() > time2.getHours())
		{
//			Miscellaneous.logEvent("i", "TimeCompare", "Time1 is bigger/later by hours.");
			return -1;
		}
		
		if(time1.getHours() < time2.getHours())
		{
//			Miscellaneous.logEvent("i", "TimeCompare", "Time2 is bigger/later by hours.");
			return 1;
		}
		
		if(time1.getHours() == time2.getHours())
		{
			if(time1.getMinutes() < time2.getMinutes())
			{
//				Miscellaneous.logEvent("i", "TimeCompare", "Hours are equal. Time2 is bigger/later by minutes.");
				return 1;
			}
			
			if(time1.getMinutes() > time2.getMinutes())
			{
//				Miscellaneous.logEvent("i", "TimeCompare", "Hours are equal. Time1 is bigger/later by minutes.");
				return -1;
			}
		}
		

		Miscellaneous.logEvent("i", "TimeCompare", "Default return code. Shouldn't be here.", 5);
		return 0;
		
	}
	
	public static String convertStreamToString(InputStream is)
	{
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	public static Context getAnyContext()
	{
		Context returnContext;

		returnContext = AutomationService.getInstance();
		if(returnContext != null)
			return returnContext;

		returnContext = ActivityMainScreen.getActivityMainScreenInstance();
		if(returnContext != null)
			return returnContext;

		returnContext = ActivityPermissions.getInstance().getApplicationContext();
		if(returnContext != null)
			return returnContext;
		
		return null;
	}
	
	@SuppressLint("NewApi")
	public static String replaceVariablesInText(String source, Context context) throws Exception
	{
		// Replace variable with actual content
//		Miscellaneous.logEvent("i", "Raw source", source);
		if(source.contains("[uniqueid]"))
			source = source.replace("[uniqueid]", Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
		
		if(source.contains("[latitude]") | source.contains("[longitude]"))
		{
			if(LocationProvider.getLastKnownLocation() != null)
			{
				source = source.replace("[latitude]", String.valueOf(LocationProvider.getLastKnownLocation().getLatitude()));
				source = source.replace("[longitude]", String.valueOf(LocationProvider.getLastKnownLocation().getLongitude()));
			}
			else
			{
				Miscellaneous.logEvent("w", "TriggerURL", context.getResources().getString(R.string.triggerUrlReplacementPositionError), 3);
			}
		}
		
		if(source.contains("[phonenr]"))
		{
			String lastPhoneNr = PhoneStatusListener.getLastPhoneNumber();
			
			if(lastPhoneNr != null && lastPhoneNr.length() > 0)
				source = source.replace("[phonenr]", PhoneStatusListener.getLastPhoneNumber());
			else
				Miscellaneous.logEvent("w", "TriggerURL", context.getResources().getString(R.string.triggerUrlReplacementPositionError), 3);
		}
		
		if(source.contains("[serialnr]"))
			if(Build.VERSION.SDK_INT > 8)
				source = source.replace("[serialnr]", Secure.getString(context.getContentResolver(), Build.SERIAL));
			else
				source = source.replace("[serialnr]", "serialUnknown");

		if(
				source.contains("[d]") |
				source.contains("[m]") |
				source.contains("[Y]") |
				source.contains("[h]") |
				source.contains("[H]") |
				source.contains("[i]") |
				source.contains("[s]") |
				source.contains("[ms]")
			)
		{
			Calendar cal = Calendar.getInstance();

			source = source.replace("[d]", String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
			source = source.replace("[m]", String.valueOf(cal.get(Calendar.MONTH)));
			source = source.replace("[Y]", String.valueOf(cal.get(Calendar.YEAR)));
			source = source.replace("[h]", String.valueOf(cal.get(Calendar.HOUR)));
			source = source.replace("[H]", String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
			source = source.replace("[i]", String.valueOf(cal.get(Calendar.MINUTE)));
			source = source.replace("[s]", String.valueOf(cal.get(Calendar.SECOND)));
			source = source.replace("[ms]", String.valueOf(cal.get(Calendar.MILLISECOND)));
		}
		
//		Miscellaneous.logEvent("i", "URL after replace", source);
		
		return source;
	}
		
	/**
	 * Write a log entry and exit the application, so the crash is actually visible.
	 * Might even cause the activity to be automatically restarted by the OS.
	 */
	public static UncaughtExceptionHandler uncaughtExceptionHandler = new UncaughtExceptionHandler()
	{			
		@Override
		public void uncaughtException(Thread thread, Throwable ex)
		{
			Miscellaneous.logEvent("e", "UncaughtException", Log.getStackTraceString(ex), 1);
			System.exit(0);
		}
	};
	
	public static AlertDialog messageBox(String title, String message, Context context)
	{
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		alertDialog.setTitle(title);
		alertDialog.setMessage(message);		

		alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				dialog.dismiss();
			}
		});

//		alertDialog.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
//		{
//			public void onClick(DialogInterface dialog, int whichButton)
//			{
//				// Canceled.
//			}
//		});

		return alertDialog.create();
	}
	
	/**
	   * Checks if the device is rooted.
	   *
	   * @return <code>true</code> if the device is rooted, <code>false</code> otherwise.
	   */
	  public static boolean isPhoneRooted()
	  {
	    // get from build info
	    String buildTags = Build.TAGS;
	    if (buildTags != null && buildTags.contains("test-keys")) {
	      return true;
	    }

	    // check if /system/app/Superuser.apk is present
	    try
	    {
	      File file = new File("/system/app/Superuser.apk");
	      if (file.exists())
	      {
	        return true;
	      }
	    }
	    catch (Exception e1)
	    {
	      // ignore
	    }

	    // try executing commands
	    return 	canExecuteCommand("/system/xbin/which su")
	    			||
	    		canExecuteCommand("/system/bin/which su")
	    			||
	    		canExecuteCommand("which su");
	  }

	  // executes a command on the system
	  private static boolean canExecuteCommand(String command)
	  {
	    boolean executedSuccesfully;
	    try
	    {
	      Runtime.getRuntime().exec(command);
	      executedSuccesfully = true;
	    }
	    catch (Exception e)
	    {
	      executedSuccesfully = false;
	    }

	    return executedSuccesfully;
	  }
	  
	  public static boolean isNumeric(String str)
	  {
	    return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	  }
	  
	  /**
     * Disables the SSL certificate checking for new instances of {@link HttpsURLConnection} This has been created to
     * aid testing on a local box, not for use on production.
     */
	    private static void disableSSLCertificateChecking()
	    {
	        try
	        {	            
	        	SSLSocketFactory ssf = null;
	        	
	            try
	    	    {
	    	        SSLContext ctx = SSLContext.getInstance("TLS");
	    	        
    	    		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    	    		trustStore.load(null, null);
    	    		ssf = new MySSLSocketFactoryInsecure(trustStore);
    		        ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    		        ctx.init(null, null, null);

//	    	        return new DefaultHttpClient(ccm, client.getParams());
	    	    }
	    	    catch (Exception ex)
	    	    {
	    	    	ex.printStackTrace();
//	    	        return null;
	    	    }
	            
	        	// Install the all-trusting trust manager
	            SSLContext sc = SSLContext.getInstance("TLS");
	            sc.init(null, getInsecureTrustManager(), new java.security.SecureRandom());
//	            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//	            HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
	            
	            // Install the all-trusting host verifier
	            HttpsURLConnection.setDefaultHostnameVerifier(getInsecureHostnameVerifier());
	            HttpsURLConnection.setDefaultHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	    	}
	        catch (KeyManagementException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        catch (NoSuchAlgorithmException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        finally
	        {
	        		
	        }
	}
	    
   public static TrustManager[] getInsecureTrustManager()
   {
		TrustManager[] trustAllCerts = 
				new TrustManager[]
		        { 
			        new X509TrustManager()
			        {
			            public X509Certificate[] getAcceptedIssuers()
			            {
			                return null;
			            }
		
			            @Override
			            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
			            {
			                // Not implemented
			            }
		
			            @Override
			            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
			            {
			                // Not implemented
			            }
			        }
		        };
		
		return trustAllCerts;
   }
   
   public static HostnameVerifier getInsecureHostnameVerifier()
   {
	   HostnameVerifier allHostsValid = new HostnameVerifier()
       {
           public boolean verify(String hostname, SSLSession session)
           {
               return true;
           }
       };
       
       return allHostsValid;
   }

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static void createDismissableNotification(String textToDisplay, int notificationId, PendingIntent pendingIntent)
	{
		NotificationManager mNotificationManager = (NotificationManager) Miscellaneous.getAnyContext().getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder dismissableNotificationBuilder = createDismissableNotificationBuilder(pendingIntent);
		dismissableNotificationBuilder.setContentText(textToDisplay);
		dismissableNotificationBuilder.setContentIntent(pendingIntent);
		dismissableNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(textToDisplay));

		Notification dismissableNotification = dismissableNotificationBuilder.build();

		mNotificationManager.notify(notificationId, dismissableNotification);

		/*NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher) // notification icon
				.setContentTitle("Notification!") // title for notification
				.setContentText("Hello word") // message for notification
				.setAutoCancel(true); // clear notification after click
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this,0,intent,Intent.FLAG_ACTIVITY_NEW_TASK);
		mBuilder.setContentIntent(pi);
		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, dismissableNotification);*/
	}

	/*protected static Notification.Builder createDismissableNotificationBuilder()
	{
		Notification.Builder builder = new Notification.Builder(AutomationService.getInstance());
		builder.setContentTitle("Automation");
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setCategory(Notification.CATEGORY_EVENT);
		builder.setWhen(System.currentTimeMillis());

		//static PendingIntent myPendingIntent = PendingIntent.getActivity(this, 0, myIntent, 0);

		//builder.setContentIntent(myPendingIntent);

//		Notification defaultNotification = new Notification();
*//*		Notification defaultNotification = builder.build();

		defaultNotification.icon = R.drawable.ic_launcher;
		defaultNotification.when = System.currentTimeMillis();

//		defaultNotification.defaults |= Notification.DEFAULT_VIBRATE;
//		defaultNotification.defaults |= Notification.DEFAULT_LIGHTS;

		defaultNotification.flags |= Notification.FLAG_AUTO_CANCEL;
//		defaultNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
		defaultNotification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

//		defaultNotification.ledARGB = Color.YELLOW;
//		defaultNotification.ledOnMS = 1500;
//		defaultNotification.ledOffMS = 1500;
*//*
		return builder;
	}*/

	protected static NotificationCompat.Builder createDismissableNotificationBuilder(PendingIntent myPendingIntent)
	{
		NotificationManager mNotificationManager = (NotificationManager) AutomationService.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder builder;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
//			chan.setLightColor(Color.BLUE);
//			chan.enableVibration(false);
//			chan.setSound(null, null);
			chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
			mNotificationManager.createNotificationChannel(chan);

			builder = new NotificationCompat.Builder(AutomationService.getInstance(), NOTIFICATION_CHANNEL_ID);
		}
		else
			builder = new NotificationCompat.Builder(AutomationService.getInstance());

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			builder.setCategory(Notification.CATEGORY_SERVICE);

		builder.setWhen(System.currentTimeMillis());
		builder.setContentIntent(myPendingIntent);

		builder.setContentTitle(AutomationService.getInstance().getResources().getString(R.string.app_name));
//		builder.setOnlyAlertOnce(true);

		builder.setSmallIcon(R.drawable.priority);

//		builder.setContentText(textToDisplay);
//		builder.setSmallIcon(icon);
//		builder.setStyle(new NotificationCompat.BigTextStyle().bigText(textToDisplay));

		return builder;
	}

	public static String explode(ArrayList<String> arrayList)
	{
		StringBuilder builder = new StringBuilder();
		for(String s : arrayList)
			builder.append(s);

		return builder.toString();
	}

	public static boolean isGooglePlayInstalled(Context context)
	{
//		return false;
		PackageManager pm = context.getPackageManager();
		boolean app_installed = false;
		try
		{
			PackageInfo info = pm.getPackageInfo("com.android.vending", PackageManager.GET_ACTIVITIES);
			String label = (String) info.applicationInfo.loadLabel(pm);
			app_installed = (label != null && !label.equals("Market"));
		}
		catch (PackageManager.NameNotFoundException e)
		{
			app_installed = false;
		}
		return app_installed;
	}

	public static double round(double value, int places)
	{
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(Double.toString(value));
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static String getRealPathFromURI(Context context, Uri contentUri)
	{
		Cursor cursor = null;
		try
		{
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		catch (Exception e)
		{
			Miscellaneous.logEvent("e", "Uri", "getRealPathFromURI Exception : " + Log.getStackTraceString(e), 1);
			return null;
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}
	}

	public static Method getClassMethodReflective(String className, String methodName)
	{
		Class atRecClass = null;
		try
		{
			atRecClass = Class.forName("ActivityDetectionReceiver");
			for(Method m : atRecClass.getMethods())
			{
				if(m.getName().equalsIgnoreCase("isPlayServiceAvailable"))
				{
					return m;
				}
			}
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		return null;
	}
}