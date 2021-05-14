package com.jens.automation2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import static com.jens.automation2.Trigger.triggerParameter2Split;

public class ActivityManageTriggerPhoneCall extends Activity
{
    public static Trigger editedPhoneCallTrigger;
    boolean edit = false;
    public static Trigger resultingTrigger;
    ProgressDialog progressDialog = null;

    EditText etTriggerPhoneCallPhoneNumber;
    RadioButton rbTriggerPhoneCallStateAny, rbTriggerPhoneCallStateRinging, rbTriggerPhoneCallStateStarted, rbTriggerPhoneCallStateStopped, rbTriggerPhoneCallDirectionAny, rbTriggerPhoneCallDirectionIncoming, rbTriggerPhoneCallDirectionOutgoing;
    Button bSaveTriggerPhoneCall, bTriggerPhoneCallImportFromContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_trigger_phone_call);

        etTriggerPhoneCallPhoneNumber = (EditText)findViewById(R.id.etTriggerPhoneCallPhoneNumber);
        rbTriggerPhoneCallStateAny = (RadioButton)findViewById(R.id.rbTriggerPhoneCallStateAny);
        rbTriggerPhoneCallStateRinging = (RadioButton)findViewById(R.id.rbTriggerPhoneCallStateRinging);
        rbTriggerPhoneCallStateStarted = (RadioButton)findViewById(R.id.rbTriggerPhoneCallStateStarted);
        rbTriggerPhoneCallStateStopped = (RadioButton)findViewById(R.id.rbTriggerPhoneCallStateStopped);
        rbTriggerPhoneCallDirectionAny = (RadioButton)findViewById(R.id.rbTriggerPhoneCallDirectionAny);
        rbTriggerPhoneCallDirectionIncoming = (RadioButton)findViewById(R.id.rbTriggerPhoneCallDirectionIncoming);
        rbTriggerPhoneCallDirectionOutgoing = (RadioButton)findViewById(R.id.rbTriggerPhoneCallDirectionOutgoing);
        bTriggerPhoneCallImportFromContacts = (Button) findViewById(R.id.bTriggerPhoneCallImportFromContacts);
        bSaveTriggerPhoneCall = (Button) findViewById(R.id.bSaveTriggerPhoneCall);

        bSaveTriggerPhoneCall.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String tp2Result = "";

                if(rbTriggerPhoneCallStateAny.isChecked())
                    tp2Result += Trigger.triggerPhoneCallStateAny;
                else if(rbTriggerPhoneCallStateRinging.isChecked())
                    tp2Result += Trigger.triggerPhoneCallStateRinging;
                else if(rbTriggerPhoneCallStateStarted.isChecked())
                    tp2Result += Trigger.triggerPhoneCallStateStarted;
                else if(rbTriggerPhoneCallStateStopped.isChecked())
                    tp2Result += Trigger.triggerPhoneCallStateStopped;

                tp2Result += triggerParameter2Split;

                if(rbTriggerPhoneCallDirectionAny.isChecked())
                    tp2Result += Trigger.triggerPhoneCallDirectionAny;
                else if(rbTriggerPhoneCallDirectionIncoming.isChecked())
                    tp2Result += Trigger.triggerPhoneCallDirectionIncoming;
                else if(rbTriggerPhoneCallDirectionOutgoing.isChecked())
                    tp2Result += Trigger.triggerPhoneCallDirectionOutgoing;

                tp2Result += triggerParameter2Split;

                if(etTriggerPhoneCallPhoneNumber.getText() != null && etTriggerPhoneCallPhoneNumber.getText().toString().length() > 0)
                    tp2Result += etTriggerPhoneCallPhoneNumber.getText().toString();
                else
                    tp2Result += Trigger.triggerPhoneCallNumberAny;

                if(edit)
                {
                    editedPhoneCallTrigger.setTriggerParameter(false);
                    editedPhoneCallTrigger.setTriggerParameter2(tp2Result);
                    ActivityManageTriggerPhoneCall.this.setResult(RESULT_OK);
                }
                else
                {
                    Intent data = new Intent();
                    data.putExtra("triggerParameter", false);
                    data.putExtra("triggerParameter2", tp2Result);
                    ActivityManageTriggerPhoneCall.this.setResult(RESULT_OK, data);
                }

                finish();
            }
        });

        Intent i = getIntent();
        if(i.getBooleanExtra("edit", false) == true)
        {
            edit = true;
            loadValuesIntoGui();
        }
    }

    private void loadValuesIntoGui()
    {
        String[] parts = editedPhoneCallTrigger.getTriggerParameter2().split(triggerParameter2Split);

        if(parts[0].equals(Trigger.triggerPhoneCallStateAny))
            rbTriggerPhoneCallStateAny.setChecked(true);
        else if(parts[0].equals(Trigger.triggerPhoneCallStateRinging))
            rbTriggerPhoneCallStateRinging.setChecked(true);
        else if(parts[0].equals(Trigger.triggerPhoneCallStateStarted))
            rbTriggerPhoneCallStateStarted.setChecked(true);
        else if(parts[0].equals(Trigger.triggerPhoneCallStateStopped))
            rbTriggerPhoneCallStateStopped.setChecked(true);

        if(parts[1].equals(Trigger.triggerPhoneCallDirectionAny))
            rbTriggerPhoneCallDirectionAny.setChecked(true);
        else if(parts[1].equals(Trigger.triggerPhoneCallDirectionIncoming))
            rbTriggerPhoneCallDirectionIncoming.setChecked(true);
        else if(parts[1].equals(Trigger.triggerPhoneCallDirectionOutgoing))
            rbTriggerPhoneCallDirectionOutgoing.setChecked(true);

        if(!parts[2].equals(Trigger.triggerPhoneCallNumberAny))
            etTriggerPhoneCallPhoneNumber.setText(parts[2]);
    }
}
