package com.jens.automation2;

import android.content.Context;
import android.os.AsyncTask;
import android.text.style.TabStopSpan;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.client.methods.HttpGet;

import java.util.ArrayList;
import java.util.Locale;


public class Action
{
	public static final String actionParameter2Split = "ap2split";
	public static final String intentPairSeperator = "intPairSplit";

	public enum Action_Enum {	
								setWifi,
								setBluetooth,
								setUsbTethering,
								setWifiTethering,
								setDisplayRotation,
								turnWifiOn,turnWifiOff,
								turnBluetoothOn,turnBluetoothOff,
								triggerUrl,
								changeSoundProfile,
								turnUsbTetheringOn,turnUsbTetheringOff,
								turnWifiTetheringOn,turnWifiTetheringOff,
								enableScreenRotation, disableScreenRotation,
								startOtherActivity,
								waitBeforeNextAction,
								wakeupDevice,
								setAirplaneMode,
								setDataConnection,
								speakText,
								playMusic,
								setScreenBrightness,
								playSound,
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
										case wakeupDevice:
											return context.getResources().getString(R.string.wakeupDevice);
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
		
		if(this.getAction().equals(Action_Enum.setWifi))
		{
			if(this.getParameter1())
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnWifiOn));
			else
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnWifiOff));
		}
		else if(this.getAction().equals(Action_Enum.setBluetooth))
		{
			if(this.getParameter1())
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnBluetoothOn));
			else
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnBluetoothOff));
		}
		else if(this.getAction().equals(Action_Enum.setUsbTethering))
		{
			if(this.getParameter1())
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnUsbTetheringOn));
			else
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnUsbTetheringOff));
		}
		else if(this.getAction().equals(Action_Enum.setWifiTethering))
		{
			if(this.getParameter1())
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnWifiTetheringOn));
			else
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnWifiTetheringOff));
		}
		else if(this.getAction().equals(Action_Enum.setDisplayRotation))
		{
			if(this.getParameter1())
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionEnableScreenRotation));
			else
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionDisableScreenRotation));
		}
		else if(this.getAction().equals(Action_Enum.setAirplaneMode))
		{
			if(this.getParameter1())
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnAirplaneModeOn));
			else
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTurnAirplaneModeOff));
		}
		else if(this.getAction().equals(Action_Enum.setDataConnection))
		{
			if(this.getParameter1())
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionSetDataConnectionOn));
			else
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionSetDataConnectionOff));
		}
		else if(this.getAction().equals(Action_Enum.startOtherActivity))
		{
			returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.startOtherActivity));
		}
		else if(this.getAction().equals(Action_Enum.triggerUrl))
		{
			returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionTriggerUrl));
		}
		else if(this.getAction().equals(Action_Enum.speakText))
		{
			returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionSpeakText));
		}
		else if(this.getAction().equals(Action_Enum.playMusic))
		{
			returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.actionPlayMusic));
		}
		else if(this.getAction().equals(Action_Enum.sendTextMessage))
		{
			returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.sendTextMessage));
		}
		else if(this.getAction().equals(Action_Enum.wakeupDevice))
		{
			returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.wakeupDevice));
		}
		else if(this.getAction().equals(Action_Enum.playSound))
		{
			returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.playSound));
		}
		else
			returnString.append(action.toString());

		if(this.getAction().equals(Action_Enum.triggerUrl))
		{
			String[] components = parameter2.split(";");
			if(components.length >= 3)
			{
				returnString.append(": " + components[2]);
				
				if(parameter1)
					returnString.append(" using authentication.");
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
				returnString.append(" to number " + components[0]);

				returnString.append(". Message: " + components[1]);
			}
		}
		else if(this.getAction().equals(Action_Enum.setScreenBrightness))
		{
			returnString.append(" to ");

			if(parameter1)
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.brightnessAuto));
			else
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.brightnessManual));

			returnString.append(" / " + Integer.parseInt(parameter2) + "%");
		}
		else
			if (parameter2 != null && parameter2.length() > 0)
				returnString.append(": " + parameter2);
		
		return returnString.toString();
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
//				if(this.getParameter2().equals("silent"))
//					Actions.setSound(context, AudioManager.RINGER_MODE_SILENT);
//				else if(this.getParameter2().equals("vibrate"))
//					Actions.setSound(context, AudioManager.RINGER_MODE_VIBRATE);
//				else if(this.getParameter2().equals("normal"))
//					Actions.setSound(context, AudioManager.RINGER_MODE_NORMAL);
//				else
//				{
					Profile p = Profile.getByName(this.getParameter2());
					if (p != null)
						p.activate(context);
//				}
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
					Actions.setWifi(context, getParameter1(), toggleActionIfPossible);
					break;
				case setWifiTethering:
					Actions.setWifiTethering(context, getParameter1(), toggleActionIfPossible);
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
				case wakeupDevice:
					Actions.wakeupDevice(Long.parseLong(this.getParameter2()));
					// wakeupDevice() will create a seperate thread. That'll take some time, we wait 100ms.
					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
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
				case playSound:
					Actions.playSound(getParameter1(), getParameter2());
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
			Miscellaneous.logEvent("e", "triggerUrl", context.getResources().getString(R.string.errorTriggeringUrl) + ": " + e.getMessage() + ", detailed: " + Log.getStackTraceString(e), 2);
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
	    		
//				try
//				{
					// Either thorough checking or no encryption
					if(!Settings.httpAcceptAllCertificates | !urlString.toLowerCase(Locale.getDefault()).contains("https"))
//					{
//						URL url = new URL(urlString);
//						URLConnection urlConnection = url.openConnection();
//						urlConnection.setReadTimeout(Settings.httpAttemptsTimeout * 1000);
//						InputStream in = urlConnection.getInputStream();
//						response = Miscellaneous.convertStreamToString(in);
						
						response = Miscellaneous.downloadURL(urlString, urlUsername, urlPassword);
//					}
					else
//					{
						response = Miscellaneous.downloadURLwithoutCertificateChecking(urlString, urlUsername, urlPassword);
//						post = new HttpGet(new URI(urlString));
//						final HttpParams httpParams = new BasicHttpParams();
//					    HttpConnectionParams.setConnectionTimeout(httpParams, Settings.httpAttemptsTimeout * 1000);
//						HttpClient client = new DefaultHttpClient(httpParams);
//
//						client = sslClient(client);
//						
//						// Execute HTTP Post Request					 
//						HttpResponse result = client.execute(post);
//						response = EntityUtils.toString(result.getEntity());
//					}
//				}
//				catch (URISyntaxException e)
//				{
//					Miscellaneous.logEvent("w", "HTTP RESULT", Log.getStackTraceString(e), 3);
//				}
//				catch (ClientProtocolException e)
//				{
//					Miscellaneous.logEvent("w", "HTTP RESULT", Log.getStackTraceString(e), 3);
//				}
//				catch (IOException e)
//				{
//					Miscellaneous.logEvent("w", "HTTP RESULT", Log.getStackTraceString(e), 3);
//					e.printStackTrace();
//				}
//				finally
//				{
					try
					{
						Thread.sleep(Settings.httpAttemptGap * 1000);
					}
					catch (InterruptedException e1)
					{
						Miscellaneous.logEvent("w", "HTTP RESULT", "Failed to pause between HTTP requests.", 5);
					}
//				}
	    	}
	    	
//	    	Miscellaneous.logEvent("i", "HTTPS RESULT", response, 3);
			
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