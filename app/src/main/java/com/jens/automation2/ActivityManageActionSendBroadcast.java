package com.jens.automation2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ActivityManageActionSendBroadcast extends Activity
{
    EditText etBroadcastToSend;
    Button bBroadcastSendShowSuggestions, bSaveSendBroadcast, bAddIntentPair;
    ListView lvIntentPairs;
    EditText etParameterName, etParameterValue;
    Spinner spinnerParameterType;
    ArrayAdapter<String> intentTypeSpinnerAdapter, intentPairAdapter;
    private static final String[] supportedIntentTypes = { "boolean", "byte", "char", "double", "float", "int", "long", "short", "String", "Uri" };
    private ArrayList<String> intentPairList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_action_send_broadcast);

        etBroadcastToSend = (EditText)findViewById(R.id.etBroadcastToSend);
        bBroadcastSendShowSuggestions = (Button)findViewById(R.id.bBroadcastSendShowSuggestions);
        bSaveSendBroadcast = (Button)findViewById(R.id.bSaveSendBroadcast);
        bAddIntentPair = (Button)findViewById(R.id.bAddIntentPair);
        lvIntentPairs = (ListView) findViewById(R.id.lvIntentPairs);
        etParameterName = (EditText) findViewById(R.id.etParameterName);
        etParameterValue = (EditText) findViewById(R.id.etParameterValue);
        spinnerParameterType = (Spinner) findViewById(R.id.spinnerParameterType);

        intentTypeSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.text_view_for_poi_listview_mediumtextsize, ActivityManageActionSendBroadcast.supportedIntentTypes);
        spinnerParameterType.setAdapter(intentTypeSpinnerAdapter);
        intentTypeSpinnerAdapter.notifyDataSetChanged();
        intentPairAdapter = new ArrayAdapter<String>(this, R.layout.text_view_for_poi_listview_mediumtextsize, intentPairList);

        bSaveSendBroadcast.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(checkInput())
                {
                    Intent answer = new Intent();

                    String param2 = etBroadcastToSend.getText().toString();

                    if(intentPairList.size() > 0)
                    {
                        param2 += Action.actionParameter2Split;

                        for (String s : intentPairList)
                            param2 += s + ";";

                        param2 = param2.substring(0, param2.length() - 1);
                    }

                    answer.putExtra(ActivityManageRule.intentNameActionParameter2, param2);
                    setResult(RESULT_OK, answer);
                    finish();
                }
            }
        });

        bBroadcastSendShowSuggestions.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityManageActionSendBroadcast.this);
                builder.setTitle(getResources().getString(R.string.selectBroadcast));
                builder.setItems(ActivityManageTriggerBroadcast.broadcastSuggestions, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which)
                    {
                        etBroadcastToSend.setText(ActivityManageTriggerBroadcast.broadcastSuggestions[which]);
                    }
                });
                builder.create().show();
            }
        });

        Intent input = getIntent();

        if(input.hasExtra(ActivityManageRule.intentNameActionParameter2))
        {
            String param2 = input.getStringExtra(ActivityManageRule.intentNameActionParameter2);
            if(!param2.contains(Action.actionParameter2Split))
                etBroadcastToSend.setText(input.getStringExtra(ActivityManageRule.intentNameActionParameter2));
            else
            {
                String[] param2Parts = param2.split(Action.actionParameter2Split);
                etBroadcastToSend.setText(param2Parts[0]);

                String[] params = param2Parts[1].split(";");

                intentPairList.clear();

                for(int i = 0; i < params.length; i++)
                {
                    if(lvIntentPairs.getVisibility() != View.VISIBLE)
                        lvIntentPairs.setVisibility(View.VISIBLE);

                    intentPairList.add(params[i]);
                }

                updateIntentPairList();
            }
        }

        bAddIntentPair.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // type;name;value
                if(spinnerParameterType.getSelectedItem().toString().length() == 0)
                {
                    Toast.makeText(ActivityManageActionSendBroadcast.this, getResources().getString(R.string.selectTypeOfIntentPair), Toast.LENGTH_LONG).show();
                    return;
                }

                if(etParameterName.getText().toString().length() == 0)
                {
                    Toast.makeText(ActivityManageActionSendBroadcast.this, getResources().getString(R.string.enterNameForIntentPair), Toast.LENGTH_LONG).show();
                    return;
                }
                else if(etParameterName.getText().toString().contains(Action.intentPairSeparator))
                {
                    Toast.makeText(ActivityManageActionSendBroadcast.this, String.format(getResources().getString(R.string.stringNotAllowed), Action.intentPairSeparator), Toast.LENGTH_LONG).show();
                    return;
                }
                else if(etParameterName.getText().toString().contains(";"))
                {
                    Toast.makeText(ActivityManageActionSendBroadcast.this, String.format(getResources().getString(R.string.stringNotAllowed), ";"), Toast.LENGTH_LONG).show();
                    return;
                }

                if(etParameterValue.getText().toString().length() == 0)
                {
                    Toast.makeText(ActivityManageActionSendBroadcast.this, getResources().getString(R.string.enterValueForIntentPair), Toast.LENGTH_LONG).show();
                    return;
                }
                else if(etParameterValue.getText().toString().contains(Action.intentPairSeparator))
                {
                    Toast.makeText(ActivityManageActionSendBroadcast.this, String.format(getResources().getString(R.string.stringNotAllowed), Action.intentPairSeparator), Toast.LENGTH_LONG).show();
                    return;
                }
                else if(etParameterValue.getText().toString().contains(";"))
                {
                    Toast.makeText(ActivityManageActionSendBroadcast.this, String.format(getResources().getString(R.string.stringNotAllowed), ";"), Toast.LENGTH_LONG).show();
                    return;
                }

                switch(supportedIntentTypes[spinnerParameterType.getSelectedItemPosition()])
                {
                    case "int":
                    case "long":
                    case "short":
                        if(!Miscellaneous.isNumeric(etParameterValue.getText().toString()))
                        {
                            Toast.makeText(ActivityManageActionSendBroadcast.this, getResources().getString(R.string.enter_a_number), Toast.LENGTH_LONG).show();
                            return;
                        }
                        break;
                    case "double":
                    case "float":
                        if(!Miscellaneous.isNumericDecimal(etParameterValue.getText().toString()))
                        {
                            Toast.makeText(ActivityManageActionSendBroadcast.this, getResources().getString(R.string.enter_a_number), Toast.LENGTH_LONG).show();
                            return;
                        }
                        break;
                    default:
                        ActivityManageActionSendBroadcast.this.etParameterValue.setInputType(InputType.TYPE_CLASS_TEXT);
                }

                String param = supportedIntentTypes[spinnerParameterType.getSelectedItemPosition()] + Action.intentPairSeparator + etParameterName.getText().toString() + Action.intentPairSeparator + etParameterValue.getText().toString();
                intentPairList.add(param);

                spinnerParameterType.setSelection(0);
                etParameterName.setText("");
                etParameterValue.setText("");

                updateIntentPairList();

                if(lvIntentPairs.getVisibility() != View.VISIBLE)
                    lvIntentPairs.setVisibility(View.VISIBLE);
            }
        });

        lvIntentPairs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                getIntentPairDialog(arg2).show();
                return false;
            }
        });

        spinnerParameterType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                if(supportedIntentTypes[arg2].equals("int") || supportedIntentTypes[arg2].equals("long") || supportedIntentTypes[arg2].equals("short"))
                    ActivityManageActionSendBroadcast.this.etParameterValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                else if(supportedIntentTypes[arg2].equals("double") || supportedIntentTypes[arg2].equals("float"))
                    ActivityManageActionSendBroadcast.this.etParameterValue.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                else
                    ActivityManageActionSendBroadcast.this.etParameterValue.setInputType(InputType.TYPE_CLASS_TEXT);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // TODO Auto-generated method stub

            }
        });
    }

    boolean checkInput()
    {
        String broadcastToSend = etBroadcastToSend.getText().toString();
        if(StringUtils.isEmpty(broadcastToSend))
        {
            Toast.makeText(ActivityManageActionSendBroadcast.this, getResources().getString(R.string.enterBroadcast), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateIntentPairList()
    {
        if(lvIntentPairs.getAdapter() == null)
            lvIntentPairs.setAdapter(intentPairAdapter);

        intentPairAdapter.notifyDataSetChanged();
    }

    private AlertDialog getIntentPairDialog(final int itemPosition)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityManageActionSendBroadcast.this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.whatToDoWithIntentPair));
        alertDialogBuilder.setItems(new String[]{getResources().getString(R.string.delete)}, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Only 1 choice at the moment, no need to check
                ActivityManageActionSendBroadcast.this.intentPairList.remove(itemPosition);
                updateIntentPairList();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        return alertDialog;
    }
}