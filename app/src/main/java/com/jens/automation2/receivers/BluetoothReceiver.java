package com.jens.automation2.receivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.jens.automation2.ActivityPermissions;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.R;
import com.jens.automation2.Rule;
import com.jens.automation2.Trigger.Trigger_Enum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

public class BluetoothReceiver extends BroadcastReceiver implements AutomationListenerInterface
{
	protected static ArrayList<BluetoothDevice> connectedDevices = new ArrayList<BluetoothDevice>();
	protected static ArrayList<BluetoothDevice> devicesInRange = new ArrayList<BluetoothDevice>();

	protected static BluetoothDevice lastAffectedDevice = null;
	protected static String lastAction = null;
	
	protected static IntentFilter bluetoothReceiverIntentFilter = null;
	protected static boolean bluetoothReceiverActive = false;
	protected static BluetoothReceiver bluetoothReceiverInstance = null;
	
	public static boolean isBluetoothReceiverActive()
	{
		return bluetoothReceiverActive;
	}
	
	public static void startBluetoothReceiver()
	{
		if(bluetoothReceiverInstance == null)
			bluetoothReceiverInstance = new BluetoothReceiver();
		
		if(bluetoothReceiverIntentFilter == null)
		{
			bluetoothReceiverIntentFilter = new IntentFilter();
			bluetoothReceiverIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
			bluetoothReceiverIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
			bluetoothReceiverIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		}
		
		try
		{
			if(!bluetoothReceiverActive)
			{
				Miscellaneous.logEvent("i", "BluetoothReceiver", "Starting BluetoothReceiver", 4);
				bluetoothReceiverActive = true;
				AutomationService.getInstance().registerReceiver(bluetoothReceiverInstance, bluetoothReceiverIntentFilter);
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "BluetoothReceiver", "Error starting BluetoothReceiver: " + Log.getStackTraceString(ex), 3);
		}
	}
	public static void stopBluetoothReceiver()
	{
		try
		{
			if(bluetoothReceiverActive)
			{
				Miscellaneous.logEvent("i", "BluetoothReceiver", "Stopping BluetoothReceiver", 4);
				bluetoothReceiverActive = false;
				AutomationService.getInstance().unregisterReceiver(bluetoothReceiverInstance);
			}
		}
		catch(Exception ex)
		{
			Miscellaneous.logEvent("e", "BluetoothReceiver", "Error stopping BluetoothReceiver: " + Log.getStackTraceString(ex), 3);
		}
	}
	
	public static BluetoothDevice getLastAffectedDevice()
	{
		return lastAffectedDevice;
	}

	public static String getLastAction()
	{
		return lastAction;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
//		Miscellaneous.logEvent("i", "BluetoothReceiver", "Bluetooth event.", 4);
		
		String action = intent.getAction();
		BluetoothDevice bluetoothDevice = null;
		
		if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED) | action.equals("android.bluetooth.device.action.ACL_CONNECTED"))
		{			
			bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			lastAffectedDevice = bluetoothDevice;
			lastAction = action;
			connectedDevices.add(bluetoothDevice);
			Miscellaneous.logEvent("i", "BluetoothReceiver", String.format(context.getResources().getString(R.string.bluetoothConnectionTo), bluetoothDevice.getName()), 3);
		}
		else if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) | action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED) | action.equals("android.bluetooth.device.ACTION_ACL_DISCONNECTED") | action.equals("android.bluetooth.device.ACTION_ACL_DISCONNECT_REQUESTED"))
		{
			bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			lastAffectedDevice = bluetoothDevice;
			lastAction = action;
			connectedDevices.remove(bluetoothDevice);
			Miscellaneous.logEvent("i", "BluetoothReceiver", String.format(context.getResources().getString(R.string.bluetoothDisconnectFrom), bluetoothDevice.getName()), 3);
		}
		else if(action.equals(BluetoothDevice.ACTION_FOUND) | action.equals("android.bluetooth.device.ACTION_FOUND"))
		{
			bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			lastAffectedDevice = bluetoothDevice;
			lastAction = action;
			devicesInRange.add(bluetoothDevice);
			Miscellaneous.logEvent("i", "BluetoothReceiver", String.format(context.getResources().getString(R.string.bluetoothDeviceInRange), bluetoothDevice.getName()), 3);
		}

		ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger_Enum.bluetoothConnection);
		for(int i=0; i<ruleCandidates.size(); i++)
		{
			if(ruleCandidates.get(i).getsGreenLight(AutomationService.getInstance()))
				ruleCandidates.get(i).activate(AutomationService.getInstance(), false);
		}
	}

	public static BluetoothDevice[] getAllPairedBluetoothDevices()
	{
		BluetoothDevice[] returnArray;

		try
		{
			Set<BluetoothDevice> deviceList = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
			returnArray = deviceList.toArray(new BluetoothDevice[deviceList.size()]);

			Arrays.sort(returnArray, new Comparator<BluetoothDevice>()
			{
				@Override
				public int compare(BluetoothDevice lhs, BluetoothDevice rhs)
				{
					return lhs.getName().compareTo(rhs.getName());
				}

				;
			});
		}
		catch(NullPointerException e)
		{
			// There are no paired bluetooth devices.

			returnArray = new BluetoothDevice[] {};
		}

		return returnArray;
	}
	
	public static String[] getAllPairedBluetoothDevicesStrings()
	{
		ArrayList<String> names = new ArrayList<String>();
		for(BluetoothDevice device : getAllPairedBluetoothDevices())
			names.add(device.getName() + " (" + device.getAddress() + ")");

		return names.toArray(new String[names.size()]);
	}
	
	public static BluetoothDevice getDeviceByName(String name)
	{
		for(BluetoothDevice device : getAllPairedBluetoothDevices())
		{
			if(device.getName().equals(name))
				return device;
		}
		
		return null;
	}
	
	public static BluetoothDevice getDeviceByAddress(String address)
	{
		for(BluetoothDevice device : getAllPairedBluetoothDevices())
		{
			if(device.getAddress().equals(address))
				return device;
		}
		
		return null;
	}
	
	public static int getDevicePositionByAddress(String address)
	{
		BluetoothDevice[] allDevices = getAllPairedBluetoothDevices();
		for(int i=0; i<allDevices.length; i++)
		{
			if(allDevices[i].getAddress().equals(address))
				return i;
		}
		
		return -1;
	}
	
	public static boolean isDeviceCurrentlyConnected(BluetoothDevice searchDevice)
	{
		for(BluetoothDevice device : connectedDevices)
		{
			try
			{
				if (device.getAddress().equals(searchDevice.getAddress()))
					return true;
			}
			catch(NullPointerException e)
			{
				/*
					Just proceed with the next loop.

					This may happen if devices have been unpaired since
					they have been added for usage in a rule.
				 */
			}
		}

		return false;
	}
	
	public static boolean isAnyDeviceConnected()
	{
		if(connectedDevices.size() > 0)
			return true;
		else
			return false;
	}
	
	public static boolean isAnyDeviceInRange()
	{		
		if(devicesInRange.size() > 0)
			return true;
		else
			return false;
	}

	public static boolean isDeviceInRange(BluetoothDevice searchDevice)
	{
		for(BluetoothDevice device : devicesInRange)
			if(device.getAddress().equals(searchDevice.getAddress()))
				return true;
		
		return false;
	}
	
	private void discovery()
	{
		BluetoothAdapter.getDefaultAdapter().startDiscovery(); 
		BroadcastReceiver discoveryReceiver = new BroadcastReceiver()
		{
			public void onReceive(Context context, Intent intent)
			{
			    String action = intent.getAction();
			    //ACTION_DISCOVERY_STARTED and ACTION_DISCOVERY_FINISHED
			    if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
			    {
			    	// This would be a good point to look for devices that are not in range anymore.
			    }
			}
		};
	}

	@Override
	public void startListener(AutomationService automationService)
	{
		BluetoothReceiver.startBluetoothReceiver();
	}

	@Override
	public void stopListener(AutomationService automationService)
	{
		BluetoothReceiver.stopBluetoothReceiver();
	}

	public static boolean haveAllPermission()
	{
		return ActivityPermissions.havePermission("android.permission.BLUETOOTH_ADMIN", Miscellaneous.getAnyContext()) &&
				ActivityPermissions.havePermission("android.permission.BLUETOOTH", Miscellaneous.getAnyContext()) &&
				ActivityPermissions.havePermission("android.permission.ACCESS_NETWORK_STATE", Miscellaneous.getAnyContext());
	}

	@Override
	public boolean isListenerRunning()
	{
		return BluetoothReceiver.isBluetoothReceiverActive();
	}

	@Override
	public Trigger_Enum[] getMonitoredTrigger()
	{
		return new Trigger_Enum[] { Trigger_Enum.bluetoothConnection };
	}

	/**
	 * Check for Bluetooth.
	 *
	 * @return true if Bluetooth is available.
	 */
	public static boolean isBluetoothEnabled()
	{
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		return bluetoothAdapter != null && bluetoothAdapter.isEnabled() && bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON;
	}
}