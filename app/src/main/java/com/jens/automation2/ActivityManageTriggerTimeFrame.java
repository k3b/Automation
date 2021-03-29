package com.jens.automation2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

public class ActivityManageTriggerTimeFrame extends Activity
{
	Button bSaveTimeFrame;
	TimePicker startPicker, stopPicker;
	CheckBox checkMonday, checkTuesday, checkWednesday, checkThursday, checkFriday, checkSaturday, checkSunday;
	RadioButton radioTimeFrameEntering, radioTimeFrameLeaving;

	public static Trigger editedTimeFrameTrigger = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_trigger_timeframe);
		
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
				
		bSaveTimeFrame.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Time startTime = new Time(0);
				startTime.setHours(startPicker.getCurrentHour());
				startTime.setMinutes(startPicker.getCurrentMinute());

				Time stopTime = new Time(0);
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

				if(editedTimeFrameTrigger.getTimeFrame() == null)
					// add new one
					editedTimeFrameTrigger.setTimeFrame(new TimeFrame(startTime, stopTime, dayList));
				else
				{
					// edit one
					editedTimeFrameTrigger.getTimeFrame().setTriggerTimeStart(startTime);
					editedTimeFrameTrigger.getTimeFrame().setTriggerTimeStop(stopTime);
					editedTimeFrameTrigger.getTimeFrame().getDayList().clear();
					editedTimeFrameTrigger.getTimeFrame().setDayList(dayList);
				}
				
				editedTimeFrameTrigger.setTriggerParameter(radioTimeFrameEntering.isChecked());
				
				setResult(RESULT_OK);
				finish();
			}
		});
		
		if(editedTimeFrameTrigger.getTimeFrame() != null)
			loadVariableIntoGui();
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
	}

}
