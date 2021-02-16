package com.jens.automation2.receivers;

import com.jens.automation2.AutomationService;
import com.jens.automation2.Trigger.Trigger_Enum;

public interface AutomationListenerInterface
{
	public void startListener(AutomationService automationService);
	public void stopListener(AutomationService automationService);
	public boolean isListenerRunning();
	public Trigger_Enum[] getMonitoredTrigger();	
}
