package com.jens.automation2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ActivityVolumeTest extends Activity
{
	TextView tvCurrentVolume, tvVolumeTestExplanation;
	EditText etReferenceValue;
	SeekBar sbReferenceValue;
	
	final int volumeRefreshInterval = 3;
	
	static ActivityVolumeTest instance = null;
	
	AsyncTask<Integer, Long, Void> volumeTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		instance = this;
		
		super.onCreate(savedInstanceState);
		Miscellaneous.setDisplayLanguage(this);
		setContentView(R.layout.activity_volume_calibration);
		
		tvCurrentVolume = (TextView)findViewById(R.id.tvCurrentVolume);
		etReferenceValue = (EditText)findViewById(R.id.etReferenceValue);
		sbReferenceValue = (SeekBar)findViewById(R.id.sbReferenceValue);
		tvVolumeTestExplanation = (TextView)findViewById(R.id.tvVolumeCalibrationExplanation);
		
		tvVolumeTestExplanation.setText(String.format(getResources().getString(R.string.volumeCalibrationExplanation), String.valueOf(volumeRefreshInterval)));
		
		etReferenceValue.setText(String.valueOf(Settings.referenceValueForNoiseLevelMeasurements));
		
		sbReferenceValue.setMax(500);
		sbReferenceValue.setProgress((int) Settings.referenceValueForNoiseLevelMeasurements);
		sbReferenceValue.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
				etReferenceValue.setText(String.valueOf(sbReferenceValue.getProgress()));
			}
		});
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		startVolumeMonitoring();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		stopVolumeMonitoring();
	}

	public static ActivityVolumeTest getInstance()
	{
		return instance;
	}

	@Override
	public void onBackPressed()
	{
		try
		{
			stopVolumeMonitoring();
			Settings.referenceValueForNoiseLevelMeasurements = Long.parseLong(etReferenceValue.getText().toString());
			Settings.writeSettings(ActivityVolumeTest.this);
			super.onBackPressed();
		}
		catch (Exception e)
		{
			Toast.makeText(ActivityVolumeTest.this, ActivityVolumeTest.this.getResources().getString(R.string.enterValidReferenceValue), Toast.LENGTH_LONG).show();
		}
	}

	synchronized private void startVolumeMonitoring()
	{
		volumeTask = new NoiseListenerMeasuring();
			
//		if(!(volumeTask.getStatus() == AsyncTask.Status.PENDING | volumeTask.getStatus() != AsyncTask.Status.RUNNING))
			volumeTask.execute(volumeRefreshInterval);
	}

	synchronized private void stopVolumeMonitoring()
	{
//		if(volumeTask != null && (volumeTask.getStatus() == Status.PENDING | volumeTask.getStatus() == Status.RUNNING))
			volumeTask.cancel(true);
	}

	synchronized void updateDisplayedNoiseLevel(long noise)
	{
		tvCurrentVolume.setText(String.valueOf(noise) + " dB");
	}

	private static class NoiseListenerMeasuring extends AsyncTask<Integer, Long, Void>
	{
		static boolean isNoiseMonitoringActive;

		@Override
		protected Void doInBackground(Integer... interval)
		{			
			if(!isNoiseMonitoringActive)
			{			
				isNoiseMonitoringActive = true;
				
				Miscellaneous.logEvent("i", "Noise level", "Periodic noise level measurement started.", 5);
				
				while(!isCancelled())
				{
					try
					{
					// Start recording but don't store data
						MediaRecorder mediaRecorder = new MediaRecorder();
						mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
						mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
						mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
						mediaRecorder.setOutputFile("/dev/null");
			//			Date myDate = new Date();
			//			mediaRecorder.setOutputFile("/sdcard/temp/" + String.valueOf(myDate.getTime()) + ".3gpp");
						
						mediaRecorder.prepare();
					    mediaRecorder.getMaxAmplitude();
						mediaRecorder.start();
					    mediaRecorder.getMaxAmplitude();
						
						long noiseLevel;
		
						try
						{		    
							Thread.sleep(interval[0] * 1000);
							// Obtain maximum amplitude since last call of getMaxAmplitude()
								noiseLevel = mediaRecorder.getMaxAmplitude();
						}
						catch(Exception e)
						{
							noiseLevel = -1;
							Miscellaneous.logEvent("e", "Noise level", "Error getting noise level: " + e.getMessage(), 2);
						}
						
						double currentReferenceValue = Double.parseDouble(ActivityVolumeTest.getInstance().etReferenceValue.getText().toString());
						double db = 20 * Math.log(noiseLevel / currentReferenceValue);
						long noiseLevelDb = Math.round(db);
						
						publishProgress(noiseLevelDb);
						
	//					Message answer = new Message();
	//					Bundle answerBundle = new Bundle();
	//					answerBundle.putLong("noiseLevelDb", noiseLevelDb);
	//					answer.setData(answerBundle);
	//					volumeHandler.sendMessage(answer);
						
						// Don't forget to release
						mediaRecorder.reset();
						mediaRecorder.release();
						
						Miscellaneous.logEvent("i", "Noise level", "Measured noise level: " + String.valueOf(noiseLevel) + " / converted to db: " + String.valueOf(db), 3);
					}
					catch(Exception e)
					{}
				}				
				
				isNoiseMonitoringActive = false;
				
				Miscellaneous.logEvent("i", "Noise level", "Periodic noise level measurement stopped.", 5);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Long... values)
		{
			ActivityVolumeTest.getInstance().updateDisplayedNoiseLevel(values[0]);
			
//			super.onProgressUpdate(values);
		}
		
		
	}
}
