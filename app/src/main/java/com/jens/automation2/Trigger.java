package com.jens.automation2;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.jens.automation2.receivers.BluetoothReceiver;

import java.util.ArrayList;


public class Trigger
{
	/*
	 * Can be several things:
	 * -PointOfInterest
	 * -TimeFrame
	 * -Event (like charging, cable plugged, etc.)
	 */
	
	public enum Trigger_Enum { 
								pointOfInterest, timeFrame, charging, batteryLevel, usb_host_connection, speed, noiseLevel, wifiConnection, process_started_stopped, airplaneMode, roaming, nfcTag, activityDetection, bluetoothConnection, headsetPlugged, notification, phoneCall; //phoneCall always needs to be at the very end because of Google's shitty so called privacy
								
								public String getFullName(Context context)
								{
									switch(this)
									{
										case pointOfInterest:
											return context.getResources().getString(R.string.triggerPointOfInterest);
										case timeFrame:
											return context.getResources().getString(R.string.triggerTimeFrame);
										case charging:
											return context.getResources().getString(R.string.triggerCharging);
										case batteryLevel:
											return context.getResources().getString(R.string.batteryLevel);
										case usb_host_connection:
											return context.getResources().getString(R.string.triggerUsb_host_connection);
										case speed:
											return context.getResources().getString(R.string.triggerSpeed);
										case noiseLevel:
											return context.getResources().getString(R.string.triggerNoiseLevel);
										case wifiConnection:
											return context.getResources().getString(R.string.wifiConnection);
										case process_started_stopped:
											return context.getResources().getString(R.string.anotherAppIsRunning);
										case airplaneMode:
											return context.getResources().getString(R.string.airplaneMode);
										case roaming:
											return context.getResources().getString(R.string.roaming);
										case phoneCall:
											return context.getResources().getString(R.string.phoneCall);
										case nfcTag:
											return context.getResources().getString(R.string.nfcTag);
										case activityDetection:
											return context.getResources().getString(R.string.activityDetection);
										case bluetoothConnection:
											return context.getResources().getString(R.string.bluetoothConnection);
										case headsetPlugged:
											return context.getResources().getString(R.string.triggerHeadsetPlugged);
										case notification:
											return context.getResources().getString(R.string.notification);
										default:
											return "Unknown";
									}
								}
		
							};

	private boolean triggerParameter; //if true->started event, if false->stopped
	private String triggerParameter2;

	public static final String triggerParameter2Split = "tp2split";
	
    private Trigger_Enum triggerType = null;
    private PointOfInterest pointOfInterest = null;
    private TimeFrame timeFrame;

	private double speed; //km/h
    private long noiseLevelDb;
    private String wifiName = "";
    private String processName = null;
    private int batteryLevel;
    private int phoneDirection = 0; // 0=any, 1=incoming, 2=outgoing
    private String phoneNumber = null;
    private String nfcTagId = null;
    private String bluetoothEvent = null;
	private String bluetoothDeviceAddress = null;
    private int activityDetectionType = -1;
    private int headphoneType = -1;
    
	public int getHeadphoneType()
	{
		return headphoneType;
	}
	public void setHeadphoneType(int headphoneType)
	{
		this.headphoneType = headphoneType;
	}
	public String getNfcTagId()
	{
		return nfcTagId;
	}
	public void setNfcTagId(String nfcTagId)
	{
		this.nfcTagId = nfcTagId;
	}
	
	public int getActivityDetectionType()
	{
		return activityDetectionType;
	}
	public void setActivityDetectionType(int activityDetectionType)
	{
		this.activityDetectionType = activityDetectionType;
	}
	public String getBluetoothDeviceAddress()
	{
		return bluetoothDeviceAddress;
	}
	public void setBluetoothDeviceAddress(String bluetoothDeviceAddress)
	{
		this.bluetoothDeviceAddress = bluetoothDeviceAddress;
	}
	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}
	public String getPhoneNumber()
	{
		return phoneNumber;
	}

	public void setPhoneDirection(int phoneDirection)
	{
		this.phoneDirection = phoneDirection;
	}
	public int getPhoneDirection()
	{
		return phoneDirection;
	}

	public int getBatteryLevel()
	{
		return batteryLevel;
	}

	public void setBatteryLevel(int batteryLevel)
	{
		this.batteryLevel = batteryLevel;
	}

	public String getProcessName()
	{
		return processName;
	}

	public void setProcessName(String processName)
	{
		this.processName = processName;
	}

	public PointOfInterest getPointOfInterest()
	{
		return pointOfInterest;
	}

	public void setPointOfInterest(PointOfInterest setPointOfInterest)
	{
		this.pointOfInterest = setPointOfInterest;
	}
	
	public double getSpeed()
	{
		return speed;
	}

	public void setSpeed(double speed)
	{
		this.speed = speed;
	}
	
	public long getNoiseLevelDb()
	{
		return noiseLevelDb;
	}

	public void setNoiseLevelDb(long noiseLevelDb)
	{
		this.noiseLevelDb = noiseLevelDb;
	}

	public Trigger_Enum getTriggerType()
	{
		return triggerType;
	}

	public void setTriggerType(Trigger_Enum settriggerType)
	{
		this.triggerType = settriggerType;
	}

	public boolean getTriggerParameter()
	{
		return triggerParameter;
	}

	public void setTriggerParameter(boolean triggerParameter)
	{
		this.triggerParameter = triggerParameter;
	}

	public String getTriggerParameter2()
	{
		return triggerParameter2;
	}

	public void setTriggerParameter2(String triggerParameter2)
	{
		this.triggerParameter2 = triggerParameter2;
	}

	public TimeFrame getTimeFrame()
	{
		return timeFrame;
	}

	public void setTimeFrame(TimeFrame timeFrame)
	{
		this.timeFrame = timeFrame;
	}


	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@SuppressWarnings("unused")
	@Override
	public String toString()
	{
		StringBuilder returnString = new StringBuilder();
		
		switch(this.getTriggerType())
		{
			case charging:
				if(getTriggerParameter())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.starting) + " ");
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.stopping) + " ");
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.triggerCharging));
				break;
			case batteryLevel:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.batteryLevel));
				if(getTriggerParameter())
					returnString.append(" " + Miscellaneous.getAnyContext().getResources().getString(R.string.exceeds) + " ");
				else
					returnString.append(" " + Miscellaneous.getAnyContext().getResources().getString(R.string.dropsBelow) + " ");
				returnString.append(String.valueOf(this.getBatteryLevel()) + " %");
				break;
			case usb_host_connection:
				if(getTriggerParameter())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.connecting) + " ");
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.disconnecting) + " ");

				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.triggerUsb_host_connection));
				break;
			case pointOfInterest:
				if(this.getPointOfInterest() != null)
				{
					if(getTriggerParameter())
						returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.entering) + " ");
					else
						returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.leaving) + " ");

					returnString.append(this.getPointOfInterest().getName().toString());
				}
				else
				{
					if(getTriggerParameter())
						returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.leaving) + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.anyLocation));
					else
						returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.entering) + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.anyLocation));
				}
				break;
			case timeFrame:
				if(getTriggerParameter())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.entering) + " ");
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.leaving) + " ");

				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.triggerTimeFrame) + ": " + this.getTimeFrame().getTriggerTimeStart().toString() + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.until) + " " + this.getTimeFrame().getTriggerTimeStop().toString() + " on days " + this.getTimeFrame().getDayList().toString());
				break;
			case speed:
				if(getTriggerParameter())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.exceeding) + " ");
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.droppingBelow) + " ");
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.triggerSpeed) + ": " + String.valueOf(this.getSpeed()) + " km/h");
				break;
			case noiseLevel:
				if(getTriggerParameter())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.exceeding) + " ");
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.droppingBelow) + " ");

				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.triggerNoiseLevel) + ": " + String.valueOf(this.getNoiseLevelDb()) + " dB");
				break;
			case wifiConnection:
				String wifiDisplayName = "";				
				if(this.getWifiName().length() == 0)
					wifiDisplayName += Miscellaneous.getAnyContext().getResources().getString(R.string.anyWifi);
				else
					wifiDisplayName += this.getWifiName();
				
				if(getTriggerParameter())
					returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.connectedToWifi), wifiDisplayName));
				else
					returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.disconnectedFromWifi), wifiDisplayName));
				
				break;
			case process_started_stopped:
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.application) + " " + this.getProcessName() + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.is) + " ");
				if(this.triggerParameter)
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.started));
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.stopped));
				break;
			case airplaneMode:
				if(getTriggerParameter())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.activated) + " ");
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.deactivated) + " ");
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.airplaneMode));
				break;
			case roaming:
				if(getTriggerParameter())
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.activated) + " ");
				else
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.deactivated) + " ");
				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.roaming));
				break;
			case phoneCall:
				if(getPhoneDirection() == 1)
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.incomingAdjective) + " ");
				else if(getPhoneDirection() == 2)
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.outgoingAdjective) + " ");

				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.phoneCall));
				if(phoneNumber != null && !phoneNumber.equals("any"))
					returnString.append(" " + Miscellaneous.getAnyContext().getResources().getString(R.string.with) + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.number) + " " + phoneNumber);
				else
					returnString.append(" " + Miscellaneous.getAnyContext().getResources().getString(R.string.with) + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.anyNumber));
				
				if(getTriggerParameter())
					returnString.append(" " + Miscellaneous.getAnyContext().getResources().getString(R.string.started));
				else
					returnString.append(" " + Miscellaneous.getAnyContext().getResources().getString(R.string.stopped));
				break;
			case nfcTag:
				// This type doesn't have an activate/deactivate equivalent
//				if(getTriggerParameter())
//					returnString += Miscellaneous.getAnyContext().getResources().getString(R.string.activated) + " ";
//				else
//					returnString += Miscellaneous.getAnyContext().getResources().getString(R.string.deactivated) + " ";

				returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.closeTo) + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.nfcTag) + " " + Miscellaneous.getAnyContext().getResources().getString(R.string.withLabel) + " " + this.getNfcTagId());
				break;
			case activityDetection:
				try
				{
					Class testClass = Class.forName(ActivityManageRule.activityDetectionClassPath);
					if (ActivityPermissions.isPermissionDeclaratedInManifest(Miscellaneous.getAnyContext(), "com.google.android.gms.permission.ACTIVITY_RECOGNITION"))
					{
						// This type doesn't have an activate/deactivate equivalent, at least not yet.
//					try
//					{
						returnString.append(Miscellaneous.runMethodReflective(ActivityManageRule.activityDetectionClassPath, "getDescription", new Object[]{getActivityDetectionType()}));
//						for(Method method : activityDetection.getMethods())
//						{
//							if(method.getName().equalsIgnoreCase("getDescription"))
//								returnString.append(method.invoke());
////							returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.detectedActivity) + " " + activityDetection.getDescription(getActivityDetectionType()));
//						}
//					}
//					catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException e)
//					{
//						e.printStackTrace();
//					}

					}
					else
						returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.featureNotInFdroidVersion));
				}
				catch(ClassNotFoundException e)
				{
					returnString.append(Miscellaneous.getAnyContext().getResources().getString(R.string.featureNotInFdroidVersion));
				}
				break;
			case bluetoothConnection:
				String device = Miscellaneous.getAnyContext().getResources().getString(R.string.anyDevice);
//				if(this.bluetoothDeviceAddress != null)
//				{
					if(bluetoothDeviceAddress.equals("<any>"))
					{
						device = Miscellaneous.getAnyContext().getResources().getString(R.string.any);
					}
					else if(bluetoothDeviceAddress.equals("<none>"))
					{
						device = Miscellaneous.getAnyContext().getResources().getString(R.string.noDevice);
					}
					else
					{
						try
						{
							device = BluetoothReceiver.getDeviceByAddress(bluetoothDeviceAddress).getName() + " (" + this.bluetoothDeviceAddress + ")";
						}
						catch(NullPointerException e)
						{
							device = Miscellaneous.getAnyContext().getResources().getString(R.string.invalidDevice);
							Miscellaneous.logEvent("w", "Trigger", Miscellaneous.getAnyContext().getResources().getString(R.string.invalidDevice), 3);
						}
					}
					
					if(bluetoothEvent.equals(BluetoothDevice.ACTION_ACL_CONNECTED) | bluetoothEvent.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED))
						if(this.triggerParameter)
							returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.bluetoothConnectionTo), device));
						else
							returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.bluetoothDisconnectFrom), device));
					else if(bluetoothEvent.equals(BluetoothDevice.ACTION_FOUND))
						if(this.triggerParameter)
							returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.bluetoothDeviceInRange), device));
						else
							returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.bluetoothDeviceOutOfRange), device));
//				}
				break;
			case headsetPlugged:
				String type;
				switch(headphoneType)
				{
					case 0:
						type = Miscellaneous.getAnyContext().getResources().getString(R.string.headphoneSimple);
						break;
					case 1:
						type = Miscellaneous.getAnyContext().getResources().getString(R.string.headphoneMicrophone);
						break;
					case 2:
						type = Miscellaneous.getAnyContext().getResources().getString(R.string.headphoneAny);
						break;
					default:
						type = "not set";
						break;
				}
				if(getTriggerParameter())
					returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.headsetConnected), type));
				else
					returnString.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.headsetDisconnected), type));
				break;
			case notification:
				String[] params = getTriggerParameter2().split(triggerParameter2Split);
				String app = params[0];
				String titleDir = params[1];
				String title = params[2];
				String textDir = params[3];
				String text = params[4];
				StringBuilder triggerBuilder = new StringBuilder();

				String appString;
				if(app.equalsIgnoreCase("-1"))
					appString = Miscellaneous.getAnyContext().getResources().getString(R.string.anyApp);
				else
					appString = "app " + app;

				triggerBuilder.append(String.format(Miscellaneous.getAnyContext().getResources().getString(R.string.postsNotification), appString));

				if(title.length() > 0)
					triggerBuilder.append(", " + Miscellaneous.getAnyContext().getString(R.string.title) + " " + Trigger.getMatchString(titleDir) + " " + title);

				if(text.length() > 0)
					triggerBuilder.append(", " + Miscellaneous.getAnyContext().getString(R.string.text) + " " + Trigger.getMatchString(textDir) + " " + text);

				returnString.append(triggerBuilder.toString());

				break;
			default:
				returnString.append("error");
				break;
		}

		return returnString.toString();
	}

	public static final String directionEquals = "eq";
	public static final String directionContains = "ct";
	public static final String directionStartsWith = "sw";
	public static final String directionEndsWith = "ew";
	public static final String directionNotEquals = "ne";

	public static String getMatchString(String direction)
	{
		switch(direction)
		{
			case directionEquals:
				return Miscellaneous.getAnyContext().getString(R.string.directionStringEquals);
			case directionContains:
				return Miscellaneous.getAnyContext().getString(R.string.directionStringContains);
			case directionStartsWith:
				return Miscellaneous.getAnyContext().getString(R.string.directionStringStartsWith);
			case directionEndsWith:
				return Miscellaneous.getAnyContext().getString(R.string.directionStringEndsWith);
			case directionNotEquals:
				return Miscellaneous.getAnyContext().getString(R.string.directionStringNotEquals);
			default:
				return Miscellaneous.getAnyContext().getString(R.string.error);
		}
	}

	public static String getMatchCode(String direction)
	{
		if(direction.equalsIgnoreCase(Miscellaneous.getAnyContext().getString(R.string.directionStringEquals)))
			return directionEquals;
		else if(direction.equalsIgnoreCase(Miscellaneous.getAnyContext().getString(R.string.directionStringContains)))
			return directionContains;
		else if(direction.equalsIgnoreCase(Miscellaneous.getAnyContext().getString(R.string.directionStringStartsWith)))
			return directionStartsWith;
		else if(direction.equalsIgnoreCase(Miscellaneous.getAnyContext().getString(R.string.directionStringEndsWith)))
			return directionEndsWith;
		else if(direction.equalsIgnoreCase(Miscellaneous.getAnyContext().getString(R.string.directionStringNotEquals)))
			return directionNotEquals;
		else
			return Miscellaneous.getAnyContext().getString(R.string.error);
	}

	public static String[] getTriggerTypesAsArray()
	{
		ArrayList<String> triggerTypesList = new ArrayList<String>();
		
		/*for(int i=0; i<Trigger_Enum.values().length; i++)
		{
			triggerTypesList.add(Trigger_Enum.values()[i].toString());
		}*/
		for(Trigger_Enum triggerType : Trigger_Enum.values())
			triggerTypesList.add(triggerType.name());
		
		return (String[])triggerTypesList.toArray(new String[triggerTypesList.size()]);
	}
	

	public static String[] getTriggerTypesStringAsArray(Context context)
	{
		ArrayList<String> triggerTypesList = new ArrayList<String>();
		
		/*for(int i=0; i<Trigger_Enum.values().length; i++)
		{
			triggerTypesList.add(Trigger_Enum.values()[i].getFullName(context));
		}*/
		for(Trigger_Enum triggerType : Trigger_Enum.values())
			triggerTypesList.add(triggerType.getFullName(context));
		
		return (String[])triggerTypesList.toArray(new String[triggerTypesList.size()]);
	}

	public String getWifiName()
	{
		return wifiName;
	}

	public void setWifiName(String wifiName)
	{
		this.wifiName = wifiName;
	}
	public void setBluetoothEvent(String string)
	{
		this.bluetoothEvent = string;
	}
	public Object getBluetoothEvent()
	{
		return this.bluetoothEvent;
	}
	
}