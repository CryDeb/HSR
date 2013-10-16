package dw.HSR.metaldetector;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainMetalDetectorActivity extends Activity implements SensorEventListener {

	private Sensor magneticField = null;
	private SensorManager sensorManager = null; 
	private Vibrator vibratorService = null;
	
	@SuppressWarnings("unused")
	private ToneGenerator toneGenerator = null;
	@SuppressWarnings("unused")
	private Thread thread = null;
	@SuppressWarnings("unused")
	private int soundIntervall = 1000;
	
	private boolean vibratorOnOff = false;
	private static int SENSOR_SPEED = SensorManager.SENSOR_DELAY_FASTEST;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_metal_detector);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_metal_detector, menu);
		createSensor();
		// Inflate the menu; this adds items to the action bar if it is present.
		
		
		MenuItem menuItem = menu.add("Log");
		menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
		 
		@Override
		public boolean onMenuItemClick(MenuItem item) {
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, SCAN_QR_CODE_REQUEST_CODE);
		return false;
		}
		});
		 
		return super.onCreateOptionsMenu(menu);
	}
	private void createSensor(){
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		vibratorService = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		List<Sensor> sensoren = sensorManager.getSensorList(Sensor.TYPE_ALL);
		for(Sensor s : sensoren){
			switch(s.getType()){
				case Sensor.TYPE_MAGNETIC_FIELD:
					magneticField = s;
					break;
				default:
					break;
			}
		}
		
		if(magneticField != null){
			sensorManager.registerListener(this, magneticField, SENSOR_SPEED);
		}
	}
	
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor == magneticField){			
			float[] werte = event.values.clone();
			ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar1);
			TextView tv = (TextView) findViewById(R.id.textView1);
			double output = Math.sqrt((werte[0] * werte[0]) + (werte[1] * werte[1]) + (werte[2] * werte[2]));
			pb.setProgress((int) output);
			tv.setText(Math.round(output) + " uT");
			if(output>200 && vibratorService.hasVibrator()){
				if(!vibratorOnOff){
					long[] pattern = { 0, 5, 1};
					try{
						vibratorService.vibrate(pattern, 0);
						vibratorOnOff = true;
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}else {
				try{
					vibratorService.cancel();
					vibratorOnOff=false;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			try{
				soundIntervall = (int) (50000 / output);				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void onResume(){
		super.onResume();
		if(magneticField != null){
			sensorManager.registerListener(this, magneticField, SENSOR_SPEED);			
		}
		//thread.start();
	}
	public void onPause(){
		super.onPause();
		sensorManager.unregisterListener(this);
		/*if(thread.isAlive())
			thread = null;*/
		vibratorOnOff=false;
	}
	private static final int SCAN_QR_CODE_REQUEST_CODE = 0;
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == SCAN_QR_CODE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String logMsg = intent.getStringExtra("SCAN_RESULT");
				log(logMsg);
			}
		}
	}
	private void log(String qrCode) {
		Intent intent = new Intent("ch.appquest.intent.LOG");
		 
		if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
			Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
			return;
		}
		 
		intent.putExtra("ch.appquest.taskname", "MagnetSensor");
		intent.putExtra("ch.appquest.logmessage", qrCode);
		 
		startActivity(intent);
	}

}
