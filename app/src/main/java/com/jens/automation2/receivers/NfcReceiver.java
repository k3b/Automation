package com.jens.automation2.receivers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.R;
import com.jens.automation2.Rule;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressLint("NewApi")
public class NfcReceiver
{
	public static final String MIME_TEXT_PLAIN = "text/plain";
    public static String lastReadLabel = null;
    
	private static NfcAdapter nfcAdapter = null;
	public static NfcAdapter getNfcAdapter(Context context)
	{	
		if(nfcAdapter == null)
		{
			if(Build.VERSION.SDK_INT <= 10)
			{
				// NFC not supported until after Gingerbread.
				Miscellaneous.logEvent("w", "NFC", context.getResources().getString(R.string.nfcNotSupportedInThisAndroidVersionYet), 3);
				return null;
			}
			else
			{
				nfcAdapter = NfcAdapter.getDefaultAdapter(context);
			}
		}
		return nfcAdapter;
	}

	public static void setNfcAdapter(NfcAdapter nfcAdapter)
	{
		NfcReceiver.nfcAdapter = nfcAdapter;
	}

	@SuppressLint("NewApi")
	public static void checkIntentForNFC(Context context, Intent intent)
	{
		if(!NfcReceiver.checkNfcRequirements(context, false))
			return;
		
		String action = intent.getAction();
		if(action == null)
		{
	    	Miscellaneous.logEvent("i", "NFC", "action=null", 5);
			return;
		}
		
		if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
		{
	    	Miscellaneous.logEvent("i", "NFC", "ACTION_NDEF_DISCOVERED", 4);
	    	
			getNfcAdapter(context);
		
			if(nfcAdapter == null)	// if it's still null the device doesn't support NFC
			{
				return;
			}
			
			String mimeType = intent.getType();
			if(mimeType.equals(MIME_TEXT_PLAIN))
			{
				Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				
//		        if(NfcReceiver.discoveredTag == null)
//		        	NfcReceiver.discoveredTag = tag;
		        
				new NdefReaderTask().execute(tag);
			}
		}
//		else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action))
//		{
//	         
//	        // In case we would still use the Tech Discovered Intent
//	        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//	        String[] techList = tag.getTechList();
//	        String searchedTech = Ndef.class.getName();
//	         
//	        for (String tech : techList)
//	        {
//	            if (searchedTech.equals(tech))
//	            {
//	                new NdefReaderTask().execute(tag);
//	                break;
//	            }
//	        }
//	    }
//		else
//		{
//			// No NFC NDEF intent
//			Miscellaneous.logEvent("w", "NFC", context.getResources().getString(R.string.nfcNoNdefIntentBut) + " " + action + ".", 5);
//		}
	}
	
	@SuppressLint("NewApi")
	public static class NdefReaderTask extends AsyncTask<Tag, Void, String>
	{		 
	    @Override
	    protected String doInBackground(Tag... params)
	    {
	        Tag tag = params[0];
	        
	        Ndef ndef = Ndef.get(tag);
	        if (ndef == null)
	        {
	            // NDEF is not supported by this Tag. 
	            return null;
	        }
	        NdefMessage ndefMessage = ndef.getCachedNdefMessage();	        
	        NdefRecord[] records = ndefMessage.getRecords();
	        for (NdefRecord ndefRecord : records)
	        {
	            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT))
	            {
	                try
	                {
	                    return readText(ndefRecord);
	                }
	                catch (UnsupportedEncodingException e)
	                {
	                    Miscellaneous.logEvent("e", "NFC", Miscellaneous.getAnyContext().getString(R.string.nfcUnsupportedEncoding) + " " + Log.getStackTraceString(e), 3);
	                }
	            }
	        }
	        return null;
	        
//	        return readTag(tag);
	    }
	     
	    @Override
	    protected void onPostExecute(String result)
	    {
	        if (result != null && result.length() > 0)
	        {
	            // Text of tag is now stored in variable "result"
	        	lastReadLabel = result;
	        	Miscellaneous.logEvent("i", "NFC", Miscellaneous.getAnyContext().getResources().getString(R.string.nfcTagFoundWithText) + " " + result, 3);
	        	Toast.makeText(Miscellaneous.getAnyContext(), Miscellaneous.getAnyContext().getResources().getString(R.string.nfcTagFoundWithText) + " " + result, Toast.LENGTH_LONG).show();

	        	AutomationService asInstance = AutomationService.getInstance();
	        	if(asInstance == null)
	        	{
	        		Context context = Miscellaneous.getAnyContext();
	        		if(context != null)
	        		{
	        			Miscellaneous.logEvent("w", "NFC", context.getResources().getString(R.string.serviceNotRunning) + " " + context.getResources().getString(R.string.cantRunRule), 4);
	        			Toast.makeText(context, context.getResources().getString(R.string.serviceNotRunning) + " " + context.getResources().getString(R.string.cantRunRule), Toast.LENGTH_LONG).show();
	        		}
	        	}
	        	else
	        	{
		    		ArrayList<Rule> allRulesWithNfcTags = Rule.findRuleCandidatesByNfc();
		    		for(int i=0; i<allRulesWithNfcTags.size(); i++)
		    		{
		    			if(allRulesWithNfcTags.get(i).getsGreenLight(asInstance))
		    				allRulesWithNfcTags.get(i).activate(asInstance, false);
		    		}
	        	}
	        }
	    }
	}
	
	public static class NdefWriterTask extends AsyncTask<Object, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Object... params)
		{
			String textToWrite = (String)params[0];
			Tag tagToWrite = (Tag)params[1];
			return writeTag(textToWrite, tagToWrite);
		}

//		@Override
//		protected void onPostExecute(Boolean result)
//		{
//			return result;
//		}		
	}
	
	public static String readTag(Tag tag)
	{    	         
//		if(tag == null)
//		{
//			Toast.makeText(Miscellaneous.getAnyContext(), Miscellaneous.getAnyContext().getResources().getString(R.string.nfcNoTag), Toast.LENGTH_LONG).show();
//			return null;
//		}
		
        Ndef ndef = Ndef.get(tag);
        if (ndef == null)
        {
            // NDEF is not supported by this Tag. 
            return null;
        }
 
        NdefMessage ndefMessage = ndef.getCachedNdefMessage();
 
        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records)
        {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT))
            {
                try
                {
                    return readText(ndefRecord);
                }
                catch (UnsupportedEncodingException e)
                {
                    Miscellaneous.logEvent("w", "NFC", "Unsupported Encoding: " +  Log.getStackTraceString(e), 3);
                }
            }
        }
 
        return null;
	}
	
	private static String readText(NdefRecord record) throws UnsupportedEncodingException
	{
	    /*
	     * See NFC forum specification for "Text Record Type Definition" at 3.2.1 
	     * 
	     * http://www.nfc-forum.org/specs/
	     * 
	     * bit_7 defines encoding
	     * bit_6 reserved for future use, must be 0
	     * bit_5..0 length of IANA language code
	     */
	
	    byte[] payload = record.getPayload();
	
	    // Get the Text Encoding
	    String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
	
	    // Get the Language Code
	    int languageCodeLength = payload[0] & 0063;
	     
	    // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
	    // e.g. "en"
	     
	    // Get the Text
	    return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
	}

	public static boolean writeTag(String textToWrite, Tag tag)
	{
		Miscellaneous.logEvent("i", "NFC", "Attempting to write tag...", 2);

		String packageName = Miscellaneous.getAnyContext().getPackageName();
		NdefRecord appRecord = NdefRecord.createApplicationRecord(packageName);
	    // Record with actual data we care about
		byte[] textBytes = textToWrite.getBytes();
		byte[] textPayload = new byte[textBytes.length + 3];
		textPayload[0] = 0x02; // 0x02 = UTF8
		textPayload[1] = 'e'; // Language = en
		textPayload[2] = 'n';
		System.arraycopy(textBytes, 0, textPayload, 3, textBytes.length);
		NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], textPayload);
		
	    // Complete NDEF message with both records
	    NdefMessage completeMessageToWrite = new NdefMessage(new NdefRecord[] {textRecord, appRecord});
		
		int size = completeMessageToWrite.toByteArray().length;
	    try
	    {
	        Ndef ndef = Ndef.get(tag);
	        if (ndef != null)
	        {
	            ndef.connect();
	            if (ndef.isWritable() && ndef.getMaxSize() > size)
	            {
	                ndef.writeNdefMessage(completeMessageToWrite);
	        		Miscellaneous.logEvent("i", "NFC", "Done writing tag.", 2);
	        		return true;
	            }
	        }
	        else
	        {
	            NdefFormatable format = NdefFormatable.get(tag);
	            if (format != null)
	            {
	                try
	                {
	                    format.connect();
	                    format.format(completeMessageToWrite);
		        		Miscellaneous.logEvent("i", "NFC", "Done writing tag.", 2);
		        		return true;
	                }
	                catch(IOException e)
	                {
		        		Miscellaneous.logEvent("e", "NFC", "Error writing tag: " + Log.getStackTraceString(e), 2);
	                }
	            }
	        }
	    }
	    catch(Exception e)
	    {
    		Miscellaneous.logEvent("e", "NFC", "Error writing tag: " + Log.getStackTraceString(e), 2);
	    }
	    
		return false;
	}
	
	public static boolean checkNfcRequirements(Context context, boolean showErrorMessage)
	{
		if(!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC))
		{
			if(showErrorMessage)
				Toast.makeText(context, context.getResources().getString(R.string.deviceDoesNotHaveNfc), Toast.LENGTH_LONG).show();
			
			return false;
		}
		else if(Build.VERSION.SDK_INT <= 10)
		{
			// NFC not supported until after Gingerbread.
			if(showErrorMessage)
				Toast.makeText(context, context.getResources().getString(R.string.nfcNotSupportedInThisAndroidVersionYet), Toast.LENGTH_LONG).show();
			
			return false;
		}
		
		return true;
	}
}
