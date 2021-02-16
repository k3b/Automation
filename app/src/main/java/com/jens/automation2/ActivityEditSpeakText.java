package com.jens.automation2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jens.automation2.Action.Action_Enum;

public class ActivityEditSpeakText extends Activity
{
	private Button bSaveSpeakText;
	private EditText etSpeakText;
	
//	private String existingUrl = "";
	
	public static boolean edit = false;
	public static Action resultingAction = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.speak_text_editor);
		
		etSpeakText = (EditText)findViewById(R.id.etTextToSpeak);
		bSaveSpeakText = (Button)findViewById(R.id.bSaveTriggerUrl);
		bSaveSpeakText.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				if(etSpeakText.getText().toString().length()>0)
				{
					if(resultingAction == null)
					{
						resultingAction = new Action();
						resultingAction.setAction(Action_Enum.speakText);
						resultingAction.setParameter2(etSpeakText.getText().toString());
					}
					backToRuleManager();
				}
				else
					Toast.makeText(getBaseContext(), getResources().getString(R.string.textTooShort), Toast.LENGTH_LONG).show();
			}
		});
		
		ActivityEditSpeakText.edit = getIntent().getBooleanExtra("edit", false);
		if(edit)
			etSpeakText.setText(ActivityEditSpeakText.resultingAction.getParameter2());
		

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
			ActivityEditSpeakText.resultingAction.setParameter2(etSpeakText.getText().toString());
		
		setResult(RESULT_OK);
		
		this.finish();
	}

}
