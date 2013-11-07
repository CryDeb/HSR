package ch.wicki.schatzkarte;

import java.io.File;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;

public class SKActivity extends Activity implements LocationListener {
	private org.osmdroid.views.MapView map;
	private IMapController controller;
	private LocationManager locationManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			setContentView(R.layout.activity_sk);
			map = (org.osmdroid.views.MapView) findViewById(R.id.MapShower);
			map.setTileSource(TileSourceFactory.MAPQUESTOSM);
			map.setMultiTouchControls(true);
			map.setBuiltInZoomControls(true);
			controller = map.getController();
			controller.setZoom(18);
			XYTileSource treasureMapTileSource = new XYTileSource("mbtiles", ResourceProxy.string.offline_mode, 1, 20, 256, ".png", "http://example.org/");
			File file = new File(Environment.getExternalStorageDirectory(), "hsr.mbtiles");
			MapTileModuleProviderBase treasureMapModuleProvider = new MapTileFileArchiveProvider(new SimpleRegisterReceiver(this),
			treasureMapTileSource, new IArchiveFile[] { MBTilesFileArchive.getDatabaseFileArchive(file) });
			MapTileProviderBase treasureMapProvider = new MapTileProviderArray(treasureMapTileSource, null,
			new MapTileModuleProviderBase[] { treasureMapModuleProvider });
			TilesOverlay treasureMapTilesOverlay = new TilesOverlay(treasureMapProvider, getBaseContext());
			treasureMapTilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
			map.getOverlays().add(treasureMapTilesOverlay);
	        
	        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);
        }else{
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("There Is no GPS Sender active, Please activate it");
        	builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
        		public void onClick(DialogInterface dialog, int id){
        			dialog.dismiss();
        			finish();
        		}
        	});
        	AlertDialog dialog = builder.create();
        	dialog.show();
        }
	}

	public void onResume(){
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}
	public void onPause(){
		super.onPause();
		locationManager.removeUpdates(this);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sk, menu);
		
		return true;
	}

	@Override
	public void onLocationChanged(Location pGeoLocation) {
		IGeoPoint iPoint;
		controller.setCenter((IGeoPoint) pGeoLocation);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}
