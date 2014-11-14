package com.khajenasir.spacedigitizing;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

public class Navigate_floorfinder extends Activity  implements SensorEventListener
{
	private SensorManager sensorManager;
	private double x, y, z, cache_x=0,cache_z=0;
	private LocationManager mgr=null;
	double _lat, _lon, _alt;
	private DatabaseHelper db=null;
	GeoHelper geo = new GeoHelper();
	double[] currentPoint;
	int BuildingID, FloorNUM;
	private List<Floor> floors = new ArrayList<Floor>();
	
	ImageButton imgUP,imgMIDDLE,imgDOWN,imgRight,imgLeft;
	double intersectAzimuth = 0, floorNum = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigate_floorfinder);
        
        imgUP = (ImageButton)findViewById(R.id.imageViewUP);
        imgMIDDLE = (ImageButton)findViewById(R.id.imageViewMIDDLE);
        imgDOWN = (ImageButton)findViewById(R.id.imageViewDOWN);
        imgRight = (ImageButton)findViewById(R.id.imageButtonRIGHT);
        imgLeft = (ImageButton)findViewById(R.id.imageButtonLEFT);
        imgUP.setVisibility(View.INVISIBLE);
        imgMIDDLE.setVisibility(View.INVISIBLE);
        imgDOWN.setVisibility(View.INVISIBLE);
        imgRight.setVisibility(View.INVISIBLE);
        imgLeft.setVisibility(View.INVISIBLE);        
        
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mgr=(LocationManager)getSystemService(LOCATION_SERVICE);

    	FloorNUM = getIntent().getExtras().getInt("FloorNUM");
    	BuildingID = getIntent().getExtras().getInt("BuildingID");
        
    	db = new DatabaseHelper(this);
        
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        
		mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1,	onLocationChange); 
		
//		sensorManager.registerListener(this,
//				sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
//                SensorManager.SENSOR_DELAY_NORMAL);
		
		//sensorManager.registerListener(this,
		//		sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
        //        SensorManager.SENSOR_DELAY_NORMAL);
		
		sensorManager.registerListener(this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_NORMAL);
		
			loadfloors();
			loadAzimuth();
    }
    
	void loadfloors()
	{
		try
		{
		
		floors.clear();
		
		String[] select = {"ID","BuildingID","Name"};
		Cursor cursor = db.SelectTable("tbl_buildingfloor", select, "BuildingID=" + BuildingID, "ID");
		cursor.moveToFirst();
		
		String[] select2 = {"ID","X","Y","H"};
		Cursor cursorBuildng = db.SelectTable("tbl_building", select2, "ID=" + BuildingID, "ID");
		cursorBuildng.moveToFirst();
		
		double[] BuildingPoint = geo.convert_Geodetic_To_Cartesian(cursorBuildng.getDouble(1), cursorBuildng.getDouble(2), cursorBuildng.getDouble(3));
		double BuidingX =  BuildingPoint[0];
		double BuidingY = BuildingPoint[1];
		double BuidingH = BuildingPoint[2];
		double deltaH=BuidingH -1300/*currentPoint[2]*/;
		double Dp = Math.pow((Math.pow((BuidingY - currentPoint[1]),2)+Math.pow((BuidingX - currentPoint[0]),2)), 0.5);
		floorNum=1;
		
		cache_x = (double)(cache_x * (-1) * Math.PI)/180;
		if(deltaH > 0)
		{
			double total_H=(double)(Dp * Math.tan(cache_x));
			double delta_D=total_H-deltaH;
			floorNum = Math.ceil((double)(delta_D/3));
		}
		else if(deltaH<0)
		{
			double H_BU=BuidingH+((double)(3*cursor.getCount()));
			double deltaH_U=H_BU-1553/*currentPoint[2]*/;
			double deltaH_B=H_BU-BuidingH;
			if(deltaH_U > 0)
			{
				double delta_U=(double)(Dp * Math.tan(cache_x));
				floorNum=Math.ceil((double)((delta_U-deltaH)/3));
				
			}
			else
			{
				double delta_UD=(double)(Dp * Math.tan(cache_x));
				double delta_UD_2=delta_UD-deltaH_U;
				floorNum=Math.ceil((double)((delta_UD_2+deltaH_B)/3));
			}
		}
		else if(deltaH==0)
		{
			double delta_lev=(double)(Dp * Math.tan(cache_x));
			floorNum=Math.ceil((double)(delta_lev/3));
		}

		
	    imgUP.setVisibility(floorNum < FloorNUM ? View.VISIBLE : View.INVISIBLE);
	    imgDOWN.setVisibility(floorNum > FloorNUM ? View.VISIBLE : View.INVISIBLE);

	    imgMIDDLE.setVisibility((floorNum == FloorNUM /*&& Math.abs(intersectAzimuth - cache_z) <=4 */) ? View.VISIBLE : View.INVISIBLE);
	    
	}
	catch(Exception ex)
	{
		ex.printStackTrace();
	}
		
	}
	
	void loadAzimuth()
	{
		try
		{
			//cache_z
			
			String[] select2 = {"ID","X","Y","H"};
			Cursor cursorBuildng = db.SelectTable("tbl_building", select2, "ID=" + BuildingID, "ID");
			cursorBuildng.moveToFirst();
			
			double[] buildingCoordiante = geo.convert_Geodetic_To_Cartesian(cursorBuildng.getDouble(1), cursorBuildng.getDouble(2), cursorBuildng.getDouble(3));
			double[] currentCoordiante = geo.convert_Geodetic_To_Cartesian(_lat, _lon, _alt);
			double m = Math.abs( (double)(buildingCoordiante[1] - currentCoordiante[1])/(buildingCoordiante[0] - currentCoordiante[0]) );
			//double m = (double)(cursorBuildng.getDouble(2) - _lon)/(cursorBuildng.getDouble(1) - _lat);
			intersectAzimuth = Math.atan(m);
			intersectAzimuth = intersectAzimuth * ((double)180/Math.PI);
			
		    imgRight.setVisibility((intersectAzimuth - cache_z) > 4 ? View.VISIBLE : View.INVISIBLE);	
		    imgLeft.setVisibility((cache_z - intersectAzimuth) > 4  ? View.VISIBLE : View.INVISIBLE);	
		    
		    //imgMIDDLE.setVisibility((floorNum == FloorNUM && Math.abs(intersectAzimuth - cache_z) <=4) ? View.VISIBLE : View.INVISIBLE);
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
			
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
				
				if(cache_x == 0)
				{
					cache_x = x;
					loadfloors();
				}
				else if (Math.abs(cache_x - x) > 2)
				{
					cache_x = x;
					loadfloors();
				}
				
				if(cache_z == 0)
				{
					cache_z = z;
					loadAzimuth();
				}
				else if (Math.abs(cache_z - z) > 2)
				{
					cache_z = z;
					loadAzimuth();
				}
				
				
			    //imgMIDDLE.setVisibility((floorNum == FloorNUM && intersectAzimuth == cache_z) ? View.VISIBLE : View.INVISIBLE);
				
			}		
			
		}
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{}
	
	LocationListener onLocationChange=new LocationListener() {
		public void onLocationChanged(Location location) 
		{
			_lat=location.getLatitude();
			_lon=location.getLongitude();
			_alt=location.getAltitude();
			
			currentPoint = geo.convert_Geodetic_To_Cartesian(_lat, _lon, _alt);
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

}
