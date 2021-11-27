package com.jens.automation2;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
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
    TextView currentOrientationX, currentOrientationY, currentOrientationZ, tvAppliesX, tvAppliesY, tvAppliesZ;
    Button bApplyPositionValues, bSavePositionValues;
    EditText etDesiredPositionX, etDesiredPositionXTolerance, etDesiredPositionY, etDesiredPositionYTolerance, etDesiredPositionZ, etDesiredPositionZTolerance;

    float desiredX, desiredY, desiredZ, desiredXTolerance, desiredYTolerance, desiredZTolerance;

    public void updateFields(float x, float y, float z)
    {
        currentOrientationX.setText(Float.toString(x));
        currentOrientationY.setText(Float.toString(y));
        currentOrientationZ.setText(Float.toString(z));

        if(checkInputs())
        {
            desiredX = Float.parseFloat(etDesiredPositionX.getText().toString());
            desiredXTolerance = Float.parseFloat(etDesiredPositionXTolerance.getText().toString());
            if(x >= desiredX - desiredXTolerance || x <= desiredX + desiredXTolerance)
            {
                tvAppliesX.setText(getResources().getString(R.string.yes));
                tvAppliesX.setTextColor(Color.GREEN);
            }
            else
            {
                tvAppliesX.setText(getResources().getString(R.string.no));
                tvAppliesX.setTextColor(Color.RED);
            }

            desiredY = Float.parseFloat(etDesiredPositionY.getText().toString());
            desiredYTolerance = Float.parseFloat(etDesiredPositionYTolerance.getText().toString());
            if(y >= desiredY - desiredYTolerance || y <= desiredY + desiredYTolerance)
            {
                tvAppliesY.setText(getResources().getString(R.string.yes));
                tvAppliesY.setTextColor(Color.GREEN);
            }
            else
            {
                tvAppliesY.setText(getResources().getString(R.string.no));
                tvAppliesY.setTextColor(Color.RED);
            }

            desiredZ = Float.parseFloat(etDesiredPositionZ.getText().toString());
            desiredZTolerance = Float.parseFloat(etDesiredPositionZTolerance.getText().toString());
            if(z >= desiredZ - desiredZTolerance || z <= desiredZ + desiredZTolerance)
            {
                tvAppliesZ.setText(getResources().getString(R.string.yes));
                tvAppliesZ.setTextColor(Color.GREEN);
            }
            else
            {
                tvAppliesZ.setText(getResources().getString(R.string.no));
                tvAppliesZ.setTextColor(Color.RED);
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_trigger_device_position);

        currentOrientationX = (TextView) findViewById(R.id.currentOrientationX);
        currentOrientationY = (TextView) findViewById(R.id.currentOrientationY);
        currentOrientationZ = (TextView) findViewById(R.id.currentOrientationZ);
        tvAppliesX = (TextView) findViewById(R.id.tvAppliesX);
        tvAppliesY = (TextView) findViewById(R.id.tvAppliesY);
        tvAppliesZ = (TextView) findViewById(R.id.tvAppliesZ);

        bApplyPositionValues = (Button) findViewById(R.id.bApplyPositionValues);
        bSavePositionValues = (Button) findViewById(R.id.bSavePositionValues);

        etDesiredPositionX = (EditText) findViewById(R.id.etDesiredPositionX);
        etDesiredPositionXTolerance = (EditText) findViewById(R.id.etDesiredPositionXTolerance);
        etDesiredPositionY = (EditText) findViewById(R.id.etDesiredPositionY);
        etDesiredPositionYTolerance = (EditText) findViewById(R.id.etDesiredPositionYTolerance);
        etDesiredPositionZ = (EditText) findViewById(R.id.etDesiredPositionZ);
        etDesiredPositionZTolerance = (EditText) findViewById(R.id.etDesiredPositionZTolerance);

        bApplyPositionValues.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!StringUtils.isEmpty(currentOrientationX.getText()))
                    etDesiredPositionX.setText(currentOrientationX.getText());

                if(!StringUtils.isEmpty(currentOrientationY.getText()))
                    etDesiredPositionY.setText(currentOrientationY.getText());

                if(!StringUtils.isEmpty(currentOrientationZ.getText()))
                    etDesiredPositionZ.setText(currentOrientationZ.getText());
            }
        });

        bSavePositionValues.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(checkInputs())
                {
                    Toast.makeText(ActivityManageTriggerDevicePosition.this, getResources().getString(R.string.enterValidNumbersIntoAllFields), Toast.LENGTH_LONG).show();
                }
                else
                {
                    // Save
                }
            }
        });
    }

    boolean checkInputs()
    {
        return(
                !StringUtils.isEmpty(etDesiredPositionX.getText().toString()) && Miscellaneous.isNumeric(etDesiredPositionX.getText().toString())
                        &&
                !StringUtils.isEmpty(etDesiredPositionXTolerance.getText().toString()) && Miscellaneous.isNumeric(etDesiredPositionXTolerance.getText().toString())
                        &&
                !StringUtils.isEmpty(etDesiredPositionY.getText().toString()) && Miscellaneous.isNumeric(etDesiredPositionY.getText().toString())
                        &&
                !StringUtils.isEmpty(etDesiredPositionYTolerance.getText().toString()) && Miscellaneous.isNumeric(etDesiredPositionYTolerance.getText().toString())
                        &&
                !StringUtils.isEmpty(etDesiredPositionZ.getText().toString()) && Miscellaneous.isNumeric(etDesiredPositionZ.getText().toString())
                        &&
                !StringUtils.isEmpty(etDesiredPositionZTolerance.getText().toString()) && Miscellaneous.isNumeric(etDesiredPositionZTolerance.getText().toString())
        );
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        DevicePositionListener.getInstance().startSensor(ActivityManageTriggerDevicePosition.this, this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        DevicePositionListener.getInstance().stopSensor();
    }
}
