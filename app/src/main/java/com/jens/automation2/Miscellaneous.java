package com.jens.automation2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.jens.automation2.location.LocationProvider;
import com.jens.automation2.receivers.NotificationListener;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.documentfile.provider.DocumentFile;

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

    public static int boolToInt(boolean input)
    {
    	if(input)
    		return 1;
    	else
    		return 0;
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
			// Use the app-specific folder as new default.
			writeableFolderStringCache = Miscellaneous.getAnyContext().getFilesDir().getAbsolutePath();

			File newConfigFile = new File(writeableFolderStringCache + "/" + XmlFileInterface.settingsFileName);

			migration:
			if (!newConfigFile.exists())
			{
				if (ActivityPermissions.havePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Miscellaneous.getAnyContext()))
				{
					// We have the storage permission, probably because it's an old installation. Files should be migrated to app-specific folder.

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

						for (String f : foldersToTestArray)
						{
//							if (testFolder(f))
//							{
								String pathToUse = f + "/" + Settings.folderName;

//						Toast.makeText(getAnyContext(), "Using " + pathToUse + " to store settings and log.", Toast.LENGTH_LONG).show();
								// Migrate existing files
								File oldDirectory = new File(pathToUse);
								File newDirectory = new File(writeableFolderStringCache);
								File oldConfigFilePath = new File(pathToUse + "/" + XmlFileInterface.settingsFileName);
								if (oldConfigFilePath.exists() && oldConfigFilePath.canWrite())
								{
									Miscellaneous.logEvent("i", "Path", "Found old path " + pathToUse + " for settings and logs. Migrating old files to new directory.", 2);

									for (File fileToBeMoved : oldDirectory.listFiles())
									{
										File dstFile = new File(writeableFolderStringCache + "/" + fileToBeMoved.getName());

										/*
											For some stupid reason Android's file.moveTo can't move files between
											mount points. That's why we have to copy it and delete the src if successful.
										 */

										if(copyFileUsingStream(fileToBeMoved, dstFile))
											fileToBeMoved.delete();
									}

									String message = String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.filesHaveBeenMovedTo), newDirectory.getAbsolutePath());
									Miscellaneous.writeStringToFile(oldDirectory.getAbsolutePath() + "/readme.txt", message);
									break migration;
								}
//							}
						}
					} catch (Exception e)
					{
						Log.w("getWritableFolder", folder + " not writable.");
					}
				}
			}
		}

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

	public static boolean compare(String direction, String needle, String haystack)
	{
		// If only one of needle or haystack is null
		if(
				(needle == null && haystack != null)
					||
				(needle != null && haystack == null)
		)
			return false;

		switch(direction)
		{
			case Trigger.directionEquals:
				if(Miscellaneous.isRegularExpression(needle))
					return haystack.matches(needle);
				else
					return haystack.equalsIgnoreCase(needle);
			case Trigger.directionNotEquals:
				return !haystack.equalsIgnoreCase(needle);
			case Trigger.directionContains:
				return haystack.toLowerCase().contains(needle.toLowerCase());
			case Trigger.directionNotContains:
				return !haystack.toLowerCase().contains(needle.toLowerCase());
			case Trigger.directionStartsWith:
				return haystack.toLowerCase().startsWith(needle.toLowerCase());
			case Trigger.directionEndsWith:
				return haystack.toLowerCase().endsWith(needle.toLowerCase());
			default:
				return false;
		}
	}
	
	public static int compareTimes(TimeObject time1, TimeObject time2)
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

	public static boolean isDarkModeEnabled(Context context)
	{
		int mode = context.getResources().getConfiguration().uiMode;
		switch(mode)
		{
			case 33:
			case Configuration.UI_MODE_NIGHT_YES:
				return true;
			case 17:
			case Configuration.UI_MODE_NIGHT_NO:
			case Configuration.UI_MODE_NIGHT_UNDEFINED:
			default:
				return false;
		}
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

		if(source.contains("[notificationTitle]"))
		{
			if(NotificationListener.getLastNotification() != null)
			{
				String notificationTitle = NotificationListener.getLastNotification().getTitle();

				if (notificationTitle != null && notificationTitle.length() > 0)
					source = source.replace("[notificationTitle]", notificationTitle);
				else
				{
					source = source.replace("[notificationTitle]", "notificationTitle unknown");
					Miscellaneous.logEvent("w", "Variable replacement", "notificationTitle was empty.", 3);
				}
			}
			else
			{
				source = source.replace("[notificationTitle]", "notificationTitle unknown");
				Miscellaneous.logEvent("w", "Variable replacement", "lastNotification was empty.", 3);
			}
		}

		if(source.contains("[notificationText]"))
		{
			if(NotificationListener.getLastNotification() != null)
			{
				String notificationText = NotificationListener.getLastNotification().getText();

				if (notificationText != null && notificationText.length() > 0)
					source = source.replace("[notificationText]", notificationText);
				else
				{
					source = source.replace("[notificationText]", "notificationText unknown");
					Miscellaneous.logEvent("w", "Variable replacement", "notificationText was empty.", 3);
				}
			}
			else
			{
				source = source.replace("[notificationText]", "notificationText unknown");
				Miscellaneous.logEvent("w", "Variable replacement", "lastNotification was empty.", 3);
			}
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

		return alertDialog.create();
	}

	private boolean haveNetworkConnection()
	{
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected())
					haveConnectedWifi = true;
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected())
					haveConnectedMobile = true;
		}
		return haveConnectedWifi || haveConnectedMobile;
	}
	
	/**
	   * Checks if the device is rooted.
	   *
	   * @return <code>true</code> if the device is rooted, <code>false</code> otherwise.
	   */
	public static boolean isPhoneRooted()
	{
//	  	if(true)
//	  		return true;

	    // get from build info
	    String buildTags = Build.TAGS;
	    if (buildTags != null && buildTags.contains("test-keys"))
	    {
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
	public static boolean isNumericDecimal(String strNum)
	{
		if (strNum == null)
		{
			return false;
		}
		try
		{
			double d = Double.parseDouble(strNum);
		}
		catch (NumberFormatException nfe)
		{
			return false;
		}
		return true;
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
	public static void createDismissibleNotificationWithDelay(long delay, String title, String textToDisplay, int notificationId, String notificationChannelId, PendingIntent pendingIntent)
	{
		/*
			Now what's this about?
			From SDK 27 onwards you can only fire 1 notification per second:
			https://developer.android.com/about/versions/oreo/android-8.1?hl=bn#notify

			There are some situations where the service is just being started - resulting in a notification. But we have
			additional need to inform the user about something and want to create another notification. That's why we have
			to delay it for a moment, but don't want to hold off the main threat.
		 */

		class AsyncTaskCreateNotification extends AsyncTask<Void, Void, Void>
		{
			@Override
			protected Void doInBackground(Void... voids)
			{
				setDefaultBehaviour(this);

				try
				{
					Thread.sleep(delay);
				}
				catch(Exception e)
				{}

				createDismissibleNotification(title, textToDisplay, notificationId, true, notificationChannelId, pendingIntent);

				return null;
			}
		}

		AsyncTaskCreateNotification astCn = new AsyncTaskCreateNotification();
		astCn.execute(null, null);
	}

	private static void setDefaultBehaviour(AsyncTask asyncTask)
	{
		// without this line debugger will - for some reason - skip all breakpoints in this class
		if(android.os.Debug.isDebuggerConnected())
			android.os.Debug.waitForDebugger();

//		Thread.setDefaultUncaughtExceptionHandler(Miscellaneous.getUncaughtExceptionHandler(activityMainRef, true));
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static void createDismissibleNotification(String title, String textToDisplay, int notificationId, boolean vibrate, String notificationChannelId, PendingIntent pendingIntent)
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			createDismissibleNotificationSdk26(title, textToDisplay, notificationId, vibrate, notificationChannelId, pendingIntent);
			return;
		}

		NotificationManager mNotificationManager = (NotificationManager) Miscellaneous.getAnyContext().getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder dismissibleNotificationBuilder = createDismissibleNotificationBuilder(vibrate, notificationChannelId, pendingIntent);

		if(title == null)
			dismissibleNotificationBuilder.setContentTitle(AutomationService.getInstance().getResources().getString(R.string.app_name));
		else
			dismissibleNotificationBuilder.setContentTitle(title);

		dismissibleNotificationBuilder.setContentText(textToDisplay);
		dismissibleNotificationBuilder.setContentIntent(pendingIntent);
		dismissibleNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(textToDisplay));
		dismissibleNotificationBuilder.setAutoCancel(true);

		if(notificationChannelId.equals(AutomationService.NOTIFICATION_CHANNEL_ID_RULES))
			dismissibleNotificationBuilder.setSmallIcon(R.drawable.info);

		Notification dismissibleNotification = dismissibleNotificationBuilder.build();

		mNotificationManager.notify(notificationId, dismissibleNotification);
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	static NotificationChannel findExistingChannel(List<NotificationChannel> channels, String channelId)
	{
		for(NotificationChannel c : channels)
		{
			if(c.getId().equals(channelId))
				return c;
		}

		return null;
	}
	@RequiresApi(api = Build.VERSION_CODES.O)
	static NotificationChannel getNotificationChannel(String channelId)
	{
		NotificationManager nm = (NotificationManager) Miscellaneous.getAnyContext().getSystemService(Context.NOTIFICATION_SERVICE);
		List<NotificationChannel> channels = nm.getNotificationChannels();

		if(!Settings.hasBeenDone(Settings.constNotificationChannelCleanupApk118) && BuildConfig.VERSION_CODE < 120)
		{
			// Perform a one-time cleanup of notification channels as they have been redesigned.

			for(NotificationChannel c : channels)
				nm.deleteNotificationChannel(c.getId());

			Settings.considerDone(Settings.constNotificationChannelCleanupApk118);
			Settings.writeSettings(Miscellaneous.getAnyContext());
		}

		NotificationChannel channel = findExistingChannel(channels, channelId);

		if(channel == null)
		{
			switch (channelId)
			{
				case AutomationService.NOTIFICATION_CHANNEL_ID_SERVICE:
					channel = new NotificationChannel(AutomationService.NOTIFICATION_CHANNEL_ID_SERVICE, AutomationService.NOTIFICATION_CHANNEL_NAME_SERVICE, NotificationManager.IMPORTANCE_LOW);
					break;
				case AutomationService.NOTIFICATION_CHANNEL_ID_FUNCTIONALITY:
					channel = new NotificationChannel(AutomationService.NOTIFICATION_CHANNEL_ID_FUNCTIONALITY, AutomationService.NOTIFICATION_CHANNEL_NAME_FUNCTIONALITY, NotificationManager.IMPORTANCE_HIGH);
					break;
				case AutomationService.NOTIFICATION_CHANNEL_ID_RULES:
					channel = new NotificationChannel(AutomationService.NOTIFICATION_CHANNEL_ID_RULES, AutomationService.NOTIFICATION_CHANNEL_NAME_RULES, NotificationManager.IMPORTANCE_HIGH);
					break;
				default:
					break;
			}
		}

		return channel;
	}

	static void createDismissibleNotificationSdk26(String title, String textToDisplay, int notificationId, boolean vibrate, String notificationChannelId, PendingIntent pendingIntent)
	{
		NotificationManager mNotificationManager = (NotificationManager) AutomationService.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder builder;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			NotificationChannel notificationChannel = getNotificationChannel(notificationChannelId);
//			notificationChannel.setLightColor(Color.BLUE);
			notificationChannel.enableVibration(vibrate);
			try
			{
				Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//				Uri notificationSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" +mContext.getPackageName()+"/"+R.raw.apple_ring));
//				Ringtone r = RingtoneManager.getRingtone(Miscellaneous.getAnyContext(), notification);
				AudioAttributes.Builder b = new AudioAttributes.Builder();
				b.setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN);
				notificationChannel.setSound(notificationSound, b.build());
			}
			catch (Exception e)
			{
				Miscellaneous.logEvent("i", "Notification", Log.getStackTraceString(e), 2);
			}
			notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
			mNotificationManager.createNotificationChannel(notificationChannel);

			builder = new NotificationCompat.Builder(AutomationService.getInstance(), notificationChannel.getId());
		}
		else
			builder = new NotificationCompat.Builder(AutomationService.getInstance());

		builder.setWhen(System.currentTimeMillis());
		builder.setContentIntent(pendingIntent);

		if(title == null)
			builder.setContentTitle(AutomationService.getInstance().getResources().getString(R.string.app_name));
		else
			builder.setContentTitle(title);

		builder.setOnlyAlertOnce(true);

		if(Settings.showIconWhenServiceIsRunning && notificationChannelId.equals(AutomationService.NOTIFICATION_CHANNEL_ID_SERVICE))
			builder.setSmallIcon(R.drawable.ic_launcher);
		else if(!notificationChannelId.equals(AutomationService.NOTIFICATION_CHANNEL_ID_SERVICE))
			builder.setSmallIcon(R.drawable.info);

		builder.setContentText(textToDisplay);
		builder.setStyle(new NotificationCompat.BigTextStyle().bigText(textToDisplay));

		NotificationManager notificationManager = (NotificationManager) Miscellaneous.getAnyContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(1, builder.build());

//		Intent notifyIntent = new Intent(context, notification.class);
//		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//
//		pendingIntent.getIntentSender().g
//
//		PendingIntent pendingIntent = PendingIntent.getActivities(context, 0,
//				new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
//
//		Notification notification = new Notification.Builder(Miscellaneous.getAnyContext())
//				.setSmallIcon(android.R.drawable.ic_dialog_info)
//				.setContentTitle("Automation")
//				.setContentText(textToDisplay)
//				.setAutoCancel(true)
//				.setContentIntent(pendingIntent)
//				.build();
//		notification.defaults |= Notification.DEFAULT_SOUND;
//		NotificationManager notificationManager =
//				(NotificationManager) Miscellaneous.getAnyContext().getSystemService(Context.NOTIFICATION_SERVICE);
//		notificationManager.notify(1, notification);
	}

	protected static NotificationCompat.Builder createDismissibleNotificationBuilder(boolean vibrate, String notificationChannelId, PendingIntent myPendingIntent)
	{
		NotificationManager mNotificationManager = (NotificationManager) AutomationService.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder builder;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			NotificationChannel notificationChannel = getNotificationChannel(notificationChannelId);
//			notificationChannel.setLightColor(Color.BLUE);
			notificationChannel.enableVibration(vibrate);
//			notificationChannel.setSound(null, null);
			notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
			mNotificationManager.createNotificationChannel(notificationChannel);

			builder = new NotificationCompat.Builder(AutomationService.getInstance(), notificationChannelId);
		}
		else
			builder = new NotificationCompat.Builder(AutomationService.getInstance());

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			builder.setCategory(Notification.CATEGORY_SERVICE);

		builder.setAutoCancel(true);
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

	public static String explode(String glue, ArrayList<String> arrayList)
	{
		if(arrayList != null)
		{
			StringBuilder builder = new StringBuilder();
			for (String s : arrayList)
				builder.append(s + glue);

			if (builder.length() > 0)
				builder.delete(builder.length() - glue.length(), builder.length());

			return builder.toString();
		}
		else
			return "";
	}

	public static String explode(String glue, String[] inputArray)
	{
		if(inputArray != null)
		{
			StringBuilder builder = new StringBuilder();
			for (String s : inputArray)
				builder.append(s + glue);

			if (builder.length() > 0)
				builder.delete(builder.length() - glue.length(), builder.length());

			return builder.toString();
		}
		else
			return "";
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
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(Double.toString(value));
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static String getRealPathFromURI(Context context, Uri contentUri)
	{
		Cursor cursor = null;
		try
		{
			String[] proj = { MediaStore.Images.Media.DATA, MediaStore.Audio.Media.DATA };
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

	public static String getRealPathFromURI2(final Context context, final Uri uri)
	{
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri))
		{
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri))
			{
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type))
				{
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri))
			{

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri))
			{
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type))
				{
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				}
				else if ("video".equals(type))
				{
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				}
				else if ("audio".equals(type))
				{
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme()))
		{
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme()))
		{
			return uri.getPath();
		}

		return null;
	}

	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs)
	{
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try
		{
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst())
			{
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		}
		finally
		{
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	public static boolean isExternalStorageDocument(Uri uri)
	{
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	public static boolean isDownloadsDocument(Uri uri)
	{
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	public static boolean isMediaDocument(Uri uri)
	{
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	public static Method getClassMethodReflective(String className, String methodName)
	{
		Class foundClass = null;
		try
		{
			foundClass = Class.forName(className);
			for(Method m : foundClass.getDeclaredMethods())
			{
				if(m.getName().equalsIgnoreCase(methodName))
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

	public static Object runMethodReflective(String className, String methodName, Object[] params)
	{
		Method m = getClassMethodReflective(className, methodName);
		Object result = null;
		try
		{
			if(params == null)
				result = m.invoke((Object[]) null);
			else
				result = m.invoke(null, params);
		}
		catch (IllegalAccessException e)
		{
			Miscellaneous.logEvent("w", "runMethodReflective", Log.getStackTraceString(e),5 );
		}
		catch (InvocationTargetException e)
		{
			Miscellaneous.logEvent("w", "runMethodReflective", Log.getStackTraceString(e),5 );
		}

		return result;
	}

	public static boolean restrictedFeaturesConfigured()
	{
		if(Rule.isAnyRuleUsing(Trigger.Trigger_Enum.activityDetection))
		{
			try
			{
				Class testClass = Class.forName(ActivityManageRule.activityDetectionClassPath);
			}
			catch (ClassNotFoundException e)
			{
				return true;
			}
		}

		return false;
	}

	public static Element getXmlTree(String inputString) throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

//		Create a Document from a file or stream
		/*
		StringBuilder xmlStringBuilder = new StringBuilder();
		xmlStringBuilder.append("<?xml version="1.0"?> <class> </class>");
		ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
		*/
//		Document doc = builder.parse(input);
		Document doc = builder.parse(new InputSource(new StringReader(inputString)));

		Element rootElement = doc.getDocumentElement();

		return rootElement;
	/*
//		Examine attributes

		//returns specific attribute
		root.getAttribute("attributeName");

		//returns a Map (table) of names/values
		root.getAttributes();

//		Examine sub-elements

		//returns a list of subelements of specified name
		root.getElementsByTagName("subelementName");

		//returns a list of all child nodes
		root.getChildNodes();
	*/
	}

	public static Calendar calendarFromLong(long input)
	{
		Calendar returnValue = Calendar.getInstance();
		returnValue.setTimeInMillis(input);
		return returnValue;
	}

	public static boolean writeStringToFile(String filename, String input)
	{
		try
		{
			FileWriter myWriter = new FileWriter(filename);
			myWriter.write(input);
			myWriter.close();
			return true;
		}
		catch (IOException e)
		{
			Miscellaneous.logEvent("e", "Error writing to file", Log.getStackTraceString(e), 3);
			return false;
		}
	}

	public static String readFileToString(String fileName)
	{
		try
		{
			StringBuilder result = new StringBuilder();
			File myObj = new File(fileName);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine())
			{
				String data = myReader.nextLine();
				result.append(data);
			}
			myReader.close();

			return result.toString();
		}
		catch (FileNotFoundException e)
		{
			Miscellaneous.logEvent("e", "Error reading file " + fileName, Log.getStackTraceString(e), 3);
			return null;
		}
	}

	public static boolean copyFileUsingStream(File source, File dest) throws IOException
	{
		boolean returnValue = false;

		InputStream is = null;
		OutputStream os = null;
		try
		{
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0)
			{
				os.write(buffer, 0, length);
			}

			returnValue = true;
		}
		finally
		{
			is.close();
			os.close();
		}

		return returnValue;
	}

	public static boolean copyDocumentFileToFile(DocumentFile src, File dst)
	{
		InputStream in = null;
		OutputStream out = null;
		String error = null;

		try
		{
			in = Miscellaneous.getAnyContext().getContentResolver().openInputStream(src.getUri());
			out = new FileOutputStream(dst);

			byte[] buffer = new byte[1024];
			int read;

			while ((read = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, read);
			}

			in.close();
			// write the output file (You have now copied the file)
			out.flush();
			out.close();

			return true;
		}
		catch (FileNotFoundException fnfe1)
		{
			error = fnfe1.getMessage();
		}
		catch (Exception e)
		{
			error = e.getMessage();
		}

		return false;
//		return error;
	}

	public static boolean copyFileToDocumentFile(File src, DocumentFile dst)
	{
		InputStream in = null;
		OutputStream out = null;
		String error = null;

		try
		{
			in = new FileInputStream(src);
			out = Miscellaneous.getAnyContext().getContentResolver().openOutputStream(dst.getUri());

			byte[] buffer = new byte[1024];
			int read;

			while ((read = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, read);
			}

			in.close();
			// write the output file (You have now copied the file)
			out.flush();
			out.close();

			return true;
		}
		catch (FileNotFoundException fnfe1)
		{
			error = fnfe1.getMessage();
		}
		catch (Exception e)
		{
			error = e.getMessage();
		}

		return false;
//		return error;
	}

	/*public static String copyDocumentFile(String inputPath, String inputFile, Uri treeUri)
	{
		InputStream in = null;
		OutputStream out = null;
		String error = null;
		DocumentFile pickedDir = DocumentFile.fromTreeUri(getActivity(), treeUri);
		String extension = inputFile.substring(inputFile.lastIndexOf(".")+1,inputFile.length());

		try
		{
			DocumentFile newFile = pickedDir.createFile("audio/"+extension, inputFile);
			out = getActivity().getContentResolver().openOutputStream(newFile.getUri());
			in = new FileInputStream(inputPath + inputFile);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, read);
			}
			in.close();
			// write the output file (You have now copied the file)
			out.flush();
			out.close();

		}
		catch (FileNotFoundException fnfe1)
		{
			error = fnfe1.getMessage();
		}
		catch (Exception e)
		{
			error = e.getMessage();
		}

		return error;
	}*/

	public static boolean googleToBlameForLocation(boolean checkExistingRules)
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
		{
			if (BuildConfig.FLAVOR.equalsIgnoreCase(AutomationService.flavor_name_googleplay))
			{
				if(checkExistingRules)
				{
					if (Rule.isAnyRuleUsing(Trigger.Trigger_Enum.pointOfInterest))
					{
						return true;
					}
				}
				else
					return true;
			}
		}

		return false;
	}

	public static void zip(String[] _files, String zipFileName)
	{
		int BUFFER = 2048;
		try
		{
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(zipFileName);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					dest));
			byte data[] = new byte[BUFFER];

			for (int i = 0; i < _files.length; i++)
			{
				Log.v("Compress", "Adding: " + _files[i]);
				FileInputStream fi = new FileInputStream(_files[i]);
				origin = new BufferedInputStream(fi, BUFFER);

				ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
				out.putNextEntry(entry);
				int count;

				while ((count = origin.read(data, 0, BUFFER)) != -1)
				{
					out.write(data, 0, count);
				}
				origin.close();
			}

			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void unzip(String _zipFile, String _targetLocation)
	{
		int BUFFER = 2048;

		try
		{
			FileInputStream fin = new FileInputStream(_zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			while ((ze = zin.getNextEntry()) != null)
			{
				//create dir if required while unzipping
				if (ze.isDirectory())
				{
			//		dirChecker(ze.getName());
				}
				else
				{
					FileOutputStream fout = new FileOutputStream(_targetLocation + ze.getName());
					for (int c = zin.read(); c != -1; c = zin.read())
					{
						fout.write(c);
					}

					zin.closeEntry();
					fout.close();
				}

			}
			zin.close();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	public static void sendEmail(Context context, String targetAddress, String subject, String message, Uri fileAttachment)
	{
		try
		{
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{targetAddress});
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			if (fileAttachment != null)
			{
				emailIntent.putExtra(Intent.EXTRA_STREAM, fileAttachment);
			}
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
			context.startActivity(Intent.createChooser(emailIntent, "Sending email..."));
		}
		catch (Throwable t)
		{
			Toast.makeText(context, "Request failed try again: "+ t.toString(), Toast.LENGTH_LONG).show();
		}
	}

	public static boolean doesActivityExist(Intent intent, Context context)
	{
		return intent.resolveActivityInfo(context.getPackageManager(), 0) != null;
	}

	public static boolean isRegularExpression(String regex)
	{
		try
		{
			"compareString".matches(regex);	//will cause expection if no valid regex
			return true;
		}
		catch(java.util.regex.PatternSyntaxException e)
		{

		}

		return false;
	}

	public static boolean comparePhoneNumbers(String number1, String number2)
	{
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.S)
		{
			TelephonyManager tm = (TelephonyManager)Miscellaneous.getAnyContext().getSystemService(Context.TELEPHONY_SERVICE);
			return PhoneNumberUtils.areSamePhoneNumber(number1, number2, tm.getNetworkCountryIso());
		}
		else
			return PhoneNumberUtils.compare(number1, number2);
	}

	public static String formatDate(Date input)
	{
		DateFormat sdf = null;
		SimpleDateFormat fallBackFormatter = new SimpleDateFormat(Settings.dateFormat);

		if(sdf == null && Settings.dateFormat != null)
			sdf = new SimpleDateFormat(Settings.dateFormat);

		String formattedDate;
		if(sdf != null)
			formattedDate = sdf.format(input);
		else
			formattedDate = fallBackFormatter.format(input);

		return formattedDate;
	}

	public static boolean arraySearch(String[] haystack, String needle, boolean caseSensitive, boolean matchFullLine)
	{
		if(matchFullLine)
		{
			if(caseSensitive)
			{
				for (String s : haystack)
				{
					if (s.equals(needle))
						return true;
				}
			}
			else
			{
				for (String s : haystack)
				{
					if (s.toLowerCase().equals(needle.toLowerCase()))
						return true;
				}
			}
		}
		else
		{
			if(caseSensitive)
			{
				for (String s : haystack)
				{
					if (s.contains(needle))
						return true;
				}
			}
			else
			{
				for (String s : haystack)
				{
					if (s.toLowerCase().contains(needle.toLowerCase()))
						return true;
				}
			}
		}

		return false;
	}

	public static boolean arraySearch(ArrayList<String> requestList, String needle, boolean caseSensitive, boolean matchFullLine)
	{
		return arraySearch(requestList.toArray(new String[requestList.size()]), needle, caseSensitive, matchFullLine);
	}

	/**
	 * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
	 * @param context Context reference to get the TelephonyManager instance from
	 * @return country code or null
	 */
	public static String getUserCountry(Context context)
	{
		try
		{
			final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			final String simCountry = tm.getSimCountryIso();
			if (simCountry != null && simCountry.length() == 2)
			{ // SIM country code is available
				return simCountry.toLowerCase(Locale.US);
			}
			else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA)
			{ // device is not 3G (would be unreliable)
				String networkCountry = tm.getNetworkCountryIso();
				if (networkCountry != null && networkCountry.length() == 2)
				{ // network country code is available
					return networkCountry.toLowerCase(Locale.US);
				}
			}
		}
		catch (SecurityException se)
		{
			return "unknown";
		}
		catch (Exception e)
		{ }

		return null;
	}

	public static String checksumSha(String filepath) throws IOException
	{
		try
		{
			MessageDigest md = null;
			md = MessageDigest.getInstance("SHA-256");

			// file hashing with DigestInputStream
			try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md))
			{
				while (dis.read() != -1)
					; //empty loop to clear the data
				md = dis.getMessageDigest();
			}

			// bytes to hex
			StringBuilder result = new StringBuilder();
			for (byte b : md.digest())
			{
				result.append(String.format("%02x", b));
			}
			return result.toString();
		}
		catch (NoSuchAlgorithmException e)
		{
			Miscellaneous.logEvent("e", "shaChecksum", Log.getStackTraceString(e), 2);
		}

		return null;
	}
}