package com.jens.automation2;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.jens.automation2.R.layout;

public class ActivityHelp extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(layout.help_text);

		TextView tvHelpTextEnergySaving = (TextView) findViewById(R.id.tvHelpTextEnergySaving);
		tvHelpTextEnergySaving.setMovementMethod(LinkMovementMethod.getInstance());
	}

}
