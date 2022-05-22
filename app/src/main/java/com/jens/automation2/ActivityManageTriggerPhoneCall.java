package com.jens.automation2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import static com.jens.automation2.Trigger.triggerParameter2Split;

public class ActivityManageTriggerPhoneCall extends Activity
{
    public static Trigger editedPhoneCallTrigger;
    boolean edit = false;
    public static Trigger resultingTrigger;
    ProgressDialog progressDialog = null;
    protected final static int requestCodeForContactsPermissions = 2345;
    protected final static int requestCodeGetContact = 3235;

    EditText etTriggerPhoneCallPhoneNumber;
    RadioButton rbTriggerPhoneCallStateRinging, rbTriggerPhoneCallStateStarted, rbTriggerPhoneCallStateStopped, rbTriggerPhoneCallDirectionAny, rbTriggerPhoneCallDirectionIncoming, rbTriggerPhoneCallDirectionOutgoing;
    Button bSaveTriggerPhoneCall, bTriggerPhoneCallImportFromContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_trigger_phone_call);

        etTriggerPhoneCallPhoneNumber = (EditText)findViewById(R.id.etTriggerPhoneCallPhoneNumber);
        rbTriggerPhoneCallStateRinging = (RadioButton)findViewById(R.id.rbTriggerPhoneCallStateRinging);
        rbTriggerPhoneCallStateStarted = (RadioButton)findViewById(R.id.rbTriggerPhoneCallStateStarted);
        rbTriggerPhoneCallStateStopped = (RadioButton)findViewById(R.id.rbTriggerPhoneCallStateStopped);
        rbTriggerPhoneCallDirectionAny = (RadioButton)findViewById(R.id.rbTriggerPhoneCallDirectionAny);
        rbTriggerPhoneCallDirectionIncoming = (RadioButton)findViewById(R.id.rbTriggerPhoneCallDirectionIncoming);
        rbTriggerPhoneCallDirectionOutgoing = (RadioButton)findViewById(R.id.rbTriggerPhoneCallDirectionOutgoing);
        bTriggerPhoneCallImportFromContacts = (Button) findViewById(R.id.bTriggerPhoneCallImportFromContacts);
        bSaveTriggerPhoneCall = (Button) findViewById(R.id.bSaveTriggerPhoneCall);

        bSaveTriggerPhoneCall.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String tp2Result = "";

                if(rbTriggerPhoneCallStateRinging.isChecked())
                    tp2Result += Trigger.triggerPhoneCallStateRinging;
                else if(rbTriggerPhoneCallStateStarted.isChecked())
                    tp2Result += Trigger.triggerPhoneCallStateStarted;
                else if(rbTriggerPhoneCallStateStopped.isChecked())
                    tp2Result += Trigger.triggerPhoneCallStateStopped;

                tp2Result += triggerParameter2Split;

                if(rbTriggerPhoneCallDirectionAny.isChecked())
                    tp2Result += Trigger.triggerPhoneCallDirectionAny;
                else if(rbTriggerPhoneCallDirectionIncoming.isChecked())
                    tp2Result += Trigger.triggerPhoneCallDirectionIncoming;
                else if(rbTriggerPhoneCallDirectionOutgoing.isChecked())
                    tp2Result += Trigger.triggerPhoneCallDirectionOutgoing;

                tp2Result += triggerParameter2Split;

                if(etTriggerPhoneCallPhoneNumber.getText() != null && etTriggerPhoneCallPhoneNumber.getText().toString().length() > 0)
                    tp2Result += etTriggerPhoneCallPhoneNumber.getText().toString();
                else
                    tp2Result += Trigger.triggerPhoneCallNumberAny;

                if(edit)
                {
                    editedPhoneCallTrigger.setTriggerParameter(false);
                    editedPhoneCallTrigger.setTriggerParameter2(tp2Result);
                    ActivityManageTriggerPhoneCall.this.setResult(RESULT_OK);
                }
                else
                {
                    Intent data = new Intent();
                    data.putExtra(ActivityManageRule.intentNameTriggerParameter1, false);
                    data.putExtra(ActivityManageRule.intentNameTriggerParameter2, tp2Result);
                    ActivityManageTriggerPhoneCall.this.setResult(RESULT_OK, data);
                }

                finish();
            }
        });

        bTriggerPhoneCallImportFromContacts.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !ActivityPermissions.havePermission("android.permission.READ_CONTACTS", ActivityManageTriggerPhoneCall.this))
                {
                    requestPermissions("android.permission.READ_CONTACTS");
                }
                else
                    openContactsDialogue();
            }
        });

        Intent i = getIntent();
        if(i.getBooleanExtra("edit", false) == true)
        {
            edit = true;
            loadValuesIntoGui();
        }
    }

    protected void requestPermissions(String... requiredPermissions)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(requiredPermissions.length > 0)
            {
                StringBuilder permissions = new StringBuilder();
                for (String perm : requiredPermissions)
                    permissions.append(perm + "; ");
                if (permissions.length() > 0)
                    permissions.delete(permissions.length() - 2, permissions.length());

                Miscellaneous.logEvent("i", "Permissions", "Requesting permissions: " + permissions, 2);

                requestPermissions(requiredPermissions, requestCodeForContactsPermissions);
            }
        }
    }

    private void openContactsDialogue()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, requestCodeGetContact);
    }

    private void loadValuesIntoGui()
    {
        String[] parts = editedPhoneCallTrigger.getTriggerParameter2().split(triggerParameter2Split);

        if(parts[0].equals(Trigger.triggerPhoneCallStateRinging))
            rbTriggerPhoneCallStateRinging.setChecked(true);
        else if(parts[0].equals(Trigger.triggerPhoneCallStateStarted))
            rbTriggerPhoneCallStateStarted.setChecked(true);
        else if(parts[0].equals(Trigger.triggerPhoneCallStateStopped))
            rbTriggerPhoneCallStateStopped.setChecked(true);

        if(parts[1].equals(Trigger.triggerPhoneCallDirectionAny))
            rbTriggerPhoneCallDirectionAny.setChecked(true);
        else if(parts[1].equals(Trigger.triggerPhoneCallDirectionIncoming))
            rbTriggerPhoneCallDirectionIncoming.setChecked(true);
        else if(parts[1].equals(Trigger.triggerPhoneCallDirectionOutgoing))
            rbTriggerPhoneCallDirectionOutgoing.setChecked(true);

        if(!parts[2].equals(Trigger.triggerPhoneCallNumberAny))
            etTriggerPhoneCallPhoneNumber.setText(parts[2]);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == requestCodeGetContact)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                String phoneNo = null;
                String name = null;

                Uri uri = data.getData();
                Cursor cursor = ActivityManageTriggerPhoneCall.this.getContentResolver().query(uri, null, null, null, null);

                if (cursor.moveToFirst())
                {
                    int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                    phoneNo = cursor.getString(phoneIndex);
                    name = cursor.getString(nameIndex);

                    etTriggerPhoneCallPhoneNumber.setText(phoneNo);
                }
            }
        }
        //super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(requestCode == requestCodeForContactsPermissions)
        {
            for(int i=0; i<permissions.length; i++)
            {
                if(permissions[i].equals("android.permission.READ_CONTACTS"))
                {
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    {
                        openContactsDialogue();
                    }
                }
            }
        }
    }
}