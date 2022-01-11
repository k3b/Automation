package com.jens.automation2;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;

import java.util.ArrayList;
import java.util.Locale;


public class Action
{
	Rule parentRule = null;

	public static final String actionParameter2Split = "ap2split";
	public static final String intentPairSeperator = "intPairSplit";
	public static final String vibrateSeparator = ",";

	public enum Action_Enum {	
								setWifi,
								setBluetooth,
								setUsbTethering,
								setWifiTethering,
								setBluetoothTethering,
								setDisplayRotation,
								turnWifiOn,turnWifiOff,
								turnBluetoothOn,turnBluetoothOff,
								triggerUrl,
								changeSoundProfile,
								turnUsbTetheringOn,turnUsbTetheringOff,
								turnWifiTetheringOn,turnWifiTetheringOff,
								enableScreenRotation,disableScreenRotation,
								startOtherActivity,
								waitBeforeNextAction,
								turnScreenOnOrOff,
								setAirplaneMode,
								setDataConnection,
								speakText,
								playMusic,
								setScreenBrightness,
								playSound,
								vibrate,
								createNotification,
								closeNotification,
								sendTextMessage;
								
								public String getFullName(Context context)
								{
									switch(this)
									{
										case setWifi:
											return context.getResources().getString(R.string.actionSetWifi);
										case setBluetooth:
											return context.getResources().getString(R.string.actionSetBluetooth);
										case setWifiTethering:
											return context.getResources().getString(R.string.actionSetWifiTethering);
										case setBluetoothTethering:
											return context.getResources().getString(R.string.actionSetBluetoothTethering);
										case setUsbTethering:
											return context.getResources().getString(R.string.actionSetUsbTethering);
										case setDisplayRotation:
											return context.getResources().getString(R.string.actionSetDisplayRotation);
										case turnWifiOn:
											return context.getResources().getString(R.string.actionTurnWifiOn);
										case turnWifiOff:
											return context.getResources().getString(R.string.actionTurnWifiOff);
										case turnBluetoothOn:
											return context.getResources().getString(R.string.actionTurnBluetoothOn);
										case turnBluetoothOff:
											return context.getResources().getString(R.string.actionTurnBluetoothOff);
										case triggerUrl:
											return context.getResources().getString(R.string.actionTriggerUrl);
										case changeSoundProfile:
											return context.getResources().getString(R.string.actionChangeSoundProfile);
										case turnUsbTetheringOn:
											return context.getResources().getString(R.string.actionTurnUsbTetheringOn);
										case turnUsbTetheringOff:
											return context.getResources().getString(R.string.actionTurnUsbTetheringOff);
										case turnWifiTetheringOn:
											return context.getResources().getString(R.string.actionTurnWifiTetheringOn);
										case turnWifiTetheringOff:
											return context.getResources().getString(R.string.actionTurnWifiTetheringOff);
										case enableScreenRotation:
											return context.getResources().getString(R.string.actionEnableScreenRotation);
										case disableScreenRotation:
											return context.getResources().getString(R.string.actionDisableScreenRotation);
										case startOtherActivity:
											return context.getResources().getString(R.string.startOtherActivity);
										case waitBeforeNextAction:
											return context.getResources().getString(R.string.waitBeforeNextAction);
										case turnScreenOnOrOff:
											return context.getResources().getString(R.string.turnScreenOnOrOff);
										case vibrate:
											return context.getResources().getString(R.string.vibrate);
										case setAirplaneMode:
											return context.getResources().getString(R.string.airplaneMode);
										case setDataConnection:
											return context.getResources().getString(R.string.actionDataConnection);
										case speakText:
											return context.getResources().getString(R.string.actionSpeakText);
										case playMusic:
											return context.getResources().getString(R.string.actionPlayMusic);
										case playSound:
											return context.getResources().getString(R.string.playSound);
										case sendTextMessage:
											return context.getResources().getString(R.string.sendTextMessage);
										case setScreenBrightness:
											return context.getResources().getString(R.string.setScreenBrightness);
										case createNotification:
											return context.getResources().getString(R.string.createNotification);
										case closeNotification:
											return context.getResources().getString(R.string.closeNotifications);
										default:
											return "Unknown";
									}
								}
							};
	
	private Action_Enum action;
	private boolean parameter1 = false;
	private String parameter2 = "";

	public Action_Enum getAction()
	{
		return action;
	}
	public void setAction(Action_Enum action)
	{
		this.action = action;
	}
	
	public boolean getParameter1()
	{
		return parameter1;
	}
	public void setParameter1(boolean parameter1)
	{
		this.parameter1 = parameter1;
	}
	public String getParameter2()
	{
		return parameter2;
	}
	public void setParameter2(String parameter)
	{
		this.parameter2 = parameter;
	}
	public String toStringShort()
	{
		String returnString = action.toString();
		
		return returnString;
	}
	@Override
	public String toString()
	{
		StringBuilder returnString = new StringBuilder();

		switch(getAction())
		{
			case setWifi:
				if (this.getParameter1())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnWifiOn));
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnWifiOff));
				break;
			case setBluetooth:
				if (this.getParameter1())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnBluetoothOn));
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnBluetoothOff));
				break;
			case setUsbTethering:
				if (this.getParameter1())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnUsbTetheringOn));
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnUsbTetheringOff));
				break;
			case setWifiTethering:
				if (this.getParameter1())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnWifiTetheringOn));
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnWifiTetheringOff));
				break;
			case setBluetoothTethering:
				if (this.getParameter1())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnBluetoothTetheringOn));
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnBluetoothTetheringOff));
				break;
			case setDisplayRotation:
				if (this.getParameter1())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionEnableScreenRotation));
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionDisableScreenRotation));
				break;
			case setAirplaneMode:
				if (this.getParameter1())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnAirplaneModeOn));
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnAirplaneModeOff));
				break;
			case setDataConnection:
				if (this.getParameter1())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionSetDataConnectionOn));
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionSetDataConnectionOff));
				break;
			case startOtherActivity:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.startOtherActivity));
				break;
			case triggerUrl:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTriggerUrl));
				break;
			case speakText:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionSpeakText));
				break;
			case playMusic:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionPlayMusic));
				break;
			case sendTextMessage:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.sendTextMessage));
				break;
			case turnScreenOnOrOff:
				if (getParameter1())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.turnScreenOn));
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.turnScreenOff));
				break;
			case playSound:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.playSound));
				break;
			case changeSoundProfile:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionChangeSoundProfile));
				break;
			case waitBeforeNextAction:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.waitBeforeNextAction));
				break;
			case setScreenBrightness:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.setScreenBrightness));
				break;
			case createNotification:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.createNotification));
				break;
			case closeNotification:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.closeNotifications));
				break;
			default:
				returnString.append(action.toString());
		}

		if(this.getAction().equals(Action_Enum.triggerUrl))
		{
			String[] components = parameter2.split(";");
			if(components.length >= 3)
			{
				returnString.append(": " + components[2]);
				
				if(parameter1)
					returnString.append(" " + Miscellaneous.getAnyContext().getResources().getString(R.string.usingAuthentication) + ".");
			}
			else
				returnString.append(": " + components[0]);
		}
		else if(this.getAction().equals(Action_Enum.startOtherActivity))
		{
			returnString.append(": " + parameter2.replace(Action.intentPairSeperator, "/"));
		}
		else if(this.getAction().equals(Action_Enum.sendTextMessage))
		{
			String[] components = parameter2.split(Actions.smsSeparator);
			if(components.length >= 2)
			{
				returnString.append(" " + Miscellaneous.getAnyContext().getResources().getString(R.string.toNumber) + " " + components[0]);

				returnString.append(". " + Miscellaneous.getAnyContext().getResources().getString(R.string.message) + ": " + components[1]);
			}
		}
		else if(this.getAction().equals(Action_Enum.setScreenBrightness))
		{
			returnString.append(" " + Miscellaneous.getAnyContext().getResources().getString(R.string.to) + " ");

			if(parameter1)
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.brightnessAuto));
			else
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.brightnessManual));

			returnString.append(" / " + Integer.parseInt(parameter2) + "%");
		}
		else
			if (parameter2 != null && parameter2.length() > 0)
				returnString.append(": " + parameter2.replace(Action.actionParameter2Split, "; "));
		
		return returnString.toString();
	}

	public Rule getParentRule()
	{
		return parentRule;
	}

	public void setParentRule(Rule parentRule)
	{
		this.parentRule = parentRule;
	}

	public static CharSequence[] getActionTypesAsArray()
	{
		ArrayList<String> actionTypesList = new ArrayList<String>();
		
		for(Action_Enum action : Action_Enum.values())
		{
        	if(      // exclusion for deprecated types
        			!action.toString().equals("turnWifiOn")
        					&&
                	!action.toString().equals("turnWifiOff")
        					&&
                	!action.toString().equals("turnBluetoothOn")
        					&&
                	!action.toString().equals("turnBluetoothOff")
        					&&
                	!action.toString().equals("turnUsbTetheringOn")
        					&&
                	!action.toString().equals("turnUsbTetheringOff")
        					&&
                	!action.toString().equals("turnWifiTetheringOn")
        					&&
                	!action.toString().equals("turnWifiTetheringOff")
        					&&
                	!action.toString().equals("enableScreenRotation")
        					&&
                	!action.toString().equals("disableScreenRotation")
        		)      // exclusion for deprecated types  	
        		actionTypesList.add(action.toString());
		}
		
		return (String[])actionTypesList.toArray(new String[actionTypesList.size()]);
	}
	public static CharSequence[] getActionTypesFullNameStringAsArray(Context context)
	{
		ArrayList<String> actionTypesList = new ArrayList<String>();

		for(Action_Enum action : Action_Enum.values())
		{
        	if(      // exclusion for deprecated types
        			!action.toString().equals("turnWifiOn")
        					&&
                	!action.toString().equals("turnWifiOff")
        					&&
                	!action.toString().equals("turnBluetoothOn")
        					&&
                	!action.toString().equals("turnBluetoothOff")
        					&&
                	!action.toString().equals("turnUsbTetheringOn")
        					&&
                	!action.toString().equals("turnUsbTetheringOff")
        					&&
                	!action.toString().equals("turnWifiTetheringOn")
        					&&
                	!action.toString().equals("turnWifiTetheringOff")
        					&&
                	!action.toString().equals("enableScreenRotation")
        					&&
                	!action.toString().equals("disableScreenRotation")
        		)      // exclusion for deprecated types  	
			actionTypesList.add(action.getFullName(context));
		}
		
		return (String[])actionTypesList.toArray(new String[actionTypesList.size()]);
	}
	
	public void run(Context context, boolean toggleActionIfPossible)
	{
		try
		{
			switch(this.getAction())
			{
				case changeSoundProfile:
					/*
					 * Old version. Those checks should not be necessary anymore. Also they didn't work
					 * because profiles were created with names like silent, vibrate  and normal.
					 */

					Profile p = Profile.getByName(this.getParameter2());
					if (p != null)
						p.activate(context);

					break;
				case triggerUrl:
					triggerUrl(context);
					break;
				case setBluetooth:
					Actions.setBluetooth(context, getParameter1(), toggleActionIfPossible);
					break;
				case setUsbTethering:
					Actions.setUsbTethering(context, getParameter1(), toggleActionIfPossible);
					break;
				case setWifi:
					Actions.WifiStuff.setWifi(context, getParameter1(), toggleActionIfPossible);
					break;
				case setWifiTethering:
					Actions.setWifiTethering(context, getParameter1(), toggleActionIfPossible);
					break;
				case setBluetoothTethering:
					Actions.BluetoothTetheringClass.setBluetoothTethering(context, getParameter1(), toggleActionIfPossible);
					break;
				case setDisplayRotation:
					Actions.setDisplayRotation(context, getParameter1(), toggleActionIfPossible);
					break;
				case startOtherActivity:
					Actions.startOtherActivity(getParameter1(), getParameter2());
					break;
				case waitBeforeNextAction:
					Actions.waitBeforeNextAction(Long.parseLong(this.getParameter2()));
					break;
				case turnScreenOnOrOff:
					if(getParameter1())
					{
						if(StringUtils.isNumeric(this.getParameter2()))
							Actions.wakeupDevice(Long.parseLong(this.getParameter2()));
						else
							Actions.wakeupDevice((long)1000);
						// wakeupDevice() will create a separate thread. That'll take some time, we wait 100ms.
						try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						Actions.turnOffScreen();
					}
					break;
				case setAirplaneMode:
					Actions.setAirplaneMode(this.getParameter1(), toggleActionIfPossible);
					break;
				case setDataConnection:
					Actions.MobileDataStuff.setDataConnection(this.getParameter1(), toggleActionIfPossible);
					break;
				case speakText:
					Actions.speakText(this.getParameter2());
					break;
				case playMusic:
					Actions.playMusic(this.getParameter1(), toggleActionIfPossible);
					break;
				case sendTextMessage:
					Actions.sendTextMessage(context, this.getParameter2().split(Actions.smsSeparator));
					break;
				case setScreenBrightness:
					Actions.setScreenBrightness(getParameter1(), Integer.parseInt(getParameter2()));
					break;
				case vibrate:
					Actions.vibrate(getParameter1(), getParameter2());
					break;
				case playSound:
					Actions.playSound(getParameter1(), getParameter2());
					break;
				case createNotification:
					Actions.createNotification(this);
					break;
				case closeNotification:
					Actions.closeNotification(this);
					break;
				default:
					Miscellaneous.logEvent("w", "Action", context.getResources().getString(R.string.unknownActionSpecified), 3);
					break;
			}
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "ActionExecution", Log.getStackTraceString(e), 1);
			Toast.makeText(context, context.getResources().getString(R.string.errorRunningRule), Toast.LENGTH_LONG).show();
		}
	}
	
	private void triggerUrl(Context context)
	{		
		String username = null;
		String password = null;
		String url;
		
		String[] components = getParameter2().split(";");
		
		if(components.length >= 3)
		{
			username = components[0];
			password = components[1];
			url = components[2];
		}
		else
			url = components[0];
			
		try
		{
			url = Miscellaneous.replaceVariablesInText(url, context);
						
			Actions myAction = new Actions();

			Miscellaneous.logEvent("i", "HTTP", "Attempting download of " + url, 4);	//getResources().getString("attemptingDownloadOf");
			
			if(this.getParameter1())	// use authentication
				new DownloadTask().execute(url, username, password);
			else
				new DownloadTask().execute(url, null, null);
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "triggerUrl", context.getResources().getString(R.string.logErrorTriggeringUrl) + ": " + e.getMessage() + ", detailed: " + Log.getStackTraceString(e), 2);
		}
	}	
	
	public class DownloadTask extends AsyncTask<String, Void, String>
	{
	    @Override
	    public String doInBackground(String... parameters)
	    {
			Thread.setDefaultUncaughtExceptionHandler(Miscellaneous.uncaughtExceptionHandler);
			
	    	int attempts=1;
	    	String urlString=parameters[0];
	    	
	    	String urlUsername = null;
	    	String urlPassword = null;
	    	if(parameters.length >= 3)
	    	{
		    	urlUsername=parameters[1];
		    	urlPassword=parameters[2];
	    	}
	    	
	    	String response = "httpError";
	    	HttpGet post;
	    	
	    	if(Settings.httpAttempts < 1)
	    		Miscellaneous.logEvent("w", "HTTP Request", Miscellaneous.getAnyContext().getResources().getString(R.string.cantDownloadTooFewRequestsInSettings), 3);
	    	
	    	while(attempts <= Settings.httpAttempts && response.equals("httpError"))
	    	{
	    		Miscellaneous.logEvent("i", "HTTP Request", "Attempt " + String.valueOf(attempts++) + " of " + String.valueOf(Settings.httpAttempts), 3);

				// Either thorough checking or no encryption
				if(!Settings.httpAcceptAllCertificates || !urlString.toLowerCase(Locale.getDefault()).contains("https"))
					response = Miscellaneous.downloadURL(urlString, urlUsername, urlPassword);
				else
					response = Miscellaneous.downloadURLwithoutCertificateChecking(urlString, urlUsername, urlPassword);

				try
				{
					Thread.sleep(Settings.httpAttemptGap * 1000);
				}
				catch (InterruptedException e1)
				{
					Miscellaneous.logEvent("w", "HTTP RESULT", "Failed to pause between HTTP requests.", 5);
				}
	    	}
	    	
	    	Miscellaneous.logEvent("i", "HTTPS RESULT", response, 5);
			
			return response;
	    }
	    
	    @Override
	    public void onPostExecute(String result)
	    {
	        //Do something with result
	    	//Toast.makeText(context, text, duration) result;
	    	Miscellaneous.logEvent("i", "HTTP RESULT", result, 3);
	    	Actions myAction=new Actions();
	    	myAction.useDownloadedWebpage(result);
	    }
	}
}