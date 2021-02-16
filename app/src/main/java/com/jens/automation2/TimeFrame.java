package com.jens.automation2;

import java.sql.Time;
import java.util.ArrayList;

public class TimeFrame
{
	// Defines a timeframe
		private Time triggerTimeStart;
		private Time triggerTimeStop;

		private ArrayList<Integer> dayList = new ArrayList<Integer>();
		public ArrayList<Integer> getDayList()
		{
			return dayList;
		}
		public void setDayList(ArrayList<Integer> dayList)
		{
			this.dayList = dayList;
		}
		public void setDayListFromString(String dayListString)
		{
//			Log.i("Parsing", "Full string: " + dayListString);
			char[] dayListCharArray = dayListString.toCharArray();
			
			dayList = new ArrayList<Integer>();
			for(char item : dayListCharArray)
			{
//				Log.i("Parsing", String.valueOf(item));
				dayList.add(Integer.parseInt(String.valueOf(item)));
			}
		}
		
		
		public Time getTriggerTimeStart()
		{
			return triggerTimeStart;
		}
		public void setTriggerTimeStart(Time triggerTimeStart)
		{
			this.triggerTimeStart = triggerTimeStart;
		}
		public Time getTriggerTimeStop()
		{
			return triggerTimeStop;
		}
		public void setTriggerTimeStop(Time triggerTimeStop)
		{
			this.triggerTimeStop = triggerTimeStop;
		}
		
		public TimeFrame (Time timeStart, Time timeEnd, ArrayList<Integer> dayList2)
		{
			this.setTriggerTimeStart(timeStart);
			this.setTriggerTimeStop(timeEnd);
			this.setDayList(dayList2);
		}
		TimeFrame (String fileContent)
		{
			String[] dateArray = fileContent.split("/"); // example: timestart/timestop/days[int]
			this.setTriggerTimeStart(Time.valueOf(dateArray[0]));
			this.setTriggerTimeStop(Time.valueOf(dateArray[1]));
			this.setDayListFromString(dateArray[2]);
		}
		@Override
		public String toString()
		{
			String returnString = this.getTriggerTimeStart().toString() + "/" + this.getTriggerTimeStop().toString() + "/";
			
			for(Integer oneDay : this.getDayList())
				returnString += String.valueOf(oneDay);
			
			return returnString;
		}
}
