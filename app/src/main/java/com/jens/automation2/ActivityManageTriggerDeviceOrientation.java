package com.jens.automation2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.jens.automation2.receivers.DeviceOrientationListener;

import org.apache.commons.lang3.StringUtils;

public class ActivityManageTriggerDeviceOrientation extends Activity
{
    TextView currentAzimuth, currentPitch, currentRoll, tvAppliesAzimuth, tvAppliesPitch, tvAppliesRoll;
    Button bApplyPositionValues, bSavePositionValues;
    EditText etDesiredAzimuth, etDesiredAzimuthTolerance, etDesiredPitch, etDesiredPitchTolerance, etDesiredRoll, etDesiredRollTolerance;
    CheckBox chkDevicePositionApplies;

    public static String vectorFieldName = "deviceVector";

    boolean editMode = false;

    float desiredAzimuth, desiredPitch, desiredRoll, desiredAzimuthTolerance, desiredPitchTolerance, desiredRollTolerance;

    public void updateFields(float azimuth, float pitch, float roll)
    {
        currentAzimuth.setText(Float.toString(azimuth));
        currentPitch.setText(Float.toString(pitch));
        currentRoll.setText(Float.toString(roll));

        try
        {
            desiredAzimuth = Float.parseFloat(etDesiredAzimuth.getText().toString());
            desiredAzimuthTolerance = Float.parseFloat(etDesiredAzimuthTolerance.getText().toString());
            if (desiredAzimuthTolerance == 180 || (desiredAzimuth - desiredAzimuthTolerance <= azimuth && azimuth <= desiredAzimuth + desiredAzimuthTolerance))
            {
                tvAppliesAzimuth.setText(getResources().getString(R.string.yes));
                tvAppliesAzimuth.setTextColor(Color.GREEN);
            }
            else
            {
                tvAppliesAzimuth.setText(getResources().getString(R.string.no));
                tvAppliesAzimuth.setTextColor(Color.RED);
            }
        }
        catch(Exception e)
        {
            tvAppliesAzimuth.setText("");
        }

        try
        {
            desiredPitch = Float.parseFloat(etDesiredPitch.getText().toString());
            desiredPitchTolerance = Float.parseFloat(etDesiredPitchTolerance.getText().toString());
            if (desiredPitchTolerance == 180 || (desiredPitch - desiredPitchTolerance <= pitch && pitch <= desiredPitch + desiredPitchTolerance))
            {
                tvAppliesPitch.setText(getResources().getString(R.string.yes));
                tvAppliesPitch.setTextColor(Color.GREEN);
            }
            else
            {
                tvAppliesPitch.setText(getResources().getString(R.string.no));
                tvAppliesPitch.setTextColor(Color.RED);
            }
        }
        catch(Exception e)
        {
            tvAppliesPitch.setText("");
        }

        try
        {
            desiredRoll = Float.parseFloat(etDesiredRoll.getText().toString());
            desiredRollTolerance = Float.parseFloat(etDesiredRollTolerance.getText().toString());
            if (desiredRollTolerance == 180 || (desiredRoll - desiredRollTolerance <= roll && roll <= desiredRoll + desiredRollTolerance))
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
        catch(Exception e)
        {
            tvAppliesRoll.setText("");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Miscellaneous.setDisplayLanguage(this);
        setContentView(R.layout.activity_manage_trigger_device_orientation);

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

        chkDevicePositionApplies = (CheckBox)findViewById(R.id.chkDevicePositionApplies);

//        etDesiredAzimuth.setFilters(new InputFilter[]{new InputFilterMinMax(-180, 180)});
//        etDesiredPitch.setFilters(new InputFilter[]{new InputFilterMinMax(-180, 180)});
//        etDesiredRoll.setFilters(new InputFilter[]{new InputFilterMinMax(-180, 180)});
        etDesiredAzimuthTolerance.setFilters(new InputFilter[]{new InputFilterMinMax(0, 180)});
        etDesiredPitchTolerance.setFilters(new InputFilter[]{new InputFilterMinMax(0, 180)});
        etDesiredRollTolerance.setFilters(new InputFilter[]{new InputFilterMinMax(0, 180)});

        if(getIntent().hasExtra(vectorFieldName))
        {
            editMode = true;
            try
            {
                boolean chkValue = getIntent().getBooleanExtra(ActivityManageRule.intentNameTriggerParameter1, true);
                chkDevicePositionApplies.setChecked(chkValue);
                String values[] = getIntent().getStringExtra(vectorFieldName).split(Trigger.triggerParameter2Split);
                etDesiredAzimuth.setText(values[0]);
                etDesiredAzimuthTolerance.setText(values[1]);
                etDesiredPitch.setText(values[2]);
                etDesiredPitchTolerance.setText(values[3]);
                etDesiredRoll.setText(values[4]);
                etDesiredRollTolerance.setText(values[5]);
            }
            catch(Exception e)
            {
                Toast.makeText(ActivityManageTriggerDeviceOrientation.this, getResources().getString(R.string.triggerWrong), Toast.LENGTH_SHORT).show();
                Miscellaneous.logEvent("e", "DevicePositionTrigger", "There\'s something wrong with a device position trigger. Content: " + getIntent().getStringExtra(vectorFieldName) + ", " + Log.getStackTraceString(e), 1);
            }
        }

        bApplyPositionValues.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Round the values. Too long decimals will destroy the layout

                if(!StringUtils.isEmpty(currentAzimuth.getText()))
                    etDesiredAzimuth.setText(String.valueOf(Math.round(Float.parseFloat(currentAzimuth.getText().toString()))));

                if(!StringUtils.isEmpty(currentPitch.getText()))
                    etDesiredPitch.setText(String.valueOf(Math.round(Float.parseFloat(currentPitch.getText().toString()))));

                if(!StringUtils.isEmpty(currentRoll.getText()))
                    etDesiredRoll.setText(String.valueOf(Math.round(Float.parseFloat(currentRoll.getText().toString()))));
            }
        });

        bSavePositionValues.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!checkInputs(true))
                {
                    Toast.makeText(ActivityManageTriggerDeviceOrientation.this, getResources().getString(R.string.enterValidNumbersIntoAllFields), Toast.LENGTH_LONG).show();
                }
                else
                {
                    // Save
                    Intent returnData = new Intent();
                    returnData.putExtra(ActivityManageRule.intentNameTriggerParameter1, chkDevicePositionApplies.isChecked());
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

    boolean checkInputs(boolean showMessages)
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

            if(Math.abs(da) > 180 || Math.abs(dp) > 180 || Math.abs(dr) > 180)
            {
                return false;
            }

            if(showMessages)
            {
                float dat = Float.parseFloat(etDesiredAzimuthTolerance.getText().toString());
                float dpt = Float.parseFloat(etDesiredPitchTolerance.getText().toString());
                float drt = Float.parseFloat(etDesiredRollTolerance.getText().toString());

            /*
                The user may enter a tolerance of 180° for two directions, but not all three.
                Otherwise this trigger would always apply.
             */
                if (Math.abs(dat) >= 180 && Math.abs(dpt) >= 180 && Math.abs(drt) >= 180)
                {
                    Miscellaneous.messageBox(getResources().getString(R.string.warning), getResources().getString(R.string.toleranceOf180OnlyAllowedIn2Fields), ActivityManageTriggerDeviceOrientation.this).show();
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        DeviceOrientationListener.getInstance().startSensorFromConfigActivity(ActivityManageTriggerDeviceOrientation.this, this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        DeviceOrientationListener.getInstance().stopSensorFromConfigActivity();
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