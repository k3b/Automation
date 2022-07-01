package com.jens.automation2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.jens.automation2.actions.wifi_router.MyOnStartTetheringCallback;
import com.jens.automation2.actions.wifi_router.MyOreoWifiManager;
import com.jens.automation2.location.WifiBroadcastReceiver;
import com.jens.automation2.receivers.ConnectivityReceiver;
import com.jens.automation2.receivers.NotificationListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import eu.chainfire.libsuperuser.Shell;

public class Actions
{
	public static AutomationService automationServerRef;
	public static Context context;
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

    public static void createNotification(Action action)
    {
		String[] elements = action.getParameter2().split(Action.actionParameter2Split);

		Miscellaneous.logEvent("w", "createNotification", "Creating notification with title " + elements[0] + " and text " + elements[1], 3);

    	int notificationId = Math.round(Calendar.getInstance().getTimeInMillis()/1000);

    	try
		{
			String title = Miscellaneous.replaceVariablesInText(elements[0], Miscellaneous.getAnyContext());
			String text = Miscellaneous.replaceVariablesInText(elements[1], Miscellaneous.getAnyContext());
			Miscellaneous.createDismissibleNotification(title, text, notificationId, false, AutomationService.NOTIFICATION_CHANNEL_ID_RULES, null);
		}
    	catch (Exception e)
		{
			Miscellaneous.logEvent("w", "createNotification", "Error occurred while replacing vars: " + Log.getStackTraceString(e), 3);
		}
    }

	@RequiresApi(api = Build.VERSION_CODES.M)
	public static void closeNotification(Action action)
	{
		NotificationManager nm = (NotificationManager) Miscellaneous.getAnyContext().getSystemService(Context.NOTIFICATION_SERVICE);
		for(StatusBarNotification n : nm.getActiveNotifications())
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			{
				String[] params = action.getParameter2().split(Action.actionParameter2Split);

				String myApp = params[0];
				String myTitleDir = params[1];
				String requiredTitle = params[2];
				String myTextDir = params[3];
				String requiredText;
				if (params.length >= 5)
					requiredText = params[4];
				else
					requiredText = "";

				for (StatusBarNotification sbn : NotificationListener.getInstance().getActiveNotifications())
				{
					NotificationListener.SimpleNotification sn = NotificationListener.convertNotificationToSimpleNotification(true, sbn);

					Miscellaneous.logEvent("i", "NotificationCloseCheck", "Checking if this notification should be closed in the context of rule " + action.getParentRule().getName() + ": "+ sn.toString(), 5);

					if (!myApp.equals(Trigger.anyAppString))
					{
						if (!myApp.equalsIgnoreCase(sn.getApp()))
						{
							Miscellaneous.logEvent("i", "NotificationCloseCheck", "Notification app name does not match rule.", 5);
							continue;
						}
					}
					else
					{
						/*
						 	Notifications from Automation are disregarded to avoid infinite loops.
						 */
						if(myApp.equals(BuildConfig.APPLICATION_ID))
						{
							continue;
						}
					}

				/*
					If there are multiple notifications ("stacked") title or text might be null:
					https://stackoverflow.com/questions/28047767/notificationlistenerservice-not-reading-text-of-stacked-notifications
				 */

					// T I T L E
					if (!StringUtils.isEmpty(requiredTitle))
					{
						if (!Miscellaneous.compare(myTitleDir, requiredTitle, sn.getTitle()))
						{
							Miscellaneous.logEvent("i", "NotificationCloseCheck", "Notification title does not match rule.", 5);
							continue;
						}
					}

					// T E X T
					if (!StringUtils.isEmpty(requiredText))
					{
						if (!Miscellaneous.compare(myTextDir, requiredText, sn.getText()))
						{
							Miscellaneous.logEvent("i", "NotificationCloseCheck", "Notification text does not match rule.", 5);
							continue;
						}
					}

					Miscellaneous.logEvent("i", "NotificationCloseCheck", "All criteria matches. Closing notification: " + sbn.getNotification().toString(), 3);
					if(NotificationListener.getInstance() != null)
						NotificationListener.getInstance().dismissNotification(sbn);
					else
						Miscellaneous.logEvent("i", "NotificationCloseCheck", "NotificationListener instance is null. Can\'t close notification.", 3);
				}
			}
		}
	}

    public static void sendBroadcast(Context context, String action)
    {
		Miscellaneous.logEvent("i", "sendBroadcast", "Sending broadcast with action " + action, 5);
		Intent broadcastIntent = new Intent();

		if(action.contains(Action.actionParameter2Split))
		{
			String[] parts = action.split(Action.actionParameter2Split);
			broadcastIntent.setAction(parts[0]);

			String[] intentparts = parts[1].split(";");
			broadcastIntent = packParametersIntoIntent(broadcastIntent, intentparts, 0);
		}
		else
			broadcastIntent.setAction(action);

		context.sendBroadcast(broadcastIntent);
    }

    public static class WifiStuff
	{
		public static Boolean setWifi(Context context, Boolean desiredState, boolean toggleActionIfPossible)
		{
			if(context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.Q)
				return setWifiWithRoot(context, desiredState, toggleActionIfPossible);
			else
				return setWifiOldFashioned(context, desiredState, toggleActionIfPossible);
		}

		public static Boolean setWifiWithRoot(Context context, Boolean desiredState, boolean toggleActionIfPossible)
		{
			Miscellaneous.logEvent("i", "Wifi", "Changing wifi to " + String.valueOf(desiredState) + ", but with root permissions.", 4);

			String command = null;
			int state = 0;

			String desiredStateString;
			if(desiredState)
				desiredStateString = "enable";
			else
				desiredStateString = "disable";

			try
			{
				command = "svc wifi " + desiredStateString;
				Miscellaneous.logEvent("i", "setWifiWithRoot()", "Running command as root: " + command.toString(), 5);
				return executeCommandViaSu(new String[]{command});
			}
			catch (Exception e)
			{
				// Oops! Something went wrong, so we throw the exception here.
				throw e;
			}
		}

		public static Boolean setWifiOldFashioned(Context context, Boolean desiredState, boolean toggleActionIfPossible)
		{
			Miscellaneous.logEvent("i", "Wifi", "Changing wifi to " + String.valueOf(desiredState), 4);

			if (desiredState && Settings.useWifiForPositioning)
				WifiBroadcastReceiver.startWifiReceiver(automationServerRef.getLocationProvider());

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

	public static class BluetoothTetheringClass
	{
		static Object instance = null;
		static Method setTetheringOn = null;
		static Method isTetheringOn = null;
		static Object mutex = new Object();

		public static Boolean setBluetoothTethering(Context context, Boolean desiredState, boolean toggleActionIfPossible)
		{
			Miscellaneous.logEvent("i", "Bluetooth Tethering", "Changing Bluetooth Tethering to " + String.valueOf(desiredState), 4);

//			boolean state = isTetheringOn(context);

//			if (toggleActionIfPossible)
//			{
//				Miscellaneous.logEvent("i", "Bluetooth Tethering", context.getResources().getString(R.string.toggling), 2);
//				desiredState = !state;
//			}

//			if (((state && !desiredState) || (!state && desiredState)))
//			{
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			Class<?> classBluetoothPan = null;
			Constructor<?> BTPanCtor = null;
			Object BTSrvInstance = null;
			Method mBTPanConnect = null;

			String sClassName = "android.bluetooth.BluetoothPan";
			try
			{
				classBluetoothPan = Class.forName(sClassName);
				Constructor<?> ctor = classBluetoothPan.getDeclaredConstructor(Context.class, BluetoothProfile.ServiceListener.class);

				ctor.setAccessible(true);
				//  Set Tethering ON

				Class[] paramSet = new Class[1];
				paramSet[0] = boolean.class;

				synchronized (mutex)
				{
					setTetheringOn = classBluetoothPan.getDeclaredMethod("setBluetoothTethering", paramSet);
					isTetheringOn = classBluetoothPan.getDeclaredMethod("isTetheringOn", null);
					instance = ctor.newInstance(context, new BTPanServiceListener(context));
				}

				classBluetoothPan = Class.forName("android.bluetooth.BluetoothPan");
				mBTPanConnect = classBluetoothPan.getDeclaredMethod("connect", BluetoothDevice.class);
				BTPanCtor = classBluetoothPan.getDeclaredConstructor(Context.class, BluetoothProfile.ServiceListener.class);
				BTPanCtor.setAccessible(true);
				BTSrvInstance = BTPanCtor.newInstance(context, new BTPanServiceListener(context));

				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

				// If there are paired devices
				if (pairedDevices.size() > 0)
				{
					// Loop through paired devices
					for (BluetoothDevice device : pairedDevices)
					{
						try
						{
							mBTPanConnect.invoke(BTSrvInstance, device);
						}
						catch (Exception e)
						{
							Miscellaneous.logEvent("e", "Bluetooth Tethering", Log.getStackTraceString(e), 1);
						}
					}
				}
				return true;
			}
			catch (NoSuchMethodException e)
			{
				Miscellaneous.logEvent("e", "Bluetooth Tethering", Log.getStackTraceString(e), 1);
			}
			catch (ClassNotFoundException e)
			{
				Miscellaneous.logEvent("e", "Bluetooth Tethering", Log.getStackTraceString(e), 1);
			}
			catch(InvocationTargetException e)
			{
				/*
					Exact error message: "Bluetooth binder is null"
					This means this device doesn't have bluetooth.
				 */
				Miscellaneous.logEvent("e", "Bluetooth Tethering", "Device probably doesn't have bluetooth. " + Log.getStackTraceString(e), 1);
				Toast.makeText(context, context.getResources().getString(R.string.deviceDoesNotHaveBluetooth), Toast.LENGTH_SHORT).show();
			}
			catch (Exception e)
			{
				Miscellaneous.logEvent("e", "Bluetooth Tethering", Log.getStackTraceString(e), 1);
			}

			return false;
		}

		public static class BTPanServiceListener implements BluetoothProfile.ServiceListener
		{
			private final Context context;

			public BTPanServiceListener(final Context context)
			{
				this.context = context;
			}

			@Override
			public void onServiceConnected(final int profile, final BluetoothProfile proxy)
			{
				//Some code must be here or the compiler will optimize away this callback.

				try
				{
					synchronized (mutex)
					{
						setTetheringOn.invoke(instance, true);
						if ((Boolean) isTetheringOn.invoke(instance, null))
						{
							Miscellaneous.logEvent("e", "Bluetooth Tethering", "BT Tethering is on", 1);
						}
						else
						{
							Miscellaneous.logEvent("e", "Bluetooth Tethering", "BT Tethering is off", 1);
						}
					}
				}
				catch (InvocationTargetException e)
				{
					Miscellaneous.logEvent("e", "Bluetooth Tethering", Log.getStackTraceString(e), 1);
				}
				catch (IllegalAccessException e)
				{
					Miscellaneous.logEvent("e", "Bluetooth Tethering", Log.getStackTraceString(e), 1);
				}
			}

			@Override
			public void onServiceDisconnected(final int profile)
			{
			}
		}
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

	public static void setDoNotDisturb(Context context, int desiredDndMode)
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			/*
				if (!notificationManager.isNotificationPolicyAccessGranted())
				--> done externally
			*/

			Miscellaneous.logEvent("i", context.getResources().getString(R.string.soundSettings), "Changing DND to " + String.valueOf(desiredDndMode), 4);
			NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.setInterruptionFilter(desiredDndMode);
		}
		else
			Miscellaneous.logEvent("w", context.getResources().getString(R.string.soundSettings), "Cannot change DND to " + String.valueOf(desiredDndMode) + ". This Android version is too and doesn\'t have that feature, yet.", 4);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public static boolean isDoNotDisturbActive(Context context)
	{
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int result = notificationManager.getCurrentInterruptionFilter();
		return (notificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL);
	}

	public static void setSound(Context context, int desiredSoundSetting)
	{
		Miscellaneous.logEvent("i", context.getResources().getString(R.string.soundSettings), "Changing sound to " + String.valueOf(desiredSoundSetting), 4);

		AudioManager myAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

//		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && desiredSoundSetting == AudioManager.RINGER_MODE_SILENT)
//		{
//			AudioManager am = (AudioManager) Miscellaneous.getAnyContext().getSystemService(Context.AUDIO_SERVICE);
//			am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, AudioManager.FLAG_PLAY_SOUND);
//			am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
//		}
//		else
		myAudioManager.setRingerMode(desiredSoundSetting);
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

    public static void vibrate(boolean parameter1, String parameter2)
    {
		String vibrateDurations[] = parameter2.split(Action.vibrateSeparator);

		int counter = 1;
		for(String vibrate : vibrateDurations)
		{
			if(counter % 2 != 0)
			{
				Vibrator vibrator = (Vibrator) Miscellaneous.getAnyContext().getSystemService(Context.VIBRATOR_SERVICE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
					vibrator.vibrate(VibrationEffect.createOneShot(Long.parseLong(vibrate), VibrationEffect.DEFAULT_AMPLITUDE));
				else
					vibrator.vibrate(Long.parseLong(vibrate));
			}
			else
			{
				try
				{
					Thread.sleep(Long.parseLong(vibrate));
				}
				catch (Exception e)
				{
					Miscellaneous.logEvent("e", "VibrateSleep", Log.getStackTraceString(e), 5);
				}
			}

			counter++;
		}
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

			if (!startByAction)
			{
				// selected by activity

				String packageName, className;

				packageName = params[0];
				className = params[1];

				Miscellaneous.logEvent("i", "StartOtherApp", "Starting app by activity: " + packageName + " " + className, 3);

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
			externalActivityIntent = packParametersIntoIntent(externalActivityIntent, params, 3);

			if (params[2].equals(ActivityManageActionStartActivity.startByActivityString))
				automationServerRef.startActivity(externalActivityIntent);
			else
				automationServerRef.sendBroadcast(externalActivityIntent);
		}
		catch (Exception e)
		{
			Miscellaneous.logEvent("e", "StartOtherApp", automationServerRef.getResources().getString(R.string.errorStartingOtherActivity) + ": " + Log.getStackTraceString(e), 2);
			Toast.makeText(automationServerRef, automationServerRef.getResources().getString(R.string.errorStartingOtherActivity) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	public static Intent packParametersIntoIntent(Intent intent, String[] params, int startIndex)
	{
		for (int i = startIndex; i < params.length; i++)
		{
			String[] singleParam = params[i].split(Action.intentPairSeparator);

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
				intent.putExtra(singleParam[1], Boolean.parseBoolean(singleParam[2]));
			}
			else if (singleParam[0].equals("byte"))
			{
				Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
				intent.putExtra(singleParam[1], Byte.parseByte(singleParam[2]));
			}
			else if (singleParam[0].equals("char"))
			{
				Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
				intent.putExtra(singleParam[1], singleParam[2].charAt(0));
			}
			else if (singleParam[0].equals("CharSequence"))
			{
				Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
				intent.putExtra(singleParam[1], (CharSequence) singleParam[2]);
			}
			else if (singleParam[0].equals("double"))
			{
				Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
				intent.putExtra(singleParam[1], Double.parseDouble(singleParam[2]));
			}
			else if (singleParam[0].equals("float"))
			{
				Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
				intent.putExtra(singleParam[1], Float.parseFloat(singleParam[2]));
			}
			else if (singleParam[0].equals("int"))
			{
				Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
				intent.putExtra(singleParam[1], Integer.parseInt(singleParam[2]));
			}
			else if (singleParam[0].equals("long"))
			{
				Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
				intent.putExtra(singleParam[1], Long.parseLong(singleParam[2]));
			}
			else if (singleParam[0].equals("short"))
			{
				Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
				intent.putExtra(singleParam[1], Short.parseShort(singleParam[2]));
			}
			else if (singleParam[0].equals("Uri"))
			{
				if (singleParam[1].equalsIgnoreCase("IntentData"))
				{
					Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with value " + singleParam[2] + " as standard data parameter.", 3);
					intent.setData(Uri.parse(singleParam[2]));
				}
				else
				{
					Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
					intent.putExtra(singleParam[1], Uri.parse(singleParam[2]));
				}
			}
			else if (singleParam[0].equals("String"))
			{
				Miscellaneous.logEvent("i", "StartOtherApp", "Adding parameter of type " + singleParam[0] + " with name " + singleParam[1] + " and value " + singleParam[2], 3);
				intent.putExtra(singleParam[1], singleParam[2]);
			}
			else
				Miscellaneous.logEvent("w", "StartOtherApp", "Unknown type of parameter " + singleParam[0] + " found.  Name " + singleParam[1] + " and value " + singleParam[2], 3);
		}

		return intent;
	}

	public static void waitBeforeNextAction(Long waitTime)
	{
		Miscellaneous.logEvent("i", "waitBeforeNextAction", "waitBeforeNextAction for " + String.valueOf(waitTime) + " milliseconds.", 4);

		wakeLockStart(60000);
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
			sms.sendTextMessage(phoneNumber, null, textToSend, pi, null);
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
			try
			{
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				WakeLock wakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Automation:Wakelock");
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
			catch(Exception e)
			{
				Miscellaneous.logEvent("e", "Wakeup device action", "Error while waking up device: " + Log.getStackTraceString(e), 1);
			}
		}
	}

	/*public static void turnOnScreen()
	{
		// turn on screen
		Miscellaneous.logEvent("i", "Actions", "Turning screen on.", 3);
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, AutomationService.NOTIFICATION_CHANNEL_ID + ":turnOffScreen");
		wakeLock.acquire();
	}*/

	@TargetApi(21) //Suppress lint error for PROXIMITY_SCREEN_OFF_WAKE_LOCK
	public static void turnOffScreen()
	{
		Miscellaneous.logEvent("i", "Actions", "Turning screen off.", 3);


		/*params.flags |= LayoutParams.FLAG_KEEP_SCREEN_ON;
		params.screenBrightness = 0;
		getWindow().setAttributes(params);*/

//		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//		WakeLock wakeLock = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,AutomationService.NOTIFICATION_CHANNEL_ID + ":turnOffScreen");
//		WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK , AutomationService.NOTIFICATION_CHANNEL_ID + ":turnOffScreen");
//		wakeLock.acquire();

//		WakeLock wakeLock = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "tag");
//		WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK , "tag");
//		wakeLock.acquire();

//		wakeLock.release();
		lockScreen();
	}

	public static void lockScreen()
	{
		Miscellaneous.logEvent("i", "Actions", "Locking screen.", 3);

		// Works, but requires Manifest.permission.BIND_DEVICE_ADMIN
//		https://stackoverflow.com/questions/23898406/java-lang-securityexception-no-active-admin-owned-by-uid-10047-for-policy-4-on
		DevicePolicyManager deviceManager = (DevicePolicyManager)Miscellaneous.getAnyContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
		deviceManager.lockNow();
	}

	// using root
	/*private void turnOffScreen()
	{
		try
		{
			Class c = Class.forName("android.os.PowerManager");
			PowerManager  mPowerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
			for(Method m : c.getDeclaredMethods())
			{
				if(m.getName().equals("goToSleep"))
				{
					m.setAccessible(true);
					if(m.getParameterTypes().length == 1)
					{
						m.invoke(mPowerManager,SystemClock.uptimeMillis()-2);
					}
				}
			}
		}
		catch (Exception e)
		{
		}
	}*/

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
			boolean isEnabled = ConnectivityReceiver.isAirplaneMode(automationServerRef);

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
			automationServerRef.speak(textToSpeak, true);
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

			return true;
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

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public static boolean controlMediaPlayback(Context context, int command)
	{
		int keyCode = -1;
		switch(command)
		{
			case 0:
				keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
				break;
			case 1:
				keyCode = KeyEvent.KEYCODE_MEDIA_PLAY;
				break;
			case 2:
				keyCode = KeyEvent.KEYCODE_MEDIA_PAUSE;
				break;
			case 3:
				keyCode = KeyEvent.KEYCODE_MEDIA_STOP;
				break;
			case 4:
				keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
				break;
			case 5:
				keyCode = KeyEvent.KEYCODE_MEDIA_NEXT;
				break;
		}

		return controlMediaByDispatch(keyCode);
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	static boolean controlMediaByDispatch(int keyCode)
	{
		AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
		mAudioManager.dispatchMediaKeyEvent(event);

		return true;
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

				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
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

		protected static boolean setDataConnectionWithRoot(boolean desiredState)
		{
			try
			{
				if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1)
				{
					if(MobileDataStuff.setMobileNetworkFromAndroid9(desiredState, automationServerRef))
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
				else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
				{
					if (MobileDataStuff.setMobileNetworkTillAndroid5(desiredState, automationServerRef))
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
				else
				{
					if (MobileDataStuff.setMobileNetworkAndroid6Till8(desiredState, automationServerRef))
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
		public static boolean setMobileNetworkAndroid6Till8(boolean desiredState, Context context) throws Exception
		{
			String command = null;

			try
			{
				int desiredStateString;
				if(desiredState)
					desiredStateString = 1;
				else
					desiredStateString = 0;

				// Get the current state of the mobile network.
//				boolean state = isMobileDataEnabled() ? 0 : 1;
				// Get the value of the "TRANSACTION_setDataEnabled" field.
				String transactionCode = getTransactionCode(context);
				// Android 5.1+ (API 22) and later.
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
						command = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + desiredStateString;
						Miscellaneous.logEvent("i", "setMobileNetworkAndroid6Till8()", "Running command: " + command.toString(), 5);
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
		public static boolean setMobileNetworkTillAndroid5(boolean desiredState, Context context) throws Exception
		{
			String command = null;

			try
			{
				int desiredStateString;
				if(desiredState)
					desiredStateString = 1;
				else
					desiredStateString = 0;

				// Get the current state of the mobile network.
//				int currentState = isMobileDataEnabled() ? 0 : 1;
				// Get the value of the "TRANSACTION_setDataEnabled" field.
				String transactionCode = getTransactionCode(context);
				// Android 5.0 (API 21) only.
				if (transactionCode != null && transactionCode.length() > 0)
				{
					// Execute the command via `su` to turn off mobile network.
					command = "service call phone " + transactionCode + " i32 " + desiredStateString;
					Miscellaneous.logEvent("i", "setMobileNetworkTillAndroid5()", "Running command: " + command.toString(), 5);
					return executeCommandViaSu(new String[]{command});
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
		public static boolean setMobileNetworkFromAndroid9(boolean desiredState, Context context) throws Exception
		{
			String command = null;

			String desiredStateString;
			if(desiredState)
				desiredStateString = "enable";
			else
				desiredStateString = "disable";

			try
			{
				/*
					Android 8.1 is the last version on which the transaction code can be determined
					with the below method. From 9.0 on the field TRANSACTION_setDataEnabled does not
					exist anymore. Usually it was 83 and we'll just try this number hardcoded.
					Alternatively the bottom of this might be an approach:
					https://stackoverflow.com/questions/26539445/the-setmobiledataenabled-method-is-no-longer-callable-as-of-android-l-and-later
				 */

				// Execute the command via `su` to turn off
				// mobile network for a subscription service.
				command = "svc data " + desiredStateString;
				Miscellaneous.logEvent("i", "setMobileNetworkFromAndroid9()", "Running command: " + command.toString(), 5);
				return executeCommandViaSu(new String[]{command});
			}
			catch (Exception e)
			{
				// Oops! Something went wrong, so we throw the exception here.
				throw e;
			}
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

	public static boolean runExecutable(Context context, boolean runAsRoot, String path, String parameters)
	{
		if(runAsRoot)
		{
			if(!StringUtils.isEmpty(parameters))
				return executeCommandViaSu(new String[] { path + " " + parameters });
			else
				return executeCommandViaSu(new String[] { path });
		}
		else
		{
			Object[] result;

			File executable = new File(path);
			File workingDir = new File(executable.getParent());

			if(!StringUtils.isEmpty(parameters))
				result = runExternalApplication(path, 0, workingDir, parameters);
			else
				result = runExternalApplication(path, 0, workingDir, null);

			boolean execResult = (boolean) result[0];

			return execResult;
		}
	}

	/**
	 *
	 * @param commandToExecute
	 * @param timeout
	 * @param params
	 * @return Returns an array: 0=exit code, 1=cmdline output
	 */
	public static Object[] runExternalApplication(String commandToExecute, long timeout, File workingDirectory, String params)
	{
		/*
		 * Classes stolen from https://github.com/stleary/JSON-java
		 */

		String fullCommand;

		if(!StringUtils.isEmpty(params))
			fullCommand = commandToExecute + " " + params;
		else
			fullCommand = commandToExecute;

		Miscellaneous.logEvent("i", "Running executable", "Running external application " + fullCommand, 4);

		Object[] returnObject = new Object[2];

		StringBuilder output = new StringBuilder();
		String line = null;
		OutputStream stdin = null;
		InputStream stderr = null;
		InputStream stdout = null;

		try
		{
			Process process = null;

			if(workingDirectory != null)
				process = Runtime.getRuntime().exec(fullCommand, null, workingDirectory);
			else
				process = Runtime.getRuntime().exec(fullCommand);
			stdin = process.getOutputStream ();
			stderr = process.getErrorStream ();
			stdout = process.getInputStream ();

			// "write" the parms into stdin
			/*line = "param1" + "\n";
			stdin.write(line.getBytes() );
			stdin.flush();

			line = "param2" + "\n";
			stdin.write(line.getBytes() );
			stdin.flush();

			line = "param3" + "\n";
			stdin.write(line.getBytes() );
			stdin.flush();*/

			stdin.close();

			// clean up if any output in stdout
			BufferedReader brCleanUp = new BufferedReader (new InputStreamReader (stdout));
			while ((line = brCleanUp.readLine ()) != null)
			{
				Miscellaneous.logEvent ("i", "Running executable", "[Stdout] " + line, 4);
				output.append(line);
			}
			brCleanUp.close();

			// clean up if any output in stderr
			brCleanUp = new BufferedReader (new InputStreamReader(stderr));
			while ((line = brCleanUp.readLine ()) != null)
			{
				Miscellaneous.logEvent ("i", "Running executable", "[Stderr] " + line, 4);
				output.append(line);
			}
			brCleanUp.close();

			try
			{
				// Wait for the process to exit, we want the return code
				if(timeout > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				{
					try
					{
						if(!process.waitFor(timeout, TimeUnit.MILLISECONDS))
						{
							Miscellaneous.logEvent("i", "Running executable", "Timeout of " + String.valueOf(timeout) + " ms reached. Killing check attempt.", 3);
							process.destroyForcibly();
						}
					}
					catch(NoSuchMethodError e)
					{
						process.waitFor();
					}
				}
				else
					process.waitFor();
			}
			catch (InterruptedException e)
			{
				Miscellaneous.logEvent("i", "Running executable", "Waiting for process failed: " + Log.getStackTraceString(e), 4);
				Miscellaneous.logEvent("i", "Running executable", Log.getStackTraceString(e), 1);
			}

			if(process.exitValue() == 0)
				Miscellaneous.logEvent("i", "Running executable", "ReturnCode: " + String.valueOf(process.exitValue()), 4);
			else
				Miscellaneous.logEvent("i", "Running executable", "External execution (RC=" + String.valueOf(process.exitValue()) + ") returned error: " + output.toString(), 3);

			returnObject[0] = process.exitValue();
			returnObject[1] = output.toString();

			return returnObject;
		}
		catch (IOException e)
		{
			Miscellaneous.logEvent("e", "Running executable", Log.getStackTraceString(e), 1);
		}

		Miscellaneous.logEvent("i", "Running executable", "Error running external application.", 1);

//		if(slotMap != null)
//			for(String key : slotMap.keySet())
//				System.clearProperty(key);

		return null;
	}

	public static boolean isTetheringActive1(Context context)
	{
		try
		{
			for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
			{
				NetworkInterface intf = en.nextElement();

				for(Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
				{
					InetAddress inetAddress = enumIpAddr.nextElement();

					if(!intf.isLoopback())
					{
						if(intf.getName().contains("rndis"))
						{
							return true;
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "isTetheringActive()", Log.getStackTraceString(e), 3);
		}

		return false;
	}

	public final static int wakeLockTimeoutDisabled = -1;
	static boolean wakeLockStopRequested = false;
	public static void wakeLockStart(long duration)
	{
		long waited = 0;
		int step = 1000;

		if(duration < 0)
			step = 99999;

		try
		{
			PowerManager powerManager = (PowerManager) Miscellaneous.getAnyContext().getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock fullWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Loneworker - FULL WAKE LOCK");
			fullWakeLock.acquire(); // turn on

			do
			{

				try
				{
					Thread.sleep(step); // turn on duration
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				waited += step;

				if(false) //stop requested
				{
					Miscellaneous.logEvent("i", "WakeLockStart", "Stop requested.", 4);
					wakeLockStopRequested = false;
					break;
				}
			}
			while(waited <= duration);

			fullWakeLock.release();
		}
		catch (Exception e)
		{
		}
	}

	public static void wakeLockStop()
	{
		Miscellaneous.logEvent("i", "WakeLockStart", "Requesting stop.", 4);
		wakeLockStopRequested = true;
	}
}