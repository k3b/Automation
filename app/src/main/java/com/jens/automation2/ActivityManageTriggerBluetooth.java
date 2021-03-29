package com.jens.automation2;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.jens.automation2.receivers.BluetoothReceiver;

public class ActivityManageTriggerBluetooth extends Activity
{
	protected static Trigger editedBluetoothTrigger;
	RadioButton radioAnyBluetoothDevice, radioNoDevice, radioDeviceFromList, radioBluetoothConnected, radioBluetoothDisconnected, radioBluetoothInRange, radioBluetoothOutRange;
	Button bSaveBluetoothTrigger;
	Spinner spinnerBluetoothDevices;
	
	ArrayAdapter<String> bluetoothDevicesSpinnerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_trigger);
		
		radioAnyBluetoothDevice = (RadioButton)findViewById(R.id.radioAnyBluetoothDevice);
		radioNoDevice = (RadioButton)findViewById(R.id.radioNoDevice);
		radioDeviceFromList = (RadioButton)findViewById(R.id.radioDeviceFromList);
		radioBluetoothConnected = (RadioButton)findViewById(R.id.radioBluetoothConnected);
		radioBluetoothDisconnected = (RadioButton)findViewById(R.id.radioBluetoothDisconnected);
		radioBluetoothInRange = (RadioButton)findViewById(R.id.radioBluetoothInRange);
		radioBluetoothOutRange = (RadioButton)findViewById(R.id.radioBluetoothOutRange);
		bSaveBluetoothTrigger = (Button)findViewById(R.id.bSaveBluetoothTrigger);
		spinnerBluetoothDevices = (Spinner)findViewById(R.id.spinnerBluetoothDevices);
		
		bluetoothDevicesSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.text_view_for_poi_listview_mediumtextsize, BluetoothReceiver.getAllPairedBluetoothDevicesStrings());
		
		radioDeviceFromList.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				spinnerBluetoothDevices.setEnabled(isChecked);
			}
		});
		
		bSaveBluetoothTrigger.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				if(saveTrigger())
				{
					setResult(RESULT_OK);
					finish();
				}
			}
		});
		
		refreshBluetoothDeviceSpinner();
		spinnerBluetoothDevices.setEnabled(false);

		if(editedBluetoothTrigger.getBluetoothDeviceAddress() != null && editedBluetoothTrigger.getBluetoothDeviceAddress().length() > 0)
			loadVariableIntoGui();
	}
	
	protected void refreshBluetoothDeviceSpinner()
	{
		Miscellaneous.logEvent("i", "Spinner", "Attempting to update spinnerBluetoothDevices", 4);
		if(spinnerBluetoothDevices.getAdapter() == null)
		{
			spinnerBluetoothDevices.setAdapter(bluetoothDevicesSpinnerAdapter);
		}

		bluetoothDevicesSpinnerAdapter.notifyDataSetChanged();
	}
	
	protected boolean saveTrigger()
	{
		try
		{			
			// DEVICE
			
			if(radioAnyBluetoothDevice.isChecked())
			{
				editedBluetoothTrigger.setBluetoothDeviceAddress("<any>");
			}
			else if(radioNoDevice.isChecked())
			{
				editedBluetoothTrigger.setBluetoothDeviceAddress("<none>");
			}
			else if(radioDeviceFromList.isChecked())
			{
				BluetoothDevice selectedDevice = BluetoothReceiver.getAllPairedBluetoothDevices()[spinnerBluetoothDevices.getSelectedItemPosition()];
				if(selectedDevice != null)
				{
					editedBluetoothTrigger.setBluetoothDeviceAddress(selectedDevice.getAddress());
				}
				else
					Miscellaneous.logEvent("w", "ActivityManageBluetoothTrigger", "Device not found.", 3);
			}
			else
			{
				Toast.makeText(ActivityManageTriggerBluetooth.this, getResources().getString(R.string.selectDeviceOption), Toast.LENGTH_LONG).show();
				return false;
			}
			
			
			// EVENT
			
			if(radioBluetoothConnected.isChecked())
			{
				editedBluetoothTrigger.setTriggerParameter(true);
				editedBluetoothTrigger.setBluetoothEvent(BluetoothDevice.ACTION_ACL_CONNECTED);
			}
			else if(radioBluetoothDisconnected.isChecked())
			{
				editedBluetoothTrigger.setTriggerParameter(false);
				editedBluetoothTrigger.setBluetoothEvent(BluetoothDevice.ACTION_ACL_DISCONNECTED);
			}
			else if(radioBluetoothInRange.isChecked())
			{
				editedBluetoothTrigger.setTriggerParameter(true);
				editedBluetoothTrigger.setBluetoothEvent(BluetoothDevice.ACTION_FOUND);
			}
			else if(radioBluetoothOutRange.isChecked())
			{
				editedBluetoothTrigger.setTriggerParameter(false);
				editedBluetoothTrigger.setBluetoothEvent(BluetoothDevice.ACTION_FOUND);
			}
			else
			{
				Toast.makeText(ActivityManageTriggerBluetooth.this, getResources().getString(R.string.selectConnectionOption), Toast.LENGTH_LONG).show();
				return false;
			}
			
			return true;
		}
		catch(Exception e)
		{			
			Miscellaneous.logEvent("w", "ActivityManageBluetoothTrigger", "Error during trigger create/change: " + Log.getStackTraceString(e), 2);
		}

		return false;
	}
	
	protected void loadVariableIntoGui()
	{
		if(editedBluetoothTrigger != null)
		{
			if(editedBluetoothTrigger.getBluetoothDeviceAddress().equals("<any>"))
			{
				radioAnyBluetoothDevice.setChecked(true);
			}
			else if(editedBluetoothTrigger.getBluetoothDeviceAddress().equals("<none>"))
			{
				radioNoDevice.setChecked(true);
			}
			else
			{
				radioDeviceFromList.setChecked(true);
				spinnerBluetoothDevices.setSelection(BluetoothReceiver.getDevicePositionByAddress(editedBluetoothTrigger.getBluetoothDeviceAddress()));
			}
			
			if(editedBluetoothTrigger.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_CONNECTED))
			{
				radioBluetoothConnected.setChecked(true);
			}
			else if(editedBluetoothTrigger.getBluetoothEvent().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED))
			{
				radioBluetoothDisconnected.setChecked(true);
			}
			else
			{
				radioBluetoothInRange.setChecked(true);
			}
		}
	}
}
