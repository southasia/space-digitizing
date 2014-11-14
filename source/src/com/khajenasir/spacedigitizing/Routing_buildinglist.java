package com.khajenasir.spacedigitizing;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.SensorEventListener;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class Routing_buildinglist extends Activity implements SensorEventListener
{
	private SensorManager sensorManager;
	//private Sensor sensor;
	private float x, y, z;
	private LocationManager mgr=null;
	double _lat, _lon,_alt, cache_lat=0, cache_lon=0;
	private DatabaseHelper db=null;
	
	ListView list;
	//boolean CanSendData = false;
	ArrayAdapter<String> adaptor;
	private List<Building> buildings = new ArrayList<Building>();
	
	
	
	double Radious = 0.00001; //yek mantaghe tehran
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routing_buildinglist);
        
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mgr=(LocationManager)getSystemService(LOCATION_SERVICE);

		
		list = (ListView)findViewById(R.id.listViewFrontBuilding);
		list.setOnItemClickListener(new OnItemClickListener() 
		{

			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) 
			{
				Intent floorList = new Intent(Routing_buildinglist.this, Routing_floorlist.class);
				floorList.putExtra("BuildingID", buildings.get(position).ID);
    			startActivity(floorList);
			}
		});
        
    	db = new DatabaseHelper(this);
    }
    
    void loadbuilding(Boolean showall)
    {
		try 
		{		
			buildings.clear();
			adaptor.clear();
			
			SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(this);
			String selectedShape = prefs1.getString("SelectedShape", "");
	    	SharedPreferences.Editor editor = prefs1.edit();
	    	editor.putString("SelectedShape", "");
	        editor.commit();
			
			String[] select = {"ID","Name","Shape","CategoryID","ParentID"};
			String where = " ID > 0 AND ((X - " + _lat + ")*(X - " + _lat + ")) + ((Y - " + _lon + ")*(Y - " + _lon + "))< " + Radious + (selectedShape.equals("") ?"":" AND Shape='" + selectedShape + "'");
			Cursor cursor = db.SelectTable("tbl_building", select, where, "ID");
			cursor.moveToFirst();
			
	        for(int i=0;i<cursor.getCount();i++)
	        {
	        	Building newBuilding = new Building();
	        	newBuilding.ID = cursor.getInt(0);
	        	newBuilding.Name = cursor.getString(1);
	        	newBuilding.Shape = cursor.getString(2);
	        	newBuilding.CategoryID = cursor.getInt(3);
	        	buildings.add(newBuilding);
	        	cursor.moveToNext();
	        }
	        
			for(int i = 0; i< buildings.size(); i++)
			{
				String[] selectPoints = {"ID","BuildingID","X","Y"};
				Cursor cursorPoints = db.SelectTable("tbl_buildingpoint", selectPoints, "BuildingID = " + buildings.get(i).ID, "ID");
				cursorPoints.moveToFirst();
				
				List<GPSPoint> points = new ArrayList<GPSPoint>();
				
				for(int j=0;j<cursorPoints.getCount();j++)
				{
					GPSPoint p = new GPSPoint();
					p.X = cursorPoints.getDouble(2);
					p.Y = cursorPoints.getDouble(3);
					points.add(p);
					
					cursorPoints.moveToNext();
				}
					
				boolean addBuilding = false;
				for(int j=0;j<points.size() - 1;j++)
				{
	        		if(hasConflict(z, points.get(j).X, points.get(j).Y, points.get(j+1).X, points.get(j+1).Y))
	        		{
	        			addBuilding = true;
	        			break;
	        		}
				}
				
	        	if(!addBuilding)
	        		continue;
	        		
				adaptor.add(buildings.get(i).Name);
			}
		}  
		catch (Exception e) 
		{
			e.printStackTrace();
		}	
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        
		mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10,	onLocationChange);
		
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_NORMAL);
		
		//sensorManager.registerListener(this,
		//		sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
        //        SensorManager.SENSOR_DELAY_NORMAL);
		
		sensorManager.registerListener(this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
		
    	adaptor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		loadbuilding(true);
    	
    	list.setAdapter(adaptor);
    }
    
	@Override
	public void onPause() 
	{
		super.onPause();
		sensorManager.unregisterListener(this);
        
		mgr.removeUpdates(onLocationChange);
    }
    
	public void onSensorChanged(SensorEvent event) 
	{
		synchronized (this) 
		{	
			if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
			{
				//Movement
				
				/*		float accelationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
				long actualTime = System.currentTimeMillis();
				if (accelationSquareRoot >= 2) //
				{
					if (actualTime - lastUpdate < 200) {
						return;
					}
					lastUpdate = actualTime;
				}*/
			}
			else if(event.sensor.getType() == Sensor.TYPE_ORIENTATION)
			{
				// 0=North, 90=East, 180=South, 270=West
				z = event.values[0];
				//Front Oriention
				x = event.values[1];
				//Side Oriention
				y = event.values[2]; 
				
			}		
			
		}
	}
	
	private boolean hasConflict(double degree, double x1, double y1, double x2, double y2)
	{	
		boolean result = false , pointingDirection = false;
		
		try
		{
			if(cache_lat == 0 || cache_lon == 0)
			{
				cache_lat = _lat;
				cache_lon = _lon;
 			}
			else if (Math.abs(_lat - cache_lat) > 0.0005 || Math.abs(_lon - cache_lon) > 0.0005)
			{
				cache_lat = _lat;
				cache_lon = _lon;
			}
		/*	double[] current_point=geo.convert_Geodetic_To_Cartesian(cache_lat, cache_lon, _alt);
			cache_lat=current_point[0];
			cache_lon=current_point[1];
			double[] startPoint= geo.convert_Geodetic_To_Cartesian(x1, y1, 0.0);
			x1=startPoint[0];
			y1=startPoint[1];
			double[] endPoint= geo.convert_Geodetic_To_Cartesian(x2, y2, 0.0);
			x2=endPoint[0];
			y2=endPoint[1];
			*/
			
			
			
			if(degree >= 0 && degree < 90)
			{
				if(   (x1 > cache_lat && y1> cache_lon)     ||    (x2 > cache_lat && y2> cache_lon) )
					pointingDirection = true;
			}
			else if(degree > 90 && degree <= 180)
			{
				if(    (x1 < cache_lat && y1 > cache_lon)     ||    (x2 < cache_lat && y2 > cache_lon))
					pointingDirection = true;
			}
			else if(degree > 180 && degree < 270)
			{
				if(    (x1 < cache_lat && y1< cache_lon)     ||    (x2 < cache_lat && y2< cache_lon))
					pointingDirection = true;
			}
			else if(degree > 270 && degree < 360)
			{
				if(  (x1 > cache_lat && y1< cache_lon)     ||    (x2 > cache_lat && y2< cache_lon)  )
					pointingDirection = true;
			}
			
			if(pointingDirection)
			{
				Vertex Line1_StartVertex = new Vertex(cache_lat,cache_lon);
				Vertex Line2_StartVertex = new Vertex(x1,y1);
				Vertex Line2_EndVertex = new Vertex(x2,y2);
				
		        double M1 = 0, M2 = 0, Conflict_X, Conflict_Y;
		        Boolean M1_IsInfinite = false, M2_IsInfinite = false;
		        
		        if (degree == 90 || degree == 270)
		            M1_IsInfinite = true;
		        else
		        {
		        	degree = (double)(degree * Math.PI)/180;
		        	M1 = Math.tan(degree);
		        }
	
		        if (Line2_EndVertex.X == Line2_StartVertex.X )
		            M2_IsInfinite = true;
		        else
		            M2 = (double) (y2 - y1)/(x2 - x1);
	
		        //region Check All States of M & Calculate X,Y Conflict Point
		        if (M1_IsInfinite && M2_IsInfinite)
		        {
		        	result = false;
		            return result;
		        }
		        else if (M1_IsInfinite)
		        {
		            Conflict_X = Line1_StartVertex.X;
		            Conflict_Y = (M2 * (Conflict_X - Line2_StartVertex.X)) + Line2_StartVertex.Y;
		        }
		        else if (M2_IsInfinite)
		        {
		            Conflict_X = Line2_StartVertex.X;
		            Conflict_Y = (M1 * (Conflict_X - Line1_StartVertex.X)) + Line1_StartVertex.Y;
		        }
		        else if (M1 == M2)
		        {
		        	result = false;
		            return result;
		        }
		        else
		        {
		            Conflict_X = (double)((M1 * Line1_StartVertex.X) - (M2 * Line2_StartVertex.X) + (Line2_StartVertex.Y) - (Line1_StartVertex.Y)) / (M1 - M2);
		            Conflict_Y = (M1 * (Conflict_X - Line1_StartVertex.X)) + Line1_StartVertex.Y;
		        }
	
		        if (IsPointOnLine(Conflict_X, Conflict_Y, Line2_StartVertex, Line2_EndVertex))
		        {
		        	result = true;
		            return result;
		        }
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}
    
    private boolean IsPointOnLine(double Conflict_X, double Conflict_Y, Vertex Line_StartVertex, Vertex Line_EndVertex)
    {
        if (Line_StartVertex.X <= Line_EndVertex.X && Line_StartVertex.Y <= Line_EndVertex.Y)
        {
            if (Conflict_X >= Line_StartVertex.X &&
                Conflict_X <= Line_EndVertex.X &&
                Conflict_Y >= Line_StartVertex.Y &&
                Conflict_Y <= Line_EndVertex.Y)
                return true;
        }
        else if (Line_StartVertex.X >= Line_EndVertex.X && Line_StartVertex.Y <= Line_EndVertex.Y)
        {
            if (Conflict_X <= Line_StartVertex.X &&
                Conflict_X >= Line_EndVertex.X &&
                Conflict_Y >= Line_StartVertex.Y &&
                Conflict_Y <= Line_EndVertex.Y)
                return true;
        }
        else if (Line_StartVertex.X <= Line_EndVertex.X && Line_StartVertex.Y >= Line_EndVertex.Y)
        {
            if (Conflict_X >= Line_StartVertex.X &&
                Conflict_X <= Line_EndVertex.X &&
                Conflict_Y <= Line_StartVertex.Y &&
                Conflict_Y >= Line_EndVertex.Y)
                return true;
        }
        else if (Line_StartVertex.X >= Line_EndVertex.X && Line_StartVertex.Y >= Line_EndVertex.Y)
        {
            if (Conflict_X <= Line_StartVertex.X &&
                Conflict_X >= Line_EndVertex.X &&
                Conflict_Y <= Line_StartVertex.Y &&
                Conflict_Y >= Line_EndVertex.Y)
                return true;
        }

        return false;
    }

/*	public boolean IsInBuilding(double lat,double lon, int BuildingID)
	{
		boolean result = false;
		String[] select = {"ID","BiuldinID","X","Y"};
		String where = " BiuldinID =" + BuildingID;
		Cursor cursor = db.SelectTable("tbl_buildingpoint", select, where, "ID");
		cursor.moveToNext();
		
		for(int j=0;j<cursor.getCount() - 1;j++)
		{
    		if(hasConflict(z, points.get(j).X, points.get(j).Y, points.get(j+1).X, points.get(j+1).Y))
    		{
    			addBuilding = true;
    			break;
    		}
		}
		
		
		
		
		
		return result;
	}*/
    
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{}
	
	LocationListener onLocationChange=new LocationListener() {
		public void onLocationChanged(Location location) 
		{
			_lon=location.getLongitude();
			_lat=location.getLatitude();
			_alt=location.getAltitude();
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
	
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.buttonRefresh:
				loadbuilding(true);
				break;
				
			case R.id.buttonSelect:
				if(buildings.size() > 0)
				{
					Intent i = new Intent().setClass(this, Routing_buildingselect.class);
    				startActivity(i);
    			}
				
				break;
				
			case R.id.buttonShowAll:
				loadbuilding(true);
				break;
		
		}
	}
	

	 	public class Vertex
	    {
	        public double X;
	        public double Y;
	        
	        public Vertex(double x, double y)
	        {
	        	X=x;
	        	Y=y;
	        }
	    }
	
}
