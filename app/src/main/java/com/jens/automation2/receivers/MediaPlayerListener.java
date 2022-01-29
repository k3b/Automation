package com.jens.automation2.receivers;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.jens.automation2.AutomationService;
import com.jens.automation2.Miscellaneous;
import com.jens.automation2.Rule;
import com.jens.automation2.Settings;
import com.jens.automation2.Trigger;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MediaPlayerListener implements AutomationListenerInterface
{
    static MediaPlayerListener instance = null;
    static AudioManager mAudioManager = null;
    static boolean listenerActive = false;
    Timer timer = null;
    TimerTask task = null;

    public static boolean isAudioPlaying(Context context)
    {
        if(mAudioManager == null)
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        return mAudioManager.isMusicActive();
    }

    public static MediaPlayerListener getInstance()
    {
        if(instance == null)
            instance = new MediaPlayerListener();

        return instance;
    }

    @Override
    public void startListener(AutomationService automationService)
    {
        Miscellaneous.logEvent("i", "MediaPlayerListener", "Starting listener.",5);

        if(!listenerActive)
        {
            if(timer == null)
            {
                timer = new Timer();
            }
            else
            {
                timer.cancel();
                timer.purge();
            }

            task = new TimerTask()
            {
                @Override
                public void run()
                {
                    ArrayList<Rule> ruleCandidates = Rule.findRuleCandidates(Trigger.Trigger_Enum.musicPlaying);
                    for(int i=0; i<ruleCandidates.size(); i++)
                    {
                        if(ruleCandidates.get(i).getsGreenLight(AutomationService.getInstance()))
                            ruleCandidates.get(i).activate(AutomationService.getInstance(), false);
                    }
                }
            };

            timer.scheduleAtFixedRate(task, 0, Settings.musicCheckFrequency * 1000);
        }
    }

    @Override
    public void stopListener(AutomationService automationService)
    {
        Miscellaneous.logEvent("i", "MediaPlayerListener", "Stopping listener.",5);

        if(listenerActive)
        {
            if (timer != null)
            {
                timer.cancel();
                timer.purge();
            }
        }
    }

    @Override
    public boolean isListenerRunning()
    {
        return listenerActive;
    }

    @Override
    public Trigger.Trigger_Enum[] getMonitoredTrigger()
    {
        return new Trigger.Trigger_Enum[] { Trigger.Trigger_Enum.musicPlaying };
    }
}