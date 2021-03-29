package com.jens.automation2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jens.automation2.receivers.NfcReceiver;

@SuppressLint("NewApi")
public class ActivityManageTriggerNfc extends Activity
{
	public static String generatedId = null;
	private static Tag discoveredTag = null;
	
	EditText etNewNfcIdValue;
	Button bReadNfcTag, bUseValueCurrentlyStored, bWriteNfcTag;
	TextView tvCurrentNfcId;
	
	private static int currentStatus = 0;
	private static ProgressDialog progressDialog = null;
	
	// Check if NFC is activated

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_trigger_nfc);
		
		etNewNfcIdValue = (EditText)findViewById(R.id.etNewNfcIdValue);
		bReadNfcTag = (Button)findViewById(R.id.bReadNfcTag);
		bUseValueCurrentlyStored = (Button)findViewById(R.id.bUseValueCurrentlyStored);
		bWriteNfcTag = (Button)findViewById(R.id.bWriteNfcTag);
		tvCurrentNfcId = (TextView)findViewById(R.id.tvCurrentNfcId);
		
		bReadNfcTag.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				if(discoveredTag != null)
				{
					generatedId = NfcReceiver.readTag(discoveredTag);
					tvCurrentNfcId.setText(generatedId);
				}
				else
				{
					progressDialog = ProgressDialog.show(ActivityManageTriggerNfc.this, null, getResources().getString(R.string.nfcBringTagIntoRange), false, true, new OnCancelListener()
					{							
						@Override
						public void onCancel(DialogInterface dialog)
						{
							progressDialog.dismiss();
							progressDialog = null;
							currentStatus = 0;
						}
					});
				}				
			}
		});
		
		bUseValueCurrentlyStored.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				if(discoveredTag != null)
				{
					if(checkEnteredText(false))
					{
						setResult(RESULT_OK);
						finish();
					}
				}
				else
				{
					progressDialog = ProgressDialog.show(ActivityManageTriggerNfc.this, null, getResources().getString(R.string.nfcBringTagIntoRange), false, true, new OnCancelListener()
					{							
						@Override
						public void onCancel(DialogInterface dialog)
						{
							progressDialog.dismiss();
							progressDialog = null;
							currentStatus = 0;
						}
					});
					
					currentStatus = 1;
				}
			}
		});
		
		bWriteNfcTag.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				if(checkEnteredText(true))
				{
//					ActivityPermissions.requestSpecificPermission("android.permission.NFC");
					if(discoveredTag != null)
					{
						tryWrite();
					}
					else
					{
						progressDialog = ProgressDialog.show(ActivityManageTriggerNfc.this, null, getResources().getString(R.string.nfcBringTagIntoRange), false, true, new OnCancelListener()
						{							
							@Override
							public void onCancel(DialogInterface dialog)
							{
								progressDialog.dismiss();
								progressDialog = null;
								currentStatus = 0;
							}
						});
						
						currentStatus = 2;
						
//						Toast.makeText(ActivityManageNfc.this, "No tag.", Toast.LENGTH_LONG).show();
//						Miscellaneous.logEvent("w", "NFC", "No tag.", 2);
					}
				}
			}
		});
		
		if(generatedId != null)
			etNewNfcIdValue.setText(generatedId);
	}

	 public static void enableForegroundDispatch(final Activity activity)
	 {
		 NfcAdapter nfcAdapter = NfcReceiver.getNfcAdapter(activity);
		   
	     final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
	     intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
	     final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
	
	     IntentFilter[] filters = new IntentFilter[1];
	     String[][] techList = new String[][]{};
	
	     // Notice that this is the same filter as in our manifest.
	     filters[0] = new IntentFilter();
	     filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
	     filters[0].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
	     filters[0].addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
	     filters[0].addCategory(Intent.CATEGORY_DEFAULT);
//	     try
//	     {
//	         filters[0].addDataType(NfcReceiver.MIME_TEXT_PLAIN);
//	     }
//	     catch (MalformedMimeTypeException e)
//	     {
//	         throw new RuntimeException("Check your mime type.");
//	     }
	      
	     nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
	     
	     Miscellaneous.logEvent("i", "NFC", "Enabled foreground dispatch.", 5);
	 }
	
	 public static void disableForegroundDispatch(final Activity activity)
	 {
		   NfcAdapter nfcAdapter = NfcReceiver.getNfcAdapter(activity);
		   nfcAdapter.disableForegroundDispatch(activity);
		   Miscellaneous.logEvent("i", "NFC", "Disabled foreground dispatch.", 5);
	 }

	@Override
	protected void onPause()
	{
	    /**
	    * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
	    */
	    disableForegroundDispatch(this);	      
      
		super.onPause();
	}

	@Override
	protected void onResume()
	{	       
		super.onResume();
		/**
		 * It's important, that the activity is in the foreground (resumed). Otherwise
		 * an IllegalStateException is thrown. 
		 */
		enableForegroundDispatch(this);
		
//		NfcReceiver.checkIntentForNFC(this, new Intent(this.getApplicationContext(), this.getClass()));
	}

	@Override
	protected void onNewIntent(Intent intent)
	{		
		Miscellaneous.logEvent("i", "NFC", "ActivityManageNfc->onNewIntent().", 5);
		
		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		
		if(tag == null)
		{
			tvCurrentNfcId.setText(getResources().getString(R.string.nfcNoTag));
		}
		else
		{
			Miscellaneous.logEvent("i", "NFC", getResources().getString(R.string.nfcTagDiscovered), 4);
			Toast.makeText(this, getResources().getString(R.string.nfcTagDiscovered), Toast.LENGTH_LONG).show();
			discoveredTag = tag;
			if(progressDialog != null)
			{
				progressDialog.dismiss();
				progressDialog = null;
			}
			
			if(currentStatus == 0)
			{
				generatedId = NfcReceiver.readTag(discoveredTag);
				if(generatedId != null && generatedId.length() > 0)						
					tvCurrentNfcId.setText(generatedId);
				else
					tvCurrentNfcId.setText(getResources().getString(R.string.nfcTagDataNotUsable));
			}
			else if(currentStatus == 1)
			{
				tryRead();
			}
			else if(currentStatus == 2)
				if(checkEnteredText(true))
					tryWrite();
		}
		
//		NfcReceiver.checkIntentForNFC(this, intent);
	}
	
	private boolean checkEnteredText(boolean checkGuiValue)
	{
		if(checkGuiValue)
			generatedId = etNewNfcIdValue.getText().toString();
		
		if(generatedId.length() == 0)
		{
			generatedId = null;
			Toast.makeText(ActivityManageTriggerNfc.this, getResources().getString(R.string.nfcEnterValidIdentifier), Toast.LENGTH_LONG).show();
			return false;
		}
		else
			return true;
	}
	
	private void tryWrite()
	{
		if(NfcReceiver.writeTag(generatedId, discoveredTag))
		{
			currentStatus = 0;
			Toast.makeText(ActivityManageTriggerNfc.this, getResources().getString(R.string.nfcTagWrittenSuccessfully), Toast.LENGTH_LONG).show();
			setResult(RESULT_OK);
			finish();
		}
		else
		{
			currentStatus = 0;
			Toast.makeText(ActivityManageTriggerNfc.this, getResources().getString(R.string.nfcTagWriteError), Toast.LENGTH_LONG).show();
			Miscellaneous.logEvent("e", "NFC", getResources().getString(R.string.nfcTagWriteError), 2);
		}		
	}
	
	private void tryRead()
	{
		generatedId = NfcReceiver.readTag(discoveredTag);
		if(checkEnteredText(false))
		{
			currentStatus = 0;
			Toast.makeText(ActivityManageTriggerNfc.this, getResources().getString(R.string.nfcTagReadSuccessfully), Toast.LENGTH_LONG).show();
			setResult(RESULT_OK);
			finish();
		}
		else
		{
			currentStatus = 0;
			Toast.makeText(ActivityManageTriggerNfc.this, getResources().getString(R.string.nfcValueNotSuitable), Toast.LENGTH_LONG).show();
			generatedId = null;
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		discoveredTag = null;
	}
}