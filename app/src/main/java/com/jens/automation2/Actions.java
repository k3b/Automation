package com.jens.automation2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.jens.automation2.actions.wifi_router.MyOnStartTetheringCallback;
import com.jens.automation2.actions.wifi_router.MyOreoWifiManager;
import com.jens.automation2.location.WifiBroadcastReceiver;
import com.jens.automation2.receivers.ConnectivityReceiver;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.KeyStore;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;

import eu.chainfire.libsuperuser.Shell;

public class Actions
{
	public static AutomationService autoMationServerRef;
	public static Context context;
	public static Context rootetcontext;
	private static Intent playMusicIntent;
	private static boolean suAvailable = false;
	private static String suVersion = null;
	private static String suVersionInternal = null;
	private static List<String> suResult = null;
	public final static String smsSeparator = "&sms&";
	public final static String dummyPackageString = "dummyPkg239asd";

	public static final String wireguard_tunnel_up = "com.wireguard.android.action.SET_TUNNEL_UP";
	public static final String wireguard_tunnel_down = "com.wireguard.android.action.SET_TUNNEL_DOWN";
	public static final String wireguard_tunnel_refresh = "com.wireguard.android.action.REFRESH_TUNNEL_STATES";

	public static Boolean setWifi(Context context, Boolean desiredState, boolean toggleActionIfPossible)
	{
		Miscellaneous.logEvent("i", "Wifi", "Changing Wifi to " + String.valueOf(desiredState), 4);

		if (desiredState && Settings.useWifiForPositioning)
			WifiBroadcastReceiver.startWifiReceiver(autoMationServerRef.getLocationProvider());

		WifiManager myWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		// toggle
		if (toggleActionIfPossible)
		{
			Toast.makeText(context, context.getResources().getString(R.string.toggling) + " " + context.getResources().getString(R.string.wifi), Toast.LENGTH_LONG).show();
			desiredState = !myWifi.isWifiEnabled();
		}

		// Only perform action if necessary
		if ((!myWifi.isWifiEnabled() && desiredState) | (myWifi.isWifiEnabled() && !desiredState))
		{
			String wifiString = "";

			if (desiredState)
			{
				wifiString = context.getResources().getString(R.string.activating) + " " + context.getResources().getString(R.string.wifi);
			}
			else
			{
				wifiString = context.getResources().getString(R.string.deactivating) + " " + context.getResources().getString(R.string.wifi);
			}

			Toast.makeText(context, wifiString, Toast.LENGTH_LONG).show();

			boolean returnValue = myWifi.setWifiEnabled(desiredState);
			if (!returnValue)
				Miscellaneous.logEvent("i", "Wifi", "Error changing Wifi to " + String.valueOf(desiredState), 2);
			else
				Miscellaneous.logEvent("i", "Wifi", "Wifi changed to " + String.valueOf(desiredState), 2);

			return returnValue;
		}

		return true;
	}

	public static void setDisplayRotation(Context myContext, Boolean desiredState, boolean toggleActionIfPossible)
	{
		Miscellaneous.logEvent("i", "ScreenRotation", "Changing ScreenRotation to " + String.valueOf(desiredState), 4);
		try
		{
			if (toggleActionIfPossible)
			{
				Miscellaneous.logEvent("i", "setScreenRotation", myContext.getResources().getString(R.string.toggling), 2);
				boolean currentStatus = android.provider.Settings.System.getInt(myContext.getContentResolver(), android.provider.Settings.System.ACCELEROMETER_ROTATION, 0) == 0;
				if (currentStatus)
					desiredState = !currentStatus;
			}

			if (desiredState)
			{
				if (android.provider.Settings.System.getInt(myContext.getContentResolver(), android.provider.Settings.System.ACCELEROMETER_ROTATION, 0) == 0)
				{
					android.provider.Settings.System.putInt(myContext.getContentResolver(), android.provider.Settings.System.ACCELEROMETER_ROTATION, 1);
					Miscellaneous.logEvent("i", "setScreenRotation", myContext.getResources().getString(R.string.screenRotationEnabled), 2);
				}
				else
					Miscellaneous.logEvent("i", "setScreenRotation", myContext.getResources().getString(R.string.screenRotationAlreadyEnabled), 2);
			}
			else
			{
				if (android.provider.Settings.System.getInt(myContext.getContentResolver(), android.provider.Settings.System.ACCELEROMETER_ROTATION, 0) == 1)
				{
					android.provider.Settings.System.putInt(myContext.getContentResolver(), android.provider.Settings.System.ACCELEROMETER_ROTATION, 0);
					Miscellaneous.logEvent("i", "setScreenRotation", myContext.getResources().getString(R.string.screenRotationDisabled), 2);
				}
				else
					Miscellaneous.logEvent("i", "setScreenRotation", myContext.getResources().getString(R.string.screenRotationAlreadyDisabled), 2);
			}
		}
		catch (Exception e)
		{
			Miscellaneous.logEvent("e", "setScreenRotation", myContext.getResources().getString(R.string.errorChangingScreenRotation) + ": " + Log.getStackTraceString(e), 2);
		}
	}

	private static boolean isWifiApEnabled(Context context)
	{
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		boolean currentlyEnabled = false;

		Method[] methods = wifiManager.getClass().getDeclaredMethods();
		for (Method method : methods)
		{
			if (method.getName().equals("isWifiApEnabled"))
			{
				try
				{
					Object returnObject = method.invoke(wifiManager);
					currentlyEnabled = Boolean.valueOf(returnObject.toString());

					if (currentlyEnabled)
						Miscellaneous.logEvent("i", "isWifiApEnabled", "true", 5);
					else
						Miscellaneous.logEvent("i", "isWifiApEnabled", "false", 5);
				}
				catch (Exception e)
				{
					Miscellaneous.logEvent("i", "isWifiApEnabled", context.getResources().getString(R.string.errorDeterminingWifiApState) + ": " + e.getMessage(), 4);
				}
			}
		}
		return currentlyEnabled;
	}

	public static Boolean setWifiTethering(Context context, Boolean desiredState, boolean toggleActionIfPossible)
	{
		Miscellaneous.logEvent("i", "WifiTethering", "Changing WifiTethering to " + String.valueOf(desiredState), 4);

		boolean state = Actions.isWifiApEnabled(context);

		if (toggleActionIfPossible)
		{
			Miscellaneous.logEvent("i", "WifiAp", context.getResources().getString(R.string.toggling), 2);
			desiredState = !state;
		}

		if (((state && !desiredState) || (!state && desiredState)))
		{
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
			{
				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				Method[] methods = wifiManager.getClass().getDeclaredMethods();
				for (Method method : methods)
				{
					Miscellaneous.logEvent("i", "WifiAp", "Trying to find appropriate method... " + method.getName(), 5);
					if (method.getName().equals("setWifiApEnabled"))
					{
						try
						{
							String desiredString = "";
							if (desiredState)
								desiredString = "activate";
							else
								desiredString = "deactivate";

							if (!toggleActionIfPossible)
							{
								Miscellaneous.logEvent("i", "WifiAp", "Trying to " + desiredString + " wifi ap...", 2);
								if (!method.isAccessible())
									method.setAccessible(true);
								method.invoke(wifiManager, null, desiredState);
							}
							else
							{
								Miscellaneous.logEvent("i", "WifiAp", "Trying to " + context.getResources().getString(R.string.toggle) + " wifi ap...", 2);
								if (!method.isAccessible())
									method.setAccessible(true);
								method.invoke(wifiManager, null, !state);
							}

							Miscellaneous.logEvent("i", "WifiAp", "Wifi ap " + desiredString + "d.", 2);
						}
						catch (Exception e)
						{
							Miscellaneous.logEvent("i", "WifiAp", context.getResources().getString(R.string.errorActivatingWifiAp) + ". " + e.getMessage(), 2);
						}
					}
				}
			}
			else
			{
				MyOnStartTetheringCallback cb = new MyOnStartTetheringCallback()
				{
					@Override
					public void onTetheringStarted()
					{
						Log.i("Tether", "Läuft");
					}

					@Override
					public void onTetheringFailed()
					{
						Log.i("Tether", "Doof");
					}
				};

				MyOreoWifiManager mowm = new MyOreoWifiManager(context);
				if (desiredState)
					mowm.startTethering(cb);
				else
					mowm.stopTethering();
			}
		}
		return true;
	}

	public static boolean setUsbTethering(Context context2, Boolean desiredState, boolean toggleActionIfPossible)
	{
		//TODO:toggle not really implemented, yet

		Miscellaneous.logEvent("i", "UsbTethering", "Changing UsbTethering to " + String.valueOf(desiredState), 4);

		boolean state = false; //Actions.isWifiApEnabled(context);
		Object connectivityServiceObject = null;
		ConnectivityManager connMgr = null;

		try
		{
			connectivityServiceObject = context.getSystemService(Context.CONNECTIVITY_SERVICE);
			connMgr = (ConnectivityManager) connectivityServiceObject;
		}
		catch (Exception e)
		{
			Miscellaneous.logEvent("e", "UsbTethering", context2.getResources().getString(R.string.logErrorGettingConnectionManagerService), 2);
			return false;
		}

		try
		{
			if ((state && !desiredState) || (!state && desiredState))
			{
				try
				{
					Method method = connectivityServiceObject.getClass().getDeclaredMethod("getTetheredIfaces");
					if (!method.isAccessible())
						method.setAccessible(true);
					String[] tetheredInterfaces = (String[]) method.invoke(connectivityServiceObject);
					if (tetheredInterfaces.length > 0)
						state = true;
				}
				catch (NoSuchMethodException e)
				{
					// System doesn't have that method, try another way

					String ipAddr = getIPAddressUsb(true);
					if (ipAddr.length() == 0)
						state = false;    //	tethering not enabled
					else
						state = true;    //	tethering enabled
				}
			}
		}
		catch (Exception e)
		{
			Miscellaneous.logEvent("w", "UsbTethering", context2.getResources().getString(R.string.logErrorDeterminingCurrentUsbTetheringState), 3);
		}

		if (toggleActionIfPossible)
		{
			Miscellaneous.logEvent("w", "UsbTethering", context2.getResources().getString(R.string.toggling), 3);
			desiredState = !state;
		}


		if ((state && !desiredState) || (!state && desiredState))
		{
			String desiredString = "";
			if (desiredState)
				desiredString = "activate";
			else
				desiredString = "deactivate";

			try
			{
				Method method = null;

				for (Method m : connectivityServiceObject.getClass().getDeclaredMethods())
				{
					if (desiredState && m.getName().equals("tether"))
					{
						method = m;
						break;
					}

					if (!desiredState && m.getName().equals("untether"))
					{
						method = m;
						break;
					}
				}

				if (method == null)
					throw new NoSuchMethodException();


				/*
				 * For some reason this doesn't work, throws NoSuchMethodExpection even if the method is present.
				 */
//				if(desiredState)
//					method = connectivityServiceObject.getClass().getDeclaredMethod("tether");
//				else
//					method = connectivityServiceObject.getClass().getDeclaredMethod("untether");

				// DETECT INTERFACE NAME
				Miscellaneous.logEvent("i", "UsbTethering", context2.getResources().getString(R.string.logDetectingTetherableUsbInterface), 4);
				String[] available = null;
				Method[] wmMethods = connMgr.getClass().getDeclaredMethods();
				for (Method getMethod : wmMethods)
				{
					if (getMethod.getName().equals("getTetherableUsbRegexs"))
					{
						try
						{
							if (!method.isAccessible())
								method.setAccessible(true);
							available = (String[]) getMethod.invoke(connMgr);
//						            break;
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				// DETECT INTERFACE NAME


				if (available.length > 0)
				{
					for (String interfaceName : available)
					{
						Miscellaneous.logEvent("i", "UsbTethering", "Detected " + String.valueOf(available.length) + " tetherable usb interfaces.", 5);
						Miscellaneous.logEvent("i", "UsbTethering", "Trying to " + desiredString + " UsbTethering on interface " + interfaceName + "...", 5);
						if (!method.isAccessible())
							method.setAccessible(true);
						Integer returnCode = (Integer) method.invoke(connectivityServiceObject, interfaceName);
						if (returnCode == 0)
						{
							Miscellaneous.logEvent("i", "UsbTethering", "UsbTethering " + desiredString + "d.", 5);
							return true;
						}
						else
						{
							Miscellaneous.logEvent("w", "UsbTethering", "Failed to " + desiredString + "Usb Tethering. ReturnCode of method " + method.getName() + ": " + String.valueOf(returnCode), 5);
						}
					}
				}
			}
			catch (NoSuchMethodException e)
			{
				Miscellaneous.logEvent("w", "UsbTethering", "Error while trying to " + desiredString + " UsbTethering. This kind of error may indicate we are above Android 2.3: " + Log.getStackTraceString(e), 3);
			}
			catch (Exception e)
			{
				Miscellaneous.logEvent("w", "UsbTethering", "Error while trying to " + desiredString + " UsbTethering. " + Log.getStackTraceString(e), 3);
			}
		}
		return false;
	}

	public static Boolean setBluetooth(Context context, Boolean desiredState, boolean toggleActionIfPossible)
	{
		Miscellaneous.logEvent("i", "Bluetooth", "Changing bluetooth to " + String.valueOf(desiredState), 4);

		try
		{
			BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

			// toggle
			if (toggleActionIfPossible)
			{
				Miscellaneous.logEvent("e", "SetBluetooth", context.getResources().getString(R.string.toggling), 2);
				desiredState = !myBluetoothAdapter.isEnabled();
			}

			// activate
			if (!myBluetoothAdapter.isEnabled() && desiredState)
			{
				Toast.makeText(context, context.getResources().getString(R.string.activating) + " Bluetooth.", Toast.LENGTH_LONG).show();
				myBluetoothAdapter.enable();
				return true;
			}

			// deactivate
			if (myBluetoothAdapter.isEnabled() && !desiredState)
			{
				Toast.makeText(context, context.getResources().getString(R.string.deactivating) + " Bluetooth.", Toast.LENGTH_LONG).show();
				myBluetoothAdapter.disable();
				return true;
			}
		}
		catch (NullPointerException e)
		{
			Miscellaneous.logEvent("e", "SetBluetooth", context.getResources().getString(R.string.failedToTriggerBluetooth), 2);
			Toast.makeText(context, context.getResources().getString(R.string.bluetoothFailed), Toast.LENGTH_LONG).show();
		}

		return false;
	}

	public static void setSound(Context context, int soundSetting)
	{
		Miscellaneous.logEvent("i", context.getResources().getString(R.string.soundSettings), "Changing sound to " + String.valueOf(soundSetting), 4);

		AudioManager myAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		myAudioManager.setRingerMode(soundSetting);
	}

	private static String getIPAddressUsb(final boolean useIPv4)
	{
		try
		{
			final List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (final NetworkInterface intf : interfaces)
			{
				if (intf.getDisplayName().startsWith("usb"))    // ro "rndis0"
				{
					final List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
					for (final InetAddress addr : addrs)
					{
						final String sAddr = addr.getHostAddress().toUpperCase();
						final boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4)
						{
							if (isIPv4)
							{
								return sAddr;
							}
						}
						else
						{
							if (!isIPv4)
							{
								final int delim = sAddr.indexOf('%');
								return delim < 0 ? sAddr : sAddr.substring(0, delim);
							}
						}
					}
				}
			}
		}
		catch (final Exception ex)
		{
			// for now eat exceptions
		}
		return "";
	}

	public static void playSound(boolean alwaysPlay, String soundFileLocation)
	{
		if (alwaysPlay || ((AudioManager) Miscellaneous.getAnyContext().getSystemService(Context.AUDIO_SERVICE)).getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
		{
			MediaPlayer mp = new MediaPlayer();
			try
			{
				File file = new File(soundFileLocation);
				if (file.exists())
				{
					Uri fileUri = Uri.parse(soundFileLocation);
					mp.setLooping(false);
					mp.setDataSource(Miscellaneous.getAnyContext(), fileUri);
					mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
					{
						@Override
						public void onCompletion(MediaPlayer mp)
						{
							mp.release();
						}
					});
					mp.prepare();
					mp.start();
				}
				else
				{
					Miscellaneous.logEvent("w", "Play sound file", "Sound file " + soundFileLocation + " does not exist. Can't play it.", 2);
					Toast.makeText(context, String.format(context.getResources().getString(R.string.cantFindSoundFile), soundFileLocation), Toast.LENGTH_SHORT).show();
				}
			}
			catch (Exception e)
			{
				Miscellaneous.logEvent("e", "Play sound file", "Error playing sound: " + Log.getStackTraceString(e), 2);
			}
		}
		else
			Miscellaneous.logEvent("i", "Play sound file", "Not playing sound file because phone is on some kind of mute state.", 2);
	}

	public void useDownloadedWebpage(String result)
	{
//		Toast.makeText(context, "Result: " + result, Toast.LENGTH_LONG).show();
	}

	public static HttpClient getInsecureSslClient(HttpClient client)
	{
		try
		{
			SSLContext ctx = SSLContext.getInstance("TLS");
			SSLSocketFactory ssf = null;
//	        MySSLSocketFactoryInsecure ssfI = null;

//	    	if(!Settings.httpAcceptAllCertificates)
//	    	{
//	    		ssf = new MySSLSocketFactory(ctx);
//	    		ssf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
//	    		
//		        char[] keystorePass="insecure".toCharArray();   //passphrase for keystore
//		        KeyStore keyStore=KeyStore.getInstance("BKS");
//		        
////		        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
//		        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
//		        tmf.init(keyStore);
//		        
//		        Miscellaneous.logEvent("i", "SSL Keystore", context.getCacheDir().toString(), 4);
//		        InputStream is = context.getResources().openRawResource(R.raw.keystore); 
//		        keyStore.load(is,keystorePass);
//	    		
//	    		ctx.init(null, tmf.getTrustManagers(), null);
//	    	}
//	    	else
//	    	{
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			ssf = new MySSLSocketFactoryInsecure(trustStore);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ctx.init(null, null, null);
//	    	}

			ClientConnectionManager ccm = client.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();

//	        if(!Settings.httpAcceptAllCertificates)
			sr.register(new Scheme("https", ssf, 443));
//	        else
//	        	sr.register(new Scheme("https", ssfI, 443));

			return new DefaultHttpClient(ccm, client.getParams());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static void startOtherActivity(boolean startByAction, String param)
	{
		Miscellaneous.logEvent("i", "StartOtherActivity", "Starting other Activity...", 4);

		String params[] = param.split(";");

		try
		{
			Intent externalActivityIntent;

			int paramsStartIndex;

			if (!startByAction)
			{
				// selected by activity

				String packageName, className;

				packageName = params[0];
				className = params[1];

				Miscellaneous.logEvent("i", "StartOtherApp", "Starting app by activity: " + packageName + " " + className, 3);

				paramsStartIndex = 2;

				externalActivityIntent = new Intent(Intent.ACTION_MAIN);
				externalActivityIntent.addCategory(Intent.CATEGORY_LAUNCHER);

//				if(packageName.equals("dummyPkg"))
//					externalActivityIntent.setAction(className);
//				else
				externalActivityIntent.setClassName(packageName, className);

				if (!Miscellaneous.doesActivityExist(externalActivityIntent, Miscellaneous.getAnyContext()))
					Miscellaneous.logEvent("w", "StartOtherApp", "Activity not found: " + className, 2);
			}
			else
			{
				// selected by action
				Miscellaneous.logEvent("i", "StartOtherApp", "Starting app by action: " + param, 3);

				externalActivityIntent = new Intent();

				if (!params[0].equals(dummyPackageString))
					externalActivityIntent.setPackage(params[0]);

				externalActivityIntent.setAction(params[1]);
			}

			externalActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			// Pack intents
			for (int i = 3; i < params.length; i++)
			{
				String[] singleParam = params[i].split(Action.intentPairSeperator);

    			/*Class c = Class.forName(singleParam[0]);
				for(Method m : c.getMethods())
				{
					if(m.getName().startsWith("parse"))
					{
						Object o = m.invoke(null, singleParam[0]);
						externalActivityIntent.putExtra(singleParam[1], o);
					}
				}*/

				if (singleParam[0].equals("boolean"))
				{
					Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
					externalActivityIntent.putExtra(singleParam[1], Boolean.parseBoolean(singleParam[2]));
				}
				else if (singleParam[0].equals("byte"))
				{
					Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
					externalActivityIntent.putExtra(singleParam[1], Byte.parseByte(singleParam[2]));
				}
				else if (singleParam[0].equals("char"))
				{
					Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
					externalActivityIntent.putExtra(singleParam[1], singleParam[2].charAt(0));
				}
				else if (singleParam[0].equals("CharSequence"))
				{
					Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
					externalActivityIntent.putExtra(singleParam[1], (CharSequence) singleParam[2]);
				}
				else if (singleParam[0].equals("double"))
				{
					Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
					externalActivityIntent.putExtra(singleParam[1], Double.parseDouble(singleParam[2]));
				}
				else if (singleParam[0].equals("float"))
				{
					Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
					externalActivityIntent.putExtra(singleParam[1], Float.parseFloat(singleParam[2]));
				}
				else if (singleParam[0].equals("int"))
				{
					Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
					externalActivityIntent.putExtra(singleParam[1], Integer.parseInt(singleParam[2]));
				}
				else if (singleParam[0].equals("long"))
				{
					Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
					externalActivityIntent.putExtra(singleParam[1], Long.parseLong(singleParam[2]));
				}
				else if (singleParam[0].equals("short"))
				{
					Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
					externalActivityIntent.putExtra(singleParam[1], Short.parseShort(singleParam[2]));
				}
				else if (singleParam[0].equals("Uri"))
				{
					if (singleParam[1].equalsIgnoreCase("IntentData"))
					{
						Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with value " + singleParam[2] + " as standard data parameter.", 3);
						externalActivityIntent.setData(Uri.parse(singleParam[2]));
					}
					else
					{
						Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
						externalActivityIntent.putExtra(singleParam[1], Uri.parse(singleParam[2]));
					}
				}
				else if (singleParam[0].equals("String"))
				{
					Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
					externalActivityIntent.putExtra(singleParam[1], singleParam[2]);
				}
				else
					Miscellaneous.logEvent("w", "StartOtherApp", "Unknown type of parameter " + singleParam[0] + " found.  Name " + singleParam[1] + " and value " + singleParam[2], 3);
			}

			if (params[2].equals(ActivityManageActionStartActivity.startByActivityString))
				autoMationServerRef.startActivity(externalActivityIntent);
			else
				autoMationServerRef.sendBroadcast(externalActivityIntent);
		}
		catch (Exception e)
		{
			Miscellaneous.logEvent("e", "StartOtherApp", autoMationServerRef.getResources().getString(R.string.errorStartingOtherActivity) + ": " + Log.getStackTraceString(e), 2);
			Toast.makeText(autoMationServerRef, autoMationServerRef.getResources().getString(R.string.errorStartingOtherActivity) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	public static void waitBeforeNextAction(Long waitTime)
	{
		Miscellaneous.logEvent("i", "waitBeforeNextAction", "waitBeforeNextAction for " + String.valueOf(waitTime) + " milliseconds.", 4);

		try
		{
			Thread.sleep(waitTime);
		}
		catch (InterruptedException e)
		{
			Miscellaneous.logEvent("e", "waitBeforeNextAction", Log.getStackTraceString(e), 2);
		}
	}

	public static void wakeupDevice(Long awakeTime)
	{
		String duration = "default";
		if (awakeTime > 0)
			duration = String.valueOf(awakeTime) + " milliseconds";

		Miscellaneous.logEvent("i", "wakeupDevice", "wakeupDevice for " + String.valueOf(duration) + ".", 4);

		if (awakeTime > 0)
		{
			/*
			 * This action needs to be performed in a separate thread. If it ran in the same one
			 * the screen would turn on, the specified amount of time would pass and the screen
			 * would turn off again before any other action in the rule would be ran.
			 */
			Thread t = new Thread(new WakeUpDeviceClass(awakeTime));
			t.start();
		}
	}

	public static void sendTextMessage(Context context, String[] parametersArray)
	{
		String phoneNumber, message;

		phoneNumber = parametersArray[0];
		message = parametersArray[1];

		try
		{
			String textToSend = Miscellaneous.replaceVariablesInText(message, context);

			/*
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
			intent.putExtra("sms_body", message);
			AutomationService.getInstance().startActivity(intent);
            */

			PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, Actions.class), 0);
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(phoneNumber, null, message, pi, null);
		}
		catch (Exception e)
		{
			Miscellaneous.logEvent("e", Miscellaneous.getAnyContext().getString(R.string.sendTextMessage), "Error in sendTextMessage: " + Log.getStackTraceString(e), 3);
		}
	}

	private static class WakeUpDeviceClass implements Runnable
	{
		private long awakeTime;

		public WakeUpDeviceClass(long awakeTime)
		{
			super();
			this.awakeTime = awakeTime;
		}

		@Override
		public void run()
		{
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			WakeLock wakeLock = pm.newWakeLock((WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Automation:Wakelock");
			wakeLock.acquire();

			try
			{
				Thread.sleep(awakeTime);
			}
			catch (InterruptedException e)
			{
				Miscellaneous.logEvent("w", context.getResources().getString(R.string.wakeupDevice), "Error keeping device awake: " + Log.getStackTraceString(e), 4);
			}

			wakeLock.release();
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@SuppressLint("NewApi")
	public static boolean setAirplaneMode(boolean desiredState, boolean toggleActionIfPossible)
	{
		/*
			Beginning from SDK Version 17 this may not work anymore.
			Setting airplane_mode_on has moved from android.provider.Settings.System to android.provider.Settings.Global, value is unchanged.

			https://stackoverflow.com/questions/22349928/permission-denial-not-allowed-to-send-broadcast-android-intent-action-airplane
			https://stackoverflow.com/questions/7066427/turn-off-airplane-mode-in-android
		 */

		boolean returnValue = false;

		try
		{
			boolean isEnabled = ConnectivityReceiver.isAirplaneMode(autoMationServerRef);

			if (isEnabled)
				Miscellaneous.logEvent("i", "Airplane mode", "Current status is enabled.", 4);
			else
				Miscellaneous.logEvent("i", "Airplane mode", "Current status is disabled.", 4);

			if (toggleActionIfPossible)
			{
				Miscellaneous.logEvent("i", "Airplane mode", context.getResources().getString(R.string.toggling), 4);
				desiredState = !isEnabled;
			}

			if (isEnabled != desiredState)
			{
				int desiredValueInt = 0;

				if (desiredState)
					desiredValueInt = 1;

				if (Build.VERSION.SDK_INT < 17 || ActivityPermissions.havePermission(Manifest.permission.WRITE_SECURE_SETTINGS, context))
				{
					//returnValue = android.provider.Settings.System.putInt(context.getContentResolver(), android.provider.Settings.System.AIRPLANE_MODE_ON, desiredValueInt);

					returnValue = android.provider.Settings.System.putInt(context.getContentResolver(), android.provider.Settings.Global.AIRPLANE_MODE_ON, desiredValueInt);

//					Intent airplaneIntent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
//					airplaneIntent.putExtra("state", desiredState);
//					context.sendBroadcast(airplaneIntent);
				}
				else
				{
					if (desiredState)
					{
						String[] commands = new String[]
								{
										"settings put global airplane_mode_on 1",
										"am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true"
								};
						returnValue = executeCommandViaSu(commands);
					}
					else
					{
						String[] commands = new String[]
								{
										"settings put global airplane_mode_on 0",
										"am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false"
								};
						returnValue = executeCommandViaSu(commands);
					}

//					returnValue = android.provider.Settings.Global.putString(context.getContentResolver(), "airplane_mode_on", String.valueOf(desiredValueInt));
				}

				// Post an intent to reload
				Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
				intent.putExtra("state", !isEnabled);
				context.sendBroadcast(intent);
			}
			else
				Miscellaneous.logEvent("i", "Airplane mode", "Airplane mode is already in status " + String.valueOf(desiredState) + ". Nothing to do.", 3);
		}
		catch (Exception e)
		{
			Miscellaneous.logEvent("e", "Airplane mode", Log.getStackTraceString(e), 2);
		}

		return returnValue;
	}

	/**
	 * Toggles the device between the different types of networks.
	 * Might not work. It seems only system apps are allowed to do this.
	 */
	public static boolean setNetworkType(int desiredType)
	{
		Miscellaneous.logEvent("i", "setNetworkType", "Asked to set network type to: " + String.valueOf(desiredType), 3);
		if (desiredType > 0)
		{
			try
			{
				ConnectivityManager connManager = (ConnectivityManager) Miscellaneous.getAnyContext().getSystemService(context.CONNECTIVITY_SERVICE);

				//			TelephonyManager.NETWORK_TYPE_EDGE

				if (connManager.getNetworkPreference() == desiredType)
				{
					Miscellaneous.logEvent("i", "setNetworkType", "Desired networkType already set. Not doing anything.", 4);
				}
				else
				{
					Miscellaneous.logEvent("i", "setNetworkType", "Setting network type to: " + String.valueOf(desiredType), 3);
					connManager.setNetworkPreference(desiredType);
				}
				return true;
			}
			catch (Exception e)
			{
				Miscellaneous.logEvent("e", "setNetworkType", "Error changing network type: " + Log.getStackTraceString(e), 2);
				return false;
			}
		}
		else
		{
			Miscellaneous.logEvent("w", "setNetworkType", "Invalid type of network specified: " + String.valueOf(desiredType), 4);
			return false;
		}
	}

	public static void speakText(String parameter2)
	{
		try
		{
			String textToSpeak = Miscellaneous.replaceVariablesInText(parameter2, context);
			autoMationServerRef.speak(textToSpeak, true);
		}
		catch (Exception e)
		{
			Miscellaneous.logEvent("e", "Speak text", "Error in speak text: " + Log.getStackTraceString(e), 3);
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	public static boolean playMusic(boolean desiredState, boolean toggleActionIfPossible)
	{
		try
		{
			//TODO:toggle
			//		if(desiredState)
			//		{
			Miscellaneous.logEvent("e", "Play music", "Starting music player...", 3);

			String deviceName = Build.MODEL;
			/*
			 * Fix for Samsung devices: http://stackoverflow.com/questions/12532207/open-music-player-on-galaxy-s3
			 */
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 | deviceName.contains("SM-"))
				playMusicIntent = new Intent(Intent.CATEGORY_APP_MUSIC);
			else
				playMusicIntent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);

			playMusicIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(playMusicIntent);

			//			playMusicIntent = new Intent();
			//			playMusicIntent.setAction(android.content.Intent.ACTION_VIEW);
			//			File file = new File(YOUR_SONG_URI);
			//			playMusicIntent.setDataAndType(Uri.fromFile(file), "audio/*");
			//			context.startActivity(playMusicIntent);

			return true;
			//		}
			//		else
			//		{
			//			if(playMusicIntent != null)
			//			{
			//				context.stopService(playMusicIntent);
			//			}
			//		}

			//		return false;
		}
		catch (ActivityNotFoundException e)
		{
			Toast.makeText(context, "Error: No music player found.", Toast.LENGTH_LONG).show();
			Miscellaneous.logEvent("e", "Play music", "Error in playerMusic(): No music player found. " + Log.getStackTraceString(e), 3);
			return false;
		}
		catch (Exception e)
		{
			Toast.makeText(context, "Error starting music player.", Toast.LENGTH_LONG).show();
			Miscellaneous.logEvent("e", "Play music", "Error in playerMusic(): " + Log.getStackTraceString(e), 3);
			return false;
		}
	}

	private String getTransactionCode()
	{
		try
		{
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			Class telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
			Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
			getITelephonyMethod.setAccessible(true);
			Object ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
			Class ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

			Class stub = ITelephonyClass.getDeclaringClass();
			Field field = stub.getDeclaredField("TRANSACTION_setDataEnabled");
			field.setAccessible(true);
			return String.valueOf(field.getInt(null));
		}
		catch (Exception e)
		{
			if (Build.VERSION.SDK_INT >= 22)
				return "86";
			else if (Build.VERSION.SDK_INT == 21)
				return "83";
		}
		return "";
	}

	private static String getTransactionCodeFromApi20(Context context) throws Exception
	{
		try
		{
			final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
			final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
			mTelephonyMethod.setAccessible(true);
			final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
			final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
			final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
			final Field field = mClass.getDeclaredField("TRANSACTION_setDataEnabled");
			field.setAccessible(true);
			return String.valueOf(field.getInt(null));
		}
		catch (Exception e)
		{
			// The "TRANSACTION_setDataEnabled" field is not available,
			// or named differently in the current API level, so we throw
			// an exception and inform users that the method is not available.
			throw e;
		}
	}

	public static class MobileDataStuff
	{
		// https://stackoverflow.com/questions/31120082/latest-update-on-enabling-and-disabling-mobile-data-programmatically

		/**
		 * Turns data on and off.
		 * Requires root permissions from lollipop on.
		 *
		 * @param toggleActionIfPossible
		 */
		public static boolean setDataConnection(boolean desiredState, boolean toggleActionIfPossible)
		{
			Miscellaneous.logEvent("i", "setData", "Asked to turn data to: " + String.valueOf(desiredState), 3);

			try
			{
				ConnectivityManager connManager = (ConnectivityManager) Miscellaneous.getAnyContext().getSystemService(Miscellaneous.getAnyContext().CONNECTIVITY_SERVICE);
				final Class conmanClass = Class.forName(connManager.getClass().getName());
				final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
				iConnectivityManagerField.setAccessible(true);
				final Object iConnectivityManager = iConnectivityManagerField.get(connManager);
				final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());

				Boolean isEnabled = isMobileDataEnabled();

				if (toggleActionIfPossible)
				{
					context.getResources().getString(R.string.toggling);
					desiredState = !isEnabled;
				}

				if (Build.VERSION.SDK_INT <= 20)
				{
					for (Method m : iConnectivityManagerClass.getDeclaredMethods())
					{
						Miscellaneous.logEvent("i", "method", m.getName(), 5);
					}

					final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
					setMobileDataEnabledMethod.setAccessible(true);

					setMobileDataEnabledMethod.invoke(iConnectivityManager, desiredState);
				}
				else
				{
					return setDataConnectionWithRoot(desiredState);
				}

				return true;
			}
			catch (Exception e)
			{
				Miscellaneous.logEvent("e", "setData", "Error changing network type: " + Log.getStackTraceString(e), 2);
				return false;
			}
		}

		protected static boolean setDataConnectionWithRoot(boolean enable)
		{
			try
			{
				int desiredState = 0;
				if (enable)
					desiredState = 1;

				if (MobileDataStuff.setMobileNetworkFromLollipop(desiredState, autoMationServerRef))
				{
					Miscellaneous.logEvent("i", "setDataConnectionWithRoot()", Miscellaneous.getAnyContext().getResources().getString(R.string.dataConWithRootSuccess), 2);
					return true;
				}
				else
				{
					Miscellaneous.logEvent("e", "setDataConnectionWithRoot()", Miscellaneous.getAnyContext().getResources().getString(R.string.dataConWithRootFail), 2);
					return false;
				}
			}
			catch (Exception e)
			{
				String rootString;
				if (Miscellaneous.isPhoneRooted())
					rootString = Miscellaneous.getAnyContext().getResources().getString(R.string.phoneIsRooted);
				else
					rootString = Miscellaneous.getAnyContext().getResources().getString(R.string.phoneIsNotRooted);

				Miscellaneous.logEvent("e", "setDataWithRoot()", "Error setting data setting with root. " + rootString + ": " + Log.getStackTraceString(e), 3);
				return false;
			}
		}

		@SuppressLint("NewApi")
		public static boolean setMobileNetworkFromLollipop(int desiredState, Context context) throws Exception
		{
			String command = null;
			int state = 0;

			try
			{
				// Get the current state of the mobile network.
				state = isMobileDataEnabled() ? 0 : 1;
				// Get the value of the "TRANSACTION_setDataEnabled" field.
				String transactionCode = getTransactionCode(context);
				// Android 5.1+ (API 22) and later.
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
				{
					SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
					// Loop through the subscription list i.e. SIM list.
					for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++)
					{
						if (transactionCode != null && transactionCode.length() > 0)
						{
							// Get the active subscription ID for a given SIM card.
							int subscriptionId = mSubscriptionManager.getActiveSubscriptionInfoList().get(i).getSubscriptionId();
							// Execute the command via `su` to turn off
							// mobile network for a subscription service.
							command = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + desiredState;
							Miscellaneous.logEvent("i", "setDataConnectionWithRoot()", "Running command: " + command.toString(), 5);
							return executeCommandViaSu(new String[]{command});
						}
					}
				}
				else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP)
				{
					// Android 5.0 (API 21) only.
					if (transactionCode != null && transactionCode.length() > 0)
					{
						// Execute the command via `su` to turn off mobile network.
						command = "service call phone " + transactionCode + " i32 " + desiredState;
						return executeCommandViaSu(new String[]{command});
					}
				}
			}
			catch (Exception e)
			{
				// Oops! Something went wrong, so we throw the exception here.
				throw e;
			}

			return false;
		}

		@SuppressLint("NewApi")
		public static boolean isMobileDataEnabled()
		{
			boolean isEnabled = false;

			if (Build.VERSION.SDK_INT <= 20)
			{
				try
				{
					ConnectivityManager connManager = (ConnectivityManager) Miscellaneous.getAnyContext().getSystemService(Miscellaneous.getAnyContext().CONNECTIVITY_SERVICE);
					final Class conmanClass = Class.forName(connManager.getClass().getName());
					final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
					iConnectivityManagerField.setAccessible(true);
					final Object iConnectivityManager = iConnectivityManagerField.get(connManager);
					final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());

					Method getMobileDataEnabledMethod = null;
					for (Method m : iConnectivityManagerClass.getDeclaredMethods())
					{
						Miscellaneous.logEvent("i", "Methods", m.getName(), 5);
						if (m.getName().equals("getMobileDataEnabled"))
						{
							getMobileDataEnabledMethod = m;
							break;
						}
					}
					//		    final Method getMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("getMobileDataEnabled", null);

					getMobileDataEnabledMethod.setAccessible(true);

					isEnabled = (Boolean) getMobileDataEnabledMethod.invoke(iConnectivityManager, (Object[]) null);
				}
				catch (Exception e)
				{
					Miscellaneous.logEvent("e", "isMobileDataEnabled()", "Error checking if mobile data is enabled: " + Log.getStackTraceString(e), 3);
				}
			}
			else
			{
				isEnabled = android.provider.Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
			}

			return isEnabled;
		}

		private static String getTransactionCode(Context context) throws Exception
		{
			try
			{
				final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
				final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
				mTelephonyMethod.setAccessible(true);
				final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
				final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
				final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
				final Field field = mClass.getDeclaredField("TRANSACTION_setDataEnabled");
				field.setAccessible(true);
				return String.valueOf(field.getInt(null));
			}
			catch (Exception e)
			{
				// The "TRANSACTION_setDataEnabled" field is not available,
				// or named differently in the current API level, so we throw
				// an exception and inform users that the method is not available.
				throw e;
			}
		}
	}

	protected static boolean executeCommandViaSu(String[] commands)
	{
		boolean success = false;

		try
		{
			suAvailable = Shell.SU.available();
			if (suAvailable)
			{
				suVersion = Shell.SU.version(false);
				suVersionInternal = Shell.SU.version(true);
				suResult = Shell.SU.run(commands);

				if (suResult != null)
					success = true;
			}
		}
		catch (Exception e)
		{
			success = false;
		}

		return success;
	}

	public static void setScreenBrightness(boolean autoBrightness, int brightnessValue)
	{
		if (autoBrightness)
			android.provider.Settings.System.putInt(context.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
		else
			android.provider.Settings.System.putInt(context.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

		int actualBrightnessValue = (int) ((float) brightnessValue / 100.0 * 255.0);
		android.provider.Settings.System.putInt(context.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, actualBrightnessValue);
	}

	public boolean isAirplaneModeOn(Context context)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			return android.provider.Settings.System.getInt(context.getContentResolver(), android.provider.Settings.System.AIRPLANE_MODE_ON, 0) != 0;
		}
		else
		{
			return android.provider.Settings.Global.getInt(context.getContentResolver(), android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
		}
	}
}