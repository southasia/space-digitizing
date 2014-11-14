package com.khajenasir.spacedigitizing;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class Navigate_buildingfinder extends MapActivity 
{
	int buildingID, FloorNUM;
	private DatabaseHelper db=null;
	private LocationManager mgr=null;
	double _lat, _lon,_alt, cache_lat=0, cache_lon=0;

	@Override
	protected boolean isRouteDisplayed() 
	{
		return false;
	}
	
	private MapView map=null;
	private MyLocationOverlay me=null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigate_buildingfinder);
        
    	buildingID = getIntent().getExtras().getInt("BuildingID");
    	FloorNUM = getIntent().getExtras().getInt("FloorNUM");
    	
    	db = new DatabaseHelper(this);
    	
		mgr=(LocationManager)getSystemService(LOCATION_SERVICE);
               
		map=(MapView)findViewById(R.id.map);
		
		//Tehran Location
		map.getController().setCenter(getPoint(35.6719444, 51.4244444));
		
		map.getController().setZoom(15);
		map.setBuiltInZoomControls(true);
		
		Drawable marker=getResources().getDrawable(R.drawable.marker);
		
		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
		marker.getIntrinsicHeight());
		
		map.getOverlays().add(new SitesOverlay(marker));
		
		me=new MyLocationOverlay(this, map);
		
		
		map.getOverlays().add(me);
		
		
    }
    
	@Override
	public void onResume() {
		super.onResume();
		
		mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10,	onLocationChange);
		
		me.enableCompass();
	}		
	
	@Override
	public void onPause() {
		super.onPause();
		mgr.removeUpdates(onLocationChange);
		me.disableCompass();
	}
	

	LocationListener onLocationChange=new LocationListener() {
		public void onLocationChanged(Location location) 
		{
			_lat=location.getLatitude();
			_lon=location.getLongitude();
			_alt=location.getAltitude();
			
			map.getController().setCenter(getPoint(_lon, _lat));
			
			//calculte distance
			String[] select = {"ID","X","Y"};
			Cursor cursor = db.SelectTable("tbl_building", select, "ID>=0 and ID=" + buildingID, "ID");
			cursor.moveToFirst();
			

			if(     ((cursor.getDouble(1) - _lat)*(cursor.getDouble(1) - _lat)) + ((cursor.getDouble(2) - _lon)*(cursor.getDouble(2) - _lon)) < 0.000001     )
			{
				Intent buildingFinder = new Intent(Navigate_buildingfinder.this, Navigate_floorfinder.class);
				buildingFinder.putExtra("FloorNUM", FloorNUM);
				buildingFinder.putExtra("BuildingID", buildingID);
    			startActivity(buildingFinder);
    			finish();
			}
			
		}
		
		public void onProviderDisabled(String provider) 
		{
			// required for interface, not used
		}
		
		public void onProviderEnabled(String provider) 
		{
			// required for interface, not used
		}
		
		public void onStatusChanged(String provider, int status, Bundle extras) 
		{
			// required for interface, not used
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
 	{
		if (keyCode == KeyEvent.KEYCODE_S) 
		{
			map.setSatellite(!map.isSatellite());
			return(true);
		}
		else if (keyCode == KeyEvent.KEYCODE_Z) 
		{
			map.displayZoomControls(true);
			return(true);
		}
		
		return(super.onKeyDown(keyCode, event));
	}

	private GeoPoint getPoint(double lat, double lon)
	{
		return(new GeoPoint((int)(lat*1000000.0), (int)(lon*1000000.0)));
	}
		
	private class SitesOverlay extends ItemizedOverlay<OverlayItem> 
	{
		private List<OverlayItem> items=new ArrayList<OverlayItem>();
		//private Drawable marker=null;
		
		public SitesOverlay(Drawable marker) 
		{
			super(marker);
			//this.marker=marker;
			
			boundCenterBottom(marker);
			
			//Load Buildings
			String[] select = {"ID","Name","Shape","X","Y"};
			Cursor cursor = db.SelectTable("tbl_building", select, "ID>=0", "ID");
			cursor.moveToFirst();
			
	        for(int i=0;i<cursor.getCount();i++)
	        { 

				items.add(new OverlayItem(getPoint(cursor.getDouble(3), cursor.getDouble(4)), cursor.getString(2), cursor.getString(1)));

	        	cursor.moveToNext();
	        }

			populate();
		}
		
		@Override
		protected OverlayItem createItem(int i) 
		{
			return(items.get(i));
		}
		
		@Override
		protected boolean onTap(int i) 
		{
			Toast.makeText(Navigate_buildingfinder.this, items.get(i).getSnippet(), Toast.LENGTH_SHORT).show();
			
			return(true);
		}
		
		@Override
		public int size() 
		{
			return(items.size());
		}
	}

}
