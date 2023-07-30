package com.jens.automation2;

import static com.jens.automation2.ActivityManageActionTriggerUrl.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jens.automation2.Action.Action_Enum;

import org.apache.commons.lang3.StringUtils;

public class ActivityManageActionSetVariable extends Activity
{
	private Button bSaveVariable;
	private EditText etVariableSetKey, etVariableSetValue;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Miscellaneous.setDisplayLanguage(this);
		this.setContentView(R.layout.activity_manage_action_set_variable);

		etVariableSetKey = (EditText)findViewById(R.id.etVariableSetKey);
		etVariableSetValue = (EditText)findViewById(R.id.etVariableSetValue);
		bSaveVariable = (Button)findViewById(R.id.bSaveVariable);
		bSaveVariable.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				if(StringUtils.isEmpty(etVariableSetKey.getText().toString()))
				{
					Toast.makeText(ActivityManageActionSetVariable.this, getResources().getString(R.string.enterVariableKey), Toast.LENGTH_SHORT).show();
				}
				else
				{
					Intent response = new Intent();

					if(StringUtils.isEmpty(etVariableSetValue.getText().toString()))
						response.putExtra(ActivityManageRule.intentNameActionParameter2, etVariableSetKey.getText().toString());
					else
						response.putExtra(ActivityManageRule.intentNameActionParameter2, etVariableSetKey.getText().toString() + Action.actionParameter2Split + etVariableSetValue.getText().toString());

					setResult(RESULT_OK, response);
					finish();
				}
			}
		});

		if(getIntent().hasExtra(ActivityManageRule.intentNameActionParameter2))
		{
			String[] input = getIntent().getStringExtra(ActivityManageRule.intentNameActionParameter2).split(Action.actionParameter2Split);
			etVariableSetKey.setText(input[0]);
			if(input.length > 1)
				etVariableSetValue.setText(input[1]);
		}
	}
}