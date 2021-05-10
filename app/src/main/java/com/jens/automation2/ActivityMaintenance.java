package com.jens.automation2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.util.ArrayList;

public class ActivityMaintenance extends Activity
{
    final static int requestCodeImport = 1001;
    final static int requestCodeExport = 1002;
    final static int requestCodeMoreSettings = 6000;

    TextView tvFileStoreLocation;
    Button bVolumeTest, bMoreSettings, bSettingsSetToDefault, bShareConfigAndLog, bImportConfiguration, bExportConfiguration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        bVolumeTest = (Button) findViewById(R.id.bVolumeTest);
        bVolumeTest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ActivityMaintenance.this, ActivityVolumeTest.class);
                startActivity(intent);
            }
        });

        bShareConfigAndLog = (Button) findViewById(R.id.bShareConfigAndLog);
        bShareConfigAndLog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getShareConfigAndLogDialogue(ActivityMaintenance.this).show();
            }
        });

        bSettingsSetToDefault = (Button) findViewById(R.id.bSettingsSetToDefault);
        bSettingsSetToDefault.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getDefaultSettingsDialog(ActivityMaintenance.this).show();
            }
        });

        Button bMoreSettings = (Button) findViewById(R.id.bMoreSettings);
        bMoreSettings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent myIntent = new Intent(ActivityMaintenance.this, ActivitySettings.class);
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

                if (AutomationService.isMyServiceRunning(ActivityMaintenance.this))
                    Toast.makeText(this, getResources().getString(R.string.settingsWillTakeTime), Toast.LENGTH_LONG).show();

                break;
            case requestCodeImport:
                if(resultCode == RESULT_OK)
                {
                    Uri uriTree = data.getData();
                    DocumentFile directory = DocumentFile.fromTreeUri(this, uriTree);
                    for(DocumentFile file : directory.listFiles())
                    {
                        if(file.canRead() && file.getName().equals(XmlFileInterface.settingsFileName))
                        {
                            // import rules, locations, etc.
                            Miscellaneous.copyFileUsingStream(file, Miscellaneous.getWriteableFolder() + "/" + XmlFileInterface.settingsFileName);
                        }
                        else if(false && file.canRead() && file.getName().equals(XmlFileInterface.settingsFileName))
                        {
                            // import rules, locations, etc.
                        }
                    }
                }
                break;
        }
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

                String logFilePath = Miscellaneous.getWriteableFolder() + "/" + Miscellaneous.logFileName;
                if((new File(logFilePath)).exists())
                    srcFilesList.add(logFilePath);

                String logFilePathArchive = Miscellaneous.getWriteableFolder() + "/" + Miscellaneous.logFileName + "-old";
                if((new File(logFilePathArchive)).exists())
                    srcFilesList.add(logFilePathArchive);

                String[] srcFiles = srcFilesList.toArray(new String[srcFilesList.size()]);

                if(dstZipFile.exists())
                    dstZipFile.delete();

                Miscellaneous.zip(srcFiles, dstZipFile.getAbsolutePath());

				/*
					Without root the zip file in the cache directory is not directly accessible.
					But have to route it through this content provider crap.
				 */

                String subject = "Automation logs";

                StringBuilder emailBody = new StringBuilder();
                emailBody.append("Device details" + Miscellaneous.lineSeparator);
                emailBody.append("OS version: " + System.getProperty("os.version") + Miscellaneous.lineSeparator);
                emailBody.append("API Level: " + android.os.Build.VERSION.SDK + Miscellaneous.lineSeparator);
                emailBody.append("Device: " + android.os.Build.DEVICE + Miscellaneous.lineSeparator);
                emailBody.append("Model: " + android.os.Build.MODEL + Miscellaneous.lineSeparator);
                emailBody.append("Product: " + android.os.Build.PRODUCT);

                Uri uri = Uri.parse("content://com.jens.automation2/" + Settings.zipFileName);

                Miscellaneous.sendEmail(ActivityMaintenance.this, "android-development@gmx.de", "Automation logs", emailBody.toString(), uri);
            }
        });
        alertDialogBuilder.setNegativeButton(context.getResources().getString(R.string.no), null);
        AlertDialog alertDialog = alertDialogBuilder.create();

        return alertDialog;
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
                        Toast.makeText(ActivityMaintenance.this, getResources().getString(R.string.noFileManageInstalled), Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
    }
}