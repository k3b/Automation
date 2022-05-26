package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

public class ActivityManageTriggerTimeFrame extends Activity
{
	Button bSaveTimeFrame;
	TimePicker startPicker, stopPicker;
	CheckBox checkMonday, checkTuesday, checkWednesday, checkThursday, checkFriday, checkSaturday, checkSunday, chkRepeat;
	RadioButton radioTimeFrameEntering, radioTimeFrameLeaving;
	EditText etRepeatEvery;
	TextView tvDaysHint;

	static Trigger editedTimeFrameTrigger = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_trigger_timeframe);
		
		startPicker = (TimePicker)findViewById(R.id.tpTimeFrameStart);
		stopPicker = (TimePicker)findViewById(R.id.tpTimeFrameStop);
		startPicker.setIs24HourView(true);
		stopPicker.setIs24HourView(true);		
		
		bSaveTimeFrame = (Button)findViewById(R.id.bSaveTimeFrame);
		checkMonday = (CheckBox)findViewById(R.id.checkMonday);
		checkTuesday = (CheckBox)findViewById(R.id.checkTuesday);
		checkWednesday = (CheckBox)findViewById(R.id.checkWednesday);
		checkThursday = (CheckBox)findViewById(R.id.checkThursday);
		checkFriday = (CheckBox)findViewById(R.id.checkFriday);
		checkSaturday = (CheckBox)findViewById(R.id.checkSaturday);
		checkSunday = (CheckBox)findViewById(R.id.checkSunday);
		radioTimeFrameEntering = (RadioButton)findViewById(R.id.radioTimeFrameEntering);
		radioTimeFrameLeaving = (RadioButton)findViewById(R.id.radioTimeFrameLeaving);
		chkRepeat = (CheckBox)findViewById(R.id.chkRepeat);
		etRepeatEvery = (EditText)findViewById(R.id.etRepeatEvery);
		tvDaysHint = (TextView)findViewById(R.id.tvDaysHint);

		bSaveTimeFrame.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				TimeObject startTime = new TimeObject();
				startTime.setHours(startPicker.getCurrentHour());
				startTime.setMinutes(startPicker.getCurrentMinute());

				TimeObject stopTime = new TimeObject();
				stopTime.setHours(stopPicker.getCurrentHour());
				stopTime.setMinutes(stopPicker.getCurrentMinute());
				
				ArrayList<Integer> dayList = new ArrayList<Integer>();
				if(checkMonday.isChecked())
					dayList.add(Calendar.MONDAY);
				if(checkTuesday.isChecked())
					dayList.add(Calendar.TUESDAY);
				if(checkWednesday.isChecked())
					dayList.add(Calendar.WEDNESDAY);
				if(checkThursday.isChecked())
					dayList.add(Calendar.THURSDAY);
				if(checkFriday.isChecked())
					dayList.add(Calendar.FRIDAY);
				if(checkSaturday.isChecked())
					dayList.add(Calendar.SATURDAY);
				if(checkSunday.isChecked())
					dayList.add(Calendar.SUNDAY);
				
				if(
					!checkMonday.isChecked()
							&&
					!checkTuesday.isChecked()
							&&
					!checkWednesday.isChecked()
							&&
					!checkThursday.isChecked()
							&&
					!checkFriday.isChecked()
							&&
					!checkSaturday.isChecked()
							&&
					!checkSunday.isChecked()
				  )
				{
					Toast.makeText(getBaseContext(), getResources().getString(R.string.selectOneDay), Toast.LENGTH_LONG).show();
					return;
				}

				boolean goOn = false;
				if(chkRepeat.isChecked())
				{
					if(!StringUtils.isEmpty(etRepeatEvery.getText().toString()))
					{
						try
						{
							long value = Long.parseLong(etRepeatEvery.getText().toString());
							if(value > 0)
							{
								goOn = true;
							}
						}
						catch(Exception e)
						{
						}
					}
				}
				else
					goOn = true;

				if(!goOn)
				{
					Toast.makeText(getBaseContext(), getResources().getString(R.string.enterRepetitionTime), Toast.LENGTH_LONG).show();
					return;
				}

				if(editedTimeFrameTrigger.getTimeFrame() == null)
				{
					// add new one
					if(chkRepeat.isChecked())
						editedTimeFrameTrigger.setTimeFrame(new TimeFrame(startTime, stopTime, dayList, Long.parseLong(etRepeatEvery.getText().toString())));
					else
						editedTimeFrameTrigger.setTimeFrame(new TimeFrame(startTime, stopTime, dayList, 0));
				}
				else
				{
					// edit one
					editedTimeFrameTrigger.getTimeFrame().setTriggerTimeStart(startTime);
					editedTimeFrameTrigger.getTimeFrame().setTriggerTimeStop(stopTime);
					editedTimeFrameTrigger.getTimeFrame().getDayList().clear();
					editedTimeFrameTrigger.getTimeFrame().setDayList(dayList);

					if(chkRepeat.isChecked())
						editedTimeFrameTrigger.getTimeFrame().setRepetition(Long.parseLong(etRepeatEvery.getText().toString()));
					else
						editedTimeFrameTrigger.getTimeFrame().setRepetition(0);
				}
				
				editedTimeFrameTrigger.setTriggerParameter(radioTimeFrameEntering.isChecked());
				editedTimeFrameTrigger.setTriggerParameter2(editedTimeFrameTrigger.getTimeFrame().toTriggerParameter2String());

				Intent response = new Intent();
				response.putExtra(ActivityManageRule.intentNameTriggerParameter1, editedTimeFrameTrigger.getTriggerParameter());
				response.putExtra(ActivityManageRule.intentNameTriggerParameter2, editedTimeFrameTrigger.getTriggerParameter2());

				setResult(RESULT_OK, response);
				finish();
			}
		});

		chkRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				etRepeatEvery.setEnabled(isChecked);
			}
		});

		if(getIntent().hasExtra(ActivityManageRule.intentNameTriggerParameter2))
		{
			editedTimeFrameTrigger = new Trigger();
			editedTimeFrameTrigger.setTriggerParameter(getIntent().getBooleanExtra(ActivityManageRule.intentNameTriggerParameter1, true));
			editedTimeFrameTrigger.setTriggerParameter2(getIntent().getStringExtra(ActivityManageRule.intentNameTriggerParameter2));
			editedTimeFrameTrigger.setTimeFrame(new TimeFrame(editedTimeFrameTrigger.getTriggerParameter2()));
			loadVariableIntoGui();
		}

		TimePicker.OnTimeChangedListener pickerListener = new TimePicker.OnTimeChangedListener()
		{
			@Override
			public void onTimeChanged(TimePicker timePicker, int i, int i1)
			{
				if(
						startPicker.getCurrentHour() > stopPicker.getCurrentHour()
								||
						(
							startPicker.getCurrentHour() == stopPicker.getCurrentHour()
								&&
							startPicker.getCurrentMinute() >= stopPicker.getCurrentMinute()
						)
				)
					tvDaysHint.setText(getResources().getString(R.string.timeFrameDaysHint));
				else
					tvDaysHint.setText("");
			}
		};
		startPicker.setOnTimeChangedListener(pickerListener);
		stopPicker.setOnTimeChangedListener(pickerListener);

		// Perform check once
		pickerListener.onTimeChanged(null, 0, 0);
	}

	private void loadVariableIntoGui()
	{
		startPicker.setCurrentHour(editedTimeFrameTrigger.getTimeFrame().getTriggerTimeStart().getHours());
		startPicker.setCurrentMinute(editedTimeFrameTrigger.getTimeFrame().getTriggerTimeStart().getMinutes());
		
		stopPicker.setCurrentHour(editedTimeFrameTrigger.getTimeFrame().getTriggerTimeStop().getHours());
		stopPicker.setCurrentMinute(editedTimeFrameTrigger.getTimeFrame().getTriggerTimeStop().getMinutes());

		radioTimeFrameEntering.setChecked(editedTimeFrameTrigger.getTriggerParameter());
		radioTimeFrameLeaving.setChecked(!editedTimeFrameTrigger.getTriggerParameter());
		
		for(int day : editedTimeFrameTrigger.getTimeFrame().getDayList())
		{
			switch(day)
			{
				case Calendar.MONDAY:
					checkMonday.setChecked(true);
					break;
				case Calendar.TUESDAY:
					checkTuesday.setChecked(true);
					break;
				case Calendar.WEDNESDAY:
					checkWednesday.setChecked(true);
					break;
				case Calendar.THURSDAY:
					checkThursday.setChecked(true);
					break;
				case Calendar.FRIDAY:
					checkFriday.setChecked(true);
					break;
				case Calendar.SATURDAY:
					checkSaturday.setChecked(true);
					break;
				case Calendar.SUNDAY:
					checkSunday.setChecked(true);
					break;
				default:
					Miscellaneous.logEvent("w", "TimeFrame", "Daylist contains invalid day: " + String.valueOf(day), 4);
					break;
			}
		}

		if(editedTimeFrameTrigger.getTimeFrame().getRepetition() > 0)
		{
			chkRepeat.setChecked(true);
			etRepeatEvery.setText(String.valueOf(editedTimeFrameTrigger.getTimeFrame().getRepetition()));
		}
	}
}