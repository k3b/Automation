package com.jens.automation2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class ActivityControlCenter extends Activity
{
    final static int requestCodeImport = 1001;
    final static int requestCodeExport = 1002;
    final static int requestCodeMoreSettings = 6000;

    final static String prefsFileName = "com.jens.automation2_preferences.xml";

    TextView tvFileStoreLocation, tvAppVersion;
    Button bVolumeTest, bMoreSettings, bSettingsSetToDefault, bSendEmailToDev, bImportConfiguration, bExportConfiguration;
    CheckBox chkShareConfigAndLog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_center);

        bVolumeTest = (Button) findViewById(R.id.bVolumeTest);
        bVolumeTest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ActivityControlCenter.this, ActivityVolumeTest.class);
                startActivity(intent);
            }
        });

        chkShareConfigAndLog = (CheckBox)findViewById(R.id.chkShareConfigAndLog);
        bSendEmailToDev = (Button) findViewById(R.id.bSendEmailToDev);
        bSendEmailToDev.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(chkShareConfigAndLog.isChecked())
                    getShareConfigAndLogDialogue(ActivityControlCenter.this).show();
                else
                {
                    String subject = "Automation";
                    Miscellaneous.sendEmail(ActivityControlCenter.this, "android-development@gmx.de", "Automation logs", getSystemInfo(), null);
                }
            }
        });

        bSettingsSetToDefault = (Button) findViewById(R.id.bSettingsSetToDefault);
        bSettingsSetToDefault.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getDefaultSettingsDialog(ActivityControlCenter.this).show();
            }
        });

        bMoreSettings = (Button) findViewById(R.id.bMoreSettings);
        bMoreSettings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent myIntent = new Intent(ActivityControlCenter.this, ActivitySettings.class);
                startActivityForResult(myIntent, requestCodeMoreSettings);
            }
        });

        bImportConfiguration = (Button) findViewById(R.id.bImportConfiguration);
        bImportConfiguration.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, requestCodeImport);
            }
        });

        bExportConfiguration = (Button) findViewById(R.id.bExportConfiguration);
        bExportConfiguration.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, requestCodeExport);
            }
        });

        tvFileStoreLocation = (TextView)findViewById(R.id.tvFileStoreLocation);
        tvAppVersion = (TextView)findViewById(R.id.tvAppVersion);

        tvAppVersion.setText(
                                "Version: " + BuildConfig.VERSION_NAME + Miscellaneous.lineSeparator +
                                "Version code: " + String.valueOf(BuildConfig.VERSION_CODE) + Miscellaneous.lineSeparator +
                                "Flavor: " + BuildConfig.FLAVOR
                            );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case requestCodeMoreSettings: //settings
                Settings.readFromPersistentStorage(this);

                if (AutomationService.isMyServiceRunning(this))
                    AutomationService.getInstance().serviceInterface(AutomationService.serviceCommands.reloadSettings);

                if (AutomationService.isMyServiceRunning(ActivityControlCenter.this))
                    Toast.makeText(this, getResources().getString(R.string.settingsWillTakeTime), Toast.LENGTH_LONG).show();

                break;
            case requestCodeImport:
                if(resultCode == RESULT_OK)
                {
                    Uri uriTree = data.getData();
                    importFiles(uriTree);
                }
                break;
            case requestCodeExport:
                if(resultCode == RESULT_OK)
                {
                    Uri uriTree = data.getData();
                    exportFiles(uriTree);
                }
                break;
        }
    }

    void importFiles(Uri uriTree)
    {
//        https://stackoverflow.com/questions/46237558/android-strange-behavior-of-documentfile-inputstream

        File dstRules = new File(Miscellaneous.getWriteableFolder() + "/" + XmlFileInterface.settingsFileName);
        File dstPrefs = new File(Miscellaneous.getWriteableFolder() + "/../shared_prefs/" + prefsFileName);

        DocumentFile directory = DocumentFile.fromTreeUri(this, uriTree);

        int applicableFilesFound = 0;
        int filesImported = 0;

        if(directory.listFiles().length > 0)
        {
            for (DocumentFile file : directory.listFiles())
            {
                if (file.getName().equals(XmlFileInterface.settingsFileName))
                {
                    applicableFilesFound++;

                    if(file.canRead())
                    {
                        // import rules, locations, etc.
                        if (Miscellaneous.copyDocumentFileToFile(file, dstRules))
                            filesImported++;
                        else
                            Toast.makeText(ActivityControlCenter.this, getResources().getString(R.string.rulesImportError), Toast.LENGTH_LONG).show();
                    }
                }
                else if (file.getName().equals(prefsFileName))
                {
                    applicableFilesFound++;

                    if(file.canRead())
                    {
                        // import rules, locations, etc.
                        if (Miscellaneous.copyDocumentFileToFile(file, dstPrefs))
                            filesImported++;
                        else
                            Toast.makeText(ActivityControlCenter.this, getResources().getString(R.string.prefsImportError), Toast.LENGTH_LONG).show();
                    }
                }
            }

            if(applicableFilesFound > 0)
            {
                if(filesImported == 0)
                    Toast.makeText(ActivityControlCenter.this, getResources().getString(R.string.noFilesImported), Toast.LENGTH_LONG).show();
                else if(filesImported < applicableFilesFound)
                    Toast.makeText(ActivityControlCenter.this, getResources().getString(R.string.notAllFilesImported), Toast.LENGTH_LONG).show();
                else if (filesImported == applicableFilesFound)
                {
                    Toast.makeText(ActivityControlCenter.this, getResources().getString(R.string.configurationImportedSuccessfully), Toast.LENGTH_LONG).show();

                    try
                    {
                        XmlFileInterface.readFile();
                        ActivityMainPoi.getInstance().updateListView();
                        ActivityMainRules.getInstance().updateListView();
                        ActivityMainProfiles.getInstance().updateListView();
                    }
                    catch (Exception e)
                    {
                        Miscellaneous.logEvent("e", "Reading import", "Rules re-read failed: " + Log.getStackTraceString(e), 1);
                        Toast.makeText(ActivityControlCenter.this, getResources().getString(R.string.errorReadingPoisAndRulesFromFile), Toast.LENGTH_LONG).show();
                    }

                    Settings.readFromPersistentStorage(ActivityControlCenter.this);

                    AutomationService service = AutomationService.getInstance();
                    if(service != null && service.isRunning)
                        service.applySettingsAndRules();
                }
                else
                    Toast.makeText(ActivityControlCenter.this, getResources().getString(R.string.noFilesImported), Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(ActivityControlCenter.this, getResources().getString(R.string.noApplicableFilesFoundInDirectory), Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(ActivityControlCenter.this, getResources().getString(R.string.noApplicableFilesFoundInDirectory), Toast.LENGTH_LONG).show();
    }

    void exportFiles(Uri uriTree)
    {
        DocumentFile directory = DocumentFile.fromTreeUri(this, uriTree);

        File srcRules = new File(Miscellaneous.getWriteableFolder() + "/" + XmlFileInterface.settingsFileName);
        File srcPrefs = new File(Miscellaneous.getWriteableFolder() + "/../shared_prefs/" + prefsFileName);

        // Clean up
        for(DocumentFile file : directory.listFiles())
        {
            /*
                On some few users' devices it seems this caused a crash because file.getName() was null.
                The reason for that remains unknown, but we don't want the export to crash because of it.
             */
            if(!StringUtils.isEmpty(file.getName()))
            {
                if (file.getName().equals(XmlFileInterface.settingsFileName) && file.canWrite())
                    file.delete();
                else if (file.getName().equals(prefsFileName) && file.canWrite())
                    file.delete();
            }
        }

        DocumentFile dstRules = directory.createFile("text/xml", XmlFileInterface.settingsFileName);
        DocumentFile dstPrefs = directory.createFile("text/xml", prefsFileName);

        if(dstRules.canWrite() && dstPrefs.canWrite())
        {
            if(Miscellaneous.copyFileToDocumentFile(srcRules, dstRules) && Miscellaneous.copyFileToDocumentFile(srcPrefs, dstPrefs))
                Toast.makeText(ActivityControlCenter.this, getResources().getString(R.string.configurationExportedSuccessfully), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(ActivityControlCenter.this, getResources().getString(R.string.ConfigurationExportError), Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(ActivityControlCenter.this, getResources().getString(R.string.ConfigurationExportError), Toast.LENGTH_LONG).show();
    }

    private static AlertDialog getDefaultSettingsDialog(final Context context)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(context.getResources().getString(R.string.areYouSure));
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (Settings.initializeSettings(context, true))
                    Toast.makeText(context, context.getResources().getString(R.string.settingsSetToDefault), Toast.LENGTH_LONG).show();
            }
        });
        alertDialogBuilder.setNegativeButton(context.getResources().getString(R.string.no), null);
        AlertDialog alertDialog = alertDialogBuilder.create();

        return alertDialog;
    }

    AlertDialog getShareConfigAndLogDialogue(final Context context)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(context.getResources().getString(R.string.shareConfigAndLogFilesWithDev));
        alertDialogBuilder.setMessage(context.getResources().getString(R.string.shareConfigAndLogExplanation));
        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                File dstZipFile = new File(Miscellaneous.getAnyContext().getCacheDir() + "/" + Settings.zipFileName);

                ArrayList<String> srcFilesList = new ArrayList<>();
                srcFilesList.add(Miscellaneous.getWriteableFolder() + "/" + XmlFileInterface.settingsFileName);
                srcFilesList.add(Miscellaneous.getWriteableFolder() + "/../shared_prefs/" + prefsFileName);

                String logFilePath = Miscellaneous.getWriteableFolder() + "/" + Miscellaneous.logFileName;
                if ((new File(logFilePath)).exists())
                    srcFilesList.add(logFilePath);

                String logFilePathArchive = Miscellaneous.getWriteableFolder() + "/" + Miscellaneous.logFileName + "-old";
                if ((new File(logFilePathArchive)).exists())
                    srcFilesList.add(logFilePathArchive);

                String[] srcFiles = srcFilesList.toArray(new String[srcFilesList.size()]);

                if (dstZipFile.exists())
                    dstZipFile.delete();

                Miscellaneous.zip(srcFiles, dstZipFile.getAbsolutePath());

            /*
                Without root the zip file in the cache directory is not directly accessible.
                But have to route it through this content provider crap.
             */

                String subject = "Automation logs";

                Uri uri = Uri.parse("content://com.jens.automation2/" + Settings.zipFileName);

                Miscellaneous.sendEmail(ActivityControlCenter.this, "android-development@gmx.de", "Automation logs", getSystemInfo(), uri);
            }
        });

        alertDialogBuilder.setNegativeButton(context.getResources().getString(R.string.no), null);
        AlertDialog alertDialog = alertDialogBuilder.create();

        return alertDialog;
    }

    public static String getSystemInfo()
    {
        StringBuilder systemInfoText = new StringBuilder();
        systemInfoText.append("App details" + Miscellaneous.lineSeparator);
        systemInfoText.append("Version name: " + BuildConfig.VERSION_NAME + Miscellaneous.lineSeparator);
        systemInfoText.append("Version code: " + BuildConfig.VERSION_CODE + Miscellaneous.lineSeparator);
        systemInfoText.append("Flavor: " + BuildConfig.FLAVOR + Miscellaneous.lineSeparator);
        systemInfoText.append("Device details" + Miscellaneous.lineSeparator);
        systemInfoText.append("OS version: " + System.getProperty("os.version") + Miscellaneous.lineSeparator);
        systemInfoText.append("API Level: " + android.os.Build.VERSION.SDK + Miscellaneous.lineSeparator);
        systemInfoText.append("Device: " + android.os.Build.DEVICE + Miscellaneous.lineSeparator);
        systemInfoText.append("Model: " + android.os.Build.MODEL + Miscellaneous.lineSeparator);
        systemInfoText.append("Product: " + android.os.Build.PRODUCT + Miscellaneous.lineSeparator);
        systemInfoText.append("Country: " + Miscellaneous.getUserCountry(Miscellaneous.getAnyContext()) + Miscellaneous.lineSeparator);
        systemInfoText.append("OS language: " + Locale.getDefault().getDisplayName() + Miscellaneous.lineSeparator);
        systemInfoText.append("Logfile written: " + String.valueOf(Settings.writeLogFile) + Miscellaneous.lineSeparator);
        systemInfoText.append("Log level: " + String.valueOf(Settings.logLevel));

        /*
            I've checked the Locale methods on my Android 4.1.2 device, and the results:

            Locale.getDefault().getLanguage()       ---> en
            Locale.getDefault().getISO3Language()   ---> eng
            Locale.getDefault().getCountry()        ---> US
            Locale.getDefault().getISO3Country()    ---> USA
            Locale.getDefault().getDisplayCountry() ---> United States
            Locale.getDefault().getDisplayName()    ---> English (United States)
            Locale.getDefault().toString()          ---> en_US
            Locale.getDefault().getDisplayLanguage()---> English
            Locale.getDefault().toLanguageTag()     ---> en-US
         */

        return systemInfoText.toString();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        String folder = Miscellaneous.getWriteableFolder();
        if (folder != null && folder.length() > 0)
        {
            tvFileStoreLocation.setText(String.format(getResources().getString(R.string.filesStoredAt), folder));
            tvFileStoreLocation.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Uri selectedUri = Uri.parse(folder);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(selectedUri, "resource/folder");

                    if (intent.resolveActivityInfo(getPackageManager(), 0) != null)
                    {
                        startActivity(intent);
                    }
                    else
                    {
                        // if you reach this place, it means there is no any file
                        // explorer app installed on your device
                        Toast.makeText(ActivityControlCenter.this, getResources().getString(R.string.noFileManageInstalled), Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
    }
}