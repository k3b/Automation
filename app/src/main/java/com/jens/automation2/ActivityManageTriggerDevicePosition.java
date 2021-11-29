package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.jens.automation2.receivers.DevicePositionListener;

import org.apache.commons.lang3.StringUtils;

public class ActivityManageTriggerDevicePosition extends Activity
{
    TextView currentAzimuth, currentPitch, currentRoll, tvAppliesAzimuth, tvAppliesPitch, tvAppliesRoll;
    Button bApplyPositionValues, bSavePositionValues;
    EditText etDesiredAzimuth, etDesiredAzimuthTolerance, etDesiredPitch, etDesiredPitchTolerance, etDesiredRoll, etDesiredRollTolerance;

    public static String vectorFieldName = "deviceVector";

    boolean editMode = false;

    float desiredAzimuth, desiredPitch, desiredRoll, desiredAzimuthTolerance, desiredPitchTolerance, desiredRollTolerance;

    public void updateFields(float azimuth, float pitch, float roll)
    {
        currentAzimuth.setText(Float.toString(azimuth));
        currentPitch.setText(Float.toString(pitch));
        currentRoll.setText(Float.toString(roll));

        if(checkInputs())
        {
            desiredAzimuth = Float.parseFloat(etDesiredAzimuth.getText().toString());
            desiredAzimuthTolerance = Float.parseFloat(etDesiredAzimuthTolerance.getText().toString());
            if(Math.abs(azimuth) <= Math.abs(desiredAzimuth - desiredAzimuthTolerance) || Math.abs(azimuth) <= desiredAzimuth + desiredAzimuthTolerance)
            {
                tvAppliesAzimuth.setText(getResources().getString(R.string.yes));
                tvAppliesAzimuth.setTextColor(Color.GREEN);
            }
            else
            {
                tvAppliesAzimuth.setText(getResources().getString(R.string.no));
                tvAppliesAzimuth.setTextColor(Color.RED);
            }

            desiredPitch = Float.parseFloat(etDesiredPitch.getText().toString());
            desiredPitchTolerance = Float.parseFloat(etDesiredPitchTolerance.getText().toString());
            if(Math.abs(pitch) <= Math.abs(desiredPitch - desiredPitchTolerance) || Math.abs(pitch) <= desiredPitch + desiredPitchTolerance)
            {
                tvAppliesPitch.setText(getResources().getString(R.string.yes));
                tvAppliesPitch.setTextColor(Color.GREEN);
            }
            else
            {
                tvAppliesPitch.setText(getResources().getString(R.string.no));
                tvAppliesPitch.setTextColor(Color.RED);
            }

            desiredRoll = Float.parseFloat(etDesiredRoll.getText().toString());
            desiredRollTolerance = Float.parseFloat(etDesiredRollTolerance.getText().toString());
            if(Math.abs(roll) <= Math.abs(desiredRoll - desiredRollTolerance) || Math.abs(roll) <= desiredRoll + desiredRollTolerance)
            {
                tvAppliesRoll.setText(getResources().getString(R.string.yes));
                tvAppliesRoll.setTextColor(Color.GREEN);
            }
            else
            {
                tvAppliesRoll.setText(getResources().getString(R.string.no));
                tvAppliesRoll.setTextColor(Color.RED);
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_trigger_device_position);

        currentAzimuth = (TextView) findViewById(R.id.tvCurrentAzimuth);
        currentPitch = (TextView) findViewById(R.id.tvCurrentOrientationPitch);
        currentRoll = (TextView) findViewById(R.id.tvCurrentRoll);
        tvAppliesAzimuth = (TextView) findViewById(R.id.tvAppliesAzimuth);
        tvAppliesPitch = (TextView) findViewById(R.id.tvAppliesPitch);
        tvAppliesRoll = (TextView) findViewById(R.id.tvAppliesRoll);

        bApplyPositionValues = (Button) findViewById(R.id.bApplyPositionValues);
        bSavePositionValues = (Button) findViewById(R.id.bSavePositionValues);

        etDesiredAzimuth = (EditText) findViewById(R.id.etDesiredAzimuth);
        etDesiredAzimuthTolerance = (EditText) findViewById(R.id.etDesiredAzimuthTolerance);
        etDesiredPitch = (EditText) findViewById(R.id.etDesiredPitch);
        etDesiredPitchTolerance = (EditText) findViewById(R.id.etDesiredPitchTolerance);
        etDesiredRoll = (EditText) findViewById(R.id.etDesiredRoll);
        etDesiredRollTolerance = (EditText) findViewById(R.id.etDesiredRollTolerance);

//        etDesiredAzimuth.setFilters(new InputFilter[]{new InputFilterMinMax(-180, 180)});
//        etDesiredPitch.setFilters(new InputFilter[]{new InputFilterMinMax(-180, 180)});
//        etDesiredRoll.setFilters(new InputFilter[]{new InputFilterMinMax(-180, 180)});
        etDesiredAzimuthTolerance.setFilters(new InputFilter[]{new InputFilterMinMax(0, 359)});
        etDesiredPitchTolerance.setFilters(new InputFilter[]{new InputFilterMinMax(0, 359)});
        etDesiredRollTolerance.setFilters(new InputFilter[]{new InputFilterMinMax(0, 359)});

        if(getIntent().hasExtra(vectorFieldName))
        {
            editMode = true;
            String values[] = getIntent().getStringExtra(vectorFieldName).split(Trigger.triggerParameter2Split);
            etDesiredAzimuth.setText(values[0]);
            etDesiredAzimuthTolerance.setText(values[1]);
            etDesiredPitch.setText(values[2]);
            etDesiredPitchTolerance.setText(values[3]);
            etDesiredRoll.setText(values[4]);
            etDesiredRollTolerance.setText(values[5]);
        }

        bApplyPositionValues.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!StringUtils.isEmpty(currentAzimuth.getText()))
                    etDesiredAzimuth.setText(currentAzimuth.getText());

                if(!StringUtils.isEmpty(currentPitch.getText()))
                    etDesiredPitch.setText(currentPitch.getText());

                if(!StringUtils.isEmpty(currentRoll.getText()))
                    etDesiredRoll.setText(currentRoll.getText());
            }
        });

        bSavePositionValues.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!checkInputs())
                {
                    Toast.makeText(ActivityManageTriggerDevicePosition.this, getResources().getString(R.string.enterValidNumbersIntoAllFields), Toast.LENGTH_LONG).show();
                }
                else
                {
                    // Save
                    Intent returnData = new Intent();
                    returnData.putExtra(vectorFieldName,
                                            etDesiredAzimuth.getText().toString() + Trigger.triggerParameter2Split +
                                                etDesiredAzimuthTolerance.getText().toString() + Trigger.triggerParameter2Split +
                                                etDesiredPitch.getText().toString() + Trigger.triggerParameter2Split +
                                                etDesiredPitchTolerance.getText().toString() + Trigger.triggerParameter2Split +
                                                etDesiredRoll.getText().toString() + Trigger.triggerParameter2Split +
                                                etDesiredRollTolerance.getText().toString());

                    setResult(RESULT_OK, returnData);
                    finish();
                }
            }
        });
    }

    boolean checkInputs()
    {
        if(
                !StringUtils.isEmpty(etDesiredAzimuth.getText().toString()) && Miscellaneous.isNumeric(etDesiredAzimuth.getText().toString())
                        &&
                !StringUtils.isEmpty(etDesiredAzimuthTolerance.getText().toString()) && Miscellaneous.isNumeric(etDesiredAzimuthTolerance.getText().toString())
                        &&
                !StringUtils.isEmpty(etDesiredPitch.getText().toString()) && Miscellaneous.isNumeric(etDesiredPitch.getText().toString())
                        &&
                !StringUtils.isEmpty(etDesiredPitchTolerance.getText().toString()) && Miscellaneous.isNumeric(etDesiredPitchTolerance.getText().toString())
                        &&
                !StringUtils.isEmpty(etDesiredRoll.getText().toString()) && Miscellaneous.isNumeric(etDesiredRoll.getText().toString())
                        &&
                !StringUtils.isEmpty(etDesiredRollTolerance.getText().toString()) && Miscellaneous.isNumeric(etDesiredRollTolerance.getText().toString())
        )
        {
            float da = Float.parseFloat(etDesiredAzimuth.getText().toString());
            float dp = Float.parseFloat(etDesiredPitch.getText().toString());
            float dr = Float.parseFloat(etDesiredRoll.getText().toString());

            if(Math.abs(da) <= 180 || Math.abs(dp) <= 180 || Math.abs(dr) <= 180)
                return true;
        }

        return false;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        DevicePositionListener.getInstance().startSensorFromConfigActivity(ActivityManageTriggerDevicePosition.this, this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        DevicePositionListener.getInstance().stopSensorFromConfigActivity();
    }

    public class InputFilterMinMax implements InputFilter
    {
        private float minimumValue;
        private float maximumValue;

        public InputFilterMinMax(float minimumValue, float maximumValue)
        {
            this.minimumValue = minimumValue;
            this.maximumValue = maximumValue;
        }

        private boolean isInRange(float a, float b, float c)
        {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
        {
            try
            {
                int input = Integer.parseInt(dest.subSequence(0, dstart).toString() + source + dest.subSequence(dend, dest.length()));
                if (isInRange(minimumValue, maximumValue, input))
                    return null;
            }
            catch (NumberFormatException nfe)
            {
            }
            return "";
        }
    }
}