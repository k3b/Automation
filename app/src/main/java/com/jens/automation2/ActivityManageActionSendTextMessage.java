package com.jens.automation2;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.jens.automation2.Action.Action_Enum;

import org.apache.commons.lang3.StringUtils;


public class ActivityManageActionSendTextMessage extends Activity
{
	Button bSaveSendTextMessage, bImportNumberFromContacts, bMmsAttachment;
	EditText etPhoneNumber, etSendTextMessage;
	RadioButton rbMessageTypeSms, rbMessageTypeMms;
	TextView tvSendMmsFileAttachment;

	protected final static int requestCodeForContactsPermissions = 9876;
	protected final static int requestCodeGetContact = 3235;
	protected final static int requestCodeGetMMSattachment = 3236;

	public static final String messageTypeSms = "sms";
	public static final String messageTypeMms = "mms";
	
	public static boolean edit = false;
	public static Action resultingAction = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_manage_action_send_textmessage);
		
		etSendTextMessage = (EditText)findViewById(R.id.etSendTextMessage);
		etPhoneNumber = (EditText)findViewById(R.id.etPhoneNumber);
		bSaveSendTextMessage = (Button)findViewById(R.id.bSaveSendTextMessage);
		bImportNumberFromContacts = (Button)findViewById(R.id.bImportNumberFromContacts);
		rbMessageTypeSms = (RadioButton)findViewById(R.id.rbMessageTypeSms);
		rbMessageTypeMms = (RadioButton) findViewById(R.id.rbMessageTypeMms);
		bMmsAttachment = (Button)findViewById(R.id.bMmsAttachment);
		tvSendMmsFileAttachment = (TextView)findViewById(R.id.tvSendMmsFileAttachment);

		bSaveSendTextMessage.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(etSendTextMessage.getText().toString().length() > 0 && etPhoneNumber.getText().toString().length() > 0)
				{
					if(rbMessageTypeMms.isChecked() && StringUtils.isEmpty(tvSendMmsFileAttachment.getText().toString()))
						Toast.makeText(getBaseContext(), getResources().getString(R.string.chooseFile), Toast.LENGTH_LONG).show();
					else
					{
						if (resultingAction == null)
						{
							resultingAction = new Action();
							resultingAction.setAction(Action_Enum.sendTextMessage);
							String messageType = null;
							String path = "";

							if(rbMessageTypeSms.isChecked())
								messageType = messageTypeSms;
							else
							{
								messageType = messageTypeMms;
								path = Actions.smsSeparator + tvSendMmsFileAttachment.getText().toString();
							}

							resultingAction.setParameter2(etPhoneNumber.getText().toString() + Actions.smsSeparator + etSendTextMessage.getText().toString() + Actions.smsSeparator + messageType + path);
						}
						backToRuleManager();
					}
				}
				else
					Toast.makeText(getBaseContext(), getResources().getString(R.string.enterPhoneNumberAndText), Toast.LENGTH_LONG).show();
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

		RadioButton.OnCheckedChangeListener checkedChangedListener = new RadioButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b)
			{
				bMmsAttachment.setEnabled(rbMessageTypeMms.isChecked());
			}
		};
		rbMessageTypeSms.setOnCheckedChangeListener(checkedChangedListener);
		rbMessageTypeMms.setOnCheckedChangeListener(checkedChangedListener);

		bMmsAttachment.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
				chooseFileIntent.setType("*/*");
				chooseFileIntent = Intent.createChooser(chooseFileIntent, getResources().getString(R.string.chooseFile));
				startActivityForResult(chooseFileIntent, requestCodeGetMMSattachment);
			}
		});

		ActivityManageActionSendTextMessage.edit = getIntent().getBooleanExtra("edit", false);
		if(edit)
		{
			String[] parameters = ActivityManageActionSendTextMessage.resultingAction.getParameter2().split(Actions.smsSeparator);
			etPhoneNumber.setText(parameters[0]);
			etSendTextMessage.setText(parameters[1]);
		}
	}
	
	private void backToRuleManager()
	{
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
				if(permissions[i].equals(Manifest.permission.READ_CONTACTS))
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
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, requestCodeGetContact);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		if(resultCode == Activity.RESULT_OK)
		{
			if(requestCode == requestCodeGetContact)
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
			else if (requestCode == requestCodeGetMMSattachment)
			{
				Uri fileUri = data.getData();
				String filePath = fileUri.getPath();
				tvSendMmsFileAttachment.setText(filePath);
			}
        }
        //super.onActivityResult(requestCode, resultCode, data);
    }
}
