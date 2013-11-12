package ch.wicki.groessenmesser;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class StartActivityGM extends Activity {
	private double CalculateValueA;
	private float CalculateValueBeta;
	private float CalculateValueAlpha;
	private double b;
	private double c;
	private static final int ANZKOMMA = 1000;
	private static final int SCAN_QR_CODE_REQUEST_CODE = 0;
	private static final int PICTURE_INFORMATION = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_activity_gm);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start_activity_gm, menu);
		return true;
	}
	
	public void openCameraIntent(View v){
		Intent intentForPictureActivity = new Intent(this, PictureActivity.class);
		startActivityForResult(intentForPictureActivity, PICTURE_INFORMATION);
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode == SCAN_QR_CODE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String logMsg = data.getStringExtra("SCAN_RESULT");
				log(logMsg);
			}
		}else if(requestCode == PICTURE_INFORMATION){
			Bundle daten = data.getExtras();
			if(daten.getBoolean("Check")){
				float[] OriVal1 = daten.getFloatArray("OriVal1");
				float[] OriVal2 = daten.getFloatArray("OriVal2");
				if(OriVal1[2]<0)
					OriVal1[2] *= -1;
				if(OriVal1[1]<0)
					OriVal1[1]*=-1;
				if(OriVal2[2]<0)
					OriVal2[2] *= -1;
				if(OriVal2[1]<0)
					OriVal2[1]*=-1;
				if(OriVal1[1] - 80 > 0)
					OriVal1[2] = 180-OriVal1[2];
				if(OriVal2[1] - 80 > 0)
					OriVal2[2] = 180-OriVal2[2];
				if(OriVal2[2] > OriVal1[2]){
					CalculateValueBeta = OriVal2[2] - OriVal1[2];
					CalculateValueAlpha = OriVal1[2];
				}
				else{
					CalculateValueBeta = OriVal1[2] - OriVal2[2];
					CalculateValueAlpha = OriVal2[2];
				}
		    	EditText setEditTextShowBeta = (EditText)findViewById(R.id.editTextAfterGetIt);
		    	setEditTextShowBeta.setText(Math.floor(CalculateValueBeta * ANZKOMMA) / ANZKOMMA +"°");
		    	EditText setEditTextShowAlpha = (EditText)findViewById(R.id.EditTextAlpha);
		    	setEditTextShowAlpha.setText(Math.floor(CalculateValueAlpha * ANZKOMMA) / ANZKOMMA +"°");
		    	
	
		    	EditText getEditTextValueA = (EditText)findViewById(R.id.inputNumberLineA);
		    	if(getEditTextValueA.getText().equals(null))
		    		calcAndSet();
			}
		}
	}
	private double calcC(double m_alpha, double m_gamma, double m_a){
		return(m_a * Math.sin(Math.PI * m_gamma / 180) / Math.sin(Math.PI * m_alpha / 180));
	}
	private double calcB(double m_alpha, double m_beta, double m_c){
		return(m_c * Math.sin(Math.PI * m_beta / 180) / Math.sin((180 - m_alpha-m_beta) * Math.PI / 180));
	}
	private void calcAndSet(){
    	EditText getEditTextValueA = (EditText)findViewById(R.id.inputNumberLineA);
    	if(!(getEditTextValueA.getText().toString().matches(""))){
        	CalculateValueA = Double.parseDouble(getEditTextValueA.getText().toString());
        	if(CalculateValueBeta > 0){
    	    	c = calcC(CalculateValueAlpha,90, CalculateValueA);
    	    	b = calcB(CalculateValueAlpha, CalculateValueBeta, c);
    	    	EditText editTextTextView = (EditText)findViewById(R.id.heightsolution);
    	    	editTextTextView.setText(Math.floor(b * ANZKOMMA) / ANZKOMMA + " m");
        	}    		
    	}
	}
	
	public void onClickCalculateButton(View v){
		calcAndSet();
	}
	
	public boolean onClickLogMenu(MenuItem item){
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, SCAN_QR_CODE_REQUEST_CODE);
		return false;		
	}
	private void log(String qrCode) {
		Intent intent = new Intent("ch.appquest.intent.LOG");
		 
		if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
			Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
			return;
		}
		 
		intent.putExtra("ch.appquest.taskname", "Grössen Messer");
		CharSequence calculatedObjectHeight = b+"";
		intent.putExtra("ch.appquest.logmessage", qrCode + ": " + calculatedObjectHeight);
		 
		startActivity(intent);
	}
	
}
