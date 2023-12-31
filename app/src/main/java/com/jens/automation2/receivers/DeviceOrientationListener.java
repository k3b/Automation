package com.jens.automation2.receivers;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.jens.automation2.ActivityManageTriggerDeviceOrientation;
import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.Settings;
import com.jens.automation2.Trigger;

import java.util.ArrayList;
import java.util.Calendar;

public class DeviceOrientationListener implements SensorEventListener, AutomationListenerInterface
{
    // https://developer.android.com/guide/topics/sensors/sensors_position#java

    ActivityManageTriggerDeviceOrientation activityManageTriggerDeviceOrientationInstance = null;

    //the Sensor Manager
    private SensorManager sManager;
    static DeviceOrientationListener instance = null;
    boolean isRunning = false;

    Calendar now = null;
    static Calendar lastTimeSignalArrived = null;
    static int sensorValueCounter = 0;

    // Gravity rotational data
    float gravity[];
    // Magnetic rotational data
    float magnetic[]; //for magnetic rotational data
    float accels[] = new float[3];
    float mags[] = new float[3];
    float[] values = new float[3];
    boolean hasMagneticSensor=false;

    // azimuth, pitch and roll
    float azimuth;
    float pitch;
    float roll;

    boolean applies = false;
    boolean flipped = false;
    boolean toggable = false;


    public static DeviceOrientationListener getInstance()
    {
        if (instance == null)
            instance = new DeviceOrientationListener();

        return instance;
    }

    public float getAzimuth()
    {
        return azimuth;
    }

    public float getPitch()
    {
        return pitch;
    }

    public float getRoll()
    {
        return roll;
    }

    public void startSensorFromConfigActivity(Context context, ActivityManageTriggerDeviceOrientation activityManageTriggerDeviceOrientationInstance)
    {
        this.activityManageTriggerDeviceOrientationInstance = activityManageTriggerDeviceOrientationInstance;

        if(!isRunning)
        {
            sManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

            /*
                register the sensor listener to listen to the gyroscope sensor, use the
                callbacks defined in this class, and gather the sensor information as quick
                as possible
            */

            isRunning = true;

            sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            hasMagneticSensor = sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stopSensorFromConfigActivity()
    {
        activityManageTriggerDeviceOrientationInstance = null;

        if(isRunning)
        {
            if(!Rule.isAnyRuleUsing(Trigger.Trigger_Enum.deviceOrientation))
            {
                //unregister the sensor listener
                sManager.unregisterListener(this);
                isRunning = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1)
    {
        //Do nothing.
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        switch (event.sensor.getType())
        {
            case Sensor.TYPE_MAGNETIC_FIELD:
                mags = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accels = event.values.clone();
                break;
        }

        if (!hasMagneticSensor)
            mags=new float[]{1f,1f,1f};

        if (mags != null && accels != null)
        {
            gravity = new float[9];
            magnetic = new float[9];
            SensorManager.getRotationMatrix(gravity, magnetic, accels, mags);
            float[] outGravity = new float[9];
            SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X, SensorManager.AXIS_Z, outGravity);
            SensorManager.getOrientation(outGravity, values);

            azimuth = values[0] * 57.2957795f;
            pitch = values[1] * 57.2957795f;
            roll = values[2] * 57.2957795f;
            mags = null;
            accels = null;
        }

        //else it will output the Roll, Pitch and Yawn values
        if(activityManageTriggerDeviceOrientationInstance != null)
            activityManageTriggerDeviceOrientationInstance.updateFields(azimuth, pitch, roll);

        /*
            For some reason the first 3 values after starting the listener
            are crap.
         */
        if(sensorValueCounter > 3)
        {
            now = Calendar.getInstance();
            if (lastTimeSignalArrived == null || now.getTimeInMillis() >= lastTimeSignalArrived.getTimeInMillis() + Settings.acceptDeviceOrientationSignalEveryX_MilliSeconds)
            {
                lastTimeSignalArrived = now;

                Miscellaneous.logEvent("i", "DeviceOrientation", "Got device orientation update: azimuth: " + String.valueOf(azimuth) + ", pitch: " + String.valueOf(pitch) + ", roll: " + String.valueOf(pitch), 4);

                if (AutomationService.isMyServiceRunning(Miscellaneous.getAnyContext()))
                {
                    ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger.Trigger_Enum.deviceOrientation);
                    for (int i = 0; i < ruleCandidates.size(); i++)
                    {
                        if (ruleCandidates.get(i).getsGreenLight(Miscellaneous.getAnyContext()))
                            ruleCandidates.get(i).activate(AutomationService.getInstance(), false);
                    }
                }
            }
        }
        else
            sensorValueCounter++;
    }

    @Override
    public void startListener(AutomationService automationService)
    {
        if(!isRunning)
        {
            sManager = (SensorManager) Miscellaneous.getAnyContext().getSystemService(SENSOR_SERVICE);

            /*
                register the sensor listener to listen to the gyroscope sensor, use the
                callbacks defined in this class, and gather the sensor information as quick
                as possible
            */

            isRunning = true;

            sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void stopListener(AutomationService automationService)
    {
        this.activityManageTriggerDeviceOrientationInstance = null;

        if(isRunning)
        {
            //unregister the sensor listener
            sManager.unregisterListener(this);
            isRunning = false;
        }
    }

    @Override
    public boolean isListenerRunning()
    {
        return isRunning;
    }

    @Override
    public Trigger.Trigger_Enum[] getMonitoredTrigger()
    {
        return new Trigger.Trigger_Enum[] { Trigger.Trigger_Enum.deviceOrientation};
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

        Computes the device's orientation based on the rotation matrix.
        When it returns, the array values are as follows:
        values[0]: Azimuth, angle of rotation about the -z axis. This value represents the angle between the device's y axis and the magnetic north pole. When facing north, this angle is 0, when facing south, this angle is π. Likewise, when facing east, this angle is π/2, and when facing west, this angle is -π/2. The range of values is -π to π.
        values[1]: Pitch, angle of rotation about the x axis. This value represents the angle between a plane parallel to the device's screen and a plane parallel to the ground. Assuming that the bottom edge of the device faces the user and that the screen is face-up, tilting the top edge of the device toward the ground creates a positive pitch angle. The range of values is -π to π.
        values[2]: Roll, angle of rotation about the y axis. This value represents the angle between a plane perpendicular to the device's screen and a plane perpendicular to the ground. Assuming that the bottom edge of the device faces the user and that the screen is face-up, tilting the left edge of the device toward the ground creates a positive roll angle. The range of values is -π/2 to π/2.
        Applying these three rotations in the azimuth, pitch, roll order transforms an identity matrix to the rotation matrix passed into this method. Also, note that all three orientation angles are expressed in radians.
     */
}