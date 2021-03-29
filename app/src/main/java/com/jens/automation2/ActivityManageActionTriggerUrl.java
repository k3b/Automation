package com.jens.automation2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.jens.automation2.Action.Action_Enum;

import java.util.Map;

public class ActivityManageActionTriggerUrl extends Activity
{
	Button bSaveTriggerUrl;
	EditText etTriggerUrl, etTriggerUrlUsername, etTriggerUrlPassword;
	ListView lvTriggerUrlPostParameters;
	CheckBox chkTriggerUrlUseAuthentication;
	TableLayout tlTriggerUrlAuthentication;
	
	ArrayAdapter<Map<String,String>> lvTriggerUrlPostParametersAdapter;
	
//	private String existingUrl = "";
	
	public static boolean edit = false;
	public static Action resultingAction = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.trigger_url_editor);
		
		etTriggerUrl = (EditText)findViewById(R.id.etTriggerUrl);
		etTriggerUrlUsername = (EditText)findViewById(R.id.etTriggerUrlUsername);
		etTriggerUrlPassword = (EditText)findViewById(R.id.etTriggerUrlPassword);
		chkTriggerUrlUseAuthentication = (CheckBox)findViewById(R.id.chkTriggerUrlUseAuthentication);
		lvTriggerUrlPostParameters = (ListView)findViewById(R.id.lvTriggerUrlPostParameters);
		tlTriggerUrlAuthentication = (TableLayout)findViewById(R.id.tlTriggerUrlAuthentication);
		bSaveTriggerUrl = (Button)findViewById(R.id.bSaveTriggerUrl);
		bSaveTriggerUrl.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				if(etTriggerUrl.getText().toString().length() > 0)
				{
					if(resultingAction == null)
					{
						resultingAction = new Action();
						resultingAction.setAction(Action_Enum.triggerUrl);
						resultingAction.setParameter1(chkTriggerUrlUseAuthentication.isChecked());
						
						String username = etTriggerUrlUsername.getText().toString();
						String password = etTriggerUrlPassword.getText().toString();
						
						if(username == null)
							username = "";
						
						if(password == null)
							password = "";
						
						ActivityManageActionTriggerUrl.resultingAction.setParameter2(
																				username + ";" +
																				password + ";" +
																				etTriggerUrl.getText().toString().trim()
																			);
					}
					backToRuleManager();
				}
				else
					Toast.makeText(getBaseContext(), getResources().getString(R.string.urlTooShort), Toast.LENGTH_LONG).show();
			}
		});


		chkTriggerUrlUseAuthentication.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if(isChecked)
					tlTriggerUrlAuthentication.setVisibility(View.VISIBLE);
				else
					tlTriggerUrlAuthentication.setVisibility(View.GONE);

				etTriggerUrlUsername.setEnabled(isChecked);
				etTriggerUrlPassword.setEnabled(isChecked);
			}
		});
		
		lvTriggerUrlPostParameters.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				return false;
			}
		});
		updateListView();

		
		ActivityManageActionTriggerUrl.edit = getIntent().getBooleanExtra("edit", false);
		if(edit)
		{
			// username,password,URL
			String[] components = ActivityManageActionTriggerUrl.resultingAction.getParameter2().split(";");
			
			if(components.length >= 3)
			{
				etTriggerUrl.setText(components[2]);				
				chkTriggerUrlUseAuthentication.setChecked(ActivityManageActionTriggerUrl.resultingAction.getParameter1());
				etTriggerUrlUsername.setText(components[0]);
				etTriggerUrlPassword.setText(components[1]);
			}
			else
				etTriggerUrl.setText(components[0]);
		}
	}
	
	private void backToRuleManager()
	{
		if(edit && resultingAction != null)
		{
			String username = etTriggerUrlUsername.getText().toString();
			String password = etTriggerUrlPassword.getText().toString();
			
			if(username == null)
				username = "";
			
			if(password == null)
				password = "";
			
			ActivityManageActionTriggerUrl.resultingAction.setParameter1(chkTriggerUrlUseAuthentication.isChecked());
			
			ActivityManageActionTriggerUrl.resultingAction.setParameter2(
																	username + ";" +
																	password + ";" +
																	etTriggerUrl.getText().toString()
																);
		}
		
		setResult(RESULT_OK);
		
		this.finish();
	}
	
	private void updateListView()
	{
		Miscellaneous.logEvent("i", "ListView", "Attempting to update lvTriggerUrlPostParameters", 4);
		try
		{
			if(lvTriggerUrlPostParameters.getAdapter() == null)
				lvTriggerUrlPostParameters.setAdapter(lvTriggerUrlPostParametersAdapter);
			
			lvTriggerUrlPostParametersAdapter.notifyDataSetChanged();
		}
		catch(NullPointerException e)
		{}
	}
}
