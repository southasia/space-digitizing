package com.khajenasir.spacedigitizing;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class newBuilding3  extends Activity
{
	private LocationManager mgr=null;
	private DatabaseHelper db = null;
	double _lat;
	double _lon;
	double _alt;
	
	public static final int MENU_REMOVE = Menu.FIRST + 2;
	
	Button BtnNew, BtnFinish;
	ListView list;
	private List<GPSPoint> points = new ArrayList<GPSPoint>();
	String[] floors = null;
	String[] pointsItem;
	ArrayAdapter<String> adaptor;
    
	long buildingID = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newbiulding3);

        BtnNew = (Button)findViewById(R.id.buttonNewPoint);
        BtnFinish = (Button)findViewById(R.id.buttonFinish);
        list = (ListView)findViewById(R.id.listViewPoints);
        
		mgr=(LocationManager)getSystemService(LOCATION_SERVICE);
        
		try 
        {
			points.clear();
            pointsItem = null;
			
        	db = new DatabaseHelper(this);
        	

        	//if(points.size() == 0)
        	adaptor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        	loadpoints();
        	//else
        	//	adaptor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pointsItem);
        	list.setAdapter(adaptor);
    		registerForContextMenu(list);
		} 
        catch (Exception e) 
		{
			e.printStackTrace();
		}
    }
    
    private void loadpoints()
    {
        if(getIntent().getExtras() != null)
        {
        	buildingID = getIntent().getExtras().getInt("BuildingID");
        	//Fill Mode
        	
			String[] select = {"ID","BuildingID","X","Y"};
			Cursor cursor = db.SelectTable("tbl_buildingpoint", select, "BuildingID = " + buildingID, "ID");
			cursor.moveToFirst();
			
	        for(int i=0;i<cursor.getCount();i++)
	        {
	        	GPSPoint newpoint = new GPSPoint();
	        	newpoint.ID = cursor.getInt(0);
	        	newpoint.BuildingID = cursor.getInt(1);
	        	newpoint.X = cursor.getDouble(2);
	        	newpoint.Y = cursor.getDouble(3);
	        	points.add(newpoint);
	        	cursor.moveToNext();
	        }
	        
	        //pointsItem = new String[points.size()];
	        for(int s=0;s<points.size();s++)
	        	adaptor.add(points.get(s).X + " , " + points.get(s).Y);
	        	//pointsItem[s] = points.get(s).X + " , " + points.get(s).Y;
	        
	        //Load Floors
	        floors = getIntent().getExtras().getStringArray("floors");
        }
			  	
    }
    
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) 
	{
		menu.add(Menu.NONE, MENU_REMOVE, Menu.NONE, "Remove");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		final AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

		switch (item.getItemId()) 
		{				
			case MENU_REMOVE:
				
				try
				{
					adaptor.remove(points.get(info.position).X + " , " + points.get(info.position).Y);
					points.remove(info.position);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				return(true);
		}
		
		return(super.onContextItemSelected(item));
	}
    
	public void onResume() 
	{
		super.onResume();
		mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1,	onLocationChange);
	}
	
	@Override
	public void onPause() 
	{
		super.onPause();
		mgr.removeUpdates(onLocationChange);
	}
	
	@Override
    public void onDestroy()
	{
		super.onDestroy();
		
		db.close();
	}
	
	LocationListener onLocationChange=new LocationListener() {
		public void onLocationChanged(Location location) 
		{
			_lon = location.getLongitude();
			_lat = location.getLatitude();
			_alt = location.getAltitude();
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
    
    public void onclick(View v)
    {
    	switch(v.getId()) 
    	{
			case R.id.buttonFinish:
				save();
				finish();
				break;
				
			case R.id.buttonNewPoint:
    			GPSPoint newPoint = new GPSPoint();
    			newPoint.X = _lat;
    			newPoint.Y = _lon;
    			newPoint.H = _alt;
    			points.add(newPoint);
    			adaptor.add(newPoint.X + " , " + newPoint.Y);
    			break;
    	}
    }

    private void save()
    {
		//Save Preference
		SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(this);
		String Name = prefs1.getString("Name", "");
		String Shape = prefs1.getString("Shape", "");
		int CategoryID = prefs1.getInt("CategoryID", -1);
		int ParentID = prefs1.getInt("ParentID", -1);
		
		//Insert Biulding
		String[] fields = {"Name","Shape","CategoryID","ParentID","X","Y","H"};
		String[] values = {Name,Shape,CategoryID + "",ParentID + "",points.get(0).X + "",points.get(0).Y + "",points.get(0).H + ""};
		if(buildingID == -1)
			buildingID = db.InsertRow("tbl_building", fields, values);
		else
			db.UpdateRow("tbl_building", fields, values, "ID=" + buildingID);
		
		//Delete Floors
		db.DeleteRow("tbl_buildingFloor", "buildingID=" + buildingID );
		//Insert Floors
		String[] fields2 = {"BuildingID","Name","Application"};
		for(int i=0;i<floors.length;i++)
		{
			String[] values2 = {buildingID + "", floors[i] ,""};
			db.InsertRow("tbl_buildingFloor", fields2, values2);
		}
		
		//Delete Points
		db.DeleteRow("tbl_buildingPoint", "buildingID=" + buildingID );
		//Insert Points
		String[] fields3 = {"BuildingID","X","Y"};
		for(int i=0;i<points.size();i++)
		{
			String[] values3 = {buildingID + "", points.get(i).X + "", points.get(i).Y + ""};
			db.InsertRow("tbl_buildingPoint", fields3, values3);
		}
		
    }
}
