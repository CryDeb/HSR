package ch.wicki.groessenmesser;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressLint("NewApi")
public class PictureActivity extends Activity implements SurfaceHolder.Callback, SensorEventListener{
	private Camera camera;
	private SurfaceHolder cameraViewHolder;
	
	private SensorManager sm;
	private Sensor OrientationSensor;
	private float[] OrientationMom;
	private float[] OrientRet1;
	private boolean checkToClose = true;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		OrientationSensor = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	}
 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.picture, menu);
		return true;
	}
	
	protected void onPause(){
		super.onPause();
		
		if(camera != null){
			camera.stopPreview();
			camera.release();
		}
		
		sm.unregisterListener(this);
	}
	
	@SuppressWarnings("deprecation")
	protected void onResume(){
		super.onResume();
		
		SurfaceView cameraView = (SurfaceView) findViewById(R.id.SurfaceViewForPicture);
		cameraViewHolder = cameraView.getHolder();
		cameraViewHolder.addCallback(this);
		cameraViewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		if(OrientationSensor != null){
			sm.registerListener(this, OrientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		camera.stopPreview();
		camera.setDisplayOrientation(90);
		Camera.Parameters params = camera.getParameters();
		Camera.Size vorschauGroesse = params.getPreviewSize();
		params.setPreviewSize(vorschauGroesse.width, vorschauGroesse.height);
		camera.setParameters(params);
		try{
			camera.setPreviewDisplay(holder);
		}catch (IOException e){
			Log.d("1", e.getMessage());
		}
		camera.startPreview();
	}

	public boolean onTouchEvent(MotionEvent event){
		boolean returnValue = false;
		if(event.getAction() == MotionEvent.ACTION_UP){
			if(checkToClose){
				OrientRet1 = OrientationMom;
				checkToClose = false;
			}else{
				safeData(OrientRet1, OrientationMom);
				this.finish();
			}
			returnValue = true;
		}else{
			returnValue = super.onTouchEvent(event);
		}
		return returnValue;
	}
	private void safeData(float[] pValue1,float[] pValue2){
		Intent returnValue = new Intent();
		returnValue.putExtra("OriVal1", pValue1);
		returnValue.putExtra("OriVal2", pValue2);
		returnValue.putExtra("Check", true);
		setResult(Activity.RESULT_OK, returnValue);		
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();			
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor == OrientationSensor){
			OrientationMom = event.values.clone();
		}		
	}
	@Override
	public void onBackPressed() {
		Intent returnValue = new Intent();
		returnValue.putExtra("Check", false);
		setResult(Activity.RESULT_OK, returnValue);	
		this.finish();		
	}
	public void onStop(){
		super.onStop();
	}

}
