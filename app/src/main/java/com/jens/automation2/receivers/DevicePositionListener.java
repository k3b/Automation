package com.jens.automation2.receivers;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.jens.automation2.ActivityManageTriggerDevicePosition;
import com.jens.automation2.Miscellaneous;

public class DevicePositionListener implements SensorEventListener
{
    // https://developer.android.com/guide/topics/sensors/sensors_position#java

    ActivityManageTriggerDevicePosition activityManageTriggerDevicePositionInstance = null;

    //the Sensor Manager
    private SensorManager sManager;
    static DevicePositionListener instance = null;

    public static DevicePositionListener getInstance()
    {
        if(instance == null)
            instance = new DevicePositionListener();

        return instance;
    }

    public void startSensor(Context context, ActivityManageTriggerDevicePosition activityManageTriggerDevicePositionInstance)
    {
        this.activityManageTriggerDevicePositionInstance = activityManageTriggerDevicePositionInstance;
        sManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        /*register the sensor listener to listen to the gyroscope sensor, use the
        callbacks defined in this class, and gather the sensor information as quick
        as possible*/
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stopSensor()
    {
        //unregister the sensor listener
        sManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1)
    {
        //Do nothing.
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        //if sensor is unreliable, return void
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
        {
            return;
        }

        //else it will output the Roll, Pitch and Yawn values
        activityManageTriggerDevicePositionInstance.updateFields(event.values[2], event.values[1], event.values[0]);

//        tvToUpdate.setText("Orientation X (Roll) :"+ Float.toString(event.values[2]) +"\n"+
//                "Orientation Y (Pitch) :"+ Float.toString(event.values[1]) +"\n"+
//                "Orientation Z (Yaw) :"+ Float.toString(event.values[0]));
    }

    /*
            Azimuth (degrees of rotation about the -z axis).
            This is the angle between the device's current compass direction and magnetic north. If the top edge of the
            device faces magnetic north, the azimuth is 0 degrees; if the top edge faces south, the azimuth is 180 degrees.
            Similarly, if the top edge faces east, the azimuth is 90 degrees, and if the top edge faces west, the azimuth is 270 degrees.

            Pitch (degrees of rotation about the x axis).
            This is the angle between a plane parallel to the device's screen and a plane parallel to the ground. If you hold the device
            parallel to the ground with the bottom edge closest to you and tilt the top edge of the device toward the ground, the pitch
            angle becomes positive. Tilting in the opposite direction— moving the top edge of the device away from the ground—causes
            the pitch angle to become negative. The range of values is -180 degrees to 180 degrees.

            Roll (degrees of rotation about the y axis).
            This is the angle between a plane perpendicular to the device's screen and a plane perpendicular to the ground.
            If you hold the device parallel to the ground with the bottom edge closest to you and tilt the left edge of the
            device toward the ground, the roll angle becomes positive. Tilting in the opposite direction—moving the right
            edge of the device toward the ground— causes the roll angle to become negative. The range of values is -90 degrees
            to 90 degrees.
     */
}
