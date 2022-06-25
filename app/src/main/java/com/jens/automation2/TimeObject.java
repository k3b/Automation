package com.jens.automation2;

import androidx.annotation.NonNull;

import java.sql.Time;

public class TimeObject
{
    int hours, minutes, seconds;

    public TimeObject()
    {

    }

    public int getHours()
    {
        return hours;
    }

    public void setHours(int hours)
    {
        this.hours = hours;
    }

    public int getMinutes()
    {
        return minutes;
    }

    public void setMinutes(int minutes)
    {
        this.minutes = minutes;
    }

    public int getSeconds()
    {
        return seconds;
    }

    public void setSeconds(int seconds)
    {
        this.seconds = seconds;
    }

    public TimeObject(int hours, int minutes, int seconds)
    {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public static TimeObject valueOf(String input)
    {
        TimeObject ro = null;

        if(input.contains(":"))
        {
            String[] parts = input.split(":");
            if(parts.length == 2)
                ro = new TimeObject(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 0);
            else if(parts.length == 3)
                ro = new TimeObject(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            else
                Miscellaneous.logEvent("w", "TimeObject", "Invalid length for time. Input: " + input, 4);
        }

        return ro;
    }

    @NonNull
    @Override
    public String toString()
    {
        Time time = Time.valueOf(this.getHours() + ":" + this.getMinutes() + ":" + this.getSeconds());
        return time.toString();
    }
}
