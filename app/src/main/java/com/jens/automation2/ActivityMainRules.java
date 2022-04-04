package com.jens.automation2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jens.automation2.AutomationService.serviceCommands;
import com.jens.automation2.receivers.DateTimeListener;

import java.util.ArrayList;

public class ActivityMainRules extends ActivityGeneric
{
	public static final String intentNameRuleName = "ruleName";
	private ListView ruleListView;
	ArrayList<Rule> ruleList = new ArrayList<>();
	private ArrayAdapter<Rule> ruleListViewAdapter;
	public static Rule ruleToEdit;
	protected static ActivityMainRules instance = null;

	public static final int requestCodeCreateRule = 3000;
	public static final int requestCodeChangeRule = 4000;

	public static ActivityMainRules getInstance()
	{
		if(instance == null)
			instance = new ActivityMainRules();

		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.main_rule_layout);

		instance = this;

		Button bAddRule = (Button)findViewById(R.id.bAddRule);
		bAddRule.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				ruleToEdit = null;
				Intent startAddRuleIntent = new Intent(ActivityMainRules.this, ActivityManageRule.class);
				startActivityForResult(startAddRuleIntent, requestCodeCreateRule);
			}
		});

		ruleListViewAdapter = new RuleArrayAdapter(this, R.layout.view_for_rule_listview, ruleList);
		ruleListView = (ListView)findViewById(R.id.lvRuleList);
		ruleListView.setClickable(true);

		ruleListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				getRuleDialog((Rule)ruleListView.getItemAtPosition(arg2)).show();
				return false;
			}
		});

		if(Settings.executeRulesAndProfilesWithSingleClick)
		{
			ruleListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					if(AutomationService.isMyServiceRunning(ActivityMainRules.this))
					{
						AutomationService runContext = AutomationService.getInstance();
						if(runContext != null)
						{
							Rule rule = (Rule)ruleListView.getItemAtPosition(position);
							rule.activate(runContext, true);
						}
					}
				}
			});
		}

		updateListView();
		
		this.storeServiceReferenceInVariable();
	}
	
	private static class RuleHolder
	{
		public ImageView ivActiveInactive;
		public TextView tvRuleName;
	}
	
	private static class RuleArrayAdapter extends ArrayAdapter<Rule>
	{
		public RuleArrayAdapter(Context context, int resource, ArrayList<Rule> objects)
		{
			super(context, resource, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{	
		    View v = convertView;
		    RuleHolder holder = new RuleHolder();
		    // First let's verify the convertView is not null
		    if (convertView == null)
		    {
		        // This a new view we inflate the new layout
		        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		        v = inflater.inflate(R.layout.view_for_rule_listview, null);
		        // Now we can fill the layout with the right values
		        TextView tv = (TextView) v.findViewById(R.id.tvRuleName);
		        ImageView img = (ImageView) v.findViewById(R.id.ivActiveInactive);
		        holder.tvRuleName = tv;
		        holder.ivActiveInactive = img;
		        v.setTag(holder);
		    }
		    else
		        holder = (RuleHolder) v.getTag();
		 
//		    System.out.println("Position ["+position+"]");
		    Rule r = Rule.getRuleCollection().get(position);
		    holder.tvRuleName.setText(r.getName());	 
		    if(r.isRuleActive())
			{
				if (r.haveEnoughPermissions())
					holder.ivActiveInactive.setImageResource(R.drawable.status_active);
				else
					holder.ivActiveInactive.setImageResource(R.drawable.status_unable);
			}
			else
				holder.ivActiveInactive.setImageResource(R.drawable.status_inactive);

		    return v;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);	
		if(AutomationService.isMyServiceRunning(this))
			bindToService();
		
		if(requestCode == requestCodeCreateRule) //add Rule
		{
			ruleToEdit = null; //clear cache
			updateListView();
		}
		
		if(requestCode == requestCodeChangeRule) //editRule
		{
			ruleToEdit = null; //clear cache
			updateListView();
		}
		
		AutomationService service = AutomationService.getInstance();
		if(service != null)
			service.applySettingsAndRules();
		
		if(boundToService && AutomationService.isMyServiceRunning(this))
		{
			myAutomationService.serviceInterface(serviceCommands.updateNotification); //in case names got changed.
			unBindFromService();
		}
	}
	
	private AlertDialog getRuleDialog(final Rule ruleThisIsAbout)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getResources().getString(R.string.whatToDoWithRule));
		alertDialogBuilder.setItems(new String[]{ getResources().getString(R.string.runManually), getResources().getString(R.string.edit), getResources().getString(R.string.deleteCapital), getResources().getString(R.string.clone) }, new DialogInterface.OnClickListener()
		{			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch(which)
				{
					case 0:
						if(AutomationService.isMyServiceRunning(ActivityMainRules.this))
						{
							AutomationService runContext = AutomationService.getInstance();
							if(runContext != null)
							{
								Miscellaneous.logEvent("i", "ActivityMainRules", "Initiating manual execution of rule " + ruleThisIsAbout.getName(), 3);
								ruleThisIsAbout.activate(runContext, true);
								break;
							}
						}
						Toast.makeText(ActivityMainRules.this, getResources().getString(R.string.serviceHasToRunForThat), Toast.LENGTH_LONG).show();
						break;
					case 1:
						Intent manageSpecificRuleIntent = new Intent (ActivityMainRules.this, ActivityManageRule.class);
						manageSpecificRuleIntent.putExtra(intentNameRuleName, ruleThisIsAbout.getName());
						startActivityForResult(manageSpecificRuleIntent, requestCodeChangeRule);
						break;
					case 2:
						if(ruleThisIsAbout.delete())
						{
							ruleToEdit = null; //clear cache
							updateListView();
						}
						break;
					case 3:
						ruleToEdit = ruleThisIsAbout;
						if(ruleToEdit.cloneRule(ActivityMainRules.this))
						{
							ruleToEdit = null; //clear cache
							updateListView();
						}
						break;
				}
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		
		return alertDialog;
	}
	
	public void updateListView()
	{
		Miscellaneous.logEvent("i", "ListView", "Attempting to update RuleListView", 4);

		ruleList.clear();
		for(Rule r : Rule.getRuleCollection())
			ruleList.add(r);

		try
		{
			if(ruleListView.getAdapter() == null)
				ruleListView.setAdapter(ruleListViewAdapter);
			
			ruleListViewAdapter.notifyDataSetChanged();
		}
		catch(NullPointerException e)
		{}
		
		try
		{
			if(AutomationService.isMyServiceRunning(this))
				DateTimeListener.reloadAlarms();
		}
		catch(NullPointerException e)
		{
			// AlarmManager instance not prepared, yet.
		}
	}
}