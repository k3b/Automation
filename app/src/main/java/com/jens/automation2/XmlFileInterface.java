package com.jens.automation2;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.jens.automation2.Action.Action_Enum;
import com.jens.automation2.Trigger.Trigger_Enum;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;

import static com.jens.automation2.Trigger.triggerParameter2Split;

public class XmlFileInterface
{
	public static String settingsFileName = "Automation_settings.xml";
    public static File settingsFile = new File(Miscellaneous.getWriteableFolder() + "/" + settingsFileName);
    public static Context context;
    
    protected static final String encryptionKey = "Y1vsP12L2S3NkTJbDOR4bQ6i02hsoo";

    public static Boolean writeFile()
    {
    	if(settingsFile.getParentFile() == null)
    	{
    		String text = context.getResources().getString(R.string.noWritableFolderFound);
    		Miscellaneous.logEvent("e", "File", text, 1);
    		return false;
//    		throw new Exception(text);
    	}
		else
			Miscellaneous.logEvent("i", "File", "Using " + settingsFile.getParentFile() + " to store data.", 1);
    	
    	File temporaryWriteFile = new File(settingsFile.getPath() + "_tmp");
    	
        Miscellaneous.logEvent("i", "File", temporaryWriteFile.toString(), 4);

   		// maybe there is no sd card??
		if(!temporaryWriteFile.exists())
    	{
        	Miscellaneous.logEvent("i", "XML", "Creating new file: " + temporaryWriteFile.getAbsolutePath(), 4);
            try
			{
            	temporaryWriteFile.createNewFile();
			}
            catch (IOException e)
			{
				e.printStackTrace();
			}
        }
        else
        	Miscellaneous.logEvent("i", "XML", "File already exists.", 4);

        //we have to bind the new file with a FileOutputStream
        FileOutputStream fileos = null;

        try
        {
            fileos = new FileOutputStream(temporaryWriteFile);

            //we create a XmlSerializer in order to write xml data
            XmlSerializer serializer = Xml.newSerializer();

            try
            {
                //we set the FileOutputStream as output for the serializer, using UTF-8 encoding
                serializer.setOutput(fileos, "UTF-8");

                //Write <?xml declaration with encoding (if encoding not null) and standalone flag (if standalone not null) 
                serializer.startDocument(null, Boolean.valueOf(true)); 

                //set indentation option
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true); 

                serializer.startTag(null, "Automation");
                
	                serializer.startTag(null, "PointOfInterestCollection");
	                for(int i=0; i<PointOfInterest.getPointOfInterestCollection().size(); i++)
	                {
	    	            //start a tag called "root"
	    	            serializer.startTag(null, "PointOfInterest");
	    	
		    	            //i indent code just to have a view similar to xml-tree
		    	            serializer.startTag(null, "name");
		    	            	serializer.text(PointOfInterest.getPointOfInterestCollection().get(i).getName());
		    	            serializer.endTag(null, "name");
		    	            
	//	    	            serializer.startTag(null, "location");
		    	            	serializer.startTag(null, "latitude");
		    	            		serializer.text(String.valueOf(PointOfInterest.getPointOfInterestCollection().get(i).getLocation().getLatitude()));
		    	            	serializer.endTag(null, "latitude");
		    	            	serializer.startTag(null, "longitude");
		                			serializer.text(String.valueOf(PointOfInterest.getPointOfInterestCollection().get(i).getLocation().getLongitude()));
		                		serializer.endTag(null, "longitude");
		    	            //set an attribute called "attribute" with a "value" for <child2>
	//	    	            serializer.attribute(null, "attribute", "value");
	//	    	            serializer.endTag(null, "location");
		    	
		    	            serializer.startTag(null, "radius");
		    	            	serializer.text(String.valueOf(PointOfInterest.getPointOfInterestCollection().get(i).getRadius()));
		    	            serializer.endTag(null, "radius");
	    	
	    	            serializer.endTag(null, "PointOfInterest");
	                }	                
		            serializer.endTag(null, "PointOfInterestCollection");
		            
		            
		            serializer.startTag(null, "ProfileCollection");
	                for(int i=0; i<Profile.getProfileCollection().size(); i++)
	                {
	    	            serializer.startTag(null, "Profile");
	    	            
		    	            serializer.startTag(null, "name");
		    	            	serializer.text(Profile.getProfileCollection().get(i).getName());
		    	            serializer.endTag(null, "name");
		    	            
	    	            	serializer.startTag(null, "changeSoundMode");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getChangeSoundMode()));
	    	            	serializer.endTag(null, "changeSoundMode");//		    	            
	    	            	serializer.startTag(null, "soundMode");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getSoundMode()));
	    	            	serializer.endTag(null, "soundMode");
	    	            
	    	            	serializer.startTag(null, "changeVolumeMusicVideoGameMedia");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getChangeVolumeMusicVideoGameMedia()));
	    	            	serializer.endTag(null, "changeVolumeMusicVideoGameMedia");//		    	            
	    	            	serializer.startTag(null, "volumeMusic");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getVolumeMusic()));
	    	            	serializer.endTag(null, "volumeMusic");
    	            
	    	            	serializer.startTag(null, "changeVolumeNotifications");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getChangeVolumeNotifications()));
	    	            	serializer.endTag(null, "changeVolumeNotifications");//		    	            
	    	            	serializer.startTag(null, "volumeNotifications");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getVolumeNotifications()));
	    	            	serializer.endTag(null, "volumeNotifications");
	            
	    	            	serializer.startTag(null, "changeVolumeAlarms");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getChangeVolumeAlarms()));
	    	            	serializer.endTag(null, "changeVolumeAlarms");//		    	            
	    	            	serializer.startTag(null, "volumeAlarms");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getVolumeAlarms()));
	    	            	serializer.endTag(null, "volumeAlarms");
    	            
	    	            	serializer.startTag(null, "changeIncomingCallsRingtone");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getChangeIncomingCallsRingtone()));
	    	            	serializer.endTag(null, "changeIncomingCallsRingtone");//		    	            
	    	            	serializer.startTag(null, "incomingCallsRingtone");
	    	            		File incomingFile = Profile.getProfileCollection().get(i).getIncomingCallsRingtone();
	    	            		if(incomingFile != null)
	    	            			serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getIncomingCallsRingtone().getPath()));
	    	            		else
	    	            			serializer.text("null");
	    	            	serializer.endTag(null, "incomingCallsRingtone");
	    	            
	    	            	serializer.startTag(null, "changeVibrateWhenRinging");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getChangeVibrateWhenRinging()));
	    	            	serializer.endTag(null, "changeVibrateWhenRinging");//		    	            
	    	            	serializer.startTag(null, "changeVibrateWhenRinging");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getVibrateWhenRinging()));
	    	            	serializer.endTag(null, "changeVibrateWhenRinging");
	    	            
	    	            	serializer.startTag(null, "changeNotificationRingtone");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getChangeNotificationRingtone()));
	    	            	serializer.endTag(null, "changeNotificationRingtone");//		    	            
	    	            	serializer.startTag(null, "notificationRingtone");
	    	            		File notificationFile = Profile.getProfileCollection().get(i).getNotificationRingtone();
	    	            		if(notificationFile != null)
	    	            			serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getNotificationRingtone().getPath()));
	    	            		else
	    	            			serializer.text("null");
	    	            	serializer.endTag(null, "notificationRingtone");
	    	            
	    	            	serializer.startTag(null, "changeAudibleSelection");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getChangeAudibleSelection()));
	    	            	serializer.endTag(null, "changeAudibleSelection");//		    	            
	    	            	serializer.startTag(null, "audibleSelection");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getAudibleSelection()));
	    	            	serializer.endTag(null, "audibleSelection");
  	            
	    	            	serializer.startTag(null, "changeScreenLockUnlockSound");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getChangeScreenLockUnlockSound()));
	    	            	serializer.endTag(null, "changeScreenLockUnlockSound");//		    	            
	    	            	serializer.startTag(null, "screenLockUnlockSound");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getScreenLockUnlockSound()));
	    	            	serializer.endTag(null, "screenLockUnlockSound");
    	            
	    	            	serializer.startTag(null, "changeHapticFeedback");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getChangeHapticFeedback()));
	    	            	serializer.endTag(null, "changeHapticFeedback");//		    	            
	    	            	serializer.startTag(null, "hapticFeedback");
	    	            		serializer.text(String.valueOf(Profile.getProfileCollection().get(i).getHapticFeedback()));
	    	            	serializer.endTag(null, "hapticFeedback"); 	            
	
	    	            serializer.endTag(null, "Profile");
	                }	                
		            serializer.endTag(null, "ProfileCollection");
		            
		            
		            
		            serializer.startTag(null, "RuleCollection");
	                for(int i=0; i<Rule.getRuleCollection().size(); i++)
	                {
	    	            serializer.startTag(null, "Rule");
	    	
		    	            serializer.startTag(null, "Name");
		    	            	serializer.text(Rule.getRuleCollection().get(i).getName());
		    	            serializer.endTag(null, "Name");
	    	
		    	            serializer.startTag(null, "RuleActive");
		    	            	serializer.text(String.valueOf(Rule.getRuleCollection().get(i).isRuleActive()));
		    	            serializer.endTag(null, "RuleActive");
	    	
		    	            serializer.startTag(null, "RuleToggle");
		    	            	serializer.text(String.valueOf(Rule.getRuleCollection().get(i).isRuleToggle()));
		    	            serializer.endTag(null, "RuleToggle");
		    	            
		    	            serializer.startTag(null, "TriggerCollection");
		    	            for(int j=0; j<Rule.getRuleCollection().get(i).getTriggerSet().size(); j++)
		    	            {		    	            
		    	            	serializer.startTag(null, "Trigger");
		    	            		serializer.startTag(null, "TriggerEvent");
		    	            			serializer.text(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType().toString());
		    	            		serializer.endTag(null, "TriggerEvent");
		    	            		serializer.startTag(null, "TriggerParameter1");
		    	            			serializer.text(String.valueOf(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerParameter()));
		    	            		serializer.endTag(null, "TriggerParameter1");
		    	            		serializer.startTag(null, "TriggerParameter2");
		    	            			if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType() == Trigger_Enum.pointOfInterest)
		    	            			{
		    	            				PointOfInterest poiToWriteInRule = Rule.getRuleCollection().get(i).getTriggerSet().get(j).getPointOfInterest();
		    	            				if(poiToWriteInRule != null)
		    	            					serializer.text(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getPointOfInterest().getName());
		    	            				else
		    	            					serializer.text("null");
		    	            			}
		    	            			else if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType() == Trigger_Enum.timeFrame)
		    	            				serializer.text(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTimeFrame().toString());
		    	            			else if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType() == Trigger_Enum.speed)
		    	            				serializer.text(String.valueOf(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getSpeed()));
		    	            			else if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType() == Trigger_Enum.noiseLevel)
		    	            				serializer.text(String.valueOf(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getNoiseLevelDb()));
		    	            			else if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType() == Trigger_Enum.wifiConnection)
		    	            				serializer.text(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerParameter2());
		    	            			else if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType() == Trigger_Enum.process_started_stopped)
		    	            				serializer.text(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getProcessName());
		    	            			else if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType() == Trigger_Enum.batteryLevel)
		    	            				serializer.text(String.valueOf(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getBatteryLevel()));
//		    	            			else if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType() == Trigger_Enum.phoneCall)
//		    	            				serializer.text(String.valueOf(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getPhoneDirection()) + "," + String.valueOf(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getPhoneNumber()));
		    	            			else if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType() == Trigger_Enum.nfcTag)
		    	            				serializer.text(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getNfcTagId());
		    	            			else if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType() == Trigger_Enum.activityDetection)
		    	            				serializer.text(String.valueOf(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getActivityDetectionType()));
		    	            			else if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType() == Trigger_Enum.bluetoothConnection)
		    	            			{
		    	            				if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getBluetoothEvent() != null && Rule.getRuleCollection().get(i).getTriggerSet().get(j).getBluetoothDeviceAddress() != null)
		    	            					serializer.text(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getBluetoothEvent() + ";" + Rule.getRuleCollection().get(i).getTriggerSet().get(j).getBluetoothDeviceAddress());
		    	            			}
		    	            			else if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType() == Trigger_Enum.headsetPlugged)
		    	            				serializer.text(String.valueOf(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getHeadphoneType()));
										else if(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerType() == Trigger_Enum.notification)
											serializer.text(String.valueOf(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerParameter2()));
										else
											serializer.text(String.valueOf(Rule.getRuleCollection().get(i).getTriggerSet().get(j).getTriggerParameter2()));
		    	            		serializer.endTag(null, "TriggerParameter2");
		    	            	serializer.endTag(null, "Trigger");
		    	            }
		    	            serializer.endTag(null, "TriggerCollection");
		    	            
		    	            serializer.startTag(null, "ActionCollection");
		    	            for(int j=0; j<Rule.getRuleCollection().get(i).getActionSet().size(); j++)
		    	            {		    	            
		    	            	serializer.startTag(null, "Action");
		    	            		serializer.startTag(null, "ActionName");
		    	            			serializer.text(Rule.getRuleCollection().get(i).getActionSet().get(j).getAction().toString());
		    	            		serializer.endTag(null, "ActionName");
		    	            		serializer.startTag(null, "ActionParameter1");
		    	            			serializer.text(String.valueOf(Rule.getRuleCollection().get(i).getActionSet().get(j).getParameter1()));
		    	            		serializer.endTag(null, "ActionParameter1");
		    	            		serializer.startTag(null, "ActionParameter2");
		    	            			// Possibly encrypt this part because of credentials
		    	            			if(Rule.getRuleCollection().get(i).getActionSet().get(j).getAction().equals(Action_Enum.triggerUrl))
		    	            			{
		    	            				String encrypted = AESCrypt.encrypt(encryptionKey, String.valueOf(Rule.getRuleCollection().get(i).getActionSet().get(j).getParameter2()));
		    	            				serializer.text(encrypted);
		    	            			}
		    	            			else
		    	            				serializer.text(String.valueOf(Rule.getRuleCollection().get(i).getActionSet().get(j).getParameter2()));
		    	            		serializer.endTag(null, "ActionParameter2");
		    	            	serializer.endTag(null, "Action");
		    	            }
		    	            serializer.endTag(null, "ActionCollection");
	    	
	    	            serializer.endTag(null, "Rule");
	                }	                
		            serializer.endTag(null, "RuleCollection");
	            
	            serializer.endTag(null, "Automation");

                serializer.endDocument();
                
                //write xml data into the FileOutputStream
                serializer.flush();

                //finally we close the file stream
                fileos.close();

//                TextView tv = (TextView)this.findViewById(R.id.result);
//                tv.setText("file has been created on SD card");
                Miscellaneous.logEvent("i", "Writer", "File has been created on SD card", 3);
                
                Collections.sort(Rule.getRuleCollection());

            	Miscellaneous.logEvent("i", "XML", "Writing of temporary file: " + temporaryWriteFile.getAbsolutePath() + " successfull.", 4);
                if(temporaryWriteFile.renameTo(settingsFile))
                {
                	Miscellaneous.logEvent("i", "XML", "Renaming of temporary file " + temporaryWriteFile.getAbsolutePath() + " to " + settingsFile.getAbsolutePath() + " successfull.", 4);
                	return true;
                }
                else
                	Miscellaneous.logEvent("i", "XML", "Renaming of temporary file " + temporaryWriteFile.getAbsolutePath() + " to " + settingsFile.getAbsolutePath() + " failed.", 4);
            }
            catch (Exception e)
            {
                Miscellaneous.logEvent("e", "Exception", "Error occurred while writing xml file: " + Log.getStackTraceString(e), 1);
            }
        }
        catch(FileNotFoundException e)
        {
            Miscellaneous.logEvent("e", "FileNotFoundException", "Can't create FileOutputStream", 1);
        }
        
		return false;

    }

    public static void readFile() throws FileNotFoundException
    {
    	/*
    		Storage location has been moved to app-specific folder in Android/data
    		Hence this permission is not requested any more. If it is already granted we assume the files are on /sdcard or similar.
    		Migration to app-specific folder has yet to be implemented.
    	 */
//		if(!ActivityPermissions.havePermission(ActivityPermissions.writeExternalStoragePermissionName, Miscellaneous.getAnyContext()))
//		{
//			/*
//				Don't have permission to access external storage. This is a show stopper as
//				the configuration file is stored on external storage.
//			 */
//			Miscellaneous.logEvent("e", "Permission", "Don't have permission to access external storage. Will request it now.", 4);
//			Toast.makeText(Miscellaneous.getAnyContext(), Miscellaneous.getAnyContext().getResources().getString(R.string.appRequiresPermissiontoAccessExternalStorage), Toast.LENGTH_LONG).show();
//			ActivityPermissions.requestSpecificPermission(ActivityPermissions.writeExternalStoragePermissionName);
//			return;
//		}

		/*
			If we are here it may be that we just got permission to read storage. We need to check for the
			config file path again.
		 */
		if(settingsFile.getPath().startsWith("null"))
			settingsFile = new File(Miscellaneous.getWriteableFolder() + "/Automation_settings.xml");

    	FileInputStream stream = new FileInputStream(settingsFile);
    	
		try
		{
			parseSettingsFile(stream);
		}
		catch (XmlPullParserException e)
		{
			Miscellaneous.logEvent("e", "XmlFileInterface", Log.getStackTraceString(e), 1);
		}
		catch(FileNotFoundException e)
		{
			// Huawei path
			
			settingsFile = new File("/HWUserData/" + Settings.folderName + "/Automation_settings.xml");
			stream = new FileInputStream(settingsFile);
	    	
			try
			{
				parseSettingsFile(stream);
			}
			catch(Exception ex)
			{
				Miscellaneous.logEvent("e", "XmlFileInterface", Log.getStackTraceString(e), 1);
			}
		}
		catch (IOException e)
		{
			Miscellaneous.logEvent("e", "XmlFileInterface", Log.getStackTraceString(e), 1);
		}
		catch(Exception e)
		{
			Miscellaneous.logEvent("e", "XmlFileInterface", "Error reading file: " + Log.getStackTraceString(e), 2);
		}
    }
    
    public static ArrayList<Rule> ruleCollection = new ArrayList<Rule>();
    public static void parseSettingsFile(InputStream in) throws XmlPullParserException, IOException
    {
        try
        {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            XmlFileInterface.readFile3(parser);
        }
        finally
        {
            in.close();
        }
    }

	protected static String ns="";
    private static void readFile3(XmlPullParser parser) throws XmlPullParserException, IOException
    {
    	Miscellaneous.logEvent("i", "File", "Reading settings file", 4);
    	parser.require(XmlPullParser.START_TAG, ns, "Automation");
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("PointOfInterestCollection"))
            {
            	PointOfInterest.getPointOfInterestCollection().clear();
            	readPoiCollection(parser);
            }
            else if (name.equals("ProfileCollection"))
            {
            	Profile.getProfileCollection().clear();
            	readProfileCollection(parser);
            }
            else if (name.equals("RuleCollection"))
            {
            	XmlFileInterface.ruleCollection.clear();
            	readRuleCollection(parser);
            }
            else
            {
                skip(parser);
            }
        }  
        Miscellaneous.logEvent("i", "File", "Reading settings file->done", 4);
    }

    private static void readPoiCollection(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, ns, "PointOfInterestCollection");       
        PointOfInterest newPoi = null;
        
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            if (name.equals("PointOfInterest"))
            {
            	newPoi = new PointOfInterest(); 
                newPoi = readPoi(parser);
                PointOfInterest.getPointOfInterestCollection().add(newPoi);
            }
            else
            {
                skip(parser);
            }
        }
        
        Collections.sort(PointOfInterest.getPointOfInterestCollection());
        
        if(newPoi != null)
        	Miscellaneous.logEvent("i", "New POI from file", newPoi.toString(), 5);
        else
        	Miscellaneous.logEvent("i", "File", "No POIs in file.", 4);
    }
    
    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private static PointOfInterest readPoi(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, ns, "PointOfInterest");
        PointOfInterest newPoi = new PointOfInterest();
//        newPoi.parentService = (AutomationService)context;
        newPoi.setLocation(new Location("POINT_LOCATION"));
        
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name"))
            {
                newPoi.setName(readTag(parser, "name"));
                
                // This checks if the last activated POI 
                if(Settings.rememberLastActivePoi)
	                if(newPoi.getName().equals(Settings.lastActivePoi))
	                	newPoi.setActivated(true);
            }
            else if (name.equals("latitude"))
            {
                newPoi.getLocation().setLatitude(Double.valueOf(readTag(parser, "latitude")));
            }
            else if (name.equals("longitude"))
            {
            	newPoi.getLocation().setLongitude(Double.valueOf(readTag(parser, "longitude")));
            }
            else if (name.equals("radius"))
            {
            	try
				{
					newPoi.setRadius(Double.valueOf(readTag(parser, "radius")), context);
				}
            	catch (NumberFormatException e)
				{
					Miscellaneous.logEvent("e", "XmlFileInterface", Log.getStackTraceString(e), 1);
				}
            	catch (Exception e)
				{
					Miscellaneous.logEvent("e", "XmlFileInterface", Log.getStackTraceString(e), 1);
				}
            }
            else
            {
                skip(parser);
            }            
        }
        
        Miscellaneous.logEvent("i", "New POI from file", newPoi.toStringLong(), 5);
        
        return newPoi;
    }

    private static void readProfileCollection(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, ns, "ProfileCollection");       
        Profile newProfile = null;
        
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Profile"))
            {
            	newProfile = new Profile(); 
            	newProfile = readProfile(parser);
                Profile.getProfileCollection().add(newProfile);
            }
            else
            {
                skip(parser);
            }
        }
        
        Collections.sort(Profile.getProfileCollection());
        
        if(newProfile != null)
        	Miscellaneous.logEvent("i", "New Profile from file", newProfile.toString(), 5);
        else
        	Miscellaneous.logEvent("i", "File", "No Profiles in file.", 4);
    }
    
    private static Profile readProfile(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, ns, "Profile");
        Profile newProfile = new Profile();
        
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            
            if (name.equals("name"))
                newProfile.setName(readTag(parser, "name"));
            else if (name.equals("changeSoundMode"))
                newProfile.setChangeSoundMode(Boolean.parseBoolean(readTag(parser, "changeSoundMode")));
            else if (name.equals("soundMode"))
                newProfile.setSoundMode(Integer.parseInt(readTag(parser, "soundMode")));
            else if (name.equals("changeVolumeMusicVideoGameMedia"))
                newProfile.setChangeVolumeMusicVideoGameMedia(Boolean.parseBoolean(readTag(parser, "changeVolumeMusicVideoGameMedia")));
            else if (name.equals("volumeMusic"))
                newProfile.setVolumeMusic(Integer.parseInt(readTag(parser, "volumeMusic")));
            else if (name.equals("changeVolumeNotifications"))
                newProfile.setChangeVolumeNotifications(Boolean.parseBoolean(readTag(parser, "changeVolumeNotifications")));
            else if (name.equals("volumeNotifications"))
                newProfile.setVolumeNotifications(Integer.parseInt(readTag(parser, "volumeNotifications")));
            else if (name.equals("changeVolumeAlarms"))
                newProfile.setChangeVolumeAlarms(Boolean.parseBoolean(readTag(parser, "changeVolumeAlarms")));
            else if (name.equals("volumeAlarms"))
                newProfile.setVolumeAlarms(Integer.parseInt(readTag(parser, "volumeAlarms")));
            else if (name.equals("changeIncomingCallsRingtone"))
                newProfile.setChangeIncomingCallsRingtone(Boolean.parseBoolean(readTag(parser, "changeIncomingCallsRingtone")));
            else if (name.equals("incomingCallsRingtone"))
            {
            	String path = readTag(parser, "incomingCallsRingtone");
            	if(!path.equals("null"))
            		newProfile.setIncomingCallsRingtone(new File(path));
            	else
            		newProfile.setIncomingCallsRingtone(null);
            }
            else if (name.equals("changeVibrateWhenRinging"))
                newProfile.setChangeVibrateWhenRinging(Boolean.parseBoolean(readTag(parser, "changeVibrateWhenRinging")));
            else if (name.equals("changeNotificationRingtone"))
                newProfile.setChangeNotificationRingtone(Boolean.parseBoolean(readTag(parser, "changeNotificationRingtone")));
            else if (name.equals("notificationRingtone"))
            {
            	String path = readTag(parser, "notificationRingtone");
            	if(!path.equals("null"))
            		newProfile.setNotificationRingtone(new File(path));
            	else
            		newProfile.setNotificationRingtone(null);
            }
            else if (name.equals("changeAudibleSelection"))
                newProfile.setChangeAudibleSelection(Boolean.parseBoolean(readTag(parser, "changeAudibleSelection")));
            else if (name.equals("audibleSelection"))
                newProfile.setAudibleSelection(Boolean.parseBoolean(readTag(parser, "audibleSelection")));
            else if (name.equals("changeScreenLockUnlockSound"))
                newProfile.setChangeScreenLockUnlockSound(Boolean.parseBoolean(readTag(parser, "changeScreenLockUnlockSound")));
            else if (name.equals("screenLockUnlockSound"))
                newProfile.setScreenLockUnlockSound(Boolean.parseBoolean(readTag(parser, "screenLockUnlockSound")));
            else if (name.equals("changeHapticFeedback"))
                newProfile.setChangeHapticFeedback(Boolean.parseBoolean(readTag(parser, "changeHapticFeedback")));
            else if (name.equals("hapticFeedback"))
                newProfile.setHapticFeedback(Boolean.parseBoolean(readTag(parser, "hapticFeedback")));
            else
                skip(parser);
            
        }
        
        Miscellaneous.logEvent("i", "New Profile from file", newProfile.toStringLong(), 5);
        
        return newProfile;
    }
    
    private static void readRuleCollection(XmlPullParser parser) throws XmlPullParserException, IOException
	{
        Miscellaneous.logEvent("i", "File", "Reading Rule Collection from file", 5);

        parser.require(XmlPullParser.START_TAG, ns, "RuleCollection");
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Rule"))
            {
            	Rule newRule = readRule(parser);
            	XmlFileInterface.ruleCollection.add(newRule);
            }
            else
            {
                skip(parser);
            }
        }  
        Collections.sort(XmlFileInterface.ruleCollection);
        Miscellaneous.logEvent("i", "File", "Reading Rule Collection from file->done", 5);
	}

	private static Rule readRule(XmlPullParser parser) throws XmlPullParserException, IOException
	{
    	/* FILE EXAMPE:
    	 * *****************
		 * <Automation>
			 * <PointOfInterestCollection>
				 * <PointOfInterest>
				       	<name>someName</name>
			        	<latitude>someLatitude</latitude>
			        	<longitude>someLongitude</longitude>
				        <radius>someRadius</radius>
			     * </PointOfInterest>
			 * </PointOfInterestCollection>
			 * <RuleCollection>
			 * 		<Rule>
			 * 			<Name>String</Name>
			 * 			<RuleActive>true/false</RuleActive>
			 * 			<TriggerCollection>
			 * 				<Trigger>
				 * 				<TriggerEvent>String: pointOfInterest, timeFrame, charging, usb_connection</TriggerEvent>
				 * 				<TriggerParameter1>true/false</TriggerParameter1>
				 * 				<TriggerParameter2>POI-Name, TimeFrame, USB-Device-Name</TriggerParameter2>
			 * 				</Trigger>
			 * 			</TriggerCollection>
			 * 			<ActionCollection>
			 * 				<Action>
				 * 				<ActionName>String</ActionName>
				 * 				<ActionParameter>String</ActionParameter>
			 * 				</Action>
			 * 			</ActionCollection>
			 * 		</Rule>
			 * </RuleCollection>
		 * </Automation>
    	*/
		
		parser.require(XmlPullParser.START_TAG, ns, "Rule");
        Rule newRule = new Rule();
        
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Name"))
            {
            	newRule.setName(readTag(parser, "Name"));
            }
            else if(name.equals("RuleActive"))
            {
            	newRule.setRuleActive(Boolean.valueOf(readTag(parser, "RuleActive")));
            }
            else if(name.equals("RuleToggle"))
            {
            	newRule.setRuleToggle(Boolean.valueOf(readTag(parser, "RuleToggle")));
            }
            else if (name.equals("TriggerCollection"))
            {
            	try
				{
					newRule.setTriggerSet(readTriggerCollection(parser));
				}
            	catch (XmlPullParserException e)
				{
					Miscellaneous.logEvent("e", "XmlFileInterface", Log.getStackTraceString(e), 1);
				}
            	catch (IOException e)
				{
					Miscellaneous.logEvent("e", "XmlFileInterface", Log.getStackTraceString(e), 1);
				}
            }
            else if (name.equals("ActionCollection"))
            {
            	try
				{
					newRule.setActionSet(readActionCollection(parser));
				}
            	catch (XmlPullParserException e)
				{
					Miscellaneous.logEvent("e", "XmlFileInterface", Log.getStackTraceString(e), 1);
				}
            	catch (IOException e)
				{
					Miscellaneous.logEvent("e", "XmlFileInterface", Log.getStackTraceString(e), 1);
				}
            }
            else
            {
                skip(parser);
            }            
        }
        
        Miscellaneous.logEvent("i", "New Rule from file", newRule.toString(), 5);
        
        return newRule;
	}

    private static ArrayList<Trigger> readTriggerCollection(XmlPullParser parser) throws XmlPullParserException, IOException
	{
    	ArrayList<Trigger> triggerCollection = new ArrayList<Trigger>();

        parser.require(XmlPullParser.START_TAG, ns, "TriggerCollection");
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Trigger"))
            {
            	triggerCollection.add(readTrigger(parser));
            }
            else
            {
                skip(parser);
            }
        }
        
        return (triggerCollection);
	}

	
	private static Trigger readTrigger(XmlPullParser parser) throws IOException, XmlPullParserException
	{
		
    	/* FILE EXAMPE:
    	 * *****************
		 * <Automation>
			 * <PointOfInterestCollection>
				 * <PointOfInterest>
				       	<name>someName</name>
			        	<latitude>someLatitude</latitude>
			        	<longitude>someLongitude</longitude>
				        <radius>someRadius</radius>
			     * </PointOfInterest>
			 * </PointOfInterestCollection>
			 * <RuleCollection>
			 * 		<Rule>
			 * 			<Name>String</Name>
			 * 			<RuleActive>true/false</RuleActive>
			 * 			<TriggerCollection>
			 * 				<Trigger>
				 * 				<TriggerEvent>String: pointOfInterest, timeFrame, charging, usb_connection</TriggerEvent>
				 * 				<TriggerParameter1>true/false</TriggerParameter1>
				 * 				<TriggerParameter2>POI-Name, TimeFrame, USB-Device-Name, Speed</TriggerParameter2>
			 * 				</Trigger>
			 * 			</TriggerCollection>
			 * 			<ActionCollection>
			 * 				<Action>
				 * 				<ActionName>String</ActionName>
				 * 				<ActionParameter>String</ActionParameter>
			 * 				</Action>
			 * 			</ActionCollection>
			 * 		</Rule>
			 * </RuleCollection>
		 * </Automation>
    	*/
		
		parser.require(XmlPullParser.START_TAG, ns, "Trigger");
        Trigger newTrigger = new Trigger();
        
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            
            if (name.equals("TriggerEvent"))
            {
            	String triggerEventString = readTag(parser, "TriggerEvent");

				if(triggerEventString.equals("process_started_stopped") | triggerEventString.equals("process_running"))
            		newTrigger.setTriggerType(Trigger_Enum.process_started_stopped);
				else
					newTrigger.setTriggerType(Trigger_Enum.valueOf(triggerEventString));
            }
            else if (name.equals("TriggerParameter1"))
            {
            	newTrigger.setTriggerParameter(Boolean.valueOf(readTag(parser, "TriggerParameter1")));
            }
            else if (name.equals("TriggerParameter2"))
            {
            	String triggerParameter2 = readTag(parser, "TriggerParameter2");
            	if(newTrigger.getTriggerType() == Trigger_Enum.pointOfInterest)
            	{
					try
					{
						if(triggerParameter2.equals("null"))
							newTrigger.setPointOfInterest(null);
						else
							newTrigger.setPointOfInterest(PointOfInterest.getByName(triggerParameter2));
					}
					catch (Exception e)
					{
						Miscellaneous.logEvent("e", "XmlFileInterface", Log.getStackTraceString(e), 2);
						Toast.makeText(context, "Error while writing file: " + Log.getStackTraceString(e), Toast.LENGTH_LONG).show();
					}
					newTrigger.setTriggerParameter2(triggerParameter2);
            	}
            	else if(newTrigger.getTriggerType() == Trigger_Enum.timeFrame)
            	{
            		newTrigger.setTimeFrame(new TimeFrame(triggerParameter2));
					newTrigger.setTriggerParameter2(triggerParameter2);
            	}
            	else if(newTrigger.getTriggerType() == Trigger_Enum.batteryLevel)
            	{
            		newTrigger.setBatteryLevel(Integer.parseInt(triggerParameter2));
					newTrigger.setTriggerParameter2(triggerParameter2);
            	}
            	else if(newTrigger.getTriggerType() == Trigger_Enum.speed)
            	{
            		newTrigger.setSpeed(Double.parseDouble(triggerParameter2));
					newTrigger.setTriggerParameter2(triggerParameter2);
            	}
            	else if(newTrigger.getTriggerType() == Trigger_Enum.noiseLevel)
            	{
            		newTrigger.setNoiseLevelDb(Long.parseLong(triggerParameter2));
					newTrigger.setTriggerParameter2(triggerParameter2);
            	}
            	else if(newTrigger.getTriggerType() == Trigger_Enum.wifiConnection)
            	{
//            		newTrigger.setWifiName(triggerParameter2);
					newTrigger.setTriggerParameter2(triggerParameter2);
            	}
            	else if(newTrigger.getTriggerType() == Trigger_Enum.process_started_stopped)
            	{
            		newTrigger.setProcessName(triggerParameter2);
					newTrigger.setTriggerParameter2(triggerParameter2);
            	}
            	else if(newTrigger.getTriggerType() == Trigger_Enum.phoneCall)
            	{
            		String[] elements = triggerParameter2.split(",");
            		if(elements.length == 2)	//old format
					{
						// 0/1/2,number
						int direction = Integer.parseInt(elements[0]);

						String number = elements[1];
						newTrigger.setPhoneDirection(direction);
						newTrigger.setPhoneNumber(number);

						String tp2String = "";

						if(newTrigger.getTriggerParameter())
							tp2String+= Trigger.triggerPhoneCallStateStarted;
						else
							tp2String+= Trigger.triggerPhoneCallStateStopped;

						tp2String += triggerParameter2Split;

						switch(direction)
						{
							case 0:
								tp2String += Trigger.triggerPhoneCallDirectionAny;
								break;
							case 1:
								tp2String += Trigger.triggerPhoneCallDirectionIncoming;
								break;
							case 2:
								tp2String += Trigger.triggerPhoneCallDirectionOutgoing;
								break;
						}

						tp2String += triggerParameter2Split;

						tp2String += number;

						newTrigger.setTriggerParameter2(tp2String);
					}
            		/*else		// new format
					{
						//tp1 is now irrelevant
						elements = triggerParameter2.split(Trigger.triggerParameter2Split);
						// state/direction/number
					}*/
					else
						newTrigger.setTriggerParameter2(triggerParameter2);
            	}
	        	else if(newTrigger.getTriggerType() == Trigger_Enum.nfcTag)
	        	{
	        		newTrigger.setNfcTagId(triggerParameter2);
					newTrigger.setTriggerParameter2(triggerParameter2);
	        	}
	        	else if(newTrigger.getTriggerType() == Trigger_Enum.activityDetection)
	        	{
	        		try
	        		{
	        			newTrigger.setActivityDetectionType(Integer.parseInt(triggerParameter2));
	        		}
	        		catch(NumberFormatException e)
	        		{
	        			newTrigger.setActivityDetectionType(0);
	        		}
					newTrigger.setTriggerParameter2(triggerParameter2);
	        	}
            	else if(newTrigger.getTriggerType() == Trigger_Enum.bluetoothConnection)
            	{
            		if(triggerParameter2.contains(";"))
            		{
            			String[] substrings = triggerParameter2.split(";");
            			newTrigger.setBluetoothEvent(substrings[0]);
            			newTrigger.setBluetoothDeviceAddress(substrings[1]);
            		}
					newTrigger.setTriggerParameter2(triggerParameter2);
            	}
	        	else if(newTrigger.getTriggerType() == Trigger_Enum.headsetPlugged)
	        	{
	        		try
	        		{
	        			newTrigger.setHeadphoneType(Integer.parseInt(triggerParameter2));
	        		}
	        		catch(NumberFormatException e)
	        		{
	        			newTrigger.setHeadphoneType(-1);
	        		}
					newTrigger.setTriggerParameter2(triggerParameter2);
	        	}
				else
					newTrigger.setTriggerParameter2(triggerParameter2);
            }
            else
            {
                skip(parser);
            }
        }
        
        return newTrigger;
	}
	
	private static ArrayList<Action> readActionCollection(XmlPullParser parser) throws XmlPullParserException, IOException
	{
    	ArrayList<Action> actionCollection = new ArrayList<Action>();

        parser.require(XmlPullParser.START_TAG, ns, "ActionCollection");
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Action"))
            {
            	actionCollection.add(readAction(parser));
            }
            else
            {
                skip(parser);
            }
        }  
        return (actionCollection);
	}

	
	private static Action readAction(XmlPullParser parser) throws IOException, XmlPullParserException
	{		
    	/* FILE EXAMPE:
    	 * *****************
		 * <Automation>
			 * <PointOfInterestCollection>
				 * <PointOfInterest>
				       	<name>someName</name>
			        	<latitude>someLatitude</latitude>
			        	<longitude>someLongitude</longitude>
				        <radius>someRadius</radius>
			     * </PointOfInterest>
			 * </PointOfInterestCollection>
			 * <RuleCollection>
			 * 		<Rule>
			 * 			<Name>String</Name>
			 * 			<RuleActive>true/false</RuleActive>
			 * 			<TriggerCollection>
			 * 				<Trigger>
				 * 				<TriggerEvent>String: pointOfInterest, timeFrame, charging, usb_connection</TriggerEvent>
				 * 				<TriggerParameter1>true/false</TriggerParameter1>
				 * 				<TriggerParameter2>POI-Name, TimeFrame, USB-Device-Name</TriggerParameter2>
			 * 				</Trigger>
			 * 			</TriggerCollection>
			 * 			<ActionCollection>
			 * 				<Action>
				 * 				<ActionName>String</ActionName>
				 * 				<ActionParameter1>String</ActionParameter1>
				 * 				<ActionParameter>String</ActionParameter>
			 * 				</Action>
			 * 			</ActionCollection>
			 * 		</Rule>
			 * </RuleCollection>
		 * </Automation>
    	*/
		
		parser.require(XmlPullParser.START_TAG, ns, "Action");
		Action newAction = new Action();
        
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            
            if (name.equals("ActionName")) // convert legacy stuff to new format
            {				
            	String actionNameString = readTag(parser, "ActionName");
            	
            // *** deprecated
            	//else
            		if(actionNameString.equals("turnWifiOn"))
            		newAction.setAction(Action_Enum.turnWifiOn);
            	else if(actionNameString.equals("turnWifiOff"))
            		newAction.setAction(Action_Enum.turnWifiOff);
            	else if(actionNameString.equals("turnBluetoothOn"))
            		newAction.setAction(Action_Enum.turnBluetoothOn);
            	else if(actionNameString.equals("turnBluetoothOff"))
            		newAction.setAction(Action_Enum.turnBluetoothOff);
            	else if(actionNameString.equals("turnUsbTetheringOn"))
            		newAction.setAction(Action_Enum.turnUsbTetheringOn);
            	else if(actionNameString.equals("turnUsbTetheringOff"))
            		newAction.setAction(Action_Enum.turnUsbTetheringOff);
            	else if(actionNameString.equals("turnWifiTetheringOn"))
            		newAction.setAction(Action_Enum.turnWifiTetheringOn);
            	else if(actionNameString.equals("turnWifiTetheringOff"))
            		newAction.setAction(Action_Enum.turnWifiTetheringOff);
            	else if(actionNameString.equals("enableScreenRotation"))
            		newAction.setAction(Action_Enum.enableScreenRotation);
	        	else if(actionNameString.equals("disableScreenRotation"))
	        		newAction.setAction(Action_Enum.disableScreenRotation);
            // *** deprecated

				else
					newAction.setAction(Action_Enum.valueOf(actionNameString));
            }
            else if (name.equals("ActionParameter1"))
            {     
            	// exclusion for deprecated types
            	if(newAction.getAction().equals(Action_Enum.turnWifiOn))
            	{
            		newAction.setAction(Action_Enum.setWifi);
            		newAction.setParameter1(true);
            		readTag(parser, "ActionParameter1"); //read the tag for the parser to head on
            	}
            	else if(newAction.getAction().equals(Action_Enum.turnWifiOff))
            	{
            		newAction.setAction(Action_Enum.setWifi);
            		newAction.setParameter1(false);
            		readTag(parser, "ActionParameter1"); //read the tag for the parser to head on
            	}
            	else if(newAction.getAction().equals(Action_Enum.turnBluetoothOn))
            	{
            		newAction.setAction(Action_Enum.setBluetooth);
            		newAction.setParameter1(true);
            		readTag(parser, "ActionParameter1"); //read the tag for the parser to head on
            	}
            	else if(newAction.getAction().equals(Action_Enum.turnBluetoothOff))
            	{
            		newAction.setAction(Action_Enum.setBluetooth);
            		newAction.setParameter1(false);
            		readTag(parser, "ActionParameter1"); //read the tag for the parser to head on
            	}
            	else if(newAction.getAction().equals(Action_Enum.turnUsbTetheringOn))
            	{
            		newAction.setAction(Action_Enum.setUsbTethering);
            		newAction.setParameter1(true);
            		readTag(parser, "ActionParameter1"); //read the tag for the parser to head on
            	}
            	else if(newAction.getAction().equals(Action_Enum.turnUsbTetheringOff))
            	{
            		newAction.setAction(Action_Enum.setUsbTethering);
            		newAction.setParameter1(false);
            		readTag(parser, "ActionParameter1"); //read the tag for the parser to head on
            	}
            	else if(newAction.getAction().equals(Action_Enum.turnWifiTetheringOn))
            	{
            		newAction.setAction(Action_Enum.setWifiTethering);
            		newAction.setParameter1(true);
            		readTag(parser, "ActionParameter1"); //read the tag for the parser to head on
            	}
            	else if(newAction.getAction().equals(Action_Enum.turnWifiTetheringOff))
            	{
            		newAction.setAction(Action_Enum.setWifiTethering);
            		newAction.setParameter1(false);
            		readTag(parser, "ActionParameter1"); //read the tag for the parser to head on
            	}
            	else if(newAction.getAction().equals(Action_Enum.enableScreenRotation))
            	{
            		newAction.setAction(Action_Enum.setDisplayRotation);
            		newAction.setParameter1(true);
            		readTag(parser, "ActionParameter1"); //read the tag for the parser to head on
            	}
	        	else if(newAction.getAction().equals(Action_Enum.disableScreenRotation))
            	{
            		newAction.setAction(Action_Enum.setDisplayRotation);
            		newAction.setParameter1(false);
            		readTag(parser, "ActionParameter1"); //read the tag for the parser to head on
            	}
	        	else
	            	// exclusion for deprecated types
	        		newAction.setParameter1(Boolean.parseBoolean(readTag(parser, "ActionParameter1")));
            }
            else if (name.equals("ActionParameter2"))
            {
            	String tag = readTag(parser, "ActionParameter2");
            	if(newAction.getAction().equals(Action_Enum.triggerUrl))	// decrypt url because of credentials
            	{
            		if(tag.toLowerCase().contains("http"))	// not encrypted, yet
            			newAction.setParameter2(tag);
            		else
            		{
            			try
	            		{
	            			newAction.setParameter2(AESCrypt.decrypt(encryptionKey, tag));
	            		}
	            		catch(GeneralSecurityException e)
	            		{
	            			newAction.setParameter2(tag);
	            		}
            		}
            	}
				else if(newAction.getAction().equals(Action_Enum.startOtherActivity))	// separator has been changed, convert in old files
				{
					String newTag;

					if(tag.contains(Action.intentPairSeperator))	// already has new format
						newTag = tag;
					else
						newTag = tag.replace("/", Action.intentPairSeperator);

					String[] newTagPieces = newTag.split(";");

					if(newTagPieces.length < 2 || (!newTagPieces[0].contains(Actions.dummyPackageString) && newTagPieces[1].contains(Action.intentPairSeperator)))
					{
						newTag = Actions.dummyPackageString + ";" + newTag;
						newTagPieces = newTag.split(";");
					}

					if(newTagPieces.length < 3)
						newTag += ";" + ActivityManageActionStartActivity.startByActivityString;
					else if(newTagPieces.length >= 3)
					{
						if(newTagPieces[2].contains(Action.intentPairSeperator))
							newTag = newTagPieces[0] + ";" + newTagPieces[1] + ";" + ActivityManageActionStartActivity.startByActivityString + ";" + newTagPieces[2];
					}

					newAction.setParameter2(newTag);
				}
            	else
            		newAction.setParameter2(tag);
            }
            else if (name.equals("ActionParameter"))	// old version, should be removed eventually
            {
            	newAction.setParameter2(readTag(parser, "ActionParameter"));
            }
            else
            {
                skip(parser);
            }
            
            if(newAction.getAction().equals(Action_Enum.changeSoundProfile))
            {
            	String[] replacements = new String[] { "silent", "vibrate", "normal" };
            	for(String s : replacements)
            	{
            		if(newAction.getParameter2().equals(s) && Profile.getByName(s) == null)	// using an old profile name and there is no backing new profile by the same name
            			Profile.createDummyProfile(context, s);
            	}
            }
        }
        
//        Miscellaneous.logEvent("i", "New Rule from file", newPoi.name + "/" + String.valueOf(newPoi.radius) + "/" + String.valueOf(newPoi.location.getLatitude()) + "/" + String.valueOf(newPoi.location.getLongitude()) + "/" + String.valueOf(newPoi.changeWifiState) + "/" + String.valueOf(newPoi.desiredWifiState) + "/" + String.valueOf(newPoi.changeCameraState) + "/" + String.valueOf(newPoi.desiredCameraState) + "/" + String.valueOf(newPoi.changeSoundSetting) + "/" + String.valueOf(newPoi.desiredSoundSetting));
        
        return newAction;
	}

    // Processes title tags in the feed.
    	private static String readTag(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException
    	{
	      parser.require(XmlPullParser.START_TAG, null, tagName);
	      String title = readText(parser);
	      parser.require(XmlPullParser.END_TAG, null, tagName);
	      return title;
    	}

    // For the tags title and summary, extracts their text values.
	    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException
	    {
	        String result = "";
	        if (parser.next() == XmlPullParser.TEXT)
	        {
	            result = parser.getText();
	            parser.nextTag();
	        }
	        return result;
	    }

		private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException
		{
		    if (parser.getEventType() != XmlPullParser.START_TAG)
		    {
		        throw new IllegalStateException();
		    }
		    int depth = 1;
		    while (depth != 0)
		    {
		        switch (parser.next())
		        {
			        case XmlPullParser.END_TAG:
			            depth--;
			            break;
			        case XmlPullParser.START_TAG:
			            depth++;
			            break;
		        }
		    }
		 }
		
		public static boolean migrateFilesFromRootToFolder(String oldPath, String newPath)
		{			
			File oldDir = new File(oldPath);
			if(oldDir.isDirectory())
			{
				File newDir = new File(newPath);
				
				File[] files = oldDir.listFiles();
				for(File file : files)
				{
					if(file.getName().startsWith("Automation") && file.isFile())
						file.renameTo(new File(newDir, file.getName()));
				}
				
				return true;
			}
			
			return false;
		}
}