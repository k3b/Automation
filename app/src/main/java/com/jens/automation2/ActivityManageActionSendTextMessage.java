package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.jens.automation2.Action.Action_Enum;


public class ActivityManageActionSendTextMessage extends Activity
{
	Button bSaveSendTextMessage, bImportNumberFromContacts;
	EditText etPhoneNumber, etSendTextMessage;

	protected final static int requestCodeForContactsPermissions = 9876;
	
//	private String existingUrl = "";
	
	public static boolean edit = false;
	public static Action resultingAction = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.send_textmessage_editor);
		
		etSendTextMessage = (EditText)findViewById(R.id.etSendTextMessage);
		etPhoneNumber = (EditText)findViewById(R.id.etPhoneNumber);
		bSaveSendTextMessage = (Button)findViewById(R.id.bSaveSendTextMessage);
		bImportNumberFromContacts = (Button)findViewById(R.id.bImportNumberFromContacts);

		bSaveSendTextMessage.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(etSendTextMessage.getText().toString().length() > 0 && etPhoneNumber.getText().toString().length() > 0)
				{
					if(resultingAction == null)
					{
						resultingAction = new Action();
						resultingAction.setAction(Action_Enum.sendTextMessage);
						resultingAction.setParameter2(etPhoneNumber.getText().toString() + Actions.smsSeparator + etSendTextMessage.getText().toString());
					}
					backToRuleManager();
				}
				else
					Toast.makeText(getBaseContext(), getResources().getString(R.string.textTooShort), Toast.LENGTH_LONG).show();
			}
		});

		bImportNumberFromContacts.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(!ActivityPermissions.havePermission("android.permission.READ_CONTACTS", ActivityManageActionSendTextMessage.this))
				{
                    requestPermissions("android.permission.READ_CONTACTS");
				}
				else
					openContactsDialogue();
			}
		});

		ActivityManageActionSendTextMessage.edit = getIntent().getBooleanExtra("edit", false);
		if(edit)
		{
			String[] parameters = ActivityManageActionSendTextMessage.resultingAction.getParameter2().split(Actions.smsSeparator);
			etPhoneNumber.setText(parameters[0]);
			etSendTextMessage.setText(parameters[1]);
		}
		

//		String url = getIntent().getStringExtra("urlToTrigger");
//		if(url != null)
//			existingUrl = url;
	}
	
	private void backToRuleManager()
	{
//		Intent returnIntent = new Intent();
//		returnIntent.putExtra("urlToTrigger", existingUrl);		
		
//		setResult(RESULT_OK, returnIntent);
		
		if(edit && resultingAction != null)
		{
			ActivityManageActionSendTextMessage.resultingAction.setParameter2(etPhoneNumber.getText().toString() + Actions.smsSeparator + etSendTextMessage.getText().toString());
		}
		
		setResult(RESULT_OK);
		
		this.finish();
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

	private void openContactsDialogue()
	{
//		Toast.makeText(ActivityEditSendTextMessage.this, "Opening contacts dialogue", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, 1000);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 1000)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                String phoneNo = null;
                String name = null;

                Uri uri = data.getData();
                Cursor cursor = ActivityManageActionSendTextMessage.this.getContentResolver().query(uri, null, null, null, null);

                if (cursor.moveToFirst())
                {
                    int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                    phoneNo = cursor.getString(phoneIndex);
                    name = cursor.getString(nameIndex);

                    etPhoneNumber.setText(phoneNo);
                }
            }
        }
        //super.onActivityResult(requestCode, resultCode, data);
    }
}
